(ns pl.tomaszgigiel.abc-xml.core-test
  (:use [clojure.test])
  (:require [clojure.java.io :as io])
  (:require [clojure.edn :as edn])
  (:require [pl.tomaszgigiel.abc-xml.core :as abc])
  (:require [pl.tomaszgigiel.abc-xml.core-test-config :as mytest]))

(use-fixtures :once mytest/once-fixture)
(use-fixtures :each mytest/each-fixture)

(deftest uri-string-test
  (is (.contains (->> "simple.xml" io/resource str) "/abc-xml/src/test/resources/simple.xml")))

(deftest parse-test
  (let [expected  (->> "simple.xml.edn" io/resource slurp edn/read-string)
        actual (abc/abc-parse (str (io/resource "simple.xml")))]
  (is (= expected actual))))
