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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.preference.IPreferenceListener;
import org.eclipse.handly.ui.preference.PreferenceChangeEvent;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract base class for link-with-editor contributions.
 * <p>
 * Contributes link-with-editor behavior, if the outline page supports 
 * linking with editor. Subclasses must implement linking logic in 
 * a {@link #getLinkingHelper() linking helper}.
 * </p>
 */
public abstract class LinkWithEditorContribution
    extends OutlineContribution
{
    private OpenAndLinkWithEditorHelper linkingHelper;
    private IBooleanPreference preference;
    private IPreferenceListener preferenceListener = new IPreferenceListener()
    {
        @Override
        public void preferenceChanged(PreferenceChangeEvent event)
        {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (linkingHelper != null && preference != null)
                        linkingHelper.setLinkWithEditor(preference.getValue());
                }
            });
        }
    };

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);
        preference = outlinePage.getLinkWithEditorPreference();
        if (preference != null)
        {
            linkingHelper = getLinkingHelper();
            linkingHelper.setLinkWithEditor(preference.getValue());
            preference.addListener(preferenceListener);
        }
    }

    @Override
    public void dispose()
    {
        if (preference != null)
        {
            preference.removeListener(preferenceListener);
            preference = null;
        }
        if (linkingHelper != null)
        {
            linkingHelper.dispose();
            linkingHelper = null;
        }
        super.dispose();
    }

    /**
     * Returns a linking helper that will provide link-with-editor logic
     * for the outline page. This method is called once, when
     * this contribution is initializing.
     *
     * @return the linking helper (not <code>null</code>)
     */
    protected abstract OpenAndLinkWithEditorHelper getLinkingHelper();
}
