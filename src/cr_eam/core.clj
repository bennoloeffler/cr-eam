(ns cr-eam.core
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :as comp]
            [compojure.route :as route]
            [clojure.pprint :as pprint]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [cr-eam.counter :as c]
            [cr-eam.config :as config]
            [hiccup.core :as h]
            [hiccup.element :as he])
  (:gen-class))

;; ring and compojure
;; https://ericnormand.me/guide/clojure-web-tutorial

;; hiccup tips
;; https://ericnormand.me/mini-guide/hiccup-tips

;; bulma
;; https://github.com/theophilusx/bulmaBook/blob/master/resources/public/index.html

;; tutorial luminus, cljs, auth, re-frame
;; https://github.com/aliaksandr-s/prototyping-with-clojure

;; deploy datahike on heroku
;; https://nextjournal.com/kommen/datahike-heroku-datalog-clojure-web-app

(defonce server (atom nil))



(defn get-query-string [req]
  (let [query-str (or (:query-string req) "no query-string found")
        params    (or (:params req) "no params found")]
    (str "query-string: " query-str "<br>params: " params)))

(defn wrap-hiccup [data]
  (h/html
    [:html

     [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title "BELs Experimente"]
      #_[:link {:href "css/style.css" :rel "stylesheet" :type "text/css"}]
      [:link {:rel "icon" :href "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Black_Skull_icon.svg/240px-Black_Skull_icon.svg.png"}]
      [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]
      [:script {:defer "true" :src "https://kit.fontawesome.com/d003778bd2.js" :crossorigin "anonymous"}]]

     [:body

      [:div.box
       [:span.icon [:i.fas.fa-home]]

       [:h1.title [:a {:href "/"} "home"]]
       (if (vector? data)
         data
         (str data))]]]))



(defn home []
  (wrap-hiccup
    [:ul
     [:li [:a {:href "/echo"} "echo request as clojure data"]]
     [:li [:a {:href "/counter"} "count and measure times for 100 requests"]]
     [:li [:a {:href "/query?name=Sabine"} "query with params"]]
     [:li [:a {:href "/about"} "about page"]]
     [:li [:a {:href "/jdbc-url"} "show jdbc-url"]]]))

(comment
  (home))

#_"<h1>Bennos Homepage</h1>
    <ul>
      <li><a href=\"/echo\">Echo request</a></li>
      <li><a href=\"/counter\">Counter</a></li>
      <li><a href=\"/about\">About</a></li>
      <li><a href=\"/query?name=Sabine\">Query</a></li>
    </ul>"


(comp/defroutes routes
                (comp/GET "/" [] {:status  200
                                  :body    (home)
                                  :headers {"Content-Type" "text/html; charset=UTF-8"}})

                (comp/ANY "/echo" req {:status  200
                                       :body    (wrap-hiccup [:pre (with-out-str (pprint/pprint req))])
                                       :headers {"Content-Type" "text/html"}})

                (comp/GET "/counter" [] {:status  200
                                         :body    (wrap-hiccup [:div.box (c/inc-counter)])
                                         :headers {"Content-Type" "text/html"}})

                (comp/GET "/query" req {:status  200
                                        :body    (wrap-hiccup [:div.box (get-query-string req)])
                                        :headers {"Content-Type" "text/html"}})

                (comp/GET "/about" [] {:status  200
                                       :body    (wrap-hiccup "<h3>Bennos kleine Seite...</h3>")
                                       :headers {"Content-Type" "text/html"}})

                (comp/GET "/jdbc-url" [] {:status 200}
                                         :body (wrap-hiccup [:pre (with-out-str (pprint/pprint (:database-url (config/config))))])
                                         :headers {"Content-Type" "text/html"})


                (route/not-found {:status  404
                                  :body    (wrap-hiccup "<h2>Page not found. Hmmm.....</h2>")
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
  (start-server))

(comment
  (start-server)
  (stop-server))