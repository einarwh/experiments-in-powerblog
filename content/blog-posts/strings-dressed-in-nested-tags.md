:page/title Strings dressed in nested tags
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2012-06-20T20:53:00"
:page/body

# Strings dressed in nested tags

Posted: June 20, 2012

If you read the previous blog post, you might wonder if you can wrap a string in nested tags, you know, something like this:

```csharp
Func<string, string> nested = 
  s => s.Tag("td").Colspan("2")
                  .Width("100")
        .Tag("tr")
        .Tag("table").Cellpadding("10")
                     .Border("1");
```

And the answer is no. No, you can't. Well you _can_, but it's not going to give you the result you want. For instance, if you apply the transform to the string "Hello", you'll get this:

![Useless attempt at tag nesting.](/images/bad-tag-nesting.png)

Which is useless.

The reason is obviously that the **Tag** method calls following the first one will all be channeled in to the same **Tag**. Even though there's an implicit cast to string, there's nothing in the code triggering that cast. Of course, you could explicitly call **ToString** on the **Tag**, like so:

```csharp
Func<string, string> nested = 
  s => s.Tag("td").Colspan("2")
                  .Width("100")
                  .ToString()
        .Tag("tr").ToString()
        .Tag("table").Cellpadding("10")
                     .Border("1");
```

But that's admitting defeat, since it breaks the illusion we're trying to create. Plus it's ugly.

A better way of working around the problem is to compose simple one-tag transforms, like so:

```csharp
Func<string, string> cell =
  s => s.Tag("td").Colspan("2")
                  .Width("100");

Func<string, string> row =
  s => s.Tag("tr");

Func<string, string> table = 
  s => s.Tag("table").Cellpadding("10")
                     .Border("1");

Func<string, string> nested = 
  s => table(row(cell(s)));
```

Which is kind of neat and yields the desired result:

![Better tag nesting with composition.](/images/good-tag-nesting.png)

But we can attack the problem more directly. There's not a whole lot we can do to prevent our **Tag** object from capturing the subsequent method calls to **Tag**. But we are free to respond to those method calls in any ol' way we like. A trivial change to **TryInvokeMember** will do just nicely:

```csharp
public override bool TryInvokeMember(
    InvokeMemberBinder binder,
    object[] args,
    out object result)
{
    string arg = GetValue(args);
    string methodName = binder.Name;

    if (methodName == "Tag" && arg != null)
    {
        result = ToString().Tag(arg);
    }
    else
    {
        _props[methodName] = arg ?? string.Empty;
        result = this;
    }
    
    return true;
}
```

So we just single out calls for a method named **Tag** with a single string parameter. For those method calls, we're not going to do the regular fluent collection of method names and parameters thing. Instead, we'll convert the existing **Tag** to a string, and return a brand new **Tag** to wrap that string. And now we can go a-nesting tags as much as we'd like, and still get the result we wanted. Win!
