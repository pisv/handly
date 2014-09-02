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
package org.eclipse.handly.document;

import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.UndoEdit;

/**
 * Applies the given change to the given document.
 */
public class DocumentChangeOperation
{
    protected final IDocument document;
    protected final IDocumentChange change;

    /**
     * Creates a new operation that can apply the given change 
     * to the given document.
     * 
     * @param document must not be <code>null</code>
     * @param change must not be <code>null</code>
     */
    public DocumentChangeOperation(IDocument document, IDocumentChange change)
    {
        if (document == null)
            throw new IllegalArgumentException();
        if (change == null)
            throw new IllegalArgumentException();

        this.document = document;
        this.change = change;
    }

    /**
     * Applies the change to the document. Note that an update conflict may occur 
     * if the document's contents have changed since the inception of the snapshot 
     * on which the change is based. In that case, a {@link StaleSnapshotException} 
     * is thrown. 
     *
     * @return undo change, if requested. Otherwise, <code>null</code>
     * @throws StaleSnapshotException if the document has changed 
     *  since the inception of the snapshot on which the change is based
     * @throws MalformedTreeException if the change's edit tree isn't 
     *  in a valid state
     * @throws BadLocationException if one of the edits in the tree 
     *  can't be executed 
     */
    public IDocumentChange execute() throws BadLocationException
    {
        if (!(document instanceof IDocumentExtension4))
            return applyChange();

        IDocumentExtension4 extension = (IDocumentExtension4)document;
        boolean isLargeEdit =
            RewriteSessionEditProcessor.isLargeEdit(change.getEdit());
        DocumentRewriteSessionType type =
            isLargeEdit ? DocumentRewriteSessionType.UNRESTRICTED
                : DocumentRewriteSessionType.UNRESTRICTED_SMALL;
        DocumentRewriteSession session = extension.startRewriteSession(type);
        try
        {
            return applyChange();
        }
        finally
        {
            extension.stopRewriteSession(session);
        }
    }

    protected IDocumentChange applyChange() throws BadLocationException
    {
        checkChange();

        LinkedModeModel.closeAllModels(document);

        long stampToRestore = getModificationStampOf(document);

        UndoEdit undoEdit = applyTextEdit();

        if (change instanceof UndoChange)
        {
            setModificationStampOf(document,
                ((UndoChange)change).stampToRestore);
        }

        return createUndoChange(undoEdit, stampToRestore);
    }

    protected ISnapshot getCurrentSnapshot()
    {
        return new DocumentSnapshot(document);
    }

    protected void checkChange()
    {
        ISnapshot baseSnapshot = change.getBase();
        if (baseSnapshot != null
            && !baseSnapshot.isEqualTo(getCurrentSnapshot()))
        {
            throw new StaleSnapshotException();
        }
    }

    protected UndoEdit applyTextEdit() throws BadLocationException
    {
        return createTextEditProcessor().performEdits();
    }

    protected TextEditProcessor createTextEditProcessor()
    {
        return new TextEditProcessor(document, change.getEdit(),
            change.getStyle());
    }

    protected IDocumentChange createUndoChange(UndoEdit undoEdit,
        long stampToRestore)
    {
        if (undoEdit == null)
            return null;

        UndoChange undoChange = new UndoChange(undoEdit, stampToRestore);
        undoChange.setBase(getCurrentSnapshot());
        undoChange.setStyle(change.getStyle());
        return undoChange;
    }

    protected static long getModificationStampOf(IDocument document)
    {
        long modificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
        if (document instanceof IDocumentExtension4)
        {
            modificationStamp =
                ((IDocumentExtension4)document).getModificationStamp();
        }
        return modificationStamp;
    }

    protected static void setModificationStampOf(IDocument document,
        long modificationStamp)
    {
        if (document instanceof IDocumentExtension4
            && modificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP)
        {
            try
            {
                ((IDocumentExtension4)document).replace(0, 0, "", //$NON-NLS-1$
                    modificationStamp);
            }
            catch (BadLocationException e)
            {
            }
        }
    }

    protected static class UndoChange
        extends DocumentChange
    {
        public final long stampToRestore;

        public UndoChange(UndoEdit undoEdit, long stampToRestore)
        {
            super(undoEdit);
            this.stampToRestore = stampToRestore;
        }
    }
}
