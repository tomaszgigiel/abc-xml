(ns pl.tomaszgigiel.abc-xml.clj-xpath-test
  (:use [clojure.test])
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string])
  (:require [clj-xpath.core :as xpath])
  (:require [pl.tomaszgigiel.abc-xml.clj-xpath :as abc])
  (:require [pl.tomaszgigiel.abc-xml.core :as abc-core])
  (:require [pl.tomaszgigiel.abc-xml.core-test-config :as mytest]))

(use-fixtures :once mytest/once-fixture)
(use-fixtures :each mytest/each-fixture)

(def xml (abc-core/string-from-resource "simple.xml"))

(deftest xpath-test
  (is (= :books (xpath/$x:tag "/*" xml)) "get root node tag")
  (is (= (list :book :book) (xpath/$x:tag* "/books/book" xml)) "get list of tag nodes by path")
  (is (= () (xpath/$x:tag* "/non-existent/non-existent" xml)) "get empty list of tag nodes by completely non existent path")
  (is (= () (xpath/$x:tag* "/books/non-existent" xml)) "get empty list of tag nodes by partly non existent path")
  (is (= (list :author :author :author :author) (xpath/$x:tag* "/books/book/authors/author" xml)) "get list of tag nodes by path")
  (is (= (list :first-name :first-name :first-name :first-name) (xpath/$x:tag* "/books/book/authors/author/first-name" xml)) "get list of tag nodes by path")
  (is (= () (xpath/$x:tag* "/books/book/authors/author/first-name/*" xml)) "get empty list of tag nodes by too deep path")
  (is (= () (xpath/$x "/books/book/authors/author/first-name/*" xml))"get empty list of nodes by too deep path")
  (is (= 2 (count (xpath/$x "./*" (first (xpath/$x "/books/book/authors/author" xml))))) "first-name last-name")
  (is (= 0 (count (xpath/$x "./*" (first (xpath/$x "/books/book/authors/author/first-name" xml))))) "nothing, too deep path")
  (is (= () (xpath/$x "./*" (first (xpath/$x "/books/book/authors/author/first-name" xml)))) "nothing, too deep path")
  (is (= clojure.lang.LazySeq (class (xpath/$x "/*" xml))) "xpath result is LazySeq"))

(deftest abc-leaf-test
  (is (= true (abc/xpath-leaf? (first (xpath/$x "/books/book/authors/author/first-name" xml)))) "no children")
  (is (= false (abc/xpath-leaf? (first (xpath/$x "/books/book/authors/author" xml)))) "has children")
  (is (= false (abc/xpath-leaf? (first (xpath/$x "/non-existent/non-existent" xml)))) "non existent path"))

(deftest abc-xpath-seq-test
  (is (= 19 (->> xml (re-seq #"</") count) (count (abc/xpath-seq xml))) "nodes count"))

(deftest xpath-transformed-seq-test
  (is (= 19 (->> xml (re-seq #"</") count) (count (abc/xpath-transformed-seq xml (fn [n] (:tag n))))) "nodes/tag count")
  (is (= 7 (->> (abc/xpath-transformed-seq xml (fn [n] (:tag n))) distinct count)) "nodes/tag count")
  (is (= (list :books :book :title :authors :author :first-name :last-name) (->> (abc/xpath-transformed-seq xml (fn [n] (:tag n))) distinct)) "all tags"))

(deftest tree-ancestry-seq-test
  (let [ms (abc/tree-ancestry-seq (fn [n] (:node n))
                     (fn [n] (xpath/$x "./*" n))
                     (first (xpath/$x "/*" xml)))]
    (is (= 19 (->> xml (re-seq #"</") count) (count ms)) "nodes/tag count")))

(deftest xpath-ancestry-seq-test
  (is (= 19 (->> xml (re-seq #"</") count) (->> xml abc/xpath-ancestry-seq count)) "nodes/tag count"))

(deftest xpath-ancestry-transformed-seq-test
  (let [node-tag (fn [n] (str "/" (name (:tag n))))
        node-tag-path (fn [ns] (string/join (map node-tag ns)))
        full-path (fn [m] (str (node-tag-path (:ancestors m)) (node-tag (:node m))))
        all-paths (distinct (abc/xpath-ancestry-transformed-seq xml full-path))
        all-paths-from-resource (string/split-lines(abc-core/string-from-resource "simple.all-paths.txt"))]
  (is (= all-paths-from-resource all-paths) "list of all distinct paths")))

(deftest kyleburton-alternative-all-tags-test
  (is (= (list :books :book :title :authors :author :first-name :last-name) (abc/kyleburton-alternative-all-tags xml)) "all tags"))

(deftest kyleburton-alternative-all-paths-test
  (let [all-paths-from-resource (string/split-lines(abc-core/string-from-resource "simple.all-paths.txt"))]
    (is (= all-paths-from-resource (abc/kyleburton-alternative-all-paths xml)) "list of all distinct paths")))
