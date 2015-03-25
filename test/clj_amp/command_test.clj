(ns clj-amp.command-test
  (:require [clojure.test :refer :all]
            [clj-amp.command :refer :all]))

(defcommand my-cool-command
  {} :foo)

(deftest command-definition
  (testing "Command definition"
    (is (= my-cool-command
           (build-command "my-cool-command" {} :foo)
           {:name "my-cool-command"
            :arguments {}
            :return-value :foo}))))
