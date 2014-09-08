/*******************************************************************************
 * Copyright (c) 2009, 2014 itemis AG (http://www.itemis.eu) and others. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - adaptation of DirtyStateEditorSupport code
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
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
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
     * @deprecated Use <code>org.eclipse.xtext.ui.refactoring.ui.SyncUtil</code>
     *  to wait for XtextEditor dirty state reconciler.
     */
    public static final Object FAMILY_DIRTY_STATE_RECONCILER = new Object();

    protected static final ISchedulingRule SCHEDULING_RULE =
        SchedulingRuleFactory.INSTANCE.newSequence();

    private volatile IDirtyStateEditorSupportClient currentClient; // unfortunately had to duplicate

    @Override
    public void initializeDirtyStateSupport(
        IDirtyStateEditorSupportClient client)
    {
        super.initializeDirtyStateSupport(client);
        IXtextDocument document = client.getDocument();
        if (document instanceof HandlyXtextDocument)
            ((HandlyXtextDocument)document).setDirtyStateEditorSupport(this);
        this.currentClient = client;
    }

    @Override
    public void removeDirtyStateSupport(IDirtyStateEditorSupportClient client)
    {
        super.removeDirtyStateSupport(client);
        IXtextDocument document = client.getDocument();
        if (document instanceof HandlyXtextDocument)
            ((HandlyXtextDocument)document).setDirtyStateEditorSupport(null);
        this.currentClient = null;
    }

    @Override
    protected UpdateEditorStateJob createUpdateEditorJob()
    {
        // default is sequential execution to ensure a minimum number of
        // spawned worker threads
        return new UpdateEditorStateJob(SCHEDULING_RULE);
    }

    protected final IDirtyStateEditorSupportClient getCurrentClient()
    {
        return currentClient;
    }

    /**
     * Initially copied from <code>DirtyStateEditorSupport.UpdateEditorStateJob</code>.
     * <p>
     * Although effectively a fork, it extends <code>DirtyStateEditorSupport.UpdateEditorStateJob</code>
     * in order to retain assignment compatibility.
     * </p>
     */
    protected class UpdateEditorStateJob
        extends DirtyStateEditorSupport.UpdateEditorStateJob
    {
        protected final AtomicInteger coarseGrainedChanges;
        protected final Queue<IResourceDescription.Delta> pendingChanges;

        public UpdateEditorStateJob(ISchedulingRule rule)
        {
            this(rule, Messages.DirtyStateEditorSupport_JobName);
        }

        public UpdateEditorStateJob(ISchedulingRule rule, String name)
        {
            super(rule, name);
            coarseGrainedChanges = new AtomicInteger();
            pendingChanges =
                new ConcurrentLinkedQueue<IResourceDescription.Delta>();
        }

        @Override
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
                if (myClient == null)
                    return Status.OK_STATUS;
                final HandlyXtextDocument document =
                    (HandlyXtextDocument)myClient.getDocument();
                if (document == null)
                    return Status.OK_STATUS;
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
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
                    return Status.CANCEL_STATUS;
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

        @Override
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
