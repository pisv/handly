/*******************************************************************************
 * Copyright (c) 2008, 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - adaptation of XtextReconciler code
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.editor;

import static org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration.XTEXT_TEMPLATE_POS_CATEGORY;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.handly.internal.xtext.ui.Activator;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.ISourceViewerExtension4;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocumentContentObserver;
import org.eclipse.xtext.ui.editor.reconciler.XtextReconciler;

/**
 * Adapted from <code>org.eclipse.xtext.ui.editor.reconciler.XtextReconciler</code>.
 * Customized for Handly reconciling story.
 * <p>
 * Bind this class in place of the default <code>XtextReconciler</code>
 * if you have {@link HandlyXtextDocument} bound:
 * </p>
 * <pre>
 * public Class&lt;? extends IReconciler&gt; bindIReconciler() {
 *     return HandlyXtextReconciler.class;
 * }</pre>
 *
 * @noextend This class is not intended to be extended by clients.
 */
// NOTE: This class extends XtextReconciler to retain assignment compatibility.
// The actual implementation is delegated to the inner class InternalReconciler
public class HandlyXtextReconciler
    extends XtextReconciler
{
    private final InternalReconciler delegate = new InternalReconciler();

    public HandlyXtextReconciler()
    {
        super(null);
    }

    @Override
    public void install(ITextViewer textViewer)
    {
        delegate.install(textViewer);
    }

    @Override
    public void uninstall()
    {
        delegate.uninstall();
    }

    @Override
    public IReconcilingStrategy getReconcilingStrategy(String contentType)
    {
        return delegate.getReconcilingStrategy(contentType);
    }

    @Override
    public void setReconcilingStrategy(IReconcilingStrategy strategy)
    {
    }

    @Override
    public void setEditor(XtextEditor editor)
    {
    }

    @Override
    public void setDelay(int delay)
    {
        // delegate is null when this method is called from the super constructor
        if (delegate == null)
            return;
        delegate.setDelay(delay);
    }

    @Override
    public void forceReconcile()
    {
        delegate.forceReconcile();
    }

    @Override
    public boolean shouldSchedule()
    {
        // #schedule() should never be called for this job
        throw new AssertionError(); // fail on a schedule request
    }

    private static class InternalReconciler
        extends Job
        implements IReconciler
    {
        private boolean isInstalled;
        private boolean shouldInstallCompletionListener;
        private volatile boolean paused;
        private final AtomicBoolean forced = new AtomicBoolean();
        private ITextViewer viewer;
        private final TextInputListener textInputListener =
            new TextInputListener();
        private final DocumentListener documentListener =
            new DocumentListener();
        private int delay = 500;

        public InternalReconciler()
        {
            super("Xtext Editor Reconciler"); //$NON-NLS-1$
            setPriority(Job.SHORT);
            setSystem(true);
        }

        public void setDelay(int delay)
        {
            this.delay = delay;
        }

        public void forceReconcile()
        {
            if (viewer == null || viewer.getDocument() == null)
                return;
            cancel();
            forced.set(true);
            schedule(delay);
        }

        @Override
        public void install(ITextViewer textViewer)
        {
            if (!isInstalled)
            {
                viewer = textViewer;
                IDocument document0 = viewer.getDocument();
                viewer.addTextInputListener(textInputListener);
                IDocument document = viewer.getDocument();
                if (document instanceof HandlyXtextDocument
                    && document == document0) // a bit of paranoia: document != document0 means the document has changed under us and the text input listener has already handled it
                {
                    ((HandlyXtextDocument)document).addXtextDocumentContentObserver(
                        documentListener);
                }
                if (viewer instanceof ISourceViewerExtension4)
                {
                    ContentAssistantFacade facade =
                        ((ISourceViewerExtension4)viewer).getContentAssistantFacade();
                    if (facade == null)
                        shouldInstallCompletionListener = true;
                    else
                        facade.addCompletionListener(documentListener);
                }
                isInstalled = true;
            }
        }

        @Override
        public void uninstall()
        {
            if (isInstalled)
            {
                viewer.removeTextInputListener(textInputListener);
                IDocument document = viewer.getDocument();
                if (document instanceof HandlyXtextDocument)
                {
                    ((HandlyXtextDocument)document).removeXtextDocumentContentObserver(
                        documentListener);
                }
                if (viewer instanceof ISourceViewerExtension4)
                {
                    ContentAssistantFacade facade =
                        ((ISourceViewerExtension4)viewer).getContentAssistantFacade();
                    facade.removeCompletionListener(documentListener);
                }
                isInstalled = false;
            }
        }

        @Override
        public IReconcilingStrategy getReconcilingStrategy(String contentType)
        {
            return null; // it's safe to return no strategy
        }

        @Override
        public boolean belongsTo(Object family)
        {
            return XtextReconciler.class.getName().equals(family);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor)
        {
            if (monitor.isCanceled() || paused)
                return Status.CANCEL_STATUS;

            IDocument document = viewer.getDocument();
            if (document instanceof HandlyXtextDocument)
            {
                HandlyXtextDocument doc = (HandlyXtextDocument)document;
                final boolean forced = this.forced.compareAndSet(true, false);
                if (forced || doc.needsReconciling())
                {
                    try
                    {
                        doc.reconcile(forced, monitor);
                    }
                    catch (OperationCanceledException e)
                    {
                        if (forced)
                            this.forced.set(true);
                        throw e;
                    }
                }
            }
            return Status.OK_STATUS;
        }

        private void handleDocumentChanged(DocumentEvent event)
        {
            cancel();
            schedule(delay);
        }

        private void pause()
        {
            paused = true;
        }

        private void resume()
        {
            paused = false;
            schedule(delay);
        }

        private class DocumentListener
            implements IXtextDocumentContentObserver, ICompletionListener
        {
            private final IPositionUpdater templatePositionUpdater =
                new TemplatePositionUpdater(XTEXT_TEMPLATE_POS_CATEGORY);

            private volatile boolean sessionStarted = false;

            @Override
            public void documentAboutToBeChanged(DocumentEvent event)
            {
            }

            @Override
            public void documentChanged(DocumentEvent event)
            {
                handleDocumentChanged(event);
            }

            @Override
            public boolean performNecessaryUpdates(Processor processor)
            {
                // Note: this method is always called with the doc's readLock held

                boolean hadUpdates = false;
                IDocument document = viewer.getDocument();
                if (document instanceof HandlyXtextDocument && !paused)
                {
                    HandlyXtextDocument doc = (HandlyXtextDocument)document;
                    try
                    {
                        if (doc.needsReconciling()) // this check is required to avoid constant rescheduling of ValidationJob
                            hadUpdates = doc.reconcile(processor);
                    }
                    catch (Throwable e)
                    {
                        Activator.log(Activator.createErrorStatus(
                            "Error while forcing reconciliation", e)); //$NON-NLS-1$
                    }
                }
                if (sessionStarted && !paused)
                {
                    pause();
                }
                return hadUpdates;
            }

            @Override
            public boolean hasPendingUpdates()
            {
                IDocument document = viewer.getDocument();
                if (document instanceof HandlyXtextDocument)
                    return ((HandlyXtextDocument)document).needsReconciling();
                return false;
            }

            @Override
            public void assistSessionStarted(ContentAssistEvent event)
            {
                IDocument document = viewer.getDocument();
                document.addPositionCategory(XTEXT_TEMPLATE_POS_CATEGORY);
                document.addPositionUpdater(templatePositionUpdater);
                sessionStarted = true;
            }

            @Override
            public void assistSessionEnded(ContentAssistEvent event)
            {
                sessionStarted = false;
                IDocument document = viewer.getDocument();
                document.removePositionUpdater(templatePositionUpdater);
                try
                {
                    document.removePositionCategory(
                        XTEXT_TEMPLATE_POS_CATEGORY);
                }
                catch (BadPositionCategoryException e)
                {
                }
                resume();
            }

            @Override
            public void selectionChanged(ICompletionProposal proposal,
                boolean smartToggle)
            {
            }
        }

        private class TextInputListener
            implements ITextInputListener
        {
            public void inputDocumentAboutToBeChanged(IDocument oldInput,
                IDocument newInput)
            {
                if (oldInput instanceof HandlyXtextDocument)
                {
                    ((HandlyXtextDocument)oldInput).removeXtextDocumentContentObserver(
                        documentListener);
                    cancel();
                }
            }

            @Override
            public void inputDocumentChanged(IDocument oldInput,
                IDocument newInput)
            {
                if (newInput instanceof HandlyXtextDocument)
                {
                    ((HandlyXtextDocument)newInput).addXtextDocumentContentObserver(
                        documentListener);
                    schedule(delay);
                }

                if (shouldInstallCompletionListener)
                {
                    ContentAssistantFacade facade =
                        ((ISourceViewerExtension4)viewer).getContentAssistantFacade();
                    if (facade != null)
                        facade.addCompletionListener(documentListener);
                    shouldInstallCompletionListener = false;
                }
            }
        }

        // Initially copied from <code>org.eclipse.xtext.ui.editor.reconciler.TemplatePositionUpdater</code>
        // only because the original is a package-private class.
        private static class TemplatePositionUpdater
            extends DefaultPositionUpdater
        {
            public TemplatePositionUpdater(String category)
            {
                super(category);
            }

            @Override
            protected void adaptToInsert()
            {
                int myStart = fPosition.offset;
                int myEnd = fPosition.offset + fPosition.length - 1;
                myEnd = Math.max(myStart, myEnd);

                int yoursStart = fOffset;
                int yoursEnd = fOffset + fReplaceLength - 1;
                yoursEnd = Math.max(yoursStart, yoursEnd);

                if (myEnd < yoursStart)
                    return;

                fPosition.length += fReplaceLength;
            }
        }
    }
}
