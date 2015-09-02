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
package org.eclipse.handly.buffer;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Buffer utilities.
 */
public class Buffers
{
    /**
     * Returns a buffer connected to the shared {@link ITextFileBuffer}
     * for the given file. It is the client responsibility to {@link
     * IBuffer#dispose() dispose} the buffer after it is no longer needed.
     * <p>
     * If <code>create == false</code> and there is no buffer currently opened
     * for the given file, <code>null</code> is returned.
     * </p>
     * <p>
     * The returned buffer is safe for use by multiple threads, provided that
     * the underlying <code>ITextFileBuffer</code> is thread-safe.
     * </p>
     *
     * @param file a text file (not <code>null</code>)
     * @param create indicates whether a buffer should be created
     *  if no open buffer exists for the given file
     * @return the buffer opened for the given file, or <code>null</code>
     *  if <code>create == false</code> and there is no buffer currently opened
     *  for the given file
     * @throws CoreException if the buffer could not be created
     */
    public static IBuffer getTextFileBuffer(IFile file, boolean create)
        throws CoreException
    {
        if (!create && ITextFileBufferManager.DEFAULT.getTextFileBuffer(
            file.getFullPath(), LocationKind.IFILE) == null)
        {
            return null;
        }
        return new TextFileBuffer(file, ITextFileBufferManager.DEFAULT);
    }

    /**
     * Returns a new buffer spawned from the given parent buffer.
     * The child buffer inherits the contents of the parent buffer initially,
     * but is modified independently. It is the client responsibility to
     * {@link IBuffer#dispose() dispose} the created buffer after it is
     * no longer needed.
     * <p>
     * Saving the child buffer propagates its contents to the parent buffer and
     * the parent's underlying resource, so in general the parent should be kept
     * alive while the child is in use. To ensure that, the client can transfer
     * ownership of the parent buffer to the created child.
     * </p>
     * <p>
     * If <code>childOwnsParent == true</code>, the client gives up ownership
     * of the parent buffer and must not dispose it, even if this method threw
     * an exception. Ownership of the parent is transferred to the created child.
     * </p>
     * <p>
     * The returned buffer is safe for use by multiple threads.
     * </p>
     *
     * @param parent the parent buffer (not <code>null</code>)
     * @param childOwnsParent whether ownership of the parent buffer
     *  should be transferred to the created child
     * @return the created child buffer (never <code>null</code>)
     */
    public static IBuffer createChildBuffer(IBuffer parent,
        boolean childOwnsParent)
    {
        IBuffer child;
        if (!childOwnsParent)
            child = new ChildBuffer(parent);
        else
        {
            boolean success = false;
            try
            {
                child = new ChildBuffer(parent)
                {
                    @Override
                    public void dispose()
                    {
                        super.dispose();
                        parent.dispose();
                    }
                };
                success = true;
            }
            finally
            {
                if (!success)
                    parent.dispose();
            }
        }
        return child;
    }

    private Buffers()
    {
    }
}
