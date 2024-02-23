(ns alignment-analysis.components.respondents
  (:require [re-frame.core :as rf]
            [alignment-analysis.components.utils :refer [labelify]]
            [alignment-analysis.components.select :as select]
            [alignment-analysis.events.select :as select-events]
            [alignment-analysis.events.scores :as score-events]
            [alignment-analysis.events.respondents :as respondent-events]
            [alignment-analysis.subs.select :as select-subs]))

(defn- update-respondent-options [select-id search-text]
  (rf/dispatch [::select-events/update-select-debounce select-id search-text
                ::respondent-events/get-respondent-options
                ::debounce-respondents]))

(defn on-value-change [select-id selected-value]
  (rf/dispatch [::select-events/set-select-selections select-id selected-value])
  (rf/dispatch [::score-events/zscores]))

(defn respondents-select []
  (let [search-text @(rf/subscribe [::select-subs/select-search-text :respondents])
        options @(rf/subscribe [::select-subs/select-options :respondents])
        labels (labelify options)]

    (when (and (empty? options) (empty? search-text))
      (rf/dispatch [::respondent-events/get-respondent-options]))

    (select/autocomplete-select "Respondents" true false labels update-respondent-options on-value-change)))
