/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsModuleDeleteThread.java,v $
 * Date   : $Date: 2003/09/05 12:22:25 $
 * Version: $Revision: 1.1 $
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

package org.opencms.threads;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.I_CmsReport;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRegistry;
import com.opencms.workplace.CmsXmlLanguageFile;

import java.util.Vector;

/**
 * Deletes a module.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.1.10
 */
public class CmsModuleDeleteThread extends A_CmsReportThread {

    private static final boolean DEBUG = false;
    private CmsObject m_cms;
    private Vector m_conflictFiles;
    private String m_moduleName;
    private Vector m_projectFiles;
    private CmsRegistry m_registry;
    private boolean m_replaceMode;

    /**
     * Creates the module delete thread.<p>
     *
     * @param cms the current cms context
     * @param reg the registry to write the new module information to
     * @param moduleName the name of the module
     * @param conflictFiles vector of conflict files
     * @param projectFiles vector of project files
     * @param replaceMode the replace mode
     */
    public CmsModuleDeleteThread(
        CmsObject cms, 
        CmsRegistry reg, 
        String moduleName, 
        Vector conflictFiles, 
        Vector projectFiles, 
        boolean replaceMode
    ) {
        super("OpenCms: Module deletion of " + moduleName);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_moduleName = moduleName;
        m_registry = reg;
        m_conflictFiles = conflictFiles;
        m_projectFiles = projectFiles;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        m_replaceMode = replaceMode;
        if (DEBUG) {
            System.err.println("CmsAdminModuleDeleteThread() constructed");
        }
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        return m_report.getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            if (DEBUG) {
                System.err.println("CmsAdminModuleDeleteThread() started");
            }
            String moduleName = m_moduleName.replace('\\', '/');
            CmsProject project = null;

            // create a Project to delete the module.
            project = m_cms.createProject("DeleteModule", "A System generated project to delete the module " + moduleName, OpenCms.getDefaultUsers().getGroupAdministrators(), OpenCms.getDefaultUsers().getGroupAdministrators(), I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
            m_cms.getRequestContext().setCurrentProject(project.getId());

            m_report.print(m_report.key("report.delete_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(" <i>" + moduleName + "</i>", I_CmsReport.C_FORMAT_HEADLINE);

            // copy the resources to the project
            for (int i = 0; i < m_projectFiles.size(); i++) {
                m_cms.copyResourceToProject((String)m_projectFiles.elementAt(i));
            }
            // delete the module
            m_registry.deleteModule(m_moduleName, m_conflictFiles, m_replaceMode, m_report);

            m_report.println(m_report.key("report.publish_project_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            // now unlock and publish the project
            m_cms.unlockProject(project.getId());
            m_cms.publishProject(m_report);

            m_report.println(m_report.key("report.publish_project_end"), I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(m_report.key("report.delete_module_end"), I_CmsReport.C_FORMAT_HEADLINE);

            if (DEBUG) {
                System.err.println("CmsAdminModuleDeleteThread() finished");
            }
        } catch (CmsException e) {
            m_report.println(e);
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
            if (DEBUG) {
                System.err.println("CmsAdminModuleDeleteThread() Exception:" + e.getMessage());
            }
        }
    }
}