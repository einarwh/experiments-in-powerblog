(ns powerblog.pages
  (:require [powerpack.markdown :as md]
            [datomic.api :as d]))

(defn get-blog-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :blog-post/author]]
            db)
       (map #(d/entity db %))
       (sort-by :blog-post/published)
       reverse))

;; <link rel= "stylesheet" href= "/path/to/styles/default.min.css" >
;; <script src= "/path/to/highlight.min.js" ></script>
;; <script>hljs.highlightAll ();</script>

(defn layout [{:keys [title]} & content]
  [:html
   [:head
    (when title [:title title])]
   [:body
    content]])

(def header
  [:header [:a {:href "/"} "@einarwh"]])

(defn render-frontpage [context page]
  (layout {:title "Einar W. Høst"}
          (md/render-html (:page/body page))
  ;;  [:h2 "Blog posts"]
          [:ul
           (for [blog-post (get-blog-posts (:app/db context))]
             [:li [:a {:href (:page/uri blog-post)} (:page/title blog-post)]])]))

(defn render-article [context page]
  (layout {}
          header
          [:script "hljs.highlightAll();"]
          (md/render-html (:page/body page))))

(defn render-blog-post [context page]
  (render-article context page))

(defn render-page [context page]
  (case (:page/kind page)
    :page.kind/frontpage (render-frontpage context page)
    :page.kind/blog-post (render-blog-post context page)
    :page.kind/article (render-article context page)))
