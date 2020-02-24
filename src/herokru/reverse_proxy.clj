(ns heroku.reverse-proxy
  (:require [clj-http.client :as client])
  (:use [slingshot.slingshot :only [try+]]))

(defprotocol reverse-proxy-service
  (rget [this vfrom] "gets a vhost mapping")
  (rput [this vfrom vto] "adds a mapping from vfrom to vto")
  (rdelete [this vfrom] "removes a vhost mapping")
  (rlist [this] "list out all vhost mappings"))

(deftype MemProxy [map-atom]
  reverse-proxy-service
  (rget [this vfrom] (@map-atom vfrom))
  (rput [this vfrom vto] (swap! map-atom assoc vfrom vto))
  (rdelete [this vfrom] (swap! map-atom dissoc vfrom))
  (rlist [this] @map-atom))

;; (def p (MemProxy. (atom {})))
;; (rget p "lmao")
;; (rput p "lmao" "f")
;; (rlist p)
;; (rdelete p "lmao")


(deftype StupidProxy [base-url]
  reverse-proxy-service
  (rget [this vfrom]
    (try+
      (let [url (str base-url "/vhost/" vfrom)
            res (client/get url)]
        (:body res))
    (catch [:status 404] _ nil)))
  (rput [this vfrom vto]
    (let [url (str base-url "/vhost/" vfrom "/" vto)
          res (client/put url)]
      (:body res)))
  (rdelete [this vfrom]
    (let [url (str base-url "/vhost/" vfrom)
          res (client/delete url)]
      (:body res)))
  (rlist [this]
    (let [url (str base-url "/vhost")
          res (client/get url {:as :json})]
      (:body res))))
;; (def p (StupidProxy. "http://localhost:5001/"))
;; (rlist p)
;; (rget p "lmaot")
;; (rput p "lmaot" "foo")
;; (rdelete p "lmaot")
;; (:body (client/get "http://localhost:5001/vhost"))
