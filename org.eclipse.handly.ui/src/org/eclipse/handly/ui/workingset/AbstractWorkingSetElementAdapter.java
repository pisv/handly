/*******************************************************************************
 * Copyright (c) 2015 1C LLC.
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
package org.eclipse.handly.ui.workingset;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * A partial implementation of {@link IWorkingSetElementAdapter}.
 * Instances of this class are capable of transforming possible
 * working set content into the most applicable form.
 * <p>
 * Each model may opt to provide an element adapter for its working sets
 * (via the <code>elementAdapterClass</code> attribute of the
 * <code>org.eclipse.ui.workingSets</code> extension point).
 * That adapter will then be used by the workbench to help manage addition
 * of elements to working sets for the model.
 * </p>
 *
 * @see #isModelElement(IAdaptable)
 * @see #adaptFromResource(IResource)
 */
public abstract class AbstractWorkingSetElementAdapter
    implements IWorkingSetElementAdapter
{
    @Override
    public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements)
    {
        HashSet<IAdaptable> result = new HashSet<IAdaptable>(elements.length);
        for (IAdaptable element : elements)
        {
            if (isModelElement(element))
            {
                result.add(element);
            }
            else
            {
                IResource resource = ResourceUtil.getResource(element);
                if (resource != null)
                {
                    element = adaptFromResource(resource);
                    if (element != null)
                        result.add(element);
                }
            }
        }
        postProcess(result);
        return result.toArray(new IAdaptable[result.size()]);
    }

    @Override
    public void dispose()
    {
    }

    /**
     * Returns whether the given element is native to the model.
     *
     * @param element (never <code>null</code>)
     * @return <code>true</code> if the given element is native to the model;
     *  <code>false</code> otherwise
     */
    protected abstract boolean isModelElement(IAdaptable element);

    /**
     * Tries to adapt the given resource to the corresponding element of the model.
     *
     * @param resource the resource to adapt (never <code>null</code>)
     * @return the (possibly adapted) resource
     */
    protected abstract IAdaptable adaptFromResource(IResource resource);

    /**
     * Post-processes the collection of elements to be returned by
     * {@link #adaptElements} method.
     * <p>
     * Default implementation does nothing. Subclasses may override.
     * </p>
     *
     * @param result the collection of elements to post-process
     */
    protected void postProcess(Collection<IAdaptable> result)
    {
    }
}
