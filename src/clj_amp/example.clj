(ns clj-amp.example
  (:require [clj-amp.command :as command]
            [clj-amp.core :refer [client simple-server make-responder]]
            [clj-amp.argument :as a]
            [manifold.deferred :as d]
            [manifold.time :as dt]
            [slingshot.slingshot :refer [try+ throw+]]))


(command/defcommand sum
  {:a {:type ::a/integer}
   :b {:type ::a/integer}}
  {:total {:type ::a/integer}}
  :command-name "Sum")


(command/defcommand divide
  {:numerator   {:type ::a/integer}
   :denominator {:type ::a/integer}}
  {:result {:type ::a/float}}
  :errors {::zero-division "ZERO_DIVISION"}
  :command-name "Divide")


(defn -example-client
  [& args]
  @(d/chain
    (client "localhost" 1234 (make-responder {}))
    (fn [[call-remote close!]]
      (d/chain
       (call-remote sum {:a 13 :b 81})
       (fn [r]
         (prn "Sum:" r)
         (call-remote divide {:numerator 1234 :denominator 2}))
       (fn [r]
         (prn "Quotient:" r)
         (close!))))))


(defn -example-client-concurrent
  [& args]
  @(d/chain
    (client "localhost" 1234 (make-responder {}))
    (fn [[call-remote close!]]
      (d/let-flow
       [sum-result (call-remote sum {:a 13 :b 81})
        quotient (d/catch
                     (call-remote divide {:numerator 1234 :denominator 2})
                     clojure.lang.ExceptionInfo
                   (fn [e]
                     (if (= ::zero-division (-> e ex-data :type))
                       (do (println "Divided by zero: returning INF")
                           1e1000)
                       (throw+ e))))]
       (prn "Done with math:" [sum quotient])
       (close!)))))


(defn- sum'
  [{:keys [a b]}]
  (let [total (+ a b)]
    (println "Did a sum:" a "+" b "=" total)
    (dt/in 2000 #(identity {:total total}))))


(defn- divide'
  [{:keys [numerator denominator]}]
  (try+
   (let [result (double (/ numerator denominator))]
     (println "Divided:" numerator "/" denominator "=" result)
     {:result result})
   (catch ArithmeticException _
     (throw+ {:type ::zero-division}))))


(defn -example-server
  [& args]
  (let [responder (make-responder {sum sum' divide divide'})]
    (simple-server responder 1234)))
