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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyNode;
import org.eclipse.handly.ui.callhierarchy.CallLocation;
import org.eclipse.handly.ui.callhierarchy.ICallHierarchyNode;
import org.eclipse.handly.ui.callhierarchy.ICallLocation;
import org.eclipse.xtext.resource.IReferenceDescription;

/**
 * A partial implementation for {@link ICallHierarchyNode} based on Xtext.
 */
public abstract class XtextCallHierarchyNode
    extends CallHierarchyNode
{
    private static final XtextCallHierarchyNode[] EMPTY_ARRAY =
        new XtextCallHierarchyNode[0];

    /**
     * Creates a new Xtext call hierarchy node.
     *
     * @param parent the parent node, or <code>null</code> if this is a root node
     * @param element the underlying model element (not <code>null</code>)
     */
    protected XtextCallHierarchyNode(XtextCallHierarchyNode parent,
        Object element)
    {
        super(parent, element);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation computes the child nodes based on the call references
     * reported by {@link #findCallReferences(Consumer, IProgressMonitor)
     * findCallReferences}. To obtain a child node, it calls {@link
     * #createChildIfAbsent(Map, IReferenceDescription)}. To create a call location,
     * it invokes {@link #createCallLocation(Object, Object, IReferenceDescription)}.
     * </p>
     */
    @Override
    protected ICallHierarchyNode[] computeChildren(IProgressMonitor monitor)
    {
        Map<URI, XtextCallHierarchyNode> children = new LinkedHashMap<>();
        findCallReferences(callReference ->
        {
            XtextCallHierarchyNode child = createChildIfAbsent(children,
                callReference);
            if (child != null)
            {
                Object caller, callee;
                switch (getKind())
                {
                case CALLER:
                    caller = child.getElement();
                    callee = getElement();
                    break;
                case CALLEE:
                    caller = getElement();
                    callee = child.getElement();
                    break;
                default:
                    throw new AssertionError();
                }
                child.addCallLocation(createCallLocation(caller, callee,
                    callReference));
            }
        }, monitor);
        return children.values().toArray(EMPTY_ARRAY);
    }

    /**
     * Finds the call references for this node.
     * <p>
     * This implementation uses the {@link #getCallHierarchyUtility()
     * call hierarchy utility} to find the call references. Depending on
     * the {@link #getKind() kind} of this node, it will find either {@link
     * XtextCallHierarchyUtility#findCallerReferences(URI, Consumer, IProgressMonitor)
     * caller references} or {@link XtextCallHierarchyUtility#findCalleeReferences(
     * URI, Consumer, IProgressMonitor) callee references}.
     * </p>
     *
     * @param acceptor accepts the matches (never <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     */
    protected void findCallReferences(Consumer<IReferenceDescription> acceptor,
        IProgressMonitor monitor)
    {
        switch (getKind())
        {
        case CALLER:
            getCallHierarchyUtility().findCallerReferences(getUri(), acceptor,
                monitor);
            break;
        case CALLEE:
            getCallHierarchyUtility().findCalleeReferences(getUri(), acceptor,
                monitor);
            break;
        default:
            throw new AssertionError();
        }
    }

    /**
     * Creates and returns a call location based on the given call reference.
     * <p>
     * This implementation returns a new instance of {@link CallLocation}.
     * It uses the {@link #getCallHierarchyUtility() call hierarchy utility}
     * to obtain the {@link XtextCallHierarchyUtility#getCallTextInfo(IReferenceDescription)
     * call text info}.
     * </p>
     *
     * @param caller the caller element, or <code>null</code> if unknown
     * @param callee the callee element, or <code>null</code> if unknown
     * @param callReference never <code>null</code>
     * @return the created call location (not <code>null</code>)
     */
    protected ICallLocation createCallLocation(Object caller, Object callee,
        IReferenceDescription callReference)
    {
        return new CallLocation(caller, callee,
            getCallHierarchyUtility().getCallTextInfo(callReference));
    }

    /**
     * Returns the corresponding URI for this node.
     *
     * @return the corresponding URI (not <code>null</code>)
     */
    protected abstract URI getUri();

    /**
     * Returns a child node for the given call reference, creating it
     * if necessary. If the given map does not already contain a mapping for
     * the requested node, this method will attempt to create it and enter it
     * into the map. Note that this method must not add call locations to
     * the child node; they are added separately.
     *
     * @param children never <code>null</code>
     * @param callReference never <code>null</code>
     * @return the corresponding (existing or created) child node, or <code>null</code>
     *  if no child node can be created for the given call reference
     */
    protected abstract XtextCallHierarchyNode createChildIfAbsent(
        Map<URI, XtextCallHierarchyNode> children,
        IReferenceDescription callReference);

    /**
     * Returns the call hierarchy utility used by this node.
     *
     * @return the call hierarchy utility (not <code>null</code>)
     */
    protected abstract XtextCallHierarchyUtility getCallHierarchyUtility();
}
