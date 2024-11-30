:page/title Strings dressed in tags
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:page/body

# Strings dressed in tags

Posted: June 20, 2012

In a project I’m working on, we needed a simple way of wrapping strings in tags in a custom grid in our ASP.NET MVC application. The strings should only be wrapped given certain conditions. We really wanted to avoid double if checks, you know, once for the opening tag and one for the closing tag?

We ended up using a **Func** from string to string to perform wrapping as appropriate. By default, the **Func** would just be the identify function; that is, it would return the string unchanged. When the right conditions were fulfilled, though, we’d replace it with a **Func** that would create a new string, where the original one was wrapped in the appropriate tag.

The code I came up with lets you write transforms such as this:

```csharp
Func<string, string> transform =
  s => s.Tag("a")
        .Href("https://einarwh.no")
        .Style("font-weight: bold");
```

Which is pretty elegant and compact, don’t you think? Though perhaps a bit unusual. In particular, you might be wondering about the following:

1. How come there’s a **Tag** method on the string?
2. Where do the other methods come from?
3. How come the return value is a string?

So #1 is easy, right? It has to be an extension method. As you well know, an extension method is just an illusion created by the C# compiler. But it’s a neat illusion that allows for succinct syntax. The extension method looks like this:

```csharp
public static class StringExtensions
{
    public static dynamic Tag(this string content, string name)
    {
        return new Tag(name, content);
    }
}
```

So it simply creates an instance of the **Tag** class, passing in the string to be wrapped and the name of the tag. That’s all. So that explains #2 as well, right? **Href** and **Style** must be methods defined on the **Tag** class? Well no. That would be tedious work, since we’d need methods for all possible HTML tag attributes. I’m not doing that.

If you look closely at the signature of the **Tag** method, you’ll see that it returns an instance of type **dynamic**. Now what does that mean, exactly? When **dynamic** was introduced in C# 4, prominent bloggers were all “oooh it’s statically typed as dynamic, my mind is blown, yada yada yada”, you know, posing as if they didn’t have giant brains grokking this stuff pretty easily? It’s not that hard. As usual, the compiler is sugaring the truth for us. Our trusty ol’ friend [ILSpy](https://github.com/icsharpcode/ILSpy) is kind enough to let us figure out what **dynamic** really means, by revealing all the gunk the compiler spews out in response to it. You’ll find that it introduces a **CallSite** at the point in code when you’re interacting with the **dynamic** type, as well as a **CallSiteBinder** to handle the run-time binding of operations on the **CallSite**.

We don’t have to deal with all of that, though. Long story short, **Tag** inherits from **DynamicObject**, a built-in building block for creating types with potensially interesting dynamic behaviour. **DynamicObject** exposes several virtual methods that are called during run-time method dispatch. So basically when the run-time is trying to figure out which method to invoke _and_ to invoke it, you’ve got these nice hooks where you can insert your own stuff. **Tag**, for instance, implements its own version of **TryInvokeMember**, which is invoked by the run-time to, uh, you know, try to invoke a member? It takes the following arguments:

* An instance of **InvokeMemberBinder** (a subtype of **CallSiteBinder**) which provides run-time binding information.
* An array of objects containing any arguments passed to the method.
* An _out_ parameter which should be assigned the return value for the method.

Here is **Tag**‘s implementation of **TryInvokeMember**:

```csharp
public override bool TryInvokeMember(
    InvokeMemberBinder binder,
    object[] args,
    out object result)
{            
    _props[binder.Name] = GetValue(args) ?? string.Empty;
    result = this;
    return true;
}

private string GetValue(object[] args)
{
    if (args.Length > 0)
    {
        var arg = args[0] as string;
        if (arg != null)
        {
            return arg;
        }
    }
    return null;
}
```

What does it do? Well, not a whole lot, really. Essentially it just hamsters values from the method call (the method name and its first argument) in a dictionary. So for instance, when trying to call the **Href** method in the example above, it’s going to store the value "https://einarwh.no"; for the key “href”. Simple enough. And what about the return value from the **Href** method call? We’ll just return the **Tag** instance itself. That way, we get a nice fluent composition of method calls, all of which end up in the **Tag**‘s internal dictionary. Finally we return true from **TryInvokeMember** to indicate that the method call succeeded.

Of course, you’re not going to get any IntelliSense to help you get the attributes for your HTML tags right. If you misspell **Href**, that’s your problem. There’s no checking of anything, this is all just a trick for getting a compact syntax.

Finally, **Tag** defines an implicit cast to **string**, which explains #3. The implicit cast just invokes the **ToString** method on the **Tag** instance.

```csharp
public static implicit operator string(Tag tag)
{
    return tag.ToString();
}

public override string ToString()
{
    var sb = new StringBuilder();

    sb.Append("<").Append(_name);

    foreach (var p in _props)
    {
        sb.Append(" ")
          .Append(p.Key.ToLower())
          .Append("=\"")
          .Append(p.Value)
          .Append("\"");
    }

    sb.Append(">")
      .Append(_content)
      .Append("</")
      .Append(_name)
      .Append(">");

    return sb.ToString();
}
```

The **ToString** method is responsible for actually wrapping the original string in opening and closing tags, as well as injecting any hamstered dictionary entries into the opening tag as attributes.

And that’s it, really. That’s all there is. Here’s the complete code:

```csharp
namespace DynamicTag
{
    class Program
    {
        static void Main()
        {
            string s = "blog"
               .Tag("a")
               .Href("https://einarwh.no")
               .Style("font-weight: bold");
            Console.WriteLine(s);
            Console.ReadLine();
        }
    }

    public class Tag : DynamicObject
    {
        private readonly string _name;
        private readonly string _content;
        private readonly IDictionary<string, string> _props = 
            new Dictionary<string, string>();

        public Tag(string name, string content)
        {
            _name = name;
            _content = content;
        }

        public override bool TryInvokeMember(
            InvokeMemberBinder binder,
            object[] args,
            out object result)
        {            
            _props[binder.Name] = GetValue(args) ?? string.Empty;
            result = this;
            return true;
        }

        private string GetValue(object[] args)
        {
            if (args.Length > 0)
            {
                var arg = args[0] as string;
                if (arg != null)
                {
                    return arg;
                }
            }
            return null;
        }

        public override string ToString()
        {
            var sb = new StringBuilder();
 
            sb.Append("<").Append(_name);

            foreach (var p in _props)
            {
                sb.Append(" ")
                  .Append(p.Key.ToLower())
                  .Append("=\"")
                  .Append(p.Value)
                  .Append("\"");
            }

            sb.Append(">")
              .Append(_content)
              .Append("</")
              .Append(_name)
              .Append(">");

            return sb.ToString();
        }

        public static implicit operator string(Tag tag)
        {
            return tag.ToString();
        }
    }

    public static class StringExtensions
    {
        public static dynamic Tag(this string content, string name)
        {
            return new Tag(name, content);
        }
    }
}
```
