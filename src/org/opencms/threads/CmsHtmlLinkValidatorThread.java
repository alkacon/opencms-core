/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsHtmlLinkValidatorThread.java,v $
 * Date   : $Date: 2004/01/28 09:32:23 $
 * Version: $Revision: 1.3 $
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

import org.opencms.db.CmsPublishList;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

/**
 * A report thread for the HTML link validator.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2004/01/28 09:32:23 $
 */
public class CmsHtmlLinkValidatorThread extends A_CmsReportThread {
    
    /** A Cms resource to be published directly.<p> */
    private CmsResource m_directPublishResource;
    
    /** True if also the siblings of a Cms resource to be published directly should be validated.<p> */
    private boolean m_directPublishSiblings;
    
    /** Flag that indicates whether the publish list should be svaed in the workplace settings. */
    private boolean m_savePublishList;
    
    private CmsWorkplaceSettings m_settings;
    
    /**
     * Creates a thread that validates the HTML links (hrefs and images) in all unpublished Cms 
     * files of the current (offline) project, if the files resource types implement the interface 
     * {@link org.opencms.validation.I_CmsHtmlLinkValidatable}.<p>
     * 
     * Please refer to the Javadoc of the I_CmsHtmlLinkValidatable interface to see which classes
     * implement this interface (and so, which file types get validated by the HTML link 
     * validator).<p>
     * 
     * The generated Cms publish list is *NOT* saved in the current user's workplace settings.<p>
     * 
     * @param cms the current OpenCms context object
     * @see com.opencms.file.CmsObject#getPublishList(org.opencms.report.I_CmsReport)
     */
    public CmsHtmlLinkValidatorThread(CmsObject cms) {
        super(cms, "OpenCms: validating HTML links in all unpublished resources of project " + cms.getRequestContext().currentProject().getName());
        
        m_directPublishResource = null;
        m_directPublishSiblings = false;
        m_savePublishList = false;  
        m_settings = null;
        
        initHtmlReport();
        start();
    }
    
    /**
     * Creates a thread that validates the HTML links (hrefs and images) in the unpublished Cms 
     * file of the current (offline) project, if the file's resource type implements the interface 
     * {@link org.opencms.validation.I_CmsHtmlLinkValidatable}.<p>
     * 
     * Please refer to the Javadoc of the I_CmsHtmlLinkValidatable interface to see which classes
     * implement this interface (and so, which file types get validated by the HTML link 
     * validator).<p>
     * 
     * The generated Cms publish list *IS* saved in the current user's workplace settings for 
     * further processing by other threads. The last thread processing this publish list *MUST* 
     * ensure that the publish list gets removed from the current user's workplace settings!<p>
     * 
     * @param cms the current OpenCms context object
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings true, if all eventual siblings of the direct published resource should also get published
     * @see com.opencms.file.CmsObject#getPublishList(CmsResource, boolean, org.opencms.report.I_CmsReport)
     */
    public CmsHtmlLinkValidatorThread(CmsObject cms, CmsResource directPublishResource, boolean directPublishSiblings, CmsWorkplaceSettings settings) {
        super(cms, "OpenCms: validating HTML links in unpublishe resource of project " + cms.getRequestContext().currentProject().getName());
        
        m_directPublishResource = directPublishResource;
        m_directPublishSiblings = directPublishSiblings;
        m_savePublishList = true;
        m_settings = settings;
                
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
        CmsPublishList publishList = null;
        
        try {
            // get the list of resources that actually get published
            if (m_directPublishResource != null) {
                publishList = getCms().getPublishList(m_directPublishResource, m_directPublishSiblings, getReport());
            } else {
                publishList = getCms().getPublishList(getReport());
            }
            
            // validate the HTML links in these resources
            getCms().validateHtmlLinks(publishList, getReport());
            
            if (m_savePublishList && m_settings != null) {
                // save the publish list optionally to be processed by further workplace threads
                m_settings.setPublishList(publishList);
            }
        } catch (Exception e) {
            if (m_savePublishList && m_settings != null) {
                // overwrite the publish list in any case with null
                m_settings.setPublishList(null);
            }
            
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error validating HTML links", e);
            }
        }
    }

}
