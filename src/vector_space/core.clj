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

(def file-data    (parse-cran-file "cran.all.1400"))
(def index        (make-index file-data))
(def test-queries (let [queries (parse-cran-file "cran.qry")] (map #(hash-map :num %1, :q %2) (:ids queries) (:content queries))))

(defn document-by-id
  [id]
  (let [document (get (:content file-data) (dec id))
        title    (get (:title file-data) (dec id))]
    {:content (str "Title: " title "\n" document)}))

(defn get-test-queries
  [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str test-queries))})

(defn query-result
  [req]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (str (json/write-str (get-ranked-documents index (:query (:params req)))))})

(defn document-page
  [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str (document-by-id (Integer. (:docID (:params req))))))})

(defroutes app-routes
  (GET "/" [] (redirect "index.html"))
  (GET "/queries" [req] get-test-queries)
  (GET "/searchResult" [req] query-result)
  (GET "/document" [req] document-page)
  (route/resources "/")
  (route/not-found "Error, page not found!"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3888"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
