(ns alignment-analysis.utils.labels
  (:require [clojure.set :as set]
            [clojure.walk :refer [postwalk-replace]]
            [alignment-analysis.utils.funcs :as f-utils]))

;; NOTE TO LATER ME:
;; if you don't understand WTF was written here
;; just know that when I wrote it, my understanding
;; was just as tenuous :)

(defn- format-for-select
  "Replaces keys to make them acceptable for react select"
  [vals]
  (map #(postwalk-replace {:name :label :id :value :subteams :options} %) vals))

(defn- tree-seq-depth
  "Flattens multilevel and adds a key/val pair for depth"
  [branch? children root]
  (let [walk (fn walk [depth node]
               (lazy-seq
                (cons [(-> node (dissoc branch?)
                                (assoc :level depth)
                                (assoc :parent (f-utils/not-empty? (branch? node))))]
                      (when (branch? node)
                        (mapcat
                         (partial walk
                                  (inc depth)) (children node))))))]
    (flatten (walk 0 root))))

(def select-team-options
  (comp
   format-for-select
   (partial tree-seq-depth :subteams :subteams)))

(defn- tree-seq-path
  "Gets child keys of a particular node"
  [branch? children root & [node-fn]]
  (let [node-fn (or node-fn identity)
        walk (fn walk [path node]
               (let [new-path (node-fn node)]
                 (lazy-seq
                  (cons new-path
                        (when (branch? node)
                          (mapcat (partial walk new-path) (children node)))))))]
    (walk [] root)))

(defn get-child-teams
  "Grabs a particular node and finds child ids"
  [coll parent-ids]
  (let [parents (vec (map #(:value %) parent-ids))]
    
  (->> (tree-seq #(or (map? %) (vector? %)) identity coll)
       (filter #(if (and (map? %) (f-utils/in? parents (:id %))) true  false))
       (map #(tree-seq-path :subteams :subteams % :id))
       flatten)))

(defn format-labels 
  [options-list selections]
  (let [child-teams (get-child-teams options-list selections)]
    (if (nil? options-list) nil
        (->> options-list
             (mapcat select-team-options)
             (map #(assoc % :hidden (f-utils/in? (vec child-teams) (:value %))))))))

(defn filter-valid-selections [existing-selection valid-options]
  (set/intersection (set existing-selection) (set valid-options)))