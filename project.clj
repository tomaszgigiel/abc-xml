(defproject abc-xml "1.0.0.0-SNAPSHOT"
  :description "abc-xml: xml in clojure"
  :url "http://tomaszgigiel.pl"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 ;; otherwise log4j.properties has no effect
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.2"]
                 [com.github.kyleburton/clj-xpath "1.4.11"]]

  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :resource-paths ["src/main/resources"]
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all :jar-name "abc-xml.jar" :uberjar-name "abc-xml-uberjar.jar"}
             :main-abc-xml {:main ^:skip-aot pl.tomaszgigiel.abc-xml.core}
             :dev {:resource-paths ["src/test/resources"] :jmx-opts ["-Xmx512m"]}}
  :aliases {"run-main-abc-xml" ["with-profile" "main-abc-xml,dev" "run"]})

