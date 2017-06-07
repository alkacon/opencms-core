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

package org.opencms.workplace.tools.content.updatexml;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsIllegalArgumentException;

/**
 * Bean to hold the settings needed for the operation of converting xml files in
 * the OpenCms VFS.
 * <p>
 *
 * @since 7.0.5
 *
 */
public final class CmsUpdateXmlSettings {

    /**
     * Property for the tag-replace contentool to know the files that have been processed before in
     * case of early terminaton in previous runs.
     */
    public static final String PROPERTY_CONTENTOOLS_TAGREPLACE = "contenttools.convertxml";

    /** The boolean value if process files in subfolders, too. */
    private boolean m_includeSubFolders;

    /** Only count files to transform. */
    private boolean m_onlyCountFiles;

    /** ResourceType. */
    private int m_resourceType;

    /** The root of all content files to process. */
    private String m_vfsFolder;

    /** Xsl file. */
    private String m_xslFile;

    /**
     * Bean constructor with cms object for path validation.
     * <p>
     *
     * @param cms used to test the working path for valididty.
     */
    public CmsUpdateXmlSettings(CmsObject cms) {

        m_resourceType = 0;
        m_vfsFolder = "";
        m_includeSubFolders = false;
        m_xslFile = "";
        m_onlyCountFiles = false;
    }

    /**
     * Gets if also files in sub folders shall become processed.<p>
     *
     * @return if also files in sub folders shall become processed.
     */
    public boolean getIncludeSubFolders() {

        return m_includeSubFolders;
    }

    /**
     * Gets if only count files to transform.<p>
     *
     * @return If only count files to transform.
     */
    public boolean getOnlyCountFiles() {

        return m_onlyCountFiles;
    }

    /**
     * Gets resource type to transform.<p>
     *
     * @return Resource type to transform.
     */
    public int getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the path under which files will be processed.
     * <p>
     *
     * @return the path under which files will be processed.
     */
    public String getVfsFolder() {

        return m_vfsFolder;
    }

    /**
     * Gets path to xsl file.<p>
     *
     * @return Path to xsl file.
     */
    public String getXslFile() {

        return m_xslFile;
    }

    /**
     * Sets value if also process files in sub folders.<p>
     *
     * @param subFolders True if process sub folders, too
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setIncludeSubFolders(boolean subFolders) throws CmsIllegalArgumentException {

        m_includeSubFolders = subFolders;
    }

    /**
     * Sets if only count files to transform.<p>
     *
     * @param countFiles True, if only count files to transform
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setOnlyCountFiles(boolean countFiles) throws CmsIllegalArgumentException {

        m_onlyCountFiles = countFiles;
    }

    /**
     * Sets resource type to transform.<p>
     *
     * @param resourceType File format to transform
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setResourceType(int resourceType) throws CmsIllegalArgumentException {

        m_resourceType = resourceType;
    }

    /**
     * Sets vfs folder to process files in.<p>
     *
     * @param vfsFolder The vfs folder to process files in
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setVfsFolder(String vfsFolder) throws CmsIllegalArgumentException {

        m_vfsFolder = vfsFolder;
    }

    /**
     * Sets path to xsl file.<p>
     *
     * @param xslFile Path to xsl file
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setXslFile(String xslFile) throws CmsIllegalArgumentException {

        m_xslFile = xslFile;
    }
}
