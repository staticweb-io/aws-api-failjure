# aws-api-failjure

[![Clojars Project](https://img.shields.io/clojars/v/io.staticweb/aws-api-failjure.svg)](https://clojars.org/io.staticweb/aws-api-failjure)

A library for dealing with anomalies when using [aws-api](https://github.com/cognitect-labs/aws-api). Provides helpers to turn anomalies into Clojure ExceptionInfo objects or into [failjure](https://github.com/adambard/failjure) Failure objects.

## Install

Add to deps.edn:
```edn
io.staticweb/aws-api-failjure {:mvn/version "1.0.0"}
```

Or add to project.clj:
```edn
[io.staticweb/aws-api-failjure "1.0.0"]
```

## Usage

Create clients as you normally would using aws-api. This example requires you to have a `com.cognitect.aws/s3` dependency, you can replace `{:api :s3}` with any other API that you have the dependency for.

```clojure
(require '[aws-api-failjure :as aaf]
         '[cognitect.aws.client.api :as aws]
         '[failjure.core :as f])

(def client (aws/client {:api :s3}))
```

### failjure

Use `aws-api-failjure/invoke` in place of aws-api's `invoke` to turn unsuccessful operations into Failure objects for use with failjure:

```clojure
(f/if-let-ok? [r (aaf/invoke client ,,,op-map,,,)]
  r
  (prn "Request failed:" (f/message r)))
```

### throwing-invoke

Use `aws-api-failjure/throwing-invoke` to turn unsuccessful operations into clojure.lang.ExceptionInfo objects:

```clojure
(try
  (aaf/throwing-invoke client ,,,op-map,,,)
  (catch clojure.lang.ExceptionInfo e
    (prn "Request failed:" (:message (ex-data e)))))
```

`(ex-data e)` is a map containing the keys `:client :message :op-map :result`.

### aws/invoke

You can also use aws-api's invoke normally, check for the presence of `:cognitect.anomalies/category`, and use `aws-api-failjure/message` to retrieve a human-readable error message.

```clojure
(let [result (aws/invoke client ,,,op-map,,,)]
  (if (:cognitect.anomalies/category result)
    (prn "Request failed:" (aaf/message result))
    result))
```
