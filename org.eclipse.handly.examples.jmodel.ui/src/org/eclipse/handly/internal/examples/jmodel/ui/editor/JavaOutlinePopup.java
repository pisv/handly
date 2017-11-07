/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.editor;

import org.eclipse.handly.examples.jmodel.ui.JavaModelContentProvider;
import org.eclipse.handly.examples.jmodel.ui.JavaModelLabelProvider;
import org.eclipse.handly.internal.examples.jmodel.ui.JavaInputElementProvider;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.quickoutline.HandlyOutlinePopup;
import org.eclipse.handly.ui.viewer.ProblemMarkerLabelDecorator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * The outline popup of the Java editor.
 */
public class JavaOutlinePopup
    extends HandlyOutlinePopup
{
    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return JavaInputElementProvider.INSTANCE;
    }

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return new JavaModelContentProvider();
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return new DecoratingStyledCellLabelProvider(
            new JavaModelLabelProvider(), new ProblemMarkerLabelDecorator(),
            null);
    }
}
