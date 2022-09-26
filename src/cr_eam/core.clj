(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :as comp]
            [compojure.route :as route]
            [clojure.pprint :as pprint]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [cr-eam.counter :as c])
  (:gen-class))

;; ring and compojure
;; https://ericnormand.me/guide/clojure-web-tutorial

;; tutorial luminus, cljs, auth, re-frame
;; https://github.com/aliaksandr-s/prototyping-with-clojure

;; deploy datahike on heroku
;; https://nextjournal.com/kommen/datahike-heroku-datalog-clojure-web-app

(defonce server (atom nil))


(defn get-query-string [req]
  (let [query-str (or (:query-string req) "no query-string found")
        params    (or (:params req) "no params found")]
    (str "query-string: " query-str "<br>params: " params)))


(comp/defroutes routes
                (comp/GET "/" [] {:status  200
                                  :body    "<h1>Bennos Homepage</h1>
                                <ul>
                                    <li><a href=\"/echo\">Echo request</a></li>
                                    <li><a href=\"/counter\">Counter</a></li>
                                    <li><a href=\"/about\">About</a></li>
                                    <li><a href=\"/query?name=Sabine\">Query</a></li>\n                                </ul>"
                                  :headers {"Content-Type" "text/html; charset=UTF-8"}})

                (comp/ANY "/echo" req {:status  200
                                       :body    (with-out-str (pprint/pprint req))
                                       :headers {"Content-Type" "text/plain"}})
                (comp/GET "/counter" [] (c/inc-counter))
                (comp/GET "/about" [] "<h1>Bennos kleine Seite...</h1>")
                (comp/GET "/query" [] get-query-string)
                (route/not-found {:status  404
                                  :body    "<h2>Page not found. Hmmm.....</h2>"
                                  :headers {"Content-Type" "text/html"}}))


;; enroll :query-string by middleware wrap-params from query-string to :query-params (string, string) to :params (keyword, string)
;(def app (wrap-params (wrap-keyword-params routes)))
(def app
  (-> (fn [req] (routes req)) ; in order to load new middleware without restarting jvm
      wrap-keyword-params
      wrap-params))


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