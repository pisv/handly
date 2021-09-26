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
package org.eclipse.handly.internal.examples.adapter.ui.typehierarchy;

import static org.eclipse.jdt.ui.JavaElementLabels.APPEND_ROOT_PATH;
import static org.eclipse.jdt.ui.JavaElementLabels.COLORIZE;
import static org.eclipse.jdt.ui.JavaElementLabels.DEFAULT_QUALIFIED;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_RETURNTYPE;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_TYPE_PARAMETERS;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_NAMES;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_TYPES;
import static org.eclipse.jdt.ui.JavaElementLabels.P_COMPRESSED;
import static org.eclipse.jdt.ui.JavaElementLabels.T_POST_QUALIFIED;
import static org.eclipse.jdt.ui.JavaElementLabels.T_TYPE_PARAMETERS;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.examples.adapter.ui.Activator;
import org.eclipse.handly.internal.examples.adapter.ui.JavaEditorUtility;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.typehierarchy.TypeHierarchyKind;
import org.eclipse.handly.ui.typehierarchy.TypeHierarchyViewPart;
import org.eclipse.handly.ui.viewer.DeferredTreeContentProvider;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.IElementCollector;

/**
 * Java-specific type hierarchy view.
 */
public class JavaTypeHierarchyView
    extends TypeHierarchyViewPart
{
    /**
     * The view ID.
     */
    public static final String ID = Activator.PLUGIN_ID
        + ".JavaTypeHierarchyView"; //$NON-NLS-1$

    private final TypeHierarchyHolder hierarchyHolder =
        new TypeHierarchyHolder();
    private TableViewer methodViewer;

    /**
     * Creates a new <code>JavaTypeHierarchyView</code>.
     */
    public JavaTypeHierarchyView()
    {
        super(EnumSet.of(TypeHierarchyKind.SUPERTYPES,
            TypeHierarchyKind.SUBTYPES));
    }

    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        methodViewer = new TableViewer(getSashForm(), SWT.MULTI);
        methodViewer.setContentProvider(new ArrayContentProvider());
        methodViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new MethodLabelProvider()));
        methodViewer.addSelectionChangedListener(e -> methodSelectionChanged(
            e.getSelection()));

        new OpenEditorHelper(methodViewer);
    }

    @Override
    public boolean supportsLayoutMode(int layoutMode)
    {
        return layoutMode == SWT.VERTICAL || layoutMode == SWT.HORIZONTAL
            || layoutMode == (SWT.VERTICAL | SWT.HORIZONTAL)
            || layoutMode == SWT.NONE;
    }

    @Override
    public String getTitleToolTip()
    {
        return getPartName() + " (Handly Adapter Example)";
    }

    @Override
    public void setInputElements(Object[] elements)
    {
        if (elements.length > 1)
            throw new IllegalArgumentException();

        super.setInputElements(elements);
    }

    @Override
    protected void onInputElementsChanged(Object[] oldInputElements,
        Object[] newInputElements)
    {
        IType inputType = newInputElements.length > 0
            ? (IType)newInputElements[0] : null;
        hierarchyHolder.setInputType(inputType);

        super.onInputElementsChanged(oldInputElements, newInputElements);
    }

    @Override
    protected boolean isPossibleInputElement(Object element)
    {
        return element instanceof IType;
    }

    @Override
    protected HistoryEntry createHistoryEntry(Object[] inputElements)
    {
        return new TypeHistoryEntry(inputElements);
    }

    @Override
    protected EditorOpener createEditorOpener()
    {
        return new EditorOpener(getSite().getPage(),
            JavaEditorUtility.INSTANCE);
    }

    @Override
    protected String computeContentDescription()
    {
        Object[] elements = getInputElements();
        if (elements.length == 0)
            return ""; //$NON-NLS-1$
        return MessageFormat.format("''{0}''", JavaElementLabels.getTextLabel(
            elements[0], T_POST_QUALIFIED | T_TYPE_PARAMETERS | P_COMPRESSED));
    }

    @Override
    protected void configureHierarchyViewer(TreeViewer viewer,
        TypeHierarchyKind kind)
    {
        viewer.setUseHashlookup(true);
        viewer.setAutoExpandLevel(2);
        viewer.setContentProvider(new TypeHierarchyContentProvider(viewer, kind,
            hierarchyHolder, getSite()));
        viewer.setLabelProvider(new JavaElementLabelProvider());
    }

    @Override
    protected void setHierarchyViewerInput(TreeViewer viewer,
        TypeHierarchyKind kind)
    {
        super.setHierarchyViewerInput(viewer, kind);

        IType inputType = hierarchyHolder.getInputType();
        if (inputType != null)
            viewer.setSelection(new StructuredSelection(inputType), true);
    }

    @Override
    protected void refresh(IContext context)
    {
        hierarchyHolder.setInputType(hierarchyHolder.getInputType()); // reset type hierarchy

        super.refresh(context);
    }

    @Override
    protected void updateStatusLine(IStatusLineManager manager,
        IStructuredSelection selection)
    {
        super.updateStatusLine(manager, selection);

        if (selection.size() == 1)
        {
            Object element = selection.getFirstElement();
            if (element instanceof IJavaElement)
            {
                manager.setMessage(JavaElementLabels.getTextLabel(element,
                    DEFAULT_QUALIFIED | APPEND_ROOT_PATH | M_PARAMETER_TYPES
                        | M_PARAMETER_NAMES | M_APP_RETURNTYPE
                        | T_TYPE_PARAMETERS));

                if (!((IJavaElement)element).exists())
                {
                    manager.setErrorMessage(MessageFormat.format(
                        "''{0}'' no longer exists. Try to refresh the view",
                        JavaElementLabels.getTextLabel(element, 0)));
                }
            }
        }
    }

    @Override
    protected void onHierarchySelectionChanged(ISelection selection,
        TypeHierarchyKind kind)
    {
        super.onHierarchySelectionChanged(selection, kind);

        Object element = getSelectedElement(selection);

        IMethod[] methods = null;
        if (element instanceof IType)
        {
            try
            {
                methods = ((IType)element).getMethods();
            }
            catch (JavaModelException e)
            {
                if (!e.isDoesNotExist())
                    Activator.logError(e);
            }
        }

        methodViewer.setInput(methods);

        if (methods != null && methods.length > 0)
            methodViewer.setSelection(new StructuredSelection(methods[0]),
                true);
    }

    private void methodSelectionChanged(ISelection selection)
    {
        Object element = getSelectedElement(selection);
        if (element != null)
        {
            if (methodViewer.getControl().isFocusControl())
            {
                updateStatusLine(
                    getViewSite().getActionBars().getStatusLineManager(),
                    methodViewer.getStructuredSelection());

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

    private static class TypeHierarchyHolder
    {
        private IType inputType;
        private ITypeHierarchy typeHierarchy;

        void setInputType(IType inputType)
        {
            this.inputType = inputType;
            this.typeHierarchy = null;
        }

        IType getInputType()
        {
            return inputType;
        }

        ITypeHierarchy getTypeHierarchy()
        {
            return typeHierarchy;
        }

        ITypeHierarchy getOrCreateTypeHierarchy(IProgressMonitor monitor)
            throws JavaModelException
        {
            if (inputType != null && typeHierarchy == null)
                typeHierarchy = inputType.newTypeHierarchy(monitor);
            return typeHierarchy;
        }
    }

    private static class TypeHierarchyContentProvider
        extends DeferredTreeContentProvider
    {
        private final TypeHierarchyKind hierarchyKind;
        private final TypeHierarchyHolder hierarchyHolder;

        TypeHierarchyContentProvider(AbstractTreeViewer viewer,
            TypeHierarchyKind hierarchyKind,
            TypeHierarchyHolder hierarchyHolder, IWorkbenchPartSite site)
        {
            super(viewer, site);
            this.hierarchyKind = Objects.requireNonNull(hierarchyKind);
            this.hierarchyHolder = Objects.requireNonNull(hierarchyHolder);
        }

        @Override
        public Object[] getElements(Object inputElement)
        {
            IType inputType = hierarchyHolder.getInputType();
            if (inputType != null)
                return new Object[] { inputType };
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parentElement)
        {
            if (!(parentElement instanceof IType))
                return new Object[0];
            ITypeHierarchy typeHierarchy = hierarchyHolder.getTypeHierarchy();
            if (typeHierarchy != null)
                return getChildren(typeHierarchy, parentElement);
            return getDeferredTreeContentManager().getChildren(parentElement);
        }

        @Override
        public Object getParent(Object element)
        {
            return null;
        }

        @Override
        public boolean hasChildren(Object element)
        {
            if (!(element instanceof IType))
                return false;
            ITypeHierarchy typeHierarchy = hierarchyHolder.getTypeHierarchy();
            if (typeHierarchy != null)
                return getChildren(typeHierarchy, element).length > 0;
            return true;
        }

        @Override
        protected void fetchDeferredChildren(Object parentElement,
            IElementCollector collector, IProgressMonitor monitor)
        {
            ITypeHierarchy typeHierarchy = null;
            try
            {
                typeHierarchy = hierarchyHolder.getOrCreateTypeHierarchy(
                    monitor);
            }
            catch (JavaModelException e)
            {
                Activator.logError(e);
            }
            if (typeHierarchy != null)
            {
                collector.add(getChildren(typeHierarchy, parentElement), null);
            }
            collector.done();
        }

        private Object[] getChildren(ITypeHierarchy typeHierarchy,
            Object parentElement)
        {
            IType type = (IType)parentElement;
            return (hierarchyKind == TypeHierarchyKind.SUBTYPES)
                ? typeHierarchy.getSubtypes(type) : typeHierarchy.getSupertypes(
                    type);
        }
    }

    private static class MethodLabelProvider
        extends JavaElementLabelProvider
    {
        private static final long M_FLAGS = M_PARAMETER_TYPES
            | M_APP_TYPE_PARAMETERS | M_APP_RETURNTYPE;

        @Override
        public String getText(Object element)
        {
            if (element instanceof IMethod)
                return JavaElementLabels.getTextLabel(element, M_FLAGS);

            return super.getText(element);
        }

        @Override
        public StyledString getStyledText(Object element)
        {
            if (element instanceof IMethod)
                return JavaElementLabels.getStyledTextLabel(element, M_FLAGS
                    | COLORIZE);

            return super.getStyledText(element);
        }
    }

    private static class TypeHistoryEntry
        extends HistoryEntry
    {
        TypeHistoryEntry(Object[] inputElements)
        {
            super(inputElements);
        }

        @SuppressWarnings("restriction")
        @Override
        public ImageDescriptor getImageDescriptor()
        {
            IJavaElement firstElement = (IJavaElement)getInputElements()[0];
            org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider imageProvider =
                new org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider();
            try
            {
                return imageProvider.getBaseImageDescriptor(firstElement, 0);
            }
            finally
            {
                imageProvider.dispose();
            }
        }

        @Override
        protected String getElementLabel(Object element)
        {
            return JavaElementLabels.getTextLabel(element, T_POST_QUALIFIED
                | T_TYPE_PARAMETERS | P_COMPRESSED);
        }
    }
}
