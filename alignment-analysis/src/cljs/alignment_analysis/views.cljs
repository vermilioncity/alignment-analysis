(ns alignment-analysis.views
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.subs :as subs]
   [alignment-analysis.events.scores :as s-events]
   [alignment-analysis.components.teams :as team-select]
   [oz.core :as oz]
   [reagent.core :as reagent]))

(defn get-min-max [scores axis]
  (println (map #(axis %) scores))
  (println axis)
  (println scores)
  (println (apply (juxt min max) (map #(axis %) scores)))
           (apply (juxt min max) (map #(axis %) scores)))

(defn scatterplot []
  (let [scores @(rf/subscribe [::subs/zscores])
        min-range (get-min-max scores :chaotic-vs-lawful)]
    
    (println min-range)
    
    {:data {:name "zscores"
            :values scores}
     :width 600
     :height 600
     :marks [{:name "marks"
              :type "symbol"
              :from {:data "zscores"}
              :encode
              {:update
               {:x {:scale "x", :field "evil_vs_good"}
                :y {:scale "y", :field "chaotic_vs_lawful"}
                :half {:scale "x", :field "chaotic_vs_lawful"}
                :shape {:value "circle"}
                :strokeWidth {:value 2}
                :opacity {:value 0.5}
                :stroke {:value "#4682b4"}
                :fill {:value "transparent"}}}}]
     :scales [{:name "x"
               :type "linear"
               :round true
               :nice true
               :zero false
               :domain {:data "zscores" :field "evil_vs_good"}
               :range "width"}
              {:name "y"
               :type "linear"
               :round true
               :nice true
               :zero false
               :domain {:data "zscores" :field "chaotic_vs_lawful"}
               :range "height"}
              {:name "x2"
               :type "linear"
               :round true
               :nice true
               :zero false
               :domain {:data "zscores" :field "evil_vs_good"}
               :range "width"}
              {:name "y2"
               :type "linear"
               :round true
               :nice true
               :zero false
               :domain {:data "zscores" :field "chaotic_vs_lawful"}
               :range "height"}]
     :axes [{:scale "x"
             :grid true
             :domain false
             :orient "bottom"
             :tickCount 5
             :title "Evil vs. Good"}
            {:scale "y"
             :grid true
             :domain false
             :orient "left"
             :titlePadding 5
             :title "Chaotic vs. Lawful"}
            {:scale "y2"
             :grid true
             :domain false
             :orient "left"
             :titlePadding 5
             :tickCount 1
             :gridWidth 1
             :gridColor "black"}
            {:scale "x2"
             :grid true
             :domain false
             :orient "bottom"
             :titlePadding 5
             :tickCount 1
             :gridWidth 1
             :gridColor "black"}]}))


(defn header
  []
  [:div
   [:h1 "A template for oz apps"]])


(defn main-panel []
  (when (nil? @(rf/subscribe [::subs/zscores]))
    (rf/dispatch [::s-events/zscores]))

  (let [scores @(rf/subscribe [::subs/zscores])]
    (when (not (nil? scores))
    [:div
     [:h1 {:style {:font-family "Tahoma"}} "Alignment Analysis!"]
     [:div (reagent/as-element [team-select/teams-select])]
     [oz/vega (scatterplot)]])))
