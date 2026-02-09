/*******************************************************************************
 * Copyright (c) 2008, 2021 SAP AG and IBM Corporation.
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
package org.eclipse.mat.report.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.mat.report"; //$NON-NLS-1$

    private static final ReportPlugin plugin = new ReportPlugin();

    public ReportPlugin()
    {}

    public static ReportPlugin getDefault()
    {
        return plugin;
    }

    public static void log(Throwable e)
    {
        log(e, Messages.ReportPlugin_InternalError);
    }

    public static void log(Throwable e, String message)
    {
        Logger.getLogger(ReportPlugin.class.getName()).log(Level.SEVERE, message, e);
    }

    public static void log(int status, String message)
    {
        Logger.getLogger(ReportPlugin.class.getName()).log(Level.WARNING, message);
    }

    static List<Runnable> stop = new ArrayList<Runnable>();
    public static void onStop(Runnable r)
    {
        synchronized(stop)
        {
            if (!stop.contains(r))
                stop.add(r);
        }
    }
}
