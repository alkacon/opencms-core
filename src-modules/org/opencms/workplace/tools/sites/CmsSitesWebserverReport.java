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
public class CmsSitesWebserverReport extends A_CmsListReport {

    /** The source file used as template for creating a web server's configuration files. */
    private String m_paramConfigtemplate;

    /** The prefix used for created web server's configuration files, created by this tool. */
    private String m_paramFilenameprefix;

    /** The directory used for log files created by the web server. */
    private String m_paramLoggingdir;

    /** The source file used as template for creating a web server's configuration files. */
    private String m_paramSecuretemplate;

    /** The target path to store the web server's configuration files. */
    private String m_paramTargetpath;

    /** The script to be executed after updating the web server configurations. */
    private String m_paramWebserverscript;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSitesWebserverReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesWebserverReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the configuration template parameter.<p>
     *
     * @return the configuration template parameter
     */
    public String getParamConfigtemplate() {

        return m_paramConfigtemplate;
    }

    /**
     * Returns the file name prefix parameter.<p>
     *
     * @return the file name prefix parameter
     */
    public String getParamFilenameprefix() {

        return m_paramFilenameprefix;
    }

    /**
     * Returns the paramLoggingdir.<p>
     *
     * @return the paramLoggingdir
     */
    public String getParamLoggingdir() {

        return m_paramLoggingdir;
    }

    /**
     * Returns the paramSecuretemplate.<p>
     *
     * @return the paramSecuretemplate
     */
    public String getParamSecuretemplate() {

        return m_paramSecuretemplate;
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
     * Returns the web server script parameter.<p>
     *
     * @return the web server script parameter
     */
    public String getParamWebserverscript() {

        return m_paramWebserverscript;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), "/sites/", new HashMap<String, String[]>()));
        return new CmsSitesWebserverThread(
            getCms(),
            getParamTargetpath(),
            getParamConfigtemplate(),
            getParamWebserverscript(),
            getParamFilenameprefix(),
            getParamLoggingdir(),
            getParamSecuretemplate());
    }

    /**
     * Sets the configuration template parameter.<p>
     *
     * @param paramConfigtemplate the configuration template parameter to set
     */
    public void setParamConfigtemplate(String paramConfigtemplate) {

        m_paramConfigtemplate = paramConfigtemplate;
    }

    /**
     * Sets the file name prefix parameter.<p>
     *
     * @param paramFilenameprefix the file name prefix parameter to set
     */
    public void setParamFilenameprefix(String paramFilenameprefix) {

        m_paramFilenameprefix = paramFilenameprefix;
    }

    /**
     * Sets the paramLoggingdir.<p>
     *
     * @param paramLoggingdir the paramLoggingdir to set
     */
    public void setParamLoggingdir(String paramLoggingdir) {

        m_paramLoggingdir = paramLoggingdir;
    }

    /**
     * Sets the paramSecuretemplate.<p>
     *
     * @param paramSecuretemplate the paramSecuretemplate to set
     */
    public void setParamSecuretemplate(String paramSecuretemplate) {

        m_paramSecuretemplate = paramSecuretemplate;
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
     * Sets the web server script parameter.<p>
     *
     * @param paramWebserverscript the web server script parameter to set
     */
    public void setParamWebserverscript(String paramWebserverscript) {

        m_paramWebserverscript = paramWebserverscript;
    }

}
