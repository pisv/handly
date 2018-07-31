/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.impl.IElementImplExtension.CLOSE_HINT;
import static org.eclipse.handly.model.impl.IElementImplExtension.CloseHint.PARENT_CLOSING;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.IElementImplExtension;

/**
 * Manages element handle/body relationships for a Handly-based model.
 * Generally, each model will have its own instance of the element manager.
 * <p>
 * An instance of this class is safe for use by multiple threads.
 * </p>
 *
 * @see IElementImplSupport#getElementManager_()
 */
public class ElementManager
{
    private static final ISourceFile[] NO_WORKING_COPIES = new ISourceFile[0];

    protected final IBodyCache cache;

    // Temporary cache of newly opened elements
    private ThreadLocal<TemporaryCache> temporaryCache = new ThreadLocal<>();

    private Map<ISourceFileImplSupport, WorkingCopyInfo> workingCopyInfos =
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
     * body cache. This method is called under the element manager lock.
     * <p>
     * Subclasses may override this method, but are not intended to invoke it.
     * </p>
     * <p>
     * This implementation invokes <code>((IElementImplExtension)element).{@link
     * IElementImplExtension#close_(IContext) close_}(context)</code>.
     * </p>
     *
     * @param element the element that needs closing (never <code>null</code>)
     * @param context the operation context (never <code>null</code>)
     */
    protected void close(IElement element, IContext context)
    {
        ((IElementImplExtension)element).close_(context);
    }

    /**
     * Returns the corresponding body for the given element, or
     * <code>null</code> if no body is registered for the element.
     * <p>
     * Checks the temporary cache first. If the current thread has no temporary
     * cache or the temporary cache contains no body for the element, checks
     * the body cache associated with this manager. Performs atomically.
     * </p>
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if no body is registered for the element
     */
    synchronized Object get(IElementImplSupport element)
    {
        TemporaryCache tempCache = temporaryCache.get();
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
     * cache or the temporary cache contains no body for the element, checks
     * the body cache associated with this manager. Performs atomically.
     * </p>
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if no body is registered for the element
     */
    synchronized Object peek(IElementImplSupport element)
    {
        TemporaryCache tempCache = temporaryCache.get();
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
     * provided element handle/body relationships.
     *
     * @param element the element being (re-)opened (not <code>null</code>)
     * @param newElements a map containing element handle/body relationships
     *  to be stored in the body cache (not <code>null</code>). At a minimum,
     *  it must contain a body for the given element
     */
    synchronized void put(IElementImplSupport element,
        Map<IElement, Object> newElements)
    {
        // remove existing children as they are replaced with the new children contained in newElements
        Object body = cache.peek(element);
        if (body != null)
        {
            for (IElement child : element.getChildrenFromBody_(body))
            {
                close(child, of(CLOSE_HINT, PARENT_CLOSING));
            }
        }

        cache.putAll(newElements);

        if (element instanceof ISourceFileImplSupport)
        {
            WorkingCopyInfo info = workingCopyInfos.get(element);
            if (info != null && !info.created) // case of wc creation
                info.created = true;
        }
    }

    /**
     * If a body for the given element is not already present in the body cache
     * associated with this manager, puts the provided element handle/body
     * relationships into the body cache. Performs atomically.
     *
     * @param element the element being opened (not <code>null</code>)
     * @param newElements a map containing element handle/body relationships
     *  to be stored in the body cache (not <code>null</code>). At a minimum,
     *  it must contain a body for the given element
     * @return the previous body for the given element, or <code>null</code>
     *  if the body cache did not previously contain a body for the element
     */
    synchronized Object putIfAbsent(IElementImplSupport element,
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
    synchronized void remove(IElementImplSupport element)
    {
        Object body = cache.peek(element);
        if (body != null)
        {
            element.removing_(body);
            for (IElement child : element.getChildrenFromBody_(body))
            {
                close(child, of(CLOSE_HINT, PARENT_CLOSING));
            }
            cache.remove(element);
        }
    }

    void pushTemporaryCache(Map<IElement, Object> newElements)
    {
        TemporaryCache parent = temporaryCache.get();
        temporaryCache.set(new TemporaryCache(parent, newElements));
    }

    void popTemporaryCache()
    {
        TemporaryCache tempCache = temporaryCache.get();
        if (tempCache == null)
            throw new IllegalStateException();
        temporaryCache.set(tempCache.parent);
    }

    /**
     * If the given source file is not already associated with a working copy
     * info, associates the source file with the given working copy info and
     * acquires a new independent ownership of the working copy buffer held by
     * the info. Otherwise, returns the working copy info already associated
     * with the source file; the given info is ignored. Increments the reference
     * count of the working copy info associated with the source file. Performs
     * atomically.
     * <p>
     * Each successful call to this method must ultimately be followed
     * by exactly one call to <code>releaseWorkingCopyInfo</code>.
     * </p>
     *
     * @param sourceFile the source file with which a working copy info
     *  is to be associated (not <code>null</code>)
     * @param info the new working copy info (not <code>null</code>)
     * @return the previous working copy info associated with the given
     *  source file, or <code>null</code> if there was no working copy info
     *  for the source file
     * @see #releaseWorkingCopyInfo(ISourceFileImplSupport)
     */
    WorkingCopyInfo putWorkingCopyInfoIfAbsent(
        ISourceFileImplSupport sourceFile, WorkingCopyInfo info)
    {
        if (sourceFile == null)
            throw new IllegalArgumentException();
        if (info == null)
            throw new IllegalArgumentException();
        IBuffer buffer = info.getBuffer();
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
                    if (info.refCount != 0)
                        throw new IllegalArgumentException();
                    workingCopyInfos.put(sourceFile, info);
                    info.refCount = 1;
                    releaseBuffer = false;
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
     * @see #releaseWorkingCopyInfo(ISourceFileImplSupport)
     */
    synchronized WorkingCopyInfo getWorkingCopyInfo(
        ISourceFileImplSupport sourceFile)
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
    synchronized WorkingCopyInfo peekAtWorkingCopyInfo(
        ISourceFileImplSupport sourceFile)
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
    WorkingCopyInfo releaseWorkingCopyInfo(ISourceFileImplSupport sourceFile)
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
                    sourceFile.remove_(EMPTY_CONTEXT);
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

    private static class TemporaryCache
    {
        final TemporaryCache parent;
        private final Map<IElement, Object> newElements;

        TemporaryCache(TemporaryCache parent, Map<IElement, Object> newElements)
        {
            this.parent = parent;
            if ((this.newElements = newElements) == null)
                throw new IllegalArgumentException();
        }

        Object get(IElement element)
        {
            Object body = newElements.get(element);
            if (body == null && parent != null)
                body = parent.get(element);
            return body;
        }
    }
}
