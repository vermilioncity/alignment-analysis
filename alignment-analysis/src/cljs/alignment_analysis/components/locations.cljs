(ns alignment-analysis.components.locations
  (:require [re-frame.core :as rf]
            [alignment-analysis.components.utils :refer [labelify]]
            [alignment-analysis.components.select :as select]
            [alignment-analysis.events.select :as select-events]
            [alignment-analysis.events.scores :as score-events]
            [alignment-analysis.events.locations :as location-events]
            [alignment-analysis.events.respondents :as respondent-events]
            [alignment-analysis.subs.select :as select-subs]))

(defn- update-location-options [select-id search-text]
  (rf/dispatch [::select-events/update-select-debounce select-id search-text
                ::location-events/get-location-options
                ::debounce-locations]))

(defn on-value-change [select-id selected-value]
  (rf/dispatch [::select-events/set-select-selections select-id selected-value])
  (rf/dispatch [::score-events/zscores])
  (rf/dispatch [::update-options-based-on-selection :location selected-value])
  (rf/dispatch [::respondent-events/get-respondent-options])
  )

(defn locations-select []
  (let [search-text @(rf/subscribe [::select-subs/select-search-text :locations])
        options @(rf/subscribe [::select-subs/select-options :locations])
        labels (labelify options)]

    (when (and (empty? options) (empty? search-text))
      (rf/dispatch [::location-events/get-location-options]))

    (select/autocomplete-select "Locations" true false labels update-location-options on-value-change)))
