(ns powerblog.core
  (:require [powerpack.markdown :as md]))

(defn get-blog-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :blog-post/author]]
            db)
       (map #(d/entity db %))))

(defn render-frontpage [context page]
  [:html
   [:head
    [:title "The Powerblog"]]
   [:body
    (md/render-html (:page/body page))
    [:h2 "Blog posts"]
    [:ul
     (for [blog-post (get-blog-posts (:app/db context))]
       [:li [:a {:href (:page/uri blog-post)} (:page/title blog-post)]])]]])

(defn render-page [context page]
  (cond
    (= "/" (:page/uri page))
    (render-frontpage context page)

    :else
    (md/render-html (:page/body page))))

(def config
  {:datomic/schema-file "resources/schema.edn"
   :site/title "The Powerblog"
   :powerpack/render-page #'render-page})
