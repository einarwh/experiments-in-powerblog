:page/title Shrink-wrapped Mkay
:blog-post/tags [:tech :programming :dotnet :aspnetmvc :dsl :javascript]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2013-03-05T20:45:00"
:page/body

# Shrink-wrapped Mkay

<p class="blog-post-date">March 5, 2013</p>

If you've been following this blog, you'll know that there is no point in writing your own custom validation attributes for ASP.NET MVC data validation any more. Instead, you should be using [Mkay](/blog/2013/02/15/mkay-one-validation-attribute-to-rule-them-all/), which allows you to specify arbitrary validation rules for your models in a LISP-like syntax. And now you can actually do it too, since I've packaged everything nicely in a [nuget package](https://www.nuget.org/packages/Mkay).

When you install the nuget package in your ASP.NET MVC Application, you'll find that a few things are added. First, there's a reference to _Mkay.dll_, which contains the **Mkay** attribute as well as the code that is executed on the server. Second, in your _Scripts_ folder, you'll find three JavaScript files: _mkay-validation.js_ (which contains the runtime for the client-side validation in mkay), _mkay-parser.js_ (which is there to support the implementation of [eval](/blog/2013/02/22/self-referential-validation-in-mkay/)) and _mkay-jquery.js_ (which hooks up the mkay runtime with unobtrusive jQuery validation). Third, there's a code file in the _App_Start_ folder called _MkayBoot.cs_. You may recall that the **Mkay** attribute must be associated with its own data validator class, in order to obtain the name of the property being validated. This happens inside the **Kick** method in the **MkayBoot** class. That method is invoked by means of [WebActivator](https://github.com/davidebbo/WebActivator) voodoo some time right after **Application_Start** in _global.asax_ has been invoked. That way, you don't have to bother with that yourself. For convenience, the **Kick** method also creates a so-called bundle of the Mkay JavaScript files.

Of course, you must remember to reference the Mkay JavaScript bundle in your view somehow, as well as the jQuery validation bundle. You might want to add them to the layout used by your view, for instance. Here's an example:

```csharp
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width" />
    <title>@ViewBag.Title</title>
    @Styles.Render("~/Content/css")
    @Scripts.Render("~/bundles/modernizr")
    @Scripts.Render("~/bundles/jquery")
    @Scripts.Render("~/bundles/jqueryval")
    @Scripts.Render("~/bundles/mkay")
</head>
<body>
    @RenderBody()
    @RenderSection("scripts", required: false)
</body>
</html>
```

And then you can start using Mkay for your own validation needs. Wee! World domination!

