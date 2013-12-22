(defproject async-experiments "0.1.0-SNAPSHOT"
  :description "Various experiments with core.async"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]
                 [org.clojure/core.async  "0.1.267.0-0d7780-alpha"]
                 [crate "0.2.4"]
                 [domina "1.0.2"]]

  :repositories {"sonatype-staging"
                 "https://oss.sonatype.org/content/groups/staging/"}

  :plugins [[lein-cljsbuild "1.0.1"]]

  :cljsbuild {
              :builds [{:id "modal"
                        :source-paths ["src/async_experiments/modal"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print false
                                   :output-dir "js/modal/"
                                   :output-to "js/modal/core.js"}}
                       {:id "search"
                        :source-paths ["src/async_experiments/search"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print false
                                   :output-dir "js/search/"
                                   :output-to "js/search/core.js"}}]})
