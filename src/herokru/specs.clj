(ns herokru.specs
  (:require [clojure.spec.alpha :as s])
  (:import org.bson.types.ObjectId))

(def coll-name "users")

(s/def ::name string?)
(s/def ::domains (s/coll-of string?))
(s/def ::image string?)
(s/def ::running boolean?)
(s/def ::env (s/map-of string? string?))
(s/def ::partial-app (s/keys :opt-un [::name ::domains ::image ::runing ::env]))
(s/def ::app-create (s/keys :req-un [::image]
                            :opt-un [::name ::domains ::runing ::env]))

(def partial-app-keys [:name, :domains :image :running :env])
(defn partial-app-defaults [map]
  (let [id (ObjectId.)
        name (str id)]
    (into {:_id id
           :name name
           :domains [name]
           :running true
           :env {}}
    map)))
;;(s/valid? ::partial-app {:env {"foo" "baz"}})

(s/def ::newpass string?)
(s/def ::app-limit integer?)
(s/def ::admin boolean?)
(s/def ::partial-user (s/keys :opt-un [::email ::newpass]))
(s/def ::partial-user-admin (s/keys :opt-un [::email ::newpass
                                             ::app-limit ::admin]))
(def partial-user-keys [:email :newpass])
(def partial-user-admin-keys [:email :newpass :app-limit :admin])

(defn partial-user-defaults [map]
  (into {:app-limit 2
         :admin false}
        map))
