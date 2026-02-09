/*******************************************************************************
 * Copyright (c) 2008, 2018 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - enhancements and fixes
 *    Standalone adaptation - removed OSGi extension point dependency
 *******************************************************************************/
package org.eclipse.mat.snapshot;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.parser.internal.SnapshotFactoryImpl;
import org.eclipse.mat.util.IProgressListener;

/**
 * {@link ISnapshot} factory - standalone version.
 * Directly instantiates SnapshotFactoryImpl instead of using OSGi extension points.
 */
public final class SnapshotFactory
{
    public interface Implementation
    {
        ISnapshot openSnapshot(File file, Map<String, String> arguments, IProgressListener listener)
                        throws SnapshotException;
        void dispose(ISnapshot snapshot);
        IOQLQuery createQuery(String queryString) throws OQLParseException, SnapshotException;
        List<SnapshotFormat> getSupportedFormats();
    }

    private static final Implementation factory = new SnapshotFactoryImpl();

    public static ISnapshot openSnapshot(File file, IProgressListener listener) throws SnapshotException
    {
        return openSnapshot(file, new HashMap<String, String>(0), listener);
    }

    public static ISnapshot openSnapshot(File file, Map<String, String> arguments, IProgressListener listener)
                    throws SnapshotException
    {
        return factory.openSnapshot(file, arguments, listener);
    }

    public static void dispose(ISnapshot snapshot)
    {
        factory.dispose(snapshot);
    }

    public static IOQLQuery createQuery(String queryString) throws OQLParseException, SnapshotException
    {
        return factory.createQuery(queryString);
    }

    public static List<SnapshotFormat> getSupportedFormats()
    {
        return factory.getSupportedFormats();
    }

    private SnapshotFactory()
    {}
}
