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

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyKind;
import org.eclipse.handly.xtext.ui.callhierarchy.XtextCallHierarchyNode;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.xtext.ui.Activator;

import com.google.inject.Injector;

abstract class XtextXtextCallHierarchyNode
    extends XtextCallHierarchyNode
{
    XtextXtextCallHierarchyNode(XtextXtextCallHierarchyNode parent,
        Element element)
    {
        super(parent, element);
    }

    @Override
    protected URI getUri()
    {
        return ((Element)getElement()).getUri();
    }

    @Override
    protected XtextXtextCallHierarchyUtility getCallHierarchyUtility()
    {
        return getInjector().getInstance(XtextXtextCallHierarchyUtility.class);
    }

    private static Injector getInjector()
    {
        return Activator.getDefault().getInjector(
            Activator.ORG_ECLIPSE_XTEXT_XTEXT);
    }
}

final class XtextXtextCallerHierarchyNode
    extends XtextXtextCallHierarchyNode
{
    XtextXtextCallerHierarchyNode(XtextXtextCallerHierarchyNode parent,
        Element element)
    {
        super(parent, element);
    }

    @Override
    public CallHierarchyKind getKind()
    {
        return CallHierarchyKind.CALLER;
    }

    @Override
    protected XtextCallHierarchyNode createChildIfAbsent(
        Map<URI, XtextCallHierarchyNode> children,
        IReferenceDescription callReference)
    {
        IEObjectDescription description =
            getCallHierarchyUtility().getDescription(
                callReference.getContainerEObjectURI());
        if (description == null)
            return null;
        return children.computeIfAbsent(description.getEObjectURI(),
            uri -> new XtextXtextCallerHierarchyNode(this, new Element(
                description, getCallHierarchyUtility().getGrammarDescription(
                    uri))));
    }
}

final class XtextXtextCalleeHierarchyNode
    extends XtextXtextCallHierarchyNode
{
    XtextXtextCalleeHierarchyNode(XtextXtextCalleeHierarchyNode parent,
        Element element)
    {
        super(parent, element);
    }

    @Override
    public CallHierarchyKind getKind()
    {
        return CallHierarchyKind.CALLEE;
    }

    @Override
    protected XtextCallHierarchyNode createChildIfAbsent(
        Map<URI, XtextCallHierarchyNode> children,
        IReferenceDescription callReference)
    {
        IEObjectDescription description =
            getCallHierarchyUtility().getRuleDescription(
                callReference.getTargetEObjectUri());
        if (description == null)
            return null;
        return children.computeIfAbsent(description.getEObjectURI(),
            uri -> new XtextXtextCalleeHierarchyNode(this, new Element(
                description, getCallHierarchyUtility().getGrammarDescription(
                    uri))));
    }
}
