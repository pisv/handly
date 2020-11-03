/*******************************************************************************
 * Copyright (c) 2015, 2020 1C-Soft LLC.
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
package org.eclipse.handly.ui.text.reconciler;

import static org.eclipse.handly.model.IElementDeltaConstants.F_CHILDREN;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MARKERS;
import static org.eclipse.handly.model.IElementDeltaConstants.F_SYNC;
import static org.eclipse.handly.model.IElementDeltaConstants.F_UNDERLYING_RESOURCE;
import static org.eclipse.handly.model.IElementDeltaConstants.F_WORKING_COPY;

import java.util.function.Function;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract base class of a working copy reconciler that is activated on
 * viewer activation and forces reconciling on a significant change in the
 * underlying model.
 */
public abstract class WorkingCopyReconciler
    extends AbstractReconciler
{
    private Function<IDocument, ISourceFile> documentToSourceFile;
    private IReconcilingStrategy strategy;
    private volatile ISourceFile sourceFile;
    private volatile boolean active = true;
    private volatile boolean modelChanged = false;
    private volatile boolean initialProcessDone = false;
    private final IElementChangeListener elementChangeListener =
        new IElementChangeListener()
        {
            @Override
            public void elementChanged(IElementChangeEvent event)
            {
                if (isRunningInReconcilerThread())
                    return;

                if (isAffectedBy(event))
                    WorkingCopyReconciler.this.elementChanged(event);
            }
        };
    private ShellListener activationListener;

    /**
     * Creates a new working copy reconciler with the given working copy manager.
     * The working copy manager is used to determine the working copy for
     * the reconciler's document. The reconciler is configured with a single
     * reconciling strategy (by default, a {@link WorkingCopyReconcilingStrategy})
     * that is used irrespective of where a dirty region is located in the
     * reconciler's document.
     *
     * @param workingCopyManager not <code>null</code>
     */
    public WorkingCopyReconciler(IWorkingCopyManager workingCopyManager)
    {
        this(workingCopyManager::getWorkingCopy);
    }

    /**
     * Creates a new working copy reconciler with a function that is used to
     * determine the source file for the reconciler's document. The reconciler
     * is configured with a single reconciling strategy (by default, a {@link
     * WorkingCopyReconcilingStrategy}) that is used irrespective of where a
     * dirty region is located in the reconciler's document.
     *
     * @param documentToSourceFile not <code>null</code>
     * @since 1.5
     */
    public WorkingCopyReconciler(
        Function<IDocument, ISourceFile> documentToSourceFile)
    {
        if (documentToSourceFile == null)
            throw new IllegalArgumentException();
        this.documentToSourceFile = documentToSourceFile;
        // Just some reasonable defaults that can be overwritten:
        setIsIncrementalReconciler(false);
        setIsAllowedToModifyDocument(false);
        setReconcilingStrategy(new WorkingCopyReconcilingStrategy(
            documentToSourceFile));
    }

    /**
     * Sets the reconciling strategy that is to be used by this reconciler.
     *
     * @param strategy not <code>null</code>
     */
    public void setReconcilingStrategy(IReconcilingStrategy strategy)
    {
        if (strategy == null)
            throw new IllegalArgumentException();
        this.strategy = strategy;
        if (strategy instanceof IReconcilingStrategyExtension)
        {
            IReconcilingStrategyExtension extension =
                (IReconcilingStrategyExtension)strategy;
            extension.setProgressMonitor(getProgressMonitor());
        }
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        super.setProgressMonitor(monitor);
        if (strategy instanceof IReconcilingStrategyExtension)
        {
            IReconcilingStrategyExtension extension =
                (IReconcilingStrategyExtension)strategy;
            extension.setProgressMonitor(monitor);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <code>WorkingCopyReconciler</code> extends this method to {@link
     * #addElementChangeListener(IElementChangeListener) register} an
     * element change listener that {@link #elementChanged(IElementChangeEvent)
     * notifies} when a change in the underlying model {@link #isAffectedBy(
     * IElementChangeEvent) affects} the reconciler in some way, and also
     * to register a shell listener that {@link #setActive(boolean) sets}
     * the active state of the reconciler when the reconciler's text viewer
     * is activated or deactivated.
     * </p>
     */
    @Override
    public void install(ITextViewer textViewer)
    {
        super.install(textViewer);

        setSourceFile(documentToSourceFile.apply(textViewer.getDocument()));

        addElementChangeListener(elementChangeListener);

        Control control = textViewer.getTextWidget();
        activationListener = new ActivationListener(control);
        control.getShell().addShellListener(activationListener);
    }

    @Override
    public void uninstall()
    {
        Control control = getTextViewer().getTextWidget();
        if (!control.isDisposed())
            control.getShell().removeShellListener(activationListener);
        activationListener = null;

        removeElementChangeListener(elementChangeListener);

        setSourceFile(null);

        super.uninstall();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation always returns the single strategy of this reconciler.
     * </p>
     */
    @Override
    public IReconcilingStrategy getReconcilingStrategy(String contentType)
    {
        return strategy;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the reconciling strategy of this reconciler supports
     * {@link IReconcilingStrategyExtension}, this implementation
     * invokes {@link IReconcilingStrategyExtension#initialReconcile()
     * initialReconcile()} on the strategy under the reconciler's
     * {@link #getReconcilerLock() lock}. 
     * </p>
     */
    @Override
    protected void initialProcess()
    {
        synchronized (getReconcilerLock())
        {
            if (strategy instanceof IReconcilingStrategyExtension)
            {
                IReconcilingStrategyExtension extension =
                    (IReconcilingStrategyExtension)strategy;
                extension.initialReconcile();
            }
        }
        initialProcessDone = true;
    }

    @Override
    protected void process(DirtyRegion dirtyRegion)
    {
        if (dirtyRegion != null)
            strategy.reconcile(dirtyRegion, dirtyRegion);
        else
        {
            IDocument document = getDocument();
            if (document != null)
                strategy.reconcile(new Region(0, document.getLength()));
        }
    }

    @Override
    protected void forceReconciling()
    {
        if (!initialProcessDone)
            return;

        super.forceReconciling();
    }

    @Override
    protected void reconcilerDocumentChanged(IDocument newDocument)
    {
        setSourceFile(documentToSourceFile.apply(newDocument));
        strategy.setDocument(newDocument);
    }

    /**
     * Returns the mutex for this reconciler. See <a
     * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=66176">Bug 66176</a>
     * for a description of the underlying problem.
     * <p>
     * Default implementation returns <code>this</code>. Subclasses may override.
     * </p>
     *
     * @return the mutex for the reconciler (never <code>null</code>)
     */
    protected Object getReconcilerLock()
    {
        return this;
    }

    /**
     * Registers the given element change listener with the underlying model.
     *
     * @param listener never <code>null</code>
     */
    protected abstract void addElementChangeListener(
        IElementChangeListener listener);

    /**
     * Removes the given element change listener from the underlying model.
     *
     * @param listener never <code>null</code>
     */
    protected abstract void removeElementChangeListener(
        IElementChangeListener listener);

    /**
     * Returns whether this reconciler is affected in some way
     * by the given element change event.
     * <p>
     * This implementation delegates to {@link #isAffectedBy(IElementDelta,
     * ISourceFile)}, passing the source file for the reconciler's document.
     * </p>
     *
     * @param event never <code>null</code>
     * @return <code>true</code> if the reconciler is affected
     *  by the given element change event, and <code>false</code> otherwise
     */
    protected boolean isAffectedBy(IElementChangeEvent event)
    {
        return isAffectedBy(event.getDeltas(), getSourceFile());
    }

    private boolean isAffectedBy(IElementDelta[] deltas, ISourceFile sourceFile)
    {
        for (IElementDelta delta : deltas)
        {
            if (isAffectedBy(delta, sourceFile))
                return true;
        }
        return false;
    }

    /**
     * Returns whether this reconciler is affected by the given element delta
     * with regard to the given source file.
     *
     * @param delta never <code>null</code>
     * @param sourceFile may be <code>null</code>
     * @return <code>true</code> if the reconciler is affected
     *  by the given delta, and <code>false</code> otherwise
     */
    protected boolean isAffectedBy(IElementDelta delta, ISourceFile sourceFile)
    {
        long flags = ElementDeltas.getFlags(delta);
        if (flags == F_SYNC || flags == F_WORKING_COPY)
            return false;
        IElement element = ElementDeltas.getElement(delta);
        if (flags == F_UNDERLYING_RESOURCE && element.equals(sourceFile))
            return false; // saving the working copy
        if (flags == F_MARKERS)
        {
            if (element.equals(sourceFile))
            {
                for (IMarkerDelta markerDelta : ElementDeltas.getMarkerDeltas(
                    delta))
                {
                    if (markerDelta.isSubtypeOf(IMarker.PROBLEM))
                        return true;
                }
            }
            return false;
        }
        if (flags != F_CHILDREN)
            return true;
        for (IElementDelta child : ElementDeltas.getAffectedChildren(delta))
        {
            if (isAffectedBy(child, sourceFile))
                return true;
        }
        return false;
    }

    /**
     * Notifies that this reconciler is affected in some way
     * by the given element change event.
     * <p>
     * <b>Note:</b> This method may be called in any thread.
     * The event object (and the deltas within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     * <p>
     * This implementation schedules a runnable to execute on the UI thread,
     * to synchronize with {@link #setActive(boolean)}. The runnable will force
     * reconciling if the reconciler is active at that time; it will also record
     * the fact that a significant change occurred in the underlying model. 
     * </p>
     *
     * @param event never <code>null</code>
     */
    protected void elementChanged(IElementChangeEvent event)
    {
        // run on the UI thread to synchronize with #setActive
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                setModelChanged(true);
                if (isActive())
                    forceReconciling();
            }
        });
    }

    /**
     * Returns whether this reconciler is currently active.
     *
     * @return <code>true</code> if this reconciler is currently active,
     *  and <code>false</code> otherwise
     */
    protected boolean isActive()
    {
        return active;
    }

    /**
     * Indicates a change in the active state of this reconciler.
     * This method can only be executed by the UI thread.
     * <p>
     * This implementation sets the active state of the reconciler to the
     * given value. Also, it forces reconciling if a significant model change
     * occurred while the reconciler was not active.
     * </p>
     *
     * @param active the boolean value to set for the reconciler active state
     */
    protected void setActive(boolean active)
    {
        this.active = active;
        if (Display.getCurrent() == null)
            throw new AssertionError(
                "This method may only be executed by the user-interface thread"); //$NON-NLS-1$
        if (!active)
            setModelChanged(false);
        else if (hasModelChanged())
            forceReconciling();
    }

    private ISourceFile getSourceFile()
    {
        return sourceFile;
    }

    private void setSourceFile(ISourceFile sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    private boolean hasModelChanged()
    {
        return modelChanged;
    }

    private void setModelChanged(boolean modelChanged)
    {
        this.modelChanged = modelChanged;
    }

    private class ActivationListener
        extends ShellAdapter
    {
        private final Control control;

        ActivationListener(Control control)
        {
            if (control == null)
                throw new IllegalArgumentException();
            this.control = control;
        }

        @Override
        public void shellActivated(ShellEvent e)
        {
            if (!control.isDisposed() && control.isVisible())
            {
                setActive(true);
            }
        };

        @Override
        public void shellDeactivated(ShellEvent e)
        {
            if (!control.isDisposed() && control.getShell() == e.getSource())
            {
                setActive(false);
            }
        };
    }
}
