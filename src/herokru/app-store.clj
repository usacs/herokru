(ns herokru.app-store)

(defprotocol AppStore
  (create [this partial-app])
  (list [this])
  (aget [this id])
  (update [this id partial-app])
  (delete [this id])
  (exec [this id command])
  (exec [this id logs]))
