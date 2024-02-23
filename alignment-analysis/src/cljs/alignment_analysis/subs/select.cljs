(ns alignment-analysis.subs.select
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries.select :as select-q]
   [alignment-analysis.utils.labels :as l-utils]))


(rf/reg-sub
 ::select-search-text
 (fn [db [_ select-type]]
   (select-q/get-select-search-text db select-type)))

(rf/reg-sub
 ::select-options
 (fn [db [_ select-type]]
   (select-q/get-select-options db select-type)))

(rf/reg-sub
 ::select-selections
 (fn [db [_ select-type]]
   (select-q/get-select-selections db select-type)))

(rf/reg-sub
 ::select-busy-state
 (fn [db [_ select-type]]
   (select-q/get-select-busy-state db select-type)))

(rf/reg-sub
 ::team-viewable-options
 (fn []
   [(rf/subscribe [::select-options :teams])
    (rf/subscribe [::select-selections :teams])])
 (fn [[options selections]]
   (l-utils/format-labels options selections)))