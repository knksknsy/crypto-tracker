(ns cryptotracker.routes.home
  (:require [cryptotracker.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn test []
  (let [resp1 (http/get "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=3&e=CCCAGG")]
    (println "Response 1's status: " (:status @resp1))
    (layout/render "about.html" {:resp @resp1})))



(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (test)))
