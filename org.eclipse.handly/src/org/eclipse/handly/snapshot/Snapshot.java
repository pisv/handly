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
package org.eclipse.handly.snapshot;

import org.eclipse.core.runtime.CoreException;

/**
 * Abstract superclass of snapshots. 
 * Implements an equivalence relation on snapshots.
 * 
 * @noextend This class is not intended to be extended by clients
 */
public abstract class Snapshot
    implements ISnapshot
{
    /**
     * Implements an equivalence relation on snapshots:
     * <ul>
     * <li>Identical snapshots are always equal
     * <li>A snapshot is never equal to <code>null</code>
     * <li><code>Snapshot</code>s can only be equal to <code>Snapshot</code>s
     * <li><code>Snapshot</code>s which have not yet expired are equal  
     *  iff their contents are equal
     * <li><code>Snapshot</code>s which have expired are not equal 
     *  to all other snapshots
     * </ul>
     * <p>
     * Subclasses may override {@link #predictEquality(Snapshot)} 
     * if in some cases they can decide on snapshots equality without actually 
     * obtaining their {@link #getContents() contents}.
     * </p>
     */
    @Override
    public final boolean isEqualTo(ISnapshot other) throws CoreException
    {
        if (this == other)
            return true;
        if (other == null || !(other instanceof Snapshot))
            return false;
        Boolean prediction = predictEquality((Snapshot)other);
        if (prediction != null)
            return prediction.booleanValue();
        String otherContents = other.getContents();
        return otherContents != null && otherContents.equals(getContents());
    }

    /**
     * Predicts whether the two snapshots are {@link #isEqualTo(ISnapshot) equal} 
     * without actually obtaining their {@link #getContents() contents}. 
     * Must return <code>null</code> if cannot tell for sure. Any non-null 
     * result must meet the contract of {@link Snapshot#isEqualTo(ISnapshot)}.
     *
     * @param other the other snapshot (not <code>null</code> 
     *  and not identical to the receiver)
     * @return whether the two snapshots are predicted to be equal
     * @throws CoreException in case of underlying resource failure
     */
    protected Boolean predictEquality(Snapshot other) throws CoreException
    {
        return null;
    }
}
