(ns user
  (:require 
            [mount.core :as mount]
            [cryptotracker.core :refer [start-app]]))

(defn start []
  (mount/start-without #'cryptotracker.core/repl-server))

(defn stop []
  (mount/stop-except #'cryptotracker.core/repl-server))

(defn restart []
  (stop)
  (start))


