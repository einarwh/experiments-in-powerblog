(ns einarwh.ingest
  (:require [datomic.api :as d]))

(defn get-page-kind [file-name]
  (cond
    (re-find #"^blog/index\.md$" file-name)
    :page.kind/blog-list
    
    (re-find #"^feed/atom.xml$" file-name)
    :page.kind/atom-feed

    (re-find #"^blog/" file-name)
    :page.kind/blog-post

    (re-find #"^index\.md" file-name)
    :page.kind/frontpage

    (re-find #"^draft/" file-name)
    :page.kind/draft))

(defn create-tx [file-name txes]
  (let [kind (get-page-kind file-name)]
    (for [tx txes]
      (cond-> tx
        (and (:page/uri tx) kind)
        (assoc :page/kind kind)))))
