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

(defn perform-nodes
  [xml fun]
  (let [nodes (tree-seq (fn [n] (:node n)) ; (:node n) - $x:node, nil or node, branch?
                        (fn [n] (xpath/$x "./*" n)) ; "./*" - children of current
                        (first (xpath/$x "/*" xml)))] ; "/*" - top
  (remove nil? (distinct (map fun nodes)))))

(defn all-tags
  [xml]
  (perform-nodes xml (fn [n] (:tag n))))

(defn path-node-pairs
  [xml]
  (let [nodes (xpath/$x "/*" xml)]
    ()))

(defn visit-nodes
  [path nodes f]
  (let [current-path (fn[path n] (str path "/" (name(:tag n))))]
    (mapcat (fn [n] (do (f (current-path path n) n)(visit-nodes (current-path path n) (xpath/$x "./*" n) f))) nodes)))

(defn leaf?
  [n]
  (empty? (xpath/$x "./*" n)))
