/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleExportThread.java,v $
 * Date   : $Date: 2003/08/30 11:30:08 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2001  The OpenCms Group
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.workplace;

import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRegistry;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;

/**
 * Exports a module, showing a progress indicator report dialog that is continuously updated.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.9 $
 * @since 5.0 rc 1
 */
public class CmsAdminModuleExportThread extends A_CmsReportThread {

    private String m_moduleName;
    private CmsRegistry m_registry;
    private CmsObject m_cms;
    private String[] m_resources;
    private String m_filename;
    private I_CmsReport m_report;

    /** DEBUG flag */
    private static final boolean DEBUG = false;

    /**
     * Creates the module delete thread.
     *
     * @param cms the current cms context
     * @param reg the registry to write the new module information to
     * @param moduleName the name of the module
     * @param conflictFiles vector of conflict files
     * @param exclusion vector of files to exclude
     * @param projectFiles vector of project files
     */
    public CmsAdminModuleExportThread(CmsObject cms, CmsRegistry reg, String moduleName, String[] resources, String filename) {
        super("OpenCms: Module export of " + moduleName);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_moduleName = moduleName;
        m_registry = reg;
        m_resources = resources;
        m_filename = filename;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        if (DEBUG) System.err.println("CmsAdminModuleExportThread() constructed");
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            if (DEBUG) System.err.println("CmsAdminModuleExportThread() started");
            String moduleName = m_moduleName.replace('\\', '/');

            m_report.print(m_report.key("report.export_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(" <i>" + moduleName + "</i>", I_CmsReport.C_FORMAT_HEADLINE);

            // export the module
            m_registry.exportModule(m_moduleName, m_resources, m_filename, m_report);

            m_report.println(m_report.key("report.export_module_end"), I_CmsReport.C_FORMAT_HEADLINE);

            if (DEBUG) System.err.println("CmsAdminModuleExportThread() finished");
        }
        catch(CmsException e) {
            m_report.println(e);
            if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
            if (DEBUG) System.err.println("CmsAdminModuleExportThread() Exception:" + e.getMessage());
        }
    }

    /**
     * Returns the part of the report that is ready.
     *
     * @return the part of the report that is ready
     */
    public String getReportUpdate(){
        return m_report.getReportUpdate();
    }
}