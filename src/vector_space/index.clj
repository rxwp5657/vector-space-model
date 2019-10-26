(ns vector-space.index)

(defn clean-string
  "return a string that only contains characters from a - z and A - Z"
  [str]
  (clojure.string/replace str #"[^a-zA-Z]" ""))
;; last (:lst ((keyword word) index)))

(defn update-file-word-occurrence
  "update the file word count"
  [index word]
  (let [entry (last (:lst ((keyword word) index)))
        new-entry (update-in entry [:occ] inc)]
    (update-in index [(keyword word) :lst] #(conj (into [] (drop-last %)) new-entry))))

(defn add-entry
  "Check if word is already in dictionary if it is, append the file id to the
   posting list. Otherwise, add as new entry"
  [index word file-id]
  (cond
    (= "missing" (get index (keyword word) "missing")) (assoc index (keyword word) {:lst [{:file-id file-id :occ 1}]})
    (= file-id (:file-id (last (:lst ((keyword word) index))))) (update-file-word-occurrence index word)
    :else (let [updated-list (update-in index [(keyword word) :lst] #(conj % {:file-id file-id :occ 1}))]
            updated-list)))

(defn process-index
  "Given a collection of strings, add all of them as keys and append the
   file id to the posting list"
  [index words file-id]
  (reduce #(add-entry %1 %2 file-id) index words))

(defn process-file
  "Read, tokenize and add entries to the dictionary"
  [index file file-id]
  (let [tokens (clojure.string/split file #"\s+")
        clean-tokens (map #(clean-string %) tokens)]
    (process-index index clean-tokens file-id)))

(defn process-file-id
  "Maintain a map that contains the id of the file and it's name"
  [file-index file-name file-id]
  (assoc file-index (keyword (str file-id)) file-name))

(defn make-file
  "Concat the author, title and content into a single string to be processed"
  [title content]
  (str title " " content))

(defn make-index
  "Make the dictionary and posting list"
  [file-data]
  (let [ids      (:ids file-data)
        titles   (:titles file-data)
        contents (:content file-data)]
    (loop [id       (first ids)
           rest-ids (rest ids)
           title    (first titles)
           rest-titles  (rest titles)
           content  (first contents)
           rest-contents (rest contents)
           index {}
           file-index {}]
      (if (nil? title)
        {:dictionary index :file-data file-index :num-files (count ids)}
        (recur (first rest-ids)
               (rest  rest-ids)
               (first rest-titles)
               (rest  rest-titles)
               (first rest-contents)
               (rest  rest-contents)
          (process-file index (make-file title content) id)
          (process-file-id file-index title id))))))
