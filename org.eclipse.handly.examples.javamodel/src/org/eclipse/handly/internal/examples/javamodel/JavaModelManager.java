/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.context.Context;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.javamodel.IJavaElement;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.ElementChangeEvent;
import org.eclipse.handly.model.impl.ElementDelta;
import org.eclipse.handly.model.impl.ElementManager;
import org.eclipse.handly.model.impl.IModelManager;
import org.eclipse.handly.model.impl.INotificationManager;
import org.eclipse.handly.model.impl.NotificationManager;
import org.eclipse.handly.util.SavedStateJob;

/**
 * The manager for the Java model.
 *
 * @threadsafe This class is intended to be thread-safe
 */
public class JavaModelManager
    implements IModelManager, IResourceChangeListener
{
    /**
     * The sole instance of the manager.
     */
    public static final JavaModelManager INSTANCE = new JavaModelManager();

    private JavaModel javaModel;
    private ElementManager elementManager;
    private NotificationManager notificationManager;
    private DeltaProcessingState deltaState;
    private Context modelContext;
    private Map<IProject, PerProjectInfo> perProjectInfo =
        new HashMap<IProject, PerProjectInfo>(5); // NOTE: this object itself is used as a lock to synchronize creation/removal of per project info

    public void startup() throws Exception
    {
        try
        {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();

            javaModel = new JavaModel(workspace);
            elementManager = new ElementManager(new JavaModelCache());
            notificationManager = new NotificationManager();
            deltaState = new DeltaProcessingState();
            deltaState.initialize();

            modelContext = new Context();
            modelContext.bind(INotificationManager.class).to(
                notificationManager);
            modelContext.bind(ElementDelta.Factory.class).to(
                element -> new JavaElementDelta((IJavaElement)element));

            workspace.addResourceChangeListener(this,
                IResourceChangeEvent.POST_CHANGE);

            new SavedStateJob(Activator.PLUGIN_ID, this).schedule();
        }
        catch (Exception e)
        {
            shutdown();
            throw e;
        }
    }

    public void shutdown() throws Exception
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        modelContext = null;
        deltaState = null;
        notificationManager = null;
        elementManager = null;
        javaModel = null;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event)
    {
        DeltaProcessor deltaProcessor = new DeltaProcessor(deltaState);
        try
        {
            event.getDelta().accept(deltaProcessor);
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
        }
        finally
        {
            deltaState.reset();
        }
        IElementDelta delta = deltaProcessor.getDelta();
        if (!ElementDeltas.isEmpty(delta))
        {
            getNotificationManager().fireElementChangeEvent(
                new ElementChangeEvent(ElementChangeEvent.POST_CHANGE, delta));
        }
    }

    @Override
    public JavaModel getModel()
    {
        if (javaModel == null)
            throw new IllegalStateException();
        return javaModel;
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

    public IContext getModelContext()
    {
        if (modelContext == null)
            throw new IllegalStateException();
        return modelContext;
    }

    /**
     * Returns the per-project info for the given project.
     * If specified, create the info if the info doesn't exist.
     * <p>
     * Note that no check is done at this time on the existence
     * or the nature of this project.
     * </p>
     *
     * @param project the given project
     * @param create indicates whether to create the info if it doesn't exist
     * @return the per-project info for the given project, or <code>null</code>
     *  if the info doesn't exist and <code>create == false</code>
     */
    public PerProjectInfo getPerProjectInfo(IProject project, boolean create)
    {
        synchronized (perProjectInfo)
        {
            PerProjectInfo info = perProjectInfo.get(project);
            if (info == null && create)
            {
                info = new PerProjectInfo(project);
                perProjectInfo.put(project, info);
            }
            return info;
        }
    }

    /**
     * Returns the per-project info for the given project. If the info
     * doesn't exist, check for the project existence and create the info.
     *
     * @param project the given project
     * @return the per-project info for the given project (never <code>null</code>)
     * @throws CoreException if the Java project doesn't exist
     */
    public PerProjectInfo getPerProjectInfoCheckExistence(IProject project)
        throws CoreException
    {
        PerProjectInfo info = getPerProjectInfo(project, false);
        if (info == null)
        {
            new JavaProject(javaModel, project).validateExistence_(
                EMPTY_CONTEXT);
            info = getPerProjectInfo(project, true);
        }
        return info;
    }

    public void removePerProjectInfo(IProject project)
    {
        synchronized (perProjectInfo)
        {
            perProjectInfo.remove(project);
        }
    }

    private JavaModelManager()
    {
    }
}
