/*******************************************************************************
 * Copyright (c) 2018, 2019 1C-Soft LLC.
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
package org.eclipse.handly.ui.callhierarchy;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.ui.DefaultEditorUtility;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.handly.ui.action.HistoryDropDownAction;
import org.eclipse.handly.ui.viewer.ColumnDescription;
import org.eclipse.handly.ui.viewer.DelegatingSelectionProvider;
import org.eclipse.handly.ui.viewer.LabelComparator;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * An abstract base implementation of a call hierarchy view.
 */
public abstract class CallHierarchyViewPart
    extends ViewPart
{
    /**
     * Pop-up menu: name of group for focus actions (value
     * <code>"group.focus"</code>).
     */
    protected static final String GROUP_FOCUS = "group.focus"; //$NON-NLS-1$

    private static final Object[] NO_ELEMENTS = new Object[0];

    private static final String KEY_HIERARCHY_KIND =
        "org.eclipse.handly.callhierarchy.view.kind"; //$NON-NLS-1$
    private static final String KEY_ORIENTATION =
        "org.eclipse.handly.callhierarchy.view.orientation"; //$NON-NLS-1$
    private static final String KEY_HORIZONTAL_RATIO =
        "org.eclipse.handly.callhierarchy.view.horizontalRatio"; //$NON-NLS-1$
    private static final String KEY_VERTICAL_RATIO =
        "org.eclipse.handly.callhierarchy.view.verticalRatio"; //$NON-NLS-1$

    private static final int ORIENTATION_AUTO = SWT.HORIZONTAL | SWT.VERTICAL;

    private final EnumSet<CallHierarchyKind> supportedHierarchyKinds;
    private CallHierarchyKind hierarchyKind;
    private int orientation = ORIENTATION_AUTO;
    private boolean orientationAdjusted;
    private int horizontalRatio = 500, verticalRatio = 500;
    private Object[] inputElements = NO_ELEMENTS;
    private boolean pinned;
    private PageBook pageBook;
    private Control noHierarchyPage;
    private SashForm sashForm;
    private TreeViewer hierarchyViewer;
    private TableViewer locationViewer;
    private EditorOpener editorOpener;
    private final RefreshAction refreshAction = new RefreshAction();
    private final RefreshElementAction refreshElementAction =
        new RefreshElementAction();
    private final RemoveFromViewAction removeFromViewAction =
        new RemoveFromViewAction();
    private SetHierarchyKindAction[] setHierarchyKindActions =
        new SetHierarchyKindAction[0];
    private SetOrientationAction[] setOrientationActions =
        new SetOrientationAction[] { new SetOrientationAction(SWT.VERTICAL),
            new SetOrientationAction(SWT.HORIZONTAL), new SetOrientationAction(
                ORIENTATION_AUTO) };
    private final FocusOnSelectionAction focusOnSelectionAction =
        new FocusOnSelectionAction();
    private HistoryDropDownAction<HistoryEntry> historyDropDownAction;
    private final PinAction pinAction = new PinAction();

    private final IPartListener partListener = new IPartListener()
    {
        @Override
        public void partActivated(IWorkbenchPart part)
        {
            if (part != CallHierarchyViewPart.this)
                return;

            getViewManager().viewOpenedOrActivated(CallHierarchyViewPart.this);
        }

        @Override
        public void partClosed(IWorkbenchPart part)
        {
            if (part != CallHierarchyViewPart.this)
                return;

            getViewManager().viewClosed(CallHierarchyViewPart.this);
        }

        @Override
        public void partOpened(IWorkbenchPart part)
        {
            if (part != CallHierarchyViewPart.this)
                return;

            getViewManager().viewOpenedOrActivated(CallHierarchyViewPart.this);
            refresh();
        }

        @Override
        public void partDeactivated(IWorkbenchPart part)
        {
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part)
        {
        }
    };

    /**
     * Constructs a call hierarchy view that supports all of the
     * call hierarchy kinds.
     *
     * @see CallHierarchyKind
     * @see #CallHierarchyViewPart(EnumSet)
     */
    public CallHierarchyViewPart()
    {
        this(EnumSet.allOf(CallHierarchyKind.class));
    }

    /**
     * Constructs a call hierarchy view that supports the given
     * call hierarchy kinds.
     *
     * @param supportedHierarchyKinds not <code>null</code> and not empty
     */
    public CallHierarchyViewPart(
        EnumSet<CallHierarchyKind> supportedHierarchyKinds)
    {
        if (supportedHierarchyKinds == null)
            throw new IllegalArgumentException();
        if (supportedHierarchyKinds.isEmpty())
            throw new IllegalArgumentException();
        this.supportedHierarchyKinds = supportedHierarchyKinds.clone();
        this.hierarchyKind = supportedHierarchyKinds.iterator().next();

        makeSetHierarchyKindActions();
    }

    private void makeSetHierarchyKindActions()
    {
        int size = supportedHierarchyKinds.size();
        if (size > 1)
        {
            setHierarchyKindActions = new SetHierarchyKindAction[size];
            int i = 0;
            for (CallHierarchyKind kind : supportedHierarchyKinds)
                setHierarchyKindActions[i++] = new SetHierarchyKindAction(kind);
            setHierarchyKindActions[0].setChecked(true);
        }
    }

    /**
     * Returns whether the given elements are possible input elements for this view.
     * This method invokes {@link #isPossibleInputElement(Object)} for each of
     * the given elements until all elements have been checked (in which case
     * it returns <code>true</code>) or <code>false</code> is returned for
     * an element (in which case it returns <code>false</code>).
     *
     * @param elements may be <code>null</code> or may contain null elements,
     *  in which case <code>false</code> will be returned; may be empty,
     *  in which case <code>true</code> will be returned
     * @return <code>true</code> if the given elements are possible input elements
     *  for this view, and <code>false</code> otherwise
     */
    public final boolean arePossibleInputElements(Object[] elements)
    {
        if (elements == null)
            return false;
        for (Object element : elements)
        {
            if (!isPossibleInputElement(element))
                return false;
        }
        return true;
    }

    /**
     * Returns whether the given element is a possible input element for this view.
     *
     * @param element may be <code>null</code>, in which case <code>false</code>
     *  will be returned
     * @return <code>true</code> if the given element is a possible input element
     *  for this view, and <code>false</code> otherwise
     * @see #arePossibleInputElements(Object[])
     */
    protected abstract boolean isPossibleInputElement(Object element);

    /**
     * Sets the current input elements for this view. Clients <b>must not</b>
     * modify the given array afterwards.
     * <p>
     * Default implementation invokes {@link #refresh()} after the input elements
     * have been set.
     * </p>
     *
     * @param elements not <code>null</code>, must not contain null elements;
     *  may be empty
     * @throws IllegalArgumentException if {@link #arePossibleInputElements(Object[])}
     *  returns <code>false</code> for the given elements
     */
    public void setInputElements(Object[] elements)
    {
        if (!arePossibleInputElements(elements))
            throw new IllegalArgumentException(Arrays.toString(elements));

        inputElements = elements;
        if (elements.length > 0)
            addHistoryEntry(createHistoryEntry(elements));
        refresh();
    }

    /**
     * Returns the current input elements for this view.
     *
     * @return the current input elements (never <code>null</code>,
     *  may be empty). Clients <b>must not</b> modify the returned array.
     */
    public final Object[] getInputElements()
    {
        return inputElements;
    }

    /**
     * Returns the current hierarchy kind for this view.
     * If the hierarchy kind has not been explicitly set,
     * returns the first of the supported hierarchy kinds.
     *
     * @return the current hierarchy kind (never <code>null</code>)
     * @see #setHierarchyKind(CallHierarchyKind)
     */
    public final CallHierarchyKind getHierarchyKind()
    {
        return hierarchyKind;
    }

    /**
     * Sets the current hierarchy kind for this view.
     * <p>
     * Default implementation invokes {@link #refresh()} if the view
     * hierarchy kind has changed; it also adjusts the checked state of the
     * 'set hierarchy kind' actions accordingly.
     * </p>
     *
     * @param kind not <code>null</code>
     * @throws IllegalArgumentException if the given kind is not {@link
     *  #supportsHierarchyKind(CallHierarchyKind) supported} by this view
     */
    public void setHierarchyKind(CallHierarchyKind kind)
    {
        if (!supportsHierarchyKind(kind))
            throw new IllegalArgumentException();
        if (kind == hierarchyKind)
            return;
        hierarchyKind = kind;
        for (SetHierarchyKindAction action : setHierarchyKindActions)
            action.setChecked(action.kind == kind);
        refresh();
    }

    /**
     * Returns whether this view supports the given hierarchy kind.
     *
     * @param kind may be <code>null</code>, in which case <code>false</code>
     *  will be returned
     * @return <code>true</code> if this view supports the given kind,
     *  and <code>false</code> otherwise
     */
    public final boolean supportsHierarchyKind(CallHierarchyKind kind)
    {
        return supportedHierarchyKinds.contains(kind);
    }

    /**
     * Sets the orientation of this view, which must be one of the constants
     * {@link SWT#HORIZONTAL} or {@link SWT#VERTICAL}. This method can also
     * be called with <code>SWT.HORIZONTAL|SWT.VERTICAL</code> for automatic
     * orientation.
     *
     * @param orientation new orientation
     */
    public void setOrientation(int orientation)
    {
        if (!supportsOrientation(orientation))
            throw new IllegalArgumentException();
        if (orientation == this.orientation)
            return;
        this.orientation = orientation;
        for (SetOrientationAction action : setOrientationActions)
            action.setChecked(action.orientation == orientation);
        adjustOrientation();
    }

    private static boolean supportsOrientation(int orientation)
    {
        return (orientation == SWT.HORIZONTAL || orientation == SWT.VERTICAL
            || orientation == ORIENTATION_AUTO);
    }

    private void adjustOrientation()
    {
        if (sashForm == null || sashForm.isDisposed())
            return;
        Point size = sashForm.getParent().getParent().getSize();
        if (size.x == 0 || size.y == 0)
            return;

        int orientation = this.orientation;
        if (orientation == ORIENTATION_AUTO)
            orientation = (size.x > size.y) ? SWT.HORIZONTAL : SWT.VERTICAL;

        if (sashForm.getOrientation() == orientation && orientationAdjusted)
            return;

        if (sashForm.getOrientation() != orientation)
        {
            if (orientationAdjusted)
                saveSplitterRatio();
            sashForm.setOrientation(orientation);
            sashForm.layout();
        }
        restoreSplitterRatio();
        orientationAdjusted = true;
    }

    private void saveSplitterRatio()
    {
        int[] weights = sashForm.getWeights();
        int ratio = (weights[0] * 1000) / (weights[0] + weights[1]);
        if (sashForm.getOrientation() == SWT.HORIZONTAL)
            horizontalRatio = ratio;
        else
            verticalRatio = ratio;
    }

    private void restoreSplitterRatio()
    {
        int ratio = (sashForm.getOrientation() == SWT.HORIZONTAL)
            ? horizontalRatio : verticalRatio;
        sashForm.setWeights(new int[] { ratio, 1000 - ratio });
    }

    /**
     * Marks this view as pinned.
     *
     * @param pinned whether the view is pinned
     */
    public void setPinned(boolean pinned)
    {
        this.pinned = pinned;
    }

    /**
     * Returns whether this view is pinned.
     *
     * @return <code>true</code> if the view is pinned,
     *  and <code>false</code> otherwise
     */
    public final boolean isPinned()
    {
        return pinned;
    }

    /**
     * Performs a full refresh of the content of this view.
     * <p>
     * The method implementation invokes {@link #refresh(IContext)}
     * with an empty context.
     * </p>
     */
    public final void refresh()
    {
        refresh(EMPTY_CONTEXT);
    }

    /**
     * Performs a refresh of the content of this view according to options
     * specified in the given context.
     * <p>
     * The method implementation does nothing if the SWT controls for this view
     * have not been created or have been disposed. Otherwise, it invokes
     * {@link #doRefresh(IContext)}.
     * </p>
     *
     * @param context the operation context (never <code>null</code>)
     */
    protected final void refresh(IContext context)
    {
        if (hierarchyViewer == null
            || hierarchyViewer.getControl().isDisposed())
            return;

        doRefresh(context);
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);
        if (memento != null)
        {
            if (supportedHierarchyKinds.size() > 1)
            {
                String value = memento.getString(KEY_HIERARCHY_KIND);
                if (value != null)
                {
                    CallHierarchyKind kind = null;
                    try
                    {
                        kind = CallHierarchyKind.valueOf(value);
                    }
                    catch (IllegalArgumentException e)
                    {
                    }
                    if (supportsHierarchyKind(kind))
                        setHierarchyKind(kind);
                }
            }

            Integer integer = memento.getInteger(KEY_ORIENTATION);
            if (integer != null)
            {
                int value = integer.intValue();
                if (supportsOrientation(value))
                    setOrientation(value);
            }

            integer = memento.getInteger(KEY_HORIZONTAL_RATIO);
            if (integer != null)
            {
                int value = integer.intValue();
                if (value > 0 && value < 1000)
                    horizontalRatio = value;
            }

            integer = memento.getInteger(KEY_VERTICAL_RATIO);
            if (integer != null)
            {
                int value = integer.intValue();
                if (value > 0 && value < 1000)
                    verticalRatio = value;
            }
        }
    }

    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        if (supportedHierarchyKinds.size() > 1)
            memento.putString(KEY_HIERARCHY_KIND, hierarchyKind.name());
        memento.putInteger(KEY_ORIENTATION, orientation);
        saveSplitterRatio(); // make sure to save the current splitter ratio
        memento.putInteger(KEY_HORIZONTAL_RATIO, horizontalRatio);
        memento.putInteger(KEY_VERTICAL_RATIO, verticalRatio);
    }

    @Override
    public void createPartControl(Composite parent)
    {
        getSite().getPage().addPartListener(partListener);

        parent.addControlListener(new ControlAdapter()
        {
            @Override
            public void controlResized(ControlEvent e)
            {
                if (!orientationAdjusted || orientation == ORIENTATION_AUTO)
                    adjustOrientation();
            }
        });

        pageBook = new PageBook(parent, SWT.NONE);

        noHierarchyPage = createNoHierarchyPage(pageBook);

        sashForm = createSashForm(pageBook);
        hierarchyViewer = createHierarchyViewer(sashForm);
        locationViewer = createLocationViewer(sashForm);

        configureHierarchyViewer(hierarchyViewer);
        configureLocationViewer(locationViewer);

        ConvertingSelectionProvider selectionProvider =
            new ConvertingSelectionProvider();
        selectionProvider.setDelegate(hierarchyViewer);
        getSite().setSelectionProvider(selectionProvider);

        selectionProvider.addSelectionChangedListener(e -> updateStatusLine(
            getViewSite().getActionBars().getStatusLineManager(),
            e.getStructuredSelection()));

        initContextMenu(hierarchyViewer.getControl(), (IMenuManager manager) ->
        {
            createHierarchyViewerMenuGroups(manager);
            fillHierarchyViewerMenu(manager);
        }, getSite().getId(), selectionProvider);
        initContextMenu(locationViewer.getControl(), (IMenuManager manager) ->
        {
            createLocationViewerMenuGroups(manager);
            fillLocationViewerMenu(manager);
        }, getSite().getId() + ".locationViewerMenu", locationViewer); //$NON-NLS-1$

        new OpenEditorHelper(hierarchyViewer);
        new OpenEditorHelper(locationViewer);

        hierarchyViewer.addSelectionChangedListener(
            e -> hierarchySelectionChanged(e.getSelection()));
        locationViewer.addSelectionChangedListener(
            e -> locationSelectionChanged(e.getSelection()));

        editorOpener = createEditorOpener();

        selectionProvider.addSelectionChangedListener(focusOnSelectionAction);

        addRefreshAction(refreshAction);

        getViewSite().getActionBars().setGlobalActionHandler(
            ActionFactory.REFRESH.getId(), refreshElementAction);

        hierarchyViewer.addSelectionChangedListener(removeFromViewAction);
        getViewSite().getActionBars().setGlobalActionHandler(
            ActionFactory.DELETE.getId(), removeFromViewAction);

        for (SetHierarchyKindAction action : setHierarchyKindActions)
            addSetHierarchyKindAction(action, action.kind);

        for (SetOrientationAction action : setOrientationActions)
            addSetOrientationAction(action, action.orientation);

        historyDropDownAction = createHistoryDropDownAction(
            new HistoryDropDownAction.History<HistoryEntry>()
            {
                @Override
                public List<HistoryEntry> getHistoryEntries()
                {
                    return getHistory();
                }

                @Override
                public void setHistoryEntries(List<HistoryEntry> entries)
                {
                    List<HistoryEntry> history = getHistory();
                    history.clear();
                    history.addAll(entries);
                    notifyHistoryUpdated();
                }

                @Override
                public HistoryEntry getActiveEntry()
                {
                    Object[] inputElements = getInputElements();
                    if (inputElements.length == 0)
                        return null;
                    return createHistoryEntry(inputElements);
                }

                @Override
                public void setActiveEntry(HistoryEntry entry)
                {
                    setInputElements(entry.getInputElements());
                }

                @Override
                public String getLabel(HistoryEntry entry)
                {
                    return entry.getLabel();
                }

                @Override
                public ImageDescriptor getImageDescriptor(HistoryEntry entry)
                {
                    return entry.getImageDescriptor();
                }
            });
        historyDropDownAction.setEnabled(!getHistory().isEmpty());
        addHistoryDropDownAction(historyDropDownAction);

        addPinAction(pinAction);
    }

    @Override
    public void dispose()
    {
        getSite().getPage().removePartListener(partListener);

        super.dispose();
    }

    @Override
    public void setFocus()
    {
        pageBook.setFocus();
        updateStatusLine(getViewSite().getActionBars().getStatusLineManager(),
            (IStructuredSelection)getSite().getSelectionProvider().getSelection());
    }

    /**
     * Returns a {@link CallHierarchyViewManager} for this view.
     * The same manager instance is returned for each call.
     *
     * @return the view manager (never <code>null</code>)
     */
    protected abstract CallHierarchyViewManager getViewManager();

    /**
     * Returns the root nodes of the current call hierarchy.
     * <p>
     * Default implementation invokes {@link #createHierarchyRoots(Object[])}
     * for the current input elements.
     * </p>
     *
     * @return the root nodes of the current call hierarchy
     *  (never <code>null</code>, may be empty)
     */
    protected ICallHierarchyNode[] getHierarchyRoots()
    {
        return createHierarchyRoots(inputElements);
    }

    /**
     * Creates and returns the root nodes for a call hierarchy based on the
     * given input elements and the current hierarchy kind.
     *
     * @param elements never <code>null</code>, may be empty
     * @return the created nodes (not <code>null</code>, may be empty)
     */
    protected abstract ICallHierarchyNode[] createHierarchyRoots(
        Object[] elements);

    /**
     * Returns a comparator for the hierarchy viewer.
     * <p>
     * Default implementation returns a {@link LabelComparator} if the current
     * hierarchy kind is {@link CallHierarchyKind#CALLER}, and <code>null</code>
     * otherwise.
     * </p>
     *
     * @return a {@link ViewerComparator}, or <code>null</code> for no sorting
     */
    protected ViewerComparator getHierarchyComparator()
    {
        if (getHierarchyKind() == CallHierarchyKind.CALLER)
            return new LabelComparator(); // sort caller hierarchy alphabetically

        return null;
    }

    /**
     * Computes the content description for this view.
     *
     * @return the computed content description (not <code>null</code>)
     * @see #getContentDescription()
     */
    protected abstract String computeContentDescription();

    /**
     * Updates the status line based on the given selection.
     * <p>
     * Default implementation clears the status line message if the selection
     * is empty; sets the message returned by {@link #getStatusLineMessage(Object)}
     * if exactly one element is selected; sets a generic message of the
     * form "(x) items selected" otherwise. The error message is cleared
     * in any case.
     * </p>
     *
     * @param manager the status line manager (never <code>null</code>)
     * @param selection the current selection (never <code>null</code>)
     */
    protected void updateStatusLine(IStatusLineManager manager,
        IStructuredSelection selection)
    {
        String message = null;
        int size = selection.size();
        if (size > 1)
        {
            message = MessageFormat.format(
                Messages.CallHierarchyViewPart_0__items_selected, size);
        }
        else if (size == 1)
        {
            message = getStatusLineMessage(selection.getFirstElement());
        }
        manager.setMessage(message);
        manager.setErrorMessage(null);
    }

    /**
     * Returns the message to show in the status line for the given element.
     * <p>
     * Default implementation always returns <code>null</code>.
     * </p>
     *
     * @param element the selected element (never <code>null</code>)
     * @return the status line message, or <code>null</code> if not available
     */
    protected String getStatusLineMessage(Object element)
    {
        return null;
    }

    /**
     * Refreshes the content of this view according to options specified in
     * the given context. This method may only be called after the SWT controls
     * for this view have been created and before they have been disposed.
     *
     * @param context the operation context (never <code>null</code>)
     */
    protected void doRefresh(IContext context)
    {
        setContentDescription(computeContentDescription());
        hierarchyViewer.setInput(null);
        locationViewer.setInput(null);
        if (inputElements.length > 0)
        {
            pageBook.showPage(sashForm);
            hierarchyViewer.setComparator(getHierarchyComparator());
            ICallHierarchyNode[] roots = getHierarchyRoots();
            hierarchyViewer.setInput(new CallHierarchy(hierarchyKind, roots));
            if (roots.length > 0)
                hierarchyViewer.setSelection(new StructuredSelection(roots[0]),
                    true);
            hierarchyViewer.getTree().setFocus();
            refreshAction.setEnabled(true);
            refreshElementAction.setEnabled(true);
        }
        else
        {
            pageBook.showPage(noHierarchyPage);
            pageBook.setFocus();
            refreshAction.setEnabled(false);
            refreshElementAction.setEnabled(false);
        }
    }

    /**
     * Creates and returns a control for the "no hierarchy" page.
     * This method is called once, when the part's control is created.
     * <p>
     * Default implementation returns a <code>Label</code> telling,
     * in general terms, that there is no call hierarchy to display.
     * Subclasses may override this method (e.g., to give details
     * on what the user needs to do to display a call hierarchy).
     * </p>
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created control (not <code>null</code>)
     */
    protected Control createNoHierarchyPage(Composite parent)
    {
        Label label = new Label(parent, SWT.LEAD + SWT.WRAP);
        label.setText(Messages.CallHierarchyViewPart_No_hierarchy_to_display);
        return label;
    }

    /**
     * Returns the parent {@link SashForm} for the hierarchy and location
     * viewers.
     *
     * @return the <code>SashForm</code>,
     *  or <code>null</code> if it has yet to be created
     * @see #createSashForm(Composite)
     */
    protected final SashForm getSashForm()
    {
        return sashForm;
    }

    /**
     * Creates and returns a {@link SashForm} that will be used as the parent
     * control for the hierarchy and location viewers. This method only creates
     * the control; it does not configure it. This method is called once,
     * when the part's control is created.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created control (not <code>null</code>)
     */
    protected SashForm createSashForm(Composite parent)
    {
        return new SashForm(parent, SWT.NONE);
    }

    /**
     * Returns the hierarchy tree viewer.
     *
     * @return the hierarchy tree viewer,
     *  or <code>null</code> if it has yet to be created
     * @see #createHierarchyViewer(Composite)
     */
    protected final TreeViewer getHierarchyViewer()
    {
        return hierarchyViewer;
    }

    /**
     * Creates and returns a tree viewer control that will be used for
     * displaying the call hierarchy. This method only creates the control;
     * it does not configure it. This method is called once, when the part's
     * control is created.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created control (not <code>null</code>)
     * @see #configureHierarchyViewer(TreeViewer)
     */
    protected TreeViewer createHierarchyViewer(Composite parent)
    {
        return new TreeViewer(parent, SWT.MULTI);
    }

    /**
     * Configures the newly created hierarchy viewer. This method has to set
     * as least a content provider and a label provider. This method is called
     * once, just after the viewer is created.
     * <p>
     * Default implementation sets a {@link CallHierarchyContentProvider}
     * as the content provider, and a {@link CallHierarchyLabelProvider}
     * backed by a {@link WorkbenchLabelProvider} as the label provider.
     * Subclasses usually need to extend this method and replace the default
     * label provider; they may also override this method completely, but
     * there is usually no need to.
     * </p>
     *
     * @param viewer the viewer to configure (never <code>null</code>)
     */
    protected void configureHierarchyViewer(TreeViewer viewer)
    {
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setUseHashlookup(true);
        viewer.setAutoExpandLevel(2);
        viewer.setContentProvider(new CallHierarchyContentProvider(this));
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new CallHierarchyLabelProvider(new WorkbenchLabelProvider())));
    }

    /**
     * Creates the menu groups for the hierarchy viewer's pop-up menu.
     * This method is called each time the pop-up menu is about to show,
     * just before {@link #fillHierarchyViewerMenu(IMenuManager)} is called.
     * <p>
     * Default implementation adds groups named {@link #GROUP_FOCUS} and
     * {@link IWorkbenchActionConstants#MB_ADDITIONS}. Subclasses may extend
     * or override this method, but should usually keep the default groups.
     * <p>
     *
     * @param manager the menu manager (never <code>null</code>)
     */
    protected void createHierarchyViewerMenuGroups(IMenuManager manager)
    {
        manager.add(new Separator(GROUP_FOCUS));
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Fills the pop-menu for the hierarchy viewer using the menu groups
     * created in {@link #createHierarchyViewerMenuGroups(IMenuManager)}.
     * This method is called each time the pop-up menu is about to show.
     * <p>
     * Default implementation appends generic actions such as
     * 'focus on selection', 'refresh element', and 'remove from view'.
     * Subclasses may extend or override this method.
     * </p>
     *
     * @param manager the menu manager (never <code>null</code>)
     */
    protected void fillHierarchyViewerMenu(IMenuManager manager)
    {
        if (focusOnSelectionAction.isEnabled())
            manager.appendToGroup(GROUP_FOCUS, focusOnSelectionAction);

        manager.appendToGroup(GROUP_FOCUS, refreshElementAction);

        if (removeFromViewAction.isEnabled())
            manager.appendToGroup(GROUP_FOCUS, removeFromViewAction);
    }

    /**
     * This method is called on each selection change in the hierarchy viewer.
     * <p>
     * Default implementation changes the input of the location viewer
     * accordingly and tries to reveal the selected hierarchy node in an
     * open editor. Subclasses may extend or even override this method, but
     * there is usually no need to.
     * </p>
     *
     * @param selection the new selection (never <code>null</code>)
     */
    protected void hierarchySelectionChanged(ISelection selection)
    {
        Object element = getSelectedElement(selection);
        if (!(element instanceof ICallHierarchyNode))
            locationViewer.setInput(null);
        else
        {
            ICallLocation[] callLocations =
                ((ICallHierarchyNode)element).getCallLocations();
            locationViewer.setInput(callLocations);
            if (callLocations.length > 0)
                locationViewer.setSelection(new StructuredSelection(
                    callLocations[0]), true);

            if (hierarchyViewer.getControl().isFocusControl())
            {
                try
                {
                    revealInEditor(element, false, false);
                }
                catch (PartInitException e)
                {
                    // cannot happen: may not open a new editor
                }
            }
        }
    }

    /**
     * Returns the location table viewer.
     *
     * @return the location table viewer,
     *  or <code>null</code> if it has yet to be created
     * @see #createLocationViewer(Composite)
     */
    protected final TableViewer getLocationViewer()
    {
        return locationViewer;
    }

    /**
     * Creates and returns a table viewer control that will be used for
     * displaying the call locations. This method only creates the control;
     * it does not configure it. This method is called once, when the part's
     * control is created.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created control (not <code>null</code>)
     * @see #configureLocationViewer(TableViewer)
     */
    protected TableViewer createLocationViewer(Composite parent)
    {
        return new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
            | SWT.FULL_SELECTION);
    }

    /**
     * Configures the newly created location viewer. This method has to set
     * as least a content provider and a label provider. This method is called
     * once, just after the viewer is created.
     * <p>
     * Default implementation sets an {@link ArrayContentProvider} as the
     * content provider, and a {@link LocationTableLabelProvider} as the
     * label provider. Also, it invokes {@link #createLocationColumns(Table)}.
     * Subclasses may extend or even override this method, but there is
     * usually no need to.
     * </p>
     *
     * @param viewer the viewer to configure (never <code>null</code>)
     */
    protected void configureLocationViewer(TableViewer viewer)
    {
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LocationTableLabelProvider());
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        createLocationColumns(table);
    }

    /**
     * Creates the table columns in the location viewer.
     * <p>
     * Default implementation creates the columns based on descriptions
     * returned by {@link #getLocationColumnDescriptions()}. Subclasses
     * may override this method, but there is usually no need to.
     * </p>
     *
     * @param table the table to create columns in (never <code>null</code>)
     */
    protected void createLocationColumns(Table table)
    {
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        ColumnDescription[] columnDescriptions =
            getLocationColumnDescriptions();
        for (ColumnDescription columnDescription : columnDescriptions)
        {
            ColumnLayoutData layoutData = columnDescription.getLayoutData();
            layout.addColumnData(layoutData);
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setResizable(layoutData.resizable);
            String header = columnDescription.getHeader();
            if (header != null)
                column.setText(header);
        }
    }

    /**
     * Returns the column descriptions for the call location table.
     * <p>
     * Default implementation returns descriptions for the 'icon' column,
     * the 'line number' column, and the 'call info' column. Subclasses
     * may override this method, but there is usually no need to.
     * </p>
     *
     * @return the column descriptions (not <code>null</code>)
     */
    protected ColumnDescription[] getLocationColumnDescriptions()
    {
        ColumnDescription iconColumn = new ColumnDescription(null,
            new ColumnPixelData(18, false, true));

        ColumnDescription lineColumn = new ColumnDescription(
            Messages.CallHierarchyViewPart_Line_column_header,
            new ColumnWeightData(60));

        ColumnDescription infoColumn = new ColumnDescription(
            Messages.CallHierarchyViewPart_Info_column_header,
            new ColumnWeightData(300));

        return new ColumnDescription[] { iconColumn, lineColumn, infoColumn };
    }

    /**
     * Creates the menu groups for the location viewer's pop-up menu.
     * This method is called each time the pop-up menu is about to show,
     * just before {@link #fillLocationViewerMenu(IMenuManager)} is called.
     * <p>
     * Default implementation adds a group named {@link
     * IWorkbenchActionConstants#MB_ADDITIONS}. Subclasses may extend
     * or override this method.
     * <p>
     *
     * @param manager the menu manager (never <code>null</code>)
     */
    protected void createLocationViewerMenuGroups(IMenuManager manager)
    {
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Fills the pop-menu for the location viewer using the menu groups
     * created in {@link #createLocationViewerMenuGroups(IMenuManager)}.
     * This method is called each time the pop-up menu is about to show.
     * <p>
     * Default implementation does nothing. Subclasses may extend or override
     * this method.
     * </p>
     *
     * @param manager the menu manager (never <code>null</code>)
     */
    protected void fillLocationViewerMenu(IMenuManager manager)
    {
    }

    /**
     * This method is called on each selection change in the location viewer.
     * <p>
     * Default implementation tries to reveal the selected call location in an
     * open editor. Subclasses may extend or even override this method, but
     * there is usually no need to.
     * </p>
     *
     * @param selection the new selection (never <code>null</code>)
     */
    protected void locationSelectionChanged(ISelection selection)
    {
        Object element = getSelectedElement(selection);
        if (!(element instanceof ICallLocation))
            return;
        if (locationViewer.getControl().isFocusControl())
        {
            try
            {
                revealInEditor(element, false, false);
            }
            catch (PartInitException e)
            {
                // cannot happen: may not open a new editor
            }
        }
    }

    /**
     * Reveals the given element in an editor on a best effort basis.
     * <p>
     * Default implementation uses the {@link #getEditorOpener()
     * editor opener} and specifically supports revealing an {@link
     * ICallLocation} and an {@link ICallHierarchyNode} (other elements
     * are handled generically). To reveal a call location in an open
     * editor, it invokes {@link #revealCallLocation(IEditorPart,
     * ICallLocation, IContext) revealCallLocation}.
     * </p>
     *
     * @param element not <code>null</code>
     * @param activate whether to activate the editor
     * @param mayOpenNewEditor whether a new editor may be opened
     *  when the element cannot be revealed in an existing editor
     * @throws PartInitException if opening the new editor failed
     */
    protected void revealInEditor(Object element, boolean activate,
        boolean mayOpenNewEditor) throws PartInitException
    {
        ICallLocation callLocation = null;
        if (element instanceof ICallLocation)
        {
            callLocation = (ICallLocation)element;
            element = callLocation.getCaller();
        }
        else if (element instanceof ICallHierarchyNode)
        {
            ICallHierarchyNode node = (ICallHierarchyNode)element;
            ICallLocation[] callLocations = node.getCallLocations();
            if (callLocations.length == 0)
                element = node.getElement();
            else
            {
                callLocation = callLocations[0];
                element = callLocation.getCaller();
            }
        }
        EditorUtility editorUtility = editorOpener.getEditorUtility();
        if (editorUtility.getEditorInput(element) == null)
            return;
        IEditorPart editor = null;
        if (mayOpenNewEditor)
            editor = editorOpener.open(element, activate, callLocation == null);
        else
        {
            IWorkbenchPage page = editorOpener.getWorkbenchPage();
            IEditorReference editorRef = editorUtility.findEditor(page,
                element);
            if (editorRef != null)
            {
                editor = editorRef.getEditor(true);
                if (activate)
                    page.activate(editor);
                else
                    page.bringToTop(editor);
                if (callLocation == null)
                    editorUtility.revealElement(editor, element);
            }
        }
        if (editor != null && callLocation != null)
            revealCallLocation(editor, callLocation, EMPTY_CONTEXT);
    }

    /**
     * Attempts to reveal the given call location in the given editor.
     * <p>
     * Default implementation uses the {@link EditorOpener#getEditorUtility()
     * editor utility} of the {@link #getEditorOpener() editor opener}
     * for revealing the call range in the given editor and invokes {@link
     * #handleCannotRevealCallLocation(IEditorPart, ICallLocation, IContext)
     * handleCannotRevealCallLocation} if the call location cannot be revealed
     * for whatever reason.
     * </p>
     *
     * @param editor never <code>null</code>
     * @param callLocation never <code>null</code>
     * @param context never <code>null</code>
     */
    protected void revealCallLocation(IEditorPart editor,
        ICallLocation callLocation, IContext context)
    {
        TextRange callRange = callLocation.getCallRange();
        if (callRange == null)
            handleCannotRevealCallLocation(editor, callLocation, context);
        else
        {
            EditorUtility editorUtility = editorOpener.getEditorUtility();
            try
            {
                editorUtility.revealTextRange(editor, callRange.getOffset(),
                    callRange.getLength(), callLocation.getSnapshot());
            }
            catch (StaleSnapshotException e)
            {
                handleCannotRevealCallLocation(editor, callLocation, context);
            }
        }
    }

    /**
     * Handles the case when a call location cannot be revealed.
     * <p>
     * Default implementation displays a generic error message
     * on the status line and attempts to reveal the caller element
     * in the given editor.
     * </p>
     *
     * @param editor never <code>null</code>
     * @param callLocation never <code>null</code>
     * @param context never <code>null</code>
     */
    protected void handleCannotRevealCallLocation(IEditorPart editor,
        ICallLocation callLocation, IContext context)
    {
        getViewSite().getActionBars().getStatusLineManager().setErrorMessage(
            Messages.CallHierarchyViewPart_Cannot_reveal_call_location);

        Object element = callLocation.getCaller();
        if (element != null)
            editorOpener.getEditorUtility().revealElement(editor, element);
    }

    /**
     * Returns the editor opener used by this view.
     *
     * @return the editor opener,
     *  or <code>null</code> if it has yet to be created
     * @see #createEditorOpener()
     */
    protected final EditorOpener getEditorOpener()
    {
        return editorOpener;
    }

    /**
     * Creates and returns an editor opener for this view.
     * This method is called once, when the part's control is created.
     * <p>
     * Subclasses may override this method if they require a specific
     * editor opener.
     * </p>
     *
     * @return the created editor opener (not <code>null</code>)
     */
    protected EditorOpener createEditorOpener()
    {
        return new EditorOpener(getSite().getPage(),
            DefaultEditorUtility.INSTANCE);
    }

    /**
     * Returns the 'focus on selection' action used by this view.
     *
     * @return the 'focus on selection' action
     */
    protected final IAction getFocusOnSelectionAction()
    {
        return focusOnSelectionAction;
    }

    /**
     * Returns the 'refresh element' action used by this view.
     *
     * @return the 'refresh element' action
     */
    protected final IAction getRefreshElementAction()
    {
        return refreshElementAction;
    }

    /**
     * Returns the 'remove from view' action used by this view.
     *
     * @return the 'remove from view' action
     */
    protected final IAction getRemoveFromViewAction()
    {
        return removeFromViewAction;
    }

    /**
     * Contributes the 'refresh' action to this view.
     * This method is called once, when the part's control is created.
     * <p>
     * Default implementation adds the given action to the view tool bar.
     * Subclasses may extend or override this method.
     * </p>
     *
     * @param action the 'refresh' action (never <code>null</code>)
     */
    protected void addRefreshAction(IAction action)
    {
        getViewSite().getActionBars().getToolBarManager().add(action);
    }

    /**
     * Contributes a 'set hierarchy kind' action to this view. This method
     * is called once for each of the 'set hierarchy kind' actions, when the
     * part's control is created. If this view supports only one hierarchy kind,
     * no 'set hierarchy kind' action is created and this method is not called.
     * <p>
     * Default implementation adds the given action to the view tool bar
     * as well as to the view menu. Subclasses may extend or override
     * this method.
     * </p>
     *
     * @param action a 'set hierarchy kind' action
     *  (never <code>null</code>)
     * @param kind the hierarchy kind set by the given action
     *  (never <code>null</code>)
     */
    protected void addSetHierarchyKindAction(IAction action,
        CallHierarchyKind kind)
    {
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.getToolBarManager().add(action);
        actionBars.getMenuManager().add(action);
    }

    /**
     * Contributes a 'set orientation' action to this view. This method
     * is called once for each of the 'set orientation' actions, when the
     * part's control is created.
     * <p>
     * Default implementation adds the given action to the 'Layout' sub-menu
     * of the view menu. The sub-menu is created if necessary. Subclasses may
     * extend or override this method.
     * </p>
     *
     * @param action a 'set orientation' action
     *  (never <code>null</code>)
     * @param orientation the orientation set by the given action
     */
    protected void addSetOrientationAction(IAction action, int orientation)
    {
        IActionBars actionBars = getViewSite().getActionBars();
        IMenuManager viewMenu = actionBars.getMenuManager();
        String id = "layout"; //$NON-NLS-1$
        IMenuManager layoutSubMenu = viewMenu.findMenuUsingPath(id);
        if (layoutSubMenu == null)
        {
            viewMenu.add(new Separator());
            layoutSubMenu = new MenuManager(
                Messages.CallHierarchyViewPart_Layout_menu, id);
            viewMenu.add(layoutSubMenu);
        }
        layoutSubMenu.add(action);
    }

    /**
     * Creates and returns a 'show history list' action for this view.
     * This method is called once, when the part's control is created.
     * <p>
     * Subclasses need to override this method if they extend
     * {@link HistoryDropDownAction}.
     * </p>
     *
     * @param history never <code>null</code>
     * @return the created action (not <code>null</code>)
     */
    protected HistoryDropDownAction<HistoryEntry> createHistoryDropDownAction(
        HistoryDropDownAction.History<HistoryEntry> history)
    {
        return new HistoryDropDownAction<>(history);
    }

    /**
     * Contributes the 'show history list' action to this view.
     * This method is called once, when the part's control is created.
     * <p>
     * Default implementation adds the given action to the view tool bar.
     * Subclasses may extend or override this method.
     * </p>
     *
     * @param action the 'show history list' action (never <code>null</code>)
     */
    protected void addHistoryDropDownAction(IAction action)
    {
        getViewSite().getActionBars().getToolBarManager().add(action);
    }

    /**
     * Contributes the 'pin' action to this view.
     * This method is called once, when the part's control is created.
     * <p>
     * Default implementation adds the given action to the view tool bar.
     * Subclasses may extend or override this method.
     * </p>
     *
     * @param action the 'pin' action (never <code>null</code>)
     */
    protected void addPinAction(IAction action)
    {
        getViewSite().getActionBars().getToolBarManager().add(action);
    }

    /**
     * Returns the history used by this view. The history is represented by
     * a "live" list of history entries.
     * <p>
     * Default implementation returns a history that is shared between all
     * views managed by the same {@link #getViewManager() view manager}.
     * </p>
     *
     * @return the view history (never <code>null</code>)
     */
    protected List<HistoryEntry> getHistory()
    {
        return getViewManager().getViewHistory();
    }

    /**
     * Creates and returns a history entry for the given input elements.
     *
     * @param inputElements never <code>null</code>; never empty
     * @return the created history entry (not <code>null</code>)
     */
    protected abstract HistoryEntry createHistoryEntry(Object[] inputElements);

    /**
     * Notifies that the history has been updated by this view.
     * <p>
     * Default implementation calls {@link #historyUpdated()} for each view
     * managed by the same {@link #getViewManager() view manager}.
     * </p>
     *
     * @see #getHistory()
     */
    protected void notifyHistoryUpdated()
    {
        List<CallHierarchyViewPart> views = getViewManager().getViews();
        for (CallHierarchyViewPart view : views)
        {
            view.historyUpdated();
        }
    }

    /**
     * A callback that is invoked when the history has been updated.
     * <p>
     * Default implementation sets the enabled state of the 'show history list'
     * action according to whether the history is empty and, if the history
     * is empty, clears the view input.
     * </p>
     *
     * @see #getHistory()
     * @see #notifyHistoryUpdated()
     */
    protected void historyUpdated()
    {
        boolean empty = getHistory().isEmpty();
        historyDropDownAction.setEnabled(!empty);
        if (empty)
            setInputElements(NO_ELEMENTS);
    }

    private void addHistoryEntry(HistoryEntry entry)
    {
        List<HistoryEntry> history = getHistory();
        history.remove(entry);
        history.add(0, entry);
        notifyHistoryUpdated();
    }

    private void initContextMenu(Control parent, IMenuListener listener,
        String menuId, ISelectionProvider selectionProvider)
    {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(listener);
        Menu menu = manager.createContextMenu(parent);
        parent.setMenu(menu);
        getSite().registerContextMenu(menuId, manager, selectionProvider);
    }

    private static Object getSelectedElement(ISelection selection)
    {
        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection ss = (IStructuredSelection)selection;
            if (ss.size() == 1)
                return ss.getFirstElement();
        }
        return null;
    }

    /**
     * Represents an entry of the call hierarchy view history list.
     */
    protected abstract static class HistoryEntry
    {
        private final Object[] inputElements;

        /**
         * Constructs a history entry for the given input elements.
         * Clients <b>must not</b> modify the given array afterwards.
         *
         * @param inputElements never <code>null</code>; never empty
         */
        protected HistoryEntry(Object[] inputElements)
        {
            if (inputElements.length == 0)
                throw new AssertionError();
            this.inputElements = inputElements;
        }

        /**
         * Returns the input elements for this history entry.
         *
         * @return the input elements (never <code>null</code>; never empty).
         *  Clients <b>must not</b> modify the returned array.
         */
        public final Object[] getInputElements()
        {
            return inputElements;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HistoryEntry other = (HistoryEntry)obj;
            if (!Arrays.equals(inputElements, other.inputElements))
                return false;
            return true;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(inputElements);
            return result;
        }

        /**
         * Returns a user-readable text label for this history entry.
         * <p>
         * Default implementation invokes {@link #getElementLabel(Object)}
         * to obtain a label for an input element.
         * </p>
         *
         * @return the text label of the history entry (never <code>null</code>)
         */
        public String getLabel()
        {
            switch (inputElements.length)
            {
            case 1:
                return MessageFormat.format(
                    Messages.CallHierarchyViewPart_History_entry_label__0,
                    getElementLabel(inputElements[0]));
            case 2:
                return MessageFormat.format(
                    Messages.CallHierarchyViewPart_History_entry_label__0__1,
                    getElementLabel(inputElements[0]), getElementLabel(
                        inputElements[1]));
            default:
                return MessageFormat.format(
                    Messages.CallHierarchyViewPart_History_entry_label__0__1_more,
                    getElementLabel(inputElements[0]), getElementLabel(
                        inputElements[1]));
            }
        }

        /**
         * Returns a user-readable text label for the given element.
         *
         * @param element a given element
         * @return the text label of the element (never <code>null</code>)
         */
        protected abstract String getElementLabel(Object element);

        /**
         * Returns an image descriptor for this history entry.
         * <p>
         * Default implementation always returns <code>null</code>.
         * </p>
         *
         * @return the image descriptor of the history entry
         *  (may be <code>null</code>)
         */
        public ImageDescriptor getImageDescriptor()
        {
            return null;
        }
    }

    private class FocusOnSelectionAction
        extends BaseSelectionListenerAction
    {
        FocusOnSelectionAction()
        {
            super(
                Messages.CallHierarchyViewPart_Focus_on_selection_action_text);
            setToolTipText(
                Messages.CallHierarchyViewPart_Focus_on_selection_action_tooltip);
        }

        @Override
        public void run()
        {
            setInputElements(getStructuredSelection().toArray());
        }

        @Override
        protected boolean updateSelection(IStructuredSelection selection)
        {
            return !selection.isEmpty() && arePossibleInputElements(
                selection.toArray());
        }
    }

    private static class ConvertingSelectionProvider
        extends DelegatingSelectionProvider
    {
        @Override
        public ISelection getSelection()
        {
            return convert(super.getSelection());
        }

        @Override
        protected SelectionChangedEvent newSelectionChangedEvent(
            ISelection selection)
        {
            return super.newSelectionChangedEvent(convert(selection));
        }

        private ISelection convert(ISelection selection)
        {
            if (!(selection instanceof IStructuredSelection)
                || selection.isEmpty())
                return selection;
            Object[] elements = ((IStructuredSelection)selection).toArray();
            int length = elements.length;
            Object[] converted = new Object[length];
            for (int i = 0; i < length; i++)
                converted[i] = convert(elements[i]);
            return new StructuredSelection(converted);
        }

        private Object convert(Object element)
        {
            if (element instanceof ICallHierarchyNode)
                return ((ICallHierarchyNode)element).getElement();
            return element;
        }
    }

    private class OpenEditorHelper
        extends OpenAndLinkWithEditorHelper
    {
        OpenEditorHelper(StructuredViewer viewer)
        {
            super(viewer);
        }

        @Override
        protected void activate(ISelection selection)
        {
            Object element = getSelectedElement(selection);
            if (element != null)
            {
                try
                {
                    revealInEditor(element, true, false);
                }
                catch (PartInitException e)
                {
                    // cannot happen: may not open a new editor
                }
            }
        }

        @Override
        protected void open(ISelection selection, boolean activate)
        {
            Object element = getSelectedElement(selection);
            if (element != null)
            {
                try
                {
                    revealInEditor(element, activate, true);
                }
                catch (PartInitException e)
                {
                    ErrorDialog.openError(getSite().getShell(),
                        Messages.CallHierarchyViewPart_Show_call_location,
                        Messages.CallHierarchyViewPart_Error_opening_editor,
                        e.getStatus());
                }
            }
        }
    }

    private class RefreshAction
        extends Action
    {
        RefreshAction()
        {
            setText(Messages.CallHierarchyViewPart_Refresh_action_text);
            setToolTipText(
                Messages.CallHierarchyViewPart_Refresh_action_tooltip);
            setImageDescriptor(Activator.getImageDescriptor(
                Activator.IMG_ELCL_REFRESH));
            setDisabledImageDescriptor(Activator.getImageDescriptor(
                Activator.IMG_DLCL_REFRESH));
        }

        @Override
        public void run()
        {
            refresh();
        }
    }

    private class RefreshElementAction
        extends Action
    {
        RefreshElementAction()
        {
            setText(Messages.CallHierarchyViewPart_Refresh_element_action_text);
            setToolTipText(
                Messages.CallHierarchyViewPart_Refresh_element_action_tooltip);
            setImageDescriptor(Activator.getImageDescriptor(
                Activator.IMG_ELCL_REFRESH));
            setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);
        }

        @Override
        public void run()
        {
            IStructuredSelection selection =
                hierarchyViewer.getStructuredSelection();
            if (selection.isEmpty())
                refresh();
            else
            {
                Iterator<?> it = selection.iterator();
                while (it.hasNext())
                {
                    Object e = it.next();
                    if (e instanceof ICallHierarchyNode)
                        ((ICallHierarchyNode)e).refresh();
                    hierarchyViewer.refresh(e);
                }
            }
        }
    }

    private class RemoveFromViewAction
        extends BaseSelectionListenerAction
    {
        RemoveFromViewAction()
        {
            super(Messages.CallHierarchyViewPart_Remove_from_view_action_text);
            setToolTipText(
                Messages.CallHierarchyViewPart_Remove_from_view_action_tooltip);
            ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
            setImageDescriptor(images.getImageDescriptor(
                ISharedImages.IMG_ELCL_REMOVE));
            setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
        }

        @Override
        public void run()
        {
            Object[] elements = getStructuredSelection().toArray();
            hierarchyViewer.setSelection(StructuredSelection.EMPTY);
            hierarchyViewer.remove(elements);
        }

        @Override
        protected boolean updateSelection(IStructuredSelection selection)
        {
            return !selection.isEmpty();
        }
    }

    private class SetHierarchyKindAction
        extends Action
    {
        final CallHierarchyKind kind;

        SetHierarchyKindAction(CallHierarchyKind kind)
        {
            super(null, AS_RADIO_BUTTON);
            this.kind = kind;
            switch (kind)
            {
            case CALLER:
                setText(
                    Messages.CallHierarchyViewPart_Show_caller_hierarchy_action_text);
                setToolTipText(
                    Messages.CallHierarchyViewPart_Show_caller_hierarchy_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_CH_CALLERS));
                break;
            case CALLEE:
                setText(
                    Messages.CallHierarchyViewPart_Show_callee_hierarchy_action_text);
                setToolTipText(
                    Messages.CallHierarchyViewPart_Show_callee_hierarchy_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_CH_CALLEES));
                break;
            }
        }

        @Override
        public void run()
        {
            setHierarchyKind(kind);
        }
    }

    private class SetOrientationAction
        extends Action
    {
        final int orientation;

        SetOrientationAction(int orientation)
        {
            super(null, AS_RADIO_BUTTON);
            this.orientation = orientation;
            switch (orientation)
            {
            case SWT.HORIZONTAL:
                setText(
                    Messages.CallHierarchyViewPart_Layout_horizontal_action_text);
                setToolTipText(
                    Messages.CallHierarchyViewPart_Layout_horizontal_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_CH_HORIZONTAL));
                break;
            case SWT.VERTICAL:
                setText(
                    Messages.CallHierarchyViewPart_Layout_vertical_action_text);
                setToolTipText(
                    Messages.CallHierarchyViewPart_Layout_vertical_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_CH_VERTICAL));
                break;
            default:
                setText(
                    Messages.CallHierarchyViewPart_Layout_automatic_action_text);
                setToolTipText(
                    Messages.CallHierarchyViewPart_Layout_automatic_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_CH_AUTOMATIC));
                setChecked(true);
            }
        }

        @Override
        public void run()
        {
            setOrientation(orientation);
        }
    }

    private class PinAction
        extends Action
    {
        PinAction()
        {
            super(Messages.CallHierarchyViewPart_Pin_action_text,
                IAction.AS_CHECK_BOX);
            setToolTipText(Messages.CallHierarchyViewPart_Pin_action_tooltip);
            setImageDescriptor(Activator.getImageDescriptor(
                Activator.IMG_ELCL_PIN_VIEW));
        }

        @Override
        public void run()
        {
            setPinned(isChecked());
        }
    }
}
