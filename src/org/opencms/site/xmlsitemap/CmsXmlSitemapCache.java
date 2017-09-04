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

package org.opencms.site.xmlsitemap;

import org.opencms.main.CmsLog;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

/**
 * Cache for XML sitemaps.<p>
 */
public class CmsXmlSitemapCache {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlSitemapCache.class);

    /** Static instance for this class. */
    public static final CmsXmlSitemapCache INSTANCE = new CmsXmlSitemapCache();

    /** The map for storing the cached sitemaps. */
    private ConcurrentHashMap<String, String> m_cache = new ConcurrentHashMap<String, String>();

    /**
     * Clears the cache.<p>
     */
    public void clear() {

        m_cache.clear();
    }

    /**
     * Gets the cached entry for the given key (the key will normally be the root path of a sitemap.xml file).<p>
     *
     * @param key the key
     * @return the cached XML sitemap, or null if no cached value exists
     */
    public String get(String key) {

        return m_cache.get(key);
    }

    /**
     * Stores an XML sitemap in the cache.<p>
     *
     * @param key the XML sitemap key (usually the root path of the sitemap.xml)
     * @param value the XML sitemap content
     */
    public void put(String key, String value) {

        LOG.info("Caching sitemap for key " + key + ", size = " + value.length());
        m_cache.put(key, value);
    }

}
