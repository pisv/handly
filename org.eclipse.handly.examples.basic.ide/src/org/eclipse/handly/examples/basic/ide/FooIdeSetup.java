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
package org.eclipse.handly.examples.basic.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.handly.examples.basic.FooRuntimeModule;
import org.eclipse.handly.examples.basic.FooStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class FooIdeSetup
    extends FooStandaloneSetup
{
    @Override
    public Injector createInjector()
    {
        return Guice.createInjector(Modules2.mixin(new FooRuntimeModule(),
            new FooIdeModule()));
    }
}
