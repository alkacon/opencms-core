/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsPublishThread.java,v $
 * Date   : $Date: 2004/01/25 12:42:45 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.threads;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

/**
 * Publishes a resource or the users current project.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.10 $
 * @since 5.1.10
 */
public class CmsPublishThread extends A_CmsReportThread {
    
    private String m_resourceName;
    private boolean m_directPublishSiblings;

    /**
     * Creates a Thread that publishes the current users project.<p>
     * 
     * @param cms the current OpenCms context object
     */
    public CmsPublishThread(CmsObject cms) {
        super(cms, "OpenCms: Publishing of project " + cms.getRequestContext().currentProject().getName());
        initHtmlReport();
    } 
    
    /**
     * Creates a Thread that publishes a selected resource directly.<p>
     * 
     * @param cms the current OpenCms context object
     * @param resourceName the name of the resource to publish directly
     * @param directPublishSiblings if true also publish all siblings directly
     */
    public CmsPublishThread(CmsObject cms, String resourceName, boolean directPublishSiblings) {
        super(cms, "OpenCms: Publishing of resource " + resourceName);
        m_resourceName = resourceName;
        m_directPublishSiblings = directPublishSiblings;
        initHtmlReport();
    }  
    
    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            if (m_resourceName != null) {
                // "publish resource directly" case
                getReport().println(getReport().key("report.publish_resource_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                getCms().publishResource(m_resourceName, m_directPublishSiblings, getReport());
                getReport().println(getReport().key("report.publish_resource_end"), I_CmsReport.C_FORMAT_HEADLINE);
            } else {
                // "publish current project" case
                getReport().println(getReport().key("report.publish_project_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                getCms().publishProject(getReport());
                getReport().println(getReport().key("report.publish_project_end"), I_CmsReport.C_FORMAT_HEADLINE);                
            }
        } catch (CmsException e) {
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error publishing project", e);
            }
        }
    }
}
