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

package org.opencms.setup;

import org.opencms.module.CmsModuleVersion;

/**
 * Conveniently stores information necessary during the update.
 */
public class CmsUpdateInfo {

    /** The single instance of this class. */
    public static CmsUpdateInfo INSTANCE = new CmsUpdateInfo();

    /** The containerpage editor version or null. */
    private CmsModuleVersion m_adeModuleVersion;

    /**
     * Gets the container page editor version.<p>
     *
     * @return the module version
     */
    public CmsModuleVersion getAdeModuleVersion() {

        return m_adeModuleVersion;
    }

    /**
     * Checks if the categoryfolder setting needs to be updated.
     *
     * @return true if the categoryfolder setting needs to be updated
     */
    public boolean needToSetCategoryFolder() {

        if (m_adeModuleVersion == null) {
            return true;
        }
        CmsModuleVersion categoryFolderUpdateVersion = new CmsModuleVersion("9.0.0");
        return (m_adeModuleVersion.compareTo(categoryFolderUpdateVersion) == -1);
    }

    /**
     * Sets the container page editor version.<p>
     *
     * @param version the module version
     */
    public void setAdeModuleVersion(CmsModuleVersion version) {

        m_adeModuleVersion = version;
    }

}
