(ns alignment-analysis.events.scores
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils.funcs :as f-utils]
   [ajax.core :as ajax]))


(rf/reg-event-fx
 ::zscores
 (fn [{db :db}]

   (let [params {:teams (vals (q/get-select-selections db :teams))
                 :locations (vals (q/get-select-selections db :locations))}]
     {:http-xhrio {:method :get
                   :uri "/zscores"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-success]
                   :on-failure [::handle-failure]}
      :db          (q/set-select-busy-state db :zscores true)})))

(rf/reg-event-db
 ::handle-success
 (fn [db [_ resp]]
   (-> db
       (q/set-zscore-data resp)
       (q/set-zscore-busy-state false))))

(rf/reg-event-fx
 ::handle-failure
 (fn [{db :db}]
   (-> db
       (q/set-zscore-data [])
       (q/set-zscore-busy-state false))))