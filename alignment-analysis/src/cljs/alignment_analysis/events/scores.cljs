(ns alignment-analysis.events.scores
  (:require
   [re-frame.core :as rf]
   [alignment-analysis.queries.select :as select-q]
   [alignment-analysis.queries.scores :as scores-q]
   [alignment-analysis.utils.funcs :as f-utils]
   [ajax.core :as ajax]))


(rf/reg-event-fx
 ::zscores
 (fn [{db :db}]

   (let [params {:team (map #(:value %) (select-q/get-select-selections db :teams))
                 :location (map #(:value %) (select-q/get-select-selections db :locations))}]
     
     {:http-xhrio {:method :get
                   :uri "/zscores"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-score-success :zscores]
                   :on-failure [::handle-score-failure :zscores]}
      :db          (scores-q/set-score-busy-state db :zscores true)})))




(rf/reg-event-fx
 ::correlation
 (fn [{db :db}]

   (let [params {:team (map #(:value %) (select-q/get-select-selections db :teams))
                 :location (map #(:value %) (select-q/get-select-selections db :locations))}]

     {:http-xhrio {:method :get
                   :uri "/correlation"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-score-success :correlation]
                   :on-failure [::handle-score-failure :correlation]}
      :db          (scores-q/set-score-busy-state db :correlation true)})))


(rf/reg-event-db
 ::handle-score-success
 (fn [db [_ score-type resp]]
   (-> db
       (scores-q/set-score-data score-type resp)
       (scores-q/set-score-busy-state score-type false))))

(rf/reg-event-db
 ::handle-score-failure
 (fn [db [_ score-type]]
   (-> db
       (scores-q/set-score-data score-type [])
       (scores-q/set-score-busy-state score-type false))))