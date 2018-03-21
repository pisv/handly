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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.Elements.BASE_SNAPSHOT;

import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

/**
 * Source element tests.
 */
public class SourceElementTest
    extends TestCase
{
    private SimpleSourceFile a;
    private SimpleSourceConstruct b;
    private SourceElementBody aBody, bBody;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        a = new SimpleSourceFile(null, "a.foo", null, new SimpleModelManager())
        {
            @Override
            public Object findBody_()
            {
                return aBody;
            }
        };
        b = new SimpleSourceConstruct(a, "B")
        {
            @Override
            public Object findBody_()
            {
                return bBody;
            }
        };
        aBody = new SourceElementBody();
        aBody.setFullRange(new TextRange(0, 10));
        aBody.addChild(b);
        bBody = new SourceElementBody();
        bBody.setFullRange(new TextRange(3, 5));
    }

    public void test1() throws Exception
    {
        assertEquals(a, a.getSourceElementAt_(0, EMPTY_CONTEXT, null));
        assertEquals(a, a.getSourceElementAt_(10, EMPTY_CONTEXT, null));
        assertNull(a.getSourceElementAt_(11, EMPTY_CONTEXT, null));
        assertEquals(b, a.getSourceElementAt_(3, EMPTY_CONTEXT, null));
        assertEquals(a, a.getSourceElementAt_(2, EMPTY_CONTEXT, null));
        assertEquals(b, a.getSourceElementAt_(8, EMPTY_CONTEXT, null));
        assertEquals(a, a.getSourceElementAt_(9, EMPTY_CONTEXT, null));
    }

    public void test2() throws Exception
    {
        Document document = new Document("0123456789");
        ISnapshot snapshot = new DocumentSnapshot(document);
        aBody.setSnapshot(snapshot);
        try
        {
            a.getSourceElementAt_(0, EMPTY_CONTEXT, null);
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }

        bBody.setSnapshot(snapshot);
        test1();

        document.replace(0, 0, "0");
        ISnapshot snapshot2 = new DocumentSnapshot(document);
        try
        {
            a.getSourceElementAt_(0, of(BASE_SNAPSHOT, snapshot2), null);
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }

        aBody.setSnapshot(snapshot2);
        try
        {
            a.getSourceElementAt_(0, of(BASE_SNAPSHOT, snapshot2), null);
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }

        bBody.setSnapshot(snapshot2);
        assertEquals(a, a.getSourceElementAt_(0, of(BASE_SNAPSHOT, snapshot2),
            null));
    }
}
