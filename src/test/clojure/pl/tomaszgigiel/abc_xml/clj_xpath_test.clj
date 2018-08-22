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

(deftest xpath-simple-test
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

;; https://stackoverflow.com/a/3926682
(deftest xpath-advanced-test
  (is (= (list :title :first-name :last-name) (distinct (xpath/$x:tag* "//*[not(*)]" xml))) "all leaves tags")
  (is (= (list :title :first-name :last-name) (distinct (xpath/$x:tag* "//*[not(child::*)]" xml))) "all leaves tags")
  (is (= 37 (count (xpath/$x:text* "//node()[not(node())]" xml))) "all nodes text (leaves or not)")
  (is (some #(= "\n" %) (xpath/$x:text* "//node()[not(node())]" xml)) "all nodes text (leaves or not)")
  (is (= (list :first-name :last-name) (distinct (xpath/$x:tag* "//*[ancestor::author]" xml))) "all leaves tags for author branch")
  (is (= "Amit" (xpath/$x:text "/books/book[1]/authors[1]/author[1]/first-name[1]" xml)) "first name")
  (is (= (list :books :book :authors :author) (distinct (xpath/$x:tag* "//first-name/ancestor::*" xml))) "first name"))

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
  (is (= (list "books" "book" "title" "authors" "author" "first-name" "last-name") (abc/kyleburton-alternative-all-tags xml)) "all tags")
  (is (= (map #(name %) (distinct (xpath/$x:tag* "//*" xml))) (abc/kyleburton-alternative-all-tags xml)) "all tags"))

(deftest kyleburton-alternative-all-paths-test
  (let [all-paths-from-resource (string/split-lines(abc-core/string-from-resource "simple.all-paths.txt"))]
    (is (= all-paths-from-resource (abc/kyleburton-alternative-all-paths xml)) "list of all distinct paths")))

(deftest path-text-pairs-test
  (is (= 10 (count (abc/path-text-pairs xml))) "list path text pairs")
  (is (some #(= {:path "/books/book/title", :text "Clojure in Action"} %) (abc/path-text-pairs xml)) "list path text pairs"))

(defn xml-to-csv
  [xml] 
  (let [cols ["/books/book/title" "/books/book/authors/author/first-name" "/books/book/authors/author/last-name"]
        col-index (fn [cols col] (.indexOf cols col))
        cell-count (fn [r] (->> r (re-seq #",") count))
        complete-row (fn [before idx] (str "before:" idx))

        perform-row (fn [last cols path val] (if (> (col-index cols path) (cell-count last)) [(str last "," val)] [last (str (complete-row last (col-index cols path)) val)]))

        perform-item (fn perform-item ([rows cell] (perform-item (pop rows) (last rows) cols (:path cell) (:text cell)))
                       ([butlast last cols path val] (into [] (concat butlast (perform-row last cols path val)))))]
    (reduce
      perform-item
      [""]
      (abc/path-text-pairs xml))))

(xml-to-csv xml)
