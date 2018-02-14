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
            [com.hypirion.clj-xchart :as c]
            [clj-time.coerce :as tc]))

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

(defn format-date [unixdates]
  (lazy-seq
    (when-let [ss (seq unixdates)]
      (cons  (tc/to-date (* (first unixdates) 1000 ))
        (format-date (next unixdates)))))
)

(defn get-data-times [data]
      (format-date (map :time  ((json/read-str (@data :body) :key-fn keyword) :Data)))
)

(defn get-data-closes [data]
       (map :close  ((json/read-str (@data :body) :key-fn keyword) :Data))
)

(def url1  "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=1") ;BTC-USD
(def url2  "https://min-api.cryptocompare.com/data/histoday?fsym=ETH&tsym=USD&limit=60&aggregate=1") ;ETH-USD
(def url3  "https://min-api.cryptocompare.com/data/histoday?fsym=DASH&tsym=USD&limit=60&aggregate=1") ;ETH-USD
(def url4 "https://www.cryptocompare.com/api/data/coinlist/"); Coinlist

(defn chart []
  (let [resp1 (http/get url1)
       resp2 (http/get url2)
       resp3 (http/get url3)
       resp4 (http/get url4)]
  (c/xy-chart {
                ;"Cur 1" [ [(tc/to-date 1502841600000) (tc/to-date 1502842600000)] [2 3] ]
                ;"Cur 1" [ [(prepare-data-for-chart-wo-comma2 data)] [(prepare-data-for-chart-wo-comma2 data)] ]
                ;"Cur 1"  { :x (prepare-data-for-chart-wo-comma resp1) :y (prepare-data-for-chart-wo-comma2 resp1) }
                "Cur 2"  { :x (get-data-times resp2) :y (get-data-closes resp2) }
                "Cur 3"  { :x (get-data-times resp3) :y (get-data-closes resp3) }
                ;"Cur 2" [ [(tc/to-date 1502841600000) (tc/to-date 1502842600000)] [3 4] ]
                ;"Dif" [ [(tc/to-date 1502841600000) (tc/to-date 1502842600000)] [5 6] ]
                ;"Rates" [ [(tc/to-date 1502841600000) (tc/to-date 1502842600000)] [7 8] ]
              }
              {
                :width 800
                :height 350
                :title "Crypto currency comparison"
                :x-axis {:title "Date"}
                :y-axis {:title "Price [$]"}
                :theme :ggplot2
                :date-pattern "dd.MM.yyyy HH:mm"
              }
  )
))


(defn page []
  ; (let [resp (http/get ChartUrl2)]

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
            [:img {:src (str "data:image/svg+xml;base64," (String. (Base64/encodeBase64 (c/to-bytes (chart) :svg)))), :class "img-responsive"}]
          ]
      ])))

(defroutes home-routes
  (GET "/" [] (page))
  (GET "/compare" [cur1 cur2 comp1 comp2]
    (str "<h1>" " Currency1: " cur1 "<br>Currency2: " cur2 "<br>Option1: " comp1 "<br>Option2: " comp2 "</h1>")
  ))
