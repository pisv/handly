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

import java.util.regex.Pattern;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * An abstract implementation of an outline popup that supports filtering
 * based on a pattern inputed by the user.
 */
public abstract class FilteringOutlinePopup
    extends OutlinePopup
{
    private Text filterText;
    private IMatcher<Object> patternMatcher;
    private Composite viewMenuButtonComposite;

    @Override
    public void init(IOutlinePopupHost host, KeyStroke invokingKeyStroke)
    {
        super.init(host, invokingKeyStroke);
        TreeViewer treeViewer = getTreeViewer();
        treeViewer.setExpandPreCheckFilters(true);
        treeViewer.addFilter(new PatternBasedFilter());
    }

    /**
     * Returns the filter text control of this outline popup.
     *
     * @return the filter text control of this outline popup,
     *  or <code>null</code> if it has not been created yet
     */
    protected final Text getFilterText()
    {
        return filterText;
    }

    /**
     * Returns the current pattern matcher for this outline popup.
     *
     * @return the current pattern matcher for this outline popup,
     *  or <code>null</code> if none
     */
    protected final IMatcher<Object> getPatternMatcher()
    {
        return patternMatcher;
    }

    @Override
    protected Control getFocusControl()
    {
        return filterText;
    }

    @Override
    protected void setTabOrder(Composite composite)
    {
        viewMenuButtonComposite.setTabList(new Control[] { filterText });
        composite.setTabList(new Control[] { viewMenuButtonComposite,
            getTreeViewer().getTree() });
    }

    @Override
    protected TreeViewer createTreeViewer(Composite parent)
    {
        TreeViewer baseTreeViewer = super.createTreeViewer(parent);
        return new FilteringOutlineTreeViewer(baseTreeViewer.getTree());
    }

    @Override
    protected Control createTitleMenuArea(Composite parent)
    {
        viewMenuButtonComposite = (Composite)super.createTitleMenuArea(parent);
        return viewMenuButtonComposite;
    }

    @Override
    protected Control createTitleControl(Composite parent)
    {
        filterText = createFilterText(parent);
        return filterText;
    }

    /**
     * Creates the text control to be used for entering the filter pattern.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created filter text control (not <code>null</code>)
     */
    protected Text createFilterText(Composite parent)
    {
        Text filterText = new Text(parent, SWT.NONE);
        Dialog.applyDialogFont(filterText);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.CENTER;
        filterText.setLayoutData(data);

        filterText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == 0x0D) // ENTER
                {
                    gotoSelectedElement();
                }
                else if (e.keyCode == SWT.ARROW_DOWN
                    || e.keyCode == SWT.ARROW_UP)
                {
                    getTreeViewer().getControl().setFocus();
                }
                else if (e.character == 0x1B) // ESC
                {
                    close();
                }
            }
        });

        if (getInvokingKeyStroke() != null)
            filterText.addKeyListener(getInvokingKeyListener());

        filterText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                updatePatternMatcher(((Text)e.widget).getText());
            }
        });

        return filterText;
    }

    /**
     * Updates the current pattern matcher to match the given pattern, then
     * calls {@link #patternMatcherUpdated()} to refresh the tree viewer.
     *
     * @param pattern the pattern string (not <code>null</code>)
     */
    protected final void updatePatternMatcher(String pattern)
    {
        patternMatcher = createPatternMatcher(pattern);
        patternMatcherUpdated();
    }

    /**
     * Returns a new pattern matcher based on the given pattern.
     *
     * @param pattern the pattern string (not <code>null</code>)
     * @return the created pattern matcher, or <code>null</code>
     *  if filtering is not required
     */
    protected IMatcher<Object> createPatternMatcher(String pattern)
    {
        int length = pattern.length();
        if (length == 0)
            return null;
        if (pattern.charAt(length - 1) != '*')
            pattern = pattern + '*';
        return new ElementMatcher(new StringMatcher(pattern,
            pattern.toLowerCase().equals(pattern)));
    }

    /**
     * Notifies that the pattern matcher has been updated.
     * <p>
     * Default implementation refreshes the tree viewer, expands all nodes
     * of the tree, and selects the first matching element.
     * </p>
     */
    protected void patternMatcherUpdated()
    {
        TreeViewer treeViewer = getTreeViewer();
        treeViewer.getControl().setRedraw(false);
        treeViewer.refresh();
        treeViewer.expandAll();
        treeViewer.getControl().setRedraw(true);
        selectFirstMatch();
    }

    /**
     * Selects the first element that matches the current filter pattern.
     */
    protected void selectFirstMatch()
    {
        Object focalElement = getFocalElement();
        Object focalItem = null;
        TreeViewer treeViewer = getTreeViewer();
        if (focalElement != null)
            focalItem = treeViewer.testFindItem(focalElement);
        TreeItem item;
        Tree tree = treeViewer.getTree();
        if (focalItem instanceof TreeItem)
            item = findItem(new TreeItem[] { (TreeItem)focalItem });
        else
            item = findItem(tree.getItems());
        if (item == null)
            treeViewer.setSelection(StructuredSelection.EMPTY);
        else
        {
            tree.setSelection(item);
            tree.showItem(item);
        }
    }

    /**
     * Returns the current focal element for this outline popup.
     * <p>
     * Default implementation returns the initially selected element.
     * Subclasses may override.
     * </p>
     *
     * @return the current focal element for this outline popup,
     *  or <code>null</code> if none
     */
    protected Object getFocalElement()
    {
        return getInitialSelection();
    }

    private TreeItem findItem(TreeItem[] items)
    {
        return findItem(items, null, true);
    }

    private TreeItem findItem(TreeItem[] items, TreeItem[] toBeSkipped,
        boolean allowToGoUp)
    {
        if (patternMatcher == null)
            return items.length > 0 ? items[0] : null;

        // First search at same level
        for (int i = 0; i < items.length; i++)
        {
            TreeItem item = items[i];
            Object element = item.getData();
            if (patternMatcher.matches(element))
                return item;
        }

        // Go one level down for each item
        for (int i = 0; i < items.length; i++)
        {
            final TreeItem item = items[i];
            TreeItem foundItem = findItem(selectItems(item.getItems(),
                toBeSkipped), null, false);
            if (foundItem != null)
                return foundItem;
        }

        if (!allowToGoUp || items.length == 0)
            return null;

        // Go one level up (parent is the same for all items)
        TreeItem parentItem = items[0].getParentItem();
        if (parentItem != null)
            return findItem(new TreeItem[] { parentItem }, items, true);

        // Check root elements
        return findItem(selectItems(items[0].getParent().getItems(), items),
            null, false);
    }

    private static boolean canSkip(TreeItem item, TreeItem[] toBeSkipped)
    {
        if (toBeSkipped == null)
            return false;

        for (int i = 0; i < toBeSkipped.length; i++)
        {
            if (toBeSkipped[i] == item)
                return true;
        }
        return false;
    }

    private static TreeItem[] selectItems(TreeItem[] items,
        TreeItem[] toBeSkipped)
    {
        if (toBeSkipped == null || toBeSkipped.length == 0)
            return items;

        int j = 0;
        for (int i = 0; i < items.length; i++)
        {
            TreeItem item = items[i];
            if (!canSkip(item, toBeSkipped))
                items[j++] = item;
        }
        if (j == items.length)
            return items;

        TreeItem[] result = new TreeItem[j];
        System.arraycopy(items, 0, result, 0, j);
        return result;
    }

    /**
     * Overrides {@link OutlinePopup.OutlineTreeViewer#canExpand(TreeItem)
     * canExpand} method to allow expanding any tree item when the pattern-based
     * filter is active.
     */
    protected class FilteringOutlineTreeViewer
        extends OutlineTreeViewer
    {
        public FilteringOutlineTreeViewer(Tree tree)
        {
            super(tree);
        }

        @Override
        protected boolean canExpand(TreeItem item)
        {
            if (patternMatcher != null)
                return true;
            return super.canExpand(item);
        }
    }

    /**
     * Determines a true or false for a given object.
     */
    protected interface IMatcher<T>
    {
        /**
         * Determines a true or false for the given object.
         *
         * @param object may be <code>null</code>
         * @return <code>true</code> if the specified object matches;
         *  <code>false</code> otherwise
         */
        boolean matches(T object);
    }

    /**
     * Pattern-based element matcher.
     */
    protected class ElementMatcher
        implements IMatcher<Object>
    {
        private final IMatcher<String> stringMatcher;

        /**
         * Creates a new element matcher based on the given string matcher.
         *
         * @param stringMatcher not <code>null</code>
         */
        public ElementMatcher(IMatcher<String> stringMatcher)
        {
            if (stringMatcher == null)
                throw new IllegalArgumentException();
            this.stringMatcher = stringMatcher;
        }

        @Override
        public final boolean matches(Object element)
        {
            if (element == null)
                return false;
            return stringMatcher.matches(getText(element));
        }

        /**
         * Returns the text for the given outline element.
         * <p>
         * Default implementation returns the label string obtained from
         * the tree viewer's label provider.
         * </p>
         *
         * @param element the outline element (never <code>null</code>)
         * @return the text for the given outline element,
         *  or <code>null</code> if no text can be obtained
         */
        protected String getText(Object element)
        {
            IBaseLabelProvider labelProvider =
                getTreeViewer().getLabelProvider();
            if (labelProvider instanceof ILabelProvider)
                return ((ILabelProvider)labelProvider).getText(element);
            else if (labelProvider instanceof IStyledLabelProvider)
                return ((IStyledLabelProvider)labelProvider).getStyledText(
                    element).toString();
            else if (labelProvider instanceof DelegatingStyledCellLabelProvider)
                return ((DelegatingStyledCellLabelProvider)labelProvider).getStyledStringProvider().getStyledText(
                    element).toString();
            return null;
        }
    }

    /**
     * Pattern-based string matcher.
     */
    protected static class StringMatcher
        implements IMatcher<String>
    {
        private final String expression;
        private final boolean ignoreCase;
        private Pattern pattern;

        /**
         * Creates a new string matcher based on the given pattern.
         *
         * @param pattern the pattern string (not <code>null</code>)
         * @param ignoreCase whether case-insensitive matching is enabled
         */
        public StringMatcher(String pattern, boolean ignoreCase)
        {
            this.expression = translatePattern(pattern);
            this.ignoreCase = ignoreCase;
        }

        @Override
        public final boolean matches(String text)
        {
            if (text == null)
                return false;
            return getPattern().matcher(text).find();
        }

        /**
         * Translates the given pattern into a regular expression.
         *
         * @param pattern the pattern string (not <code>null</code>)
         * @return the regular expression corresponding to the pattern
         *  (never <code>null</code>)
         */
        protected String translatePattern(String pattern)
        {
            String expression = pattern.replaceAll("\\(", "\\\\("); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\)", "\\\\)"); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\[", "\\\\["); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\]", "\\\\]"); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\{", "\\\\{"); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\}", "\\\\}"); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
            expression = expression.replaceAll("\\?", "."); //$NON-NLS-1$ //$NON-NLS-2$
            if (!expression.startsWith("^")) //$NON-NLS-1$
                expression = "^" + expression; //$NON-NLS-1$
            return expression;
        }

        private Pattern getPattern()
        {
            if (pattern == null)
            {
                if (ignoreCase)
                    pattern = Pattern.compile(expression,
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                else
                    pattern = Pattern.compile(expression);
            }
            return pattern;
        }
    }

    private class PatternBasedFilter
        extends ViewerFilter
    {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element)
        {
            if (patternMatcher == null)
                return true;

            if (patternMatcher.matches(element))
                return true;

            return hasUnfilteredChild((TreeViewer)viewer, element);
        }

        private boolean hasUnfilteredChild(TreeViewer treeViewer,
            Object element)
        {
            // This works only because 'expandPreCheckFilters' was set to true
            return treeViewer.isExpandable(element);
        }
    }
}
