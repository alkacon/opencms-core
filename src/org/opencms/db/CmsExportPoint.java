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

package org.opencms.db;

import org.opencms.main.OpenCms;

/**
 * Contains the data of a single export point.<p>
 *
 * @since 6.0.0
 */
public class CmsExportPoint {

    /** The configured destination path. */
    private String m_configuredDestination;

    /** The destination path in the "real" file system, relative to the web application folder. */
    private String m_destinationPath;

    /** The URI of the OpenCms VFS resource (folder) of the export point. */
    private String m_uri;

    /**
     * Creates a new, empty export point.<p>
     */
    public CmsExportPoint() {

        m_uri = "";
        m_configuredDestination = "";
        m_destinationPath = "";
    }

    /**
     * Creates a new export point.<p>
     *
     * @param uri the folder in the OpenCms VFS to write as export point
     * @param destination the destination folder in the "real" file system,
     *     relative to the web application root
     */
    public CmsExportPoint(String uri, String destination) {

        m_uri = uri;
        m_configuredDestination = destination;
        m_destinationPath = null;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsExportPoint clone = new CmsExportPoint(m_uri, m_configuredDestination);
        clone.setDestinationPath(getDestinationPath());
        return clone;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsExportPoint) {
            return ((CmsExportPoint)obj).m_uri.equals(m_uri);
        }
        return false;
    }

    /**
     * Returns the configured destination path.<p>
     *
     * The configured destination path is always relative to the
     * web application path.<p>
     *
     * @return the configured destination path
     *
     * @see #getDestinationPath()
     */
    public String getConfiguredDestination() {

        return m_configuredDestination;
    }

    /**
     * Returns the destination path in the "real" file system.<p>
     *
     * @return the destination
     */
    public String getDestinationPath() {

        if (m_destinationPath == null) {
            m_destinationPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(
                m_configuredDestination);
        }
        return m_destinationPath;
    }

    /**
     * Returns the uri of the OpenCms VFS folder to write as export point.<p>
     *
     * @return the uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getUri().hashCode();
    }

    /**
     * Sets the configured destination path.<p>
     *
     * The configured destination path is always relative to the
     * web application path.<p>
     *
     * This set method will automatically set the destination path as well.<p>
     *
     * @param value the configured destination path
     *
     */
    public void setConfiguredDestination(String value) {

        m_configuredDestination = value;
        m_destinationPath = null;
    }

    /**
     * Dummy method to expose the destination path as bean property.<p>
     *
     * This is required by the {@link org.apache.commons.beanutils.BeanUtils} package in order to
     * enable using this Object with the digester.<p>
     *
     * The method does not actually change the value of the destination path.
     * Use the <code>{@link #setConfiguredDestination(String)}</code> method instead.
     *
     * @param value the destination path (will be ignored)
     */
    public void setDestinationPath(String value) {

        if (value == null) {
            // check required to avoid "unused parameter" warning
        }
    }

    /**
     * Sets the uri of the OpenCms VFS folder to write as export point.<p>
     *
     * @param value the uri to set
     */
    public void setUri(String value) {

        m_uri = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[" + getClass().getName() + ", uri: " + m_uri + ", destination: " + getDestinationPath() + "]";
    }
}