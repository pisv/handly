/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.adapter;

import org.eclipse.handly.model.impl.support.SimpleElement;

import junit.framework.TestCase;

/**
 * <code>NullContentAdapter</code> tests.
 */
public class NullContentAdapterTest
    extends TestCase
{
    private IContentAdapter adapter = NullContentAdapter.INSTANCE;

    public void test1()
    {
        SimpleElement e = new SimpleElement(null, null, null);
        assertSame(e, adapter.adapt(e));
        assertSame(e, adapter.getCorrespondingElement(e));
    }

    public void test2()
    {
        assertNull(adapter.adapt("foo"));
        assertNull(adapter.adapt(null));
        assertNull(adapter.getCorrespondingElement(null));
    }
}
