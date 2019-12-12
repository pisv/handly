/*******************************************************************************
 * Copyright (c) 2016, 2019 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.basic.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.internal.examples.basic.ui.Activator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.xtext.ui.editor.XtextEditor;

import com.google.inject.Inject;

/**
 * A simple multi-page editor with a single page containing a nested XtextEditor.
 * Not used by default, but can be quickly hooked up in plugin.xml in place of
 * the default XtextEditor in a setting where having a multi-page editor would
 * be desirable (e.g. for testing purposes).
 */
public class FooEditor
    extends MultiPageEditorPart
{
    @Inject
    private XtextEditor nestedEditor;

    @Override
    public void doSave(IProgressMonitor monitor)
    {
        nestedEditor.doSave(monitor);
    }

    @Override
    public void doSaveAs()
    {
        nestedEditor.doSaveAs();
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return nestedEditor.isSaveAsAllowed();
    }

    @Override
    protected void createPages()
    {
        try
        {
            int pageIndex = addPage(nestedEditor, getEditorInput());
            setPageText(pageIndex, "Source");
        }
        catch (PartInitException e)
        {
            Activator.logError(e);
        }
    }

    @Override
    protected void handlePropertyChange(int propertyId)
    {
        super.handlePropertyChange(propertyId);
        if (propertyId == IEditorPart.PROP_INPUT)
            setInputWithNotify(nestedEditor.getEditorInput());
    }

    @Override
    protected void setInput(IEditorInput input)
    {
        super.setInput(input);
        setPartName(input.getName());
    }

    @Override
    protected void setInputWithNotify(IEditorInput input)
    {
        super.setInputWithNotify(input);
        setPartName(input.getName());
    }
}
