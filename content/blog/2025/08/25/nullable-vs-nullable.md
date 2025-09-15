:page/title Nullable vs nullable in C#
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

:blog-post/published #time/ldt "2025-08-25T19:30:00"

:blog-post/description

In C#, nullable means something very different for reference types and value types, but the syntax is the same for both. That's bound to lead to problems.

:page/body

# Nullable vs nullable in C#

<p class="blog-post-date">August 25, 2025</p>

One of the most unfortunate parts of the nullability narrative in C# is the reuse of the <span class="inline-code">T?</span> syntax to denote two completely separate concepts for value types and reference types. This leads to some odd and confusing behaviour.

As you may know, nullable value types is a much older concept than nullable reference types. Nullable value types were introduced in C# 2.0, whereas nullable reference types came in C# 8.0. And they're not the same. Nullable isn't nullable.

For value types, <span class="inline-code">T?</span> is syntactic sugar for the wrapper type <span class="inline-code">Nullable&lt;T&gt;</span>. An expression like <span class="inline-code">int? maybe = 5</span> compiles to <span class="inline-code">int? maybe = new Nullable(5)</span>, wrapping the integer value in a nullable value. This means that <span class="inline-code">T?</span> and <span class="inline-code">T</span> are distinct types.

Nullable reference types are a very different beast. For reference types, <span class="inline-code">T?</span> is a communication device. It says something about intentions. In essence it says "I expect nulls here". Its counterpart <span class="inline-code">T</span> communicates the opposite: "there shouldn't be nulls here". But once the compiler has done its job of warning that you may be violating your own intentions, there is no difference. <span class="inline-code">T?</span> and <span class="inline-code">T</span> are the same type, and that type allows nulls.

"So what?" you may ask. How is this a problem? I'm glad you asked! Let's take a look at an example to illustrate the consequences of overloading <span class="inline-code">T?</span> to mean different things for value types and reference types.

The [Enumerable](https://learn.microsoft.com/en-us/dotnet/api/system.linq.enumerable?view=net-9.0) class contains many extension methods for types that implement the <span class="inline-code">IEnumerable&lt;T&gt;</span> interface. However, it does not contain a method that corresponds to [List.choose](https://fsharp.github.io/fsharp-core-docs/reference/fsharp-collections-listmodule.html#choose) in F#! Let's try to fix that.

<span class="inline-code">List.choose</span> is interesting in that it combines the effect of <span class="inline-code">map</span> and <span class="inline-code">filter</span>, or <span class="inline-code">Select</span> and <span class="inline-code">Where</span> in C#. It maps each element of a list to an optional value of some type, and then it filters based on that mapping, keeping only the genuine values as it were. In case that's not entirely clear, my first naive attempt at writing such a method in C# should make it clearer.

```csharp
public static IEnumerable<TR> SelectNotNull<T, TR>(
    this IEnumerable<T> source,
    Func<T, TR?> fn)
{
    return source.Select(fn)
                 .Where(it => it != null)
                 .Cast<TR>();
}
```

This compiles, but unfortunately it doesn't quite work.

The intention here is to call the <span class="inline-code">fn</span> function on each element, and then to filter out any null values. You'll note that <span class="inline-code">fn</span> returns a <span class="inline-code">TR?</span> value, indicating a nullable value (for value types), or at least something that could be null (for reference types), whereas the return type of <span class="inline-code">SelectNotNull</span> is <span class="inline-code">IEnumerable&lt;TR&gt;</span>. No allowance for null there. Indeed, that's the whole point of <span class="inline-code">SelectNotNull</span>!

What is the problem? After all, it compiles. Well, what happens when we try to use it?

Let's start with reference types. We write the following code to test our new method.

```csharp
IEnumerable<string?> maybeStrs = ["foo", null, "baz", null, "quux"];
IEnumerable<string> strs = maybeStrs.SelectNotNull(it => it);
```

This works as intended! It filters out the nulls and gives us just the actual strings - as well as the type to go with it. Great stuff!

Now let's try value types. We write the equivalent code:

```csharp
IEnumerable<int?> maybeNums = [1, null, 3, null, 7];
IEnumerable<int> nums = maybeNums.SelectNotNull(it => it);
```

It doesn't compile! We get the compiler error [CS0266](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/compiler-messages/cs0266), with the explanation "cannot implicitly convert type <span class="inline-code">IEnumerable&lt;int?&gt;</span> to <span class="inline-code">IEnumerable&lt;int&gt;</span>. But how? Why? How can this be? First, it worked for strings, and second, the whole point of the method is that the return type sheds the possibility of null! So what implicit conversion could we possibly be talking about?

Well. The problem is, of course, in the interpretation of the question mark. As I mentioned, the compiler can interpret <span class="inline-code">T?</span> in two very different ways: either as sugar for <span class="inline-code">Nullable&lt;T&gt;</span> or as <span class="inline-code">T</span>'y with a chance of null. What it can't do is interpret it as _both at the same time_. It has to choose. And it sides with the reference type interpretation, apparently. Which means that for value types, there is no cast. It does filter, but that's just half the job. I told it to cast. There's a cast there.

So. How can we fix this? Is it fixable? It is! Duplication to the rescue!

```csharp
public static IEnumerable<TR> SelectNotNull<T, TR>(
    this IEnumerable<T> source,
    Func<T, TR?> fn)
    where TR : class
{
    return source.Select(fn)
                 .Where(it => it != null)
                 .Cast<TR>();
}

public static IEnumerable<TR> SelectNotNull<T, TR>(
    this IEnumerable<T> source,
    Func<T, TR?> fn)
    where TR : struct
{
    return source.Select(fn)
                 .Where(it => it != null)
                 .Cast<TR>();
}
```

So, the only thing I've done here is to create two copies of the exact same code, and add a different type constraint to each copy. Now first, am I even allowed to do this? After all, I can't declare methods with identical signatures in C#. But they're not identical! They just look that way! Indeed the whole point is that constraining <span class="inline-code">TR</span> as class and struct means that <span class="inline-code">TR?</span> is interpreted to mean <span class="inline-code">TR</span> and <span class="inline-code">Nullable&lt;TR&gt;</span> respectively, creating unique overloads. The code just looks identical due to the overloaded syntax.

<blockquote>Note: I originally wrote that this overload was possible since type constraints are part of the method signature. This is inaccurate and misses the point I was trying to make :-)</blockquote>

So the compiler allows it. Not only that, but it solves our problem! Now it works for both strings and ints!

If you think this is nuts, you're right. It is. But it is also completely understandable. Now the compiler doesn't have to choose between reference type interpretation and value type interpretation of <span class="inline-code">T?</span>. It can do both, since we've given it two methods to work with. The type constraint makes the choice of interpretation unambiguous.

But it's still nuts.
