(ns einarwh.ingest
  (:require [datomic.api :as d]))

(defn get-page-kind [file-name]
  (cond
    (re-find #"^blog/index\.md$" file-name)
    :page.kind/blog-list

    (re-find #"^aoc/index\.md$" file-name)
    :page.kind/aoc-list 
    
    (re-find #"^hypecycles/" file-name)
    :page.kind/html-page 

    (re-find #"^feed/" file-name)
    :page.kind/feed

    (re-find #"^blog/" file-name)
    :page.kind/blog-post
    
    (re-find #"^aoc/" file-name)
    :page.kind/aoc-post

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
