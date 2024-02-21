(ns alignment-analysis.components.correlation
  (:require
   [clojure.set :as set]
   [re-frame.core :as rf]
   [goog.string :as gstring]
   [goog.string.format]
   [alignment-analysis.subs.scores :as score-subs]
   [alignment-analysis.subs.select :as select-subs]
   [alignment-analysis.utils.funcs :as f-utils]))

(defn explode [data]
  (flatten
   (for [x data]
     (for [y (:scores x)]
       (merge (select-keys
               (set/rename-keys x {:id :id1 :name :name1})
               [:name1 :id1])
              (set/rename-keys y {:id :id2 :name :name2}))))))

(defn score-rank [scores ids]
  (->> scores
       (filter #(f-utils/in? ids (:id %)))
       (map :scores)
       (map #(sort-by :score %))
       (map #(butlast %))))

(defn score-rows [scores]
  (for [person (apply map vector scores)]
    [:tr {:key (str (map :id person))
          :style {:text-align "center"
                  :font-size "80%"}}
     (for [score person]
       [:td {:key (str (:id score) (:name score))
             :style {:margin-right 10
                     :padding "0 10px 5px 10px"}}
        [:span {:style {:float "left"
                        :margin-right 10}}
         (:name score)]
        [:span {:style {:float "right"}}
         (gstring/format "%.2f" (:score score))]])]))

(defn enum [s]
  (map vector (range) s))

(defn score-table [scores selections]
  (let [ids (map :value selections)
        names (map :label selections)
        ranks (score-rank scores ids)
        bottom (map #(take 5 %) ranks)
        top (map #(reverse (take-last 5 %)) ranks)]

    [:table {:style {:float "left"
                     :font-family "Tahoma"
                     :border-collapse "collapse"
                     :width "300px"}}
     [:thead {:style {:background-color "#f0f2f0"
                      :text-align "center"
                      :border "1px"
                      :border-radius "10px"}}
      [:tr
       (for [[index name] (enum (sort names))]
         [:th {:key (str "header" name)
               :style {:padding "9px 10px 9px 0"
                       :border-top-left-radius (if (= index 0) 5 0)
                       :border-bottom-left-radius (if (= index 0) 5 0)
                       :border-top-right-radius (if (= index (- (count names) 1)) 5 0)
                       :border-bottom-right-radius (if (= index (- (count names) 1)) 5 0)}} name])]]
     [:tbody
      [:tr
       [:td {:col-span (count names)
             :style {:text-align "center"
                     :padding "7px 0 7px 0"
                     :fontWeight "bold"}} "Most Similar"]]
      [:td {:style {:margin-right 10
                    :padding "0 10px 5px 10px"}}
       [:span {:style {:float "left"
                       :margin-right 10
                       :fontWeight "bold"
                       :fontSize "14px"}}
        "Name"]
       [:span {:style {:float "right"
                       :fontWeight "bold"
                       :fontSize "14px"}} "Correlation"]]
      (score-rows top)
      [:tr
       [:td {:col-span (count names)
             :style {:text-align "center"
                     :padding "7px 0 7px 0"
                     :fontWeight "bold"}} "Least Similar"]]
            [:td {:style {:margin-right 10
                    :padding "0 10px 5px 10px"}}
       [:span {:style {:float "left"
                       :margin-right 10
                       :fontWeight "bold"
                       :fontSize "14px"}}
        "Name"]
       [:span {:style {:float "right"
                       :fontWeight "bold"
                       :fontSize "14px"}} "Correlation"]]
      (score-rows bottom)]]))


(def tooltip-label
  (str "{'Name': datum['name1']"))

(defn correlation []
  (let [scores @(rf/subscribe [::score-subs/correlation])
        respondents @(rf/subscribe [::select-subs/select-selections :respondents])]
    (when-not (or (nil? scores) (nil? respondents))
      [score-table scores (take 4 respondents)])))
