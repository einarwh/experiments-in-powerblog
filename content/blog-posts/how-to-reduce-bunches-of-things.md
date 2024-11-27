:page/title How to reduce bunches of things
:blog-post/tags [:tech :programming :functional-programming :csharp]
:blog-post/author {:person/id :einarwh}
:page/body

# How to reduce bunches of things

Posted: October 5, 2017

So there you are, a pragmatic C# programmer out to provide business value for your end users and all that stuff. That’s great.

One of the (admittedly many) things you might want to do is reduce a bunch of things of some type into a single thing of that type. For instance, you might want to add a bunch of numbers together, or concatinate a bunch of strings and so on. How would you do that? (Assuming there’s no built-in Aggregate method available, that is.) Well, you’d write a Reduce function, right? And since we haven’t specified in advance what kinds of things we should reduce, we better make it generic. So it could work on an IEnumerable<T> of things.

Now how should the actual reduction take place? An obvious idea is to do it stepwise. It’s both a good problem solving strategy in general, and kind of necessary when dealing with an IEnumerable. For that to work, though, you need some way of taking two values and combining them to produce a single value. So Reduce needs to be a higher-order function. The caller should pass in a combine function, as well as some initial value to combine with the first element. And then the completed function might look something like this:

```csharp
public static T Reduce(this IEnumerable<T> things, 
  Func<T, T, T> combine, 
  T initialValue) 
{
  T result = initialValue;
  foreach (var t in things) 
  {
    result = combine(result, t);
  }
  return result;
}
```

And now if you have a bunch of integers, say, you can add them all up like this:

```csharp
var integers = new [] { 1, 2, 3, 4 };
var sum = integers.Reduce((a, b) => a + b, 0);
```

If, on the other hand, you have a bunch of lists, you’d do something like this instead:

```csharp
var lists = new [] {
  new List { 1 },
  new List { 2, 3 }
};
var sum = lists.Reduce((a, b) => 
  {
    var list = new List();
    list.AddRange(a);
    list.AddRange(b);
    return list;
  },
  new List());
```

And this would give you the list of elements 1, 2, 3. Great.

Now there are other things you might wonder about with respect to the combine function. For whatever reason, you might want to consider alternative implementations of Reduce. For instance, you’d might like to create batches of n things, reduce each batch, and then reduce those results for the final result. It would be nice to have that freedom of implementation. For that to be an option, though, you need your combine function to be associative.

Assume you have three values t1, t2, t3. Your combine function is associative if the following holds:

```csharp
combine(t1, combine(t2, t3)) == combine(combine(t1, t2), t3)
```

Unfortunately there is nothing in the C# type system that lets us specify and verify that a function is associative, so we need to rely on documentation and discipline for that.

Alternatively, we can turn to mathematics. It turns out that mathematicians have a name for precisely the kind of thing we’re talking about. A semigroup is a structure that consists of a set of values and an associative binary operation for combining such values. Granted, it’s a strange-sounding name, but it identifies a very precise concept that gives us something to reason about. So it’s a useful abstraction that actually gives us some guarantees that we can rely on when programming.

To represent a semigroup in our program, we can introduce an interface:

```csharp
public interface ISemigroup<T>
{
  T Combine(T a, T b);
}
```

And we can modify our Reduce function to work with semigroups, which by definition guarantees that the Combine function is associative.

```csharp
public static T Reduce<T>(this IEnumerable<T> things, 
  ISemigroup<T> semigroup, 
  T initialValue)
{
  T result = initialValue;
  foreach (var thing in things)
  {
    result = semigroup.Combine(result, thing);
  }
  return result;
}
```

And we can introduce a bunch of concrete implementations of this interface, like:

```csharp
class IntegerUnderAdditionSemigroup : ISemigroup<int>
{
  public int Combine(int a, int b)
  {
    return a + b;
  }
}

class IntegerUnderMultiplicationSemigroup : ISemigroup<int>
{
  public int Combine(int a, int b)
  {
    return a * b;
  }
}

class StringSemigroup : ISemigroup<string>
{
  public string Combine(string a, string b) 
  {
    return a + b;
  }
}

class ListSemigroup<T> : ISemigroup<List<T>> 
{
  public List Combine(List a, List b) 
  {
    var result = new List();
    result.AddRange(a);
    result.AddRange(b);
    return result;
  }
}

class FuncSemigroup<T> : ISemigroup<Func<T, T>>
{
  public Func<T, T> Combine(Func<T, T> f, Func<T, T> g) 
  {
    return it => g(f(it));
  }
}
```

So that’s quite nice. We can rely on meaningful and precise abstractions to give us some guarantees in our programs.

There is still a small problem when working with semigroups for reduction though. What should the initial value be? We really just want to reduce a bunch of values of some type, we don’t want to be bothered with some additional value.

One approach, I guess, would be to just pick the first value and then perform reduce on the rest.

```csharp
public static T Reduce(this IEnumerable<T> things, 
  ISemigroup<T> semigroup)
{
  return things.Skip(1).Reduce(semigroup, things.First();
}
```

This would work for non-empty bunches of things. But that means we’d have to check for that in some way before calling Reduce. That’s quite annoying.

What would be useful is some sort of harmless value that we could combine with any other value and just end up with the other value. So we could just use that magical value as the initial value for our Reduce.

Luckily, it turns out that there are such magical values for all the semigroups we’ve looked at. In fact, we’ve seen two such values already. For integers under addition, it’s zero. For lists, it’s the empty list. But there are others. For integers under multiplication, it’s one. For strings (under concatination), it’s the empty string. And for functions (under composition) it’s the identity function, which just returns whatever value you hand it. Now if you can provide such a value, which is called the unit value, for your semigroup, you get what the mathematicians call a monoid. It’s another intensely unfamiliar-sounding name, but again the meaning is very precise.

We can represent monoids in our programs by introducing another interface:

```csharp
public interface IMonoid<T> : ISemigroup<T> 
{
  T Unit { get; }
}
```

So there is nothing more to a monoid than exactly this: it’s a semigroup with a unit value. And the contract that the unit value operates under is this:

```csharp
Compose(Unit, t) == Compose(t, Unit) == t
```

This just says that the unit value is magical in the sense we outlined. We can combine it with any value t any way we want, and we end up with t.

Now we can write a new Reduce function that works on monoids:

```csharp
public static T Reduce(this IEnumerable<T> things, 
  IMonoid<T> monoid)
{
  return things.Reduce(monoid, monoid.Unit);
}
```

This is quite nice, because we don’t have to worry any more about whether or not the bunch of things is empty. We can proceed to implement concrete monoids that we might want to use.

```csharp
class IntegerUnderAdditionMonoid 
  : IntegerUnderAdditionSemigroup, IMonoid<int>
{
  public int Unit
  {
    get { return 0; }
  }
}

class IntegerUnderMultiplicationMonoid 
  : IntegerUnderMultiplicationSemigroup, IMonoid<int>
{
  public int Unit
  {
    get { return 1; }
  }
}

class StringMonoid : StringSemigroup, IMonoid<string>
{
  public string Unit
  {
    get { return ""; }
  }
}

class ListMonoid<T> 
  : ListSemigroup<T>, IMonoid<List<T>>
{
  public List<T> Unit
  {
    get { return new List<T>(); }
  }
}

class FuncMonoid<T> : FuncSemigroup<T>, IMonoid<Func<T, T>>
{
  public Func<T, T> Unit 
  {
    get { return it => it; }
  }
}
```

And we might write a small test program to see if they work as intended.

```csharp
public static void Main(string[] args)
{
  var integers = new[] { 1, 2, 4, 8 };
  var sum = integers.Reduce(new IntegerUnderAdditionMonoid());
  var product = integers.Reduce(new IntegerUnderMultiplicationMonoid());
  var strings = new[] { "monoids", " ", "are", " ", "nifty" };
  var str = strings.Reduce(new StringMonoid());
  var lists = new[] {
    new List { "monoids", " " },
    new List { "are" },
    new List { " ", "nice" }
  };
  var list = lists.Reduce(new ListMonoid());
  var str2 = list.Reduce(new StringMonoid());
  var integerFunctions = new Func<T, T>[] { it => it + 1, it => it % 3 };
  var intFun = integerFunctions.Reduce(new FuncMonoid());
  var stringFunctions = new Func<T, T>[] { s => s.ToUpper(), s => s.Substring(0, 5) };
  var strFun = stringFunctions.Reduce(new FuncMonoid());

  Console.WriteLine(sum);
  Console.WriteLine(product);
  Console.WriteLine(str);
  Console.WriteLine(list.Count);
  Console.WriteLine(str2);
  Console.WriteLine(intFun(1));
  Console.WriteLine(intFun(2));
  Console.WriteLine(strFun("hello world"));
  Console.WriteLine(strFun(str));
}
```

Can you work out what the program will print? If not, you might want to try to run it.

Hopefully this post gives some indication of the flexibility and power that can come with very simple abstractions. It might even give you a creeping sensation that these Haskell heads are onto something when they claim that mathematics that studies structure and composition can be useful for programmers. At the face of things, the processes of adding up integers, concatenating strings, appending lists and composing functions seem quite different, but structurally they nevertheless share some fundamental traits that can be leveraged to great effect.
