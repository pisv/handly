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
package org.eclipse.handly.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.handly.internal.Activator;

/**
 * Internal base for file related snapshot implementations.
 */
abstract class TextFileSnapshotBase
    extends Snapshot
{
    private static final int DEFAULT_READING_SIZE = 8192;
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    private Reference<String> contents;
    private volatile IStatus status = Status.OK_STATUS;

    @Override
    public synchronized String getContents()
    {
        if (!exists())
            return ""; //$NON-NLS-1$

        String result = null;
        boolean sync = isSynchronized();
        if (contents != null)
        {
            if (!sync)
                contents = null; // no need to continue holding on contents
            else
                result = contents.get();
        }
        if (result == null && sync)
        {
            try
            {
                cacheCharset();
                String currentContents = readContents();
                if (isSynchronized()) // still current
                    contents = new SoftReference<String>(result =
                        currentContents);
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
                status = e.getStatus();
            }
        }
        return result;
    }

    /**
     * Returns whether an I/O error was encountered while reading the file.
     *
     * @return an error status if an I/O error was encountered, or OK status
     *  otherwise
     */
    public IStatus getStatus()
    {
        return status;
    }

    /**
     * Returns whether the file existed at the moment this snapshot was taken.
     *
     * @return <code>true</code> if the file existed at the moment this snapshot
     *  was taken, <code>false</code> otherwise
     */
    public abstract boolean exists();

    abstract boolean isSynchronized();

    abstract void cacheCharset() throws CoreException;

    abstract String readContents() throws CoreException;

    static String getCharset(InputStream contents, String fileName)
        throws IOException
    {
        IContentDescription description =
            Platform.getContentTypeManager().getDescriptionFor(contents,
                fileName, new QualifiedName[] { IContentDescription.CHARSET });
        if (description != null)
            return description.getCharset();
        return null;
    }

    static char[] getInputStreamAsCharArray(InputStream stream,
        InputStreamReader reader) throws IOException
    {
        char[] contents = EMPTY_CHAR_ARRAY;
        int contentsLength = 0;
        int amountRead = -1;
        do
        {
            int amountRequested = Math.max(stream.available(),
                DEFAULT_READING_SIZE); // read at least 8K

            // resize contents if needed
            if (contentsLength + amountRequested > contents.length)
            {
                System.arraycopy(contents, 0, contents = new char[contentsLength
                    + amountRequested], 0, contentsLength);
            }

            // read as many chars as possible
            amountRead = reader.read(contents, contentsLength, amountRequested);

            if (amountRead > 0)
            {
                // remember length of contents
                contentsLength += amountRead;
            }
        }
        while (amountRead != -1);

        // Do not keep first character for UTF-8 BOM encoding
        int start = 0;
        if (contentsLength > 0 && "UTF-8".equals(reader.getEncoding())) //$NON-NLS-1$
        {
            if (contents[0] == 0xFEFF)
            { // if BOM char then skip
                contentsLength--;
                start = 1;
            }
        }
        // resize contents if necessary
        if (contentsLength < contents.length)
        {
            System.arraycopy(contents, start, contents =
                new char[contentsLength], 0, contentsLength);
        }
        return contents;
    }
}
