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
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.quickoutline;

import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.quickoutline.HandlyOutlinePopup;

import com.google.inject.Inject;

/**
 * A partial implementation of Handly-based outline popup for Xtext editor.
 * <p>
 * Note that this class relies on the language-specific implementation of
 * {@link IInputElementProvider} being available through injection.
 * </p>
 */
public abstract class HandlyXtextOutlinePopup
    extends HandlyOutlinePopup
{
    private IInputElementProvider inputElementProvider;

    @Inject
    public void setInputElementProvider(IInputElementProvider provider)
    {
        inputElementProvider = provider;
    }

    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return inputElementProvider;
    }
}
