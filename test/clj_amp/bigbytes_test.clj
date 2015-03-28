(ns clj-amp.bigbytes-test
  (:require [clojure.test :refer :all]
            [clj-amp.bigbytes :as bb]
            [clj-amp.argument :as a]
            [clj-amp.box :refer [boxes=]]
            [byte-streams :refer [bytes=, print-bytes]]))


(defn roundtrips
  [argument value serialized]
  (is (= (gloss.io/to-byte-buffer value)
         (gloss.io/to-byte-buffer (a/from-box argument serialized))))
  (is (boxes= serialized
              (a/to-box argument value))))


(deftest argument-serialization
  (testing "big-bytes"
    (roundtrips {:type ::bb/big-bytes :name "bytes" :chunk-size 2}
                (gloss.io/to-byte-buffer [0x00 0x01 0x02 0x03 0x04])
                {"bytes"   (list (gloss.io/to-byte-buffer [0x00 0x01]))
                 "bytes.2" (list (gloss.io/to-byte-buffer [0x02 0x03]))
                 "bytes.3" (list (gloss.io/to-byte-buffer [0x04]))})))
