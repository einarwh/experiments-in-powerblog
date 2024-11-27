:page/title Something for nothing
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:page/body

# Something for nothing

I thought I’d jot down some fairly obvious things about values in programs.

Say you have some value in your program. For instance, it could be a String, or a Thing. Then conceptually, each String belongs to the set of possible Strings, and each Thing belongs to the set of possible Things. Right?

Like so:

thing-string

Of course, you might even have something like a function from String to Thing or the other way around. That’s no different, they’re just values that belong to some set of possible values. Hard to draw, though.

In programs, this notion of sets of possible values is baked into the concept of types. So instead of saying that some value belongs to the set of possible Things, we say that it has type Thing or is of type Thing.

I wish that was all there was to it, but alas, it gets more complicated. Not only do we want to represent the presence of values in our programs, sometimes we want to represent the absence of values as well. The absence of a value isn’t necessarily an error. It could be, but it could be fine, too. There are many valid reasons why we might end up with absences flowing through our programs. So we need to represent them.

This is where null enters the picture.

In languages like C# and Java – any object-oriented language that carries DNA from Tony Hoare’s ALGOL W – the drawing of the sets above doesn’t map directly over to types. For each so-called reference type (object values that are accessed by means of references), there’s a twist. In addition to the set of possible values, each reference type also allows for the value null:

thing-string-null.png

It looks pretty similar, but the consequences for program semantics are significant.

The purpose of null is, of course, to represent the absence of a value of a given type. But now your type identifies a pretty weird set of possible values. In the case of Thing, for instance, you have all the legitimate actual Things, but also this weird thing that represents the absence of a Thing. So it’s decidedly not a Thing as such, yet it is of type Thing. It’s bizarre.

But it’s not just confusing to think about. It causes practical problems since null just doesn’t fit in. It’s a phony – a hologram that successfully fools the compiler, which is unable to distinguish between null and proper values of a given type. It’s nothing like the other values. Hence you need to think about it and worry about it all the time. Since it’s not really there, you can’t treat it like a proper value. You most decidedly can not invoke a method on it, which is sort of what you do with objects. The interpretation of null is radically different from the interpretation of all the other values of the same type. (Interestingly, it’s different in precisely the same way for all types: how null sticks out from legit Things mirrors how it sticks out from legit Strings and everything else)

But it gets worse. Once you’ve invited null into your home, there’s no way of getting rid of it! In other words, when you make null part of, say, the Thing type, you can no longer express the idea one of the actual, legit Things, not that spectral special “Thing”. There is no way you can say explicitly in your program that you know that a value is present. It’s all anecdotes and circumstance. You can obviously take a look at some value at any given time in your program and decide whether it’s a legit Thing or just an illusion, but it’s completely ephemeral. You’ve given your type system a sort of brain damage that prevents it from forming memories about absence and presence of values: you might check for null, but your program immediately forgets about it!

So much for reference types. What about so-called primitive values, like integers and booleans? Neither can be null in C# or Java. So the question is: lacking null, how do we represent the absence of a value?

Well, one hack is to think “Gee, I’m not really going to use all the possible values, so I can take one of them and pretend it’s sort of like null.” So instead of interpreting the value literally, you override the interpretation for some magic values. (Using -1 as a special value for integers is a classic, in the case where your legit values are non-negative.) The consequence is that you now have two kinds of values inside your type, operating at different semantic levels and being used for different purposes. Your set of values isn’t just a set of values anymore – it’s a conglomerate of conceptually different things.

This leaves us in a situation that’s similar to the reference type situation, except it’s ad-hoc, convention-based at best. In both cases, we have two things we’re trying to express. One thing is the presence or absence of a value. And the other thing is the set of possible values that value belongs to. These are distinct, orthogonal concerns. And usually, the word of wisdom in programming is to let distinct things be distinct, and avoid mixing things up.

So how can we do that? If we reject the temptation to put special values with special interpretation rules into the same bag as the legit values, what can we do?

If you’re a C# programmer, you’re thinking that you can use Nullable to represent the absence of a primitive value instead. This is true, and you should. Nullable is a wrapper around a primitive type, creating a new type that can have either null or a legit instance of the primitive type as value. On top of that, the the compiler works pretty to hard to blur the line between the underlying type and its Nullable counterpart, such as special syntax and implicit type conversion from T to Nullable<T>.

In a language with sum types, we can do something similar to Nullable, but in a completely generic way. What is a sum type? It is a composite type created by combining various classes of things into a single thing. Here’s an example:

type Utterance = Fee | Fie | Foe | Fum

So it’s sort of like an enumeration of variants. But it’s a rich man’s enumeration, since you can associate a variant with a value of another type. Like so:

type ThingOrNothing = Indeed of Thing | Nothing

This creates a type that distinguishes neatly between legit Things and the absence of a Thing. Either you’ll indeed have a Thing, or you’ll have nothing. And since absence stands to presence in the same way for all types, we can generalize it:

type Mayhaps<'T> = Indeed of 'T | Nothing

The nice thing is that we can now distinguish between the case where we might have a value or not and the case where we know we do have a value (provided our language doesn’t allow null, of course). In other words, we can have a function of type Mayhaps<Thing> -> Thing. This is a big deal! We can distinguish between the parts of our program that have to worry about absent values and the parts that don’t. We’ve fixed the brain damage, our program can form memories of the checks we’ve made. It’s a beautiful feat of surgery, enabled by sum types and the absence of null.

So sum types neatly solves this problem with representing absence and presence of values, on top of, and orthogonally to, the issue of defining a set of possible values for a given shape of data. What’s more, this is just one application of a general feature of the language. There is no need to complicate the language itself with special handling of absence. Instead, you’re likely to find something like Mayhaps in the standard library. In F# it’s called Option, in Elm it’s called Maybe. In languages like F# and Elm – any functional language that carries DNA from Robin Milner’s ML – you’ll find that you have both sum types and pattern matching. Pattern matching makes it very easy to work with the sum type variants.

The code you write to handle absence and presence follows certain patterns, which means you can create abstractions. For instance, say you have some function thingify of type String -> Thing, which takes a String and produces a Thing. Now suppose you’re given an Mayhaps<String> instead of a String. If you don’t have a String, you don’t get a Thing, but if indeed you do have a String, you’d like to apply thingify to get a Thing. Right?

Here’s how you might write it out, assuming F#-style syntax and pattern matching:

match dunnoCouldBe with 
| Indeed str -> Indeed (thingify str) 
| Nothing -> Nothing

This pattern is going to pop up again and again when you’re working with Mayhaps values, where the only thing that varies is the function you’d like to apply. So you can write a general function to handle this:

let quux (f : 'T -> 'U) (v : Mayhaps<'T>) : Mayhaps<'U> = 
  match v with 
  | Indeed t -> Indeed (f t) 
  | Nothing -> Nothing

Whenever you need to optionally transform an option value using some function, you just pass both of them to quux to handle it. But of course, chances are you’ll find that quux has already been written for you. In F#, it’s called Option.map. Because it map from one kind of Option value to another.

At this point, we’ve got the mechanics and practicalities of working with values that could be present or absent worked out. Now what should you do when you change your mind about where and how you handle the absence of a value? This is a design decision, even a business rule. These things change.

The short answer is that when these things change, you get a rippling change in type signatures in your program – from the place where you made a change, to the place where you handle the effect of the change. This is a good thing. This is the compiler pointing out where you need to do some work to make the change work as planned. That’s another benefit of treating the absence of values explicitly instead of mixing it up with the values themselves.

That’s all well and good, but what can you do if you’re a C# or Java programmer? What if your programming language has null and no sum types? Well, you could implement something similar to Mayhaps using the tools available to you.

Here’s a naive implementation written down very quickly, without a whole lot of thought put into it:


public abstract class Mayhaps<T>
{
  private Mayhaps() {}
  
  public abstract bool HasValue { get; }

  public abstract T Value { get; }

  public abstract Mayhaps<TR> Map<TR>(Func<T, TR> f);

  private class MayhapsValue : Mayhaps<T>
  {
    private readonly T _value;
    
    public MayhapsValue(T value)
    {
      if (value == null) { 
        throw new ArgumentNullException("Begone, null!"); 
      }
      
      _value = value;
    }
    
    public override bool HasValue
    {
      get { return true; }
    }

    public override T Value
    {
      get { return _value; }
    }

    public override Mayhaps<TR> Map<TR>(Func<T, TR> f)
    {  
      return Mayhaps<TR>.Indeed(f(_value));
    }
  }

  private class MayhapsNothing : Mayhaps<T>
  {
    public override bool HasValue
    {
      get { return false; }
    }

    public override T Value
    { get { throw new InvalidOperationException("Nothing here."); } }

    public override Mayhaps<TR> Map<TR>(Func<T, TR> f)
    {
      return Mayhaps<TR>.Nothing;
    }
  }

  private static MayhapsNothing _nothing = new MayhapsNothing();

  public static Mayhaps<T> Indeed(T value)
  {
    return new MayhapsValue(value);
  }

  public static Mayhaps<T> Nothing
  {
    get { return _nothing; }
  }
}

view raw


Mayhaps.cs

hosted with ❤ by GitHub

Now you can write code like this:

var foo = Mayhaps<string>.Nothing;
var bar = Mayhaps<string>.Indeed("lol");
var couldBeStrings = new[] { foo, bar };
var couldBeLengths = couldBeStrings.Select(it => it.Map(s => s.Length));

A better solution would be to use a library such as Succinc<T> to do the job for you.

Regardless of how you do it, however, it’s always going to be a bit clunky. What’s more is it won’t really solve our problem.

As you’ll recall, the problem with null is that you can’t escape from it. In a sense, what is missing isn’t Mayhaps. It’s the opposite. With null, everything is Mayhaps! We still don’t have a way to say that we know that the value is there. So perhaps a better solution is to implement the opposite? We could try. Here’s a very simple type that banishes null:


public sealed class Indeed<T> {

  private readonly T _value;

  public Indeed(T value) {
    if (value == null)
    {
      throw new ArgumentNullException("Begone, null!"));
    }

    _value = value;
  }

  public T Value { 
    get { return _value; }
  }
}

view raw


Indeed.cs

hosted with ❤ by GitHub

Now the question is – apart from being very clunky – does it work? And the depressing answer is: not really. It addresses the correct problem, but it fails for an obvious reason – how do you ensure that the Indeed value itself isn’t null? Put it inside another Indeed?

Implementing Indeed as a struct (that is, a value type) doesn’t work too great either. While a struct Indeed cannot be null, you can very easily obtain an uninitialized Indeed, for instance by getting the default value of the struct, which is always available. In that case, you would end up with an Indeed which wraps a null, which is unacceptable.

So I’m afraid it really is true. You can’t get rid of null once you’ve invited it in. It’s pretty annoying. I wish they hadn’t invited in null in the first place.
