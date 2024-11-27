:page/title Donkey code
:blog-post/tags [:software-design :modelling :domain-driven-design]
:blog-post/author {:person/id :einarwh}
:page/body

# Donkey code

Posted: January 21, 2017

_This is an attempt to write down a very simple example I’ve been using to explain the profound impact the language we use has on thought, discussion and ultimately code._

Imagine you have a computer system, and that you’re one of the programmers working on that system (not too hard, is it?). The system is called, oh I don’t know, eQuest. It has to do with horses. So it typically works with entities of this kind:

![Horse](/images/horse-only.png)

eQuest is a tremendous success for whatever reason, perhaps there’s very little competition. But it is a success, and so it’s evolving, and one day your product owner comes up with the idea to expand to handle entities of this kind as well:

![Donkey](/images/donkey-only.png)

It’s a new kind of horse! It’s mostly like the other horses and so lots of functionality can easily be reused. However, it has some special characteristics, and must be treated a little differently in some respects. Physically it is quite short, but very strong. Behaviour-wise, it is known to be stubborn, intelligent and not easily startled. It’s an interesting kind of horse.  It also likes carrots a lot (but then don’t all horses?). Needless to say, there will be some adjustments to some of the modules.

Design meetings ensue, to flesh out the new functionality and figure out the correct adjustments to be made to eQuest. Discussions go pretty well. Everyone has heard of these “horses that are small and stubborn” as they’re called. (Some rumors indicate that genetically they’re not actually horses at all – apparently there are differences at the DNA level, but the real world is always riddled with such technicalities. From a pragmatic viewpoint, they’re certainly horses. Albeit short and stubborn, of course. And strong, too.) So it’s not that hard to discuss features that apply to the new kind of horse.

There is now a tendency for confusion when discussing other kinds of changes to the product, though. The unqualified term “horse” is obviously used quite a bit in all kinds of discussions, but sometimes the special short and stubborn kind is meant to be included and sometimes it is not. So to clarify, you and your co-workers find yourself saying things like “any horse” to mean the former and “regular horse”, “ordinary horse”, “old horse”, “horse-horse” or even “horse that’s not small and stubborn” to mean the latter.

To implement support for the new horse in eQuest, you need some way of distinguish between it and an ordinary horse-horse. So you add an IsShort property to your Horse data representation. That’s easy, it’s just a derived property from the Height property. No changes to the database schema or anything. In addition, you add an IsStubborn property and checkbox to the registration form for horses in eQuest. That’s a new flag in the database, but that’s OK. With that in place, you have everything you need to implement the new functionality and make the necessary adjustments otherwise.

Although much of the code applies to horses and short, stubborn horses alike, you find that the transport module, the feeding module, the training module and the breeding module all need a few adjustments, since the new horses aren’t quite like the regular horses in all respects. You need to inject little bits of logic here, split some cases in two there. It takes a few different forms, and you and your co-workers do things a bit differently. Sometimes you employ if-branches with logic that looks like this:

```csharp
if (horse.IsShort && horse.IsStubborn) {
  // Logic for the new horse case.
}
else {
  // Regular horse code here.
}
```

Other times you go fancy with LINQ:

```csharp
var newHorses = horses.Where(h => h.IsShort && h.IsStubborn);
var oldHorses = horses.Except(newHorses);
foreach (var h in newHorses) {
  // New horse logic.
}
foreach (var h in oldHorses) {
  // Old horse logic.
}
```

And that appears to work pretty well, and you go live with support for short and stubborn horses.

Next day, you have a couple of new bug reports, one in the training module and two concerning the feeding module. It turns out that some of the regular horses are short and stubborn too, so your users would register short regular horses, tick the stubborn checkbox, and erroneously get new horse logic instead of the appropriate horse-horse logic. That’s awkward, but understandable. So you call a few meetings, discuss the issue with your fellow programmers, scratch your head, talk to a UX designer and your product owner. And you figure out that not only are the new horses short and stubborn, they make a distinct sound too. They don’t neigh the way regular horses do, they hee-haw instead.

So you fix the bug. A new property on horse, Sound, with values Neigh and HeeHaw, and updates to logic as appropriate. No biggie.

In design meetings, most people still use the term “horse that’s short and stubborn” to mean the new kind of horse, even though you’re encouraging people to include the sound they make as well, or even just say “hee-hawing horse”. But apart from this nit-picking from your side, things proceed well. It appears that most bugs have been ironed out, and your product owner is happy. So happy, in fact, that there is a new meeting. eQuest is expanding further, to handle entities of this kind as well:

![Mule](/images/mule-only.png)

What is it? Well, it’s the offspring from a horse and a horse that’s short and stubborn and says hee-haw. It shares some properties with the former kind of horse and some with the latter, so obviously there will be much reuse! And a few customizations and adjustments unique for this new new kind of horse. At this point you’re getting worried. You sense there will be trouble if you can’t speak clearly about this horse, so you cry out “let’s call it a half hee-haw!” But it doesn’t catch on. Talking about things is getting a bit cumbersome.

“But at least I can still implement it,” you think for yourself. “And I can mostly guess what kind of horses the UX people are thinking about when they say ‘horse’ anyway, I’ll just map it in my head to the right thing”. You add a Sire and a Dam property to Horse. And you proceed to update existing logic and write new logic.

You now have code that looks like this:

```csharp
if (horse.IsShort && 
    horse.IsStubborn && 
    horse.Sound == Sound.HeeHaw) || 
   (horse.Sire.IsShort && 
    horse.Sire.IsStubborn && 
    horse.Sire.Sound == Sound.HeeHaw) ||
   (horse.Dam.IsShort && 
    horse.Dam.IsStubborn && 
    horse.Dam.Sound == Sound.HeeHaw)) {
  // Logic for both the new horse and the new-new horse!
}
else {
  // Really regular horse code here.
}
```

Which turns out to be wrong, since the new new horse doesn’t really neigh or hee-haw, it does something in-between. There is no word for it, so you invent one: the neigh-haw. You extend the Sound enumeration to incorporate it and fix your code.

Getting all the edge cases right takes a while. Your product owner is starting to wonder why development is slowing down, when so much of the functionality can be reused. You mumble something about technical debt. But you manage to get the bug count down to acceptable levels, much thanks to diligent testing.

At this point, there is another meeting. You are shown two photographs of horses of the newest kind. Or so you think. The product owner smiles. “They’re almost identical, but not quite!” he says. “You see, this one has a horse as a mother and a short stubborn horse as a father.” You see where this is going. “But this one, this one has a short stubborn horse as a mother and a horse as a father.” “Does it matter?” you ask. “This last one is always sterile,” he says. “So you need to handle that in the breeding module.” Oh.

“And then there’s this.”

![Zeedonk](/images/zeedonk-only.png)

_The point of this example is that it takes very little for software development to get crippled by complexity without precise language. You need words for the things you want to talk about, both in design discussions and in code. Without them, it becomes very difficult to have meaningful communication, and the inability to articulate a thought precisely is made manifest in the code itself. Your task quickly turns from implementing new functionality to making sure that you don’t break existing functionality._

_The example is special in that the missing words are sort of jumping out at you. It’s so obvious what they should be. This is not always the case. In many domains, it can be much, much harder to figure out what the words should be. It’s likely to require a lot of time and effort, and include frustrating and heated discussions with people who think differently than you. You might find that you and your team have to invent new words to represent the concepts that are evolving in your collective mind. But it can also be that you’ve all become accustomed to the set of words you’re currently using, and gone blind to the donkeys in your system._
