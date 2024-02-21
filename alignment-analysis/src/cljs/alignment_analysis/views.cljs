(ns alignment-analysis.views
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.subs.scores :as score-subs]
   [alignment-analysis.events.scores :as score-events]
   [alignment-analysis.components.teams :refer [teams-select]]
   [alignment-analysis.components.locations :refer [locations-select]]
   [alignment-analysis.components.respondents :refer [respondents-select]]
   [alignment-analysis.components.scatterplot :refer [scatterplot]]
   [alignment-analysis.components.correlation :refer [correlation]]
   [alignment-analysis.components.questions :refer [questions]]
   [oz.core :as oz]
   [reagent.core :as reagent]))


(defn main-panel []
  (when (nil? @(rf/subscribe [::score-subs/zscores]))
    (rf/dispatch [::score-events/zscores]))
  
  (when (nil? @(rf/subscribe [::score-subs/correlation]))
    (rf/dispatch [::score-events/correlation]))

  (let [scores @(rf/subscribe [::score-subs/zscores])]
    (when (not (nil? scores))
    [:div
     [:h1 {:style {:font-family "Tahoma"}} "Alignment Analysis!"]
     [:div {:style {:display "flex"}}
      [oz/vega (scatterplot)]
      [questions]]
     [:div
     [correlation]]
     [:div {:id "filter-box"}
      [:div {:style {:font-family "Tahoma"
                     :font-size 20
                     :width "100%"
                     :text-align "center"}} "Filters"]
     [:div {:style {:display "flex"
                    :width "100%"}}
      [:div {:style {:margin "10px 10px 10px 0"
                     :width "30%"}}
       (reagent/as-element [teams-select])]
      [:div {:style {:margin "10px 10px 10px 0"
                     :width "30%"}}
       (reagent/as-element [locations-select])]
      [:div {:style {:margin "10px 10px 10px 0"
                     :width "30%"}}
       (reagent/as-element [respondents-select])]
      ]]])))
