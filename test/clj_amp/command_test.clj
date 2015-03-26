(ns clj-amp.command-test
  (:require [clojure.test :refer :all]
            [clj-amp.command :refer :all]
            [clj-amp.argument :as a]
            [clj-amp.box :refer [boxes=]]))


(defcommand my-cool-command
  {:named   {:name "name" :type ::a/integer}
   :unnamed {:type ::a/string :optional? true}} :foo)


(deftest command-definition
  (testing "Command definition"
    (is (= my-cool-command
           (build-command
            "my-cool-command"
            {:named   {:name "name" :type ::a/integer}
             :unnamed {:type ::a/string :optional? true}}
            :foo)
           {:name "my-cool-command"
            :arguments {:named   {:name "name"
                                  :type ::a/integer}
                        :unnamed {
                                  :type ::a/string
                                  :optional? true}}
            :response :foo}))))


(deftest command-parsing
  (testing "Command parsing"
    (let [input  {:named 42}
          output {"name" (byte-array [0x34 0x32])}]
      (is (boxes= (to-box my-cool-command input)
                  output))
      (is (= input
             (from-box my-cool-command output))))))
