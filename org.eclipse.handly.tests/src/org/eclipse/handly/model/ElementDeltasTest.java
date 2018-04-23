/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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

import static org.eclipse.handly.model.IElementDeltaConstants.F_CHILDREN;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_FINE_GRAINED;

import org.eclipse.handly.model.impl.support.ElementDelta;
import org.eclipse.handly.model.impl.support.SimpleElement;
import org.eclipse.handly.model.impl.support.SimpleModelManager;

import junit.framework.TestCase;

/**
 * <code>ElementDeltas</code> tests.
 */
public class ElementDeltasTest
    extends TestCase
{
    private final SimpleElement root = new SimpleElement(null, "root",
        new SimpleModelManager());
    private ElementDelta delta;
    private ElementDelta.Builder builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        delta = new ElementDelta(root);
        builder = new ElementDelta.Builder(delta);
    }

    public void test01()
    {
        assertFalse(ElementDeltas.isStructuralChange(delta));
    }

    public void test02()
    {
        builder.added(root);
        assertTrue(ElementDeltas.isStructuralChange(delta));
    }

    public void test03()
    {
        builder.removed(root);
        assertTrue(ElementDeltas.isStructuralChange(delta));
    }

    public void test04()
    {
        builder.changed(root, F_CHILDREN);
        assertTrue(ElementDeltas.isStructuralChange(delta));
    }

    public void test05()
    {
        builder.changed(root, F_CONTENT);
        assertTrue(ElementDeltas.isStructuralChange(delta));
    }

    public void test06()
    {
        builder.changed(root, F_CONTENT | F_FINE_GRAINED);
        assertFalse(ElementDeltas.isStructuralChange(delta));
    }

    public void test07()
    {
        builder.changed(root, Long.MAX_VALUE ^ (F_CHILDREN | F_CONTENT));
        assertFalse(ElementDeltas.isStructuralChange(delta));
    }
}
