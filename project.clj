(defproject om-semantic "0.1.0-SNAPSHOT"
            :description "Semantic UI components built with Om"
            :url "https://github.com/vikeri/om-semantic"
            :license {:name "Eclipse"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}

            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2371"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [om "0.7.3"]]

            :git-dependencies [["https://github.com/vikeri/om-autocomplete.git"]] ; Fork of https://github.com/arosequist/om-autocomplete

            :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
                      [lein-git-deps "0.0.2-SNAPSHOT"]]

            :source-paths ["src"]

            :cljsbuild {
                        :builds [{:id           "search"
                                  :source-paths ["src"
                                                 ".lein-git-deps/om-autocomplete/src/arosequist/"
                                                 "examples/search/src"]
                                  :compiler     {
                                                 :output-to     "examples/search/main.js"
                                                 :output-dir    "examples/search/out"
                                                 :optimizations :none
                                                 :source-map    true}}]})
