/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabaseExportThread.java,v $
* Date   : $Date: 2003/02/21 15:18:23 $
* Version: $Revision: 1.21 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;
import com.opencms.util.Utils;

/**
 * Thread for create a new project.
 * Creation date: (13.10.00 14:39:20)
 * @author Hanjo Riege
 */

public class CmsAdminDatabaseExportThread extends A_CmsReportThread {

    private CmsObject m_cms;

    private String m_fileName;

    private String[] m_exportPaths;

    private String[] m_exportModules;

    private boolean m_excludeSystem;

    private boolean m_excludeUnchanged;

    private boolean m_exportUserdata;

    private I_CmsSession m_session;

    private boolean m_moduledataExport;

    private long m_contentAge;

    // the object to send the information to the workplace.
    private I_CmsReport m_report;

    /**
     * Export the VFS (Virtual File System) resources.
     */
    public CmsAdminDatabaseExportThread(CmsObject cms, String fileName,
            String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged,
            boolean exportUserdata, 
            long contentAge,
            I_CmsSession session) {
        m_cms = cms;
        m_exportPaths = exportPaths;
        m_fileName = fileName;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_contentAge = contentAge;
        m_session = session;
        String locale  = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        m_moduledataExport = false;
    }

    /**
     * Export the COS (Content Object Store) resources, ie. the module data
     */

    public CmsAdminDatabaseExportThread(CmsObject cms, String fileName,
            String[] exportChannels, String[] exportModules, I_CmsSession session) {
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);        
        m_exportPaths = exportChannels;
        m_exportModules = exportModules;
        m_fileName = fileName;
        m_session = session;
        m_moduledataExport = true;
    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {
            // do the export
            if(m_moduledataExport){
                m_cms.exportModuledata(m_fileName, m_exportPaths, m_exportModules);
            } else {
                m_report.println(m_report.key("report.export_db_begin"), I_CmsReport.C_FORMAT_HEADLINE); 
                m_cms.exportResources(m_fileName, m_exportPaths, m_excludeSystem, m_excludeUnchanged, m_exportUserdata, m_contentAge, m_report);
                m_report.println(m_report.key("report.export_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }
        }
        catch(CmsException e) {
            m_report.println(e);
            m_session.putValue(I_CmsConstants.C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }

    /**
     * returns the part of the report that is ready.
     */
    public String getReportUpdate(){
        return m_report.getReportUpdate();
    }
}
