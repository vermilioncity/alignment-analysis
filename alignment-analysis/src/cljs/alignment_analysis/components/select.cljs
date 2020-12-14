(ns alignment-analysis.components.select
  (:require ["react-select" :default Select]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [alignment-analysis.subs :as subs]
            [clojure.string :as str]))

(defn select [props]
  [:> Select props])

(defn ac-filter [option input]
  true)

(def format-map {:0 {:marginLeft "5px" :fontSize "18px"}
                 :1 {:marginLeft "20px" :fontSize "16px"}
                 :2 {:marginLeft "35px"}
                 :3 {:marginLeft "50px"}})

(defn options-style [provided state]
  (let [original-style (js->clj provided :keywordize-keys true)
        level          (.. state -data -level)
        parent?        (.. state -data -parent)
        hidden?        (.. state -data -hidden)]

    (clj->js (merge original-style
                    (get format-map level)
                    {:fontWeight (if parent? "bold" "regular")
                     :display (if hidden? "none" "block")}))))

(defn OptionLabel [props]
  (let [level  (str (.. props -level))
        label (.. props -label)
        hidden? (.. props -hidden)
        parent? (.. props -parent)]
    
   (r/as-element [:div {:style (merge (get format-map (keyword level))
                                      {:display hidden?
                                       :fontWeight (if parent? "bold" "regular")})} label])))

(defn autocomplete-select [title is-multi busy-state options on-input-func on-value-change & overrides]
  (let [select-id            (keyword (str (str/lower-case title) "-select"))
        current-search-text @(rf/subscribe [::subs/select-search-text select-id])
        loading-status       (rf/subscribe [::subs/select-busy-state select-id])

        default-props {:formatOptionLabel OptionLabel
                       :placeholder     title
                       :label           title
                       :disabled        busy-state
                       :inputValue      current-search-text
                       :options         options
                       :isMulti         is-multi
                       :isClearable     true
                       :isLoading       @loading-status
                       :filterOption    ac-filter
                       :on-change       #(on-value-change select-id %)
                       :on-input-change #(on-input-func select-id %)
                       :styles {:flex-grow 1
                                :option options-style
                                }}]

    ^{:key select-id}
    (select (merge default-props (first overrides)))))
