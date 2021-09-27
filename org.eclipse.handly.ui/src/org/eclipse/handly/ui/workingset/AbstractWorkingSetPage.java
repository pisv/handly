/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.ui.workingsets.AbstractWorkingSetWizardPage)
 *******************************************************************************/
package org.eclipse.handly.ui.workingset;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.ui.viewer.LabelComparator;
import org.eclipse.handly.util.ArrayUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * A partial implementation of {@link IWorkingSetPage}. A tree viewer on the
 * left is used to show the workspace content, a table viewer on the right
 * is used to show the working set content. Buttons to move content from right
 * to left and vice versa are available between the two viewers. A text field
 * allows to set/change the working set name.
 */
public abstract class AbstractWorkingSetPage
    extends WizardPage
    implements IWorkingSetPage
{
    /**
     * A zero-length array of the runtime type <code>IAdaptable[]</code>.
     */
    protected static final IAdaptable[] NO_ELEMENTS = new IAdaptable[0];

    private Text workingSetName;
    private TreeViewer tree;
    private TableViewer table;
    private ITreeContentProvider treeContentProvider;
    private IWorkingSet workingSet;
    private boolean firstCheck = true;
    private Set<IAdaptable> selectedElements = new LinkedHashSet<>();
    private IStructuredSelection initialSelection;

    /**
     * Creates a new wizard page with the given name, title, and image.
     *
     * @param pageName the name of the page (not <code>null</code>)
     * @param title the title for this wizard page,
     *  or <code>null</code> if none
     * @param titleImage the image descriptor for the title of this wizard page,
     *  or <code>null</code> if none
     */
    public AbstractWorkingSetPage(String pageName, String title,
        ImageDescriptor titleImage)
    {
        super(pageName, title, titleImage);
        setDescription(Messages.AbstractWorkingSetPage_workingSet_description);
        IWorkbenchWindow activeWorkbenchWindow =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null)
        {
            ISelection selection =
                activeWorkbenchWindow.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection)
                initialSelection = (IStructuredSelection)selection;
        }
    }

    /**
     * Sets the selection used as a basis for the initial state of this page.
     *
     * @param selection may be <code>null</code> or empty
     */
    public void setInitialSelection(IStructuredSelection selection)
    {
        this.initialSelection = selection;
    }

    @Override
    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        setControl(composite);

        Label label = new Label(composite, SWT.WRAP);
        label.setText(Messages.AbstractWorkingSetPage_workingSet_name);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL
            | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        label.setLayoutData(gd);

        workingSetName = new Text(composite, SWT.SINGLE | SWT.BORDER);
        workingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
            | GridData.HORIZONTAL_ALIGN_FILL));
        workingSetName.addModifyListener(e -> validateInput());

        Composite leftCenterRightComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = convertHeightInCharsToPixels(20);
        leftCenterRightComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftCenterRightComposite.setLayout(gridLayout);

        Composite leftComposite = new Composite(leftCenterRightComposite,
            SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        leftComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);

        Composite centerComposite = new Composite(leftCenterRightComposite,
            SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        centerComposite.setLayout(gridLayout);
        centerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
            false));

        Composite rightComposite = new Composite(leftCenterRightComposite,
            SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        rightComposite.setLayoutData(gridData);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        rightComposite.setLayout(gridLayout);

        createTree(leftComposite);
        createTable(rightComposite);

        if (workingSet != null)
            workingSetName.setText(workingSet.getName());

        initializeSelectedElements();
        validateInput();

        table.setInput(selectedElements);
        table.refresh(true);
        tree.refresh(true);

        createButtonBar(centerComposite);

        workingSetName.setFocus();
        workingSetName.setSelection(0, workingSetName.getText().length());

        Dialog.applyDialogFont(composite);
    }

    @Override
    public void finish()
    {
        String name = workingSetName.getText();
        IAdaptable[] elements = getFinalWorkingSetElements(workingSet,
            selectedElements.toArray(NO_ELEMENTS));

        if (workingSet == null)
        {
            IWorkingSetManager workingSetManager =
                PlatformUI.getWorkbench().getWorkingSetManager();
            workingSet = workingSetManager.createWorkingSet(name, elements);
            workingSet.setId(getPageId());
        }
        else
        {
            workingSet.setName(name);
            workingSet.setElements(elements);
        }
    }

    @Override
    public IWorkingSet getSelection()
    {
        return workingSet;
    }

    @Override
    public void setSelection(IWorkingSet workingSet)
    {
        if (workingSet == null)
            throw new IllegalArgumentException();

        this.workingSet = workingSet;
        if (getContainer() != null && getShell() != null
            && workingSetName != null)
        {
            firstCheck = false;
            workingSetName.setText(workingSet.getName());
            initializeSelectedElements();
            validateInput();
        }
    }

    /**
     * Returns the page id as specified in the extension point
     * <code>org.eclipse.ui.workingSets</code>.
     *
     * @return the page id
     */
    protected abstract String getPageId();

    /**
     * Configures the tree viewer on the left side of this page.
     * <p>
     * Implementors must set:
     * </p>
     * <ul>
     *  <li>The content provider</li>
     *  <li>The label provider</li>
     *  <li>The viewer input</li>
     * </ul>
     * <p>
     * They may also set:
     * </p>
     * <ul>
     *   <li>The viewer comparator</li>
     *   <li>Any viewer filter</li>
     * </ul>
     * <p>
     * Note that the initial selection is set in {@link
     * #initializeTreeSelection(TreeViewer)}, which is called
     * right after this method.
     * </p>
     *
     * @param tree the tree viewer to configure (never <code>null</code>)
     */
    protected abstract void configureTree(TreeViewer tree);

    /**
     * Configures the table viewer on the right side of this page.
     * <p>
     * Implementors must set:
     * </p>
     * <ul>
     *  <li>The label provider</li>
     * </ul>
     * <p>
     * They may also set:
     * </p>
     * <ul>
     *   <li>The viewer comparator</li>
     * </ul>
     * <p>
     * They must not set:
     * </p>
     * <ul>
     *  <li>The content provider</li>
     *  <li>The viewer input</li>
     *  <li>Any viewer filter</li>
     * </ul>
     *
     * @param table the table viewer to configure (never <code>null</code>)
     */
    protected abstract void configureTable(TableViewer table);

    /**
     * Returns the selection used as a basis for the initial state of this page.
     *
     * @return a selection (may be <code>null</code> or empty)
     */
    protected final IStructuredSelection getInitialSelection()
    {
        return initialSelection;
    }

    /**
     * Computes and sets the initial selection for the tree viewer.
     * <p>
     * If the working set that will be configured by this page exists,
     * this implementation adapts the elements from {@link #getInitialSelection()}
     * using {@link #adaptElements(IAdaptable[])} and sets the result as the
     * current selection for the tree viewer.
     * </p>
     *
     * @param tree the tree viewer (never <code>null</code>)
     */
    protected void initializeTreeSelection(TreeViewer tree)
    {
        if (workingSet == null)
            return;

        IStructuredSelection selection = getInitialSelection();
        if (selection != null && !selection.isEmpty())
        {
            IAdaptable[] elements = adaptElements(getAdaptables(
                selection.toArray()));
            if (elements.length > 0)
            {
                tree.setSelection(new StructuredSelection(elements));
            }
        }
    }

    /**
     * Transforms the supplied elements into elements that are suitable for
     * containment in the working set configured by this page.
     * <p>
     * This implementation creates a temporary working set, sets its id to {@link
     * #getPageId()}, and delegates to {@link IWorkingSet#adaptElements(IAdaptable[])}.
     * </p>
     *
     * @param objects the objects to transform (never <code>null</code>)
     * @return an array of transformed elements that may be empty if no elements
     *  from the original array are suitable (not <code>null</code>)
     */
    @SuppressWarnings("restriction")
    protected IAdaptable[] adaptElements(IAdaptable[] objects)
    {
        IWorkingSetManager workingSetManager =
            PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet workingSet = workingSetManager.createWorkingSet("", //$NON-NLS-1$
            NO_ELEMENTS);
        workingSet.setId(getPageId());
        ((org.eclipse.ui.internal.AbstractWorkingSet)workingSet).connect(
            workingSetManager);
        try
        {
            return workingSet.adaptElements(objects);
        }
        finally
        {
            ((org.eclipse.ui.internal.AbstractWorkingSet)workingSet).disconnect();
        }

        // Unfortunately, this "clean" implementation doesn't work,
        // as WorkingSet#adaptElements expects a WorkingSetManager
        // rather than AbstractWorkingSetManager:

//        ILocalWorkingSetManager workingSetManager =
//            PlatformUI.getWorkbench().createLocalWorkingSetManager();
//        try
//        {
//            IWorkingSet workingSet =
//                workingSetManager.createWorkingSet("", NO_ELEMENTS);
//            workingSet.setId(getPageId());
//            workingSetManager.addWorkingSet(workingSet);
//            return workingSet.adaptElements(objects);
//        }
//        finally
//        {
//            workingSetManager.dispose();
//        }
    }

    /**
     * Given an array, selects the {@link IAdaptable} elements.
     *
     * @param elements not <code>null</code>
     * @return an array of the <code>IAdaptable</code> elements
     *  selected from the given array (never <code>null</code>)
     */
    protected static IAdaptable[] getAdaptables(Object[] elements)
    {
        return ArrayUtil.elementsOfType(elements, IAdaptable.class).toArray(
            NO_ELEMENTS);
    }

    /**
     * Returns the elements that are to be shown in the table viewer initially.
     * Returns an empty array if the table viewer should be initially empty.
     * The given working set is the working set that will be configured
     * by this page, or <code>null</code> if it does not yet exist.
     * <p>
     * This implementation returns the elements contained in the working set or,
     * if the working set does not yet exist, the elements adapted from {@link
     * #getInitialSelection()} using {@link #adaptElements(IAdaptable[])}.
     * </p>
     *
     * @param workingSet the working set to configure,
     *  or <code>null</code> if it does not yet exist
     * @return the elements to show in the table viewer (not <code>null</code>)
     */
    protected IAdaptable[] getInitialWorkingSetElements(IWorkingSet workingSet)
    {
        if (workingSet != null)
            return workingSet.getElements();
        else
        {
            IStructuredSelection selection = getInitialSelection();
            if (selection != null && !selection.isEmpty())
                return adaptElements(getAdaptables(selection.toArray()));
        }
        return NO_ELEMENTS;
    }

    /**
     * Returns the elements that are to be set into the working set
     * configured by this page.
     * <p>
     * This implementation returns the given elements unchanged.
     * </p>
     *
     * @param workingSet the working set to configure,
     *  or <code>null</code> if it does not yet exist
     * @param elements the elements explicitly selected by the user
     *  (never <code>null</code>)
     * @return the elements to ultimately set into the working set
     *  (not <code>null</code>)
     */
    protected IAdaptable[] getFinalWorkingSetElements(IWorkingSet workingSet,
        IAdaptable[] elements)
    {
        return elements;
    }

    private void createTree(Composite parent)
    {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
        label.setText(Messages.AbstractWorkingSetPage_workspace_content);

        tree = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.MULTI);
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
            true));

        tree.setComparator(new LabelComparator());
        tree.addFilter(new AddedElementsFilter());
        tree.setUseHashlookup(true);

        configureTree(tree);
        initializeTreeSelection(tree);

        treeContentProvider = (ITreeContentProvider)tree.getContentProvider();
    }

    private void createButtonBar(Composite parent)
    {
        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        final Button addButton = new Button(parent, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        addButton.setText(Messages.AbstractWorkingSetPage_add_button);
        addButton.setEnabled(!tree.getSelection().isEmpty());

        final Button addAllButton = new Button(parent, SWT.PUSH);
        addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
            false));
        addAllButton.setText(Messages.AbstractWorkingSetPage_addAll_button);
        addAllButton.setEnabled(tree.getTree().getItems().length > 0);

        final Button removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
            false));
        removeButton.setText(Messages.AbstractWorkingSetPage_remove_button);
        removeButton.setEnabled(!table.getSelection().isEmpty());

        final Button removeAllButton = new Button(parent, SWT.PUSH);
        removeAllButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
            false));
        removeAllButton.setText(
            Messages.AbstractWorkingSetPage_removeAll_button);
        removeAllButton.setEnabled(!selectedElements.isEmpty());

        tree.addSelectionChangedListener(event -> addButton.setEnabled(
            !event.getSelection().isEmpty()));

        addButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                addTreeSelection();

                removeAllButton.setEnabled(true);
                addAllButton.setEnabled(tree.getTree().getItems().length > 0);
            }
        });

        tree.addDoubleClickListener(event ->
        {
            addTreeSelection();

            removeAllButton.setEnabled(true);
            addAllButton.setEnabled(tree.getTree().getItems().length > 0);
        });

        table.addSelectionChangedListener(event -> removeButton.setEnabled(
            !event.getSelection().isEmpty()));

        removeButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                removeTableSelection();

                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(!selectedElements.isEmpty());
            }
        });

        table.addDoubleClickListener(event ->
        {
            removeTableSelection();

            addAllButton.setEnabled(true);
            removeAllButton.setEnabled(!selectedElements.isEmpty());
        });

        addAllButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                TreeItem[] items = tree.getTree().getItems();
                for (TreeItem item : items)
                {
                    Object element = item.getData();
                    if (element instanceof IAdaptable)
                        selectedElements.add((IAdaptable)element);
                }
                table.refresh();
                tree.refresh();

                addAllButton.setEnabled(false);
                removeAllButton.setEnabled(true);
            }
        });

        removeAllButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                selectedElements.clear();

                table.refresh();
                tree.refresh();

                removeAllButton.setEnabled(false);
                addAllButton.setEnabled(true);
            }
        });
    }

    /*
     * Moves selected elements in the tree into the table
     */
    private void addTreeSelection()
    {
        IStructuredSelection selection =
            (IStructuredSelection)tree.getSelection();
        IAdaptable[] elements = getAdaptables(selection.toArray());
        selectedElements.addAll(Arrays.asList(elements));
        table.add(elements);
        tree.remove((Object[])elements);
        table.setSelection(selection);
        table.getControl().setFocus();
        validateInput();
    }

    /*
     * Moves the selected elements in the table into the tree
     */
    private void removeTableSelection()
    {
        IStructuredSelection selection =
            (IStructuredSelection)table.getSelection();
        selectedElements.removeAll(selection.toList());
        Object[] selectedElements = selection.toArray();
        table.remove(selectedElements);
        try
        {
            tree.getTree().setRedraw(false);
            for (Object selectedElement : selectedElements)
            {
                tree.refresh(treeContentProvider.getParent(selectedElement),
                    true);
            }
        }
        finally
        {
            tree.getTree().setRedraw(true);
        }
        tree.setSelection(selection);
        tree.getControl().setFocus();
        validateInput();
    }

    private void createTable(Composite parent)
    {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(Messages.AbstractWorkingSetPage_workingSet_content);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        table = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.getControl().setLayoutData(gd);

        table.setComparator(new LabelComparator());
        table.setUseHashlookup(true);

        configureTable(table);

        table.setContentProvider(new IStructuredContentProvider()
        {
            @Override
            public Object[] getElements(Object inputElement)
            {
                return selectedElements.toArray();
            }

            @Override
            public void dispose()
            {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput)
            {
            }
        });
    }

    private void validateInput()
    {
        String errorMessage = null;
        String infoMessage = null;
        String newText = workingSetName.getText();

        if (!newText.equals(newText.trim()))
        {
            errorMessage =
                Messages.AbstractWorkingSetPage_warning_nameWhitespace;
        }

        if (newText.isEmpty())
        {
            if (firstCheck)
            {
                setPageComplete(false);
                firstCheck = false;
                return;
            }
            else
            {
                errorMessage =
                    Messages.AbstractWorkingSetPage_warning_nameMustNotBeEmpty;
            }
        }

        firstCheck = false;

        if (errorMessage == null && workingSet != null && !newText.equals(
            workingSet.getName()))
        {
            IWorkingSet[] workingSets =
                PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
            for (IWorkingSet workingSet : workingSets)
            {
                if (newText.equals(workingSet.getName()))
                {
                    errorMessage =
                        Messages.AbstractWorkingSetPage_warning_workingSetExists;
                }
            }
        }

        if (!hasSelectedElement())
        {
            infoMessage =
                Messages.AbstractWorkingSetPage_warning_elementMustBeChecked;
        }

        setMessage(infoMessage, INFORMATION);
        setErrorMessage(errorMessage);
        setPageComplete(errorMessage == null);
    }

    private boolean hasSelectedElement()
    {
        return !selectedElements.isEmpty();
    }

    private void initializeSelectedElements()
    {
        selectedElements.clear();
        selectedElements.addAll(Arrays.asList(getInitialWorkingSetElements(
            workingSet)));
    }

    private class AddedElementsFilter
        extends ViewerFilter
    {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element)
        {
            return !selectedElements.contains(element);
        }
    }
}
