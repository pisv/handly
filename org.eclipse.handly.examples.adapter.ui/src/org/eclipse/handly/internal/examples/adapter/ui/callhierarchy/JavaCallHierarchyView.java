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
package org.eclipse.handly.internal.examples.adapter.ui.callhierarchy;

import static org.eclipse.jdt.ui.JavaElementLabels.APPEND_ROOT_PATH;
import static org.eclipse.jdt.ui.JavaElementLabels.COLORIZE;
import static org.eclipse.jdt.ui.JavaElementLabels.DEFAULT_QUALIFIED;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_RETURNTYPE;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_TYPE_PARAMETERS;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_NAMES;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_TYPES;
import static org.eclipse.jdt.ui.JavaElementLabels.M_POST_QUALIFIED;
import static org.eclipse.jdt.ui.JavaElementLabels.P_COMPRESSED;
import static org.eclipse.jdt.ui.JavaElementLabels.T_TYPE_PARAMETERS;

import java.text.MessageFormat;
import java.util.EnumSet;

import org.eclipse.handly.internal.examples.adapter.ui.Activator;
import org.eclipse.handly.internal.examples.adapter.ui.JavaEditorUtility;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyKind;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyLabelProvider;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewManager;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.handly.ui.callhierarchy.ICallHierarchyNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Java-specific call hierarchy view.
 */
public final class JavaCallHierarchyView
    extends CallHierarchyViewPart
{
    /**
     * The view ID.
     */
    public static final String ID = Activator.PLUGIN_ID
        + ".JavaCallHierarchyView"; //$NON-NLS-1$

    /**
     * Creates a new <code>JavaCallHierarchyView</code>.
     */
    public JavaCallHierarchyView()
    {
        super(EnumSet.of(CallHierarchyKind.CALLER));
    }

    @Override
    public String getTitleToolTip()
    {
        return getPartName() + " (Handly Adapter Example)";
    }

    @Override
    public boolean isPossibleInputElement(Object element)
    {
        return element instanceof IMethod;
    }

    @Override
    protected CallHierarchyViewManager getViewManager()
    {
        return JavaCallHierarchyViewManager.INSTANCE;
    }

    @Override
    protected ICallHierarchyNode[] createHierarchyRoots(Object[] elements)
    {
        int length = elements.length;
        ICallHierarchyNode[] roots = new ICallHierarchyNode[length];
        for (int i = 0; i < length; i++)
        {
            IMethod method = (IMethod)elements[i];
            roots[i] = JavaCallerHierarchyNode.newRootNode(method);
        }
        return roots;
    }

    @Override
    protected String computeContentDescription()
    {
        Object[] elements = getInputElements();
        switch (elements.length)
        {
        case 0:
            return ""; //$NON-NLS-1$
        case 1:
            return MessageFormat.format("Methods calling ''{0}''",
                JavaElementLabels.getTextLabel(elements[0],
                    JavaElementLabels.ALL_DEFAULT));
        case 2:
            return MessageFormat.format("Methods calling ''{0}'', ''{1}''",
                JavaElementLabels.getTextLabel(elements[0], 0),
                JavaElementLabels.getTextLabel(elements[1], 0));
        default:
            return MessageFormat.format("Methods calling ''{0}'', ''{1}'', ...",
                JavaElementLabels.getTextLabel(elements[0], 0),
                JavaElementLabels.getTextLabel(elements[1], 0));
        }
    }

    @Override
    protected void updateStatusLine(IStatusLineManager manager,
        IStructuredSelection selection)
    {
        super.updateStatusLine(manager, selection);
        if (selection.size() == 1)
        {
            Object element = selection.getFirstElement();
            if (element instanceof IJavaElement
                && !((IJavaElement)element).exists())
            {
                manager.setErrorMessage(MessageFormat.format(
                    "''{0}'' no longer exists", JavaElementLabels.getTextLabel(
                        element, 0)));
            }
        }
    }

    @Override
    protected String getStatusLineMessage(Object element)
    {
        return JavaElementLabels.getTextLabel(element, DEFAULT_QUALIFIED
            | APPEND_ROOT_PATH | M_PARAMETER_TYPES | M_PARAMETER_NAMES
            | M_APP_RETURNTYPE | T_TYPE_PARAMETERS);
    }

    @Override
    protected void configureHierarchyViewer(TreeViewer viewer)
    {
        super.configureHierarchyViewer(viewer);
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new CallHierarchyLabelProvider(new MethodLabelProvider())));
    }

    @Override
    protected EditorOpener createEditorOpener()
    {
        return new EditorOpener(getSite().getPage(),
            JavaEditorUtility.INSTANCE);
    }

    @Override
    protected HistoryEntry createHistoryEntry(Object[] inputElements)
    {
        return new MethodHistoryEntry(inputElements);
    }

    private static class MethodHistoryEntry
        extends HistoryEntry
    {
        private static final long LABEL_FLAGS = M_PARAMETER_TYPES
            | M_APP_TYPE_PARAMETERS | M_APP_RETURNTYPE | M_POST_QUALIFIED
            | T_TYPE_PARAMETERS | P_COMPRESSED;

        MethodHistoryEntry(Object[] inputElements)
        {
            super(inputElements);
        }

        @SuppressWarnings("restriction")
        @Override
        public ImageDescriptor getImageDescriptor()
        {
            Object firstElement = getInputElements()[0];
            if (firstElement instanceof IMethod)
            {
                org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider imageProvider =
                    new org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider();
                ImageDescriptor desc = imageProvider.getBaseImageDescriptor(
                    (IMethod)firstElement, 0);
                imageProvider.dispose();
                return desc;
            }
            return super.getImageDescriptor();
        }

        @Override
        protected String getElementLabel(Object element)
        {
            if (element instanceof IMethod)
                return JavaElementLabels.getTextLabel(element, LABEL_FLAGS);

            return super.getElementLabel(element);
        }
    }

    private static class MethodLabelProvider
        extends JavaElementLabelProvider
    {
        private static final long M_FLAGS = M_PARAMETER_TYPES
            | M_APP_TYPE_PARAMETERS | M_APP_RETURNTYPE | M_POST_QUALIFIED;

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
}
