/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminLinkmanagementThread.java,v $
 * Date   : $Date: 2003/08/14 15:37:24 $
 * Version: $Revision: 1.15 $
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
import com.opencms.file.CmsObject;
import com.opencms.linkmanagement.LinkChecker;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;

/**
 * A thread to check anchors in pages.
 * 
 * @author Hanjo Riege
 * @version $Revision: 1.15 $ 
 */
public class CmsAdminLinkmanagementThread extends A_CmsReportThread{

    private CmsObject m_cms;
    private I_CmsReport m_report;
    private int m_projectId;
    private String m_directPublishResourceName;
    private int m_oldProjectId;

    public CmsAdminLinkmanagementThread(CmsObject cms, int projectId) {
        this (cms, projectId, null);
    }
    
    public CmsAdminLinkmanagementThread(CmsObject cms, int projectId, String directPublishResourceName) {
        super("OpenCms: Linkmanagement for " + directPublishResourceName);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_projectId = projectId;
        m_oldProjectId = m_cms.getRequestContext().currentProject().getId();
        m_directPublishResourceName = directPublishResourceName;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
    }

    public void run() {
        try {
            m_report.println(m_report.key("report.check_links_begin"), I_CmsReport.C_FORMAT_HEADLINE);            
            (new LinkChecker()).checkProject(m_cms, m_projectId, m_report);
            m_report.println(m_report.key("report.check_links_end"), I_CmsReport.C_FORMAT_HEADLINE);            
        } catch(CmsException e) {
            m_report.println(e);
            if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());                
            }
        } finally {
            if (m_directPublishResourceName != null) {
                // if this was a direct publish project - delete it here 
                // another project will be created for the real publish
                // if the user selects "continue" on the workplace dialog
                try {
                    // make sure all resources are removed from the temp project
                    // m_cms.lockResource(m_directPublishResourceName, true);
                    // m_cms.unlockResource(m_directPublishResourceName);
                    m_cms.changeLockedInProject(m_oldProjectId, m_directPublishResourceName);
                    // delete the direct publish project
                    m_cms.deleteProject(m_projectId);
                } catch (Exception e) {
                    m_report.println(e);
                    // ignore exception, nothing we can do here
                    if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                        OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
                    }                
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
        return m_report.hasBrokenLinks();
    }
}