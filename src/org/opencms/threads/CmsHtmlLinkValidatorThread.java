/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsHtmlLinkValidatorThread.java,v $
 * Date   : $Date: 2004/01/23 10:56:01 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;

/**
 * A report thread for the HTML link validator.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2004/01/23 10:56:01 $
 */
public class CmsHtmlLinkValidatorThread extends A_CmsReportThread {
    
    private CmsResource m_directPublishResource = null;
    private boolean m_directPublishSiblings = false;
    
    /**
     * Creates a thread which checks all offline resources of the current project and starts it.<p>
     * 
     * @param cms the current OpenCms context object
     */
    public CmsHtmlLinkValidatorThread(CmsObject cms) {
        super(cms, "OpenCms: validating HTML links in all unpublished resources of project " + cms.getRequestContext().currentProject().getName());
        initHtmlReport();
        start();
    }
    
    /**
     * Creates a thread which checks the list of offline resources of the current project and starts it.<p>
     * 
     * @param cms the current OpenCms context object
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings true, if all eventual siblings of the direct published resource should also get published
     */
    public CmsHtmlLinkValidatorThread(CmsObject cms, CmsResource directPublishResource, boolean directPublishSiblings) {
        super(cms, "OpenCms: validating HTML links in all unpublished resources of project " + cms.getRequestContext().currentProject().getName());
        m_directPublishResource = directPublishResource;
        m_directPublishSiblings = directPublishSiblings;
        initHtmlReport();
        start();
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
            if (m_directPublishResource == null) {
                // validate all offline resources
                getCms().validateHtmlLinks(getReport());
            } else {
                // validate the given list of resources
                getCms().validateHtmlLinks(m_directPublishResource, m_directPublishSiblings, getReport());
            }
        } catch (Exception e) {
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error validating HTML links", e);
            }
        }
    }

}
