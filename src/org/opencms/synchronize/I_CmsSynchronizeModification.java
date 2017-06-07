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

package org.opencms.synchronize;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.io.File;

/**
 * Defines methods which can be pluged into the syncronisation process between VFS and "real" FS.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsSynchronizeModification {

    /**
     * Possibility to modify a resource after it has benn exported or updated
     * to the FS.<p>
     *
     * @param cms the current CmsObject
     * @param vfsRes the resource in the VFS
     * @param fsFile the resource in the FS
     * @throws CmsSynchronizeException if something goes wrong
     */
    void modifyFs(CmsObject cms, CmsResource vfsRes, File fsFile) throws CmsSynchronizeException;

    /**
     * Possibility to modify a resource after it has been imported or updated
     * into the VFS.<p>
     *
     * @param cms the current CmsObject
     * @param vfsRes the resource in the VFS
     * @param fsFile the resource in the FS
     * @throws CmsSynchronizeException if something goes wrong
     */
    void modifyVfs(CmsObject cms, CmsResource vfsRes, File fsFile) throws CmsSynchronizeException;

    /**
     * Translates the resource name.<p>
     *
     * This is nescessary since the server FS does allow different naming
     * conventions than the VFS.
     * If no special translation is required and the default OpenCms FS-VFS
     * translation should be used,
     * null must be returned.
     *
     * @param cms the current CmsObject
     * @param resName the resource name to be translated
     * @return the translated resource name or null
     * @throws CmsSynchronizeException if something goes wrong
     */
    String translate(CmsObject cms, String resName) throws CmsSynchronizeException;
}
