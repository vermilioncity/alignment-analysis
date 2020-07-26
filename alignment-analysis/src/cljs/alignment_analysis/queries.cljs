(ns alignment-analysis.queries
  (:require [alignment-analysis.utils :as utils]))

(defn get-respondent-options [db]
  (get-in db [:respondents :options]))

(defn set-respondent-options [db vals]
  (assoc-in db [:respondents :options] vals))

(defn get-respondent-selections [db]
  (get-in db [:respondents :selections]))

(defn set-respondent-selections [db vals]
  (let [valid-options (get-respondent-options db)
        valid-selections ((utils/filter-valid-selections vals valid-options))]
    (assoc-in db [:respondents :selections] valid-selections)))

(defn set-respondent-busy-state [db busy-state]
  (assoc-in db [:respondents :busy-state] busy-state))

(defn get-respondent-busy-state [db]
  (get-in db [:respondents :selections]))

(defn get-team-options [db]
  (get-in db [:teams :options]))

(defn set-team-options [db vals]
  (assoc-in db [:teams :options] vals))

(defn get-team-selections [db]
  (get-in db [:teams :selections]))

(defn set-team-selections [db vals]
  (let [valid-options (get-team-options db)
        valid-selections ((utils/filter-valid-selections vals valid-options))]
    (assoc-in db [:teams :selections] valid-selections)))

(defn get-team-busy-state [db]
  (get-in db [:teams :selections]))

(defn set-team-busy-state [db busy-state]
  (assoc-in db [:teams :busy-state] busy-state))

(defn get-location-options [db]
  (get-in db [:locations :options]))

(defn set-location-options [db vals]
  (assoc-in db [:locations :options] vals))

(defn get-location-selections [db]
  (get-in db [:locations :selections]))

(defn set-location-selections [db vals]
  (let [valid-options (get-location-options db)
        valid-selections ((utils/filter-valid-selections vals valid-options))]
    (assoc-in db [:locations :selections] valid-selections)))

(defn get-location-busy-state [db]
  (get-in db [:locations :selections]))

(defn set-location-busy-state [db busy-state]
  (assoc-in db [:locations :busy-state] busy-state))

(defn get-correlation-data [db]
  (get-in db [:correlations :data]))

(defn set-correlation-data [db vals]
  (assoc-in db [:correlations :data] vals))

(defn get-correlation-busy-state [db]
  (get-in db [:correlations :busy-state]))

(defn set-correlation-busy-state [db busy-state]
  (assoc-in db [:correlations :busy-state] busy-state))

(defn get-zscore-data [db]
  (get-in db [:zscores :data]))

(defn set-zscore-data [db vals]
  (assoc-in db [:zscores :data] vals))

(defn get-zscore-busy-state [db]
  (get-in db [:zscores :busy-state]))

(defn set-zscore-busy-state [db busy-state]
  (assoc-in db [:zscores :busy-state] busy-state))