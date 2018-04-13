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
package org.eclipse.handly.text;

import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.SimpleSynchronizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;

import junit.framework.TestCase;

/**
 * <code>UiDocumentChangeRunner</code> tests.
 */
public class UiDocumentChangeRunnerTest
    extends TestCase
{
    private IDocument document;
    private SimpleSynchronizer synchronizer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        document = new Document();
        synchronizer = new SimpleSynchronizer();
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (synchronizer != null)
            synchronizer.dispose();
        super.tearDown();
    }

    public void test1() throws Exception
    {
        DocumentChange change = new DocumentChange(new InsertEdit(0, "foo"));
        DocumentChangeOperation operation = new DocumentChangeOperation(
            document, change)
        {
            @Override
            public IDocumentChange execute() throws BadLocationException
            {
                assertSame(synchronizer.getThread(), Thread.currentThread());
                return super.execute();
            }
        };
        new UiDocumentChangeRunner(synchronizer, operation).run();
        assertEquals("foo", document.get());
    }

    public void test2() throws Exception
    {
        synchronizer.syncExec(() ->
        {
            try
            {
                test1();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public void test3() throws Exception
    {
        DocumentChange change = new DocumentChange(new InsertEdit(0, "foo"));
        change.setBase(new DocumentSnapshot(document));
        document.set("bar");
        try
        {
            new UiDocumentChangeRunner(synchronizer,
                new DocumentChangeOperation(document, change)).run();
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }
    }
}
