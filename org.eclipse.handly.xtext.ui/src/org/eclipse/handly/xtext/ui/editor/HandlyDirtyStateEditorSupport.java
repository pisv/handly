/*******************************************************************************
 * Copyright (c) 2014 1C LLC. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse Xtext work)
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.editor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.handly.internal.xtext.ui.Activator;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.ResourceDescriptionChangeEvent;
import org.eclipse.xtext.ui.editor.DirtyStateEditorSupport;
import org.eclipse.xtext.ui.editor.Messages;
import org.eclipse.xtext.ui.editor.SchedulingRuleFactory;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.collect.Maps;

/**
 * Extends {@link DirtyStateEditorSupport} for our reconciling story.
 * Should be used together with {@link HandlyXtextDocument}.
 */
public class HandlyDirtyStateEditorSupport
    extends DirtyStateEditorSupport
{
    /**
     * Constant identifying the job family identifier 
     * for the dirty state reconciler job.
     */
    public static final Object FAMILY_DIRTY_STATE_RECONCILER = new Object();

    protected static final ISchedulingRule RECONCILER_RULE =
        SchedulingRuleFactory.INSTANCE.newSequence();

    private volatile IDirtyStateEditorSupportClient currentClient; // unfortunately had to duplicate
    private DirtyStateReconciler dirtyStateReconciler;

    @Override
    public void initializeDirtyStateSupport(
        IDirtyStateEditorSupportClient client)
    {
        super.initializeDirtyStateSupport(client);
        this.currentClient = client;
    }

    @Override
    public void removeDirtyStateSupport(IDirtyStateEditorSupportClient client)
    {
        super.removeDirtyStateSupport(client);
        this.currentClient = null;
    }

    @Override
    public void descriptionsChanged(final IResourceDescription.Event event)
    {
        if (!getDirtyResource().isInitialized())
            return;
        for (IResourceDescription.Delta delta : event.getDeltas())
        {
            if (delta.getOld() == getDirtyResource().getDescription()
                || delta.getNew() == getDirtyResource().getDescription())
                return;
        }
        scheduleReconciler(event);
    }

    protected final IDirtyStateEditorSupportClient getCurrentClient()
    {
        return currentClient;
    }

    protected DirtyStateReconciler createReconciler()
    {
        // default is sequential execution to ensure a minimum number of
        // spawned worker threads
        return new DirtyStateReconciler(RECONCILER_RULE);
    }

    protected final void scheduleReconciler(IResourceDescription.Event event)
    {
        synchronized (this)
        {
            if (dirtyStateReconciler == null)
            {
                dirtyStateReconciler = createReconciler();
            }
        }
        dirtyStateReconciler.scheduleFor(event);
    }

    /**
     * Initially copied from <code>DirtyStateEditorSupport.UpdateEditorStateJob</code>. 
     * Unfortunately had to fork, could not subclass.
     */
    protected class DirtyStateReconciler
        extends Job
    {
        protected final AtomicInteger coarseGrainedChanges;
        protected final Queue<IResourceDescription.Delta> pendingChanges;

        public DirtyStateReconciler(ISchedulingRule rule)
        {
            this(rule, Messages.DirtyStateEditorSupport_JobName);
        }

        public DirtyStateReconciler(ISchedulingRule rule, String name)
        {
            super(name);
            setRule(rule);
            coarseGrainedChanges = new AtomicInteger();
            pendingChanges =
                new ConcurrentLinkedQueue<IResourceDescription.Delta>();
        }

        public void scheduleFor(IResourceDescription.Event event)
        {
            cancel();
            if (event instanceof IResourceDescription.CoarseGrainedEvent)
                coarseGrainedChanges.incrementAndGet();
            else
                pendingChanges.addAll(event.getDeltas());
            schedule(getDelay());
        }

        @Override
        public boolean belongsTo(Object family)
        {
            return family == FAMILY_DIRTY_STATE_RECONCILER;
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor)
        {
            try
            {
                IDirtyStateEditorSupportClient myClient = currentClient;
                if (myClient == null || monitor.isCanceled())
                    return Status.OK_STATUS;
                final HandlyXtextDocument document =
                    (HandlyXtextDocument)myClient.getDocument();
                if (document == null)
                    return Status.OK_STATUS;
                int coarseGrainedChangesSeen = coarseGrainedChanges.get();
                final boolean[] isReparseRequired =
                    new boolean[] { coarseGrainedChangesSeen > 0 };
                final Pair<IResourceDescription.Event, Integer> event =
                    mergePendingDeltas();
                final Collection<Resource> affectedResources =
                    document.readOnly(new IUnitOfWork<Collection<Resource>, XtextResource>()
                    {
                        public Collection<Resource> exec(XtextResource resource)
                            throws Exception
                        {
                            if (resource == null
                                || resource.getResourceSet() == null)
                                return null;
                            Collection<Resource> affectedResources =
                                collectAffectedResources(resource,
                                    event.getFirst());
                            if (monitor.isCanceled()
                                || !affectedResources.isEmpty())
                            {
                                return affectedResources;
                            }
                            if (!isReparseRequired[0])
                            {
                                isReparseRequired[0] =
                                    isReparseRequired(resource,
                                        event.getFirst());
                            }
                            return affectedResources;
                        }
                    });
                if (monitor.isCanceled())
                    return Status.OK_STATUS;
                unloadAffectedResourcesAndReparseDocument(document,
                    affectedResources, isReparseRequired[0]);
                for (int i = 0; i < event.getSecond(); i++)
                {
                    pendingChanges.poll();
                }
                coarseGrainedChanges.addAndGet(-coarseGrainedChangesSeen);
            }
            catch (Throwable e)
            {
                Activator.log(Activator.createErrorStatus(
                    "Error updating editor state", e)); //$NON-NLS-1$
            }
            return Status.OK_STATUS;
        }

        protected Pair<IResourceDescription.Event, Integer> mergePendingDeltas()
        {
            Map<URI, IResourceDescription.Delta> uriToDelta =
                Maps.newLinkedHashMap();
            Iterator<IResourceDescription.Delta> iter =
                pendingChanges.iterator();
            int size = 0;
            while (iter.hasNext())
            {
                IResourceDescription.Delta delta = iter.next();
                URI uri = delta.getUri();
                IResourceDescription.Delta prev = uriToDelta.get(uri);
                if (prev == null)
                    uriToDelta.put(uri, delta);
                else if (prev.getOld() != delta.getNew())
                    uriToDelta.put(uri, createDelta(delta, prev));
                else
                    uriToDelta.remove(uri);
                size++;
            }
            IResourceDescription.Event event =
                new ResourceDescriptionChangeEvent(uriToDelta.values());
            return Tuples.create(event, size);
        }

        protected int getDelay()
        {
            return 500;
        }

        private void unloadAffectedResourcesAndReparseDocument(
            final HandlyXtextDocument document,
            final Collection<Resource> affectedResources,
            boolean reparseRequired)
        {
            if ((affectedResources == null || affectedResources.isEmpty())
                && !reparseRequired)
                return;
            document.internalModify(new IUnitOfWork.Void<XtextResource>()
            {
                @Override
                public void process(XtextResource resource) throws Exception
                {
                    if (resource == null || resource.getResourceSet() == null)
                        return;
                    ResourceSet resourceSet = resource.getResourceSet();
                    if (affectedResources != null)
                    {
                        for (Resource affectedResource : affectedResources)
                        {
                            affectedResource.unload();
                            resourceSet.getResources().remove(affectedResource);
                        }
                    }
                    document.reconcile(true);
                }
            });
        }
    }
}
