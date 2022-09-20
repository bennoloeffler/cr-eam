(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "2 BEL: Hello, World!")
  (jetty/run-jetty (fn [req] {:status 200 :body "BELHello" :headers {}})  ;; a really basic handler
                   {:port (Integer/parseInt (System/getenv "PORT"))     ;; listen on port 3001
                    :join? false})
  (println "main thread finished"))

   ;; don't block the main thread)

