/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsPublishThread.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
 * Version: $Revision: 1.18 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

/**
 * Publishes a resource or the users current project.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.18 $
 * @since 5.1.10
 */
public class CmsPublishThread extends A_CmsReportThread {
    
    /** The list of resources to publish. */
    private CmsPublishList m_publishList;

    /**
     * Creates a Thread that publishes the Cms resources contained in the specified Cms publish 
     * list.<p>
     * 
     * @param cms the current OpenCms context object
     * @param publishList a Cms publish list
     * @see org.opencms.file.CmsObject#getPublishList(org.opencms.file.CmsResource, boolean)
     * @see org.opencms.file.CmsObject#getPublishList()
     */
    public CmsPublishThread(CmsObject cms, CmsPublishList publishList) {
        super(cms, "OpenCms: Publishing of resources in publish list");

        m_publishList = publishList;          
        initHtmlReport(cms.getRequestContext().getLocale());
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
            getReport().println(getReport().key("report.publish_resource_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            getCms().publishProject(getReport(), m_publishList);
            getReport().println(getReport().key("report.publish_resource_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (Exception e) {
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error publishing project", e);
            }
        }
    }
}
