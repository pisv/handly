/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.model.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.SimpleElement;

import junit.framework.TestCase;

/**
 * <code>DefaultContentAdapter</code> tests.
 */
public class DefaultContentAdapterTest
    extends TestCase
{
    private IContentAdapter adapter = DefaultContentAdapter.INSTANCE;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        Platform.getAdapterManager().registerAdapters(AdapterFactory.INSTANCE,
            IntWrapper.class);
        Platform.getAdapterManager().registerAdapters(AdapterFactory.INSTANCE,
            TestElement.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        Platform.getAdapterManager().unregisterAdapters(
            AdapterFactory.INSTANCE);
        super.tearDown();
    }

    public void test1()
    {
        IntWrapper o = new IntWrapper(333);
        assertEquals(o, adapter.getCorrespondingElement(adapter.adapt(o)));
    }

    public void test2()
    {
        TestElement e = new TestElement(777);
        assertEquals(e, adapter.adapt(adapter.getCorrespondingElement(e)));
    }

    private static class AdapterFactory
        implements IAdapterFactory, ICorrespondingElementProvider
    {
        static final AdapterFactory INSTANCE = new AdapterFactory();

        private static final Class<?>[] ADAPTER_LIST = new Class<?>[] {
            IElement.class, ICorrespondingElementProvider.class };

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getAdapter(Object adaptableObject, Class<T> adapterType)
        {
            if (adapterType == ICorrespondingElementProvider.class)
                return (T)this;
            if (adaptableObject instanceof IntWrapper
                && adapterType == IElement.class)
                return (T)new TestElement(((IntWrapper)adaptableObject).i);
            return null;
        }

        @Override
        public Class<?>[] getAdapterList()
        {
            return ADAPTER_LIST;
        }

        @Override
        public IAdaptable getCorrespondingElement(IElement element)
        {
            if (element instanceof TestElement)
            {
                return new IntWrapper(Integer.valueOf(
                    ((TestElement)element).getName_()));
            }
            return null;
        }
    }

    private static class IntWrapper
        extends PlatformObject
    {
        final int i;

        IntWrapper(int i)
        {
            this.i = i;
        }

        @Override
        public int hashCode()
        {
            return i;
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
            return i == ((IntWrapper)obj).i;
        }

        @Override
        public String toString()
        {
            return "IntWrapper: " + i;
        }
    }

    private static class TestElement
        extends SimpleElement
    {
        TestElement(int i)
        {
            super(null, Integer.toString(i), null);
        }
    }
}
