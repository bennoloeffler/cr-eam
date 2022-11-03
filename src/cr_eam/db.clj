(ns cr-eam.db
  (:require  [datahike.api :as d]
             [datahike-jdbc.core]
             [cr-eam.config :as config]
             [cr-eam.example-data :as example]))


(def app-state (atom {:conn nil
                      :cfg nil}))

(def schema
  "The database schema which is transacted when the app starts"
  [{:db/ident :person/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :person/last-name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :person/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

; WORKS!
#_(def cfg (or (config/config-jdbc)
               {:store {:backend :file :path "/tmp/example"}}))

; WORKS!
#_(def cfg {:store {:backend :jdbc
                    :dbtype "postgresql"
                    :host "localhost"
                    :port 5432
                    :user "benno"
                    :password ""
                    :dbname "cream"}})

; postgresql://localhost/mydb?user=other&password=secret
; https://stackoverflow.com/questions/3582552/what-is-the-format-for-the-postgresql-connection-string-url
; WORKS!
#_(def cfg {:store {:backend :jdbc
                    :dbtype "postgresql"
                    :dbname "benno"
                    :jdbc-url "postgresql://localhost:5432/benno?user=benno&password=''"}})

;; example: https://github.com/kommen/datahike-heroku/blob/main/resources/clj/new/datahike_heroku/server.clj

;(def cfg (config/env-db-config))
;(println cfg)
(declare test-db)
(declare add-person!)
(declare all-persons)
(declare delete-db!)
(declare start-db-local)

(comment
  (start-db-local)
  (add-person!)
  (all-persons)
  (delete-db!))


(defn start-db-local []
  ; connect, schema and app-state
  (let [cfg {:store {:backend :file :path "/tmp/example"}}
        _   (when-not (d/database-exists? cfg)(d/create-database cfg))
        conn (d/connect cfg)]
    (d/transact conn schema)
    (swap! app-state assoc :conn conn)
    (swap! app-state assoc :cfg cfg)))


(defn start-db! []
  "Creates a datahike connection and transacts the schema
  If no DATABASE_URL is present, uses an in-memory store"
  []
  (let [db-config (or (config/env-db-config)
                      {:store {:backend :mem :id "server"}})]
    (when-not (d/database-exists? db-config)
      (d/create-database db-config))

    (let [conn (d/connect db-config)]
      (d/transact conn schema)
      (swap! app-state assoc :conn conn)
      (swap! app-state assoc :cfg db-config)
      "success...")))

(defn delete-db! []
  []
  (let [conn (:conn @app-state)
        cfg (:cfg @app-state)]
    (swap! app-state assoc :conn nil)
    (if conn
      (do
        (d/release conn)
        (d/delete-database cfg)
        "successfully deleted")
      "there is no connected database to delete...")))


(defn add-person! []
 (let [conn (:conn @app-state)
       p (example/person)]
   (d/transact conn [p])))

(defn all-persons []
  (let [conn (:conn @app-state)]
    (d/q '[:find ?name ?last-name ?email
           :where
           [?e :person/name ?name]
           [?e :person/last-name ?last-name]
           [?e :person/email ?email]]
         @conn)))




(defn test-db []
 (let [cfg (config/env-db-config)]

  (d/delete-database cfg)

  (d/create-database cfg)
  (let [conn (d/connect cfg)]

    ;; use the filesystem as storage medium
    ;;(def cfg {:store {:backend :file :path "/tmp/example"}})

    ;; create a database at this place, per default configuration we enforce a strict
    ;; schema and keep all historical data


    ;; the first transaction will be the schema we are using
    ;; you may also add this within database creation by adding :initial-tx
    ;; to the configuration
    (d/transact conn [{:db/ident       :name
                       :db/valueType   :db.type/string
                       :db/cardinality :db.cardinality/one}
                      {:db/ident       :age
                       :db/valueType   :db.type/long
                       :db/cardinality :db.cardinality/one}])

    ;; lets add some data and wait for the transaction
    (d/transact conn [{:name "Alice", :age 20}
                      {:name "Bob", :age 30}
                      {:name "Charlie", :age 40}
                      {:age 15}])

    ;; search the data
    (println (d/q '[:find ?e ?n ?a
                    :where
                    [?e :name ?n]
                    [?e :age ?a]]
                  @conn))
    ;; => #{[3 "Alice" 20] [4 "Bob" 30] [5 "Charlie" 40]}

    ;; add new entity data using a hash map
    (d/transact conn {:tx-data [{:db/id 3 :age 25}]})

    ;; if you want to work with queries like in
    ;; https://grishaev.me/en/datomic-query/,
    ;; you may use a hashmap
    (println (d/q {:query '{:find  [?e ?n ?a]
                            :where [[?e :name ?n]
                                    [?e :age ?a]]}
                   :args  [@conn]}))
    ;; => #{[5 "Charlie" 40] [4 "Bob" 30] [3 "Alice" 25]}

    ;; query the history of the data
    (let [data (d/q '[:find ?a
                      :where
                      [?e :name "Alice"]
                      [?e :age ?a]]
                    (d/history @conn))]
      ;; => #{[20] [25]}

      ;; you might need to release the connection for specific stores
      (d/release conn)

      ;; clean up the database if it is not need any more

      data))))





