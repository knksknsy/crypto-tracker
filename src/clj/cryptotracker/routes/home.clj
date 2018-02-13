(ns cryptotracker.routes.home
  (:use [hiccup core form util page])
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
                    [:title "Crypto Tracker"]
                    (include-css "/assets/bootstrap/css/bootstrap.min.css")
                    (include-css "/assets/font-awesome/web-fonts-with-css/css/fontawesome-all.min.css")
                    (include-css "/css/screen.css")
                    )]))
    ([head-content]
      (h/html [:head
        (h/html [:meta {:http-equiv "Content-Type"
            :content    "text/html; charset=UTF-8"
            :charset    "UTF-8"}]
            [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1"}]
            [:title "Crypto Tracker"]
            (include-css "/assets/bootstrap/css/bootstrap.min.css")
            (include-css "/assets/font-awesome/web-fonts-with-css/css/fontawesome-all.min.css")
            (include-css "/css/screen.css")
            head-content)])))

(defn app-body
  ([]
    (h/html [:body
              [:nav {:class "navbar navbar-dark bg-primary navbar-expand-md", :role "navigation"}
                [:a {:class "navbar-brand", :href "/"} "Cryptotracker"]
              ]
            ]))
  ([body-content]
    (h/html [:body
              [:nav {:class "navbar navbar-dark bg-primary navbar-expand-md", :role "navigation"}
                [:a {:class "navbar-brand", :href "/"} "Cryptotracker"]
              ]
              body-content
              (include-js "/assets/jquery/jquery.min.js")
              (include-js "/assets/font-awesome/svg-with-js/js/fontawesome.min.js")
              (include-js "/assets/tether/dist/js/tether.min.js")
              (include-js "/assets/bootstrap/js/bootstrap.min.js")
            ])))

(def chart
  (c/xy-chart {
                "Expected rate" [ [10 9 8 7 6 5 4 3 2 2] [2 3 4 5 6 7 8 9 10 10] ]
                "Actual rate" [ [2 3 4 5 6 7 8 9 10 11] [2 2 3 4 5 6 7 8 9 11] ]
                "Difference" [ [2 2 3 4 5 6 7 8 9 11] [2 3 4 5 6 7 8 9 10 11] ]
                "Price Rates" [ [2 2 3 4 5 6 7 8 9 10] [10 9 8 7 6 5 4 3 2 2] ]
              }
              {
                :width 800
                :height 350
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
      [:div {:class "container"}
        (form-to {:class "form-horizontal"} [:get "/compare"]
          [:div {:class "form-group"}
            (label {:class "control-label"} "cur1" "Currency 1")
            [:select#cur1 {:name "cur1", :class "form-control"}
              (select-options [["BitCoin" "BTC"] ["Ethereum" "ETH"]])
            ]
          ]
          [:div {:class "form-group"}
            (label {:class "control-label"} "cur2" "Currency 2")
            [:select#cur2 {:name "cur2", :class "form-control"}
              (select-options [["BitCoin" "BTC"] ["Ethereum" "ETH"]])
            ]
          ]
          [:div {:class "row"}
            [:div {:class "col-6"}
              (label {:class "control-label"} "options" "Compare options:")
              [:div#options {:class "form-group "}
                [:div {:class "form-check form-check-inline"}
                  (check-box {:class "form-check-input"} "comp1" false)
                  (label {:class "form-check-label"} "comp1" "Difference")
                ]
                [:div {:class "form-check form-check-inline"}
                  (check-box {:class "form-check-input"} "comp2" false)
                  (label {:class "form-check-label"} "comp2" "Price Rate")
                ]
              ]
            ]
            [:div {:class "form-group col-6"}
              (submit-button {:class "btn btn-primary float-right"} "Compare")
            ]
          ])
          [:div {:class "chart-container"}
            [:img {:src (str "data:image/svg+xml;base64," (String. (Base64/encodeBase64 (c/to-bytes chart :svg)))), :class "img-responsive"}]
          ]
      ])))

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

(defn compare []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (page))
  (GET "/about" [] (test))
  (GET "/compare" [] (compare)))
