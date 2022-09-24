(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty])
  (:gen-class))

;; https://ericnormand.me/guide/clojure-web-tutorial

(def server (atom nil))

(def counter (atom 0N))
(def last-time (atom (System/currentTimeMillis)))

(defn duration []
  (let [this-time     (System/currentTimeMillis)
        duration      (as-> @last-time $
                            (- this-time $)
                            (long $))
        result        (str duration " msecs")]
    (reset! last-time this-time)
    result))

(defn app [req]
  (swap! counter inc)
  ;;(println @counter)
  (let [timing (if (== 0 (mod @counter 100))
                 (duration)
                 "")
        _      (when (not= timing "") (println "t: " timing " msecs for 100 requests"))
        body   (str (format "counter: %,d" (biginteger @counter)) " " timing)]

    {:status 200 :body body :headers {}}))

(defn start-server []
  (reset! server
          (jetty/run-jetty (fn [req] (app req))
                           {:port  (Integer/parseInt (or (System/getenv "PORT") "80"))
                            :join? false})))

(defn stop-server []
  (when-let [s @server]
    (.stop s)
    (let [bs (bean s)]
      (if (or (:stopping bs) (:stopped bs))
        (reset! server nil)
        (println "NOT STOPPED!")))))

(defn -main [& args]
  (println "$PORT = " (System/getenv "PORT") ", default = 80")
  (start-server)
  (println "main thread finished"))

(comment
  (start-server))