(ns cryptotracker.routes.home
  (:use [hiccup core form util])
  (:require [cryptotracker.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [hiccup.core :as h]
            [hiccup.page :as p]
            [hiccup.element :as e]
            [compojure.core :refer :all]
            [com.hypirion.clj-xchart :as c]))

(import (org.apache.commons.codec.binary Base64))

(defn app-head
  ([]
    (h/html [:head
      (h/html [:meta {:http-equiv "Content-Type"
                            :content    "text/html; charset=UTF-8"
                            :charset    "UTF-8"}]
                    [:meta {:name    "viewport"
                            :content "width=device-width, initial-scale=1"}]
                    [:title "Crypto Tracker"])]))
    ([head-content]
      (h/html [:head
        (h/html [:meta {:http-equiv "Content-Type"
            :content    "text/html; charset=UTF-8"
            :charset    "UTF-8"}]
            [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1"}]
            [:title "Crypto Tracker"]
            head-content)])))

(defn app-body
  ([]
    (h/html [:body]))
    ([body-content]
      (h/html [:body body-content])))

; (defn page []
;   (h/html (app-head)
;   (app-body
;     (form-to [:get "/about"]
;     (text-field {:placeholder "screen name"} "id")
;     (password-field {:placeholder "password"} "pass")
;     (submit-button "about")))))

(def chart
  (c/xy-chart {
                "Expected rate" [ [2 2 3 4 5 6 7 8 9 10] [10 9 8 7 6 5 4 3 2 2] ]
                "Actual rate" [ [2 3 4 5 6 7 8 9 10 11] [2 2 3 4 5 6 7 8 9 10] ]
                "Difference" [ [2 2 3 4 5 6 7 8 9 10] [10 9 8 7 6 5 4 3 2 2] ]
                "Price Rates" [ [2 3 4 5 6 7 8 9 10 11] [2 2 3 4 5 6 7 8 9 10] ]
              }
              {
                :width 1000
                :height 500
                :title "Crypto currency comparison"
                :x-axis {:title "Date"}
                :y-axis {:title "Price [$]"}
                :theme :ggplot2
              }
  )
)

(defn page []
  (h/html (app-head)
    (app-body
      ; (form-to [:get "/compare"]
      ; ())
      [:img {:src (str "data:image/jpeg;base64," (String. (Base64/encodeBase64 (c/to-bytes chart :jpg))))}])))

(defn home-page []
  (page
    (let [resp (http/get "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=3&e=CCCAGG")]
    (apply str (map #(select-keys % [:time])((json/read-str (@resp :body) :key-fn keyword) :Data))))))

(defn about-page []
  (layout/render "about.html"))

(defn test []
  (let [resp (http/get "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=3&e=CCCAGG")
        resp2 (http/get "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=3&e=CCCAGG")]
    (layout/render "about.html" {:resp
      (apply str (map #(select-keys % [:time])((json/read-str (@resp :body) :key-fn keyword) :Data)))
      })))

(defroutes home-routes
  (GET "/" [] (page))
  (GET "/about" [] (test)))
