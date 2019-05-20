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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.xtext.ui.Activator;

import com.google.inject.Inject;

/**
 * A handler that opens {@link XtextXtextCallHierarchyView} for the rule
 * selected in the Xtext grammar editor.
 */
public final class OpenCallHierarchyHandler
    extends AbstractHandler
{
    @Inject
    private EObjectAtOffsetHelper helper;

    @Inject
    private XtextXtextCallHierarchyUtility utility;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
        if (editor != null)
        {
            IWorkbenchPage page =
                editor.getSite().getWorkbenchWindow().getActivePage();
            if (page != null)
            {
                ISelection selection =
                    editor.getSelectionProvider().getSelection();
                if (selection instanceof ITextSelection)
                {
                    editor.getDocument().priorityReadOnly(resource ->
                    {
                        EObject eObject = helper.resolveElementAt(resource,
                            ((ITextSelection)selection).getOffset());
                        if (eObject != null)
                        {
                            URI objectUri =
                                EcoreUtil2.getPlatformResourceOrNormalizedURI(
                                    eObject);
                            IEObjectDescription description =
                                utility.getRuleDescription(objectUri);
                            if (description != null)
                            {
                                Element element = new Element(description,
                                    utility.getGrammarDescription(objectUri));
                                try
                                {
                                    XtextXtextCallHierarchyViewManager.INSTANCE.openView(
                                        page, new Object[] { element });
                                }
                                catch (PartInitException e)
                                {
                                    Activator.getDefault().getLog().log(
                                        e.getStatus());
                                }
                            }
                        }
                        return null;
                    });
                }
            }
        }
        return null;
    }
}
