(ns clj-amp.encoding-test
  (:require [clojure.test :refer :all]
            [clj-amp.encoding :refer :all]
            [gloss.io :refer [encode decode to-byte-buffer]]
            [byte-streams :refer [bytes=]]))

(deftest roundtrip-tests
  (let [byte-specimens [(to-byte-buffer
                         [0x00 0x01 0x61 0x00 0x01 0x40 0x00 0x01 0x62 0x00
                          0x03 0x62 0x61 0x62 0x00 0x00])]
        box-specimens [{}
                       {"a" [(to-byte-buffer "b")]}
                       {"\0\0\0" [(to-byte-buffer [0 0 0])]}]
        roundtrip (comp to-byte-buffer
                        (partial encode ampbox-codec)
                        (partial decode ampbox-codec))
        roundtrip2 (comp (partial decode ampbox-codec)
                         (partial encode ampbox-codec))]
    (testing "Box decode->encode roundtripping"
      (doseq [specimen byte-specimens]
        (is (bytes= specimen (roundtrip specimen)))))
    (testing "Box encode->decode roundtripping"
      (doseq [specimen box-specimens]
        (is (= specimen (roundtrip2 specimen)))))))
