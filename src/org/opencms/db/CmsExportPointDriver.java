/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsExportPointDriver.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.18 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides methods to write export points to the "real" file system.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.18 $
 * 
 * @since 6.0.0
 */
public class CmsExportPointDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExportPointDriver.class);

    /** The export points resolved to a lookup map. */
    private HashMap m_exportpointLookupMap;

    /** The configured export points. */
    private Set m_exportpoints;

    /**
     * Constructor for a CmsExportPointDriver.<p>
     *
     * @param exportpoints the list of export points
     */
    public CmsExportPointDriver(Set exportpoints) {

        m_exportpoints = exportpoints;
        m_exportpointLookupMap = new HashMap();
        Iterator i = m_exportpoints.iterator();
        while (i.hasNext()) {
            CmsExportPoint point = (CmsExportPoint)i.next();
            if (point.getDestinationPath() != null) {
                // otherwise this point is not valid, but must be kept for serializing the configuration
                m_exportpointLookupMap.put(point.getUri(), point.getDestinationPath());
            }
        }
    }

    /**
     * Creates a new folder in the real file system.<p>
     *
     * @param foldername the complete path to the folder
     * @param exportpoint the name of the export point
     */
    public void createFolder(String foldername, String exportpoint) {

        File discFolder = new File(absoluteName(foldername, exportpoint));
        if (!discFolder.exists()) {
            boolean success = discFolder.mkdirs();
            if (LOG.isWarnEnabled() && (!success)) {
                LOG.warn(Messages.get().key(Messages.LOG_CREATE_FOLDER_FAILED_1, absoluteName(foldername, exportpoint)));
            }
        }
    }

    /**
     * Returns the export point path of the given resource,
     * or <code>null</code> if the resource is not contained in 
     * any export point.<p>
     *
     * @param filename the uri of a resource in the OpenCms VFS
     * @return the matching export points path or <code>null</code> if no export point matches
     */
    public String getExportPoint(String filename) {

        Iterator i = getExportPointPaths().iterator();
        while (i.hasNext()) {
            String point = (String)i.next();
            if (filename.startsWith(point)) {
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
    public Set getExportPointPaths() {

        return m_exportpointLookupMap.keySet();
    }

    /**
     * Deletes a file (or folder) in the real file sytem.<p>
     *
     * @param resourcename the complete path to the resource to be deleted
     * @param exportpoint the name of the export point
     */
    public void removeResource(String resourcename, String exportpoint) {

        File discFile = new File(absoluteName(resourcename, exportpoint));
        if (discFile.exists()) {
            discFile.delete();
        }
    }

    /**
     * Writes a file with the given content to the real file system.<p>
     *
     * @param filename the path of the file to write
     * @param exportpoint the name of the export point
     * @param content the contents of the file to write
     */
    public void writeFile(String filename, String exportpoint, byte[] content) {

        File discFile = new File(absoluteName(filename, exportpoint));
        try {
            OutputStream s = new FileOutputStream(discFile);
            s.write(content);
            s.close();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_WRITE_EXPORT_POINT_FAILED_1, discFile.getAbsolutePath()), e);
            }
        }
    }

    /**
     * Returns the absolute path of an export point in the real file system.<p>
     *
     * @param filename name of a file in the VFS
     * @param exportpoint the name of the export point
     * @return the absolute path of an export point in the real file system
     */
    private String absoluteName(String filename, String exportpoint) {

        StringBuffer exportpath = new StringBuffer(128);
        exportpath.append((String)m_exportpointLookupMap.get(exportpoint));
        exportpath.append(filename.substring(exportpoint.length()));
        return exportpath.toString();
    }
}
