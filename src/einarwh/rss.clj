(ns einarwh.rss
  (:require
   [clojure.data.xml :as xml]
   [xmlns.http%3a%2f%2fwww.w3.org%2f2005%2fAtom :as-alias atom]
   [hiccup.core :refer [html]]
   [powerpack.markdown :as md])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(defn ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "yyy-MM-dd")))

(defn feed-entry [post]
  [::atom/entry
   [::atom/title (:page/title post)]
   [::atom/updated (:blog-post/published post)]
   [::atom/author (:person/full-name (:blog-post/author post))]
   [::atom/link {:href (str "https://einarwh.no" (:path post))}]
   [::atom/id (str "urn:einarwh-no:feed:post:" (:page/uri post))]
   [::atom/content {:type "html"} 
    (html
        [:div
         [:div (md/render-html (:blog-post/description post))]
         [:p [:a {:href (:page/uri post)}
              "Read blog post"]]])]])

;; :page/title Mkay: One validation attribute to rule them all
;; :blog-post/tags [:tech :dotnet :aspnet :csharp :meta-programming :javascript]
;; :blog-post/author {:person/id :einarwh}
;; :blog-post/published #time/ldt "2013-02-15T12:07:00"
;; :page/body

(defn atom-xml [posts]
  (xml/emit-str
   (xml/sexp-as-element 
      [::atom/feed
       [::atom/id "urn:einarwh-no:feed"]
       [::atom/updated (-> posts first :date)]
       [::atom/title {:type "text"} "einarwh"]
       [::atom/link {:rel "self" :href "http://einarwh.no/feed/atom.xml"}]
       (map feed-entry posts)])))