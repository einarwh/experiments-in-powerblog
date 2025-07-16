(ns einarwh.rss
  (:require
   [clojure.data.xml :as xml]
   [xmlns.http%3a%2f%2fwww.w3.org%2f2005%2fAtom :as-alias atom]
   [hiccup.core :refer [html]]
   [powerpack.markdown :as md]))

(defn feed-entry [post]
  [::atom/entry
   [::atom/title (:page/title post)]
   [::atom/updated (:blog-post/published post)]
   [::atom/author (:person/full-name (:blog-post/author post))]
   [::atom/link {:href (str "https://einarwh.no" (:page/uri post))}]
   [::atom/id (str "urn:einarwh-no:feed:post:" (:page/uri post))]
   [::atom/content {:type "html"} 
    (html
        [:div
         [:div (md/render-html (:blog-post/description post))]
         [:p [:a {:href (:page/uri post)}
              "Read blog post"]]])]])

(defn atom-xml [posts]
  (xml/emit-str
   (xml/sexp-as-element 
      [::atom/feed
       [::atom/id "urn:einarwh-no:feed"]
       [::atom/updated (-> posts first :date)]
       [::atom/title {:type "text"} "einarwh"]
       [::atom/link {:rel "self" :href "http://einarwh.no/feed/atom.xml"}]
       (map feed-entry posts)])))