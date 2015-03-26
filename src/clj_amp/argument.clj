(ns clj-amp.argument
  (:require [gloss.core :as g]
            [gloss.io]))


(defmacro defcommand
  "Define an AMP Command.
  Usage: (defcommand
          name
          command-name?
          {argument-name {:type argument-type
                          :name amp-argument-name?
                          ...}
           ...}
           return-value)"
  ([defname arguments return-value]
   `(def ~defname (build-command ~(name defname) ~arguments ~return-value)))
  ([defname command-name arguments return-value]
   `(def ~defname (build-command ~command-name ~arguments ~return-value))))


(defmulti to-box
  "Serialize a Command argument to a map of Box keys/values."
  (fn [argument value] (:type argument)))


(defmethod to-box ::bytes
  [argument value]
  {(:name argument) value})


(defmulti from-box
  "Deserialize a Command argument from a Box."
  (fn [argument box] (:type argument)))


(defmethod from-box ::bytes
  [argument box]
  (get box (:name argument)))


(defn defargument
  "Define an AMP argument type using a gloss codec."
  [type codec]
  (defmethod to-box type
     [argument value]
     {(:name argument) (gloss.io/encode codec value)})
  (defmethod from-box type
     [argument box]
     (gloss.io/decode codec (get box (:name argument)))))


;; TODO: Make this use a BigNum if necessary
(defargument ::integer
  (g/compile-frame (g/string-integer :ascii)))


(defargument ::string
  (g/compile-frame (g/string :utf-8)))


(defargument ::boolean
  (g/compile-frame
   (g/string :utf-8)
   #(if %1 "True" "False")
   (partial = "True")))


;; This is represented as the Clojure double type
(defargument ::float
  (g/compile-frame (g/string-float :ascii)))


(defargument ::decimal
  (g/compile-frame
   (g/string :utf-8)
   str
   bigdec))


;; TODO: Implement DateTime
