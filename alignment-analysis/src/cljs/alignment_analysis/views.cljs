(ns alignment-analysis.views
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.subs :as subs]
   [alignment-analysis.select :as select]
   [oz.core :as oz]
   [reagent.core :as reagent]))

(def line-plot
  {:data {:name "table"
          :values [{:Horsepower 1, :Miles_per_Gallon 28 :Origin "Blue" :Acceleration 1}
                   {:Horsepower 2, :Miles_per_Gallon 55 :Origin "Blue" :Acceleration 2}
                   {:Horsepower 3, :Miles_per_Gallon 43 :Origin "Blue" :Acceleration 3}
                   {:Horsepower 4, :Miles_per_Gallon 31 :Origin "Red" :Acceleration 4}
                   {:Horsepower 5, :Miles_per_Gallon 41 :Origin "Red" :Acceleration 5}]}
   :width 600
   :height 600
   :marks [{:name "marks"
            :type "symbol"
            :from {:data "table"}
            :encode
            {:update
             {:x {:scale "x", :field "Horsepower"}
              :y {:scale "y", :field "Miles_per_Gallon"}
              :shape {:value "circle"}
              :strokeWidth {:value 2}
              :opacity {:value 0.5}
              :stroke {:value "#4682b4"}
              :fill {:value "transparent"}}}}]
   :legends [{:title "Acceleration"
              :format "s"
              :symbolStrokeColor "#4682b4"
              :symbolStrokeWidth 2
              :symbolOpacity 0.5
              :symbolType "circle"
              :size "size"}]
   :scales [{:name "x"
             :type "linear"
             :round true
             :nice true
             :zero true
             :domain {:data "table" :field "Horsepower"}
             :range "width"}
             {:name "y"
             :type "linear"
             :round true
             :nice true
             :zero true
             :domain {:data "table" :field "Miles_per_Gallon"}
             :range "height"}
            {:name "half"
             :type "linear"
             :round true
             :nice false
             :zero true
             :domain {:data "table" :field "Acceleration"}
             :range [300, 300]}
            {:name "size"
             :type "linear"
             :round true
             :nice false
             :zero true
             :domain {:data "table" :field "Acceleration"}
             :range [1,5]}]
   :axes [{:scale "x"
           :grid true
           :domain false
           :orient "bottom"
           :tickCount 5
           :title "Horsepower"}
          {:scale "y"
           :grid true
           :domain false
           :orient "left"
           :titlePadding 5
           :title "Miles_per_Gallon"}
          {:scale "half"
           :grid true
           :domain false
           :orient "left"
           :titlePadding 5
           :tickCount 1
           :gridWidth 1
           :gridColor "black"}
          {:scale "half"
           :grid true
           :domain false
           :orient "bottom"
           :titlePadding 5
           :tickCount 1
           :gridWidth 1
           :gridColor "black"}]})

(defn header
  []
  [:div
   [:h1 "A template for oz apps"]])

(defn main-panel []
  (let [name "test"]
    [:div
     [:h1 "Hello from " name]
     [:div (reagent/as-element [select/header])]]))
