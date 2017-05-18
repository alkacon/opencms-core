/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db;

import java.util.Set;

/**
 * Provides methods to write export points to the "real" file system.<p>
 */
public interface I_CmsExportPointDriver {

    /**
     * If required, creates the folder with the given root path in the real file system.<p>
     *
     * @param resourceName the root path of the folder to create
     * @param exportpoint the export point to create the folder in
     */
    void createFolder(String resourceName, String exportpoint);

    /**
     * Deletes a file or a folder in the real file sytem.<p>
     *
     * If the given resource name points to a folder, then this folder is only deleted if it is empty.
     * This is required since the same export point RFS target folder may be used by multiple export points.
     * For example, this is usually the case with the <code>/WEB-INF/classes/</code> and
     * <code>/WEB-INF/lib/</code> folders which are export point for multiple modules.
     * If all resources in the RFS target folder where deleted, uninstalling one module would delete the
     * export <code>classes</code> and <code>lib</code> resources of all other modules.<p>
     *
     * @param resourceName the root path of the resource to be deleted
     * @param exportpoint the name of the export point
     */
    void deleteResource(String resourceName, String exportpoint);

    /**
     * Returns the export point path for the given resource root path,
     * or <code>null</code> if the resource is not contained in
     * any export point.<p>
     *
     * @param rootPath the root path of a resource in the OpenCms VFS
     * @return the export point path for the given resource, or <code>null</code> if the resource is not contained in
     *      any export point
     */
    String getExportPoint(String rootPath);

    /**
     * Returns the set of all VFS paths that are exported as an export point.<p>
     *
     * @return the set of all VFS paths that are exported as an export point
     */
    Set<String> getExportPointPaths();

    /**
     * Writes the file with the given root path to the real file system.<p>
     *
     * If required, missing parent folders in the real file system are automatically created.<p>
     *
     * @param resourceName the root path of the file to write
     * @param exportpoint the export point to write file to
     * @param content the contents of the file to write
     */
    void writeFile(String resourceName, String exportpoint, byte[] content);

}