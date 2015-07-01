(defproject clj-amp/clj-amp "0.9.2"
  :description "Clojure implementation of AMP"
  :url "https://github.com/mithrandi/clj-codetip"
  :license {:name "Expat (MIT) license"
            :url "http://spdx.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [gloss "0.2.5"]
                 [aleph "0.4.0"]
                 [slingshot "0.12.2"]
                 [prismatic/plumbing "0.4.4"]
                 [clj-time "0.9.0"]
                 [manifold "0.1.0"]]
  :deploy-repositories [["releases" :clojars]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]])
