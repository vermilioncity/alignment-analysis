(ns alignment-analysis.events.respondents
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries.select :as select-q]
   [alignment-analysis.utils.funcs :as f-utils]
   [alignment-analysis.events.select :as select-events]
   [ajax.core :as ajax]))


(rf/reg-event-fx
 ::get-respondent-options
 (fn [{db :db}]

   (let [params {:team (vals (select-q/get-select-selections db :teams))
                 :location (vals (select-q/get-select-selections db :locations))}]
     {:http-xhrio {:method :get
                   :uri "/respondents"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::select-events/handle-select-option-success :respondents]
                   :on-failure [::select-events/handle-failure]}
      :db          (select-q/set-select-busy-state db :respondents true)})))