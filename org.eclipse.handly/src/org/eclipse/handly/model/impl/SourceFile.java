/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.handly.snapshot.TextFileSnapshot;
import org.eclipse.handly.util.TextRange;

/**
 * Common superclass of {@link ISourceFile} implementations.
 */
public abstract class SourceFile
    extends SourceElement
    implements ISourceFile
{
    private static final ThreadLocal<AstHolder> AST_HOLDER =
        new ThreadLocal<AstHolder>();
    private static final ThreadLocal<WcCreation> WC_CREATION =
        new ThreadLocal<WcCreation>();

    /**
     * The underlying workspace file.
     */
    protected final IFile file;

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given underlying workspace file.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param file the workspace file underlying the element (not <code>null</code>)
     */
    public SourceFile(Handle parent, IFile file)
    {
        super(parent, file.getName());
        this.file = file;
    }

    @Override
    public final IResource getResource()
    {
        return file;
    }

    @Override
    public final IFile getFile()
    {
        return file;
    }

    @Override
    public final IBuffer getBuffer() throws CoreException
    {
        return getBuffer(true, null);
    }

    @Override
    public final IBuffer getBuffer(boolean create, IProgressMonitor monitor)
        throws CoreException
    {
        WorkingCopyInfo info = acquireWorkingCopy();
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
                IWorkingCopyBuffer buffer = info.getBuffer();
                buffer.addRef();
                return buffer;
            }
            finally
            {
                discardWorkingCopy();
            }
        }
    }

    @Override
    @Deprecated
    public final IBuffer openBuffer(IProgressMonitor monitor)
        throws CoreException
    {
        return getBuffer(true, monitor);
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
     * those structure and properties can be explicitly {@link #reconcile(
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
     * by exactly one call to <code>discardWorkingCopy</code>.
     * </p>
     *
     * @param buffer the working copy buffer (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the working copy info previously associated with
     *  this source file, or <code>null</code> if there was no
     *  working copy info for this source file
     * @throws CoreException if the working copy cannot be created
     * @see #discardWorkingCopy()
     */
    public final WorkingCopyInfo becomeWorkingCopy(IWorkingCopyBuffer buffer,
        IProgressMonitor monitor) throws CoreException
    {
        return becomeWorkingCopy(buffer, null, monitor);
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
     * those structure and properties can be explicitly {@link #reconcile(
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
     * by exactly one call to <code>discardWorkingCopy</code>.
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
     * @see #discardWorkingCopy()
     */
    public final WorkingCopyInfo becomeWorkingCopy(IWorkingCopyBuffer buffer,
        IWorkingCopyInfoFactory factory, IProgressMonitor monitor)
            throws CoreException
    {
        WorkingCopyInfo oldInfo = getHandleManager().putWorkingCopyInfoIfAbsent(
            this, buffer, factory);
        if (oldInfo == null)
        {
            boolean success = false;
            try
            {
                WC_CREATION.set(WcCreation.IN_PROGRESS);
                reconcile(false, monitor);
                success = true;
            }
            finally
            {
                WcCreation creationState = WC_CREATION.get();
                WC_CREATION.set(null);
                if (!success)
                {
                    if (getHandleManager().discardWorkingCopyInfo(this)
                        && creationState == WcCreation.FINISHED)
                    {
                        workingCopyModeChanged();
                    }
                }
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
     * Each successful call to this method that did not return
     * <code>null</code> must ultimately be followed by exactly
     * one call to <code>discardWorkingCopy</code>.
     * </p>
     *
     * @return the working copy info for this source file,
     *  or <code>null</code> if this source file is not a working copy
     * @see #discardWorkingCopy()
     */
    public final WorkingCopyInfo acquireWorkingCopy()
    {
        return getHandleManager().getWorkingCopyInfo(this);
    }

    /**
     * Relinquishes an independent ownership of the working copy by decrementing
     * an internal counter. If there are no remaining independent owners of the
     * working copy, switches this source file from working copy mode back to
     * its original mode and releases the working copy buffer. Has no effect
     * if this source file was not in working copy mode. Performs atomically.
     * <p>
     * Each independent ownership of the working copy must ultimately end
     * with exactly one call to this method.
     * </p>
     *
     * @return <code>true</code> if this source file was switched from
     *  working copy mode back to its original mode, <code>false</code>
     *  otherwise
     */
    public final boolean discardWorkingCopy()
    {
        if (getHandleManager().discardWorkingCopyInfo(this))
        {
            workingCopyModeChanged();
            return true;
        }
        return false;
    }

    @Override
    public final boolean isWorkingCopy()
    {
        return getWorkingCopyInfo() != null;
    }

    @Override
    public final boolean needsReconciling()
    {
        WorkingCopyInfo info = acquireWorkingCopy();
        if (info == null)
            return false;
        else
        {
            try
            {
                return info.getBuffer().needsReconciling();
            }
            finally
            {
                discardWorkingCopy();
            }
        }
    }

    @Override
    public final void reconcile(boolean force, IProgressMonitor monitor)
        throws CoreException
    {
        reconcile(force, null, monitor);
    }

    /**
     * This overload of the <code>reconcile</code> method takes an additional
     * <code>Object</code> argument reserved for model-specific use.
     *
     * @param force indicates whether reconciling has to be performed
     *  even if the working copy's contents have not changed since it was last
     *  reconciled
     * @param arg reserved for model-specific use (may be <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @throws CoreException if this working copy cannot be reconciled
     * @throws OperationCanceledException if this method is cancelled
     * @see #reconcile(boolean, IProgressMonitor)
     */
    public final void reconcile(boolean force, Object arg,
        IProgressMonitor monitor) throws CoreException
    {
        WorkingCopyInfo info = acquireWorkingCopy();
        if (info == null)
            return; // not a working copy
        else
        {
            try
            {
                if (monitor == null)
                    monitor = new NullProgressMonitor();

                info.getBuffer().reconcile(force, arg, monitor);
            }
            finally
            {
                discardWorkingCopy();
            }
        }
    }

    @Override
    public final boolean close()
    {
        if (isWorkingCopy())
            return false; // must not close a working copy

        return super.close();
    }

    /**
     * Returns a reconcile operation for this source file.
     * <p>
     * This method may be used in {@link IWorkingCopyBuffer} implementations.
     * Other clients are not intended to invoke this method.
     * </p>
     * <p>
     * Subclasses which extend {@link ReconcileOperation} should override
     * this method.
     * </p>
     *
     * @return a reconcile operation for this source file (not <code>null</code>)
     */
    public ReconcileOperation getReconcileOperation()
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
     * @see #acquireWorkingCopy()
     */
    protected final WorkingCopyInfo getWorkingCopyInfo()
    {
        return getHandleManager().peekAtWorkingCopyInfo(this);
    }

    /**
     * Notifies about a working copy mode change: either the source file
     * became a working copy or reverted back from the working copy mode.
     */
    protected void workingCopyModeChanged()
    {
        // subclasses might fire an appropriate event, etc.
    }

    @Override
    protected SourceElementBody newBody()
    {
        return new SourceElementBody();
    }

    @Override
    protected void validateExistence() throws CoreException
    {
        if (!isWorkingCopy())
        {
            if (!file.exists())
                throw new CoreException(Activator.createErrorStatus(
                    MessageFormat.format(
                        Messages.SourceFile_File_does_not_exist__0,
                        file.getFullPath().makeRelative()), null));
        }
    }

    @Override
    protected final void buildStructure(Body body,
        Map<IHandle, Body> newElements, IProgressMonitor monitor)
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
                            TextFileSnapshot result = new TextFileSnapshot(
                                getFile(), true);
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
                Object ast = createStructuralAst(snapshot.getContents(),
                    new SubProgressMonitor(monitor, 1));
                astHolder = new AstHolder(ast, snapshot);
                --ticks;
            }

            SourceElementBody thisBody = (SourceElementBody)body;
            String source = astHolder.snapshot.getContents();
            ISnapshot snapshot = astHolder.snapshot.getWrappedSnapshot();

            buildStructure(thisBody, newElements, astHolder.ast, source,
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
     * @throws OperationCanceledException if this method is cancelled
     * @see #buildStructure(SourceElementBody, Map, Object, String, IProgressMonitor)
     */
    protected abstract Object createStructuralAst(String source,
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
     * @throws OperationCanceledException if this method is cancelled
     */
    protected abstract void buildStructure(SourceElementBody body,
        Map<IHandle, Body> newElements, Object ast, String source,
        IProgressMonitor monitor);

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

    @Override
    protected void toStringName(StringBuilder builder)
    {
        if (isWorkingCopy())
            builder.append("[Working copy] "); //$NON-NLS-1$
        super.toStringName(builder);
    }

    private static void setSnapshot(SourceElementBody body, ISnapshot snapshot,
        Map<IHandle, Body> newElements)
    {
        body.setSnapshot(snapshot);
        for (IHandle child : body.getChildren())
        {
            setSnapshot((SourceElementBody)newElements.get(child), snapshot,
                newElements);
        }
    }

    /**
     * A reconcile operation for this source file.
     * Intended to be used in {@link IWorkingCopyBuffer} implementations.
     * <p>
     * This class can be extended to augment the default behavior, e.g.
     * to send out a delta notification indicating the nature of the change
     * of the working copy since the last time it was reconciled.
     * </p>
     * @see SourceFile#getReconcileOperation()
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
         * @throws OperationCanceledException if this method is cancelled
         */
        public void reconcile(Object ast, NonExpiringSnapshot snapshot,
            boolean forced, IProgressMonitor monitor) throws CoreException
        {
            if (!forced || shouldRebuildStructureIfForced())
            {
                if (AST_HOLDER.get() != null)
                    throw new AssertionError(); // should never happen
                AST_HOLDER.set(new AstHolder(ast, snapshot));
                try
                {
                    open(newBody(), true, monitor);
                }
                finally
                {
                    AST_HOLDER.set(null);
                }
            }

            if (WC_CREATION.get() == WcCreation.IN_PROGRESS)
            {
                workingCopyModeChanged(); // notify about wc creation
                WC_CREATION.set(WcCreation.FINISHED);
            }
        }
    }

    private static enum WcCreation
    {
        IN_PROGRESS,
        FINISHED
    }

    private static class AstHolder
    {
        public final Object ast;
        public final NonExpiringSnapshot snapshot;

        public AstHolder(Object ast, NonExpiringSnapshot snapshot)
        {
            this.ast = ast;
            this.snapshot = snapshot;
        }
    }
}
