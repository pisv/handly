/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.adapter;

import org.eclipse.handly.internal.examples.adapter.AdapterModelManager;
import org.eclipse.handly.internal.examples.adapter.JavaHandle;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandle;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Facade to the adapter for the JDT Java model.
 */
public class JavaModelAdapter
{
    /**
     * Returns <code>IHandle</code> corresponding to the given Java element.
     *
     * @param javaElement may be <code>null</code>
     * @return <code>IHandle</code> corresponding to the given Java element,
     *  or <code>null</code> if none
     */
    public static IHandle getHandle(IJavaElement javaElement)
    {
        return JavaHandle.create(javaElement);
    }

    /**
     * Returns the Java element corresponding to the given <code>IHandle</code>.
     *
     * @param handle may be <code>null</code>
     * @return the Java element corresponding to the given <code>IHandle</code>,
     *  or <code>null</code> if none
     */
    public static IJavaElement getJavaElement(IHandle handle)
    {
        if (handle instanceof JavaHandle)
            return ((JavaHandle)handle).getJavaElement();
        return null;
    }

    /**
     * Adds the given listener for changes to elements in the adapter model.
     * Has no effect if an identical listener is already registered.
     * <p>
     * Once registered, a listener starts receiving notification of changes to
     * elements in the adapter model. The listener continues to receive
     * notifications until it is removed.
     * </p>
     *
     * @param listener the listener (not <code>null</code>)
     * @see #removeElementChangeListener(IElementChangeListener)
     */
    public static void addElementChangeListener(IElementChangeListener listener)
    {
        AdapterModelManager.INSTANCE.addElementChangeListener(listener);
    }

    /**
     * Removes the given element change listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener the listener (not <code>null</code>)
     */
    public static void removeElementChangeListener(
        IElementChangeListener listener)
    {
        AdapterModelManager.INSTANCE.removeElementChangeListener(listener);
    }

    private JavaModelAdapter()
    {
    }
}
