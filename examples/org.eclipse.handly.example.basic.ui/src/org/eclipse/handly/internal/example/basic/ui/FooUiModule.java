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
package org.eclipse.handly.internal.example.basic.ui;

import org.eclipse.handly.example.basic.ui.AbstractFooUiModule;
import org.eclipse.handly.internal.example.basic.ui.outline.FooOutlinePage;
import org.eclipse.handly.model.ISourceFileFactory;
import org.eclipse.handly.xtext.ui.editor.HandlyDirtyStateEditorSupport;
import org.eclipse.handly.xtext.ui.editor.HandlyXtextDocument;
import org.eclipse.handly.xtext.ui.editor.HandlyXtextEditorCallback;
import org.eclipse.handly.xtext.ui.editor.HandlyXtextReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.xtext.ui.editor.DirtyStateEditorSupport;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;
import org.eclipse.xtext.ui.editor.model.XtextDocument;

import com.google.inject.Binder;
import com.google.inject.name.Names;

/**
 * Use this class to register components to be used within the IDE.
 */
public class FooUiModule
    extends AbstractFooUiModule
{
    public FooUiModule(AbstractUIPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public Class<? extends IContentOutlinePage> bindIContentOutlinePage()
    {
        return FooOutlinePage.class;
    }

// the following bindings are required for Handly/Xtext integration:

    @Override
    public Class<? extends IReconciler> bindIReconciler()
    {
        return HandlyXtextReconciler.class;
    }

    public Class<? extends XtextDocument> bindXtextDocument()
    {
        return HandlyXtextDocument.class;
    }

    public Class<? extends DirtyStateEditorSupport> bindDirtyStateEditorSupport()
    {
        return HandlyDirtyStateEditorSupport.class;
    }

    public void configureXtextEditorCallback(Binder binder)
    {
        binder.bind(IXtextEditorCallback.class).annotatedWith(
            Names.named(HandlyXtextEditorCallback.class.getName())).to(
            HandlyXtextEditorCallback.class);
    }

    public Class<? extends ISourceFileFactory> bindISourceFileFactory()
    {
        return FooFileFactory.class;
    }
}
