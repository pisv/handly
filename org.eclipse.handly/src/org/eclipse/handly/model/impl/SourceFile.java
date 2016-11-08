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

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.model.IElementDeltaConstants.F_WORKING_COPY;
import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.MEDIUM;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;

/**
 * Common superclass for source files.
 * <p>
 * This abstract implementation doesn't depend on a specific representation for
 * the underlying file, like <code>IFile</code> or <code>IFileStore</code>.
 * The binding has to be made in a subclass.
 * </p>
 * <p>
 * If a notification manager is registered in the model context, this class
 * will take advantage of it to send out working copy notifications. See
 * {@link #hWorkingCopyModeChanged()} and {@link NotifyingReconcileOperation}.
 * </p>
 *
 * @see WorkspaceSourceFile
 */
public abstract class SourceFile
    extends SourceElement
    implements ISourceFileImpl
{
    /**
     * Constructs a handle for a source file with the given parent element and
     * the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     */
    public SourceFile(Element parent, String name)
    {
        super(parent, name);
    }

    @Override
    public final IBuffer hBuffer(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        monitor.beginTask("", 100); //$NON-NLS-1$
        try
        {
            WorkingCopyInfo info = hAcquireExistingWorkingCopy(
                new SubProgressMonitor(monitor, 10));
            if (info == null)
            {
                return hFileBuffer(context, new SubProgressMonitor(monitor,
                    90));
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
                    hReleaseWorkingCopy();
                }
            }
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * If this source file is not already in working copy mode, switches it
     * into a working copy, associates it with a working copy buffer via a new
     * working copy info, and acquires an independent ownership of the working
     * copy (and, hence, of the working copy buffer). Performs atomically.
     * <p>
     * In working copy mode the source file's structure and properties
     * shall no longer correspond to the underlying resource contents
     * and must no longer be updated by a resource delta processor.
     * Instead, the source file's structure and properties can be explicitly
     * {@link #hReconcile(IContext, IProgressMonitor) reconciled} with the
     * current contents of the working copy buffer.
     * </p>
     * <p>
     * This method supports the following options, which may be specified
     * in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #WORKING_COPY_BUFFER} - Specifies the working copy buffer.
     * If not set, a default buffer is associated with the working copy.
     * </li>
     * <li>
     * {@link #WORKING_COPY_INFO_FACTORY} - Specifies the working copy info factory.
     * If not set, a default factory is used for obtaining a new working copy info.
     * </li>
     * </ul>
     * <p>
     * If the source file was already in working copy mode, this method acquires
     * a new independent ownership of the working copy by incrementing an internal
     * counter and returns the info associated with the working copy; the given
     * context is ignored. The returned info is owned by the working copy and
     * must not be explicitly disposed by the client.
     * </p>
     * <p>
     * Each successful call to this method must ultimately be followed
     * by exactly one call to <code>hReleaseWorkingCopy</code>.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the working copy info previously associated with
     *  this source file, or <code>null</code> if there was no
     *  working copy info for this source file
     * @throws CoreException if the working copy could not be created successfully
     * @throws OperationCanceledException if this method is canceled
     * @see #hReleaseWorkingCopy()
     * @see #hAcquireExistingWorkingCopy(IProgressMonitor)
     */
    public final WorkingCopyInfo hBecomeWorkingCopy(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        if (context == null)
            throw new IllegalArgumentException();
        IBuffer buffer = context.get(WORKING_COPY_BUFFER);
        if (buffer == null)
        {
            try (IBuffer defaultBuffer = hFileBuffer(context, monitor))
            {
                return hBecomeWorkingCopy(with(of(WORKING_COPY_BUFFER,
                    defaultBuffer), context), monitor);
            }
        }
        WorkingCopyProvider provider = new WorkingCopyProvider()
        {
            @Override
            protected WorkingCopyInfo doAcquireWorkingCopy()
            {
                return hElementManager().putWorkingCopyInfoIfAbsent(
                    SourceFile.this, buffer, context.get(
                        WORKING_COPY_INFO_FACTORY));
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
                WorkingCopyInfo newInfo =
                    hElementManager().peekAtWorkingCopyInfo(this);
                newInfo.initTask.execute(context, monitor);
                success = true;
            }
            finally
            {
                if (!success)
                    hReleaseWorkingCopy();
            }
        }
        return oldInfo;
    }

    /**
     * Specifies the working copy buffer.
     * @see #hBecomeWorkingCopy(IContext, IProgressMonitor)
     */
    public static final Property<IBuffer> WORKING_COPY_BUFFER = Property.get(
        SourceFile.class.getName() + ".workingCopyBuffer", IBuffer.class); //$NON-NLS-1$
    /**
     * Specifies the working copy info factory.
     * @see #hBecomeWorkingCopy(IContext, IProgressMonitor)
     */
    public static final Property<WorkingCopyInfo.Factory> WORKING_COPY_INFO_FACTORY =
        Property.get(SourceFile.class.getName() + ".workingCopyInfoFactory", //$NON-NLS-1$
            WorkingCopyInfo.Factory.class);

    /**
     * If this source file is in working copy mode, acquires a new independent
     * ownership of the working copy by incrementing an internal counter and
     * returns the info associated with the working copy. The returned info is
     * owned by the working copy and must not be explicitly disposed by the client.
     * Returns <code>null</code> if this source file is not a working copy.
     * Performs atomically.
     * <p>
     * Each successful call to this method that did not return <code>null</code>
     * must ultimately be followed by exactly one call to <code>hReleaseWorkingCopy</code>.
     * </p>
     *
     * @return the working copy info for this source file,
     *  or <code>null</code> if this source file is not a working copy
     * @see #hReleaseWorkingCopy()
     * @see #hBecomeWorkingCopy(IContext, IProgressMonitor)
     */
    public final WorkingCopyInfo hAcquireExistingWorkingCopy(
        IProgressMonitor monitor)
    {
        WorkingCopyProvider provider = new WorkingCopyProvider()
        {
            @Override
            protected WorkingCopyInfo doAcquireWorkingCopy()
            {
                return hElementManager().getWorkingCopyInfo(SourceFile.this);
            }

            @Override
            protected boolean isCanceled()
            {
                if (monitor == null)
                    return false;
                return monitor.isCanceled();
            }
        };
        return provider.acquireWorkingCopy();
    }

    /**
     * Relinquishes an independent ownership of the working copy by decrementing
     * an internal counter. If there are no remaining owners of the working copy,
     * switches this source file from working copy mode back to its original mode
     * and releases the working copy buffer. Performs atomically.
     * <p>
     * Each independent ownership of the working copy must ultimately end
     * with exactly one call to this method. Clients that do not own the
     * working copy must not call this method.
     * </p>
     *
     * @return <code>true</code> if this source file was switched from
     *  working copy mode back to its original mode, <code>false</code>
     *  otherwise
     */
    public final boolean hReleaseWorkingCopy()
    {
        WorkingCopyInfo info = hElementManager().releaseWorkingCopyInfo(this);
        if (info == null)
            throw new IllegalStateException("Not a working copy: " + hToString( //$NON-NLS-1$
                of(FORMAT_STYLE, MEDIUM)));
        if (info.isDisposed() && info.created)
        {
            hWorkingCopyModeChanged();
            return true;
        }
        return false;
    }

    /**
     * If this source file is in working copy mode, returns the working copy info
     * without acquiring an independent ownership of the working copy. The
     * returned info is owned by the working copy and must not be explicitly
     * disposed by the client. Returns <code>null</code> if this source file
     * is not a working copy.
     * <p>
     * Note that if this method is invoked by a client that does not own the
     * working copy, the returned info may get disposed from another thread.
     * </p>
     *
     * @return the working copy info for this source file,
     *  or <code>null</code> if this source file is not a working copy
     * @see #hAcquireExistingWorkingCopy(IProgressMonitor)
     * @see #hBecomeWorkingCopy(IContext, IProgressMonitor)
     */
    public final WorkingCopyInfo hWorkingCopyInfo()
    {
        WorkingCopyInfo info = hElementManager().peekAtWorkingCopyInfo(this);
        if (info == null)
            return null;
        if (info.created)
            return info;
        // special case: wc creation is in progress on the current thread
        if (this.equals(CURRENTLY_RECONCILED.get()))
            return info;
        return null;
    }

    @Override
    public final boolean hIsWorkingCopy()
    {
        return hWorkingCopyInfo() != null;
    }

    @Override
    public final boolean hNeedsReconciling()
    {
        WorkingCopyInfo info = hAcquireExistingWorkingCopy(null);
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
                hReleaseWorkingCopy();
            }
        }
    }

    @Override
    public final void hReconcile(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        monitor.beginTask("", 100); //$NON-NLS-1$
        try
        {
            WorkingCopyInfo info = hAcquireExistingWorkingCopy(
                new SubProgressMonitor(monitor, 10));
            if (info == null)
                return; // not a working copy
            else
            {
                try
                {
                    info.reconcile(context, new SubProgressMonitor(monitor,
                        90));
                }
                finally
                {
                    hReleaseWorkingCopy();
                }
            }
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * Returns a reconcile operation for this source file.
     * <p>
     * This implementation returns a new instance of {@link
     * NotifyingReconcileOperation} if there is a notification manager
     * registered in the model context. Otherwise, a new instance of
     * {@link ReconcileOperation} is returned.
     * </p>
     * <p>
     * Clients are not intended to invoke this method, but may override it.
     * </p>
     *
     * @return a reconcile operation for this source file (not <code>null</code>)
     */
    protected ReconcileOperation hReconcileOperation()
    {
        if (hModel().getModelContext().get(INotificationManager.class) != null)
            return new NotifyingReconcileOperation();

        return new ReconcileOperation();
    }

    /**
     * Returns a context to be associated with a new working copy of this
     * source file. The given operation context is propagated from the
     * {@link #hBecomeWorkingCopy} method.
     * <p>
     * This implementation returns an empty context. Subclasses may override.
     * </p>
     *
     * @param context the operation context (never <code>null</code>)
     * @return the working copy context (not <code>null</code>)
     */
    protected IContext hWorkingCopyContext(IContext context)
    {
        return EMPTY_CONTEXT;
    }

    /**
     * Notifies about a working copy mode change: either the source file
     * became a working copy or reverted back from the working copy mode.
     * <p>
     * This implementation does nothing if no notification manager is
     * registered in the model context. Otherwise, it sends out a delta
     * notification indicating the nature of the working copy mode change.
     * </p>
     * <p>
     * Clients are not intended to invoke this method, but may override it.
     * </p>
     */
    protected void hWorkingCopyModeChanged()
    {
        INotificationManager notificationManager =
            hModel().getModelContext().get(INotificationManager.class);
        if (notificationManager == null)
            return;

        ElementDelta.Factory deltaFactory = hModel().getModelContext().get(
            ElementDelta.Factory.class);
        if (deltaFactory == null)
            deltaFactory = element -> new ElementDelta(element);
        ElementDelta rootDelta = deltaFactory.newDelta(hRoot());
        ElementDelta.Builder builder = new ElementDelta.Builder(rootDelta);
        if (hFileExists())
            builder.changed(this, F_WORKING_COPY);
        else if (hIsWorkingCopy())
            builder.added(this, F_WORKING_COPY);
        else
            builder.removed(this, F_WORKING_COPY);
        notificationManager.fireElementChangeEvent(new ElementChangeEvent(
            ElementChangeEvent.POST_CHANGE, builder.getDelta()));
    }

    @Override
    protected void hValidateExistence(IContext context) throws CoreException
    {
        if (!hIsWorkingCopy())
        {
            if (!hFileExists())
                throw hDoesNotExistException();
        }
    }

    @Override
    void hBuildStructure0(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (!context.containsKey(SOURCE_CONTENTS) && !context.containsKey(
            SOURCE_AST))
        {
            if (hIsWorkingCopy())
                throw new AssertionError();
            // NOTE: source files that are not working copies must reflect
            // the structure of the underlying file rather than the buffer
            NonExpiringSnapshot snapshot;
            try (ISnapshotProvider provider = hFileSnapshotProvider())
            {
                try
                {
                    snapshot = new NonExpiringSnapshot(provider);
                }
                catch (IllegalStateException e)
                {
                    Throwable cause = e.getCause();
                    if (cause instanceof CoreException)
                        throw (CoreException)cause;
                    throw new CoreException(Activator.createErrorStatus(
                        e.getMessage(), e));
                }
            }
            context = with(of(SOURCE_CONTENTS, snapshot.getContents()), of(
                SOURCE_SNAPSHOT, snapshot.getWrappedSnapshot()), context);
        }

        super.hBuildStructure0(context, monitor);

        Map<IElement, Object> newElements = context.get(NEW_ELEMENTS);
        Object body = newElements.get(this);
        if (body instanceof SourceElementBody)
        {
            SourceElementBody thisBody = (SourceElementBody)body;

            String source = context.get(SOURCE_CONTENTS);
            if (source != null)
                thisBody.setFullRange(new TextRange(0, source.length()));

            ISnapshot snapshot = context.get(SOURCE_SNAPSHOT);
            if (snapshot != null)
                setSnapshot(thisBody, snapshot, newElements);
        }
    }

    /**
     * Returns whether the underlying file exists.
     *
     * @return <code>true</code> if the underlying file exists,
     *  and <code>false</code> otherwise
     */
    protected abstract boolean hFileExists();

    /**
     * Returns a snapshot provider for the underlying file's stored contents.
     * <p>
     * The client takes (potentially shared) ownership of the returned provider
     * and is responsible for releasing it. The provider will be disposed
     * only after it is released by every owner. The provider must not
     * be accessed by clients that don't own it.
     * </p>
     *
     * @return a snapshot provider for the underlying file's stored contents
     *  (not <code>null</code>)
     * @see ISnapshotProvider
     */
    protected abstract ISnapshotProvider hFileSnapshotProvider();

    /**
     * Returns the buffer opened for the underlying file of this source file.
     * Note that buffers may be shared by multiple clients, so the returned buffer
     * may have unsaved changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for releasing it. The buffer will be disposed
     * only after it is released by every owner. The buffer must not
     * be accessed by clients that don't own it.
     * </p>
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#CREATE_BUFFER CREATE_BUFFER} -
     * Indicates whether a new buffer should be created if none already exists.
     * </li>
     * </ul>
     *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the buffer opened for the underlying file of this source file,
     *  or <code>null</code> if <code>CREATE_BUFFER == false</code> and
     *  there is currently no buffer opened for that file
     * @throws CoreException if the buffer could not be opened successfully
     * @throws OperationCanceledException if this method is canceled
     * @see IBuffer
     */
    protected abstract IBuffer hFileBuffer(IContext context,
        IProgressMonitor monitor) throws CoreException;

    /**
     * Creates and initializes bodies for this element and for each
     * of its descendant elements according to options specified in the
     * given context. Uses the {@link #NEW_ELEMENTS} map in the given context
     * to associate the created bodies with their respective elements.
     * <p>
     * The following context options influence how the structure is built and,
     * if simultaneously present, must be mutually consistent:
     * </p>
     * <ul>
     * <li>
     * {@link #SOURCE_AST} - Specifies the AST to use when building the structure.
     * The AST is safe to read in the dynamic context of this method call, but
     * must not be modified.
     * </li>
     * <li>
     * {@link #SOURCE_CONTENTS} - Specifies the source string to use when
     * building the structure.
     * </li>
     * </ul>
     * <p>
     * At least one of <code>SOURCE_AST</code> or <code>SOURCE_CONTENTS</code>
     * will have a non-null value in the given context.
     * </p>
     * <p>
     * The given context may provide additional data that this method can use,
     * including the following:
     * </p>
     * <ul>
     * <li>
     * {@link #SOURCE_SNAPSHOT} - Specifies the source snapshot from which
     * <code>SOURCE_AST</code> was created or <code>SOURCE_CONTENTS</code>
     * was obtained. The snapshot may expire.
     * </li>
     * </ul>
     *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if this method fails
     * @throws OperationCanceledException if this method is canceled
     */
    @Override
    protected abstract void hBuildStructure(IContext context,
        IProgressMonitor monitor) throws CoreException;

    /**
     * Specifies the source AST.
     * @see #hBuildStructure(IContext, IProgressMonitor)
     */
    protected static final Property<Object> SOURCE_AST = Property.get(
        SourceFile.class.getName() + ".sourceAst", Object.class); //$NON-NLS-1$
    /**
     * Specifies the source string.
     * @see #hBuildStructure(IContext, IProgressMonitor)
     */
    protected static final Property<String> SOURCE_CONTENTS = Property.get(
        SourceFile.class.getName() + ".sourceContents", String.class); //$NON-NLS-1$
    /**
     * Specifies the source snapshot.
     * @see #hBuildStructure(IContext, IProgressMonitor)
     */
    protected static final Property<ISnapshot> SOURCE_SNAPSHOT = Property.get(
        SourceFile.class.getName() + ".sourceSnapshot", ISnapshot.class); //$NON-NLS-1$

    @Override
    protected void hGenerateAncestorBodies(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        if (hIsWorkingCopy())
            return; // don't open ancestors for a working copy
        super.hGenerateAncestorBodies(context, monitor);
    }

    @Override
    protected boolean hClose(boolean external)
    {
        if (hIsWorkingCopy())
            return false; // a working copy cannot be removed
        return super.hClose(external);
    }

    @Override
    protected void hToStringName(StringBuilder builder, IContext context)
    {
        if (hIsWorkingCopy())
            builder.append("[Working copy] "); //$NON-NLS-1$
        super.hToStringName(builder, context);
    }

    private static void setSnapshot(SourceElementBody body, ISnapshot snapshot,
        Map<IElement, Object> newElements)
    {
        body.setSnapshot(snapshot);
        for (IElement child : body.getChildren())
        {
            Object childBody = newElements.get(child);
            if (childBody instanceof SourceElementBody)
            {
                setSnapshot((SourceElementBody)childBody, snapshot,
                    newElements);
            }
        }
    }

    /**
     * Indicates whether the structure should be rebuilt when reconciling
     * is forced.
     * @see ReconcileOperation#reconcile(IContext, IProgressMonitor)
     */
    protected static final Property<Boolean> REBUILD_STRUCTURE_IF_FORCED =
        Property.get(SourceFile.class.getName() + ".rebuildStructureIfForced", //$NON-NLS-1$
            Boolean.class).withDefault(false);
    /**
     * Indicates whether reconciling was forced, i.e. the working copy buffer
     * has not been modified since the last time it was reconciled.
     * @see ReconcileOperation#reconcile(IContext, IProgressMonitor)
     */
    static final Property<Boolean> RECONCILING_FORCED = Property.get(
        SourceFile.class.getName() + ".reconcilingForced", //$NON-NLS-1$
        Boolean.class).withDefault(false);

    private static final ThreadLocal<SourceFile> CURRENTLY_RECONCILED =
        new ThreadLocal<SourceFile>(); // the source file being reconciled

    /**
     * A reconcile operation for this source file.
     * <p>
     * Clients are not intended to use instances of this class or a subclass
     * of this class, but may extend it to augment the default behavior, e.g.
     * to send out a delta notification indicating the nature of the change
     * of the working copy since the last time it was reconciled. Clients that
     * extend this class or a subclass of this class should consider
     * overriding {@link SourceFile#hReconcileOperation()} method.
     * </p>
     *
     * @see NotifyingReconcileOperation
     */
    protected class ReconcileOperation
    {
        /**
         * Reconciles this working copy according to options specified
         * in the given context.
         * <p>
         * The following context options can influence whether the structure
         * of the working copy gets rebuilt:
         * </p>
         * <ul>
         * <li>
         * {@link #REBUILD_STRUCTURE_IF_FORCED} - Indicates whether the structure
         * should be rebuilt even if reconciling was forced, i.e. the working copy
         * buffer has not been modified since the last time it was reconciled.
         * </li>
         * </ul>
         * <p>
         * The following context options influence rebuilding of the structure
         * of the working copy and, if simultaneously present, must be mutually
         * consistent:
         * </p>
         * <ul>
         * <li>
         * {@link #SOURCE_AST} - Specifies the AST to use when building the
         * structure. The AST is safe to read in the dynamic context of this
         * method call, but must not be modified.
         * </li>
         * <li>
         * {@link #SOURCE_CONTENTS} - Specifies the source string to use when
         * building the structure.
         * </li>
         * </ul>
         * <p>
         * At least one of <code>SOURCE_AST</code> or <code>SOURCE_CONTENTS</code>
         * must have a non-null value in the given context.
         * </p>
         * <p>
         * The given context may provide additional data that this method can use,
         * including the following:
         * </p>
         * <ul>
         * <li>
         * {@link #SOURCE_SNAPSHOT} - Specifies the source snapshot from which
         * <code>SOURCE_AST</code> was created or <code>SOURCE_CONTENTS</code>
         * was obtained. The snapshot may expire.
         * </li>
         * </ul>
         * <p>
         * Subclasses may override this method, but must call the <b>super</b>
         * implementation.
         * </p>
         *
         * @param context the operation context (not <code>null</code>)
         * @param monitor a progress monitor, or <code>null</code>
         *  if progress reporting is not desired
         * @throws CoreException if the working copy cannot be reconciled
         * @throws OperationCanceledException if this method is canceled
         */
        protected void reconcile(IContext context, IProgressMonitor monitor)
            throws CoreException
        {
            if (context.get(SOURCE_AST) == null && context.get(
                SOURCE_CONTENTS) == null)
            {
                throw new IllegalArgumentException();
            }
            WorkingCopyInfo info = hElementManager().peekAtWorkingCopyInfo(
                SourceFile.this);
            boolean create = !info.created; // case of wc creation
            if (create || !context.getOrDefault(RECONCILING_FORCED)
                || context.getOrDefault(REBUILD_STRUCTURE_IF_FORCED))
            {
                if (CURRENTLY_RECONCILED.get() != null)
                    throw new AssertionError(); // should never happen
                CURRENTLY_RECONCILED.set(SourceFile.this);
                try
                {
                    hOpen(with(of(FORCE_OPEN, true), context), monitor);
                }
                finally
                {
                    CURRENTLY_RECONCILED.set(null);
                }
            }
            if (create)
            {
                if (!info.created)
                    throw new AssertionError(); // should never happen
                hWorkingCopyModeChanged(); // notify about wc creation
            }
        }
    }

    /**
     * Reconciles this working copy and sends out a delta notification
     * indicating the nature of the change of the working copy since
     * the last time it was reconciled. Uses the notification manager
     * registered in the model context.
     *
     * @see INotificationManager
     */
    protected class NotifyingReconcileOperation
        extends ReconcileOperation
    {
        @Override
        protected void reconcile(IContext context, IProgressMonitor monitor)
            throws CoreException
        {
            ElementDelta.Factory deltaFactory = hModel().getModelContext().get(
                ElementDelta.Factory.class);
            if (deltaFactory == null)
                deltaFactory = element -> new ElementDelta(element);
            ElementDelta rootDelta = deltaFactory.newDelta(SourceFile.this);
            ElementDifferencer differ = new ElementDifferencer(
                new ElementDelta.Builder(rootDelta));

            doReconcile(context, monitor);

            differ.buildDelta();
            if (!differ.isEmptyDelta())
            {
                hModel().getModelContext().get(
                    INotificationManager.class).fireElementChangeEvent(
                        new ElementChangeEvent(
                            ElementChangeEvent.POST_RECONCILE,
                            differ.getDelta()));
            }
        }

        /**
         * This implementation calls {@link ReconcileOperation#reconcile(
         * IContext, IProgressMonitor) super.reconcile(..)}.
         * <p>
         * Subclasses may override this method, but must call its <b>super</b>
         * implementation.
         * </p>
         *
         * @param context the operation context (not <code>null</code>)
         * @param monitor a progress monitor, or <code>null</code>
         *  if progress reporting is not desired
         * @throws CoreException if the working copy cannot be reconciled
         * @throws OperationCanceledException if this method is canceled
         */
        protected void doReconcile(IContext context, IProgressMonitor monitor)
            throws CoreException
        {
            super.reconcile(context, monitor);
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
                        hReleaseWorkingCopy();
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
}
