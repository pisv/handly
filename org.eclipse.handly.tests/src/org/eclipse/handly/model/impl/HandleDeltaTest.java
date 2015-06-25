/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
 * <code>HandleDelta</code> tests.
 */
public class HandleDeltaTest
    extends TestCase
{
    /**
     * Regression test for bug 456060 - AIOOB in HandleDelta.addAffectedChild.
     *
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=456060
     */
    public void testBug456060()
    {
        SimpleHandle root = new SimpleHandle(null, "root");
        HandleDelta delta = new HandleDelta(root);
        delta.insertAdded(root.getChild("A"));
        delta.insertAdded(root.getChild("B"));
        delta.insertAdded(root.getChild("C"));
        delta.insertAdded(root.getChild("D"));
        delta.insertMovedFrom(root.getChild("C"), root.getChild("X"));
        delta.insertMovedFrom(root.getChild("D"), root.getChild("Y"));
    }
}
