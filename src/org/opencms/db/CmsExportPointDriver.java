/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsExportPointDriver.java,v $
 * Date   : $Date: 2003/11/14 10:09:12 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.OpenCms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Provides methods to write export points to the "real" file system.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 */
public class CmsExportPointDriver {

    /** The configured exportpoints */
    private Hashtable m_exportpointStorage = null;

    /**
     * Constructor for a CmsExportPointDriver.<p>
     *
     * @param exportpoints the vfs directories to export
     */
    public CmsExportPointDriver(Hashtable exportpoints) {
        m_exportpointStorage = exportpoints;
    }
    
    /**
     * Returns the absolute path of an export point in the real file system.<p>
     *
     * @param filename name of a file in the VFS
     * @param exportpoint the name of the export point
     * @return the absolute path of an export point in the real file system
     */
    private String absoluteName(String filename, String exportpoint) {
        String exportpath = (String)m_exportpointStorage.get(exportpoint);
        return exportpath + filename.substring(exportpoint.length());
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
            if (OpenCms.getLog(this).isWarnEnabled() && (!success)) {
                OpenCms.getLog(this).warn("Couldn't create folder " + absoluteName(foldername, exportpoint));
            }
        }
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
            // do nothing        
        }
        content = null;
    }
}
