:page/title Dragging a dead priest: programs and programmers in time
:blog-post/tags [:software-development]
:blog-post/author {:person/id :einarwh}
:page/body

# Dragging a dead priest: programs and programmers in time

Posted: February 5, 2023

A fundamental challenge we face as programmers is that the world is alive, but the programs we write, alas, are not. When we deploy our dead programs to operate in the living world, it presents an immediate maintenance problem. Or, if you like, a matter of keeping up appearances.

In the movie [Night on Earth](https://www.imdb.com/title/tt0102536/), Roberto Benigni plays an exuberant and eccentric taxi driver working the night shift in Rome. You might be able to see [the movie clip](https://www.imdb.com/title/tt0102536/) on YouTube. If you can, you should.

As night turns into early morning, the taxi driver picks up a priest. During the ride, the talkative driver starts confessing his many sins of the flesh to the priest – including affairs with a pumpkin, a sheep named Lola, and his brother’s wife. The priest listens in increasing disbelief and discomfort. Apparently he has a weak heart. By the time the taxi driver has finished his confessions, the poor priest is long dead.

![The taxi driver and the dead priest](/images/night-on-earth-dead-priest.jpg)

What should the taxi driver do? He decides to try to make it look as if the priest has not in fact died, but rather just fallen asleep on a bench. Unfortunately, the priest’s body is unwieldy and heavy, which makes the task cumbersome. (As the taxi driver puts it, “he is only a priest, but he weighs enough to be a cardinal”.) He drags the priest out of his taxi, and with much effort manages to place him on a bench. But dead people are not good at sitting upright. The priest repeatedly slides down. After several tries, however, he succeeds in placing the priest in a reasonable sitting position. He immediately notices another problem: the priest’s eyes are wide open. It’s not a natural look. He tries to close his eyelids, but they spring back open. The solution? The sunglasses he has been wearing while driving taxi at night. He puts the sunglasses on the priest, and drives away. As he does so, the priest slumps to the side.

I suggest that we programmers are Roberto Benigni in this story, and that our programs play the part of the priest. We are constantly dragging our programs around, trying to make them look alive. In our case, we have the additional problem that the programs grow larger as we drag them around – as if the priest were gaining ever more weight.

The world around us constantly changes. Our programs are unable to respond and adapt to those changes by themselves. They are, after all, mere code. They are dead and have no agency. It falls on us, the programmers, to run around modifying the code as appropriate in response to the changes that occur in the world. Unfortunately, since this is a manual process and we are bound by time and economic constraints, our response is bound to be partial and imperfect. We might not immediately be aware of all relevant changes that we need to respond to. Sometimes we don’t know exactly how to respond. We might not consider it worthwhile to respond to the change. The change might be incompatible with previous choices or assumptions we have made. It might take a lot of time to make the necessary changes to the code. When there are many changes, we might lag behind.

The problem, then, is that our programs are dead structures that exist in time. By necessity, they exhibit inertia in the face of change. Effort must be expended to overcome that inertia. Effort does not only cost resources, but also takes time. And time brings with it more changes.

There are further complicating factors as well. For instance, it’s increasingly hard to clearly define what the program is and isn’t. Is it just the code in the git repository? Surely not, because it almost certainly depends on third-party components and external services as well, not to mention a hosting environment, perhaps in the cloud somewhere. In a sense, our code is just a plug-in to this much larger program – which also has structure, is affected by change, exhibits inertia, and needs external help in adapting to that change. The interplay between changes and more or less delayed responses to these changes in various parts of the conglomerate program can be chaotic.

Speaking of help: the program is interwoven with the organization that creates the program, as part of a larger socio-technical system. This system is also hard to delineate. The distinction between system and non-system is an abstraction that we will into being by collaborative illusion-making. And of course the people, too, are in a sense structures (albeit live ones), with their jobs, roles, salaries, status, group memberships, relationships, loyalties and trust. We also exhibit inertia in the face of change.

What changes brush up against and confront all this inertia? All kinds: functional requirements, non-functional requirements, vulnerabilities in third party components, fashions and trends, legislation, macro and micro economic developments, funding, people leaving and joining – anything that changes the relationship between the program (or our part of it, anyway), the organization and the world they both operate in. The world is an engine that creates constant gaps, and we must keep running to stand relatively still. Or rather to maintain our balance, since the world is spinning, never where it was a moment ago.

What happens if we can’t keep up? If the gaps between expectations and reality becomes too large, and the illusion of the living program breaks down? Is it a second death that awaits when the priest has become too heavy for us to move?
