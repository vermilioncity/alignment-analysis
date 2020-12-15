(ns alignment-analysis.events.db
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))