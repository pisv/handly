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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

/**
 * <code>ElementChangeRecorder</code> tests.
 */
public class ElementChangeRecorderTest
    extends TestCase
{
    private SimpleSourceFile root;
    private SimpleSourceConstruct a, b, c;
    private SourceElementBody rootBody, aBody, bBody, cBody;
    private ElementChangeRecorder recorder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        root = new SimpleSourceFile(null, "root", null,
            new SimpleModelManager())
        {
            @Override
            public Object findBody_()
            {
                return rootBody;
            }

            @Override
            public Object open_(IContext context, IProgressMonitor monitor)
                throws CoreException
            {
                throw newDoesNotExistException_();
            }
        };
        a = new SimpleSourceConstruct(root, "A")
        {
            @Override
            public Object findBody_()
            {
                return aBody;
            }
        };
        b = new SimpleSourceConstruct(root, "B")
        {
            @Override
            public Object findBody_()
            {
                return bBody;
            }
        };
        c = new SimpleSourceConstruct(b, "C")
        {
            @Override
            public Object findBody_()
            {
                return cBody;
            }
        };
        rootBody = new SourceElementBody();
        aBody = new SourceElementBody();
        bBody = new SourceElementBody();
        cBody = new SourceElementBody();
        recorder = new ElementChangeRecorder();
    }

    public void test01()
    {
        assertFalse(recorder.isRecording());
        try
        {
            recorder.endRecording();
            fail();
        }
        catch (IllegalStateException e)
        {
        }
        recorder.beginRecording(root);
        assertTrue(recorder.isRecording());
        assertTrue(ElementDeltas.isNullOrEmpty(
            recorder.endRecording().getDelta()));
        assertFalse(recorder.isRecording());
    }

    public void test02()
    {
        rootBody = null;
        recorder.beginRecording(root, null, 0);
        rootBody = new SourceElementBody();
        assertEquals("root[+]: {}",
            recorder.endRecording().getDelta().toString());
    }

    public void test03()
    {
        recorder.beginRecording(root, null, 0);
        rootBody = null;
        assertEquals("root[-]: {}",
            recorder.endRecording().getDelta().toString());
    }

    public void test04()
    {
        recorder.beginRecording(root);
        rootBody.addChild(a);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[+]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test05()
    {
        rootBody.addChild(b);
        recorder.beginRecording(root);
        rootBody.addChild(a);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[+]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test06()
    {
        rootBody.addChild(b);
        recorder.beginRecording(root);
        rootBody.setChildren(new SimpleSourceConstruct[] { a, b });
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[+]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test07()
    {
        rootBody.addChild(a);
        recorder.beginRecording(root);
        rootBody.removeChild(a);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[-]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test08()
    {
        rootBody.setChildren(new SimpleSourceConstruct[] { a, b });
        recorder.beginRecording(root);
        rootBody.removeChild(a);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[-]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test09()
    {
        rootBody.setChildren(new SimpleSourceConstruct[] { b, a });
        recorder.beginRecording(root);
        rootBody.removeChild(a);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[-]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test10()
    {
        rootBody.setChildren(new SimpleSourceConstruct[] { a, b });
        recorder.beginRecording(root);
        rootBody.setChildren(new SimpleSourceConstruct[] { b, a });
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  B[*]: {REORDERED | FINE GRAINED}\n" +
            "  A[*]: {REORDERED | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test11()
    {
        rootBody.setFullRange(new TextRange(0, 0));
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.setFullRange(new TextRange(0, 1));
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test12()
    {
        rootBody.setFullRange(new TextRange(0, 1));
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.setFullRange(new TextRange(0, 1));
        assertTrue(ElementDeltas.isNullOrEmpty(
            recorder.endRecording().getDelta()));
    }

    public void test13()
    {
        rootBody.setFullRange(new TextRange(0, 1));
        Document document = new Document("a");
        rootBody.setSnapshot(new DocumentSnapshot(document));
        recorder.beginRecording(root);
        document.set("b");
        rootBody = new SourceElementBody();
        rootBody.setFullRange(new TextRange(0, 1));
        rootBody.setSnapshot(new DocumentSnapshot(document));
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test14()
    {
        rootBody.setFullRange(new TextRange(0, 1));
        Document document = new Document("a");
        rootBody.setSnapshot(new DocumentSnapshot(document));
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.setFullRange(new TextRange(0, 1));
        rootBody.setSnapshot(new DocumentSnapshot(document));
        assertTrue(ElementDeltas.isNullOrEmpty(
            recorder.endRecording().getDelta()));
    }

    public void test15()
    {
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.set(Property.get("p", String.class), "a");
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test16()
    {
        rootBody.set(Property.get("p", String.class), "a");
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test17()
    {
        Property<String> p = Property.get("p", String.class);
        rootBody.set(p, "a");
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.set(p, "b");
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test18()
    {
        Property<String[]> p = Property.get("p", String[].class);
        rootBody.set(p, new String[] { "a" });
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.set(p, new String[] { "a", "b" });
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test19()
    {
        rootBody.set(Property.get("p1", String.class), "a");
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.set(Property.get("p2", String.class), "a");
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test20()
    {
        Property<String> p = Property.get("p", String.class);
        rootBody.set(p, "a");
        recorder.beginRecording(root);
        rootBody = new SourceElementBody();
        rootBody.set(p, "a");
        assertTrue(ElementDeltas.isNullOrEmpty(
            recorder.endRecording().getDelta()));
    }

    public void test21()
    {
        recorder.beginRecording(root, null, 0);
        assertEquals("root[*]: {CONTENT}",
            recorder.endRecording().getDelta().toString());
    }

    public void test22()
    {
        rootBody = null;
        recorder.beginRecording(root, null, 0);
        rootBody = new SourceElementBody();
        assertEquals("root[+]: {}",
            recorder.endRecording().getDelta().toString());
    }

    public void test23()
    {
        recorder.beginRecording(root, null, 0);
        rootBody = null;
        assertEquals("root[-]: {}",
            recorder.endRecording().getDelta().toString());
    }

    public void test24()
    {
        rootBody.addChild(a);
        recorder.beginRecording(root, null, 0);
        rootBody.addChild(b);
        assertEquals("root[*]: {CONTENT}",
            recorder.endRecording().getDelta().toString());
    }

    public void test25()
    {
        rootBody.setChildren(new SimpleSourceConstruct[] { a, b });
        recorder.beginRecording(root, null, 0);
        rootBody.setChildren(new SimpleSourceConstruct[] { b, a });
        assertEquals("root[*]: {CONTENT}",
            recorder.endRecording().getDelta().toString());
    }

    public void test26()
    {
        recorder.beginRecording(root, null, 0);
        rootBody = new SourceElementBody();
        rootBody.set(Property.get("p", String.class), "a");
        assertEquals("root[*]: {CONTENT}",
            recorder.endRecording().getDelta().toString());
    }

    public void test27()
    {
        recorder.beginRecording(root, null, 1);
        assertTrue(ElementDeltas.isNullOrEmpty(
            recorder.endRecording().getDelta()));
    }

    public void test28()
    {
        rootBody.addChild(a);
        recorder.beginRecording(root, null, 1);
        rootBody.addChild(b);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[*]: {CONTENT}\n" +
            "  B[+]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test29()
    {
        rootBody.addChild(a);
        rootBody.addChild(b);
        recorder.beginRecording(root, null, 1);
        rootBody.removeChild(b);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[*]: {CONTENT}\n" +
            "  B[-]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test30()
    {
        recorder.beginRecording(root, null, 1);
        rootBody = new SourceElementBody();
        rootBody.set(Property.get("p", String.class), "a");
        assertEquals("root[*]: {CONTENT | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
    }

    public void test31()
    {
        rootBody.addChild(a);
        recorder.beginRecording(root, null, 1);
        aBody = new SourceElementBody();
        aBody.set(Property.get("p", String.class), "a");
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[*]: {CONTENT}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test32()
    {
        rootBody.addChild(a);
        rootBody.addChild(b);
        recorder.beginRecording(root, null, 1);
        bBody.addChild(c);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[*]: {CONTENT}\n" +
            "  B[*]: {CONTENT}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test33()
    {
        rootBody.addChild(a);
        rootBody.addChild(b);
        recorder.beginRecording(root, null, 2);
        bBody.addChild(c);
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  B[*]: {CHILDREN | FINE GRAINED}\n" +
            "    C[+]: {}", recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test34()
    {
        rootBody.setChildren(new SimpleSourceConstruct[] { a, b });
        recorder.beginRecording(root, null, 1);
        rootBody.setChildren(new SimpleSourceConstruct[] { b, a });
        aBody = new SourceElementBody();
        aBody.set(Property.get("p", String.class), "a");
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  B[*]: {CONTENT | REORDERED}\n" +
            "  A[*]: {CONTENT | REORDERED}",
            recorder.endRecording().getDelta().toString());
        //@formatter:on
    }

    public void test35()
    {
        rootBody.setChildren(new SimpleSourceConstruct[] { a, b });
        recorder.beginRecording(root, null, 2);
        rootBody.setChildren(new SimpleSourceConstruct[] { b, a });
        aBody = new SourceElementBody();
        aBody.set(Property.get("p", String.class), "a");
        //@formatter:off
        assertEquals("root[*]: {CHILDREN | FINE GRAINED}\n" +
            "  A[*]: {CONTENT | REORDERED | FINE GRAINED}\n" +
            "  B[*]: {REORDERED | FINE GRAINED}",
            recorder.endRecording().getDelta().toString());
        //@formatter:on
    }
}
