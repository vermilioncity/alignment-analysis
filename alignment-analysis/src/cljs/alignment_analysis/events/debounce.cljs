(ns alignment-analysis.events.debounce
  (:require [re-frame.core :refer [reg-fx dispatch]]))

(defn now [] (.getTime (js/Date.)))

(def registered-keys (atom nil))

(defn dispatch-if-not-superceded [{:keys [key delay event time-received]}]
  (when (= time-received (get @registered-keys key))
    ;; no new events on this key!
    (dispatch event)))

(defn dispatch-later [{:keys [delay] :as debounce}]
  (js/setTimeout
   (fn [] (dispatch-if-not-superceded debounce))
   delay))

(reg-fx
 :dispatch-debounce
 (fn dispatch-debounce [debounce]
   (let [ts (now)]
     (swap! registered-keys assoc (:key debounce) ts)
     (dispatch-later (assoc debounce :time-received ts)))))