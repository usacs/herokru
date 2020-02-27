(ns herokru.docker-client
  (:require [clj-docker-client.core :as docker]))

(def conn (docker/connect {:uri "unix:///var/run/docker.sock"}))

(defprotocol DockerClient
  (create [this user-id partial-app])
  (update [this container-id artial-app])
  (delete [this container-id])
  (start [this container-id])
  (stop [this container-id])
  (restart [this container-id]))

(deftype DockerClientNoVolumes [images containers]
  DockerClient
  (create [this user-id partial-app]
    (let [image (docker/invoke images
                               {:op     :ImageCreate
                                :params {:fromImage (partial-app :image)}})]
      (if (image :Id) ;; check to make sure image successfuly pulled
        (docker/invoke containers
                       {:op     :ContainerCreate
                        :params {:name (str user-id "-" (partial-app :name))
                                 :body {:Image (partial-app :image)}}})
        image)
  (update [this container-id artial-app] nil)
  (delete [this container-id] nil)
  (start [this container-id] nil)
  (stop [this container-id] nil)
  (restart [this container-id]) nil)

(def containers (docker/client {:category :containers
                                :conn conn
                                :api-version "v1.39"}))
(docker/ops containers)

(docker/invoke containers)

(def images (docker/client {:category :images
                            :conn     conn
                            :api-version "v1.39"}))

(docker/ops images)
(docker/doc images :ImageHistory)

(docker/invoke images {:op     :ImageCreate
                       :params {:fromImage "busybox:musl"}})
(def response (docker/invoke images {:op     :ImageInspect
                                     :params {:name "busybox:musl88"}}))
(keys response)

(docker/invoke containers
               {:op     :ContainerCreate
                :params {:name "conny2"
                         :body {:Image "busybox:musl"
                                :Cmd   ["sh"
                                        "-c"
                                        "i=1; while :; do echo $i; sleep 1; i=$((i+1)); done"]}}})
