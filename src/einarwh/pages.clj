(ns einarwh.pages
  (:require [datomic-type-extensions.api :as d]
            [powerpack.markdown :as md]
            [einarwh.rss :as rss])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(defn get-blog-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :blog-post/author]
              [?e :blog-post/published]]
            db)
       (map #(d/entity db %))
       (sort-by :blog-post/published)
       reverse))

(defn ->ldt [inst]
  (when inst
    (LocalDateTime/ofInstant (.toInstant inst) (java.time.ZoneId/of "Europe/Oslo"))))

(defn ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "yyy-MM-dd")))

(defn md [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "MMMM dd")))

(defn layout [{:keys [title]} & content]
  [:html
   [:head
    (when title [:title title])
    [:link {:rel "icon" :sizes "any" :type "image/svg+xml" :href "/images/favfish.svg"}]
    [:script {:data-goatcounter "https://einarwh.goatcounter.com/count" :async true :src "https://gc.zgo.at/count.js"}]]
   [:body
    content]])

(def header
  [:header [:a {:href "/"} "einarwh"] [:hr]])

(defn render-blog-list [context page]
  (layout {}
          (md/render-html (:page/body page))
          header
          [:h1 "Blog posts"]
          [:ul {:class "blog-post-list"}
           (for [blog-post (get-blog-posts (:app/db context))]
             [:li {class "blog-post-list-item"}
              [:p {:class "blog-post-list-date"} (ymd (:blog-post/published blog-post))]
              [:a {:href (:page/uri blog-post)} (:page/title blog-post)]])]))

(defn render-rss [context _]
  (let [posts (get-blog-posts (:app/db context))] 
    {:status 200
   :headers {"Content-Type" "application/atom+xml"}
   :body (rss/atom-xml posts)}))

(defn render-article [context page]
  (layout {}
          header
          (md/render-html (:page/body page))))

(defn render-blog-post [context page]
  (render-article context page))

(defn render-draft [context page]
  (render-article context page))

(defn render-page [context page]
  (case (:page/kind page)
    :page.kind/frontpage (render-blog-list context page)
    :page.kind/rss (render-rss context page)
    :page.kind/blog-list (render-blog-list context page)
    :page.kind/blog-post (render-blog-post context page)
    :page.kind/draft (render-draft context page)))
