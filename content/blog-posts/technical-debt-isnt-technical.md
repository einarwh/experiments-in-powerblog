:page/title Technical debt isn't technical
:blog-post/tags [:tech :technical-debt :software-development]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2015-12-05T22:29:00"
:page/body

# Technical debt isn't technical

Posted: December 5, 2015

## TL;DR

_Technical debt_ is not primarily caused by clumsy programming, it is a third-order effect of poor communication. Technical debt is a symptom of an underlying lack of appropriate abstractions, which in turn stems from insufficient modelling of the problem domain. This means that necessary communication has not taken place: discussions and decisions to resolve ambiguity and make informed trade-offs have been swept under the rug. Technical debt is the reification of this lack of resolution in code.

## The technical debt meme

For a while now, I've been wanting to write about _technical debt_. As we all know, technical debt is a very successful meme in software development - it needs no introduction as a concept. Like any good virus, it has self-replicated and spread throughout the software development world, even reaching into the minds of project leaders and stakeholders. This is good, since the notion of technical debt brings attention to the fact that the internal quality of software matters - that there are aspects of software that are invisible to anyone but the programmers, but still have very visible effects - in the form of prolonged quality problems, missed deadlines, development grinding to a halt and so forth. For this reason, we should tip our hats to [Ward Cunningham](https://en.wikipedia.org/wiki/Ward_Cunningham) for coming up with the term. It gives us terminology that allows us to communicate better with non-technical stakeholders in software projects.

## Why technical debt is a misnomer

That's not what I want to write about however. What I want to say is that technical debt is also a deeply problematic notion, because it speaks little of the causes of technical debt, or how to fix them.

The usual story is that technical debt stems from project deadlines. If the code is inadequate, sloppy or otherwise "bad", it has probably been written in a hurry because the project leader said so. This indicates that _time_ is the cause of our problems, and also conveniently places the responsibility of the mess on someone else than the developers.

This is certainly true in some cases; we have all written code like that, and for those exact reasons. I just don't think it's the whole story, or even a major part of it. It seems to me entirely inadequate to explain the majority of technical debt that I've seen on software projects. The so-called technical problems go much deeper than mere sloppiness of implementation, and reveal fundamental problems in the process of understanding of the business domain and how that understanding is captured and represented in software. In particular, it is very common to see weak abstractions that fail to represent the richness of the domain. The code tends to be overgrown with conditional and flags, which indicates a weak model that has handled evolution and change very poorly - by ad hoc spouting of extra branches and the booleans needed to navigate them as appropriate for different use cases.  Complexity grows like ever new epicycles on the inadequate model - easily recognizable as things in your code that cannot be given meaningful names because they have no meaningful counterpart in the problem domain. The end result is a horrific steampunk contraption of accidental complexity.

This makes the code extraordinarily difficult to reason about. Hence, it would seem that the so-called _technical debt_ really stems from _modelling debt_; the code lacks the higher-level concepts of a rich domain model that would make it possible to express the use cases more directly.

> The currency of technical debt is knowledge. 
>
>— @sarahmei

In DDD terms, modelling debt indicates that insufficient [knowledge crunching](http://www.informit.com/articles/article.aspx?p=102604&seqNum=2) has taken place. Knowledge crunching involves learning about the problem domain and capturing that knowledge in a suitable domain model. This is a communication-driven process that involves identifying and resolving ambiguity in the problem domain, and expressing the domain as clearly as possible. Most of all, it is a chaotic and messy process that involves people and discussion. Insufficient knowledge crunching in turn points towards the ultimate cause of technical debt: poor communication.

> Communication is the principal portion of the "technical debt." Messy code is just the ever-increasing interest. 
>
> — @nycplayer

## Why technical debt is misrepresented

So if technical debt isn't really technical - or at least not ultimately caused by technical issues - why do we keep referring to it as technical debt? Unfortunately, it seems to me that developers have a tendency to look for technical solutions to soft problems.

Technical tasks are alluring because, unlike modelling and communication, they have no psychological dimensions, and tend not to lead to conflict. I don't want to add to the stereotype of the programmer as particularly socially inept; suffice it to say that most people will prefer to avoid conflict if possible. Technical work is a series of puzzles to be solved. Modelling work uncovers human issues, differences of opinion, different focus, different hopes for the application, even personal conflicts. Figuring out what the application should do exposes all of these issues, and it is painful! This is why everyone is quoting [Conway's Law](https://en.wikipedia.org/wiki/Conway's_law) these days.

And so, even as widespread as the meme technical debt is, it seems to be poorly understood, even by developers - perhaps even particularly by developers! Indeed, [the Wikipedia page](https://en.wikipedia.org/wiki/Technical_debt) for technical debt - no doubt authored by developers - currently lists 11 causes of technical debt, but lack of understanding of the problem domain is not one of them! "Lack of knowledge" sounds promising, until you read the explanation: "when the developer simply doesn't know how to write elegant code"(!)

Again we see the focus on the technical aspects, as if technical debt were caused by clumsy, unskilled programmers with nagging, incompetent project leaders - and hence as if it were fixable by some virtuous programmer - a master craftsman, no less! - using generic, context-free principles like SOLID, dependency injection and patterns. It is not! Code hygiene is certainly a virtue, but it is no substitute for modelling, just like frantically washing your hands is not sufficient for successful surgery. Getting lost in code hygiene discussions is like arguing about the optimal kinds of soap and water temperature while the patient is dying on the operating table.

And yet it is _indirectly_ true: a developer who doesn't know the importance of understanding the problem domain, of proper modelling, will certainly fail to write elegant code. Elegance of the implementation can only stem from an elegant model that reflects a deep understanding of the problem addressed.

## Paying off debt

This has deep ramifications, in particular in how we address technical debt. _Refactoring_ is another successful meme in software development, and we often use it to describe the process of paying off technical debt. But if technical debt isn't just clumsy code, if instead it is clumsy code caused by unresolved ambiguity in the problem domain, then it is poorly addressed by rearranging code. We need to start in the other end, with a better understanding of the problem we are trying to solve, and with modelling concepts permeating the code instead of branches and booleans. This is what Eric Evans calls "refactoring towards deeper insight". Unless we have a model to drive our efforts, there is no reason to believe that we will be able to do much better than before. Refactoring without an improved domain model is just hubris.

> A rewrite will end up with the same problems as the original unless you close the understanding gap. 
>
> — @sarahmei

## To conclude

That's what I wanted to say about technical debt. It's not very technical at all. It's about code that gets bad because humans fail to communicate when trying to solve problems in some business domain using software. It usually is.
