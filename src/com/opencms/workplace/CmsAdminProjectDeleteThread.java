/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectDeleteThread.java,v $
* Date   : $Date: 2003/08/14 15:37:24 $
* Version: $Revision: 1.8 $
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
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.report.A_CmsReportThread;
import com.opencms.util.Utils;

/**
 * Thread for deleting a project.
 * Creation date: (20.09.01)
 * @author Edna Falkenhan
 */

public class CmsAdminProjectDeleteThread extends A_CmsReportThread {

    private int m_projectId;

    private CmsObject m_cms;
    private I_CmsSession m_session;

    /**
     * Insert the method's description here.
     */

    public CmsAdminProjectDeleteThread(CmsObject cms, int projectId, I_CmsSession session) {
        super("OpenCms: Project deletion of " + projectId);
        m_cms = cms;
        m_session = session;
        m_projectId = projectId;
    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {
            m_cms.deleteProject(m_projectId);
        }
        catch(CmsException e) {
            m_session.putValue(I_CmsConstants.C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
            if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
    
    /**
     * Returns the part of the report that is ready.
     * 
     * @return the part of the report that is ready
     */
    public String getReportUpdate(){
        return "";
    }    
}