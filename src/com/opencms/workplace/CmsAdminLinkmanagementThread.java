/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminLinkmanagementThread.java,v $
 * Date   : $Date: 2002/11/07 13:29:04 $
 * Version: $Revision: 1.3 $
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

/**
 * @author Hanjo Riege
 * @version $Revision: 1.3 $
 */

import com.opencms.file.*;
import com.opencms.linkmanagement.*;
import com.opencms.report.*;
import com.opencms.util.*;
import com.opencms.core.*;

public class CmsAdminLinkmanagementThread extends Thread {

    private CmsObject m_cms;
    private CmsReport m_report;
    private int m_projectId;

    public CmsAdminLinkmanagementThread(CmsObject cms, int projectId) {
        m_cms = cms;
        m_projectId = projectId;
        m_report = new CmsReport(new String[]{"<br>", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "<b>", "</b>"});
    }


    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {
            (new LinkChecker()).checkProject(m_cms, m_projectId, m_report);
        }
        catch(CmsException e) {
            m_report.addSeperator(0);
            m_report.addString(e.getMessage());            
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
                StackTraceElement[] stack = e.getStackTrace();
                int len = (stack.length>5)?5:stack.length;
                for (int i=0; i<len; i++) {
                    A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, " > " + stack[i]);
                }
            }
        }
    }

    /**
     * returns the part of the report that is ready.
     */
    public String getReportUpdate(){
        return m_report.getReportUpdate();
    }

    /**
     * shows if there are broken links in this project so far.
     */
    public boolean brokenLinksFound(){
        return m_report.containsPageLinks();
    }
}