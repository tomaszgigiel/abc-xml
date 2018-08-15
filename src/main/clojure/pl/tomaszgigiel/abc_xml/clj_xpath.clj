(ns pl.tomaszgigiel.abc-xml.clj-xpath
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string])
  (:require [clojure.tools.logging :as log])
  (:require [clj-xpath.core :as xpath])
  (:gen-class))

;; https://github.com/kyleburton/clj-xpath
;; http://kyleburton.github.io/clj-xpath/site/

(defn xml-string-by-resource
  [r]
  (slurp (io/resource r)))

(defn all-tags
  [xml]
  (let [nodes (tree-seq (fn [n] (:node n)) ; (:node n) - $x:node, nil or node
                        (fn [n] (xpath/$x "./*" n)) ; "./*" - children of current
                        (first (xpath/$x "/*" xml))) ; "/*" - top
        fun (fn [n] (:tag n))]
  (distinct (map fun nodes))))

(defn visit-nodes
  [path nodes f]
  (let [current-path (fn[path n] (str path "/" (name(:tag n))))]
    (mapcat (fn [n] (do (f (current-path path n) n)(visit-nodes (current-path path n) (xpath/$x "./*" n) f))) nodes)))

(defn leaf?
  [n]
  (empty? (xpath/$x "./*" n)))
