(ns alignment-analysis.queries)

(defn get-select-search-text [db select-type]
  (get-in db [(keyword select-type) :search-text]))

(defn get-select-options [db select-type]
  (get-in db [(keyword select-type) :options]))

(defn get-select-selections [db select-type]
  (get-in db [(keyword select-type) :choices]))

(defn get-select-busy-state [db select-type]
  (get-in db [(keyword select-type) :busy-state]))

(defn set-select-search-text [db select-type search-text]
  (assoc-in db [(keyword select-type) :search-text] search-text))

(defn set-select-options [db select-type options]
  (assoc-in db [(keyword select-type) :options] options))

(defn set-select-selections [db select-type selections]
  (assoc-in db [(keyword select-type) :selections] selections))

(defn set-select-busy-state [db select-type busy-state]
  (assoc-in db [(keyword select-type) :busy-state] busy-state))

#_(defn set-respondent-selections [db vals]
  (let [valid-options (get-respondent-options db)
        valid-selections ((utils/filter-valid-selections vals valid-options))]
    (assoc-in db [:respondents :selections] valid-selections)))

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