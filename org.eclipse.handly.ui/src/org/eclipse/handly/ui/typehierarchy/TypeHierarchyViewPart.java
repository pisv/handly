/*******************************************************************************
 * Copyright (c) 2021 1C-Soft LLC.
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
package org.eclipse.handly.ui.typehierarchy;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.ui.DefaultEditorUtility;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.handly.ui.PartListenerAdapter;
import org.eclipse.handly.ui.action.HistoryDropDownAction;
import org.eclipse.handly.ui.viewer.DelegatingSelectionProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * An abstract base implementation of a type hierarchy view.
 *
 * @since 1.6 Provisional API
 */
public abstract class TypeHierarchyViewPart
    extends ViewPart
{
    /**
     * Pop-up menu: name of group for focus actions (value
     * <code>"group.focus"</code>).
     */
    protected static final String GROUP_FOCUS = "group.focus"; //$NON-NLS-1$

    private static final Object[] NO_ELEMENTS = new Object[0];

    private static final String KEY_HIERARCHY_KIND =
        "org.eclipse.handly.typehierarchy.view.hierarchyKind"; //$NON-NLS-1$
    private static final String KEY_LAYOUT_MODE =
        "org.eclipse.handly.typehierarchy.view.layoutMode"; //$NON-NLS-1$
    private static final String KEY_HORIZONTAL_WEIGHTS =
        "org.eclipse.handly.typehierarchy.view.horizontalWeights"; //$NON-NLS-1$
    private static final String KEY_VERTICAL_WEIGHTS =
        "org.eclipse.handly.typehierarchy.view.verticalWeights"; //$NON-NLS-1$

    private static final int LAYOUT_AUTOMATIC = SWT.HORIZONTAL | SWT.VERTICAL;

    private final Set<TypeHierarchyKind> supportedHierarchyKinds;
    private Set<TypeHierarchyKind> enabledHierarchyKinds;
    private TypeHierarchyKind hierarchyKind;
    private int layoutMode = getDefaultLayoutMode();
    private boolean layoutAdjusted;
    private int[] horizontalWeights, verticalWeights;
    private Object[] inputElements = NO_ELEMENTS;
    private final List<HistoryEntry> history = new ArrayList<>();
    private PageBook pageBook, hierarchyPageBook;
    private Control noHierarchyPage;
    private SashForm sashForm;
    private final Map<TypeHierarchyKind, TreeViewer> hierarchyViewers =
        new HashMap<>();
    private EditorOpener editorOpener;
    private SetHierarchyKindAction[] setHierarchyKindActions =
        new SetHierarchyKindAction[0];
    private SetLayoutModeAction[] setLayoutModeActions =
        new SetLayoutModeAction[0];
    private final FocusOnSelectionAction focusOnSelectionAction =
        new FocusOnSelectionAction();
    private HistoryDropDownAction<HistoryEntry> historyDropDownAction;
    private final RefreshAction refreshAction = new RefreshAction();

    private final IPartListener partListener = new PartListenerAdapter()
    {
        @Override
        public void partOpened(IWorkbenchPart part)
        {
            if (part != TypeHierarchyViewPart.this)
                return;

            refresh();
        };
    };

    /**
     * Constructs a type hierarchy view that supports all of the
     * type hierarchy kinds.
     *
     * @see TypeHierarchyKind
     * @see #TypeHierarchyViewPart(Set)
     */
    public TypeHierarchyViewPart()
    {
        this(EnumSet.allOf(TypeHierarchyKind.class));
    }

    /**
     * Constructs a type hierarchy view that supports the given
     * type hierarchy kinds. Clients <b>must not</b> modify the given set
     * afterwards.
     *
     * @param supportedHierarchyKinds not <code>null</code> and not empty
     */
    public TypeHierarchyViewPart(Set<TypeHierarchyKind> supportedHierarchyKinds)
    {
        if (supportedHierarchyKinds.isEmpty())
            throw new IllegalArgumentException();

        this.supportedHierarchyKinds = supportedHierarchyKinds;
        this.enabledHierarchyKinds = supportedHierarchyKinds;
        this.hierarchyKind = supportedHierarchyKinds.iterator().next();

        makeSetHierarchyKindActions();
        makeSetLayoutModeActions();
        refreshAction.setEnabled(false);
    }

    /**
     * Returns whether the given elements are possible input elements for this view.
     * <p>
     * Default implementation invokes {@link #isPossibleInputElement(Object)} for
     * each of the given elements until <code>false</code> is returned for an element
     * (in which case this method will return <code>false</code>) or all elements
     * have been checked (in which case it will return <code>true</code>).
     * </p>
     *
     * @param elements may be <code>null</code> or may contain null elements,
     *  in which case <code>false</code> will be returned; may be empty,
     *  in which case <code>true</code> will be returned
     * @return <code>true</code> if the given elements are possible input elements
     *  for this view, and <code>false</code> otherwise
     */
    public boolean arePossibleInputElements(Object[] elements)
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
     * Sets the current input elements for this view. Clients <b>must not</b>
     * modify the given array afterwards.
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

        Object[] oldInputElements = inputElements;

        inputElements = elements;

        refreshAction.setEnabled(elements.length > 0);

        if (elements.length > 0)
            addHistoryEntry(createHistoryEntry(elements));

        onInputElementsChanged(oldInputElements, elements);
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
     * Returns whether the given hierarchy kind is currently enabled for this view.
     *
     * @param kind may be <code>null</code>, in which case <code>false</code>
     *  will be returned
     * @return <code>true</code> if the given kind is currently enabled,
     *  and <code>false</code> otherwise
     */
    public final boolean isEnabledHierarchyKind(TypeHierarchyKind kind)
    {
        return enabledHierarchyKinds.contains(kind);
    }

    /**
     * Sets the current hierarchy kind for this view.
     *
     * @param kind not <code>null</code>
     * @throws IllegalArgumentException if the given kind is not currently {@link
     *  #isEnabledHierarchyKind(TypeHierarchyKind) enabled} for this view
     */
    public void setHierarchyKind(TypeHierarchyKind kind)
    {
        if (!isEnabledHierarchyKind(kind))
            throw new IllegalArgumentException();

        if (kind == hierarchyKind)
            return;

        TypeHierarchyKind oldKind = hierarchyKind;

        hierarchyKind = kind;

        for (SetHierarchyKindAction action : setHierarchyKindActions)
            action.setChecked(action.kind == kind);

        onHierarchyKindChanged(oldKind, kind);
    }

    /**
     * Returns the current hierarchy kind for this view.
     *
     * @return the current hierarchy kind (never <code>null</code>)
     * @see #setHierarchyKind(TypeHierarchyKind)
     */
    public final TypeHierarchyKind getHierarchyKind()
    {
        return hierarchyKind;
    }

    /**
     * Sets layout mode for this view, which may be one of the constants
     * {@link SWT#HORIZONTAL} or {@link SWT#VERTICAL}; this method may also
     * be called with <code>SWT.HORIZONTAL|SWT.VERTICAL</code> for automatic
     * layout or {@link SWT#NONE} for hierarchy-only layout.
     * <p>
     * Note that only a subset of the allowed values for layout mode may be
     * {@link #supportsLayoutMode(int) supported} by the view.
     * </p>
     *
     * @param layoutMode the given layout mode
     * @throws IllegalArgumentException if the given layout mode is not
     *  {@link #supportsLayoutMode(int) supported} by the view
     */
    public void setLayoutMode(int layoutMode)
    {
        if (!supportsLayoutMode(layoutMode))
            throw new IllegalArgumentException();

        if (layoutMode == this.layoutMode)
            return;

        int oldLayoutMode = this.layoutMode;

        this.layoutMode = layoutMode;

        for (SetLayoutModeAction action : setLayoutModeActions)
            action.setChecked(action.layoutMode == layoutMode);

        onLayoutModeChanged(oldLayoutMode, layoutMode);
    }

    /**
     * Returns the current layout mode for this view.
     *
     * @return the current layout mode
     * @see #setLayoutMode(int)
     */
    public final int getLayoutMode()
    {
        return layoutMode;
    }

    /**
     * Returns <code>true</code> if the given value represents a supported
     * layout mode for this view, and <code>false</code> otherwise.
     * The set of the supported layout mode values does not change over
     * the lifetime of the view and is never empty.
     * <p>
     * Default implementation only supports {@link SWT#NONE}, which represents
     * hierarchy-only layout.
     * </p>
     *
     * @param layoutMode the layout mode to check
     * @return <code>true</code> if the given value represents a supported
     *  layout mode for this view, and <code>false</code> otherwise
     */
    public boolean supportsLayoutMode(int layoutMode)
    {
        return layoutMode == SWT.NONE;
    }

    /**
     * Performs a full refresh of the content of this view.
     */
    public final void refresh()
    {
        refresh(EMPTY_CONTEXT);
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
                    TypeHierarchyKind kind = null;
                    try
                    {
                        kind = TypeHierarchyKind.valueOf(value);
                    }
                    catch (IllegalArgumentException e)
                    {
                    }
                    if (supportedHierarchyKinds.contains(kind))
                        setHierarchyKind(kind);
                }
            }

            Integer layoutMode = memento.getInteger(KEY_LAYOUT_MODE);
            if (layoutMode != null)
            {
                int value = layoutMode.intValue();
                if (supportsLayoutMode(value))
                    setLayoutMode(value);
            }

            try
            {
                horizontalWeights = toIntArray(memento.getString(
                    KEY_HORIZONTAL_WEIGHTS));
            }
            catch (NumberFormatException e)
            {
            }

            try
            {
                verticalWeights = toIntArray(memento.getString(
                    KEY_VERTICAL_WEIGHTS));
            }
            catch (NumberFormatException e)
            {
            }
        }
    }

    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        if (supportedHierarchyKinds.size() > 1)
            memento.putString(KEY_HIERARCHY_KIND, hierarchyKind.name());
        memento.putInteger(KEY_LAYOUT_MODE, layoutMode);
        saveSashFormWeights();
        memento.putString(KEY_HORIZONTAL_WEIGHTS, toString(horizontalWeights));
        memento.putString(KEY_VERTICAL_WEIGHTS, toString(verticalWeights));
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
                if (!layoutAdjusted || layoutMode == LAYOUT_AUTOMATIC)
                    adjustLayout(layoutMode);
            }
        });

        DelegatingSelectionProvider selectionProvider =
            new DelegatingSelectionProvider();

        getSite().setSelectionProvider(selectionProvider);

        selectionProvider.addSelectionChangedListener(e ->
        {
            ISelection selection = e.getSelection();
            if (selection instanceof IStructuredSelection)
                updateStatusLine(
                    getViewSite().getActionBars().getStatusLineManager(),
                    (IStructuredSelection)selection);
        });

        pageBook = new PageBook(parent, SWT.NONE);

        noHierarchyPage = createNoHierarchyPage(pageBook);

        sashForm = new SashForm(pageBook, SWT.NONE);

        hierarchyPageBook = new PageBook(sashForm, SWT.NONE);

        if (layoutMode == SWT.NONE)
            sashForm.setMaximizedControl(hierarchyPageBook);

        for (TypeHierarchyKind kind : supportedHierarchyKinds)
        {
            TreeViewer hierarchyViewer = createHierarchyViewer(
                hierarchyPageBook, kind);

            configureHierarchyViewer(hierarchyViewer, kind);

            initContextMenu(hierarchyViewer.getControl(), (
                IMenuManager manager) ->
            {
                createHierarchyViewerMenuGroups(manager, kind);
                fillHierarchyViewerMenu(manager, kind);
            }, getSite().getId(), selectionProvider);

            new OpenEditorHelper(hierarchyViewer);

            hierarchyViewer.getControl().addFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                    selectionProvider.setDelegate(hierarchyViewer);
                    onHierarchySelectionChanged(hierarchyViewer.getSelection(),
                        kind);
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                    selectionProvider.setDelegate(null);
                }
            });

            hierarchyViewer.addSelectionChangedListener(
                e -> onHierarchySelectionChanged(e.getSelection(), kind));

            hierarchyViewers.put(kind, hierarchyViewer);
        }

        editorOpener = createEditorOpener();

        selectionProvider.addSelectionChangedListener(focusOnSelectionAction);

        addRefreshAction(refreshAction);

        getViewSite().getActionBars().setGlobalActionHandler(
            ActionFactory.REFRESH.getId(), refreshAction);

        for (SetHierarchyKindAction action : setHierarchyKindActions)
            addSetHierarchyKindAction(action, action.kind);

        for (SetLayoutModeAction action : setLayoutModeActions)
            addSetLayoutModeAction(action, action.layoutMode);

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
                    onHistoryChanged();
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
     * Creates and returns a history entry for the given input elements.
     *
     * @param inputElements never <code>null</code>; never empty
     * @return the created history entry (not <code>null</code>)
     */
    protected abstract HistoryEntry createHistoryEntry(Object[] inputElements);

    /**
     * Computes the content description for this view.
     *
     * @return the computed content description (not <code>null</code>)
     * @see #getContentDescription()
     */
    protected abstract String computeContentDescription();

    /**
     * Returns whether the view has already been opened and has not yet been
     * closed.
     *
     * @return <code>true</code> if the view is currently open,
     *  and <code>false</code> otherwise
     */
    protected final boolean isOpen()
    {
        return pageBook != null && !pageBook.isDisposed();
    }

    /**
     * Performs a refresh of the content of this view according to options
     * specified in the given context.
     *
     * @param context the operation context (never <code>null</code>)
     */
    protected void refresh(IContext context)
    {
        if (!isOpen())
            return;

        setContentDescription(computeContentDescription());

        for (Map.Entry<TypeHierarchyKind, TreeViewer> entry : hierarchyViewers.entrySet())
            setHierarchyViewerInput(entry.getValue(), entry.getKey());

        updateHierarchyPage();
    }

    /**
     * Ensures that the appropriate hierarchy page is shown by this view.
     * <p>
     * This method may only be called after the SWT controls for this view
     * have been created and before they have been disposed.
     * </p>
     */
    protected void updateHierarchyPage()
    {
        if (inputElements.length > 0)
        {
            TreeViewer hierarchyViewer = hierarchyViewers.get(hierarchyKind);
            Control hierarchyPage = hierarchyViewer.getControl();
            pageBook.showPage(sashForm);
            hierarchyPageBook.showPage(hierarchyPage);
            hierarchyPage.setFocus();
        }
        else
        {
            pageBook.showPage(noHierarchyPage);
            pageBook.setFocus();
        }
    }

    /**
     * A callback that is invoked when the view input elements change.
     *
     * @param oldInputElements never <code>null</code>
     * @param newInputElements never <code>null</code>
     */
    protected void onInputElementsChanged(Object[] oldInputElements,
        Object[] newInputElements)
    {
        refresh();
    }

    /**
     * A callback that is invoked when the current hierarchy kind changes.
     *
     * @param oldKind never <code>null</code>
     * @param newKind never <code>null</code>
     */
    protected void onHierarchyKindChanged(TypeHierarchyKind oldKind,
        TypeHierarchyKind newKind)
    {
        if (!isOpen())
            return;

        updateHierarchyPage();
    }

    /**
     * Returns a set of the supported hierarchy kinds for this view.
     *
     * @return the supported hierarchy kinds (never <code>null</code>,
     *  never empty)
     */
    protected final Set<TypeHierarchyKind> getSupportedHierarchyKinds()
    {
        return supportedHierarchyKinds;
    }

    /**
     * Sets the enabled hierarchy kinds for this view. Note that at least one
     * hierarchy kind must be enabled. Clients <b>must not</b> modify the given
     * set afterwards.
     *
     * @param kinds not <code>null</code> and not empty
     */
    protected void setEnabledHierarchyKinds(Set<TypeHierarchyKind> kinds)
    {
        if (kinds.isEmpty())
            throw new IllegalArgumentException();

        if (!supportedHierarchyKinds.containsAll(kinds))
            throw new IllegalArgumentException();

        if (enabledHierarchyKinds.equals(kinds))
            return;

        Set<TypeHierarchyKind> oldKinds = enabledHierarchyKinds;

        enabledHierarchyKinds = kinds;

        for (SetHierarchyKindAction action : setHierarchyKindActions)
            action.setEnabled(kinds.contains(action.kind));

        if (!kinds.contains(hierarchyKind))
            setHierarchyKind(kinds.iterator().next());

        onEnabledHierarchyKindsChanged(oldKinds, kinds);
    }

    /**
     * A callback that is invoked when the set of enabled hierarchy kinds changes.
     *
     * @param oldKinds never <code>null</code>
     * @param newKinds never <code>null</code>
     */
    protected void onEnabledHierarchyKindsChanged(
        Set<TypeHierarchyKind> oldKinds, Set<TypeHierarchyKind> newKinds)
    {
        if (!isOpen())
            return;

        for (TypeHierarchyKind kind : symmetricDifference(oldKinds, newKinds))
            setHierarchyViewerInput(hierarchyViewers.get(kind), kind);
    }

    /**
     * A callback that is invoked when the view layout mode changes.
     *
     * @param oldLayoutMode the old layout mode
     * @param newLayoutMode the new layout mode
     */
    protected void onLayoutModeChanged(int oldLayoutMode, int newLayoutMode)
    {
        if (!isOpen())
            return;

        sashForm.setMaximizedControl(newLayoutMode == SWT.NONE
            ? hierarchyPageBook : null);
        adjustLayout(newLayoutMode);
    }

    /**
     * Updates the status line based on the given selection in this view.
     * <p>
     * Default implementation clears the status line message if the selection
     * is empty or if exactly one element is selected; sets a generic message
     * of the form "(x) items selected" otherwise. It always clears the error
     * message.
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
                Messages.TypeHierarchyViewPart_0__items_selected, size);
        }
        manager.setMessage(message);
        manager.setErrorMessage(null);
    }

    /**
     * Creates and returns a control for the 'no hierarchy' page.
     * This method is called once, when the part's control is created.
     * <p>
     * Default implementation returns a <code>Label</code> telling,
     * in general terms, that there is no type hierarchy to display.
     * Subclasses may override this method (e.g., to give details
     * on what the user needs to do to display a type hierarchy).
     * </p>
     *
     * @param parent the parent composite (never <code>null</code>)
     * @return the created control (not <code>null</code>)
     */
    protected Control createNoHierarchyPage(Composite parent)
    {
        Label label = new Label(parent, SWT.LEAD + SWT.WRAP);
        label.setText(Messages.TypeHierarchyViewPart_No_hierarchy_to_display);
        return label;
    }

    /**
     * Returns the {@link SashForm} created for this view.
     *
     * @return the <code>SashForm</code>,
     *  or <code>null</code> if it has yet to be created
     */
    protected final SashForm getSashForm()
    {
        return sashForm;
    }

    /**
     * Returns the hierarchy tree viewer for the given hierarchy kind.
     *
     * @param kind the type hierarchy kind (never <code>null</code>)
     * @return the hierarchy tree viewer,
     *  or <code>null</code> if it has yet to be created
     * @see #createHierarchyViewer(Composite, TypeHierarchyKind)
     */
    protected final TreeViewer getHierarchyViewer(TypeHierarchyKind kind)
    {
        return hierarchyViewers.get(kind);
    }

    /**
     * Creates and returns a tree viewer that will be used for displaying
     * the type hierarchy of the given kind. This method only creates the viewer;
     * it does not configure it. This method is called once, when the part's
     * control is created.
     *
     * @param parent the parent composite (never <code>null</code>)
     * @param kind the type hierarchy kind (never <code>null</code>)
     * @return the created viewer (not <code>null</code>)
     * @see #configureHierarchyViewer(TreeViewer, TypeHierarchyKind)
     */
    protected TreeViewer createHierarchyViewer(Composite parent,
        TypeHierarchyKind kind)
    {
        return new TreeViewer(parent, SWT.SINGLE);
    }

    /**
     * Configures the newly created hierarchy viewer for the given hierarchy kind.
     * This method has to set as least a content provider and a label provider.
     * This method is called once, just after the viewer is created.
     *
     * @param viewer the viewer to configure (never <code>null</code>)
     * @param kind the type hierarchy kind (never <code>null</code>)
     */
    protected abstract void configureHierarchyViewer(TreeViewer viewer,
        TypeHierarchyKind kind);

    /**
     * Sets the input for the hierarchy viewer for the given hierarchy kind.
     *
     * @param viewer the viewer to set the input for (never <code>null</code>)
     * @param kind the type hierarchy kind (never <code>null</code>)
     */
    protected void setHierarchyViewerInput(TreeViewer viewer,
        TypeHierarchyKind kind)
    {
        viewer.setInput(inputElements.length > 0 && isEnabledHierarchyKind(kind)
            ? inputElements : null);
    }

    /**
     * Creates the menu groups for the pop-up menu of the hierarchy viewer of
     * the given hierarchy kind. This method is called each time the pop-up menu
     * is about to show, just before {@link #fillHierarchyViewerMenu} is called.
     * <p>
     * Default implementation adds groups named {@link #GROUP_FOCUS} and
     * {@link IWorkbenchActionConstants#MB_ADDITIONS}. Subclasses may extend
     * or override this method, but should usually keep the default groups.
     * <p>
     *
     * @param manager the menu manager (never <code>null</code>)
     * @param kind the type hierarchy kind (never <code>null</code>)
     */
    protected void createHierarchyViewerMenuGroups(IMenuManager manager,
        TypeHierarchyKind kind)
    {
        manager.add(new Separator(GROUP_FOCUS));
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Fills the pop-menu for the hierarchy viewer for the given hierarchy kind
     * using the menu groups created by {@link #createHierarchyViewerMenuGroups}.
     * This method is called each time the pop-up menu is about to show.
     * <p>
     * Default implementation adds generic actions such as 'focus on selection'.
     * Subclasses may extend or override this method.
     * </p>
     *
     * @param manager the menu manager (never <code>null</code>)
     * @param kind the type hierarchy kind (never <code>null</code>)
     */
    protected void fillHierarchyViewerMenu(IMenuManager manager,
        TypeHierarchyKind kind)
    {
        if (focusOnSelectionAction.isEnabled())
            manager.appendToGroup(GROUP_FOCUS, focusOnSelectionAction);
    }

    /**
     * A callback that is invoked when selection changes in the hierarchy viewer
     * for the given hierarchy kind.
     * <p>
     * Default implementation tries to reveal the selected hierarchy node in an
     * open editor with {@link #revealInEditor(Object, boolean, boolean)
     * revealInEditor}.
     * </p>
     *
     * @param selection the new selection (never <code>null</code>)
     * @param kind the type hierarchy kind (never <code>null</code>)
     */
    protected void onHierarchySelectionChanged(ISelection selection,
        TypeHierarchyKind kind)
    {
        Object element = getSelectedElement(selection);
        if (element != null)
        {
            if (hierarchyViewers.get(kind).getControl().isFocusControl())
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
     * Reveals the given element in an appropriate editor on a best effort basis.
     * <p>
     * Default implementation uses the {@link #getEditorOpener() editor opener}.
     * </p>
     *
     * @param element not <code>null</code>
     * @param activate whether to activate the editor
     * @param mayOpenNewEditor whether a new editor may be opened
     *  when the element cannot be revealed in an existing editor
     * @throws PartInitException if a new editor could not be created
     *  or initialized
     */
    protected void revealInEditor(Object element, boolean activate,
        boolean mayOpenNewEditor) throws PartInitException
    {
        EditorUtility editorUtility = editorOpener.getEditorUtility();
        if (editorUtility.getEditorInput(element) == null)
            return;

        if (mayOpenNewEditor)
            editorOpener.open(element, activate, true);
        else
        {
            IWorkbenchPage page = editorOpener.getWorkbenchPage();
            IEditorReference editorRef = editorUtility.findEditor(page,
                element);
            if (editorRef != null)
            {
                IEditorPart editor = editorRef.getEditor(true);
                if (editor != null)
                {
                    if (activate)
                        page.activate(editor);
                    else
                        page.bringToTop(editor);
                    editorUtility.revealElement(editor, element);
                }
            }
        }
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
     * @see #setHierarchyKind(TypeHierarchyKind)
     */
    protected void addSetHierarchyKindAction(IAction action,
        TypeHierarchyKind kind)
    {
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.getToolBarManager().add(action);
        actionBars.getMenuManager().add(action);
    }

    /**
     * Contributes a 'set layout mode' action to this view. This method
     * is called once for each of the 'set layout mode' actions, when the
     * part's control is created. If this view supports only one layout mode,
     * no 'set layout mode' action is created and this method is not called.
     * <p>
     * Default implementation adds the given action to the 'Layout' sub-menu
     * of the view menu. The sub-menu is created if necessary. Subclasses may
     * extend or override this method.
     * </p>
     *
     * @param action a 'set layout mode' action
     *  (never <code>null</code>)
     * @param layoutMode the layout mode set by the given action
     * @see #setLayoutMode(int)
     */
    protected void addSetLayoutModeAction(IAction action, int layoutMode)
    {
        IActionBars actionBars = getViewSite().getActionBars();
        IMenuManager viewMenu = actionBars.getMenuManager();
        String id = "layout"; //$NON-NLS-1$
        IMenuManager layoutSubMenu = viewMenu.findMenuUsingPath(id);
        if (layoutSubMenu == null)
        {
            viewMenu.add(new Separator());
            layoutSubMenu = new MenuManager(
                Messages.TypeHierarchyViewPart_Layout_menu, id);
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
     * Returns the history used by this view; the history is represented by
     * a "live" list of history entries.
     *
     * @return the view history (never <code>null</code>)
     */
    protected List<HistoryEntry> getHistory()
    {
        return history;
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
     */
    protected void onHistoryChanged()
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
        onHistoryChanged();
    }

    private void makeSetHierarchyKindActions()
    {
        int size = supportedHierarchyKinds.size();
        if (size > 1)
        {
            setHierarchyKindActions = new SetHierarchyKindAction[size];
            int i = 0;
            for (TypeHierarchyKind kind : supportedHierarchyKinds)
                setHierarchyKindActions[i++] = new SetHierarchyKindAction(kind);
        }
    }

    private void makeSetLayoutModeActions()
    {
        List<SetLayoutModeAction> actions = new ArrayList<>();
        if (supportsLayoutMode(SWT.VERTICAL))
            actions.add(new SetLayoutModeAction(SWT.VERTICAL));
        if (supportsLayoutMode(SWT.HORIZONTAL))
            actions.add(new SetLayoutModeAction(SWT.HORIZONTAL));
        if (supportsLayoutMode(LAYOUT_AUTOMATIC))
            actions.add(new SetLayoutModeAction(LAYOUT_AUTOMATIC));
        if (supportsLayoutMode(SWT.NONE))
            actions.add(new SetLayoutModeAction(SWT.NONE));
        int size = actions.size();
        if (size > 1)
            setLayoutModeActions = actions.toArray(
                new SetLayoutModeAction[size]);
    }

    private int getDefaultLayoutMode()
    {
        if (supportsLayoutMode(LAYOUT_AUTOMATIC))
            return LAYOUT_AUTOMATIC;
        if (supportsLayoutMode(SWT.VERTICAL))
            return SWT.VERTICAL;
        if (supportsLayoutMode(SWT.HORIZONTAL))
            return SWT.HORIZONTAL;
        if (supportsLayoutMode(SWT.NONE))
            return SWT.NONE;
        throw new AssertionError();
    }

    private void adjustLayout(int layoutMode)
    {
        if (layoutMode == SWT.NONE)
            return;

        Point size = sashForm.getParent().getParent().getSize();
        if (size.x == 0 || size.y == 0)
            return;

        int orientation = (layoutMode == LAYOUT_AUTOMATIC) ? (size.x > size.y
            ? SWT.HORIZONTAL : SWT.VERTICAL) : layoutMode;

        if (sashForm.getOrientation() == orientation && layoutAdjusted)
            return;

        if (sashForm.getOrientation() != orientation)
        {
            saveSashFormWeights();
            sashForm.setOrientation(orientation);
        }

        restoreSashFormWeights();
        layoutAdjusted = true;
    }

    private void saveSashFormWeights()
    {
        if (!layoutAdjusted)
            return;

        int[] weights = sashForm.getWeights();
        if (weights.length < 2)
            return;

        if (sashForm.getOrientation() == SWT.HORIZONTAL)
            horizontalWeights = weights;
        else
            verticalWeights = weights;
    }

    private void restoreSashFormWeights()
    {
        int[] weights = (sashForm.getOrientation() == SWT.HORIZONTAL)
            ? horizontalWeights : verticalWeights;
        if (weights != null)
            sashForm.setWeights(weights);
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

    private static Set<TypeHierarchyKind> symmetricDifference(
        Set<TypeHierarchyKind> kindsA, Set<TypeHierarchyKind> kindsB)
    {
        Set<TypeHierarchyKind> result = EnumSet.copyOf(kindsA);
        for (TypeHierarchyKind kind : kindsB)
        {
            if (!result.add(kind))
                result.remove(kind);
        }
        return result;
    }

    private static String toString(int[] values)
    {
        if (values == null)
            return null;
        StringBuilder sb = new StringBuilder();
        int length = values.length;
        for (int i = 0; i < length; i++)
        {
            sb.append(values[i]);
            if (i < length - 1)
                sb.append(',');
        }
        return sb.toString();
    }

    private static int[] toIntArray(String s) throws NumberFormatException
    {
        if (s == null)
            return null;
        String[] values = s.split(","); //$NON-NLS-1$
        int length = values.length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++)
        {
            result[i] = Integer.parseInt(values[i]);
        }
        return result;
    }

    /**
     * Represents an entry of the type hierarchy view history list.
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
            return Arrays.hashCode(inputElements);
        }

        /**
         * Returns a user-readable text label for this history entry.
         * <p>
         * Default implementation composes a label based on labels for
         * input elements. It invokes {@link #getElementLabel(Object)}
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
                    Messages.TypeHierarchyViewPart_History_entry_label__0,
                    getElementLabel(inputElements[0]));
            case 2:
                return MessageFormat.format(
                    Messages.TypeHierarchyViewPart_History_entry_label__0__1,
                    getElementLabel(inputElements[0]), getElementLabel(
                        inputElements[1]));
            default:
                return MessageFormat.format(
                    Messages.TypeHierarchyViewPart_History_entry_label__0__1_more,
                    getElementLabel(inputElements[0]), getElementLabel(
                        inputElements[1]));
            }
        }

        /**
         * Returns a user-readable text label for the given element.
         *
         * @param element the given element
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

    /**
     * Helper for opening editors on the viewer's selection.
     */
    protected class OpenEditorHelper
        extends OpenAndLinkWithEditorHelper
    {
        /**
         * Creates a new helper for the given viewer.
         *
         * @param viewer the viewer
         */
        public OpenEditorHelper(StructuredViewer viewer)
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
                        Messages.TypeHierarchyViewPart_Open_selected_element,
                        Messages.TypeHierarchyViewPart_Error_opening_editor,
                        e.getStatus());
                }
            }
        }
    }

    private class FocusOnSelectionAction
        extends BaseSelectionListenerAction
    {
        FocusOnSelectionAction()
        {
            super(
                Messages.TypeHierarchyViewPart_Focus_on_selection_action_text);
            setToolTipText(
                Messages.TypeHierarchyViewPart_Focus_on_selection_action_tooltip);
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

    private class RefreshAction
        extends Action
    {
        RefreshAction()
        {
            setText(Messages.TypeHierarchyViewPart_Refresh_action_text);
            setToolTipText(
                Messages.TypeHierarchyViewPart_Refresh_action_tooltip);
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

    private class SetHierarchyKindAction
        extends Action
    {
        final TypeHierarchyKind kind;

        SetHierarchyKindAction(TypeHierarchyKind kind)
        {
            super(null, AS_RADIO_BUTTON);
            this.kind = kind;
            switch (kind)
            {
            case TYPES:
                setText(
                    Messages.TypeHierarchyViewPart_Show_type_hierarchy_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Show_type_hierarchy_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_TH_TYPES));
                break;
            case SUPERTYPES:
                setText(
                    Messages.TypeHierarchyViewPart_Show_supertype_hierarchy_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Show_supertype_hierarchy_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_TH_SUPERTYPES));
                break;
            case SUBTYPES:
                setText(
                    Messages.TypeHierarchyViewPart_Show_subtype_hierarchy_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Show_subtype_hierarchy_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_TH_SUBTYPES));
                break;
            }
            if (kind == getHierarchyKind())
                setChecked(true);
        }

        @Override
        public void run()
        {
            setHierarchyKind(kind);
        }
    }

    private class SetLayoutModeAction
        extends Action
    {
        final int layoutMode;

        SetLayoutModeAction(int layoutMode)
        {
            super(null, AS_RADIO_BUTTON);
            this.layoutMode = layoutMode;
            switch (layoutMode)
            {
            case SWT.VERTICAL:
                setText(
                    Messages.TypeHierarchyViewPart_Layout_vertical_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Layout_vertical_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_LAYOUT_VERTICAL));
                break;
            case SWT.HORIZONTAL:
                setText(
                    Messages.TypeHierarchyViewPart_Layout_horizontal_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Layout_horizontal_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_LAYOUT_HORIZONTAL));
                break;
            case LAYOUT_AUTOMATIC:
                setText(
                    Messages.TypeHierarchyViewPart_Layout_automatic_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Layout_automatic_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_LAYOUT_AUTOMATIC));
                break;
            case SWT.NONE:
                setText(
                    Messages.TypeHierarchyViewPart_Layout_single_action_text);
                setToolTipText(
                    Messages.TypeHierarchyViewPart_Layout_single_action_tooltip);
                setImageDescriptor(Activator.getImageDescriptor(
                    Activator.IMG_ELCL_LAYOUT_SINGLE));
                break;
            default:
                throw new IllegalArgumentException();
            }
            if (layoutMode == getLayoutMode())
                setChecked(true);
        }

        @Override
        public void run()
        {
            setLayoutMode(layoutMode);
        }
    }
}
