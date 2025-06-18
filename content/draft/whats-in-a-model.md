:page/title What's in a model?
:blog-post/tags [:tech :modelling :ddd ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2025-05-11T21:00:00" -->

:page/body

# What's in a model?

<p class="blog-post-date">June 16, 2025</p>

Most developers don't know much about the British statistician George Box. If we know anything at all, it is this: he is supposed to have said that "all models are wrong, but some are useful". The quote is somewhat of a staple at domain-driven design conferences and the like. It reminds us of two things: that models are models, and that the way to evaluate models is through their utility.

### Sketches and blueprints

What makes a model useful? It depends on the kind of model. For the purposes of this blog post, I would like to introduce a very crude and simple model for models. It identifies and distinguishes between two different kinds of models: sketches and blueprints.

Sketch models are more or less apt attempts at describing observed reality. They range from facial composites (e.g. scientific theories) to caricatures (e.g. maps and my own model of sketches and blueprints). The narrower the context, the more outrageous the caricature can afford to be. Sketches are useful because they allow us to explain why something happened and predict what will happen under various circumstances. For instance, if I throw a ball, classical mechanics can be used to predict its arc through the air. Or a map can be used to predict where I will find certain places or objects of interest. If the predictions are good enough, sketches can be used for planning, which can be very useful indeed. Sketches, then, are useful to the extent that they influence our behaviour and our decisions, how we choose to act as agents in the real world.

Blueprint models are recipes to be followed when we try to create some structure in the world. Examples include the architecture drawing for a particular kind of house, or the Spotify model for the organization of product development teams. A blueprint needs to be implemented. It can be implemented more or less faithfully. Deviations from the blueprint can either be deliberate (in an attempt to account for particularities of a given context) or inadvertent (through inertia or lack of skill). Either way, the goal of a blueprint is to build a well-functioning structure. The distinction between a blueprint and a tool is fuzzy.

A blueprint tacitly or overtly relies on and embodies theories, assumptions, beliefs, superstitions, and models of the sketch variety. In the case of the Spotify model, it has built-in beliefs about human networking behaviour and group dynamics. For instance, the authors of the whitepaper that gave rise to the Spotify model explictly mention "Dunbar's number" as influencing the size of "tribes".

Why is it useful to distinguish between sketches and blueprints? It has to do with our degrees of freedom when working with them. Sketches are wrong as per Box, but within their scope, they posit to say something important about reality. They posit to predict what will happen under certain circumstances. What actually happens, of course, is the litmus test of a sketch model.

Models are not always congruent. They don’t agree on the effects of various actions. What is perceived as beneficial under one model can be harmful under another. So if models guide our actions, what should we do?

We are of course free to choose models but we are not free to choose realities.

We are free to choose which model we want in a given context, but we are not free to choose reality. Reality doesn't care, it just unfolds.

This opens up the possibility for us to choose the wrong model. That is, not just a model that is wrong in the sense that _all_ models are somewhat wrong by virtue of being models, but a model that completely missed the point.

"The reason there are so many models is that each of them is useful, but only in some contexts. The problems arise when we try to apply a model that doesn't match the situation in front of our eyes." -- Gerald Weinberg

Put succinctly, we are free to choose whatever model we like, but we ignore gravity at our own peril.

Sometimes we don't make explicit choices of sketch models, but choose them indirectly through tools and blueprints.

### Hybrid models

If all models are wrong, perhaps a hybrid is less wrong?

Box:

Models are not necessarily congruent.

What is the hybridization process? How are the models combined?

What does the hybrid model

Hybrid models as weasel models.

Hedging your bets, covering your back: if one of the models turned out to be wrong, perhaps the other one was right. S

It does not help with predictions.

### Are we serious?

What are we going to use our models for? There is another kind of model use that is purely nominal. Namedropping models.

Application of a model as facile or serious.

Pillaging/scavenging models for words (and soundbites?).

What does it mean to take a model seriously? To take a model seriously is to take its predictions seriously.

Models are useful - to use Box’ phrase - to the extent that they influence our behaviour and our decisions.

Namedropping models

What are the behaviours and the decisions influenced by which models? This should tell us much about which models we are serious about and which we are not.

If we believe that «complex adaptive system» is a useful model for an organization that makes software products, what are the implications for our actions? What do we do differently? If we play a little game and interpret «useful» as «full of use», what are some of those uses?

Model relativism. Choice of model remains serious business to the extent that the model influences our actions. Not all models are equally good, models are not interchangeable. I can use different models in different contexts, and it may work out fine, since there are no contradictions. But when models compete within the same context, proclaim to be applicable for the same problem, then we must choose.

The pendulum swings, from the Platonist primacy of the model over reality, the idea that reality is illusion, and that the model is the real, albeit inaccessible reality. I have said many times that we shouldn’t take models too seriously. And this is true. Models are attempts to capture something that will not be captured. There is always remaining otherness or richness. But at the same time, some models do perform very well under a wide range of circumstances. Newtonian mechanics is such a model. You have to look carefully for otherness that invalidates Newtonian mechanics.

The pendulum can swing too far in the other direction. We may take models not seriously enough. That is, if we adopt a model, we owe it to ourselves and to the model to take it seriously enough that we familiarize ourselves with what it says, what it takes to be salient features of realities, what circumstances it proclaims to apply to under which preconditions, what it predicts will happen under various events etc.

I am free to choose whatever model I like, but I ignore gravity at my own peril.

Descriptive vs constructive models. (Facial composite vs blueprint)

Constructive models act as blueprints to be implemented. Built into the blueprints are theories or models about reality. An implementation may be more or less faithful to the model, and may be more or less successful.

It is weird when there is no congruence between the blueprints we use and the models and theories we supposedly subscribe to.

This is why it’s absurd to apply tools like Lean optimisation to your organisation while at the same time believing that organisations are complex adaptive systems. We can’t be serious in our beliefs, since being serious in our beliefs means that we see waste as indistinguishable from value, that we think that system properties cannot be optimised in isolation, etc.
