(ns alignment-analysis.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [alignment-analysis.events.db :as db-events]
   [alignment-analysis.views :as views]
   [alignment-analysis.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::db-events/initialize-db])
  (dev-setup)
  (mount-root))

(defn ^:export main
  []
  (rf/dispatch-sync [::db-events/initialize-db])
  (dev-setup)
  (mount-root))
