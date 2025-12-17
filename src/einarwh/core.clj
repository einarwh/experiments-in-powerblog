(ns einarwh.core
  (:require [einarwh.ingest :as ingest]
            [einarwh.pages :as pages]))

(def config
  (-> {:site/title "einarwh"
       :powerpack/render-page #'pages/render-page
       :powerpack/create-ingest-tx #'ingest/create-tx
       :powerpack/content-file-suffixes ["md" "edn" "html" "pdf"]

       :optimus/bundles {"app.css"
                         {:public-dir "public"
                          :paths ["/css/jonas.css" "/css/themes/ascetic.mod.css"]}}

       :optimus/assets [{:public-dir "public"
                         :paths [#".*\.js" #".*\.svg" #".*\.jpg" #".*\.png" #".*\.gif" #".*\.mp4"]}]

       :imagine/config {:prefix "image-assets"
                        :resource-path "public"
                        :disk-cache? true
                        :transformations
                        {:preview-small
                         {:transformations [[:fit {:width 184 :height 184}]
                                            [:crop {:preset :square}]]
                          :retina-optimized? true
                          :retina-quality 0.4
                          :width 184}}}}))


