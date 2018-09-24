/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.xtext.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.text.IDocumentChange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

/**
 * Handly extension interface for {@link IXtextDocument}.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IHandlyXtextDocument
    extends IXtextDocument
{
    /**
     * Returns whether the document model is out of sync
     * with the document text and needs to be {@link
     * #reconcile(boolean, IProgressMonitor) reconciled}.
     *
     * @return <code>true</code> if reconciling is needed,
     *  and <code>false</code> otherwise
     */
    boolean needsReconciling();

    /**
     * Reconciles the document model so it is up to date again.
     * <p>
     * Note that a document model with cross-document references may become
     * out of date even when it is in sync with the document text.
     * The <code>force</code> parameter controls whether the document model
     * has to be reconciled even if it is already in sync with the document text.
     * </p>
     *
     * @param force controls whether the document model has to be reconciled
     *  even if it is already in sync with the the document text
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws OperationCanceledException if this method is canceled
     * @throws NoXtextResourceException if <code>force</code> is <code>true</code>
     *  and the document has no Xtext resource (i.e., either the resource
     *  has yet to be set or it has already been disposed)
     */
    void reconcile(boolean force, IProgressMonitor monitor);

    /**
     * Returns the document snapshot that the document model was most recently
     * {@link #reconcile(boolean, IProgressMonitor) reconciled} with. Returns
     * <code>null</code> if this document has no Xtext resource (i.e., either
     * the resource has yet to be set or it has already been disposed).
     *
     * @return the most recently reconciled snapshot, or <code>null</code>
     */
    ISnapshot getReconciledSnapshot();

    /**
     * Executes the given unit of work under the document's read lock.
     * The unit of work <b>must not</b> modify the Xtext resource or expose
     * any references to it. Read-only units of work may nest into each other.
     * <p>
     * While inside the dynamic context of the unit of work,
     * the Xtext resource is guaranteed to be based on the most recently
     * {@link #getReconciledSnapshot() reconciled snapshot}. Usually,
     * there is no need for clients to invoke {@link #reconcile(boolean,
     * IProgressMonitor) reconcile} before calling this method to ensure
     * a fresh snapshot; reconciling happens automatically before the
     * top-level unit of work is run.
     * </p>
     *
     * @param work a read-only unit of work (not <code>null</code>)
     * @return the unit of work's result (may be <code>null</code>).
     *  Must not contain any references to the Xtext resource or its contents
     *  (semantic objects or parse tree nodes)
     */
    @Override
    <T> T readOnly(IUnitOfWork<T, XtextResource> work);

    /**
     * Executes the given unit of work under the document's write lock.
     * The unit of work may modify the Xtext resource but <b>must not</b>
     * expose any references to it.
     * <p>
     * The document text is automatically updated so it is reconciled with
     * the resource modifications performed by the unit of work. Units of work
     * may be nested; changes are only applied to the document after successful
     * completion of the top-level modification, i.e., when all work is done.
     * If the top-level unit of work is {@link IUndoableUnitOfWork undoable},
     * it will be {@link IUndoableUnitOfWork#acceptUndoChange(IDocumentChange)
     * informed} of an undo change that can be {@link #applyChange(
     * IDocumentChange) applied} to the document to revert any modifications
     * made by the whole transaction.
     * </p>
     * <p>
     * Before the top-level unit of work is run, the Xtext resource is
     * guaranteed to be based on the most recently {@link #getReconciledSnapshot()
     * reconciled snapshot}. That snapshot is regarded as the <i>base</i>
     * snapshot for the whole transaction, i.e., if it turns out to be stale
     * when changes are to be applied to the document, this method will throw a
     * {@link StaleSnapshotException}. Usually, there is no need for clients
     * to invoke {@link #reconcile(boolean, IProgressMonitor) reconcile}
     * before calling this method to ensure a fresh snapshot; reconciling
     * happens automatically before the top-level unit of work is run.
     * </p>
     *
     * @param work a modifying unit of work (not <code>null</code>)
     * @return the unit of work's result (may be <code>null</code>).
     *  Must not contain any references to the Xtext resource or its contents
     *  (semantic objects or parse tree nodes)
     * @throws StaleSnapshotException if the document text has changed
     *  since the inception of the transaction's base snapshot
     */
    @Override
    <T> T modify(IUnitOfWork<T, XtextResource> work);

    /**
     * Applies the given change to this document.
     * <p>
     * Note that an update conflict may occur if the document text has changed
     * since the inception of the snapshot on which the given change is based.
     * In that case, a {@link StaleSnapshotException} is thrown.
     * </p>
     *
     * @param change a document change (not <code>null</code>)
     * @return undo change, if requested by the given change.
     *  Otherwise, <code>null</code>
     * @throws StaleSnapshotException if the document text has changed since
     *  the inception of the snapshot on which the given change is based
     * @throws MalformedTreeException if the change's edit tree is not
     *  in a valid state
     * @throws BadLocationException if one of the edits in the change's
     *  edit tree could not be executed
     */
    IDocumentChange applyChange(IDocumentChange change)
        throws BadLocationException;

    /**
     * Represents an undoable unit of work.
     * <p>
     * If a top-level modifying unit of work is undoable, it will be {@link
     * IUndoableUnitOfWork#acceptUndoChange(IDocumentChange) informed} of an
     * undo change that can be {@link IHandlyXtextDocument#applyChange(
     * IDocumentChange) applied} to the document to revert any {@link
     * IHandlyXtextDocument#modify(IUnitOfWork) modifications} made by
     * the whole transaction.
     * </p>
     */
    interface IUndoableUnitOfWork<R, P>
        extends IUnitOfWork<R, P>
    {
        /**
         * Informs this unit of work of an undo change.
         *
         * @param undoChange never <code>null</code>
         */
        void acceptUndoChange(IDocumentChange undoChange);
    }
}
