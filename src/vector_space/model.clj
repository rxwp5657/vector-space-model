(ns vector-space.model)

(defn weight
  "Calculate weight of term"
  [index word]
  (let [posting-list (:lst ((keyword word) (:dictionary index)))
        number-docs (:num-files index)
        dft (count posting-list)]
    (if (= dft 0)
      0
      (Math/log10 (/ number-docs dft)))))

(defn dot
  "Dot product"
  [v1 v2]
  (let [mult (map * v1 v2)]
    (reduce + mult)))

(defn get-weight
  "If the word exist on the document, Calculate its weight else, cero"
  [index num-doc word]
  (let [posting-list (:lst ((keyword word) (:dictionary index)))
        appears (filter #(= (str num-doc) (:file-id %)) posting-list)]
    (if (not (empty? appears))
      (* (weight index word) (:occ (first appears)))
      0)))

(defn- doc-vector
  "Given a document and a list of words, return its vector representation"
  [index words num-doc]
  (into [] (map #(get-weight index num-doc %) words)))

(defn get-doc-vectors
  "Get all the vector representation of all files"
  [index words]
  (map #(doc-vector index words %) (range 1 (inc (:num-files index)))))

(defn- clean-string
  "return a string that only contains characters from a - z and A - Z"
  [str]
  (clojure.string/replace str #"[^a-zA-Z]" ""))

(defn query-vector
  "Transorm the query to the vector form"
  [index words]
  (into [] (map #(weight index %) words)))

(defn ranked-values
  "Get the ranked list"
  [index query]
  (let [tokens (clojure.string/split query #"\s+")
        words  (map #(clean-string %) tokens)
        query-v (query-vector index words)
        docs-v  (get-doc-vectors index words)
        ranks (map #(hash-map :id (str %3) :rank (dot %1 %2)) (take (:num-files index) (repeat query-v)) docs-v (range 1 (inc (:num-files index))))]
    (sort-by :rank > ranks)))

(defn get-ranked-documents
  [index query]
  (let [ranked-v (ranked-values index query)]
    (map #(hash-map :title ((keyword (:id %)) (:file-data index)) :id (:id %) :rank (:rank %)) ranked-v)))
