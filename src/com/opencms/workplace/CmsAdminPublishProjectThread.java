/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminPublishProjectThread.java,v $
* Date   : $Date: 2002/12/06 23:16:47 $
* Version: $Revision: 1.10 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsLogChannels;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.report.CmsReport;

/**
 * Thread for publishing a project.
 * Creation date: (13.10.00 14:39:20)
 * @author: Hanjo Riege
 */

public class CmsAdminPublishProjectThread extends Thread implements I_CmsConstants {

    private int m_projectId;

    private CmsObject m_cms;
    private I_CmsSession m_session;

    // the object to send the information to the workplace.
    private CmsReport m_report;

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */

    public CmsAdminPublishProjectThread(CmsObject cms, int projectId, I_CmsSession session) {
        m_cms = cms;
        m_session = session;
        m_projectId = projectId;
        m_report = new CmsReport(new String[]{"<br>", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "<b>", "</b>", "<br><b>Static Export</b><br>&nbsp; links to start:","<span style='color:#009900'>","</span><br><br>"});
    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {
            m_cms.publishProject(m_projectId, m_report);
        }
        catch(CmsException e) {
            /*
            m_report.addSeperator(0);
            m_report.addSeperator(0);
            m_report.addString(e.getMessage());
            */
            
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
