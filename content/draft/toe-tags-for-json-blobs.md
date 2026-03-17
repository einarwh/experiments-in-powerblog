:page/title Toe tags for JSON blobs
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Toe tags for JSON blobs

<p class="blog-post-date">February 9, 2026</p>

Many people use JSON documents for persistence, whether in a key-value store of some sort, or simply as a blob that happens to contain structured data. A benefit of this approach is that it is _schemaless_, as opposed to the rigid structures typically imposed by relational databases. This allows the documents to evolve seamlessly with the application - properties can be added, changed, or removed at will.

Example:

Say I have a JSON document representing my cat Pellegriff.

{
"name": "Pellegriff",
"age": 1,
"miceCaught": 0
}

The main drawback is that it is schemaless, and that over the lifetime of the application, properties will have been added, changed or removed. This can be problematic, because while the exact structure of the JSON documents can vary, the application typically has a single interpretation of all them all. JSON is a serialization format, after all. We typically deserialize the document to some type when we want to work with the data.

It is here we pay the potential cost of the free-wheeling evolution of the data in our application. We discover that schemaless isn't _really_ schemaless - it means that the schema is implied rather than explicit. Lacking an explicit schema, it is the type that acts as the deserialization target that substitutes as schema. Sometimes this leads to unpleasant surprises. One bad outcome is that deserialization fails outright, and we find that we may have to migrate some data after all. Another is that deserialization succeeds, but the deserialized instance violates some assumption we have about the data. This is the realm of _null_. Deserializing works, but dereferencing doesn't.

The way to fix this, of course, is to introduce a proper, explicit schema: we associate each JSON document with a JSON Schema. This puts us in the hilarious situation of having "schemaless persistance, but also btw here is the schema". There are a number of benefits to this "bring your own schema" approach though. We retain most of the flexibility of the schemaless approach. We can evolve the schema without immediately migrating data unless we want to. We are not confined to a single schema at a time. At the same time we have a proper contract for our documents, which tells us whether or not they are valid.

(Relational databases typically have the same problem as the type in our application: the schema it presents is always the latest incarnation.)

The type is really just an implementation detail.

Having a JSON Schema be the contract has a number of benefits. It decouples the contract of the JSON document from any particular application. All you need to check that the document is valid is the document, the schema and a tool of your choice. (Although there is a problem that tools may interpret the various JSON Schema draft specifications differently.)

How do we associcate our JSON documents with the appropriate schema? This brings us to the title of this blog post. If you have ever seen one of the many forensic crime TV shows, you will be familiar with the concept of a toe tag. It is used to identify a corpse in a morgue. I propose a similar approach to JSON documents. The JSON document is the dead body, the JSON Schema is the toe tag, and your persistence technology of choice is the morgue where the dead body is kept.

Rule: "no bodies in the morgue without a toe tag".

There are two

External or internal.

Pros and cons of each.

// Problems with type as contract.
Argu
It turns out that
The type is a moving target.

We have opinions about what constitutes valid data.

...
