/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Classpath utilities.
 */
class ClasspathUtil
{
    /**
     * Returns whether the given resource is a source folder
     * on the given classpath.
     */
    static boolean isSourceFolder(IResource resource,
        IClasspathEntry[] classpath)
    {
        if (resource.getType() == IResource.FOLDER
            || resource.getType() == IResource.PROJECT)
        {
            IClasspathEntry entry = findClasspathEntry(classpath,
                resource.getFullPath());
            if (entry != null
                && entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the first classpath entry with the given path,
     * or <code>null</code> if none.
     */
    static IClasspathEntry findClasspathEntry(IClasspathEntry[] classpath,
        IPath entryPath)
    {
        for (IClasspathEntry entry : classpath)
        {
            if (entryPath.equals(entry.getPath()))
                return entry;
        }
        return null;
    }

    private ClasspathUtil()
    {
    }
}
