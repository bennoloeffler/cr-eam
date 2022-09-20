(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty])
  (:gen-class))
(def counter (atom 0))
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "2 BEL: Hello, World!")
  (jetty/run-jetty (fn [req]
                     (swap! counter inc)
                     {:status 200 :body (str "BEL-Hello <br/><br/> " @counter) :headers {}})  ;; a really basic handler
                   {:port (Integer/parseInt (System/getenv "PORT"))     ;; listen on port 3001
                    :join? false})
  (println "main thread finished"))

   ;; don't block the main thread)

