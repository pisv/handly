/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     Ondrej Ilcik (Codasip) - adaptation (adapted from
 *        org.eclipse.handly.internal.examples.basic.ui.navigator.FooLinkHelper)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel.ui.navigator;

import org.eclipse.handly.internal.examples.javamodel.ui.JavaInputElementProvider;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.navigator.LinkHelper;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * Link helper for the Java Navigator.
 */
public class JavaLinkHelper
    extends LinkHelper
{
    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return JavaInputElementProvider.INSTANCE;
    }

    @Override
    protected IViewPart getNavigatorView()
    {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
            JavaNavigator.ID);
    }
}
