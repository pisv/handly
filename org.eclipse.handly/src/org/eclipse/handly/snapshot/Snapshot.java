/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
package org.eclipse.handly.snapshot;

/**
 * Abstract superclass of all snapshots.
 * Implements an equivalence relation on snapshots.
 */
public abstract class Snapshot
    implements ISnapshot
{
    @Override
    public final boolean isEqualTo(ISnapshot other)
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
     * result must meet the contract of {@link ISnapshot#isEqualTo(ISnapshot)}.
     *
     * @param other the other snapshot (not <code>null</code>
     *  and not identical to the receiver)
     * @return <code>true</code> if the two snapshots are predicted to be equal,
     *  <code>false</code> if the two snapshots are predicted to be unequal,
     *  and <code>null</code> if there is no prediction
     */
    protected Boolean predictEquality(Snapshot other)
    {
        return null;
    }
}
