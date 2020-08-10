(ns alignment-analysis.events.teams
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils :as utils]
   [alignment-analysis.events.select :as select-events]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))


(re-frame/reg-event-fx
 ::get-team-options
 (fn [{db :db}]

   (let [params {:locations (vals (q/get-select-selections :locations db))}]
     {:http-xhrio {:method :get
                   :uri "/teams"
                   :params (utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::select-events/handle-select-option-success :teams]
                   :on-failure [::select-events/handle-failure]}
      :db          (q/set-select-busy-state db :teams true)})))