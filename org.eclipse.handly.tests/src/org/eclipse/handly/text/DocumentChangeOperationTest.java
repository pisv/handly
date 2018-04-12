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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;

import junit.framework.TestCase;

/**
 * <code>DocumentChangeOperation</code> tests.
 */
public class DocumentChangeOperationTest
    extends TestCase
{
    private IDocument document;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        document = new Document();
    }

    public void test1() throws Exception
    {
        DocumentChange change = new DocumentChange(new InsertEdit(0, "foo"));
        IDocumentChange undoChange = new DocumentChangeOperation(document,
            change).execute();
        assertEquals("foo", document.get());
        IDocumentChange redoChange = new DocumentChangeOperation(document,
            undoChange).execute();
        assertEquals("", document.get());
        new DocumentChangeOperation(document, redoChange).execute();
        assertEquals("foo", document.get());
    }

    public void test2() throws Exception
    {
        DocumentChange change = new DocumentChange(new InsertEdit(0, "foo"));
        change.setStyle(IDocumentChange.NONE);
        assertNull(new DocumentChangeOperation(document, change).execute()); // no undo
    }

    public void test3() throws Exception
    {
        DocumentChange change = new DocumentChange(new InsertEdit(0, "foo"));
        change.setBase(new DocumentSnapshot(document));
        document.set("bar");
        try
        {
            new DocumentChangeOperation(document, change).execute();
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }
    }

    public void test4() throws Exception
    {
        DocumentChange change = new DocumentChange(new InsertEdit(0, "foo"));
        IDocumentChange undoChange = new DocumentChangeOperation(document,
            change).execute();
        document.set("");
        try
        {
            new DocumentChangeOperation(document, undoChange).execute();
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }
    }
}
