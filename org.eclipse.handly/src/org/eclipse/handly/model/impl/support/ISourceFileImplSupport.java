/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.model.Elements.CREATE_BUFFER;
import static org.eclipse.handly.model.IElementDeltaConstants.CHANGED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_WORKING_COPY;
import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.MEDIUM;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.ICoreTextFileBufferProvider;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.context.Context;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.DefaultWorkingCopyCallback;
import org.eclipse.handly.model.impl.IElementImplExtension;
import org.eclipse.handly.model.impl.IReconcileStrategy;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.handly.model.impl.IWorkingCopyCallback;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.handly.snapshot.TextFileSnapshot;
import org.eclipse.handly.snapshot.TextFileStoreSnapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;

/**
 * A "trait-like" interface providing a skeletal implementation of {@link
 * ISourceFileImplExtension} to minimize the effort required to implement
 * that interface. Clients may implement ("mix in") this interface directly or
 * extend a class in the {@link SourceFile} hierarchy.
 * <p>
 * In general, the members first defined in this interface are not intended
 * to be referenced outside the subtype hierarchy.
 * </p>
 * <p>
 * If a notification manager is registered in the model context,
 * this implementation will take advantage of it to send out working copy
 * notifications. See {@link #workingCopyModeChanged_()} and {@link
 * NotifyingReconcileOperation}.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceFileImplSupport
    extends ISourceElementImplSupport, ISourceFileImplExtension
{
    @Override
    default int defaultHashCode_()
    {
        Object file = getFileObject_();
        if (file != null)
            return file.hashCode();
        return ISourceElementImplSupport.super.defaultHashCode_();
    }

    @Override
    default boolean defaultEquals_(Object obj)
    {
        if (!(obj instanceof ISourceFileImplSupport))
            return false;
        Object file = getFileObject_();
        return (file == null || file.equals(
            ((ISourceFileImplSupport)obj).getFileObject_()))
            && ISourceElementImplSupport.super.defaultEquals_(obj);
    }

    /**
     * Returns the underlying file object, if any. The relationship between
     * a source file and its underlying file object does not change.
     * <p>
     * This implementation returns the underlying <code>IFile</code>, if any.
     * If there is no underlying <code>IFile</code>, this implementation returns
     * the corresponding <code>IFileStore</code> (if any), on the assumption that
     * the relationship between this source file and the file store does not change.
     * </p>
     *
     * @return the underlying file object, or <code>null</code> if none
     * @since 1.3
     * @see #getFile_()
     * @see #getFileStore_()
     */
    default Object getFileObject_()
    {
        IFile file = getFile_();
        if (file != null)
            return file;

        return getFileStore_();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #getFileBuffer_(IContext,
     * IProgressMonitor)} if this source file is not a working copy;
     * otherwise, it returns the working copy buffer.
     * </p>
     * @throws CoreException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     */
    @Override
    default IBuffer getBuffer_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        if (!acquireExistingWorkingCopy_(subMonitor.split(10)))
        {
            return getFileBuffer_(context, subMonitor.split(90));
        }
        else
        {
            try
            {
                WorkingCopyInfo info =
                    getElementManager_().peekAtWorkingCopyInfo(this);

                if (info == null)
                    throw new AssertionError(
                        "This method probably needs to be overridden"); //$NON-NLS-1$

                IBuffer buffer = info.getBuffer();
                buffer.addRef();
                return buffer;
            }
            finally
            {
                releaseWorkingCopy_();
            }
        }
    }

    @Override
    default boolean becomeWorkingCopy_(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        if (context == null)
            throw new IllegalArgumentException();

        IBuffer buffer = context.get(WORKING_COPY_BUFFER);
        if (buffer == null)
            try (IBuffer defaultBuffer = getFileBuffer_(context, null))
            {
                return becomeWorkingCopy_(with(of(WORKING_COPY_BUFFER,
                    defaultBuffer), context), monitor);
            }

        IWorkingCopyCallback callback = context.get(WORKING_COPY_CALLBACK);
        if (callback == null)
            callback = new DefaultWorkingCopyCallback();

        WorkingCopyInfo info = new WorkingCopyInfo(buffer,
            newWorkingCopyContext_(context), new ReconcileStrategy(this),
            callback);

        return WorkingCopyHelper.becomeWorkingCopy(this, info, monitor);
    }

    @Override
    default boolean acquireExistingWorkingCopy_(IProgressMonitor monitor)
    {
        return WorkingCopyHelper.acquireExistingWorkingCopy(this, monitor);
    }

    @Override
    default boolean releaseWorkingCopy_()
    {
        WorkingCopyInfo info = getElementManager_().releaseWorkingCopyInfo(
            this);
        if (info == null)
            throw new IllegalStateException("Not a working copy: " + toString_( //$NON-NLS-1$
                of(FORMAT_STYLE, MEDIUM)));
        if (info.isDisposed() && info.created)
        {
            workingCopyModeChanged_();
            return true;
        }
        return false;
    }

    @Override
    default IContext getWorkingCopyContext_()
    {
        WorkingCopyInfo info = getElementManager_().peekAtWorkingCopyInfo(this);
        if (info == null)
            return null;
        if (info.created)
            return info.getContext();
        // special case: wc creation is in progress on the current thread
        if (this.equals(ReconcileOperation.CURRENTLY_RECONCILED.get()))
            return info.getContext();
        return null;
    }

    @Override
    default boolean isWorkingCopy_()
    {
        return getWorkingCopyContext_() != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to the namesake method of the working copy
     * callback, provided that this source file is a working copy.
     * </p>
     * @see IWorkingCopyCallback#needsReconciling()
     */
    @Override
    default boolean needsReconciling_()
    {
        if (!acquireExistingWorkingCopy_(null))
            return false;
        else
        {
            try
            {
                WorkingCopyInfo info =
                    getElementManager_().peekAtWorkingCopyInfo(this);

                if (info == null)
                    throw new AssertionError(
                        "This method probably needs to be overridden"); //$NON-NLS-1$

                return info.callback.needsReconciling();
            }
            finally
            {
                releaseWorkingCopy_();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to the namesake method of the working copy
     * callback, provided that this source file is a working copy.
     * </p>
     * @throws CoreException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     * @see IWorkingCopyCallback#reconcile(IContext, IProgressMonitor)
     */
    @Override
    default void reconcile_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        if (!acquireExistingWorkingCopy_(subMonitor.split(10)))
            return; // not a working copy
        else
        {
            try
            {
                WorkingCopyInfo info =
                    getElementManager_().peekAtWorkingCopyInfo(this);

                if (info == null)
                    throw new AssertionError(
                        "This method probably needs to be overriden"); //$NON-NLS-1$

                info.callback.reconcile(context, subMonitor.split(90));
            }
            finally
            {
                releaseWorkingCopy_();
            }
        }
    }

    /**
     * Returns a reconcile operation for this source file.
     * <p>
     * This method is called internally; it is not intended to be invoked by
     * clients.
     * </p>
     * <p>
     * This implementation returns a new instance of {@link
     * NotifyingReconcileOperation} if there is a notification manager
     * registered in the model context; otherwise, a new instance of
     * {@link ReconcileOperation} is returned.
     * </p>
     *
     * @return a reconcile operation for this source file
     *  (never <code>null</code>)
     */
    default ReconcileOperation getReconcileOperation_()
    {
        if (Elements.getModelContext(this).get(
            INotificationManager.class) != null)
            return new NotifyingReconcileOperation(this);

        return new ReconcileOperation(this);
    }

    /**
     * Returns a context to be associated with a new working copy of this
     * source file. The given operation context is propagated from the
     * {@link #becomeWorkingCopy_(IContext, IProgressMonitor)} method.
     * <p>
     * The returned context is composed of the context explicitly {@link
     * ISourceFileImplExtension#WORKING_COPY_CONTEXT specified} when creating
     * the working copy and an intrinsic context of the working copy itself,
     * in that order.
     * </p>
     * <p>
     * This method is called internally; it is not intended to be invoked by
     * clients.
     * </p>
     * <p>
     * This implementation returns <code>context.getOrDefault(WORKING_COPY_CONTEXT)</code>.
     * </p>
     *
     * @param context the operation context (never <code>null</code>)
     * @return the working copy context (never <code>null</code>)
     * @see #getWorkingCopyContext_()
     */
    default IContext newWorkingCopyContext_(IContext context)
    {
        return context.getOrDefault(WORKING_COPY_CONTEXT);
    }

    /**
     * Informs this source file about a working copy mode change: either
     * the source file became a working copy or reverted back from the
     * working copy mode.
     * <p>
     * This method is called internally; it is not intended to be invoked by
     * clients.
     * </p>
     * <p>
     * This implementation sends out a delta notification indicating the nature
     * of the working copy mode change, provided that a notification manager is
     * registered in the model context.
     * </p>
     *
     * @see INotificationManager
     */
    default void workingCopyModeChanged_()
    {
        INotificationManager notificationManager = Elements.getModelContext(
            this).get(INotificationManager.class);
        if (notificationManager == null)
            return;

        ElementDelta.Factory deltaFactory = Elements.getModelContext(this).get(
            ElementDelta.Factory.class);
        if (deltaFactory == null)
            deltaFactory = element -> new ElementDelta(element);

        ElementDelta.Builder builder = new ElementDelta.Builder(
            deltaFactory.newDelta(getRoot_()));

        if (fileExists_())
            builder.changed(this, F_WORKING_COPY);
        else if (isWorkingCopy_())
            builder.added(this, F_WORKING_COPY);
        else
            builder.removed(this, F_WORKING_COPY);

        notificationManager.fireElementChangeEvent(new ElementChangeEvent(
            ElementChangeEvent.POST_CHANGE, builder.getDelta()));
    }

    /**
     * Returns whether the underlying file exists.
     * <p>
     * If this source file has an underlying <code>IFile</code>,
     * this implementation returns <code>getFile_().exists()</code>.
     * Otherwise, if this source file has a corresponding <code>IFileStore</code>,
     * this implementation returns <code>getFileStore_().fetchInfo().exists()</code>.
     * Otherwise, an assertion error is thrown.
     * </p>
     *
     * @return <code>true</code> if the underlying file exists,
     *  and <code>false</code> otherwise
     */
    default boolean fileExists_()
    {
        IFile file = getFile_();
        if (file != null)
            return file.exists();

        IFileStore fileStore = getFileStore_();
        if (fileStore != null)
            return fileStore.fetchInfo().exists();

        throw new AssertionError("Please override this method"); //$NON-NLS-1$
    }

    /**
     * Returns a snapshot provider for the underlying file's stored contents.
     * <p>
     * The client takes (potentially shared) ownership of the returned provider
     * and is responsible for releasing it. The provider will be disposed
     * only after it is released by every owner. The provider must not
     * be accessed by clients which do not own it.
     * </p>
     * <p>
     * This implementation returns a snapshot provider for the stored contents
     * of the underlying <code>IFile</code> or, if this source file has no
     * underlying file in the workspace, of the corresponding <code>IFileStore</code>
     * (if any). If there is neither underlying <code>IFile</code> nor
     * corresponding <code>IFileStore</code>, an assertion error is thrown.
     * </p>
     *
     * @return a snapshot provider for the underlying file's stored contents
     *  (never <code>null</code>)
     */
    default ISnapshotProvider getFileSnapshotProvider_()
    {
        IFile file = getFile_();
        if (file != null)
            return () ->
            {
                TextFileSnapshot result = new TextFileSnapshot(file,
                    TextFileSnapshot.Layer.FILESYSTEM);
                if (!result.exists())
                {
                    throw new IllegalStateException(
                        newDoesNotExistException_());
                }
                if (result.getContents() == null && !result.getStatus().isOK())
                {
                    throw new IllegalStateException(new CoreException(
                        result.getStatus()));
                }
                return result;
            };

        IFileStore fileStore = getFileStore_();
        if (fileStore != null)
            return () ->
            {
                TextFileStoreSnapshot result = new TextFileStoreSnapshot(
                    fileStore);
                if (!result.exists())
                {
                    throw new IllegalStateException(
                        newDoesNotExistException_());
                }
                if (result.getContents() == null && !result.getStatus().isOK())
                {
                    throw new IllegalStateException(new CoreException(
                        result.getStatus()));
                }
                return result;
            };

        throw new AssertionError("Please override this method"); //$NON-NLS-1$
    }

    /**
     * Returns a buffer opened for the underlying file of this source file.
     * Note that buffers may be shared by multiple clients, so the returned buffer
     * may have unsaved changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for releasing it. The buffer will be disposed
     * only after it is released by every owner. The buffer must not
     * be accessed by clients which do not own it.
     * </p>
     * <p>
     * A new object may be returned, even for the same underlying buffer,
     * each time this method is invoked.
     * </p>
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#CREATE_BUFFER CREATE_BUFFER} -
     * Indicates whether a new buffer should be created if none already exists
     * for the underlying file.
     * </li>
     * </ul>
     * <p>
     * This implementation returns a buffer opened for the underlying
     * <code>IFile</code> or, if this source file has no underlying file
     * in the workspace, for the corresponding <code>IFileStore</code> (if any).
     * If there is neither underlying <code>IFile</code> nor corresponding
     * <code>IFileStore</code>, an assertion error is thrown.
     * If <code>CREATE_BUFFER</code> is <code>false</code> in the given context
     * and there is currently no buffer opened for the file, <code>null</code>
     * is returned.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return a buffer opened for the underlying file of this source file,
     *  or <code>null</code> if <code>CREATE_BUFFER</code> is <code>false</code>
     *  in the given context and there is currently no buffer opened for that file
     * @throws CoreException if the buffer could not be opened
     * @throws OperationCanceledException if this method is canceled
     */
    default IBuffer getFileBuffer_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        ICoreTextFileBufferProvider provider;
        IFile file = getFile_();
        if (file != null)
        {
            provider = ICoreTextFileBufferProvider.forLocation(
                file.getFullPath(), LocationKind.IFILE,
                ITextFileBufferManager.DEFAULT);
        }
        else
        {
            IFileStore fileStore = getFileStore_();
            if (fileStore != null)
            {
                provider = ICoreTextFileBufferProvider.forFileStore(fileStore,
                    ITextFileBufferManager.DEFAULT);
            }
            else
                throw new AssertionError("Please override this method"); //$NON-NLS-1$
        }
        if (!context.getOrDefault(CREATE_BUFFER)
            && provider.getBuffer() == null)
        {
            return null;
        }
        return new TextFileBuffer(provider, monitor);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing if this source file is a working copy;
     * otherwise, it throws a {@link #newDoesNotExistException_()} if the
     * underlying file does not {@link #fileExists_() exist}.
     * </p>
     */
    @Override
    default void validateExistence_(IContext context) throws CoreException
    {
        if (!isWorkingCopy_())
        {
            if (!fileExists_())
                throw newDoesNotExistException_();
        }
    }

    /**
     * Opens the parent element if necessary.
     * <p>
     * This method is called internally; it is not intended to be invoked
     * by clients.
     * </p>
     * <p>
     * This implementation does nothing if this source file is a working copy;
     * otherwise, it attempts to {@link #open_(IContext, IProgressMonitor) open}
     * the parent element if it supports {@link IElementImplExtension} and
     * is not already open.
     * </p>
     * @throws CoreException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     */
    @Override
    default void openParent_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (isWorkingCopy_())
            return; // don't open the parent element for a working copy
        ISourceElementImplSupport.super.openParent_(context, monitor);
    }

    /**
     * This implementation delegates to {@link #buildSourceStructure_(IContext,
     * IProgressMonitor)} with an appropriately augmented context. In particular,
     * if the given context contains neither {@link #SOURCE_CONTENTS} nor {@link
     * #SOURCE_AST}, it is augmented with {@link #SOURCE_CONTENTS} and the
     * corresponding {@link #SOURCE_SNAPSHOT} obtained from the underlying
     * file's {@link #getFileSnapshotProvider_() stored contents}. Also,
     * performs some post-processing of created {@link SourceElementBody}s
     * to complete their initialization, such as setting the source snapshot
     * on which they are based.
     *
     * @throws CoreException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     */
    @Override
    default void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (!context.containsKey(SOURCE_CONTENTS) && !context.containsKey(
            SOURCE_AST))
        {
            if (isWorkingCopy_())
                throw new AssertionError();
            // NOTE: source files that are not working copies must reflect
            // the structure of the underlying file rather than the buffer
            NonExpiringSnapshot snapshot;
            try (ISnapshotProvider provider = getFileSnapshotProvider_())
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

        buildSourceStructure_(context, monitor);

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
                thisBody.setSnapshot(snapshot, newElements);
        }
    }

    /**
     * Specifies the source AST.
     *
     * @see #buildSourceStructure_(IContext, IProgressMonitor)
     */
    Property<Object> SOURCE_AST = Property.get(
        ISourceFileImplSupport.class.getName() + ".sourceAst", Object.class); //$NON-NLS-1$

    /**
     * Specifies the source string.
     *
     * @see #buildSourceStructure_(IContext, IProgressMonitor)
     */
    Property<String> SOURCE_CONTENTS = Property.get(
        ISourceFileImplSupport.class.getName() + ".sourceContents", //$NON-NLS-1$
        String.class);

    /**
     * Specifies the source snapshot.
     *
     * @see #buildSourceStructure_(IContext, IProgressMonitor)
     */
    Property<ISnapshot> SOURCE_SNAPSHOT = Property.get(
        ISourceFileImplSupport.class.getName() + ".sourceSnapshot", //$NON-NLS-1$
        ISnapshot.class);

    /**
     * Creates and initializes bodies for this element and for each
     * of its descendant elements according to options specified in the
     * given context. Uses the {@link #NEW_ELEMENTS} map in the given context
     * to associate the created bodies with their respective elements.
     * <p>
     * The following context options, if simultaneously present, must be
     * mutually consistent:
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
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor (not <code>null</code>).
     *  The caller must not rely on {@link IProgressMonitor#done()}
     *  having been called by the receiver
     * @throws CoreException if the structure could not be determined
     * @throws OperationCanceledException if this method is canceled
     */
    void buildSourceStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException;

    /**
     * {@inheritDoc}
     * <p>
     * Note that a working copy is never permitted to close.
     * </p>
     */
    @Override
    default void close_(IContext context)
    {
        synchronized (getElementManager_())
        {
            if (isWorkingCopy_())
                return;
            ISourceElementImplSupport.super.close_(context);
        }
    }

    @Override
    default void toStringName_(StringBuilder builder, IContext context)
    {
        if (isWorkingCopy_())
            builder.append("[Working copy] "); //$NON-NLS-1$
        ISourceElementImplSupport.super.toStringName_(builder, context);
    }

    /**
     * Reconciles a working copy.
     * <p>
     * This class or a subclass of this class are not intended to be used by
     * clients for purposes other than extension or instance creation;
     * instances of this class or a subclass of this class are not intended
     * to be used by clients for purposes other than returning from an
     * implementation of {@link ISourceFileImplSupport#getReconcileOperation_()}.
     * </p>
     *
     * @see NotifyingReconcileOperation
     */
    class ReconcileOperation
    {
        static final Property<Boolean> INITIAL_RECONCILE = Property.get(
            ReconcileOperation.class.getName() + ".initialReconcile", //$NON-NLS-1$
            Boolean.class).withDefault(false);

        static final Property<Boolean> RECONCILING_FORCED = Property.get(
            ReconcileOperation.class.getName() + ".reconcilingForced", //$NON-NLS-1$
            Boolean.class).withDefault(false);

        static final ThreadLocal<ISourceFileImplSupport> CURRENTLY_RECONCILED =
            new ThreadLocal<>(); // the source file being reconciled

        /**
         * This operation's source file.
         */
        protected final ISourceFileImplSupport sourceFile;

        /**
         * Constructs a reconcile operation for the given source file.
         *
         * @param sourceFile not <code>null</code>
         */
        protected ReconcileOperation(ISourceFileImplSupport sourceFile)
        {
            if (sourceFile == null)
                throw new IllegalArgumentException();
            this.sourceFile = sourceFile;
        }

        /**
         * Reconciles the working copy according to options specified
         * in the given context.
         * <p>
         * The following context options, if simultaneously present, must be
         * mutually consistent:
         * </p>
         * <ul>
         * <li>
         * {@link #SOURCE_AST} - Specifies the AST to use when reconciling.
         * The AST is safe to read in the dynamic context of this method call,
         * but must not be modified.
         * </li>
         * <li>
         * {@link #SOURCE_CONTENTS} - Specifies the source string to use when
         * reconciling.
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
         * Subclasses may override this method, but must make sure to call
         * the <b>super</b> implementation.
         * </p>
         * <p>
         * This implementation calls {@link #reconcileStructure(IContext,
         * IProgressMonitor) reconcileStructure} if, and only if, {@link
         * #shouldReconcileStructure(IContext) shouldReconcileStructure}
         * returns <code>true</code>.
         * </p>
         *
         * @param context the operation context (not <code>null</code>)
         * @param monitor a progress monitor, or <code>null</code>
         *  if progress reporting is not desired. The caller must not rely on
         *  {@link IProgressMonitor#done()} having been called by the receiver
         * @throws CoreException if the working copy could not be reconciled
         * @throws OperationCanceledException if this method is canceled
         */
        protected void reconcile(IContext context, IProgressMonitor monitor)
            throws CoreException
        {
            if (shouldReconcileStructure(context))
            {
                reconcileStructure(context, monitor);
            }
        }

        /**
         * Returns whether the structure of the working copy needs to be
         * reconciled by rebuilding it according to options specified in the
         * given context.
         * <p>
         * Subclasses may override this method, but must return <code>true</code>
         * whenever the <b>super</b> implementation returns <code>true</code>;
         * they may return <code>true</code> when the <b>super</b> implementation
         * returns <code>false</code>.
         * </p>
         *
         * @param context the operation context (not <code>null</code>)
         * @return <code>true</code> if the working copy structure needs to be
         *  reconciled, and <code>false</code> otherwise
         * @see #reconcile(IContext, IProgressMonitor)
         * @see #reconcileStructure(IContext, IProgressMonitor)
         */
        protected boolean shouldReconcileStructure(IContext context)
        {
            return isInitialReconcile(context) || !isReconcilingForced(context);
        }

        /**
         * Returns whether this is initial reconcile.
         *
         * @param context the operation context (not <code>null</code>)
         * @return <code>true</code> if this is initial reconcile,
         *  and <code>false</code> otherwise
         * @see #reconcile(IContext, IProgressMonitor)
         * @see #reconcileStructure(IContext, IProgressMonitor)
         */
        protected static boolean isInitialReconcile(IContext context)
        {
            return context.getOrDefault(INITIAL_RECONCILE);
        }

        /**
         * Returns whether reconciling is forced, i.e., the working copy buffer
         * has not been modified since the last time the working copy was
         * reconciled.
         *
         * @param context the operation context (not <code>null</code>)
         * @return <code>true</code> if reconciling is forced,
         *  and <code>false</code> otherwise
         * @see #reconcile(IContext, IProgressMonitor)
         * @see #reconcileStructure(IContext, IProgressMonitor)
         */
        protected static boolean isReconcilingForced(IContext context)
        {
            return context.getOrDefault(RECONCILING_FORCED);
        }

        /**
         * Reconciles the structure of the working copy by rebuilding it
         * according to options specified in the given context.
         * <p>
         * The following context options, if simultaneously present, must be
         * mutually consistent:
         * </p>
         * <ul>
         * <li>
         * {@link #SOURCE_AST} - Specifies the AST to use when reconciling.
         * The AST is safe to read in the dynamic context of this method call,
         * but must not be modified.
         * </li>
         * <li>
         * {@link #SOURCE_CONTENTS} - Specifies the source string to use when
         * reconciling.
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
         * Subclasses may override this method, but must make sure to call
         * the <b>super</b> implementation.
         * </p>
         *
         * @param context the operation context (not <code>null</code>)
         * @param monitor a progress monitor, or <code>null</code>
         *  if progress reporting is not desired. The caller must not rely on
         *  {@link IProgressMonitor#done()} having been called by the receiver
         * @throws CoreException if the working copy could not be reconciled
         * @throws OperationCanceledException if this method is canceled
         * @see #reconcile(IContext, IProgressMonitor)
         * @see #shouldReconcileStructure(IContext)
         */
        protected void reconcileStructure(IContext context,
            IProgressMonitor monitor) throws CoreException
        {
            if (context.get(SOURCE_AST) == null && context.get(
                SOURCE_CONTENTS) == null)
            {
                throw new IllegalArgumentException();
            }
            if (CURRENTLY_RECONCILED.get() != null)
                throw new AssertionError(); // should never happen
            CURRENTLY_RECONCILED.set(sourceFile);
            try
            {
                sourceFile.open_(with(of(FORCE_OPEN, true), context), monitor);
            }
            finally
            {
                CURRENTLY_RECONCILED.set(null);
            }
            if (isInitialReconcile(context))
            {
                WorkingCopyInfo info =
                    sourceFile.getElementManager_().peekAtWorkingCopyInfo(
                        sourceFile);
                if (!info.created)
                    throw new AssertionError(); // should never happen

                sourceFile.workingCopyModeChanged_(); // notify about wc creation
            }
        }
    }

    /**
     * Reconciles a working copy and sends out a delta notification
     * indicating the nature of the change of the working copy since
     * the last time it was reconciled. Uses the notification manager
     * registered in the model context.
     * <p>
     * This class or a subclass of this class are not intended to be used by
     * clients for purposes other than extension or instance creation;
     * instances of this class or a subclass of this class are not intended
     * to be used by clients for purposes other than returning from an
     * implementation of {@link ISourceFileImplSupport#getReconcileOperation_()}.
     * </p>
     *
     * @see INotificationManager
     */
    class NotifyingReconcileOperation
        extends ReconcileOperation
    {
        /**
         * Constructs a notifying reconcile operation for the given source file.
         *
         * @param sourceFile not <code>null</code>
         */
        protected NotifyingReconcileOperation(ISourceFileImplSupport sourceFile)
        {
            super(sourceFile);
        }

        /**
         * {@inheritDoc}
         * <p>
         * If <code>shouldReconcileStructure</code> returns <code>true</code>,
         * this implementation invokes <code>reconcileStructure</code>, builds
         * the resulting delta using an element {@link #newChangeRecorder()
         * change recorder}, and sends out a <code>POST_RECONCILE</code> event
         * using the notification manager registered in the model context.
         * </p>
         * <p>
         * Since 1.1, no delta will be built and no event will be sent
         * by this implementation if <code>shouldNotifyChange</code>
         * returns <code>false</code>.
         * </p>
         */
        @Override
        protected void reconcile(IContext context, IProgressMonitor monitor)
            throws CoreException
        {
            if (!shouldReconcileStructure(context) || !shouldNotifyChange(
                context))
                super.reconcile(context, monitor);
            else
            {
                ElementChangeRecorder recorder = newChangeRecorder();
                recorder.beginRecording(sourceFile);

                super.reconcile(context, monitor);

                IElementDelta delta = recorder.endRecording().getDelta();
                if (delta != null && ElementDeltas.getKind(delta) == CHANGED)
                {
                    Elements.getModelContext(sourceFile).get(
                        INotificationManager.class).fireElementChangeEvent(
                            new ElementChangeEvent(
                                ElementChangeEvent.POST_RECONCILE, delta));
                }
            }
        }

        /**
         * Returns whether the delta notification is needed according to
         * options specified in the given context. If this method returns
         * <code>false</code>, no delta will be built and no event will be
         * sent.
         * <p>
         * Note that if you override <code>shouldReconcileStructure</code>,
         * you might need to override this method too.
         * </p>
         *
         * @param context the operation context (not <code>null</code>)
         * @return <code>true</code> if the delta notification is needed,
         *  and <code>false</code> otherwise
         * @since 1.1
         * @see #reconcile(IContext, IProgressMonitor)
         * @see #shouldReconcileStructure(IContext)
         */
        protected boolean shouldNotifyChange(IContext context)
        {
            if (isInitialReconcile(context))
            {
                ISnapshot snapshot = context.get(SOURCE_SNAPSHOT);
                if (snapshot != null)
                {
                    ISnapshot currentSnapshot = null;
                    Object body = sourceFile.peekAtBody_();
                    if (body == null)
                    {
                        try (
                            ISnapshotProvider provider =
                                sourceFile.getFileSnapshotProvider_())
                        {
                            try
                            {
                                currentSnapshot = provider.getSnapshot();
                            }
                            catch (IllegalStateException e)
                            {
                                // ignore
                            }
                        }
                    }
                    else if (body instanceof SourceElementBody)
                    {
                        currentSnapshot =
                            ((SourceElementBody)body).getSnapshot();
                    }
                    if (snapshot.isEqualTo(currentSnapshot))
                        return false;
                }
            }
            return true;
        }

        /**
         * Returns a new instance of element change recorder for this operation.
         *
         * @return a new instance of element change recorder
         *  (never <code>null</code>)
         */
        protected ElementChangeRecorder newChangeRecorder()
        {
            return new ElementChangeRecorder();
        }
    }
}

abstract class WorkingCopyHelper
{
    static boolean becomeWorkingCopy(ISourceFileImplSupport sourceFile,
        WorkingCopyInfo info, IProgressMonitor monitor) throws CoreException
    {
        WorkingCopyHelper helper = new WorkingCopyHelper(sourceFile)
        {
            @Override
            WorkingCopyInfo doAcquireWorkingCopy()
            {
                return sourceFile.getElementManager_().putWorkingCopyInfoIfAbsent(
                    sourceFile, info);
            }

            @Override
            boolean isCanceled()
            {
                if (monitor == null)
                    return false;
                return monitor.isCanceled();
            }
        };
        WorkingCopyInfo existingInfo = helper.acquireWorkingCopy();
        if (existingInfo == null)
        {
            boolean success = false;
            try
            {
                info.initTask.execute(monitor);
                success = true;
            }
            finally
            {
                if (!success)
                    sourceFile.releaseWorkingCopy_();
            }
        }
        return existingInfo == null;
    }

    static boolean acquireExistingWorkingCopy(ISourceFileImplSupport sourceFile,
        IProgressMonitor monitor)
    {
        WorkingCopyHelper helper = new WorkingCopyHelper(sourceFile)
        {
            @Override
            WorkingCopyInfo doAcquireWorkingCopy()
            {
                return sourceFile.getElementManager_().getWorkingCopyInfo(
                    sourceFile);
            }

            @Override
            boolean isCanceled()
            {
                if (monitor == null)
                    return false;
                return monitor.isCanceled();
            }
        };
        WorkingCopyInfo existingInfo = helper.acquireWorkingCopy();
        return existingInfo != null;
    }

    private final ISourceFileImplSupport sourceFile;

    private WorkingCopyHelper(ISourceFileImplSupport sourceFile)
    {
        if (sourceFile == null)
            throw new IllegalArgumentException();
        this.sourceFile = sourceFile;
    }

    WorkingCopyInfo acquireWorkingCopy()
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
                    sourceFile.releaseWorkingCopy_();
            }
            if (success)
                return info;
        }
    }

    abstract WorkingCopyInfo doAcquireWorkingCopy();

    boolean isCanceled()
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
                return info.initTask.wasSuccessful(10, TimeUnit.MILLISECONDS);
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

class ReconcileStrategy
    implements IReconcileStrategy
{
    private final ISourceFileImplSupport sourceFile;

    ReconcileStrategy(ISourceFileImplSupport sourceFile)
    {
        if (sourceFile == null)
            throw new IllegalArgumentException();
        this.sourceFile = sourceFile;
    }

    @Override
    public void reconcile(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (context.get(SOURCE_AST) == null && context.get(
            SOURCE_CONTENTS) == null)
        {
            throw new IllegalArgumentException();
        }

        Context context2 = new Context();

        WorkingCopyInfo info =
            sourceFile.getElementManager_().peekAtWorkingCopyInfo(sourceFile);
        context2.bind(
            ISourceFileImplSupport.ReconcileOperation.INITIAL_RECONCILE).to(
                !info.created);

        context2.bind(
            ISourceFileImplSupport.ReconcileOperation.RECONCILING_FORCED).to(
                context.getOrDefault(RECONCILING_FORCED));

        if (context.containsKey(SOURCE_AST))
            context2.bind(ISourceFileImplSupport.SOURCE_AST).to(context.get(
                SOURCE_AST));

        if (context.containsKey(SOURCE_CONTENTS))
            context2.bind(ISourceFileImplSupport.SOURCE_CONTENTS).to(
                context.get(SOURCE_CONTENTS));

        if (context.containsKey(SOURCE_SNAPSHOT))
            context2.bind(ISourceFileImplSupport.SOURCE_SNAPSHOT).to(
                context.get(SOURCE_SNAPSHOT));

        sourceFile.getReconcileOperation_().reconcile(with(context2, context),
            monitor);
    }
}
