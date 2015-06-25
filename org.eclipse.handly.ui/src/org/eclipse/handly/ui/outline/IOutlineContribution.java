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

/**
 * Represents a contribution to the common outline page.
 * This interface may be implemented by clients.
 */
public interface IOutlineContribution
{
    /**
     * Initializes this contribution. This method is called after
     * the outline page's tree viewer has been created.
     *
     * @param outlinePage the contribution's outline page
     *  (never <code>null</code>)
     */
    void init(ICommonOutlinePage outlinePage);

    /**
     * Disposes of this contribution. Implementors should clean up
     * any resources associated with this contribution. Note that
     * there is no guarantee that <code>init()</code> has been called.
     * This method has no effect if this contribution has already
     * been disposed.
     */
    void dispose();
}
