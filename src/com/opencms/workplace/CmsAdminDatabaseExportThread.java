/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabaseExportThread.java,v $
* Date   : $Date: 2003/03/22 07:24:54 $
* Version: $Revision: 1.22 $
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
import com.opencms.file.CmsObject;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;

/**
 * Exports the database, showing a progress indicator report dialog that is continuously updated.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Hanjo Riege 
 * 
 * @version $Revision: 1.22 $
 */
public class CmsAdminDatabaseExportThread extends A_CmsReportThread {

    private CmsObject m_cms;
    private String m_fileName;
    private String[] m_exportPaths;
    private String[] m_exportModules;
    private boolean m_excludeSystem;
    private boolean m_excludeUnchanged;
    private boolean m_exportUserdata;
    private boolean m_moduledataExport;
    private long m_contentAge;
    private I_CmsReport m_report;

    /**
     * Export the VFS (Virtual File System) resources.<p>
     */
    public CmsAdminDatabaseExportThread(
        CmsObject cms, 
        String fileName,
        String[] exportPaths, 
        boolean excludeSystem, 
        boolean excludeUnchanged,
        boolean exportUserdata, 
        long contentAge
    ) {        
        m_cms = cms;
        m_exportPaths = exportPaths;
        m_fileName = fileName;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_contentAge = contentAge;
        String locale  = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        m_moduledataExport = false;
    }

    /**
     * Export the COS (Content Object Store) resources and the module data.<p>
     */
    public CmsAdminDatabaseExportThread(
        CmsObject cms, 
        String fileName,
        String[] exportChannels, 
        String[] exportModules
    ) {        
        m_cms = cms;    
        m_exportPaths = exportChannels;
        m_exportModules = exportModules;
        m_fileName = fileName;
        String locale  = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);        
        m_moduledataExport = true;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            // do the export
            m_report.println(m_report.key("report.export_db_begin"), I_CmsReport.C_FORMAT_HEADLINE); 
            if(m_moduledataExport){
                m_cms.exportModuledata(m_fileName, m_exportPaths, m_exportModules, m_report);
            } else {
                m_cms.exportResources(m_fileName, m_exportPaths, m_excludeSystem, m_excludeUnchanged, m_exportUserdata, m_contentAge, m_report);
            }
            m_report.println(m_report.key("report.export_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
        }
        catch(CmsException e) {
            m_report.println(e);
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }

    /**
     * Returns the part of the report that is ready.<p>
     */
    public String getReportUpdate(){
        return m_report.getReportUpdate();
    }
}
