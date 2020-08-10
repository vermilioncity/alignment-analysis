(ns alignment-analysis.components.teams
  (:require [re-frame.core :as re-frame]
            [alignment-analysis.components.select :as select]
            [alignment-analysis.events.select :as select-events]
            [alignment-analysis.events.teams :as team-events]
            [alignment-analysis.subs :as subs]
            [clojure.walk :refer [postwalk-replace]]))

(defn- format-for-select [teams]
  (map #(postwalk-replace {:name :label :id :value :subteams :options} %) teams))

(defn- update-team-options [select-id search-text]
(re-frame/dispatch [::select-events/update-select-debounce select-id search-text
                      ::select-events/get-team-options
                      ::debounce-teams]))

(defn on-value-change [select-id selected-value]
  (re-frame/dispatch [::select-events/set-select-selections select-id selected-value]))


(defn teams-select []
  (let [search-text @(re-frame/subscribe [::subs/select-search-text :teams])
        options-list (format-for-select @(re-frame/subscribe [::subs/select-options :teams]))]
    
    (when (and (empty? options-list) (empty? search-text))
      (re-frame/dispatch [::team-events/get-team-options]))

  (select/autocomplete-select "Teams" true false options-list update-team-options on-value-change)))
