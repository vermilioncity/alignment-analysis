(ns alignment-analysis.events.select
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils :as utils]
   [alignment-analysis.events.debounce]))


(re-frame/reg-event-db
 ::handle-select-option-success
 (fn [db [_ select-type resp]]
   (-> db
       (q/set-select-options select-type resp)
       (q/set-select-busy-state select-type false))))

(re-frame/reg-event-fx
 ::handle-failure
 (fn [{db :db} [_ select-type _]]
   (-> db
       (q/set-select-selections select-type [])
       (q/set-select-busy-state select-type false))))

(re-frame/reg-event-db
 ::set-select-selections
 (fn [db [_ select-type resp]]
   (q/set-select-selections db select-type resp)))

(re-frame/reg-event-fx
 ::issue-debounce
 (fn [cofx [_ select-id event key]]
   (let [db (:db cofx)]
     {:dispatch-debounce {:key key
                          :event [event select-id]
                          :delay 300}})))
(re-frame/reg-event-db
 ::set-select-search-text
 (fn [db [_ select-type resp]]
   (q/set-select-search-text db select-type resp)))

(re-frame/reg-event-fx
 ::update-select-debounce
 (fn [cofx [_ select-id search-text event key]]
   (let [db (:db cofx)
         char-count (count search-text)
         to-dispatch (if (utils/gt? char-count 2)
                       (list [::issue-debounce select-id event key]
                             [::set-select-search-text select-id search-text])
                       (list [::set-select-search-text select-id search-text]))]
     {:dispatch-n to-dispatch})))