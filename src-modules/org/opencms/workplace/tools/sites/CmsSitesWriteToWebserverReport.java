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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The write to web server report.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesWriteToWebserverReport extends A_CmsListReport {

    /** The script to be executed after updating the web server configurations. */
    private String m_paramWebserverscript;

    /** The target path to store the virtual host files. */
    private String m_paramTargetpath;

    /** The prefix used for created virtual host configuration files, created by this tool. */
    private String m_paramVhostprefix;

    /** The source file used as template for creating a virtual host configuration files. */
    private String m_paramVhostsource;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesWriteToWebserverReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesWriteToWebserverReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the web server script parameter.<p>
     *
     * @return the web server script parameter
     */
    public String getParamWebserverscript() {

        return m_paramWebserverscript;
    }

    /**
     * Returns the paramTargetpath.<p>
     *
     * @return the paramTargetpath
     */
    public String getParamTargetpath() {

        return m_paramTargetpath;
    }

    /**
     * Returns the paramVhostprefix.<p>
     *
     * @return the paramVhostprefix
     */
    public String getParamVhostprefix() {

        return m_paramVhostprefix;
    }

    /**
     * Returns the paramVhostsource.<p>
     *
     * @return the paramVhostsource
     */
    public String getParamVhostsource() {

        return m_paramVhostsource;
    }

    /** 
     * 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), "/sites/", new HashMap<String, String[]>()));
        return new CmsSitesWriteToWebserverThread(
            getCms(),
            getParamTargetpath(),
            getParamVhostsource(),
            getParamWebserverscript(),
            getParamVhostprefix());
    }

    /**
     * Sets the web server script parameter.<p>
     *
     * @param paramWebserverscript the web server script parameter to set
     */
    public void setParamWebserverscript(String paramWebserverscript) {

        m_paramWebserverscript = paramWebserverscript;
    }

    /**
     * Sets the paramTargetpath.<p>
     *
     * @param paramTargetpath the paramTargetpath to set
     */
    public void setParamTargetpath(String paramTargetpath) {

        m_paramTargetpath = paramTargetpath;
    }

    /**
     * Sets the paramVhostprefix.<p>
     *
     * @param paramVhostprefix the paramVhostprefix to set
     */
    public void setParamVhostprefix(String paramVhostprefix) {

        m_paramVhostprefix = paramVhostprefix;
    }

    /**
     * Sets the paramVhostsource.<p>
     *
     * @param paramVhostsource the paramVhostsource to set
     */
    public void setParamVhostsource(String paramVhostsource) {

        m_paramVhostsource = paramVhostsource;
    }

}
