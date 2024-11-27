:page/title Rethrow recoil
:blog-post/tags [:tech :programming :csharp]
:blog-post/author {:person/id :einarwh}
:page/body

# Rethrow recoil

The closure-based exception handling scheme in the previous blog post works almost perfectly, and it would have worked entirely perfectly if a catch block were an ordinary block of code. But alas, it is not. Not quite. A catch block is special in that there is one statement that you can make inside a catch block that you cannot make anywhere else in your program. Unfortunately, it is also a fairly common statement to use inside catch blocks: the rethrow statement – that is, a throw statement with no operand. We cannot simply use a throw statement with an operand instead, since that has different semantics. In fact, it’s not a rethrow at all, it’s a brand new throw (even when we’re using the exception we just caught). The consequence is that the original stack trace is lost. In other words, we lose trace of where the exception originally occurred, which is almost never what we want.

So that’s unfortunate. Unacceptable even. Can we fix it? Of course we can fix it! We’re programmers, we can fix anything!

There’s no way to put a rethrow into our lambda expession though, so we need to do something else. That something else turns out to be trivial. We do have a genuine catch-block available, so we’ll just put it there. Or rather, we’ll create a new try-catch block with a hard-coded rethrow inside, and put that block inside a new extension method which complements the one we created last time. Like so:


public static Func<TR> Touch<TR, TE>(this Func<TR> f, Action<TE> h)
  where TE : Exception
{
  return () =>
  {
    try
    {
      return f();
    }
    catch (TE e)
    {
      h(e);
      throw;
    }
  };
}

view raw


Touch.cs

hosted with ❤ by GitHub

In this case, we use an Action<TE> instead of a Func<TE, TR>, because obviously the exception handler won’t be returning anything – we just hard-coded a rethrow in there!

Why Touch and not an overload of the Catch method we saw before? Well, first of all we’re not catching the exception, we’re merely touching it on the way through – hence Catch is not really a suitable name for what we’re doing. And besides, the following code snippet would be ambiguous, at least to the reader of the code. Assuming we had overloaded the Catch method, how should we interpret something like this?


Catch((Exception ex) => { throw ex; })

view raw


AmbCatch.cs

hosted with ❤ by GitHub

Is that an Action<TE> or a Func<TE, TR>? I have no idea. It turns out that the compiler is OK with the ambivalence – it decides it must be an Action<TE> (which leaves us with a rather strange catch block where the throw ex statement in the handler terminates the block before the rethrow occurs). But I’m not! Better to be explicit. Touch it is.

Now we can write code like this:


var g = f
  .Catch((ArgumentException ex) => "Something")
  .Touch((NullReferenceException ex) => Console.WriteLine("I saw you!"))
  .Catch((Exception ex) => "ex");

Console.WriteLine(g());

view raw


CatchTouchSample.cs

hosted with ❤ by GitHub

So what happens in the last line? Well, if an ArgumentException is thrown, we’ll see the string “Something” written. If a NullReferenceException is thrown, however, we’ll see the “I saw you” string, in addition to “ex”, since the exception percolates up to the outer handler where it is caught and swallowed.

Fixed!
