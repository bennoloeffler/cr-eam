; https://gist.github.com/pithyless/e00362aa6061bfb4e4749079a33be073
; https://cljdoc.org/d/io.replikativ/datahike/0.3.3/doc/readme
; https://docs.datomic.com/on-prem/overview/introduction.html
; https://docs.datomic.com/on-prem/tutorial/introduction.html
; https://github.com/Datomic/day-of-datomic/blob/master/doc-examples/tutorial2.repl
; https://github.com/Datomic/day-of-datomic source for tutorial
; https://github.com/ftravers/datomic-tutorial
; https://github.com/kristianmandrup/datascript-tutorial/blob/master/SUMMARY.md
; http://www.learndatalogtoday.org/
; https://clojureverse.org/t/a-quick-way-to-start-experimenting-with-datomic/5004
; http://www.karimarttila.fi/clojure/2020/11/14/clojure-datomic-exercise.html
; https://stackoverflow.com/questions/42786046/how-to-update-overwrite-a-ref-attribute-with-cardinality-many-in-datomic
; https://blog.davemartin.me/posts/datomic-how-to-update-cardinality-many-attribute/

(ns cr-eam.db
  (:require
    ; https://gist.github.com/pithyless/e00362aa6061bfb4e4749079a33be073
    [datahike.api :as d]
    [datahike-jdbc.core]
    [cr-eam.config :as config]
    [cr-eam.example-data :as example]
    [puget.printer :refer [cprint]]
    [clojure.pprint :refer [pprint]]))

;; domain: users that work on companies with persons, history and todos
;; Use-cases:
;; create and connect database
;; create schema
;; create company
;; create person
;; create user
;; add person to company
;; remove person from company
;; find person that belongs to 2 or more companies
;; add _todo to company and optional to user and person
(defonce app-state (atom {:conn nil
                          :cfg  nil}))

(def schema
  "The database schema which is transacted when the app starts"
  [{:db/ident       :person/name ; person
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :person/last-name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :person/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   {:db/ident       :person/phone
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :person/mobile
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :company/name ; company
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   {:db/ident       :company/persons
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}])


(declare test-db)
(declare add-person!)
(declare all-persons)
(declare remove-person!)
(declare add-company!)
(declare all-companies)
(declare remove-company!)
(declare delete-db!)
(declare start-db-file)
(declare start-db-cfg)
(declare add-random-person-to-comp)
(declare pull-companies-with-persons)
(declare remove-random-person-from-comp)

; WORKS!
(comment
  (def cfg {:store {:backend :file :path "/tmp/example"}})
  (def cfg {:store {:backend :mem :id "server"}})
  (def cfg {:store {:backend  :jdbc
                    :dbtype   "postgresql" ; NOT postgres
                    :host     "localhost"
                    :port     5432
                    :user     "benno"
                    :password ""
                    :dbname   "cream"}})

  ; postgresql://localhost/cream?user=benno&password=
  ; https://stackoverflow.com/questions/3582552/what-is-the-format-for-the-postgresql-connection-string-url
  ; WORKS!
  (def cfg {:store {:backend  :jdbc
                    :dbtype   "postgresql"
                    :dbname   "cream"
                    :jdbc-url "postgresql://localhost:5432/cream?user=benno&password=''"}})

  (def cfg (config/env-db-config))

  (defn start-db-cfg []
    ; connect, schema and app-state
    (when-not (d/database-exists? cfg) (d/create-database cfg))
    (let [conn (d/connect cfg)]
      (d/transact conn schema)
      (swap! app-state assoc :conn conn)
      (swap! app-state assoc :cfg cfg)))

  ; set cfg before!
  (start-db-cfg)
  (start-db-file)

  (add-person!)
  (all-persons)
  (remove-person!)

  (add-company!)
  (all-companies)
  (remove-company!)

  (add-random-person-to-comp)
  (remove-random-person-from-comp)
  (pull-companies-with-persons)

  (delete-db!)

  nil)



(defn start-db-file []
  (let [cfg  {:store {:backend :file :path "/tmp/example"}}
        _    (when-not (d/database-exists? cfg) (d/create-database cfg))
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
        cfg  (:cfg @app-state)]
    (swap! app-state assoc :conn nil)
    (if conn
      (do
        (d/release conn)
        (d/delete-database cfg)
        "successfully deleted")
      "there is no connected database to delete...")))


(defn add-person! []
  (let [conn (:conn @app-state)
        p    (example/person)]
    (d/transact conn [p])
    (pprint p)
    p))

(defn add-company! []
  (let [conn (:conn @app-state)
        c    (example/firma)]
    (d/transact conn [c])
    (pprint c)
    c))

(comment
  (add-company!))

(defn all-persons []
  (let [conn    (:conn @app-state)

        ; could use rules?
        rules   '[[(find-person ?name ?last-name ?email)
                   [?id :person/email ?email]
                   [?id :person/last-name ?lname]]
                  [(find-person ?user ?fname ?lname ?email)
                   [?id :person/email ?email]]]

        persons (d/q '[:find ?email ; ?name ?last-name
                       :where
                       ;[?e :person/name ?name]
                       ;[?e :person/last-name ?last-name]
                       [?e :person/email ?email]]
                     @conn)]
    (pprint persons)
    persons))

(comment
  (all-persons))

(defn all-companies []
  (let [conn      (:conn @app-state)
        companies (d/q '[:find ?cn
                         :where
                         [?e :company/name ?cn]]
                       @conn)]
    (pprint companies)
    companies))

(comment
  (all-companies))

(defn remove-person!
  "remove a person and all refs from all companies, that refer to that person."
  []
  (let [conn    (:conn @app-state)
        all-ids (d/q '[:find ?p-ids
                       :where
                       [?p-ids :person/email _]]
                     @conn)
        p-id    (first (first all-ids))]
    (when p-id
      (d/transact conn [[:db/retractEntity p-id]]))))

(comment
  (remove-person!))

(defn remove-company!
  "remove a company."
  []
  (let [conn    (:conn @app-state)
        all-ids (d/q '[:find ?c-ids
                       :where
                       [?c-ids :company/name _]]
                     @conn)
        c-id    (first (first all-ids))]
    (when c-id
      (d/transact conn [[:db/retractEntity c-id]]))))

(comment
  (remove-company!))


(defn all-companies-with-persons []
  (let [conn      (:conn @app-state)
        companies (d/q '[:find ?cn ?p
                         :where
                         [?p :person/email ?pe]
                         [?p :person/last-name ?pln]
                         [?p :person/name ?pn]
                         [?e :company/persons ?p]
                         [?e :company/name ?cn]]
                       @conn)]
    ;(pprint companies)
    companies))

(comment
  (all-companies-with-persons))


(defn pull-companies-with-persons []
  (let [conn      (:conn @app-state)
        companies (d/q '[:find (pull ?e
                                     [:company/name
                                      {:company/persons [:person/email :person/name :person/last-name]}])

                         :where
                         [?e :company/persons _]
                         [?e :company/name _]]
                       @conn)]
    (pprint companies)
    companies))
(comment
  (pull-companies-with-persons))

(defn remove-random-person-from-comp []
  (let [conn    (:conn @app-state)
        all-ids (d/q '[:find ?c-id ?p-ids
                       :where
                       [?c-id :company/persons ?p-ids]
                       [?c-id :company/name _]]
                     @conn)
        ids     (first all-ids)
        c       (first ids)
        p       (second ids)]
    (when (seq ids)
      (d/transact conn [[:db/retract c :company/persons p]]))))
(comment
  (add-random-person-to-comp)
  (remove-random-person-from-comp)
  (pull-companies-with-persons))

(defn add-random-person-to-comp []
  (let [conn  (:conn @app-state)
        c-ids (d/q '[:find ?c-id
                     :where
                     [?c-id :company/name _]]
                   @conn)
        p-ids (d/q '[:find ?p-id
                     :where
                     [?p-id :person/name _]]
                   @conn)

        c     (first (rand-nth (seq c-ids)))
        p     (first (rand-nth (seq p-ids)))

        cn    (d/q '[:find ?cn .
                     :in $ ?c-id
                     :where
                     [?c-id :company/name ?cn]]
                   @conn c)
        pn    (d/q '[:find ?pn .
                     :in $ ?p-id
                     :where
                     [?p-id :person/email ?pn]]
                   @conn p)

        _     (println cn "  -->  " pn)]


    (d/transact conn [{:db/id c :company/persons [p]}])))

(comment
  (def result (add-random-person-to-comp)))

(defn add-the-comp-and-persons []
  (let [conn (:conn @app-state)]
    (d/transact conn [{:db/id -1 :company/name "TheComp"
                       :company/persons
                       [{:db/id -2 :person/email "special-1@person.de"}
                        {:db/id -3 :person/email "special-2@person.de"}]}])))

(defn del-the-comp []
  (let [conn (:conn @app-state)]
    (d/transact conn [[:db/retractEntity [:company/name "TheComp"]]])))

(defn del-person [email]
  (let [conn (:conn @app-state)]
    (d/transact conn [[:db/retractEntity [:person/email email]]])))

(comment
  (del-person "special-1@person.de")
  (del-person "special-2@person.de"))

(comment
  (all-companies)
  (add-the-comp-and-persons)
  (del-the-comp)
  (all-persons)
  (pull-companies-with-persons))

(defn comps-with-persons-named [name]
  (let [conn (:conn @app-state)]
    (d/q '[:find ?cn
           :in $ ?name
           :where
           [?c-id :company/name ?cn]
           [?c-id :company/persons ?p-id]
           [?p-id :person/name ?name]]
         @conn name)))


(comment
  (all-persons)
  (all-companies)
  (comps-with-persons-named "Ulrike")
  (comps-with-persons-named "Reiner"))

#_(defn test-db []
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





