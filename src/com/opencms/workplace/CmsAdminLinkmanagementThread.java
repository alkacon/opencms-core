package com.opencms.workplace;

/**
 * Title:        OpenCms
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Hanjo Riege
 * @version 1.0
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