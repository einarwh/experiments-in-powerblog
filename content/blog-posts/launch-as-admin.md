:page/title Launch as admin
:blog-post/tags [:tech :programming :tools :visual-studio :windows]
:blog-post/author {:person/id :einarwh}
:page/body

# Launch as admin

Posted: September 27, 2011

Ignorance can hurt you for weeks and months, by forcing you to go through inefficient, mundane and – here’s the main thing – unnecessary hoops to accomplish every-day, common-place things. Like launching Visual Studio with administrative rights.

First off, I use Launchy to do pretty much anything (and you should too). Certainly I use it to launch Visual Studio. Every day. Thanks to Launchy, I don’t have to take my spidery fingers off my lovely keyboard at all. No feeding that gluttenous RSI monster by entering mouse-pointing mode. I just hit ALT+SPACE (to bring up Launchy), type “10”, press ENTER and Visual Studio is on its way. (Launchy has figured out that when I type “10” I really mean Visual Studio 2010.) Total of five keystrokes. Good stuff, not much room for improvement there.

TODO: Image: Launchy-10

Here’s the catch, though: Sometimes you need to launch Visual Studio with administrative rights. How do you launch an application with administrative rights? Why, you right-click on the short-cut and choose “Run as administrator”, of course.
Launch-as-admin-manual

Suddenly I’m tossed out of my stream-lined keyboard-centric work flow, and forced to go chasing that pesky mouse. Ouch.

For days and weeks and months I suffered through that, growing ever-so-slightly more annoyed each time. Finally I couldn’t take it anymore and invested the five minutes it took to find a solution. Turns out it’s facepalm-inducingly simple to fix.

See, there’s a little checkbox you can tick off, which says that the shortcut should launch the application as administrator. To find the checkbox, choose Properties for the shortcut and click the Advanced button. You’ll see this:

TODO: Image: Admin-visual-studio-run-as-admin

So that really fixes the issue at hand right there. However, I wanted to keep the ability to launch Visual Studio as non-administrator as well, since most of the time I don’t need the administrative rights. And you know, when launching as administrator, you have to pause to jump through the UAC hoop. Luckily, you don’t need a mouse to do that, but still.

So I ended up cloning the shortcut, which is is located at C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Microsoft Visual Studio 2010. The clone has the same name as the original, with an “a” (for “admin”) tacked on at the end.

TODO: Image: Admin-visual-studio-shortcut-smaller

Now I can hit ALT+SPACE, type “10a”, press ENTER, wait a second or two for the UAC to pop up, confirm the launch, and Visual Studio 2010 is on its merry way, running as administrator. Like so:

TODO: Image: Launchy-10a

Good stuff, at last.