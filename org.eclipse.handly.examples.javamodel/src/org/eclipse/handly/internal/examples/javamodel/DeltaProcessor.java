/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.javamodel.ICompilationUnit;
import org.eclipse.handly.examples.javamodel.IJavaElement;
import org.eclipse.handly.examples.javamodel.IJavaElementDelta;
import org.eclipse.handly.examples.javamodel.IJavaProject;
import org.eclipse.handly.examples.javamodel.JavaModelCore;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.Element;
import org.eclipse.handly.model.impl.ElementDelta;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * This class is used by the <code>JavaModelManager</code> to convert
 * resource deltas into Java element deltas. It also does some processing
 * on the Java elements involved (e.g. closing them).
 */
class DeltaProcessor
    implements IResourceDeltaVisitor
{
    private final JavaElementDelta currentDelta;
    private final DeltaProcessingState state;

    DeltaProcessor(DeltaProcessingState state)
    {
        currentDelta = new JavaElementDelta(state.getJavaModel());
        this.state = state;
    }

    /**
     * Returns the Java element delta built from the resource delta.
     * Returns an empty delta if no Java elements were affected
     * by the resource change.
     *
     * @return Java element delta (never <code>null</code>)
     */
    JavaElementDelta getDelta()
    {
        return currentDelta;
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException
    {
        switch (delta.getResource().getType())
        {
        case IResource.ROOT:
            return processRoot(delta);

        case IResource.PROJECT:
            return processProject(delta);

        case IResource.FOLDER:
            return processFolder(delta);

        case IResource.FILE:
            return processFile(delta);

        default:
            return true;
        }
    }

    private boolean processRoot(IResourceDelta delta) throws CoreException
    {
        state.initOldClasspathInfo();
        state.initOldJavaProjectNames();

        if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
            markersChanged(state.getJavaModel(), delta.getMarkerDeltas());

        return true;
    }

    private boolean processProject(IResourceDelta delta) throws CoreException
    {
        switch (delta.getKind())
        {
        case IResourceDelta.ADDED:
            return processAddedProject(delta);

        case IResourceDelta.REMOVED:
            return processRemovedProject(delta);

        case IResourceDelta.CHANGED:
            return processChangedProject(delta);

        default:
            return true;
        }
    }

    private boolean processAddedProject(IResourceDelta delta)
        throws CoreException
    {
        IProject project = (IProject)delta.getResource();
        if (project.hasNature(IJavaProject.NATURE_ID))
        {
            JavaProject javaProject = (JavaProject)JavaModelCore.create(
                project);
            addToModel(javaProject);
            translateAddedDelta(delta, javaProject);
            state.classpathChanged(javaProject, false);
        }
        else
        {
            addResourceDelta(state.getJavaModel(), delta);
        }
        return false;
    }

    private boolean processRemovedProject(IResourceDelta delta)
        throws CoreException
    {
        IProject project = (IProject)delta.getResource();
        if (wasJavaProject(project))
        {
            JavaProject javaProject = (JavaProject)JavaModelCore.create(
                project);
            removeFromModel(javaProject);
            translateRemovedDelta(delta, javaProject);
            state.classpathChanged(javaProject, true);
        }
        else
        {
            addResourceDelta(state.getJavaModel(), delta);
        }
        return false;
    }

    private boolean processChangedProject(IResourceDelta delta)
        throws CoreException
    {
        IProject project = (IProject)delta.getResource();
        JavaProject javaProject = (JavaProject)JavaModelCore.create(project);

        if ((delta.getFlags() & IResourceDelta.OPEN) != 0)
        {
            if (project.isOpen())
            {
                if (project.hasNature(IJavaProject.NATURE_ID))
                {
                    addToModel(javaProject);
                    currentDelta.hInsertChanged(javaProject,
                        IJavaElementDelta.F_OPEN);
                    state.classpathChanged(javaProject, false);
                }
            }
            else
            {
                if (wasJavaProject(project))
                {
                    removeFromModel(javaProject);
                    currentDelta.hInsertChanged(javaProject,
                        IJavaElementDelta.F_OPEN);
                    state.classpathChanged(javaProject, true);
                }
            }
            addResourceDelta(state.getJavaModel(), delta);
            return false;
        }

        boolean isJavaProject = project.hasNature(IJavaProject.NATURE_ID);
        if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0)
        {
            boolean wasJavaProject = wasJavaProject(project);
            if (wasJavaProject != isJavaProject)
            {
                // Java nature has been added or removed
                if (isJavaProject)
                {
                    addToModel(javaProject);
                    currentDelta.hInsertAdded(javaProject);
                    state.classpathChanged(javaProject, false);
                }
                else
                {
                    removeFromModel(javaProject);
                    currentDelta.hInsertRemoved(javaProject);
                    state.classpathChanged(javaProject, true);
                }
                addResourceDelta(state.getJavaModel(), delta);
                return false; // when Java nature is added/removed don't process children
            }
            else
            {
                if (isJavaProject)
                {
                    currentDelta.hInsertChanged(javaProject,
                        IJavaElementDelta.F_DESCRIPTION);
                }
            }
        }

        if (isJavaProject)
        {
            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
                markersChanged(javaProject, delta.getMarkerDeltas());

            if ((delta.getFlags() & IResourceDelta.SYNC) != 0)
                currentDelta.hInsertChanged(javaProject,
                    IJavaElementDelta.F_SYNC);

            Body parentBody = findBody(javaProject.getParent());
            IElement[] children = parentBody.getChildren();
            if (!Arrays.asList(children).contains(javaProject))
                addToModel(javaProject); // in case the project was removed then added then changed

            checkClasspathChange(delta);

            return true;
        }
        else
        {
            addResourceDelta(state.getJavaModel(), delta);
            return false;
        }
    }

    private void checkClasspathChange(IResourceDelta projectDelta)
    {
        IResourceDelta delta = projectDelta.findMember(new Path(".classpath")); //$NON-NLS-1$
        if (delta == null || delta.getResource().getType() != IResource.FILE)
            return;

        JavaProject javaProject = (JavaProject)JavaModelCore.create(
            delta.getResource().getProject());
        switch (delta.getKind())
        {
        case IResourceDelta.CHANGED:
            int flags = delta.getFlags();
            if ((flags & IResourceDelta.CONTENT) == 0)
                break;
            //$FALL-THROUGH$
        case IResourceDelta.ADDED:
        case IResourceDelta.REMOVED:
            if (state.classpathChanged(javaProject, false))
            {
                currentDelta.hInsertChanged(javaProject,
                    IJavaElementDelta.F_CLASSPATH_CHANGED);
                close(javaProject);
            }
        }
    }

    private boolean processFolder(IResourceDelta delta) throws CoreException
    {
        switch (delta.getKind())
        {
        case IResourceDelta.ADDED:
            return processAddedFolder(delta);

        case IResourceDelta.REMOVED:
            return processRemovedFolder(delta);

        case IResourceDelta.CHANGED:
            return processChangedFolder(delta);

        default:
            return false;
        }
    }

    private boolean processAddedFolder(IResourceDelta delta)
    {
        IFolder folder = (IFolder)delta.getResource();
        IJavaElement element = state.createElement(folder, false);
        if (element != null)
        {
            addToModel(element);
            translateAddedDelta(delta, element);
            return true;
        }
        else
        {
            addResourceDelta(state.createElement(
                delta.getResource().getParent(), false), delta);
            return false;
        }
    }

    private boolean processRemovedFolder(IResourceDelta delta)
    {
        IFolder folder = (IFolder)delta.getResource();
        IJavaElement element = state.createElement(folder, true);
        if (element != null)
        {
            removeFromModel(element);
            translateRemovedDelta(delta, element);
            return true;
        }
        else
        {
            addResourceDelta(state.createElement(
                delta.getResource().getParent(), false), delta);
            return false;
        }
    }

    private boolean processChangedFolder(IResourceDelta delta)
    {
        IFolder folder = (IFolder)delta.getResource();
        IJavaElement element = state.createElement(folder, false);
        if (element != null)
        {
            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
                markersChanged(element, delta.getMarkerDeltas());

            if ((delta.getFlags() & IResourceDelta.SYNC) != 0)
                currentDelta.hInsertChanged(element, IJavaElementDelta.F_SYNC);

            return true;
        }
        else
        {
            addResourceDelta(state.createElement(
                delta.getResource().getParent(), false), delta);
            return false;
        }
    }

    private boolean processFile(IResourceDelta delta)
    {
        switch (delta.getKind())
        {
        case IResourceDelta.ADDED:
            return processAddedFile(delta);

        case IResourceDelta.REMOVED:
            return processRemovedFile(delta);

        case IResourceDelta.CHANGED:
            return processChangedFile(delta);

        default:
            return false;
        }
    }

    private boolean processAddedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IJavaElement element = state.createElement(file, false);
        if (element != null)
        {
            addToModel(element);
            translateAddedDelta(delta, element);
        }
        else
        {
            addResourceDelta(state.createElement(
                delta.getResource().getParent(), false), delta);
        }
        return false;
    }

    private boolean processRemovedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IJavaElement element = state.createElement(file, true);
        if (element != null)
        {
            removeFromModel(element);
            translateRemovedDelta(delta, element);
        }
        else
        {
            addResourceDelta(state.createElement(
                delta.getResource().getParent(), false), delta);
        }
        return false;
    }

    private boolean processChangedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IJavaElement element = state.createElement(file, false);
        if (element != null)
        {
            if ((delta.getFlags() & ~(IResourceDelta.MARKERS
                | IResourceDelta.SYNC)) != 0)
                contentChanged(element);

            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
                markersChanged(element, delta.getMarkerDeltas());

            if ((delta.getFlags() & IResourceDelta.SYNC) != 0)
                currentDelta.hInsertChanged(element, IJavaElementDelta.F_SYNC);
        }
        else
        {
            addResourceDelta(state.createElement(
                delta.getResource().getParent(), false), delta);
        }
        return false;
    }

    private boolean wasJavaProject(IProject project)
    {
        return state.getOldJavaProjectNames().contains(project.getName());
    }

    private void addToModel(IJavaElement element)
    {
        Body parentBody = findBody(element.getParent());
        if (parentBody != null)
        {
            // Insert package fragment roots in the same order as in classpath
            if (element instanceof PackageFragmentRoot)
            {
                addPackageFragmentRoot(parentBody,
                    (PackageFragmentRoot)element);
            }
            else
                parentBody.addChild(element);
        }
        close(element);
    }

    private void addPackageFragmentRoot(Body parentBody,
        PackageFragmentRoot root)
    {
        IElement[] roots = parentBody.getChildren();
        if (roots.length == 0)
        {
            parentBody.addChild(root);
            return;
        }

        IClasspathEntry[] classpath;
        try
        {
            classpath = root.getParent().getRawClasspath();
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
            parentBody.addChild(root);
            return;
        }

        IPath rootPath = root.getPath();
        int indexToInsert = -1;
        int lastComparedIndex = -1;
        int i = 0, j = 0;
        while (i < roots.length && j < classpath.length)
        {
            IPath entryPath = classpath[j].getPath();
            if (lastComparedIndex != j && rootPath.equals(entryPath))
            {
                indexToInsert = i;
                break;
            }
            lastComparedIndex = j;

            if (Elements.getPath(roots[i]).equals(entryPath))
                i++;
            else
                j++;
        }

        for (; i < roots.length; i++)
        {
            // If the new root is already among the children, no need to proceed further
            if (roots[i].equals(root))
                return;

            if (!Elements.getPath(roots[i]).equals(rootPath))
                break;
        }

        if (indexToInsert < 0)
            parentBody.addChild(root);
        else
        {
            int newSize = roots.length + 1;
            IElement[] newChildren = new IElement[newSize];
            if (indexToInsert > 0)
                System.arraycopy(roots, 0, newChildren, 0, indexToInsert);
            newChildren[indexToInsert] = root;
            System.arraycopy(roots, indexToInsert, newChildren, indexToInsert
                + 1, newSize - indexToInsert - 1);
            parentBody.setChildren(newChildren);
        }
    }

    private void removeFromModel(IJavaElement element)
    {
        Body parentBody = findBody(element.getParent());
        if (parentBody != null)
            parentBody.removeChild(element);
        close(element);
        if (element instanceof IJavaProject)
            JavaModelManager.INSTANCE.removePerProjectInfo(
                ((IJavaProject)element).getProject());
    }

    private void translateAddedDelta(IResourceDelta delta, IJavaElement element)
    {
        if ((delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) // regular addition
        {
            currentDelta.hInsertAdded(element);
        }
        else
        {
            IJavaElement movedFromElement = state.createElement(getResource(
                delta.getMovedFromPath(), delta.getResource().getType()), true);
            if (movedFromElement == null)
                currentDelta.hInsertAdded(element);
            else
                currentDelta.hInsertMovedTo(element, movedFromElement);
        }
    }

    private void translateRemovedDelta(IResourceDelta delta,
        IJavaElement element)
    {
        if ((delta.getFlags() & IResourceDelta.MOVED_TO) == 0) // regular removal
        {
            currentDelta.hInsertRemoved(element);
        }
        else
        {
            IJavaElement movedToElement = state.createElement(getResource(
                delta.getMovedToPath(), delta.getResource().getType()), false);
            if (movedToElement == null)
                currentDelta.hInsertRemoved(element);
            else
                currentDelta.hInsertMovedFrom(element, movedToElement);
        }
    }

    private void contentChanged(IJavaElement element)
    {
        if (element instanceof ICompilationUnit
            && ((ICompilationUnit)element).isWorkingCopy())
        {
            currentDelta.hInsertChanged(element, IJavaElementDelta.F_CONTENT
                | IJavaElementDelta.F_UNDERLYING_RESOURCE);
            return;
        }

        close(element);
        currentDelta.hInsertChanged(element, IJavaElementDelta.F_CONTENT);
    }

    private void markersChanged(IJavaElement element,
        IMarkerDelta[] markerDeltas)
    {
        ElementDelta delta = currentDelta.hDeltaFor(element);
        if (delta == null)
        {
            currentDelta.hInsertChanged(element, 0);
            delta = currentDelta.hDeltaFor(element);
        }
        delta.hSetMarkerDeltas(markerDeltas);
    }

    private void addResourceDelta(IJavaElement element,
        IResourceDelta resourceDelta)
    {
        if (element == null)
            return;

        // reset non-Java resources if element was open
        Body body = findBody(element);
        if (body != null)
        {
            if (body instanceof JavaModelBody)
                ((JavaModelBody)body).setNonJavaProjects(null);
            else if (body instanceof JavaProjectBody)
                ((JavaProjectBody)body).setNonJavaResources(null);
            else if (body instanceof PackageFragmentRootBody)
                ((PackageFragmentRootBody)body).setNonJavaResources(null);
            else if (body instanceof PackageFragmentBody)
                ((PackageFragmentBody)body).setNonJavaResources(null);
            else
                throw new AssertionError();
        }

        ElementDelta delta = currentDelta.hDeltaFor(element);
        if (delta == null)
        {
            currentDelta.hInsertChanged(element, 0);
            delta = currentDelta.hDeltaFor(element);
        }
        delta.hAddResourceDelta(resourceDelta);
    }

    private static Body findBody(IJavaElement element)
    {
        return ((Element)element).hFindBody();
    }

    private static void close(IJavaElement element)
    {
        ((Element)element).hClose();
    }

    private static IResource getResource(IPath fullPath, int resourceType)
    {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        switch (resourceType)
        {
        case IResource.ROOT:
            return root;

        case IResource.PROJECT:
            return root.getProject(fullPath.lastSegment());

        case IResource.FOLDER:
            return root.getFolder(fullPath);

        case IResource.FILE:
            return root.getFile(fullPath);

        default:
            return null;
        }
    }
}
