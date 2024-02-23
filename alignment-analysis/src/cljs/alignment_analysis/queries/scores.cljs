(ns alignment-analysis.queries.scores)

(defn get-score-data [db score-type]
  (get-in db [:scores score-type :data]))

(defn set-score-data [db score-type vals]
  (assoc-in db [:scores score-type :data] vals))

(defn get-score-busy-state [db score-type]
  (get-in db [:scores score-type :busy-state]))

(defn set-score-busy-state [db score-type busy-state]
  (assoc-in db [:scores score-type :busy-state] busy-state))