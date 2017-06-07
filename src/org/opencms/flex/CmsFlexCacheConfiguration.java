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

package org.opencms.flex;

import org.opencms.jsp.util.CmsJspDeviceSelectorDesktopMobileTablet;
import org.opencms.jsp.util.I_CmsJspDeviceSelector;
import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Flex Cache configuration class.<p>
 *
 * @since 6.0.0
 */
public class CmsFlexCacheConfiguration {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexCacheConfiguration.class);

    /** The average cache bytes. */
    private long m_avgCacheBytes;

    /** Indicates if the cache is enabled or not. */
    private boolean m_cacheEnabled;

    /** Indicates if offline resources should be cached or not. */
    private boolean m_cacheOffline;

    /** The device selector. */
    private I_CmsJspDeviceSelector m_deviceSelector;

    /** The device selector configuration. */
    private String m_deviceSelectorConfiguration;

    /**
     * Sizing parameters for the cached "entries" (ie. pages) in the FlexCache.<p>
     *
     * The amount of server memory available obviously is the
     * critical factor here. The values below are set in byte size.
     * The default is 2mb memory for the cached pages _or_ a maximum of 4000
     * cached page variations in total.
     */
    private long m_maxCacheBytes;

    /** The maximum entry bytes. */
    private int m_maxEntryBytes;

    /** The maximum key. */
    private int m_maxKeys;

    /**
     * Empty public constructor for the digester.
     */
    public CmsFlexCacheConfiguration() {

        // empty public constructor for digester
    }

    /**
     * Returns the average cache bytes.<p>
     *
     * @return the average cache bytes
     */
    public long getAvgCacheBytes() {

        return m_avgCacheBytes;
    }

    /**
     * Returns the deviceSelector.<p>
     *
     * @return the deviceSelector
     */
    public I_CmsJspDeviceSelector getDeviceSelector() {

        if (m_deviceSelector == null) {
            m_deviceSelector = new CmsJspDeviceSelectorDesktopMobileTablet();
        }
        return m_deviceSelector;
    }

    /**
     * Returns the device selector configuration.<p>
     *
     * @return the device selector configuration
     */
    public String getDeviceSelectorConfiguration() {

        return m_deviceSelectorConfiguration;
    }

    /**
     * Returns the maxCacheBytes.<p>
     *
     * @return the maxCacheBytes
     */
    public long getMaxCacheBytes() {

        return m_maxCacheBytes;
    }

    /**
     * Returns the maxEntryBytes.<p>
     *
     * @return the maxEntryBytes
     */
    public int getMaxEntryBytes() {

        return m_maxEntryBytes;
    }

    /**
     * Returns the maxKeys.<p>
     *
     * @return the maxKeys
     */
    public int getMaxKeys() {

        return m_maxKeys;
    }

    /**
     * Initializes the flex cache configuration with required parameters.<p>
     *
     * @param enabled enables or disable the flexcache
     * @param offline enable the flexcache for the offline project
     * @param maxCacheBytes the max bytes for cache
     * @param avgCacheBytes the average bytes for cache
     * @param maxEntryBytes the max bytes for entry
     * @param maxKeys the max keys
     */
    public void initialize(
        String enabled,
        String offline,
        String maxCacheBytes,
        String avgCacheBytes,
        String maxEntryBytes,
        String maxKeys) {

        setCacheEnabled(Boolean.valueOf(enabled).booleanValue());
        setCacheOffline(Boolean.valueOf(offline).booleanValue());
        setMaxCacheBytes(Long.parseLong(maxCacheBytes));
        setAvgCacheBytes(Long.parseLong(avgCacheBytes));
        setMaxEntryBytes(Integer.parseInt(maxEntryBytes));
        setMaxKeys(Integer.parseInt(maxKeys));
    }

    /**
     * Checks if flexcache is enabled or not.<p>
     *
     * @return true if flexcache is enabled; otherwise false
     */
    public boolean isCacheEnabled() {

        return m_cacheEnabled;
    }

    /**
     * Checks the cacheOffline.<p>
     *
     * @return true if cacheoffline is set to true; otherwise false
     */
    public boolean isCacheOffline() {

        return m_cacheOffline;
    }

    /**
     * Sets the avgCacheBytes.<p>
     *
     * @param avgCacheBytes the avgCacheBytes to set
     */
    public void setAvgCacheBytes(long avgCacheBytes) {

        m_avgCacheBytes = avgCacheBytes;
    }

    /**
     * Sets the enabled.<p>
     *
     * @param enabled the enabled to set
     */
    public void setCacheEnabled(boolean enabled) {

        m_cacheEnabled = enabled;
    }

    /**
     * Sets the cacheOffline.<p>
     *
     * @param cacheOffline the cacheOffline to set
     */
    public void setCacheOffline(boolean cacheOffline) {

        m_cacheOffline = cacheOffline;
    }

    /**
     * Sets the device selector configuration.<p>
     *
     * @param deviceSelector the device selector to set
     */
    public void setDeviceSelectorConfiguration(String deviceSelector) {

        m_deviceSelectorConfiguration = deviceSelector;

        Object objectInstance;

        try {
            objectInstance = Class.forName(m_deviceSelectorConfiguration).newInstance();
        } catch (Throwable t) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_CLASS_INIT_FAILURE_1, m_deviceSelectorConfiguration),
                t);
            return;
        }
        if (objectInstance instanceof I_CmsJspDeviceSelector) {
            m_deviceSelector = (I_CmsJspDeviceSelector)objectInstance;
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_FLEXCACHE_DEVICE_SELECTOR_SUCCESS_1,
                        m_deviceSelectorConfiguration));
            }
        } else {
            if (CmsLog.INIT.isFatalEnabled()) {
                CmsLog.INIT.fatal(
                    Messages.get().getBundle().key(
                        Messages.INIT_FLEXCACHE_DEVICE_SELECTOR_FAILURE_1,
                        m_deviceSelectorConfiguration));
            }
        }
    }

    /**
     * Sets the maxCacheBytes.<p>
     *
     * @param maxCacheBytes the maxCacheBytes to set
     */
    public void setMaxCacheBytes(long maxCacheBytes) {

        m_maxCacheBytes = maxCacheBytes;
    }

    /**
     * Sets the maxEntryBytes.<p>
     *
     * @param maxEntryBytes the maxEntryBytes to set
     */
    public void setMaxEntryBytes(int maxEntryBytes) {

        m_maxEntryBytes = maxEntryBytes;
    }

    /**
     * Sets the maxKeys.<p>
     *
     * @param maxKeys the maxKeys to set
     */
    public void setMaxKeys(int maxKeys) {

        m_maxKeys = maxKeys;
    }
}
