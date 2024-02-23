(ns alignment-analysis.queries.select
  (:require [clojure.string :as str]))

(defn- get-select-key [select-type]
  (if (str/ends-with? (name select-type) "-select")
    (keyword (first (str/split (name select-type) "-")))
    select-type))

(defn get-select-search-text [db select-type]
  (get-in db [(get-select-key select-type) :search-text]))

(defn get-select-options [db select-type]
  (get-in db [(get-select-key select-type) :options]))

(defn get-select-selections [db select-type]
  (get-in db [(get-select-key select-type) :selections]))

(defn get-select-busy-state [db select-type]
  (get-in db [(get-select-key select-type) :busy-state]))

(defn set-select-search-text [db select-type search-text]
  (assoc-in db [(get-select-key select-type) :search-text] search-text))

(defn set-select-options [db select-type options]
  (assoc-in db [(get-select-key select-type) :options] options))

(defn set-select-selections [db select-type selections]
  (assoc-in db [(get-select-key select-type) :selections] selections))

(defn set-select-busy-state [db select-type busy-state]
  (assoc-in db [(get-select-key select-type) :busy-state] busy-state))
