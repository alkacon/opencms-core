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

package org.opencms.ui.apps.cacheadmin;

import com.alkacon.simapi.Simapi;

import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.I_CmsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheEntry;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceService;
import org.opencms.ui.A_CmsUI;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.io.IOUtils;

/**
 * Bean for Variations im image resources.<p>
 */
public class CmsVariationBean {

    /**Variation path.*/
    private String m_variationPath;

    /** The external image cache. */
    private I_CmsImageCache m_imageCache;

    /** The variation length. */
    private long m_length = -1;

    /** The maintenance service used for lazy metadata access. */
    private CmsImageCacheMaintenanceService m_maintenance;

    /** Whether timestamp metadata has been loaded. */
    private boolean m_metadataLoaded;

    /** The variation name. */
    private String m_name;

    /** The last modification time. */
    private Instant m_lastModified;

    /**
     * Creates a variation bean with lazy metadata access.<p>
     *
     * @param imageCache the external image cache
     * @param maintenance the image cache maintenance service
     * @param name the image cache entry name
     * @param length the image cache entry length
     */
    public CmsVariationBean(
        I_CmsImageCache imageCache,
        CmsImageCacheMaintenanceService maintenance,
        String name,
        long length) {

        m_imageCache = imageCache;
        m_maintenance = maintenance;
        m_name = name.startsWith("/") ? name : "/" + name;
        m_length = length;

    }

    /**
     * Creates a variation bean for an external image cache entry.<p>
     *
     * @param imageCache the external image cache
     * @param name the image cache entry name
     * @param length the image cache entry length
     */
    public CmsVariationBean(I_CmsImageCache imageCache, String name, long length) {

        this(imageCache, null, name, length);
    }

    /**
     * public constructor.<p>
     *
     * @param variation path to variation file to hold information for
     */
    public CmsVariationBean(String variation) {

        m_variationPath = variation;
    }

    /** Returns the formatted derivative last-modified time, if available. */
    public String getDateLastModified() {

        loadMetadata();
        return formatDate(m_lastModified);
    }

    /**
     * Gets the dimensions of the current variation.<p>
     *
     * @return String representation of the dimensions
     */
    public String getDimensions() {

        try {
            BufferedImage img = Simapi.read(readContent());
            return "" + img.getWidth() + " x " + img.getHeight() + "px";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get length of variation.<p>
     *
     * @return string representation of length
     */
    public String getLength() {

        if (m_length >= 0) {
            return CmsFileUtil.formatFilesize(m_length, A_CmsUI.get().getLocale());
        }
        return CmsFileUtil.formatFilesize(new java.io.File(m_variationPath).length(), A_CmsUI.get().getLocale());
    }

    /**
     * Gets path of variation.<p>
     *
     * @return path
     */
    public String getName() {

        if (m_name != null) {
            return m_name;
        }
        String repositoryPath = CmsImageLoader.getImageRepositoryPath();
        if ((repositoryPath == null) || !m_variationPath.startsWith(repositoryPath)) {
            return "";
        }
        return m_variationPath.substring(repositoryPath.length());
    }

    /** Formats a timestamp for the current UI locale. */
    private String formatDate(Instant timestamp) {

        if (timestamp == null) {
            return "";
        }
        return CmsDateUtil.getDateTime(Date.from(timestamp), DateFormat.SHORT, A_CmsUI.get().getLocale());
    }

    /** Loads timestamp metadata when the variations dialog requests it. */
    private synchronized void loadMetadata() {

        if (m_metadataLoaded) {
            return;
        }
        m_metadataLoaded = true;
        try {
            if (m_maintenance != null) {
                CmsImageCacheEntry entry = m_maintenance.getEntry(m_name);
                if (entry != null) {
                    m_lastModified = entry.getLastModified();
                }
            } else if (m_variationPath != null) {
                m_lastModified = Files.getLastModifiedTime(
                    Paths.get(m_variationPath),
                    LinkOption.NOFOLLOW_LINKS).toInstant();
            }
        } catch (Exception e) {
            // The cache entry may have disappeared between listing and opening the dialog.
        }
    }

    /**
     * Reads the variation content.<p>
     *
     * @return the variation content
     * @throws Exception if reading fails
     */
    private byte[] readContent() throws Exception {

        if (m_imageCache != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            m_imageCache.writeTo(m_name, out);
            return out.toByteArray();
        }
        try (FileInputStream in = new FileInputStream(m_variationPath)) {
            return IOUtils.toByteArray(in);
        }
    }
}
