:page/title Toe tags for JSON blobs
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Toe tags for JSON blobs

<p class="blog-post-date">February 9, 2026</p>

Many people use JSON documents for persistence, whether in a key-value store of some sort, or simply as a blob that happens to contain structured data. A benefit of this approach is that it is _schemaless_, as opposed to the rigid structures typically imposed by relational databases. This allows the documents to evolve seamlessly with the application - properties can be added, changed, or removed at will.

Example:

Say I have a JSON document representing something sad and enterprisey, like a customer.

``````json
{
  "username": "john",
  "email": "john.doe@foomail.com",
  "age": 27,
}```

The main drawback is that it is schemaless, and that over the lifetime of the application, properties will have been added, changed or removed. This can be problematic, because while the exact structure of the JSON documents can vary, the application typically has a single interpretation of all them all. JSON is a serialization format, after all.

In a statically typed language, the data will typically be represented by some _type_. When we persist the value, we serialize it into JSON. And when we want to work with the data again, we deserialize it back into an instance of that type. But types, alas, can moving targets. They may be great contracts for a single point in time, but across time, they become more problematic. In most applications, the type only exists in its latest incarnation, whereas the serialized JSON? That may be a different matter entirely. It may very well be that our JSON document was serialized from an instance of T0, and now we try to deserialize it into an instance of T1.

It is here we pay the potential cost of the free-wheeling evolution of the data in our application. We discover that schemaless isn't _really_ schemaless - it means that the schema is implied rather than explicit. Lacking an explicit schema, it is the type that acts as the deserialization target that substitutes as schema. Sometimes this leads to unpleasant surprises. One bad outcome is that deserialization fails outright, and we find that we may have to migrate some data after all. Another is that deserialization succeeds, but the deserialized instance violates some assumption we have about the data. This is the realm of _null_. Deserializing may work, but dereferencing may not.

For instance, our JSON document may have been the result of serializing an instance of a class Customer that looked like this:

```csharp
class Customer
{
    public string UserName { get; set; }
    public string Email { get; set; }
    public int Age { get; set; }
}```

Our application evolves, and we decide to add a new property to this class. It is a display name that the customer is free to change at will. So now the class looks like this.

```csharp
class Customer
{
    public string UserName { get; set; }
    public string DisplayName { get; set; }
    public string Email { get; set; }
    public int Age { get; set; }
}```

We persist new values of this type, yielding JSON documents like this.

`````json
{
  "username": "jane",
  "displayName": "Jane D'oh",
  "email": "jane.doh@quuxmail.com",
  "age": 25
}```

We make sure that display name is always set to a value, because after all, null is the bane of existence.

Now what happens when we deserialize the `john` document back into an instance of Customer? Well, chances are that the deserialization library we use will blithely accept the document, translating the fact that _displayName_ is undefined into a null value. Oops?

But maybe it's not a big problem! Maybe things work just fine. At least for a while! There may be some delay before we discover that everything is not well. Say our UX component gladly rendered null as an empty string. But then someone found out that it would look better to uppercase the display name before rendering it, and now suddenly we are dereferencing null. Oops!

How do we fix this? One way is to introduce a proper, explicit schema: we associate each JSON document with a JSON Schema. When we want to augment the customer data, we introduce a new version of the customer schema. But all the old values are still associated with the original schema. So now we can distinguish between the expectations we have of john and jane.

Yes, I know what you're thinking. This puts us in the hilarious situation of having "schemaless persistance, but also btw here is the schema". There are a number of benefits to this "bring your own schema" approach though. We retain most of the flexibility of the schemaless approach. We can evolve the schema without immediately migrating data unless we want to. We are not confined to a single schema at a time. At the same time we have a proper contract for our documents, which tells us whether or not they are valid.

(Relational databases typically have the same problem as the type in our application: the schema it presents is always the latest incarnation.)

The type is really just an implementation detail.

Having a JSON Schema be the contract has a number of benefits. It decouples the contract of the JSON document from any particular application. All you need to check that the document is valid is the document, the schema and a tool of your choice. (Although there is a problem that tools may interpret the various JSON Schema draft specifications differently.)

How do we associcate our JSON documents with the appropriate schema? This brings us to the title of this blog post. If you have ever seen one of the many forensic crime TV shows, you will be familiar with the concept of a toe tag. It is used to identify a corpse in a morgue. I propose a similar approach to JSON documents. The JSON document is the dead body, the JSON Schema is the toe tag, and your persistence technology of choice is the morgue where the dead body is kept. Morbid? No, apt.

Rule: "no bodies in the morgue without a toe tag".

There are two main approaches. Either the association is external to the JSON document, or it is internal, that is, embedded _within_ the document. Each approach has some pros and cons, and each can be done in more than one way.

Keeping the link between the document, the schema and the validation external to the document has the benefit that it is very clean, very decoupled, very flexible. We can even pull out the fancy and honorary designation _orthogonal_. I call this the ledger approach. Tying a tag to the document's toe isn't the right metaphor for this. It's more like maintaining a ledger, a log where you list the names of documents and the results of validating them against schemas.

Example: what a ledger may look like
Blob-ID | Schema | Validation Result

PROS AND CONS OF THIS?

The ledger is completely decoupled from the documents it describes.

Overview: all the information is in one place. "Seeing like a state?"
Discoverability: you must know about the ledger to find it! A developer unfamiliar with the solution may not know that there is any schema validation going on at all. They will look at the blob and see... JSON.

A tempting variation may be to use any built-in support for metadata that the persistance technology may have.

For instance, Azure Blob Storage allows you to associate _user defined metadata_ in the form of key-value pairs with any given blob. You could for instance define a "schema" property, the value of which is the ID or URL to your schema, e.g. "/myschemas/schema-20260319.json". You could also define a "valid" property with "true" and "false" as the obvious values.

A caveat in the case of Azure Blob Storage is that the key names must be valid HTTP header key names, and the values must be valid HTTP header values. You may guess the reason: blob metadata is accessed through the HTTP header mechanism. This is engineer-neat in the sense that you can read the metadata through an HTTP HEAD request. You don't actually need to download the blob itself.

This is not quite a ledger. It is perhaps more like putting a sticker on the outside of each mortuary drawer. You know the typically stainless steel drawers where the bodies are kept at refrigerator temperature? You scribble whatever you want on the sticker, but you don't attach it to the body itself. Which means that when you when you inspect the body, you don't have the metadata. You have to go back to the drawer for that.

PROS AND CONS OF THIS
Tightly coupled to a particular storage mechanism. Arbitrary constraints. Not portable. If you ever decide to move your JSON documents, you will have to perform some sort of migration. Map onto some other storage-related mechanism, or find some other solution (ledger or internal)

The internal approach means that the association between the content of the JSON document and the schema used to validate it is put _inside the document itself_. Yes, yes, I can feel some of you recoiling in disgust, your sensitivies violated! Certainly this is less clean, less decoupled, less flexible, not orthogonal at all. Instead it _fuses_ the process of schema validation with the process of serialization! It _complects_ them in Hickey-lingo. Surely this must be bad? And yet... this is my preferred approach. This is what I think of as the toe-tag approach.

It is a matter of trade-offs. I am trading some purity and orthogonality that doesn't really _do_ much for me, with some tangible benefits that _do_ do something for me. When the schema validation and the validation result is embedded within the document, it is always present. It is entirely stand-alone. If I have the JSON document, I also know which schema it is supposed to satisfy, and information about whether or not it does. I don't even need to know where the JSON document has been stored. This also means that it is trivial to move the JSON documents from one persistance store to another without losing anything.

I don't need any knowledge of ledgers. There are no logs to dig through, nothing to compare, combine or collate. I immediately know that this document with exactly this content has been validated. There is no comparing of timestamps between a ledger of validations and when the document was last modified. It's all there. There is only one place to look, and that is within the document itself. I can open it in my favorite editor and there it is.

The invariant _no bodies in my morgue without a toe tag_ is more important to me than the decoupling of bodies and tags.

Yes, of course there are ways to undermine this process if you really want to. You can of course manually edit the document to _lie_ about the validation process for instance. I can delete the entire schema property! Ha-ha! The toe tag is gone! What now?!? But this kind of active self-sabotage is not very relevant. You can always choose yourself in the foot if you're dedicated enough. It is more interesting and more relevant to consider the normal situation and the normal situation is pretty pleasant. Every document will be tagged with the schema outlining its contract.

Pros and cons of each.

// Problems with type as contract.
Argu
It turns out that
The type is a moving target.

We have opinions about what constitutes valid data.

...
``````
