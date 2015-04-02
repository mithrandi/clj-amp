(defproject clj-amp/clj-amp "0.9.0-SNAPSHOT"
  :description "Clojure implementation of AMP"
  :url "https://github.com/mithrandi/clj-codetip"
  :license {:name "Expat (MIT) license"
            :url "http://spdx.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha6"]
                 [gloss "0.2.4"]
                 [aleph "0.4.0-beta3"]
                 [slingshot "0.12.2"]
                 [prismatic/plumbing "0.4.1"]
                 [clj-time "0.9.0"]
                 [manifold "0.1.0-SNAPSHOT"]]
  :deploy-repositories [["releases" :clojars]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]])
