/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsUpdateBean.java,v $
 * Date   : $Date: 2005/10/11 14:34:33 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.CmsShellReport;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

/**
 * A java bean as a controller for the OpenCms update wizard.<p>
 * 
 * @author  Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUpdateBean extends CmsSetupBean {

    /** path to the update folder. */
    public static final String UPDATE_DATA_FOLDER = "update";

    /** replace pattern constant for the cms script. */
    private static final String C_ADMIN_PWD = "@ADMIN_PWD@";

    /** replace pattern constant for the cms script. */
    private static final String C_ADMIN_USER = "@ADMIN_USER@";

    /** replace pattern constant for the cms script. */
    private static final String C_ADMIN_GROUP = "@ADMIN_GROUP@";

    /** replace pattern constant for the cms script. */
    private static final String C_UPDATE_PROJECT = "@UPDATE_PROJECT@";

    /** replace pattern constant for the cms script. */
    private static final String C_UPDATE_SITE = "@UPDATE_SITE@";

    /** the admin user password. */
    private String m_adminPwd = "admin";

    /** The used admin user name. */
    private String m_adminUser = "Admin";

    /** the update project. */
    private String m_updateProject = "updateProject";

    /** the site for update. */
    private String m_updateSite = "/sites/default/";

    /** The workplace import thread. */
    private CmsUpdateThread m_workplaceUpdateThread;

    /** 
     * Default constructor.<p>
     */
    public CmsUpdateBean() {

        super();
    }

    /**
     * Returns the admin Pwd.<p>
     *
     * @return the admin Pwd
     */
    public String getAdminPwd() {

        return m_adminPwd;
    }

    /**
     * Returns the admin User.<p>
     *
     * @return the admin User
     */
    public String getAdminUser() {

        return m_adminUser;
    }

    /**
     * Returns a map with all available modules.<p>
     * 
     * The map contains maps keyed by module package names. Each of these maps contains various
     * information about the module such as the module name, version, description, and a list of 
     * it's dependencies. You should refer to the source code of this method to understand the data 
     * structure of the map returned by this method!<p>
     * 
     * @return a map with all available modules
     */
    public Map getAvailableModules() {

        try {
            m_availableModules = new HashMap();
            m_moduleDependencies = new HashMap();

            // open the folder "/update/modules/"
            File packagesFolder = new File(m_webAppRfsPath
                + UPDATE_DATA_FOLDER
                + File.separator
                + CmsSystemInfo.FOLDER_MODULES);

            if (packagesFolder.exists()) {
                // list all child resources in the packages folder
                File[] childResources = packagesFolder.listFiles();

                if (childResources != null) {
                    for (int i = 0; i < childResources.length; i++) {
                        File childResource = childResources[i];

                        if (childResource.isFile() && !(childResource.getAbsolutePath().toLowerCase().endsWith(".zip"))) {
                            // skip non-ZIP files
                            continue;
                        }

                        // parse the module's manifest
                        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(childResource.getAbsolutePath());

                        // module package name
                        String moduleName = module.getName();
                        // module group name
                        String moduleGroup = module.getGroup();
                        // module nice name
                        String moduleNiceName = module.getNiceName();
                        // module version
                        String moduleVersion = module.getVersion().getVersion();
                        // module description
                        String moduleDescription = module.getDescription();

                        // if module a depends on module b, and module c depends also on module b:
                        // build a map with a list containing "a" and "c" keyed by "b" to get a 
                        // list of modules depending on module "b"...                        
                        List dependencies = module.getDependencies();
                        for (int j = 0, n = dependencies.size(); j < n; j++) {
                            CmsModuleDependency dependency = (CmsModuleDependency)dependencies.get(j);

                            // module dependency package name
                            String moduleDependencyName = dependency.getName();
                            // get the list of dependend modules
                            List moduleDependencies = (List)m_moduleDependencies.get(moduleDependencyName);

                            if (moduleDependencies == null) {
                                // build a new list if "b" has no dependend modules yet
                                moduleDependencies = new ArrayList();
                                m_moduleDependencies.put(moduleDependencyName, moduleDependencies);
                            }

                            // add "a" as a module depending on "b"
                            moduleDependencies.add(moduleName);
                        }

                        // create a map holding the collected module information
                        Map moduleData = new HashMap();
                        moduleData.put("name", moduleName);
                        moduleData.put("niceName", moduleNiceName);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(moduleGroup)) {
                            moduleData.put("group", moduleGroup);
                        }
                        moduleData.put("version", moduleVersion);
                        moduleData.put("description", moduleDescription);
                        moduleData.put("filename", childResource.getName());

                        // put the module information into a map keyed by the module packages names
                        m_availableModules.put(moduleName, moduleData);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }

        return m_availableModules;
    }

    /**
     * Returns the name of the update log file.<p>
     * 
     * @return the name of the update log file
     */
    public String getUpdateLogName() {

        StringBuffer result = new StringBuffer(m_webAppRfsPath).append("WEB-INF");
        result.append(File.separator).append("logs").append(File.separator).append("update.log");
        return result.toString();
    }

    /**
     * Returns the update Project.<p>
     *
     * @return the update Project
     */
    public String getUpdateProject() {

        return m_updateProject;
    }

    /**
     * Returns the update site.<p>
     *
     * @return the update site
     */
    public String getUpdateSite() {

        return m_updateSite;
    }

    /**
     * Returns the workplace update thread.<p>
     * 
     * @return the workplace update thread
     */
    public CmsUpdateThread getWorkplaceUpdateThread() {

        return m_workplaceUpdateThread;
    }

    /** 
     * Creates a new instance of the setup Bean.<p>
     * 
     * @param webAppRfsPath path to the OpenCms web application
     * @param servletMapping the OpenCms servlet mapping
     * @param defaultWebApplication the name of the default web application
     * 
     */
    public void init(String webAppRfsPath, String servletMapping, String defaultWebApplication) {

        try {
            super.init(webAppRfsPath, servletMapping, defaultWebApplication);

            if (m_workplaceUpdateThread != null) {
                if (m_workplaceUpdateThread.isAlive()) {
                    m_workplaceUpdateThread.kill();
                }
                m_workplaceUpdateThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Prepares step 4 of the update wizard.<p>
     */
    public void prepareUpdateStep4() {

        if (isInitialized()) {
            try {
                String fileName = getWebAppRfsPath() + UPDATE_DATA_FOLDER + File.separatorChar + "cmsupdate";
                FileInputStream fis = new FileInputStream(fileName + ".ori");
                String script = "";
                int readChar = fis.read();
                while (readChar > -1) {
                    script += (char)readChar;
                    readChar = fis.read();
                }
                script = CmsStringUtil.substitute(script, C_ADMIN_USER, getAdminUser());
                script = CmsStringUtil.substitute(script, C_ADMIN_PWD, getAdminPwd());
                script = CmsStringUtil.substitute(script, C_UPDATE_PROJECT, getUpdateProject());
                script = CmsStringUtil.substitute(script, C_UPDATE_SITE, getUpdateSite());
                script = CmsStringUtil.substitute(
                    script,
                    C_ADMIN_GROUP,
                    OpenCms.getDefaultUsers().getGroupAdministrators());
                FileOutputStream fos = new FileOutputStream(fileName + ".txt");
                fos.write(script.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Prepares the update wizard.<p>
     */
    public void prepareUpdateStep4b() {

        if (!isInitialized()) {
            return;
        }

        if ((m_workplaceUpdateThread != null) && (m_workplaceUpdateThread.isFinished())) {
            // update is already finished, just wait for client to collect final data
            return;
        }

        if (m_workplaceUpdateThread == null) {
            m_workplaceUpdateThread = new CmsUpdateThread(this);
        }

        if (!m_workplaceUpdateThread.isAlive()) {
            m_workplaceUpdateThread.start();
        }
    }

    /**
     * Generates the output for the update wizard.<p>
     * 
     * @param out the JSP print stream
     * 
     * @throws IOException in case errors occur while writing to "out"
     */
    public void prepareUpdateStep4bOutput(JspWriter out) throws IOException {

        m_oldLoggingOffset = m_newLoggingOffset;
        m_newLoggingOffset = m_workplaceUpdateThread.getLoggingThread().getMessages().size();
        if (isInitialized()) {
            for (int i = m_oldLoggingOffset; i < m_newLoggingOffset; i++) {
                String str = m_workplaceUpdateThread.getLoggingThread().getMessages().get(i).toString();
                str = CmsEncoder.escapeWBlanks(str, "UTF-8");
                out.println("output[" + (i - m_oldLoggingOffset) + "] = \"" + str + "\";");
            }
        } else {
            out.println("output[0] = 'ERROR';");
        }

        boolean threadFinished = m_workplaceUpdateThread.isFinished();
        boolean allWritten = m_oldLoggingOffset >= m_workplaceUpdateThread.getLoggingThread().getMessages().size();

        out.println("function initThread() {");
        if (isInitialized()) {
            out.print("send();");
            if (threadFinished && allWritten) {
                out.println("setTimeout('top.display.finish()', 500);");
            } else {
                int timeout = 5000;
                if (getWorkplaceUpdateThread().getLoggingThread().getMessages().size() < 20) {
                    timeout = 1000;
                }
                out.println("setTimeout('location.reload()', " + timeout + ");");
            }
        }
        out.println("}");
    }

    /**
     * Prepares step 6 of the update wizard.<p>
     */
    public void prepareUpdateStep6() {

        if (isInitialized()) {
            // lock the wizard for further use 
            lockWizard();
            // save Properties to file "opencms.properties" 
            saveProperties(getProperties(), "opencms.properties", false);
        }
    }

    /**
     * Sets the admin Pwd.<p>
     *
     * @param adminPwd the admin Pwd to set
     */
    public void setAdminPwd(String adminPwd) {

        m_adminPwd = adminPwd;
    }

    /**
     * Sets the admin User.<p>
     *
     * @param adminUser the admin User to set
     */
    public void setAdminUser(String adminUser) {

        m_adminUser = adminUser;
    }

    /**
     * Sets the update Project.<p>
     *
     * @param updateProject the update Project to set
     */
    public void setUpdateProject(String updateProject) {

        m_updateProject = updateProject;
    }

    /**
     * Sets the update site.<p>
     *
     * @param site the update site to set
     */
    public void setUpdateSite(String site) {

        m_updateSite = site;
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellExit()
     */
    public void shellExit() {

        System.out.println();
        System.out.println();
        System.out.println("The update is finished!\nThe OpenCms system used for the update will now shut down.");
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellStart()
     */
    public void shellStart() {

        System.out.println();
        System.out.println("Starting Workplace update for OpenCms!");

        String[] copy = org.opencms.main.Messages.COPYRIGHT_BY_ALKACON;
        for (int i = copy.length - 1; i >= 0; i--) {
            System.out.println(copy[i]);
        }
        System.out.println("This is OpenCms " + OpenCms.getSystemInfo().getVersionName());
        System.out.println();
        System.out.println();
    }

    /**
     * Installed all modules that have been set using {@link #setInstallModules(String)}.<p>
     * 
     * This method is invoked as a shell command.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void updateModulesFromUpdateBean() throws Exception {

        // read here how the list of modules to be installed is passed from the setup bean to the
        // setup thread, and finally to the shell process that executes the setup script:
        // 1) the list with the package names of the modules to be installed is saved by setInstallModules
        // 2) the setup thread gets initialized in a JSP of the setup wizard
        // 3) the instance of the setup bean is passed to the setup thread by setAdditionalShellCommand
        // 4) the setup bean is passed to the shell by startSetup
        // 5) because the setup bean implements I_CmsShellCommands, the shell constructor can pass the shell's CmsObject back to the setup bean
        // 6) thus, the setup bean can do things with the Cms

        if (m_cms != null && m_installModules != null) {
            for (int i = 0; i < m_installModules.size(); i++) {
                Map module = (Map)m_availableModules.get(m_installModules.get(i));
                String filename = (String)module.get("filename");
                String name = (String)module.get("name");
                try {
                    updateModule(name, filename);
                } catch (Exception e) {
                    // log a exception during module import, but make sure the next module is still imported
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Imports a module (zipfile) from the default module directory, 
     * creating a temporary project for this.<p>
     * 
     * @param moduleName the name of the module to replace
     * @param importFile the name of the import module located in the update module directory
     * 
     * @throws Exception if something goes wrong
     * 
     * @see org.opencms.importexport.CmsImportExportManager#importData(org.opencms.file.CmsObject, String, String, org.opencms.report.I_CmsReport)
     */
    protected void updateModule(String moduleName, String importFile) throws Exception {

        String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(
            UPDATE_DATA_FOLDER + File.separatorChar + CmsSystemInfo.FOLDER_MODULES + importFile);

        if (OpenCms.getModuleManager().getModule(moduleName) != null) {
            OpenCms.getModuleManager().deleteModule(
                m_cms,
                moduleName,
                true,
                new CmsShellReport(m_cms.getRequestContext().getLocale()));
        }

        OpenCms.getImportExportManager().importData(
            m_cms,
            fileName,
            null,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }
}