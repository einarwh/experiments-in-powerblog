:page/title Toe tags for JSON blobs
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Toe tags for JSON blobs

<p class="blog-post-date">February 9, 2026</p>

Many people use JSON documents for persistence, whether in a key-value store of some sort, or simply as a blob that happens to contain structured data. 

A much-touted benefit of this approach is that it is _schemaless_, as opposed to the rigid structures typically imposed by relational databases. This allows the documents to evolve seamlessly with the application - properties can be added, changed, or removed at will.

The main drawback is that it is _schemaless_, and that over the lifetime of the application, properties will have been added, changed or removed. This can be problematic, because while the exact structure of the JSON documents can vary depending on when the document was created, the application typically has a single interpretation of all them all, based on the here and now. 

In a statically typed language, the value will typically be represented by an instance some _type_ when it is involved in anything interesting. When the interesting work is done, we serialize the value into JSON to persist it. As such, a JSON document is a value put on hold, halted at some particular point in time. 

Later, when we want to work with the value again, we deserialize it back into an instance of a type. But types, alas, can moving targets. They may be great contracts for any given point in time, but as we all know, the defining feature of time is that it passes. As it does, the type faces a conundrum. Should it stand still - which is what good contracts do - or go with the flow? Most chose the latter. The type usually only exists in its latest incarnation. But for the halted values, the serialized JSON, the situation may be different. It may very well be that our JSON document was serialized from an instance of type T, and now we try to deserialize it into an instance of a slightly different type T'. That could be a problem.

For instance, say we have a class representing something sad and enterprisey, like a customer. In its original conception, at time t<sub>0</sub>, it may look something like this.

```csharp
class Customer
{
    public string UserName { get; set; }
    public string Email { get; set; }
    public DateTime BirthDate { get; set; }
}
```

For a particular customer value, that we may colloquially refer as "John", this will yield the following JSON when serialized: 

```json
{
    "userName": "john", 
    "email": "john.doe@foomail.com", 
    "birthDate": "2002-08-13"
}
```

So far so good. I am sure you are familiar with this process. 

Now assume that time passes and that our application evolves, as applications are wont to do. At this new point in time, t<sub>1</sub>, we decide to add a new property to this class. It is a _display name_ that the customer is free to change at will! So now the class looks like this.

```csharp
class Customer
{
    public string UserName { get; set; }
    public string DisplayName { get; set; }
    public string Email { get; set; }
    public DateTime BirthDate { get; set; }
}
```

We persist new values of this type, yielding JSON documents like this.

```json
{
    "userName": "jane", 
    "displayName": "Jane D'oh",
    "email": "jane.doh@quuxpost.org", 
    "birthDate": "1998-02-18"
}
```

When we create new customer objects, we make sure that display name is always set to a value, because after all, null is the bane of existence. 

But what about John? That is, what happens when we take the first JSON document and attempt to deserialize it into an instance of the second class? We are taking a value created at t<sub>0</sub> and try to make it conform to the expectations of t<sub>1</sub>. Is that OK?

Well, chances are that the deserialization library we use will blithely accept the document, translating the fact that displayName is undefined into a null value. So the deserialization is likely to succeed, unless you do something to prevent that. But the serialization library will have made a silent translation based on an assumption, and you will have a null in your application. And we just stated that null is the bane of existence. 

Is this really a problem though? Maybe not! Or at least maybe not immediately, at time t<sub>1</sub>, which is where we are right now. Maybe things work just fine at time t<sub>1</sub>. 

But of course, time t<sub>1</sub> is not perpetuity. Time moves on, and t<sub>1</sub> will eventually be superceded by time t<sub>2</sub> and t<sub>3</sub> and many more points in time after that. Will things still work? It's hard to tell. We may not run into trouble ever, or there may simply be some delay before shit hits the fan. For instance, it may be that our UX component gladly renders null as an empty string. Great. But if at some point in time, someone decides that it would look better to uppercase the display name before rendering it, suddenly we are dereferencing null. Oops! 

How do we avoid this? It's very annoying to have things that may suddenly blow up like that. In general, we don't want time bombs in our applications if we can avoid them.

But before we start reaching for solutions, it's important to understand _why_ this happens. The problem is that the contract moved but the value stood still! 

In other words, it is here we pay the cost of the free-wheeling evolution of the data in our application. We discover that schemaless isn't _really_ schemaless - it just means that the schema is implicit rather than explicit. Lacking an explicit schema, it is the type that is forced to take on the role as schema. But it's ill equipped to do so, since the type represents a single point in time, whereas the JSON documents - the arrested values - will be spread out in time. Sometimes this can lead to unpleasant surprises. 

One bad outcome is that deserialization fails outright, and we find that we may have to migrate some data after all. Another, more insiduous, is that deserializing may work, but dereferencing may not. This was the case in our hypothetical example above.

One way to fix this problem is to introduce a proper, explicit schema for our JSON documents. We will associate each JSON document with a [JSON Schema](https://json-schema.org/). 

The original schema for customer values may look something like this:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "/hellish-enterprise/customer-schema-20260101.json",
  "title": "Customer",
  "description": "Validation schema for customers.",
  "type": "object",
  "required": ["userName", "email", "birthDate"],
  "properties": {
    "userName": {
      "type": "string",
      "minLength": 1
    },
    "email": {
      "type": "string",
      "format": "email"
    },
    "birthDate": {
      "type": "string",
      "format": "date"
    }
  }
}
```

This schema requires the presence of a user name, an email and a birth date. Neither property can be null, because that would require null as an explicit option for the type. For instance, if we wanted to allow null for emails, we should specify a type of [string, null]. There are also certain additional constraints that are rarely captured by type systems. The user name cannot be empty since the minimum length is 1. The email must be a valid email, the interpretation of which may vary depending on the tool you're using. The birth date must be string that follows the ISO-8601 standard for dates, which is the YYYY-MM-DD format. 

This, then, is a schema that could be used at time t<sub>0</sub>. The schema has an id which uses an ad-hoc embedded versioning scheme based on the date when the schema was published. Any superceding versions will follow the same pattern. 

When we want to augment the customer data, we introduce a new version of the schema. In the example above, we would add a "displayName" property to "properties", specifying a type of string. We would also add that properties to the list of required properties. If we didn't, the schema would be backwards compatible, in the sense that our original JSON document would in fact be valid according the new schema! Often, this is a good thing, but not here. The entire point is to capture the changed requirements in our new contract, and accurately describe the new JSON documents. If we allow the display name to be undefined, the JSON serializer will hand us nulls, and we don't want nulls. 

So now we have introduced a new version of the schema, but all the old JSON documents are still associated with the old schema. Is that a problem? No, it's great, because it means we're being explicit. It means we can distinguish between the expectations we have of John and Jane. If we want to consolidate those expectations, e.g. by making the John document conform to the Jane schema, we will have to amend the document through some migration process. More on this later.

Yes, I know what you're thinking. This puts us in the hilarious situation of having "schemaless persistance, but also btw here is the schema". There are a number of benefits to this "bring your own schema" approach though. We retain most of the flexibility of the schemaless approach. We can evolve the schema without immediately migrating data unless we want to. We can do it gradually, at our leisure. We are not confined to a single schema at a time. At the same time we have proper contracts for our documents, which tells us what it means for them to be valid.

Having an explicit JSON Schema be the contract for our JSON documents has a number of benefits. It decouples the contract of the JSON document from any particular application. All we need in order to check that the document is valid is the document, the schema and a tool of our choice. It's worth bearing in mind that some tools interpret the various JSON Schema draft specifications differently though. This means that different tools may actually disagree on whether or not a given document satisfies the schema, as in the case of the "email" format above. In practice this is only a problem if we use more than one validation tool, or if the tool has a different interpretation than we assume it to have.

A more pressing question is: how do we associcate our JSON documents with the appropriate schema? This brings us to the title of this blog post. If you have ever seen one of the many forensic crime TV shows, you will be familiar with the concept of a toe tag. It is used to identify a corpse in a morgue. I propose a similar approach to JSON documents. The JSON document is the dead body, the JSON Schema is the toe tag, and your persistence technology of choice is the morgue where the dead body is kept. Morbid? No, apt.

There are two main approaches that I can think of. Either the association is external to the JSON document, that is, outside the document somewhere in the great elsewhere, or it is internal, that is, embedded within the document. Each approach has some pros and cons, and each can be done in more than one way.

Keeping the association external to the document has the benefit that it is very clean, very decoupled, very flexible. We can even pull out the honorary designation _orthogonal_ to describe how the document and its schema relate to each other. A toe tag isn't the right metaphor for this approach though. It's more like maintaining a ledger, a log where you list the names of documents and the results of validating them against schemas. Hence I call this the ledger approach. 

Conceptually, the ledger is a table with entries. In our example, the table may look something like this:

```text
+-----------+--------------------------------+-------+
| Document  | Schema                         | Valid |
+-----------+--------------------------------+-------+
| john.json | /customer-schema-20260101.json | true  |
| jane.json | /customer-schema-20260319.json | true  | 
+-----------+--------------------------------+-------+
```

In practice, the ledger may simply be structured entries in a log of some sort. 

What are the pros and cons of this approach? The ledger is completely decoupled from the documents described by its entries. Any given document could in principle be validated against zero, one or more schemas. Throw the ledger away, and the document remains unchanged and unfazed.

A benefit of the ledger approach is that all the information about schemas and validation can be viewed in the same place. A drawback is that you must know about the ledger in order to find it! A developer unfamiliar with the solution may not know that there is any schema validation going on at all. They will look at the blob and see... JSON. If the ledger is just entries in a log, it means that a developer will have to sift and sort through it when they want to see the associations between documents and schemas, as well as the validation results. I think this is a bit cumbersome and reduces the utility of schema validation.

A tempting variation is to use any built-in support for metadata that the persistance technology may offer. For instance, Azure Blob Storage allows you to associate _user defined metadata_ in the form of key-value pairs with any given blob. You could for instance define a "schema" property, the value of which is the ID or URL to your schema, e.g. "/hellish-enterprise/customer-schema-20260101.json". You could also define a "valid" property with "true" and "false" as the obvious values.

A caveat with this approach in the case of Azure Blob Storage is that the key names must be valid HTTP header key names, and the values must be valid HTTP header values. You may guess the reason: blob metadata is accessed through the HTTP header mechanism. This is engineer-neat in the sense that you can read the metadata through an HTTP HEAD request. You don't actually need to download the blob itself.

I wonder what the appropriate metaphor for this is. It's not really a toe tag, and it's not quite a ledger either. It is perhaps more like putting a sticker on the outside of each mortuary drawer. You know the typically stainless steel drawers where the bodies are kept at refrigerator temperature? You scribble whatever you want on the sticker, but you don't attach it to the body itself. That means that when you when you inspect the body, you don't have the metadata containing the information about the schema and the validation. You have to go back to the drawer for that.

What should we think of the drawer-sticker approach? The metadata is typically tightly coupled to a particular storage mechanism. This means that it's not easily portable. If you ever decide to move your JSON documents, you will have to perform some sort of separate migration of the metadata. In addition to moving the bodies themselves, you'll also have to peel the stickers off the drawers so to speak. Maybe you'll be able to map what was written on the stickers onto some other storage-specific mechanism. If not, you will have to find some other solution, such as a introducing a ledger or a toe tag. As we have seen, there may also be somewhat arbitrary constraints on what you may write on the stickers in the first place. 

So much for the external approaches. The internal approach means that the association between the content of the JSON document and the schema used to validate it is put _inside the document itself_. Yes, yes, I can feel some of you recoiling in disgust, your sensitivies violated by this profane idea! Certainly this is less clean, less decoupled and less flexible than using a ledger. It's not orthogonal, it's some other kind of gonal. Indeed it _fuses_ the process of schema validation with the process of serialization! It _complects_ them, to use Rich Hickey's term. Surely this must be bad? And yet... this is my preferred approach. This is what I think of as the toe tag approach.

As always, it is a matter of trade-offs and context. I am trading some purity and orthogonality that doesn't really _do_ much for me, with some tangible benefits that _do_ do something for me. When the association between the document and the schema as well the result of validating against that schema is embedded _within the document_, they are always present. That makes the document entirely stand-alone. If I have the JSON document, I also know which schema it is supposed to satisfy, and whether or not it does. I don't even need to know where the JSON document has been stored. I can share a JSON document with you, and the information follows right along. This also means that it is trivial to move the JSON documents from one persistance store to another without losing anything.

No-one needs to know about any ledgers or other external sources of information. There are no logs to dig through, nothing to compare, combine or collate. I know immediately and without a doubt that this document with exactly this content has been validated against this schema. There is no comparing of timestamps between a ledger of validations and when the document was last modified. There is only one place to look, and that is within the document itself. I can open it in my favorite editor and there it is.

To summarize, the invariant _no bodies in my morgue without a toe tag_ is more important to me than the decoupling of bodies and tags. You could argue that it's more than a toe tag, it's like scribbling on the body itself, but that's OK with me. If that's what it takes, I'll do it.

Yes, of course there are ways to undermine this process if you really want to. You can manually edit the document to _lie_ about the validation process for instance. You can delete the entire schema property! Ha-ha! The toe tag is gone! What now?!? But this kind of active self-sabotage is not very relevant. You can always find ways to shoot yourself in the foot if you're dedicated enough. It is more relevant to consider the normal situation and the normal situation is pretty pleasant. The normal situation is that every document will be linked to a schema outlining its contract.

What exactly should we write on the toe tag though? There are many possible variations that you can choose from, depending on your context, needs and preferences. The one thing you'll definitely want to do though, is to add a $schema property to the root element of your JSON document. The value of this property should correspond to the value of the $id property of the JSON Schema.

This is the same approach taken by JSON Schema itself. There are several draft versions of the standard. I use the latest version in the example above. 

Our original JSON document would then look like this: 

```json
{
    "$schema": "/hellish-enterprise/customer-schema-20260101.json",
    "userName": "john", 
    "email": "john.doe@foomail.com", 
    "birthDate": "2002-08-13"
}
```

How would you go about doing that in practice though? When you serialize a type in a static language like C#, the JSON property typically matches the name of the C# property, but $schema isn't a valid name for a C# property. You could go through some hoops to make your JSON serializer spit out a property named $schema nevertheless. Many serializers offer some way of overriding the name of a property to whatever you want, for instance through custom attributes.

A more direct approach is to add the metadata after serialization. When working with the JSON structure itself, you can do whatever is legal in JSON. For instance, you could do something like this:

```csharp
public string Serialize(Customer customer, string schemaName)
{
    var jsonDoc = JsonSerializer.SerializeToDocument(customer, GetSerializerOptions());
    var jsonObj = JsonObject.Create(jsonDoc.RootElement);
    jsonObj.Add("$schema", schemaName);
    return jsonObj.ToString();
}
```

Here I'm using System.Text.Json for serialization and JsonSchema.Net for validation. There may be more clever and performant ways of doing this, by integrating more tightly with the serializer library. But this is easy to understand and suffices to illustrate the point. 

// TODO: Maybe include the part about deserialization here? 
// TODO: How does this solve the time gap?

Maybe you feel that including the link to the schema is enough, and it could be. But how do you know if the document conforms to the schema or not? You don't necessarily. 

But you could know it indirectly. If you always perform a schema validation whenever you serialize a value into JSON, you could in choose to _reject_ any JSON documents that failed validation. That way, the very presence of the document means that it is valid! But this is a pretty radical approach, and may not be what you want. Operations will be interrupted. You will need to log it as en error and figure out what went wrong. Data will be lost, because it won't be persisted. This may be unacceptable, in particular since this is a problem that occurs during serialization. Your application already accepted the value into its midst. If the value was unacceptable somehow, the application should have rejected it earlier and allowed for correction. 

A gentler approach is to accept the document, but to mark it as invalid. That would require you to scribble more stuff on the toe tag so to speak. That is, you would need to explicitly state the result of the validation. The simplest version would be to include a boolean flag in the document. I suggest adding a little bit more while you're at it. Since different tools may have slightly different interpretations of the JSON Schema standard, it makes sense to include the name of the tool that was used for validation. 

To add this extra bit of metadata, I have to extend my Serialize method a little bit. 

```csharp
public string Serialize(Customer customer, string schemaName, JsonSchema schema)
{
    var jsonDoc = JsonSerializer.SerializeToDocument(customer, GetSerializerOptions());
    var evaluation = schema.Evaluate(jsonDoc.RootElement, GetEvaluationOptions());
    var assembly = Assembly.GetAssembly(typeof(JsonSchema));
    var fileVersion =
        assembly
        .GetCustomAttribute<AssemblyFileVersionAttribute>()?
        .Version;
    var validatorName = $"{assembly.GetName().Name}-{fileVersion}";
    var jsonObj = JsonObject.Create(jsonDoc.RootElement);
    jsonObj.Add("$schema", schemaName);
    var toolObj = new JsonObject();
    toolObj.Add("tool", validatorName);
    toolObj.Add("timestamp", DateTime.UtcNow);
    toolObj.Add("result", evaluation.IsValid);
    jsonObj.Add("$validation", toolObj);
    return jsonObj.ToString();
}
```

This yields the following JSON document. 

```json
{
    "$schema": "/hellish-enterprise/customer-schema-20260101.json",
    "$validation": {
        "tool": "JsonSchema.Net-9.1.3",
        "timestamp": "2026-03-17T07:52:34.837129Z",
        "result": true
    },
    "userName": "john", 
    "email": "john.doe@foomail.com", 
    "birthDate": "2002-08-13"
}
```

This is nice, because it gives me all I need to reproduce the validation result if I should want to. (Omitting the fact that you can pass various evaluation options to the validation tool.)

But ok, what have we gained by this? We have attached a toe tag to the JSON document. Does this solve our original problem with the time gap between the serialized value and the type acting as deserialization target? No! It solves nothing on its own. We are just being explicit and keeping notes. When it's time to resuscitate John from his deep slumber, we still have to deal with the time gap, the fact that the world has moved ahead while John was in limbo. 

So how should we deal with it? There are two obvious solutions. 

We could of course perform a migration whenever we introduce a new version of the contract. This is a process that is out-of-band to the application. But that is a little bit cumbersome, even worrisome, because when should we run that process, and what happens while it is running? How should we synchronize it with the deployment of our application?

The alternative is to handle the data migration just-in-time for each JSON document. After all, it is when John leaves the morgue that he will have to face the new reality. That's when the leap must be made from t<sub>0</sub> to t<sub>1</sub> (or later, if John has been dormant for a long time). John will have to go through a series of transformations, typically involving being outfitted with suitable default values for new properties, or mappings of old ones to new ones. Then he will be ready to face life in the here and now again! And when it's time to go back to sleep, he will of course be serialized according to the latest version of the contract, since he will be a modern customer, good as new. 
