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


(defn answers-chart []
  (let [answers @(rf/subscribe [::qna-subs/answers])]

{:width 500
 :height 200
 :padding 5

 :data [
    {
      :name "answers"
      :values answers
      :transform [
        {
          :type "stack"
          :sort {:field "c"}
          :field "y"
        }
      ]
    }
  ]

  :scales [
    {
      :name "x"
      :type "band"
      :range "width"
      :domain {:data "answers"
               :field "x"}
    }
    {
      :name "y"
      :type "linear"
      :range "height"
      :nice true
      :zero true
      :domain {:data "answers" :field "y1"}
    }
    {
      :name "color"
      :type "ordinal"
      :range "category"
      :domain {:data "answers" :field "c"}
    }
  ]

  :axes [
    {:orient "bottom" :scale "x" :zindex 1}
    {:orient "left" :scale "y" :zindex 1}
  ]

  :marks [
    {
      :type "rect"
      :from {:data "answers"}
      :encode {
        :enter {
          :x {:scale "x" :field "x"}
          :width {:scale "x" :band 1 :offset -1}
          :y {:scale "y" :field "y0"}
          :y2 {:scale "y" :field "y1"}
          :fill {:scale "color" :field "c"}
        }
        :update {
          :fillOpacity {:value 1}
        }
        :hover {
          :fillOpacity {:value 0.5}
        }
      }
    }
  ]
}
