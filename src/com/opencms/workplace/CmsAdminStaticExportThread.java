/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminStaticExportThread.java,v $
* Date   : $Date: 2003/07/22 00:29:22 $
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
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;

/**
 * A thread to export resources into the physical file system.
 * 
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsAdminStaticExportThread extends A_CmsReportThread {

    private CmsObject m_cms;


    // the object to send the information to the workplace.
    private CmsHtmlReport m_report;

    public CmsAdminStaticExportThread(CmsObject cms, I_CmsSession session) {
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        String errormessage = "Error exporting resources:<br>";
        try {
            // start the export
            m_cms.exportStaticResources(CmsObject.getStaticExportProperties().getStartPoints(), null, null, null, m_report);

        }catch(CmsException e){
            errormessage += " "+e.getTypeText() +" "+e.getMessage();
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL,"error in static export "+e.getMessage());
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