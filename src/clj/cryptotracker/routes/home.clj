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

(def btcUrl   "https://min-api.cryptocompare.com/data/histoday?fsym=BTC&tsym=USD&limit=60&aggregate=1") ;BTC-USD
(def ethUrl   "https://min-api.cryptocompare.com/data/histoday?fsym=ETH&tsym=USD&limit=60&aggregate=1") ;ETH-USD
(def dashUrl  "https://min-api.cryptocompare.com/data/histoday?fsym=DASH&tsym=USD&limit=60&aggregate=1") ;ETH-USD
(def coinsUrl "https://www.cryptocompare.com/api/data/coinlist/"); Coinlist

(defn prepare-data-for-exlist [data]
  (into [] (map :FullName (vals ((json/read-str (@data :body) :key-fn keyword) :Data))))
)
  ;(str \[ (clojure.string/join ", " (map :FullName (vals ((json/read-str (@data :body) :key-fn keyword) :Data)))))
  ;(str \[ (clojure.string/join ", " (map :Symbol (vals ((json/read-str (@data :body) :key-fn keyword) :Data)))))



(defn average [numbers]
  (/ (apply + numbers) (count numbers))
)

(defn format-cur [number]
  (str (format "%.2f" number) " $")
)

(defn chart [cur1 cur1Times cur1Closes cur2 cur2Times cur2Closes]
  (c/xy-chart
    {
      (str cur1 " " "\nmin: " (format-cur (apply min cur1Closes)) "\nmax: " (format-cur (apply max cur1Closes)) "\naverage: " (format-cur (average cur1Closes)))
      {
        :x cur1Times
        :y cur1Closes
        :style { :marker-type :none }
      }
      (str cur2 "\nmin: " (format-cur(apply min cur2Closes)) "\nmax: " (format-cur (apply max cur2Closes)) "\naverage: " (format-cur (average cur2Closes)))
      {
        :x cur2Times
        :y cur2Closes
        :style { :marker-type :none }
      }
      (str cur1 " - " cur2)
      {
        :x cur2Times
        :y (map - cur1Closes cur2Closes)
        :style { :marker-type :none }
      }
    }
    {
      :width 1050
      :height 400
      :title (str "Comparison of " cur1 " and " cur2)
      :x-axis { :title "Date" :ticks-visible? true :tick-mark-spacing-hint 20 }
      :y-axis { :title "Price [$]" :decimal-pattern "######" :tick-mark-spacing-hint 20 }
      :theme :ggplot2
      :date-pattern "dd.MM.yyyy"
    }
  )
)

(defn empty-chart []
  (c/xy-chart
    {}
    {
      :width 1050
      :height 400
      :title "Select crypto currencies to compare"
      :x-axis { :title "Date" :ticks-visible? true :tick-mark-spacing-hint 20 }
      :y-axis { :title "Price [$]" :decimal-pattern "######" :tick-mark-spacing-hint 20 }
      :theme :ggplot2
      :date-pattern "dd.MM.yyyy"
    }
  )
)

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

(defn base-page [chart-container respCoinlist]
  (h/html (app-head)
    (app-body
      [:div {:class "container"}
        (form-to {:class "form-horizontal"} [:get "/compare"]
          [:div {:class "form-group"}
            (label {:class "control-label"} "cur1" "Currency 1")
            [:select#cur1 {:name "cur1", :class "form-control"}
              (select-options [respCoinlist])
            ]
          ]
          [:div {:class "form-group"}
            (label {:class "control-label"} "cur2" "Currency 2")
            [:select#cur2 {:name "cur2", :class "form-control"}
              (select-options [["BitCoin" "BTC"] ["Ethereum" "ETH"] ["LiteCoin" "LTC"]])
            ]
          ]
          [:div {:class "row"}
            [:div {:class "col-6"}
              [:div {:class "form-group"}
                (label {:class "control-label"} "range" "Range of days")
                [:select#cur1 {:name "range", :class "form-control"}
                  (select-options [["7" 7] ["30" 30] ["365" 365]])
                ]
              ]
            ]
            [:div {:class "form-group col-6"}
              (submit-button {:class "btn btn-primary float-right"} "Compare")
            ]
          ])
          chart-container
      ]
    )
  )
)

(defn home-page []
  (base-page
    [:div {:class "chart-container"}
      [:img {:src (str "data:image/svg+xml;base64," (String. (Base64/encodeBase64 (c/to-bytes (empty-chart) :svg)))), :class "img-responsive"}]
    ]
  )
)

(defn compare-page [cur1 respCur1Times respCur1Closes cur2 respCur2Times respCur2Closes respCoinlist]
  (base-page respCoinlist
    [:div {:class "chart-container"}
      [:img {:src (str "data:image/svg+xml;base64," (String. (Base64/encodeBase64 (c/to-bytes (chart cur1 respCur1Times respCur1Closes cur2 respCur2Times respCur2Closes) :svg)))), :class "img-responsive"}]
    ]
  )
)

(defn format-date [unixdates]
  (lazy-seq
    (when-let [ss (seq unixdates)]
      (cons  (tc/to-date (* (first unixdates) 1000 ))
        (format-date (next unixdates)))))
)

(defn get-data-times [data]
  (format-date (map :time ((json/read-str (@data :body) :key-fn keyword) :Data)))
)

(defn get-data-closes [data]
  (map :close ((json/read-str (@data :body) :key-fn keyword) :Data))
)

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/compare" [cur1 cur2 range]
    (let [respCur1Times   (get-data-times (http/get (str "https://min-api.cryptocompare.com/data/histoday?fsym=" cur1 "&tsym=USD&limit=" range "&aggregate=1")))
          respCur1Closes  (get-data-closes (http/get (str "https://min-api.cryptocompare.com/data/histoday?fsym=" cur1 "&tsym=USD&limit=" range "&aggregate=1")))
          respCur2Times   (get-data-times (http/get (str "https://min-api.cryptocompare.com/data/histoday?fsym=" cur2 "&tsym=USD&limit=" range "&aggregate=1")))
          respCur2Closes  (get-data-closes (http/get (str "https://min-api.cryptocompare.com/data/histoday?fsym=" cur2 "&tsym=USD&limit=" range "&aggregate=1")))
          respCoinlist (prepare-data-for-exlist (http/get coinsUrl))]
      (compare-page cur1 respCur1Times respCur1Closes cur2 respCur2Times respCur2Closes respCoinlist)
    )
  )
)
