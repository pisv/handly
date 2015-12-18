/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel.ui.filters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.javamodel.IField;
import org.eclipse.handly.examples.javamodel.IMember;
import org.eclipse.handly.examples.javamodel.IType;
import org.eclipse.handly.internal.examples.javamodel.ui.Activator;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters out non-public members.
 */
public class NonPublicMemberFilter
    extends ViewerFilter
{
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
        if (element instanceof IMember)
        {
            try
            {
                IMember member = (IMember)element;
                int flags = member.getFlags();
                if (!Flags.isPublic(flags) &&
                    !isMemberInInterfaceOrAnnotation(member) &&
                    !isTopLevelType(member) && !isEnumConstant(member))
                    return false;
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
            }
        }
        return true;
    }

    private static boolean isMemberInInterfaceOrAnnotation(IMember member)
        throws CoreException
    {
        IType type = member.getDeclaringType();
        return type != null && type.isInterface();
    }

    private static boolean isTopLevelType(IMember member)
    {
        return member.getDeclaringType() == null;
    }

    private static boolean isEnumConstant(IMember member) throws CoreException
    {
        return member instanceof IField && ((IField)member).isEnumConstant();
    }
}
