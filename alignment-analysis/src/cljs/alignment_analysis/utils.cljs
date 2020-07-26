(ns alignment-analysis.utils
  (:require [clojure.set :as set]))

(defn filter-valid-selections [existing-selection valid-options]
  (set/intersection (set existing-selection) (set valid-options)))

(defn not-empty? [val]
  (not (empty? val)))

(defn filter-map-empty-values [map]
  (into {} (filter second map)))