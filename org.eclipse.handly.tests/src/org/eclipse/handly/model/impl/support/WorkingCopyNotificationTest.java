/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.IElementChangeEvent.POST_CHANGE;
import static org.eclipse.handly.model.IElementChangeEvent.POST_RECONCILE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.Buffer;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;

/**
 * Working copy change notification tests.
 */
public class WorkingCopyNotificationTest
    extends WorkspaceTestCase
{
    private SimpleSourceFile sourceFile;
    private SimpleSourceConstruct aChild;
    private List<IElementChangeEvent> events;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        NotificationManager notificationManager = new NotificationManager();
        notificationManager.addElementChangeListener((event) -> events.add(
            event));
        SimpleModelManager modelManager = new SimpleModelManager();
        modelManager.model.context = of(INotificationManager.class,
            notificationManager);
        IFile file = setUpProject("Test001").getFile("a.foo");
        sourceFile = new SimpleSourceFile(null, file.getName(), file,
            modelManager)
        {
            @Override
            public void buildSourceStructure_(IContext context,
                IProgressMonitor monitor) throws CoreException
            {
                SourceElementBody body = new SourceElementBody();
                if ("A".equals(context.get(SOURCE_CONTENTS)))
                {
                    body.addChild(aChild);

                    context.get(NEW_ELEMENTS).put(aChild,
                        new SourceElementBody());
                }
                context.get(NEW_ELEMENTS).put(this, body);
            };
        };
        aChild = sourceFile.getChild("A");
        events = new ArrayList<>();
    }

    public void test1() throws Exception
    {
        sourceFile.reconcile_(EMPTY_CONTEXT, null); // not a working copy
        assertTrue(events.isEmpty()); // -> no effect
    }

    public void test2() throws Exception
    {
        sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            assertEquals(1, events.size());
            assertEvent(0, POST_CHANGE,
                "[Working copy] a.foo[*]: {WORKING COPY}");

            sourceFile.reconcile_(EMPTY_CONTEXT, null); // no changes
            assertEquals(1, events.size()); // -> no effect

            try (IBuffer buffer = sourceFile.getBuffer_(EMPTY_CONTEXT, null))
            {
                buffer.applyChange(new BufferChange(new InsertEdit(0, "A")),
                    null);
            }

            sourceFile.reconcile_(EMPTY_CONTEXT, null);
            assertEquals(2, events.size());
            assertEvent(1, POST_RECONCILE,
                "[Working copy] a.foo[*]: {CHILDREN | CONTENT | FINE GRAINED}\n"
                    + "  A[+]: {}");
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }
        assertEquals(3, events.size());
        assertEvent(2, POST_CHANGE, "a.foo[*]: {WORKING COPY}");
    }

    public void test3() throws Exception
    {
        sourceFile.becomeWorkingCopy_(of(
            ISourceFileImplExtension.WORKING_COPY_BUFFER, new Buffer("A")),
            null);
        try
        {
            assertEquals(2, events.size());
            assertEvent(0, POST_CHANGE,
                "[Working copy] a.foo[*]: {WORKING COPY}");
            assertEvent(1, POST_RECONCILE,
                "[Working copy] a.foo[*]: {CHILDREN | CONTENT | FINE GRAINED}\n"
                    + "  A[+]: {}");

            sourceFile.reconcile_(of(Elements.FORCE_RECONCILING, true), null); // no changes
            assertEquals(2, events.size()); // -> no effect

            try (IBuffer buffer = sourceFile.getBuffer_(EMPTY_CONTEXT, null))
            {
                buffer.applyChange(new BufferChange(new DeleteEdit(0, 1)),
                    null);
            }

            sourceFile.reconcile_(EMPTY_CONTEXT, null);
            assertEquals(3, events.size());
            assertEvent(2, POST_RECONCILE,
                "[Working copy] a.foo[*]: {CHILDREN | CONTENT | FINE GRAINED}\n"
                    + "  A[-]: {}");
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }
        assertEquals(4, events.size());
        assertEvent(3, POST_CHANGE, "a.foo[*]: {WORKING COPY}");
    }

    public void test4() throws Exception
    {
        sourceFile.getFile_().delete(true, null);
        sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            assertEquals(1, events.size());
            assertEvent(0, POST_CHANGE,
                "[Working copy] a.foo[+]: {WORKING COPY}");
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }
        assertEquals(2, events.size());
        assertEvent(1, POST_CHANGE, "a.foo[-]: {WORKING COPY}");
    }

    private void assertEvent(int index, int type, String expectedDelta)
    {
        IElementChangeEvent event = events.get(index);
        assertEquals(type, event.getType());
        assertEquals(expectedDelta, event.getDeltas()[0].toString());
    }
}
