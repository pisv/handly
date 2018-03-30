/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;

import junit.framework.TestCase;

/**
 * <code>Element</code> tests.
 */
public class ElementTest
    extends TestCase
{
    private boolean rootExists;
    private SimpleElement root;
    private SimpleElement a;
    private SimpleSourceConstruct b, c;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rootExists = true;
        root = new SimpleElement(null, "root", new SimpleModelManager())
        {
            @Override
            public void validateExistence_(IContext context)
                throws CoreException
            {
                if (!rootExists)
                    throw newDoesNotExistException_();
            }

            @Override
            public void buildStructure_(IContext context,
                IProgressMonitor monitor) throws CoreException
            {
                Body body = new Body();
                body.setChildren(new Element[] { a, b });
                Map<IElement, Object> newElements = context.get(NEW_ELEMENTS);
                newElements.put(this, body);
                SourceElementBody bBody = new SourceElementBody();
                newElements.put(b, bBody);
                // test temporary cache:
                assertSame(bBody, b.getBody_());
            }
        };
        a = root.getChild("A");
        b = new SimpleSourceConstruct(root, "B");
        c = new SimpleSourceConstruct(root, "C");
    }

    public void test01() throws Exception
    {
        assertNull(root.findBody_());
        assertNull(a.findBody_());
        assertNull(b.findBody_());
        Body rootBody = (Body)root.getBody_();
        assertSame(rootBody, root.peekAtBody_());
        assertEquals(Arrays.asList(a, b), Arrays.asList(
            rootBody.getChildren()));
        SourceElementBody bBody = (SourceElementBody)b.peekAtBody_();
        assertNotNull(bBody);
        assertNull(a.peekAtBody_());
        Body aBody = (Body)a.getBody_();
        assertNotNull(aBody);
        assertSame(aBody, a.peekAtBody_());
        root.close_();
        assertNull(root.findBody_());
        assertNull(a.findBody_());
        assertNull(b.findBody_());
    }

    public void test02() throws Exception
    {
        assertNull(root.findBody_());
        assertNull(a.findBody_());
        assertNull(b.findBody_());
        SourceElementBody bBody = (SourceElementBody)b.getBody_();
        assertNotNull(bBody);
        assertSame(bBody, b.peekAtBody_());
        Body rootBody = (Body)root.peekAtBody_();
        assertEquals(Arrays.asList(a, b), Arrays.asList(
            rootBody.getChildren()));
        assertNull(a.peekAtBody_());
        b.close_();
        assertSame(bBody, b.findBody_());
        assertSame(rootBody, root.findBody_());
        root.close_();
        assertNull(root.findBody_());
        assertNull(a.findBody_());
        assertNull(b.findBody_());
    }

    public void test03() throws Exception
    {
        assertNull(root.findBody_());
        assertNull(a.findBody_());
        assertNull(b.findBody_());
        Body aBody = (Body)a.getBody_();
        assertNotNull(aBody);
        assertSame(aBody, a.peekAtBody_());
        Body rootBody = (Body)root.peekAtBody_();
        assertEquals(Arrays.asList(a, b), Arrays.asList(
            rootBody.getChildren()));
        SourceElementBody bBody = (SourceElementBody)b.peekAtBody_();
        assertNotNull(bBody);
        a.close_();
        assertNull(a.findBody_());
        assertSame(rootBody, root.findBody_());
        assertSame(bBody, b.findBody_());
        root.close_();
        assertNull(root.findBody_());
        assertNull(a.findBody_());
        assertNull(b.findBody_());
    }

    public void test04()
    {
        assertTrue(root.exists_());
        assertNull(root.peekAtBody_());
    }

    public void test05()
    {
        assertTrue(a.exists_());
        assertNull(a.peekAtBody_());
        assertNull(root.peekAtBody_());
    }

    public void test06()
    {
        assertTrue(b.exists_());
        assertNotNull(b.peekAtBody_());
        assertNotNull(root.peekAtBody_());
        assertNull(a.peekAtBody_());
    }

    public void test07()
    {
        assertFalse(c.exists_());
        assertNull(c.peekAtBody_());
        assertNotNull(root.peekAtBody_());
        assertNotNull(b.peekAtBody_());
        assertNull(a.peekAtBody_());
    }

    public void test08()
    {
        rootExists = false;
        assertFalse(root.exists_());
        try
        {
            root.getBody_();
            fail();
        }
        catch (CoreException e)
        {
        }
    }

    public void test09()
    {
        rootExists = false;
        assertFalse(a.exists_());
        try
        {
            a.getBody_();
            fail();
        }
        catch (CoreException e)
        {
        }
    }

    public void test10()
    {
        rootExists = false;
        assertFalse(b.exists_());
        try
        {
            b.getBody_();
            fail();
        }
        catch (CoreException e)
        {
        }
    }

    public void test11()
    {
        rootExists = false;
        root.getElementManager_().put(root, Collections.singletonMap(root,
            new Body()));
        assertTrue(root.exists_());
    }

    public void test12() throws Exception
    {
        Body body = new Body();
        root.getElementManager_().put(root, Collections.singletonMap(root,
            body));
        // open an element that is already open:
        assertSame(body, root.open_(EMPTY_CONTEXT, null));
    }

    public void test13() throws Exception
    {
        assertEquals(Arrays.asList(a, b), Arrays.asList(root.getChildrenOfType_(
            Element.class, EMPTY_CONTEXT, null)));
        assertEquals(Arrays.asList(a), Arrays.asList(root.getChildrenOfType_(
            SimpleElement.class, EMPTY_CONTEXT, null)));
        assertEquals(Arrays.asList(b), Arrays.asList(root.getChildrenOfType_(
            SimpleSourceConstruct.class, EMPTY_CONTEXT, null)));
    }

    public void testBug530821()
    {
        class TestElement
            extends SimpleElement
        {
            TestElement(IElement parent)
            {
                super(parent, null, null);
            }

            @Override
            public boolean equals(Object obj)
            {
                return obj instanceof TestElement;
            }
        }
        TestElement e1 = new TestElement(null);
        TestElement e2 = new TestElement(e1);
        assertEquals(e1, e2);
        assertFalse(e1.equalsAndSameParentChain_(e2));
        assertTrue(e1.equalsAndSameParentChain_(new TestElement(null)));
    }
}
