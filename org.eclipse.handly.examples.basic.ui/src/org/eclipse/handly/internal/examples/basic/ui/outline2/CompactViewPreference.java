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

import org.eclipse.handly.ui.preference.BooleanPreference;
import org.eclipse.handly.ui.preference.FlushingPreferenceStore;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Controls whether the compact view of the outline is active.
 */
@Singleton
public class CompactViewPreference
    extends BooleanPreference
{
    /**
     * @param preferenceStoreAccess preference store access for the language
     *  (not <code>null</code>)
     */
    @Inject
    public CompactViewPreference(IPreferenceStoreAccess preferenceStoreAccess)
    {
        super("Outline.CompactView", new FlushingPreferenceStore( //$NON-NLS-1$
            preferenceStoreAccess.getWritablePreferenceStore()));
    }
}
