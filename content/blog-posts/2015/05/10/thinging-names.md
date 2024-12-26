:page/title Thinging names
:blog-post/tags [:tech :software-development :modelling]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2015-05-10T21:09:00"
:page/body

# Thinging names

<p class="blog-post-date">May 10, 2015</p>

The other night I made a tweet. It was this:

> Programmers are always chasing proximate causes. This is why naming things is considered hard, not finding the right abstractions to name.

And I meant something by that, but what? I got some responses that indicated that some people interpreted it differently than I intended, so evidently it's not crystal clear. I can see why, too. Like most tweets, it is lacking in at least two ways: it lacks context and it lacks precision. (Incidentally this is why I write "I made a tweet", much like I'd write "I made a mistake".) Of course, tweets are prone to these shortcomings, and it takes special talent and a gift for brevity to avoid them. Alas, as the poor reader may have noticed, it is a gift I don't possess - that much is evident from this paragraph alone!

Therefore, I'm making this attempt at a long-winded deliberation of what I tried to express - that should better suit my talents. It turns out I was even stupid enough to try to say two or even three things at once, which is surely hubris and the death of pithy tweets. First, I was trying to make a rather bold general claim about programmers: that we tend to chase proximate causes rather than ultimate ones. Second, I said that programmers often talk about how hard a problem _naming things_ is, but that instead we should be worried about choosing the appropriate abstractions to name in the first place. And third, I implied that the latter is a particular instance of the former.

So, let's see if I can clarify and justify what I mean by all these things.

A bit of context first - where does this come from? I've been increasingly preoccupied with domain modelling lately, so the tweet ideally should be interpreted with that in mind. I'm absolutely convinced that the only way we can succeed with non-trivial software projects is by working domain-driven. The work we do must reflect insight that we arrive at by talking to users and domain experts and thinking really hard about the problem domain. Otherwise we go blind - and although we might be going at high velocity, we'll quite simply miss our target and get lost. In the words of Eric Evans, we need to do _knowledge crunching_ to develop a _deep model_ and keep _refactoring towards greater insight_ to ensure that the software 1) solves the current problem and 2) can co-evolve with the business. This is the primary concern. Everything else is secondary, including all the so-called "best practices" you might be employing. Kudos to you, but your craftmanship really is nothing unless it's applied to the domain.

I think that many of the things we struggle with as programmers are ultimately caused by inadequate domain modelling. Unfortunately, we're not very good at admitting that to ourselves. Instead, we double and triple our efforts at chasing proximate causes. We keep our code squeaky clean. We do TDD. We program to interfaces and inject our dependencies. This is all very well and good, but it has limited effect, because we're treating the symptoms rather than curing the disease. In fact, I've made drive-by tweets about SOLID (with similar lack of context and precision) that hint at the same thing. Why? Because I think that SOLID is insufficient to ensure a sensible design. It's not that SOLID is bad advice, it's just that it deals with secondary rather than primary causes and hence has too little leverage to fix the issues that matter. Even if you assume that SOLID will expose all your modelling inadequacies as design smells and implementation pains, fixing the problem that way is inefficient at best.

So that was a bit of context. Now for precision. The term "naming things" is incredible vague in and of itself, so to make any sense of the tweet, I should qualify what I meant by that. The term stems from a famous quote by Phil Karlton, which goes like this:

> There are only two hard problems in Computer Science: cache invalidation and naming things.

Unfortunately I don't know much about Phil Karlton, except that he was at Netscape when Netscape mattered, and that he obviously had the gift of brevity that I lack.

What are "things" though? I don't know what Phil Karlton had in mind, but for the purposes of my tweet, I was thinking about "code things", things like classes and methods. Naming such things appropriately is important, since we rely on those names when we abstract away from the details of implementation. But it shouldn't be _hard_ to name them! If it is hard, it is because we're doing something wrong - it is a symptom that the very thing we're naming has problems and should probably not exist. This is why I think that addressing the "naming things" problem is dealing with proximate causes. Naming things is hard because of the ultimate problem of inadequate domain modelling.

Of course it is quite possible to think of different "things" when you speak of "naming things", in particular "things" in the domain. In that case, tackling the problem of naming things really _is_ dealing with ultimate causes! This is the most important activity in domain-driven design! And with that interpretation, my tweet is completely nonsensical, since naming things in the domain and finding the right domain abstractions become one and the same. (Ironically, this all goes to show that "naming things" interpreted as "describing things with words" certainly is problematic!)

So where does this leave us? To summarize, I think that domain names should precede code things. This is really just another way of stating that we need the model to drive the implementation. We should concentrate our effort on coming up with the right concepts to embody in code, rather than writing chunks code and coming up with names for them afterwards. Making things from names ("thinging names") is easy. Making names from things ("naming things"), however, can be hard.
