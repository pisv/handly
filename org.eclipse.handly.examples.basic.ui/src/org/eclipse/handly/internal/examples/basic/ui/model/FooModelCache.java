/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.util.HashMap;

import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.Body;
import org.eclipse.handly.model.impl.support.ElementCache;
import org.eclipse.handly.model.impl.support.IBodyCache;

/**
 * The Foo Model cache.
 */
class FooModelCache
    implements IBodyCache
{
    private static final int DEFAULT_PROJECT_SIZE = 5;
    private static final int DEFAULT_FILE_SIZE = 100;
    private static final int DEFAULT_CHILDREN_SIZE = DEFAULT_FILE_SIZE * 20; // average 20 children per file

    // The memory ratio that should be applied to the above constants.
    private final double memoryRatio = getMemoryRatio();

    private Object modelBody; // Foo model element's body
    private HashMap<IElement, Object> projectCache; // cache of open Foo projects
    private ElementCache fileCache; // cache of open Foo files
    private HashMap<IElement, Object> childrenCache; // cache of children of open Foo files

    public FooModelCache()
    {
        // set the size of the caches as a function of the maximum amount of memory available
        projectCache = new HashMap<>(DEFAULT_PROJECT_SIZE);
        fileCache = new ElementCache((int)(DEFAULT_FILE_SIZE * memoryRatio));
        childrenCache = new HashMap<>((int)(DEFAULT_CHILDREN_SIZE
            * memoryRatio));
    }

    @Override
    public Object get(IElement element)
    {
        if (element instanceof IFooModel)
            return modelBody;
        else if (element instanceof IFooProject)
            return projectCache.get(element);
        else if (element instanceof IFooFile)
            return fileCache.get(element);
        else
            return childrenCache.get(element);
    }

    @Override
    public Object peek(IElement element)
    {
        if (element instanceof IFooModel)
            return modelBody;
        else if (element instanceof IFooProject)
            return projectCache.get(element);
        else if (element instanceof IFooFile)
            return fileCache.peek(element);
        else
            return childrenCache.get(element);
    }

    @Override
    public void put(IElement element, Object body)
    {
        if (element instanceof IFooModel)
            modelBody = body;
        else if (element instanceof IFooProject)
        {
            projectCache.put(element, body);
            fileCache.ensureSpaceLimit(((Body)body).getChildren().length,
                element);
        }
        else if (element instanceof IFooFile)
            fileCache.put(element, body);
        else
            childrenCache.put(element, body);
    }

    @Override
    public void remove(IElement element)
    {
        if (element instanceof IFooModel)
            modelBody = null;
        else if (element instanceof IFooProject)
        {
            projectCache.remove(element);
            fileCache.resetSpaceLimit((int)(DEFAULT_FILE_SIZE * memoryRatio),
                element);
        }
        else if (element instanceof IFooFile)
            fileCache.remove(element);
        else
            childrenCache.remove(element);
    }

    /*
     * Adapted from org.eclipse.jdt.internal.core.JavaModelCache#getMemoryRatio()
     */
    private double getMemoryRatio()
    {
        long maxMemory = Runtime.getRuntime().maxMemory();
        // if max memory is infinite, set the ratio to 4d
        // which corresponds to the 256MB that Eclipse defaults to
        // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=111299)
        return maxMemory == Long.MAX_VALUE ? 4d : ((double)maxMemory) / (64
            * 0x100000); // 64MB is the base memory for most JVM
    }
}
