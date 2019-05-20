/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
package org.eclipse.handly.xtext.ui.callhierarchy;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.Snapshot;
import org.eclipse.handly.ui.callhierarchy.CallTextInfo;
import org.eclipse.handly.ui.callhierarchy.ICallLocation;
import org.eclipse.handly.util.TextRange;
import org.eclipse.xtext.findReferences.IReferenceFinder;
import org.eclipse.xtext.findReferences.IReferenceFinder.IResourceAccess;
import org.eclipse.xtext.findReferences.ReferenceAcceptor;
import org.eclipse.xtext.findReferences.TargetURIs;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Serves as a basis for the implementation of {@link XtextCallHierarchyNode}
 * by providing API and default implementation to find the call references
 * and obtain the call text info.
 * <p>
 * Usually, clients need to override methods {@link #isCallReference(IReferenceDescription)}
 * and/or {@link #getCallRegion(EObject, EReference, int)} in a language-specific
 * subclass.
 * </p>
 * <p>
 * An instance of this class is intended to be created by Guice. Also, clients
 * need to set a {@link #setResourceAccess(IReferenceFinder.IResourceAccess)
 * resource access} and an {@link #setIndexData(IResourceDescriptions)
 * index data} before using an instance of this class.
 * </p>
 */
@SuppressWarnings("restriction")
public class XtextCallHierarchyUtility
{
    protected IResourceAccess resourceAccess;
    protected IResourceDescriptions indexData;

    @Inject
    protected IReferenceFinder referenceFinder;

    @Inject
    protected Provider<TargetURIs> targetUrisProvider;

    @Inject
    protected IResourceServiceProvider.Registry resourceServiceProviderRegistry;

    @Inject
    protected ILocationInFileProvider locationInFileProvider;

    /**
     * Prevents direct instantiation. An instance of this class is intended
     * to be created by Guice.
     */
    protected XtextCallHierarchyUtility()
    {
    }

    /**
     * Sets the resource access.
     *
     * @param resourceAccess not <code>null</code>
     */
    public void setResourceAccess(IResourceAccess resourceAccess)
    {
        this.resourceAccess = resourceAccess;
    }

    /**
     * Sets the index data.
     *
     * @param indexData not <code>null</code>
     */
    public void setIndexData(IResourceDescriptions indexData)
    {
        this.indexData = indexData;
    }

    /**
     * Finds the caller references to the callee identified by the given URI.
     * <p>
     * This implementation calls {@link #isCallReference(IReferenceDescription)}
     * to filter the references reported by the {@link #referenceFinder}
     * before they are passed to the given acceptor.
     * </p>
     *
     * @param calleeUri not <code>null</code>
     * @param acceptor accepts the matches (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     */
    public void findCallerReferences(URI calleeUri,
        Consumer<IReferenceDescription> acceptor, IProgressMonitor monitor)
    {
        if (calleeUri == null)
            throw new IllegalArgumentException();
        if (acceptor == null)
            throw new IllegalArgumentException();

        TargetURIs targetUris = targetUrisProvider.get();
        targetUris.addURI(calleeUri);
        referenceFinder.findAllReferences(targetUris, resourceAccess, indexData,
            getReferenceAcceptor(acceptor), monitor);
    }

    /**
     * Finds the callee references from the caller identified by the given URI.
     * <p>
     * This implementation calls {@link #isCallReference(IReferenceDescription)}
     * to filter the references reported by the {@link #referenceFinder}
     * before they are passed to the given acceptor.
     * </p>
     *
     * @param callerUri not <code>null</code>
     * @param acceptor accepts the matches (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     */
    public void findCalleeReferences(URI callerUri,
        Consumer<IReferenceDescription> acceptor, IProgressMonitor monitor)
    {
        if (callerUri == null)
            throw new IllegalArgumentException();
        if (acceptor == null)
            throw new IllegalArgumentException();
        readOnly(callerUri, caller ->
        {
            referenceFinder.findAllReferences(caller, getReferenceAcceptor(
                acceptor), monitor);
            return null;
        });
    }

    /**
     * Returns the call text info based on the given call reference.
     * <p>
     * This implementation invokes {@link #getCallRegion(EObject, EReference, int)}
     * to obtain the call text region.
     * </p>
     *
     * @param callReference not <code>null</code>
     * @return the call text info (never <code>null</code>)
     */
    public CallTextInfo getCallTextInfo(IReferenceDescription callReference)
    {
        if (callReference == null)
            throw new IllegalArgumentException();
        CallTextInfo info = readOnly(callReference.getSourceEObjectUri(),
            sourceObject ->
            {
                String callText = ""; //$NON-NLS-1$
                TextRange callRange = null;
                int lineNumber = ICallLocation.UNKOWN_LINE_NUMBER;
                ISnapshot snapshot = null;

                ITextRegion callRegion = getCallRegion(sourceObject,
                    callReference.getEReference(),
                    callReference.getIndexInList());
                if (callRegion != null
                    && callRegion != ITextRegion.EMPTY_REGION)
                {
                    callRange = new TextRange(callRegion.getOffset(),
                        callRegion.getLength());

                    INode node = NodeModelUtils.getNode(sourceObject);
                    if (node != null)
                    {
                        String text = node.getRootNode().getText();

                        callText = text.substring(callRange.getOffset(),
                            callRange.getEndOffset());

                        lineNumber = NodeModelUtils.getLineAndColumn(node,
                            callRange.getOffset()).getLine() - 1;

                        snapshot = new Snapshot()
                        {
                            @Override
                            public String getContents()
                            {
                                return text;
                            }
                        };
                    }
                }

                return new CallTextInfo(callText, callRange, lineNumber,
                    snapshot);
            });
        if (info == null)
            info = new CallTextInfo("", null, //$NON-NLS-1$
                ICallLocation.UNKOWN_LINE_NUMBER, null);
        return info;
    }

    /**
     * Returns whether the given reference is a call reference.
     * <p>
     * Default implementation returns <code>true</code> iff the given reference
     * is not <code>null</code>. Clients usually need to override this method
     * for a specific language.
     * </p>
     *
     * @param reference may be <code>null</code>,
     *  in which case <code>false</code> is returned
     * @return <code>true</code> if the given reference is a call reference,
     *  and <code>false</code> otherwise
     */
    protected boolean isCallReference(IReferenceDescription reference)
    {
        return reference != null;
    }

    /**
     * Returns the text region for the given call reference.
     * <p>
     * Default implementation returns the <i>significant region</i> as reported
     * by the {@link #locationInFileProvider}. Clients may need to override
     * this method for a specific language.
     * </p>
     *
     * @param owner the owner of the reference (never <code>null</code>)
     * @param callReference never <code>null</code>
     * @param indexInList the index of the reference,
     *  or -1 if it is a single value reference
     * @return the call region (not <code>null</code>)
     */
    protected ITextRegion getCallRegion(EObject owner, EReference callReference,
        int indexInList)
    {
        return locationInFileProvider.getSignificantTextRegion(owner,
            callReference, indexInList);
    }

    /**
     * Executes the given unit of work while providing it with safe read access
     * to the {@link EObject} identified by the given URI. Note that this method
     * may return <code>null</code> if it was not able to start executing the
     * unit of work for some reason.
     * <p>
     * This implementation uses the {@link #resourceAccess} to provide
     * safe read access to the object.
     * </p>
     *
     * @param objectUri never <code>null</code>
     * @param work never <code>null</code>
     * @return the execution result (may be <code>null</code>)
     */
    protected <R> R readOnly(URI objectUri, IUnitOfWork<R, EObject> work)
    {
        return resourceAccess.readOnly(objectUri, resourceSet ->
        {
            EObject object = resourceSet.getEObject(objectUri, true);
            if (object == null)
                return null;
            return work.exec(object);
        });
    }

    private ReferenceAcceptor getReferenceAcceptor(
        Consumer<IReferenceDescription> acceptor)
    {
        return new ReferenceAcceptor(resourceServiceProviderRegistry,
            reference ->
            {
                if (isCallReference(reference))
                    acceptor.accept(reference);
            });
    }
}
