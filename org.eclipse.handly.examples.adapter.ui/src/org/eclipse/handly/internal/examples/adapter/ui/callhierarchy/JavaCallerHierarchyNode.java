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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.internal.examples.adapter.ui.Activator;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyKind;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyNode;
import org.eclipse.handly.ui.callhierarchy.CallLocation;
import org.eclipse.handly.ui.callhierarchy.ICallHierarchyNode;
import org.eclipse.handly.ui.callhierarchy.ICallLocation;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

final class JavaCallerHierarchyNode
    extends CallHierarchyNode
{
    private static final JavaCallerHierarchyNode[] EMPTY_ARRAY =
        new JavaCallerHierarchyNode[0];

    static JavaCallerHierarchyNode newRootNode(IMethod method)
    {
        return new JavaCallerHierarchyNode(null, method);
    }

    private JavaCallerHierarchyNode(ICallHierarchyNode parent, IMethod method)
    {
        super(parent, method);
    }

    IMethod getMethod()
    {
        return (IMethod)getElement();
    }

    @Override
    public CallHierarchyKind getKind()
    {
        return CallHierarchyKind.CALLER;
    }

    @Override
    protected ICallHierarchyNode[] computeChildren(IProgressMonitor monitor)
    {
        Map<IMethod, JavaCallerHierarchyNode> callerNodes = new HashMap<>();
        SearchEngine engine = new SearchEngine();
        SearchPattern pattern = SearchPattern.createPattern(getMethod(),
            IJavaSearchConstants.REFERENCES);
        SearchRequestor requestor = new SearchRequestor()
        {
            @Override
            public void acceptSearchMatch(SearchMatch match)
                throws CoreException
            {
                if (match.getAccuracy() != SearchMatch.A_ACCURATE
                    || match.isInsideDocComment())
                    return;

                Object element = match.getElement();
                if (!(element instanceof IMethod))
                    return;

                IMethod caller = (IMethod)element;
                IMethod callee = getMethod();

                JavaCallerHierarchyNode callerNode = callerNodes.get(caller);
                if (callerNode == null)
                {
                    callerNode = new JavaCallerHierarchyNode(
                        JavaCallerHierarchyNode.this, caller);
                    callerNodes.put(caller, callerNode);
                }

                String callText = "";
                TextRange callRange = null;
                int lineNumber = ICallLocation.UNKOWN_LINE_NUMBER;

                int offset = match.getOffset();
                int length = match.getLength();
                if (offset >= 0 && length >= 0)
                {
                    callRange = new TextRange(offset, length);

                    IBuffer buffer = caller.getOpenable().getBuffer();
                    if (buffer != null)
                    {
                        try
                        {
                            callText = buffer.getText(offset, length);
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            // buffer is out of sync, ignore
                        }

                        Document document = new Document(buffer.getContents());
                        try
                        {
                            lineNumber = document.getLineOfOffset(offset);
                        }
                        catch (BadLocationException e)
                        {
                            // buffer is out of sync, ignore
                        }
                    }
                }

                callerNode.addCallLocation(new CallLocation(caller, callee,
                    callText, callRange, lineNumber, null));
            }
        };
        try
        {
            engine.search(pattern, new SearchParticipant[] {
                SearchEngine.getDefaultSearchParticipant() },
                SearchEngine.createWorkspaceScope(), requestor, monitor);
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
        }
        return callerNodes.values().toArray(EMPTY_ARRAY);
    }
}
