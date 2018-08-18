(ns pl.tomaszgigiel.abc-xml.core
  (:require [clojure.java.io :as io])
  (:require [clojure.tools.logging :as log])
  (:gen-class))

;; TODO:
;; xml-seq: https://github.com/clojure/clojure/blob/clojure-1.9.0/src/clj/clojure/core.clj#L4898

(defn string-from-resource
  [r]
  (slurp (io/resource r)))

(def not-nil? (complement nil?))

(defn -main
  "abc-xml: xml in clojure"
  [& args]
  (log/info "ok"))
