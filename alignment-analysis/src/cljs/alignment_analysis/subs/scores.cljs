(ns alignment-analysis.subs.scores
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries.scores :as scores-q]))

(rf/reg-sub
 ::correlation
 (fn [db]
   (scores-q/get-score-data db :correlation)))

(rf/reg-sub
 ::zscores
 (fn [db]
   (scores-q/get-score-data db :zscores))
 )