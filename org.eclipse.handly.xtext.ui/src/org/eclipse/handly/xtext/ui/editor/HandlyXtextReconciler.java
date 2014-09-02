/*******************************************************************************
 * Copyright (c) 2008, 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - adaptation of XtextReconciler code
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.ISourceViewerExtension4;
import org.eclipse.xtext.ui.editor.model.IXtextDocumentContentObserver;
import org.eclipse.xtext.ui.editor.reconciler.XtextReconciler;

/**
 * Adapted from <code>org.eclipse.xtext.ui.editor.reconciler.XtextReconciler</code>. 
 * Customized for our reconciling story. Should be used together with 
 * {@link HandlyXtextDocument}.
 * 
 * @noextend This class is not indended to be extended by clients.
 */
public class HandlyXtextReconciler
    extends Job
    implements IReconciler
{
    /**
     * Constant identifying the job family identifier for the reconciler job.
     */
    public static final Object FAMILY_RECONCILER =
        XtextReconciler.class.getName();

    private boolean isInstalled;
    private boolean shouldInstallCompletionListener;
    private volatile boolean paused;
    private ITextViewer viewer;
    private final TextInputListener textInputListener = new TextInputListener();
    private final DocumentListener documentListener = new DocumentListener();
    private int delay = 500;

    public HandlyXtextReconciler()
    {
        super("Xtext Editor Reconciler"); //$NON-NLS-1$
        setPriority(Job.SHORT);
        setSystem(true);
    }

    public void setDelay(int delay)
    {
        this.delay = delay;
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
                ((HandlyXtextDocument)document).addXtextDocumentContentObserver(documentListener);
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
                ((HandlyXtextDocument)document).removeXtextDocumentContentObserver(documentListener);
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
        return FAMILY_RECONCILER.equals(family);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor)
    {
        if (monitor.isCanceled() || paused)
            return Status.CANCEL_STATUS;

        IDocument document = viewer.getDocument();
        if (document instanceof HandlyXtextDocument)
        {
            ((HandlyXtextDocument)document).reconcile(false);
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
        public void performNecessaryUpdates(Processor processor)
        {
            // Note: this method is always called with the doc's readLock held

            IDocument document = viewer.getDocument();
            if (document instanceof HandlyXtextDocument && !paused)
            {
                HandlyXtextDocument doc = (HandlyXtextDocument)document;
                if (doc.needsReconciling()) // this check is required to avoid constant rescheduling of ValidationJob
                    doc.reconcile(false, processor);
            }
            if (sessionStarted && !paused)
            {
                pause();
            }
        }

        @Override
        public void assistSessionStarted(ContentAssistEvent event)
        {
            sessionStarted = true;
        }

        @Override
        public void assistSessionEnded(ContentAssistEvent event)
        {
            sessionStarted = false;
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
                ((HandlyXtextDocument)oldInput).removeXtextDocumentContentObserver(documentListener);
                cancel();
            }
        }

        @Override
        public void inputDocumentChanged(IDocument oldInput, IDocument newInput)
        {
            if (newInput instanceof HandlyXtextDocument)
            {
                ((HandlyXtextDocument)newInput).addXtextDocumentContentObserver(documentListener);
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
}
