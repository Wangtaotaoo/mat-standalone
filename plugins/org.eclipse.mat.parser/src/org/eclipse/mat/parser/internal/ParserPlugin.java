/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    Standalone adaptation - removed OSGi dependency
 *******************************************************************************/
package org.eclipse.mat.parser.internal;

import org.eclipse.mat.parser.internal.util.ParserRegistry;

public class ParserPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.mat.parser"; //$NON-NLS-1$

    private static final ParserPlugin plugin = new ParserPlugin();
    private final ParserRegistry registry;
    private boolean debugging = false;

    public ParserPlugin()
    {
        registry = new ParserRegistry();
    }

    public static ParserPlugin getDefault()
    {
        return plugin;
    }

    public ParserRegistry getParserRegistry()
    {
        return registry;
    }

    public boolean isDebugging()
    {
        return debugging;
    }
}
