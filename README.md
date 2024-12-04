Eclipse Handly™
==============

The Eclipse Handly™ project provides basic building blocks for handle-based
models, with an emphasis on language-specific source code models of the
underlying Eclipse workspace. It allows creating highly scalable, robust,
and thoroughly optimized models similar in design principles to the
tried-and-tested Java model of Eclipse Java development tools while reducing
programming effort, fostering software reuse, and enabling interoperability.

Handly is designed for flexibility and can be used to create source code models
for practically any language, whether general-purpose or domain-specific; it is
compatible with any parsing technology. The model implementor has complete
control over the model's base-level API, including the ability to implement a
preexisting handle-based model API. At the same time, the provided uniform
meta-level API establishes a common language and makes it possible for IDE
components to work in a generic way with any Handly-based model.

Background
----------

Handle-based models employ a variation of the handle/body idiom, where clients
have direct access only to *handles* that act like a key to a model element and
have the following principal characteristics:

- Immutable, equal by value.

- Can define behavior of the element, but don't keep any element state beyond
the key information. The element state beyond the key information is stored
separately in an internal *body*.

- Can refer to non-existing elements.

Such design has a number of important properties:

- Handles are stable, you can freely keep references to them.

- Handles are lightweight, but can be rich in behavior.

- Bodies can be virtualized and computed on demand.

This makes handle-based models highly scalable and perfectly suited to
presenting in Eclipse views such as Project Explorer, Search, Outline, etc.

It is surely not a coincidence that handle-based models are an important
ingredient in the Eclipse IDE. The handle-based resource model of the Eclipse
workspace provides a common low-level foundation for language-specific
development tools. The handle-based Java model, which wraps the workspace
resource model and renders it from the Java language's angle, is one of the
pillars of Eclipse Java development tools (JDT). To a great extent, it is the
Java model that makes possible seamless tool integration and unified user
experience in JDT. Meanwhile, models with design properties similar to those of
the Java model can play an equally important role in Eclipse-based development
tools for other languages, as illustrated by the C model of Eclipse C/C++
Development Tooling (CDT).

Why Handly?
-----------

Traditionally, handle-based models such as the JDT Java model or the CDT C
model were built either entirely from scratch or by copying and modifying,
with due consideration of possible licensing issues, the source code of a
preexisting model. The traditional process required much effort, was tedious
and error-prone. The resulting models were effectively silos with a completely
isolated API, which prevented a possibility of developing reusable IDE
components around those models, although the models did seem to have certain
traits in common.

The Handly project begs to differ with the traditional approach. It aims to
reduce programming effort, foster software reuse, and enable interoperability
by providing a unified architecture and a set of basic building blocks for
handle-based models, with an emphasis on language-specific source code models
for Eclipse-based development tools. The provided implementation allows
creating highly scalable, robust, and thoroughly optimized models on a par with
the JDT Java model in quality.

Handly aims to retain much of flexibility of the traditional approach and can
be used to create source code models for practically any language, whether
general-purpose or domain-specific, as distinct from other existing efforts
such as Eclipse Dynamic Languages Toolkit (DLTK). It is compatible with any
parsing technology. The model implementor has complete control over the model's
base-level API, including the ability to implement a preexisting handle-based
model API that needs to be preserved for backward compatibility.

At the same time, any model that is based on Handly can be uniformly accessed
via a common meta-level API, which makes it possible to develop generic IDE
components that will work with any Handly-based model. To demonstrate utility
of this API and enhance value proposition for adopters, the project has
provided a number of production-quality UI components that work with
Handly-based models, such as a Common Outline Framework.

What's in the Box?
------------------

- Common interfaces and skeletal implementations for handle-based model
elements, including code-centric ones such as source files and source elements.

- Common interfaces and default implementations for change notifications in a
Handly-based model.

- Infrastructure interfaces and implementations for buffer and cache
management.

- Comprehensive working copy support, including integration with the Xtext
editor and support for integrating other source editors.

- Common UI components such as an outline framework, quick outline, and
navigator support that work with Handly-based models.

- Exemplary implementations, including a basic Xtext-based example and a more
advanced Java model example.

More Information
----------------

- [Success Stories](https://wiki.eclipse.org/Handly/Adopters): Handly has been
successfully used in large-scale commercial products, as demonstrated in
success stories related by our adopters.

- [Getting Started Tutorial](https://github.com/pisv/gethandly/wiki):
 A comprehensive step-by-step guide hosted on GitHub and made available under
EPL-2.0.

- [Architectural Overview](https://www.eclipse.org/downloads/download.php?file=/handly/docs/handly-overview.pdf&r=1):
 An overview of the Handly core framework.

- [Contributing](CONTRIBUTING.md): How to report bugs, set up a developer
workspace, build locally, contribute a patch, etc.

Contacts
--------

- Website: <https://eclipse.org/handly>

- Community Discussions: <https://github.com/eclipse-handly/handly/discussions>

- Developer Mailing List: <https://dev.eclipse.org/mailman/listinfo/handly-dev>

License
-------

Eclipse Handly is licensed under the Eclipse Public License 2.0.
See [LICENSE](LICENSE.txt) for the full license text.
