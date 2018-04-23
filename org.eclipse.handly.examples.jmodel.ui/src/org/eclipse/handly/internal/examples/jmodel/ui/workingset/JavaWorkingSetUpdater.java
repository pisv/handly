/*******************************************************************************
 * Copyright (c) 2015 Codasip Ltd.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.workingset;

import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.ui.workingset.AbstractWorkingSetUpdater;

/**
 * Java working set updater.
 */
public class JavaWorkingSetUpdater
    extends AbstractWorkingSetUpdater
{
    @Override
    protected void addElementChangeListener(IElementChangeListener listener)
    {
        JavaModelCore.getJavaModel().addElementChangeListener(listener);
    }

    @Override
    protected void removeElementChangeListener(IElementChangeListener listener)
    {
        JavaModelCore.getJavaModel().removeElementChangeListener(listener);
    }
}
