:page/title LINQ to Exceptions
:blog-post/tags [:tech :programming :linq :csharp]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2014-06-28T12:00:00"
:page/body

# LINQ to Exceptions

Posted: June 28, 2014

Lately I've been thinking about the mundane task of _exception handling_. Remarkably unsexy, and yet completely mandatory. There's no escape from handling the exceptions that may (and therefore will) occur in your application, unless you want it to crash or misbehave regularly.

Proper exception handling can be a chore anywhere in your application, but in particular at integration points. Whenever your application needs to communicate with some external service, any number of things can go wrong, at various levels in the stack – from application level errors to network problems. We need to be able to cope with all these different kinds of errors, and hence we surround each service call with a veritable flood of exception handlers. That sort of works, but there are problems – first, the actual service is drowning amidst all the exception handling, and second, we get a lot of code duplication, since we tend to handle many kinds of exceptions in the same way for all service calls.

I've been discussing how to best solve this problem in a completely general and flexible way in several impromptu popsicle- and coffee-driven sessions with some of my compatriots at work (in particular Johan and Jonas – thanks guys!). Some of the code examples in this blog post, especially those who exhibit some trace of intelligence, can legitimately be considered rip-offs, evolutions, misunderstandings or various other degenerations of similar code snippets they have come up with.

But I'm getting ahead of myself. Let's return to the root of the problem: how exception handling code has a tendency to overshadow the so-called business logic in our applications and to cause severe cases of duplication.

The problem is minor if there is a single exception we're catching:

```csharp
try {
  // Interesting code.
}
catch (SomeException ex) {
  // Dull exception handling code.
}
```

However, things regress quickly with additional types of exceptions:

```csharp
try {
  // Interesting code.
}
catch (SomeException ex) {
  // Dull code.
}
catch (SomeOtherException ex) {
  // Dull code.
}
catch (YetAnotherException ex) {
  // Dull code.
}
catch (Exception ex) {
  // Dull code.
}
```

This is a bit messy, and the signal-to-noise-ratio is low. It gets much worse when you have a second piece of interesting code that you need to guard with the same exception handling. Suddenly you have rampant code repetition on your hands.

A solution would be to use a closure to inject the interesting code into a generic exception handling method, like this:

```csharp
TR Call<TR>(Func<TR> f) {
  try {
    return f();
  } 
  catch (SomeException ex) {
    // Dull code.
  }
  catch (SomeOtherException ex) {
    // Dull code.
  }
  catch (YetAnotherException ex) {
    // Dull code.
  }
  catch (Exception ex) {
    // Dull code.
  }
}
```

And then you can use this in your methods:

```csharp
Foo SafeMethod1(int x, string s) {
  Call(() => Method1(x, s));
}

Bar SafeMethod2(double d) {
  Call(() => Method2(d));
}
```

Adding another method is trivial:

```csharp
Quux SafeMethod3() {
  Call(() => Method3());
}
```

This works pretty nicely and solves our immediate issue. But there are a couple of limitations. In particular, three problems spring to mind:

1. What if you want to return some legal, non-default value of type **TR** from one or more of the catch-blocks? As it stands now, each catch-block must either rethrow or return the default value of **TR**.
2. What if there are variations in how you want to handle some of the exceptions? For instance, it may be that **YetAnotherException** should be handled differently for each method.
3. What if there are slight variations between the "catching" needs for the various methods? What if you decided that **SafeMethod2** doesn't need to handle **SomeOtherException**, whereas **SafeMethod3** should handle **IdiosyncraticException** in addition to the "standard" ones?

As an answer to the two first problems, you could pass in each exception handler to the **Call** method! Then you would have a method like this:

```csharp
TR Call<TR>(
  Func<TR> f, 
  Func<SomeException, TR> h1, 
  Func<SomeOtherException, TR> h2, 
  Func<YetAnotherException, TR> h3, 
  Func<Exception, TR> h4) 
{
  try {
    return f();
  } 
  catch (SomeException ex) {
    return h1(ex);
  }
  catch (SomeOtherException ex) {
    return h2(ex);
  }
  catch (YetAnotherException ex) {
    return h3(ex);
  }
  catch (Exception ex) {
    return h4(ex);
  }
}
```

And at this point you're about to stop reading this blog post, because WTF. Now your methods look like this:

```csharp
Foo SafeMethod1(int x, string s) {
  Call(() => Method1(x, s),
    ex => // Handle SomeException,
    ex => // Handle SomeOtherException,
    ex => // Handle YetAnotherException,
    ex => // Handle Exception 
  );
}
```

So we're pretty much back at square one, except it's a bit more convoluted and confusing for the casual reader. And we still don't have a good solution for the third problem. We could of course fake "non-handling" of **SomeOtherException** for **SafeMethod2** by simply handing in a non-handling handler, that is, one that simply rethrows directly: `ex => { throw ex; }`. But that's ugly and what about the **IdiosyncraticException**? Well, it's not going to be pretty:

```csharp
Quux SafeMethod3() {
  try {
    return Call(() => Method3(),
       ex => // Handle SomeException,
       ex => // Handle SomeOtherException,
       ex => // Handle YetAnotherException,
       ex => // Handle Exception
    );    
  }
  catch (IdiosyncraticException ex) {
    // Do something.
  }
}
```

Which just might be the worst code ever, and also has the idiosyncracy that the additional catch-handler will only be reached if the **Exception** handler rethrows. Horrible. Better, perhaps, to put it inside?

```csharp
Quux SafeMethod3() {
  return Call(() => 
    { 
      try { return Method3(); } catch (IdiosyncraticException ex) { throw ex; } 
    },
    ex => // Handle SomeException,
    ex => // Handle SomeOtherException,
    ex => // Handle YetAnotherException,
    ex => // Handle Exception
  );    
}
```

Well yes, slightly better, but still pretty horrible, and much worse than just suffering the duplication in the first place. But maybe we've learned something. What we need is composition – our current solution doesn't compose at all. we need to be able to put together exactly the exception handlers we need for each method, while at the same time avoiding repetition. The problem is in some sense the coupling between the exception handlers. What if we tried a different approach, handling a single exception at a time?

We could have a less ambitious **Call**-method, that would handle just a single type of exception for a method. Like this:

```csharp
TR Call<TR, TE>(
  Func<TR> f, 
  Func<TE, TR> h) 
  where TE : Exception
{
  try {
    return f();
  } 
  catch (TE ex) {
    return h(ex);
  }
}
```

Now we have a single generic exception handler **h**. Note that when we constrain the type variable **TE** to be a subclass of **Exception**, we can use **TE** in the catch clause, to select precisely the exceptions we would like to catch. Then we could write a method like this:

```csharp
Frob SafeMethod4a() {
  return Call(
    () => new Frob(), 
    (NullReferenceException ex) => ... );
}
```

What if we wanted to catch another exception as well? The solution is obvious:

```csharp
Frob SafeMethod4b() {
  return Call(
    () => SafeMethod4a(),
    (InvalidOperationException ex) => ... );
}
```

And yet another exception?

```csharp
Frob SafeMethod4c() {
  return Call(
    () => SafeMethod4b(),
    (FormatException ex) => ... );
}
```

You get the picture. Of course, we can collapse all three to a single method if we want to:

```csharp
Frob SafeMethod4() {
  return 
    Call(() => 
      Call(() => 
        Call(() => 
          FrobFactory.Create(), 
          (NullReferenceException ex) => ... ), 
        (InvalidOperationException ex) => ... ),
      (FormatException ex) => ... );
}
```

What have we gained? Well, not readability, I'll admit that. But we've gained flexibility! Flexibility goes a long way! And we'll work on the readability shortly. First, though: just in case it's not clear, what we've done is that we've created an exception handling scenario that is similar to this:

```csharp
Frob TraditionalSafeMethod4() {
  try {
    try {
      try {
        return FrobFactory.Create();
      }
      catch (NullReferenceException ex) { ... handler code ... }
    }
    catch (InvalidOperationException ex) { ... handler code ... }
  }
  catch (FormatException ex) { ... handler code ... }
}
```

So there's nothing very complicated going on here. In fact, I bet you can see how similar the two methods really are – the structure is identical! All we've done is replace the familiar _try-catch_ construct with our own **Call**-construct.

As an aside, we should note that the composed try-catch approach has slightly different semantics than the sequential, coupled try-catch approach. The difference in semantics is due to decoupling provided by the composed try-catch approach – each catch-block is completely independent. Therefore, there is nothing stopping us from having multiple catch-handlers for the same type of exception should we so desire.

Now, to work on the readability a bit. What we really would like is some way to attach catch-handlers for various exception types to our function call. So assuming that we wrap up our original function call in a closure using a delegate of type **Func&lt;TR&gt;**, we would like to be able to attach a catch-handler for some exception type **TE**, and end up with a new closure that still has the type **Func&lt;TR&gt;**. Then we would have encapsulated the exception handling completely. Our unambitious **Call**-method from above is almost what we need, but not quite. Instead, let's define an extension method on the type that we would like to extend! **Func&lt;TR&gt;**, that is:

```csharp
static class CatchExtensions
{
  static Func<TR> Catch<TR, TE>(
    this Func<TR> f,
    Func<TE, TR> h) 
  where TE : Exception
  {
    return () => {
      try {
        return f ();
      } catch (TE ex) {
        return h (ex);
      };
    };
  }
}
```

So the trick is to return a new closure that encapsulates calling the original closure and the exception handling. Then we can write code like this:

```csharp
Frob ExtSafeMethod4() {
  Func<Frob> it = () => FrobFactory.Create();
  var safe = 
    it.Catch((NullReferenceException ex) => ... )
      .Catch((InvalidOperationException ex) => ... )
      .Catch((FormatException ex) => ... );
  return safe();
}
```

Now the neat thing is that you can very easily separate out the catch-handler-attachment from the rest of the code:

```csharp
Frob ExtSafeMethod4b() {
  var safe = Protect(() => FrobFactory.Create);
  return safe();
}

Func<TR> Protect<TR>(Func<TR> it) {
  return 
    it.Catch((NullReferenceException ex) => ... )
      .Catch((InvalidOperationException ex) => ... )
      .Catch((FormatException ex) => ... );
}
```

So we have essentially created a fluent interface for attaching catch-handlers to a method call. The cool thing is that it is trivial to attach additional exception handlers as needed – and since we do so programmatically, we can even have logic to control the attachment of handlers. Say we discovered that we needed to catch **WerewolfException**s when the moon is full? No problem:

```csharp
Func<Frob> WolfProof() {
  var f = Protect(() => FrobFactory.Create());
  if (IsFullMoon()) {
    f = f.Catch((WerewolfException ex) => ... ); // silver bullet?
  }
  return f;
}
```

In my eyes, this is pretty cool. You might be running away screaming, thinking I'm crazy and that with this approach, you'll never know which exceptions you're actually catching anymore. You could be right. Opinions differ.

But that's OK. All I'm doing is providing an alternative approach to the handling of multiple exception – one that I think offers increased power and flexibility. I'm not saying you should take advantage of it. With greater power comes greater responsibility and all that.

And besides, we still haven't talked about Linq. An alternative (and attractive) solution to our current fluent interface is to attach a sequence of catch-handlers at once. Something like this:

```csharp
// Unfortunately, this won't compile.
Func<TR> Protect<TR>(Func<TR> it) {
  return 
    it.CatchAll(
      (NullReferenceException ex) => ... ,
      (InvalidOperationException ex) => ... ,
      (FormatException ex) => ... );
}
```

However, it's surprisingly difficult to provide a suitable type for that sequence of catch-handlers – in fact, the C# compiler fails to do so! The problem is that delegates are contravariant in their parameters, which means that a delegate **D1** is considered a subtype of delegate **D2** if the parameters of **D1** are supertypes of the parameters of **D2**. That's all a bit abstract, so perhaps an example will help:

```csharp
Action<object> d1 = (object o) => {}; 
Action<string> d2 = (string s) => {}; 

d1 = d2; // This won't compile.
d2 = d1; // This is OK.
```

To make sense of the abstract description above, assume that **D1** is Action&lt;object&gt; and D2 is Action&lt;string>. Since the **D1** parameter (object) is a supertype of the **D2** parameter (string), it follows that **D1** is a subtype of **D2** – and not the other way around, as we might have guessed. This is why the C# compiler won't let us assign a **D2** instance to a **D1** reference.

The implication is that the C# compiler will fail to find a type that will reconcile the catch handlers above. In particular, due to the contravariance of delegate parameters, we cannot type the sequence as **Func&lt;Exception, TR&gt;**, since neither **Func&lt;NullReferenceException, TR&gt;**, nor **Func&lt;InvalidOperationException, TR&gt;**, nor **Func&lt;FormatException, TR&gt;** are assignable to **Func&lt;Exception, TR&gt;**. It would go the other way around: we could assign a **Func&lt;Exception, TR&gt;** to all three of the other types, but which one should the compiler pick? If it (arbitrarily) picked **Func<&lt;NullReferenceException, TR&gt;**, clearly it wouldn't work for the two other delegates – and all other choices have the same problem.

So we're stuck. Sort of. The only solution we have is to _hide_ the exception type somehow, so that we don't have to include the exception type in the type of the sequence. Now how do we do that? Well, in some sense, we've already seen an example of how to do that: we hide the exception handling (and the type) inside a closure. So all we need is some way to convert an exception handler to a simple transformation function that doesn't care about the type of the exception itself. Like this:

```csharp
Func<Func<TR>, Func<TR>> Encapsulate<TR, TE>(Func<TE, TR> h) 
  where TE : Exception
{
  return f => () =>  
  {
    try {
      return f();
    } 
    catch (TE ex) {
      return h(ex);
    };
  };
}
```

So what is this thing? It's a method that encapsulates the catch-handler inside a closure. This closure will take as input a closure of type **Func&lt;TR&gt;** and produce as output another closure of type **Func&lt;TR&gt;**. In the process, we have hidden the type **TE**, so that the C# compiler doesn't have to worry about it anymore: all we have is a thing that will transform a **Func&lt;TR&gt;** to another **Func&lt;TR&gt;**.

So now we can sort of accomplish what we wanted, even though it's less than perfect.

```csharp
Func<TR> Protect<TR>(Func<TR> it) {
  return 
    it.CatchAll(
      Encapsulate((NullReferenceException ex) => ...),
      Encapsulate((InvalidOperationException ex) => ...),
      Encapsulate((FormatException ex) => ... ));
}
```

But now we can have some fun using Linq's **Aggregate** method to compose our exception handlers. So we might write code like this:

```csharp
var catchers = new [] {
  Encapsulate((ArgumentException x) => x.Message),
  Encapsulate((InvalidOperationException x) => { Log(x.Message); throw x; },
  Encapsulate((NullReferenceException x) => "Uh oh")
};

var protect = catchers.Aggregate((acc, nxt) => thing => nxt(acc(thing)));

var f = protect(() => FetchStringSomewhere());

var s = f();
```

The cool part is obviously the **Aggregate** call, where **acc** is the "accumulated" composed closure, **nxt** is the next encapsulated exception handler and **thing** is the thing we're trying to protect with our exception handlers – so in other words, the closure that contains the call to **FetchStringSomewhere**.

And of course we can now implement **CatchAll** if we want to:

```csharp
static class CatchExtensions
{
  Func<TR> CatchAll<TR>(	
    this Func<TR> f, 
    params Func<Func<TR>, Func<TR>>[] catchers) 
  {
    var protect = catchers.Aggregate((acc, nxt) => thing => nxt(acc(thing)));
    return protect(f);
  }
}
```

Now please, if you are Eric Lippert and can come up with code that proves that I'm wrong with respect to the typing of sequences of exception handler delegates – please let me know! I would very much like to be corrected if that is the case.
