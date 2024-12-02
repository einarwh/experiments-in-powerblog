:page/title Launch as admin
:blog-post/tags [:tech :programming :tools :visual-studio :windows]
:blog-post/author {:person/id :einarwh}
:blog-post/published #time/ldt "2011-09-27T17:17:00"
:page/body

# Launch as admin

Posted: September 27, 2011

Ignorance can hurt you for weeks and months, by forcing you to go through inefficient, mundane and - here's the main thing - _unnecessary_ hoops to accomplish every-day, common-place things. Like launching Visual Studio with administrative rights.

First off, I use [Launchy](http://www.launchy.net/) to do pretty much anything (and you should too). Certainly I use it to launch Visual Studio. Every day. Thanks to Launchy, I don't have to take my spidery fingers off [my lovely keyboard](http://www.daskeyboard.com/) at all. No feeding that gluttenous RSI monster by entering mouse-pointing mode. I just hit ALT+SPACE (to bring up Launchy), type "10", press ENTER and Visual Studio is on its way. (Launchy has figured out that when I type "10" I really mean Visual Studio 2010.) Total of five keystrokes. Good stuff, not much room for improvement there.

![Launchy about to launch Visual Studio 2010.](/images/launchy-10.png)

Here's the catch, though: Sometimes you need to launch Visual Studio with administrative rights. How do you launch an application with administrative rights? Why, you right-click on the short-cut and choose "Run as administrator", of course.

![Launching Visual Studio 2010 as admin manually.](/images/launch-as-admin-manual.png)

Suddenly I'm tossed out of my stream-lined keyboard-centric work flow, and forced to go chasing that pesky mouse. Ouch.

For days and weeks and months I suffered through that, growing ever-so-slightly more annoyed each time. Finally I couldn't take it anymore and invested the five minutes it took to find a solution. Turns out it's facepalm-inducingly simple to fix.

See, there's a little checkbox you can tick off, which says that the shortcut should launch the application as administrator. To find the checkbox, choose _Properties_ for the shortcut and click the _Advanced_ button. You'll see this:

![Ticking off the checkbox for running Visual Studio 2010 as administrator.](/images/visual-studio-run-as-admin.png)

So that really fixes the issue at hand right there. However, I wanted to keep the ability to launch Visual Studio as non-administrator as well, since most of the time I don't need the administrative rights. And you know, when launching as administrator, you have to pause to jump through the UAC hoop. Luckily, you don't need a mouse to do that, but still.

So I ended up cloning the shortcut, which is located at "C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Microsoft Visual Studio 2010". The clone has the same name as the original, with an "a" (for "admin") tacked on at the end.

![Creating an extra shortcut for running Visual Studio 2010 as administrator.](/images/visual-studio-extra-shortcut.png)

Now I can hit ALT+SPACE, type "10a", press ENTER, wait a second or two for the UAC to pop up (great UX, thx Microsoft), confirm the launch, and Visual Studio 2010 is on its merry way, running as administrator. Like so:

![Launchy about to launch Visual Studio 2010 as administrator.](/images/launchy-10a.png)

Good stuff, at last.