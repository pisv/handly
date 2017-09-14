/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.examples.javamodel.IJavaElementDelta;
import org.eclipse.handly.examples.javamodel.IJavaModel;
import org.eclipse.handly.examples.javamodel.IJavaProject;
import org.eclipse.handly.examples.javamodel.JavaModelCore;

import junit.framework.TestCase;

/**
 * <code>JavaElementDelta</code> tests.
 */
public class JavaElementDeltaTest
    extends TestCase
{
    private IJavaModel jm = JavaModelCore.getJavaModel();
    private IJavaProject a = jm.getJavaProject("a");
    private IJavaProject b = jm.getJavaProject("b");
    private IJavaProject c = jm.getJavaProject("c");
    private IJavaProject d = jm.getJavaProject("d");
    private IJavaProject e = jm.getJavaProject("e");
    private IJavaElementDelta delta;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        JavaElementDelta.Builder builder = new JavaElementDelta.Builder(
            new JavaElementDelta(jm));
        builder.added(a);
        builder.removed(b);
        builder.changed(c, IJavaElementDelta.F_CHILDREN);
        builder.movedFrom(d, e);
        builder.movedTo(e, d);
        delta = (IJavaElementDelta)builder.getDelta();
    }

    public void test1()
    {
        assertEquals(jm, delta.getElement());
        assertEquals(IJavaElementDelta.CHANGED, delta.getKind());
        assertEquals(IJavaElementDelta.F_CHILDREN, delta.getFlags());

        IJavaElementDelta[] affectedChildren = delta.getAffectedChildren();
        assertEquals(5, affectedChildren.length);

        assertEquals(a, affectedChildren[0].getElement());
        assertEquals(IJavaElementDelta.ADDED, affectedChildren[0].getKind());
        assertEquals(0, affectedChildren[0].getFlags());

        assertEquals(b, affectedChildren[1].getElement());
        assertEquals(IJavaElementDelta.REMOVED, affectedChildren[1].getKind());
        assertEquals(0, affectedChildren[1].getFlags());

        assertEquals(c, affectedChildren[2].getElement());
        assertEquals(IJavaElementDelta.CHANGED, affectedChildren[2].getKind());
        assertEquals(IJavaElementDelta.F_CHILDREN,
            affectedChildren[2].getFlags());

        assertEquals(d, affectedChildren[3].getElement());
        assertEquals(IJavaElementDelta.REMOVED, affectedChildren[3].getKind());
        assertEquals(IJavaElementDelta.F_MOVED_TO,
            affectedChildren[3].getFlags());
        assertEquals(e, affectedChildren[3].getMovedToElement());
        assertNull(affectedChildren[3].getMovedFromElement());

        assertEquals(e, affectedChildren[4].getElement());
        assertEquals(IJavaElementDelta.ADDED, affectedChildren[4].getKind());
        assertEquals(IJavaElementDelta.F_MOVED_FROM,
            affectedChildren[4].getFlags());
        assertEquals(d, affectedChildren[4].getMovedFromElement());
        assertNull(affectedChildren[4].getMovedToElement());

        IJavaElementDelta[] addedChildren = delta.getAddedChildren();
        assertEquals(2, addedChildren.length);
        assertSame(affectedChildren[0], addedChildren[0]);
        assertSame(affectedChildren[4], addedChildren[1]);

        IJavaElementDelta[] removedChildren = delta.getRemovedChildren();
        assertEquals(2, removedChildren.length);
        assertSame(affectedChildren[1], removedChildren[0]);
        assertSame(affectedChildren[3], removedChildren[1]);

        IJavaElementDelta[] changedChildren = delta.getChangedChildren();
        assertEquals(1, changedChildren.length);
        assertSame(affectedChildren[2], changedChildren[0]);

        assertSame(delta, delta.findDelta(jm));
        assertSame(affectedChildren[0], delta.findDelta(a));
        assertSame(affectedChildren[1], delta.findDelta(b));
        assertSame(affectedChildren[2], delta.findDelta(c));
        assertSame(affectedChildren[3], delta.findDelta(d));
        assertSame(affectedChildren[4], delta.findDelta(e));
    }
}
