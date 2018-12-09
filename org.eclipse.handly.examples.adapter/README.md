Adapter Example
===============

The adapter example (`o.e.handly.examples.adapter*`) demonstrates
a Handly-based model that plays the role of an adapter for the JDT Java model.
Put differently, it implements the uniform Handly API on top of the Java model.
The adapter model is then used to implement an outline page for the Java editor.
Notably, the Handly-based outline page re-uses content- and label providers
supplied by JDT. This is made possible by the model adaptation facility
(<https://bugs.eclipse.org/472840>).

To try it out, launch runtime Eclipse, open a Java compilation unit with the
"Java Editor (Handly Adapter Example)", and play with the outline.

Things to note:

* Since the outline's content is based on the JDT Java model,
  the standard label provider for Java elements just works, and
  some JDT actions such as "Toggle Class Load Breakpoint" and
  "Toggle Method Breakpoint" are automatically available in
  the outline's context menu.

* The outline page is Handly-based and knows nothing about
  the Java model API. It makes use of a content adapter to deal
  with the JDT-based content, e.g. for "Link with Editor" support.
  The content adapter defines a one-to-one correspondence (bijection)
  between Java elements and the adapter model.

Handly 1.1 introduced support for building a search result view page and
a call hierarchy view. The adapter example demonstrates that support. Use
"Show Calls" and "Find References" actions in the outline's context menu
to open the example's call hierarchy view and search result page respectively.
