(ns alignment-analysis.events
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.db :as db]
   [alignment-analysis.queries :as q]
   [alignment-analysis.utils :as utils]
   [ajax.core :as ajax]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::get-respondent-options
 (fn [{db :db}]

   (let [params {:teams (vals (q/get-team-selections))
                 :locations (vals (q/get-location-selections))}]
     {:http-xhrio {:method :get
                   :uri "/respondents"
                   :params (utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-respondent-list-success]
                   :on-failure [::handle-failure]}
      :db          (q/set-respondent-busy-state db true)})))

(re-frame/reg-event-fx
 ::get-team-options
 (fn [{db :db}]

   (let [params {:locations (vals (q/get-location-selections))}]
     {:http-xhrio {:method :get
                   :uri "/teams"
                   :params (utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-team-list-success]
                   :on-failure [::handle-failure]}
      :db          (q/set-team-busy-state db true)})))

(re-frame/reg-event-fx
 ::get-location-options
 (fn [{db :db}]

   (let [params {:teams (vals (q/get-team-selections))}]
     {:http-xhrio {:method :get
                   :uri "/locations"
                   :params (utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::handle-location-list-success]
                   :on-failure [::handle-failure]}
      :db          (q/set-location-busy-state db true)})))

(re-frame/reg-event-fx
 ::handle-respondent-list-success
 (fn [{db :db} [_ resp]]
   (if (not= resp (q/get-respondent-values))
   (-> db
       (q/set-respondent-values resp)
       (q/set-respondent-busy-state false))
     (q/set-respondent-busy-state false))))

(re-frame/reg-event-fx
 ::handle-team-list-success
(fn [{db :db} [_ resp]]
  (if (not= resp (q/get-team-values))
    (-> db
        (q/set-team-values resp)
        (re-frame/dispatch [::get-location-options])
        (re-frame/dispatch [::get-respondent-options])
        (q/set-team-busy-state false))
    (q/set-team-busy-state false))))

(re-frame/reg-event-fx
 ::handle-location-list-success
 (fn [{db :db} [_ resp]]
   (if (not= resp (q/get-location-values))
     (-> db
         (q/set-location-values resp)
         (re-frame/dispatch [::get-team-options])
         (re-frame/dispatch [::get-respondent-options])
         (q/set-location-busy-state false))
     (q/set-location-busy-state false))))

(re-frame/reg-event-fx
 ::handle-failure
 (fn [{db :db} [_ resp]]
   (println resp)))