:page/title Polymorphic pain in ASP.NET data binding
:blog-post/tags [:tech :programming :dotnet :aspnet :csharp]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2011-04-17T12:00:00"
:page/body

# Polymorphic pain in ASP.NET data binding

Posted: April 17, 2011 

I recently found out – the hard way, of course – that data binding in ASP.NET is broken with respect to polymorphism. It’s not consistently broken, though – it depends on the particular control you’re using. Makes life as a programmer that much more interesting, doesn’t it?

Let’s consider a very simple example. We have a type hierarchy like the following:

TODO: Image: Canine-hierarchy

There’s an ICanine interface, with implementing classes Wolf and Dog. Chihuahua is a subclass of the latter. The code looks like this:

```csharp
interface ICanine 
{
  string Bark { get; }
}

class Wolf : ICanine
{
  public virtual string Bark { get { return "Aooo!"; } } 
}

class Dog : ICanine
{
  public virtual string Bark { get { return "Woof!"; } } 
}

class Chihuahua : Dog
{
  public override string Bark { get { return "Arff!"; } } 
}
```

Now we’d like to conjure up a collection of canines and bind to them. Sounds innocent enough, right? And it is, if you use one of the benign controls. “Benign” as in “not badly broken”. ListBox is one of those. We create an IEnumerable<ICanine> like so:

```csharp
var canines = new List<ICanine> { new Dog(), new Wolf(), new Chihuahua() };
```

And then we do the two-step song-and-dance of ASP.NET data binding (assuming that _box is an instance of ListBox):

```csharp
_box.DataSource = canines;
_box.DataBind();
```

We run it, and get the following result:

TODO: Image: Bark-listbox

Presto! All is well! What is this guy talking about? Broken polymorphism? Where?

Well, that was the benign control, remember? Here’s a malicious one: DataGrid.

We do the exact same thing, except using _grid of the obvious type:

```csharp
_grid.DataSource = canines;
_grid.DataBind();
```

We run it, and get…

TODO: Image: Exception-grid-wolf

Ouch.

Evidently, there’s some reflection voodoo going on underneath the hood when you’re doing data binding in ASP.NET. And in the case of DataGrid, that voodoo is just too feeble.

Now, consider a variation of the code above, omitting the Wolf. Wolves are trouble after all.

```csharp
var canines = new List<ICanine> { new Dog(), new Chihuahua() };
_box.DataSource = canines;
_box.DataBind();
```

This time...

TODO: Image: Bark-grid-dogs

It works! Oh man, that’s weird. So apparently subclassing works as long as there’s a common base class? You wish. Let’s try this instead:

```csharp
var canines = new List<ICanine> { new Chihuahua(), new Dog() };
_box.DataSource = canines;
_box.DataBind();
```

That is, we reverse the order, passing in the Chihuahua first, before the Dog. And now:

TODO: Image: Reflection-dog-exception

Gaah!

The reflection voodoo seems to be making some arbitrary assumptions regarding types based on the first element in the enumerable or some such. You could probably tease out the details using Reflector and coffee, but there’s no point. It’s just broken; I don’t care exactly how. What we need is a simple and predicatable workaround.

## Workaround

You can mitigate the problem (aka complicate your program in order to work around a broken framework) by using a wrapper type that always stays the same. That way, the type of the instances handed out by the IEnumerable stays nice and homogenous, just the way DataGrid likes it. Inside the wrapper, you delegate to whatever ICanine you want:

```csharp
class DataBindingCanineWrapper : ICanine 
{
  private readonly ICanine _;

  public DataBindingCanineWrapper(ICanine canine) {
    _ = canine;
  }
  
  public string Bark { get { return _.Bark; } }
}
```

This effectively replaces the original IEnumerable<ICanine> containing the bare Chihuahua and Dog with one that contains only wrapper canines. So data binding should work. And it does:

TODO: Image: Wrapped-chihuahua

Notice that we got the Chihuahua Arff!ing as the first grid element.

You could generate such wrappers on the fly, using reflection. In fact, you can download and use something called a “defensive datasource“. Turns out I’m not the only one who’s been bitten and annoyed by this issue.

## Peace, love and understanding

Why is DataGrid broken? Well, if you crack open BaseDataList, a base class for both DataGrid and ListBox, you’ll find that the DataSource property assumes that you’re passing it an IEnumerable. No T, just a plain ol’ .NET 1.1-style untyped IEnumerable. So basically, it’s just a series of arbitrary stuff, the type of which could be anything. You could stuff apples and oranges in there, no problem. Now recall this result:
Bark-grid-dogs

See the Bark header? That’s the name of the property shared by Dog and Chihuahua. This is DataGrid auto-generating columns based on the properties of the objects you pass it for data binding. It’s sort of cool, even though it’s broken. Of course, the DataGrid couldn’t pull that trick off without knowing something about the types of the instances in the IEnumerable. In fact, it absolutely needs to know that all instances share the properties that it’s going to display. If you put instances of both Apple and Orange into your IEnumerable, what would you expect to see? You need some commonality between the types, or the whole premise of DataGrid just falls apart.

Of course, an IEnumerable<T> would give you what you need: a common type T for the DataGrid to work with. But DataGrid is stuck with an IEnumerable for its data source and has to make do, somehow. One solution would be to build an inheritance tree for all the elements in the IEnumerable and pick the root type, the least common denominator so to speak. But I imagine that would be costly. Instead, DataGrid looks at the type of the first element, and assumes that the rest will be just like it (or a subclass). Weird, arbitrary, yet at least not completely irrational.

## ListBox revisited

Now, given that ListBox also expects an untyped IEnumerable, how come polymorphism seems to work in that case?

Consider three unrelated classes, Huey, Dewey and Louie. We’ll make them singletons since there can only be one of each. More importantly, they all inherit directly from System.Object; there’s nothing else linking them together (no IDuck interface, no Duck base class). Coincidentally, though, they each sport a QuacksLike property.

Here’s the code for Huey:

```csharp
class Huey
{
  private Huey() { }

  private static readonly Huey _instance = new Huey();

  public static Huey Instance { get { return _instance; } }

  public string QuacksLike { get { return "Huey."; } }
}
```

As you can imagine, the declarations for Dewey and Louie are remarkably similar.

Let’s toss all three into an IEnumerable and see what happens:

```csharp
_box.DataSource = new ArrayList { Huey.Instance, Dewey.Instance, Louie.Instance };
_box.DataBind();
```

The result is this:

TODO: Image: Ducks-listbox

Isn’t that something? It’s not really polymorphic at all! Instead, it turns out that ListBox supports duck typing. As long as the object has a public property matching the DataTextField for the ListBox, the actual type is irrelevant. The property’s type is irrelevant too. We could change Dewey‘s implementation of QuacksLike like this, and it will still work:

```csharp
public Quacker QuacksLike { get { return new Quacker(); } }
```

Quacker could be anything, really. Here’s a possibility:

```csharp
public class Quacker
{
  public override string ToString() 
  {
    return "Any ol' duck.";
  }
}
```

Now we get this:

TODO: Image: Any-old-duck

Of course, if we were to replace Dewey with, say, an Apple, we’d be in trouble (unless it happens to have a public QuacksLike property, but that seems unlikely):

TODO: Image: Exception-apple-quacks

No duck typing without quacking, that’s what I always say!

## Conclusion

So polymorphism is not actually supported by either control. It’s just that it’s more likely that you’ll notice when you use a DataGrid than a ListBox. Fun!
