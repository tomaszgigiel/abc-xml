(ns pl.tomaszgigiel.abc-xml.core-test-config
  (:use [clojure.test])
  (:require [pl.tomaszgigiel.abc-xml.core :as abc]))

(defn setup [] ())
(defn teardown [] ())

(defn once-fixture [f]
  (setup)
  (f)
  (teardown))

(defn each-fixture [f]
  (setup)
  (f)
  (teardown))

