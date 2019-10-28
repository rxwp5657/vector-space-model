(ns vector-space.cran-parser)

(defn dump-buffer
  "Add the contents of the buffer to the corresponding entry"
  [structure]
  (update structure (keyword (:current structure)) #(conj % (apply str (:buffer structure)))))

(defn process-id
  "Add a new file id to the ids data structure"
  [file-structure id]
  (let [new-state    (update file-structure :current (fn [actual] "ids"))
        empty-buffer (update new-state :buffer (fn [actual] []))
        add-id       (update empty-buffer :ids #(conj % id))]
    (if (not (empty? (:buffer file-structure)))
      (update add-id (keyword (:current file-structure)) #(conj % (apply str (:buffer file-structure))))
      add-id)))

(defn set-state-to
  "Change the state of the buffer"
  [state structure]
  (let [empty-buffer (dump-buffer structure)
        set-buffer   (update empty-buffer :buffer (fn [actual] []))]
    (update set-buffer :current (fn [actual] state))))

(defn construct-buffer
  "Keep adding information to the buffer"
  [strucure line]
  (update strucure :buffer #(conj % (str " " line))))

(defn- process-line
  "Given a line set the data structure to the state:
   id, title, author, content or something and add the data
   to the correspoinding field"
  [file-structure line]
  (let [tokens (clojure.string/split line #"\s+")]
    (cond
      (= ".I" (first tokens)) (process-id file-structure (last tokens))
      (= ".T" (first tokens)) (set-state-to "titles"    file-structure)
      (= ".A" (first tokens)) (set-state-to "authors"   file-structure)
      (= ".B" (first tokens)) (set-state-to "something" file-structure)
      (= ".W" (first tokens)) (set-state-to "content"   file-structure)
      :else (construct-buffer file-structure line))))

(defn parse-cran-file
  "Parse the 'cran' file by retrieving all it's documents"
  [file-name]
  (with-open [rdr (clojure.java.io/reader (clojure.java.io/resource file-name))]
    (let [lines (reduce conj [] (line-seq rdr))]
      (loop [actual-line (first lines)
             rest-lines  (rest lines)
             parsed-file {:ids [] :titles [] :authors [] :something [] :content [] :current "ids" :buffer []}]
        (if (nil? actual-line)
          (let [res (dump-buffer parsed-file)]
            {:ids (filter #(not (clojure.string/blank? %)) (:ids res)) :titles (:titles res) :authors (:authors res) :something (:something res) :content (:content res)})
          (recur (first rest-lines) (rest rest-lines) (process-line parsed-file actual-line)))))))
