; https://gist.github.com/pithyless/e00362aa6061bfb4e4749079a33be073
; https://cljdoc.org/d/io.replikativ/datahike/0.3.3/doc/readme
; https://cljdoc.org/d/io.replikativ/datahike/0.6.1522/api/datahike.api
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
; https://docs.datomic.com/cloud/tutorial/retract.html
; https://cljdoc.org/d/datascript/datascript/1.3.15/api/datascript.core#q

(ns cr-eam.db
  (:require
    ; https://gist.github.com/pithyless/e00362aa6061bfb4e4749079a33be073
    [datahike.api :as d]
    [datahike-jdbc.core]
    [cr-eam.config :as config]
    [cr-eam.example-data :as example]
    [cr-eam.schema :as schema]
    [puget.printer :refer [cprint]]
    [clojure.pprint :refer [pprint]]))

;; domain: users that work on companies with persons, history and todos

(defonce app-state (atom {:conn nil
                          :cfg  nil}))


(declare start-db!)
(declare delete-db!)

(declare add-person!)
(declare all-persons)
(declare remove-person!)

(declare add-company!)
(declare all-companies)
(declare remove-company!)

(declare add-random-person-to-comp)
(declare pull-companies-with-persons)
(declare remove-random-person-from-comp)

(comment

  (start-db!)

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



(defn delete-db! []
  []
  (let [db-config (or (config/env-db-config)
                      {:store {:backend :mem :id "server"}})]
    (if (d/database-exists? db-config)
      (let [conn (d/connect db-config)]
        (d/release conn)
        (d/delete-database db-config)
        (swap! app-state assoc :conn nil)
        (swap! app-state assoc :cfg nil)
        "deleted...")
      "found no database to delete...")))

(defn start-db!
  "Creates a datahike connection and transacts the schema
  If no DATABASE_URL is present, uses an in-memory store"
  []
  (let [db-config (or (config/env-db-config)
                      {:store {:backend :mem :id "server"}})]
    (when-not (d/database-exists? db-config)
      (d/create-database db-config))

    (let [conn (d/connect db-config)]
      (d/transact conn schema/schema)
      (swap! app-state assoc :conn conn)
      (swap! app-state assoc :cfg db-config)
      "success...")))

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




