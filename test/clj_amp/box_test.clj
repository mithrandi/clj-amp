(ns clj-amp.box-test
  (:require [clojure.test :refer :all]
            [clj-amp.box :refer :all]
            [gloss.io]
            [manifold.stream :as s]
            [byte-streams :as bs]
            [slingshot.test]))


;; FIXME: This doesn't really test wrap-ampbox-stream but I can't figure out
;; how to hook it up for that.
(defn- encode-box
  [box]
  (gloss.io/to-byte-buffer
   (map (partial gloss.io/encode ampbox-item-codec)
        (concat (conj (vec box) nil)))))


(defn- decode-box
  [data]
  (let [s  (s/stream)
        s' (wrap-ampbox-stream s)]
    @(s/put! s data)
    (s/close! s)
    @(s/try-take! s' 0)))


(deftest roundtrip-tests
  (let [specimens [[(gloss.io/to-byte-buffer
                     [0x00 0x01 0x61 0x00 0x01 0x40 0x00 0x01 0x62 0x00
                      0x03 0x62 0x61 0x62 0x00 0x00])
                    {"a" "@" "b" "bab"}]
                   [(gloss.io/to-byte-buffer
                     [0x00 0x01 0x00 0x00 0x01 0x00 0x00 0x00])
                    {"\0" (gloss.io/to-byte-buffer [0x00])}]
                   [(byte-array [0x00 0x00])
                    {}]]]
    (testing "Box encoding verification"
      (doseq [[encoded decoded] specimens]
        (is (boxes= decoded (decode-box encoded)))
        (is (bs/bytes= encoded (encode-box decoded)))
        (is (boxes= decoded (-> decoded encode-box decode-box)))
        (is (bs/bytes= encoded (-> encoded decode-box encode-box)))))))


(deftest invalid-boxes
  (testing "Empty key"
    (is (thrown+?
         [:type :clj-amp.box/empty-key]
         (validate-box {"" ""}))))
  (testing "Overlong key"
    (is (thrown+?
         [:type :clj-amp.box/key-too-long]
         (validate-box {(clojure.string/join (repeat (+ max-key-length 1) "a")) []}))))
  (testing "Overlong value"
    (is (thrown+?
         [:type :clj-amp.box/value-too-long]
         (validate-box {"key" (byte-array (+ max-value-length 1))})))))
