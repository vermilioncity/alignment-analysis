(ns alignment-analysis.events.respondents
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils.funcs :as f-utils]
   [ajax.core :as ajax]))


(rf/reg-event-fx
 ::get-respondent-options
 (fn [{db :db}]

   (let [params {:teams (vals (q/get-select-selections db :teams))
                 :locations (vals (q/get-select-selections db :locations))}]
     {:http-xhrio {:method :get
                   :uri "/respondents"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-select-option-success :respondents]
                   :on-failure [::handle-failure]}
      :db          (q/set-select-busy-state db :respondents true)})))