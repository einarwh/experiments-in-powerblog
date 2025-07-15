(ns einarwh.rss
  (:require
    [clojure.data.xml :as xml]
    [xmlns.http%3a%2f%2fwww.w3.org%2f2005%2fAtom :as-alias atom]))

(defn feed-entry [post]
  [::atom/entry
   [::atom/title (:name post)]
   [::atom/updated (:date post)]
   [::atom/author [:name "Einar W. Høst"]]
   [::atom/link {:href (str "https://einarwh.no" (:path post))}]
   [::atom/id (str "urn:einarwh-no:feed:post:" (:name post))]
   [::atom/content {:type "html"} (:content post)]])

(defn atom-xml [posts]
  (xml/emit-str
   (xml/sexp-as-element 
      [::atom/feed
       [::atom/id "urn:einarwh-no:feed"]
       [::atom/updated (-> posts first :date)]
       [::atom/title {:type "text"} "einarwh"]
       [::atom/link {:rel "self" :href "http://einarwh.no/feed/atom.xml"}]
       (map feed-entry posts)])))