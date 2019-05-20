Xtext-Xtext Example
===================

The Xtext-Xtext example (`o.e.handly.examples.xtext.xtext.ui`) is a fragment
to the `o.e.xtext.xtext.ui` plugin. It demonstrates a call hierarchy view
for the Xtext language. The plugin `o.e.xtext.xtext.ui` already contains a
call hierarchy view. The example fragment contributes an alternative view
created using a framework provided by Handly that is much reacher in
functionality.

To try it out, launch runtime Eclipse and open an Xtext grammar file (`.xtext`)
with the Xtext editor. (Hint: You can import one of the Xtext example projects
into your workspace if you don't have an `.xtext` file. To do so, use
`File -> New -> Example... -> Xtext Examples`)

Place the cursor on a rule name in the grammar editor, open the context menu
and choose "Open Call Hierarchy (Handly)". A `Rule Calls` view opens, with
the feature set on par with the standard JDT `Call Hierarchy` view, including
view history, pinning and layout support, among other things. Play with the
view to explore the functionality. Alternatively, you can play with the
default Xtext call hierarchy view. To open it, choose "Open Call Hierarchy"
from the context menu.
