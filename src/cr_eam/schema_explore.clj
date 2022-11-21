(ns cr-eam.schema-explore
  (:require
    ; https://gist.github.com/pithyless/e00362aa6061bfb4e4749079a33be073
    [datahike.api :as d]
    [datahike-jdbc.core]
    [cr-eam.example-data :as example]
    [cr-eam.config :as config]
    [cr-eam.schema :as schema]
    [puget.printer :refer [cprint]]
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]))

;; TODO
;; ok - or and
;; ok - rules
;; ok - aggregates
;; - component
;; - filter
;; ok - fulltext - seems only datomic and xtdb


(defonce app-state (atom {:conn nil
                          :cfg  nil}))

(def cfg nil)

(comment

  ;; in memory db
  (def cfg {:store {:backend :mem :id "server-1"}})

  ;; file db
  (def cfg {:store {:backend :file :path "/tmp/example"}})

  ; postgresql://localhost/cream?user=benno&password=
  ; https://stackoverflow.com/questions/3582552/what-is-the-format-for-the-postgresql-connection-string-url
  (def cfg {:store {:backend  :jdbc
                    :dbtype   "postgresql"
                    :dbname   "cream"
                    :jdbc-url "postgresql://localhost:5432/cream?user=benno&password=''"}})

  (def cfg {:store    {:backend :jdbc}
            :dbtype   "postgresql" ; NOT postgres
            :host     "localhost"
            :port     5432
            :user     "benno"
            :password ""
            :dbname   "cream"})

  ;; read config with (System/getenv "DATABASE_URL")
  (def cfg (config/env-db-config))

  nil)

(defn start-db-cfg []
  (if cfg
    (if (d/database-exists? cfg)
      (str "db already exists: " cfg)
      (let [_    (d/create-database cfg)
            conn (d/connect cfg)]
        (d/transact conn schema/schema)
        (swap! app-state assoc :conn conn)
        (swap! app-state assoc :cfg cfg)))
    "cfg = nil -> please def cfg"))

(defn delete-app-state-db! []
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


(comment

  (delete-app-state-db!)
  (start-db-cfg)
  (def conn (:conn @app-state))
  ;; the first transaction will be the schema we are using
  ;; you may also add this within database creation by adding :initial-tx
  ;; to the configuration
  (d/transact conn schema/schema)

  ;; one new datom
  (d/transact conn [{:person/email     "info@v-und-s.de"
                     :person/last-name "Anonymous"
                     :person/name      "Shadow"}])
  (def info-vunds (:db/id (d/entity @conn [:person/email "info@v-und-s.de"])))

  ;; lets add some related data
  ;; temp-ids are used
  (d/transact conn [{:db/id -1 :person/email "loeffler@v-und-s.de"}
                    #:person{:db/id -2 :last-name "Tietz" :name "Nicole" :email "tietz@v-und-s.de"}
                    {:company/short-name "V&S", :site/persons [-1 -2 info-vunds]}])


  ;; change data of loeffler@
  (d/transact conn [{:db/id            [:person/email "loeffler@v-und-s.de"]
                     :person/last-name "Löffler"
                     :person/name      "Benno"}])

  ;; or more explicitly
  ; https://docs.datomic.com/cloud/tutorial/retract.html

  ;; show number of employees in V&S
  (defn employees []
    (:site/employees (d/entity @conn [:company/short-name "V&S"])))
  (employees)

  ;; now it's 7
  (d/transact
    conn
    {:tx-data
     [[:db/add [:company/short-name "V&S"]
       :site/employees 7]]})
  (employees)

  ;; now it's 80
  (d/transact
    conn
    {:tx-data
     [[:db/add [:company/short-name "V&S"]
       :site/employees 80]]})
  (employees)

  ;; correct to 8
  (d/transact
    conn
    {:tx-data
     [[:db/add [:company/short-name "V&S"]
       :site/employees 8]
      [:db/add "datomic.tx" :db/doc "correct data entry error"]]})
  (employees)
  ;; AND: find the doc for the transaction
  (d/q '[:find ?empl ?doc
         :where [?ec :company/short-name "V&S"]
                [?ec :site/employees ?empl ?tx]
                [?tx :db/doc ?doc]]
        @conn)


  ;; now it should dissapear - because we
  ;; no not know, how much it is now
  (d/transact
    conn
    {:tx-data
     [[:db/retract [:company/short-name "V&S"]
       :site/employees]]})
  ;; this DOES NOT not work at the first time... BUT THE SECOND...
  ;; seems we need the real value to distract... ? REALLY?
  (employees)

  (d/transact
    conn
    {:tx-data
     [[:db/retractAttribute [:company/short-name "V&S"]
       :site/employees]]})
  ;; what does that? remove all entries?
  (employees)

  (d/transact
    conn
    {:tx-data
     [[:db/retract [:company/short-name "V&S"]
       :site/employees 80]]})

  ;; no more employees attribute...
  (employees)

  #_(d/transact
      conn
      {:tx-data [[:db/retract [:company/name "V&S"] :company/employees 8]
                 [:db/add "datomic.tx" :db/doc "remove incorrect assertion"]]})

  (d/transact conn [{:db/id            [:person/email "loeffler@v-und-s.de"]
                     :person/last-name "Löffler"
                     :person/name      "Benno"}])


  ;; get entity by identity attribute
  (d/entity @conn [:person/email "loeffler@v-und-s.de"])

  (def c-id (d/entity @conn [:company/short-name "V&S"]))
  (seq c-id) ;; realize lazy entity
  (into {} c-id) ;; create hash-map from entity
  (type c-id)

  ;; get entity by raw long
  (def c-id-raw (:db/id c-id))
  (type c-id-raw)
  (println c-id-raw)
  (seq (d/entity @conn c-id-raw))


  ;; get attribute value from entity
  (:person/name (d/entity @conn [:person/email "loeffler@v-und-s.de"]))

  ;; use relation naturally "forward" - along the ref (find persons referenced by company)
  (:site/persons (d/entity @conn [:company/short-name "V&S"]))

  ;; use relation "backwards" - from the referred person to the referring companies
  (:site/_persons (d/entity @conn [:person/email "loeffler@v-und-s.de"]))
  ;; use them like maps again
  (map :company/short-name (:site/_persons (d/entity @conn [:person/email "loeffler@v-und-s.de"])))

  (println (d/q '[:find ?n ?ln
                  :in $ ?cn
                  :where
                  [?e :person/last-name ?ln]
                  [?e :person/name ?n]
                  [?c :site/persons ?e]
                  [?c :company/short-name ?cn]]
                @conn "V&S"))

  ;; add new entity data using a hash map
  (d/transact conn {:tx-data [{:person/name "Anna" :person/email "anl@v-und-s.de"}]})
  (d/transact conn {:tx-data [{:company/short-name "Ukraine-Hilfe"}]})

  (cprint (seq (d/entity @conn [:person/email "anl@v-und-s.de"])))

  ;; connect company with person
  (d/transact conn {:tx-data [{:db/id        [:company/short-name "Ukraine-Hilfe"]
                               ;; lookup ref inside many... otherwise lookup ref will be
                               ;; interpreted as refs
                               :site/persons [[:person/email "anl@v-und-s.de"]]}
                              {:db/id        [:company/short-name "V&S"]
                               ;; lookup ref inside many... otherwise lookup ref will be
                               ;; interpreted as refs
                               :site/persons [[:person/email "anl@v-und-s.de"]]}]})

  (d/transact conn {:tx-data [{:db/id            [:person/email "tietz@v-und-s.de"]
                               :person/last-name "Tietz"}]})



  (:site/persons (d/entity @conn [:company/short-name "V&S"]))

  (map :person/email (:site/persons (d/entity @conn [:company/short-name "Ukraine-Hilfe"])))
  (map :person/email (:site/persons (d/entity @conn [:company/short-name "V&S"])))

  (cprint (d/q '[:find ?e ?a ?v
                 :where
                 [?e ?a ?v]]
               @conn))


  (cprint (d/q '[:find [?n ...]
                 :where
                 [?e :person/email ?n]]
               @conn))

  (cprint (d/q '[:find ?n
                 :where
                 [?e :company/short-name ?n]]
               @conn))

  (cprint (d/q '[:find ?c ?pn
                 :where
                 [?e :company/short-name ?c]
                 [?e :site/persons ?p]
                 [?p :person/email ?pn]]
               @conn))

  (cprint (d/q '[:find ?n . ;; use . to unpack
                 :where
                 [?e :person/email ?n]
                 [?c :site/persons ?e]
                 (not [?c :company/short-name "V&S"])]
               @conn))

  (cprint (d/q '[:find ?email ?csn ;; use . to unpack
                 :where
                 [?e :person/email ?email]
                 [?c :site/persons ?e]
                 [?c :company/short-name ?csn]
                 [(clojure.string/includes? ?email "o")]]
               @conn))

  ;; if you want to work with queries like in
  ;; https://grishaev.me/en/datomic-query/,
  ;; you may use a hashmap
  (cprint (d/q {:query '{:find  [?cn ?pn]
                         :where [[?e :company/short-name ?cn]
                                 [?e :site/persons ?p]
                                 [?p :person/name ?pn]]}
                :args  [@conn]}))

  ;; query the history of the data
  (pprint (d/q '[:find ?na ?ne
                 :where
                 [?e :site/employees ?ne]
                 [?e :company/short-name ?na]]
               (d/history @conn)))

  ; pull: ; https://github.com/Datomic/day-of-datomic
  (cprint (d/pull @conn '[*] [:company/short-name "V&S"]))
  (cprint (d/pull @conn '[*] [:person/email "tietz@v-und-s.de"]))
  (cprint (d/pull @conn '[:company/short-name {:site/persons [:person/name]}] [:company/short-name "V&S"]))
  (cprint (d/pull @conn '[:person/name {:site/_persons [:company/short-name]}] [:person/email "anl@v-und-s.de"]))
  (cprint (d/pull @conn '[{:site/_persons [:company/short-name {:site/persons [*]}]}] [:person/email "anl@v-und-s.de"]))

  (def anna (:db/id (d/entity @conn [:person/email "anl@v-und-s.de"])))

  (def persons-in-same-company
    '[:find [(pull ?all pattern) ...]
      :in $ ?email pattern
      :where
      [?p :person/email ?email]
      [?s :site/persons ?p]
      [?s :site/persons ?all]])

  (d/q persons-in-same-company @conn "anl@v-und-s.de" [:person/name])
  (map :person/name (d/q persons-in-same-company @conn "anl@v-und-s.de" [:person/name]))

  (d/q persons-in-same-company
       @conn
       "anl@v-und-s.de"
       '[:person/name {:site/_persons [:company/short-name]}])

  (d/q '[:find [(pull ?all pattern) ...]
         :in $ ?email pattern
         :where
         [?p :person/email ?email]
         [?s :site/persons ?p]
         [?s :site/persons ?all]]
       @conn
       "anl@v-und-s.de"
       [:person/email])

  ;; find all the pesons with 'z' or 'S' in either name or email
  (d/q '[:find [(pull ?e [:person/name :person/email]) ...]
         :in $ [?letter ...] [?attr ...]
         :where
         [?e ?attr ?name]
         [(clojure.string/includes? ?name ?letter)]]

       @conn
       ["z" "S"]
       [:person/name :person/email])

  (def rules
    '[[(collegues ?person ?collegues)
       [?site :site/persons ?person]
       [?site :site/persons ?collegues]]])

  (def persons-in-same-company-2
    '[:find [(pull ?all pattern) ...]
      :in $ % ?email pattern
      :where
      [?p :person/email ?email]
      (collegues ?p ?all)])

  (d/q persons-in-same-company-2
       @conn
       rules
       "anl@v-und-s.de"
       '[*])

  (d/q persons-in-same-company-2
       @conn
       rules
       "anl@v-und-s.de"
       '[:person/email {:site/_persons [:company/short-name]}])


  (d/q '[:find [(pull ?t [*]) ...]
         :where
         [?e ?a ?v ?t]
         [?t]]
       @conn)

  (d/q '[:find (count ?e) . ;; [(pull ?e [*]) ...]) ; just pull to see data
         :where
         ;[?e :person/name ?name]
         (or-join [?e]
                  (and
                    [?e :person/name ?name]
                    [(clojure.string/includes? ?name "A")])
                  (and
                    [?e :person/last-name ?last-name]
                    [(count ?last-name) ?len]   ;; ONE CALC STEP - not nested!
                    [(> ?len 7)])
                  (and
                    [?e :company/short-name ?s]
                    [(count ?s) ?len]   ;; ONE CALC STEP - not nested!
                    [(< ?len 4)]))]
       @conn)

  nil)

