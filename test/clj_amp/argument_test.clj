(ns clj-amp.argument-test
  (:require [clojure.test :refer :all]
            [clj-amp.argument :as a :refer :all]
            [clj-amp.box :refer [boxes=]]
            [clj-time.core :as t]))


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
                {"str" (byte-array [0x78 0x3a 0x20 0xe2 0x98 0x83])}))
  (testing "boolean"
    (roundtrips {:type ::a/boolean :name "bool"}
                true
                {"bool" "True"}))
  (testing "float"
    (roundtrips {:type ::a/float :name "float"}
                -123.40000000000002
                {"float" "-123.40000000000002"}))
  (testing "decimal"
    (roundtrips {:type ::a/decimal :name "dec"}
                123.450M
                {"dec" "123.450"}))
  (testing "date-time"
    (roundtrips {:type ::a/date-time :name "datetime"}
                (t/date-time 2012 01 23 12 34 56 54)
                {"datetime" "2012-01-23T12:34:56.054000+00:00"})
    (is (= (t/date-time 2012 01 23 13 57 56 54)
           (from-box {:type ::a/date-time :name "datetime"}
                     {"datetime" "2012-01-23T12:34:56.054-01:23"}))))
  (testing "amp-list"
    (roundtrips {:type ::a/list :name "list" :of ::a/integer}
                [1 20 500]
                {"list" (byte-array [0x00 0x01 0x31
                                     0x00 0x02 0x32 0x30
                                     0x00 0x03 0x35 0x30 0x30])})))
