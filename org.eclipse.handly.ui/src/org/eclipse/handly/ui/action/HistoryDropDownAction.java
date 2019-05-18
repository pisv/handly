/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.action;

import static org.eclipse.swt.events.SelectionListener.widgetDefaultSelectedAdapter;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

/**
 * Shows a history list in a drop-down.
 *
 * @param <E> the type of elements managed by the history
 */
public class HistoryDropDownAction<E>
    extends Action
{
    private History<E> history;
    private Menu menu;

    /**
     * Constructs a <code>HistoryDropDownAction</code> on the given history.
     *
     * @param history not <code>null</code>
     */
    public HistoryDropDownAction(History<E> history)
    {
        if (history == null)
            throw new IllegalArgumentException();
        this.history = history;
        setToolTipText(Messages.HistoryDropDownAction_tooltip);
        setImageDescriptor(Activator.getImageDescriptor(
            Activator.IMG_ELCL_HISTORY_LIST));
        setDisabledImageDescriptor(Activator.getImageDescriptor(
            Activator.IMG_DLCL_HISTORY_LIST));
        setMenuCreator(new HistoryMenuCreator());
    }

    @Override
    public void run()
    {
        new HistoryListAction().run();
    }

    /**
     * Configures the history list action. Default implementation does nothing.
     * Subclasses may override (e.g., to set a more specific text or an image
     * for the action).
     *
     * @param action never <code>null</code>
     */
    protected void configureHistoryListAction(IAction action)
    {
    }

    /**
     * Returns the title for the history list dialog. Default implementation
     * returns a generic title.
     *
     * @return the title for the history list dialog (not <code>null</code>)
     */
    protected String getHistoryListDialogTitle()
    {
        return Messages.HistoryDropDownAction_History_list_dialog_title;
    }

    /**
     * Returns the message for the history list dialog. Default implementation
     * returns a generic message which prompts the user to select the element
     * to open.
     *
     * @return the message for the history list dialog (not <code>null</code>)
     */
    protected String getHistoryListDialogMessage()
    {
        return Messages.HistoryDropDownAction_History_list_dialog_message;
    }

    /**
     * Returns a 'clear history' action. Default implementation returns
     * a generic action, which calls {@link History#setHistoryEntries(List)}
     * with an empty list.
     *
     * @return the 'clear history' action (may be <code>null</code>,
     *  in which case no 'clear history' action will be contributed to the
     *  history drop-down)
     */
    protected IAction getClearHistoryAction()
    {
        return new ClearHistoryAction();
    }

    /**
     * Allows subclasses to place additional menu entries to the history
     * drop-down. Default implementation does nothing.
     *
     * @param manager never <code>null</code>
     */
    protected void addMenuEntries(IMenuManager manager)
    {
    }

    /**
     * Returns the maximum number of entries in the history drop-down.
     *
     * @return the maximum number of entries in the history drop-down
     */
    protected int getMaxEntriesInDropDown()
    {
        return 10;
    }

    /**
     * Returns the shell to use as the parent shell of the history list dialog.
     * Default implementation returns the currently active shell.
     *
     * @return the shell (may be <code>null</code>)
     */
    protected Shell getShell()
    {
        return PlatformUI.getWorkbench().getDisplay().getActiveShell();
    }

    /**
     * Represents the underlying history for the <code>HistoryDropDownAction</code>.
     *
     * @param <E> the type of history entries
     */
    public static abstract class History<E>
    {
        /**
         * Returns a list of the history entries. The list
         * will not be modified or retained by the caller.
         *
         * @return the history entries (not <code>null</code>, may be empty,
         *  must not contain nulls)
         */
        public abstract List<E> getHistoryEntries();

        /**
         * Replaces the history entries with the given entries.
         *
         * @param entries never <code>null</code>, may be empty, does not
         *  contain nulls
         */
        public abstract void setHistoryEntries(List<E> entries);

        /**
         * Returns the history entry currently designated as 'active'.
         *
         * @return the active entry (may be <code>null</code>)
         */
        public abstract E getActiveEntry();

        /**
         * Designates the given history entry as the currently 'active' entry.
         * This method should not throw an exception if for some reason
         * the given entry cannot be made active.
         *
         * @param entry never <code>null</code>
         */
        public abstract void setActiveEntry(E entry);

        /**
         * Returns a user-readable text label for the given history entry.
         *
         * @param entry never <code>null</code>
         * @return the text label for the entry (not <code>null</code>)
         */
        public abstract String getLabel(E entry);

        /**
         * Returns an image descriptor for the given history entry.
         *
         * @param entry never <code>null</code>
         * @return the image descriptor for the entry (may be <code>null</code>)
         */
        public abstract ImageDescriptor getImageDescriptor(E entry);
    }

    private class HistoryMenuCreator
        implements IMenuCreator
    {
        @Override
        public Menu getMenu(Menu parent)
        {
            return null;
        }

        @Override
        public Menu getMenu(Control parent)
        {
            if (menu != null)
                menu.dispose();

            MenuManager manager = new MenuManager();
            manager.setRemoveAllWhenShown(true);
            manager.addMenuListener(new IMenuListener()
            {
                @Override
                public void menuAboutToShow(IMenuManager manager)
                {
                    if (history == null)
                        return;

                    addEntryMenuItems(manager, history.getHistoryEntries());

                    manager.add(new Separator());

                    HistoryListAction historyListAction =
                        new HistoryListAction();
                    configureHistoryListAction(historyListAction);
                    manager.add(historyListAction);

                    IAction clearHistoryAction = getClearHistoryAction();
                    if (clearHistoryAction != null)
                        manager.add(clearHistoryAction);

                    manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));

                    addMenuEntries(manager);
                }

                private void addEntryMenuItems(IMenuManager manager,
                    List<E> entries)
                {
                    E active = history.getActiveEntry();
                    int size = Math.min(entries.size(),
                        getMaxEntriesInDropDown());
                    for (int i = 0; i < size; i++)
                    {
                        E entry = entries.get(i);
                        HistoryAction action = new HistoryAction(entry);
                        action.setChecked(entry.equals(active));
                        manager.add(action);
                    }
                }
            });

            menu = manager.createContextMenu(parent);

            // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=170647
            Display display = parent.getDisplay();
            menu.addMenuListener(new MenuAdapter()
            {
                @Override
                public void menuHidden(final MenuEvent e)
                {
                    display.asyncExec(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            manager.removeAll();
                            if (menu != null)
                            {
                                menu.dispose();
                                menu = null;
                            }
                        }
                    });
                }
            });
            return menu;
        }

        @Override
        public void dispose()
        {
            history = null;

            if (menu != null)
            {
                menu.dispose();
                menu = null;
            }
        }
    }

    private class HistoryAction
        extends Action
    {
        final E entry;

        HistoryAction(E entry)
        {
            super(null, AS_RADIO_BUTTON);
            if (entry == null)
                throw new IllegalArgumentException();
            this.entry = entry;
            setText(history.getLabel(entry));
            setImageDescriptor(history.getImageDescriptor(entry));
        }

        @Override
        public void run()
        {
            if (isChecked())
                history.setActiveEntry(entry);
        }
    }

    private class HistoryListAction
        extends Action
    {
        HistoryListAction()
        {
            super(Messages.HistoryDropDownAction_History_list_action_text);
        }

        @Override
        public void run()
        {
            HistoryListDialog dialog = new HistoryListDialog(getShell());
            if (dialog.open() == Window.OK)
            {
                history.setHistoryEntries(dialog.getRemaining());
                history.setActiveEntry(dialog.getResult());
            }
        }
    }

    private class HistoryListDialog
        extends StatusDialog
    {
        private List<E> historyList;
        private E result;
        private TableViewer historyViewer;
        private Button removeButton;

        HistoryListDialog(Shell parentShell)
        {
            super(parentShell);
            historyList = new ArrayList<>(history.getHistoryEntries());
            setTitle(getHistoryListDialogTitle());
            setHelpAvailable(false);
        }

        List<E> getRemaining()
        {
            return historyList;
        }

        E getResult()
        {
            return result;
        }

        @Override
        protected boolean isResizable()
        {
            return true;
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);

            Label label = new Label(composite, SWT.NONE);
            label.setText(getHistoryListDialogMessage());

            createListArea(composite);

            applyDialogFont(composite);
            return composite;
        }

        private void createListArea(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.numColumns = 2;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));

            createTableArea(composite);
            createListButtons(composite);

            setTableSelection();
        }

        private void createTableArea(Composite parent)
        {
            Table table = new Table(parent, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL);

            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.widthHint = convertWidthInCharsToPixels(50);
            gd.heightHint = convertHeightInCharsToPixels(12);
            gd.grabExcessHorizontalSpace = true;
            gd.grabExcessVerticalSpace = true;
            table.setLayoutData(gd);

            table.addSelectionListener(widgetDefaultSelectedAdapter(e ->
            {
                if (getStatus().isOK())
                {
                    okPressed();
                }
            }));

            historyViewer = new TableViewer(table);
            historyViewer.setLabelProvider(new LabelProvider()
            {
                final ResourceManager resourceManager =
                    new LocalResourceManager(JFaceResources.getResources(
                        historyViewer.getControl().getDisplay()));

                @Override
                public void dispose()
                {
                    resourceManager.dispose();
                    super.dispose();
                }

                @SuppressWarnings("unchecked")
                @Override
                public String getText(Object element)
                {
                    return history.getLabel((E)element);
                }

                @SuppressWarnings("unchecked")
                @Override
                public Image getImage(Object element)
                {
                    ImageDescriptor descriptor = history.getImageDescriptor(
                        (E)element);
                    if (descriptor == null)
                        return null;
                    return (Image)resourceManager.get(descriptor);
                }
            });
            historyViewer.setContentProvider(new ArrayContentProvider());
            historyViewer.setInput(historyList);

            historyViewer.addSelectionChangedListener(e ->
            {
                IStructuredSelection selection =
                    historyViewer.getStructuredSelection();
                int selectionSize = selection.size();
                IStatus status;
                if (selectionSize == 1)
                {
                    status = Status.OK_STATUS;
                    @SuppressWarnings("unchecked")
                    E element = (E)selection.getFirstElement();
                    result = element;
                }
                else
                {
                    status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
                    result = null;
                }
                removeButton.setEnabled(historyList.size() > selectionSize
                    && selectionSize > 0);
                updateStatus(status);
            });
        }

        private void createListButtons(Composite parent)
        {
            removeButton = new Button(parent, SWT.PUSH);
            removeButton.setText(
                Messages.HistoryDropDownAction_History_list_dialog_remove_button_text);

            GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
            int widthHint = convertHorizontalDLUsToPixels(
                IDialogConstants.BUTTON_WIDTH);
            Point minSize = removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                true);
            gd.widthHint = Math.max(widthHint, minSize.x);
            removeButton.setLayoutData(gd);

            removeButton.addSelectionListener(widgetSelectedAdapter(e ->
            {
                IStructuredSelection selection =
                    historyViewer.getStructuredSelection();
                historyList.removeAll(selection.toList());
                historyViewer.remove(selection.toArray());
            }));
        }

        private void setTableSelection()
        {
            E activeEntry = history.getActiveEntry();
            ISelection sel = activeEntry == null ? StructuredSelection.EMPTY
                : new StructuredSelection(activeEntry);
            historyViewer.setSelection(sel, true);
        }
    }

    private class ClearHistoryAction
        extends Action
    {
        ClearHistoryAction()
        {
            super(Messages.HistoryDropDownAction_Clear_history_action_text);
        }

        @Override
        public void run()
        {
            history.setHistoryEntries(Collections.emptyList());
        }
    }
}
