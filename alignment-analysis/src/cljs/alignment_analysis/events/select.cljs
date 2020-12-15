(ns alignment-analysis.events.select
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils.funcs :as f-utils]
   [alignment-analysis.events.debounce]))


(rf/reg-event-db
 ::handle-select-option-success
 (fn [db [_ select-type resp]]
   (-> db
       (q/set-select-options select-type resp)
       (q/set-select-busy-state select-type false))))

(rf/reg-event-fx
 ::handle-failure
 (fn [{db :db} [_ select-type _]]
   (-> db
       (q/set-select-selections select-type [])
       (q/set-select-busy-state select-type false))))

(rf/reg-event-db
 ::set-select-selections
 (fn [db [_ select-type resp]]
   (q/set-select-selections db select-type (js->clj resp :keywordize-keys true))))

(rf/reg-event-fx
 ::issue-debounce
 (fn [cofx [_ select-id event key]]
   (let [db (:db cofx)]
     {:dispatch-debounce {:key key
                          :event [event select-id]
                          :delay 300}})))
(rf/reg-event-db
 ::set-select-search-text
 (fn [db [_ select-type resp]]
   (q/set-select-search-text db select-type resp)))

(rf/reg-event-fx
 ::update-select-debounce
 (fn [cofx [_ select-id search-text event key]]
   (let [db (:db cofx)
         char-count (count search-text)
         to-dispatch (if (f-utils/gt? char-count 2)
                       (list [::issue-debounce select-id event key]
                             [::set-select-search-text select-id search-text])
                       (list [::set-select-search-text select-id search-text]))]
     {:dispatch-n to-dispatch})))