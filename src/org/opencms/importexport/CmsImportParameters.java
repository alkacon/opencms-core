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

package org.opencms.importexport;

/**
 * Import parameters.<p>
 *
 * @since 7.0.4
 */
public class CmsImportParameters {

    /** The path in the OpenCms VFS to import into.*/
    private String m_destinationPath;

    /** If set, the permissions set on existing resources will not be modified.*/
    private boolean m_keepPermissions;

    /** The file path, could be a folder or a zip file. */
    private String m_path;

    /** If set, the manifest.xml file will be validated during the import. */
    private boolean m_xmlValidation;

    /**
     * Constructor.<p>
     *
     * @param path the file path, could be a folder or a zip file
     * @param destination path in the OpenCms VFS to import into
     * @param keepPermissions if set, the permissions set on existing resources will not be modified
     */
    public CmsImportParameters(String path, String destination, boolean keepPermissions) {

        setPath(path);
        setDestinationPath(destination);
        setKeepPermissions(keepPermissions);
    }

    /**
     * Returns the path in the OpenCms VFS to import into.<p>
     *
     * @return the path in the OpenCms VFS to import into
     */
    public String getDestinationPath() {

        return m_destinationPath;
    }

    /**
     * Returns the file path, could be a folder or a zip file.<p>
     *
     * @return the file path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the keep permissions flags.
     * if set, the permissions set on existing resources will not be modified.<p>
     *
     * @return the keep permissions flag
     */
    public boolean isKeepPermissions() {

        return m_keepPermissions;
    }

    /**
     * Checks if the manifest.xml file will be validated during the import.<p>
     *
     * @return the xml validation flag
     */
    public boolean isXmlValidation() {

        return m_xmlValidation;
    }

    /**
     * Sets the path in the OpenCms VFS to import into.<p>
     *
     * @param importPath the import path to set
     */
    public void setDestinationPath(String importPath) {

        m_destinationPath = importPath;
    }

    /**
     * Sets the keep permissions flag.
     * If set, the permissions set on existing resources will not be modified.<p>
     *
     * @param keepPermissions the keep permissions flag to set
     */
    public void setKeepPermissions(boolean keepPermissions) {

        m_keepPermissions = keepPermissions;
    }

    /**
     * Sets the file path, could be a folder or a zip file.<p>
     *
     * @param path the file path, could be a folder or a zip file
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the xml validation flag. If set, the manifest.xml file will be validated during the import.<p>
     *
     * @param xmlValidation the xml validation flag to set
     */
    public void setXmlValidation(boolean xmlValidation) {

        m_xmlValidation = xmlValidation;
    }
}
