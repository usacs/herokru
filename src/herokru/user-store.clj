(ns herokru.user-store)

(defprotocol UserStore
  (create [this partial-user] "creates new user")
  (uget [this id] "gets a sepcific user")
  (update [this id partial-user])
  (disable [this id] "marks a user as disabled")
  (users-apps [this id] "lists a users apps")
  (all-apps [this query] (str "list all apps. query in the form of"
                              "{running:undef|true|false, users:[id]}"))
  (list [this] "lists all users"))

(deftype MongoUserStore [db])
