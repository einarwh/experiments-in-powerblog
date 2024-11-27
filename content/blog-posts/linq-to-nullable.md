:page/title LINQ to Nullable
:blog-post/tags [:tech :programming :functional-programming :csharp :dotnet]
:blog-post/author {:person/id :einarwh}
:page/body

# LINQ to Nullable

Things got a bit out of hand today.

It all started when I added a point to the agenda for our backend team meeting saying I’d explain real quick what a functor is – or at least what my understanding of what a functor is is. And so I did.

Now the explanation itself didn’t go half bad, I don’t think. While I’m sure I would have offended mathematicians and possibly some haskellites, they weren’t there. Instead, the room was filled with C# programmers.

I think I said something like the following. Assume you have a parameterized type S<T>, where S defines some structure on top of type T. The obvious example for a C# programmer would be an IEnumerable<T>, but of course there are others, including Task<T> and Nullable<T> and indeed Whatever<T>. Now if you have such an S and a mapping function that given some S<T> and a function from T to U produces an S<U> then you almost have a functor already! In addition to that, you just need to make sure that your mapping is well-behaved in a sense. First, mapping the identity function over a structure shouldn’t change it. So if you map it => it over some structure S, that should just give you the same structure you started with. And second, assume you have a function f from T to U and a function g from U to V. If you map f over S to yield S<U> and then map g over that to yield S<V>, that should give you the same result as mapping the composed function it => g(f(it)) over S<T>.

To illustrate, I explained that Nullable<T> is a functor – or at least it should be. And it would be, if we defined the appropriate mapping function for Nullable<T>. So I wrote the following on the whiteboard:

public static class NullableExtensions {
  public static TTarget? Select<TSource, TTarget>(
      this TSource? t, 
      Func<TSource, TTarget> selector)
    where TSource : struct
    where TTarget : struct
  {
    return t.HasValue ? (TTarget?) selector(t.Value) : null;
  }
}

So this is our mapping function, even though I named it Select, which is the name used in the C# and LINQ world. A benefit of this function is that you no longer have to manually handle the mundane issues of worrying about whether or not some Nullable<T> is null. So instead of writing code like this, which resembles something from our code base:

Duration? duration = null;
if (thing.Frames.HasValue)
{
  var ms = thing.Frames.Value * 40;
  duration = Duration.FromMilliseconds(ms);
}

You can write this instead:

Duration? duration = thing.Frames.Select(fs => Duration.FromMilliseconds(fs * 40));

I think it is quite nice – at least if you can get comfortable calling an extension method on something that might be null. But from this point on, things started to go awry. But it wasn’t my fault! They started it!

See, some of the people in the meeting said they kind of liked the approach, but argued that Map would be a better name because it would avoid confusion with Select, which is associated with LINQ and IEnumerable<T>. In some sense, this was the opposite argument I used for choosing Select over Map in the first place! I thought it would make sense to call it Select precisely because that’s the name for the exact same thing for another kind of structure.

So as I left the meeting, I started wondering. I suspected that there really was nothing particular that tied LINQ and the query syntax to IEnumerable<T>, which would mean you could use it for other things. Other functors. And so I typed the following into LinqPad:

DateTime? maybeToday = DateTime.Today;
var maybeTomorrow = from dt in maybeToday select dt.AddDays(1);
maybeTomorrow.Dump();

And it worked, which I thought was pretty cool. I consulted the C# specification and found that as long as you implement methods of appropriate names and signatures, you can use the LINQ query syntax. And so I decided to let functors be functors and just see what I could do with Nullables using LINQ. So I wrote this:

public static TSource? Where<TSource>(
    this TSource? t, 
    Func<TSource, bool> predicate)
  where TSource : struct
  {
    return t.HasValue && predicate(t.Value) ? t : null;
  }

Which allowed me to write

DateTime? MaybeSaturday(DateTime? maybeDateTime)
{
  return
    from dt in maybeDateTime
    where dt.DayOfWeek == DayOfWeek.Friday
    select dt.AddDays(1);
}

Which will return null unless it’s passed a Nullable that wraps a DateTime representing a Friday. Useful.

It should have stopped there, but the C# specification is full of examples of expressions written in query syntax and what they’re translated into. For instance, I found that implementing this:

public static TTarget? SelectMany<TSource, TTarget>(
    this TSource? t, 
    Func<TSource, TTarget?> selector)
  where TSource : struct
  where TTarget : struct
{
  return t.HasValue ? selector(t.Value) : null;
}

public static TResult? SelectMany<TSource, TIntermediate, TResult>(
    this TSource? t, 
    Func<TSource, TIntermediate?> selector, 
    Func<TSource, TIntermediate, TResult> resultSelector)
  where TSource : struct
  where TIntermediate : struct
  where TResult : struct
{
  return t.SelectMany(selector)
          .Select(it => resultSelector(t.Value, it));
}

I could suddenly write this, which is actually quite nice:

TimeSpan? Diff(DateTime? maybeThis, DateTime? maybeThat)
{
  return
    from dt1 in maybeThis
    from dt2 in maybeThat
    select (dt2 - dt1);
}

It will give you a wrapped TimeSpan if you pass it two wrapped DateTimes, null otherwise. How many checks did you write? None.

And as I said, it sort of got a bit out of hand. Which is why I now have implementations of Contains, Count, Any, First, FirstOrDefault, even Aggregate, and I don’t seem to be stopping. You can see the current state of affairs here.

What I find amusing is that you can usually find a reasonable interpretation and implementation for each of these functions. Count, for instance, will only ever return 0 or 1, but that sort of makes sense. First means unwrapping the value inside the Nullable<T> without checking that there is an actual value there. Any answers true if the Nullable<T> holds a value. And so on and so forth.

Finally, as an exercise for the reader: what extension methods would you write to enable this?

static async Task Greet()
{
  var greeting =
    from v1 in Task.FromResult("hello")
    from v2 in Task.FromResult("world")
    select (v1 + " " + v2);

  Console.WriteLine(await greeting);
}
