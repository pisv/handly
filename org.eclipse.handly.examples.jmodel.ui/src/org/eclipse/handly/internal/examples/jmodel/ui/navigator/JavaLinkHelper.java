/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     Ondrej Ilcik (Codasip) - adaptation (adapted from
 *        org.eclipse.handly.internal.examples.basic.ui.navigator.FooLinkHelper)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.navigator;

import org.eclipse.handly.internal.examples.jmodel.ui.JavaInputElementProvider;
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
