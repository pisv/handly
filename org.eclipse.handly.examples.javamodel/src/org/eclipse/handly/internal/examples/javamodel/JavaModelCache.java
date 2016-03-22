/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.handly.examples.javamodel.ICompilationUnit;
import org.eclipse.handly.examples.javamodel.IJavaModel;
import org.eclipse.handly.examples.javamodel.IJavaProject;
import org.eclipse.handly.examples.javamodel.IPackageFragment;
import org.eclipse.handly.examples.javamodel.IPackageFragmentRoot;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.ElementCache;
import org.eclipse.handly.model.impl.IBodyCache;

/**
 * The Java model cache.
 */
class JavaModelCache
    implements IBodyCache
{
    private static final int DEFAULT_PROJECT_SIZE = 5;
    private static final int DEFAULT_ROOT_SIZE = 50;
    private static final int DEFAULT_PKG_SIZE = 500;
    private static final int DEFAULT_FILE_SIZE = 250;
    private static final int DEFAULT_CHILDREN_SIZE = DEFAULT_FILE_SIZE * 20; // average 20 children per file

    // The memory ratio that should be applied to the above constants.
    private final double memoryRatio = getMemoryRatio();

    private Body modelBody; // Java model element's body
    private HashMap<IElement, Body> projectCache; // cache of open Java projects
    private ElementCache rootCache; // cache of open package fragment roots
    private ElementCache pkgCache; // cache of open package fragments
    private ElementCache fileCache; // cache of open Java files
    private HashMap<IElement, Body> childrenCache; // cache of children of open Foo files

    public JavaModelCache()
    {
        // set the size of the caches as a function of the maximum amount of memory available
        projectCache = new HashMap<IElement, Body>(DEFAULT_PROJECT_SIZE);
        rootCache = new ElementCache((int)(DEFAULT_ROOT_SIZE * memoryRatio));
        pkgCache = new ElementCache((int)(DEFAULT_PKG_SIZE * memoryRatio));
        fileCache = new ElementCache((int)(DEFAULT_FILE_SIZE * memoryRatio));
        childrenCache = new HashMap<IElement, Body>((int)(DEFAULT_CHILDREN_SIZE
            * memoryRatio));
    }

    @Override
    public Body get(IElement element)
    {
        if (element instanceof IJavaModel)
            return modelBody;
        else if (element instanceof IJavaProject)
            return projectCache.get(element);
        else if (element instanceof IPackageFragmentRoot)
            return rootCache.get(element);
        else if (element instanceof IPackageFragment)
            return pkgCache.get(element);
        else if (element instanceof ICompilationUnit)
            return fileCache.get(element);
        else
            return childrenCache.get(element);
    }

    @Override
    public Body peek(IElement element)
    {
        if (element instanceof IJavaModel)
            return modelBody;
        else if (element instanceof IJavaProject)
            return projectCache.get(element);
        else if (element instanceof IPackageFragmentRoot)
            return rootCache.peek(element);
        else if (element instanceof IPackageFragment)
            return pkgCache.peek(element);
        else if (element instanceof ICompilationUnit)
            return fileCache.peek(element);
        else
            return childrenCache.get(element);
    }

    @Override
    public void put(IElement element, Body body)
    {
        if (element instanceof IJavaModel)
            modelBody = body;
        else if (element instanceof IJavaProject)
        {
            projectCache.put(element, body);
            rootCache.ensureSpaceLimit(body, element);
        }
        else if (element instanceof IPackageFragmentRoot)
        {
            rootCache.put(element, body);
            pkgCache.ensureSpaceLimit(body, element);
        }
        else if (element instanceof IPackageFragment)
        {
            pkgCache.put(element, body);
            fileCache.ensureSpaceLimit(body, element);
        }
        else if (element instanceof ICompilationUnit)
            fileCache.put(element, body);
        else
            childrenCache.put(element, body);
    }

    @Override
    public void putAll(Map<IElement, Body> elements)
    {
        for (Map.Entry<IElement, Body> entry : elements.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void remove(IElement element)
    {
        if (element instanceof IJavaModel)
            modelBody = null;
        else if (element instanceof IJavaProject)
        {
            projectCache.remove(element);
            rootCache.resetSpaceLimit((int)(DEFAULT_ROOT_SIZE * memoryRatio),
                element);
        }
        else if (element instanceof IPackageFragmentRoot)
        {
            rootCache.remove(element);
            pkgCache.resetSpaceLimit((int)(DEFAULT_PKG_SIZE * memoryRatio),
                element);
        }
        else if (element instanceof IPackageFragment)
        {
            pkgCache.remove(element);
            fileCache.resetSpaceLimit((int)(DEFAULT_FILE_SIZE * memoryRatio),
                element);
        }
        else if (element instanceof ICompilationUnit)
            fileCache.remove(element);
        else
            childrenCache.remove(element);
    }

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
