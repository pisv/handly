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
package org.eclipse.handly.internal.examples.basic.ui.outline2;

import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.internal.examples.basic.ui.FooContentProvider;
import org.eclipse.handly.internal.examples.basic.ui.FooLabelProvider;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.ui.outline.ProblemMarkerListenerContribution;
import org.eclipse.handly.ui.viewer.ProblemMarkerLabelDecorator;
import org.eclipse.handly.xtext.ui.outline.HandlyXtextOutlinePage;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import com.google.inject.Inject;

/**
 * A more advanced implementation of Foo Outline page with Handly outline
 * framework.
 * <p>
 * Over the basic implementation in the <code>outline</code> package,
 * this one provides problem marker decoration and a custom filter.
 * </p>
 */
public class FooOutlinePage
    extends HandlyXtextOutlinePage
{
    @Inject
    private CompactViewActionContribution compactViewActionContribution;
    @Inject
    private CompactViewFilterContribution compactViewFilterContribution;
    @Inject
    private FooContentProvider contentProvider;
    @Inject
    private FooLabelProvider labelProvider;

    @Override
    protected void addOutlineContributions()
    {
        super.addOutlineContributions();
        addOutlineContribution(new ProblemMarkerListenerContribution());
        addOutlineContribution(compactViewActionContribution);
        addOutlineContribution(compactViewFilterContribution);
    }

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return contentProvider;
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return new DecoratingStyledCellLabelProvider(labelProvider,
            new ProblemMarkerLabelDecorator(), null);
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
