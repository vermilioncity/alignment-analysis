(ns alignment-analysis.components.scatterplot
  (:require
   [goog.string :as gstring]
   [re-frame.core :as rf]
   [alignment-analysis.subs.scores :as score-subs]
   [alignment-analysis.subs.select :as select-subs]
   [clojure.string :as str]))

(def blue "#4C78A8")
(def yellow "#FFDB58")
(def dark-yellow "#FFDB58")

(def inactive {:fill {:value blue}})

(def active {:fill {:value yellow}
             :stroke {:value dark-yellow}})

(def tooltip-label
  (let [chaos "round(datum.chaotic_vs_lawful)"
        evil "round(datum.evil_vs_good)"
        conds [(str "if ((" chaos " > 0) && (" evil " < 0), 'Lawful/Evil', ")
               (str "if ((" chaos " > 0) && (" evil " > 0), 'Lawful/Good', ")
               (str "if ((" chaos " < 0) && (" evil " > 0), 'Chaotic/Good', ")
               (str "if ((" chaos " < 0) && (" evil " < 0), 'Chaotic/Evil', ")
               (str "if ((" chaos " == 0) && (" evil " < 0), 'Neutral/Evil', ")
               (str "if ((" chaos " == 0) && (" evil " > 0), 'Neutral/Good', ")
               (str "if ((" chaos " > 0) && (" evil " == 0), 'Lawful/Neutral', ")
               (str "if ((" chaos " < 0) && (" evil " == 0), 'Chaotic/Neutral', ")]
        fmtconds (apply str (interpose "" conds))]
    (str "{'Name': datum['name'], 'Assignment' : " fmtconds " 'Neutral'))))))))}")))

(defn scatterplot []
  (let [scores @(rf/subscribe [::score-subs/zscores])
        respondents @(rf/subscribe [::select-subs/select-selections :respondents])]

    {:data {:name "zscores"
            :values scores}
     :width 600
     :height 600
     :title {:text "Alignment Score"
              :fontSize 20}
     :selection {:hover {:type "single"
                         :empty "none"
                         :on "mouseover"}
                 :select {:type "multi"}}
     :marks [{:name "lawful-evil"
              :type "text"
              :encode {:enter {:fill {:value "#dbd9d9"}
                               :text {:value "Lawful/Evil"}
                               :x {:value 75}
                               :y {:value 170}
                               :fontSize {:value 30}
                               :opacity {:value 0.5}}}}
             {:name "chaotic-evil"
              :type "text"
              :encode {:enter {:fill {:value "#dbd9d9"}
                               :text {:value "Chaotic/Evil"}
                               :x {:value 75}
                               :y {:value 450}
                               :fontSize {:value 30}
                               :opacity {:value 0.5}}}}
             {:name "chaotic-good"
              :type "text"
              :encode {:enter {:fill {:value "#dbd9d9"}
                               :text {:value "Chaotic/Good"}
                               :x {:value 350}
                               :y {:value 450}
                               :fontSize {:value 30}
                               :opacity {:value 0.5}}}}
             {:name "lawful-good"
              :type "text"
              :encode {:enter {:fill {:value "#dbd9d9"}
                               :text {:value "Lawful/Good"}
                               :x {:value 350}
                               :y {:value 170}
                               :fontSize {:value 30}
                               :opacity {:value 0.5}}}}

             {:name "marks"
              :type "symbol"
              :from {:data "zscores"}
              :encode
              {:fill inactive
               :enter (merge active {:tooltip {:signal tooltip-label}})
               :exit inactive
               :hover active
               :update
               {:x {:scale "x" :field "evil_vs_good"}
                :y {:scale "y", :field "chaotic_vs_lawful"}
                :half {:scale "x", :field "chaotic_vs_lawful"}
                :shape {:value "circle"}
                :strokeWidth {:value 2}
                :stroke blue
                :fill [{:test (str "indexof([" (str/join ", " (map #(:value %) respondents)) "], datum['id']) > -1")
                        :value dark-yellow}
                       {:value blue}]}}}]
     :scales [{:name "x"
               :type "linear"
               :round true
               :nice true
               :zero true
               :domain [-20 20]
               :range "width"}
              {:name "y"
               :type "linear"
               :round true
               :nice true
               :zero true
               :domain [-20 20]
               :range "height"}
              {:name "x2"
               :type "linear"
               :round true
               :nice true
               :zero false
               :domain [-20 20]
               :range "width"}
              {:name "y2"
               :type "linear"
               :round true
               :nice true
               :zero false
               :domain [-20 20]
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
