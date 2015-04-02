(ns clj-amp.core
  (:require [clj-amp.box :as box]
            [clj-amp.command :as command]
            [clj-amp.argument :as a]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [aleph.tcp :as tcp]
            [gloss.io :as io]
            [slingshot.slingshot :refer [throw+]]
            [plumbing.core :refer [for-map]]))


(defn- ampbox-client
  [host port]
  (d/chain
   (tcp/client {:host host, :port port})
   box/wrap-ampbox-stream))


(defn start-ampbox-server
  [handler port]
  (tcp/start-server
    (fn [s info]
      (handler (box/wrap-ampbox-stream s) info))
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
    (s/put!
     stream
     (merge box
            {"_command" (:name command)
             "_ask" tag}))
    d))


(defn- str-from-box
  [key box]
  (let [val (get box key)]
    (if-not (nil? val)
      (a/from-bytes {:type ::a/byte-string} val))))


(defn- dispatch-response
  [pendings box]
  (let [tag      (str-from-box "_answer" box)
        [d com]  (get @pendings tag)
        response (command/from-box (:response com) box)]
    (swap! pendings dissoc tag)
    (d/success! d response)
    nil))


(defn- dispatch-error
  [pendings box]
  (let [tag     (str-from-box "_error" box)
        [d com] (get @pendings tag)
        error   (command/error-from-box com box)]
    (swap! pendings dissoc tag)
    (d/error! d error)
    nil))


(defn- protocol-error
  [error-type]
  (throw+ {:type :amp-protocol-error}))


(defn- box-handler
  [pendings responder box]
  (cond
    (contains? box "_answer")  (dispatch-response pendings box)
    (contains? box "_error")   (dispatch-error pendings box)
    (contains? box "_command") (responder box)
    :else                      (protocol-error :no-empty-boxes)))


(defn amp-connection
  [responder stream]
  (let [next-tag     (make-generator 0)
        pendings     (atom {})
        call-remote' #(call-remote stream pendings (str (next-tag)) %1 %2)
        close!       #(s/close! stream)]
    (s/connect
     (->> stream
          (s/map (partial box-handler pendings responder))
          (s/realize-each)
          (s/filter (comp not nil?)))
     stream)
    [call-remote' close!]))


(defn make-responder
  [responders]
  (let [responders'
        (for-map [[c r] responders]
                 (:name c)
                 [c r])]
    (fn [box]
      (let [name (str-from-box "_command" box)
            tag  (str-from-box "_ask" box)]
        (if (contains? responders' name)
          (let [[command responder]
                (get responders' name)]
            (-> (command/from-box (:arguments command) box)
                (d/chain
                 responder
                 #(assoc
                   (command/to-box (:response command) %)
                   "_answer" tag))
                (d/catch
                    #(assoc
                      (command/error-to-box command %)
                      "_error" tag))))
          ({"_command"           name
            "_error"             tag
            "_error_code"        "UNHANDLED"
            "_error_description" "Unhandled command"}))))))


(defn client
  [host port responder]
  (d/chain
   (ampbox-client host port)
   (partial amp-connection responder)))


(defn simple-server
  [responder port]
  (start-ampbox-server
   (fn [stream info]
     (amp-connection responder stream))
   port))
