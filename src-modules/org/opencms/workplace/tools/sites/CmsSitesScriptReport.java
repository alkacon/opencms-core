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
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.report.I_CmsReportThread;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.io.FileUtils;

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

        /** Constant for the "http" port. */
        private static final int PORT_HTTP = 80;

        /** Constant for the "https" port. */
        private static final int PORT_HTTPS = 443;

        /** The placeholder string searched and replaced with the server's name in the virtual host template file. */
        private static final String SERVER_NAME_PLACE_HOLDER = "SERVER_NAME_PLACE_HOLDER";

        /** The target path. */
        private String m_targetPath;

        /**
         * Public constructor.<p>
         * 
         * @param cms the cms object
         */
        protected CmsSitesActionThread(CmsObject cms) {

            super(cms, "sites-apache-action");
            m_targetPath = getParamTargetpath().endsWith(File.separator) ? getParamTargetpath() : getParamTargetpath()
                + File.separator;
            setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), "/sites/", new HashMap<String, String[]>()));
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
                deleteAllOpenCmsVhosts();
                createAllOpenCmsVhosts();
                executeScript();
            } catch (Exception e) {
                getReport().println(e);
            }
        }

        /**
         * Creates the new virtual host configuration files from the given template file.<p>
         * 
         * @throws IOException if something goes wrong
         */
        private void createAllOpenCmsVhosts() throws IOException {

            String template = FileUtils.readFileToString(new File(getParamVhostsource()));
            List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCms(), true);
            for (CmsSite site : sites) {
                if (site.getSiteMatcher() != null) {
                    getReport().println(
                        Messages.get().container(Messages.RPT_CREATING_VHOST_FOR_SITE_1, site),
                        I_CmsReport.FORMAT_OK);
                    String filename = generateVhostFilename(site);
                    File newFile = new File(filename);
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                    }
                    String conf = template.replaceAll(SERVER_NAME_PLACE_HOLDER, site.getSiteMatcher().getServerName());
                    FileUtils.writeStringToFile(newFile, conf);
                }
            }
        }

        /**
         * Deletes all OpenCms generated virtual host configuration files.<p>
         */
        private void deleteAllOpenCmsVhosts() {

            File file = new File(m_targetPath);
            if (file.exists() && file.isDirectory()) {
                File[] vhostFiles = file.listFiles(new FilenameFilter() {

                    /**
                     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
                     */
                    public boolean accept(File dir, String name) {

                        if (name.startsWith(getParamVhostprefix())) {
                            return true;
                        }
                        return false;
                    }
                });
                for (File f : vhostFiles) {
                    getReport().println(
                        Messages.get().container(Messages.RPT_DELETING_FILE_1, f),
                        I_CmsReport.FORMAT_OK);
                    f.delete();
                }
            }
        }

        /**
         * Executes the console script.<p>
         * 
         * @throws IOException if something goes wrong
         * @throws InterruptedException if something goes wrong
         */
        private void executeScript() throws IOException, InterruptedException {

            File script = new File(getParamConsolescript());
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
        }

        /**
         * Generates the file name for the virtual host configuration of the given site.<p>
         * 
         * @param site the site to get the virtual host configuration file name for 
         * 
         * @return the file name for the virtual host configuration of the given site
         */
        private String generateVhostFilename(CmsSite site) {

            int port = site.getSiteMatcher().getServerPort();
            String serverName = site.getSiteMatcher().getServerName();
            String portPart = ((port != PORT_HTTP) && (port != PORT_HTTPS)) ? "_" + port : "";
            return m_targetPath + getParamVhostprefix() + "_" + serverName + portPart;
        }
    }

    /** The script to be executed after updating the virtual host configurations, e.g. "/etc/apache2/reload.sh". */
    private String m_paramConsolescript;

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
     * Returns the paramConsolescript.<p>
     *
     * @return the paramConsolescript
     */
    public String getParamConsolescript() {

        return m_paramConsolescript;
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

        return new CmsSitesActionThread(getCms());
    }

    /**
     * Sets the paramConsolescript.<p>
     *
     * @param paramConsolescript the paramConsolescript to set
     */
    public void setParamConsolescript(String paramConsolescript) {

        m_paramConsolescript = paramConsolescript;
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
