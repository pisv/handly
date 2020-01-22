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
package org.eclipse.handly.snapshot;

/**
 * A snapshot that never expires. It never returns <code>null</code> from
 * {@link #getContents()}; it wraps another snapshot and holds on to its
 * contents.
 * <p>
 * Protractedly holding on to non-expiring snapshots is not recommended,
 * as they may potentially consume large amount of space.
 * </p>
 */
public final class NonExpiringSnapshot
    extends Snapshot
{
    private final ISnapshot wrapped;
    private final String contents;

    /**
     * Takes a snapshot from the given provider and wraps it in a new
     * non-expiring snapshot.
     *
     * @param provider a snapshot provider from which a snapshot is to be taken
     *  (not <code>null</code>)
     * @throws IllegalStateException if the snapshot could not be created
     *  because of an inappropriate state of the snapshot provider
     */
    public NonExpiringSnapshot(ISnapshotProvider provider)
    {
        // get a snapshot and make sure it has not expired yet
        ISnapshot snapshot = null;
        String contents = null;
        for (int i = 0; i < 30 && contents == null; i++)
        {
            snapshot = provider.getSnapshot();
            contents = snapshot.getContents();
        }
        if (contents == null)
        {
            throw new IllegalStateException(
                "Could not get a non-expired snapshot. Ill-behaved snapshot provider?"); //$NON-NLS-1$
        }

        // wrap the snapshot and store its contents
        this.wrapped = snapshot;
        this.contents = contents;
    }

    /**
     * Returns the cached contents of the wrapped snapshot.
     * <p>
     * Protractedly holding on to the returned contents is not recommended,
     * as it may potentially consume significant amount of space.
     * </p>
     *
     * @return the cached contents of the wrapped snapshot
     *  (never <code>null</code>)
     */
    @Override
    public String getContents()
    {
        return contents;
    }

    /**
     * Returns the snapshot wrapped by this snapshot.
     * 
     * @return the wrapped snapshot (never <code>null</code>)
     */
    public ISnapshot getWrappedSnapshot()
    {
        return wrapped;
    }
}
