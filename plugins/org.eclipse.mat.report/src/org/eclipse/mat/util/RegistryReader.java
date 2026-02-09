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
 *    Standalone adaptation - removed OSGi extension point dependency
 *******************************************************************************/
package org.eclipse.mat.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Standalone version of RegistryReader - no OSGi extension points.
 * Subclasses register delegates manually.
 */
public abstract class RegistryReader<D>
{
    protected Set<D> delegates = new HashSet<D>();

    protected RegistryReader()
    {}

    /**
     * No-op in standalone mode. Extension point identifier is ignored.
     */
    protected final void init(Object tracker, String extensionPointIdentifier)
    {
        // Standalone: no extension point scanning.
        // Subclasses should register delegates manually.
    }

    /**
     * Manually register a delegate.
     */
    public void registerDelegate(D delegate)
    {
        if (delegate != null)
            delegates.add(delegate);
    }

    protected abstract D createDelegate(Object configElement) throws Exception;

    protected void removeDelegate(D delegate)
    {}

    public Collection<D> delegates()
    {
        return Collections.unmodifiableCollection(delegates);
    }
}
