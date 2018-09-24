/*******************************************************************************
 * Copyright (c) 2015, 2018 itemis AG (http://www.itemis.eu) and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.editor;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;

/**
 * Adaptation of <code>JvmTypesAwareDirtyStateEditorSupport</code> for Handly
 * reconciling story.
 * <p>
 * If you extend <code>DefaultCommonTypesUiModule</code> (directly or indirectly)
 * and have {@link HandlyXtextDocument} bound, bind this class in place of the
 * default <code>JvmTypesAwareDirtyStateEditorSupport</code>:
 * </p>
 * <pre>
 * public Class&lt;? extends DirtyStateEditorSupport&gt; bindDirtyStateEditorSupport() {
 *     return HandlyJvmTypesAwareDirtyStateEditorSupport.class;
 * }
 * </pre>
 */
public class HandlyJvmTypesAwareDirtyStateEditorSupport
    extends HandlyDirtyStateEditorSupport
{
    private static final URI OBJECTS_URI = URI.createURI("java:/Objects"); //$NON-NLS-1$

    @Override
    protected void processDelta(IResourceDescription.Delta delta,
        Resource context, List<Resource> result)
    {
        super.processDelta(delta, context, result);
        ResourceSet resourceSet = context.getResourceSet();
        if (delta.getNew() != null)
        {
            Iterable<IEObjectDescription> exportedJvmTypes =
                delta.getNew().getExportedObjectsByType(
                    TypesPackage.Literals.JVM_GENERIC_TYPE);
            for (IEObjectDescription jvmTypeDesc : exportedJvmTypes)
            {
                URI uriToJvmType = OBJECTS_URI.appendSegment(
                    jvmTypeDesc.getQualifiedName().toString());
                Resource jvmResourceInResourceSet = resourceSet.getResource(
                    uriToJvmType, false);
                if (jvmResourceInResourceSet != null)
                    result.add(jvmResourceInResourceSet);
            }
        }
    }
}
