(ns clj-amp.encoding-test
  (:require [clojure.test :refer :all]
            [clj-amp.encoding :refer :all]
            [gloss.io :refer [encode decode to-byte-buffer]]
            [byte-streams :refer [bytes=]]))

(deftest roundtrip-tests
  (let [specimens [[(to-byte-buffer
                     [0x00 0x01 0x61 0x00 0x01 0x40 0x00 0x01 0x62 0x00
                      0x03 0x62 0x61 0x62 0x00 0x00])
                    {"a" [(to-byte-buffer "@")]
                     "b" [(to-byte-buffer "bab")]}]
                   [(to-byte-buffer
                     [0x00 0x01 0x00 0x00 0x01 0x00 0x00 0x00])
                    {"\0" [(to-byte-buffer [0x00])]}]
                   [(to-byte-buffer [0x00 0x00])
                    {}]]
        encode-box (comp to-byte-buffer (partial encode ampbox-codec))
        decode-box (partial decode ampbox-codec)]
    (testing "Box encoding verification"
      (doseq [[encoded decoded] specimens]
        (is (= decoded (decode-box encoded)))
        (is (bytes= encoded (encode-box decoded)))))))
