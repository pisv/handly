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
package org.eclipse.handly.ui.text.reconciler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

/**
 * Composes multiple reconciling strategies into one.
 */
public class CompositeReconcilingStrategy
    implements IReconcilingStrategy, IReconcilingStrategyExtension
{
    private IReconcilingStrategy[] strategies;

    /**
     * Creates a composition of the given reconciling strategies.
     * The strategies will be applied in the given order.
     *
     * @param strategies the reconciling strategies to compose
     */
    public CompositeReconcilingStrategy(IReconcilingStrategy... strategies)
    {
        for (IReconcilingStrategy strategy : strategies)
        {
            if (strategy == null)
                throw new IllegalArgumentException();
        }
        this.strategies = strategies;
    }

    @Override
    public void setDocument(IDocument document)
    {
        for (IReconcilingStrategy strategy : strategies)
        {
            strategy.setDocument(document);
        }
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
    {
        for (IReconcilingStrategy strategy : strategies)
        {
            strategy.reconcile(dirtyRegion, subRegion);
        }
    }

    @Override
    public void reconcile(IRegion partition)
    {
        for (IReconcilingStrategy strategy : strategies)
        {
            strategy.reconcile(partition);
        }
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        for (IReconcilingStrategy strategy : strategies)
        {
            if (strategy instanceof IReconcilingStrategyExtension)
            {
                IReconcilingStrategyExtension extension =
                    (IReconcilingStrategyExtension)strategy;
                extension.setProgressMonitor(monitor);
            }
        }
    }

    @Override
    public void initialReconcile()
    {
        for (IReconcilingStrategy strategy : strategies)
        {
            if (strategy instanceof IReconcilingStrategyExtension)
            {
                IReconcilingStrategyExtension extension =
                    (IReconcilingStrategyExtension)strategy;
                extension.initialReconcile();
            }
        }
    }
}
