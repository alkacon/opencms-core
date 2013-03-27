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
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.report.I_CmsReportThread;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The apache action report.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesScriptReport extends A_CmsListReport {

    /**
     * Executes a script file.<p>
     * 
     * @since 9.0.0
     */
    private class CmsSitesActionThread extends A_CmsReportThread {

        /** The script file to execute. */
        private String m_script;

        /**
         * Public constructor.<p>
         * 
         * @param cms the cms object
         * @param script the script to execute
         */
        protected CmsSitesActionThread(CmsObject cms, String script) {

            super(cms, "sites-apache-action");
            m_script = script;
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

            try {

                File script = new File(m_script);
                ProcessBuilder pb = new ProcessBuilder(script.getAbsolutePath());
                pb.directory(new File(script.getParent()));
                Process pr = pb.start();
                pr.waitFor();
                BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                while (buf.ready()) {
                    String line = buf.readLine();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(line)) {
                        getReport().println(
                            Messages.get().container(Messages.RPT_OUTPUT_CONSOLE_1, buf.readLine()),
                            I_CmsReport.FORMAT_OK);
                    }
                }
            } catch (Exception e) {
                getReport().println(e);
            }
        }
    }

    /** The paths of the script file to execute. */
    private String m_paramScript;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesScriptReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesScriptReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the Script.<p>
     *
     * @return the Script
     */
    public String getParamScript() {

        return m_paramScript;
    }

    /**
     * Sets the Script.<p>
     *
     * @param paramScript the Script to set
     */
    public void setParamScript(String paramScript) {

        m_paramScript = paramScript;
    }

    /** 
     * 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        return new CmsSitesActionThread(getCms(), m_paramScript);
    }

}
