(ns cr-eam.config
  (:require
    [clojure.string :as str]
    [puget.printer :refer [cprint]]
    [clojure.tools.logging :as l]
    [clojure.pprint :refer [pprint]]
    [cprop.core :refer [load-config]]
    [cprop.source :refer [from-system-props
                          from-env]]
    [taoensso.timbre :as timbre
     ;; Optional, just refer what you like:
     :refer [log trace debug info warn error fatal report
             logf tracef debugf infof warnf errorf fatalf reportf
             spy set-min-level!]]
    [taoensso.timbre.appenders.core :as appenders]
    [taoensso.timbre.tools.logging :refer [use-timbre]])
  (:import [java.util TimeZone]))

;; :trace < :debug < :info < :warn < :error < :fatal < :report
(set-min-level! :info)

(use-timbre) ;Sets the root binding of `clojure.tools.logging/*logger-factory*` to use Timbre.

; format logging message
; see https://www.demystifyfp.com/clojure/marketplace-middleware/configuring-logging-using-timbre/
#_(defn- bels-output [{:keys [level msg_ instant ?file]}] ;<1>
    ; idea: use default, replace host with "" and append file.clj with line number to make it clickable
    (let [event (read-string (force msg_))] ;<2>
      (json/generate-string {:timestamp instant ;<3>
                             :level level
                             :event event})))

; http://ptaoussanis.github.io/timbre/taoensso.timbre.html#var-*config*
(timbre/merge-config!
  {:appenders      {:spit (appenders/spit-appender {:fname "cr-eam.log"})}
   :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss" :timezone (TimeZone/getTimeZone "CET")}})
   ;:output-fn bels-output})

; taoensso.timbre/*config*
; taoensso.timbre/default-timestamp-opts


(comment
  (log :info "abc") ; log with timbre
  (l/log :info "def")) ; log with clojure.logging

(defn config []
  (let [ps "DATABASE_URL"
        js "JDBC_DATABASE_URL"
        p (System/getenv ps)
        j (System/getenv js)]
    (println ps p)
    (println js j)
    j))

(defn env-db-config
  "Constructs a datahike configuration map from the the heroku
  provided `DATABASE_URL` or returns nil if that env var is not present.
  ATTENTION: heroku delivers 'postgres' as dbtype.
  For jdbc, 'postgresql' is needed instead.
  So a perfect jdbc URL looks like:
  postgresql://user:pw@host:port/database.
  For usage with a localhost postgres installation (without pw), use:
  DATABASE_URL=postgres://benno:@localhost:5432/cream
  Details for heroku are there:
  https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-jdbc_database_url"
  []
  (when-let [db-url (System/getenv "DATABASE_URL")]
    (let [uri (java.net.URI. db-url)
          [username password] (str/split (.getUserInfo uri) #":")]
      {:store
       {:backend  :jdbc
        :dbtype   "postgresql"
        :host     (.getHost uri)
        :user     username
        :password password
        :dbname   (str/join (drop 1 (.getPath uri)))
        :port     (.getPort uri)}})))

(comment
  (env-db-config))

;(println)
;(cprint :-----system-properties-------------------------------------------------)
;(cprint (from-system-props))


;(println)
;(cprint :-----environment-------------------------------------------------------)
;(cprint (from-env))

;(println)
;(cprint :-----my-config.edn-----------------------------------------------------)
;(cprint (load-config :file "my-config.edn"))
