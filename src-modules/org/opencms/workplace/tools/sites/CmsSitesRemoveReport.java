/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.sites;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReportThread;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListReport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for removing sites.<p> 
 * 
 * @since 9.0.0 
 */
public class CmsSitesRemoveReport extends A_CmsListReport {

    /**
     * Removes a site from the configuration.<p>
     * 
     * @since 9.0.0
     */
    private class CmsSitesRemoveThread extends A_CmsReportThread {

        /** The sites to remove. */
        private String m_sites;

        /**
         * Public constructor.<p>
         * 
         * @param cms the cms object
         * @param sites the name of the thread
         */
        protected CmsSitesRemoveThread(CmsObject cms, String sites) {

            super(cms, "site-remove-thread");
            m_sites = sites;
            initHtmlReport(cms.getRequestContext().getLocale());
        }

        /**
         * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
         */
        @Override
        public String getReportUpdate() {

            return getReport().getReportUpdate();
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            if (m_sites != null) {
                for (String sitePath : CmsStringUtil.splitAsList(m_sites, ",")) {
                    try {
                        OpenCms.getSiteManager().removeSite(
                            getCms(),
                            OpenCms.getSiteManager().getSiteForSiteRoot(sitePath));
                        getReport().println(Messages.get().container(Messages.RPT_REMOVED_SITE_SUCCESSFUL_1, sitePath));
                    } catch (CmsException e) {
                        getReport().addError(e);
                    }
                }
            }
        }
    }

    /** The paths of the sites to remove. */
    private String m_paramSites;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesRemoveReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesRemoveReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the paths of the sites to remove.<p>
     * 
     * @return the paths of the sites to remove
     */
    public String getParamSites() {

        return m_paramSites;
    }

    /** 
     * 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        return new CmsSitesRemoveThread(getCms(), m_paramSites);
    }

    /** 
     * Sets the sites parameter.<p>
     * 
     * @param paramSites the paths of the sites to remove
     */
    public void setParamSites(String paramSites) {

        m_paramSites = paramSites;
    }
}
