/*******************************************************************************
 * Copyright (c) 2008, 2016 SAP AG, IBM Corporation and others
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
 *    James Livingston - CollectionExtractorProviderRegistry based on existing MAT code
 *******************************************************************************/
package org.eclipse.mat.snapshot.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.internal.MATPlugin;
import org.eclipse.mat.snapshot.extension.CollectionExtractionInfo;
import org.eclipse.mat.snapshot.extension.ICollectionExtractorProvider;
import org.eclipse.mat.util.RegistryReader;

/**
 * @since 1.6
 */
public final class CollectionExtractorProviderRegistry extends RegistryReader<ICollectionExtractorProvider>
{
    private static final CollectionExtractorProviderRegistry INSTANCE = new CollectionExtractorProviderRegistry();
    
    private final Map<ICollectionExtractorProvider, List<CollectionExtractionInfo>> providerMap = new HashMap<ICollectionExtractorProvider, List<CollectionExtractionInfo>>();
    private final List<CollectionExtractionInfo> extractorInfos = new ArrayList<CollectionExtractionInfo>();
    
    // it would be too expensive to re-create the wrapper every time
    private final List<CollectionExtractionInfo> unmodifiableExtractorInfos;

    public static CollectionExtractorProviderRegistry instance()
    {
        return INSTANCE;
    }

    private CollectionExtractorProviderRegistry()
    {
        init(null, MATPlugin.PLUGIN_ID + ".collectionExtractorProvider"); //$NON-NLS-1$
        unmodifiableExtractorInfos = Collections.unmodifiableList(extractorInfos);
    }

    @Override
    protected ICollectionExtractorProvider createDelegate(Object configElement)
    {
        return null;
    }

    @Override
    protected void removeDelegate(ICollectionExtractorProvider provider)
    {
        List<CollectionExtractionInfo> infos = providerMap.get(provider);
        extractorInfos.removeAll(infos);
    }

    public List<CollectionExtractionInfo> getCollectionExtractionInfo() {
        return unmodifiableExtractorInfos;
    }
}
