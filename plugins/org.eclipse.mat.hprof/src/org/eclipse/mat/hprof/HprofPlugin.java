/*******************************************************************************
 * Copyright (c) 2012,2022 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Standalone adaptation - removed OSGi dependency
 *******************************************************************************/
package org.eclipse.mat.hprof;

/**
 * Standalone HprofPlugin - no OSGi Plugin base class.
 */
public class HprofPlugin
{
    private static final HprofPlugin plugin = new HprofPlugin();

    public HprofPlugin()
    {}

    public static HprofPlugin getDefault()
    {
        return plugin;
    }
}
