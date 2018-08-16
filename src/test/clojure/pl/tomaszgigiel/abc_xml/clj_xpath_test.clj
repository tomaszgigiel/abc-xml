(ns pl.tomaszgigiel.abc-xml.clj-xpath-test
  (:use [clojure.test])
  (:require [clojure.java.io :as io])
  (:require [clj-xpath.core :as xpath])
  (:require [pl.tomaszgigiel.abc-xml.clj-xpath :as abc])
  (:require [pl.tomaszgigiel.abc-xml.core-test-config :as mytest]))

(use-fixtures :once mytest/once-fixture)
(use-fixtures :each mytest/each-fixture)

(def not-nil? (complement nil?))

(deftest xml-string-by-resource-test
  (is (.contains (abc/xml-string-by-resource "simple.xml") "<books>")))

(deftest xpath-test
  (let [xml (abc/xml-string-by-resource "simple.xml")
        nodes (xpath/$x "/*" xml)
        simple-visit-nodes (slurp (io/resource "simple.visit-nodes.txt"))
        leaf-info (fn [p n] (if (abc/leaf? n) (printf "%1$s=%2$s\r\n" p (:text n))))
        leaf-or-nil (fn [p n] (if (abc/leaf? n) n))]
    (is (= :books (xpath/$x:tag "/*" xml)))
    (is (= (list :book :book) (xpath/$x:tag* "/books/book" xml)))
    (is (= () (xpath/$x:tag* "/non-existent/non-existent" xml)))
    (is (= () (xpath/$x:tag* "/books/non-existent" xml)))
    (is (= (list :author :author :author :author) (xpath/$x:tag* "/books/book/authors/author" xml)))
    (is (= (list :first-name :first-name :first-name :first-name) (xpath/$x:tag* "/books/book/authors/author/first-name" xml)))
    (is (= () (xpath/$x:tag* "/books/book/authors/author/first-name/*" xml)))
    (is (= () (xpath/$x "/books/book/authors/author/first-name/*" xml)))
    (is (= 2 (count (xpath/$x "./*" (first (xpath/$x "/books/book/authors/author" xml))))) "first-name last-name")
    (is (= 0 (count (xpath/$x "./*" (first (xpath/$x "/books/book/authors/author/first-name" xml))))) "nothing")
    (is (= () (xpath/$x "./*" (first (xpath/$x "/books/book/authors/author/first-name" xml)))) "as above")
    (is (= true (abc/leaf? (first (xpath/$x "/books/book/authors/author/first-name" xml)))) "no children")
    (is (= false (abc/leaf? (first (xpath/$x "/books/book/authors/author" xml)))) "has children")
    (is (= clojure.lang.LazySeq (class nodes)))
    (is (= clojure.lang.LazySeq (class nodes)))
    (is (= (list :books :book :title :authors :author :first-name :last-name) (abc/all-tags xml)))
    (is (nil? (leaf-info "dummy" (first (xpath/$x "/books/book/authors/author/first-name" xml)))))
    (is (not-nil? (leaf-or-nil "dummy" (first (xpath/$x "/books/book/authors/author/first-name" xml)))))
    (is (= simple-visit-nodes (with-out-str (abc/visit-nodes "" nodes leaf-info))))
    (is (= 0 (count (abc/visit-nodes "" nodes leaf-or-nil))))))

(deftest perform-nodes-test
  (let [xml (abc/xml-string-by-resource "simple.xml")
        leaf-or-nil (fn [n] (if (abc/leaf? n) n))]
    (is (= 10 (count (abc/perform-nodes xml leaf-or-nil))))))
