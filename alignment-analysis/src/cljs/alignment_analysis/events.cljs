(ns alignment-analysis.events
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
