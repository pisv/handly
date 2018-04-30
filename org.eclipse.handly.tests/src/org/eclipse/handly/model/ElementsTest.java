/*******************************************************************************
 * Copyright (c) 2017, 2018 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.impl.support.SimpleElement;
import org.eclipse.handly.model.impl.support.SimpleModel;
import org.eclipse.handly.model.impl.support.SimpleModelManager;
import org.eclipse.handly.model.impl.support.SimpleSourceConstruct;
import org.eclipse.handly.model.impl.support.SimpleSourceFile;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.snapshot.StaleSnapshotException;

import com.google.common.collect.Iterables;

import junit.framework.TestCase;

/**
 * <code>Elements</code> tests.
 */
public class ElementsTest
    extends TestCase
{
    private SimpleModelManager manager;
    private SimpleElement root;
    private SimpleSourceFile aFile;
    private SimpleElement foo;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        manager = new SimpleModelManager();
        root = new SimpleElement(null, "root", manager);
        aFile = new SimpleSourceFile(root, "aFile", null, manager);
        foo = new SimpleElement(aFile, "foo", manager);
    }

    public void testGetName()
    {
        assertEquals("root", Elements.getName(root));
        assertEquals("aFile", Elements.getName(aFile));
        assertEquals("foo", Elements.getName(foo));
    }

    public void testGetParent()
    {
        assertNull(Elements.getParent(root));
        assertEquals(root, Elements.getParent(aFile));
        assertEquals(aFile, Elements.getParent(foo));
    }

    public void testGetRoot()
    {
        assertEquals(root, Elements.getRoot(root));
        assertEquals(root, Elements.getRoot(aFile));
        assertEquals(root, Elements.getRoot(foo));
    }

    public void testGetParentChain()
    {
        assertFalse(Elements.getParentChain(null).iterator().hasNext());
        assertEquals(root, Elements.getParentChain(root).iterator().next());
        assertTrue(Iterables.elementsEqual(Arrays.asList(aFile, root),
            Elements.getParentChain(aFile)));
        assertTrue(Iterables.elementsEqual(Arrays.asList(foo, aFile, root),
            Elements.getParentChain(foo)));
    }

    public void testGetParentChainUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertFalse(Elements.getParentChainUntil(null,
            eqRoot).iterator().hasNext());
        assertFalse(Elements.getParentChainUntil(root,
            eqRoot).iterator().hasNext());
        assertEquals(aFile, Elements.streamParentChainUntil(aFile,
            eqRoot).iterator().next());
        assertTrue(Iterables.elementsEqual(Arrays.asList(foo, aFile),
            Elements.getParentChainUntil(foo, eqRoot)));
    }

    public void testStreamParentChain()
    {
        assertEquals(Collections.emptyList(), Elements.streamParentChain(
            null).collect(Collectors.toList()));
        assertEquals(Collections.singletonList(root),
            Elements.streamParentChain(root).collect(Collectors.toList()));
        assertEquals(Arrays.asList(aFile, root), Elements.streamParentChain(
            aFile).collect(Collectors.toList()));
        assertEquals(Arrays.asList(foo, aFile, root),
            Elements.streamParentChain(foo).collect(Collectors.toList()));
    }

    public void testStreamParentChainUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertEquals(Collections.emptyList(), Elements.streamParentChainUntil(
            null, eqRoot).collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), Elements.streamParentChainUntil(
            root, eqRoot).collect(Collectors.toList()));
        assertEquals(Arrays.asList(aFile), Elements.streamParentChainUntil(
            aFile, eqRoot).collect(Collectors.toList()));
        assertEquals(Arrays.asList(foo, aFile), Elements.streamParentChainUntil(
            foo, eqRoot).collect(Collectors.toList()));
    }

    public void testCollectParentChain()
    {
        assertEquals(Collections.emptyList(), Elements.collectParentChain(null,
            new ArrayList<>()));
        assertEquals(Collections.singletonList(root),
            Elements.collectParentChain(root, new ArrayList<>()));
        assertEquals(Arrays.asList(aFile, root), Elements.collectParentChain(
            aFile, new ArrayList<>()));
        assertEquals(Arrays.asList(foo, aFile, root),
            Elements.collectParentChain(foo, new ArrayList<>()));
    }

    public void testCollectParentChainUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertEquals(Collections.emptyList(), Elements.collectParentChainUntil(
            null, new ArrayList<>(), eqRoot));
        assertEquals(Collections.emptyList(), Elements.collectParentChainUntil(
            root, new ArrayList<>(), eqRoot));
        assertEquals(Arrays.asList(aFile), Elements.collectParentChainUntil(
            aFile, new ArrayList<>(), eqRoot));
        assertEquals(Arrays.asList(foo, aFile),
            Elements.collectParentChainUntil(foo, new ArrayList<>(), eqRoot));
    }

    public void testFindMatchingAncestor()
    {
        assertNull(Elements.findMatchingAncestor(null,
            e -> e instanceof SimpleElement));
        assertEquals(root, Elements.findMatchingAncestor(root,
            e -> e instanceof SimpleElement));
        assertEquals(root, Elements.findMatchingAncestor(aFile,
            e -> e instanceof SimpleElement));
        assertEquals(foo, Elements.findMatchingAncestor(foo,
            e -> e instanceof SimpleElement));
    }

    public void testFindMatchingAncestorUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertNull(Elements.findMatchingAncestorUntil(null,
            e -> e instanceof SimpleElement, eqRoot));
        assertNull(Elements.findMatchingAncestorUntil(root,
            e -> e instanceof SimpleElement, eqRoot));
        assertNull(Elements.findMatchingAncestorUntil(aFile,
            e -> e instanceof SimpleElement, eqRoot));
        assertEquals(foo, Elements.findMatchingAncestorUntil(foo,
            e -> e instanceof SimpleElement, eqRoot));
    }

    public void testFindAncestorOfType()
    {
        assertNull(Elements.findAncestorOfType(null, SimpleElement.class));
        assertEquals(root, Elements.findAncestorOfType(root,
            SimpleElement.class));
        assertEquals(root, Elements.findAncestorOfType(aFile,
            SimpleElement.class));
        assertEquals(foo, Elements.findAncestorOfType(foo,
            SimpleElement.class));
    }

    public void testFindAncestorOfTypeUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertNull(Elements.findAncestorOfTypeUntil(null, SimpleElement.class,
            eqRoot));
        assertNull(Elements.findAncestorOfTypeUntil(root, SimpleElement.class,
            eqRoot));
        assertNull(Elements.findAncestorOfTypeUntil(aFile, SimpleElement.class,
            eqRoot));
        assertEquals(foo, Elements.findAncestorOfTypeUntil(foo,
            SimpleElement.class, eqRoot));
    }

    public void testFindLastMatchingAncestor()
    {
        assertNull(Elements.findLastMatchingAncestor(null,
            e -> e instanceof SimpleElement));
        assertEquals(root, Elements.findLastMatchingAncestor(root,
            e -> e instanceof SimpleElement));
        assertEquals(root, Elements.findLastMatchingAncestor(aFile,
            e -> e instanceof SimpleElement));
        assertEquals(root, Elements.findLastMatchingAncestor(foo,
            e -> e instanceof SimpleElement));
    }

    public void testFindLastMatchingAncestorUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertNull(Elements.findLastMatchingAncestorUntil(null,
            e -> e instanceof SimpleElement, eqRoot));
        assertNull(Elements.findLastMatchingAncestorUntil(root,
            e -> e instanceof SimpleElement, eqRoot));
        assertNull(Elements.findLastMatchingAncestorUntil(aFile,
            e -> e instanceof SimpleElement, eqRoot));
        assertEquals(foo, Elements.findLastMatchingAncestorUntil(foo,
            e -> e instanceof SimpleElement, eqRoot));
    }

    public void testFindLastAncestorOfType()
    {
        assertNull(Elements.findLastAncestorOfType(null, SimpleElement.class));
        assertEquals(root, Elements.findLastAncestorOfType(root,
            SimpleElement.class));
        assertEquals(root, Elements.findLastAncestorOfType(aFile,
            SimpleElement.class));
        assertEquals(root, Elements.findLastAncestorOfType(foo,
            SimpleElement.class));
    }

    public void testFindLastAncestorOfTypeUntil()
    {
        Predicate<Object> eqRoot = Predicate.isEqual(root);
        assertNull(Elements.findLastAncestorOfTypeUntil(null,
            SimpleElement.class, eqRoot));
        assertNull(Elements.findLastAncestorOfTypeUntil(root,
            SimpleElement.class, eqRoot));
        assertNull(Elements.findLastAncestorOfTypeUntil(aFile,
            SimpleElement.class, eqRoot));
        assertEquals(foo, Elements.findLastAncestorOfTypeUntil(foo,
            SimpleElement.class, eqRoot));
    }

    public void testFindCommonAncestor()
    {
        SimpleElement bar = new SimpleElement(aFile, "bar", manager);
        SimpleElement baz = bar.getChild("baz");
        assertNull(Elements.findCommonAncestor(null, root));
        assertNull(Elements.findCommonAncestor(root, null));
        assertEquals(root, Elements.findCommonAncestor(root, root));
        assertEquals(root, Elements.findCommonAncestor(root, aFile));
        assertEquals(root, Elements.findCommonAncestor(root, foo));
        assertEquals(aFile, Elements.findCommonAncestor(aFile, foo));
        assertEquals(aFile, Elements.findCommonAncestor(foo, bar));
        assertEquals(aFile, Elements.findCommonAncestor(foo, baz));
        assertEquals(bar, Elements.findCommonAncestor(bar, baz));
    }

    public void testFindCommonAncestorUntil()
    {
        SimpleElement bar = new SimpleElement(aFile, "bar", manager);
        SimpleElement baz = bar.getChild("baz");
        Predicate<Object> eqAFile = Predicate.isEqual(aFile);
        assertNull(Elements.findCommonAncestorUntil(null, root, eqAFile));
        assertNull(Elements.findCommonAncestorUntil(root, null, eqAFile));
        assertEquals(root, Elements.findCommonAncestorUntil(root, root,
            eqAFile));
        assertNull(Elements.findCommonAncestorUntil(root, aFile, eqAFile));
        assertNull(Elements.findCommonAncestorUntil(root, foo, eqAFile));
        assertNull(Elements.findCommonAncestorUntil(aFile, foo, eqAFile));
        assertNull(Elements.findCommonAncestorUntil(foo, bar, eqAFile));
        assertNull(Elements.findCommonAncestorUntil(foo, baz, eqAFile));
        assertEquals(bar, Elements.findCommonAncestorUntil(bar, baz, eqAFile));
    }

    public void testFindCommonAncestor2()
    {
        SimpleElement bar = new SimpleElement(aFile, "bar", manager);
        SimpleElement baz = bar.getChild("baz");
        assertNull(Elements.findCommonAncestor(Collections.emptySet()));
        assertEquals(root, Elements.findCommonAncestor(Collections.singleton(
            root)));
        assertNull(Elements.findCommonAncestor(Arrays.asList(null, root)));
        assertNull(Elements.findCommonAncestor(Arrays.asList(root, null)));
        assertEquals(root, Elements.findCommonAncestor(Arrays.asList(root,
            root)));
        assertEquals(aFile, Elements.findCommonAncestor(Arrays.asList(foo,
            bar)));
        assertEquals(aFile, Elements.findCommonAncestor(Arrays.asList(foo, bar,
            baz)));
    }

    public void testFindCommonAncestorUntil2()
    {
        SimpleElement bar = new SimpleElement(aFile, "bar", manager);
        SimpleElement baz = bar.getChild("baz");
        Predicate<Object> eqAFile = Predicate.isEqual(aFile);
        assertNull(Elements.findCommonAncestorUntil(Collections.emptySet(),
            eqAFile));
        assertEquals(root, Elements.findCommonAncestorUntil(
            Collections.singleton(root), eqAFile));
        assertNull(Elements.findCommonAncestorUntil(Arrays.asList(null, root),
            eqAFile));
        assertNull(Elements.findCommonAncestorUntil(Arrays.asList(root, null),
            eqAFile));
        assertEquals(root, Elements.findCommonAncestorUntil(Arrays.asList(root,
            root), eqAFile));
        assertNull(Elements.findCommonAncestorUntil(Arrays.asList(foo, bar),
            eqAFile));
        assertNull(Elements.findCommonAncestorUntil(Arrays.asList(foo, bar,
            baz), eqAFile));
        assertEquals(bar, Elements.findCommonAncestorUntil(Arrays.asList(baz,
            bar.getChild("a"), baz.getChild("b")), eqAFile));
    }

    public void testIsAncestorOf()
    {
        assertFalse(Elements.isAncestorOf(root, null));
        assertTrue(Elements.isAncestorOf(root, root));
        assertTrue(Elements.isAncestorOf(root, aFile));
        assertFalse(Elements.isAncestorOf(aFile, root));
        assertTrue(Elements.isAncestorOf(root, foo));
        assertFalse(Elements.isAncestorOf(foo, root));
        assertTrue(Elements.isAncestorOf(aFile, aFile));
        assertTrue(Elements.isAncestorOf(aFile, foo));
        assertFalse(Elements.isAncestorOf(foo, aFile));
        assertTrue(Elements.isAncestorOf(foo, foo));
    }

    public void testRemoveDescendants()
    {
        SimpleElement bar = new SimpleElement(aFile, "bar", manager);
        SimpleElement baz = bar.getChild("baz");
        ArrayList<IElement> elements = new ArrayList<>();

        Elements.removeDescendants(elements);
        assertTrue(elements.isEmpty());

        elements.add(root);
        elements.add(root);
        Elements.removeDescendants(elements);
        assertEquals(Arrays.asList(root, root), elements);

        elements.clear();
        elements.add(root);
        elements.add(aFile);
        elements.add(aFile);
        elements.add(foo);
        Elements.removeDescendants(elements);
        assertEquals(Collections.singletonList(root), elements);

        elements.clear();
        elements.add(aFile);
        elements.add(foo);
        Elements.removeDescendants(elements);
        assertEquals(Collections.singletonList(aFile), elements);

        elements.clear();
        elements.add(bar);
        elements.add(baz);
        elements.add(foo);
        Elements.removeDescendants(elements);
        assertEquals(Arrays.asList(bar, foo), elements);
    }

    public void testIsOfModel()
    {
        assertTrue(Elements.isOfModel(root, manager.getModel()));
        assertFalse(Elements.isOfModel(root, new SimpleModel()));
    }

    public void testSplitIntoElementsAndResources()
    {
        List<IElement> elements = new ArrayList<>();

        Elements.splitIntoElementsAndResources(Collections.emptySet(), elements,
            null, null, null);
        assertTrue(elements.isEmpty());

        Elements.splitIntoElementsAndResources(Arrays.asList(root, null),
            elements, null, null, null);
        assertEquals(Collections.singletonList(root), elements);
        elements.clear();

        Elements.splitIntoElementsAndResources(Arrays.asList("abc", root),
            elements, null, null, null);
        assertEquals(Collections.singletonList(root), elements);
        elements.clear();

        SimpleModelManager manager2 = new SimpleModelManager();
        manager2.model = new SimpleModel();
        SimpleElement root2 = new SimpleElement(null, "root", manager2);

        Elements.splitIntoElementsAndResources(Arrays.asList(root, root2),
            elements, null, null, null);
        assertEquals(Arrays.asList(root, root2), elements);
        elements.clear();

        Elements.splitIntoElementsAndResources(Arrays.asList(root, root2),
            elements, manager.getModel(), null, null);
        assertEquals(Collections.singletonList(root), elements);
        elements.clear();

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
            "p");
        IFile file = project.getFile("abc");

        Elements.splitIntoElementsAndResources(Arrays.asList(root, project,
            file), elements, null, null, null);
        assertEquals(Collections.singletonList(root), elements);
        elements.clear();

        List<IResource> resources = new ArrayList<>();

        Elements.splitIntoElementsAndResources(Arrays.asList(root, project,
            file), elements, null, resources, null);
        assertEquals(Collections.singletonList(root), elements);
        assertEquals(Arrays.asList(project, file), resources);
        elements.clear();
        resources.clear();

        boolean[] abcExists = new boolean[1];
        SimpleSourceFile abc = new SimpleSourceFile(root, "abc", null, manager2)
        {
            @Override
            public boolean exists_()
            {
                return abcExists[0];
            }
        };
        IElementHandleFactory elementHandleFactory = new IElementHandleFactory()
        {
            @Override
            public IElement createFromResourceHandle(IResource resource)
            {
                if (file.equals(resource))
                    return abc;
                return null;
            }

            @Override
            public IElement createFromHandleMemento(String memento)
            {
                return null;
            }
        };

        Elements.splitIntoElementsAndResources(Arrays.asList(root, project,
            file), elements, null, resources, elementHandleFactory);
        assertEquals(Collections.singletonList(root), elements);
        assertEquals(Arrays.asList(project, file), resources);
        elements.clear();
        resources.clear();

        abcExists[0] = true;
        Elements.splitIntoElementsAndResources(Arrays.asList(root, project,
            file), elements, null, resources, elementHandleFactory);
        assertEquals(Arrays.asList(root, abc), elements);
        assertEquals(Collections.singletonList(project), resources);
        elements.clear();
        resources.clear();

        Elements.splitIntoElementsAndResources(Arrays.asList(root, project,
            file), elements, manager.getModel(), resources,
            elementHandleFactory);
        assertEquals(Collections.singletonList(root), elements);
        assertEquals(Arrays.asList(project, file), resources);
        elements.clear();
        resources.clear();
    }

    public void testGetSourceElementAt2()
    {
        class TestSourceFile
            extends SimpleSourceFile
        {
            int testMode;

            TestSourceFile()
            {
                super(null, null, null, manager);
            }

            @Override
            public ISourceElement getSourceElementAt_(int position,
                IContext context, IProgressMonitor monitor) throws CoreException
            {
                if (testMode == 1)
                    throw newDoesNotExistException_();
                else if (testMode == 2)
                    throw new CoreException(Activator.createErrorStatus("error",
                        null));
                else if (testMode == 3)
                    throw new StaleSnapshotException();
                else
                    return this;
            }

            @Override
            public boolean exists_()
            {
                return testMode != 1;
            }
        }
        TestSourceFile sourceFile = new TestSourceFile();
        assertSame(sourceFile, Elements.getSourceElementAt2(sourceFile, 0,
            null));
        sourceFile.testMode = 1;
        assertNull(Elements.getSourceElementAt2(sourceFile, 0, null));
        sourceFile.testMode = 2;
        assertNull(Elements.getSourceElementAt2(sourceFile, 0, null));
        sourceFile.testMode = 3;
        assertNull(Elements.getSourceElementAt2(sourceFile, 0, null));
    }

    public void testGetSourceElementInfo2()
    {
        class TestSourceFile
            extends SimpleSourceFile
        {
            int testMode;

            TestSourceFile()
            {
                super(null, null, null, manager);
            }

            @Override
            public ISourceElementInfo getSourceElementInfo_(IContext context,
                IProgressMonitor monitor) throws CoreException
            {
                if (testMode == 1)
                    throw newDoesNotExistException_();
                else if (testMode == 2)
                    throw new CoreException(Activator.createErrorStatus("error",
                        null));
                else
                    return new SourceElementBody();
            }

            @Override
            public boolean exists_()
            {
                return testMode != 1;
            }
        }
        TestSourceFile sourceFile = new TestSourceFile();
        assertNotSame(Elements.NO_SOURCE_ELEMENT_INFO,
            Elements.getSourceElementInfo2(sourceFile));
        sourceFile.testMode = 1;
        assertSame(Elements.NO_SOURCE_ELEMENT_INFO,
            Elements.getSourceElementInfo2(sourceFile));
        sourceFile.testMode = 2;
        assertSame(Elements.NO_SOURCE_ELEMENT_INFO,
            Elements.getSourceElementInfo2(sourceFile));
    }

    public void testNoSourceElementInfo()
    {
        ISourceElementInfo info = Elements.NO_SOURCE_ELEMENT_INFO;
        assertNull(info.getSnapshot());
        assertNull(info.getFullRange());
        assertNull(info.getIdentifyingRange());
        assertEquals(0, info.getChildren().length);
    }

    public void testGetSourceFile()
    {
        assertEquals(aFile, Elements.getSourceFile(foo));
    }

    public void testEnsureReconciled()
    {
        SimpleSourceConstruct elementNotInSourceFile =
            new SimpleSourceConstruct(root, null);
        Elements.ensureReconciled(elementNotInSourceFile, null);

        class TestSourceFile
            extends SimpleSourceFile
        {
            int testMode;

            TestSourceFile()
            {
                super(null, null, null, manager);
            }

            @Override
            public void reconcile_(IContext context, IProgressMonitor monitor)
                throws CoreException
            {
                if (testMode == 1)
                    throw new CoreException(Activator.createErrorStatus("error",
                        null));
            }
        }
        TestSourceFile sourceFile = new TestSourceFile();
        Elements.ensureReconciled(sourceFile, null);
        sourceFile.testMode = 1;
        Elements.ensureReconciled(sourceFile, null);
    }
}
