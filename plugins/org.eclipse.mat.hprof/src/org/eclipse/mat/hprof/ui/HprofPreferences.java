/*******************************************************************************
 * Copyright (c) 2011, 2023 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Standalone adaptation - removed OSGi/Platform preferences
 *******************************************************************************/
package org.eclipse.mat.hprof.ui;

/**
 * Standalone HprofPreferences - returns sensible defaults without OSGi.
 */
public class HprofPreferences
{
    /** Strictness of the HPROF parser */
    public static final String STRICTNESS_PREF = "hprofStrictness"; //$NON-NLS-1$

    /** Default strictness for preferences and value parsing */
    public static final HprofStrictness DEFAULT_STRICTNESS = HprofStrictness.STRICTNESS_STOP;

    /** Additional references for classes */
    public static final String ADDITIONAL_CLASS_REFERENCES = "hprofAddClassRefs"; //$NON-NLS-1$

    /** Whether to treat stack frames as pseudo-objects and methods as pseudo-classes */
    public static final String P_METHODS = "methodsAsClasses"; //$NON-NLS-1$
    public static final String NO_METHODS_AS_CLASSES = "none"; //$NON-NLS-1$
    public static final String RUNNING_METHODS_AS_CLASSES = "running"; //$NON-NLS-1$
    public static final String FRAMES_ONLY = "frames"; //$NON-NLS-1$

    /**
     * Return the currently selected preference for strictness.
     * Standalone: returns DEFAULT_STRICTNESS, but checks -D system properties.
     */
    public static HprofStrictness getCurrentStrictness()
    {
        HprofStrictness strictnessPreference = DEFAULT_STRICTNESS;

        // Check if the user overrides on the command line
        for (HprofStrictness strictness : HprofStrictness.values())
        {
            if (Boolean.getBoolean(strictness.toString()))
            {
                strictnessPreference = strictness;
                break;
            }
        }

        return strictnessPreference;
    }

    /**
     * Enumeration for the parser strictness.
     */
    public enum HprofStrictness
    {
        STRICTNESS_STOP("hprofStrictnessStop"), //$NON-NLS-1$
        STRICTNESS_WARNING("hprofStrictnessWarning"), //$NON-NLS-1$
        STRICTNESS_PERMISSIVE("hprofStrictnessPermissive"); //$NON-NLS-1$

        private final String name;

        HprofStrictness(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public static HprofStrictness parse(String value)
        {
            if (value != null && value.length() > 0)
            {
                for (HprofStrictness strictness : values())
                {
                    if (strictness.toString().equals(value)) { return strictness; }
                }
            }
            return DEFAULT_STRICTNESS;
        }
    }

    /**
     * Standalone: return false (no additional class references).
     */
    public static boolean useAdditionalClassReferences()
    {
        return false;
    }

    /**
     * Standalone: return NO_METHODS_AS_CLASSES.
     */
    public static String methodsAsClasses()
    {
        return NO_METHODS_AS_CLASSES;
    }
}
