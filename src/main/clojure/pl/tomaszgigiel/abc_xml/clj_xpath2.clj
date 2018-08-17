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


(defn almost
  [nodes path]
  (map (fn [n] {(str path (name (:tag n))) n}) nodes))

(defn path-node-pairs
  ([xml] 
    (path-node-pairs (xpath/$x "/*" xml) ""))
  ([nodes path]
    (conj (almost nodes path) 
          {}
          ;;(almost (xpath/$x "./*" nodes) (str path "/" "aaa"))
          )))


(defn my-xml [] (xml-string-by-resource "simple.xml"))
(defn my-nodes [] (xpath/$x "/*" (my-xml)))
(defn my-nodes-children [] (xpath/$x "./*" (my-nodes)))


(first (all-tags (my-xml)))
(class my-nodes)

(path-node-pairs (my-xml))



(defn visit-nodes
  [path nodes f]
  (let [current-path (fn[path n] (str path "/" (name(:tag n))))]
    (mapcat (fn [n] (do (f (current-path path n) n)(visit-nodes (current-path path n) (xpath/$x "./*" n) f))) nodes)))

(defn leaf?
  [n]
  (empty? (xpath/$x "./*" n)))









;; https://github.com/clojure/clojure/blob/clojure-1.9.0/src/clj/clojure/core.clj#L4871
(defn tree-seq-2
  [branch? children root]
  (let [walk
        (fn walk [node] (lazy-seq (cons node (when (branch? node)(mapcat walk (children node))))))]
    (walk root)))

(defn tree-seq-fun
  [branch? children root fun]
  (let [walk
        (fn walk [node path] 
          (lazy-seq 
            (cons {path node} 
                  (when (branch? node)
                    (mapcat 
                      (fn [n] (walk n (str path "/" (name (:tag n)))))
                      (children node))))))]
    (walk root "")))

(defn perform-nodes
  [xml fun]
  (let [nodes (tree-seq-fun (fn [n] (:node n)) ; (:node n) - $x:node, nil or node, branch?
                            (fn [n] (xpath/$x "./*" n)) ; "./*" - children of current
                            (first (xpath/$x "/*" xml))
                            (fn [n] "aaa"))] ; fun
  (remove nil? (distinct (map fun nodes)))))

(defn all-tags
  [xml]
  (perform-nodes xml (fn [m] m)))

(all-tags (my-xml))
