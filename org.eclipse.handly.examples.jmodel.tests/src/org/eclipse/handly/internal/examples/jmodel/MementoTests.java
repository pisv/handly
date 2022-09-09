/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.core.tests.model.MementoTests)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.handly.examples.jmodel.ICompilationUnit;
import org.eclipse.handly.examples.jmodel.IField;
import org.eclipse.handly.examples.jmodel.IImportContainer;
import org.eclipse.handly.examples.jmodel.IImportDeclaration;
import org.eclipse.handly.examples.jmodel.IJavaElement;
import org.eclipse.handly.examples.jmodel.IJavaProject;
import org.eclipse.handly.examples.jmodel.IMethod;
import org.eclipse.handly.examples.jmodel.IPackageDeclaration;
import org.eclipse.handly.examples.jmodel.IPackageFragment;
import org.eclipse.handly.examples.jmodel.IPackageFragmentRoot;
import org.eclipse.handly.examples.jmodel.IType;
import org.eclipse.handly.examples.jmodel.JavaModelCore;

import junit.framework.TestCase;

/**
 * Java element handle memento tests.
 */
public class MementoTests
    extends TestCase
{
    /**
     * Tests that a compilation unit can be persisted and restored using its memento.
     */
    public void testCompilationUnitMemento1()
    {
        ICompilationUnit cu = getCompilationUnit("P", "src", "p", "X.java");
        assertMemento("=P/src<p{X.java", cu);

        cu = getCompilationUnit("P", "src", "", "Y.java");
        assertMemento("=P/src<{Y.java", cu);
    }

    /**
     * Tests that an import declaration can be persisted and restored using its memento.
     */
    public void testImportContainerMemento()
    {
        IImportContainer importContainer = getCompilationUnit("P", "src", "p",
            "X.java").getImportContainer();
        assertMemento("=P/src<p{X.java#", importContainer);
    }

    /**
     * Tests that an import declaration can be persisted and restored using its memento.
     */
    public void testImportDeclarationMemento()
    {
        IImportDeclaration importDecl = getCompilationUnit("P", "src", "p",
            "X.java").getImport("java.io.Serializable");
        assertMemento("=P/src<p{X.java#java.io.Serializable", importDecl);

        importDecl = getCompilationUnit("P", "src", "p", "X.java").getImport(
            "java.util.*");
        assertMemento("=P/src<p{X.java#java.util.*", importDecl);
    }

    /**
     * Tests that a package declaration can be persisted and restored using its memento.
     */
    public void testPackageDeclarationMemento()
    {
        IPackageDeclaration declaration = getCompilationUnit("P", "src", "p",
            "X.java").getPackageDeclaration("p");
        assertMemento("=P/src<p{X.java%p", declaration);

        declaration = getCompilationUnit("P", "src", "p1.p2",
            "X.java").getPackageDeclaration("p1.p2");
        assertMemento("=P/src<p1.p2{X.java%p1.p2", declaration);
    }

    /**
     * Tests that a package fragment can be persisted and restored using its memento.
     */
    public void testPackageFragmentMemento()
    {
        IPackageFragment pkg = getPackage("P", "src", "p");
        assertMemento("=P/src<p", pkg);

        pkg = getPackage("P", "src", "p1.p2");
        assertMemento("=P/src<p1.p2", pkg);

        pkg = getPackage("P", "src", "");
        assertMemento("=P/src<", pkg);
    }

    /**
     * Tests that a source folder package fragment root can be persisted and restored using its memento.
     */
    public void testPackageFragmentRootMemento1()
    {
        IPackageFragmentRoot root = getPackageFragmentRoot("P", "src");
        assertMemento("=P/src", root);
    }

    /**
     * Test that a package fragment root name starting with '!' can be reconstructed from its memento.
     */
    public void testPackageFragmentRootMemento2()
    {
        IPackageFragmentRoot root = getPackageFragmentRoot("P", "!");
        assertMemento("=P/\\!", root);
    }

    /**
     * Tests that a project can be persisted and restored using its memento.
     */
    public void testProjectMemento()
    {
        IJavaProject project = getJavaProject("P");
        assertMemento("=P", project);
    }

    /**
     * Tests that a project with special chararcters in its name can be persisted and restored using its memento.
     */
    public void testProjectMemento2()
    {
        IJavaProject project = getJavaProject("P [abc] ~");
        assertMemento("=P \\[abc] \\~", project);
    }

    /**
     * Tests that a bogus memento cannot be restored.
     */
    public void testRestoreBogusMemento()
    {
        IJavaElement restored = JavaModelCore.create("bogus");
        assertEquals("should not be able to restore a bogus memento", null,
            restored);
    }

    /**
     * Tests that a source field can be persisted and restored using its memento.
     */
    public void testSourceFieldMemento()
    {
        IField field = getCompilationUnit("P", "src", "p", "X.java").getType(
            "X").getField("field");
        assertMemento("=P/src<p{X.java[X^field", field);
    }

    /**
     * Tests that a source inner type, inner field and inner method can be persisted and restored
     * using mementos.
     */
    public void testSourceInnerTypeMemento()
    {
        IType innerType = getCompilationUnit("P", "src", "p", "X.java").getType(
            "X").getType("Inner");
        assertMemento("=P/src<p{X.java[X[Inner", innerType);
    }

    /**
     * Tests that a source method can be persisted and restored using its memento.
     */
    public void testSourceMethodMemento1()
    {
        IType type = getCompilationUnit("P", "src", "p", "X.java").getType("X");
        IMethod method = type.getMethod("foo", new String[] { "I",
            "Ljava.lang.String;" });
        assertMemento("=P/src<p{X.java[X~foo~I~Ljava.lang.String;", method);
    }

    /**
     * Tests that a source method can be persisted and restored using its memento.
     */
    public void testSourceMethodMemento2()
    {
        IType type = getCompilationUnit("P", "src", "p", "X.java").getType("X");
        IMethod method = type.getMethod("bar", new String[] { });
        assertMemento("=P/src<p{X.java[X~bar", method);
    }

    /**
     * Tests that a source method can be persisted and restored using its memento.
     */
    public void testSourceMethodMemento3()
    {
        IType type = getCompilationUnit("P", "src", "p", "X.java").getType("X");
        IMethod method = type.getMethod("fred", new String[] { "[Z" });
        assertMemento("=P/src<p{X.java[X~fred~\\[Z", method);
    }

    /**
     * Tests that a source type can be persisted and restored using its memento.
     */
    public void testSourceTypeMemento()
    {
        IType type = getCompilationUnit("P", "src", "p", "X.java").getType("X");
        assertMemento("=P/src<p{X.java[X", type);
    }

    private void assertMemento(String expected, IJavaElement element)
    {
        String actual = element.getHandleIdentifier();
        assertEquals("Unexpected memento for " + element, expected, actual);
        IJavaElement restored = JavaModelCore.create(actual);
        assertEquals("Unexpected restored element", element, restored);
        String restoredHandleIdentifier = restored.getHandleIdentifier();
        assertEquals("Unexpected memento for restored element " + restored,
            expected, restoredHandleIdentifier);
    }

    private ICompilationUnit getCompilationUnit(String projectName,
        String rootName, String packageName, String unitName)
    {
        return getPackage(projectName, rootName,
            packageName).getCompilationUnit(unitName);
    }

    private IPackageFragment getPackage(String projectName, String rootName,
        String packageName)
    {
        return getPackageFragmentRoot(projectName, rootName).getPackageFragment(
            packageName);
    }

    private IPackageFragmentRoot getPackageFragmentRoot(String projectName,
        String rootName)
    {
        IJavaProject javaProject = getJavaProject(projectName);
        return javaProject.getPackageFragmentRoot(
            javaProject.getProject().getFolder(rootName));
    }

    private IJavaProject getJavaProject(String name)
    {
        return JavaModelCore.getJavaModel().getJavaProject(name);
    }
}
