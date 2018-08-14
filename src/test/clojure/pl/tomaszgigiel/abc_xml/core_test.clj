(ns pl.tomaszgigiel.abc-xml.core-test
  (:use [clojure.test])
  (:require [pl.tomaszgigiel.abc-xml.core :as abc])
  (:require [pl.tomaszgigiel.abc-xml.core-test-config :as mytest]))

(use-fixtures :once mytest/once-fixture)
(use-fixtures :each mytest/each-fixture)

