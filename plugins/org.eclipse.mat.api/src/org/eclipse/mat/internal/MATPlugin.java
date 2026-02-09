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
package org.eclipse.mat.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MATPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.mat.api"; //$NON-NLS-1$

    private static final MATPlugin plugin = new MATPlugin();

    public MATPlugin()
    {}

    public static MATPlugin getDefault()
    {
        return plugin;
    }

    public static void log(Throwable e)
    {
        log(e, Messages.MATPlugin_InternalError);
    }

    public static void log(Throwable e, String message)
    {
        Logger.getLogger(MATPlugin.class.getName()).log(Level.SEVERE, message, e);
    }

    public static void log(String message)
    {
        Logger.getLogger(MATPlugin.class.getName()).log(Level.SEVERE, message);
    }

    public static void log(int severity, String message)
    {
        Logger.getLogger(MATPlugin.class.getName()).log(Level.WARNING, message);
    }
}
