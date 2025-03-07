/*******************************************************************************
 * Copyright (c) 2015, 2022 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.SaveMode;
import org.eclipse.handly.examples.jmodel.ICompilationUnit;
import org.eclipse.handly.examples.jmodel.IField;
import org.eclipse.handly.examples.jmodel.IMethod;
import org.eclipse.handly.examples.jmodel.IType;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Working copy change notification tests.
 */
public class WorkingCopyNotificationTest
    extends NoJobsWorkspaceTestCase
{
    private CompilationUnit workingCopy;
    private JavaModelListener listener = new JavaModelListener();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject project = setUpProject("Test010");
        workingCopy = (CompilationUnit)JavaModelCore.createCompilationUnitFrom(
            project.getFile(new Path("src/X.java")));
        workingCopy.getJavaModel().addElementChangeListener(listener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (workingCopy != null)
            workingCopy.getJavaModel().removeElementChangeListener(listener);
        super.tearDown();
    }

    public void test001() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            //@formatter:off
            listener.assertDelta(
                "Java Model[*]: {CHILDREN}\n" +
                "  Test010[*]: {CHILDREN}\n" +
                "    src[*]: {CHILDREN}\n" +
                "      <default>[*]: {CHILDREN}\n" +
                "        [Working copy] X.java[*]: {WORKING COPY}"
            );
            //@formatter:on

            workingCopy.getFile().touch(monitor);

            //@formatter:off
            listener.assertDelta(
                "Java Model[*]: {CHILDREN}\n" +
                "  Test010[*]: {CHILDREN}\n" +
                "    src[*]: {CHILDREN}\n" +
                "      <default>[*]: {CHILDREN}\n" +
                "        [Working copy] X.java[*]: {CONTENT | UNDERLYING RESOURCE}"
            );
            //@formatter:on
        });
        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test010[*]: {CHILDREN}\n" +
            "    src[*]: {CHILDREN}\n" +
            "      <default>[*]: {CHILDREN}\n" +
            "        X.java[*]: {WORKING COPY}"
        );
        //@formatter:on
    }

    public void test002() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            IType typeX = workingCopy.getType("X");
            TextRange r = typeX.getSourceElementInfo().getIdentifyingRange();
            BufferChange change = new BufferChange(new ReplaceEdit(
                r.getOffset(), r.getLength(), "Y"));
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            try (IBuffer buffer = workingCopy.getBuffer())
            {
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            //@formatter:off
            listener.assertDelta(
                "[Working copy] X.java[*]: {CHILDREN | CONTENT | FINE GRAINED}\n" +
                "  Y[+]: {}\n" +
                "  X[-]: {}"
            );
            //@formatter:on
        });
    }

    public void test003() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            IField fieldX = workingCopy.getType("X").getField("x");
            TextRange r = fieldX.getSourceElementInfo().getFullRange();
            BufferChange change = new BufferChange(new DeleteEdit(r.getOffset(),
                r.getLength()));
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            try (IBuffer buffer = workingCopy.getBuffer())
            {
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            //@formatter:off
            listener.assertDelta(
                "[Working copy] X.java[*]: {CHILDREN | CONTENT | FINE GRAINED}\n" +
                "  X[*]: {CHILDREN | FINE GRAINED}\n" +
                "    x[-]: {}"
            );
            //@formatter:on

            listener.delta = null;

            change = new BufferChange(new InsertEdit(r.getOffset(), "int y;"));
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            try (IBuffer buffer = workingCopy.getBuffer())
            {
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            //@formatter:off
            listener.assertDelta(
                "[Working copy] X.java[*]: {CHILDREN | CONTENT | FINE GRAINED}\n" +
                "  X[*]: {CHILDREN | FINE GRAINED}\n" +
                "    y[+]: {}"
            );
            //@formatter:on
        });
    }

    public void test004() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            IMethod methodFI = workingCopy.getType("X").getMethod("f",
                new String[] { "I" });
            TextRange r = methodFI.getSourceElementInfo().getFullRange();
            BufferChange change = new BufferChange(new ReplaceEdit(
                r.getOffset(), r.getLength(), "void f() {}"));
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            try (IBuffer buffer = workingCopy.getBuffer())
            {
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            //@formatter:off
            listener.assertDelta(
                "[Working copy] X.java[*]: {CHILDREN | CONTENT | FINE GRAINED}\n" +
                "  X[*]: {CHILDREN | FINE GRAINED}\n" +
                "    f()[+]: {}\n" +
                "    f(int)[-]: {}"
            );
            //@formatter:on
        });
    }

    public void test005() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            IMethod methodFI = workingCopy.getType("X").getMethod("f",
                new String[] { "I" });
            TextRange r = methodFI.getSourceElementInfo().getFullRange();
            BufferChange change = new BufferChange(new ReplaceEdit(
                r.getOffset(), r.getLength(), "void f(int y) {}")); // renamed arg
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            try (IBuffer buffer = workingCopy.getBuffer())
            {
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            //@formatter:off
            listener.assertDelta(
                "[Working copy] X.java[*]: {CHILDREN | CONTENT | FINE GRAINED}\n" +
                "  X[*]: {CHILDREN | FINE GRAINED}\n" +
                "    f(int)[*]: {CONTENT | FINE GRAINED}"
            );
            //@formatter:on
        });
    }

    public void test006() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            try (IBuffer buffer = workingCopy.getBuffer())
            {
                TextEdit edit = new MultiTextEdit();
                try
                {
                    IDocument document = buffer.getDocument();
                    MoveSourceEdit source = new MoveSourceEdit(
                        document.getLineOffset(2), document.getLineLength(2));
                    MoveTargetEdit target = new MoveTargetEdit(
                        document.getLineOffset(4), source);
                    edit.addChild(source);
                    edit.addChild(target);
                }
                catch (BadLocationException e)
                {
                    throw new AssertionError();
                }
                BufferChange change = new BufferChange(edit);
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            //@formatter:off
            listener.assertDelta(
                "[Working copy] X.java[*]: {CHILDREN | CONTENT | FINE GRAINED}\n" +
                "  X[*]: {CHILDREN | FINE GRAINED}\n" +
                "    f(int)[*]: {REORDERED | FINE GRAINED}\n" +
                "    x[*]: {REORDERED | FINE GRAINED}"
            );
            //@formatter:on
        });
    }

    public void test007() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            BufferChange change = new BufferChange(new InsertEdit(0, "\n"));
            change.setSaveMode(SaveMode.LEAVE_UNSAVED);
            try (IBuffer buffer = workingCopy.getBuffer())
            {
                buffer.applyChange(change, null);
            }

            assertNull(listener.delta);

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            listener.assertDelta(
                "[Working copy] X.java[*]: {CONTENT | FINE GRAINED}");
        });
    }

    public void test008() throws Exception
    {
        doWithWorkingCopy(monitor ->
        {
            listener.delta = null;

            workingCopy.reconcile(ICompilationUnit.NO_AST, 0, monitor);

            assertNull(listener.delta);
        });
    }

    private void doWithWorkingCopy(ICoreRunnable runnable) throws CoreException
    {
        workingCopy.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            runnable.run(null);
        }
        finally
        {
            workingCopy.releaseWorkingCopy_();
        }
    }
}
