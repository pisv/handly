/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
