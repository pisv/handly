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
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.impl.Element.CLOSE_HINT;
import static org.eclipse.handly.model.impl.Element.CloseHint.PARENT_CLOSING;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
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

    protected final IBodyCache cache;

    // Temporary cache of newly opened elements
    private ThreadLocal<Map<IElement, Object>> temporaryCache =
        new ThreadLocal<>();

    private Map<SourceFile, WorkingCopyInfo> workingCopyInfos = new HashMap<>();

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
     * Returns the working copies currently managed by this manager.
     * Performs atomically.
     * <p>
     * Note that the result may immediately become stale if other threads can
     * create or destroy working copies that are managed by this manager.
     * </p>
     *
     * @return the working copies currently managed by this manager
     *  (never <code>null</code>)
     */
    public final synchronized ISourceFile[] getWorkingCopies()
    {
        return workingCopyInfos.keySet().toArray(NO_WORKING_COPIES);
    }

    /**
     * Attempts to close the given element according to options specified in the
     * given context. If the current state of an open element does not permit
     * closing (e.g., a working copy), it remains open. Closing of an element
     * generally involves closing its children and removal of its body from the
     * cache.
     * <p>
     * This method is called internally; it is not intended to be invoked by clients.
     * This method is called under the element manager lock.
     * </p>
     *
     * @param element the element that needs closing (never <code>null</code>)
     * @param context the operation context (never <code>null</code>)
     */
    protected void close(IElement element, IContext context)
    {
        ((Element)element).hClose(context);
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
    synchronized Object get(Element element)
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
    synchronized Object peek(Element element)
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
    synchronized void put(Element element, Map<IElement, Object> newElements)
    {
        // remove existing children as they are replaced with the new children contained in newElements
        Object body = cache.peek(element);
        if (body != null)
        {
            for (IElement child : element.hChildren(body))
            {
                close(child, of(CLOSE_HINT, PARENT_CLOSING));
            }
        }

        cache.putAll(newElements);

        if (element instanceof SourceFile)
        {
            WorkingCopyInfo info = workingCopyInfos.get(element);
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
    synchronized Object putIfAbsent(Element element,
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
     * @see #close(IElement, IContext)
     */
    synchronized void remove(Element element)
    {
        Object body = cache.peek(element);
        if (body != null)
        {
            element.hRemoving(body);
            for (IElement child : element.hChildren(body))
            {
                close(child, of(CLOSE_HINT, PARENT_CLOSING));
            }
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
     * by exactly one call to <code>releaseWorkingCopyInfo</code>.
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
     * @see #releaseWorkingCopyInfo(SourceFile)
     */
    WorkingCopyInfo putWorkingCopyInfoIfAbsent(SourceFile sourceFile,
        IBuffer buffer, WorkingCopyInfo.Factory factory)
    {
        if (sourceFile == null)
            throw new IllegalArgumentException();
        if (buffer == null)
            throw new IllegalArgumentException();

        final WorkingCopyInfo info;
        if (factory == null)
            info = new DefaultWorkingCopyInfo(sourceFile, buffer);
        else
            info = factory.newWorkingCopyInfo(sourceFile, buffer);
        if (info.refCount != 0)
            throw new AssertionError();
        if (info.getSourceFile() != sourceFile)
            throw new AssertionError();
        boolean disposeInfo = true;
        boolean releaseBuffer = false;
        try
        {
            if (info.getBuffer() != buffer)
                throw new AssertionError();
            buffer.addRef();
            releaseBuffer = true;
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
            try
            {
                if (disposeInfo)
                    info.dispose();
            }
            finally
            {
                if (releaseBuffer)
                    buffer.release();
            }
        }
    }

    /**
     * Returns the working copy info for the given source file, incrementing
     * the reference count for the info. Returns <code>null</code> if the
     * source file has no working copy info. Performs atomically.
     * <p>
     * Each successful call to this method that did not return
     * <code>null</code> must ultimately be followed by exactly
     * one call to <code>releaseWorkingCopyInfo</code>.
     * </p>
     *
     * @param sourceFile the source file whose working copy info is to be returned
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file has no working copy info
     * @see #releaseWorkingCopyInfo(SourceFile)
     */
    synchronized WorkingCopyInfo getWorkingCopyInfo(SourceFile sourceFile)
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
    synchronized WorkingCopyInfo peekAtWorkingCopyInfo(SourceFile sourceFile)
    {
        return workingCopyInfos.get(sourceFile);
    }

    /**
     * Decrements the reference count of the working copy info associated with
     * the given source file. If there are no remaining references, removes the
     * working copy info and releases the working copy buffer. Has no effect if
     * there was no working copy info for the source file. Performs atomically.
     *
     * @param sourceFile the source file whose working copy info is to be released
     * @return the working copy info for the given source file,
     *  or <code>null</code> if the source file had no working copy info
     */
    WorkingCopyInfo releaseWorkingCopyInfo(SourceFile sourceFile)
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
                    sourceFile.hRemove(EMPTY_CONTEXT);
                }
                return info;
            }
        }
        finally
        {
            if (infoToDispose != null)
            {
                IBuffer buffer = infoToDispose.getBuffer();
                try
                {
                    infoToDispose.dispose();
                }
                finally
                {
                    buffer.release();
                }
            }
        }
    }
}
