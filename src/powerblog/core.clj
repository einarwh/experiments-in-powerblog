(ns powerblog.core
  (:require [powerpack.markdown :as md]))

(defn render-page [context page]
  (md/render-html (:page/body page)))

(def config
  {:datomic/schema-file "resources/schema.edn"
   :site/title "The Powerblog"
   :powerpack/render-page #'render-page})
