(ns pl.tomaszgigiel.abc-xml.core-test
  (:use [clojure.test])
  (:require [clojure.java.io :as io])
  (:require [clojure.edn :as edn])
  (:require [pl.tomaszgigiel.abc-xml.core :as abc])
  (:require [pl.tomaszgigiel.abc-xml.core-test-config :as mytest]))

(use-fixtures :once mytest/once-fixture)
(use-fixtures :each mytest/each-fixture)

(deftest string-from-resource-test
  (is (.contains (abc/string-from-resource "simple.xml") "<books>")) "string")

(deftest not-nil?-test
  (is (= false (abc/not-nil? nil)) "nil")
  (is (= true (abc/not-nil? "ok")) "not nil"))
