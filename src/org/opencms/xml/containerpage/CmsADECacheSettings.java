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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

/**
 * The cache settings for ADE.<p>
 *
 * @since 8.0.0
 */
public class CmsADECacheSettings {

    /** Default size for container page caches. */
    private static final int DEFAULT_CONTAINER_PAGE_SIZE = 128;

    /** The size of the container page offline cache. */
    private int m_containerPageOfflineSize;

    /** The size of the container page online cache. */
    private int m_containerPageOnlineSize;

    /** Default size for group container caches. */
    private static final int DEFAULT_GROUP_CONTAINER_SIZE = 128;

    /** The size of the group container offline cache. */
    private int m_groupContainerOfflineSize;

    /** The size of the group container online cache. */
    private int m_groupContainerOnlineSize;

    /**
     * Default constructor.<p>
     */
    public CmsADECacheSettings() {

        super();
    }

    /**
     * Returns the size of the container page offline cache.<p>
     *
     * @return the size of the container page offline cache
     */
    public int getContainerPageOfflineSize() {

        if (m_containerPageOfflineSize <= 0) {
            return DEFAULT_CONTAINER_PAGE_SIZE;
        }
        return m_containerPageOfflineSize;
    }

    /**
     * Returns the size of the container page online cache.<p>
     *
     * @return the size of the container page online cache
     */
    public int getContainerPageOnlineSize() {

        if (m_containerPageOnlineSize <= 0) {
            return DEFAULT_CONTAINER_PAGE_SIZE;
        }
        return m_containerPageOnlineSize;
    }

    /**
     * Sets the size of the cache for offline container pages.<p>
     *
     * @param size the size of the cache for offline container pages
     */
    public void setContainerPageOfflineSize(String size) {

        m_containerPageOfflineSize = getIntValue(size, DEFAULT_CONTAINER_PAGE_SIZE);
    }

    /**
     * Sets the size of the cache for online container pages.<p>
     *
     * @param size the size of the cache for online container pages
     */
    public void setContainerPageOnlineSize(String size) {

        m_containerPageOnlineSize = getIntValue(size, DEFAULT_CONTAINER_PAGE_SIZE);
    }

    /**
     * Returns the size of the group container offline cache.<p>
     *
     * @return the size of the group container offline cache
     */
    public int getGroupContainerOfflineSize() {

        if (m_groupContainerOfflineSize <= 0) {
            return DEFAULT_GROUP_CONTAINER_SIZE;
        }
        return m_groupContainerOfflineSize;
    }

    /**
     * Returns the size of the group container online cache.<p>
     *
     * @return the size of the group container online cache
     */
    public int getGroupContainerOnlineSize() {

        if (m_groupContainerOnlineSize <= 0) {
            return DEFAULT_GROUP_CONTAINER_SIZE;
        }
        return m_groupContainerOnlineSize;
    }

    /**
     * Sets the size of the cache for offline group containers.<p>
     *
     * @param size the size of the cache for offline group containers
     */
    public void setGroupContainerOfflineSize(String size) {

        m_groupContainerOfflineSize = getIntValue(size, DEFAULT_GROUP_CONTAINER_SIZE);
    }

    /**
     * Sets the size of the cache for online group containers.<p>
     *
     * @param size the size of the cache for online group containers
     */
    public void setGroupContainerOnlineSize(String size) {

        m_groupContainerOnlineSize = getIntValue(size, DEFAULT_GROUP_CONTAINER_SIZE);
    }

    /**
     * Turns a string into an int.<p>
     *
     * @param str the string to be converted
     * @param defaultValue a default value to be returned in case the string could not be parsed or the parsed int value is <= 0
     * @return the int value of the string
     */
    private int getIntValue(String str, int defaultValue) {

        try {
            int intValue = Integer.parseInt(str);
            return (intValue > 0) ? intValue : defaultValue;
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
        return defaultValue;
    }
}
