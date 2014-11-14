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
 * A basic implementation of {@link IOutlineContribution}.
 * Keeps reference to the contribution's outline page.
 * This class is intended to be extended by clients.
 */
public class OutlineContribution
    implements IOutlineContribution
{
    private ICommonOutlinePage outlinePage;
    
    /**
     * Returns this contribution's outline page.
     *
     * @return the outline page of this contribution, 
     *  or <code>null</code> if this contribution has not been
     *  {@link #init(ICommonOutlinePage) initialized}
     */
    public final ICommonOutlinePage getOutlinePage()
    {
        return outlinePage;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This method may be extended by subclasses. Subclasses must call
     * the superclass implementation.
     * </p>
     */
    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        if (this.outlinePage != null)
            throw new IllegalStateException();
        this.outlinePage = outlinePage;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method may be extended by subclasses. Subclasses must call
     * the superclass implementation.
     * </p>
     */
    @Override
    public void dispose()
    {
    }
}
