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
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.antlr.stringtemplate.StringTemplate;

/**
 * Executes a script file.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesWebserverThread extends A_CmsReportThread {

    /** Constant for the "http" port. */
    private static final int PORT_HTTP = 80;

    /** Constant for the "https" port. */
    private static final int PORT_HTTPS = 443;

    /** The file path. */
    private String m_filePrefix;

    /** The script path. */
    private String m_scriptPath;

    /** The target path. */
    private String m_targetPath;

    /** The template path. */
    private String m_templatePath;

    /** The logging directory. */
    private String m_loggingDir;

    /** The files that have been written. */
    private List<String> m_writtenFiles = new ArrayList<String>();

    /**
     * Public constructor.<p>
     * 
     * @param cms the cms object
     * @param targetPath the target path
     * @param templatePath the template path
     * @param scriptPath the script path
     * @param filePrefix the filename prefix
     * @param loggingDir the logging directory
     */
    public CmsSitesWebserverThread(
        CmsObject cms,
        String targetPath,
        String templatePath,
        String scriptPath,
        String filePrefix,
        String loggingDir) {

        super(cms, "write-to-webserver");

        m_targetPath = targetPath.endsWith(File.separator) ? targetPath : targetPath + File.separator;
        m_templatePath = templatePath;
        m_scriptPath = scriptPath;
        m_filePrefix = filePrefix;
        m_loggingDir = loggingDir;
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
            deleteAllWebserverConfigs(m_filePrefix);
            createAllWebserverConfigs();
            executeScript();
        } catch (Exception e) {
            getReport().println(e);
        }
    }

    /**
     * Creates the new web server configuration files from the given template file.<p>
     * 
     * @throws IOException if something goes wrong
     */
    private void createAllWebserverConfigs() throws IOException {

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCms(), true);
        for (CmsSite site : sites) {
            if ((site.getSiteMatcher() != null) && site.isWebserver()) {
                StringTemplate config = new StringTemplate(FileUtils.readFileToString(new File(m_templatePath)));
                String filename = m_targetPath
                    + m_filePrefix
                    + "_"
                    + generateWebserverConfigName(site.getSiteMatcher(), "_");
                getReport().println(
                    Messages.get().container(Messages.RPT_CREATING_CONFIG_FOR_SITE_2, filename, site),
                    I_CmsReport.FORMAT_OK);
                File newFile = new File(filename);
                if (!newFile.exists()) {
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                }

                // system info
                String webappPath = OpenCms.getSystemInfo().getWebApplicationRfsPath();
                config.setAttribute("DOCUMENT_ROOT", webappPath.substring(0, webappPath.length() - 1));
                config.setAttribute("WEBAPP_NAME", OpenCms.getSystemInfo().getWebApplicationName());
                config.setAttribute("CONTEXT_PATH", OpenCms.getSystemInfo().getContextPath());
                config.setAttribute("SERVLET_PATH", OpenCms.getSystemInfo().getServletPath());
                config.setAttribute("DEFAULT_ENCODING", OpenCms.getSystemInfo().getDefaultEncoding());
                config.setAttribute("CONFIG_FILENAME", generateWebserverConfigName(site.getSiteMatcher(), "_"));
                config.setAttribute("LOGGING_DIRECTORY", m_loggingDir);

                // site info
                config.setAttribute("SERVER_URL", site.getUrl());
                config.setAttribute("SERVER_PROTOCOL", site.getSiteMatcher().getServerProtocol());
                config.setAttribute("SERVER_NAME", site.getSiteMatcher().getServerName());
                config.setAttribute("SERVER_PORT", site.getSiteMatcher().getServerPort());
                config.setAttribute("SERVER_NAME_WITH_PORT", generateWebserverConfigName(site.getSiteMatcher(), ":"));
                config.setAttribute("SITE_TITLE", site.getTitle());
                if (site.getErrorPage() != null) {
                    config.setAttribute("ERROR_PAGE", site.getErrorPage());
                }

                // alias info
                if ((site.getAliases() != null) && !site.getAliases().isEmpty()) {
                    config.setAttribute("ALIAS_DIRECTIVE", "ServerAlias");
                    for (CmsSiteMatcher alias : site.getAliases()) {
                        config.setAttribute("SERVER_ALIASES", generateWebserverConfigName(alias, ":") + " ");
                    }
                }

                // secure info
                if (site.hasSecureServer()) {
                    if (site.getSecureUrl() != null) {
                        config.setAttribute("SECURE_URL", site.getSecureUrl());
                    }
                    if (site.getSecureServer() != null) {
                        config.setAttribute("SECURE_SERVER_NAME", site.getSecureServer().getServerName());
                        config.setAttribute("SECURE_SERVER_PORT", site.getSecureServer().getServerPort());
                        config.setAttribute("SECURE_SERVER_PROTOCOL", site.getSecureServer().getServerProtocol());
                    }
                }

                FileUtils.writeStringToFile(newFile, config.toString());
                m_writtenFiles.add(newFile.getAbsolutePath());
            }
        }
    }

    /**
     * Deletes all web server's configuration files with the given prefix.<p>
     */
    private void deleteAllWebserverConfigs(final String prefix) {

        File file = new File(m_targetPath);
        if (file.exists() && file.isDirectory()) {
            File[] configFiles = file.listFiles(new FilenameFilter() {

                /**
                 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
                 */
                public boolean accept(File dir, String name) {

                    if (name.startsWith(prefix)) {
                        return true;
                    }
                    return false;
                }
            });
            for (File f : configFiles) {
                getReport().println(Messages.get().container(Messages.RPT_DELETING_FILE_1, f), I_CmsReport.FORMAT_OK);
                f.delete();
            }
        }
    }

    /**
     * Executes the webserver script.<p>
     * 
     * @throws IOException if something goes wrong
     * @throws InterruptedException if something goes wrong
     */
    private void executeScript() throws IOException, InterruptedException {

        File script = new File(m_scriptPath);
        List<String> params = new LinkedList<String>();
        params.add(script.getAbsolutePath());
        params.addAll(m_writtenFiles);
        ProcessBuilder pb = new ProcessBuilder(params.toArray(new String[params.size()]));
        pb.directory(new File(script.getParent()));
        Process pr = pb.start();
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        while (buf.ready()) {
            String line = buf.readLine();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(line)) {
                getReport().println(
                    Messages.get().container(Messages.RPT_OUTPUT_WEBSERVER_1, buf.readLine()),
                    I_CmsReport.FORMAT_OK);
            }
        }
    }

    /**
     * Generates the web server configuration filename for the given site.<p>
     * 
     * @param site the site to get the web server configuration filename for 
     * 
     * @return the web server configuration filename
     */
    private String generateWebserverConfigName(CmsSiteMatcher macther, String separator) {

        int port = macther.getServerPort();
        String serverName = macther.getServerName();
        String portPart = ((port != PORT_HTTP) && (port != PORT_HTTPS)) ? separator + port : "";
        return serverName + portPart;
    }
}
