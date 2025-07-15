(ns einarwh.dev
  (:require [einarwh.core :as blog]
            [powerpack.dev :as dev]))

(defmethod dev/configure! :default []
  blog/config)  ;; 1

(comment

  (set! *print-namespace-maps* false)

  (dev/start)
  (dev/stop)
  (dev/reset)

  (def app (dev/get-app))

  (require '[datomic.api :as d])

  (def db (d/db (:datomic/conn app)))

  (->> (d/entity db [:page/uri "/blog-posts/first-post/"])
       :blog-post/author
       (into {}))
  )
