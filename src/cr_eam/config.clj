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
        dbu (or (:database-url env) "DATABASE_URL: missing")]
    (log :info dbu)
    dbu))

(defn config-jdbc []
  (let [env (from-env)
        jdbc (:database-url env)]
    (if jdbc {:store {:backend :pg
                      ;:dbtype "postgresql"
                      :jdbcUrl jdbc}}
             nil)))


(comment
  (config)
  (log :info "abc"))


;(println)
;(cprint :-----system-properties-------------------------------------------------)
;(cprint (from-system-props))


;(println)
;(cprint :-----environment-------------------------------------------------------)
;(cprint (from-env))

;(println)
;(cprint :-----my-config.edn-----------------------------------------------------)
;(cprint (load-config :file "my-config.edn"))
