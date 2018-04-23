/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
