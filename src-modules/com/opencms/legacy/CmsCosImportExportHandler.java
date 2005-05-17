/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsCosImportExportHandler.java,v $
 * Date   : $Date: 2005/05/17 13:47:30 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
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
 
package com.opencms.legacy;

import org.opencms.file.CmsObject;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.importexport.I_CmsImportExportHandler;
import org.opencms.main.I_CmsConstants;
import org.opencms.report.I_CmsReport;
import org.opencms.xml.CmsXmlException;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Import/export handler implementation for COS data.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2005/05/17 13:47:30 $
 * @since 5.3
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsCosImportExportHandler extends Object implements I_CmsImportExportHandler {
    
    /** The description of this import/export handler.<p> */
    private String m_description;
    
    /** The name of the export file in the real file system.<p> */
    private String m_fileName;
    
    /** The COS channels to be exported.<p> */
    private List m_exportChannels; 
    
    /** The COS modules to be exported.<p> */
    private List m_exportModules;

    /**
     * Creates a new COS import/export handler.<p>
     */
    public CmsCosImportExportHandler() {
        super();
        m_description = C_DEFAULT_DESCRIPTION;
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_exportChannels != null) {
                m_exportChannels.clear();
            }
            m_exportChannels = null;
            
            if (m_exportModules != null) {
                m_exportModules.clear();
            }
            m_exportModules = null;            
        } catch (Exception e) {
            // noop
        } finally {
            super.finalize();
        }
    }    

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report) throws CmsImportExportException {
        report.println(report.key("report.export_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        new CmsExportModuledata(cms, getFileName(), getExportChannels(), getExportModules(), report);
        report.println(report.key("report.export_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     */
    public synchronized void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsXmlException, CmsImportExportException {
        report.println(report.key("report.import_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        CmsImportModuledata cosImport = new CmsImportModuledata(cms, importFile, importPath, report);
        cosImport.importResources();
        report.println(report.key("report.import_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
    }    

    /**
     * Returns the COS channels to be exported.<p>
     * 
     * @return the COS channels to be exported
     */
    public String[] getExportChannels() {
        return (String[]) m_exportChannels.toArray();
    }
    
    /**
     * Returns the COS channels to be exported as a list.<p>
     * 
     * @return the COS channels to be exported as a list
     */
    public List getExportChannelsAsList() {
        return m_exportChannels;
    }    

    /**
     * Returns the COS modules to be exported.<p>
     * 
     * @return the COS modules to be exported
     */
    public String[] getExportModules() {
        return (String[]) m_exportModules.toArray();
    }
    
    /**
     * Returns the COS modules to be exported as a list.<p>
     * 
     * @return the COS modules to be exported as a list
     */
    public List getExportModulesAsList() {
        return m_exportModules;
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
     * Sets the COS channels to be exported.<p>
     * 
     * @param exportChannels the COS channels to be exported
     */
    public void setExportChannels(String[] exportChannels) {
        m_exportChannels = Arrays.asList(exportChannels);
    }

    /**
     * Sets the COS modules to be exported.<p>
     * 
     * @param exportModules the COS modules to be exported
     */
    public void setExportModules(String[] exportModules) {
        m_exportModules = Arrays.asList(exportModules);
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
        
        boolean hasModuleExportNode = (I_CmsConstants.C_EXPORT_TAG_MODULEXPORT.equalsIgnoreCase(rootElement.getName()));
        boolean hasChannelNodes = (rootElement.selectNodes("./channels/file").size() > 0);

        return (hasModuleExportNode && hasChannelNodes);
    }
    
}

