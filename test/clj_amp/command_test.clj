(ns clj-amp.command-test
  (:require [clojure.test :refer :all]
            [clj-amp.command :refer :all]
            [clj-amp.argument :as a]
            [clj-amp.box :refer [boxes=]]))


(defcommand my-cool-command
  {:named   {:name "name" :type ::a/integer}
   :unnamed {:type ::a/string :optional? true}}
  {:a       {:type ::a/string}}
  :errors   {::foo "FOO"})


(defcommand named-command
  {} {} :command-name "lalala")


(deftest command-definition
  (testing "Command definition"
    (is (= my-cool-command
           (build-command
            "my-cool-command"
            {:named   {:name "name"
                       :type ::a/integer}
             :unnamed {:type ::a/string
                       :optional? true}}
            {:a       {:name "a"
                       :type ::a/string}}
            {::foo "FOO"})
           {:name "my-cool-command"
            :arguments  {:named   {:name "name"
                                   :type ::a/integer}
                         :unnamed {:name "unnamed"
                                   :type ::a/string
                                   :optional? true}}
            :response   {:a       {:name "a"
                                   :type ::a/string}}
            :errors     {::foo "FOO"}
            :inv-errors {"FOO" ::foo}}))
    (is (= (:name named-command)
           "lalala"))))


(deftest command-parsing
  (testing "Command parsing"
    (let [input  {:named 42}
          output {"name" (byte-array [0x34 0x32])}]
      (is (boxes= (to-box (:arguments my-cool-command) input)
                  output))
      (is (= input
             (from-box (:arguments my-cool-command) output))))))
