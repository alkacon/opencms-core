/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

/**
 * A bean which can either represent a folder or a relative folder name, but not both at the same time.<p>
 *
 * @author Georg Westenberger
 *
 * @version $Revision: 1.0$
 *
 * @since 8.0.0
 */
public class CmsFolderOrName {

    /** The folder name. */
    private String m_folderName;

    /** The folder resource's structure id. */
    private CmsResource m_folder;

    /** The base path which the folder name is relative to. */
    private String m_basePath;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFolderOrName.class);

    /**
     * Creates an instance based on an existing folder.<p>
     *
     * @param folder the folder
     */
    public CmsFolderOrName(CmsResource folder) {

        m_folder = folder;
    }

    /**
     * Creates an instance based on a relative folder name.<p>
     *
     * @param basePath the base path which the folder name is relative to
     * @param name the relative folder name
     */
    public CmsFolderOrName(String basePath, String name) {

        m_basePath = basePath;

        m_folderName = name;
    }

    /**
     * Gets the base path.<p>
     *
     * @return the base path
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Gets the folder.<p>
     *
     * @return the folder
     */
    public CmsResource getFolder() {

        return m_folder;
    }

    /**
     * Gets the relative folder name if available, else null.<p>
     *
     * @return the relative folder name null
     */
    public String getFolderName() {

        return m_folderName;
    }

    /**
     * Computes the folder root path.<p>
     *
     * @param cms the CMS context to use
     * @return the folder root path
     */
    public String getFolderPath(CmsObject cms) {

        if (m_folder != null) {
            try {
                return OpenCms.getADEManager().getRootPath(
                    m_folder.getStructureId(),
                    cms.getRequestContext().getCurrentProject().isOnlineProject());
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return m_folder.getRootPath();
            }
        } else {
            if (m_basePath != null) {
                return CmsStringUtil.joinPaths(m_basePath, m_folderName);
            } else {
                return CmsStringUtil.joinPaths(
                    cms.getRequestContext().getSiteRoot(),
                    CmsADEManager.CONTENT_FOLDER_NAME,
                    m_folderName);
            }
        }
    }

    /**
     * Returns true if the current instance was created with a folder structure id parameter.<p>
     *
     * @return true if this instance was created with a folder structure id parameter
     */
    public boolean isFolder() {

        return m_folder != null;
    }

    /**
     * Returns true if this instance was created with a folder name parameter.<p>
     *
     * @return true if this instance was created with a folder name parameter
     */
    public boolean isName() {

        return m_folderName != null;
    }

}
