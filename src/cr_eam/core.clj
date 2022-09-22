(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty])
  (:gen-class))
(def counter (atom 0M))
(def last-time (atom 0))

(defn duration []
  (let [this-time (System/currentTimeMillis)
        duration (as-> @last-time $
                       (- this-time $)
                       (long $))]
    (when (not= @last-time 0)
      (println (str duration "msecs for 100 requests")))
    (reset! last-time this-time)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "$PORT = " (System/getenv "PORT") ", default = 8080")

  (future (while true
            (Thread/sleep 1000)
            (swap! counter dec)))

  (jetty/run-jetty (fn [req]
                     ;(println (mod @counter 100))
                     (when (== 0 (mod @counter 100))
                       (duration))
                     (swap! counter inc)
                     ;;(println @counter)
                     {:status 200 :body (str "count CR-EAM :-)   " @counter) :headers {}}) ;; a really basic handler
                   {:port  (Integer/parseInt (or (System/getenv "PORT") "80")) ;; listen on port 3001
                    :join? false})
  (println "main thread finished"))

;; don't block the main thread)

