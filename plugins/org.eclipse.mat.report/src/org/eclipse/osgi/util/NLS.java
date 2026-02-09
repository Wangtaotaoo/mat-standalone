/*******************************************************************************
 * Standalone replacement for org.eclipse.osgi.util.NLS.
 * Loads message strings from .properties files into static fields.
 *******************************************************************************/
package org.eclipse.osgi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Minimal standalone NLS implementation compatible with Eclipse's NLS class.
 * Loads messages from a .properties resource bundle into public static String fields.
 */
public class NLS
{
    /**
     * Initialize the given class's public static String fields from a resource bundle.
     *
     * @param bundleName the fully qualified resource bundle name (e.g. "org.eclipse.mat.report.internal.messages")
     * @param clazz the class containing public static String fields to initialize
     */
    public static void initializeMessages(String bundleName, Class<?> clazz)
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields)
            {
                if (field.getType() == String.class
                    && Modifier.isPublic(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers()))
                {
                    try
                    {
                        String value = bundle.getString(field.getName());
                        field.set(null, value);
                    }
                    catch (MissingResourceException e)
                    {
                        // Leave field as null or set a default
                        field.set(null, "!" + field.getName() + "!");
                    }
                }
            }
        }
        catch (MissingResourceException e)
        {
            // Bundle not found - set all fields to their key names
            initializeDefaults(clazz);
        }
        catch (IllegalAccessException e)
        {
            // Should not happen with public fields
            throw new RuntimeException(e);
        }
    }

    private static void initializeDefaults(Class<?> clazz)
    {
        try
        {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields)
            {
                if (field.getType() == String.class
                    && Modifier.isPublic(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers()))
                {
                    field.set(null, "!" + field.getName() + "!");
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
