(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty])
  (:gen-class))

;; https://ericnormand.me/guide/clojure-web-tutorial

(def server (atom nil))

(def counter (atom 0M))
(def last-time (atom 0))

(defonce count-down
         (future (while true
                   (Thread/sleep 1000)
                   (swap! counter dec))))

(defn duration []
  (let [this-time (System/currentTimeMillis)
        duration (as-> @last-time $
                       (- this-time $)
                       (long $))]
    (when (not= @last-time 0)
      (println (str duration "msecs for 100 requests")))
    (reset! last-time this-time)))

(defn app [req]
    ;(println (mod @counter 100))
    (when (== 0 (mod @counter 100))
      (duration))
    (swap! counter inc)
    {:status 200 :body (str "count CR-EAM :-)   " @counter) :headers {}}) ;; a really basic handler

(defn start-server []
  (reset! server
          (jetty/run-jetty (fn [req](app req))
                           {:port  (Integer/parseInt (or (System/getenv "PORT") "80")) ;; listen on port 3001
                            :join? false})))

(defn stop-server []
  (when-let [s @server]
    (.stop s)
    (reset! server nil)))

(defn -main [& args]
  (println "$PORT = " (System/getenv "PORT") ", default = 80")
  (start-server)
  (println "main thread finished"))

