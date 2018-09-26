Handly Contributor Guide
========================

Welcome to Handly, a technology project at Eclipse,
and thanks for your interest in this project.

Project description
-------------------

Eclipse Handly provides basic building blocks for handle-based models,
with an emphasis on high-level code-centric models that render Eclipse
workspace from a programming language angle. It allows creating
highly scalable, robust, and thoroughly optimized models similar in quality
to the tried-and-tested Java model of Eclipse Java development tools
while reducing programming effort, fostering software reuse, and
enabling interoperability.

Handly is designed for flexibility and can be used to create source code models
for practically any language, whether general-purpose or domain-specific;
it is compatible with any parsing technology. The model implementor has
complete control over the model's base-level API, including the ability to
implement a preexisting handle-based model API. At the same time, the provided
uniform meta-level API establishes a common language and makes it possible
for IDE components to work in a generic way with any Handly-based model.

For more information, visit the project's website

- <https://www.eclipse.org/handly/>

Developer resources
-------------------

Information regarding source code management and builds

- <https://projects.eclipse.org/projects/technology.handly/developer>

Coding standards, bugs lifecycle, and more

- <https://wiki.eclipse.org/Handly/Committer_FAQ>

Building locally
----------------

The build is based on Maven and Tycho and is easy to run on a local machine:

 1. Make sure you have JDK 8 and Maven 3 installed. Both should be on the path.

 2. Clone the Handly Git repository and pick the desired branch.

 3. Open a shell to the Git workspace and execute

    $ cd releng

    $ mvn clean install

Once the build completes, the `repository/target` folder in the Git workspace
will contain a repository of build artifacts.

Setting up a developer workspace
--------------------------------

Handly currently uses `JavaSE-1.8` for compilation. Please add a matching JRE.
Perfect match (i.e. JDK 1.8) is recommended to avoid build warnings.

Handly employs specific Java code formatting conventions. Please import and use
the formatter profile from `tools/formatter.xml`. Note that the current profile
is intended to be used with the new formatter available since the Eclipse Mars
(4.5) release, and may produce suboptimal results if used with previous
Eclipse versions.

Import all projects from the Git workspace (don't search for nested projects)
and set the target platform using a `.target` file provided within the
`targets` project.

There are currently two target platforms defined. The base platform
(`base.target`) defines the base API level, whereas the latest platform
(`latest.target`) defines the latest functional level. Development tends
to be done against the base platform, while continuous integration tends
to be done against the latest platform.

Handly Examples define some auxiliary Xtext-based languages. Note that for
the MWE2 generator of the language infrastructure to work correctly, the Eclipse
installation and the target platform should contain the same version of Xtext.
A simple way to ensure that is to install Eclipse using one of the provided
`.p2f` files that can be found in the Git workspace `tools` folder. These
`.p2f` files are kept consistent with their namesake `.target` files.

Handly uses [API Tools](https://wiki.eclipse.org/PDE/API_Tools/User_Guide)
to assist developers in API maintenance by reporting API defects. This requires
setting an API baseline for the workspace. Please use the target platform defined
in the `baseline.target` file as the source of the default API baseline.

Contributing
------------

General process and policy for making a contribution

- <https://wiki.eclipse.org/Development_Resources/Contributing_via_Git>

Project-specific details and further references

- <https://wiki.eclipse.org/Handly/Committer_FAQ>

Eclipse Contributor Agreement
-----------------------------

Before your contribution can be accepted by the project, you need to have
signed the Eclipse Contributor Agreement (ECA)

- <https://www.eclipse.org/legal/ECA.php>

For more information, see ECA FAQ

- <https://www.eclipse.org/legal/ecafaq.php>

Contact
-------

Contact the project developers via the project's "dev" list

- <https://dev.eclipse.org/mailman/listinfo/handly-dev>

Search for bugs
---------------

This project uses Bugzilla to track ongoing development and issues

- <https://bugs.eclipse.org/bugs/buglist.cgi?product=Handly>

Create a new bug
----------------

Bug reporting FAQ

- <https://wiki.eclipse.org/Bug_Reporting_FAQ>

Be sure to search for existing bugs before you create another one

- <https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Handly>

Remember that contributions are always welcome!