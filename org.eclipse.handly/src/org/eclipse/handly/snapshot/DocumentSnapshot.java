/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
package org.eclipse.handly.snapshot;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ISynchronizable;

/**
 * A snapshot of a document. Thread-safe.
 */
public final class DocumentSnapshot
    extends Snapshot
{
    private final IDocument document;
    private final long modificationStamp;

    /**
     * Takes a snapshot of the given document.
     *
     * @param document must not be <code>null</code>.
     *  Must implement {@link IDocumentExtension4}.
     */
    public DocumentSnapshot(IDocument document)
    {
        if (!(document instanceof IDocumentExtension4))
            throw new IllegalArgumentException();
        this.document = document;
        this.modificationStamp = getModificationStamp(document);
    }

    @Override
    public String getContents()
    {
        if (document instanceof ISynchronizable)
        {
            Object lock = ((ISynchronizable)document).getLockObject();
            if (lock != null)
            {
                synchronized (lock)
                {
                    return internalGetContents();
                }
            }
        }
        return internalGetContents();
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (other instanceof DocumentSnapshot)
        {
            if (document == ((DocumentSnapshot)other).document
                && modificationStamp == ((DocumentSnapshot)other).modificationStamp)
                return true;
        }

        if (isStale())
            return false; // expired

        return null;
    }

    private String internalGetContents()
    {
        String result = null;
        if (!isStale())
        {
            String contents = document.get();
            if (!isStale()) // still current
                result = contents;
        }
        return result;
    }

    private boolean isStale()
    {
        return modificationStamp != getModificationStamp(document);
    }

    private static long getModificationStamp(IDocument document)
    {
        return ((IDocumentExtension4)document).getModificationStamp();
    }
}
