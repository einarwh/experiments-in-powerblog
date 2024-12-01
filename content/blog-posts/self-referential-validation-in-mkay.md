:page/title Self-referential validation in Mkay
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2013-02-22T21:16:00"
:page/body

# Self-referential validation in Mkay

Posted: February 22, 2013

...so I implemented **eval** for [Mkay](/blog-posts/mkay-one-validation-attribute-to-rule-them-al/). That sentence doesn’t have a first half, because I couldn’t think of any good reasons for doing so. I happen to think that’s a perfectly valid reason in and by itself, but I fear that’s a minority stance. But it doesn’t really matter. The second half of the sentence is true in any case. I implemented **eval** for Mkay.

It might be unclear to you exactly what I mean by that, though. What I mean is that Mkay now has a function (called **eval**) that you can call inside an Mkay expression. That function will take another Mkay expression as a string parameter and produce a boolean result when called. That result will then be used within the original Mkay expression. Still opaque? A concrete example should make it entirely transparent.

```csharp
public class Guardian
{
  public string Rule { get; set; }

  [Mkay("eval Rule")]
  public string Value { get; set; }
}
```

So here we have a model that uses **eval** inside an Mkay expression. How does it work in practice? Have a look:

TODO: Video

So what happens in the video is that the rule "(eval Rule)" that annotates the Value property says that you should take the content of the **Rule** property and interpret that as the rule that the **Value** property must adher to. It’s sort of like SQL injection, only for Mkay. Isn’t that nice?

The string passed to eval could of course be gleaned from several sources, not just a single property. For instance, the rule "(eval (+ “and ” A ” ” B))" creates and evaluates a validation rule by combining the string “and ” with the value of property **A**, a space, and the value of property **B**.

```csharp
public class Composed
{
  public string A { get; set; }

  public string B { get; set; }

  [Mkay("eval (+ \"and \" A \" \" B)")]
  public string Value { get; set; }
}
```

TODO: Video

It’s even more amusing if you go all self-referential and [Douglas Hofstadter](http://en.wikipedia.org/wiki/Douglas_Hofstadter)-like, and have the value and the rule be one and the same thing. To accomplish that, all you have to do is annotate your property with “(eval .)”.

```csharp
public class Self
{
  [Mkay("eval .")]
  public string Me { get; set; }

  [Mkay("eval .")]
  public string Too { get; set; }
}
```

And then we can do stuff like this:

TODO: Video.

Can you do anything useful with this? Probably not. But you’ve got to admit it’s pretty cute.
