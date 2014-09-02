/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.Activator;

/**
 * A snapshot of a text file. Thread-safe. 
 */
public final class TextFileSnapshot
    extends Snapshot
{
    private static final int DEFAULT_READING_SIZE = 8192;
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    private final IFile file;
    private final long modificationStamp;
    private Reference<String> contents;
    private volatile boolean readError;

    /**
     * Takes a snapshot of the given text file.
     * 
     * @param file must not be <code>null</code>
     */
    public TextFileSnapshot(IFile file)
    {
        this.file = file;
        this.modificationStamp = file.getModificationStamp();
    }

    @Override
    public synchronized String getContents()
    {
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
                String currentContents = readContents();
                if (isSynchronized()) // still current
                    contents = new SoftReference<String>(result = currentContents);
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
                readError = true;
            }
        }
        return result;
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (!isSynchronized())
            return false;

        if (other instanceof TextFileSnapshot)
        {
            if (!((TextFileSnapshot)other).isSynchronized())
                return false;

            if (file.equals(((TextFileSnapshot)other).file)
                && modificationStamp == ((TextFileSnapshot)other).modificationStamp)
                return true;
        }

        return null;
    }

    private boolean isSynchronized()
    {
        return modificationStamp == file.getModificationStamp() && !readError;
    }

    private String readContents() throws CoreException
    {
        if (!file.exists())
            return ""; //$NON-NLS-1$

        String encoding = null;
        try
        {
            encoding = file.getCharset();
        }
        catch (CoreException ce)
        {
            // use no encoding
        }

        try
        {
            InputStream stream = file.getContents(false);
            try
            {
                InputStreamReader reader =
                    (encoding == null ? new InputStreamReader(stream)
                        : new InputStreamReader(stream, encoding));
                try
                {
                    return String.valueOf(getInputStreamAsCharArray(stream,
                        reader));
                }
                finally
                {
                    reader.close();
                }
            }
            finally
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    private static char[] getInputStreamAsCharArray(InputStream stream,
        InputStreamReader reader) throws IOException
    {
        char[] contents = EMPTY_CHAR_ARRAY;
        int contentsLength = 0;
        int amountRead = -1;
        do
        {
            int amountRequested =
                Math.max(stream.available(), DEFAULT_READING_SIZE); // read at least 8K

            // resize contents if needed
            if (contentsLength + amountRequested > contents.length)
            {
                System.arraycopy(contents, 0, contents =
                    new char[contentsLength + amountRequested], 0,
                    contentsLength);
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
        if (contentsLength > 0 && "UTF-8".equals(reader.getEncoding())) { //$NON-NLS-1$
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
