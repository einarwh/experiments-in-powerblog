(ns einarwh.pages
  (:require [datomic-type-extensions.api :as d]
            [powerpack.markdown :as md]
            [einarwh.feed :as feed])
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

(defn get-aoc-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :blog-post/author]
              [?e :aoc/puzzle-timestamp]]
            db)
       (map #(d/entity db %))
       (sort-by :aoc/puzzle-timestamp)
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

(defn aoc-layout [{:keys [title aocjs]} & content]
  [:html
   [:head
    (when title [:title title])
    [:link {:rel "icon" :sizes "any" :type "image/svg+xml" :href "/images/favfish.svg"}]
    [:script {:data-goatcounter "https://einarwh.goatcounter.com/count" :async true :src "https://gc.zgo.at/count.js"}]
    (when aocjs [:script {:src aocjs}])
    ]
   [:body
    content]])

(def header
  [:header
   [:div {:id "blog-header"} 
    [:span {:style "display:inline-block"}
     [:a {:href "/"} "einarwh"]]
    [:span {:style "float: right;"}
     [:a {:href "/feed/atom.xml"} "feed"]]]
   [:hr]])

(def aoc-header
  [:header
   [:div {:id "blog-header"} 
    [:span {:style "display:inline-block"}
     [:a {:href "/"} "einarwh"]]
    [:span {:style "float: right;"}
     [:a {:href "/aoc/"} "aoc"]]]
   [:hr]])

(defn render-front-page [context page]
  (layout {}
          header 
          (md/render-html (:page/body page))
          [:hr]
          [:h1 "Latest blog posts"]
          [:ul {:class "blog-post-list"}
           (for [blog-post (take 3 (get-blog-posts (:app/db context)))]
             [:li {class "blog-post-list-item"}
              [:p {:class "blog-post-list-date"} (ymd (:blog-post/published blog-post))]
              [:a {:href (:page/uri blog-post)} (:page/title blog-post)]
              [:div (:blog-post/description blog-post)]])]))

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

(defn render-aoc-list [context page]
  (layout {}
          aoc-header
          [:h1 "Advent of Code"]
          [:ul {:class "blog-post-list"}
           (for [post (get-aoc-posts (:app/db context))]
             [:li {class "blog-post-list-item"}
              [:p {:class "blog-post-list-date"} (ymd (:aoc/puzzle-timestamp post))]
              [:a {:href (:page/uri post)} (:page/title post)]])]))

(defn render-feed [context _]
  (let [posts (get-blog-posts (:app/db context))] 
    {:status 200
   :headers {"Content-Type" "application/atom+xml; charset=utf-8"}
   :body (feed/atom-xml posts)}))

(defn render-article [context page]
  (layout {}
          header
          (md/render-html (:page/body page))))

(defn render-blog-post [context page]
  (render-article context page))

(defn render-aoc-post [context page]
  (aoc-layout {:aocjs (:aoc/js page)}
          aoc-header 
              [:div {:id "elm-app" :style "max-width: 100vw"}]
          [:script "
      function lambda() {
        const app = Elm.Main.init({
          node: document.getElementById(\"elm-app\"),
        });
      }
      document.addEventListener(\"DOMContentLoaded\", lambda);"] 
          ))

(defn render-draft [context page]
  (render-article context page))

(defn render-html-page [_ page]
  (:page/body page))

(defn render-page [context page]
  (case (:page/kind page)
    :page.kind/frontpage (render-front-page context page)
    :page.kind/feed (render-feed context page)
    :page.kind/blog-list (render-blog-list context page)
    :page.kind/blog-post (render-blog-post context page)
    :page.kind/aoc-list (render-aoc-list context page)
    :page.kind/aoc-post (render-aoc-post context page)
    :page.kind/html-page (render-html-page context page)
    :page.kind/draft (render-draft context page)))
