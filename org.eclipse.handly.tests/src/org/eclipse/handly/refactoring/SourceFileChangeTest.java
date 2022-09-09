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
package org.eclipse.handly.refactoring;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.SaveMode;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.support.SimpleModelManager;
import org.eclipse.handly.model.impl.support.SimpleSourceFile;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * <code>SourceFileChange</code> tests.
 */
public class SourceFileChangeTest
    extends NoJobsWorkspaceTestCase
{
    private ISourceFile sourceFile;
    private IBuffer buffer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFile file = setUpProject("Test001").getFile("a.foo");
        sourceFile = new SimpleSourceFile(null, file.getName(), file,
            new SimpleModelManager());
        buffer = Elements.getBuffer(sourceFile);
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (buffer != null)
            buffer.release();
        super.tearDown();
    }

    public void test01() throws Exception
    {
        TextEdit edit = new InsertEdit(0, "foo");
        SourceFileChange change = new SourceFileChange("", sourceFile, edit);
        assertSame(edit, change.getEdit());
        assertEquals(sourceFile, change.getModifiedElement());
        assertEquals(Arrays.asList(Elements.getFile(sourceFile)), Arrays.asList(
            change.getAffectedObjects()));
        assertTrue(change.isValid(null).isOK());
        Change undoChange = change.perform(null);
        assertEquals("foo", buffer.getDocument().get());
        assertFalse(buffer.isDirty());

        assertEquals(sourceFile, undoChange.getModifiedElement());
        assertEquals(Arrays.asList(Elements.getFile(sourceFile)), Arrays.asList(
            undoChange.getAffectedObjects()));
        undoChange.initializeValidationData(null);
        assertTrue(undoChange.isValid(null).isOK());
        Change redoChange = undoChange.perform(null);
        assertEquals("", buffer.getDocument().get());
        assertFalse(buffer.isDirty());

        assertEquals(sourceFile, redoChange.getModifiedElement());
        assertEquals(Arrays.asList(Elements.getFile(sourceFile)), Arrays.asList(
            redoChange.getAffectedObjects()));
        redoChange.initializeValidationData(null);
        assertTrue(redoChange.isValid(null).isOK());
        redoChange.perform(null);
        assertEquals("foo", buffer.getDocument().get());
        assertFalse(buffer.isDirty());
    }

    public void test02() throws Exception
    {
        SourceFileChange change = new SourceFileChange("", sourceFile,
            new InsertEdit(0, "foo"));
        change.setSaveMode(SaveMode.LEAVE_UNSAVED);
        change.perform(null);
        assertEquals("foo", buffer.getDocument().get());
        assertTrue(buffer.isDirty());

        change = new SourceFileChange("", sourceFile, new ReplaceEdit(0, 3,
            "bar"));
        change.perform(null);
        assertEquals("bar", buffer.getDocument().get());
        assertTrue(buffer.isDirty()); // keep saved state

        change = new SourceFileChange("", sourceFile, new DeleteEdit(0, 3));
        change.setSaveMode(SaveMode.FORCE_SAVE);
        change.perform(null);
        assertEquals("", buffer.getDocument().get());
        assertFalse(buffer.isDirty());
    }

    public void test03() throws Exception
    {
        SourceFileChange change = new SourceFileChange("", sourceFile,
            new InsertEdit(0, "foo"));
        change.setBase(new DocumentSnapshot(buffer.getDocument()));
        assertTrue(change.isValid(null).isOK());
        buffer.getDocument().set("bar");
        assertTrue(change.isValid(null).hasFatalError());
        try
        {
            change.perform(null);
            fail();
        }
        catch (CoreException e)
        {
            assertTrue(e.getCause() instanceof StaleSnapshotException);
        }
    }

    public void test04() throws Exception
    {
        SourceFileChange change = new SourceFileChange("", sourceFile,
            new InsertEdit(0, "foo"));
        Change undoChange = change.perform(null);
        undoChange.initializeValidationData(null);
        assertTrue(undoChange.isValid(null).isOK());
        buffer.getDocument().set("");
        assertTrue(undoChange.isValid(null).hasFatalError());
        try
        {
            undoChange.perform(null);
            fail();
        }
        catch (CoreException e)
        {
            assertTrue(e.getCause() instanceof StaleSnapshotException);
        }
    }

    public void test05() throws Exception
    {
        SourceFileChange change = new SourceFileChange("", sourceFile,
            new InsertEdit(0, "foo"));
        Change undoChange = change.perform(null);
        undoChange.initializeValidationData(null);
        assertTrue(undoChange.isValid(null).isOK());
        Elements.getFile(sourceFile).delete(true, null);
        assertTrue(undoChange.isValid(null).hasFatalError());
    }

    public void test06() throws Exception
    {
        SourceFileChange change = new SourceFileChange("", sourceFile,
            new InsertEdit(0, "foo"));
        Change undoChange = change.perform(null);
        Change undoDeleteResourceChange = new DeleteResourceChange(
            Elements.getFile(sourceFile).getFullPath(), true).perform(null);
        undoChange.initializeValidationData(null);
        assertTrue(undoChange.isValid(null).isOK());
        undoDeleteResourceChange.perform(new NullProgressMonitor());
        assertTrue(undoChange.isValid(null).hasFatalError());
    }

    public void test07() throws Exception
    {
        buffer.getDocument().set("\nfoo\n");

        TextEdit edit1 = new InsertEdit(0, "bar");
        TextEdit edit2 = new DeleteEdit(1, 3);
        TextEdit edit3 = new InsertEdit(5, "baz");

        SourceFileChange change = new SourceFileChange("", sourceFile);
        change.addGroupedEdits(new TextEditGroup("bar", edit1));
        change.addGroupedEdits(new TextEditBasedChangeGroup(change,
            new TextEditGroup("foo", edit2)));
        change.addGroupedEdits(new TextEditGroup("baz", edit3));

        assertEquals(Arrays.asList(edit1, edit2, edit3), Arrays.asList(
            change.getEdit().getChildren()));
        TextEditBasedChangeGroup[] groups = change.getChangeGroups();
        assertEquals(3, groups.length);

        assertEquals("\nfoo\n", change.getCurrentContent(null));
        assertEquals("bar\n\nbaz", change.getPreviewContent(null));

        assertGroupContent(groups[0], 0, "", "bar");
        assertGroupContent(groups[1], 0, "foo", "");
        assertGroupContent(groups[2], 0, "", "baz");

        assertGroupContent(groups[0], 1, "", "bar\nfoo");
        assertGroupContent(groups[1], 1, "\nfoo\n", "\n");
        assertGroupContent(groups[2], 1, "foo\n", "foo\nbaz");

        assertGroupContent(groups[0], 2, "\nfoo", "bar\nfoo\n");
        assertGroupContent(groups[1], 2, "\nfoo\n", "\n\n");
        assertGroupContent(groups[2], 2, "\nfoo\n", "\nfoo\nbaz");

        groups[1].setEnabled(false);
        groups[2].setEnabled(false);
        assertEquals("bar\nfoo\n", change.getPreviewContent(null));

        change.perform(null);
        assertEquals("bar\nfoo\n", change.getCurrentContent(null));
    }

    public void test08() throws Exception
    {
        buffer.getDocument().set("012345");

        TextEdit edit = new ReplaceEdit(2, 3, "xy");

        SourceFileChange change = new SourceFileChange("", sourceFile, edit);
        change.perform(null);
        assertEquals("01xy5", buffer.getDocument().get());
        assertRegion(edit.getRegion(), 2, 2); // update regions
    }

    public void test09() throws Exception
    {
        buffer.getDocument().set("012345");

        TextEdit edit = new ReplaceEdit(2, 3, "xy");

        SourceFileChange change = new SourceFileChange("", sourceFile, edit);
        change.setKeepPreviewEdits(true);

        try
        {
            change.getPreviewEdit(edit);
            fail();
        }
        catch (IllegalStateException e)
        {
        }
        try
        {
            change.getPreviewEdits(new TextEdit[] { edit });
            fail();
        }
        catch (IllegalStateException e)
        {
        }

        assertEquals("01xy5", change.getPreviewContent(null));

        TextEdit previewEdit = change.getPreviewEdit(edit);
        assertRegion(previewEdit.getRegion(), 2, 2);

        assertEquals(Arrays.asList(previewEdit), Arrays.asList(
            change.getPreviewEdits(new TextEdit[] { edit })));

        change.setKeepPreviewEdits(false);
        try
        {
            change.getPreviewEdit(edit);
            fail();
        }
        catch (IllegalStateException e)
        {
        }
        try
        {
            change.getPreviewEdits(new TextEdit[] { edit });
            fail();
        }
        catch (IllegalStateException e)
        {
        }
    }

    public void test10() throws Exception
    {
        buffer.getDocument().set("012345");

        TextEdit edit1 = new ReplaceEdit(1, 4, "9876");
        TextEdit edit2 = new ReplaceEdit(2, 2, "xy");
        TextEdit edit3 = new ReplaceEdit(1, 4, "1234");

        SourceFileChange change = new SourceFileChange("", sourceFile);
        change.addEdits(new TextEdit[] { edit1, edit2, edit3 });
        assertSame(change.getEdit(), edit1.getParent());
        assertSame(edit3, edit2.getParent());
        assertSame(edit1, edit3.getParent());
        assertEquals("098765", change.getPreviewContent(null));
    }

    private static void assertGroupContent(TextEditBasedChangeGroup group,
        int surroundingLines, String expectedCurrentContent,
        String expectedPreviewContent) throws CoreException
    {
        TextEditBasedChange change = group.getTextEditChange();
        assertEquals(expectedCurrentContent, change.getCurrentContent(
            group.getRegion(), true, surroundingLines, null));
        assertEquals(expectedPreviewContent, change.getPreviewContent(
            new TextEditBasedChangeGroup[] { group }, group.getRegion(), true,
            surroundingLines, null));
    }

    private static void assertRegion(IRegion region, int expectedOffset,
        int expectedLength)
    {
        assertEquals(expectedOffset, region.getOffset());
        assertEquals(expectedLength, region.getLength());
    }
}
