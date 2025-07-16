:page/title Pragmatism is poison
:blog-post/tags [:tech]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2016-03-16T07:14:00"

:blog-post/description

Appeal to pragmatism is a logical fallacy. We should make better arguments.

:page/body

# Pragmatism is poison

<p class="blog-post-date">March 10, 2016</p>

_Yesterday I gave a lightning talk called "Pragmatism is Poison" at the [Booster Conference](http://www.boosterconf.no/) in Bergen. This blog post is essentially that talk in written form._

The basic idea of the talk, and therefore of this blog post, is to launch a public attack on perhaps the most fundamental virtue of the software craftsman: pragmatism. The reason is that I think pragmatism has turned toxic, to the point where it causes more harm than good.

While I do believe that pragmatism was once a useful maxim to combat analysis paralysis and over-engineering, I also believe that the usefulness has expired. Pragmatism no longer represents a healthy attitude, in my mind it's not even a word anymore. It has degenerated into a thought-terminating cliché, which is used to stifle discussion, keep inquiries at bay and to justify not thinking things through.

These are, of course, pretty harsh words. I'll try to make my case though.

Let's start by having a look at what it means to be pragmatic. The definition Google gave me is as follows:

> Dealing with things sensibly and realistically in a way that is based on practical rather than theoretical considerations.

(A small caveat: this isn't necessarily the only or "correct" definition of what it means to be pragmatic. But I suppose it's a sensible and realistic approximation?)

Anyways: this is good by definition! How can it possibly be bad?

Well, for a whole bunch of reasons really. We can divide the problems in two parts: 1) that pragmatism lends itself to abuse because it's ambiguous and subjective, and 2) all the forms that abuse can take.

So the underlying problem is that the definition relies heavily on subjective judgment. (Consider the task of devising a test that would determine whether or not someone was being pragmatic - the very idea is absurd!) One thing is that what qualifies as "realistic" and "sensible" is clearly subjective. For any group of people, you will find varying degrees of agreement and disagreement depending on who they are and the experiences they've had. But the distinction between "practical" and "theoretical" considerations is problematic as well.

Here are some quick considerations to -uh- consider:

1. Laws of nature change
2. Bomb hits data center
3. Cloud providers go down
4. Servers are hacked
5. Unlikely timing of events

I suppose we can agree that #1 is of theoretical interest only. What about the others? If #2 happens, maybe we have more serious problems that your service going down, but it depends on how critical that service is. The same goes for #3. With respect to #4, a lot of people seem surprised when their servers are hacked - as if they hadn't thought it possible! And #5 depends on whether or not you'd like luck to be a part of your architecture. I've been on projects where people would tell me that scenarios I asked about were so unlikely that they were practically impossible. Which to me just means they'll be hard to debug and reproduce when they happen in production.

The point, though, is that the distinction between "practical" and "theoretical" is pretty much arbitrary. Where do we draw the line? Who's to say? But it's important, because mislabeling important considerations - things that affect the quality of software! - as "theoretical" leads to bad software.

So that's the underlying problem of ambiguity. On to the various forms of abuse!

The first is that pragmatism is often used to present a false dilemma in software development. We love our dichotomies! I could use cheap rhetoric to imply that it traces back to the 0s and 1s of our computers, but let's not - I have no evidence for such a claim! Luckily I don't need it either. Suffice it to say that the world is complex, and it's always very tempting to see things in black and white instead. And so we pit "pragmatism" on the one hand against "dogmatism" on the other, and it's really important to stay on the right side of that divide! Sometimes we use different words instead, like "practical" vs "theoretical" or "real world" vs "ivory tower". It all means the same thing: "good" vs "bad". Which is a big lie, because we're not making ethical judgments, we're trying to assess the pros and cons of different solutions to particular problems in concrete contexts. This isn't The Lord of the Rings.

The consequences of this polarizing are pretty severe. The false dilemma is often used as a self-defense bluff in discussions between team members. The so-called [impostor syndrome](https://en.wikipedia.org/wiki/Impostor_syndrome) is rampant in our industry, and so we reach for tools that help us deal with insecurity. One such tool is pragmatism, which can be abused as a magical spell to turn insecurity on its head.

Here's how it works. Because of the false dilemma, a claim to be pragmatic is implicitly an accusation that says that whoever disagrees is dogmatic: they're not being sensible, they're not being realistic, they're just obsessing over theoretical considerations. So while a statement like "I'm being pragmatic" sounds innocuous enough, it's really not. It leads to stupid, unrefined, pointless discussions where no knowledge is gained. Instead we're fighting over who's good and who's bad. Polarizing does not make discussions more interesting, it makes them degenerate into banality.

A related strategy is to use pragmatism as a diversion or a smoke bomb, offering the confronted part with an easy exit and effectively ending the discussion. The reason is that it takes a lot of guts and perseverance to call someone's bluff when they're claiming to be pragmatic. You might approach your co-worker with a concern like "hey, I was looking at the code, and it seems like we're blocking in our streaming API, which sort of defeats the purpose of a streaming API in the first place". It sounds like a valid concern until your co-worker says the magic words "I was just being pragmatic" and vanishes in a puff of smoke, like so:

![Magician going up in smoke](/images/puffanimate.gif)

What we should do instead is accept the complexity we're faced with and resist the urge to trivialize it. There's always a need for thinking and discussion, and spurious claims of pragmatism don't help.

Another problem with pragmatism is that it can be - and is - used as an excuse for sloppy thinking, or no thinking at all. Pragmatism encourages partial "solutions" that work not by reflecting a conceptual solution to a problem, but rather by mimicking correct behavior for various inputs. That way, we can short-circuit the need for design and collaboration. Instead we start with a trivial "happy path" solution and add flags and epicycles to flesh it out into richer behavior as needed. This approach yields software that more or less works for the inputs we've tried, and maybe for other inputs as well. (Behavior in the latter case is not as well understood, for obvious reasons.) If we come across inputs that cause problems, we apply patches in the form of additional flags and epicycles.

Because the approach sounds rather dubious when written out like that, we use the magic word "pragmatic" to make it sound better. It's _pragmatic_ problem-solving. We call the solutions themselves "good enough" solutions - wasting any more time thinking would be gold-plating! Sometimes we use quotes like "perfect is the enemy of good" as further evidence that we're doing a good thing - as if the problem we're facing is too much perfect software in the world!

Here's an obviously made-up example of this approach:

```javascript
function square(x) = {
    if (x == 1) then return 1;
    if (x == 2) then return 4;
    if (x == 3) then return 9;
    if (x == 4) then return 15;
    if (x == 5) then return 25;
    // Should never happen.
    return -1;
 }
```

This is a function that computes the square of the integers 1-5, not by reflecting any understanding of what it means to square a number, but rather by emulating correct behavior. It has a small bug for the input number 4, but that doesn't matter much, we rarely get that value, and the result isn't too far off. It's a perfectly pragmatic solution if you can assume that x will always be in the range 1-5.

A more general solution would be this:

```javascript
function square(x) = {
    return x * x;
}
```

Which solves the general case of calculating the square of integers - sort of! Unfortunately, integers themselves are deeply pragmatic. (What happens when x \* x is greater than the maximum value for integers?)

But these are all silly examples - theoretical considerations!

So let's consider something more "real world". Since software exists and executes in time, software typically needs to take time into account - by registering time stamps for various events and so forth.

How do you handle dates and times in your applications? Are you aware of the related complexities that exist? Do you handle those complexities explicitly? Do you think about how they might affect your application in various ways? Or do you simply close your eyes and hope for the best? Assume that all the systems you integrate with use time stamps from the same time zone? Assume that leap years and leap seconds won't affect you (that all years have 365 days and all minutes have 60 seconds)? Assume that daylight savings time won't cause any problems (even though it means that time isn't linear - depending on your time zone(s), some points in time may not exist, whereas others may exist more than once)? Assume that everyone else around you are making the same assumptions? That's mighty pragmatic!

Finally, pragmatism is sometimes used to create outright logical contradictions. Pragmatism is about _compromise_, but some compromises cannot be made without _compromising_ the concept itself! For instance, some architectural properties have principles that cannot be violated while still maintaining the properties - they are simply no longer present due to the violation. (A vegetarian cannot eat meat in the weekends and still be a vegetarian, if you will.) Not even pragmatism can fix this, but that doesn't stop people from trying!

To illustrate, here's (a reproduction of) a funny meme I found [on the Internet](http://www.troyhunt.com/2014/02/your-api-versioning-is-wrong-which-is.html).

![Batman slapping Robin for saying "but it's not RESTful if you"](/images/batman-rest.png)

I think it's funny because a lot of people seem to get annoyed when someone points out that their self-proclaimed RESTful APIs aren't really RESTful because they violate some property of REST or other - typically the hypermedia constraint. People get annoyed because they don't want to think about that, they'd rather be pragmatic and get on with stuff.

But for some reason they still want to keep that word, REST. Maybe they think it sounds good, or maybe they promised their manager a REST API, I don't know. It doesn't matter. They want to keep that word. And so they say "well, maybe it's not your Ivory Tower REST" (implicitly bad!), "maybe it's Pragmatic REST instead" (implicitly good!). And then they go on to do something like JSON over HTTP, which is really simple and great, and they can easily deserialize the JSON in their JavaScript, and they've practically shipped it already. And when someone comes along and talks about hypermedia being a requirement for REST, they just slap them! Pretty funny!

Here's another meme. I made this one myself. Unfortunately it's not funny.

![Batman slapping Robin for saying "but it's not SECURE if you"](/images/batman-secure.png)

Why isn't it funny? It looks a lot like the previous one.

The problem is that when someone violates some established principle of security - maybe they decide it's convenient to store encrypted passwords on their server or to roll their own cryptography - we think it's a bad idea. And we don't think it's a good excuse to say "well, maybe it's not Ivory Tower Security, maybe it's Pragmatic Security instead". We simply don't agree that it's very secure at all. So in some sense it's not funny because the roles have been reversed. Turns out it's much more funny being Batman than being Robin in this meme. Go figure.

Now we have the strange situation that it's apparently OK for some words in software to have no meaning (REST), whereas we insist that others do have meaning (secure). For the meaningless words, prefixing "pragmatic" will absolve you from your sins, for the meaningful words, it will not. This is a problem, I think. Who's to decide which words should have meaning?

Here's a third meme. I made this one too.

![Batman slapping Robin for saying "but it's not TEA if you"](/images/batman-tea.png)

It's a bit strange, I'll admit that. But bear with me. It's the last one, I promise.

What would you say if someone offered you hot water and a biscuit and said "have some pragmatic tea"? Would you be content? Would you _pay_ for a cup of pragmatic tea? Or would you take the route of the dogmatist and argue that it's not really tea if no tea was involved in the preparation? Well, SLAP you! Crazy tea zealot! Hang out with the other ivory tower hipsters, and have your fancy tea! Who drinks tea anyway?!

At this point you can see I've gone absurd - but we're still just doing variations of the same joke. I didn't bring the absurdity, it was there all along. The point I'm trying to make is that words do have meaning, whether it's REST or security or tea. We should respect that meaning instead of using pragmatism as a poor excuse for undermining it. Some properties have principles that cannot be broken while at the same time keeping the properties intact. You can't have REST without Representational State Transfer, because that's literally what the acronym means. Secure applications shouldn't be storing passwords, even if they're encrypted. Tea should contain tea. (Please don't get me started on Rooibos.)

I should add that it's perfectly fine to _not care_ about REST, or to _dispute the value_ of REST - or tea, or security, for that matter. Those are conversations worth having. It's a free world, and everyone is entitled to choose the properties they care about, based on the context they're in. Lots of people very vocally don't care about REST, perhaps even more people than people who know what REST actually is! I have no problem with that. What's less fine is pretending it has no meaning.

This concludes my attack on pragmatism, at least for now! To reiterate: pragmatism is easily abused because it's hard to tell if someone is genuinely pragmatic or just claiming to be so. The abuse takes various forms: false dilemma, self-defense bluff, smoke bomb, justification for sloppy thinking and undermining of the meaning of words.

A call for action? Please stop using pragmatism as an excuse for doing sloppy work! And if you find you do need to use the word, please have it be the beginning of a discussion rather than the end of one.
