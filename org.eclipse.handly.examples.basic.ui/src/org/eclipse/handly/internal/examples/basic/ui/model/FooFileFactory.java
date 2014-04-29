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
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.ISourceFileFactory;

/**
 * Implementation of <code>ISourceFileFactory</code> that must be bound 
 * in the Xtext UI module for the language. It's a required component of 
 * Handly/Xtext integration.
 * </p>
 */
public class FooFileFactory
    implements ISourceFileFactory
{
    @Override
    public ISourceFile getSourceFile(IFile file)
    {
        return FooModelCore.create(file);
    }
}
