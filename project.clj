(defproject clj-amp/clj-amp "0.9.3-SNAPSHOT"
  :description "Clojure implementation of AMP"
  :url "https://github.com/mithrandi/clj-codetip"
  :license {:name "Expat (MIT) license"
            :url "http://spdx.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [gloss "0.2.5"]
                 [aleph "0.4.0"]
                 [slingshot "0.12.2"]
                 [prismatic/plumbing "0.5.0"]
                 [clj-time "0.11.0"]
                 [manifold "0.1.0"]]
  :plugins [[codox "0.8.13"]]
  :codox {:src-dir-uri "http://github.com/mithrandi/clj-amp/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :deploy-repositories [["releases" :clojars]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]])
