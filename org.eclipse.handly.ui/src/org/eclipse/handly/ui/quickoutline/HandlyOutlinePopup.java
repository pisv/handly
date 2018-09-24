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
package org.eclipse.handly.ui.quickoutline;

import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;

/**
 * A partial implementation of Handly-based outline popup. This class
 * is intended to be used with a text-based host such as a text editor.
 */
public abstract class HandlyOutlinePopup
    extends FilteringOutlinePopup
    implements IContentAdapterProvider
{
    /**
     * Returns the content adapter that defines a mapping between elements
     * of a Handly-based model and the outline's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    @Override
    public IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses the {@link #getInputElementProvider()
     * input element provider} to obtain an {@link IElement} corresponding to
     * the editor input for the outline popup's host and returns an outline
     * element corresponding to the <code>IElement</code>, as determined by
     * the {@link #getContentAdapter() content adapter}.
     * </p>
     */
    @Override
    protected Object computeInput()
    {
        IElement inputElement = getInputElementProvider().getElement(
            getHost().getEditorInput());
        return getContentAdapter().getCorrespondingElement(inputElement);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the given selection is a text selection, this implementation finds
     * the smallest {@link ISourceElement} that includes the offset of the
     * text selection and returns an outline element corresponding to the
     * found source element, as determined by the {@link #getContentAdapter()
     * content adapter}.
     */
    @Override
    protected Object getCorrespondingElement(ISelection hostSelection)
    {
        if (!(hostSelection instanceof ITextSelection))
            return null;
        IElement input = getContentAdapter().adapt(getTreeViewer().getInput());
        if (!(input instanceof ISourceElement))
            return null;
        ISourceElement sourceElement = (ISourceElement)input;
        if (!Elements.ensureReconciled(sourceElement, null))
            return null;
        return getContentAdapter().getCorrespondingElement(
            Elements.getSourceElementAt2(sourceElement,
                ((ITextSelection)hostSelection).getOffset(), null));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation attempts to adapt the given outline element to an 
     * {@link IElement} through the {@link #getContentAdapter() content adapter}.
     * If the adapter element is an {@link ISourceElement} and is contained in
     * the host as computed by {@link #isInHost(IElement)}, the identifying range
     * of the source element is selected in the outline popup's host.
     * </p>
     */
    @Override
    protected boolean revealInHost(Object outlineElement)
    {
        IElement element = getContentAdapter().adapt(outlineElement);
        if (!(element instanceof ISourceElement))
            return false;
        ISourceElement sourceElement = (ISourceElement)element;
        if (!isInHost(sourceElement))
            return false;
        TextRange identifyingRange = Elements.getSourceElementInfo2(
            sourceElement).getIdentifyingRange();
        if (identifyingRange == null)
            return false;
        TextSelection textSelection = new TextSelection(
            identifyingRange.getOffset(), identifyingRange.getLength());
        getHost().getSelectionProvider().setSelection(textSelection);
        return true;
    }

    /**
     * Returns whether the given element is contained in the host
     * of this outline popup.
     * <p>
     * This implementation uses the {@link #getInputElementProvider()
     * input element provider} to obtain an {@link IElement} corresponding to
     * the editor input for the host. It then checks whether the <code>IElement</code>
     * {@link Elements#isAncestorOf(IElement, IElement) contains} the given
     * element and returns the result.
     * </p>
     *
     * @param element may be <code>null</code>
     * @return <code>true</code> if the element is contained in the host;
     *  <code>false</code> otherwise
     */
    protected boolean isInHost(IElement element)
    {
        IElement inputElement = getInputElementProvider().getElement(
            getHost().getEditorInput());
        return inputElement != null && Elements.isAncestorOf(inputElement,
            element);
    }

    /**
     * Returns the input element provider for this outline popup.
     *
     * @return the input element provider for this outline popup
     */
    protected abstract IInputElementProvider getInputElementProvider();
}
