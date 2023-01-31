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

(def nachnamen ["Müller"
                "Schmidt"
                "Schneider"
                "Fischer"
                "Weber"
                "Meyer"
                "Wagner"
                "Becker"
                "Schulz"
                "Hoffmann"
                "Schäfer"
                "Bauer"
                "Koch"
                "Richter"
                "Klein"
                "Wolf"
                "Schröder"
                "Neumann"
                "Schwarz"
                "Braun"
                "Hofmann"
                "Zimmermann"
                "Schmitt"
                "Hartmann"
                "Krüger"
                "Schmid"
                "Werner"
                "Lange"
                "Schmitz"
                "Meier"
                "Krause"
                "Maier"
                "Lehmann"
                "Huber"
                "Mayer"
                "Herrmann"
                "Köhler"
                "Walter"
                "König"
                "Schulze"
                "Fuchs"
                "Kaiser"
                "Lang"
                "Weiß"
                "Peters"
                "Scholz"
                "Jung"
                "Möller"
                "Hahn"
                "Keller"
                "Vogel"
                "Schubert"
                "Roth"
                "Frank"
                "Friedrich"
                "Beck"
                "Günther"
                "Berger"
                "Winkler"
                "Lorenz"
                "Baumann"
                "Schuster"
                "Kraus"
                "Böhm"
                "Simon"
                "Franke"
                "Albrecht"
                "Winter"
                "Ludwig"
                "Martin"
                "Krämer"
                "Schumacher"
                "Vogt"
                "Jäger"
                "Stein"
                "Otto"
                "Groß"
                "Sommer"
                "Haas"
                "Graf"
                "Heinrich"
                "Seidel"
                "Schreiber"
                "Ziegler"
                "Brandt"
                "Kuhn"
                "Schulte"
                "Dietrich"
                "Kühn"
                "Engel"
                "Pohl"
                "Horn"
                "Sauer"
                "Arnold"
                "Thomas"
                "Bergmann"
                "Busch"
                "Pfeiffer"
                "Voigt"
                "Götz"
                "Seifert"
                "Lindner"
                "Ernst"
                "Hübner"
                "Kramer"
                "Franz"
                "Beyer"])

(def firma-zwischen [" und " " & " " + "])
(def firma-form [" GmbH" " UG" " AG" " GmbH & Co. KG"])
(def firma-buchst "AAABCDEEEFFFFGGGGGGHIIIIIJKLLLLLMMMMMMNOOOOOOPQRSTUUUUUUUVWXXXXXYZ")

(defn firma-abkuerz[]
  (str (str/join (take 3 (repeatedly #(rand-nth firma-buchst)))) (rand-nth firma-form)))

(defn firma-mittel []
  (str (rand-nth nachnamen) (rand-nth firma-form)))

(defn firma-lang []
  (str (rand-nth nachnamen) (rand-nth firma-zwischen) (rand-nth nachnamen) (rand-nth firma-form)))

(defn firma []
  {:company/site-name ((rand-nth [firma-abkuerz firma-mittel firma-lang]))})

(comment
  (firma))

(defn mail-replace-umlauts [e-mail]
  (reduce
    #(str/replace %1 (first %2) (second %2))
    e-mail
    mail-replacements))

(defn e-mail [vn nn d]
  (-> (str vn "." nn "@" d)
      str/lower-case
      (mail-replace-umlauts)))

(defn fname-mail [vn nn d]
  (-> (str (subs vn 0 1) "." nn "@" d)
      str/lower-case
      (mail-replace-umlauts)))

(defn full-mail [vn nn d]
  (-> (str vn "." nn "@" d)
      str/lower-case
      (mail-replace-umlauts)))

(defn abbrev-mail [vn nn d]
  (-> (str (subs vn 0 2) (subs nn 0 1) "@" d)
      str/lower-case
      (mail-replace-umlauts)))


(defn e-mail [vn nn d]
 (let [r (rand-int 3)]
  (case r
    0 (full-mail vn nn d)
    1 (abbrev-mail vn nn d)
    2 (fname-mail vn nn d))))



(defn person []
  (let [vn (rand-nth vornamen)
        nn (rand-nth nachnamen)
        d  (rand-nth domains)
        em (e-mail vn nn d)]
    {:person/name vn :person/last-name nn :person/email em}))

(comment
  (person))