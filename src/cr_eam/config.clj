(ns cr-eam.config
  (:require
    [puget.printer :refer  [cprint]]
    [clojure.pprint :refer [pprint]]
    [cprop.core :refer [load-config]]
    [cprop.source :refer [from-system-props
                          from-env]]))



(defn config []
  (from-env))


(println)
(cprint :-----system-properties-------------------------------------------------)
(cprint (from-system-props))


(println)
(cprint :-----environment-------------------------------------------------------)
(cprint (from-env))

(println)
(cprint :-----my-config.edn-----------------------------------------------------)
(cprint (load-config :file "my-config.edn"))
