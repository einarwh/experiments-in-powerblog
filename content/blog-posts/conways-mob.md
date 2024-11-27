:page/title Conway's mob
:blog-post/tags [:tech software-development :mob-programming]
:blog-post/author {:person/id :einarwh}
:page/body

# Conway's mob

Is there anything interesting happening at the intersection between Conway’s law and mob programming? Yes! What? Read on!

As you may know, the term “Conway’s law” comes from a 1968 paper by Mel Conway called “How do committes invent?“. If you haven’t already, I really recommend taking the time to read the paper. It’s just four pages! Granted, it’s four pages in a really tiny, downright miniscule font, yes, but still just four pages. It’s also very readable, very approachable, very understandable. I bet you could work through it in half an hour with a good cup of coffee. And if you do that, you’ll get a much richer understanding of what Mel Conway is trying to say, the argument that he is making. If nothing else, you can use that understanding to call out people who write blog posts or do talks at conferences when they’re misrepresenting Conway. It’s a win-win.

So what did Mel Conway say in 1968? Here’s my take. The paper talks about the relationship between organizations and the systems they design. There are two things I’d like to highlight. First, in the introduction, Conway says that given some team organization – any team organization – there is going to be a class of design alternatives that cannot be effectively pursued by that organization because the necessary communication paths aren’t there. This tells us, of course, that communication is vital for design! But perhaps we already suspected as much, and if that were all there was to it, perhaps we wouldn’t be reading Conway’s paper today. But then he goes on to argue that the influence of communication patterns on design is much more direct than we might expect. Indeed, he says that organizations are constrained to produce system designs that are copies of the communication patterns in the organization! And this startling insight is what has become known as Conway’s law.

To reiterate: given some organization tasked with designing some system, then according to Conway’s law, there is a force at work to mimic the communication patterns of the organization in the communication patterns of the system. If this is true, it follows that if you care about the system design, you better care about the team organization as well! In fact, you should make sure that you organize your teams in such a way that you can efficiently pursue a satisfactory system design. The Team Topologies book discusses this at some length, including the perils of being ignorant of Conway’s law when organizing your teams.

Deliberately creating an organization that will produce the system architecture you want is sometimes called “The inverse Conway manuever”. This always struck me as odd, because I can’t see anything inverse about it. To me, it’s just “having read Conway’s paper”! It’s still the same force acting in the same direction: deriving a system design from an organization structure. You’re just trying to be in control and use that force for good.

Anyway, I think we’re still only looking at half the picture. We can’t really step outside reality and coolly and objectively consider the ideal organization to produce the ideal system design. We are always entrenched in reality, which means we are part of some existing organization, and chances are there is also an existing system! This is the system we’re working on as developers or designers or architects or whatever. And of course, Conway’s law will have been in effect, so the communication patterns of the organization will have their counterparts in the system as well.

In this situation, there is an actual inverse force at work. The very existence of the system mandates that your organization communicates along certain paths – the communication paths of the system! So this is a force that tries to mimic the communication patterns of the system in the communication patterns of the organization. Not only is the organization shaping the system, the system is shaping the organization as well. This is a reinforcing loop.

Allan Kelly calls this “the homomorphic force“, where homomorphic means “having the same shape”. It is a bi-directional force ensuring that the organization structure and the system architecture stay in sync. If you try to change either one, communication will suffer. It is a very stable and change-resistant constellation. The inertia of the organization will prevent changes to the system, and the inertia of the system will prevent changes to the organization.

This is potentially very bad! Sometimes we want and need change! (XP said embrace change!) How can we make that happen? What kind of organizational juggernaut has the energy required to overcome “the homomorphic force”?

I’m glad you asked! Let’s switch gears and talk about mob programming.

Mob programming works roughly like this. You gather a group of people, ideally a cross-functional group of people, in a room with a big screen and a keyboard. (Complimentary skills are a great asset in this setting, because it means that the mob can handle more kinds of challenges.) There is a single person sitting at the keyboard and typing and the rest of the group is telling the typist what to do. At fairly short intervals (10-15 minutes, perhaps) the roles rotate. Everyone gets to be a typist at regular intervals.

I think there are some good reasons to be sort of strict about the mob programming routine. One thing is that it keeps everyone focused on the task at hand, because they know they will be typing soon. Also, if you have a mob consisting of people who haven’t worked together before, having a structured, role-based, semi-formal mode of working can help overcome some initial awkwardness as you gain trust and get to know each other. And finally it counters some potential bad group dynamics, e.g. maybe someone really wants that keyboard or decide that should be done or whatever.

There are many potential make-ups of such mobs, that is, we can put them together in various ways for various purposes. We can distinguish, for instance, between team mobs (where the participants all belong to the same team) and inter-team mobs (where there are participants from multiple teams). A team mob is – quite naturally – appropriate for features that can be implemented by a single team. If you have teams that are closely aligned with the value streams in your organization, you may often find yourself in this situation. But in other cases, you may find that multiple teams need to be involved to implement a feature. There can be many reasons for this. For instance, if your organization makes a software product that is available on multiple platforms such as iOS, Android and desktop, you may have separate teams for each of those platforms. If you want to launch a new feature across all your different platforms, you’ll need to coordinate and communicate across team boundaries.

In such cases, communication really is the bottleneck. Communication is almost always the bottleneck in software development, but within a single team, communication bandwidth tends to be high (maxing out at a team size of one, of course) and so it’s less noticable. In cross-team development efforts the communication challenges become more pronounced. It’s all too easy to end up in a situation where the different teams are doing a lot of handoffs, constantly waiting for each other, talking past each other, misunderstanding each other, not talking to each other when they really should be talking to each other and so on and so forth. It’s practically inevitable.

The problem with communication is that it rots. I mean that quite literally: we forget what we’ve been talking about. This is not much of a problem when you have constant communication, because you’re constantly reminded. But when the communication stops and starts at irregular intervals, it can be a disaster. We may talk together, but then we forget and our shared understanding rots, our agreements rot, our promises rot. The consequence is that it can be very difficult to get anything done, because we need a critical mass of sustained, shared understanding in order to be able to implement a feature in a meaningful way.

Since you’re a perceptive reader, you’ll no doubt have noticed that we are back to talking about communication! Just like with Conway’s law! If communication is the bottleneck and we notice that our communication patterns are inadequate and don’t allow us to implement a feature efficiently, we need to change our communication patterns!

One way of doing that is to form a inter-team mob to implement a feature across team boundaries. That is going to give us high-bandwidth, sustained communication focused on the feature. In a sense, the mob becomes a temporary feature team. Note that membership in the mob doesn’t need to be fixed, participants may vary over the lifetime of the mob. The important part is that the mob has the knowledge and resources it needs to implement the feature at all times. The mob is more important than the individual participants in the mob.

The great thing about such a mob is that it frees itself from having to work within the communication pattern structure dictated by “the homomorphic force”. It can work across both team and subsystem boundaries. That’s why I like to think of it as Conway’s mob.

I am not much of a sci-fi person, but I’ve watched just enough Star Trek to know that there is something there called “The Borg“. The Borg is a collective of cybernetic organisms with a single hive mind. The Borg acquire the technology and knowledge of other alien species through a process known as “assimilation”. And that’s how Conway’s mob can work as well. Whenever the mob encounters a team boundary, needs to communicate someone new, needs to acquire new knowledge to solve a problem, it can simply invite people in to join the mob.

If you work like this, you may notice a remarkable effect. I have. We are so used to, I think, that cross-team features run into friction and delays at the team boundaries, that it feels weird when it doesn’t happen. It’s almost as if something is wrong, or that we’re cheating. You get the feeling that you’re practically unstoppable. It’s exhilarating. It has so much momentum. There is so much competence and knowledge in the mob room at all times that things never grind to a halt. There is so much decision-making power as well that decisions can be made directly, because everyone that needs to be involved in the decision is already present. You have the discussion right there in the mob room, make a decision and move on. There is no need call meetings a week in advance, hope that everyone shows up, hopefully reach a conclusion before the meeting is over, and then forget what you agreed upon afterwards. It’s remarkably efficient and a lot of fun.

Too much? Too good to be true? Not buying it?

Obviously nothing is ever perfect. Mob programming is not magic. Crossing team boundaries is always difficult. People are different, not everyone shares the same perspective or the same experience. You are likely to encounter some issues, challenges and concerns if you try to launch a Conway’s mob to take on “the homomorphic force” and work across the dual boundaries of teams and subsystems. That’s to be expected.

One concern has to do with priorities. How do you split your time? If you’re member of a cross-team mob, chances are you’re also a member of a long-term team that is aligned with existing subsystems. Those teams in turn have their own priorities – other features, refactorings, bug fixes, all kinds of stuff. How can we just put all of those things on hold while we work on the one feature that the mob is working on?

To my mind, this is really an alignment issue, which in turn means it’s a communication issue. (Yes, again. Communication – is there anything it can’t do?) Do we agree, across teams, that the feature the mob is working on takes precedence? That’s obviously a very important question to ask, but it’s not like it’s impossible to answer. For the product, there is a single answer to that question. It’s a great question to ask a product owner. If the answer is no, then the mob’s work will be much more difficult. Perhaps a cross-team mob shouldn’t be formed at all. But if the answer is yes? Problem solved, go ahead! Whatever the answer, the question should be asked only once. There is no need to revisit it. If the question keeps recurring, that’s yet another communication issue! Someone didn’t hear the answer, or didn’t like it.

A related concern has to do with productivity. In my experience, and contrary to what one might think, it’s primarily developers who are concerned with this. Not so much team leads or product owners. (Perhaps I’ve just been lucky with team leads and product owners, but there you go.) Some developers are likely to feel that the mob is inefficient or unnecessary. The feature could have been split according to team boundaries, each team would do their part, we could work in parallel – much more efficient. Of course I think they’re wrong because I believe very strongly that progress is not going to be bound by typing speed, but by communication bandwidth. But I think I understand where this viewpoint comes from. Ten, fifteen years of sprinting and standups and Jira tickets and burndown charts will do that to you. The productivity thing gets under your skin. We’ve been told over and over again that the ideal situation for a developer is to “get in the zone” and stay there uninterrupted, closing Jira tickets. But we risk suboptimization when we work like that. Productivity is fine, but it should always be focused on the product, not individual developers or individual teams.

And finally, some developers may feel that mob programming is fine, but that team mobs are much better than intra-team mobs. Intra-team mobs has too much overhead and friction and it’s just not worth it. And again, I understand where this sentiment comes from. It’s true, intra-team mobs involve much more overhead and friction that team mobs. But I think this view misses something important. It’s too narrow in scope. We’re taught that friction is always a bad thing, that it is waste. But friction can also be a sign of change. We’re encountering friction and overhead in cross-team mobs because we’re changing communication patterns. We need to spend time to establish trust and even develop a shared language. We need to be able to understand each other. This work never ends up on Jira tickets. But it can still be valuable and important.

We need to remember that the systems we are working on are sociotechnical systems, and that we are part of those systems. We are not just building the software, we are also building the organization that builds the software. When we are changing our communication patterns we are in fact refactoring a sociotechnical system. I think mob programming – in particular using intra-team mobs – can be a great vehicle in bringing about change in sociotechnical systems, since they can free themselves from established patterns of communciation.