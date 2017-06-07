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

package org.opencms.util;

/**
 * Simple utility class for caching a single value with a given cache expiration time.<p>
 *
 * If the time between the last write operation and a read operation is longer than the expiration time,
 * null will be returned from the read operation.<p>
 *
 * @param <T> the type of the cached value
 */
public class CmsExpiringValue<T> {

    /** The cached value. */
    private T m_value;

    /** Last time the value was set. */
    private long m_lastUpdate;

    /** The expiration time. */
    private long m_expirationTime;

    /**
     * Creates a new instance.<p>
     *
     * @param expirationTime the expiration time in milliseconds
     */
    public CmsExpiringValue(long expirationTime) {
        m_expirationTime = expirationTime;
    }

    /**
     * Gets the cached value.<p>
     *
     * Returns null if the last update was longer ago than the expiration time.
     *
     * @return the cached value
     */
    public synchronized T get() {

        if ((System.currentTimeMillis() - m_lastUpdate) > m_expirationTime) {
            return null;
        }
        return m_value;
    }

    /**
     * Sets the cached value.<p>
     *
     * @param value the cached value
     */
    public synchronized void set(T value) {

        m_lastUpdate = System.currentTimeMillis();
        m_value = value;
    }

}
