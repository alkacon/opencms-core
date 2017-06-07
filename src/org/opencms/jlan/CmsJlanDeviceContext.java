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

package org.opencms.jlan;

import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.FileSystem;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;

/**
 * OpenCms implementation of the JLAN device context.<p>
 *
 * For now, it just contains a reference to the corresponding CmsJLanRepository interface.<p>
 */
public class CmsJlanDeviceContext extends DiskDeviceContext {

    /** The repository to which this device context belongs. */
    private CmsJlanRepository m_repository;

    /**
     * Creates a new device context instance.<p>
     *
     * @param repo the repository for which this is a device context
     */
    public CmsJlanDeviceContext(CmsJlanRepository repo) {

        super(repo.getName());
        m_repository = repo;
        setFilesystemAttributes(
            FileSystem.CasePreservedNames | FileSystem.UnicodeOnDisk | FileSystem.CaseSensitiveSearch);

        // Need to set the disk information, even with arbitrary numbers, because not setting it will cause
        // a hanging dialog in Windows 7 when creating a copy of a file on the network share in the same folder
        SrvDiskInfo diskInfo = new SrvDiskInfo(2560000, 64, 512, 2304000);
        setDiskInformation(diskInfo);
    }

    /**
     * Gets the repository to which this device context belongs.<p>
     *
     * @return the repository to which this device context belongs
     */
    public CmsJlanRepository getRepository() {

        return m_repository;
    }

}
