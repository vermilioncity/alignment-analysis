(ns alignment-analysis.components.select
  (:require ["react-select" :default Select]
            [re-frame.core :as rf]
            [alignment-analysis.subs.select :as select-subs]
            [clojure.string :as str]))

(defn select [props]
  [:> Select props])

(defn ac-filter [option input]
  (cond
    (nil? input)
    true
    (> (count input) 2)
      (str/starts-with? (str/lower-case 
                         (:label (js->clj option :keywordize-keys true))) 
                        (str/lower-case input))
    :else true))

(def format-map {:0 {:marginLeft "5px" :fontSize "100%"}
                 :1 {:marginLeft "20px" :fontSize "95%"}
                 :2 {:marginLeft "35px" :fontSize "90%"}
                 :3 {:marginLeft "50px" :fontSize "85%"}})


(defn- update-styles [styles new-styles]
  (js/Object.assign
   #js {}
   (clj->js
   (merge
    (js->clj styles :keywordize-keys true)
    new-styles))))

(defn option-style [styles value]
  (let [level  (str (.. value -data -level))
        parent? (.. value -data -parent)]
    
    (update-styles styles
                   (merge
                    {:fontSize "85%"
                     :text-wrap true
                     :fontWeight (if parent? "bold" "regular")
                     :lineHeight "80%"
                     :fontFamily "Tahoma"}
                    (get format-map (keyword level))))))

(defn selection-style
  [styles _]
  (update-styles styles
                 {:fontWeight "regular"
                  :marginLeft 0
                  :fontSize "85%"
                  :fontFamily "Tahoma"}))

(defn placeholder-style
  [styles _]
  (update-styles styles {:fontFamily "Tahoma"}))

(defn autocomplete-select [title is-multi busy-state options on-input-func on-value-change 
                           & {:keys [elm-overrides style-overrides]}]
  (let [select-id            (keyword (str (str/lower-case title) "-select"))
        current-search-text @(rf/subscribe [::select-subs/select-search-text select-id])
        loading-status       (rf/subscribe [::select-subs/select-busy-state select-id])

        default-props {:placeholder     title
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
                       :styles {:multiValueLabel selection-style
                                :option option-style
                                :placeholder placeholder-style}}
        
        style-props {:flex-grow 1}]

    ^{:key select-id}
    (select (merge default-props (assoc (merge default-props elm-overrides)
                                        :style (merge style-props style-overrides))))))
