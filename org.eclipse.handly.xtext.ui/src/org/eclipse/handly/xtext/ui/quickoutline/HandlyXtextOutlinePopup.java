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
package org.eclipse.handly.xtext.ui.quickoutline;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.model.ISourceFileFactory;
import org.eclipse.handly.ui.quickoutline.SourceElementOutlinePopup;

import com.google.inject.Inject;

/**
 * A partial implementation of <code>ISourceElement</code>-based outline popup
 * for Xtext editor.
 * <p>
 * Note that this class relies on a language-specific implementation of
 * <code>ISourceFileFactory</code> being available through injection.
 * </p>
 */
public abstract class HandlyXtextOutlinePopup
    extends SourceElementOutlinePopup
{
    @Inject
    private ISourceFileFactory sourceFileFactory;

    @Override
    protected Object computeInput()
    {
        IFile file = getHost().getFile();
        if (file == null)
            return null;
        return sourceFileFactory.getSourceFile(file);
    }
}
