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
package org.eclipse.handly.internal.examples.xtext.xtext.ui.callhierarchy;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.resource.IEObjectDescription;

final class Element
{
    private final IEObjectDescription description, grammarDescription;

    Element(IEObjectDescription description,
        IEObjectDescription grammarDescription)
    {
        if (description == null)
            throw new IllegalArgumentException();
        if (grammarDescription != null
            && grammarDescription.getEClass() != XtextPackage.Literals.GRAMMAR)
            throw new IllegalArgumentException();
        this.description = description;
        this.grammarDescription = grammarDescription;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Element other = (Element)obj;
        return getUri().equals(other.getUri());
    }

    @Override
    public int hashCode()
    {
        return getUri().hashCode();
    }

    @Override
    public String toString()
    {
        return description.getQualifiedName().toString();
    }

    URI getUri()
    {
        return description.getEObjectURI();
    }

    IEObjectDescription getDescription()
    {
        return description;
    }

    IEObjectDescription getGrammarDescription()
    {
        return grammarDescription;
    }
}
