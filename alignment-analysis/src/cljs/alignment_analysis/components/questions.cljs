(ns alignment-analysis.components.questions
  (:require [re-frame.core :as rf]
            [alignment-analysis.components.utils :refer [labelify]]
            [alignment-analysis.components.select :as select]
            [alignment-analysis.events.select :as select-events]
            [alignment-analysis.events.qna :as qna-events]
            [alignment-analysis.subs.select :as select-subs]))


(defn- update-question-options [select-id search-text]
  (rf/dispatch [::select-events/update-select-debounce select-id search-text
                ::qna-events/get-question-options
                ::debounce-questions]))

(defn on-value-change [select-id selected-value]
  (rf/dispatch [::select-events/set-select-selections select-id selected-value])
  (rf/dispatch [::qna-events/answers]))

(defn questions-select []
  (let [search-text @(rf/subscribe [::select-subs/select-search-text :questions])
        options @(rf/subscribe [::select-subs/select-options :questions])
        labels (labelify options)]

    (when (and (empty? options) (empty? search-text))
      (rf/dispatch [::qna-events/get-question-options]))

    (select/autocomplete-select "Question" true false labels update-question-options on-value-change)))

(defn questions []
  [questions-select])

