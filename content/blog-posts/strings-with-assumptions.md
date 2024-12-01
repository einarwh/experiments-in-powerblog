:page/title Strings with assumptions
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2016-01-29T12:00:00"
:page/body

# Strings with assumptions

Posted: January 29, 2016

_TL;DR Strings always come with strings attached._

I had a little rant about strings on Twitter the other day. It started like this:

> I hate string.
> 
> — Einar W. Høst (@einarwh) January 28, 2016

This blog post is essentially the same rant, with a bit of extra cheese.

Here’s the thing: I find that most code bases I encounter are unapologetically littered with strings. Strings are used to hold values of all kinds of kinds, from customer names to phone numbers to XML and JSON structures and what have you. As such, strings are incredibly versatile and flexible; properties we tend to think of as positive when we talk about code. So why do I hate strings?

Well, the problem is that we don’t want our types to be flexible like that – as in “accepting of all values”. In fact, the whole point of types is to avoid this flexibility! Types are about restricting the number of possible values in your program, to make it easier to reason about. You want to allow exactly the legal values, and to forbid all the illegal values. String restricts nothing! String is essentially object! But people who have the decency to refrain from using object will still gladly use string all over the place. It’s weird. And dangerous. That’s why we should never give in to the temptation to escape from the type system by submerging our values in the untyped sea of string. When that value resurfaces sometime later on, we’ll effectively be attempting a downcast from object back to the actual type. Will it succeed? Let’s hope so!

So to be very explicit about it: if you have a string in your program, it could be anything – anything! You’re communicating to the computer that you’re willing to accept any and all of the following fine string specimen as data in your program:

```
Hello World

(╯°□°）╯︵ ┻━┻

να ζει κανείς ή να μην ζει

{ "items": [ {"key1": "val1"}, {"key2": "val2"} ] }

'';!–"<XSS>=&{()}

Robert'); DROP TABLE STUDENTS; —

448094;FL;CLAY COUNTY;1322376.3

— !amplecode.com/^post
postid: 1338
date:   2016-01-29
author: &id001
   given  : Einar
   family : H\u0184st

<?php $s='fiddlesticks'; ?>
<html>
  <body>
    <script type="text/javascript">  
      alert("<?php echo $s; ?>");
    </script>
  </body>
</html>
```

Your program does not distinguish between them, they’re all equally good. When you declare a string in your program, you’re literally saying that I’m willing to accept – I’m expecting! – any and all of those as a value. (I should point out that you’re expecting very _big_ strings too, but I didn’t feel like putting any of them in here, because they’re so unwieldy. Not to mention the door is open to that mirage doppelganger of a string, **null**, as well – but that’s a general problem, not limited to string.)

Of course, we never want this. This is never what the programmer intends. Instead, the programmer has assumptions in their head, that the string value should really be drawn from a very small subset of the entire domain of strings, a subset that fits the programmer’s purpose. Common assumptions include “not terribly big”, “as large as names get”, “reasonable”, “benign”, “as big as the input field in the form that should provide the value”, “similar to values we’ve seen before”, “some format parsable as a date”, “a number”, “fits the limit of the database column that’s used to persist the value”, “well-formed XML”, “matching some regular expression pattern or other” and so on and so forth. I’m sure you can come up with some additional ones as well.

The assumptions might not be explicitly articulated anywhere, but they’re still there. If the assumptions are implicit, what we have is basically a modelling issue that the programmer hasn’t bothered to tackle explicitly yet. It is modelling debt. So whenever you see string in a program, you’re really seeing “string with assumptions”, with the caveats that the assumptions may not be terribly well defined and there may or may not be attempts to have them enforced. In other words, we can’t trust that the assumptions hold. This is a problem.

So what should we do instead? We can’t realistically eradicate strings from our programs altogether. For instance, we do need to be able to speak string at the edges of our programs. Quite often, we need to use strings to exchange data with others, or to persist values in a database. This is fine. But we can minimize the time we allow strings to be “raw”, without enforced assumptions. As soon as we can, we should make our assumptions explicit – even though that means we might need to spend a little time articulating and modelling those assumptions. (That’s a bonus by the way, not a drawback.) We should never allow a string pass unchecked through any part of our system. An unchecked string is Schrödinger’s time bomb. You don’t know if it will explode or not until you try to use it. If it turns out your string is a bomb, the impact may vary from the inconvenient to the embarrassing to the catastrophic.

Unsurprisingly, the good people who care about security (which should be all of us!) find strings with assumptions particularly interesting. Why? Because security bugs can be found precisely where assumptions are broken. In particular, since the **string** type allows for any string, the scene is set for “Houdini strings” to try to escape the cage where they’re held as data, and break free into the realm of code.

To make our assumptions explicit, we need to use types that are not strings. But it’s perfectly fine for them to carry strings along. Here’s a class to represent a phone number in C#:

```csharp
public sealed class PhoneNumber
{
  private readonly string _;

  public PhoneNumber(string s) 
  {
    if (!ValidPhoneNumber(s))
    {
      throw new ArgumentException("Not a valid phone number");
    }

    _ = s;
  }

  public override bool Equals(object that)
  {
    return 
      that != null &&
      this.GetType() == that.GetType() &&
      this.ToString().Equals(that.ToString());
  }

  public override int GetHashCode()
  {
    return _.GetHashCode();
  }

  public override string ToString()
  {
    return _;
  }
}
```

Nothing clever, perfectly mundane. You create your **PhoneNumber** and use it whenever you’d use “string with assumption: valid phone number”. As you can see, the class does nothing more than hold on to a string value, but it does make sure that the string belongs to that small subset of strings that happen to be valid phone numbers as well. It will reject all the other strings. When you need to speak string (at the edges of your program, you just never do it internally), you call **ToString()** and shed the protection of your type – but at least at that point you know you have a valid phone number.

So it’s not difficult. So why do we keep littering our programs with strings with assumptions?
