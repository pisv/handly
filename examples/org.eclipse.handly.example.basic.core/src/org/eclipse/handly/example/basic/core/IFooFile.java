/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.example.basic.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.ISourceFile;

/**
 * Represents a Foo source file.
 */
public interface IFooFile
    extends IFooElement, ISourceFile
{
    /**
     * Foo file extension.
     */
    String EXT = "foo";
    
    /**
     * Returns the variable with the given name declared in this Foo file.
     * This is a handle-only method. The variable may or may not exist.
     *
     * @param name the name of the requested variable in the Foo file
     * @return a handle onto the corresponding variable (never <code>null</code>). 
     *  The variable may or may not exist.
     */
    IFooVar getVar(String name);
    
    /**
     * Returns the variables declared in this Foo file in the order in which 
     * they appear in the source.
     *
     * @return the variables declared in this Foo file (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception 
     *  occurs while accessing its corresponding resource
     */
    IFooVar[] getVars() throws CoreException;
    
    /**
     * Returns the function with the given name and the given arity defined 
     * in this Foo file. This is a handle-only method. The function may or 
     * may not exist.
     *
     * @param name the name of the requested function in the Foo file
     * @param arity the arity of the requested function in the Foo file
     * @return a handle onto the corresponding function (never <code>null</code>). 
     *  The function may or may not exist.
     */
    IFooDef getDef(String name, int arity);
    
    /**
     * Returns the functions defined in this Foo file in the order in which 
     * they appear in the source.
     *
     * @return the functions defined in this Foo file (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception 
     *  occurs while accessing its corresponding resource
     */
    IFooDef[] getDefs() throws CoreException;
}
