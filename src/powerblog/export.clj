(ns powerblog.export
  (:require [powerblog.core :as blog]
            [powerpack.export :as export]))

(defn ^:export export! [& args]
  (-> blog/config
      (assoc :site/base-url "https://einarwh.no")
      export/export!))
      