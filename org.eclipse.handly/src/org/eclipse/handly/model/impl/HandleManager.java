/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.ISourceFile;

/**
 * Manages handle/body relationships for a handle-based model.
 * Generally, each model will have its own instance of the handle manager.
 *
 * @see Handle#getHandleManager()
 * @threadsafe This class is intended to be thread-safe
 */
public class HandleManager
{
    private IBodyCache cache;

    // Temporary cache of newly opened elements
    private ThreadLocal<Map<IHandle, Body>> temporaryCache =
        new ThreadLocal<Map<IHandle, Body>>();

    private Map<ISourceFile, WorkingCopyInfo> workingCopyInfos =
        new HashMap<ISourceFile, WorkingCopyInfo>();

    /**
     * Constructs a handle manager with the given body cache.
     *
     * @param cache the body cache to be used by the handle manager
     *  (not <code>null</code>)
     */
    public HandleManager(IBodyCache cache)
    {
        if (cache == null)
            throw new IllegalArgumentException();

        this.cache = cache;
    }

    /**
     * Returns the corresponding body for the given handle, or
     * <code>null</code> if no body is registered for the handle.
     * <p>
     * Checks the temporary cache first. If the current thread has no temporary
     * cache or it contains no body for the handle, checks the body cache
     * associated with this manager.
     * </p>
     *
     * @param handle the handle whose body is to be returned
     * @return the corresponding body for the given handle, or
     *  <code>null</code> if no body is registered for the handle
     */
    synchronized Body get(IHandle handle)
    {
        Map<IHandle, Body> tempCache = temporaryCache.get();
        if (tempCache != null)
        {
            Body body = tempCache.get(handle);
            if (body != null)
                return body;
        }
        return cache.get(handle);
    }

    /**
     * Returns the corresponding body for the given handle without
     * disturbing cache ordering, or <code>null</code> if no body
     * is registered for the handle.
     * <p>
     * Checks the temporary cache first. If the current thread has no temporary
     * cache or it contains no body for the handle, checks the body cache
     * associated with this manager.
     * </p>
     *
     * @param handle the handle whose body is to be returned
     * @return the corresponding body for the given handle, or
     *  <code>null</code> if no body is registered for the handle
     */
    synchronized Body peek(IHandle handle)
    {
        Map<IHandle, Body> tempCache = temporaryCache.get();
        if (tempCache != null)
        {
            Body body = tempCache.get(handle);
            if (body != null)
                return body;
        }
        return cache.peek(handle);
    }

    /**
     * Atomically updates the body cache associated with this manager.
     *
     * @param handle the element being "opened" (not <code>null</code>)
     * @param newElements a map containing handle/body relationships
     *  to be stored in the body cache (not <code>null</code>). At a minimum,
     *  must contain the body for the given handle
     */
    synchronized void put(IHandle handle, Map<IHandle, Body> newElements)
    {
        // remove existing children as they are replaced with the new children contained in newElements
        removeBodyAndChildren(handle);

        cache.putAll(newElements);
    }

    /**
     * If a body for the given handle is not already present in the body cache
     * associated with this manager, atomically updates the body cache.
     *
     * @param handle the element being "opened" (not <code>null</code>)
     * @param newElements a map containing handle/body relationships
     *  to be stored in the body cache (not <code>null</code>). At a minimum,
     *  must contain the body for the given handle
     * @return the previous body for the given handle, or <code>null</code>
     *  if the body cache did not previously contain a body for the handle
     */
    synchronized Body putIfAbsent(IHandle handle,
        Map<IHandle, Body> newElements)
    {
        Body existingBody = cache.peek(handle);
        if (existingBody != null)
            return existingBody;

        cache.putAll(newElements);
        return null;
    }

    /**
     * Removes from the body cache associated with this manager any previously
     * contained handle/body relationships for the given handle and its existing
     * descendants.
     *
     * @param handle the handle for the root of a subtree of elements
     *  whose existing handle/body relationships are to be removed
     *  from the body cache
     */
    synchronized void removeBodyAndChildren(IHandle handle)
    {
        Body body = cache.peek(handle);
        if (body != null)
        {
            for (IHandle child : body.getChildren())
            {
                removeBodyAndChildren(child);
            }
            cache.remove(handle);
        }
    }

    /**
     * Returns the temporary cache of handle/body relationships for the current
     * thread, creating it if needed.
     *
     * @return the temporary cache of handle/body relationships
     *  for the current thread (never <code>null</code>)
     */
    Map<IHandle, Body> getTemporaryCache()
    {
        Map<IHandle, Body> result = temporaryCache.get();
        if (result == null)
        {
            result = new HashMap<IHandle, Body>();
            temporaryCache.set(result);
        }
        return result;
    }

    /**
     * Returns whether the current thread has the temporary cache set.
     *
     * @return <code>true</code> if the temporary cache is associated with
     *  the current thread, <code>false</code> otherwise
     */
    boolean hasTemporaryCache()
    {
        return temporaryCache.get() != null;
    }

    /**
     * Resets the temporary cache for the current thread. After this method
     * call and before <code>getTemporaryCache()</code> call in the same thread,
     * the thread will have no temporary cache.
     */
    void resetTemporaryCache()
    {
        temporaryCache.set(null);
    }

    /**
     * Creates the working copy info for the given source file via the given
     * factory, or increments the reference count of the existing info.
     * <p>
     * If there is no working copy info for the given source file,
     * the given factory is used to create a working copy info and
     * associate it with the given working copy buffer; the new info
     * is then <code>addRef</code>'ed. If the working copy info for
     * the given source file already exists, but with a different buffer,
     * a runtime exception is thrown. Otherwise, the existing info is
     * <code>addRef</code>'ed.
     * </p>
     *
     * @param handle the source file whose working copy info is to be created
     *  (not <code>null</code>)
     * @param buffer the working copy buffer to be associated with the created
     *  info (not <code>null</code>)
     * @param factory the factory of working copy info (not <code>null</code>)
     * @return <code>true</code> if the working copy info was created;
     *  <code>false</code> if the working copy info already exists
     * @throws IllegalStateException if the working copy info already exists,
     *  but with a different buffer
     */
    synchronized boolean createWorkingCopyInfo(ISourceFile handle,
        IWorkingCopyBuffer buffer, IWorkingCopyInfoFactory factory)
    {
        if (handle == null)
            throw new IllegalArgumentException();
        if (buffer == null)
            throw new IllegalArgumentException();
        if (factory == null)
            throw new IllegalArgumentException();

        boolean created = false;
        WorkingCopyInfo info = workingCopyInfos.get(handle);
        if (info == null)
        {
            info = factory.createWorkingCopyInfo(buffer);
            workingCopyInfos.put(handle, info);
            created = true;
        }
        else if (!buffer.equals(info.getBuffer()))
        {
            throw new IllegalStateException(
                "Already a working copy with another buffer"); //$NON-NLS-1$
        }
        info.addRef();
        return created;
    }

    /**
     * Returns the working copy info for the given source file,
     * incrementing its reference count.
     *
     * @param handle the source file whose working copy info is to be returned
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file has no working copy info
     */
    synchronized WorkingCopyInfo getWorkingCopyInfo(ISourceFile handle)
    {
        WorkingCopyInfo info = workingCopyInfos.get(handle);
        if (info != null)
            info.addRef();
        return info;
    }

    /**
     * Returns the working copy info for the given source file without
     * incrementing its reference count.
     *
     * @param handle the source file whose working copy info is to be returned
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file has no working copy info
     */
    synchronized WorkingCopyInfo peekAtWorkingCopyInfo(ISourceFile handle)
    {
        return workingCopyInfos.get(handle);
    }

    /**
     * Decrements the reference count of the working copy info for the given
     * source file. Removes the working copy info if there are no more
     * references to it. Does nothing if the source file has no
     * working copy info.
     *
     * @param handle the source file whose working copy info is to be discarded
     * @return <code>true</code> if the working copy info for the given
     *  source file was removed; <code>false</code> if the source file
     *  has had no working copy info or there are still references to
     *  the working copy info left
     */
    synchronized boolean discardWorkingCopyInfo(ISourceFile handle)
    {
        WorkingCopyInfo info = workingCopyInfos.get(handle);
        if (info == null || info.release() != 0)
            return false;

        workingCopyInfos.remove(handle);
        removeBodyAndChildren(handle);
        return true;
    }
}
