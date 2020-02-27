(ns herokru.docker-client
  (:require [clj-docker-client.core :as docker]))

(defprotocol DockerClient
  (create
    [this user-id partial-app]
    [this user-id partial-app options])
  (delete [this container-id])
  (start [this container-id])
  (stop [this container-id])
  (restart [this container-id])
  (logs [this container-id])
  (status [this container-id]))

(deftype DockerClientNoVolumes [images containers]
  DockerClient
  (create [this user-id partial-app] (create this user-id partial-app {}))
  (create [this user-id partial-app options]
    (let [cont-name (str user-id "-" (partial-app :name))
          image-name (partial-app :image)
          image (docker/invoke images ;; TODO make cronjob that cleans out unused images
                           {:op     :ImageCreate
                            :params {:fromImage (partial-app :image)}})]
      (if (image :message) ;; check to make sure image successfuly pulled
        image
        (docker/invoke containers
                       {:op     :ContainerCreate
                        :params {:name cont-name
                                 :body (into {:Image image-name}
                                             options)}}))))
  (delete [this container-id]
    (docker/invoke containers {:op :ContainerDelete
                               :params {:id container-id
                                        :force true}}))
  (start [this container-id]
    (docker/invoke containers {:op :ContainerStart
                               :params {:id container-id}}))
  (stop [this container-id]
    (docker/invoke containers {:op :ContainerStop
                               :params {:id container-id}}))
  (restart [this container-id]
    (docker/invoke containers {:op :ContainerRestart
                               :params {:id container-id}}))
  (logs [this container-id]
    (docker/invoke containers {:op :ContainerLogs
                               :params {:id container-id
                                        :stdout true
                                        :stderr true}}))
  (status [this container-id]
    (let [container (docker/invoke containers {:op :ContainerInspect
                                               :id container-id})]
      (if (container :message)
        (container :message)
        (container :Status)))))

;; example client lifecycle
;; (def conn (docker/connect {:uri "unix:///var/run/docker.sock"}))
;; (def containers (docker/client {:category :containers
;;                                 :conn conn
;;                                 :api-version "v1.39"}))
;; (def images (docker/client {:category :images
;;                             :conn     conn
;;                             :api-version "v1.39"}))

;; (def dc (DockerClientNoVolumes. images containers))
;; (def cont (create dc "me" {:image "centos:6" :name "blerf"}
;;                 {:Cmd ["/bin/bash" "-c" "while [ 1 ]; do echo working; sleep 1; done"]}))
;; (def startres (start dc "me-blerf"))
;; (def logsres (logs dc "me-blerf"))
;; (print logsres)
;; (def stopres (stop dc "me-blerf"))
;; (delete dc "me-blerf")

;; scratchpad
;; (docker/ops containers)
;; (docker/invoke containers)
;; (docker/ops images)
;; (docker/doc containers :ContainerCreate)


(defn image-chk [image-client name]
  (docker/invoke image-client
                 {:op     :ImageInspect
                  :params {:name name}}))

;; this function isn't actually used
;; confusingly when you call ImageCreate the docker api just responds with "pulling
;; image <your image>" it doesn't specify if the pull succeeded, so i wrote this
;; retry function to check to make sure the container is pulled. however it seems
;; like the api will block to make sure it pulled succesfully before returning?
;; the docker cli doesn't implement any retry logic to make sure its pulled so
;; just calling CreateImage should be fine
(defn ensure-image-pulled [image-client name]
  "if image not locally pulled, pull it and then check to make sure the pull suceeded"
  (let [image (image-chk images name)]
    (if (nil? (image :message))
      image
      ;;else pull image and retry if the image doesn't exist
      (do (docker/invoke images ;; TODO make cronjob that cleans out unused images
                           {:op     :ImageCreate
                            :params {:fromImage name}})
            (loop [attempts-left 5
                   image (image-chk images name)]
              (if (nil? (image :message))
                image
                ;; else keep retrying
                (if (<= 0 attempts-left)
                  {:message "error retries exceeded, image pulling took to long"}
                  (recur (dec attempts-left)
                         @(delay (image-chk images name)
                                 75))))))))) ;; 75ms retries
