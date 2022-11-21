(ns cr-eam.schema
  (:require [clojure.string :as str]))

(defn k->s [k]
  (str/replace (str k) ":" ""))

(defn attribute [entity attrib-name type cardinality doc unique]
  (let [ident      (keyword (str (k->s entity) "/" (k->s attrib-name)))
        unique-map {:db/unique :db.unique/value}
        doc-map    {:db/doc doc}
        data       {:db/ident       ident ; :todo/who-doing
                    :db/valueType   (keyword (str "db.type/" (k->s type))) ; :db.type/ref)) ; user
                    :db/cardinality (keyword (str "db.cardinality/" (k->s cardinality)))}
        data       (if unique (merge data unique-map) data)
        data       (if doc (merge data doc-map) data)]
    data))

(defn au
  ([entity attrib-name type cardinality comment]
   (attribute entity attrib-name type cardinality comment true))
  ([entity attrib-name type cardinality]
   (attribute entity attrib-name type cardinality nil true)))

(defn a
  ([entity attrib-name type cardinality comment]
   (attribute entity attrib-name type cardinality comment false))
  ([entity attrib-name type cardinality]
   (attribute entity attrib-name type cardinality nil false)))


(comment
  (attribute :todo :who-doing :ref :one "user, that that is responsible for doing the todo" true)
  (attribute :todo :who-doing :ref :one "user, that that is responsible for doing the todo" false)
  (attribute :todo :who-doing :ref :one nil false)


  (a :todo :who-doing :ref :one)
  (a :todo :who-doing :ref :one "the doc")
  (au :todo :who-doing :ref :one)
  (au :todo :who-doing :ref :one "the doc"))


(def schema
  "The database schema which is transacted when the app starts"
  [

   ;;;;;;;;;;;;;;;;;;
   ;; person
   ;;;;;;;;;;;;;;;;;
   (a :person :name :string :one)
   (a :person :last-name :string :one)
   (au :person :email :string :one)
   (a :person :phone :string :one)
   (a :person :mobile :string :one)

   #_{:db/ident       :person/name
      :db/valueType   :db.type/string
      :db/cardinality :db.cardinality/one}
   #_{:db/ident       :person/last-name
      :db/valueType   :db.type/string
      :db/cardinality :db.cardinality/one}
   #_{:db/ident       :person/email
      :db/valueType   :db.type/string
      :db/cardinality :db.cardinality/one
      :db/unique      :db.unique/value}
   #_{:db/ident       :person/phone
      :db/valueType   :db.type/string
      :db/cardinality :db.cardinality/one}
   #_{:db/ident       :person/mobile
      :db/valueType   :db.type/string
      :db/cardinality :db.cardinality/one}

   ;;;;;;;;;;;;;;;;;;
   ;; company or site - recursively
   ;;;;;;;;;;;;;;;;;
   {:db/ident       :company/short-name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value
    :db/doc         "e.g. Bosch"}
   {:db/ident       :company/legal-name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value
    :db/doc         "e.g. Rebert Bosch GmbH"}
   {:db/ident       :company/site-name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value
    :db/doc         "e.g. Bosch Blaichach"}
   {:db/ident       :site/mother
    :db/valueType   :db.type/ref ; to site
    :db/cardinality :db.cardinality/one}

   ;;;;;;;;;;;;;;;;;;
   ;; site
   ;;;;;;;;;;;;;;;;;
   ;; address
   {:db/ident       :site/persons
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :site/street
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   {:db/ident       :site/street-number
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   {:db/ident       :site/zip-code
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   {:db/ident       :site/city
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   ;; economic data
   {:db/ident       :site/employees
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :site/turnover
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}

   ;;;;;;;;;;;;;;;;;;
   ;; _todo
   ;;;;;;;;;;;;;;;;;
   {:db/ident       :todo/who-doing
    :db/valueType   :db.type/ref ; user
    :db/cardinality :db.cardinality/one
    :db/doc         "user, that that is responsible for doing the todo"}
   {:db/ident       :todo/who-informed
    :db/valueType   :db.type/ref ; users
    :db/cardinality :db.cardinality/many
    :db/doc         "users, that need to be aware of the todo"}
   {:db/ident       :todo/when
    :db/valueType   :db.type/long ; unix time stamp UTC
    :db/cardinality :db.cardinality/one}
   {:db/ident       :todo/what
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :todo/persons
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :todo/done
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}

   ;;;;;;;;;;;;;;;;;;
   ;; history entry
   ;;;;;;;;;;;;;;;;;

   {:db/ident       :history/entry
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "description of what is to be remembered"}
   {:db/ident       :history/who
    :db/valueType   :db.type/ref ; user
    :db/cardinality :db.cardinality/one}
   {:db/ident       :history/when
    :db/valueType   :db.type/long ; UTC
    :db/cardinality :db.cardinality/one}

   ;;;;;;;;;;;;;;;;;;
   ;; lead
   ;;;;;;;;;;;;;;;;;
   {:db/ident       :lead/title
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value}
   {:db/ident       :lead/persons
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :lead/site
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident       :lead/todos
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :lead/history-entries
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :lead/lead-kpis
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}


   ;;;;;;;;;;;;;;;;;;
   ;; lead-kpi
   ;;;;;;;;;;;;;;;;;
   {:db/ident       :lead-kpis/who
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident       :lead-kpis/type
    :db/valueType   :db.type/string ;; "HOT", "OFFER", "ORDER" - make it enum
    :db/cardinality :db.cardinality/one}
   {:db/ident       :lead-kpis/percentage
    :db/valueType   :db.type/long ; 0 to 100
    :db/cardinality :db.cardinality/one}
   {:db/ident       :lead-kpis/start
    :db/valueType   :db.type/long ; UTC
    :db/cardinality :db.cardinality/one}
   {:db/ident       :lead-kpis/monthly-euro
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :lead-kpis/runtime-in-month
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}])



