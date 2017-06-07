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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsResource;

/**
 * A single item of the ADE file type configuration.<p>
 *
 * A configuration item describes which file should be used as a template for new
 * content elements, and at which location in the VFS they should be created.<p>
 *
 * It does not contain a type, since the type is given by the type of the source file.<p>
 *
 * @since 7.6
 */
public class CmsConfigurationItem {

    /** The destination folder. */
    private final CmsResource m_folder;

    /** Flag to indicate if this item is default for it's resource-type. */
    private boolean m_isDefault;

    /** The lazy folder object. */
    private CmsLazyFolder m_lazyFolder;

    /** The file pattern. */
    private final String m_pattern;

    /** The source file. */
    private final CmsResource m_sourceFile;

    /**
     * Creates a new type configuration item.<p>
     *
     * @param sourceFile the source file
     * @param destinationFolder the destination folder
     * @param lazyFolder the lazy folder object
     * @param pattern the file pattern
     * @param isDefault <code>true</code> if this item is default for it's resource-type
     **/
    public CmsConfigurationItem(
        CmsResource sourceFile,
        CmsResource destinationFolder,
        CmsLazyFolder lazyFolder,
        String pattern,
        boolean isDefault) {

        m_isDefault = isDefault;
        m_sourceFile = sourceFile;
        m_folder = destinationFolder;
        m_pattern = pattern;
        m_lazyFolder = lazyFolder;
    }

    /**
     * Returns the destination folder uri.<p>
     *
     * @return the destination folder uri
     */
    public CmsResource getFolder() {

        return m_folder;
    }

    /**
     * Returns a helper object which represents a folder which may still have to be created.<p>
     *
     * @return a lazy folder object
     */
    public CmsLazyFolder getLazyFolder() {

        return m_lazyFolder;
    }

    /**
     * Returns the file pattern.<p>
     *
     * @return the file pattern
     */
    public String getPattern() {

        return m_pattern;
    }

    /**
     * Gets the source file uri.<p>
     *
     * @return the source file uri
     */
    public CmsResource getSourceFile() {

        return m_sourceFile;
    }

    /**
     * Returns if this item is default for it's resource-type.<p>
     *
     * @return <code>true</code> if this item is default for it's resource-type
     */
    public boolean isDefault() {

        return m_isDefault;
    }
}
