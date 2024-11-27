:page/title Enumerating enumerables
:blog-post/tags [:tech :programming :dotnet :csharp :python]
:blog-post/author {:person/id :einarwh}
:page/body

# Enumerating enumerables

Posted: May 7, 2011 

You know when you’re iterating over some IEnumerable, and you need to associate the items in the IEnumerable with a sequence number?

In Python, you could do this:
Python-enumerate-shell

In C#, however, you’re forced to do something like this:


var items = new [] { "zero", "one", "two" };
int no = 0;
foreach (var it in items) 
{
  Console.WriteLine("{0} => {1}", no, it);
  ++no;
}

view raw


Enumeration.cs

hosted with ❤ by GitHub

Yuck. I feel dirty each time. It’s two measly lines of code, but it sure feels like I’m nailing something onto the loop that doesn’t belong there. (And that’s probably because that’s exactly what I’m doing.) It feels out of sync with the level of abstraction for the foreach statement, and it’s just plain ugly. So what I’m looking for is an approach that’s more appealing aesthetically, something a little more polished, something like:


var items = new [] { "zero", "one", "two" };
foreach (var it in items.Enumerate()) 
{
  Console.WriteLine("{0} => {1}", it.Number, it.Item);
}

view raw


Enumerate.cs

hosted with ❤ by GitHub

To be sure, this is still not as clean as the Python code (for one, there’s no decomposition of tuple types).  But personally, I like it a whole lot better than the original C# version. It’s prettier, cleaner, and plugs the leaky abstraction.

As you can imagine, I’m using an extension method to pretend that IEnumerables can be, you know, enumerated. The task of the extension method is just to turn an IEnumerable<T> into an IEnumerable<Enumerated<T>>, like so:


    public static IEnumerable<Enumerated<T>> Enumerate<T>(this IEnumerable<T> e)   
    {  
      int i = 0;  
      return e.Select(it => new Enumerated<T>(i++, it));  
    }

view raw


Enumerate.Ext.cs

hosted with ❤ by GitHub

And Enumerated<T> is just a necessary evil to appease the C# compiler:


class Enumerated<T>
{
  private readonly int _number;
  private readonly T _;

  public Enumerated(int number, T t)
  {
    _number = number;
    _ = t;
  }

  public int Number
  {
    get { return _number; }
  }

  public T Item
  {
    get { return _; }
  }
}

view raw


Enumerated.cs

hosted with ❤ by GitHub

It is easy to augment types with arbitrary information this way; sequence numbers is just one example. For a general solution, though, you probably wouldn’t want to keep writing these plumbing wrappers like Enumerated<T>. It’s not just that your brain would go numb, you also need something more versatile, something that’s not bound to the specific type of information you’re augmenting with. The task-specific types are an obstacle to a simple, generic and flexible implementation.

A solution is to use the Tuple<T1, T2> type introduced in .NET 4. It’s sort of a compromise, though, and I don’t quite like it. Since it is a generic tuple, the names of the properties are meaningless (Item1 and Item2), and I believe rather firmly that names should be meaningful. However, using the Tuple<T1, T2> type makes it very easy to generalize the augmentation process. Here’s how you could go about it:


public static IEnumerable<Tuple<T, T1>> Augment<T, T1>(this IEnumerable<T> e, Func<T1> aug)
{
  return e.Select(it => Tuple.Create(it, aug()));
}

view raw


Augment.Ext.cs

hosted with ❤ by GitHub

You can use Augment directly, like so:


foreach (var it in items.Augment(() => Guid.NewGuid()))
{
  Console.WriteLine("{0} => {1}", it.Item2, it.Item1);
}

view raw


Augment.Use.cs

hosted with ❤ by GitHub

In this case, I’m augmenting each item with a Guid. Here’s the output:
Csharp-augment-prompt

This is convenient for one-off scenarios. If you’re going to augment types the same way multiple times, though, you might go through the trouble of defining some extension methods:


public static IEnumerable<Tuple<T, int>> Enumerate<T>(this IEnumerable<T> e)
{
  int i = 0;
  return Augment(e, () => i++);
}

public static IEnumerable<Tuple<T, DateTime>> WithTimestamps<T>(this IEnumerable<T> e)
{
  return Augment(e, () => DateTime.Now);
}

public static IEnumerable<Tuple<T, Guid>> WithGuids<T>(this IEnumerable<T> e)
{
  return Augment(e, Guid.New);
}

view raw


Augmenters.cs

hosted with ❤ by GitHub

And so on and so forth, for all your clever augmentation needs.

Then your code would look like this:


foreach (var it in items.WithGuids())
{
  Console.WriteLine("{0} => {1}", it.Item2, it.Item1);
}

view raw


WithGuids.Use.cs

hosted with ❤ by GitHub

Which is pretty neat. If you can stomach Item1 and Item2, that is.
