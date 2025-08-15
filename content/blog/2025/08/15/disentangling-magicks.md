:page/title Disentangling intertwingled magicks in ASP.NET Core Web APIs
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Disentangling intertwingled magicks in ASP.NET Core Web APIs

<p class="blog-post-date">August 15, 2025</p>

Enterprise programming is, of course, not serious. The consequences of enterprise programming might be serious, but the practice itself is not. By means of illustration, I offer this blog post.

Some context: after a hiatus of a few years, I am reacquainting myself with the realities of web programming in C# on the .NET platform. It is a place of intertwingled magicks, with years of accumulated conventions and defaults and overrides, some of which I still remember, some of which I am relearning, and some entirely new to me. Here I would like to recount my recent learnings as pertains to a particularly critical part of web programming, the intersection where an HTTP request hits the API.

Let's assume that we are building a web API that allows clients to register. To do so, they must make the appropriate POST request with a payload to our API. The client will first consult our OpenAPI documentation. This tends to be reverse engineered from the API itself using either Microsoft's built-in support in ASP.NET Core, or a tool like [Swashbuckle](https://github.com/domaindrivendev/Swashbuckle.AspNetCore) or [NSwag](https://github.com/RicoSuter/NSwag). This is the case in this example too, because otherwise, we wouldn't be doing enterprise programming. For whatever reason, we're using NSwag. Don't ask why, it's just the enterprise reality we're in.

Reverse engineered OpenAPI documentation is the first kind of magick we encounter. It is based on reflection of course, a sort of introspection on the running API. To carry it out requires intimate knowledge of the workings of the web framework, to deduce what it all means in terms of HTTP methods and routes and schemas for requests and responses. It's all very impressive, a massive engineering effort and accomplishment.

Anyway, having consulted the OpenAPI documentation, the client puts together an HTTP request and sends it off somehow, with a payload of JSON data as described. Maybe they used [curl](https://curl.se/), maybe they used [Postman](https://www.postman.com/) (the enterprise option, for when you want to log into curl), maybe they generated a client from the OpenAPI document (after all, programmers love code but hate writing it, so why not).

Having been launched through whatever means, the request makes its way over the Internet and hits our application, where it is routed through one of way too many routing mechanisms to hit the code we have written to receive such requests. But! We are enterprise programmers, so we are not about to receive a raw HTTP request message! Hahaha! No, no, seriously, how would that work? That would break the OpenAPI documentation generation too! We can't have that. Rather, we rely on the magick of _model binding_ to inspect the incoming request message, deserialize the JSON payload, and put the right bits into the right properties of our .NET types. This, then, is the second kind of magick we'll be discussing.

The model binding process, like all enterprise magick, is guided by defaults, conventions and annotations. In our case, the data for the model binding comes from the HTTP request body, which ASP.NET Core may be able to figure out on its own depending on what else is going on, or we may inform it about this through an explicit [\[FromBody\]](https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.mvc.frombodyattribute?view=aspnetcore-9.0) annotation on the parameter for our handler method. _Easy._

When the model binding process works its magick, it uses reflection to inspect the target type for the deserialization. What else could it possibly do? After all, it needs to put the right things in the right places.

Here it comes into contact with a unique trick of C# magick known as _nullable reference types_. As we all know by now, in 2009 Tony Hoare referred to the invention of the type system imposter known as _null_ way back in 1965 as his billion dollar mistake. Alas, the warning came too late for the C# designers! To compensate for partaking in that horrible mistake (and perhaps due to a tad of language envy due to quaint .NET cousin F#), C# has since invented the illusion of option types for reference types - (in contrast with the actual option type [Nullable\<T\>](https://learn.microsoft.com/en-us/dotnet/api/system.nullable-1?view=net-9.0) which is available for value types).

With nullable reference types enabled, a type declaration of <span class="inline-code adaptive">string</span> _makes the claim_ that the value will be an actual string and not null, but it does not guarantee it. It just means that the compiler will warn you if you try to assign it a value that might be null. It's just talk. Indeed, there is a designated "trust me" operator (officially "[null-forgiving](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/null-forgiving)") that can be used to tell the compiler to stop worrying and learn to love the null. It can even be used in blantant defiance to the whole notion of non-nullable values in the expression <span class="inline-code adaptive">null!</span>, which force-feeds a <span class="inline-code adaptive">null</span> into a place that ostensibly doesn't allow such things.

While non-nullable reference types aren't really real, the compiler does leave traces of its shenanigans in the generated IL code, in the form of the [\[Nullable\]](https://learn.microsoft.com/en-us/dotnet/api/system.runtime.compilerservices.nullableattribute?view=net-9.0) attribute sprinkled about. The model binding magick picks up on those clues, as does NSwag's OpenAPI generation magick! We find ourselves at the intersection of several magicks! Exciting!

It's worth noting that the OpenAPI generation process and the model binding process describe two sides of the same coin: the resulting OpenAPI document presents schemas describing valid data for our API, and the model binding process largely determines what data is legal in practice. We would obviously like them to be in agreement about this. It would be both problematic and embarrassing if they were not. Wouldn't it? (Less exciting! More troubling!)

Now what happens when the NSwag magick and the model binding magick look at a property with a type declaration of <span class="inline-code adaptive">string?</span>, indicating an old-school string that might also be null? All is well! NSwag correctly marks the property as "nullable" in the generated OpenAPI document, indicating that null is valid. It will also not require the property to be present. Similarly, model binding will work both for the case where the property has an explicit null value, and if the property itself is missing - the "undefined" scenario.

What about <span class="inline-code adaptive">string</span>? This is more problematic. In this case, NSwag does _not_ mark the property as nullable (meaning it isn't!), but it also doesn't doesn't require it to be present. That is, NSwag claims that the contract is that null is forbidden, but undefined is ok. Model binding does not agree. And how could it? It's task is to provide the property with a string value that is not null, but it can't do that if there is no property in the JSON data. It simply has no string to offer. Hence it throws a fit and says that the property is required.

Oof. This won't do. We have a discrepancy between the contract and the actual behaviour. We say one thing and do another. Luckily there are ways to fix it.

What does the enterprise programmer reach for when the magick magicks wrong? Annotations! By annotating our property with the [\[Required\]](https://learn.microsoft.com/en-us/dotnet/api/system.componentmodel.dataannotations.requiredattribute?view=net-9.0) attribute, NSwag will mark the propery as "required" in the OpenAPI schema. All good? Well actually no. If you do that, you'll find that you've changed the behavior of the model binding! It no longer accepts empty strings! Why (oh why?)? It turns out that the <span class="inline-code adaptive">\[Required\]</span> attribute has a property called <span class="inline-code adaptive">AllowEmptyStrings</span>, which is _false_ by default! When model binding finds a property declared to have a non-nullable reference type, it treats it as if it were annotated by the <span class="inline-code adaptive">\[Required(AllowEmptyStrings=true)\]</span>! But if you provide your own attribute, yours takes precedence. Hence we must explicitly do what model binding does implicitly. That is, we must remember to add <span class="inline-code adaptive">AllowEmptyStrings=true</span> if it's a non-nullable string. (If it's a non-nullable anything else, of course it doesn't matter.)

Speaking of required values. C# 11 introduced the <span class="inline-code adaptive">required</span> modifier which can be applied to fields and properties. How does it affect things? Does it affect things at all? Yes. While the <span class="inline-code adaptive">required</span> modifier is primarily a compile-time thing, it leaves a (\[RequiredMember\])[https://learn.microsoft.com/en-us/dotnet/api/system.runtime.compilerservices.requiredmemberattribute?view=net-9.0] attribute in the generated IL. (Note that this is not the same as the <span class="inline-code adaptive">[Required]</span> attribute we just met.) Model binding will pick up on this and require the property to be present. But NSwag does not. Hence you will need to use both the <span class="inline-code adaptive">[Required]</span> attribute and the <span class="inline-code adaptive">required</span> modifier!

So, to summarize:

<table style="border: 1px solid lightgrey">
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Property type</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Nullable</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Required</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Null</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Undefined</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Empty string</b></td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">string</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">string?</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top">required<br/>string</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top">required<br/>string?</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top">[Required]<br/>string</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">[Required(AES=true)]<br/>string</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom"">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top">[Required(AES=true)]<br/>string?</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top">[Required(AES=true)]<br/>required<br/>string</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top">[Required(AES=true)]<br/>required<br/>string?</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: bottom">yes</td>
  </tr>
</table>

I skipped all the variations with the default of _false_ for <span class="inline-code adaptive">AllowEmptyString</span> except one. The only difference is whether or not empty strings are allowed.

Have we solved our problems? I'm afraid not. What about lists of things? Or to keep it simple, lists of strings? We have four cases to consider: the nullable list of nullable and non-nullable strings, and the non-nullable list of nullable and non-nullable strings, respectively. Can you hold them all in your head and see them clearly? Both the list and the string can nullable or not.

Anyway. The nullable list as such is just like the case of the string. If the list is nullable, NSwag will claim it is nullable and not required, which matches model binding, which will accept both null and undefined. Similarly, <span class="inline-code adaptive">List&lt;string?&gt;</span> is no more of a problem that the string case. Model binding will see a non-nullable reference type and treat it as required, rejecting both the null and the undefined case. To make NSwag reflect that reality, we can add the <span class="inline-code adaptive">\[Required\]</span> attribute to the property (but as above, the <span class="inline-code adaptive">required</span> modifier won't do). That leaves just <span class="inline-code adaptive">List&lt;string&gt;</span>. Unfortunately, if your data is a JSON array containing null values, model binding will blithely accept it. Just ram it in there, nulls and all. It's not just lists either. The same goes for arrays. NSwag, on the other hand, handles it correctly, indicating that list and list items are nullable as appropriate.

I have no fix for this. What are the options? Well, one solution is to avoid using <span class="inline-code adaptive">List&lt;string&gt;</span> in types targeted by model binding, declaring the property to be <span class="inline-code adaptive">List&lt;string?&gt;</span> (which is the truth). But then what? What are you going to do about the potential nulls? You'll have to accept them somehow. You can't reject them, since NSwag will claim that the contract allows for nulls in the list. The best solution is to filter them out silently. The other is to accept that the type declaration <span class="inline-code adaptive">List&lt;string&gt;</span> is a lie and then reject the nulls yourself, allowing for a stricter OpenAPI contract. But then you and everyone in your team would have to remember this. But of course that's the case either way, since you'd have to know to avoid <span class="inline-code adaptive">List&lt;string&gt;</span> too.

To summarize the situation for lists of string-things, dropping everything about <span class="inline-code">required</span> and <span class="inline-code">\[Required\]</span> for (relative) simplicity:

<table style="border: 1px solid lightgrey">
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Property type</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Nullable list</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Nullable item</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Null list</b></td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top""><b>Null item</b></td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">List&lt;string&gt;</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes (!!)</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">List&lt;string&gt;?</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">List&lt;string?&gt;</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">no</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
  </tr>
  <tr>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">List&lt;string?&gt;?</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
    <td style="padding: 0px 20px 0px 0px; vertical-align: top"">yes</td>
  </tr>
</table>

Where does this leave us? We have seen that life at the intersection of magicks is neither simple nor easy, and sometimes it simply breaks down. There are not only quirks and corner cases, but failed cases as well. The fact that model binding fails to respect lists of non-nullable reference types is a problem.

We could conceivably settle for more lenient target types for model binding and do more validation ourselves, but this would undermine the assumption that the OpenAPI generation magick relies on - that the target type _is_ the schema to validate against. This is not necessarily true. We may wish to do further validation that is not easily expressed in a C# type. When faced with such limits, we tend to reach for annotations to guide and strengthen the magick. But there is of course another alternative - to abandon the magick itself. But who would do such a thing?
