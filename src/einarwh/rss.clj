(ns einarwh.rss
  (:require
   [clojure.data.xml :as xml]
   [xmlns.http%3a%2f%2fwww.w3.org%2f2005%2fAtom :as-alias atom]
   [hiccup.core :refer [html]]
   [powerpack.markdown :as md])
  (:import [java.time ZoneId]
           [java.time.format DateTimeFormatter]
           [java.net URLEncoder]))

(defn escape-url [url]
  (URLEncoder/encode url "UTF-8"))

(defn time-str [ldt]
  #_(str (.toOffsetDateTime
        (.atZone ldt (ZoneId/of "Europe/Oslo"))))
  (.format (.atZone ldt (ZoneId/of "Europe/Oslo")) DateTimeFormatter/ISO_OFFSET_DATE_TIME))

(defn feed-entry [post]
  [::atom/entry
   [::atom/title (:page/title post)]
   [::atom/updated (time-str (:blog-post/published post))]
   [::atom/author [:name (:person/full-name (:blog-post/author post))]]
   [::atom/link {:href (str "https://einarwh.no" (:page/uri post))}]
   [::atom/id (str "urn:einarwh-no:feed:post:" (escape-url (:page/uri post)))]
   [::atom/content {:type "html"} 
    (html
        [:div
         [:div (md/render-html (:blog-post/description post))]
         [:p [:a {:href (:page/uri post)}
              "Read blog post"]]])]])

(defn atom-xml [posts]
  (xml/emit-str
   (xml/sexp-as-element 
      [::atom/feed {:xmlns "http://www.w3.org/2005/Atom"
            :xmlns:media "http://search.yahoo.com/mrss/"}
       [::atom/id "urn:einarwh-no:feed"]
       [::atom/updated (time-str (-> posts first :blog-post/published))]
       [::atom/title {:type "text"} "einarwh"]
       [::atom/link {:rel "self" :href "https://einarwh.no/feed/atom.xml"}]
       (map feed-entry posts)])))