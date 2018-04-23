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
