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

(defn leaf?
  [n]
  (empty? (xpath/$x "./*" n)))

;; https://github.com/clojure/clojure/blob/clojure-1.9.0/src/clj/clojure/core.clj#L4871
(defn tree-seq-ancestry
  [branch? children root]
  (let [walk
        (fn walk [node parents] 
          (lazy-seq 
            (cons {node parents} 
                  (when (branch? node)
                    (mapcat (fn [n] (walk n (cons n parents)))(children node))))))]
    (walk root (list))))

;; disadvantages: traversing twice, tree, list
(defn xpath-seq-ancestry
  [xml]
  (let [nodes (tree-seq-ancestry (fn [n] (:node n))            ; branch?: nil or node
                                 (fn [n] (xpath/$x "./*" n))   ; children: "./*" - children of current
                                 (first (xpath/$x "/*" xml)))] ; root: "/*" - top
    (remove nil? (distinct nodes))))

;; disadvantages: traversing twice, tree, list
(defn xpath-seq-transition
  [xml f]
  (let [nodes (tree-seq (fn [n] (:node n))            ; branch?: nil or node
                        (fn [n] (xpath/$x "./*" n))   ; children: "./*" - children of current
                        (first (xpath/$x "/*" xml)))] ; root: "/*" - top
  (remove nil? (distinct (map f nodes)))))

;; disadvantages: traversing twice, tree, list
(defn xpath-seq-ancestry-transition
  [xml f]
  (let [nodes])
  (xpath-seq-transition xml f))
  
  (let [nodes (tree-seq-ancestry (fn [n] (:node n))            ; branch?: nil or node
                                 (fn [n] (xpath/$x "./*" n))   ; children: "./*" - children of current
                                 (first (xpath/$x "/*" xml)))] ; root: "/*" - top
    (remove nil? (distinct nodes))))




(defn all-tags
  [xml]
  (xpath-seq-transition xml (fn [n] (:tag n))))

(defn all-paths
  [xml]

  
  ;;(tree-seq-ancestry xml (fn [n] (:tag n))))


;;;
(defn my-xml [] (xml-string-by-resource "simple.xml"))
(all-tags (my-xml))

(first (all-paths (my-xml)))
