/*******************************************************************************
 * Copyright (c) 2018, 2019 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.lsp;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.ApiLevel;
import org.eclipse.handly.context.Context;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.lsp.ILanguageElement;
import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.IModel;
import org.eclipse.handly.model.impl.IModelImpl;
import org.eclipse.handly.model.impl.support.ElementChangeEvent;
import org.eclipse.handly.model.impl.support.ElementDelta;
import org.eclipse.handly.model.impl.support.ElementManager;
import org.eclipse.handly.model.impl.support.IModelManager;
import org.eclipse.handly.model.impl.support.INotificationManager;
import org.eclipse.handly.model.impl.support.NotificationManager;
import org.eclipse.handly.util.SavedStateJob;

/**
 * The manager for the language model.
 */
public final class ModelManager
    implements IModelManager, IResourceChangeListener
{
    /**
     * The sole instance of the manager.
     */
    public static final ModelManager INSTANCE = new ModelManager();

    private IModel model;
    private ElementManager elementManager;
    private NotificationManager notificationManager;
    private ServerManager serverManager;
    private Context modelContext;

    void startup() throws Exception
    {
        try
        {
            model = new IModelImpl()
            {
                @Override
                public IContext getModelContext_()
                {
                    return getModelContext();
                }

                @Override
                public int getModelApiLevel_()
                {
                    return ApiLevel.CURRENT;
                }
            };
            elementManager = new ElementManager(new ModelCache());
            notificationManager = new NotificationManager();
            serverManager = new ServerManager();
            serverManager.startup();

            modelContext = new Context();
            modelContext.bind(INotificationManager.class).to(
                notificationManager);
            modelContext.bind(ElementDelta.Factory.class).to(
                element -> new LanguageElementDelta((ILanguageElement)element));

            ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
                IResourceChangeEvent.POST_CHANGE);

            new SavedStateJob(Activator.PLUGIN_ID, this).schedule();
        }
        catch (Exception e)
        {
            shutdown();
            throw e;
        }
    }

    void shutdown() throws Exception
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        modelContext = null;
        if (serverManager != null)
        {
            serverManager.shutdown();
            serverManager = null;
        }
        notificationManager = null;
        elementManager = null;
        model = null;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event)
    {
        DeltaProcessor deltaProcessor = new DeltaProcessor();
        try
        {
            event.getDelta().accept(deltaProcessor);
        }
        catch (CoreException e)
        {
            Activator.logError(e);
        }
        IElementDelta[] deltas = deltaProcessor.getDeltas();
        if (deltas.length > 0)
        {
            getNotificationManager().fireElementChangeEvent(
                new ElementChangeEvent(ElementChangeEvent.POST_CHANGE, deltas));
        }
    }

    @Override
    public IModel getModel()
    {
        if (model == null)
            throw new IllegalStateException();
        return model;
    }

    @Override
    public ElementManager getElementManager()
    {
        if (elementManager == null)
            throw new IllegalStateException();
        return elementManager;
    }

    public NotificationManager getNotificationManager()
    {
        if (notificationManager == null)
            throw new IllegalStateException();
        return notificationManager;
    }

    ServerManager getServerManager()
    {
        if (serverManager == null)
            throw new IllegalStateException();
        return serverManager;
    }

    IContext getModelContext()
    {
        if (modelContext == null)
            throw new IllegalStateException();
        return modelContext;
    }

    /**
     * Returns the source file element corresponding to the given file,
     * or <code>null</code> if unable to associate the given file with a
     * source file element.
     *
     * @param file the given file (may be <code>null</code>)
     * @return the source file element corresponding to the given file,
     *  or <code>null</code> if unable to associate the given file with a
     *  source file element
     */
    public ILanguageSourceFile createSourceFileFrom(IFile file)
    {
        if (file == null)
            return null;
        if (getServerManager().canUseLanguageServer(file.getFileExtension()))
            return new LanguageSourceFile(null, file);
        return null;
    }

    private ModelManager()
    {
    }
}
