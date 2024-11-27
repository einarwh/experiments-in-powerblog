:page/title Telepathic computer control with Launchy
:blog-post/tags [:tech :programming :tools :windows]
:blog-post/author {:person/id :einarwh}
:page/body

# Telepathic computer control with Launchy

Posted: September 25, 2011

You’d think that, as a professional computer user, I’d have all kinds of fancy tricks up my sleeve to help me command the dang thing. But I don’t. Maybe I should, but I don’t. In fact, using Launchy is my one claim to power-user-dom.

Launchy is a magical software artifact that allows you to translate thoughts to computer commands. The thoughts travel at light speed through my hands directly into the CPU. No kidding.

A more mundane description would be that it allows you to launch applications using the keyboard alone, optionally passing in parameters to the application as well (the official label being “an open-source keystroke launcher”). Therein lies great power.

Launchy runs in the background on my computer, listening for me to hit ALT+SPACE. This brings up an innocuous prompt, floating on top of any applications, in the middle of my screen:

TODO: Image: Launchy-prompt

When the prompt pops up, I’ll do one of three things:

1. Type the name of an application.
2. Type a command.
3. Type the path of a folder.

It might not look like much, but it is actually incredibly empowering. In fact, I feel severely limited when I’m using a machine without Launchy, like I’m trapped in a slow motion movie. It’s so ingrained my workflow that I’ll invariably hit ALT+SPACE followed by some command every now and then. Of course, this has arbitrary effects on the machine, since Launchy isn’t there to capture the keystrokes following ALT+SPACE. (When that happens, I recoil into the fetal position out of fear that I’ve unwittingly triggered some fatal process on the computer. Luckily I never have. I guess hitting a few letters and possibly a TAB doesn’t have much potential for wreaking havoc.)

Anyways, let’s take a look at each item in the list in turn.

## Type the name of an application

Actually, I won’t do that. I’ll write some shorthand that Launchy is clever enough to interprete as an application name. For instance, I’ll type “ff” to launch Firefox. (Yeah, I know that “fx” is the official abbreviation. I also don’t care.) It looks like this:

TODO: Image: Launchy-ff

Sometimes, I won’t hit ENTER directly – instead I’ll hit TAB and type in some more text first. The extra text will be interpreted as a parameter to the application. For instance, I can type “ff TAB http://www.launchy.net” to go to the Launchy website.

## Type a command

That’s really just half the story. I’ll type in the name of a command, followed by TAB and some text. I use this mostly for search (and quick web navigation in general, see below). I launch a Google search by typing “g TAB search term“. For instance, to dig up some info on Windows Phone 7 and the MVVM pattern, I’d do this:

TODO: Image: Blog-wp7-mvvm

Launchy comes with “Google” as a pre-configured search option, but I’ve shortened it to “g” since it’s such a common task. The command will launch my default web browser and display the list of search results.

There are a bunch of other built-in search options as well, such as “Amazon”, “YouTube” and “Wikipedia”. I’ve added a few of my own as well, including “img” (which does a Google image search), “tlf” (which does a lookup based on a phone number using the GuleSider service) and “dokpro” (which is a Norwegian dictionary).

I also use a teeny-tiny but oh-so-useful hack to use Launchy and Google to navigate quickly to whatever web site I want: “j TAB search term“. The letter j is a mnemonic for “jump”. I’ve configured it in Launchys options pane (which you can bring up after right-clicking on the prompt):

TODO: Image: Launchy-options-jump-cut

As you can see, the j command is really just Google’s “I feel lucky” search (which is the regular search plus a query string option of “btnI”). I don’t really use it for searching per se, though. I use it to navigate to something without typing the actual URL. Maybe I don’t remember the exact URL, or maybe I’m just too lazy to type it in.

For example, say I want to navigate to Pluralsight’s web site. The actual URL is http://www.pluralsight-training.net, but I tend to forget that. Instead I type “j TAB pluralsight”, which takes me right there:
Blog-pluralsight

This launches my web browser and takes me to the Pluralsight web site, essentially using Google as a redirecting proxy.

## Type the path of folder

This is nothing fancy, but still pretty useful. To bring up any folder, just start typing in the path. Launchy will help by performing autocomplete, so I typically just have to type a few letters of each part of path. This should sum it up nicely:
Launchy-folder

So that’s it. A small bag of tricks to be sure, but what tricks they are! (Of course, you could go much further with Launchy if you’re so inclined. You could use it to maintain a TODO list, send messages to twitter, or even update the status of an issue in JIRA.)