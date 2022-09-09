/*******************************************************************************
 * Copyright (c) 2022 1C-Soft LLC.
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
package org.eclipse.handly.junit;

/**
 * A {@link WorkspaceTestCase} that suspends execution of all jobs
 * in its {@link #setUp()} method and resumes execution of jobs in its
 * {@link #tearDown()} method. It ensures that no jobs will execute
 * while a test is running.
 *
 * @since 1.7
 */
public class NoJobsWorkspaceTestCase
    extends WorkspaceTestCase
{
    /**
     * No-arg constructor to enable serialization. This method
     * is not intended to be used by mere mortals without calling setName().
     */
    public NoJobsWorkspaceTestCase()
    {
        super();
    }

    /**
     * Constructs a test case with the given name.
     */
    public NoJobsWorkspaceTestCase(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        suspendJobs();
    }

    @Override
    protected void tearDown() throws Exception
    {
        resumeJobs();
        super.tearDown();
    }
}
