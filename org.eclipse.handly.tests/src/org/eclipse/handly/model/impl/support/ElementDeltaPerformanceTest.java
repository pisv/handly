/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
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

import org.eclipse.handly.model.IElementDeltaConstants;

import com.google.common.base.Strings;

import junit.framework.TestCase;

/**
 * <code>ElementDelta</code> tests that can be useful for assessing performance.
 */
public class ElementDeltaPerformanceTest
    extends TestCase
{
    private SimpleElement root;
    private SimpleElement[] leafs;
    private ElementDelta delta;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // build a delta of depth 4 with 10 000 leafs
        root = new SimpleElement(null, "root", new SimpleModelManager());
        ElementDelta.Builder builder = new ElementDelta.Builder(
            new ElementDelta(root));
        leafs = new SimpleElement[10000];
        for (int i = 0; i < leafs.length; i++)
        {
            String path = Strings.padStart(String.valueOf(i), 4, '0');
            builder.added(getChild(root, path));
            leafs[i] = getChild(root, path); // store an equal element but not the same
        }
        delta = builder.getDelta();
    }

    public void testFindDeltaPerformance()
    {
        for (int i = 0; i < leafs.length; i++)
        {
            ElementDelta found = delta.findDelta_(leafs[i]);
            assertNotNull(found);
            assertEquals(leafs[i], found.getElement_());
            assertEquals(IElementDeltaConstants.ADDED, found.getKind_());
        }
    }

    private static SimpleElement getChild(SimpleElement root, String path)
    {
        SimpleElement child = root;
        for (int i = 0; i < path.length(); i++)
            child = child.getChild(Character.toString(path.charAt(i)));
        return child;
    }
}
