/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabaseImportThread.java,v $
* Date   : $Date: 2003/07/23 09:58:55 $
* Version: $Revision: 1.20 $
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
import com.opencms.file.CmsObject;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;

/**
 * Imports the database, showing a progress indicator report dialog that is continuously updated.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Hanjo Riege 
 * 
 * @version $Revision: 1.20 $
 */
public class CmsAdminDatabaseImportThread extends A_CmsReportThread {

    private String m_existingFile;
    private CmsObject m_cms;
    private I_CmsReport m_report;

    /**
     * Imports the database.<p>
     */
    public CmsAdminDatabaseImportThread(
        CmsObject cms, 
        String existingFile
    ) {
        super("OpenCms: Database import from " + existingFile);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_existingFile = existingFile;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            m_report.println(m_report.key("report.import_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);            
            m_cms.importResources(m_existingFile, I_CmsConstants.C_ROOT, m_report);
            m_report.println(m_report.key("report.import_db_end"), I_CmsReport.C_FORMAT_HEADLINE);            
        }
        catch(CmsException e) {
            m_report.println(e);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
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
