/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.model.impl.support;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.ISourceConstructImplExtension;

/**
 * A helper class that is typically used when building the entire structure of a
 * source file. A typical usage pattern is <pre>
 *    Body parentBody = new ...;
 *
 *    // for each child element
 *    SourceConstruct element = new ...;
 *    helper.resolveDuplicates(element);
 *    Body body = new ...; // create and initialize the body
 *    newElements.put(element, body);
 *    helper.pushChild(parentBody, element);
 *
 *    parentBody.setChildren(helper.popChildren(parentBody).toArray(...));</pre>
 * <p>
 * Note that calling <code>parentBody.addChild(element)</code> for each child
 * element would generally be less efficient than using the pattern shown above.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 */
public class StructureHelper
{
    /*
     * Map from body to list of child elements.
     */
    private final Map<Object, List<IElement>> children = new HashMap<>();

    private final Map<ISourceConstructImplExtension, Integer> occurrenceCounts =
        new HashMap<>();

    /**
     * Remembers the given element as a child of the given parent body.
     * If the given element has already been remembered as a child of a body,
     * the behavior is unspecified.
     *
     * @param parentBody the body of the parent element (not <code>null</code>)
     * @param child the handle for the child element (not <code>null</code>)
     * @see #popChildren(Object)
     */
    public final void pushChild(Object parentBody, IElement child)
    {
        if (parentBody == null)
            throw new IllegalArgumentException();
        if (child == null)
            throw new IllegalArgumentException();
        List<IElement> childrenList = children.get(parentBody);
        if (childrenList == null)
            children.put(parentBody, childrenList = new ArrayList<>());
        childrenList.add(child);
    }

    /**
     * Retrieves and forgets the child elements previously remembered for the
     * given body. The returned children are in the order in which they were
     * remembered.
     *
     * @param body
     * @return a list of child elements for the given body, possibly empty
     *  (never <code>null</code>)
     * @see #pushChild(Object, IElement)
     */
    public final List<IElement> popChildren(Object body)
    {
        return ofNullable(children.remove(body)).orElse(emptyList());
    }

    /**
     * Resolves duplicate source constructs by incrementing their {@link
     * ISourceConstructImplExtension#getOccurrenceCount_() occurrence count}.
     *
     * @param element a source construct (not <code>null</code>)
     */
    public void resolveDuplicates(ISourceConstructImplExtension element)
    {
        if (element.getOccurrenceCount_() != 1)
            throw new IllegalArgumentException();
        Integer occurrenceCount = occurrenceCounts.get(element);
        if (occurrenceCount == null)
            occurrenceCounts.put(element, 1);
        else
        {
            int newOccurrenceCount = occurrenceCount + 1;
            occurrenceCounts.put(element, newOccurrenceCount);
            element.setOccurrenceCount_(newOccurrenceCount);
        }
    }
}
