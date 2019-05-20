/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.xtext.xtext.ui.callhierarchy;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyLabelProvider;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewManager;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.handly.ui.callhierarchy.ICallHierarchyNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.xtext.ui.labeling.XtextDescriptionLabelProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Call hierarchy view for the Xtext language, org.eclipse.xtext.Xtext.
 */
public final class XtextXtextCallHierarchyView
    extends CallHierarchyViewPart
{
    /**
     * The view ID.
     */
    public static final String ID =
        "org.eclipse.handly.examples.xtext.xtext.ui.XtextXtextCallHierarchyView"; //$NON-NLS-1$

    @Inject
    private MyLabelProvider labelProvider;
    @Inject
    private MyEditorUtility editorUtility;

    @Override
    public String getTitleToolTip()
    {
        return getPartName() + " (Handly Xtext-Xtext Example)";
    }

    @Override
    protected boolean isPossibleInputElement(Object element)
    {
        return element instanceof Element;
    }

    @Override
    protected CallHierarchyViewManager getViewManager()
    {
        return XtextXtextCallHierarchyViewManager.INSTANCE;
    }

    @Override
    protected ICallHierarchyNode[] createHierarchyRoots(Object[] inputElements)
    {
        int length = inputElements.length;
        ICallHierarchyNode[] roots = new ICallHierarchyNode[length];
        for (int i = 0; i < length; i++)
        {
            roots[i] = createHierarchyRoot((Element)inputElements[i]);
        }
        return roots;
    }

    private XtextXtextCallHierarchyNode createHierarchyRoot(Element element)
    {
        switch (getHierarchyKind())
        {
        case CALLER:
            return new XtextXtextCallerHierarchyNode(null, element);
        case CALLEE:
            return new XtextXtextCalleeHierarchyNode(null, element);
        default:
            throw new AssertionError();
        }
    }

    @Override
    protected String computeContentDescription()
    {
        Object[] elements = getInputElements();
        switch (getHierarchyKind())
        {
        case CALLER:
            switch (elements.length)
            {
            case 0:
                return ""; //$NON-NLS-1$
            case 1:
                return MessageFormat.format("Callers of ''{0}''", elements[0]);
            case 2:
                return MessageFormat.format("Callers of ''{0}'', ''{1}''",
                    elements[0], elements[1]);
            default:
                return MessageFormat.format("Callers of ''{0}'', ''{1}'', ...",
                    elements[0], elements[1]);
            }
        case CALLEE:
            switch (elements.length)
            {
            case 0:
                return ""; //$NON-NLS-1$
            case 1:
                return MessageFormat.format("Calls from ''{0}''", elements[0]);
            case 2:
                return MessageFormat.format("Calls from ''{0}'', ''{1}''",
                    elements[0], elements[1]);
            default:
                return MessageFormat.format("Calls from ''{0}'', ''{1}'', ...",
                    elements[0], elements[1]);
            }
        default:
            throw new AssertionError();
        }
    }

    @Override
    protected void configureHierarchyViewer(TreeViewer viewer)
    {
        super.configureHierarchyViewer(viewer);
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new CallHierarchyLabelProvider(labelProvider)));
    }

    @Override
    protected EditorOpener createEditorOpener()
    {
        return new EditorOpener(getSite().getPage(), editorUtility);
    }

    @Override
    protected HistoryEntry createHistoryEntry(Object[] inputElements)
    {
        return new MyHistoryEntry(inputElements);
    }

    private class MyHistoryEntry
        extends HistoryEntry
    {
        MyHistoryEntry(Object[] inputElements)
        {
            super(inputElements);
        }

        @Override
        public ImageDescriptor getImageDescriptor()
        {
            return labelProvider.getImageDescriptor(getInputElements()[0]);
        }

        @Override
        protected String getElementLabel(Object element)
        {
            return labelProvider.getText(element);
        }
    }

    private static class MyLabelProvider
        extends XtextDescriptionLabelProvider
    {
        @Override
        public String getText(Object element)
        {
            return getStyledText(element).getString();
        }

        @Override
        public StyledString getStyledText(Object element)
        {
            if (element instanceof Element)
            {
                Element e = (Element)element;
                StyledString name = new StyledString(
                    e.getDescription().getQualifiedName().toString());
                IEObjectDescription grammarDescription =
                    e.getGrammarDescription();
                if (grammarDescription == null)
                    return name;
                return name.append(" - "
                    + grammarDescription.getQualifiedName(),
                    StyledString.QUALIFIER_STYLER);
            }
            return new StyledString(String.valueOf(element));
        }

        @Override
        public Image getImage(Object element)
        {
            if (element instanceof Element)
                return super.getImage(((Element)element).getDescription());

            return null;
        }

        @Override
        public ImageDescriptor getImageDescriptor(Object element)
        {
            if (element instanceof Element)
                return super.getImageDescriptor(
                    ((Element)element).getDescription());

            return null;
        }
    }

    @Singleton
    private static class MyEditorUtility
        extends EditorUtility
    {
        @Inject
        private IStorage2UriMapper mapper;
        @Inject
        private ILocationInFileProvider locationInFileProvider;

        @Override
        public IEditorInput getEditorInput(Object element)
        {
            if (element instanceof Element)
            {
                Iterator<Pair<IStorage, IProject>> it = mapper.getStorages(
                    ((Element)element).getUri().trimFragment()).iterator();
                if (it.hasNext())
                    return EditorUtils.createEditorInput(it.next().getFirst());
            }
            return super.getEditorInput(element);
        }

        @Override
        public void revealElement(IEditorPart editor, Object element)
        {
            if (element instanceof Element)
            {
                XtextEditor xtextEditor = EditorUtils.getXtextEditor(editor);
                if (xtextEditor != null)
                {
                    xtextEditor.getDocument().priorityReadOnly(resource ->
                    {
                        EObject object = resource.getEObject(
                            ((Element)element).getUri().fragment());
                        if (object != null)
                        {
                            ITextRegion region =
                                locationInFileProvider.getSignificantTextRegion(
                                    object);
                            xtextEditor.selectAndReveal(region.getOffset(),
                                region.getLength());
                        }
                        return null;
                    });
                }
            }
            super.revealElement(editor, element);
        }
    }
}
