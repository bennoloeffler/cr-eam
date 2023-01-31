(ns cr-eam.view
  "create standard-views based on schema"
  [:require [taoensso.timbre :as timbre]])

(def ps [#:person{:email     "heidi.seifert@aol.com",
                  :name      "Heidi",
                  :last-name "Seifert"}
         #:person{:email     "heidi.koehler@arschgesicht.com",
                  :name      "Heidi",
                  :last-name "Köhler"}
         #:person{:email     "reiner.vogel@gmx.de",
                  :name      "Reiner",
                  :last-name "Vogel"}])

(def c-with-p [[#:company{:name "IOL UG",
                          :persons
                          [#:person{:email     "ulrike.guenther@hotmail.com",
                                    :name      "Ulrike",
                                    :last-name "Günther"}]}]
               [#:company{:name    "Schmidt GmbH & Co. KG",
                          :persons ps}]])


(defn headings
  "returns vector with three elements:
  - vector of headings without namespace
  - the namespace itself string
  - vector of headings with namespace"
  [maps]
  (let [namespaced-attrib-names (vec (set (flatten (map keys maps))))
        name-space              (namespace (first namespaced-attrib-names))
        attrib-names            (mapv name namespaced-attrib-names)]
    [attrib-names name-space namespaced-attrib-names]))



(defn row [element headings]
  (map (fn [heading] (heading element)) headings))

(defn table [data]
  (let [heading (headings data)]))

;; if vector: map elements
;; if map: take keys
(defn detect [data]
  (if (map? (first data))
    data
    (flatten data)))


(comment
  (map println c-with-p)
  (detect c-with-p)

  (def p (first ps))
  (headings (detect ps))
  (headings (detect c-with-p))

  (name :person/email)
  (namespace :person/email))

(defn table [])

(defn comp-with-persons [data]
  (timbre/spy :info data)
  (let [out (map (fn [comp] [:div.h1.title.is-4 (:company/site-name (first comp))
                             [:div.box (map (fn [pers] [:h1.subtitle.is-6 (:person/email pers) ", " (:person/name pers) " " (:person/last-name pers)]) (:site/persons (first comp)))]]) data)]
    (timbre/spy :info out)
    out))

(comment
  (comp-with-persons c-with-p))

