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
package org.eclipse.handly.xtext.ui.outline;

import org.eclipse.handly.ui.preference.BooleanPreference;
import org.eclipse.handly.ui.preference.FlushingPreferenceStore;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A language-specific lexical sort preference for the outline.
 */
@Singleton
public class LexicalSortPreference
    extends BooleanPreference
{
    /**
     * Creates a new lexical sort preference for the outline
     * using the given language-specific preference store access.
     *
     * @param preferenceStoreAccess preference store access for the language
     *  (not <code>null</code>)
     */
    @Inject
    public LexicalSortPreference(IPreferenceStoreAccess preferenceStoreAccess)
    {
        super("Outline.LexicalSort", new FlushingPreferenceStore( //$NON-NLS-1$
            preferenceStoreAccess.getWritablePreferenceStore()));
    }
}
