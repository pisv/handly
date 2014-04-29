/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooElement;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.xtext.util.Strings;

/**
 * Foo label provider.
 */
public class FooLabelProvider
    extends LabelProvider
{
    private ResourceManager resourceManager = new LocalResourceManager(
        JFaceResources.getResources());

    @Override
    public String getText(Object element)
    {
        if (element instanceof IFooDef)
        {
            IFooDef def = (IFooDef)element;
            if (def.getArity() == 0)
                return def.getName() + "()";
            try
            {
                return def.getName()
                    + '('
                    + Strings.concat(", ",
                        Arrays.asList(def.getParameterNames())) + ')';
            }
            catch (CoreException e)
            {
            }
        }
        if (element instanceof IFooElement)
            return ((IFooElement)element).getName();
        if (element instanceof IResource)
            return ((IResource)element).getName();
        return super.getText(element);
    };

    @Override
    public Image getImage(Object element)
    {
        if (element instanceof IFooDef)
            return Activator.getImage(Activator.IMG_OBJ_DEF);
        if (element instanceof IFooVar)
            return Activator.getImage(Activator.IMG_OBJ_VAR);
        IResource resource = null;
        if (element instanceof IFooProject || element instanceof IFooFile)
            resource = ((IFooElement)element).getResource();
        if (element instanceof IResource)
            resource = (IResource)element;
        if (resource != null)
        {
            IWorkbenchAdapter adapter =
                (IWorkbenchAdapter)resource.getAdapter(IWorkbenchAdapter.class);
            if (adapter != null)
                return (Image)resourceManager.get(adapter.getImageDescriptor(resource));
        }
        return super.getImage(element);
    }

    @Override
    public void dispose()
    {
        resourceManager.dispose();
        super.dispose();
    }
}
