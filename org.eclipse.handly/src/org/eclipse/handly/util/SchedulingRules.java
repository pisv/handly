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
 *     (inspired by org.eclipse.debug.internal.ui.viewers.AsynchronousSchedulingRuleFactory)
 *******************************************************************************/
package org.eclipse.handly.util;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Provides static utility methods for obtaining and manipulating
 * scheduling rules.
 *
 * @since 1.1
 */
public class SchedulingRules
{
    /**
     * Scheduling rules returned by this method do not allow nesting
     * and will conflict with each other if and only if they correspond
     * to the same (i.e., equal) lock object.
     * 
     * @param lock a lock object (not <code>null</code>)
     * @return the corresponding scheduling rule (never <code>null</code>)
     */
    public static ISchedulingRule getSerialPerObjectRule(Object lock)
    {
        return new SerialPerObjectRule(lock);
    }

    private SchedulingRules()
    {
    }

    private static class SerialPerObjectRule
        implements ISchedulingRule
    {
        private final Object lock;

        SerialPerObjectRule(Object lock)
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
}
