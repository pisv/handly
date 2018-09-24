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
package org.eclipse.handly.ui.viewer;

import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * A basic label provider for {@link IElement}s.
 * Uses the name of the element as its text. No images.
 */
public class ElementLabelProvider
    extends LabelProvider
{
    @Override
    public String getText(Object element)
    {
        if (element instanceof IElement)
            return Elements.getName((IElement)element);
        return super.getText(element);
    };
}
