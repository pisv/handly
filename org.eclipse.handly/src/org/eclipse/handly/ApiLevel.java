/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly;

/**
 * Declares constants for API levels as defined by various releases
 * of the framework.
 */
public class ApiLevel
{
    /**
     * The 1.0 API.
     */
    public static final int _1_0 = 0;
    /**
     * Corresponds to the API level the code using this constant
     * was built against.
     */
    public static final int CURRENT = _1_0;

    private ApiLevel()
    {
    }
}
