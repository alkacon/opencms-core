/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsVfsImportExportHandler.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Import/export handler implementation for VFS data.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.5 $ $Date: 2005/02/17 12:43:47 $
 * @since 5.3
 */
public class CmsVfsImportExportHandler extends Object implements I_CmsImportExportHandler {
    
    /** The description of this import/export handler.<p> */
    private String m_description;    

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
        m_description = C_DEFAULT_DESCRIPTION;
        m_excludeSystem = true;
        m_excludeUnchanged = false;
        m_exportUserdata = true;
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
        } finally {
            super.finalize();
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report) throws CmsException {
        report.println(report.key("report.export_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);        
        new CmsExport(cms, getFileName(), getExportPaths(), excludeSystem(), isExcludeUnchanged(), null, isExportUserdata(), getContentAge(), report);        
        report.println(report.key("report.export_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     */
    public synchronized void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException {
        report.println(report.key("report.import_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        CmsImport vfsImport = new CmsImport(cms, importFile, importPath, report);
        vfsImport.importResources();
        report.println(report.key("report.import_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
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

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getDescription()
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        m_description = description;
    } 
    
    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#matches(org.dom4j.Document)
     */
    public boolean matches(Document manifest) {
        Element rootElement = manifest.getRootElement();
        
        boolean hasModuleNode = (rootElement.selectNodes("./module/name").size() > 0);
        boolean hasFileNodes = (rootElement.selectNodes("./files/file").size() > 0);
        
        return (!hasModuleNode && hasFileNodes);
    }    

}
