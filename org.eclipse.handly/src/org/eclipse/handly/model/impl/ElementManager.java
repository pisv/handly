/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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

import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceFile;

/**
 * Manages handle/body relationships for a Handly-based model.
 * Generally, each model will have its own instance of the element manager.
 * <p>
 * An instance of this class is safe for use by multiple threads.
 * </p>
 *
 * @see Element#hElementManager()
 */
public class ElementManager
{
    private static final ISourceFile[] NO_WORKING_COPIES = new ISourceFile[0];

    private IBodyCache cache;

    // Temporary cache of newly opened elements
    private ThreadLocal<Map<IElement, Object>> temporaryCache =
        new ThreadLocal<>();

    private Map<ISourceFile, WorkingCopyInfo> workingCopyInfos =
        new HashMap<>();

    /**
     * Constructs an element manager with the given body cache.
     *
     * @param cache the body cache to be used by the element manager
     *  (not <code>null</code>)
     */
    public ElementManager(IBodyCache cache)
    {
        if (cache == null)
            throw new IllegalArgumentException();

        this.cache = cache;
    }

    /**
     * Returns the source files that have corresponding working copy info.
     */
    public final synchronized ISourceFile[] getWorkingCopies()
    {
        return workingCopyInfos.keySet().toArray(NO_WORKING_COPIES);
    }

    /**
     * A handle/body relationship is going to be removed from the body cache
     * associated with this manager. Do any necessary cleanup.
     * <p>
     * This method is called internally; it is not intended to be called by clients.
     * </p>
     *
     * @param element the element whose body is going to be removed
     *  (never <code>null</code>)
     * @param body the corresponding body that is going to be removed
     *  (never <code>null</code>)
     */
    protected void removing(IElement element, Object body)
    {
        ((Element)element).hRemoving(body);
    }

    /**
     * Given a body, closes the children of the given element. If the current
     * state of a child element does not permit closing (e.g., a working copy),
     * it will stay open. Closing of an element usually involves closing its
     * children and removal of its body from the cache.
     * <p>
     * This method is called internally; it is not intended to be called by clients.
     * </p>
     *
     * @param element the element whose children need to be closed
     *  (never <code>null</code>)
     * @param body the body corresponding to the given element
     *  (never <code>null</code>)
     */
    protected void closeChildren(IElement element, Object body)
    {
        for (IElement child : (((Element)element).hChildren(body)))
        {
            ((Element)child).hClose(false);
        }
    }

    /**
     * Returns the corresponding body for the given element, or
     * <code>null</code> if no body is registered for the element.
     * <p>
     * Checks the temporary cache first. If the current thread has no temporary
     * cache or it contains no body for the element, checks the body cache
     * associated with this manager. Performs atomically.
     * </p>
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if no body is registered for the element
     */
    synchronized Object get(IElement element)
    {
        Map<IElement, Object> tempCache = temporaryCache.get();
        if (tempCache != null)
        {
            Object body = tempCache.get(element);
            if (body != null)
                return body;
        }
        return cache.get(element);
    }

    /**
     * Returns the corresponding body for the given element without
     * disturbing cache ordering, or <code>null</code> if no body
     * is registered for the element.
     * <p>
     * Checks the temporary cache first. If the current thread has no temporary
     * cache or it contains no body for the element, checks the body cache
     * associated with this manager. Performs atomically.
     * </p>
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if no body is registered for the element
     */
    synchronized Object peek(IElement element)
    {
        Map<IElement, Object> tempCache = temporaryCache.get();
        if (tempCache != null)
        {
            Object body = tempCache.get(element);
            if (body != null)
                return body;
        }
        return cache.peek(element);
    }

    /**
     * Atomically updates the body cache associated with this manager with the
     * provided handle/body relationships.
     *
     * @param element the element being (re-)opened (not <code>null</code>)
     * @param newElements a map containing handle/body relationships
     *  to be stored in the body cache (not <code>null</code>). At a minimum,
     *  it must contain a body for the given element
     */
    synchronized void put(IElement element, Map<IElement, Object> newElements)
    {
        // remove existing children as they are replaced with the new children contained in newElements
        remove(element);

        cache.putAll(newElements);

        if (element instanceof ISourceFile)
        {
            WorkingCopyInfo info = workingCopyInfos.get((ISourceFile)element);
            if (info != null && !info.created) // case of wc creation
                info.created = true;
        }
    }

    /**
     * If a body for the given element is not already present in the body cache
     * associated with this manager, puts the provided handle/body relationships
     * into the body cache. Performs atomically.
     *
     * @param element the element being opened (not <code>null</code>)
     * @param newElements a map containing handle/body relationships
     *  to be stored in the body cache (not <code>null</code>). At a minimum,
     *  it must contain a body for the given element
     * @return the previous body for the given element, or <code>null</code>
     *  if the body cache did not previously contain a body for the element
     */
    synchronized Object putIfAbsent(IElement element,
        Map<IElement, Object> newElements)
    {
        Object existingBody = cache.peek(element);
        if (existingBody != null)
            return existingBody;

        cache.putAll(newElements);
        return null;
    }

    /**
     * Removes from the body cache associated with this manager the cached body
     * for the given element after closing its children. Does nothing if the cache
     * contained no body for the element. Performs atomically.
     *
     * @param element the element whose body is to be removed from the body cache
     * @see #removing(IElement, Object)
     * @see #closeChildren(IElement, Object)
     */
    synchronized void remove(IElement element)
    {
        Object body = cache.peek(element);
        if (body != null)
        {
            removing(element, body);
            closeChildren(element, body);
            cache.remove(element);
        }
    }

    /**
     * Returns the temporary cache of handle/body relationships for the current
     * thread, creating it if needed.
     *
     * @return the temporary cache of handle/body relationships
     *  for the current thread (never <code>null</code>)
     */
    Map<IElement, Object> getTemporaryCache()
    {
        Map<IElement, Object> result = temporaryCache.get();
        if (result == null)
        {
            result = new HashMap<>();
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
     * @param sourceFile the source file with which a working copy info
     *  is to be associated (not <code>null</code>)
     * @param buffer the working copy buffer (not <code>null</code>)
     * @param factory the working copy info factory, or <code>null</code>
     *  if a default factory is to be used
     * @return the previous working copy info associated with the given
     *  source file, or <code>null</code> if there was no working copy info
     *  for the source file
     * @see #discardWorkingCopyInfo(ISourceFile)
     */
    WorkingCopyInfo putWorkingCopyInfoIfAbsent(ISourceFile sourceFile,
        IWorkingCopyBuffer buffer, IWorkingCopyInfoFactory factory)
    {
        if (sourceFile == null)
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
                    WorkingCopyInfo oldInfo = workingCopyInfos.get(sourceFile);
                    if (oldInfo != null)
                        oldInfo.refCount++;
                    else
                    {
                        workingCopyInfos.put(sourceFile, info);
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
     * @param sourceFile the source file whose working copy info is to be returned
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file has no working copy info
     * @see #discardWorkingCopyInfo(ISourceFile)
     */
    synchronized WorkingCopyInfo getWorkingCopyInfo(ISourceFile sourceFile)
    {
        WorkingCopyInfo info = workingCopyInfos.get(sourceFile);
        if (info != null)
            info.refCount++;
        return info;
    }

    /**
     * Returns the working copy info for the given source file without
     * incrementing the reference count for the info.
     *
     * @param sourceFile the source file whose working copy info is to be returned
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file has no working copy info
     */
    synchronized WorkingCopyInfo peekAtWorkingCopyInfo(ISourceFile sourceFile)
    {
        return workingCopyInfos.get(sourceFile);
    }

    /**
     * Decrements the reference count of the working copy info associated with
     * the given source file. If there are no remaining references, removes the
     * working copy info and releases the working copy buffer. Has no effect if
     * there was no working copy info for the source file. Performs atomically.
     *
     * @param sourceFile the source file whose working copy info is to be discarded
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file had no working copy info
     */
    WorkingCopyInfo discardWorkingCopyInfo(ISourceFile sourceFile)
    {
        WorkingCopyInfo infoToDispose = null;
        try
        {
            synchronized (this)
            {
                WorkingCopyInfo info = workingCopyInfos.get(sourceFile);
                if (info != null && --info.refCount == 0)
                {
                    infoToDispose = info;

                    workingCopyInfos.remove(sourceFile);
                    remove(sourceFile);
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
