(ns alignment-analysis.components.utils
  (:require [clojure.set :refer [rename-keys]]))

(defn labelify [options]
  (sort-by :label (map #(rename-keys % {:name :label :id :value}) options)))
