/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleImportThread.java,v $
 * Date   : $Date: 2003/08/14 15:37:24 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.workplace;

import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.I_CmsRegistry;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;

import java.util.Vector;

/**
 * Imports a module, showing a progress indicator report dialog that is continuously updated.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.14 $
 * @since 5.0 rc 1
 */
public class CmsAdminModuleImportThread extends A_CmsReportThread {

    private String m_moduleName;
    private String m_zipName;
    private Vector m_conflictFiles;
    private I_CmsRegistry m_registry;
    private CmsObject m_cms;
    private I_CmsReport m_report;
    
    /** DEBUG flag */
    private static final boolean DEBUG = false;    

    /**
     * Creates the module import thread.
     * 
     * @param cms the current cms context  
     * @param reg the registry to write the new module information to
     * @param moduleName the name of the module 
     * @param zipName the name of the module ZIP file
     * @param conflictFiles vector of conflict files 
     */
    public CmsAdminModuleImportThread(CmsObject cms, I_CmsRegistry reg, String moduleName, String zipName, Vector conflictFiles) {
        super("OpenCms: Module import of " + moduleName);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_moduleName = moduleName;
        m_zipName = zipName;
        m_registry = reg;
        m_conflictFiles = conflictFiles;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        if (DEBUG) System.err.println("CmsAdminModuleImportThread() constructed"); 
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {            
            if (DEBUG) System.err.println("CmsAdminModuleImportThread() started");            
            String moduleName = m_moduleName.replace('\\', '/');
            CmsProject project = null;
            
            try {
                m_cms.getRequestContext().saveSiteRoot();
                m_cms.getRequestContext().setSiteRoot("/");
                // create a Project to import the module.
                project = m_cms.createProject(
                    "ImportModule", 
                    "A System generated project to import the module " + moduleName, 
                    OpenCms.getDefaultUsers().getGroupAdministrators(), 
                        OpenCms.getDefaultUsers().getGroupAdministrators(), 
                    I_CmsConstants.C_PROJECT_TYPE_TEMPORARY
                );
                m_cms.getRequestContext().setCurrentProject(project.getId());
                // copy the root folder to the project
                m_cms.copyResourceToProject("/");
            } finally {
                m_cms.getRequestContext().restoreSiteRoot();
            } 

            m_report.print(m_report.key("report.import_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(" <i>" + moduleName + "</i>", I_CmsReport.C_FORMAT_HEADLINE);
                       
            // import the module
            m_registry.importModule(m_zipName, m_conflictFiles, m_report);   

            m_report.println(m_report.key("report.publish_project_begin"), I_CmsReport.C_FORMAT_HEADLINE);            
            // now unlock and publish the project
            m_cms.unlockProject(project.getId());
            m_cms.publishProject(m_report);                

            m_report.println(m_report.key("report.publish_project_end"), I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(m_report.key("report.import_module_end"), I_CmsReport.C_FORMAT_HEADLINE);
            
            if (DEBUG) System.err.println("CmsAdminModuleImportThread() finished");          
        } catch (CmsException e) {
            m_report.println(e);
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
            if (DEBUG) System.err.println("CmsAdminModuleImportThread() Exception:" + e.getMessage());            
        }
    }

    /**
     * Returns the part of the report that is ready.
     * 
     * @return the part of the report that is ready
     */
    public String getReportUpdate() {
        return m_report.getReportUpdate();
    }
}
