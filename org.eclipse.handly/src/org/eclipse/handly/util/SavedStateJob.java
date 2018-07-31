/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
package org.eclipse.handly.util;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A workspace job to process notification of resource changes
 * that might have happened while a given plug-in was not active.
 *
 * @see ISavedState
 */
public class SavedStateJob
    extends WorkspaceJob
{
    private final String pluginId;
    private final IResourceChangeListener listener;

    /**
     * Constructs a saved state job.
     *
     * @param pluginId the unique identifier of the plug-in
     *  (not <code>null</code>)
     * @param listener the resource change listener to process a <code>POST_BUILD</code>
     *  event supplied by the saved state for the given plug-in (not <code>null</code>)
     */
    public SavedStateJob(String pluginId, IResourceChangeListener listener)
    {
        super("SavedState"); //$NON-NLS-1$
        if ((this.pluginId = pluginId) == null)
            throw new IllegalArgumentException();
        if ((this.listener = listener) == null)
            throw new IllegalArgumentException();
        setSystem(true);
        setPriority(SHORT); // process ASAP
        setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
    {
        ISavedState savedState =
            ResourcesPlugin.getWorkspace().addSaveParticipant(pluginId,
                new SaveParticipant());
        if (savedState != null)
            savedState.processResourceChangeEvents(listener);
        return Status.OK_STATUS;
    }

    private static class SaveParticipant
        implements ISaveParticipant
    {
        @Override
        public void doneSaving(ISaveContext context)
        {
        }

        @Override
        public void prepareToSave(ISaveContext context) throws CoreException
        {
        }

        @Override
        public void rollback(ISaveContext context)
        {
        }

        @Override
        public void saving(ISaveContext context) throws CoreException
        {
            if (context.getKind() == ISaveContext.FULL_SAVE)
            {
                context.needDelta();
            }
        }
    }
}
