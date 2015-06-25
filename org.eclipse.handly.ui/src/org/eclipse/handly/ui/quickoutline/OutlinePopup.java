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
package org.eclipse.handly.ui.quickoutline;

import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * An abstract base implementation of an outline popup.
 */
public abstract class OutlinePopup
    extends PopupDialog
{
    private IOutlinePopupHost host;
    private KeyStroke invokingKeyStroke;
    private TreeViewer treeViewer;
    private Object initialSelection;

    /**
     * Creates a new outline popup.
     * <p>
     * Note: The parent shell will be set when this outline popup is initialized
     * with its host.
     * </p>
     * @see #init(IOutlinePopupHost, KeyStroke)
     */
    public OutlinePopup()
    {
        super(null, HOVER_SHELLSTYLE | SWT.RESIZE, true, true, false, true,
            true, null, null);
    }

    /**
     * Initializes this outline popup with the given host and, optionally,
     * invoking keystroke. This method must be called by clients before
     * attempting to {@link #open() open} the outline popup.
     * <p>
     * This method may be extended by subclasses. Subclasses must call
     * the superclass implementation.
     * </p>
     *
     * @param host the host of this outline popup (not <code>null</code>)
     * @param invokingKeyStroke the keystroke for invoking this outline popup,
     *  or <code>null</code> if none
     */
    public void init(IOutlinePopupHost host, KeyStroke invokingKeyStroke)
    {
        setParentShell(host.getControl().getShell());
        this.host = host;
        this.invokingKeyStroke = invokingKeyStroke;

        setInfoText(""); //$NON-NLS-1$
        create(); // create all controls, including tree viewer

        treeViewer.setUseHashlookup(shouldUseHashlookup());
        treeViewer.setContentProvider(getContentProvider());
        treeViewer.setLabelProvider(getLabelProvider());
        treeViewer.setInput(computeInput());

        initialSelection = computeInitialSelection();
        if (initialSelection != null)
        {
            if (initialSelection.equals(treeViewer.getInput()))
                initialSelection = null;
            else
                treeViewer.setSelection(new StructuredSelection(
                    initialSelection), true);
        }

        updateInfoText();
    }

    /**
     * Returns the host of this outline popup.
     *
     * @return the host of this outline popup,
     *  or <code>null</code> if it has not been set yet
     */
    protected final IOutlinePopupHost getHost()
    {
        return host;
    }

    /**
     * Returns the keystroke for invoking this outline popup.
     *
     * @return the keystroke for invoking this outline popup,
     *  or <code>null</code> if none
     */
    protected final KeyStroke getInvokingKeyStroke()
    {
        return invokingKeyStroke;
    }

    /**
     * Returns the tree viewer of this outline popup.
     *
     * @return the tree viewer of this outline popup,
     *  or <code>null</code> if it has not been created yet
     */
    protected final TreeViewer getTreeViewer()
    {
        return treeViewer;
    }

    /**
     * Returns the initially selected outline element.
     *
     * @return the initially selected outline element,
     *  or <code>null</code> if none
     */
    protected final Object getInitialSelection()
    {
        return initialSelection;
    }

    /**
     * Returns the currently selected outline element.
     *
     * @return the currently selected outline element,
     *  or <code>null</code> if none
     */
    protected final Object getSelectedElement()
    {
        return ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
    }

    @Override
    protected Control getFocusControl()
    {
        return treeViewer.getControl();
    }

    @Override
    protected void setTabOrder(Composite composite)
    {
        composite.setTabList(new Control[] { treeViewer.getTree() });
    }

    @Override
    protected Point getDefaultLocation(Point initialSize)
    {
        Control control = host.getControl();
        Point size = control.getSize();
        Point location = new Point((size.x / 2) - (initialSize.x / 2), (size.y
            / 2) - (initialSize.y / 2));
        return control.toDisplay(location);
    }

    @Override
    protected IDialogSettings getDialogSettings()
    {
        String sectionName = "QuickOutline"; //$NON-NLS-1$
        IDialogSettings settings =
            Activator.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null)
            settings = Activator.getDefault().getDialogSettings().addNewSection(
                sectionName);
        return settings;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        treeViewer = createTreeViewer(parent);
        Tree tree = treeViewer.getTree();
        addKeyListener(tree);
        addSelectionListener(tree);
        addMouseHoverListener(tree);
        addMouseListener(tree);
        return tree;
    }

    /**
     * Creates a tree viewer for this outline popup. The viewer has no input,
     * no content provider, a default label provider, no sorter, and no filters.
     * This method is called once, when the popup's control is created.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created tree viewer (not <code>null</code>)
     */
    protected TreeViewer createTreeViewer(Composite parent)
    {
        Tree tree = new Tree(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = tree.getItemHeight() * 12;
        tree.setLayoutData(gd);
        return new OutlineTreeViewer(tree);
    }

    /**
     * Updates the text to be shown in the popup's info area.
     * <p>
     * This implementation sets a default text. Subclasses may override.
     * </p>
     */
    protected void updateInfoText()
    {
        setInfoText(Messages.OutlinePopup_text);
    }

    /**
     * Attempts to reveal the currently selected outline element in the
     * outline popup's host. If successful, closes this outline popup.
     */
    protected void gotoSelectedElement()
    {
        Object element = getSelectedElement();
        if (element == null)
            return;
        if (revealInHost(element))
            close();
    }

    /**
     * Changes the mode of this outline popup. This method is called when
     * the invoking keystroke is pressed.
     * <p>
     * Default implementation does nothing. Subclasses may override.
     * </p>
     */
    protected void changeOutlineMode()
    {
    }

    /**
     * Returns whether the outline popup's tree viewer should use hash lookup.
     * This method is called once, when the popup's control is created.
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
     * Returns the content provider that is to be used by the outline popup's
     * tree viewer. This method is called once, when the popup's control
     * is created.
     *
     * @return the content provider (not <code>null</code>)
     */
    protected abstract ITreeContentProvider getContentProvider();

    /**
     * Returns the label provider that is to be used by the outline popup's
     * tree viewer. This method is called once, when the popup's control
     * is created.
     *
     * @return the label provider (not <code>null</code>)
     */
    protected abstract IBaseLabelProvider getLabelProvider();

    /**
     * Computes the input element for the outline popup's tree viewer.
     * This method is called once, when the popup's control is created.
     *
     * @return the input element (may be <code>null</code>)
     */
    protected abstract Object computeInput();

    /**
     * Computes the element to be selected initially in the outline popup's
     * tree viewer. This method is called once, when the popup's control
     * is created.
     *
     * @return the element to be selected initially in this outline popup,
     *  or <code>null</code> if none
     */
    protected Object computeInitialSelection()
    {
        ISelection hostSelection = host.getSelectionProvider().getSelection();
        if (hostSelection == null || hostSelection.isEmpty())
            return null;
        return getCorrespondingElement(hostSelection);
    }

    /**
     * Returns the element corresponding to the given host selection.
     *
     * @param hostSelection the host selection (never <code>null</code>, never empty)
     * @return the element corresponding to the host selection,
     *  or <code>null</code> if there is no such element
     */
    protected abstract Object getCorrespondingElement(ISelection hostSelection);

    /**
     * Attempts to reveal the given outline element in the outline popup's host.
     *
     * @param outlineElement the outline element (never <code>null</code>)
     * @return <code>true</code> if the element was successfully revealed;
     *  <code>false</code> otherwise
     */
    protected abstract boolean revealInHost(Object outlineElement);

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
     * Returns the invoking key listener. When the invoking key is pressed,
     * this listener changes the mode of the outline popup and updates the
     * text in the popup's info area.
     *
     * @return the invoking key listener (never <code>null</code>)
     */
    protected final KeyListener getInvokingKeyListener()
    {
        return new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int accelerator =
                    SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
                KeyStroke keyStroke =
                    SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
                if (keyStroke.equals(invokingKeyStroke))
                {
                    changeOutlineMode();
                    updateInfoText();
                    e.doit = false;
                }
            }
        };
    }

    private void addKeyListener(Tree tree)
    {
        tree.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.character == 0x1B) // ESC
                {
                    close();
                }
            }
        });

        if (invokingKeyStroke != null)
            tree.addKeyListener(getInvokingKeyListener());
    }

    private void addSelectionListener(Tree tree)
    {
        tree.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                gotoSelectedElement();
            }
        });
    }

    private void addMouseHoverListener(final Tree tree)
    {
        tree.addMouseMoveListener(new MouseMoveListener()
        {
            TreeItem lastItem = null;

            public void mouseMove(MouseEvent e)
            {
                if (tree.equals(e.getSource()))
                {
                    Object o = tree.getItem(new Point(e.x, e.y));
                    if (o instanceof TreeItem)
                    {
                        if (!o.equals(lastItem))
                        {
                            lastItem = (TreeItem)o;
                            tree.setSelection(new TreeItem[] { lastItem });
                        }
                        else if (e.y < tree.getItemHeight() / 4)
                        {
                            // Scroll up
                            Point p = tree.toDisplay(e.x, e.y);
                            Item item = treeViewer.scrollUp(p.x, p.y);
                            if (item instanceof TreeItem)
                            {
                                lastItem = (TreeItem)item;
                                tree.setSelection(new TreeItem[] { lastItem });
                            }
                        }
                        else if (e.y > tree.getBounds().height
                            - tree.getItemHeight() / 4)
                        {
                            // Scroll down
                            Point p = tree.toDisplay(e.x, e.y);
                            Item item = treeViewer.scrollDown(p.x, p.y);
                            if (item instanceof TreeItem)
                            {
                                lastItem = (TreeItem)item;
                                tree.setSelection(new TreeItem[] { lastItem });
                            }
                        }
                    }
                }
            }
        });
    }

    private void addMouseListener(final Tree tree)
    {
        tree.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseUp(MouseEvent e)
            {
                if (tree.getSelectionCount() < 1)
                    return;

                if (e.button != 1)
                    return;

                if (tree.equals(e.getSource()))
                {
                    Object o = tree.getItem(new Point(e.x, e.y));
                    TreeItem selection = tree.getSelection()[0];
                    if (selection.equals(o))
                        gotoSelectedElement();
                }
            }
        });
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
         * Default implementation simply delegates to {@link OutlinePopup#
         * isAutoExpandable(TreeItem)}. Subclasses may extend.
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
