/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * An abstract base implementation of {@link ICommonOutlinePage}.
 */
public abstract class CommonOutlinePage
    extends Page
    implements ICommonOutlinePage
{
    private TreeViewer treeViewer;
    private IEditorPart editor;
    private List<IOutlineContribution> contributionList =
        new ArrayList<IOutlineContribution>();
    private ListenerList<IOutlineInputChangeListener> inputChangeListeners =
        new ListenerList<>();
    private ListenerList<ISelectionChangedListener> selectionChangedListeners =
        new ListenerList<>();
    private IPropertyListener editorInputListener = new IPropertyListener()
    {
        public void propertyChanged(Object source, int propId)
        {
            if (propId == IEditorPart.PROP_INPUT)
            {
                editorInputChanged();
            }
        }
    };

    @Override
    public final TreeViewer getTreeViewer()
    {
        return treeViewer;
    }

    @Override
    public void init(IEditorPart editor)
    {
        if (editor == null)
            throw new IllegalArgumentException();
        this.editor = editor;
    }

    @Override
    public final IEditorPart getEditor()
    {
        return editor;
    }

    @Override
    public void addOutlineContribution(IOutlineContribution contribution)
    {
        if (contributionList.add(contribution))
        {
            if (getTreeViewer() != null && !getControl().isDisposed())
                contribution.init(this);
        }
    }

    @Override
    public void removeOutlineContribution(IOutlineContribution contribution)
    {
        if (contributionList.remove(contribution))
        {
            if (getTreeViewer() != null)
                contribution.dispose();
        }
    }

    @Override
    public void addInputChangeListener(IOutlineInputChangeListener listener)
    {
        inputChangeListeners.add(listener);
    }

    @Override
    public void removeInputChangeListener(IOutlineInputChangeListener listener)
    {
        inputChangeListeners.remove(listener);
    }

    @Override
    public IBooleanPreference getLinkWithEditorPreference()
    {
        return null;
    }

    @Override
    public IBooleanPreference getLexicalSortPreference()
    {
        return null;
    }

    @Override
    public void init(IPageSite pageSite)
    {
        super.init(pageSite);
        pageSite.setSelectionProvider(this);
    }

    @Override
    public final Control getControl()
    {
        if (treeViewer == null)
            return null;
        return treeViewer.getControl();
    }

    @Override
    public void createControl(Composite parent)
    {
        if (editor == null)
            throw new IllegalStateException(
                "init(IEditorPart) must be called before the outline page's control is created"); //$NON-NLS-1$

        treeViewer = createTreeViewer(parent);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                fireSelectionChanged(event.getSelection());
            }
        });
        treeViewer.setUseHashlookup(shouldUseHashlookup());
        treeViewer.setContentProvider(getContentProvider());
        IBaseLabelProvider labelProvider = getLabelProvider();
        setUpDecorationContextFor(labelProvider);
        treeViewer.setLabelProvider(labelProvider);
        treeViewer.setInput(computeInput());

        editor.addPropertyListener(editorInputListener);

        registerGlobalActions();

        initContributions();
    }

    @Override
    public void setFocus()
    {
        treeViewer.getControl().setFocus();
    }

    @Override
    public void dispose()
    {
        if (editor != null)
            editor.removePropertyListener(editorInputListener);
        disposeContributions();
        super.dispose();
    }

    @Override
    public ISelection getSelection()
    {
        if (treeViewer == null)
            return StructuredSelection.EMPTY;
        return treeViewer.getSelection();
    }

    @Override
    public void setSelection(ISelection selection)
    {
        if (treeViewer != null)
            treeViewer.setSelection(selection);
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener)
    {
        selectionChangedListeners.add(listener);
    }

    @Override
    public void removeSelectionChangedListener(
        ISelectionChangedListener listener)
    {
        selectionChangedListeners.remove(listener);
    }

    /**
     * Completely refreshes this outline page's tree viewer,
     * preserving expanded tree paths where possible.
     */
    public void refresh()
    {
        Control control = treeViewer.getControl();
        control.setRedraw(false);
        BusyIndicator.showWhile(control.getDisplay(), new Runnable()
        {
            public void run()
            {
                TreePath[] treePaths = treeViewer.getExpandedTreePaths();
                treeViewer.refresh();
                treeViewer.setExpandedTreePaths(treePaths);
            }
        });
        control.setRedraw(true);
    }

    /**
     * Returns whether the outline page's tree viewer should use hash lookup.
     * This method is called once, when the page's control is created.
     * <p>
     * Default implementation returns <code>true</code>.
     * Subclasses may override.
     * </p>
     *
     * @see TreeViewer#setUseHashlookup(boolean)
     */
    protected boolean shouldUseHashlookup()
    {
        return true;
    }

    /**
     * Returns the content provider that is to be used by the outline page's
     * tree viewer. This method is called once, when the page's control
     * is created.
     *
     * @return the content provider (not <code>null</code>)
     */
    protected abstract ITreeContentProvider getContentProvider();

    /**
     * Returns the label provider that is to be used by the outline page's
     * tree viewer. This method is called once, when the page's control
     * is created.
     *
     * @return the label provider (not <code>null</code>)
     */
    protected abstract IBaseLabelProvider getLabelProvider();

    /**
     * Computes the new input element for the outline page's tree viewer.
     *
     * @return the input element (may be <code>null</code>)
     */
    protected abstract Object computeInput();

    /**
     * Creates a tree viewer for this outline page. The viewer has no input,
     * no content provider, a default label provider, no sorter, and no filters.
     * This method is called once, when the page's control is created.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created tree viewer (not <code>null</code>)
     */
    protected OutlineTreeViewer createTreeViewer(Composite parent)
    {
        return new OutlineTreeViewer(new Tree(parent, SWT.MULTI | SWT.H_SCROLL
            | SWT.V_SCROLL));
    }

    /**
     * Notifies that the outline page's editor input has changed.
     * <p>
     * Default implementation sets the outline page's viewer input accordingly.
     * Subclasses may extend.
     * </p>
     */
    protected void editorInputChanged()
    {
        treeViewer.setInput(computeInput());
    }

    /**
     * Returns whether the given tree item is auto-expandable. This method
     * should be consulted by the tree viewer's <code>expandXXX</code> methods.
     * If it returns <code>false</code>, the item is to be collapsed,
     * rather than expanded.
     * <p>
     * Default implementation returns <code>true</code> for the root item
     * and <code>false</code> for all other items. Subclasses may override.
     * </p>
     *
     * @param item the tree item (never <code>null</code>)
     * @return <code>true</code> if the given item is auto-expandable;
     *  <code>false</code> otherwise
     */
    protected boolean isAutoExpandable(TreeItem item)
    {
        return item.getParentItem() == null;
    }

    /**
     * Hook to register global action handlers.
     * Subclasses may extend or override.
     */
    protected void registerGlobalActions()
    {
        IActionBars actionBars = getSite().getActionBars();
        if (editor instanceof ITextEditor)
        {
            ITextEditor textEditor = (ITextEditor)editor;
            actionBars.setGlobalActionHandler(ITextEditorActionConstants.UNDO,
                textEditor.getAction(ITextEditorActionConstants.UNDO));
            actionBars.setGlobalActionHandler(ITextEditorActionConstants.REDO,
                textEditor.getAction(ITextEditorActionConstants.REDO));

            IAction action = textEditor.getAction(
                ITextEditorActionConstants.NEXT);
            actionBars.setGlobalActionHandler(
                ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, action);
            actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT,
                action);
            action = textEditor.getAction(ITextEditorActionConstants.PREVIOUS);
            actionBars.setGlobalActionHandler(
                ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION,
                action);
            actionBars.setGlobalActionHandler(
                ITextEditorActionConstants.PREVIOUS, action);
        }
    }

    /**
     * Hook to initialize decoration context.
     * Subclasses may extend.
     *
     * @param context the decoration context (never <code>null</code>)
     */
    protected void initDecorationContext(DecorationContext context)
    {
        if (this instanceof IContentAdapterProvider)
            context.putProperty(IContentAdapterProvider.class.getName(), this);
    }

    private void initContributions()
    {
        List<IOutlineContribution> contributions =
            new ArrayList<IOutlineContribution>(contributionList);
        for (final IOutlineContribution contribution : contributions)
        {
            SafeRunner.run(new ISafeRunnable()
            {
                public void run() throws Exception
                {
                    contribution.init(CommonOutlinePage.this);
                }

                public void handleException(Throwable exception)
                {
                    // already logged by Platform
                }
            });
        }
    }

    private void disposeContributions()
    {
        List<IOutlineContribution> contributions =
            new ArrayList<IOutlineContribution>(contributionList);
        for (final IOutlineContribution contribution : contributions)
        {
            SafeRunner.run(new ISafeRunnable()
            {
                public void run() throws Exception
                {
                    contribution.dispose();
                }

                public void handleException(Throwable exception)
                {
                    // already logged by Platform
                }
            });
        }
    }

    private void fireInputChanged(final Object input, final Object oldInput)
    {
        Object[] listeners = inputChangeListeners.getListeners();
        for (final Object listener : listeners)
        {
            SafeRunner.run(new ISafeRunnable()
            {
                public void run()
                {
                    ((IOutlineInputChangeListener)listener).inputChanged(
                        CommonOutlinePage.this, input, oldInput);
                }

                public void handleException(Throwable exception)
                {
                    // already logged by Platform
                }
            });
        }
    }

    private void fireSelectionChanged(ISelection selection)
    {
        final SelectionChangedEvent event = new SelectionChangedEvent(this,
            selection);

        Object[] listeners = selectionChangedListeners.getListeners();
        for (final Object listener : listeners)
        {
            SafeRunner.run(new ISafeRunnable()
            {
                public void run()
                {
                    ((ISelectionChangedListener)listener).selectionChanged(
                        event);
                }

                public void handleException(Throwable exception)
                {
                    // already logged by Platform
                }
            });
        }
    }

    private void setUpDecorationContextFor(IBaseLabelProvider labelProvider)
    {
        if (labelProvider instanceof DecoratingLabelProvider)
        {
            DecoratingLabelProvider dlp =
                (DecoratingLabelProvider)labelProvider;
            dlp.setDecorationContext(createDecorationContext(
                dlp.getDecorationContext()));
        }
        else if (labelProvider instanceof DecoratingStyledCellLabelProvider)
        {
            DecoratingStyledCellLabelProvider dsclp =
                (DecoratingStyledCellLabelProvider)labelProvider;
            dsclp.setDecorationContext(createDecorationContext(
                dsclp.getDecorationContext()));
        }
    }

    private IDecorationContext createDecorationContext(
        IDecorationContext existingContext)
    {
        DecorationContext newContext = new DecorationContext();
        initDecorationContext(newContext);
        if (existingContext != null)
        {
            for (String property : existingContext.getProperties())
            {
                newContext.putProperty(property, existingContext.getProperty(
                    property));
            }
        }
        return newContext;
    }

    /**
     * The tree viewer used for displaying the outline.
     */
    protected class OutlineTreeViewer
        extends TreeViewer
    {
        /**
         * Creates a new tree viewer on the given tree control.
         * Sets auto-expand level to <code>ALL_LEVELS</code>.
         *
         * @param tree the tree control (not <code>null</code>)
         */
        public OutlineTreeViewer(Tree tree)
        {
            super(tree);
            setAutoExpandLevel(ALL_LEVELS);
        }

        @Override
        protected void inputChanged(Object input, Object oldInput)
        {
            super.inputChanged(input, oldInput);
            fireInputChanged(input, oldInput);
        }

        @Override
        protected void internalExpandToLevel(Widget node, int level)
        {
            if (node instanceof TreeItem)
            {
                TreeItem item = (TreeItem)node;
                if (!canExpand(item))
                {
                    setExpanded(item, false);
                    return;
                }
            }
            super.internalExpandToLevel(node, level);
        }

        /**
         * Returns whether the given tree item can be expanded from
         * <code>expandXXX</code> methods.
         * <p>
         * Default implementation simply delegates to {@link
         * CommonOutlinePage#isAutoExpandable(TreeItem)}. Subclasses may extend.
         * </p>
         *
         * @param item the tree item (never <code>null</code>)
         * @return <code>true</code> if the given item can be expanded;
         *  <code>false</code> if it is to be collapsed
         */
        protected boolean canExpand(TreeItem item)
        {
            return isAutoExpandable(item);
        }
    }
}
