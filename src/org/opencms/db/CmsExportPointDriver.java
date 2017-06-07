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

package org.opencms.db;

import org.opencms.main.CmsLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides methods to write export points to the "real" file system.<p>
 *
 * @since 6.0.0
 */
public class CmsExportPointDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExportPointDriver.class);

    /** The export points resolved to a lookup map. */
    private Map<String, String> m_exportpointLookupMap;

    /** The configured export points. */
    private Set<CmsExportPoint> m_exportpoints;

    /**
     * Constructor for a CmsExportPointDriver.<p>
     *
     * @param exportpoints the list of export points
     */
    public CmsExportPointDriver(Set<CmsExportPoint> exportpoints) {

        m_exportpoints = exportpoints;
        m_exportpointLookupMap = new HashMap<String, String>();
        Iterator<CmsExportPoint> i = m_exportpoints.iterator();
        while (i.hasNext()) {
            CmsExportPoint point = i.next();
            if (point.getDestinationPath() != null) {
                // otherwise this point is not valid, but must be kept for serializing the configuration
                m_exportpointLookupMap.put(point.getUri(), point.getDestinationPath());
            }
        }
    }

    /**
     * If required, creates the folder with the given root path in the real file system.<p>
     *
     * @param resourceName the root path of the folder to create
     * @param exportpoint the export point to create the folder in
     */
    public void createFolder(String resourceName, String exportpoint) {

        writeResource(resourceName, exportpoint, null);
    }

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
    public void deleteResource(String resourceName, String exportpoint) {

        File file = getExportPointFile(resourceName, exportpoint);
        if (file.exists() && file.canWrite()) {
            // delete the file (or folder)
            file.delete();
            // also delete empty parent directories
            File parent = file.getParentFile();
            if (parent.canWrite()) {
                parent.delete();
            }
        }
    }

    /**
     * Returns the export point path for the given resource root path,
     * or <code>null</code> if the resource is not contained in
     * any export point.<p>
     *
     * @param rootPath the root path of a resource in the OpenCms VFS
     * @return the export point path for the given resource, or <code>null</code> if the resource is not contained in
     *      any export point
     */
    public String getExportPoint(String rootPath) {

        Iterator<String> i = getExportPointPaths().iterator();
        while (i.hasNext()) {
            String point = i.next();
            if (rootPath.startsWith(point)) {
                return point;
            }
        }
        return null;
    }

    /**
     * Returns the set of all VFS paths that are exported as an export point.<p>
     *
     * @return the set of all VFS paths that are exported as an export point
     */
    public Set<String> getExportPointPaths() {

        return m_exportpointLookupMap.keySet();
    }

    /**
     * Writes the file with the given root path to the real file system.<p>
     *
     * If required, missing parent folders in the real file system are automatically created.<p>
     *
     * @param resourceName the root path of the file to write
     * @param exportpoint the export point to write file to
     * @param content the contents of the file to write
     */
    public void writeFile(String resourceName, String exportpoint, byte[] content) {

        writeResource(resourceName, exportpoint, content);
    }

    /**
     * Returns the File for the given export point resource.<p>
     *
     * @param rootPath name of a file in the VFS
     * @param exportpoint the name of the export point
     * @return the File for the given export point resource
     */
    private File getExportPointFile(String rootPath, String exportpoint) {

        StringBuffer exportpath = new StringBuffer(128);
        exportpath.append(m_exportpointLookupMap.get(exportpoint));
        exportpath.append(rootPath.substring(exportpoint.length()));
        return new File(exportpath.toString());
    }

    /**
     * Writes (if required creates) a resource with the given name to the real file system.<p>
     *
     * @param resourceName the root path of the resource to write
     * @param exportpoint the name of the export point
     * @param content the contents of the file to write
     */
    private void writeResource(String resourceName, String exportpoint, byte[] content) {

        File file = getExportPointFile(resourceName, exportpoint);
        try {
            File folder;
            if (content == null) {
                // a folder is to be created
                folder = file;
            } else {
                // a file is to be written
                folder = file.getParentFile();
            }
            // make sure the parent folder exists
            if (!folder.exists()) {
                boolean success = folder.mkdirs();
                if (!success) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_CREATE_FOLDER_FAILED_1, folder.getAbsolutePath()));
                }
            }
            if (content != null) {
                // now write the file to the real file system
                OutputStream s = new FileOutputStream(file);
                s.write(content);
                s.close();
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_WRITE_EXPORT_POINT_FAILED_1, file.getAbsolutePath()),
                e);
        }
    }
}