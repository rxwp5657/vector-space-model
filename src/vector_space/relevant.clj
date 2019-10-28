(ns vector-space.relevant)

(defn- process-line
  "Process a relevant file for a query"
  [file-structure line]
  (let [tokens   (clojure.string/split line #"\s+")
        query-id (first tokens)
        relevant-doc (second tokens)]
    (if (= "none" (get file-structure (keyword query-id) "none"))
      (assoc  file-structure (keyword query-id) [relevant-doc])
      (update file-structure (keyword query-id) #(conj % relevant-doc)))))

(defn parse-relevant-file
  "Parse the 'cran' file by retrieving all it's documents"
  [file-name]
  (with-open [rdr (clojure.java.io/reader (clojure.java.io/resource file-name))]
    (let [lines (reduce conj [] (line-seq rdr))]
      (loop [actual-line (first lines)
             rest-lines  (rest lines)
             parsed-file {}]
        (if (nil? actual-line)
          parsed-file
          (recur (first rest-lines) (rest rest-lines) (process-line parsed-file actual-line)))))))

(defn- relevant?
  "Check if a doc on the result set is relevant"
  [relevant-list search-doc]
  (let [result (some #(= (str (:id search-doc)) %) relevant-list)]
    (if (nil? result) false true)))

(defn precision
  "ratio of the number of relevant documents retrieved to the total number retrieved."
  [relevant-index num-query search-results]
  (let [relevant-lst  ((keyword (str (Integer. num-query))) relevant-index)
        num-retrieved (count search-results)
        num-relevant  (count (filter #(relevant? relevant-lst %) search-results))]
    (double (/ num-relevant num-retrieved))))

(defn recall
  "ratio of number of relevant documents retrieved
   to the total number of documents in the collection that are
   believed to be relevant."
  [relevant-index num-query search-results]
  (let [relevant-lst  ((keyword (str (Integer. num-query))) relevant-index)
        num-relevant  (count relevant-lst)
        num-relevant-retrieved (count (filter #(relevant? relevant-lst %) search-results))]
    (double (/ num-relevant-retrieved num-relevant))))

(defn query-precision-recall
  [relevant-index query-num search-results]
  (loop [simulated-search-results [(first search-results)]
         rest-search-results (rest search-results)
         precision-results []
         recall-results    []]
    (if (= (count search-results) (count simulated-search-results))
      {:precision precision-results :recall recall-results}
      (recur
        (conj simulated-search-results (first rest-search-results))
        (rest rest-search-results)
        (conj precision-results (precision relevant-index query-num simulated-search-results))
        (conj recall-results    (recall relevant-index query-num simulated-search-results))))))
