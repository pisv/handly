/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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

import org.eclipse.handly.ui.quickoutline.OutlinePopup;
import org.eclipse.handly.ui.quickoutline.OutlinePopupHandler;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A simple handler that opens Handly-based outline popup for Xtext editor.
 * <p>
 * Note that this class relies on the injected {@link Provider
 * Provider&lt;OulinePopup&gt;} for creating instances of the
 * language-specific implementation of {@link OutlinePopup}.
 * </p>
 */
public class HandlyXtextOutlinePopupHandler
    extends OutlinePopupHandler
{
    @Inject
    private Provider<OutlinePopup> outlinePopupProvider;

    @Override
    protected OutlinePopup createOutlinePopup()
    {
        return outlinePopupProvider.get();
    }
}
