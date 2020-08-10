(ns alignment-analysis.events.respondents
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils :as utils]
   [ajax.core :as ajax]))


(re-frame/reg-event-fx
 ::get-respondent-options
 (fn [{db :db}]

   (let [params {:teams (vals (q/get-select-selections :teams db))
                 :locations (vals (q/get-select-selections :locations db))}]
     {:http-xhrio {:method :get
                   :uri "/respondents"
                   :params (utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-select-option-success :respondents]
                   :on-failure [::handle-failure]}
      :db          (q/set-select-busy-state db :respondents true)})))