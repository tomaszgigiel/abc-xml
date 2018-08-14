(ns pl.tomaszgigiel.abc-xml.core
  (:require [clojure.tools.logging :as log])
  (:require [clojure.xml])
  (:gen-class))

(defn abc-parse
  [s]
  (clojure.xml/parse s))

(defn -main
  "abc-xml: xml in clojure"
  [& args]
  (log/info "ok"))
