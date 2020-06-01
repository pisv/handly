Basic Example
=============

The basic example (`o.e.handly.examples.basic*`) demonstrates a full-featured 
Handly-based model for a simple Xtext-based language, complete with automated 
tests, content outline, and a navigator view.

Launch runtime Eclipse and open the Resource perspective, then create a new
project using the `Foo Project` wizard. The wizard is in the `Handly Examples`
category.

Create a new file in the root of the project you've just created; the filename 
must have the `.foo` extension. For example, create the `Module1.foo` file 
with the following contents:

```
var x;
var y;
def f() {}
def f(x) {}
def f(x, y) {}
```

Play with the Handly-based Outline view a little.

Open the `Foo Navigator` view (under the `Handly Examples` category)
and give it a try. Don't forget to expand Foo source files in the view
to show their inner elements, and try out the `Link with Editor` functionality.
Edit a Foo file and see how changes in its inner structure are reflected
in the Foo Navigator alongside the Outline view. (The navigator view
is built with Eclipse Common Navigator Framework and Handly.)

Now you might want to study the implementation. It is probably a good idea 
to begin with `o.e.handly.examples.basic.ui.tests` (that can be run with 
the same-named predefined launch configuration) and work from there toward 
`o.e.handly.examples.basic.ui`.
