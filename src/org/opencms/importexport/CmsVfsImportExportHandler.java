/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsVfsImportExportHandler.java,v $
 * Date   : $Date: 2004/02/23 17:38:27 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.importexport;

import java.util.Arrays;
import java.util.List;

/**
 * Import/export handler implementation for VFS data.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/02/23 17:38:27 $
 * @since 5.3
 */
public class CmsVfsImportExportHandler extends Object implements I_CmsImportExportHandler {

    /** The type of this import/export handler.<p> */
    private String m_type;

    /** The name of the export file in the real file system.<p> */
    private String m_fileName;

    /** The VFS paths to be exported.<p> */
    private List m_exportPaths;

    /** Boolean flag to decide whether VFS resources under /system/ should be exported or not.<p> */
    private boolean m_excludeSystem;

    /** Boolean flag to decide whether unchanged resources should be exported or not.<p> */
    private boolean m_excludeUnchanged;

    /** Boolean flag to decide whether user/group data should be exported or not.<p> */
    private boolean m_exportUserdata;

    /** Timestamp to limit the resources to be exported by date.<p> */
    private long m_contentAge;

    /**
     * Creates a new VFS import/export handler.<p>
     */
    public CmsVfsImportExportHandler() {
        super();
        m_type = C_TYPE_VFSDATA;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getType()
     */
    public String getType() {
        return m_type;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_exportPaths != null) {
                m_exportPaths.clear();
            }
            m_exportPaths = null;
        } catch (Exception e) {
            // noop
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData()
     */
    public void exportData() {
        // not yet implemented
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData()
     */
    public void importData() {
        // not yet implemented
    }

    /**
     * Returns the timestamp to limit the resources to be exported by date.<p>
     * 
     * Only resources that have been modified after this date will be exported.<p>
     * 
     * @return the timestamp to limit the resources to be exported by date
     */
    public long getContentAge() {
        return m_contentAge;
    }

    /**
     * Returns the boolean flag to decide whether VFS resources under /system/ should be exported or not.<p>
     * 
     * @return true, if VFS resources under /system/ should not be exported
     */
    public boolean excludeSystem() {
        return m_excludeSystem;
    }

    /**
     * Returns the boolean flag to decide whether unchanged resources should be exported or not.<p>
     * 
     * @return true, if unchanged resources should not be exported
     */
    public boolean isExcludeUnchanged() {
        return m_excludeUnchanged;
    }

    /**
     * Returns the VFS paths to be exported.<p>
     * 
     * @return the VFS paths to be exported
     */
    public String[] getExportPaths() {
        return (String[]) m_exportPaths.toArray();
    }

    /**
     * Returns the VFS paths to be exported as a list.<p>
     * 
     * @return the VFS paths to be exported as a list
     */
    public List getExportPathsAsList() {
        return m_exportPaths;
    }

    /**
     * Returns the boolean flag to decide whether user/group data should be exported or not.<p>
     * 
     * @return true, if user/group data should be exported
     */
    public boolean isExportUserdata() {
        return m_exportUserdata;
    }

    /**
     * Returns the name of the export file in the real file system.<p>
     * 
     * @return the name of the export file in the real file system
     */
    public String getFileName() {
        return m_fileName;
    }

    /**
     * Sets the timestamp to limit the resources to be exported by date.<p>
     * 
     * Only resources that have been modified after this date will be exported.<p>
     * 
     * @param contentAge the timestamp to limit the resources to be exported by date
     */
    public void setContentAge(long contentAge) {
        m_contentAge = contentAge;
    }

    /**
     * Sets the boolean flag to decide whether VFS resources under /system/ should be exported or not.<p>
     * 
     * @param excludeSystem true, if VFS resources under /system/ should not be exported
     */
    public void setExcludeSystem(boolean excludeSystem) {
        m_excludeSystem = excludeSystem;
    }

    /**
     * Sets the boolean flag to decide whether unchanged resources should be exported or not.<p>
     * 
     * @param excludeUnchanged true, if unchanged resources should not be exported
     */
    public void setExcludeUnchanged(boolean excludeUnchanged) {
        m_excludeUnchanged = excludeUnchanged;
    }

    /**
     * Sets the VFS paths to be exported.<p>
     * 
     * @param exportPaths the VFS paths to be exported
     */
    public void setExportPaths(String[] exportPaths) {
        m_exportPaths = Arrays.asList(exportPaths);
    }

    /**
     * Sets the boolean flag to decide whether user/group data should be exported or not.<p>
     * 
     * @param exportUserdata true, if user/group data should not be exported
     */
    public void setExportUserdata(boolean exportUserdata) {
        m_exportUserdata = exportUserdata;
    }

    /**
     * Sets the name of the export file in the real file system.<p>
     * 
     * @param fileName the name of the export file in the real file system
     */
    public void setFileName(String fileName) {
        m_fileName = fileName;
    }

}
