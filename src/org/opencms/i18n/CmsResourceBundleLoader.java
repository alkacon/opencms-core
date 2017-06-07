/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import java.util.List;
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
 * @see org.opencms.i18n.CmsPropertyResourceBundle
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

        /** The base bundle name. */
        private String m_baseName;

        /** The hash code. */
        private int m_hashcode;

        /** The locale. */
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
         * Checks if the given base name is identical to the base name of this bundle key.<p>
         *
         * @param baseName the base name to compare
         *
         * @return <code>true</code> if the given base name is identical to the base name of this bundle key
         */
        public boolean isSameBase(String baseName) {

            return m_baseName.equals(baseName);
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
    private static Map<BundleKey, ResourceBundle> m_bundleCache;

    /** The last default Locale we saw, if this ever changes then we have to reset our caches. */
    private static Locale m_lastDefaultLocale;

    /** Cache lookup key to avoid having to a new one for every getBundle() call. */
    private static BundleKey m_lookupKey = new BundleKey();

    /**  The permanent list resource bundle cache. */
    private static Map<String, I_CmsResourceBundle> m_permanentCache;

    /** Singleton cache entry to represent previous failed lookups. */
    private static final ResourceBundle NULL_ENTRY = new CmsListResourceBundle();

    /**
     * Hides the public constructor.<p>
     */
    private CmsResourceBundleLoader() {

        // noop
    }

    /**
     * Adds the specified resource bundle to the permanent cache.<p>
     *
     * @param baseName the raw bundle name, without locale qualifiers
     * @param locale the locale
     * @param bundle the bundle to cache
     */
    public static synchronized void addBundleToCache(String baseName, Locale locale, I_CmsResourceBundle bundle) {

        String key = baseName;
        if (locale != null) {
            key += "_" + locale;
        }
        m_permanentCache.put(key, bundle);
    }

    /**
     * Flushes the complete resource bundle cache.<p>
     */
    public static synchronized void flushBundleCache() {

        m_bundleCache.clear();

        // We are not flushing the permanent cache on clear!
        // Reason: It's not 100% clear if the cache would be filled correctly from the XML after a flush.
        // For example if a reference to an XML content object is held, than after a clear cache, this
        // object would not have a working localization since the schema and handler would not be initialized again.
        // For XML contents that are unmarshalled after the clear cache the localization would work, but it
        // seems likely that old references are held.
        // On the other hand, if something is changed in the XML, the cache is updated anyway, so we won't be
        // stuck with "old" resource bundles that require a server restart.

        // m_permanentCache.clear();
    }

    /**
     * Flushes all variations for the provided bundle from the cache.<p>
     *
     * @param baseName the bundle base name to flush the variations for
     * @param flushPermanent if true, the cache for additional message bundles will be flushed, too
     */
    public static synchronized void flushBundleCache(String baseName, boolean flushPermanent) {

        if (baseName != null) {
            // first check and clear the bundle cache
            HashMap<BundleKey, ResourceBundle> bundleCacheNew = new HashMap<BundleKey, ResourceBundle>(
                m_bundleCache.size());
            for (Map.Entry<BundleKey, ResourceBundle> entry : m_bundleCache.entrySet()) {
                if (!entry.getKey().isSameBase(baseName)) {
                    // entry has a different base name, keep it
                    bundleCacheNew.put(entry.getKey(), entry.getValue());
                }
            }
            if (bundleCacheNew.size() < m_bundleCache.size()) {
                // switch caches if only if at least one entry was removed
                m_bundleCache = bundleCacheNew;
            }
            if (flushPermanent) {
                // now check and clear the permanent cache
                HashMap<String, I_CmsResourceBundle> permanentCacheNew = new HashMap<String, I_CmsResourceBundle>(
                    m_permanentCache.size());
                for (Map.Entry<String, I_CmsResourceBundle> entry : m_permanentCache.entrySet()) {
                    String key = entry.getKey();
                    if (!(key.startsWith(baseName)
                        && ((key.length() == baseName.length()) || (key.charAt(baseName.length()) == '_')))) {
                        // entry has a different base name, keep it
                        permanentCacheNew.put(entry.getKey(), entry.getValue());
                    }
                }
                if (permanentCacheNew.size() < m_permanentCache.size()) {
                    // switch caches if only if at least one entry was removed
                    m_permanentCache = permanentCacheNew;
                }
            }
        }
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
            m_bundleCache = new HashMap<BundleKey, ResourceBundle>();
            m_lastDefaultLocale = defaultLocale;
            if (m_permanentCache == null) {
                // the permanent cache is not cleared after the default locale changes
                m_permanentCache = new HashMap<String, I_CmsResourceBundle>();
            }
        }

        // This will throw NullPointerException if any arguments are null.
        m_lookupKey.set(baseName, locale);

        Object obj = m_bundleCache.get(m_lookupKey);

        if (obj == NULL_ENTRY) {

        } else if (obj instanceof ResourceBundle) {
            return (ResourceBundle)obj;
        } else if (obj == NULL_ENTRY) {
            // Lookup has failed previously. Fall through.
        } else {
            // First, look for a bundle for the specified locale. We don't want
            // the base bundle this time.
            boolean wantBase = locale.equals(defaultLocale);
            ResourceBundle bundle = tryBundle(baseName, locale, wantBase);

            // Try the default locale if necessary
            if ((bundle == null) && !locale.equals(defaultLocale)) {
                bundle = tryBundle(baseName, defaultLocale, true);
            }

            BundleKey key = new BundleKey(baseName, locale);
            if (bundle != null) {
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
    private static I_CmsResourceBundle tryBundle(String localizedName) {

        I_CmsResourceBundle result = null;

        try {

            String resourceName = localizedName.replace('.', '/') + ".properties";
            URL url = CmsResourceBundleLoader.class.getClassLoader().getResource(resourceName);

            if (url != null) {
                // the resource was found on the file system
                InputStream is = null;
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
                if (is != null) {
                    result = new CmsPropertyResourceBundle(is);
                }
            } else {
                // no found with class loader, so try the injected list cache
                I_CmsResourceBundle additionalBundle = m_permanentCache.get(localizedName);
                if (additionalBundle != null) {
                    result = additionalBundle.getClone();
                }
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

        return result;
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
    private static ResourceBundle tryBundle(String baseName, Locale locale, boolean wantBase) {

        I_CmsResourceBundle first = null; // The most specialized bundle.
        I_CmsResourceBundle last = null; // The least specialized bundle.

        List<String> bundleNames = CmsLocaleManager.getLocaleVariants(baseName, locale, true, true);
        for (String bundleName : bundleNames) {
            // break if we would try the base bundle, but we do not want it directly
            if (bundleName.equals(baseName) && !wantBase && (first == null)) {
                break;
            }
            I_CmsResourceBundle foundBundle = tryBundle(bundleName);
            if (foundBundle != null) {
                if (first == null) {
                    first = foundBundle;
                }

                if (last != null) {
                    last.setParent((ResourceBundle)foundBundle);
                }
                foundBundle.setLocale(locale);

                last = foundBundle;
            }
        }
        return (ResourceBundle)first;
    }
}