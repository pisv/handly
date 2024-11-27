Contributing to Eclipse Handly™
==============================

Welcome to Eclipse Handly™, and thanks for your interest in this project.
Big or small, every contribution matters.

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

- <https://projects.eclipse.org/projects/technology.handly>

Terms of Use
------------

This repository is subject to the Terms of Use of the Eclipse Foundation

- <https://www.eclipse.org/legal/termsofuse.php>

Developer resources
-------------------

Information regarding source code management and builds

- <https://projects.eclipse.org/projects/technology.handly/developer>

The project issues and source code are maintained in the following
GitHub repository

- <https://github.com/eclipse-handly/handly>

In the past, the project used Bugzilla to track ongoing development and
issues, which is preserved for the sake of history

- <https://bugs.eclipse.org/bugs/buglist.cgi?product=Handly>

Building locally
----------------

The build is based on [Apache Maven](https://maven.apache.org/) and
[Eclipse Tycho](https://www.eclipse.org/tycho/) and is easy to run
on a local machine:

 1. Make sure you have JDK 17 and Maven 3.9.0 or newer installed.
 Both should be on the path.

 2. Make sure you have a local clone of the Handly Git repository.

 3. Open a shell to the local clone of the Handly Git repository and execute

    `$ cd releng`

    `$ mvn clean verify`

Once the build completes, the `repository/target` folder will contain
a repository of build artifacts.

Setting up a developer workspace
--------------------------------

Handly currently uses `JavaSE-17` for compilation. Please add a matching JDK.

Handly employs specific Java code formatting conventions. Please import and use
the formatter profile from `tools/formatter.xml`.

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

Eclipse Development Process
---------------------------

This project is governed by the Eclipse Foundation Development Process and
operates under the terms of the Eclipse IP Policy.

- <https://eclipse.org/projects/dev_process>

- <https://www.eclipse.org/org/documents/Eclipse_IP_Policy.pdf>

Eclipse Contributor Agreement
-----------------------------

In order to be able to contribute to Eclipse Foundation projects you must
electronically sign the Eclipse Contributor Agreement (ECA)

- <https://www.eclipse.org/legal/ECA.php>

The ECA provides the Eclipse Foundation with a permanent record that you agree
that each of your contributions will comply with the commitments documented in
the Developer Certificate of Origin (DCO). Having an ECA on file associated with
the email address matching the "Author" field of your contribution's Git commits
fulfils the DCO's requirement that you sign-off on your contributions.

For more information, please see the Eclipse Committer Handbook:
<https://www.eclipse.org/projects/handbook/#resources-commit>

Contact
-------

Contact the project developers via the project's "dev" list.

- <https://dev.eclipse.org/mailman/listinfo/handly-dev>
