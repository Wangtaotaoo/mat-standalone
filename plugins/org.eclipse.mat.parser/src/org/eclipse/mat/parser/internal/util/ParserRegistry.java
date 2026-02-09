/*******************************************************************************
 * Copyright (c) 2008, 2021 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    Andrew Johnson - content types
 *    Standalone adaptation - removed OSGi dependency
 *******************************************************************************/
package org.eclipse.mat.parser.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.mat.hprof.HprofHeapObjectReader;
import org.eclipse.mat.hprof.HprofIndexBuilder;
import org.eclipse.mat.parser.IIndexBuilder;
import org.eclipse.mat.parser.IObjectReader;
import org.eclipse.mat.parser.internal.Messages;
import org.eclipse.mat.snapshot.SnapshotFormat;
import org.eclipse.mat.util.RegistryReader;

/**
 * Standalone ParserRegistry - no OSGi extension points.
 * Hard-codes the HPROF parser registration.
 */
public class ParserRegistry extends RegistryReader<ParserRegistry.Parser>
{
    public static final String INDEX_BUILDER = "indexBuilder";//$NON-NLS-1$
    public static final String OBJECT_READER = "objectReader";//$NON-NLS-1$

    public class Parser
    {
        private String id;
        private String uniqueIdentifier;
        private SnapshotFormat snapshotFormat;
        private Pattern[] pattern;
        private Class<? extends IIndexBuilder> indexBuilderClass;
        private Class<? extends IObjectReader> objectReaderClass;

        public Parser(String id, String uniqueIdentifier, SnapshotFormat snapshotFormat,
                       Pattern[] pattern,
                       Class<? extends IIndexBuilder> indexBuilderClass,
                       Class<? extends IObjectReader> objectReaderClass)
        {
            this.id = id;
            this.uniqueIdentifier = uniqueIdentifier;
            this.snapshotFormat = snapshotFormat;
            this.pattern = pattern;
            this.indexBuilderClass = indexBuilderClass;
            this.objectReaderClass = objectReaderClass;
        }

        public String getId()
        {
            return id;
        }

        public String getUniqueIdentifier()
        {
            return uniqueIdentifier;
        }

        public SnapshotFormat getSnapshotFormat()
        {
            return snapshotFormat;
        }

        @SuppressWarnings("unchecked")
        public <I> I create(Class<I> type, String attribute)
        {
            try
            {
                Class<?> clazz;
                if (INDEX_BUILDER.equals(attribute))
                    clazz = indexBuilderClass;
                else if (OBJECT_READER.equals(attribute))
                    clazz = objectReaderClass;
                else
                    return null;

                if (clazz == null)
                    return null;

                return (I) clazz.getDeclaredConstructor().newInstance();
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Error creating " + type.getSimpleName() + " for attribute " + attribute, e);
                return null;
            }
        }
    }

    public ParserRegistry()
    {
        registerHprofParser();
    }

    private void registerHprofParser()
    {
        try
        {
            String[] extensions = {"hprof", "bin"};
            Pattern[] patterns = new Pattern[extensions.length];
            for (int ii = 0; ii < extensions.length; ii++)
                patterns[ii] = Pattern.compile("(.*\\.)((?i)" + extensions[ii] + ")(\\.[0-9]*)?");//$NON-NLS-1$//$NON-NLS-2$

            SnapshotFormat snapshotFormat = new SnapshotFormat("HPROF", extensions);
            Parser parser = new Parser(
                "hprof",
                "org.eclipse.mat.hprof.hprof",
                snapshotFormat,
                patterns,
                HprofIndexBuilder.class,
                HprofHeapObjectReader.class
            );
            registerDelegate(parser);
        }
        catch (PatternSyntaxException e)
        {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                "Error compiling file name pattern for HPROF parser", e);
        }
    }

    @Override
    protected Parser createDelegate(Object configElement) throws Exception
    {
        // Not used in standalone mode
        return null;
    }

    @Override
    protected void removeDelegate(Parser delegate)
    {}

    public Parser lookupParser(String uniqueIdentifier)
    {
        for (Parser p : delegates())
            if (uniqueIdentifier.equals(p.getUniqueIdentifier()))
                return p;
        return null;
    }

    public List<Parser> matchParser(String fileName)
    {
        List<Parser> answer = new ArrayList<Parser>();
        for (Parser p : delegates())
        {
            for (Pattern regex : p.pattern)
            {
                if (regex.matcher(fileName).matches())
                {
                    answer.add(p);
                    break;
                }
            }
        }
        return answer;
    }
}
