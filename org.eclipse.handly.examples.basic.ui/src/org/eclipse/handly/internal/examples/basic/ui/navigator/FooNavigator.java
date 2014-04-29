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
package org.eclipse.handly.internal.examples.basic.ui.navigator;

import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * Foo Navigator view.
 * <p>
 * Note that elements inside a Foo file are automagically reconciled 
 * with the Foo editor's contents, thanks to Handly/Xtext integration.
 * </p>
 */
public class FooNavigator
    extends CommonNavigator
    implements IElementChangeListener
{
    /**
     * Foo Navigator view id.
     */
    public static final String ID =
        "org.eclipse.handly.examples.basic.ui.views.fooNavigator"; //$NON-NLS-1$

    @Override
    public void init(IViewSite site) throws PartInitException
    {
        super.init(site);
        FooModelCore.getFooModel().addElementChangeListener(this);
    }

    @Override
    public void dispose()
    {
        FooModelCore.getFooModel().removeElementChangeListener(this);
        super.dispose();
    }

    @Override
    public void elementChanged(IElementChangeEvent event)
    {
        // NOTE: don't hold on the event or its delta.
        // The delta is only valid during the dynamic scope of the notification.
        // In particular, don't pass it to another thread (e.g. via asyncExec). 
        final Control control = getCommonViewer().getControl();
        control.getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                if (!control.isDisposed())
                {
                    refresh(); // full refresh should suffice for our example (but not for production code)
                }
            }
        });
    }

    @Override
    protected Object getInitialInput()
    {
        return FooModelCore.getFooModel();
    }

    private void refresh()
    {
        Control control = getCommonViewer().getControl();
        control.setRedraw(false);
        BusyIndicator.showWhile(control.getDisplay(), new Runnable()
        {
            public void run()
            {
                TreePath[] treePaths = getCommonViewer().getExpandedTreePaths();
                getCommonViewer().refresh(); 
                getCommonViewer().setExpandedTreePaths(treePaths);
            }
        });
        control.setRedraw(true);
    }
}
