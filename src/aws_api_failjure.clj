(ns aws-api-failjure
  (:require [cognitect.aws.client.api :as aws]
            [failjure.core :as f]))

(defrecord Failure [message error]
  f/HasFailed
  (failed? [self] true)
  (message [self] (:message self)))

(defn fail [message error]
  (->Failure message error))

(defn message [result]
  (or (:message result)
    (:Message result)
    (:cognitect.anomalies/message result)
    (-> result :ErrorResponse :Error :Message)
    (-> result :Response :Errors :Error :Message)))

(defn invoke [client op-map]
  (let [result (aws/invoke client op-map)]
    (if (:cognitect.anomalies/category result)
      (fail (message result) result)
      result)))

(defn throwing-invoke [client op-map]
  (let [result (aws/invoke client op-map)]
    (if-not (:cognitect.anomalies/category result)
      result
      (throw
        (ex-info
          (str "Anomaly during invoke: " (message result))
          {:client client :op-map op-map :result result})))))
