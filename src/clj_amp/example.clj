(ns clj-amp.example
  (:require [clj-amp.command :as command]
            [clj-amp.core :refer [client simple-server make-responder]]
            [clj-amp.argument :as a]
            [manifold.deferred :as d]))


(command/defcommand sum
  {:a {:type ::a/integer}
   :b {:type ::a/integer}}
  {:total {:type ::a/integer}}
  :command-name "Sum")


(command/defcommand divide
  {:numerator   {:type ::a/integer}
   :denominator {:type ::a/integer}}
  {:result {:type ::a/float}}
  :command-name "Divide")


(defn -example-client
  [& args]
  @(d/chain
    (client "localhost" 1234 println)
    (fn [[call-remote close!]]
      (d/chain (call-remote sum {:a 42 :b 56})
               (fn [result]
                 (println result)
                 (close!))))))


(defn- sum'
  [{:keys [a b]}]
  (let [total (+ a b)]
    (println "Did a sum:" a "+" b "=" total)
    {:total total}))


(defn- divide'
  [{:keys [numerator denominator]}]
  (let [result (double (/ numerator denominator))]
    (println "Divided:" numerator "/" denominator "=" result)
    {:result result}))


(defn -example-server
  [& args]
  (let [responder (make-responder {sum sum' divide divide'})]
    (simple-server responder 1234)))
