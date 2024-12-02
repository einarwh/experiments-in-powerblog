:page/title A property with a view
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2012-05-01T21:29:00"
:page/body

# A property with a view

Posted: May 1, 2012

I've never been much of an early adopter, so now that Silverlight is dead and cold and has been for a while, it seems appropriate for me to blog about it. More specifically, I'd like to write about the chore that is **INotifyPropertyChanged** and how to make practically all the grunt work go away.

(Actually, I'm not sure that Silverlight is completely dead yet. It may be that Microsoft won't be pumping much fresh blood into its veins, but that's not quite the same thing as being dead. A technology isn't dead as long as there's a market for it and all that jazz. Assuming that there are some kinds of applications (such as rich line-of-business applications) that are easier to build with Silverlight than HTML5 given the toolsets that are available, I think we'll find that Silverlight hangs around to haunt us for quite a while. But that's sort of a side issue. It doesn't really matter if Silverlight is dead or not, the issue of tackling **INotifyPropertyChanged** is interesting in and of itself.)

So **INotifyPropertyChanged** is the hoop you have to jump through to enable Silverlight views to update themselves as the view models they bind to change. It's hard to envision not wanting the view to update when the view model changes. So typically you'll want all the properties you bind to, to automatically cause the view to refresh itself. The problem is that this doesn't happen out of the box. Instead, there's this cumbersome and tiresome ritual you have to go through where you implement **INotifyPropertyChanged**, and have all the setters in your view model holler "hey, I've changed" by firing the **PropertyChanged** event. Brains need not apply to do this kind of work; it's just mind-numbing, repetitive plumbing code. It would be much nicer if the framework would just be intelligent enough to provide the necessary notifications all by itself. Unfortunately, that's not the case.

## Solution: IL weaving

Silver.Needle is the name I use for some code I wrote to do fix that. The basic idea is to use IL manipulation to automatically turn the plain vanilla .NET properties on your view models into view-update-triggering properties with the boring but necessary plumbing just magically \*there\*. Look ma, no hands!

If you're unfamiliar with IL manipulation, you might assume that it's hard to do because it's sort of low-level and therefore all voodooy and scary. But you'd be wrong. It might have been, without proper tools. Enter the star of this blog post: [Mono.Cecil](http://www.mono-project.com/Cecil). Mono.Cecil is a library for IL manipulation written by Jb Evain. It is so powerful, it's almost indecent: you get the feeling that IL manipulation shouldn't be that easy. But it is, it really is. It's a walk in the park. And the power trip you get is unbelievable.

Of course, since I rarely have original thoughts, Silver.Needle isn't unique. You'll find that Justin Angel described [a very similar approach](http://justinangel.net/AutomagicallyImplementingINotifyPropertyChanged) on his blog, more than two years ago. He used Mono.Cecil too. So did the **Kind of Magic** and **NotifyPropertyWeaver** projects (_Update 2024: both now apparently defunct_). But as always, it's much more fun and educational to roll your own!

> Disclaimer: it is fairly easy to shoot yourself in the foot when you're meddling with IL directly. I accept no liability if you try to run any of the code included in this blog post and end up injecting IL into your cat, or causing your boss to fail spectacularly at runtime, or encountering any other unfortunate and grotesque mishap as a result of doing so. You have been warned.

## Viewable properties

To do the IL manipulation, we need a way to distinguish between properties to tamper with and properties to leave alone. We'll refer to the former as _viewable_ properties because, you know, they're able to _work_ with a _view_?

Silver.Needle gives you two options for indicating that a property is viewable. The first option is to opt-in for individual properties on a class, by annotating each property with the **Viewable** attribute. The second option is to annotate the entire class as **Viewable**, and optionally opt-out for individual properties on that class using the **Opaque** attribute. In either case, the class is considered to be a "view model", with one or more viewable properties that notify the view of any changes.

```csharp
public class ViewableAttribute: Attribute {}
```

So the task solved by Silver.Needle is to perform the IL voodoo necessary to make sure that the output of the C# compiler of this pretty lean and neato code:

```csharp
public class PersonViewModel
{
  [Viewable]
  public string Name { get; set; }
}
```

...is the same as the output generated directly when compiling this cumbersome and clumsy mess:

```csharp
public class PersonViewModel : INotifyPropertyChanged
{
  private string _name;

  public event PropertyChangedEventHandler PropertyChanged;

  public string Name
  {
    get
    {
      return _name;
    }
    set
    {
      _name = value;
      NotifyViewableProperty("Name");
    }
  }

  private void NotifyViewableProperty(string propertyName)
  {
    var propertyChanged = this.PropertyChanged;
    if (propertyChanged != null)
    {
      propertyChanged.Invoke(this, 
        new PropertyChangedEventArgs(propertyName));
    }
  }
}
```

We start by using Mono.Cecil to look for types that contain such properties. It's a simple matter of 1) loading the assembly with Mono.Cecil, 2) iterating over the types in the assembly and 3) iterating over the properties defined for each type. Of course, if we find one or more "view model" types with properties that should perform view notification, we must proceed to do the necessary IL manipulation and write the changed assembly to disk afterwards. The meat of the matter is in scanning an individual type and doing the IL manipulation. We'll come to that shortly. The surrounding bureaucracy is handled by the **NotificationTamperer** class.

```csharp
public class NotificationTamperer : ITamperer
{
  private readonly string _assemblyOutputFileName;

  public NotificationTamperer() : this("default_tampered.dll") {}

  public NotificationTamperer(string assemblyOutputFileName)
  {
    _assemblyOutputFileName = assemblyOutputFileName;
  }

  private static AssemblyDefinition ReadSilverlightAssembly(
    string assemblyPath)
  {
    var resolver = new DefaultAssemblyResolver();
    resolver.AddSearchDirectory(@"C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\Silverlight\v4.0");

    var assembly = AssemblyDefinition.ReadAssembly(
      assemblyPath,
      new ReaderParameters { AssemblyResolver = resolver });
    return assembly;
  }

  public bool TamperWith(string assemblyPath)
  {
    var assembly = ReadSilverlightAssembly(assemblyPath);
    bool result = TamperWith(assembly);
    if (result)
    {
      assembly.Write(_assemblyOutputFileName);
    }
    return result;
  }

  private bool TamperWith(AssemblyDefinition assembly)
  {
    bool result = false;
    foreach (TypeDefinition type in assembly.MainModule.Types)
    {
      result = new TypeTamperer(type).MaybeTamperWith() || result;
    }
    return result;
  }
}
```

There's not much going on here worth commenting upon, it's just the stuff outlined above. I guess the only thing worth noting is that we need to add a reference to the Silverlight assemblies, so that Mono.Cecil can resolve type dependencies as necessary later on. (For simplicity, I just hard-coded the path to the assemblies on my system. Did I mention it's not quite ready for the enterprise yet?)

The interesting stuff happens in the **TypeTamperer**. You'll notice that the **TypeTamperer** works on a single type, which is passed in to the constructor. This is the type that may or may not contain viewable properites, and may or may not end up being tampered with. The type is represented by a Mono.Cecil **TypeDefinition**, which has collections for interfaces, methods, fields, events and so forth.

The **TypeTamperer** does two things. First, it looks for any viewable properties. Second, if any viewable properties were found, it ensures that the type in question implements the **INotifyPropertyChanged** interface, and that the viewable properties participate in the notification mechanism by raising the **PropertyChanged** event as appropriate.

Let's see how the identification happens:

```csharp
public bool MaybeTamperWith()
{
  return _typeDef.IsClass 
    && HasPropertiesToTamperWith() 
    && ReallyTamperWith();
}

private bool HasPropertiesToTamperWith()
{
  FindPropertiesToTamperWith();
  return _map.Count > 0;
}

private void FindPropertiesToTamperWith()
{
  var isViewableType = IsViewable(_typeDef);
  foreach (var prop in _typeDef.Properties
    .Where(p => IsViewable(p) || (isViewableType && !IsOpaque(p))))
  {
    HandlePropertyToNotify(prop);
  }
}

private static bool IsViewable(ICustomAttributeProvider item)
{
  return HasAttribute(item, ViewableAttributeName);
}

private static bool IsOpaque(ICustomAttributeProvider item)
{
  return HasAttribute(item, OpaqueAttributeName);
}

private static bool HasAttribute(ICustomAttributeProvider item, 
  string attributeName)
{
  return item.CustomAttributes.Any(
    a => a.AttributeType.Name == attributeName);
}
```

As you can see, the code is very straight-forward. We just make sure that the type we're inspecting is a class (as opposed to an interface), and look for viewable properties. If we find a viewable property, the HandlePropertyToNotify method is called. We'll look at that method in detail later on. For now though, we'll just note that the property will end up in an **IDictionary** named _map, so that the **ReallyTamperWith** method is called, triggering the IL manipulation.

For each of the view model types, we need to make sure that the type implements INotifyPropertyChanged. From an IL manipulation point of view, this entails three things:

* Adding interface declaration as needed.
* Adding event declaration as needed.
* Adding event trigger method as needed.

Silver.Needle tries to play nicely with a complete or partial hand-written implementation of **INotifyPropertyChanged**. It's not too hard to do, the main complicating matter being that we need to consider inheritance. The type might inherit from another type (say, **ViewModelBase**) that implements the interface. Obviously, we shouldn't do anything in that case. We should only inject implementation code for types that do not already implement the interface, either directly or in a base type. To do this, we need to walk the inheritance chain up to **System.Object** before we can conclude that the interface is indeed missing and proceed to inject code for the implementation.

```csharp
private bool ReallyTamperWith()
{
  EnsureTypeImplementsInterface();
  TamperWithPropertySetters();
  return true;
}

private void EnsureTypeImplementsInterface()
{
  if (!TypeAlreadyImplementsInterface())
  {
    InjectInterfaceImplementation();
  }
  IdentifyNotificationMethod();
}

private static bool TypeImplementsInterface(TypeDefinition typeDef)
{
  return 
    typeDef.Interfaces.Any(
      it => it.Name == "INotifyPropertyChanged") 
    ||
    (typeDef.BaseType != null 
      && TypeImplementsInterface(typeDef.BaseType.Resolve()));
}

private void InjectInterfaceImplementation()
{
  InjectInterfaceDeclaration();
  InjectEventHandler();
}
```

This is still pretty self-explanatory. The most interesting method is **TypeImplementsInterface**, which calls itself recursively to climb up the inheritance ladder until it either finds a type that implements **INotifyPropertyChanged** or a type whose base type is **null** (that would be **System.Object**).

## Implementing the interface

Injecting code to implement the interface consists of two parts, just as if you were implementing the interface by writing source code by hand: 1) injecting the declaration of the interface, and 2) injecting the code to fulfil the contract defined by the interface, that is, the declaration of the **PropertyChanged** event handler.

```csharp
private void InjectInterfaceDeclaration()
{
  _typeDef.Interfaces.Add(_types.INotifyPropertyChanged);
}
```

The code to add the interface declaration is utterly trivial: you just add the appropriate type to the **TypeDefinition**‘s **Interfaces** collection. You get a first indication of the power of Mono.Cecil right there. You do need to obtain the proper **TypeReference** (another Mono.Cecil type) though. I've created a helper class to make this as simple as I could as well. The code looks like this:

```csharp
public class TypeResolver
{
  private readonly TypeDefinition _typeDef;
  private readonly IDictionary<Type, TypeReference> _typeRefs = 
    new Dictionary<Type, TypeReference>();
  private readonly TypeSystem _ts;
  private readonly ModuleDefinition _systemModule;
  private readonly ModuleDefinition _mscorlibModule;

  public TypeResolver(TypeDefinition typeDef)
  {
    _typeDef = typeDef;
    _ts = typeDef.Module.TypeSystem;
    Func<string, ModuleDefinition> getModule = 
      m => typeDef.Module.AssemblyResolver.Resolve(m).MainModule;
    _systemModule = getModule("system");
    _mscorlibModule = getModule("mscorlib");
  }

  public TypeReference Object
  {
    get { return _ts.Object; }
  }

  public TypeReference String
  {
    get { return _ts.String; }
  }

  public TypeReference Void
  {
    get { return _ts.Void; }
  }

  public TypeReference INotifyPropertyChanged
  {
    get { return LookupSystem(typeof(INotifyPropertyChanged)); }
  }

  public TypeReference PropertyChangedEventHandler
  {
    get { return LookupSystem(typeof(PropertyChangedEventHandler)); }
  }

  public TypeReference PropertyChangedEventArgs
  {
    get { return LookupSystem(typeof(PropertyChangedEventArgs)); }
  }

  public TypeReference Delegate
  {
    get { return LookupCore(typeof(Delegate)); }
  }

  public TypeReference Interlocked
  {
    get { return LookupCore(typeof(Interlocked)); }
  }

  private TypeReference LookupCore(Type t)
  {
    return Lookup(t, _mscorlibModule);
  }

  private TypeReference LookupSystem(Type t)
  {
    return Lookup(t, _systemModule);
  }

  private TypeReference Lookup(Type t, ModuleDefinition moduleDef)
  {
    if (!_typeRefs.ContainsKey(t))
    {
      var typeRef = moduleDef.Types.FirstOrDefault(
        td => td.FullName == t.FullName);
      if (typeRef == null)
      {
        return null;
      }
      var importedTypeRef = _typeDef.Module.Import(typeRef);
      _typeRefs[t] = importedTypeRef;
    }
    return _typeRefs[t];
  }
}
```

Mono.Cecil comes with a built-in **TypeSystem** type that contains **TypeReference** objects for the most common types, such as **Object** and **String**. For other types, though, you need to use Mono.Cecil's assembly resolver to get the appropriate **TypeReference** objects. For convenience, **TypeResolver** defines properties with **TypeReference** objects for all the types used by **TypeTamperer**.

With the interface declaration in place, we need to provide an implementation (otherwise, we get nasty runtime exceptions).

Herein lies a potential hickup which might lead to problems in case the implementer is exceedingly stupid, though. Since Silver.Needle is a proof-of-concept rather than a super-robust enterprise tool, I don't worry too much about such edge cases. Nevertheless, I try to play nice where I can (and if it's easy to do), so here goes: The issue is that the view model type might already have a member of some sort named **PropertyChanged**, even though the type itself doesn't inherit from **INotifyPropertyChanged**. If it actually is an event handler such as defined by **INotifyPropertyChanged**, everything is fine (I just need to make sure that I don't add it.) The real issue if there is some other member named **PropertyChanged**, say, a property or a method. I can't imagine why you'd want to do such a thing, but of course there's no stopping the inventiveness of the sufficiently stupid programmer. To avoid producing a weird assembly that will fail dramatically during runtime, Silver.Needle will discover the presence of a malplaced, ill-typed **PropertyChanged** and give up, leaving the type untampered (and hence not implementing **INotifyPropertyChanged**).

Adding the event handler is a bit more work than you might expect. If you inspect the IL, it becomes abundantly clear that C# provides a good spoonful of syntactic sugar for events. At the IL level, you'll find that the simple event declaration expands to this:

* A field for the event handler.
* An event, which hooks up the field with add and remove methods.
* Implementation for the add and remove methods.

It's quite a bit of IL:

```
field private class [System]System.ComponentModel.PropertyChangedEventHandler PropertyChanged

event [System]System.ComponentModel.PropertyChangedEventHandler PropertyChanged
{
  .addon instance void Silver.Needle.Tests.Data.Dependencies.Complex.PersonViewModel::add_PropertyChanged(class [System]System.ComponentModel.PropertyChangedEventHandler)
  .removeon instance void Silver.Needle.Tests.Data.Dependencies.Complex.PersonViewModel::remove_PropertyChanged(class [System]System.ComponentModel.PropertyChangedEventHandler)
}

.method public final hidebysig specialname newslot virtual 
  instance void add_PropertyChanged (
    class [System]System.ComponentModel.PropertyChangedEventHandler 'value'
  ) cil managed 
{
  .maxstack 3
  .locals init (
    [0] class [System]System.ComponentModel.PropertyChangedEventHandler,
    [1] class [System]System.ComponentModel.PropertyChangedEventHandler,
    [2] class [System]System.ComponentModel.PropertyChangedEventHandler
  )

  IL_0000: ldarg.0
  IL_0001: ldfld class [System]System.ComponentModel.PropertyChangedEventHandler Silver.Needle.Tests.Data.Dependencies.Complex.PersonViewModel::PropertyChanged
  IL_0006: stloc.0
  // loop start (head: IL_0007)
  IL_0007: ldloc.0
  IL_0008: stloc.1
  IL_0009: ldloc.1
  IL_000a: ldarg.1
  IL_000b: call class [mscorlib]System.Delegate [mscorlib]System.Delegate::Combine(class [mscorlib]System.Delegate, class [mscorlib]System.Delegate)
  IL_0010: castclass [System]System.ComponentModel.PropertyChangedEventHandler
  IL_0015: stloc.2
  IL_0016: ldarg.0
  IL_0017: ldflda class [System]System.ComponentModel.PropertyChangedEventHandler Silver.Needle.Tests.Data.Dependencies.Complex.PersonViewModel::PropertyChanged
  IL_001c: ldloc.2
  IL_001d: ldloc.1
  IL_001e: call !!0 [mscorlib]System.Threading.Interlocked::CompareExchange<class [System]System.ComponentModel.PropertyChangedEventHandler>(!!0&, !!0, !!0)
  IL_0023: stloc.0
  IL_0024: ldloc.0
  IL_0025: ldloc.1
  IL_0026: bne.un.s IL_0007
  // end loop
  IL_0028: ret
} // end of method PersonViewModel::add_PropertyChanged

.method public final hidebysig specialname newslot virtual 
  instance void remove_PropertyChanged (
    class [System]System.ComponentModel.PropertyChangedEventHandler 'value'
  ) cil managed 
{
  .maxstack 3
  .locals init (
    [0] class [System]System.ComponentModel.PropertyChangedEventHandler,
    [1] class [System]System.ComponentModel.PropertyChangedEventHandler,
    [2] class [System]System.ComponentModel.PropertyChangedEventHandler
  )

  IL_0000: ldarg.0
  IL_0001: ldfld class [System]System.ComponentModel.PropertyChangedEventHandler Silver.Needle.Tests.Data.Dependencies.Complex.PersonViewModel::PropertyChanged
  IL_0006: stloc.0
  // loop start (head: IL_0007)
  IL_0007: ldloc.0
  IL_0008: stloc.1
  IL_0009: ldloc.1
  IL_000a: ldarg.1
  IL_000b: call class [mscorlib]System.Delegate [mscorlib]System.Delegate::Remove(class [mscorlib]System.Delegate, class [mscorlib]System.Delegate)
  IL_0010: castclass [System]System.ComponentModel.PropertyChangedEventHandler
  IL_0015: stloc.2
  IL_0016: ldarg.0
  IL_0017: ldflda class [System]System.ComponentModel.PropertyChangedEventHandler Silver.Needle.Tests.Data.Dependencies.Complex.PersonViewModel::PropertyChanged
  IL_001c: ldloc.2
  IL_001d: ldloc.1
  IL_001e: call !!0 [mscorlib]System.Threading.Interlocked::CompareExchange<class [System]System.ComponentModel.PropertyChangedEventHandler>(!!0&, !!0, !!0)
  IL_0023: stloc.0
  IL_0024: ldloc.0
  IL_0025: ldloc.1
  IL_0026: bne.un.s IL_0007
  // end loop
  IL_0028: ret
} // end of method PersonViewModel::remove_PropertyChanged
```

The bad news is that it's up to us to inject all that goo into our type. The good news is that Mono.Cecil makes it fairly easy to do. We'll get right to it:

```csharp
private void InjectEventHandler()
{
  InjectPropertyChangedField();
  InjectEventDeclaration();
}

private void InjectPropertyChangedField()
{
  //.field private class [System]System.ComponentModel.PropertyChangedEventHandler PropertyChanged
  var field = new FieldDefinition(PropertyChangedFieldName, 
    FieldAttributes.Private, 
    _types.PropertyChangedEventHandler);
  _typeDef.Fields.Add(field);
}

private void InjectEventDeclaration()
{
  // .event [System]System.ComponentModel.PropertyChangedEventHandler PropertyChanged
  // {
  // 	.addon instance void Voodoo.ViewModel.GoalViewModel::add_PropertyChanged(class [System]System.ComponentModel.PropertyChangedEventHandler)
  // 	.removeon instance void Voodoo.ViewModel.GoalViewModel::remove_PropertyChanged(class [System]System.ComponentModel.PropertyChangedEventHandler)
  // }
  var eventDef = new EventDefinition(PropertyChangedFieldName, 
    EventAttributes.None, 
    _types.PropertyChangedEventHandler)
  {
    AddMethod = CreateAddPropertyChangedMethod(),
    RemoveMethod = CreateRemovePropertyChangedMethod()
  };
  _typeDef.Methods.Add(eventDef.AddMethod);
  _typeDef.Methods.Add(eventDef.RemoveMethod);
  _typeDef.Events.Add(eventDef);
}
```

Here we add the field for the event handler, and we create an event which hooks up to two methods for adding and removing event handlers, respectively. We're still not done, though – in fact, the bulk of the nitty gritty work remains.

That bulk is the implementation of the _add_ and _remove_ methods. If you examine the IL, you'll see that the implementations are virtually identical, except for a single method call in the middle somewhere (_add_ calls a method called **Combine**, _remove_ calls **Remove**). We can abstract that out, like so:

```csharp
private MethodDefinition CreateAddPropertyChangedMethod()
{
  return CreatePropertyChangedEventHookupMethod(
    "add_PropertyChanged", 
    "Combine");
}

private MethodDefinition CreateRemovePropertyChangedMethod()
{
  return CreatePropertyChangedEventHookupMethod(
    "remove_PropertyChanged", 
    "Remove");
}

private MethodDefinition CreatePropertyChangedEventHookupMethod(
  string eventHookupMethodName, 
  string delegateMethodName)
{
  // .method public final hidebysig specialname newslot virtual 
  //   instance void add_PropertyChanged (
  //     class [System]System.ComponentModel.PropertyChangedEventHandler 'value'
  //   ) cil managed 
  var methodDef = new MethodDefinition(eventHookupMethodName,
    MethodAttributes.Public |
    MethodAttributes.Final |
    MethodAttributes.HideBySig |
    MethodAttributes.SpecialName |
    MethodAttributes.NewSlot |
    MethodAttributes.Virtual,
    _types.Void);
  var paramDef = new ParameterDefinition("value", 
    ParameterAttributes.None, 
    _types.PropertyChangedEventHandler);
  methodDef.Parameters.Add(paramDef);
  methodDef.Body.MaxStackSize = 3;
  for (int i = 0; i < 3; i++)
  {
    var v = new VariableDefinition(_types.PropertyChangedEventHandler);
    methodDef.Body.Variables.Add(v);
  }
  methodDef.Body.InitLocals = true;
  var il = methodDef.Body.GetILProcessor();
  Action<OpCode> op = x => il.Append(il.Create(x));
  // IL_0000: ldarg.0
  op(OpCodes.Ldarg_0);
  // IL_0001: ldfld class [System]System.ComponentModel.PropertyChangedEventHandler Voodoo.ViewModel.GoalViewModel::PropertyChanged
  var eventHandlerFieldDef = _typeDef.Fields
    .FirstOrDefault(f => f.Name == PropertyChangedFieldName);
  il.Append(il.Create(OpCodes.Ldfld, eventHandlerFieldDef));
  // IL_0006: stloc.0
  op(OpCodes.Stloc_0);
  // // loop start (head: IL_0007)
  //    IL_0007: ldloc.0
  var loopTargetInsn = il.Create(OpCodes.Ldloc_0);
  il.Append(loopTargetInsn);
  //    IL_0008: stloc.1
  op(OpCodes.Stloc_1);
  //    IL_0009: ldloc.1
  op(OpCodes.Ldloc_1);
  //    IL_000a: ldarg.1
  op(OpCodes.Ldarg_1);
  //    IL_000b: call class [mscorlib]System.Delegate [mscorlib]System.Delegate::Combine(class [mscorlib]System.Delegate, class [mscorlib]System.Delegate)
  var combineMethodReference = new MethodReference(
    delegateMethodName, 
    _types.Delegate, 
    _types.Delegate);
  var delegateParamDef = new ParameterDefinition(_types.Delegate);
  combineMethodReference.Parameters.Add(delegateParamDef);
  combineMethodReference.Parameters.Add(delegateParamDef);
  il.Append(il.Create(OpCodes.Call, combineMethodReference));
  //    IL_0010: castclass [System]System.ComponentModel.PropertyChangedEventHandler
  il.Append(il.Create(OpCodes.Castclass, 
    _types.PropertyChangedEventHandler));
  //    IL_0015: stloc.2
  op(OpCodes.Stloc_2);
  //    IL_0016: ldarg.0
  op(OpCodes.Ldarg_0);
  //    IL_0017: ldflda class [System]System.ComponentModel.PropertyChangedEventHandler Voodoo.ViewModel.GoalViewModel::PropertyChanged
  il.Append(il.Create(OpCodes.Ldflda, eventHandlerFieldDef));
  //    IL_001c: ldloc.2
  op(OpCodes.Ldloc_2);
  //    IL_001d: ldloc.1
  op(OpCodes.Ldloc_1);
  //    IL_001e: call !!0 [mscorlib]System.Threading.Interlocked::CompareExchange<class [System]System.ComponentModel.PropertyChangedEventHandler>(!!0&, !!0, !!0)
  //    var declaringTypeRef = _typeDef.Module.Import(typeof(Interlocked));
  var declaringTypeRef = _types.Interlocked;
  var elementMethodRef = new MethodReference(
    "CompareExchange", 
    _types.Void, 
    declaringTypeRef);
  var genParam = new GenericParameter("!!0", elementMethodRef);
  elementMethodRef.ReturnType = genParam;
  elementMethodRef.GenericParameters.Add(genParam);
  var firstParamDef = new ParameterDefinition(
    new ByReferenceType(genParam));
  var otherParamDef = new ParameterDefinition(genParam);
  elementMethodRef.Parameters.Add(firstParamDef);
  elementMethodRef.Parameters.Add(otherParamDef);
  elementMethodRef.Parameters.Add(otherParamDef);
  var genInstanceMethod = new GenericInstanceMethod(elementMethodRef);
  genInstanceMethod.GenericArguments.Add(
    _types.PropertyChangedEventHandler);
  il.Append(il.Create(OpCodes.Call, genInstanceMethod));
  //    IL_0023: stloc.0
  op(OpCodes.Stloc_0);
  //    IL_0024: ldloc.0
  op(OpCodes.Ldloc_0);
  //    IL_0025: ldloc.1
  op(OpCodes.Ldloc_1);
  //    IL_0026: bne.un.s IL_0007
  il.Append(il.Create(OpCodes.Bne_Un_S, loopTargetInsn));
  // // end loop
  // IL_0028: ret
  op(OpCodes.Ret);
    
  return methodDef;
}
```

It looks a little bit icky at first glance, but it's actually quite straightforward. You just need to accurately and painstakingly reconstruct the IL statement by statement. As you can see, I've left the original IL in the source code as comments, to make it clear what we're trying to reproduce. It takes patience more than brains.

The final piece of the implementation puzzle is to implement a method for firing the event. Again, Silver.Needle tries to play along with any hand-written code you have. So if you have implemented a method so-and-so to do view notification, it's quite likely that Silver.Needle will discover it and use it. Basically it will scan all methods in the inheritance chain for your view model, and assume that a method which accepts a single string parameter, returns void and calls **PropertyChangedEventHandler.Invoke** somewhere in the method body is indeed a notification method.

```csharp
private static MethodDefinition FindNotificationMethod(
  TypeDefinition typeDef, 
  bool includePrivateMethods = true)
{
  foreach (var m in typeDef.Methods.Where(m => includePrivateMethods 
    || m.Attributes.HasFlag(MethodAttributes.Public)))
  {
    if (IsProbableNotificationMethod(m))
    {
      return m;
    }
  }
  var baseTypeRef = typeDef.BaseType;
  if (baseTypeRef.FullName != "System.Object")
  {
    return FindNotificationMethod(baseTypeRef.Resolve(), false);
  } 
  return null;
}

private static bool IsProbableNotificationMethod(
  MethodDefinition methodDef)
{
  return methodDef.HasBody 
   && IsProbableNotificationMethodWithBody(methodDef);
}

private static bool IsProbableNotificationMethodWithBody(
  MethodDefinition methodDef)
{
  foreach (var insn in methodDef.Body.Instructions)
  {
    if (insn.OpCode == OpCodes.Callvirt)
    {
      var callee = (MethodReference) insn.Operand;
      if (callee.Name == "Invoke" 
        && callee.DeclaringType.Name == "PropertyChangedEventHandler")
      {
        return true;
      }
    }    
  }
  return false;
}
```

Should Silver.Needle fail to identify an existing notification method, though, there is no problem. After all, it's perfectly OK to have more than one method that can be used to fire the event. Hence if no notification method is found, one is injected. No sleep lost.

In case no existing notification method was found, we need to provide one. We're getting used to this kind of code by now:

```csharp
private MethodDefinition CreateNotificationMethodDefinition()
{
  const string MethodName = "NotifyViewableProperty";
  var methodDef = new MethodDefinition(MethodName,
    MethodAttributes.Private |
    MethodAttributes.HideBySig,
    this._types.Void);
  var paramDef = new ParameterDefinition("propertyName", 
    ParameterAttributes.None, 
    _types.String);
  methodDef.Parameters.Add(paramDef);
  methodDef.Body.MaxStackSize = 4;
  var v = new VariableDefinition(_types.PropertyChangedEventHandler);
  methodDef.Body.Variables.Add(v);
  methodDef.Body.InitLocals = true;

  var il = methodDef.Body.GetILProcessor();
  Action<OpCode> op = x => il.Append(il.Create(x));
  // IL_0000: ldarg.0
  op(OpCodes.Ldarg_0);
  // IL_0001: ldfld class [System]System.ComponentModel.PropertyChangedEventHandler Voodoo.ViewModel.GoalViewModel::PropertyChanged
  var eventHandlerFieldDef = FindEventFieldDeclaration(_typeDef);
  il.Append(il.Create(OpCodes.Ldfld, eventHandlerFieldDef));
  // IL_0006: stloc.0
  op(OpCodes.Stloc_0);
  // IL_0007: ldloc.0
  op(OpCodes.Ldloc_0);
  //IL_0008: brfalse.s IL_0017
  var jumpTargetInsn = il.Create(OpCodes.Ret); // See below, IL_0017
  il.Append(il.Create(OpCodes.Brfalse_S, jumpTargetInsn));
  // IL_000a: ldloc.0
  op(OpCodes.Ldloc_0);
  // IL_000b: ldarg.0
  op(OpCodes.Ldarg_0);
  // IL_000c: ldarg.1
  op(OpCodes.Ldarg_1);
  // IL_000d: newobj instance void [System]System.ComponentModel.PropertyChangedEventArgs::.ctor(string)
  var eventArgsTypeRef = _types.PropertyChangedEventArgs;
  var ctorRef = new MethodReference(".ctor", 
    _types.Void, 
    eventArgsTypeRef);
  var ctorParamDef = new ParameterDefinition("propertyName", 
    ParameterAttributes.None, 
    _types.String);
  ctorRef.Parameters.Add(ctorParamDef);
  ctorRef.HasThis = true;
  il.Append(il.Create(OpCodes.Newobj, ctorRef));
  // IL_0012: callvirt instance void [System]System.ComponentModel.PropertyChangedEventHandler::Invoke(object, class [System]System.ComponentModel.PropertyChangedEventArgs)
  var invokeMethodRef = new MethodReference("Invoke", 
    _types.Void, 
    _types.PropertyChangedEventHandler);
  invokeMethodRef.Parameters.Add(
    new ParameterDefinition(_types.Object));
  invokeMethodRef.Parameters.Add(
    new ParameterDefinition(eventArgsTypeRef));
  invokeMethodRef.HasThis = true;
  il.Append(il.Create(OpCodes.Callvirt, invokeMethodRef));
  // IL_0017: ret
  il.Append(jumpTargetInsn);
  return methodDef;
}
```

This produces IL for a **NotifyViewableProperty** method just like the one we wrote in C# in the "hand-implemented" **PersonViewModel** above.

## Injecting notification

With the interface implementation and notification method in place, we finally come to the fun part – injecting the property notification itself!

Unless you're the kind of person who use [ILSpy](http://wiki.sharpdevelop.net/ilspy.ashx) or [ILDasm](http://msdn.microsoft.com/en-us/library/aa309387%28v=vs.71%29.aspx) regularly, you might wonder if and how it will work with auto-properties – properties where you don't actually provide any body for the getters and setters. Well, it doesn't matter. Auto-properties are a C# feature, they don't exist in IL. So you'll find there's a backing field there (albeit with a weird name) that the C# compiler conjured up for you. It's just syntactic sugar to reduce typing.

What about get-only properties? That is, properties that have getters but no setters? Well, first of all, can they change? Even if they're get-only? Sure they can. Say you have a property which derives its value from another property. For instance, you might have an **Age** property which depends upon a **BirthDate** property, like so:

```csharp
private DateTime _birthDate;

public DateTime BirthDate
{
  get { return _birthDate; }
  set { _birthDate = value; }
}

[Viewable]
public int Age
{ 
  get { return DateTime.Now.Years – BirthDate.Years; }
}
```

In the (admittedly unlikely) scenario that the **BirthDate** changes, the **Age** will change too. And if **Age** is a property on a view model that a view will bind to, you'll want any display of **Age** to update itself automatically whenever **BirthDate** changes. How can we do that? Well, if we implemented this by hand, we could add a notification call in **BirthDate**‘s setter to say that **Age** changed.

```csharp
private DateTime _birthDate;

public DateTime BirthDate
{
    get { return _birthDate; }
    set 
    { 
        _birthDate = value;
        Notify("Age");
    }
}
```

It feels a little iffy, since it sort of goes the wrong way – the observed knowing about the observer rather than the other way around. But that's how you'd do it.

Silver.Needle does the same thing for you automatically. That is, for get-only properties, Silver.Needle will inspect the getter to find any calls to getters on other properties on the same object instance. If those properties turn out to have setters, notifications to update the get-only property will be injected there. If those properties are get-only too, the process repeats itself recursively. So you could have chains of properties that depend on properties that depend on properties etc.

To do this correctly, the injection process has two steps. First, we identify which properties depend on which, second, we do the actual IL manipulation to insert the notification calls.

So, first we identify dependencies between properties. In the normal case of a property with a setter of its own, the property will simply depend on itself. (Of course, there might be other properties that depend on it as well.) So for each property with a setter, we build a list of dependent properties – that is, properties that we need to inject notification calls for. Note that while we only do notification for properties tagged as **Viewable**, we might inject notification calls into the setters of any property on the view model, **Viewable** or not. (In the example above, you'll notice that **BirthDate** is not, in fact, tagged **Viewable**. When the setter is called, it will announce that **Age** changed, but not itself!)

The code to register the dependencies between properties is as follows:

```csharp
private void HandlePropertyToNotify(PropertyDefinition prop)
{
  foreach (var affector in FindAffectingProperties(prop, new List<string>()))
  {
    AddDependency(affector, prop);
  }
}

private void AddDependency(PropertyDefinition key, 
  PropertyDefinition value)
{
  if (!_map.ContainsKey(key))
  {
    _map[key] = new List<PropertyDefinition>();
  }
  _map[key].Add(value);
}

private List<PropertyDefinition> FindAffectingProperties(
  PropertyDefinition prop, 
  IList<string> seen)
{
  if (seen.Any(n => n == prop.Name))
  {
    return new List<PropertyDefinition>();    
  }
  seen.Add(prop.Name);
  if (prop.SetMethod != null)
  {
    return new List<PropertyDefinition> {prop};
  }
  if (prop.GetMethod != null)
  {
    return FindAffectingPropertiesFromGetter(prop.GetMethod, seen);
  }
  return new List<PropertyDefinition>();
}

private List<PropertyDefinition> FindAffectingPropertiesFromGetter(
  MethodDefinition getter, 
  IList<string> seen)
{
  var result = new List<PropertyDefinition>();
  foreach (var insn in getter.Body.Instructions)
  {
    if (insn.OpCode == OpCodes.Call)
    {
       var methodRef = (MethodReference)insn.Operand;
       if (methodRef.Name.StartsWith(PropertyGetterPrefix))
       {
         // Found an affecting getter inside the current getter!
         // Get list of dependencies from this getter.
         string affectingPropName = methodRef.Name
           .Substring(PropertyGetterPrefix.Length);
         var affectingProp = _typeDef.Properties
           .FirstOrDefault(p => p.Name == affectingPropName);
         if (affectingProp != null)
         {
           result.AddRange(FindAffectingProperties(affectingProp, seen));
         }
       }
     }
  }
  return result;
}
```

So you can see that it's a recursive process to walk the dependency graph for a get-only property. You'll notice that there is some code there to recognize that we've seen a certain property before, to avoid infinite loops when walking the graph. Of course, it might happen that we don't find any setters to inject notification into. For instance, it may turn out that a viewable property actually depends on constant values only. In that case, Silver.Needle will simply give up, since there is no place to inject the notification.

When we have the complete list of properties and dependant properties, we can do the actual IL manipulation. That is, for each affecting property, we can inject notifications for all affected properties.

There are two possible strategies for the injection itself: simple and sophisticated. The simple strategy employed by Silver.Needle is to do notification regardless of whether any state change occurs as a result of calling the property setter. For instance, you might have some guard clause deciding whether or not to actually update the field backing the property – a conditional setter if you will. Perhaps you want to write to the backing field only when the value has actually changed. Silver.Needle doesn't care about that. If the setter is called the view is notified. I believe this makes sense, since the setter is the abstraction boundary for the operation you're performing, not whatever backing field you may or may not write to. Also, I reckon that it doesn't *hurt* much to do a couple of superfluous view refreshes.

It would be entirely possible to do something a little bit more sophisticated, though – I just don't think it's worth the effort (plus it violates encapsulation, doesn't it?). If we wanted to, we could use a simple program analysis to distinguish between paths that may or may not result in the view model altering state. Technically, we could take the presence of a **stfld** IL instruction (which stores a value to a field) as evidence for state change. We could even throw in a little bit of data flow analysis to see if the value passed to the setter was actually on the stack when to the **stfld** was executed. In that case, we'd interpret "property change" to mean "some field acquires the value passed to the setter", which may or may not seem right to you. So it could be done, within reason.

Notice, though, the appeal to reason. It's easy to come up with a setter which results in an observable state change without ever calling **stfld**. For instance, you could push the value onto a stack instead of storing it in a field, and have the getter return the top element of the stack. Sort of contrived, but it could be done. Or you could pass the value to some method, which may or may not store it somewhere. So you see, it's hard to do properly in the general case. Hence Silver.Needle keeps things simple, and says that the view should be notified of property change whenever the setter is called. That way, we might do a couple of superfluous notifications, but at least we don't miss any.

Now we just need to figure out where to inject the notification calls. Obviously it needs to be the last thing you do in the setter, to ensure that any state change has actually occurred before we do the notification (otherwise we'd refresh the view to show a stale property value!). That's easy if you have a single return point from your setter, somewhat harder if there are are several.

You could of course inject notification calls before each return point. That would give the correct semantics but is a bit brutish and not particularly elegant. Instead, Silver.Needle will essentially perform an extract method refactoring if there is more than one return point. The original body of the property setter is moved to a new method with a name derived from the property name. The property setter is then given a new body, consisting of a call to the new method, followed by necessary notification calls. Nice and tidy.

A third alternative would be to wrap the body of the setter in a try block and perform notification in a finally block. Yes, that would mean that notifications would be given even if an exception is thrown during the execution of the setter. Would that be a problem? No. Why not? Because you shouldn't throw exceptions in your setters. Again, if you have complex logic in the setters of your view models, I have a meme for you: "View models, you're doing it wrong".

So, implementation-wise, we need to support two scenarios: with or without refactoring. In either case, we end up with a setter that has a single return point preceeded by notification calls. As usual, it's pretty straight-forward to do the necessary alternations to the body of the setter using Mono.Cecil. Here's the code:

```csharp
private void InjectNotification(MethodDefinition methodDef, 
  IEnumerable<string> propNames)
{
  if (_notifyMethodDef == null || methodDef == null)
  {
    return;
  }
  if (HasMultipleReturnPoints(methodDef))
  {
    RefactorSetterAndInjectNotification(methodDef, propNames);
  }
  else
  {
    InjectNotificationDirectly(methodDef, propNames);
  }
}

private bool HasMultipleReturnPoints(MethodDefinition methodDef)
{
  return methodDef.Body.Instructions.Count(
    insn => insn.OpCode == OpCodes.Ret) > 1;
}

private void RefactorSetterAndInjectNotification(
  MethodDefinition oldMethodDef, 
  IEnumerable<string> propNames)
{
  var methodName = "Refactored" + oldMethodDef.Name
    .Substring(PropertySetterPrefix.Length) + "Setter";
  var methodDef = new MethodDefinition(methodName,
    oldMethodDef.Attributes, 
    oldMethodDef.ReturnType);
  foreach (var oldParamDef in oldMethodDef.Parameters)
  {
    var paramDef = new ParameterDefinition(
      oldParamDef.Name, 
      oldParamDef.Attributes, 
      oldParamDef.ParameterType);
    methodDef.Parameters.Add(paramDef);
  }
  methodDef.Body = oldMethodDef.Body;
  _typeDef.Methods.Add(methodDef);

  oldMethodDef.Body = new MethodBody(oldMethodDef);
  var il = oldMethodDef.Body.GetILProcessor();
  Action<OpCode> op = x => il.Append(il.Create(x));
  op(OpCodes.Ldarg_0);
  op(OpCodes.Ldarg_1);
  il.Append(il.Create(OpCodes.Call, methodDef));
  op(OpCodes.Ret);

  InjectNotificationDirectly(oldMethodDef, propNames);
}

private void InjectNotificationDirectly(MethodDefinition methodDef, 
  IEnumerable<string> propNames)
{
  var il = methodDef.Body.GetILProcessor();
  var returnInsn = il.Body.Instructions.Last();
  foreach (var s in propNames)
  {
    var loadThis = il.Create(OpCodes.Ldarg_0);
    var loadString = il.Create(OpCodes.Ldstr, s);
    var callMethod = il.Create(OpCodes.Call, _notifyMethodDef);
    il.InsertBefore(returnInsn, loadThis);
    il.InsertBefore(returnInsn, loadString);
    il.InsertBefore(returnInsn, callMethod);
  }
}
```

The code isn't too complicated. The **MethodDefinition** passed to **InjectionNotification** is the setter method for the property, and **propNames** contains the names of properties to notify change for when the setter is called. In case of multiple return points from the setter, we perform a bit of crude surgery to separate the method body from the method declaration. We provide a new method definition for the body, with a name derived from the name of the property. While in Dr Frankenstein mode, we proceed to assemble a new method body for the setter. That body consists of three instructions: push the **this** reference onto the stack, push the value passed to the setter onto the stack, and invoke the new method we just created out of the original method body.

Now we know that the setter has a single return point, and we can inject the notification calls. We just need to loop over the properties to notify, and inject a trio of 1) push **this**, 2) push property name and 3) invoke notification method for each.

And that's it, really. We're done. Mission accomplished, view model complete.

Of course, to make things practical, you're gonna need a build task and a Visual Studio template as well. I'll get to that some day.
