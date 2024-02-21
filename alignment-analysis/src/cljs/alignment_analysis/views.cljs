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
   [oz.core :as oz]
   [reagent.core :as reagent]))


(defn main-panel []
  (when (nil? @(rf/subscribe [::score-subs/zscores]))
    (rf/dispatch [::score-events/zscores]))

  (when (nil? @(rf/subscribe [::score-subs/correlation]))
    (rf/dispatch [::score-events/correlation]))

  (let [scores @(rf/subscribe [::score-subs/zscores])]
    (when (not (nil? scores))
      [:div {:style { :margin-left "20px"}}
       [:h1 {:style {:font-family "Tahoma"}} "Alignment Analysis!"]
       [:div {:style {:display "flex" :align-items "flex-start" :flex-wrap "nowrap" :flex-direction "column"}}
        [:div {:style {:display "flex" :align-items "flex-start" :flex-wrap "nowrap" :flex-direction "row"}}
         [:div {:style {:flex "2" :min-width "0"}}
          [oz/vega (scatterplot)]
          ]
         [:div
          [correlation]]]]
       [:h4 {:style {:font-family "Tahoma" :margin-left "340px"}} "Filters"]
       [:div {:style {:display "flex" :flex-direction "row"
                      :justify-content "center" :align-items "flex-start" :max-width "700px"}}
        [:div {:style {:margin-bottom "10px" :margin-right "10px" :width "100%"}}
         (reagent/as-element [teams-select])]
        [:div {:style {:margin-bottom "10px" :margin-right "10px" :width "80%"}}
         (reagent/as-element [locations-select])]
        [:div {:style {:margin-bottom "10px" :margin-right "10px" :width "80%"}}
         (reagent/as-element [respondents-select])]]])))
