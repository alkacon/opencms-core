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

package org.opencms.workplace.threads;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Settings object that provides the settings to repair XML content resources in the OpenCms virtual file system (VFS).<p>
 *
 * @since 6.2.0
 */
public final class CmsXmlContentRepairSettings {

    /** Needed to verify if a VFS path String denotes a folder in VFS. */
    private final CmsObject m_cms;

    /** Flag indicating if to force the reparation. */
    private boolean m_force;

    /** Flag indicating if resources in sub folders should be repaired, too. */
    private boolean m_includeSubFolders;

    /** The resource type of the XML contents to process. */
    private String m_resourceType;

    /** The VFS folder of all XML content files to process. */
    private String m_vfsFolder;

    /**
     * Default constructor with cms object that is used for VFS path validation.<p>
     *
     * @param cms the current users context to check the VFS path information
     */
    public CmsXmlContentRepairSettings(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Returns the resource type name of the XML contents to process.<p>
     *
     * @return the resource type name of the XML contents to process
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the VFS folder under which XML contents will be processed recursively.<p>
     *
     * @return the VFS folder under which XML contents will be processed recursively
     */
    public String getVfsFolder() {

        return m_vfsFolder;
    }

    /**
     * Checks if to force the reparation.<p>
     *
     * @return <code>true</code> if to force the reparation
     */
    public boolean isForce() {

        return m_force;
    }

    /**
     * Returns the flag indicating if resources in sub folders should be repaired, too.<p>
     *
     * @return the flag indicating if resources in sub folders should be repaired, too
     */
    public boolean isIncludeSubFolders() {

        return m_includeSubFolders;
    }

    /**
     * Sets the force reparation flag.<p>
     *
     * @param force the force reparation flag to set
     */
    public void setForce(boolean force) {

        m_force = force;
    }

    /**
     * Sets the flag indicating if resources in sub folders should be repaired, too.<p>
     *
     * @param includeSubFolders the flag indicating if resources in sub folders should be repaired, too
     */
    public void setIncludeSubFolders(boolean includeSubFolders) {

        m_includeSubFolders = includeSubFolders;
    }

    /**
     * Sets the resource type name of the XML contents to process.<p>
     *
     * @param resourceType the resource type name of the XML contents to process
     */
    public void setResourceType(String resourceType) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourceType)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_VALUE_EMPTY_0));
        }
        m_resourceType = resourceType;
    }

    /**
     * Sets the VFS folder under which XML contents will be processed recursively.<p>
     *
     * @param vfsFolder the VFS folder under which XML contents will be processed recursively
     *
     * @throws CmsIllegalArgumentException if the given VFS path is not valid
     */
    public void setVfsFolder(String vfsFolder) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(vfsFolder)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_VALUE_EMPTY_0));
        }
        // test if it is a valid path
        if (!m_cms.existsResource(vfsFolder, CmsResourceFilter.ALL.addRequireFolder())) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XMLCONTENT_VFSFOLDER_1, vfsFolder));
        }
        m_vfsFolder = vfsFolder;
    }

    /**
     * Returns the resource type ID of the XML contents to process.<p>
     *
     * @return the resource type ID of the XML contents to process
     */
    protected int getResourceTypeId() {

        if (CmsStringUtil.isNotEmpty(getResourceType())) {
            try {
                return OpenCms.getResourceManager().getResourceType(getResourceType()).getTypeId();
            } catch (CmsException e) {
                // ignore, no valid resource type ID found
            }
        }
        return -1;
    }
}
