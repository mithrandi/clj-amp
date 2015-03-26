(ns clj-amp.argument-test
  (:require [clojure.test :refer :all]
            [clj-amp.argument :as a :refer :all]
            [clj-amp.box :refer [boxes=]]))

;; (deftest command-definition
;;   (testing "Command definition"
;;     (is (= my-cool-command
;;            (build-command "my-cool-command" {} :foo)
;;            {:name "my-cool-command"
;;             :arguments {}
;;             :return-value :foo}))))


(defn roundtrips
  [argument value serialized]
  (is (= value
         (from-box argument serialized)))
  (is (boxes= serialized
              (to-box argument value))))


(deftest argument-serialization
  (testing "integer"
    (roundtrips {:type ::a/integer :name "int"}
                42
                {"int" "42"}))
  (testing "bytes"
    (roundtrips {:type ::a/bytes :name "bytes"}
                (gloss.io/to-byte-buffer [0x00 0x01 0x02])
                {"bytes" (gloss.io/to-byte-buffer [0x00 0x01 0x02])}))
  (testing "string"
    (roundtrips {:type ::a/string :name "str"}
                "x: \u2603"
                {"str" (gloss.io/to-byte-buffer (byte-array [0x78 0x3a 0x20 0xe2 0x98 0x83]))}))
  (testing "boolean"
    (roundtrips {:type ::a/boolean :name "bool"}
                true
                {"bool" "True"}))
  (testing "float"
    (roundtrips {:type ::a/float :name "float"}
                -123.40000000000002
                {"float" "-123.40000000000002"})))
