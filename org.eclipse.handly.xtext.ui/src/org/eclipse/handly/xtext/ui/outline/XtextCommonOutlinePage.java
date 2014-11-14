/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.outline;

import org.eclipse.handly.ui.outline.CommonOutlinePage;
import org.eclipse.handly.ui.outline.ICommonOutlinePage;
import org.eclipse.handly.ui.outline.IOutlineContribution;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.xtext.ui.editor.IXtextEditorAware;
import org.eclipse.xtext.ui.editor.XtextEditor;

/**
 * An abstract subclass of {@link CommonOutlinePage}
 * that provides basic integration with Xtext editor.
 * Subclasses provide more functionality.
 * 
 * @see HandlyXtextOutlinePage
 */
public abstract class XtextCommonOutlinePage
    extends CommonOutlinePage
    implements IXtextEditorAware
{
    @Override
    public void setEditor(final XtextEditor editor)
    {
        init(editor);

        addOutlineContribution(new IOutlineContribution()
        {
            @Override
            public void init(ICommonOutlinePage outlinePage)
            {
                IActionBars actionBars = getSite().getActionBars();
                actionBars.setGlobalActionHandler(
                    ITextEditorActionConstants.UNDO,
                    editor.getAction(ITextEditorActionConstants.UNDO));
                actionBars.setGlobalActionHandler(
                    ITextEditorActionConstants.REDO,
                    editor.getAction(ITextEditorActionConstants.REDO));
            }

            @Override
            public void dispose()
            {
                editor.outlinePageClosed();
            }
        });
    }
}
