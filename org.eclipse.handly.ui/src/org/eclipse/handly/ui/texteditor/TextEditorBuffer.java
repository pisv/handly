/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.texteditor;

import static org.eclipse.handly.buffer.IBufferListener.BUFFER_SAVED;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.handly.buffer.BufferChangeOperation;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.IBufferChange;
import org.eclipse.handly.buffer.IBufferListener;
import org.eclipse.handly.buffer.UiBufferChangeRunner;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.UiSynchronizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Implementation of {@link IBuffer} backed by a text editor document.
 * <p>
 * An instance of this class is safe for use by multiple threads,
 * provided that the underlying text editor document is thread-safe.
 * However, certain operations can only be executed by the UI thread:
 * </p>
 * <ul>
 * <li>{@link #save(IContext, IProgressMonitor) save}</li>
 * <li>{@link #isDirty() isDirty}</li>
 * <li>{@link #applyChange(IBufferChange, IProgressMonitor) applyChange} -
 * if save is requested</li>
 * <li>{@link #addListener(IBufferListener) addListener}</li>
 * <li>{@link #removeListener(IBufferListener) removeListener}</li>
 * </ul>
 */
public final class TextEditorBuffer
    implements IBuffer
{
    private final UiSynchronizer uiSynchronizer;
    private final IEditorInput editorInput;
    private final IDocumentProvider documentProvider;
    private IDocument document;
    private IAnnotationModel annotationModel;
    private int refCount = 1;
    private ListenerList<IBufferListener> listeners;
    private IElementStateListener elementStateListener;

    /**
     * Creates a new buffer instance and connects it to the current document
     * of the given text editor.
     * <p>
     * <b>Note:</b> The association between the buffer and the document will not
     * change even if the association between the text editor and the document
     * changes (e.g., when a new editor input is set).
     * </p>
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     * <p>
     * This constructor can only be executed by the UI thread.
     * </p>
     *
     * @param editor not <code>null</code>
     * @throws CoreException if the buffer could not be connected
     * @deprecated Use {@link TextEditorBuffer#TextEditorBuffer(IDocumentProvider,
     *  IEditorInput)} instead.
     */
    public TextEditorBuffer(ITextEditor editor) throws CoreException
    {
        this(editor.getDocumentProvider(), editor.getEditorInput());
    }

    /**
     * Creates a new buffer instance and connects it to the document provided
     * for the given editor input via the given provider.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     * <p>
     * This constructor can only be executed by the UI thread.
     * </p>
     *
     * @param provider a document provider (not <code>null</code>)
     * @param input an editor input (not <code>null</code>)
     * @throws CoreException if the buffer could not be connected
     * @since 1.5
     */
    public TextEditorBuffer(IDocumentProvider provider, IEditorInput input)
        throws CoreException
    {
        if ((editorInput = input) == null)
            throw new IllegalArgumentException();
        if ((documentProvider = provider) == null)
            throw new IllegalArgumentException();
        if ((uiSynchronizer = UiSynchronizer.getDefault()) == null)
            throw new AssertionError();
        checkThread();
        documentProvider.connect(editorInput);
        boolean f = false;
        try
        {
            if ((document = documentProvider.getDocument(editorInput)) == null)
                throw new AssertionError();
            annotationModel = documentProvider.getAnnotationModel(editorInput);
            f = true;
        }
        finally
        {
            if (!f)
                documentProvider.disconnect(editorInput);
        }
    }

    @Override
    public IDocument getDocument()
    {
        IDocument result = document;
        if (result == null)
            throw new IllegalStateException(
                "Attempt to access a disconnected TextEditorBuffer for " //$NON-NLS-1$
                    + editorInput.getToolTipText());
        return result;
    }

    @Override
    public IAnnotationModel getAnnotationModel()
    {
        return annotationModel;
    }

    @Override
    public ISnapshot getSnapshot()
    {
        return new DocumentSnapshot(getDocument());
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        try
        {
            UiBufferChangeRunner runner = new UiBufferChangeRunner(
                uiSynchronizer, new BufferChangeOperation(this, change));
            return runner.run(monitor);
        }
        catch (MalformedTreeException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
        catch (BadLocationException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    @Override
    public void save(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        checkThread();
        documentProvider.saveDocument(monitor, editorInput, getDocument(),
            false);
    }

    @Override
    public boolean isDirty()
    {
        checkThread();
        return documentProvider.canSaveDocument(editorInput);
    }

    @Override
    public int getSupportedListenerMethods()
    {
        return BUFFER_SAVED;
    }

    @Override
    public void addListener(IBufferListener listener)
    {
        checkThread();
        if (listeners != null)
            listeners.add(listener);
        else
        {
            getDocument(); // check not disconnected
            listeners = new ListenerList<>();
            listeners.add(listener);
            elementStateListener = new ElementStateListener();
            documentProvider.addElementStateListener(elementStateListener);
        }
    }

    @Override
    public void removeListener(IBufferListener listener)
    {
        checkThread();
        if (listeners == null)
            return;
        listeners.remove(listener);
        if (listeners.isEmpty())
        {
            documentProvider.removeElementStateListener(elementStateListener);
            elementStateListener = null;
            listeners = null;
        }
    }

    @Override
    public synchronized void addRef()
    {
        ++refCount;
    }

    @Override
    public synchronized void release()
    {
        if (--refCount == 0 && document != null)
        {
            document = null;
            annotationModel = null;
            uiSynchronizer.asyncExec(() ->
            {
                try
                {
                    listeners = null;
                    if (elementStateListener != null)
                    {
                        documentProvider.removeElementStateListener(
                            elementStateListener);
                        elementStateListener = null;
                    }
                }
                finally
                {
                    documentProvider.disconnect(editorInput);
                }
            });
        }
    }

    private void checkThread()
    {
        if (!Thread.currentThread().equals(uiSynchronizer.getThread()))
            throw new IllegalStateException(
                "Invalid thread access to TextEditorBuffer for " //$NON-NLS-1$
                    + editorInput.getToolTipText());
    }

    private long getModificationStamp()
    {
        return documentProvider.getModificationStamp(editorInput);
    }

    private boolean isSynchronized()
    {
        if (documentProvider instanceof IDocumentProviderExtension3)
            return ((IDocumentProviderExtension3)documentProvider).isSynchronized(
                editorInput);

        return getModificationStamp() == documentProvider.getSynchronizationStamp(
            editorInput);
    }

    private void fireBufferSaved()
    {
        for (IBufferListener listener : listeners)
        {
            SafeRunner.run(() -> listener.bufferSaved(this));
        }
    }

    private class ElementStateListener
        extends ElementStateListenerAdapter
    {
        private long modificationStamp = getModificationStamp();

        @Override
        public void elementDirtyStateChanged(Object element, boolean isDirty)
        {
            if (editorInput.equals(element) && !isDirty)
            {
                checkThread(); // assert it is always called in the UI thread
                if (isSynchronized()
                    && modificationStamp != getModificationStamp())
                {
                    modificationStamp = getModificationStamp();
                    fireBufferSaved();
                }
            }
        }
    }
}
