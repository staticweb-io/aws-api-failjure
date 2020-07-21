(ns aws-api-failjure
  (:require [cognitect.aws.client.api :as aws]
            [failjure.core :as f]))

(defrecord Failure [message error]
  f/HasFailed
  (failed? [self] true)
  (message [self] (:message self)))

(defn fail [message error]
  (->Failure message error))

(defn invoke [client op-map]
  (let [result (aws/invoke client op-map)
        message (or (:message result)
                  (:Message result)
                  (:cognitect.anomalies/message result))]
    (if (:cognitect.anomalies/category result)
      (fail message result)
      result)))
