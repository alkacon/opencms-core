/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsModuleDeleteThread.java,v $
 * Date   : $Date: 2003/09/19 14:42:52 $
 * Version: $Revision: 1.7 $
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
import org.opencms.report.I_CmsReport;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRegistry;

import java.util.Vector;

/**
 * Deletes a module.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.1.10
 */
public class CmsModuleDeleteThread extends A_CmsReportThread {

    private static final boolean DEBUG = false;
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
        super(cms, "OpenCms: Module deletion of " + moduleName);
        m_moduleName = moduleName;
        m_registry = reg;
        m_conflictFiles = conflictFiles;
        m_projectFiles = projectFiles;
        m_replaceMode = replaceMode;
        initHtmlReport();        
        if (DEBUG) {
            System.err.println("CmsAdminModuleDeleteThread() constructed");
        }
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        return getReport().getReportUpdate();
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
            project = getCms().createProject("DeleteModule", "A System generated project to delete the module " + moduleName, OpenCms.getDefaultUsers().getGroupAdministrators(), OpenCms.getDefaultUsers().getGroupAdministrators(), I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
            getCms().getRequestContext().setCurrentProject(project.getId());

            getReport().print(getReport().key("report.delete_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            getReport().println(" <i>" + moduleName + "</i>", I_CmsReport.C_FORMAT_HEADLINE);

            // copy the resources to the project
            for (int i = 0; i < m_projectFiles.size(); i++) {
                getCms().copyResourceToProject((String)m_projectFiles.elementAt(i));
            }
            // delete the module
            m_registry.deleteModule(m_moduleName, m_conflictFiles, m_replaceMode, getReport());

            getReport().println(getReport().key("report.publish_project_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            // now unlock and publish the project
            getCms().unlockProject(project.getId());
            getCms().publishProject(getReport());

            getReport().println(getReport().key("report.publish_project_end"), I_CmsReport.C_FORMAT_HEADLINE);
            getReport().println(getReport().key("report.delete_module_end"), I_CmsReport.C_FORMAT_HEADLINE);

            if (DEBUG) {
                System.err.println("CmsAdminModuleDeleteThread() finished");
            }
        } catch (CmsException e) {
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error deleting module " + m_moduleName, e);
            }
            if (DEBUG) {
                System.err.println("CmsAdminModuleDeleteThread() Exception:" + e.getMessage());
            }
        }
    }
}