(ns pl.tomaszgigiel.abc-xml.clj-xpath
  (:require [clojure.string :as string])
  (:require [clojure.tools.logging :as log])
  (:require [clj-xpath.core :as xpath])
  (:gen-class))

;; https://github.com/kyleburton/clj-xpath
;; http://kyleburton.github.io/clj-xpath/site/
;; https://github.com/clojure/clojure/blob/clojure-1.9.0/src/clj/clojure/core.clj#L4871

(defn xpath-leaf?
  [n]
  (empty? (xpath/$x "./*" n)))

(defn xpath-seq
  [xml]
  (tree-seq (fn [n] (:node n))            ; branch?: nil or node with children or not
            (fn [n] (xpath/$x "./*" n))   ; children: "./*" - children of current
            (first (xpath/$x "/*" xml)))) ; root: "/*" - top

;; disadvantages: traversing twice, tree, list
(defn xpath-transformed-seq
  [xml f]
  (map f (xpath-seq xml)))

(defn tree-ancestry-seq
  [branch? children root]
  (let [walk
        (fn walk [node ancestors] 
          (lazy-seq 
            (conj (when (branch? node) (mapcat (fn [n] (walk n (conj ancestors node)))(children node)))
                  {:node node :ancestors ancestors})))]
    (walk root [])))

(defn xpath-ancestry-seq
  [xml]
  (tree-ancestry-seq (fn [n] (:node n))            ; branch?: nil or node
                     (fn [n] (xpath/$x "./*" n))   ; children: "./*" - children of current
                     (first (xpath/$x "/*" xml)))) ; root: "/*" - top

;; disadvantages: traversing twice, tree, list
(defn xpath-ancestry-transformed-seq
  [xml f]
  (map f (xpath-ancestry-seq xml)))

(defn kyleburton-alternative-all-tags
  [xml]
  (distinct (xpath-transformed-seq xml (fn [n] (name (:tag n))))))

(defn kyleburton-alternative-all-paths
  [xml]
  (let [node-tag (fn [n] (str "/" (name (:tag n))))
        node-tag-path (fn [ns] (string/join (map node-tag ns)))
        full-path (fn [m] (str (node-tag-path (:ancestors m)) (node-tag (:node m))))]
    (distinct(xpath-ancestry-transformed-seq xml full-path))))

(defn path-text-pairs
  [xml]
  (let [node-tag (fn [n] (str "/" (name (:tag n))))
        node-tag-path (fn [ns] (string/join (map node-tag ns)))
        full-path (fn [m] (str (node-tag-path (:ancestors m)) (node-tag (:node m))))
        node-text (fn [m] (:text (:node m)))
        path-text-pair (fn [m] (when (xpath-leaf? m) {:path (full-path m) :text (node-text m)}))]
    (filter some? (xpath-ancestry-transformed-seq xml path-text-pair))))
