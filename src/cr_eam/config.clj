(ns cr-eam.config
  (:require
    [puget.printer :refer  [cprint]]
    [clojure.tools.logging :refer :all]
    [clojure.pprint :refer [pprint]]
    [cprop.core :refer [load-config]]
    [cprop.source :refer [from-system-props
                          from-env]]))



(defn config []
  (let [env (from-env)
        dbu (:database-url env)]
    (println (str "DATABASE_URL: " (or dbu "missing!")))
    dbu))

(comment
  (config)
  (log :info "abc"))


(println)
(cprint :-----system-properties-------------------------------------------------)
(cprint (from-system-props))


(println)
(cprint :-----environment-------------------------------------------------------)
(cprint (from-env))

(println)
;(cprint :-----my-config.edn-----------------------------------------------------)
;(cprint (load-config :file "my-config.edn"))
