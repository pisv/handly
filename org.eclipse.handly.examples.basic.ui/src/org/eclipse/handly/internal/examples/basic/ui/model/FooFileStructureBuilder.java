/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.handly.examples.basic.foo.Def;
import org.eclipse.handly.examples.basic.foo.Unit;
import org.eclipse.handly.examples.basic.foo.Var;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.Body;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.model.impl.support.StructureHelper;
import org.eclipse.handly.util.TextRange;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.ITextRegion;

/**
 * Builds the inner structure for a {@link FooFile}.
 */
class FooFileStructureBuilder
{
    private final Map<IElement, Object> newElements;
    private final ILocationInFileProvider locationProvider;
    private final StructureHelper helper = new StructureHelper();

    /**
     * Constructs a new Foo file structure builder.
     *
     * @param newElements the map to populate with structure elements
     *  (not <code>null</code>)
     * @param resourceServiceProvider Xtext's {@link IResourceServiceProvider}
     *  for the language (not <code>null</code>)
     */
    FooFileStructureBuilder(Map<IElement, Object> newElements,
        IResourceServiceProvider resourceServiceProvider)
    {
        if (newElements == null)
            throw new IllegalArgumentException();
        this.newElements = newElements;
        if (resourceServiceProvider == null)
            throw new IllegalArgumentException();
        this.locationProvider = resourceServiceProvider.get(
            ILocationInFileProvider.class);
    }

    /**
     * Builds the structure for the given {@link FooFile} based on
     * its {@link Unit AST}.
     *
     * @param handle the handle to a Foo file (not <code>null</code>)
     * @param body the body of the Foo file (not <code>null</code>)
     * @param unit the AST of the Foo file (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws OperationCanceledException if this method is canceled
     */
    void buildStructure(FooFile handle, SourceElementBody body, Unit unit,
        IProgressMonitor monitor)
    {
        SubMonitor subMonitor = SubMonitor.convert(monitor,
            unit.getVars().size() + unit.getDefs().size());
        for (Var var : unit.getVars())
        {
            buildStructure(handle, body, var);
            subMonitor.split(1);
        }
        for (Def def : unit.getDefs())
        {
            buildStructure(handle, body, def);
            subMonitor.split(1);
        }
        body.setChildren(helper.popChildren(body).toArray(
            Elements.EMPTY_ARRAY));
    }

    private void buildStructure(FooFile parent, Body parentBody, Var var)
    {
        if (var.getName() == null || var.getName().isEmpty())
            return;

        FooVar handle = new FooVar(parent, var.getName());
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getFullRange(var));
        body.setIdentifyingRange(getIdentifyingRange(var));
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(FooFile parent, Body parentBody, Def def)
    {
        if (def.getName() == null || def.getName().isEmpty())
            return;

        int arity = def.getParams().size();
        FooDef handle = new FooDef(parent, def.getName(), arity);
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getFullRange(def));
        body.setIdentifyingRange(getIdentifyingRange(def));
        body.set(FooDef.PARAMETER_NAMES, def.getParams().toArray(
            new String[arity]));
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private TextRange getFullRange(EObject eObject)
    {
        return toTextRange(locationProvider.getFullTextRegion(eObject));
    }

    private TextRange getIdentifyingRange(EObject eObject)
    {
        return toTextRange(locationProvider.getSignificantTextRegion(eObject));
    }

    private static TextRange toTextRange(ITextRegion region)
    {
        if (region == null || region.equals(ITextRegion.EMPTY_REGION))
            return null;
        else
            return new TextRange(region.getOffset(), region.getLength());
    }
}
