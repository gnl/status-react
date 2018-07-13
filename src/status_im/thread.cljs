(ns status-im.thread
  (:require [cognitect.transit :as transit]
            [re-frame.db]
            [status-im.tron :as tron]))

(println "\n\n" :WOW (nil? (.-UIManager (js/require "react-native"))) "\n\n")
(def rn-threads (.-Thread (js/require "react-native-threads")))
#_(rn-threads. (str "worker." "android" ".js"))

(goog-define platform "android")

(def thread (atom nil))
(def writer (transit/writer :json))
(def reader (transit/reader :json))

(def initialized? (atom false))
(def calls (atom []))

(defn post [data-str]
  (tron/log (str "Post message: " data-str))
  (.postMessage thread data-str))

(defn make-stored-calls []
  (doseq [call @calls]
    (post call)))

(defn dispatch [args]
  #_(println :HEY args)
  (let [args' (transit/write writer args)]
    (tron/log (str "dispatch: " (first args)))
    (if @initialized?
      (post args')
      (swap! calls conj args'))))

(comment

 (.terminate thread))

(defn start []
  (let [thread' (reset! thread (rn-threads. (str "worker." platform ".js")))]
    (set! (.-onmessage thread')
          (fn [data]
            (let [[event event-data :as data'] (transit/read reader data)]
              (tron/log (str "Response: " data'))

              (case event
                :initialized (do (reset! initialized? true)
                                 (make-stored-calls))
                :db (reset! re-frame.db/app-db event-data)))))))

