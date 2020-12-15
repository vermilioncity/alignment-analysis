(ns alignment-analysis.utils.funcs
  (:require [clojure.set :as set]))

(defn filter-valid-selections [existing-selection valid-options]
  (set/intersection (set existing-selection) (set valid-options)))

(defn filter-map-empty-values [map]
  (into {} (filter second map)))

(defn in? [coll v]
  (some #(= v %) coll))

(defn is-empty? [v]
  (cond
    (boolean? v)   (not v)
    (string? v)    (= v "")
    (undefined? v) true
    (nil? v)       true
    (fn? v)        false
    (seq? v)       (empty? v)
    (vector? v)    (empty? v)
    (set? v)       (empty? v)
    (map? v)       (empty? v)
    (array? v)     (empty? v)
    (object? v)    (empty? (js->clj v))

    :else false))

(defn not-empty? [val]
  (not (is-empty? val)))

(defn has-value? [v]
  (not (is-empty? v)))

(defn to-seq [a]
  (if (has-value? a)
    (if (sequential? a) a [a])
    []))

(defn gt? [x y]
  (case (compare x y)
    1  true
    0  false
    -1 false))