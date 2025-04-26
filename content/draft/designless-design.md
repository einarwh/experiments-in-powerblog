:page/title Designless design
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Designless design

<p class="blog-post-date">February 8, 2025</p>

Claim: lots of software is designed without design. I'm not talking about UX.

- Incrementalism.
- Design by epicycle.
- One-patch-away development.

A process of approximation.

Incrementalism is the belief that

Of course, writing software necessarily must produce a design regardless of how you do it. Hence designless design.

Designless design arrives at a design by accumulating structure rather than implementing a conceptual model.

Is design an attempt to stop time?

What is more or less brittle? What is more or less likely to suffer from problems like overfitting?

Incrementalism vs working in iterations or implementing in increments.

Conceptual breakthrough?

"The clean design wants to emerge"?

Is it a matter of "listening to the material?"

What's the place for tacit knowledge? The reflective practitioner.

There is little evidence that "emergent design" is successful in teasing out good designs on a regular basis. Rather, what seem to emerge are so-called big balls of mud. We shouldn't be surprised by this. Spaghetti code is just vernacular for epicycles upon epicycles - what you would expect when practicing design by epicycle.

"The code is the design"
"There is no design to be done apart from mucking about in the code."

In a way, it fits TDD like a glove. Each new failing test is a bug report against an incomplete solution. Underlying assumption: we'll always make progress. The incomplete solution approaches a complete solution in the limit. It will become better and reach a satisfactory state, with an arbitrarily small epsilon of incompleteness remaining. But this assumption is just an assumption, it's not necessarily universally true, even though it may hold under many circumstances in practice. Sometimes, you may find that the epsilon remains large.

There's a certain tinge of Zeno's paradox to incrementalism.

Assumptions:

- there will always be progress
- incremental improvements are always possible
- the next incremental improvement is not harder than the previous one
- error can be made arbitrarily small

Working in small increments means that I am given many chances to adjust my course and to listen to feedback.

The idea that changes are small are offered as rationale for these assumptions. I can easily paint myself into a corner using small strokes. (Will not be understood, need better image.)

I can not go to the moon by

A benefit of (extreme) incrementalism is that you don't need a conceptual solution or even a well-defined problem. You sort of just chip away at it until you're more or less happy. If you're happy and then find out later you're actually unhappy because the solution is incomplete or unsatisfactory, you patch it. This is the great strength of designless design, and also its great weakness.

Partial solutions to ill-defined problems.

Can these be valuable? Yes, obviously, evidently.

ANYTHING WORTH DOING IS WORTH DOING POORLY.

There's lots of valuable software out there written using this approach. Is all well then? Is designless design the way to go? Not necessarily. There's also lots of software that has effectively ground to a halt, whose value is deteriorating over time.

Lots of line-of-business applications are just a bunch of easy problems stacked on top of each other. The challenge is not in the individual problems, but in putting them all together in a way that progress and change does not grind to a halt over time.

Non-trivial/hard problems don't necessarily yield to incrementalism. The classic example is the suduko affair.

https://explaining.software/archive/the-sudoku-affair/

Writing a Sudoku solver is harder than the typical "business problem" of shuffling data in and out of databases or over HTTP. This despite actually being a rare instance of a well-defined problem. You need a conceptual solution to the problem or you might find that you will struggle and get stuck.

Many developers have the same experience solving Advent of Code puzzles. The first week is pleasant, because the problems yield to very nicely incrementalism and design by epicycle. That is, naive approaches are sufficient. But at some point, naive solutions won't do, and you will have to employ certain data structures or algorithms or you simply won't be able to solve the problems.

The distinction between implementing a design and ...?

Many problems in line-of-business applications yield very well to incrementalism. Undesigned solutions typically compose poorly, because they lack conceptual integrity. But you can still stack a great number of such approximate on top of each other and make them work by practicing one-patch-away development and adding epicycles to your architecture.

Problem: Rampant "complexity" or complicatedness. One-patch-away development works best when patches are easy to make and there are few interconnections. Over time, it tends to become harder to write patches. We call this "technical debt". Loss of degrees of freedom, solutions that don't play well together, that don't compose, that don't add up.

Examples of designless design: creating an API by exposing some data. What is the problem with this?

What, if anything, can designful design do that designless design can not? Well, first of all, it can solve harder problems. Second, it can stand a greater chance of allowing composition of solutions into larger solutions, of avoiding or postponing the gradual grinding to a halt so endemic in software.

It can be useful to try
