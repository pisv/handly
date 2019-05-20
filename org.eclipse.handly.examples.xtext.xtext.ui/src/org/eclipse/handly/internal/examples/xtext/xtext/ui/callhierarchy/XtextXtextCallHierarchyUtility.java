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

import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.handly.xtext.ui.callhierarchy.XtextCallHierarchyUtility;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.ui.editor.findrefs.EditorResourceAccess;
import org.eclipse.xtext.util.ITextRegion;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
final class XtextXtextCallHierarchyUtility
    extends XtextCallHierarchyUtility
{
    @Inject
    XtextXtextCallHierarchyUtility(EditorResourceAccess resourceAccess,
        IResourceDescriptions indexData)
    {
        setResourceAccess(resourceAccess);
        setIndexData(indexData);
    }

    @Override
    protected boolean isCallReference(IReferenceDescription reference)
    {
        return super.isCallReference(reference) && isRule(
            reference.getEReference().getEReferenceType());
    }

    @Override
    protected ITextRegion getCallRegion(EObject sourceObject,
        EReference callReference, int indexInList)
    {
        Assignment assignment = GrammarUtil.containingAssignment(sourceObject);
        if (assignment != null)
            return locationInFileProvider.getSignificantTextRegion(assignment);

        return super.getCallRegion(sourceObject, callReference, indexInList);
    }

    IEObjectDescription getRuleDescription(URI objectUri)
    {
        IEObjectDescription description = getDescription(objectUri);
        if (description != null && isRule(description.getEClass()))
            return description;

        return readOnly(objectUri, object -> getDescription(
            EcoreUtil2.getContainerOfType(object, AbstractRule.class)));
    }

    IEObjectDescription getGrammarDescription(URI objectUri)
    {
        IResourceDescription resourceDescription =
            indexData.getResourceDescription(objectUri.trimFragment());
        if (resourceDescription != null)
        {
            Iterator<IEObjectDescription> it =
                resourceDescription.getExportedObjectsByType(
                    XtextPackage.Literals.GRAMMAR).iterator();
            if (it.hasNext())
                return it.next();
        }
        return null;
    }

    IEObjectDescription getDescription(URI objectUri)
    {
        IResourceDescription resourceDescription =
            indexData.getResourceDescription(objectUri.trimFragment());
        if (resourceDescription != null)
        {
            Iterable<IEObjectDescription> descriptions =
                resourceDescription.getExportedObjects();
            for (IEObjectDescription description : descriptions)
            {
                if (objectUri.equals(description.getEObjectURI()))
                    return description;
            }
        }
        return null;
    }

    IEObjectDescription getDescription(EObject object)
    {
        if (object != null)
        {
            Iterator<IEObjectDescription> it =
                indexData.getExportedObjectsByObject(object).iterator();
            if (it.hasNext())
                return it.next();
        }
        return null;
    }

    static boolean isRule(EClass someClass)
    {
        return XtextPackage.Literals.ABSTRACT_RULE.isSuperTypeOf(someClass);
    }
}
