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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Helper class for creating a folder if it doesn'T already exist.<p>
 *
 * @since 8.0.0
 */
public class CmsLazyFolder {

    /** The folder, if it already exists. */
    private CmsResource m_folder;

    /** The path at which the folder should be created if it doesn'T exist. */
    private String m_path;

    /**
     * Initializes this object with an existing folder.<p>
     *
     * @param folder the existing folder
     */
    public CmsLazyFolder(CmsResource folder) {

        assert folder != null;
        m_folder = folder;
        m_path = null;
    }

    /**
     * Initializes this object with a path at which the folder should be created.<p>
     *
     * @param path the path at which the folder should be created
     */
    public CmsLazyFolder(String path) {

        assert path != null;
        m_path = path;
        m_folder = null;
    }

    /**
     * Creates the folder with the given name if it doesn't already exist, and returns it.<p>
     *
     * @param cms the current CMS context
     *
     * @return the created folder or the already existing folder
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource createFolder(CmsObject cms) throws CmsException {

        if (m_folder != null) {
            return m_folder;
        }
        return cms.createResource(
            m_path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.RESOURCE_TYPE_NAME).getTypeId());
    }

    /**
     * Returns the folder if it already exists, or null if it doesn't.<p>
     *
     * @param cms the current CMS context
     *
     * @return the folder if it exists, else null
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource getFolder(CmsObject cms) throws CmsException {

        if (m_folder != null) {
            return m_folder;
        }
        try {
            CmsResource folder = cms.readResource(m_path);
            return folder;
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns the folder if it already exists, or creates and returns it if it doesn't.<p>
     *
     * @param cms the current CMS context
     *
     * @return the folder
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource getOrCreateFolder(CmsObject cms) throws CmsException {

        CmsResource result = getFolder(cms);
        if (result != null) {
            return result;
        }
        return createFolder(cms);
    }

    /**
     * Returns the folder to check for permissions, which is either the folder itself if it already exists,
     * or the parent folder if it doesn't.<p>
     *
     * @param cms the current CMS context
     *
     * @return the folder to check for permissions
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource getPermissionCheckFolder(CmsObject cms) throws CmsException {

        CmsResource folder = getFolder(cms);
        if (folder != null) {
            return folder;
        }
        String parentPath = CmsResource.getParentFolder(m_path);
        CmsResource parent = cms.readResource(parentPath);
        return parent;
    }

    /**
     * Computes the site path of the folder, which is either the original path constructor argument, or the site
     * path of the original resource constructor argument.<p>
     *
     * @param cms the current CMS context
     * @return the site path of the lazy folder
     */
    public String getSitePath(CmsObject cms) {

        if (m_path != null) {
            return m_path;
        } else if (m_folder != null) {
            return cms.getRequestContext().removeSiteRoot(m_folder.getRootPath());
        }
        return null;
    }

}
