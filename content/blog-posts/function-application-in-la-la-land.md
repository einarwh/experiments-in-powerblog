:page/title Function application in la-la land
:blog-post/tags [:tech :functional-programming]
:blog-post/author {:person/id :einarwh}
:page/body

# Function application in la-la land

Here’s a familiar scenario for a programmer: You have some useful function that you would like to apply to some values. It could be concat that concatinates two strings, or add that adds two integers, or cons which prepends an element to a list, or truncate which cuts off a string at the specified length, or indeed any old function f you come up with which takes a bunch of arguments and produces some result.

Simple, right? But there’s a twist! The values you’d like to apply your function to are all trapped in la-la land! And once you have values in la-la land, it’s not obvious how you’d go about getting them out of there. It really depends on the kind of la-la land your values are in. It’s sort of like being trapped in the afterlife. You might be able to return to the land of the living, but it’s not trivial. Certainly not something you’d want your pure, innocent function to have to deal with!

You might wonder how the values ended up in la-la land in the first place. In many cases, they were born there. They are la-la land natives – it’s the only existence they’ve ever known. It sounds weird, but it’s surprisingly common. Indeed, many programs contain not one but several distinct la-la lands, each with their own peculiar laws and customs! Some familiar la-la lands in the .NET world include `Task`, `Nullable` and `List`.

Since la-la lands are so pervasive in our programs, clearly we need to be able to apply functions to the values that dwell there. Previously we’ve seen that if your la-la land is a Functor, there is a `Map` function that lets us do that. But there is a problem: `Map` cannot work with any of the functions I mentioned above. The reason is that they all take more than one argument. `Map` can transform a single value of type `T1` inside la-la land to a single value of type `T2` inside la-la land. What Map does is teleport the `T1` value out of la-la land, apply your function to obtain a `T2` value, and teleport that back into la-la land. You can of course map multiple times, but you’ll still involving just one la-la land value at a time. So that’s not going to work.

What alternatives do we have? Well, one idea that springs to mind is partial application. If we had a curried function, we could apply it to the la-la land values one by one, producing intermediate functions until we have the final result. For instance, say we have a curried version of add which looks like this:

```csharp
Func<int, Func<int, int>> add = a => b => a + b;
```

Now we have a single-argument function that returns a single-argument function that returns a value. So we can use it like this:

```csharp
Func<int, Func<int, int>> add = a => b => a + b;
Func<int, int> incr = add(1);
int four = incr(3);
```

Unfortunately, this still won’t work with `Map`. What would happen if we passed the curried add to `Map`? We would get an `incr` function stuck inside of la-la land! And then we’d be stuck too. But what if we replaced `Map` with something that could work with functions living in la-la land? Something like this:

```csharp
Lala<TR> Apply<T, TR>(Lala<Func<T, TR>> f, Lala<T> v);
```

What `Apply` needs to do is teleport both the function and the value out of la-la land, apply the function to the value, and teleport the result back into la-la land.

How would this work with our curried add function? Well, first we’d need to teleport the function itself into la-la land. For this, we need a function, which we’ll call `Pure`. It looks like this:

```csharp
Lala<T> Pure<T>(T val);
```

In other words, `Pure` is a one-way portal to la-la land.

Let’s see how this would work for our curried `add` function:

```csharp
static Lala<int> AddLalaIntegers(Lala<int> a, Lala<int> b) 
{
    Func<int, Func<int, int>> add = a => b => a + b;
    Lala<Func<int, Func<int, int>>> lalaAdd = Lala.Pure(add);
    Lala<Func<int, int>> lalaPartial = Lala.Apply(lalaAdd, a);
    Lala<int> lalaResult = Lala.Apply(lalaPartial, b);
    return lalaResult;    
}
```

Success! Who would have thought?

Well, someone, obviously. It turns out that la-la lands that support `Pure` and `Apply` are known as `Applicative`.

But there are still questions worth asking, such as: How do we implement these functions? Like `Map`, `Pure` and `Apply` must obey the laws of the particular la-la land they work with. We’re going to look at two examples in C#.

First, consider the la-la land known as `Task<T>`.

```csharp
public static class TaskApplicative 
{
    public static Task<T> Pure(T val) 
    {
        return Task.FromResult(val);
    }

    public static async Task<TR> Apply<T, TR>(
        this Task<Func<T, TR> funTask, 
        Task<T> valTask)
    {
        var fun = await funTask;
        var val = await valTask;
        return fun(val);
    }
}
```

Awaiting the tasks bring them out of `Task`-land, and the return value is automatically transported back by the async machinery.

Second, imagine a type called `Mayhaps<T>`. `Mayhaps` is like `Nullable`, but it works on any type `T`. Why is this important? Because delegates are reference types, which means they can’t be put inside a `Nullable`. In other words, functions are not allowed into the la-la land that is `Nullable`. So `Mayhaps` it is.

`Mayhaps` has two possible values, `Indeed` and `Sorry`. `Indeed` holds a value, `Sorry` does not. That’s really all you need to know about `Mayhaps`. (For implementation details, look [here](https://gist.github.com/einarwh/0df548e1496d561242ab659d2b3841af).)

Here are `Pure` and `Apply` for `Mayhaps`:

```csharp
public static class MayhapsApplicative
{
    public static Mayhaps<TR> Pure<TR>(TR v)
    {
        return Mayhaps<TR>.Indeed(v);
    }

    public static Mayhaps<TR> Apply<T, TR>(
        this Mayhaps<Func<T, TR>> mayhapsFunction,
        Mayhaps<T> mayhapsValue)
    {
        if (mayhapsFunction.HasValue && mayhapsValue.HasValue)
        {
            var fun = mayhapsFunction.Value;
            var val = mayhapsValue.Value;
            return Mayhaps<TR>.Indeed(fun(val));
        }
        else
        {
            return Mayhaps<TR>.Sorry;
        }
    }
}
```

The semantics of `Mayhaps` is to propagate `Sorry` – you can only get a new `Indeed` if you have both a function and a value.

And of course the nice thing now is that we can separate our logic from the idiosyncracies of each la-la land! Which is pretty great.

But I’ll admit that we’re currently in a situation where calling a function is a little bit involved and awkward. It’s involved because there’s quite a bit of boilerplate, and it’s awkward because working with curried functions and partial application isn’t necessarily the bread and butter of C# programming. So let’s write some helper functions to alleviate some of that pain.

We can start by writing functions to curry `Func`s, which should reduce the awkward. There are quite a few of them; here’s an example that curries a `Func` with four input parameters:

```csharp
public static Func<T1, Func<T2, Func<T3, Func<T4, TR>>>> Curry<T1, T2, T3, T4, TR>(
    this Func<T1, T2, T3, T4, TR> f)
{
    return a => b => c => d => f(a, b, c, d);
}
```

We can use it like this:

```csharp
Func<int, int, int, int, int> sirplusalot = 
    (a, b, c, d) => a + b + c + d; 
Func<int, Func<int, Func<int, Func<int, int>>>> = 
    sirplusalot.Curry();
```

A little less awkward. What about involved? We’ll define some helper functions to reduce the boilerplate. The idea is to use a function `Lift` to handle pretty much everything for us. Here is one that can be used with `sirplusalot`:

```csharp
public static Lala<TR> Lift<T1, T2, T3, T4, TR>(
    this Func<T1, T2, T3, T4, TR> f,
    Lala<T1> v1,
    Lala<T2> v2,
    Lala<T3> v3,
    Lala<T4> v4)
{
    return Pure(f.Curry()).Apply(v1).Apply(v2).Apply(v3).Apply(v4);
}
```

Note that all `Lift` functions will have the same structure, regardless of which la-la land they operate in. Only the implementations of `Pure` and `Apply` will vary.

And now we can implement functions that look like this:

```csharp
private async static Task<int> Plus(
    Task<int> ta, 
    Task<int> tb, 
    Task<int> tc, 
    Task<int> td) 
{ 
    Func<int, int, int, int, int> sirplusalot = 
        (a, b, c, d) => a + b + c + d;
    return await sirplusalot.Lift(ta, tb, tc, td);
}

private static Mayhaps<int> Plus(
    Mayhaps<int> ma, 
    Mayhaps<int> mb, 
    Mayhaps<int> mc, 
    Mayhaps<int> md)
{
    Func<int, int, int, int, int> sirplusalot = 
        (a, b, c, d) => a + b + c + d;
    return sirplusalot.Lift(ma, mb, mc, md);
}
```

Which is quite nice? Yes?
