/*******************************************************************************
 * Copyright (c) 2021 1C-Soft LLC.
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
package org.eclipse.handly.ui;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides default implementations for the methods of {@link IPartListener}.
 * <p>
 * Clients may extend this class and override only the methods which they are
 * interested in.
 * </p>
 *
 * @since 1.6
 */
public class PartListenerAdapter
    implements IPartListener
{
    @Override
    public void partOpened(IWorkbenchPart part)
    {
    }

    @Override
    public void partClosed(IWorkbenchPart part)
    {
    }

    @Override
    public void partActivated(IWorkbenchPart part)
    {
    }

    @Override
    public void partDeactivated(IWorkbenchPart part)
    {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part)
    {
    }
}
