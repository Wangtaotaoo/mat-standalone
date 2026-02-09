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
package org.eclipse.mat.hprof;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.mat.hprof.extension.IParsingEnhancer;
import org.eclipse.mat.hprof.extension.IRuntimeEnhancer;

/**
 * Standalone EnhancerRegistry - no OSGi extension points.
 * Returns empty collections (no enhancers in standalone mode).
 */
public class EnhancerRegistry
{
    public static class Enhancer
    {
        public IParsingEnhancer parser()
        {
            return null;
        }

        public IRuntimeEnhancer runtime()
        {
            return null;
        }
    }

    private static final EnhancerRegistry instance = new EnhancerRegistry();

    public static EnhancerRegistry instance()
    {
        return instance;
    }

    private EnhancerRegistry()
    {}

    public Collection<Enhancer> delegates()
    {
        return Collections.emptyList();
    }
}
