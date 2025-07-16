:page/title Socio-technical API patterns
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2025-04-28T16:17:00"

:blog-post/description

When we connect software systems through APIs, we also connect the people who work on those systems. Socio-technical forces tend to favor certain patterns of relationships between provider and consumer. This blog post describes four patterns that I have observed many times in my career: The Millstone, The Mountain (or the Volcano), The Rapids (with or without Beaver Dams) and The Sock Puppet.

:page/body

# Socio-technical API patterns

<p class="blog-post-date">April 28, 2025</p>

![A pattern of stick people.](/svg/byteman-pattern-long.svg)

An API is a way to connect two or more systems together. What kinds of systems? Software systems, of course. What a silly question! It is Application Programming Interface after all. It is an interface between applications. Right?

Yes. But there are more questions we could ask. Why should these systems be connected? Who wants them to connect? What are they trying to accomplish? What constraints do they operate under? Who designs the interface? Who will be responsible for the evolution of the systems and the interface that connects them as time passes? All of these questions point toward the existence of people. The reason is, of course, that software systems come with people attached. When we connect software systems, we also connect socio-technical systems. Leaving this out when discussing APIs causes us to miss a lot of things.

By asking questions like these, we start laying out the socio-technical landscape of APIs. The answers to those questions in a particular context places a given API (and the people involved in it either as providers or consumers) in that landscape. My experience suggests that socio-technical forces tend to pull APIs towards certain points in that landscape. In other words, there are recurring socio-technical API patterns.

Noticing and labeling such patterns may help us better understand and fix potential problems. For instance, we may discover that we've fallen into a known anti-pattern. This is valuable in at least two ways. First, articulating our pain and understanding what contributes to it is useful in itself. Second, it might provide us with strategies to move towards better, healthier patterns.

The purpose of this blog post, then, is to articulate some questions that help lay out the socio-technical landscape for APIs, identify some typical answers, and describe a few patterns I have observed in my career. Those patterns are [The Millstone](#millstone), [The Mountain](#mountain), [The Rapids](#rapids) and [The Sock Puppet](#sock-puppet). I hope you will find them useful.

But first, let's take a look at the socio-technical landscape and the forces that give rise to these patterns.

### Consumer-provider relationship

On the two sides of the API divide we find the provider and the consumer(s) of the API. To investigate the dynamics between the two parties, let's start with a basic question. How important is the API to them?

For the API provider, the answer can range from main focus (the team exists primarily to provide the API) to nuisance (the team has been instructed to provide an API to another team), with the middle ground being something like a side hustle. This is typically mirrored by the amount of resources available both for the initial development of the API and for its subsequent upkeep and evolution. For the API consumer, it can range from essential to optional.

The consumer and the provider are not necessarily aligned with respect to how important the API is to them. All combinations are possible. A particularly interesting one is the case where the API is essential to the consumer but a nuisance to the provider. This is quite common for internal APIs.

The degree to which the consumer and the provider need to collaborate and communicate in order to connect their systems vary greatly. In the case where the API is a product offered by the provider and the consumer is just another customer, direct communication may be entirely absent. The consumer can handle onboarding by themselves, with no other guidance than the documentation offered by the provider. At the other end of the spectrum, there is no existing API, and the prospective provider and consumer need to work out what the API should be.

Correspondingly, the communication between consumer and provider ranges from practically non-existent (except perhaps the ability to report bugs in a fire-and-forget manner) to being able to walk over to someone's desk and have a face-to-face conversation. (Of course being _able to_ have a face-to-face conversation is not the same as _using_ that ability, since people are subject to social awkwardness, inhibitions and self-defense mechanisms, leading us to prefer lower-bandwidth but less-exposed asynchronous communication channels). This is unfortunate, since poor communication or communication avoidance often leads to workarounds and dependencies on assumptions, perceived invariants, and undocumented features. (An example is consumers scavenging string values for data items like IDs.) This in turn makes it harder to change the API since the implementation becomes the interface. As long as the consumer-provider relationship is sufficiently dysfunctional, a single consumer is enough to fulfil [Hyrum's Law](https://www.hyrumslaw.com/), i.e.

> With a sufficient number of users of an API,
> it does not matter what you promise in the contract:
> all observable behaviors of your system
> will be depended on by somebody.

### API design

When a system needs to connect to another system and there is no existing API, what happens? How does the API come about? How is it designed, and by whom?

We can distinguish between unilateral and bilateral (or multilateral) design efforts. A unilateral or one-sided design effort means that the API provider does the all the work alone. This seems to be the default in many organizations. By contrast, a bilateral (or multilateral) design effort means that one or more API consumers is involved in the design. In that case, the design process is a collaboration. This seems to be rare.

A number of prerequisites must be in place for a bilateral design process to be an option. In particular, the provider and the consumer(s) must be able to and want to collaborate. This requires a basic level of alignment and trust. Bilateral design is mostly feasible for internal APIs, where the API provider and the API consumer are two teams inside the same organization.

A bilateral design process can be more or less efficient and functional. We may speak of gradients from unilateral to bilateral. To be "proper" bilateral, however, it is not enough for the API provider to check in with the API consumer often or solicit feedback during the design or implementation of the API. The design work must be a joint effort, with provider and consumer working together to determine the design of the API. This does not necessarily mean that the implementation of the API is a joint effort, just like the implementation of the client code that uses the API doesn't have to be a joint effort. It can be, and it might be useful if it would be, but it doesn't have to be.

In practical terms, the API provider and the API consumer just need to collaborate on the design of the interface itself. For Web APIs, it means producing an OpenAPI document together. The astute backend developer will note with terror that this means that the OpenAPI document can't be reverse engineered from the API implementation using a tool like Swashbuckle. It is simply too cumbersome. You need much faster iteration cycles. Hence the OpenAPI document must be co-authored "by hand", for instance in a mob design session involving at least one member from each team. An upside is that it is much easier to create high-quality OpenAPI documents by hand than it is to reverse engineer them. Another is that co-authoring the document has valuable social byproducts in improved bilateral understanding, trust, communication and respect.

A bilateral design process radically changes the interpretation and role of the OpenAPI document itself. In a unilateral API design, the OpenAPI document is _documentation_, that is, a description of how the API implementation works. As such, it is not really an interface. Often the document will be coupled to the actual implementation through a reverse engineering process, ensuring that the documentation is always up to date. By contrast, in a bilateral API design, the OpenAPI document is better thought of as a _contract_: it is a description of what the provider and consumer(s) have agreed upon. Importantly, it is the contract that is the source of truth, not the behavior of the API. If there is a discrepancy between the contract and the behavior, it is a bug in the implementation of the API.

We want different things from documentation and contracts. Documentation should be accurate and up to date. Contracts should be reliable and stable. If the API provider pushes a change to the API implementation, we don't want the contract to shift inadvertently. This is not how contracts work. Changing the contract is a serious matter. If my tenant starts paying me less for the apartment they're renting, I don't want the contract we signed to automatically adjust itself to reflect the new, lower rent. It's not what we agreed upon. A contract is not the same as the description of behavior. A contract is, and must be, decoupled from behavior. This is a strength, not a weakness, with contracts.

## Service level

Of course it is not enough to design or even build an API. It also needs to be operational. Consumers need the API to work, but what does that mean? Answering that question is harder than it may appear. As usual, the opinions of API providers and API consumers are not necessarily aligned.

Points of contention include:

- Downtime.
- Errors.
- Change.

Teams are often naive with respect to these points, leaving them unaddressed. That means that there often is no definition of what it means for the API to be down, nor any agreement on acceptable downtime. The same goes for errors.

For APIs-as-products, these points are typically included in a service-level agreement (SLA). For internal APIs, SLAs are rare. This is unfortunate. An API contract in broader terms should also cover the operation of the API.

Of course, the API consumer typically don't know precisely what they need in terms of non-functional requirements. They just want the API to "work". This is understandable, but it is also an ill-defined and impossible request. It is impossible to verify that the API "works" if there is no definition of what that means. No API delivers 100% uptime, 0 faults, 0 latency and infinite throughput, hence we need a specification of what the consumer can live with.

Similarly, the API provider typically don't know precisely what they can deliver in terms of guarantees. And so they offer a guarantee of "best effort". This is also understandable, but again void of meaning. We take for granted that the team will try their best with the time and resources they have. But how good is the team's best effort? Is it any good at all? There is no way to tell. And so it's just a euphemism for "no guarantees" or even "it is what it is".

Another point of contention is change: both how often the API is expected to change, and how change should be handled. Unfortunately teams tend to shun such discussions both since they may find that they have no good solutions for it, and because it may surface differences of opinion. As a result, changes are typically handled as an afterthought, inconsistently and unpredictably.

Leaving important points of contention undefined means that they will always have to be negotiated in the spur of the moment, to be resolved on a one-by-one basis by the combination of conscientousness and availability of resources of the providers on one hand and the clout and leverage of the consumer on the other. In other words, it will be left to power dynamics. A much healthier approach is to overcome the discomfort of negotiation and settling of differences, and take the cost of reaching an agreement. As before, this comes with valuable social side-effects, strengthening the consumer-provider relationship.

## Patterns

We have seen that there are many variables at play in the relationship between the consumers and the provider of an API. This gives rise to distinct socio-technical patterns. Below I describe four patterns I have observed in my career. You may notice that they tend to be anti-patterns or at least potentially problematic patterns. I attribute this to the [Anna Karenina principle](https://en.wikipedia.org/wiki/Anna_Karenina_principle), based on the famous line "All happy families are alike; each unhappy family is unhappy in its own way." Similarly, one might say that the road to a healthy API is to avoid certain common dysfunctions.

<h3 id="millstone">The Millstone</h3>

The Millstone is a common anti-pattern for internal APIs. It comes about when a software team is instructed to provide an API for another team by some actor with the necessary clout in the organization. Providing this API is not the main focus for the team, but they are required to do so anyway. Hence the API quickly becomes a nuisance. There is no budget for either initial design or upkeep of the API, so they are inclined to do the minimal effort to fulfil their end of the bargain. This typically means "exposing some data" over HTTP. There is no time or inclination to do any proper design or product development.

Exposing data is by definition a matter of turning internal data into public data. The unfortunate consequence is that the API provider has now effectively poured concrete over their internal data model, and their life is much harder. It affects not just the API, but everything else they do. The API provider will typically want to change the API over time to enable them/reflect the evolution of their data model. However, they will need to synchronize this with the API consumer(s) who may have little incentive to change. Their ability to coerce the consumers to update is a matter of organizational power dynamics. The API is a millstone around their necks, hence the name of the pattern.

The problems with the Millstone pattern are much more pronounced for the provider than for the consumers. However the situation isn’t necessarily great for consumers either. The API tends to be poorly specified. There is no stable contract, just reverse engineered documentation. The API may be subject to inadvertent changes, in particular since the API provider is using the same data model for their primary focus as well. There is no SLA, just the "best effort" of a team that would rather spend their time on something else. Best may fall quite a bit short of good. An upside is that the consumer team at least tends to have easy access to the provider team.

How do you avoid falling into the Millstone pattern? The forces pulling toward the pattern are strong, and so must be tackled head-on. It poses a number of challenges. First, the organization needs to be aware of the dangers of the Millstone pattern. Decision makers need to be mature enough to realize that providing an API is not a one-time cost, it is a long-time commitment. Initial investments in API quality pay off, whereas sloppy initial work can exact a cost for the lifetime of the API.

The API must be treated as a proper product. The API enters the portfolio of products that the team needs to support, whether the team wants it to or not, and whether the organization recognizes it or not. The only variables are the quality of the API and how much unacknowledged ghost work the provider team will need to do. It is important that the team's environment and stakeholders understand this as well. The team will need to allocate some of its time and focus on the API, which means less time and focus on other products.

The provider and the consumer should collaborate on bilateral API design, to create a design that better serves consumer needs. There should be explicit agreements on anything that will otherwise be left to power dynamics. Tacit assumptions about uptimes and support must be made explicit. There should be a reciprocal service level agreement that outlines commitments of both provider and consumer. The teams should agree on a versioning strategy and a change cadence. A mechanism for deprecation must be built in.

<h3 id="mountain">The Mountain (and the Volcano)</h3>

The Mountain is a common pattern for APIs-as-products. Typically the API provider is a big tech company. The name of the pattern comes from the saying "if the Mountain won’t go to Mohammed, then Mohammed must come to the Mountain".

In terms of time, the API already exists when the consumer makes the decision about whether or not to use it. There is typically a large number of existing consumers. For each consumer, the choice is to accept the API as it is or leave it. Sometimes there are viable alternative APIs to choose from, but they tend to be made by other big tech companies, meaning they are Mountains too. APIs provided by big tech companies are likely to be covered by broad service level agreements. Communication channels tend to be indirect, through various layers of customer support. Larger customers may have better communication channels.

Mountain APIs are extremely stable once they have reached general availability. They rarely evolve at all, since that would risk breaking any number of consumers. If the API changes, it is through a cataclysmic event where the provider unilaterally decides to deprecate the current API, turning the Mountain into a Volcano. Luckily it tends to be a controlled eruption in the sense that some time is allowed for consumers to evacuate. But the effect is dramatic: the old Mountain is obliterated. In the best case the old API is replaced by a new API, which may require more or less substantial changes to the consumers, and may or may not satisfy their needs. In the worst case there is no replacement, and the consumer is left to work out what it means for them and what to do. The impact of this depends on how important the API is to the consumer, ranging from the inconvenient to Pompeii.

The Mountain is not an anti-pattern, and there are no obvious alternatives to it. To the extent that you need to use an API provided by a big tech company in your software system, you will naturally find yourself in the Mountain pattern. You will have to go to the API, it will not come to you.

<h3 id="rapids">The Rapids (with or without Beaver Dams)</h3>

The Rapids pattern describes the scenario where you have an API in the middle of a value stream. It is a very prevalent and dubious pattern. The prototypical example is when you have a backend team and a frontend team working on the same product. The backend team becomes the API provider and the frontend team the API consumer. The API naturally becomes extremely volatile, since each new product feature requires additions to the API, as well as potential other changes as well. Neither team can deliver any value by themselves.

Teams in the Rapids pattern face a choice of constant collaboration and staying in lockstep or inviting one or more socio-technical dysfunctions. Often teams will choose the latter, consciously or inadvertently. There are multiple socio-technical forces that contribute to this. Indeed, one might wonder why there are two teams of it not to accomplish the opposite; that is, independence and autonomy. While the participation of both provider and consumer is required to deliver value, there is no guarantee that any given feature will take an equal amount of time and effort on both sides. There are natural variations in the pace of work making it seem wasteful to remain in sync. But of course asynchrony leads to much less efficient communication since information and context decay quickly. Common psychological factors come into play as well (e.g. preferring written asynchronous communication and working alone to face-to-face communication and working together). Tribal factors strengthen these tendencies.

Sometimes the teams go all in in this direction and try to "decouple" from each other. For instance the frontend team might build their own "backend-for-frontend" API in front of the backend API, or the teams may agree on using something like GraphQL for the API. The idea is to solve the social problem of collaboration through technical means, by reducing the need for communication and negotiation. This is tantamount to building a beaver dam in the rapids. It is a very bad idea for obvious reasons. Beaver dams inhibit flow. You don’t want beaver dams in your value streams.

Moving away from the Rapids pattern is a "[inverse Conway](https://www.thoughtworks.com/insights/blog/customer-experience/inverse-conway-maneuver-product-development-teams)" kind of problem. It's easiest to start by merging the teams, to get rid of the social inhibitions to communication and collaboration. Sometimes this results in a single team that is "too big". For instance, you may find that it violates the oft-quoted "two pizza" rule. There are at least three obvious solutions to this that avoid the frontend/backend partitioning of teams. The first is to challenge the idea that the team is too big. Maybe it is, and may it isn't. Maybe you can just buy three pizzas. As long as the communication improves when you merge the teams, the situation has improved. The second is reduce the size of the merged team. You might find that the improved flow makes up for the reduced headcount. The third is to look for vertical partitionings rather than horizontal ones. That is, make thinner value streams.

<h3 id="sock-puppet">The Sock Puppet</h3>

In the Sock Puppet pattern, a team is talking to itself through an API. That is, a single team is both the provider and the sole consumer of the API. The Sock Puppet is not necessarily an anti-pattern. At least it's not as obviously a bad idea as it might seem at first glance. If you're making a Single Page Application (SPA), you basically have the choice between The Rapids and The Sock Puppet. In that case, The Sock Puppet is preferable for the reasons described above. In other cases, it may be worth revisiting the reason for having an API. What benefits do you get? Do they outweigh the costs? Is it worth it? This is particularly the case for APIs that change often.

You should also beware if you start getting a large number of socks. Exactly what constitutes a large number may vary, but it may be worth recalling that a human has only two hands, and so can only manipulate two puppets with anything resembling ease and elegance. The number may be similarly low for teams.

Microservice architectures run this risk. A well-modularized system of microservices with stable APIs might be able to reap the purported architectural benefits like scalability, fault isolation, faster deployment and cost efficiency. If you find yourself doing shotgun surgery on multiple Sock Puppets and have to coordinate deployments every time you make a change, however, you should consider merging services. It may be that there is no natural, stable API between your Sock Puppets. In that case you should eliminate the API and have a single process or at least fewer, more coarse-grained services.

## Summary

When we connect software systems through APIs, we also connect the people who work on those systems. The socio-technical context of the APIs is important both for the provider and the consumer of the API. Common socio-technical forces such as economics and psychological factors tend to favor certain patterns of relationships between provider and consumer. In this blog post, I have described four such patterns that I have observed many times during my career. With luck, perhaps you will be inspired to describe some patterns that you have observed as well. That would make me very happy.
