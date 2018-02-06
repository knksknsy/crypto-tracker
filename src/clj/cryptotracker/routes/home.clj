(ns cryptotracker.routes.home
  (:require [cryptotracker.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn test []
  (let [resp (http/get "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=3&e=CCCAGG")
        resp2 (http/get "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=3&e=CCCAGG")]
    (layout/render "about.html" {:resp
      (apply str (map #(select-keys % [:time])((json/read-str (@resp :body) :key-fn keyword) :Data)))
      })))



(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (test)))
