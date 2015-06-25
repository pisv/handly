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
package org.eclipse.handly.internal.examples.basic.ui.outline;

import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.internal.examples.basic.ui.FooContentProvider;
import org.eclipse.handly.internal.examples.basic.ui.FooLabelProvider;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.xtext.ui.outline.HandlyXtextOutlinePage;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import com.google.inject.Inject;

/**
 * This implementation of Foo Outline page employs Handly outline framework
 * and is both richer in ability and less in size than the class {@link FooOutlinePage},
 * which implements a similar functionality from scratch. See also the
 * <code>outline2</code> package, which provides a more advanced example.
 */
public class FooOutlinePage2
    extends HandlyXtextOutlinePage
{
    @Inject
    private FooContentProvider contentProvider;
    @Inject
    private FooLabelProvider labelProvider;

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return contentProvider;
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return labelProvider;
    }

    @Override
    protected void addElementChangeListener(IElementChangeListener listener)
    {
        FooModelCore.getFooModel().addElementChangeListener(listener);
    }

    @Override
    protected void removeElementChangeListener(IElementChangeListener listener)
    {
        FooModelCore.getFooModel().removeElementChangeListener(listener);
    }
}
