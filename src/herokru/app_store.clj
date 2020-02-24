(ns herokru.app-store
  (:require [monger.collection :as mc]
            [monger.core :as mg]
            [monger.operators :refer :all]
            [herokru.specs :as hs :refer [coll-name]])
  (:import org.bson.types.ObjectId))

(defn $set-app [updates]
  (str "restructure app ditionary for $set in mongodb"
       "ex {:name \"foo\" :image \"python\"} -> "
       "{\"apps.$.name\" \"foo\" \"apps.$.image\" \"\"}") 
  (into {} (->> hs/partial-app-keys
                (map (fn [k] [(str "apps.$." (name k)) (updates k)]))
                (remove (fn [[_ v]] (nil? v)))
                )))
;; ($set-app {:name "f" :domains ["f" "ff"]})

(defprotocol AppStore
  (create-app [this partial-app])
  (list-apps [this])
  (get-app [this id])
  (update-app [this id partial-app])
  (delete-app [this id]))


(defn user-apps [user-id & extra]
  (into [{$match {:_id user-id}}
         {$unwind "$apps"}
         ;; newer operators not defined by monger so strings will do
         {"$addFields" {:apps.userId user-id}}
         {"$replaceRoot" {:newRoot "$apps"}}]
        extra))

(deftype MongoAppStore [db user-id] ;;TODO doc "given a user mongodb collection do app store stuff"
  AppStore
  (create-app [this partial-app]
    (mc/update-by-id db coll-name user-id {$push {:apps partial-app}}))
  (list-apps [this]
    (mc/aggregate db coll-name (user-apps user-id) :cursor {}))
  (get-app [this id]
    (mc/aggregate db coll-name (user-apps user-id {$match {:_id id}})
                  :cursor {}))
  (update-app [this id partial-app] db coll-name
    {:apps {$elemMatch {:_id id}}}
    {$set ($set-app partial-app)})
  (delete-app [this id]
    (mc/update-by-id db coll-name user-id {$pull {:apps {:_id id}}})))

;; (def db  (mg/get-db (mg/connect) "herokrut"))
;; (def user-id (ObjectId. "5e51f77dc94830365a67e065"))
;; (def tc (MongoAppStore. db user-id ))
;; (mc/aggregate db coll-name (user-apps user-id) :cursor {})
;; (create-app tc (hs/partial-app-defaults {:name "foo" :domains ["foo"]}))
;; (list-apps tc)
