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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

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

    /** The placeholder string searched and replaced with the server's name in the web server's template file. */
    private static final String SERVER_NAME_PLACE_HOLDER = "SERVER_NAME_PLACE_HOLDER";

    /** The placeholder string searched and replaced with aliases in the web server's template file. */
    private static final String ALIASES_PLACE_HOLDER = "ALIASES_PLACE_HOLDER";

    /** The file path. */
    private String m_filePrefix;

    /** The script path. */
    private String m_scriptPath;

    /** The target path. */
    private String m_targetPath;

    /** The template path. */
    private String m_templatePath;

    /**
     * Public constructor.<p>
     * 
     * @param cms the cms object
     * @param targetPath the target path
     * @param templatePath the template path
     * @param scriptPath the script path
     * @param filePrefix the filename prefix
     */
    public CmsSitesWebserverThread(
        CmsObject cms,
        String targetPath,
        String templatePath,
        String scriptPath,
        String filePrefix) {

        super(cms, "write-to-webserver");

        m_targetPath = targetPath.endsWith(File.separator) ? targetPath : targetPath + File.separator;
        m_templatePath = templatePath;
        m_scriptPath = scriptPath;
        m_filePrefix = filePrefix;
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

        String template = FileUtils.readFileToString(new File(m_templatePath));
        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCms(), true);
        for (CmsSite site : sites) {
            if ((site.getSiteMatcher() != null) && site.isWebserver()) {
                String filename = generateWebserverConfigFilename(site);
                getReport().println(
                    Messages.get().container(Messages.RPT_CREATING_CONFIG_FOR_SITE_2, filename, site),
                    I_CmsReport.FORMAT_OK);
                File newFile = new File(filename);
                if (!newFile.exists()) {
                    newFile.createNewFile();
                }
                String conf = template.replaceAll(SERVER_NAME_PLACE_HOLDER, site.getSiteMatcher().getServerName());

                if ((site.getAliases() != null) && !site.getAliases().isEmpty()) {
                    StringBuffer buf = new StringBuffer();
                    Iterator<CmsSiteMatcher> iter = site.getAliases().iterator();
                    while (iter.hasNext()) {
                        CmsSiteMatcher alias = iter.next();
                        buf.append(alias.getServerName());
                        if (iter.hasNext()) {
                            buf.append(",");
                        }
                    }
                    conf = template.replaceAll(ALIASES_PLACE_HOLDER, buf.toString());
                }
                FileUtils.writeStringToFile(newFile, conf);
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
        ProcessBuilder pb = new ProcessBuilder(script.getAbsolutePath());
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
    private String generateWebserverConfigFilename(CmsSite site) {

        int port = site.getSiteMatcher().getServerPort();
        String serverName = site.getSiteMatcher().getServerName();
        String portPart = ((port != PORT_HTTP) && (port != PORT_HTTPS)) ? "_" + port : "";
        return m_targetPath + m_filePrefix + "_" + serverName + portPart;
    }
}
