/*******************************************************************************
 * Copyright (c) 2016, 2020 1C-Soft LLC.
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
package org.eclipse.handly.buffer;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * <code>TextFileBuffer</code> tests.
 */
public class TextFileBufferTest
    extends WorkspaceTestCase
{
    private TextFileBuffer buffer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = getProject("p");
        p.create(null);
        p.open(null);
        buffer = TextFileBuffer.forFile(p.getFile("f"));
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (buffer != null)
            buffer.release();
        super.tearDown();
    }

    public void testBug496840() throws Throwable
    {
        buffer.applyChange(new BufferChange(new InsertEdit(0, "a")), null);

        Throwable[] exception = new Throwable[1];
        Display display = PlatformUI.createDisplay();
        try (
            DisplayAutoCloseable r1 = new DisplayAutoCloseable(display);
            WorkbenchAutoCloseable r2 = new WorkbenchAutoCloseable())
        {
            PlatformUI.createAndRunWorkbench(display, new WorkbenchAdvisor()
            {
                @Override
                public void postStartup()
                {
                    try
                    {
                        buffer.applyChange(new BufferChange(new InsertEdit(0,
                            "b")), null);

                        buffer.getCoreTextFileBufferProvider().getBuffer().requestSynchronizationContext();
                        buffer.applyChange(new BufferChange(new InsertEdit(0,
                            "c")), null);
                    }
                    catch (CoreException e)
                    {
                        exception[0] = e;
                    }
                    getWorkbenchConfigurer().emergencyClose();
                }

                @Override
                public void eventLoopException(Throwable e)
                {
                    exception[0] = e;
                }

                @Override
                public boolean openWindows()
                {
                    return true;
                }

                @Override
                public String getInitialWindowPerspectiveId()
                {
                    return null;
                }
            });
            if (exception[0] != null)
                throw exception[0];
        }

        assertFalse(PlatformUI.isWorkbenchRunning());
        try
        {
            buffer.applyChange(new BufferChange(new InsertEdit(0, "d")), null);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Synchronization context is requested but workbench is not running
        }

        buffer.getCoreTextFileBufferProvider().getBuffer().releaseSynchronizationContext();
        buffer.applyChange(new BufferChange(new InsertEdit(0, "d")), null);
        assertEquals("dcba", buffer.getSnapshot().getContents());
    }

    public void testSaveListener() throws Exception
    {
        SaveListener listener = new SaveListener();
        buffer.addListener(listener);
        try
        {
            buffer.applyChange(new BufferChange(new InsertEdit(0, "a")), null);
            assertFalse(buffer.isDirty());
            assertSame(buffer, listener.savedBuffer);
            listener.savedBuffer = null;
            // wait a second to allow for filesystem timestamp precision variances
            Thread.sleep(1000);

            buffer.save(EMPTY_CONTEXT, null); // noop
            assertNull(listener.savedBuffer);

            buffer.applyChange(new BufferChange(new InsertEdit(0, "b")), null);
            assertFalse(buffer.isDirty());
            assertSame(buffer, listener.savedBuffer);
            listener.savedBuffer = null;
            Thread.sleep(1000);

            BufferChange change = new BufferChange(new InsertEdit(0, "c"));
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            IBufferChange undoChange = buffer.applyChange(change, null);
            assertTrue(buffer.isDirty());
            assertNull(listener.savedBuffer);

            buffer.applyChange(undoChange, null);
            assertFalse(buffer.isDirty());
            assertNull(listener.savedBuffer);
        }
        finally
        {
            buffer.removeListener(listener);
        }
    }

    private static class DisplayAutoCloseable
        implements AutoCloseable
    {
        private final Display display;

        DisplayAutoCloseable(Display display)
        {
            this.display = display;
        }

        @Override
        public void close()
        {
            display.dispose();
        }
    }

    private class WorkbenchAutoCloseable
        implements AutoCloseable
    {
        @Override
        public void close()
        {
            if (PlatformUI.isWorkbenchRunning())
                assertTrue(PlatformUI.getWorkbench().close());
        }
    }

    private static class SaveListener
        implements IBufferListener
    {
        IBuffer savedBuffer;

        @Override
        public void bufferSaved(IBuffer buffer)
        {
            savedBuffer = buffer;
        }
    }
}
