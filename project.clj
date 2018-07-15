(defproject com.mjachimowicz/duct-testing "0.1.1"
  :description "Some helpers for testing Duct based systems"
  :url "https://github.com/mariusz-jachimowicz-83/duct-testing"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [org.clojure/java.jdbc "0.7.7"]
                 [duct/core "0.6.2"]
                 [duct/module.logging "0.3.1"]]
  :deploy-repositories [["clojars" {:sign-releases false}]]

  ;; lein cloverage --fail-threshold 95
  ;; lein kibit
  ;; lein eastwood
  :profiles {:dev {:dependencies [[fipp "0.6.12"]
                                  [org.xerial/sqlite-jdbc "3.20.1"]
                                  [org.slf4j/slf4j-nop    "1.7.25"]
                                  [org.clojure/java.jdbc  "0.7.3"]]
                   :plugins [[lein-cloverage "1.0.10"]
                             [lein-kibit "0.1.6"]
                             [jonase/eastwood "0.2.5"]]}})