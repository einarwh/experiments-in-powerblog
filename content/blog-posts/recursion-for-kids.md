:page/title Recursion for kids
:blog-post/tags [:tech :programming :math :recursion :csharp]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2012-01-19T14:15:00"
:page/body

# Recursion for kids

Posted: January 19, 2012

Consider the following problem:

> The field vole can have up to 18 litters (batches of offspring) each year, each litter contains up to 8 children. The newborn voles may have offspring of their own after 25 days. How many field voles can a family grow to during the course of a year?

Of course, unless you're a native English speaker, you might wonder what the heck a field vole is. I know I did.

This a field vole:

![A field vole up close](/images/field-vole.jpg)

I'm not really sure if it's _technically_ a mouse or just a really close relative, but for all our intents and purposes, it sure is. A small, very reproductive mouse.

So, do you have an answer to the problem? _No_?

To provide a bit of background: this problem was presented to a class of fifth graders. Does that motivate you? Do you have an answer now?

If you do, that's great, but if you don't, you probably have a whole litter of questions instead. That's OK too.

You see, the father of one of those fifth graders is a friend of mine. He emailed this problem to a rather eclectic group of people (including some with PhDs in matematics). Between us, we came up with a list of questions including these:

* What is the distribution of sexes among the voles?
* What is the average number of voles per litter? And the distribution?
* How many voles are gay?
* How many voles die before they reach a fertile age?
* How many voles are celibate? Alternatively, how many voles prefer to live without offspring? (Given that voles don't use prophylactics, these questions yield equivalent results.)
* Will ugly voles get laid?
* What is the cheese supply like?
* Are there cats in the vicinity?

And so on and so forth. Luckily, the fifth grade teacher was able to come up with some constraints for us. Of course, they were rather arbitrary, but perhaps not completely unreasonable:

> Each litter contains exactly 8 new voles, 4 females and 4 males. No voles die during the year in question.

That's great! Given these constraints, we can get to work on a solution.

First, we make the arbitrary choice of associating the offspring with the female voles only. The male voles will be counted as having no offspring at all. While perhaps a bit old fashioned, this greatly simplifies our task. (Of course, we could just as well have opted for the opposite.)

Now we just need to count the offspring of female voles. Since we know that the offspring function is purely deterministic, this isn't too hard. Given a certain number of days available for reproduction, a female vole we will always yield the same number of offspring. (As if women were idempotent!)

To calculate an answer, we can write a small program.

```csharp
public class Voles 
{
  private static int _daysBeforeFirst = 25;
  private static int _daysBetween = 20;
 
  private static Dictionary<int, long> _cache = 
    new Dictionary<int, long>();
  
  public static long F(int days) {
    if (!_cache.ContainsKey(days)) {
      _cache[days] = F0(days);
    }
    return _cache[days];
  }

  private static long F0(int days) {
    int end = days - _daysBeforeFirst;
    if (end < 0) {
      return 1;
    }
    int start = end % _daysBetween;
    long count = 0;
    for (int d = start; d <= end; d += _daysBetween) {
      count += F(d) + 1;
    }
    return 1 + 4 * count;
  }
}
```

The **F** method calculates the total number of offspring for a female vole as a function of how many days it has lived. If you call **F** with an input of **365** days, you'll find that the answer is **55,784,398,225**. That's a lot of voles.

How does the algorithm work, though? Well, we assume that we start with a single newborn female vole that has 365 days available to produce offspring (with the first litter arriving after 25 days). Then the number of offspring is given by:

```
F(365) = 1 + 4 * F(340) + 4 + 4 * F(320) + 4 + … + 4 * F(0) + 4
```

Of course, you can factor out all the 4's, like so:

```
F(365) = 1 + 4 * (F(340) + 1 + F(320 + 1 + … + F(0) + 1)
```

And that's pretty much what the code does. In addition, it uses a cache, so that it won't have to calculate a value twice.

As you might imagine, the kids weren't really expected to come up with a solution to this problem. Instead, they were supposed to think about recursion and reasonable constraints. Which are noble things to teach kids, for sure. More of that, please.

Nevertheless, I still think the problem kinda sucked. Even if the kids were able to come up with reasonable constraints, they wouldn't have the tools at hand to produce an answer. Pretty demotivating, I'd say.

My friend's son was unfazed and cool about it, though. In fact, he was content and confident that the tree structure he started drawing would yield the correct answer, if only he had a sufficiently large piece of paper. How cool is that?
