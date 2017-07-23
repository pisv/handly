/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.buffer.Buffer;

import junit.framework.TestCase;

/**
 * Working copy tests.
 */
public class WorkingCopyTest
    extends TestCase
{
    private SimpleSourceFile sourceFile = new SimpleSourceFile(null, null, null,
        new SimpleModelManager());

    public void testBug479623() throws Exception
    {
        // concurrent creation/acquisition of working copy
        final boolean[] stop = new boolean[1];
        final boolean[] failure = new boolean[1];
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!stop[0])
                {
                    if (sourceFile.hAcquireExistingWorkingCopy(null))
                    {
                        try
                        {
                            WorkingCopyInfo info =
                                sourceFile.hElementManager().peekAtWorkingCopyInfo(
                                    sourceFile);
                            if (!info.isInitialized())
                            {
                                failure[0] = true;
                                return;
                            }
                        }
                        finally
                        {
                            sourceFile.hReleaseWorkingCopy();
                        }
                    }
                }
            }
        });
        thread.start();
        try (Buffer buffer = new Buffer())
        {
            sourceFile.hBecomeWorkingCopy(of(
                ISourceFileImplExtension.WORKING_COPY_BUFFER, buffer), null);
            sourceFile.hReleaseWorkingCopy();
            assertFalse(failure[0]);
            assertTrue(thread.isAlive());
        }
        finally
        {
            stop[0] = true;
            thread.join();
        }
    }

    public void testCallbackLifecycle() throws Exception
    {
        class TestCallback
            extends DefaultWorkingCopyCallback
        {
            boolean onInit, onDispose;

            @Override
            public void onInit(IWorkingCopyInfo info) throws CoreException
            {
                super.onInit(info);
                onInit = true;
            }

            @Override
            public void onDispose()
            {
                onDispose = true;
                super.onDispose();
            }
        }
        class ThrowingCallback
            extends TestCallback
        {
            @Override
            public void onInit(IWorkingCopyInfo info) throws CoreException
            {
                super.onInit(info);
                throw new CoreException(Status.CANCEL_STATUS);
            };
        }

        try (Buffer buffer = new Buffer())
        {
            TestCallback callback1 = new TestCallback();
            assertTrue(sourceFile.hBecomeWorkingCopy(with(of(
                ISourceFileImplExtension.WORKING_COPY_BUFFER, buffer), of(
                    ISourceFileImplExtension.WORKING_COPY_CALLBACK, callback1)),
                null));
            assertTrue(callback1.onInit);
            assertFalse(callback1.onDispose);

            TestCallback callback2 = new TestCallback();
            assertFalse(sourceFile.hBecomeWorkingCopy(with(of(
                ISourceFileImplExtension.WORKING_COPY_BUFFER, buffer), of(
                    ISourceFileImplExtension.WORKING_COPY_CALLBACK, callback2)),
                null));
            assertFalse(sourceFile.hReleaseWorkingCopy());
            assertFalse(callback2.onInit);
            assertFalse(callback2.onDispose);
            assertFalse(callback1.onDispose);

            assertTrue(sourceFile.hReleaseWorkingCopy());
            assertTrue(callback1.onDispose);

            ThrowingCallback callback3 = new ThrowingCallback();
            try
            {
                sourceFile.hBecomeWorkingCopy(with(of(
                    ISourceFileImplExtension.WORKING_COPY_BUFFER, buffer), of(
                        ISourceFileImplExtension.WORKING_COPY_CALLBACK,
                        callback3)), null);
                fail();
            }
            catch (CoreException e)
            {
                assertSame(e.getStatus(), Status.CANCEL_STATUS);
            }
            assertTrue(callback3.onInit);
            assertTrue(callback3.onDispose);
        }
    }
}
