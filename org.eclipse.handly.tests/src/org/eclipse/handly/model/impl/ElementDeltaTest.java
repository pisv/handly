/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import junit.framework.TestCase;

/**
 * <code>ElementDelta</code> tests.
 */
public class ElementDeltaTest
    extends TestCase
{
    /**
     * Regression test for bug 456060 - AIOOB in #addAffectedChild.
     */
    public void testBug456060()
    {
        SimpleElement root = new SimpleElement(null, "root");
        ElementDelta delta = new ElementDelta(root);
        delta.hInsertAdded(root.getChild("A"));
        delta.hInsertAdded(root.getChild("B"));
        delta.hInsertAdded(root.getChild("C"));
        delta.hInsertAdded(root.getChild("D"));
        delta.hInsertMovedFrom(root.getChild("C"), root.getChild("X"));
        delta.hInsertMovedFrom(root.getChild("D"), root.getChild("Y"));
    }
}
