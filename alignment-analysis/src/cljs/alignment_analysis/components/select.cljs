(ns alignment-analysis.components.select
  (:require ["react-select" :default Select]
            [re-frame.core :as re-frame]
            [alignment-analysis.subs :as subs]))

(defn select [props]
  [:> Select props])

(defn ac-filter [option input]
  true)

(defn autocomplete-select [title is-multi busy-state options on-input-func on-value-change & overrides]
  (let [select-id           (keyword (str title "-select"))
        current-search-text (re-frame/subscribe [::subs/select-search-text select-id])
        current-value       (re-frame/subscribe [::subs/select-options select-id])
        loading-status      (re-frame/subscribe [::subs/select-busy-state select-id])
        input-value         (if (nil? @current-search-text) "" @current-search-text)
        values              (if (empty? @current-value) nil @current-value)

        default-props {:placeholder     title
                       :label           title
                       :disabled        busy-state
                       :inputValue      input-value
                       :value           values
                       :options         options
                       :isMulti         is-multi
                       :isClearable     true
                       :isLoading       @loading-status
                       :filterOption    ac-filter
                       :on-change       #(on-value-change select-id %)
                       :on-input-change #(on-input-func select-id %)}]

    ^{:key select-id}
    (select (merge default-props (first overrides)))))
