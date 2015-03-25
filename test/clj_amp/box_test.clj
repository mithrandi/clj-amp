(ns clj-amp.box-test
  (:require [clojure.test :refer :all]
            [clj-amp.box :refer :all]
            [gloss.io]
            [byte-streams :as bs]
            [slingshot.test]
            [plumbing.core :refer [map-vals]]))


(defn on- [op f x y] (op (f x) (f y)))

(defn boxes=
  [box1 box2]
  (on- = (partial map-vals gloss.io/to-byte-buffer) box1 box2))
  ;; (every? #(= (->> %1 (get box1) (to-byte-buffer))
  ;;             (->> %1 (get box2) (to-byte-buffer)))
  ;;         (concat (keys box1) (keys box2))))


(deftest roundtrip-tests
  (let [specimens [[(gloss.io/to-byte-buffer
                     [0x00 0x01 0x61 0x00 0x01 0x40 0x00 0x01 0x62 0x00
                      0x03 0x62 0x61 0x62 0x00 0x00])
                    {"a" "@" "b" "bab"}]
                   [(gloss.io/to-byte-buffer
                     [0x00 0x01 0x00 0x00 0x01 0x00 0x00 0x00])
                    {"\0" (gloss.io/to-byte-buffer [0x00])}]
                   [(byte-array [0x00 0x00])
                    {}]]
        encode-box (comp gloss.io/to-byte-buffer
                         (partial gloss.io/encode ampbox-codec))
        decode-box (partial gloss.io/decode ampbox-codec)]
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
         (->> {"" ""} validate-box (gloss.io/encode ampbox-codec)))))
  (testing "Overlong key"
    (is (thrown+?
         [:type :clj-amp.box/key-too-long]
         (->> {(clojure.string/join (repeat (+ max-key-length 1) "a")) []}
              validate-box (gloss.io/encode ampbox-codec)))))
  (testing "Overlong value"
    (is (thrown+?
         [:type :clj-amp.box/value-too-long]
         (->> {"key" (byte-array (+ max-value-length 1))}
         validate-box (gloss.io/encode ampbox-codec))))))
