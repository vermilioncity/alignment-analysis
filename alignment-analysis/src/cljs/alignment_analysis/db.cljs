(ns alignment-analysis.db)

(def default-db
  {:teams {:selections nil
           :options nil
           :search-text nil
           :busy-state false}
   :respondents {:selections nil
                 :options nil
                 :search-text nil
                 :busy-state false}
   :locations {:selections nil
               :options nil
               :search-text nil
               :busy-state false}
   :scores {:zscores {:data nil
                      :busy-state false}
            :correlation {:data nil
                          :busy-state false}}})
