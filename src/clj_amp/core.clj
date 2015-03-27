(ns clj-amp.core
  (:require [clj-amp.box :refer [ampbox-codec]]
            [clj-amp.command :as command]
            [clj-amp.argument :as a]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [aleph.tcp :as tcp]
            [gloss.io :as io])
  (:gen-class))


(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
      (s/map (partial io/encode protocol) out)
      s)
    (s/splice
      out
      (io/decode-stream s protocol))))


(defn- ampbox-client
  [host port]
  (d/chain
   (tcp/client {:host host, :port port})
   (partial wrap-duplex-stream ampbox-codec)))


(defn- start-ampbox-server
  [handler port]
  (tcp/start-server
    (fn [s info]
      (handler (wrap-duplex-stream ampbox-codec s) info))
    {:port port}))


(defn make-generator [init]
  (let [a (atom init)]
    #(swap! a inc)))


(defn- call-remote
  [stream pendings tag command arguments]
  (let [d   (d/deferred)
        box (command/to-box (:arguments command)
                            arguments)]
    (swap! pendings assoc tag [d command])
    (s/put! stream
            (merge box
                   {"_command" (:name command)
                    "_ask" tag}))
    d))


(defn- box-handler
  [pendings responder box]
  (let [tag (->> "_answer"
                 (get box)
                 (a/from-bytes {:type ::a/string}))
        [d com] (get @pendings tag)
        response (command/from-box (:response com) box)]
    (swap! pendings dissoc tag)
    (d/success! d response)))


(defn amp-connection
  [responder stream]
  (let [next-tag (make-generator 0)
        pendings (atom {})
        call-remote' #(call-remote stream pendings (str (next-tag)) %1 %2)
        close! #(s/close! stream)]
    (s/connect
     (s/map #(box-handler pendings responder %1) stream)
     stream)
    [call-remote' close!]))


(defn client
  [host port responder]
  (d/chain
   (ampbox-client host port)
   (partial amp-connection responder)))


(command/defcommand sum
  {:a {:type ::a/integer}
   :b {:type ::a/integer}}
  {:total {:type ::a/integer}}
  :command-name "Sum")


(defn -main
  [& args]
  @(d/chain
    (client "localhost" 1234 println)
    (fn [[call-remote close!]]
      (d/chain (call-remote sum {:a 42 :b 56})
               (fn [result]
                 (println result)
                 (close!))))))
