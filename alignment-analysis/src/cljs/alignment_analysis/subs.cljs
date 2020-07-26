(ns alignment-analysis.subs
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.queries :as q]))

(re-frame/reg-sub
 ::respondent-options
 (fn [db]
   (q/get-respondent-options db)))

(re-frame/reg-sub
 ::team-options
 (fn [db]
   (q/get-team-options db)))

(re-frame/reg-sub
 ::location-options
 (fn [db]
   (q/get-location-options db)))

(re-frame/reg-sub
 ::respondent-selections
 (fn [db]
   (q/get-respondent-selections db)))

(re-frame/reg-sub
 ::team-selections
 (fn [db]
   (q/get-team-selections db)))

(re-frame/reg-sub
 ::location-selections
 (fn [db]
   (q/get-location-selections db)))

(re-frame/reg-sub
 ::correlations
 (fn [db]
   (q/get-correlation-data db)))

(re-frame/reg-sub
 ::zscores
 (fn [db]
   (q/get-zscore-data db)))