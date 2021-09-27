/*******************************************************************************
 * Copyright (c) 2014, 2021 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.preference.IPreferenceListener;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract base class for link-with-editor contributions.
 * <p>
 * Contributes link-with-editor behavior, if the outline page supports
 * linking with editor. The activation of the feature is governed by the
 * corresponding {@link ICommonOutlinePage#getLinkWithEditorPreference()
 * preference}. Subclasses must implement linking logic in a {@link
 * #getLinkingHelper() linking helper}.
 * </p>
 */
public abstract class LinkWithEditorContribution
    extends OutlineContribution
{
    private OpenAndLinkWithEditorHelper linkingHelper;
    private IBooleanPreference preference;
    private IPreferenceListener preferenceListener =
        event -> PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
        {
            if (linkingHelper != null && preference != null)
                linkingHelper.setLinkWithEditor(preference.getValue());
        });

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
