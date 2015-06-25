/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.text.reconciler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.ui.IWorkingCopyProvider;
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
public abstract class BaseReconciler
    extends AbstractReconciler
    implements IWorkingCopyProvider
{
    private IWorkingCopyProvider provider;
    private IReconcilingStrategy strategy;
    private volatile ISourceFile workingCopy;
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
                    BaseReconciler.this.elementChanged(event);
            }
        };
    private ShellListener activationListener;

    /**
     * Creates a new reconciler that reconciles the working copy provided
     * by the given provider.
     *
     * @param provider the working copy provider (not <code>null</code>)
     */
    public BaseReconciler(IWorkingCopyProvider provider)
    {
        if (provider == null)
            throw new IllegalArgumentException();
        this.provider = provider;
        // Just some reasonable defaults that can be overwritten:
        setIsIncrementalReconciler(false);
        setIsAllowedToModifyDocument(false);
        setReconcilingStrategy(new WorkingCopyReconcilingStrategy(this));
    }

    /**
     * Sets the reconciling strategy that is to be used by this reconciler.
     *
     * @param strategy the reconciling strategy (not <code>null</code>)
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

    @Override
    public void install(ITextViewer textViewer)
    {
        super.install(textViewer);

        setWorkingCopy(provider.getWorkingCopy());

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

        super.uninstall();
    }

    @Override
    public IReconcilingStrategy getReconcilingStrategy(String contentType)
    {
        return strategy;
    }

    @Override
    public ISourceFile getWorkingCopy()
    {
        return workingCopy;
    }

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
        setWorkingCopy(provider.getWorkingCopy());
        strategy.setDocument(newDocument);
    }

    /**
     * Returns the mutex for the reconciler.
     * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66176
     * for a description of the underlying problem.
     *
     * @return the mutex for the reconciler (never <code>null</code>)
     */
    protected Object getReconcilerLock()
    {
        return this; // Null Object
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
     * Returns whether the reconciler is affected in some way
     * by the given element change event.
     *
     * @param event never <code>null</code>
     * @return <code>true</code> if the reconciler is affected
     *  by the given element change event, <code>false</code> otherwise
     */
    protected boolean isAffectedBy(IElementChangeEvent event)
    {
        return isAffectedBy(event.getDelta(), getWorkingCopy());
    }

    /**
     * Returns whether the reconciler is affected by the given delta
     * with regard to the given working copy.
     *
     * @param delta never <code>null</code>
     * @param workingCopy may be <code>null</code>
     * @return <code>true</code> if the reconciler is affected
     *  by the given delta, <code>false</code> otherwise
     */
    protected boolean isAffectedBy(IHandleDelta delta, ISourceFile workingCopy)
    {
        int flags = delta.getFlags();
        if (flags == IHandleDelta.F_SYNC
            || flags == IHandleDelta.F_WORKING_COPY)
            return false;
        IHandle element = delta.getElement();
        if (flags == IHandleDelta.F_UNDERLYING_RESOURCE && element.equals(
            workingCopy))
            return false; // saving this reconciler's working copy
        if (flags == IHandleDelta.F_MARKERS)
        {
            if (element.equals(workingCopy))
            {
                for (IMarkerDelta markerDelta : delta.getMarkerDeltas())
                {
                    if (markerDelta.isSubtypeOf(IMarker.PROBLEM))
                        return true;
                }
            }
            return false;
        }
        if (flags != IHandleDelta.F_CHILDREN)
            return true;
        for (IHandleDelta child : delta.getAffectedChildren())
        {
            if (isAffectedBy(child, workingCopy))
                return true;
        }
        return false;
    }

    /**
     * Notifies that the reconciler is affected in some way
     * by the given element change event.
     * <p>
     * <b>Note</b> This method may be called in any thread.
     * The event object (and the delta within it) is valid only
     * for the duration of the invocation of this method.
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
     *  <code>false</code> otherwise
     */
    protected boolean isActive()
    {
        return active;
    }

    /**
     * Indicates a change in the active state of this reconciler.
     * This method can only be executed by the UI thread.
     *
     * @param active the boolean value to set for the reconciler active state
     */
    protected void setActive(boolean active)
    {
        this.active = active;
        if (Display.getCurrent() == null)
            throw new AssertionError(
                "This method can only be executed by the UI thread"); //$NON-NLS-1$
        if (!active)
            setModelChanged(false);
        else if (hasModelChanged())
            forceReconciling();
    }

    private boolean hasModelChanged()
    {
        return modelChanged;
    }

    private void setModelChanged(boolean modelChanged)
    {
        this.modelChanged = modelChanged;
    }

    private void setWorkingCopy(ISourceFile workingCopy)
    {
        this.workingCopy = workingCopy;
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
