/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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

import static org.eclipse.jdt.ui.JavaElementLabels.COLORIZE;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_RETURNTYPE;
import static org.eclipse.jdt.ui.JavaElementLabels.M_APP_TYPE_PARAMETERS;
import static org.eclipse.jdt.ui.JavaElementLabels.M_PARAMETER_TYPES;
import static org.eclipse.jdt.ui.JavaElementLabels.M_POST_QUALIFIED;

import java.text.MessageFormat;
import java.util.EnumSet;

import org.eclipse.handly.internal.examples.adapter.ui.Activator;
import org.eclipse.handly.internal.examples.adapter.ui.JavaEditorUtility;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.callhierarchy.CallHierarchy;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyKind;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyLabelProvider;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.handly.ui.callhierarchy.ICallHierarchy;
import org.eclipse.handly.ui.callhierarchy.ICallHierarchyNode;
import org.eclipse.handly.util.ArrayUtil;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
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
    public void setInputElements(Object[] elements)
    {
        if (elements == null)
            throw new IllegalArgumentException();
        if (ArrayUtil.hasElementsNotOfType(elements, IMethod.class))
            throw new IllegalArgumentException();

        super.setInputElements(elements);
    }

    @Override
    protected ICallHierarchy createHierarchy()
    {
        Object[] inputElements = getInputElements();
        int length = inputElements.length;

        ICallHierarchyNode[] rootNodes = new ICallHierarchyNode[length];
        for (int i = 0; i < length; i++)
        {
            IMethod method = (IMethod)inputElements[i];
            rootNodes[i] = JavaCallerHierarchyNode.newRootNode(method);
        }

        String label;
        if (length == 0)
            label = "";
        else if (length == 1)
            label = MessageFormat.format("Methods calling ''{0}''",
                JavaElementLabels.getTextLabel(inputElements[0],
                    JavaElementLabels.ALL_DEFAULT));
        else if (length == 2)
            label = MessageFormat.format("Methods calling ''{0}'', ''{1}''",
                JavaElementLabels.getTextLabel(inputElements[0], 0),
                JavaElementLabels.getTextLabel(inputElements[1], 0));
        else
            label = MessageFormat.format(
                "Methods calling ''{0}'', ''{1}'', ...",
                JavaElementLabels.getTextLabel(inputElements[0], 0),
                JavaElementLabels.getTextLabel(inputElements[1], 0));

        return new CallHierarchy(CallHierarchyKind.CALLER, rootNodes, label);
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
