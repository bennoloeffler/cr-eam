(ns cr-eam.example-data
  (:require [clojure.string :as str]))

(def vornamen ["Benno"
               "Sabine"
               "Leo"
               "Paul"
               "Heidi"
               "Armin"
               "Markus"
               "Ulrike"
               "Andy"
               "Reiner"
               "Rosa"])

(def nachnamen ["Mohr"
                "Löffler"
                "Rothfuß"
                "Schneider"
                "Kiefer"
                "Überärger"
                "Hülsensack"
                "Rübennäschen"
                "Kübelböck"
                "Lochfraß"
                "Kalkeimer"
                "Sahneschnitte"
                "Schlüpfer"
                "Zufall"
                "Arbeit"
                "Ärger"
                "Übergewicht"
                "Öxchen"])

(def domains ["gmx.de"
              "hotmail.com"
              "webmail.de"
              "gmail.com"
              "googlemail.com"
              "t-online.com"
              "yahoo.com"
              "aol.com"
              "outlook.com"
              "icloud.com"
              "arschgesicht.com"])

(def mail-replacements {"ö" "oe"
                        "ä" "ae"
                        "ü" "ue"
                        "Ö" "Oe"
                        "Ä" "Ae"
                        "Ü" "Ue"
                        "ß" "ss"})

(defn mail-replace-umlauts [e-mail]
  (reduce
    #(str/replace %1 (first %2) (second %2))
    e-mail
    mail-replacements))

(defn e-mail [vn nn d]
  (-> (str vn "." nn "@" d)
      str/lower-case
      (mail-replace-umlauts)))

(defn person []
  (let [vn (rand-nth vornamen)
        nn (rand-nth nachnamen)
        d  (rand-nth domains)
        em (e-mail vn nn d)]
    {:person/name vn :person/last-name nn :person/email em}))

(comment
  (person))