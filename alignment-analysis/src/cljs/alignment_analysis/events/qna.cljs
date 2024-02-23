(ns alignment-analysis.events.qna
  (:require
   [re-frame.core :as re-frame]
   [alignment-analysis.queries.select :as select-q]
   [alignment-analysis.events.select :as select-events]
   [alignment-analysis.utils.funcs :as f-utils]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))


(re-frame/reg-event-fx
 ::get-question-options
 (fn [{db :db}]

     {:http-xhrio {:method :get
                   :uri "/questions"
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::select-events/handle-select-option-success :questions]
                   :on-failure [::select-events/handle-select-option-failure :questions]}
      :db          (select-q/set-select-busy-state db :questions true)}))


(re-frame/reg-event-fx
 ::get-answer-options
 (fn [{db :db}]

   (let [params {:location (vals (select-q/get-select-selections db :locations))
                 :team (vals (select-q/get-select-options db :teams))
                 :question (vals (select-q/get-select-options db :questions))
                 :respondent (vals (select-q/get-select-options db :respondent))}]
     {:http-xhrio {:method :get
                   :uri "/answers"
                   :params (f-utils/filter-map-empty-values params)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::select-events/handle-select-option-success :answers]
                   :on-failure [::select-events/handle-select-option-failure :answers]}
      :db          (select-q/set-select-busy-state db :answers true)})))