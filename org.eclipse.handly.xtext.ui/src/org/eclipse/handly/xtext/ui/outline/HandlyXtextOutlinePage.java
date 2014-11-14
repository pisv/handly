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
package org.eclipse.handly.xtext.ui.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.ISourceFileFactory;
import org.eclipse.handly.ui.outline.CollapseAllActionContribution;
import org.eclipse.handly.ui.outline.ElementChangeListenerContribution;
import org.eclipse.handly.ui.outline.LexicalSortActionContribution;
import org.eclipse.handly.ui.outline.LexicalSortContribution;
import org.eclipse.handly.ui.outline.LinkWithEditorActionContribution;
import org.eclipse.handly.ui.outline.LinkWithEditorContribution;
import org.eclipse.handly.ui.outline.SourceElementLinkingHelper;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PlatformUI;

import com.google.inject.Inject;

/**
 * An abstract base class of a Handly outline page for Xtext editor.
 * <p>
 * Pre-wired with contributions that are common to a typical outline of
 * <code>ISourceElement</code>. In particular, it provides out-of-the-box
 * support for linking with editor and lexical sorting functionality.
 * </p>
 * <p>
 * Note that this class relies on a language-specific implementation of
 * <code>ISourceFileFactory</code> being available through injection.
 * </p>
 */
public abstract class HandlyXtextOutlinePage
    extends XtextCommonOutlinePage
{
    @Inject
    private ISourceFileFactory sourceFileFactory;
    @Inject
    private LinkWithEditorPreference linkWithEditorPreference;
    @Inject
    private LexicalSortPreference lexicalSortPreference;

    @Override
    public IBooleanPreference getLinkWithEditorPreference()
    {
        return linkWithEditorPreference;
    }

    @Override
    public IBooleanPreference getLexicalSortPreference()
    {
        return lexicalSortPreference;
    }

    /**
     * Adds the necessary contributions to this outline page.
     * <p>
     * Default implementation adds contributions that are common to 
     * a typical outline of <code>ISourceElement</code>. Subclasses may extend
     * this method, but must preserve the <code>@Inject</code> annotation.
     * </p>
     */
    @Inject
    protected void addOutlineContributions()
    {
        addOutlineContribution(new ElementChangeListenerContribution()
        {
            @Override
            protected void addElementChangeListener(
                IElementChangeListener listener)
            {
                HandlyXtextOutlinePage.this.addElementChangeListener(listener);
            }

            @Override
            protected void removeElementChangeListener(
                IElementChangeListener listener)
            {
                HandlyXtextOutlinePage.this.removeElementChangeListener(listener);
            }

            @Override
            protected void elementChanged(IElementChangeEvent event)
            {
                HandlyXtextOutlinePage.this.elementChanged(event);
            }
        });
        addCollapseAllSupport();
        addLinkWithEditorSupport();
        addSortingSupport();
    }

    /**
     * Adds collapse-all support. Subclasses may override this method.
     */
    protected void addCollapseAllSupport()
    {
        addOutlineContribution(new CollapseAllActionContribution());
    }

    /**
     * Adds link-with-editor support. Subclasses may override this method.
     */
    protected void addLinkWithEditorSupport()
    {
        addOutlineContribution(new LinkWithEditorActionContribution());
        addOutlineContribution(new LinkWithEditorContribution()
        {
            @Override
            protected OpenAndLinkWithEditorHelper getLinkingHelper()
            {
                return new SourceElementLinkingHelper(getOutlinePage());
            }
        });
    }

    /**
     * Adds sorting support. Subclasses may override this method.
     */
    protected void addSortingSupport()
    {
        addOutlineContribution(new LexicalSortActionContribution());
        addOutlineContribution(new LexicalSortContribution());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns <code>ISourceFile</code> computed from the
     * editor input via the language-specific <code>ISourceFileFactory</code>.
     * Subclasses may override.
     * </p>
     */
    @Override
    protected Object computeInput()
    {
        IEditorInput editorInput = getEditor().getEditorInput();
        if (editorInput instanceof IFileEditorInput)
        {
            IFile file = ((IFileEditorInput)editorInput).getFile();
            return sourceFileFactory.getSourceFile(file);
        }
        return null;
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
     * Notifies that the outline page is affected in some way 
     * by the given element change event. 
     * <p>
     * <b>Note</b> This method may be called in any thread. 
     * The event object (and the delta within it) is valid only 
     * for the duration of the invocation of this method.
     * </p>
     * <p>
     * Default implementation schedules {@link #refresh() refresh} 
     * of this page in the UI thread.
     * </p>
     *
     * @param event never <code>null</code>
     */
    protected void elementChanged(IElementChangeEvent event)
    {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                if (!getTreeViewer().getControl().isDisposed())
                {
                    refresh();
                }
            }
        });
    }
}
