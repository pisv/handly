/*******************************************************************************
 * Copyright (c) 2017, 2019 1C-Soft LLC.
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
package org.eclipse.handly;

/**
 * Provides constants for API levels as defined by major and minor releases
 * of Handly.
 */
public final class ApiLevel
{
    /**
     * The 1.0 API level.
     */
    public static final int _1_0 = 0;

    /**
     * The 1.1 API level.
     *
     * @since 1.1
     */
    public static final int _1_1 = 1;

    /**
     * The 1.2 API level.
     *
     * @since 1.2
     */
    public static final int _1_2 = 2;

    /**
     * Corresponds to the API level the code using this constant
     * was built against.
     */
    public static final int CURRENT = _1_2;

    private ApiLevel()
    {
    }
}
