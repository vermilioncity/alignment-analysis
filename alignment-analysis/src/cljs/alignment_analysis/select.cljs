(ns alignment-analysis.select
  (:require ["react-select" :default Select]))

(defn header
  []
  [:div
   [:> Select {:onChange (fn [e]
                           (js/console.log "onChange" e))
               :isMulti true
               :options
               [{:value 1 :label "1"}
                {:value 2 :label "2"}]}]])