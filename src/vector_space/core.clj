(ns vector-space.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (use vector-space.cran-parser)
  (use vector-space.index)
  (use vector-space.model)
  (use ring.util.response))

(def file-data (parse-cran-file "cran-test.txt"))
(def index (make-index file-data))

;;(def queries (get-queries))

(defroutes app-routes
  (GET "/" [] (redirect "index.html"))
  (route/resources "/")
  (route/not-found "Error, page not found!"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3888"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
