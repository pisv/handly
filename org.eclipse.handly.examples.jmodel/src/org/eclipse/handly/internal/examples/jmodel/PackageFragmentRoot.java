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
package org.eclipse.handly.internal.examples.jmodel;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IPackageFragment;
import org.eclipse.handly.examples.jmodel.IPackageFragmentRoot;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;

/**
 * Implementation of {@link IPackageFragmentRoot}.
 */
public class PackageFragmentRoot
    extends JavaElement
    implements IPackageFragmentRoot
{
    private static final IPackageFragment[] NO_CHILDREN =
        new IPackageFragment[0];

    private final IResource resource;

    /**
     * Constructs a package fragment root with the given parent element
     * and the given underlying resource.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param resource the resource underlying the element (not <code>null</code>)
     */
    public PackageFragmentRoot(JavaProject parent, IResource resource)
    {
        super(parent, resource.getName());
        if (parent == null)
            throw new IllegalArgumentException();
        this.resource = resource;
    }

    @Override
    public JavaProject getParent()
    {
        return (JavaProject)getParent_();
    }

    @Override
    public PackageFragment getPackageFragment(String packageName)
    {
        return new PackageFragment(this, packageName);
    }

    PackageFragment getPackageFragment(String[] simpleNames)
    {
        return new PackageFragment(this, simpleNames);
    }

    @Override
    public IPackageFragment[] getPackageFragments() throws CoreException
    {
        return (IPackageFragment[])getChildren();
    }

    @Override
    public Object[] getNonJavaResources() throws CoreException
    {
        return ((PackageFragmentRootBody)getBody_()).getNonJavaResources(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof PackageFragmentRoot))
            return false;
        PackageFragmentRoot other = (PackageFragmentRoot)o;
        return resource.equals(other.resource) && getParent().equals(
            other.getParent());
    }

    @Override
    public int hashCode()
    {
        return resource.hashCode();
    }

    @Override
    public IResource getResource_()
    {
        return resource;
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        validateOnClasspath();

        if (!resource.isAccessible())
            throw newDoesNotExistException_();
    }

    protected void validateOnClasspath() throws CoreException
    {
        IClasspathEntry[] rawClasspath = getParent().getRawClasspath();
        if (!ClasspathUtil.isSourceFolder(resource, rawClasspath))
            throw newDoesNotExistException_();
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        PackageFragmentRootBody body = new PackageFragmentRootBody();
        ArrayList<IPackageFragment> children = new ArrayList<>();
        if (resource.getType() == IResource.FOLDER
            || resource.getType() == IResource.PROJECT)
        {
            IContainer rootFolder = (IContainer)resource;
            computeFolderChildren(rootFolder, Path.EMPTY, children);
        }
        body.setChildren(children.toArray(NO_CHILDREN));
        context.get(NEW_ELEMENTS).put(this, body);
    }

    private void computeFolderChildren(IContainer folder, IPath packagePath,
        ArrayList<IPackageFragment> children) throws CoreException
    {
        children.add(new PackageFragment(this, packagePath.segments()));

        IResource[] members = folder.members();
        if (members.length > 0)
        {
            JavaProject javaProject = getParent();
            String sourceLevel = javaProject.getOption(JavaCore.COMPILER_SOURCE,
                true);
            String complianceLevel = javaProject.getOption(
                JavaCore.COMPILER_COMPLIANCE, true);
            for (IResource member : members)
            {
                if (member instanceof IFolder)
                {
                    String memberName = member.getName();
                    if (JavaConventions.validateIdentifier(memberName,
                        sourceLevel,
                        complianceLevel).getSeverity() != IStatus.ERROR)
                    {
                        computeFolderChildren((IFolder)member,
                            packagePath.append(memberName), children);
                    }
                }
            }
        }
    }

    @Override
    protected void getHandleMemento(StringBuilder sb)
    {
        getParent().getHandleMemento(sb);
        sb.append(getHandleMementoDelimiter());
        escapeMementoName(sb, resource.getProjectRelativePath().toString());
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        return JavaElement.JEM_PACKAGEFRAGMENTROOT;
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.PACKAGEFRAGMENT)
        {
            String packageName = ""; //$NON-NLS-1$
            token = null;
            if (memento.hasMoreTokens())
            {
                token = memento.nextToken();
                if (!MementoTokenizer.isDelimeter(token))
                {
                    packageName = token;
                    token = null;
                }
            }
            PackageFragment packageFragment = getPackageFragment(packageName);
            if (token == null)
                return packageFragment.getHandleFromMemento(memento);
            else
                return packageFragment.getHandleFromMemento(token, memento);
        }
        return null;
    }
}
