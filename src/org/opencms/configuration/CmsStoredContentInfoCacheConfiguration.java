/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.configuration;

import org.opencms.db.storage.CmsInMemoryStoredContentInfoCacheFactory;

/**
 * Configuration for the stored content info cache.<p>
 */
public class CmsStoredContentInfoCacheConfiguration {

    /** The default cache size. */
    public static final int DEFAULT_SIZE = 10000;

    /** The default time to live in seconds. */
    public static final long DEFAULT_TTL = 300;

    /** The factory class. */
    private String m_factoryClass = CmsInMemoryStoredContentInfoCacheFactory.class.getName();

    /** Indicates if the cache is enabled. */
    private boolean m_enabled = true;

    /** The maximum number of cached entries. */
    private int m_size = DEFAULT_SIZE;

    /** The time to live in seconds. */
    private long m_ttl = DEFAULT_TTL;

    /**
     * Returns the factory class name.<p>
     *
     * @return the factory class name
     */
    public String getFactoryClass() {

        return m_factoryClass;
    }

    /**
     * Returns the cache size.<p>
     *
     * @return the cache size
     */
    public int getSize() {

        return m_size;
    }

    /**
     * Returns the time to live in seconds.<p>
     *
     * @return the time to live in seconds
     */
    public long getTtl() {

        return m_ttl;
    }

    /**
     * Initializes the configuration.<p>
     *
     * @param factoryClass the factory class
     * @param enabled if the cache is enabled
     * @param size the maximum number of cached entries
     * @param ttl the time to live in seconds
     */
    public void initialize(String factoryClass, String enabled, String size, String ttl) {

        m_factoryClass = factoryClass;
        m_enabled = Boolean.valueOf(enabled).booleanValue();
        m_size = Integer.parseInt(size);
        m_ttl = Long.parseLong(ttl);
    }

    /**
     * Returns if the cache is enabled.<p>
     *
     * @return <code>true</code> if the cache is enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Sets the factory class name.<p>
     *
     * @param factoryClass the factory class name
     */
    public void setFactoryClass(String factoryClass) {

        m_factoryClass = factoryClass;
    }
}
