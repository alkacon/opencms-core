/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminVfsLinkManagementThread.java,v $
 * Date   : $Date: 2003/03/04 17:18:33 $
 * Version: $Revision: 1.1 $
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
 * A thread to join all VFS links with their target resources.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsAdminVfsLinkManagementThread extends Thread implements I_CmsConstants {

    private CmsObject m_cms;
    private I_CmsReport m_Report;
    private I_CmsSession m_Session;

    public CmsAdminVfsLinkManagementThread(CmsObject cms, I_CmsSession session) {
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);

        m_cms = cms;
        m_Session = session;
        m_Report = (I_CmsReport) new CmsHtmlReport(locale);
    }

    public void run() {
        try {            
            m_cms.joinLinksToTargets(m_Report);
        } catch (CmsException e) {
            m_Report.println(e);
            m_Session.putValue(I_CmsConstants.C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }

    public String getReportUpdate() {
        return m_Report.getReportUpdate();
    }
}