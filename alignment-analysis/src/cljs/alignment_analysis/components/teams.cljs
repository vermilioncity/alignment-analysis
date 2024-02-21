(ns alignment-analysis.components.teams
  (:require [re-frame.core :as rf]
            [alignment-analysis.components.select :as select]
            [alignment-analysis.events.select :as select-events]
            [alignment-analysis.events.scores :as score-events]
            [alignment-analysis.events.teams :as team-events]
            [alignment-analysis.subs.select :as select-subs]))

(defn- update-team-options [select-id search-text]
(rf/dispatch [::select-events/update-select-debounce select-id search-text
                      ::team-events/get-team-options
                      ::debounce-teams]))

(defn on-value-change [select-id selected-value]
  (rf/dispatch [::select-events/set-select-selections select-id selected-value])
  (rf/dispatch [::score-events/zscores]))

(defn teams-select []
  (let [search-text @(rf/subscribe [::select-subs/select-search-text :teams])
        viewable-options @(rf/subscribe [::select-subs/team-viewable-options])]

    (when (and (empty? viewable-options) (empty? search-text))
      (rf/dispatch [::team-events/get-team-options]))

    (select/autocomplete-select "Teams" true false viewable-options update-team-options on-value-change)))
