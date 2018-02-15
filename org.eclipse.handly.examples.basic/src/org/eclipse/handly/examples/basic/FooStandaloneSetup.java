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
package org.eclipse.handly.examples.basic;

/**
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
public class FooStandaloneSetup
    extends FooStandaloneSetupGenerated
{
    public static void doSetup()
    {
        new FooStandaloneSetup().createInjectorAndDoEMFRegistration();
    }
}
