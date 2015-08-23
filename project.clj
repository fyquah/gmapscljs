(defproject gmapscljs "0.0.1"
  :description "Yet another interface to google maps, in clojurescript+reagent"
  :url "http://github.com/fyquah95/gmapscljs"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [reagent "0.5.1-rc"]
                 [org.clojure/clojurescript "1.7.48" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-npm "0.6.1"]
            [lein-cljsbuild "1.0.6"]]
  :npm {:dependencies [[source-map-support "0.3.2"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release"]
  :target-path "target")
