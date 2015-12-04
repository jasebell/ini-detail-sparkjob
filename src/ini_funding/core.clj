(ns ini-funding.core
  (:require [sparkling.core :as spark]
            [sparkling.conf :as conf]
            [sparkling.destructuring :as s-de]
            [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def filepath "/change/path/to/your/csv/file/InvestNI_local_2011_14.csv")

(def fieldnames [:business-name :address-no :address1 :address2 :address3 :town :county :country :postcode :total-assistance-offered  :date-offer-made :accepted-date :source-of-funding :jobs-to-be-created :total-investment-by-company :sector :start-up :conditions-of-offer :district-council :no-of-employees-in-company])

(defn load-ini-data [sc filename]
  (->> (spark/text-file sc filename)
       (spark/map #(->> (csv/read-csv % :separator \, :quote-char \") first))
       (spark/filter #(not= (first %) "Business Name"))
       (spark/map #(zipmap fieldnames %))
       (spark/map-to-pair (fn [rec]
                            (let [biz (:business-name rec)]
                              (spark/tuple biz rec))))
       (spark/group-by-key)))



(comment
  (def c (-> (conf/spark-conf)
             (conf/master "local[3]")
             (conf/app-name "inn-funding-sparkjob")))
  (def sc (spark/spark-context c)))
