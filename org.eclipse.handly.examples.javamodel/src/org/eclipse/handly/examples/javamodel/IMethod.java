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
package org.eclipse.handly.examples.javamodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.util.Property;

/**
 * Represents a method (or constructor) declared in a type.
 */
public interface IMethod
    extends IMember
{
    /**
     * Parameter names property.
     * @see #getParameterNames()
     */
    Property<String[]> PARAMETER_NAMES = Property.get("parameterNames", //$NON-NLS-1$
        String[].class);
    /**
     * Return type property.
     * @see #getReturnType()
     */
    Property<String> RETURN_TYPE = Property.get("returnType", String.class); //$NON-NLS-1$
    /**
     * Exception types property.
     * @see #getExceptionTypes()
     */
    Property<String[]> EXCEPTION_TYPES = Property.get("exceptionTypes", //$NON-NLS-1$
        String[].class);
    /**
     * Is_constructor property.
     * @see #isConstructor()
     */
    Property<Boolean> IS_CONSTRUCTOR = Property.get("isConstructor", //$NON-NLS-1$
        Boolean.class);

    @Override
    default IType getParent()
    {
        return (IType)IMember.super.getParent();
    }

    /**
     * Returns the type signatures for the parameters of this method.
     * Returns an empty array if this method has no parameters.
     * This is a handle-only method.
     *
     * @return the type signatures for the parameters of this method
     *  (never <code>null</code>). Clients <b>must not</b> modify the
     *  returned array.
     * @see org.eclipse.jdt.core.Signature
     */
    String[] getParameterTypes();

    /**
     * Returns the names of parameters in this method.
     * Returns an empty array if this method has no parameters.
     *
     * @return the names of parameters in this method (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    String[] getParameterNames() throws CoreException;

    /**
     * Returns the type signature of the return value of this method.
     * For constructors, this returns the signature for void.
     *
     * @return the type signature of the return value of this method,
     *  void for constructors (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     * @see org.eclipse.jdt.core.Signature
     */
    String getReturnType() throws CoreException;

    /**
     * Returns the type signatures of the exceptions this method throws,
     * in the order declared in the source. Returns an empty array
     * if this method throws no exceptions.
     *
     * @return the type signatures of the exceptions this method throws,
     *  in the order declared in the source (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     * @see org.eclipse.jdt.core.Signature
     */
    String[] getExceptionTypes() throws CoreException;

    /**
     * Returns the signature of this method. This includes the signatures for
     * the parameter types and return type, but does not include the method name,
     * exception types, or type parameters.
     *
     * @return the signature of this method (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     * @see org.eclipse.jdt.core.Signature
     */
    String getSignature() throws CoreException;

    /**
     * Returns whether this method is a constructor.
     *
     * @return <code>true</code> if this method is a constructor,
     *  <code>false</code> otherwise
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    boolean isConstructor() throws CoreException;
}
