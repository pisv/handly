/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.handly.examples.jmodel.IJavaElementDelta;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;

import junit.framework.TestCase;

class JavaModelListener
    implements IElementChangeListener
{
    IJavaElementDelta delta;

    @Override
    public void elementChanged(IElementChangeEvent event)
    {
        delta = (IJavaElementDelta)event.getDeltas()[0];
    }

    void assertDelta(String expected)
    {
        String actual = (delta == null ? "<null>" : delta.toString());
        TestCase.assertEquals(expected, actual);
    }
}
