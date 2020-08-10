(ns alignment-analysis.subs
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.queries :as q]))

(re-frame/reg-sub
 ::select-search-text
 (fn [db [_ select-type]]
   (q/get-select-search-text db select-type)))

(re-frame/reg-sub
 ::select-options
 (fn [db [_ select-type]]
   (q/get-select-options db select-type)))

(re-frame/reg-sub
 ::select-selections
 (fn [db [_ select-type]]
   (q/get-select-selections db select-type)))

(re-frame/reg-sub
 ::select-busy-state
 (fn [db [_ select-type]]
   (q/get-select-busy-state db select-type)))

(re-frame/reg-sub
 ::correlations
 (fn [db]
   (q/get-correlation-data db)))

(re-frame/reg-sub
 ::zscores
 (fn [db]
   (q/get-zscore-data db)))