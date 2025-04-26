:page/title Socio-technical API patterns
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Socio-technical API patterns

<p class="blog-post-date">April 22, 2025</p>

An API is a way to connect two (or more) systems together. What kinds of systems? Software systems, of course. What a silly question! It is Application Programming Interface after all. It is an interface between applications. Right?

But there are more questions we could ask.

 <!-- Later? -->

These days, Web APIs are common, typically in the form of CRUD JSON. (Some people refer to them as REST APIs, bless their souls. But that is a topic for another day.) HTTP has become the ultimate integration protocol, the lingua franca that allows us to connect systems built on all kinds of technologies.

Having connected systems in this manner, what do we have? We can have a pointless philosophical discussion about how many systems we now have: is it one large system? Two connected systems? Both at the same time?

A more interesting question is: How does an API come about, and under what circumstances, conditions and constraints? And having opened up for those kinds of questions, new ones immediately gush out: Why should these systems be connected? Who wants them to connect? What are they trying to accomplish? (What will happen to the systems and the interface as time passes?) All of these questions point toward the existence of people. The reason is that when we are connecting software systems, we are also connecting socio-technical systems. That is, we are connecting both software and people.

The answers to all the different questions provides a socio-technical fingerprint. We should be able to identity recurring patterns. It is worth noticing and labeling such patterns to better understand and address important questions and fix potential problems. In particular we should be able to recognize when we find ourselves in an anti-pattern. First, articulating our pain and understanding what contributes to them is useful in itself. Second, it might provide us with strategies to move towards better, healthier patterns.
The particulars of the socio-technical context, the answers to the relevant questions we can ask about the socio-technical context, will tend toward

## API design

But I'm getting ahead of myself. We can't address all those questions at once. For now, let's focus on how the API comes about, that is, how is it designed, and by whom? We can distinguish between unilateral and bilateral (or multilateral) design efforts. A unilateral or one-sided design effort means that the API provider - the party that will implement the API - does the all the work alone. (In fact there might now be much of a distinction between designing the API and implementing it.) By contrast, a bilateral (or multilateral) design effort means that one or more API consumers is involved in the design. That means that the design process is a collaboration and involves negotiation.

A number of prerequisites must be in place for a bilateral design process to be feasible. In particular, must be able to and want to collaborate. Alignment and trust. More or less cumbersome, more or less efficient. To some extent, we can talk of gradients from unilateral to bilateral. To be "proper" bilateral, it is not enough for the API provider to check in with the API consumer often or solicit feedback during the design or implementation of the API. The design work must be a joint effort, with provider and consumer working together to determine the design of the API. This does not necessarily mean that the implementation of the API is a joint effort, just like the implementation of the client code that uses the API doesn't have to be a joint effort. It can be, and it might be useful if it would be, but it doesn't have to be.

Bilateral design is mostly feasible for internal APIs, where the API provider and the API consumer are two teams inside the same organization.

In practical terms, the API provider and the API consumer just need to collaborate on the design of the interface itself. For Web APIs, it means producing an OpenAPI document as a joint effort. The astute backend developer will note with terror that this means that the OpenAPI document can't be reverse engineered from the API implementation using a tool like Swashbuckle if you want to do bilateral design. It is simply too cumbersome. Instead, the OpenAPI document must be co-authored by hand, for instance in a mob design session involving at least one member from each team. This radically changes the interpretation and role of the OpenAPI document itself. In a unilateral API design, the OpenAPI document is _documentation_, that is, a description of how the API implementation works. Often the document will be coupled to the actual implementation through a reverse engineering process, ensuring that the documentation is always up to date. By contrast, in a bilateral API design, the OpenAPI document is better thought of as a specification or a contract: it is a description of what the provider and consumer(s) have agreed upon.

We want different things from documentation and contracts. Documentation should be accurate and up to date. Contracts should be reliable and stable. If the API provider pushes a change to the API implementation, we don't want the contract to shift. This is not how contracts work. If my tenant starts paying me less for the appartment they're renting, I don't want the contract we signed to automatically change to reflect the new, lower rent. It's not what we agreed upon. A contract is not the same as the description of behavior. A contract is, and must be, decoupled from behavior. This is a strength, not a weakness, with contracts.

## Non-functional properties

The so-called non-functional properties of an API are surprisingly often unknown. They are both undocumented and unspecified. This is particularly true for internal APIs. This is regrettable, since non-functional requirements are business requirements. For APIs-as-products, some non-functional properties may be specified in a service-level agreement.

## Socio-technical environment

If it is true that we are connecting socio-technical systems, what is the interface of those systems? We don't have anything like OpenAPI for software teams. But maybe it's weird to put it like that. We might be more accustomed to something like agreement. (But contract sounds familiar.)

Can we outline what an agreement or a contract for the socio-technical interface might look like? Very often, there is no articulated agreement. (The nearest we might have is an SLA.)

Description of the socio-technical reality/context.

## Socio-technical context

To understand the socio-technical
Understanding the actors. Asking questions. Who is the consumer and who is the provider? We can drill down with more specific questions, to provide more detailed descriptions of the socio-technical environment/reality. Who is the consumer? What are their goals? Why are they using the API? What are they trying to accomplish? What are their expectations? What constraints do they operate under? We can ask similar questions about the provider, but of course we might receive quite different answers.

## Consumer-provider relationship

- Points of contention:

  - Downtime
  - Errors
  - Change
  - API stability/change rate

- Provider:
  - How is work on the API financed?
  - How much time and effort does it require?
    - Initial development cost
    - Maintenance, evolution, operations
- Consumers:
  - Motivation to evolve client code.
- Power dynamics
  - The consumer's ability to influence/ignore the actions of the provider
  - The provider's ability to influence/ignore the actions of the consumer
- How is change handled?
- Poor communication leads to workarounds, depending on assumptions, perceived invariants, undocumented features.
- How important is the API for the consuming system? (Optional, required)
- No SLA
- "Best effort" aka "It is what it is"
- Determined by consciousness, availability of resources of the providers.
- Determined by the clout and leverage of the consumer(s).
- Very often unspecified.
- Often even unarticulated/unacknowledged.
- Range in ambition:
  - API as product
  - Data exposure

We use APIs to connect software systems

Few software systems exist in isolation.

Agenda

- What is an API?
- Technical perspective
- Socio-technical perspective
- Patterns
- Socio-technical checklist

What is an API?

- Connecting two or more systems
- What kinds of systems?

Technical perspective

- Connecting two technical systems
- Why are these systems being connected? (Access to data, something-as-a-service)
- Typical case: Web API, JSON over HTTP.
- How is the API designed? (Unilaterally, bilaterally, collaboration? negotiation?)
- Is there a spesification? (E.g. OpenAPI)
- Documentation (unilateral) vs contract (bilateral)
- Non-functional requirements typically undefined
- Non-functional properties typically unknown
- This can be a problem :-)
- Points towards SLAs, which shifts our perspective.

Socio-technical perspective

- Who are connecting these systems?
- How are the systems connected? Who does the connecting?
- Connecting two socio-technical systems
- Who are the consumers and who are the providers?
- What are their constraints and their goals?
- Points of contention:
  - Downtime
  - Errors
  - Change
  - API stability/change rate
- Responsibilities of consumers
- Responsibilities of providers
- Provider:
  - How is work on the API financed?
  - How much time and effort does it require?
    - Initial development cost
    - Maintenance, evolution, operations
- Consumers:
  - Motivation to evolve client code.
- Power dynamics
  - The consumer's ability to influence/ignore the actions of the provider
  - The provider's ability to influence/ignore the actions of the consumer
- How is change handled?
- Poor communication leads to workarounds, depending on assumptions, perceived invariants, undocumented features.
- How important is the API for the consuming system? (Optional, required)
- No SLA
- "Best effort" aka "It is what it is"
- Determined by consciousness, availability of resources of the providers.
- Determined by the clout and leverage of the consumer(s).
- Very often unspecified.
- Often even unarticulated/unacknowledged.
- Range in ambition:
  - API as product
  - Data exposure

Patterns

- The millstone
- The mountain
- The rapids (with or without beaver dams)
- The sockpuppets
- Crowd-sourcing: challenge you to identify other patterns, e.g. by identifying a dominant socio-technical factor

Socio-technical checklist

- Improve visibility
- Articulate the unarticulated
- Silently undefined -> either explicitly defined or explicitly undefined
- Checklist
  - Who is the consumer and who is the provider?
  - What constraints do they operate under?
  - What are the power dynamics between consumer and provider?
  - What is the collaboration like?
  - What are the communication channels?
  - How important is the API to each party?
  - What is the expected change rate for the API?
  - What is the SLA?
  - Are both sides happy?

## Patterns

### The Millstone

Characteristics:

- Internal API
- API provider
- Minimal effort. No design. Expose some data for consumption. Consequences: API provider have poured concrete over their internals and now their life is much harder.
- No budget for upkeep
- Unilateral design effort.
- No SLA, best effort.
- Everything is up to power dynamics in the organization.
- Minimal communication.

### The Mountain (and the Volcano)

Characteristics:

-

Mountain: The API is provided by big tech, you can take it or leave it. There is no communication and the consumer has no leverage. Sometimes the mountain explodes in changes, then it’s called the volcano.

### The Rapids (with or without beaver dams)

Rapids: API in the middle of a value stream, provider and consumer needs constant collaboration but are not always in sync or share priorities. Sometimes they build beaver dams to decouple, which is really an anti-pattern. You don’t want beaver dams in your value stream. Maybe the teams should consider merging.

### The Sock Puppet

If they do, you have the sock puppet pattern, which is one team talking to itself through an API. This is sort of the microservice approach. You might want to eliminate the API and have a single process.
