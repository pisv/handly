/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC and others.
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
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * A partial implementation of {@link IWorkingSetElementAdapter}.
 * <p>
 * Working set element adapters are capable of transforming possible working
 * set content into the most applicable form. Each model may opt to provide an
 * element adapter for its working sets via the <code>elementAdapterClass</code>
 * attribute of the <code>org.eclipse.ui.workingSets</code> extension point.
 * The workbench will use the element adapter to help manage addition
 * of elements to working sets for the model.
 * </p>
 */
public abstract class AbstractWorkingSetElementAdapter
    implements IWorkingSetElementAdapter
{
    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through the given elements in order.
     * If an element is {@link #isModelElement(IAdaptable) native} to the
     * underlying model, it is added to a result set. Otherwise, if it could
     * be adapted to an {@link IResource}, the resource is passed to the {@link
     * #adaptFromResource(IResource)} method and the result is then added to
     * the result set. Lastly, this implementation {@link #postProcess(Collection)
     * post-processes} the result set before returning it.
     */
    @Override
    public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements)
    {
        Set<IAdaptable> result = new LinkedHashSet<>(elements.length);
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
     * Returns whether the given element is native to the underlying model.
     *
     * @param element never <code>null</code>
     * @return <code>true</code> if the given element is native to the model,
     *  and <code>false</code> otherwise
     */
    protected abstract boolean isModelElement(IAdaptable element);

    /**
     * Attempts to adapt the given resource to the corresponding element
     * of the underlying model.
     *
     * @param resource never <code>null</code>
     * @return the (possibly adapted) resource
     */
    protected abstract IAdaptable adaptFromResource(IResource resource);

    /**
     * Post-processes the collection of elements to be returned from
     * the {@link #adaptElements(IWorkingSet, IAdaptable[])} method.
     * <p>
     * Default implementation does nothing. Subclasses may override.
     * </p>
     *
     * @param result the collection of elements to post-process
     *  (never <code>null</code>)
     */
    protected void postProcess(Collection<IAdaptable> result)
    {
    }
}
