/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsResourceBundleLoader.java,v $
 * Date   : $Date: 2011/03/23 14:50:58 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.i18n;

import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Resource bundle loader for property based resource bundles from OpenCms that has a flushable cache.<p>
 * 
 * The main reason for implementing this is that the Java default resource bundle loading mechanism 
 * provided by {@link java.util.ResourceBundle#getBundle(java.lang.String, java.util.Locale)} uses a 
 * cache that can NOT be flushed by any standard means. This means for every simple change in a resource 
 * bundle, the Java VM (and the webapp container that runs OpenCms) must be restarted. 
 * This non-standard resource bundle loader avoids this by providing a flushable cache.<p>
 * 
 * In case the requested bundle can not be found, a fallback mechanism to 
 * {@link java.util.ResourceBundle#getBundle(java.lang.String, java.util.Locale)} is used to look up 
 * the resource bundle with the Java default resource bundle loading mechanism.<p>
 * 
 * @see java.util.ResourceBundle
 * @see java.util.PropertyResourceBundle
 * @see org.opencms.i18n.CmsResourceBundle
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.2.0 
 */
public final class CmsResourceBundleLoader {

    /** 
     * Cache key for the ResourceBundle cache.<p>
     * 
     * Resource bundles are keyed by the combination of bundle name, locale, and class loader. 
     */
    private static class BundleKey {

        private String m_baseName;
        private int m_hashcode;
        private Locale m_locale;

        /**
         * Create an ampty bundly key.<p>
         */
        BundleKey() {

            // noop
        }

        /**
         * Create an initialized bundle key.<p>
         * 
         * @param s the base name
         * @param l the locale
         */
        BundleKey(String s, Locale l) {

            set(s, l);
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {

            if (!(o instanceof BundleKey)) {
                return false;
            }
            BundleKey key = (BundleKey)o;
            return (m_hashcode == key.m_hashcode) && m_baseName.equals(key.m_baseName) && m_locale.equals(key.m_locale);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return m_hashcode;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_baseName + "_" + m_locale;
        }

        /**
         * Initialize this bundle key.<p>
         * 
         * @param s the base name
         * @param l the locale
         */
        void set(String s, Locale l) {

            m_baseName = s;
            m_locale = l;
            m_hashcode = m_baseName.hashCode() ^ m_locale.hashCode();
        }
    }

    /**  The resource bundle cache. */
    private static Map m_bundleCache;

    /** The last default Locale we saw, if this ever changes then we have to reset our caches. */
    private static Locale m_lastDefaultLocale;

    /** Cache lookup key to avoid having to a new one for every getBundle() call. */
    private static BundleKey m_lookupKey = new BundleKey();

    /** Singleton cache entry to represent previous failed lookups. */
    private static final Object NULL_ENTRY = new Object();

    /**
     * Hides the public constructor.<p>
     */
    private CmsResourceBundleLoader() {

        // noop
    }

    /**
     * Flushes the resource bundle cache.<p>
     */
    public static synchronized void flushBundleCache() {

        m_bundleCache = new HashMap();
    }

    /**
     * Get the appropriate ResourceBundle for the given locale. The following
     * strategy is used:
     *
     * <p>A sequence of candidate bundle names are generated, and tested in
     * this order, where the suffix 1 means the string from the specified
     * locale, and the suffix 2 means the string from the default locale:</p>
     *
     * <ul>
     * <li>baseName + "_" + language1 + "_" + country1 + "_" + variant1</li>
     * <li>baseName + "_" + language1 + "_" + country1</li>
     * <li>baseName + "_" + language1</li>
     * <li>baseName + "_" + language2 + "_" + country2 + "_" + variant2</li>
     * <li>baseName + "_" + language2 + "_" + country2</li>
     * <li>baseName + "_" + language2</li>
     * <li>baseName</li>
     * </ul>
     *
     * <p>In the sequence, entries with an empty string are ignored. Next,
     * <code>getBundle</code> tries to instantiate the resource bundle:</p>
     *
     * <ul>
     * <li>This implementation only resolves property based resource bundles.
     * Class based resource bundles are nor found.</li>
     * <li>A search is made for a property resource file, by replacing
     * '.' with '/' and appending ".properties", and using
     * ClassLoader.getResource(). If a file is found, then a
     * PropertyResourceBundle is created from the file's contents.</li>
     * </ul>
     * 
     * <p>If no resource bundle was found, the default resource bundle loader
     * is used to look for the resource bundle. Class based resource bundles
     * will be found now.<p>
     *
     * @param baseName the name of the ResourceBundle
     * @param locale A locale
     * @return the desired resource bundle
     */
    // This method is synchronized so that the cache is properly
    // handled.
    public static synchronized ResourceBundle getBundle(String baseName, Locale locale) {

        // If the default locale changed since the last time we were called,
        // all cache entries are invalidated.
        Locale defaultLocale = Locale.getDefault();
        if (defaultLocale != m_lastDefaultLocale) {
            m_bundleCache = new HashMap();
            m_lastDefaultLocale = defaultLocale;
        }

        // This will throw NullPointerException if any arguments are null.
        m_lookupKey.set(baseName, locale);

        Object obj = m_bundleCache.get(m_lookupKey);

        if (obj instanceof CmsResourceBundle) {
            return (CmsResourceBundle)obj;
        } else if (obj == NULL_ENTRY) {
            // Lookup has failed previously. Fall through.
        } else {
            // First, look for a bundle for the specified locale. We don't want
            // the base bundle this time.
            boolean wantBase = locale.equals(defaultLocale);
            CmsResourceBundle bundle = tryBundle(baseName, locale, wantBase);

            // Try the default locale if neccessary.
            if ((bundle == null) && !locale.equals(defaultLocale)) {
                bundle = tryBundle(baseName, defaultLocale, true);
            }

            BundleKey key = new BundleKey(baseName, locale);
            if (bundle == null) {
                // Cache the fact that this lookup has previously failed.
                m_bundleCache.put(key, NULL_ENTRY);
            } else {
                // Cache the result and return it.
                m_bundleCache.put(key, bundle);
                return bundle;
            }
        }

        // unable to find the resource bundle with this implementation
        // use default Java mechanism to look up the bundle again
        return ResourceBundle.getBundle(baseName, locale);
    }

    /**
     * Tries to load a property file with the specified name.
     *
     * @param localizedName the name
     * @return the resource bundle if it was loaded, otherwise the backup
     */
    private static CmsResourceBundle tryBundle(String localizedName) {

        CmsResourceBundle bundle = null;

        try {

            InputStream is = null;
            String resourceName = localizedName.replace('.', '/') + ".properties";
            URL url = CmsResourceBundleLoader.class.getClassLoader().getResource(resourceName);

            if (url != null) {
                String path = CmsFileUtil.normalizePath(url);
                File file = new File(path);
                try {
                    // try to load the resource bundle from a file, NOT with the resource loader first
                    // this is important since using #getResourceAsStream() may return cached results,
                    // for example Tomcat by default does cache all resources loaded by the class loader
                    // this means a changed resource bundle file is not loaded 
                    is = new FileInputStream(file);
                } catch (IOException ex) {
                    // this will happen if the resource is contained for example in a .jar file
                    is = CmsResourceBundleLoader.class.getClassLoader().getResourceAsStream(resourceName);
                } catch (AccessControlException acex) {
                    // fixed bug #1550
                    // this will happen if the resource is contained for example in a .jar file
                    // and security manager is turned on.
                    is = CmsResourceBundleLoader.class.getClassLoader().getResourceAsStream(resourceName);
                }
            }
            if (is != null) {
                bundle = new CmsResourceBundle(is);
            }
        } catch (IOException ex) {
            // can't localized these message since this may lead to a chicken-egg problem
            MissingResourceException mre = new MissingResourceException(
                "Failed to load bundle '" + localizedName + "'",
                localizedName,
                "");
            mre.initCause(ex);
            throw mre;
        }

        return bundle;
    }

    /**
     * Tries to load a the bundle for a given locale, also loads the backup
     * locales with the same language.
     *
     * @param baseName the raw bundle name, without locale qualifiers
     * @param locale the locale
     * @param wantBase whether a resource bundle made only from the base name
     *        (with no locale information attached) should be returned.
     * @return the resource bundle if it was loaded, otherwise the backup
     */
    private static CmsResourceBundle tryBundle(String baseName, Locale locale, boolean wantBase) {

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        int baseLen = baseName.length();

        // Build up a StringBuffer containing the complete bundle name, fully
        // qualified by locale.
        StringBuffer sb = new StringBuffer(baseLen + variant.length() + 7);

        sb.append(baseName);

        if (language.length() > 0) {
            sb.append('_');
            sb.append(language);

            if (country.length() > 0) {
                sb.append('_');
                sb.append(country);

                if (variant.length() > 0) {
                    sb.append('_');
                    sb.append(variant);
                }
            }
        }

        // Now try to load bundles, starting with the most specialized name.
        // Build up the parent chain as we go.
        String bundleName = sb.toString();
        CmsResourceBundle first = null; // The most specialized bundle.
        CmsResourceBundle last = null; // The least specialized bundle.

        while (true) {
            CmsResourceBundle foundBundle = tryBundle(bundleName);
            if (foundBundle != null) {
                if (first == null) {
                    first = foundBundle;
                }

                if (last != null) {
                    last.setParent(foundBundle);
                }
                foundBundle.setLocale(locale);

                last = foundBundle;
            }
            int idx = bundleName.lastIndexOf('_');
            // Try the non-localized base name only if we already have a
            // localized child bundle, or wantBase is true.
            if ((idx > baseLen) || ((idx == baseLen) && ((first != null) || wantBase))) {
                bundleName = bundleName.substring(0, idx);
            } else {
                break;
            }
        }

        return first;
    }
}