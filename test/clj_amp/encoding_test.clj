(ns clj-amp.encoding-test
  (:require [clojure.test :refer :all]
            [clj-amp.encoding :refer :all]
            [gloss.io :refer [encode decode to-byte-buffer]]
            [byte-streams :refer [bytes= to-byte-array]]
            [slingshot.test]))


(defn update-map [f m]
  (reduce-kv (fn [m k v]
               (assoc m k (f v))) {} m))


(defn boxes=
  [box1 box2]
  (every? #(= (-> %1 (get box1) (to-byte-buffer)) (-> %1 (get box2) (to-byte-buffer))) (concat (keys box1) (keys box2))))


(deftest roundtrip-tests
  (let [specimens [[(to-byte-buffer
                     [0x00 0x01 0x61 0x00 0x01 0x40 0x00 0x01 0x62 0x00
                      0x03 0x62 0x61 0x62 0x00 0x00])
                    {"a" "@"
                     "b" "bab"}]
                   [(to-byte-buffer
                     [0x00 0x01 0x00 0x00 0x01 0x00 0x00 0x00])
                    {(to-byte-array "\0") (to-byte-buffer [0x00])}]
                   [(byte-array [0x00 0x00])
                    {}]]
        encode-box (comp to-byte-buffer (partial encode ampbox-codec))
        decode-box (partial decode ampbox-codec)]
    (testing "Box encoding verification"
      (doseq [[encoded decoded] specimens]
        (is (boxes= decoded (decode-box encoded)))
        (is (bytes= encoded (encode-box decoded)))))))


(deftest invalid-boxes
  (testing "Empty key"
    (is (thrown+?
         [:type :clj-amp.encoding/empty-key]
         (->> {"" ""} validate-box (encode ampbox-codec)))))
  (testing "Overlong key"
    (is (thrown+?
         [:type :clj-amp.encoding/key-too-long]
         (->> {(clojure.string/join (repeat (+ max-key-length 1) "a")) []}
              validate-box (encode ampbox-codec)))))
  (testing "Overlong value"
    (is (thrown+?
         [:type :clj-amp.encoding/value-too-long]
         (->> {"key" (byte-array (+ max-value-length 1))}
         validate-box (encode ampbox-codec))))))
