(ns alignment-analysis.subs
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils.labels :as l-utils]))

(rf/reg-sub
 ::select-search-text
 (fn [db [_ select-type]]
   (q/get-select-search-text db select-type)))

(rf/reg-sub
 ::select-options
 (fn [db [_ select-type]]
   (q/get-select-options db select-type)))

(rf/reg-sub
 ::select-selections
 (fn [db [_ select-type]]
   (q/get-select-selections db select-type)))

(rf/reg-sub
 ::select-busy-state
 (fn [db [_ select-type]]
   (q/get-select-busy-state db select-type)))

(rf/reg-sub
 ::team-viewable-options
 (fn []
   [(rf/subscribe [::select-options :teams])
    (rf/subscribe [::select-selections :teams])
    ])
 (fn [[options selections]]
   (l-utils/format-labels options selections)))

(rf/reg-sub
 ::correlations
 (fn [db]
   (q/get-correlation-data db)))

(rf/reg-sub
 ::zscores
 (fn [db]
   (q/get-zscore-data db)))