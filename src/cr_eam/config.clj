(ns cr-eam.config
  (:require
    [clojure.string :as str]
    [puget.printer :refer [cprint]]
    [clojure.tools.logging :refer :all]
    [clojure.pprint :refer [pprint]]
    [cprop.core :refer [load-config]]
    [cprop.source :refer [from-system-props
                          from-env]]))


;; jdbc:postgresql://host:port/database
;; jdbc:postgresql://localhost:5432/benno

(defn env-db-config
  "Constructs a datahike configuration map from the the heroku
  provided `DATABASE_URL` or returns nil if that env var is not present"
  []
  (when-let [db-url "postgres://rrjvsoocgphkgg:caeb1948c4cf925050515ee5520ea2397954bb3b221bca4f3ca2f790a79f05c7@ec2-3-217-219-146.compute-1.amazonaws.com:5432/da6jsqhjv7p3k5"] ;(System/getenv "DATABASE_URL")]
    (let [uri (java.net.URI. db-url)
          [username password] (str/split (.getUserInfo uri) #":")]
      {:store
       {:backend  :jdbc
        :host     (.getHost uri)
        :user     username
        :password password
        :dbname   (str/join (drop 1 (.getPath uri)))
        :port     (.getPort uri)}})))

(comment
  (env-db-config))

(defn config []
  (let [env (from-env)
        dbu (or (:database-url env) "DATABASE_URL: missing")]
    (log :info dbu)
    dbu))

(defn config-jdbc []
  (let [env  (from-env)
        jdbc (:database-url env)]
    (if jdbc {:store {:backend :jdbc
                      ;:dbtype "postgres"
                      :dbtype  "postgresql"
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
