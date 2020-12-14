(ns alignment-analysis.events.locations
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils.funcs :as f-utils]
   [ajax.core :as ajax]))


(rf/reg-event-fx
 ::get-location-options
 (fn [{db :db}]

   (let [params {:teams (vals (q/get-select-selections db :teams))}]
     {:http-xhrio {:method :get
                   :uri "/locations"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-select-option-success :locations]
                   :on-failure [::handle-failure]}
      :db          (q/set-select-busy-state db :locations true)})))