/*******************************************************************************
 * Copyright (c) 2018, 2022 1C-Soft LLC.
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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.Buffer;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.handly.model.ISourceConstruct;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.TextFileSnapshot;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;

/**
 * Source file tests.
 */
public class SourceFileTest
    extends NoJobsWorkspaceTestCase
{
    private SimpleSourceFile sourceFile;
    private SimpleSourceConstruct aChild;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFile file = setUpProject("Test001").getFile("a.foo");
        sourceFile = new SimpleSourceFile(null, file.getName(), file,
            new SimpleModelManager())
        {
            @Override
            public void buildSourceStructure_(IContext context,
                IProgressMonitor monitor) throws CoreException
            {
                SourceElementBody body = new SourceElementBody();
                if ("A".equals(context.get(SOURCE_CONTENTS)))
                {
                    body.addChild(aChild);

                    SourceElementBody aBody = new SourceElementBody();
                    aBody.setFullRange(new TextRange(0, 1));
                    context.get(NEW_ELEMENTS).put(aChild, aBody);
                }
                context.get(NEW_ELEMENTS).put(this, body);
            }
        };
        aChild = sourceFile.getChild("A");
    }

    public void test01() throws Exception
    {
        assertTrue(sourceFile.exists_());
        sourceFile.getFile_().delete(true, null);
        assertFalse(sourceFile.exists_());
        sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            assertTrue(sourceFile.exists_());
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }
        assertFalse(sourceFile.exists_());
    }

    public void test02() throws Exception
    {
        ISourceElementInfo info = sourceFile.getSourceElementInfo_(
            EMPTY_CONTEXT, null);
        assertEquals(0, info.getChildren().length);
        assertEquals(new TextRange(0, 0), info.getFullRange());
        assertNull(info.getIdentifyingRange());
        assertTrue(new TextFileSnapshot(sourceFile.getFile_(),
            TextFileSnapshot.Layer.FILESYSTEM).isEqualTo(info.getSnapshot()));
        assertFalse(aChild.exists_());
    }

    public void test03() throws Exception
    {
        try (IBuffer buffer = sourceFile.getBuffer_(EMPTY_CONTEXT, null))
        {
            buffer.applyChange(new BufferChange(new InsertEdit(0, "A")), null);
        }
        ISourceElementInfo info = sourceFile.getSourceElementInfo_(
            EMPTY_CONTEXT, null);
        ISourceConstruct[] children = info.getChildren();
        assertEquals(1, children.length);
        assertEquals(aChild, children[0]);
        assertEquals(new TextRange(0, 1), info.getFullRange());
        assertNull(info.getIdentifyingRange());
        assertTrue(new TextFileSnapshot(sourceFile.getFile_(),
            TextFileSnapshot.Layer.FILESYSTEM).isEqualTo(info.getSnapshot()));
        assertChildInfo();
    }

    public void test04() throws Exception
    {
        test02();
        assertFalse(sourceFile.needsReconciling_()); // not a working copy
        sourceFile.reconcile_(EMPTY_CONTEXT, null); // -> no effect
        test02();
    }

    public void test05() throws Exception
    {
        test02();
        sourceFile.close_();
        test03();
    }

    public void test06() throws Exception
    {
        assertFalse(sourceFile.acquireExistingWorkingCopy_(null));
        boolean b = sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            assertTrue(b);
            b = sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
            try
            {
                assertFalse(b);
                b = sourceFile.acquireExistingWorkingCopy_(null);
                try
                {
                    assertTrue(b);
                }
                finally
                {
                    assertFalse(sourceFile.releaseWorkingCopy_());
                }
            }
            finally
            {
                assertFalse(sourceFile.releaseWorkingCopy_());
            }
        }
        finally
        {
            assertTrue(sourceFile.releaseWorkingCopy_());
        }
    }

    public void test07() throws Exception
    {
        sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            test02();

            assertFalse(sourceFile.needsReconciling_());

            sourceFile.reconcile_(EMPTY_CONTEXT, null);

            test02();

            ISnapshot snapshot;
            try (IBuffer buffer = sourceFile.getBuffer_(EMPTY_CONTEXT, null))
            {
                IDocument document = buffer.getDocument();
                document.set("A");
                snapshot = new DocumentSnapshot(document);
            }

            assertTrue(sourceFile.needsReconciling_());

            sourceFile.reconcile_(EMPTY_CONTEXT, null);

            ISourceElementInfo info = sourceFile.getSourceElementInfo_(
                EMPTY_CONTEXT, null);
            ISourceConstruct[] children = info.getChildren();
            assertEquals(1, children.length);
            assertEquals(aChild, children[0]);
            assertEquals(new TextRange(0, 1), info.getFullRange());
            assertNull(info.getIdentifyingRange());
            assertTrue(snapshot.isEqualTo(info.getSnapshot()));
            assertChildInfo();
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }

        test02();
    }

    public void test08() throws Exception
    {
        Buffer buffer = new Buffer("A");
        sourceFile.becomeWorkingCopy_(of(
            ISourceFileImplExtension.WORKING_COPY_BUFFER, buffer), null);
        try
        {
            ISourceElementInfo info = sourceFile.getSourceElementInfo_(
                EMPTY_CONTEXT, null);
            ISourceConstruct[] children = info.getChildren();
            assertEquals(1, children.length);
            assertEquals(aChild, children[0]);
            assertEquals(new TextRange(0, 1), info.getFullRange());
            assertNull(info.getIdentifyingRange());
            assertTrue(new DocumentSnapshot(buffer.getDocument()).isEqualTo(
                info.getSnapshot()));
            assertChildInfo();
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }

        test02();
    }

    public void test09() throws Exception
    {
        assertNull(sourceFile.peekAtBody_());
        sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            assertNotNull(sourceFile.peekAtBody_());
            sourceFile.close_(); // cannot close a working copy
            assertNotNull(sourceFile.peekAtBody_());
        }
        finally
        {
            assertTrue(sourceFile.releaseWorkingCopy_());
        }
        assertNull(sourceFile.peekAtBody_());
    }

    private void assertChildInfo() throws Exception
    {
        ISourceElementInfo info = aChild.getSourceElementInfo_(EMPTY_CONTEXT,
            null);
        assertEquals(0, info.getChildren().length);
        assertEquals(new TextRange(0, 1), info.getFullRange());
        ISnapshot snapshot = sourceFile.getSourceElementInfo_(EMPTY_CONTEXT,
            null).getSnapshot();
        assertNotNull(snapshot);
        assertTrue(snapshot.isEqualTo(info.getSnapshot()));
    }
}
