/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.SaveMode;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.internal.examples.basic.ui.model.FooFile;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.DelegatingWorkingCopyBuffer;
import org.eclipse.handly.model.impl.IWorkingCopyBuffer;
import org.eclipse.handly.model.impl.WorkingCopyInfoFactory;
import org.eclipse.handly.model.impl.WorkingCopyReconciler;
import org.eclipse.handly.util.TextRange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <code>FooFile</code> working copy tests.
 */
public class FooWorkingCopyTest
    extends WorkspaceTestCase
{
    private FooFile workingCopy;
    private IWorkingCopyBuffer buffer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFooProject fooProject = FooModelCore.create(setUpProject("Test002"));
        workingCopy = (FooFile)fooProject.getFooFile("test.foo");
        buffer = new DelegatingWorkingCopyBuffer(workingCopy.openBuffer(null),
            new WorkingCopyReconciler(workingCopy));
    }

    @Override
    protected void tearDown() throws Exception
    {
        buffer.dispose();
        super.tearDown();
    }

    public void test1() throws Exception
    {
        doWithWorkingCopy(new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IFooDef[] defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                IFooDef def = workingCopy.getDef("f", 0);
                assertEquals(def, defs[0]);

                TextRange r =
                    defs[0].getSourceElementInfo().getIdentifyingRange();
                BufferChange change = new BufferChange(new ReplaceEdit(
                    r.getOffset(), r.getLength(), "g"));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                assertEquals(def, defs[0]);

                workingCopy.reconcile(false, null);

                assertFalse(def.exists());

                defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                assertEquals(workingCopy.getDef("g", 0), defs[0]);
            }
        });
    }

    public void test2() throws Exception
    {
        doWithWorkingCopy(new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IFooVar[] vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                IFooVar var1 = workingCopy.getVar("x");
                assertEquals(var1, vars[0]);
                IFooVar var2 = workingCopy.getVar("y");
                assertEquals(var2, vars[1]);

                ISourceElementInfo info = var2.getSourceElementInfo();
                TextRange r = info.getFullRange();
                String var2Text = info.getSnapshot().getContents().substring(
                    r.getOffset(), r.getEndOffset());

                BufferChange change = new BufferChange(new DeleteEdit(
                    r.getOffset(), r.getLength()));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                assertEquals(var2, vars[1]);

                workingCopy.reconcile(false, null);

                assertFalse(var2.exists());

                vars = workingCopy.getVars();
                assertEquals(1, vars.length);
                assertEquals(var1, vars[0]);

                info = var1.getSourceElementInfo();
                r = info.getFullRange();

                change = new BufferChange(new InsertEdit(r.getOffset(),
                    var2Text));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                vars = workingCopy.getVars();
                assertEquals(1, vars.length);

                workingCopy.reconcile(false, null);

                vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                assertEquals(var2, vars[0]);
                assertEquals(var1, vars[1]);
            }
        });
    }

    private void doWithWorkingCopy(IWorkspaceRunnable runnable)
        throws CoreException
    {
        workingCopy.becomeWorkingCopy(buffer, WorkingCopyInfoFactory.INSTANCE,
            null);
        try
        {
            runnable.run(null);
        }
        finally
        {
            workingCopy.discardWorkingCopy();
        }
    }
}
