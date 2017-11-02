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
 * is intended to be used with a text-based host such as text editor.
 */
public abstract class HandlyOutlinePopup
    extends FilteringOutlinePopup
    implements IContentAdapterProvider
{
    /**
     * Returns the content adapter that defines a mapping between elements
     * of a Handly based model and the outline's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return {@link IContentAdapter} (never <code>null</code>)
     */
    @Override
    public IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    @Override
    protected Object computeInput()
    {
        IElement inputElement = getInputElementProvider().getElement(
            getHost().getEditorInput());
        return getContentAdapter().getCorrespondingElement(inputElement);
    }

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

    protected abstract IInputElementProvider getInputElementProvider();
}
