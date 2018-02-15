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
