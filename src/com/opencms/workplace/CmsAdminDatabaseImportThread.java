/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabaseImportThread.java,v $
* Date   : $Date: 2003/01/30 19:36:49 $
* Version: $Revision: 1.16 $
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
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;
import com.opencms.util.Utils;

/**
 * Thread for Import.
 * Creation date: (13.10.00 14:39:20)
 * @author Hanjo Riege
 */

public class CmsAdminDatabaseImportThread extends Thread implements I_CmsConstants {

    private String m_existingFile;

    private CmsObject m_cms;

    private I_CmsReport m_report;

    private I_CmsSession m_session;

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */

    public CmsAdminDatabaseImportThread(CmsObject cms, String existingFile, I_CmsSession session) {
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);        
        m_existingFile = existingFile;
        m_session = session;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {

            // import the database
            m_report.println(m_report.key("report.import_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);            
            m_cms.importResources(m_existingFile, C_ROOT, m_report);
            m_report.println(m_report.key("report.import_db_end"), I_CmsReport.C_FORMAT_HEADLINE);            
        }
        catch(CmsException e) {
            m_report.println(e);
            m_session.putValue(C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
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
