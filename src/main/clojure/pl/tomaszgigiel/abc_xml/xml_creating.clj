(ns pl.tomaszgigiel.abc-xml.xml-creating
  (:require [clojure.data.xml :as xml])
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string])
  (:require [clojure.tools.logging :as log])
  (:gen-class))

;; (take 30 (abc-seq))
(defn abc-seq
  ([] (abc-seq \A \Z))
  ([min max]
    (let [min-int (int min)
          max-int (int max)
          next-item (fn next-item [v]
                      (let [last (peek v)
                            next? (= last max-int)
                            new (if next? min-int (+ last 1))
                            beginning (if next? v (pop v))]
                        (conj beginning new)))]
      (map (fn [v] (apply str (map char v))) (iterate next-item [(int min)])))))


;; (take 30 (tag-seq))
(defn tag-seq [] (map #(string/replace (format "%10s" %) #" " "k") (abc-seq)))

;; (take 30 (val-seq))
(defn val-seq [] (map #(string/replace (format "%20s" %) #" " "x") (abc-seq)))

;; (println (xml/emit-str (sample-xml-trapeze 2 3 15 (tag-seq) (val-seq))))
(defn sample-xml-trapeze
  [b h count tags vals]
  (let [generator-line (fn generator-line [b h tags vals]
                         (let [tag (str (first tags))]
                           (cond
                             (> b 1) (xml/element tag {} (generator-line (- b 1) h (rest tags) vals))
                             (= b 1) (xml/element tag {} "\n" (generator-line (- b 1) h (rest tags) vals))
                             (= h 0) ()
                             (= b 0) (cons (xml/element tag {} (first vals)) (generator-line b (- h 1) (rest tags) (rest vals))))))
        geneator-lines (fn generator-lines [b h count tags vals]
                         (cons (generator-line b h tags vals) (if (> count 1) (generator-lines b h (- count 1) tags vals) ())))]
    (xml/element (str (first tags)) {} (geneator-lines b h count (rest tags) vals))))

;; (spit "sample-trapeze.xml" (xml/emit-str (sample-xml-trapeze 2 5 7 (tag-seq) (val-seq))))
;; (spit "sample-trapeze.xml" (xml/emit-str (sample-xml-trapeze 25 25 500 (tag-seq) (val-seq))))
