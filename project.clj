(defproject async-experiments "0.1.0-SNAPSHOT"
  :description "Various experiments with core.async"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [core.async  "0.1.0-SNAPSHOT"]
                 [crate "0.2.4"]
                 [domina "1.0.1"]]
  
  :repositories {"sonatype-staging"
                 "https://oss.sonatype.org/content/groups/staging/"}
  
  :plugins [[lein-cljsbuild "0.3.2"]]
  
  :cljsbuild {
              :builds [{:id "modal"
                        :source-paths ["src/async_experiments/modal"]
                        :compiler {:output-to "js/modal.js"
                                   :optimizations :simple
                                   :pretty-print true}}]})