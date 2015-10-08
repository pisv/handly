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
 * <p>
 * An instance of this class is safe for use by multiple threads.
 * </p>
 *
 * @see Handle#getHandleManager()
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
     * associated with this manager. Performs atomically.
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
     * associated with this manager. Performs atomically.
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

        if (handle instanceof ISourceFile)
        {
            WorkingCopyInfo info = workingCopyInfos.get((ISourceFile)handle);
            if (info != null && !info.created) // case of wc creation
                info.created = true;
        }
    }

    /**
     * If a body for the given handle is not already present in the body cache
     * associated with this manager, updates the body cache. Performs atomically.
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
     * descendants. Performs atomically.
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
     * If the given source file is not already associated with a working copy
     * info, uses the given factory to create a working copy info holding the
     * given working copy buffer, associates the source file with the created
     * info, and acquires a new independent ownership of the working copy buffer.
     * Otherwise, returns the working copy info already associated with the
     * source file; the given buffer and factory are ignored. Increments the
     * reference count of the working copy info associated with the source file.
     * Performs atomically.
     * <p>
     * Each successful call to this method must ultimately be followed
     * by exactly one call to <code>discardWorkingCopyInfo</code>.
     * </p>
     *
     * @param handle the source file with which a working copy info
     *  is to be associated (not <code>null</code>)
     * @param buffer the working copy buffer (not <code>null</code>)
     * @param factory the working copy info factory, or <code>null</code>
     *  if a default factory is to be used
     * @return the previous working copy info associated with the given
     *  source file, or <code>null</code> if there was no working copy info
     *  for the source file
     * @see #discardWorkingCopyInfo(ISourceFile)
     */
    WorkingCopyInfo putWorkingCopyInfoIfAbsent(ISourceFile handle,
        IWorkingCopyBuffer buffer, IWorkingCopyInfoFactory factory)
    {
        if (handle == null)
            throw new IllegalArgumentException();
        if (buffer == null)
            throw new IllegalArgumentException();

        final WorkingCopyInfo info;
        if (factory == null)
            info = new WorkingCopyInfo(buffer);
        else
            info = factory.createWorkingCopyInfo(buffer);
        if (info.refCount != 0)
            throw new AssertionError();
        boolean disposeInfo = true;
        try
        {
            if (info.getBuffer() != buffer)
                throw new AssertionError();
            buffer.addRef();
            boolean releaseBuffer = true;
            try
            {
                synchronized (this)
                {
                    WorkingCopyInfo oldInfo = workingCopyInfos.get(handle);
                    if (oldInfo != null)
                        oldInfo.refCount++;
                    else
                    {
                        workingCopyInfos.put(handle, info);
                        info.refCount = 1;
                        releaseBuffer = disposeInfo = false;
                    }
                    return oldInfo;
                }
            }
            finally
            {
                if (releaseBuffer)
                    buffer.release();
            }
        }
        finally
        {
            if (disposeInfo)
                info.dispose();
        }
    }

    /**
     * Returns the working copy info for the given source file, incrementing
     * the reference count for the info. Returns <code>null</code> if the
     * source file has no working copy info. Performs atomically.
     * <p>
     * Each successful call to this method that did not return
     * <code>null</code> must ultimately be followed by exactly
     * one call to <code>discardWorkingCopyInfo</code>.
     * </p>
     *
     * @param handle the source file whose working copy info is to be returned
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file has no working copy info
     * @see #discardWorkingCopyInfo(ISourceFile)
     */
    synchronized WorkingCopyInfo getWorkingCopyInfo(ISourceFile handle)
    {
        WorkingCopyInfo info = workingCopyInfos.get(handle);
        if (info != null)
            info.refCount++;
        return info;
    }

    /**
     * Returns the working copy info for the given source file without
     * incrementing the reference count for the info.
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
     * Decrements the reference count of the working copy info associated with
     * the given source file. If there are no remaining references, removes the
     * working copy info and releases the working copy buffer. Has no effect if
     * there was no working copy info for the source file. Performs atomically.
     *
     * @param handle the source file whose working copy info is to be discarded
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file had no working copy info
     */
    WorkingCopyInfo discardWorkingCopyInfo(ISourceFile handle)
    {
        WorkingCopyInfo infoToDispose = null;
        try
        {
            synchronized (this)
            {
                WorkingCopyInfo info = workingCopyInfos.get(handle);
                if (info != null && --info.refCount == 0)
                {
                    infoToDispose = info;

                    workingCopyInfos.remove(handle);
                    removeBodyAndChildren(handle);
                }
                return info;
            }
        }
        finally
        {
            if (infoToDispose != null)
            {
                infoToDispose.getBuffer().release();
                infoToDispose.dispose();
            }
        }
    }
}
