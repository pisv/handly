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

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.handly.snapshot.TextFileSnapshot;
import org.eclipse.handly.util.TextRange;

/**
 * Common superclass for source files.
 */
public abstract class SourceFile
    extends SourceElement
    implements ISourceFileImpl
{
    private static final ThreadLocal<AstHolder> AST_HOLDER =
        new ThreadLocal<AstHolder>();

    private final IFile file;

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given underlying workspace file.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param file the workspace file underlying the element (not <code>null</code>)
     */
    public SourceFile(Element parent, IFile file)
    {
        super(parent, file.getName());
        this.file = file;
    }

    @Override
    public final IResource hResource()
    {
        return file;
    }

    /**
     * Returns the underlying {@link IFile}. This is a handle-only method.
     *
     * @return the underlying <code>IFile</code> (never <code>null</code>)
     */
    @Override
    public final IFile hFile()
    {
        return file;
    }

    @Override
    public final IBuffer hBuffer(boolean create, IProgressMonitor monitor)
        throws CoreException
    {
        WorkingCopyInfo info = hAcquireWorkingCopy();
        if (info == null)
        {
            if (!create && ITextFileBufferManager.DEFAULT.getTextFileBuffer(
                file.getFullPath(), LocationKind.IFILE) == null)
            {
                return null;
            }
            return new TextFileBuffer(file, ITextFileBufferManager.DEFAULT);
        }
        else
        {
            try
            {
                IBuffer buffer = info.getBuffer();
                buffer.addRef();
                return buffer;
            }
            finally
            {
                hDiscardWorkingCopy();
            }
        }
    }

    /**
     * If this source file is not already in working copy mode, switches it
     * into a working copy, associates it with the given buffer, and acquires
     * an independent ownership of the working copy and its buffer. Performs
     * atomically.
     * <p>
     * Switching to working copy means that the source file's structure and
     * properties will no longer correspond to the underlying resource contents
     * and will no longer be updated by a resource delta processor. Instead,
     * those structure and properties can be explicitly {@link #hReconcile(
     * boolean, IProgressMonitor) reconciled} with the current contents of
     * the working copy buffer.
     * </p>
     * <p>
     * If the source file was already in working copy mode, this method acquires
     * a new independent ownership of the working copy by incrementing an internal
     * counter and returns the info associated with the working copy; the given
     * buffer is ignored.
     * </p>
     * <p>
     * Each successful call to this method must ultimately be followed
     * by exactly one call to <code>hDiscardWorkingCopy</code>.
     * </p>
     *
     * @param buffer the working copy buffer (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the working copy info previously associated with
     *  this source file, or <code>null</code> if there was no
     *  working copy info for this source file
     * @throws CoreException if the working copy cannot be created
     * @throws OperationCanceledException if this method is canceled
     * @see #hDiscardWorkingCopy()
     */
    public final WorkingCopyInfo hBecomeWorkingCopy(IBuffer buffer,
        IProgressMonitor monitor) throws CoreException
    {
        return hBecomeWorkingCopy(buffer, null, monitor);
    }

    /**
     * If this source file is not already in working copy mode, switches it
     * into a working copy, associates it with the given buffer via a new
     * working copy info obtained from the given factory, and acquires an
     * independent ownership of the working copy and its buffer. Performs
     * atomically.
     * <p>
     * Switching to working copy means that the source file's structure and
     * properties will no longer correspond to the underlying resource contents
     * and will no longer be updated by a resource delta processor. Instead,
     * those structure and properties can be explicitly {@link #hReconcile(
     * boolean, IProgressMonitor) reconciled} with the current contents of
     * the working copy buffer.
     * </p>
     * <p>
     * If the source file was already in working copy mode, this method acquires
     * a new independent ownership of the working copy by incrementing an internal
     * counter and returns the info associated with the working copy; the given
     * buffer and factory are ignored.
     * </p>
     * <p>
     * Each successful call to this method must ultimately be followed
     * by exactly one call to <code>hDiscardWorkingCopy</code>.
     * </p>
     *
     * @param buffer the working copy buffer (not <code>null</code>)
     * @param factory the working copy info factory, or <code>null</code>
     *  if a default factory is to be used
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the working copy info previously associated with
     *  this source file, or <code>null</code> if there was no
     *  working copy info for this source file
     * @throws CoreException if the working copy cannot be created
     * @throws OperationCanceledException if this method is canceled
     * @see #hDiscardWorkingCopy()
     */
    public final WorkingCopyInfo hBecomeWorkingCopy(IBuffer buffer,
        IWorkingCopyInfoFactory factory, IProgressMonitor monitor)
        throws CoreException
    {
        WorkingCopyProvider provider = new WorkingCopyProvider()
        {
            @Override
            protected WorkingCopyInfo doAcquireWorkingCopy()
            {
                return putWorkingCopyInfoIfAbsent(buffer, factory);
            }

            @Override
            protected boolean isCanceled()
            {
                if (monitor == null)
                    return false;
                return monitor.isCanceled();
            }
        };
        WorkingCopyInfo oldInfo = provider.acquireWorkingCopy();
        if (oldInfo == null)
        {
            boolean success = false;
            try
            {
                WorkingCopyInfo newInfo = hPeekAtWorkingCopyInfo();
                newInfo.workingCopy = this;
                newInfo.initTask.execute(monitor);
                success = true;
            }
            finally
            {
                if (!success)
                    hDiscardWorkingCopy();
            }
        }
        return oldInfo;
    }

    /**
     * If this source file is in working copy mode, acquires a new independent
     * ownership of the working copy by incrementing an internal counter and
     * returns the info associated with the working copy. Returns <code>null</code>
     * if this source file is not a working copy. Performs atomically.
     * <p>
     * Each successful call to this method that did not return <code>null</code>
     * must ultimately be followed by exactly one call to <code>hDiscardWorkingCopy</code>.
     * </p>
     *
     * @return the working copy info for this source file,
     *  or <code>null</code> if this source file is not a working copy
     * @see #hDiscardWorkingCopy()
     */
    public final WorkingCopyInfo hAcquireWorkingCopy()
    {
        WorkingCopyProvider provider = new WorkingCopyProvider()
        {
            @Override
            protected WorkingCopyInfo doAcquireWorkingCopy()
            {
                return getWorkingCopyInfo();
            }
        };
        return provider.acquireWorkingCopy();
    }

    /**
     * Relinquishes an independent ownership of the working copy by decrementing
     * an internal counter. If there are no remaining independent owners of the
     * working copy, switches this source file from working copy mode back to
     * its original mode and releases the working copy buffer. Performs
     * atomically.
     * <p>
     * Each independent ownership of the working copy must ultimately end
     * with exactly one call to this method. If a client is not an independent
     * owner of the working copy, it must not call this method.
     * </p>
     *
     * @return <code>true</code> if this source file was switched from
     *  working copy mode back to its original mode, <code>false</code>
     *  otherwise
     */
    public final boolean hDiscardWorkingCopy()
    {
        WorkingCopyInfo info = hElementManager().discardWorkingCopyInfo(this);
        if (info == null)
            throw new IllegalStateException("Not a working copy: " + hPath()); //$NON-NLS-1$
        if (info.isDisposed() && info.created)
        {
            hWorkingCopyModeChanged();
            return true;
        }
        return false;
    }

    @Override
    public final boolean hIsWorkingCopy()
    {
        WorkingCopyInfo info = hPeekAtWorkingCopyInfo();
        if (info == null)
            return false;
        if (info.created)
            return true;
        // special case: wc creation is in progress on the current thread
        AstHolder astHolder = AST_HOLDER.get();
        if (astHolder != null && astHolder.getSourceFile().equals(this))
            return true;
        return false;
    }

    @Override
    public final boolean hNeedsReconciling()
    {
        WorkingCopyInfo info = hAcquireWorkingCopy();
        if (info == null)
            return false;
        else
        {
            try
            {
                return info.needsReconciling();
            }
            finally
            {
                hDiscardWorkingCopy();
            }
        }
    }

    @Override
    public final void hReconcile(boolean force, Object arg,
        IProgressMonitor monitor) throws CoreException
    {
        WorkingCopyInfo info = hAcquireWorkingCopy();
        if (info == null)
            return; // not a working copy
        else
        {
            try
            {
                if (monitor == null)
                    monitor = new NullProgressMonitor();

                info.reconcile(force, arg, monitor);
            }
            finally
            {
                hDiscardWorkingCopy();
            }
        }
    }

    /**
     * Returns a reconcile operation for this source file.
     * <p>
     * This method may be used in {@link WorkingCopyInfo} implementations.
     * Other clients are not intended to invoke this method.
     * </p>
     * <p>
     * Subclasses which extend {@link ReconcileOperation} should override
     * this method.
     * </p>
     *
     * @return a reconcile operation for this source file (not <code>null</code>)
     */
    public ReconcileOperation hReconcileOperation()
    {
        return new ReconcileOperation();
    }

    /**
     * If this source file is in working copy mode, returns the working copy info
     * without acquiring an independent ownership of the working copy. Returns
     * <code>null</code> if this source file is not a working copy.
     *
     * @return the working copy info for this source file,
     *  or <code>null</code> if this source file is not a working copy
     * @see #hAcquireWorkingCopy()
     */
    protected final WorkingCopyInfo hPeekAtWorkingCopyInfo()
    {
        return hElementManager().peekAtWorkingCopyInfo(this);
    }

    /**
     * Notifies about a working copy mode change: either the source file
     * became a working copy or reverted back from the working copy mode.
     * Clients are not supposed to call this method, but may override it.
     */
    protected void hWorkingCopyModeChanged()
    {
        // subclasses might fire an appropriate event, etc.
    }

    @Override
    protected SourceElementBody hNewBody()
    {
        return new SourceElementBody();
    }

    @Override
    protected void hValidateExistence() throws CoreException
    {
        if (!hIsWorkingCopy())
        {
            if (!file.exists())
                throw new CoreException(Activator.createErrorStatus(
                    MessageFormat.format(
                        Messages.SourceFile_File_does_not_exist__0,
                        file.getFullPath().makeRelative()), null));
        }
    }

    @Override
    protected final void hBuildStructure(Object body,
        Map<IElement, Object> newElements, IProgressMonitor monitor)
        throws CoreException
    {
        int ticks = 2;
        monitor.beginTask("", ticks); //$NON-NLS-1$
        try
        {
            AstHolder astHolder = AST_HOLDER.get();

            if (astHolder == null) // not a working copy
            {
                // NOTE: AST is created from the underlying file contents,
                // not from the buffer contents, since source files that are not
                // working copies must reflect the structure of the underlying file
                NonExpiringSnapshot snapshot;
                try
                {
                    snapshot = new NonExpiringSnapshot(new ISnapshotProvider()
                    {
                        @Override
                        public ISnapshot getSnapshot()
                        {
                            TextFileSnapshot result = new TextFileSnapshot(file,
                                true);
                            if (result.getContents() == null
                                && !result.getStatus().isOK())
                            {
                                throw new IllegalStateException(
                                    new CoreException(result.getStatus()));
                            }
                            return result;
                        }
                    });
                }
                catch (IllegalStateException e)
                {
                    Throwable cause = e.getCause();
                    if (cause instanceof CoreException)
                        throw (CoreException)cause;
                    throw new AssertionError(e); // should never happen
                }
                Object ast = hCreateStructuralAst(snapshot.getContents(),
                    new SubProgressMonitor(monitor, 1));
                astHolder = new AstHolder(ast, snapshot);
                --ticks;
            }

            if (!astHolder.getSourceFile().equals(this))
                throw new AssertionError(); // should never happen

            SourceElementBody thisBody = (SourceElementBody)body;
            String source = astHolder.snapshot.getContents();
            ISnapshot snapshot = astHolder.snapshot.getWrappedSnapshot();

            hBuildStructure(thisBody, newElements, astHolder.ast, source,
                new SubProgressMonitor(monitor, ticks));

            thisBody.setFullRange(new TextRange(0, source.length()));
            setSnapshot(thisBody, snapshot, newElements);
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * Returns a new AST created from the given source string. The AST may
     * contain just enough information for computing this source file's
     * structure and properties as well as of all of its descendant elements.
     *
     * @param source the source string to parse (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @return the AST created from the given source string (not <code>null</code>)
     * @throws CoreException if the AST could not be created
     * @throws OperationCanceledException if this method is canceled
     * @see #hBuildStructure(SourceElementBody, Map, Object, String, IProgressMonitor)
     */
    protected abstract Object hCreateStructuralAst(String source,
        IProgressMonitor monitor) throws CoreException;

    /**
     * Initializes the given body based on the given AST and the given source
     * string from which the AST was created. Also, creates and initializes
     * bodies for all descendant elements and puts them into the given
     * <code>newElements</code> map.
     * <p>
     * The AST is safe to read in the dynamic context of this method call,
     * but must not be modified. Implementations must not keep references
     * to any part of the AST or the source string outside the dynamic scope
     * of the invocation of this method.
     * </p>
     *
     * @param body a new, uninitialized body for this element
     *  (never <code>null</code>)
     * @param newElements a map containing handle/body relationships
     *  (never <code>null</code>)
     * @param ast the AST created from the given source string
     *  (never <code>null</code>)
     * @param source the source string from which the given AST was created
     *  (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws OperationCanceledException if this method is canceled
     */
    protected abstract void hBuildStructure(SourceElementBody body,
        Map<IElement, Object> newElements, Object ast, String source,
        IProgressMonitor monitor);

    @Override
    protected void hToStringName(StringBuilder builder, IContext context)
    {
        if (hIsWorkingCopy())
            builder.append("[Working copy] "); //$NON-NLS-1$
        super.hToStringName(builder, context);
    }

    @Override
    protected void hGenerateAncestorBodies(Map<IElement, Object> newElements,
        IProgressMonitor monitor) throws CoreException
    {
        if (hIsWorkingCopy())
            return; // don't open ancestors for a working copy
        super.hGenerateAncestorBodies(newElements, monitor);
    }

    @Override
    protected boolean hClose(boolean external)
    {
        if (hIsWorkingCopy())
            return false; // a working copy cannot be removed
        return super.hClose(external);
    }

    private static void setSnapshot(SourceElementBody body, ISnapshot snapshot,
        Map<IElement, Object> newElements)
    {
        body.setSnapshot(snapshot);
        for (IElement child : body.getChildren())
        {
            setSnapshot((SourceElementBody)newElements.get(child), snapshot,
                newElements);
        }
    }

    private WorkingCopyInfo putWorkingCopyInfoIfAbsent(IBuffer buffer,
        IWorkingCopyInfoFactory factory)
    {
        return hElementManager().putWorkingCopyInfoIfAbsent(this, buffer,
            factory);
    }

    private WorkingCopyInfo getWorkingCopyInfo()
    {
        return hElementManager().getWorkingCopyInfo(this);
    }

    /**
     * A reconcile operation for this source file.
     * Intended to be used in {@link WorkingCopyInfo} implementations.
     * <p>
     * This class can be extended to augment the default behavior, e.g.
     * to send out a delta notification indicating the nature of the change
     * of the working copy since the last time it was reconciled.
     * </p>
     * @see SourceFile#hReconcileOperation()
     */
    public class ReconcileOperation
    {
        /**
         * Reconciles this working copy according to the given AST and the given
         * non-expiring snapshot on which the AST is based. The AST should contain
         * enough information for computing this working copy's structure and
         * properties as well as of all its descendant elements.
         * <p>
         * The AST is safe to read in the dynamic context of this method call,
         * but must not be modified. Implementations must not keep references
         * to any part of the AST or the snapshot outside the dynamic scope
         * of the invocation of this method.
         * </p>
         * <p>
         * Subclasses may override this method, but must call the <b>super</b>
         * implementation.
         * </p>
         *
         * @param ast the working copy AST based on the given snapshot
         *  (not <code>null</code>)
         * @param snapshot the non-expiring snapshot on which the given AST
         *  is based (not <code>null</code>)
         * @param forced indicates whether reconciling was forced, i.e.
         *  the working copy buffer has not been modified since the last time
         *  it was reconciled
         * @param monitor a progress monitor, or <code>null</code>
         *  if progress reporting is not desired
         * @throws CoreException if the working copy cannot be reconciled
         * @throws OperationCanceledException if this method is canceled
         */
        public void reconcile(Object ast, NonExpiringSnapshot snapshot,
            boolean forced, IProgressMonitor monitor) throws CoreException
        {
            WorkingCopyInfo info = hPeekAtWorkingCopyInfo();
            boolean create = !info.created; // case of wc creation
            if (create || !forced || shouldRebuildStructureIfForced())
            {
                if (AST_HOLDER.get() != null)
                    throw new AssertionError(); // should never happen
                AST_HOLDER.set(new AstHolder(ast, snapshot));
                try
                {
                    hOpen(hNewBody(), true, monitor);
                }
                finally
                {
                    AST_HOLDER.set(null);
                }
            }
            if (create)
            {
                if (!info.created)
                    throw new AssertionError(); // should never happen
                hWorkingCopyModeChanged(); // notify about wc creation
            }
        }

        /**
         * Returns whether the structure should be rebuilt when reconciling
         * is forced (i.e. the working copy buffer has not been modified
         * since the last time it was reconciled). Default implementation
         * returns <code>false</code> since typically the structure remains
         * the same if reconciling is forced. Subclasses may override.
         *
         * @return <code>true</code> if the structure should be rebuilt
         *  when reconciling is forced, <code>false</code> otherwise
         */
        protected boolean shouldRebuildStructureIfForced()
        {
            return false;
        }
    }

    private abstract class WorkingCopyProvider
    {
        public WorkingCopyInfo acquireWorkingCopy()
        {
            for (;;)
            {
                if (isCanceled())
                    throw new OperationCanceledException();
                WorkingCopyInfo info = doAcquireWorkingCopy();
                if (info == null)
                    return null;
                boolean success = false;
                try
                {
                    success = waitForInit(info);
                }
                finally
                {
                    if (!success)
                        hDiscardWorkingCopy();
                }
                if (success)
                    return info;
            }
        }

        protected abstract WorkingCopyInfo doAcquireWorkingCopy();

        protected boolean isCanceled()
        {
            return false;
        }

        private boolean waitForInit(WorkingCopyInfo info)
        {
            for (int i = 0; i < 10; i++)
            {
                if (isCanceled())
                    throw new OperationCanceledException();
                try
                {
                    return info.initTask.wasSuccessful(10,
                        TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e)
                {
                }
                catch (TimeoutException e)
                {
                }
            }
            return false;
        }
    }

    private class AstHolder
    {
        public final Object ast;
        public final NonExpiringSnapshot snapshot;

        public AstHolder(Object ast, NonExpiringSnapshot snapshot)
        {
            this.ast = ast;
            this.snapshot = snapshot;
        }

        public SourceFile getSourceFile()
        {
            return SourceFile.this;
        }
    }
}
