Handly Examples
===============

Some examples of Handly usage are provided in bundles under the 
common `org.eclipse.handly.examples` namespace. Each example has its own 
namespace (e.g., `o.e.handly.examples.basic`) and the corresponding 
set of bundles. The root bundle for every example contains the 
accompanying `README.md` file. There are predefined launch configurations
for launching runtime Eclipse and plug-in tests.

You might want to clone the project's Git repository and set up 
a developer workspace to be able to play with the examples most 
productively. Please see the project's Developer Resources for 
information on setting things up.

- <https://projects.eclipse.org/projects/technology.handly/developer>

Another option is to materialize example projects in your workspace using
wizards under the `Handly Examples` category. Make sure that you have the
`org.eclipse.handly.examples` feature installed, then use `File -> New ->
Example...` to select the appropriate wizard. The root project for every
example contains the accompanying `example.target` file you can use to easily
set a target platform for the example. Where necessary, the `example.p2f`
file is also provided that describes software items you need to have installed
in your IDE to be able to play with the example most productively. To install it,
use `File -> Import... -> Install Software Items From File`.
