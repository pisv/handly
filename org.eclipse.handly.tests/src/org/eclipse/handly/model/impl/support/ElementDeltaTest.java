/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_DESCRIPTION;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.IElementDeltaImpl;

import junit.framework.TestCase;

/**
 * <code>ElementDelta</code> tests.
 */
public class ElementDeltaTest
    extends TestCase
{
    private SimpleElement root;
    private ElementDelta delta;
    private ElementDelta.Builder builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        root = new SimpleElement(null, "root", new SimpleModelManager());
        delta = new ElementDelta(root);
        builder = new ElementDelta.Builder(delta);
    }

    public void test00()
    {
        assertDelta("root[?]: {}");
    }

    public void test01()
    {
        builder.added(root);
        assertDelta("root[+]: {}");
    }

    public void test02()
    {
        builder.removed(root);
        assertDelta("root[-]: {}");
    }

    public void test03()
    {
        builder.changed(root, F_CONTENT);
        assertDelta("root[*]: {CONTENT}");
    }

    public void test04()
    {
        builder.added(root);
        builder.added(root);
        assertDelta("root[+]: {}");
    }

    public void test05()
    {
        builder.added(root);
        builder.changed(root, F_CONTENT);
        assertDelta("root[+]: {}");
    }

    public void test06()
    {
        builder.added(root);
        builder.removed(root);
        assertDelta("root[?]: {}");
    }

    public void test07()
    {
        builder.removed(root);
        builder.added(root);
        assertDelta("root[*]: {CONTENT}");
    }

    public void test08()
    {
        builder.removed(root);
        builder.changed(root, F_CONTENT);
        assertDelta("root[-]: {}");
    }

    public void test09()
    {
        builder.removed(root);
        builder.removed(root);
        assertDelta("root[-]: {}");
    }

    public void test10()
    {
        builder.changed(root, F_CONTENT);
        builder.added(root);
        assertDelta("root[+]: {}");
    }

    public void test11()
    {
        builder.changed(root, F_CONTENT);
        builder.removed(root);
        assertDelta("root[-]: {}");
    }

    public void test12()
    {
        builder.changed(root, F_CONTENT);
        builder.changed(root, F_DESCRIPTION);
        assertDelta("root[*]: {CONTENT | DESCRIPTION}");
    }

    public void test13()
    {
        // merge with empty delta
        delta.mergeWith_(new ElementDelta(root));
        assertDelta("root[?]: {}");
    }

    public void test14()
    {
        builder.added(root);
        // merge with empty delta
        delta.mergeWith_(new ElementDelta(root));
        assertDelta("root[+]: {}");
    }

    public void test15()
    {
        builder.removed(root);
        // merge with empty delta
        delta.mergeWith_(new ElementDelta(root));
        assertDelta("root[-]: {}");
    }

    public void test16()
    {
        builder.changed(root, F_CONTENT);
        // merge with empty delta
        delta.mergeWith_(new ElementDelta(root));
        assertDelta("root[*]: {CONTENT}");
    }

    public void test17()
    {
        // copy from empty delta
        delta.copyFrom_(new ElementDelta(root), false);
        assertDelta("root[?]: {}");
    }

    public void test18()
    {
        builder.added(root);
        // copy from empty delta
        delta.copyFrom_(new ElementDelta(root), false);
        assertDelta("root[+]: {}");
    }

    public void test19()
    {
        builder.removed(root);
        // copy from empty delta
        delta.copyFrom_(new ElementDelta(root), false);
        assertDelta("root[-]: {}");
    }

    public void test20()
    {
        builder.changed(root, F_CONTENT);
        // copy from empty delta
        delta.copyFrom_(new ElementDelta(root), false);
        assertDelta("root[*]: {CONTENT}");
    }

    public void test21()
    {
        ElementDelta d = new ElementDelta(root);
        d.copyFrom_(delta, false);
        assertEquals("root[?]: {}", d.toString());
    }

    public void test22()
    {
        builder.added(root);
        ElementDelta d = new ElementDelta(root);
        d.copyFrom_(delta, false);
        assertEquals("root[+]: {}", d.toString());
    }

    public void test23()
    {
        builder.removed(root);
        ElementDelta d = new ElementDelta(root);
        d.copyFrom_(delta, false);
        assertEquals("root[-]: {}", d.toString());
    }

    public void test24()
    {
        builder.changed(root, F_CONTENT);
        ElementDelta d = new ElementDelta(root);
        d.copyFrom_(delta, false);
        assertEquals("root[*]: {CONTENT}", d.toString());
    }

    public void test25()
    {
        builder.added(root);
        ElementDelta d = new ElementDelta(root);
        d.setKind_(REMOVED);
        try
        {
            d.copyFrom_(delta, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void test26()
    {
        builder.added(root.getChild("A"));
        builder.removed(root.getChild("B"));
        builder.changed(root.getChild("C"), F_CONTENT);
        //@formatter:off
        assertDelta("root[*]: {CHILDREN}\n" +
            "  A[+]: {}\n" +
            "  B[-]: {}\n" +
            "  C[*]: {CONTENT}");
        //@formatter:on
        assertEquals(3, delta.getAffectedChildren_().length);
        assertEquals(1, delta.getAddedChildren_().length);
        assertEquals(1, delta.getRemovedChildren_().length);
        assertEquals(1, delta.getChangedChildren_().length);
    }

    public void test27()
    {
        builder.added(root);
        builder.added(root.getChild("A"));
        builder.removed(root.getChild("B"));
        builder.changed(root.getChild("C"), F_CONTENT);
        assertDelta("root[+]: {}");
        assertEquals(0, delta.getAffectedChildren_().length);
        assertEquals(0, delta.getAddedChildren_().length);
        assertEquals(0, delta.getRemovedChildren_().length);
        assertEquals(0, delta.getChangedChildren_().length);
    }

    public void test28()
    {
        builder.removed(root);
        builder.added(root.getChild("A"));
        builder.removed(root.getChild("B"));
        builder.changed(root.getChild("C"), F_CONTENT);
        assertDelta("root[-]: {}");
        assertEquals(0, delta.getAffectedChildren_().length);
        assertEquals(0, delta.getAddedChildren_().length);
        assertEquals(0, delta.getRemovedChildren_().length);
        assertEquals(0, delta.getChangedChildren_().length);
    }

    public void test29()
    {
        SimpleElement a = root.getChild("A");
        SimpleElement b = root.getChild("B");
        builder.movedFrom(a, b);
        builder.movedTo(b, a);
        //@formatter:off
        assertDelta("root[*]: {CHILDREN}\n" +
            "  A[-]: {MOVED_TO(B)}\n" +
            "  B[+]: {MOVED_FROM(A)}");
        //@formatter:on
        assertEquals(2, delta.getAffectedChildren_().length);
        assertEquals(1, delta.getAddedChildren_().length);
        assertEquals(1, delta.getRemovedChildren_().length);
        assertEquals(0, delta.getChangedChildren_().length);
    }

    public void test30()
    {
        SimpleElement a = root.getChild("A");
        SimpleElement b = a.getChild("B");
        SimpleElement c = b.getChild("C");
        builder.added(b);
        builder.removed(c);
        //@formatter:off
        assertDelta("root[*]: {CHILDREN}\n" +
            "  A[*]: {CHILDREN}\n" +
            "    B[+]: {}");
        //@formatter:on
        assertEquals(delta, delta.findDelta_(root));
        assertEquals(1, delta.getAffectedChildren_().length);
        assertEquals(0, delta.getAddedChildren_().length);
        assertEquals(0, delta.getRemovedChildren_().length);
        assertEquals(1, delta.getChangedChildren_().length);
        ElementDelta d = delta.findDelta_(a);
        assertEquals(1, d.getAffectedChildren_().length);
        assertEquals(1, d.getAddedChildren_().length);
        assertEquals(0, d.getRemovedChildren_().length);
        assertEquals(0, d.getChangedChildren_().length);
        assertEquals(d.getAddedChildren_()[0], delta.findDelta_(b));
        assertNull(delta.findDelta_(c));
    }

    public void test31()
    {
        SimpleModelManager manager = new SimpleModelManager();
        SimpleSourceFile sourceFile = new SimpleSourceFile(root, "a.foo", null,
            manager);
        ElementDelta.Builder builder = new ElementDelta.Builder(
            new ElementDelta(sourceFile));
        builder.added(new SimpleElement(sourceFile, "A", manager));
        //@formatter:off
        assertEquals("a.foo[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[+]: {}", builder.getDelta().toString());
        //@formatter:on
    }

    public void test32()
    {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IResourceChangeDescriptionFactory factory =
            ResourceChangeValidator.getValidator().createDeltaFactory();
        for (int i = 1; i < 10; i++)
        {
            factory.create(workspaceRoot.getProject("p" + i));
        }
        IResourceDelta[] resourceDeltas =
            factory.getDelta().getAffectedChildren();
        for (IResourceDelta resourceDelta : resourceDeltas)
        {
            builder.addResourceDelta(root, resourceDelta);
        }
        //@formatter:off
        assertDelta("root[*]: {CONTENT}\n" +
            "  ResourceDelta(/p1)[+]\n" +
            "  ResourceDelta(/p2)[+]\n" +
            "  ResourceDelta(/p3)[+]\n" +
            "  ResourceDelta(/p4)[+]\n" +
            "  ResourceDelta(/p5)[+]\n" +
            "  ResourceDelta(/p6)[+]\n" +
            "  ResourceDelta(/p7)[+]\n" +
            "  ResourceDelta(/p8)[+]\n" +
            "  ResourceDelta(/p9)[+]");
        //@formatter:on
    }

    public void test33() throws Exception
    {
        class ResourceChangeListener
            implements IResourceChangeListener
        {
            IResourceDelta delta;

            @Override
            public void resourceChanged(IResourceChangeEvent event)
            {
                delta = event.getDelta();
            }
        }
        ResourceChangeListener listener = new ResourceChangeListener();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(listener);
        IMarker marker = workspace.getRoot().createMarker(IMarker.PROBLEM);
        try
        {
            IMarkerDelta[] markerDeltas = listener.delta.getMarkerDeltas();
            assertEquals(1, markerDeltas.length);
            builder.markersChanged(root, markerDeltas);
            assertDelta("root[*]: {MARKERS}");
            assertSame(markerDeltas[0], delta.getMarkerDeltas_()[0]);

            try
            {
                builder.markersChanged(root, markerDeltas);
                fail();
            }
            catch (Throwable e)
            {
            }
        }
        finally
        {
            workspace.removeResourceChangeListener(listener);
            marker.delete();
        }
    }

    /**
     * Regression test for bug 456060 - AIOOB in #addAffectedChild.
     */
    public void testBug456060()
    {
        builder.added(root.getChild("A"));
        builder.added(root.getChild("B"));
        builder.added(root.getChild("C"));
        builder.added(root.getChild("D"));
        builder.movedFrom(root.getChild("C"), root.getChild("X"));
        builder.movedFrom(root.getChild("D"), root.getChild("Y"));
    }

    public void testMalformedDeltaTree()
    {
        SimpleElement parent = new SimpleElement(null, "parent",
            new SimpleModelManager());
        SimpleElement child = parent.getChild("child");
        ElementDelta.Builder builder = new ElementDelta.Builder(
            new ElementDelta(child));
        try
        {
            builder.added(parent);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests implementation of default method {@link IElementDeltaImpl#findDelta_(IElement)}.
     * (<code>ElementDelta</code> overrides it with an optimized implementation)
     */
    public void testDefaultFindDelta()
    {
        class TestDelta
            implements IElementDeltaImpl
        {
            final IElement element;
            final IElementDelta[] children;

            TestDelta(IElement element, IElementDelta[] children)
            {
                this.element = element;
                this.children = children;
            }

            @Override
            public IElement getElement_()
            {
                return element;
            }

            @Override
            public IElementDelta[] getAffectedChildren_()
            {
                return children;
            }

            @Override
            public int getKind_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getFlags_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IElementDelta[] getAddedChildren_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IElementDelta[] getRemovedChildren_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IElementDelta[] getChangedChildren_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IElement getMovedFromElement_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IElement getMovedToElement_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IMarkerDelta[] getMarkerDeltas_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public IResourceDelta[] getResourceDeltas_()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString_(IContext context)
            {
                throw new UnsupportedOperationException();
            }
        }
        SimpleElement a = root.getChild("A");
        SimpleElement b = a.getChild("B");
        SimpleElement c = root.getChild("C");
        TestDelta bDelta = new TestDelta(b, ElementDeltas.EMPTY_ARRAY);
        TestDelta aDelta = new TestDelta(a, new IElementDelta[] { bDelta });
        TestDelta delta = new TestDelta(root, new IElementDelta[] { aDelta });
        assertSame(bDelta, delta.findDelta_(b));
        assertNull(delta.findDelta_(c));
        assertNull(delta.findDelta_(null));
        assertNull(aDelta.findDelta_(c));
    }

    private void assertDelta(String expected)
    {
        assertEquals(expected, delta.toString());
    }
}
