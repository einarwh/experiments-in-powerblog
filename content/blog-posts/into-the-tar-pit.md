:page/title Into the tar pit
:blog-post/tags [:tech :software-development]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2020-05-19T12:00:00"
:page/body

# Into the tar pit

Posted: May 19, 2020

I recently re-read the [“Out of the Tar Pit”](https://github.com/papers-we-love/papers-we-love/blob/master/design/out-of-the-tar-pit.pdf) paper by Ben Moseley and Peter Marks for a Papers We Love session at work. It is a pretty famous paper. You can find it at the Papers We Love repository at GitHub for the simple reason that lots of people love it. Reading the paper again triggered some thoughts, hence this blog post.

The title of the paper is taken from [Alan Perlis](https://en.wikipedia.org/wiki/Alan_Perlis) ([epigram #54](http://pu.inf.uni-tuebingen.de/users/klaeren/epigrams.html)):

> Beware of the Turing tar-pit in which everything is possible but nothing of interest is easy.

A [Turing tar-pit](https://en.wikipedia.org/wiki/Turing_tarpit) is a language or system that is Turing complete (and so can do as much as any language or system can do) yet is cumbersome and impractical to use. The Turing machine itself is a Turing tar-pit, because you probably wouldn’t use it at work to solve real problems. It might be amusing but not practical.

The implication of the title is that we are currently in a Turing tar-pit, and we need to take measures to get out of it. Specifically, the measures outlined in the paper.

The paper consists of two parts. The first part is an essay about the causes and effects of complexity in software. The second part is a proposed programming model to minimize so-called accidental complexity in software.

The argument of the paper goes like this: Complexity is the primary cause of problems in software development. Complexity is problematic because it hinders the understanding of software systems. This leads to all kinds of bad second-order effects including unreliability, security issues, late delivery and poor performance, and – in a vicious circle – compound complexity, making all the problems worse in a non-linear fashion as systems grow larger.

Following [Fred Brooks](https://en.wikipedia.org/wiki/Fred_Brooks) in [“No Silver Bullet“](http://worrydream.com/refs/Brooks-NoSilverBullet.pdf), the authors distinguish between essential and accidental complexity. The authors define “essential complexity” as the complexity inherent in the problem seen by the users. The “accidental complexity” is all the rest, including everything that has to do with the mundane, practical aspects of computing.

The authors identify _state handling_, _control flow_ and _code volume_ as drivers of complexity. Most of this complexity is labeled “accidental”, since it has to do with the physical reality of the machine, not with the user’s problem.

The proposed fix is to turn the user’s informal problem statement into a formal one: to derive an executable specification. Beyond that, we should only allow for the minimal addition of “accidental” complexity as needed for practical efficiency concerns.

The authors find our current programming models inadequate because they incur too much accidental complexity. Hence a new programming model is needed, one that incurs a minimum of accidental complexity. The second part of the paper presents such a model.

What struck me as I was reading the paper again was that it is wrong about the causes of complexity and naive about software development in general.

The paper is wrong for two reasons. First, because it treats software development as an implementation problem. It would be nice if that were true. It’s not. We will not get much better at software development if we keep thinking that it is. Second, because it ignores the dynamics of software development and makes invalid assumptions. Specifically, it is naive about the nature of the problems we address by making software.

I agree with the authors that complexity is a tremendous problem in software. The non-linear cumulation of complexity often threatens to make non-trivial software development efforts grind to a halt. Many software systems are not just riddled with _technical_ debt which is the term we often use for runaway complexity – they have practically gone bankrupt! However, the problem of complexity cannot be solved by means of a better programming model alone. We must trace the causes of complexity beyond the realm of the machine and into the real world. While better programming models would be nice, we can’t expect wonders from them. The reason is that the root cause of complexity is to be found in the strenuous relationship between the software system and the world in which it operates. This is outside the realm of programming models.

According to the authors, the role of a software development team is “to produce (using some given language and infrastructure) and maintain a software system which serves the purposes of its users”. In other words, the role is to implement the software. The role of the user, on the other hand, is to act as oracle with respect to the problem that needs to be solved. The authors note in parenthesis that they are assuming “that the users do in fact know and understand the problem that they want solved”. Yet it is well-known that this assumption doesn’t hold! Ask anyone in software! Already we’re in trouble. How can we create an executable specification without a source for this knowledge and understanding?

The paper’s analysis of the causes of complexity begins like this:

> In any non-trivial system there is some complexity inherent in the problem that needs to be solved.

So clearly the problem is important. But what is it? In fact, let’s pull the sentence “the problem that needs to be solved” apart a bit, by asking some questions.

Where did the problem come from? Who defined the problem? How is the problem articulated and communicated, by whom, to whom? Is there agreement on what the problem is? How many interpretations and formulations of the problem are there? Why this problem and not some other problem? Who are affected by this problem? Who has an interest in it? Who owns it? Why does the problem matter? Who determined that it was a problem worth solving? Why does it need to be solved? How badly does it need to be solved? Is time relevant? Has it always been a problem? How long has this problem or similar problems existed? Could it cease to be a problem? What happens if it isn’t solved? Is a partial solution viable? How does the problem relate to other problems, or to solutions to other problems? How often does the problem change? What does it mean for the problem to change? Will it still need solving? What forces in the real world could potentially lead to changes? How radical can we expect such changes to be?

We quickly see that the problem isn’t the solution, the problem is the problem itself! How can we even begin to have illusions about how to best develop a “solution” to our “problem” without answers to at least some of these questions? The curse of software development is that we can never fully answer all of these questions, yet they are crucial to our enterprise! If we are to look for root causes of complexity in software, we must start by addressing questions such as these.

When we treat the problem definition as somehow outside the scope of the software development effort, we set ourselves up for nasty surprises – and rampant complexity. As [Gerald Weinberg](https://en.wikipedia.org/wiki/Gerald_Weinberg) put it in [“Are Your Lights On?“](https://www.amazon.com/Are-Your-Lights-Figure-Problem/dp/0932633161): “The computer field is a mother lode of problem definition lessons.” Indeed, any ambiguity, misunderstandings, conflicts, conflict avoidance etc with respect to what the problem is will naturally come back to haunt us in the form of complexity when we try to implement a solution.

Consider an example of actual software development by an actual organization in an actual domain: the TV streaming service offered by NRK, the national public broadcaster in Norway. It’s where I work. What is the problem? It depends on who you ask. Who should you ask? I happen to be nearby. If you ask me, one of many developers working on the service, I might say something like “to provide a popular, high-quality, diverse TV streaming service for the Norwegian public”. It is immediately clear that providing such a service is not a purely technical problem: we need great content, great presentation, great usability, a great delivery platform, among many other things. Creating useful, non-trivial software systems is a multi-disciplinary effort.

It is also clear that such a high-level problem statement must be interpreted and detailed in a million ways in order to be actionable. All the questions above start pouring in. Who provides the necessary interpretation and deliberation? Who owns this problem? Is it our CEO? The product owner for the TV streaming service? The user experience experts? Me? The public? The answer is all and none of us!

But it gets worse, or more interesting, depending on your perspective. The world is dynamic. It changes all the time, whether we like it or not. Hence “the problem” changes as well. It is not something that we can exercise full control over. We don’t exist in a vacuum. We are heavily influenced by changes to the media consumption habits of the public, for instance. The actions of the international media giants influence our actions as well, as do the actions of the large social media platforms. Everything changes, sometimes in surprising ways from surprising angles.

With this backdrop, how do we address “the problem”? What is the best future direction for our service? What would make it more popular, higher quality, more diverse, better for the public? Opinions vary! Is it ML-driven customization and personalization? Is it more social features? Is it radical new immersive and interactive experiences that challenge what TV content is and how it is consumed? We don’t know. No-one knows.

It is naive to think that there is such a thing as “the user”. If there were such a thing as “the user”, it is naive to think that they could provide us with “the problem”. If they could provide us with “the problem”, it is naive to think that it would stay stable over time. If “the problem” did stay stable over time, it is naive to think that everyone would understand it the same way. And so on and so forth.

We cannot expect “the user” to provide us with a problem description, at least not one that we could use to implement an executable specification. The problem of defining the problem unfolds over time in a concrete yet shifting context, in a complex system of human actors. There is nothing inessential about this, it is ingrained in everything we do. We can’t escape from it. Labeling it accidental won’t make it go away.

Instead of ignoring it or dreaming about an “ideal world” where all of these aspects of software development can be ignored, we should accept it. Not only accept it, in fact, but see it as our job to handle. Software developers should provide expertise not just in programming or in running software in production, but also in the externalization of mental models to facilitate communication and enable collaborative modelling. Software development is largely a communication problem. We should take active part in defining, delineating, describing and exploring the problem domain itself, which goes beyond the software system. We should contribute to better concepts and a richer language to describe the domain. It will help us uncover new and better problem descriptions, which will lead to new and better software systems. This exploration is a never-ending process of discovery, negotiation and reevaluation. We should lead in this effort, not wait for someone else to do it for us.

When we pretend that there is such a thing as “the essential problem” that the user can hand over to “the development team” for implementation, we are being naive Platonists. We’re acting as if “the problem” is something stable and eternal, an a priori, celestial entity that we can uncover. But that is not the reality of most problem domains. It may be possible to identify such problems for purely abstract, mathematical structures – structures that need no grounding in the fleeting world that we inhabit. But most software systems don’t deal with such structures.

Instead, most programs or software systems deal with informal, ambiguous, self-contradictory, fluctuating, unstable problems in a shifting, dynamic world. “The problem that needs solving” is always in a state of negotiation and partial understanding. Assumptions and presumed invariants are rendered invalid by a reality that has no particular regard for our attempts to describe it. Indeed, there can be no innovation without the invalidation of existing models! The problem of software development is not “how to implement a solution to a given problem without shooting yourself in the foot”. It is to formalize something that in its nature is informal and unformalizable. As [Stephen Jay Gould](https://en.wikipedia.org/wiki/Stephen_Jay_Gould) puts it in “[What, if anything, is a zebra?]“(http://courses.ischool.utexas.edu/Lease_Matt/2010/Fall/INF384C/readings/gould_zebra.pdf), “I do not believe that nature frustrates us by design, but I rejoice in her intransigence nonetheless.”

As software developers, we can’t turn a blind eye to this state of affairs. It is an intrinsic and hence essential problem in software development, and one that we must tackle head-on. In [“The World and the Machine“](http://mcs.open.ac.uk/mj665/icse17kn.pdf), [Michael A. Jackson](https://en.wikipedia.org/wiki/Michael_A._Jackson) refers to what he calls the “Von Neumann principle”:

> There is no point in using exact methods where there is no clarity in the concepts and issues to which they are to be applied.

This means that we must gain a deep understanding and a rich language to describe the problem domain itself, not just the software system we want to operate in that problem domain.

The challenge is to fight an impossible battle successfully. We must constantly try to pin down a problem to a sufficient degree to be able to construct a useful machine that helps solve the problem, as we currently understand it. We must accept that this solution is temporary since the problem will change. And then we must try to keep a dance going on between a fundamentally unstable problem and a machine that longs for stability, without toppling over.

We can’t hope to be successful in this endeavor if we ignore the nature of this process. An account of complexity in software that doesn’t account for the continuous tension between a necessarily formal system and an irreducibly informal world is missing something essential about software development. That’s why “Out of the Tar Pit” is wrong.

I think we need to accept and embrace the tar-pit. At least then we’re grappling with the real causes of complexity. The real world is a hot and sticky place. This is where our software systems and we, as software developers, must operate. Nothing of interest is ever going to be easy. But perhaps we can take heart that everything is still possible!
