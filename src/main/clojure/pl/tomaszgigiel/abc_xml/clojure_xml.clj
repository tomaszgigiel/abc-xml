(ns pl.tomaszgigiel.abc-xml.clojure-xml
  (:require [clojure.tools.logging :as log])
  (:require [clojure.xml])
  (:gen-class))

;; https://clojuredocs.org/clojure.xml

(defn abc-parse
  [s]
  (clojure.xml/parse s))
