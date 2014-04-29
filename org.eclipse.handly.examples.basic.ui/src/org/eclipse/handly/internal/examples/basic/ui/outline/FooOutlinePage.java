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
package org.eclipse.handly.internal.examples.basic.ui.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.internal.examples.basic.ui.FooContentProvider;
import org.eclipse.handly.internal.examples.basic.ui.FooLabelProvider;
import org.eclipse.handly.internal.examples.basic.ui.SourceElementUtil;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.xtext.ui.editor.IXtextEditorAware;
import org.eclipse.xtext.ui.editor.XtextEditor;

import com.google.inject.Inject;

/**
 * Foo Outline page.
 * <p>
 * Note that much of the code is free from specifics of the Foo Model, 
 * thanks to the uniform API provided by Handly.
 * </p>
 */
public final class FooOutlinePage
    extends ContentOutlinePage
    implements IXtextEditorAware, IElementChangeListener
{
    private XtextEditor editor;
    private LinkingHelper linkingHelper;

    @Inject
    private FooContentProvider contentProvider;
    @Inject
    private FooLabelProvider labelProvider;

    @Override
    public void setEditor(XtextEditor editor)
    {
        this.editor = editor;
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        getTreeViewer().setContentProvider(contentProvider);
        getTreeViewer().setLabelProvider(labelProvider);
        getTreeViewer().setInput(computeInput());
        linkingHelper = new LinkingHelper();
        FooModelCore.getFooModel().addElementChangeListener(this);
    }

    @Override
    public void dispose()
    {
        FooModelCore.getFooModel().removeElementChangeListener(this);
        linkingHelper.dispose();
        editor.outlinePageClosed();
        super.dispose();
    }

    @Override
    public void elementChanged(IElementChangeEvent event)
    {
        if (affects(event.getDelta(), (IHandle)getTreeViewer().getInput()))
        {
            final Control control = getTreeViewer().getControl();
            control.getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (!control.isDisposed())
                    {
                        refresh();
                    }
                }
            });
        }
    }

    private boolean affects(IHandleDelta delta, IHandle element)
    {
        if (delta.getElement().equals(element))
            return true;
        IHandleDelta[] children = delta.getAffectedChildren();
        for (IHandleDelta child : children)
        {
            if (affects(child, element))
                return true;
        }
        return false;
    }

    private Object computeInput()
    {
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput instanceof IFileEditorInput)
        {
            IFile file = ((IFileEditorInput)editorInput).getFile();
            return FooModelCore.create(file);
        }
        return null;
    }

    private void refresh()
    {
        Control control = getControl();
        control.setRedraw(false);
        BusyIndicator.showWhile(control.getDisplay(), new Runnable()
        {
            public void run()
            {
                TreePath[] treePaths = getTreeViewer().getExpandedTreePaths();
                getTreeViewer().refresh();
                getTreeViewer().setExpandedTreePaths(treePaths);
            }
        });
        control.setRedraw(true);
    }

    private class LinkingHelper
        extends OpenAndLinkWithEditorHelper
    {
        private ISelectionChangedListener editorListener =
            new ISelectionChangedListener()
            {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    if (!getTreeViewer().getControl().isFocusControl())
                    {
                        linkToOutline(event.getSelection());
                    }
                }
            };

        public LinkingHelper()
        {
            super(getTreeViewer());
            setLinkWithEditor(true);
            ISelectionProvider selectionProvider =
                editor.getSite().getSelectionProvider();
            if (selectionProvider instanceof IPostSelectionProvider)
                ((IPostSelectionProvider)selectionProvider).addPostSelectionChangedListener(editorListener);
            else
                selectionProvider.addSelectionChangedListener(editorListener);
        }

        @Override
        public void dispose()
        {
            ISelectionProvider selectionProvider =
                editor.getSite().getSelectionProvider();
            if (selectionProvider instanceof IPostSelectionProvider)
                ((IPostSelectionProvider)selectionProvider).removePostSelectionChangedListener(editorListener);
            else
                selectionProvider.removeSelectionChangedListener(editorListener);
            super.dispose();
        }

        @Override
        public void setLinkWithEditor(boolean enabled)
        {
            super.setLinkWithEditor(enabled);
            if (enabled)
                linkToOutline(editor.getSite().getSelectionProvider().getSelection());
        }

        @Override
        protected void activate(ISelection selection)
        {
            linkToEditor(selection);
        }

        @Override
        protected void open(ISelection selection, boolean activate)
        {
            linkToEditor(selection);
        }

        @Override
        protected void linkToEditor(ISelection selection)
        {
            if (selection == null || selection.isEmpty())
                return;
            Object element =
                ((IStructuredSelection)selection).getFirstElement();
            if (!(element instanceof ISourceElement))
                return;
            SourceElementUtil.revealInTextEditor(editor,
                (ISourceElement)element);
        }

        @SuppressWarnings("unchecked")
        protected void linkToOutline(ISelection selection)
        {
            if (selection == null || selection.isEmpty())
                return;
            IStructuredSelection linkedSelection = null;
            if (selection instanceof ITextSelection)
                linkedSelection = getLinkedSelection((ITextSelection)selection);
            if (linkedSelection != null)
            {
                IStructuredSelection currentSelection =
                    (IStructuredSelection)getTreeViewer().getSelection();
                if (currentSelection == null
                    || !currentSelection.toList().containsAll(
                        linkedSelection.toList()))
                {
                    getTreeViewer().setSelection(linkedSelection, true);
                }
            }
        }

        private IStructuredSelection getLinkedSelection(ITextSelection selection)
        {
            Object input = getTreeViewer().getInput();
            if (!(input instanceof ISourceElement))
                return null;
            ISourceFile sourceFile = ((ISourceElement)input).getSourceFile();
            ISourceElement element =
                SourceElementUtil.getSourceElement(sourceFile,
                    selection.getOffset());
            if (element == null)
                return null;
            return new StructuredSelection(element);
        }
    }
}
