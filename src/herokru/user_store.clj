(ns herokru.user-store
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.spec.alpha :as s]
            [monger.operators :refer :all]
            [herokru.specs :as hs :refer [coll-name]]
            [herokru.app-store :as a])
  (:import org.bson.types.ObjectId))
;; TODO unrelated users theoretically have infinite @rutgers.edu emails \
;; maybe only allow @scarletmail.rutgers.edu emails
(defprotocol UserStore
  (create-user [this partial-user] "creates new user")
  (get-user [this id] "gets a sepcific user")
  (update-user [this id partial-user])
  (delete-user [this id] "deletes a user and apps from db (not cluster)")
  (users-apps-store [this id] "returns store for working with a users apps")
  (all-apps [this query] (str "list all apps. query in the form of"
                              "{running:undef|true|false, users:[id]}"))
  (list-users [this] "lists all users"))

(deftype MongoUserStore [db]
  UserStore
  (create-user [this partial-user]
    (mc/insert db coll-name partial-user))
  (get-user [this id]
    (mc/find-one-as-map db coll-name {:_id id}))
  (update-user [this id partial-user] ;; should be used with select-keys
    (mc/update-by-id db coll-name id {$set partial-user}))
  (delete-user [this id]
    (mc/remove-by-id db coll-name id))
  (users-apps-store [this id] (a/->MongoAppStore db id))
  (all-apps [this query]
    (mc/aggregate db coll-name [{$unwind "$apps"}
                                {"$addFields" {:apps.userId "$_id"}}
                                {"$replaceRoot" {:newRoot "$apps"}}]
                  :cursor {}))
  (list-users [this]
    (mc/find-maps db coll-name)))

;; (def db  (mg/get-db (mg/connect) "herokrut"))
;; (def id (ObjectId. "5e53429903e3047fc9ddfdbc"))
;; (def mus (MongoUserStore. db))
;; (create-user mus (hs/partial-user-defaults {:_id "5e53429903e3047fc9ddfdbc" :email "t@t.co" :pass "fwef"}))
;; (get-user mus id)
;; (update-user mus id {:appLimit 5})
;; (def as (users-apps-store mus id))
;; (a/create-app as (hs/partial-app-defaults {:name "foo2" :domains ["foo2"]}))
;; (all-apps mus {})
;; (list-users mus)
;; (delete-user mus id)
