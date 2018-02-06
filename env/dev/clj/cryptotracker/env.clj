(ns cryptotracker.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [cryptotracker.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[cryptotracker started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[cryptotracker has shut down successfully]=-"))
   :middleware wrap-dev})
