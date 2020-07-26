(ns alignment-analysis.utils)

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

(defn has-value? [v]
  (not (is-empty? v)))

(defn to-seq [a]
  (if (has-value? a)
    (if (sequential? a) a [a])
    []))


