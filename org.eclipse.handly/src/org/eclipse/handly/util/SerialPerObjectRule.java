/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.util;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A scheduling rule that does not allow nesting and conflicts only with
 * a <code>SerialPerObjectRule</code> pertaining to the same (i.e.,
 * equal) lock object.
 *
 * @since 1.1
 */
public final class SerialPerObjectRule
    implements ISchedulingRule
{
    private final Object lock;

    /**
     * Creates a new {@link SerialPerObjectRule} for the given lock object.
     *
     * @param lock a lock object (not <code>null</code>)
     */
    public SerialPerObjectRule(Object lock)
    {
        if (lock == null)
            throw new IllegalArgumentException();
        this.lock = lock;
    }

    @Override
    public boolean contains(ISchedulingRule rule)
    {
        return rule == this;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule)
    {
        if (rule instanceof SerialPerObjectRule)
        {
            return lock.equals(((SerialPerObjectRule)rule).lock);
        }
        return false;
    }
}
