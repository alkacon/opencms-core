/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleVersion;
import org.opencms.module.CmsModuleXmlHandler;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.setup.db.CmsUpdateDBThread;
import org.opencms.setup.xml.CmsXmlConfigUpdater;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.threads.CmsXmlContentRepairSettings;
import org.opencms.workplace.threads.CmsXmlContentRepairThread;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;
import org.opencms.xml.CmsXmlException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.logging.Log;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * A java bean as a controller for the OpenCms update wizard.<p>
 *
 * @since 6.0.0
 */
public class CmsUpdateBean extends CmsSetupBean {

    /** The empty jar marker attribute key. */
    public static final String EMPTY_JAR_ATTRIBUTE_KEY = "OpenCms-empty-jar";

    /** Folder constant name.<p> */
    public static final String FOLDER_UPDATE = "WEB-INF/updatedata" + File.separatorChar;

    /** The static log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsUpdateBean.class);

    /** replace pattern constant for the cms script. */
    private static final String C_ADMIN_GROUP = "@ADMIN_GROUP@";

    /** replace pattern constant for the cms script. */
    private static final String C_ADMIN_PWD = "@ADMIN_PWD@";

    /** replace pattern constant for the cms script. */
    private static final String C_ADMIN_USER = "@ADMIN_USER@";

    /** replace pattern constant for the cms script. */
    private static final String C_UPDATE_PROJECT = "@UPDATE_PROJECT@";

    /** replace pattern constant for the cms script. */
    private static final String C_UPDATE_SITE = "@UPDATE_SITE@";

    /** New MySQL JDBC driver class name. */
    private static final String MYSQL_DRIVER_CLASS_NEW = "com.mysql.cj.jdbc.Driver";

    /** Old MySQL JDBC driver class name. */
    private static final String MYSQL_DRIVER_CLASS_OLD = "org.gjt.mm.mysql.Driver";

    /** MariaDB MySQL JDBC driver class name (used from OpenCms 12 onwards). */
    private static final String MYSQL_DRIVER_CLASS_MARIADB = "org.mariadb.jdbc.Driver";

    /** The obsolete modules that should be removed. */
    private static String[] OBSOLETE_MODULES = new String[] {
        "org.opencms.ade.config",
        "org.opencms.ade.containerpage",
        "org.opencms.ade.contenteditor",
        "org.opencms.ade.editprovider",
        "org.opencms.ade.galleries",
        "org.opencms.ade.postupload",
        "org.opencms.ade.properties",
        "org.opencms.ade.publish",
        "org.opencms.ade.sitemap",
        "org.opencms.ade.upload",
        "org.opencms.editors.codemirror",
        "org.opencms.editors.tinymce",
        "org.opencms.editors",
        "org.opencms.gwt",
        "org.opencms.jquery",
        "org.opencms.jsp.search",
        "org.opencms.locale.cs",
        "org.opencms.locale.da",
        "org.opencms.locale.de",
        "org.opencms.locale.es",
        "org.opencms.locale.it",
        "org.opencms.locale.ja",
        "org.opencms.locale.ru",
        "org.opencms.locale.zh",
        "org.opencms.ugc",
        "org.opencms.workplace",
        "org.opencms.workplace.administration",
        "org.opencms.workplace.explorer",
        "org.opencms.workplace.handler",
        "org.opencms.workplace.spellcheck",
        "org.opencms.workplace.tools.accounts",
        "org.opencms.workplace.tools.cache",
        "org.opencms.workplace.tools.content",
        "org.opencms.workplace.tools.database",
        "org.opencms.workplace.tools.galleryoverview",
        "org.opencms.workplace.tools.git",
        "org.opencms.workplace.tools.history",
        "org.opencms.workplace.tools.link",
        "org.opencms.workplace.tools.modules",
        "org.opencms.workplace.tools.projects",
        "org.opencms.workplace.tools.publishqueue",
        "org.opencms.workplace.tools.scheduler",
        "org.opencms.workplace.tools.searchindex",
        "org.opencms.workplace.tools.sites",
        "org.opencms.workplace.tools.workplace",
        "org.opencms.workplace.traditional",
        "org.opencms.workplace.help.de",
        "org.opencms.workplace.help.en",
        "org.opencms.workplace.help",
        "org.opencms.workplace.tools.git"};

    /** Static flag to indicate if all modules should be updated regardless of their version number. */
    private static final boolean UPDATE_ALL_MODULES = false;

    /** The new logging offset in the database update thread. */
    protected int m_newLoggingDBOffset;

    /** The old logging offset in the database update thread. */
    protected int m_oldLoggingDBOffset;

    /** The used admin user name. */
    private String m_adminGroup = "_tmpUpdateGroup" + (System.currentTimeMillis() % 1000);

    /** the admin user password. */
    private String m_adminPwd = "admin";

    /** The used admin user name. */
    private String m_adminUser = "Admin";

    /** The XML updater instance (lazily initialized). */
    private CmsXmlConfigUpdater m_configUpdater;

    /** The update database thread. */
    private CmsUpdateDBThread m_dbUpdateThread;

    /** The detected mayor version, based on DB structure. */
    private double m_detectedVersion;

    /** Parameter for keeping the history. */
    private boolean m_keepHistory;

    /** List of module to be updated. */
    private List<String> m_modulesToUpdate;

    /** The list of modules that should keep their libs. */
    private List<String> m_preserveLibModules;

    /** the update project. */
    private String m_updateProject = "_tmpUpdateProject" + (System.currentTimeMillis() % 1000);

    /** the site for update. */
    private String m_updateSite = CmsResource.VFS_FOLDER_SITES + "/default/";

    /** Cache for the up-to-date module names. */
    private List<String> m_uptodateModules;

    /** The workplace import thread. */
    private CmsUpdateThread m_workplaceUpdateThread;

    /**
     * Default constructor.<p>
     */
    public CmsUpdateBean() {

        super();
        m_preserveLibModules = Collections.emptyList();
        m_modulesFolder = FOLDER_UPDATE + CmsSystemInfo.FOLDER_MODULES;
        m_logFile = OpenCms.getSystemInfo().getLogFileRfsFolder() + "update.log";
    }

    /**
     * Adds the subscription driver to the properties.<p>
     */
    public void addSubscriptionDriver() {

        setExtProperty("driver.subscription", "db");
        String dbName = getExtProperty("db.name");
        String packageName = getDbPackage(dbName);
        setExtProperty("db.subscription.driver", "org.opencms.db." + packageName + ".CmsSubscriptionDriver");
        setExtProperty("db.subscription.pool", "opencms:default");
        setExtProperty("db.subscription.sqlmanager", "org.opencms.db." + packageName + ".CmsSqlManager");
    }

    /**
     * Compatibility check for OCEE modules.<p>
     *
     * @param version the opencms version
     *
     * @return <code>false</code> if OCEE is present but not compatible with opencms version
     */
    @SuppressWarnings({"boxing"})
    public boolean checkOceeVersion(String version) {

        try {
            Class<?> manager = Class.forName("org.opencms.ocee.base.CmsOceeManager");
            Method checkVersion = manager.getMethod("checkOceeVersion", String.class);
            return (Boolean)checkVersion.invoke(manager, version);
        } catch (@SuppressWarnings("unused") ClassNotFoundException e) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the shared folder if possible.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void createSharedFolder() throws Exception {

        String originalSiteRoot = m_cms.getRequestContext().getSiteRoot();
        CmsProject originalProject = m_cms.getRequestContext().getCurrentProject();
        try {
            m_cms.getRequestContext().setSiteRoot("");
            m_cms.getRequestContext().setCurrentProject(m_cms.createTempfileProject());
            if (!m_cms.existsResource("/shared")) {
                m_cms.createResource("/shared", OpenCms.getResourceManager().getResourceType("folder"));
            }

            try {
                m_cms.lockResourceTemporary("/shared");
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            try {
                m_cms.chacc("/shared", "group", "Users", "+v+w+r+i");
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            CmsResource shared = m_cms.readResource("/shared");
            try {
                OpenCms.getPublishManager().publishProject(
                    m_cms,
                    new CmsHtmlReport(m_cms.getRequestContext().getLocale(), m_cms.getRequestContext().getSiteRoot()),
                    shared,
                    false);
                OpenCms.getPublishManager().waitWhileRunning();
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } finally {
            m_cms.getRequestContext().setSiteRoot(originalSiteRoot);
            m_cms.getRequestContext().setCurrentProject(originalProject);
        }

    }

    /**
     * CmsShell command to delete spellcheck index.<p>
     *
     * Called by cmsupdate.ori to remove spellcheck index. Necessary because Solr/Lucene versions might have
     * incompatible changes, and deleting the index causes the spellcheck index to be rebuilt.
     */
    public void deleteSpellcheckIndex() {

        String dataPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("solr/spellcheck/data");
        File dataDir = new File(dataPath);
        if (dataDir.exists()) {
            try {
                Files.walkFileTree(dataDir.toPath(), new FileVisitor<Path>() {

                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                        dir.toFile().delete();
                        return FileVisitResult.CONTINUE;
                    }

                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                        return FileVisitResult.CONTINUE;
                    }

                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        file.toFile().delete();
                        return FileVisitResult.CONTINUE;
                    }

                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

                        return FileVisitResult.CONTINUE;

                    }
                });
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

    }

    /**
     * Returns html code to display an error.<p>
     *
     * @param pathPrefix to adjust the path
     *
     * @return html code
     */
    @Override
    public String displayError(String pathPrefix) {

        if (pathPrefix == null) {
            pathPrefix = "";
        }
        StringBuffer html = new StringBuffer(512);
        html.append("<table border='0' cellpadding='5' cellspacing='0' style='width: 100%; height: 100%;'>");
        html.append("\t<tr>");
        html.append("\t\t<td style='vertical-align: middle; height: 100%;'>");
        html.append(getHtmlPart("C_BLOCK_START", "Error"));
        html.append("\t\t\t<table border='0' cellpadding='0' cellspacing='0' style='width: 100%;'>");
        html.append("\t\t\t\t<tr>");
        html.append("\t\t\t\t\t<td><img src='").append(pathPrefix).append("resources/error.png' border='0'></td>");
        html.append("\t\t\t\t\t<td>&nbsp;&nbsp;</td>");
        html.append("\t\t\t\t\t<td style='width: 100%;'>");
        html.append("\t\t\t\t\t\tThe Alkacon OpenCms update wizard has not been started correctly!<br>");
        html.append("\t\t\t\t\t\tPlease click <a href='").append(pathPrefix);
        html.append("index.jsp'>here</a> to restart the wizard.");
        html.append("\t\t\t\t\t</td>");
        html.append("\t\t\t\t</tr>");
        html.append("\t\t\t</table>");
        html.append(getHtmlPart("C_BLOCK_END"));
        html.append("\t\t</td>");
        html.append("\t</tr>");
        html.append("</table>");
        return html.toString();
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
     * Gets the folder for config files.
     *
     * @return the folder for config files
     */
    public File getConfigFolder() {

        return new File(getWebAppRfsPath() + "WEB-INF/config");
    }

    /**
     * Returns the detected mayor version, based on DB structure.<p>
     *
     * @return the detected mayor version
     */
    public double getDetectedVersion() {

        return m_detectedVersion;
    }

    /**
     * Returns a map of all previously installed modules.<p>
     *
     * @return a map of <code>[String, {@link org.opencms.module.CmsModuleVersion}]</code> objects
     *
     * @see org.opencms.module.CmsModuleManager#getAllInstalledModules()
     */
    public Map<String, CmsModuleVersion> getInstalledModules() {

        String file = CmsModuleConfiguration.DEFAULT_XML_FILE_NAME;
        // /opencms/modules/module[?]
        String basePath = new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsModuleConfiguration.N_MODULES).append("/").append(CmsModuleXmlHandler.N_MODULE).append(
                "[?]/").toString();
        Map<String, CmsModuleVersion> modules = new HashMap<String, CmsModuleVersion>();
        String name = "";
        for (int i = 1; name != null; i++) {
            if (i > 1) {
                String ver = CmsModuleVersion.DEFAULT_VERSION;
                try {
                    ver = getXmlHelper().getValue(
                        file,
                        CmsStringUtil.substitute(basePath, "?", "" + (i - 1)) + CmsModuleXmlHandler.N_VERSION);
                } catch (@SuppressWarnings("unused") CmsXmlException e) {
                    // ignore
                }
                modules.put(name, new CmsModuleVersion(ver));
            }
            try {
                name = getXmlHelper().getValue(
                    file,
                    CmsStringUtil.substitute(basePath, "?", "" + i) + CmsModuleXmlHandler.N_NAME);
            } catch (@SuppressWarnings("unused") CmsXmlException e) {
                // ignore
            }
        }
        return modules;
    }

    /**
     * List of modules to be updated.<p>
     *
     * @return a list of module names
     */
    public List<String> getModulesToUpdate() {

        if (m_modulesToUpdate == null) {
            getUptodateModules();
            m_components = new CmsIdentifiableObjectContainer<CmsSetupComponent>(true, true);
            try {
                addComponentsFromPath(m_webAppRfsPath + FOLDER_UPDATE);
            } catch (CmsConfigurationException e) {
                //
            }
        }
        return m_modulesToUpdate;
    }

    /**
     * Returns the update database thread.<p>
     *
     * @return the update database thread
     */
    public CmsUpdateDBThread getUpdateDBThread() {

        return m_dbUpdateThread;
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
     * Returns the modules that does not need to be updated.<p>
     *
     * @return a list of module names
     */
    public List<String> getUptodateModules() {

        if (m_uptodateModules == null) {
            m_uptodateModules = new ArrayList<String>();
            m_modulesToUpdate = new ArrayList<String>();
            Map<String, CmsModuleVersion> installedModules = getInstalledModules();
            Map<String, CmsModule> availableModules = getAvailableModules();
            Iterator<Map.Entry<String, CmsModule>> itMods = availableModules.entrySet().iterator();
            while (itMods.hasNext()) {
                Map.Entry<String, CmsModule> entry = itMods.next();
                String name = entry.getKey();
                CmsModuleVersion instVer = installedModules.get(name);
                CmsModuleVersion availVer = entry.getValue().getVersion();
                boolean uptodate = (!UPDATE_ALL_MODULES) && ((instVer != null) && (instVer.compareTo(availVer) >= 0));
                if (uptodate) {
                    m_uptodateModules.add(name);
                } else {
                    m_modulesToUpdate.add(name);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        name + " --- installed: " + instVer + " available: " + availVer + " --- uptodate: " + uptodate);
                }
            }
        }
        return m_uptodateModules;
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
     * Gets the XML updater (lazily create it if it hasn't been created yet).
     *
     * @return the XML updater
     */
    public CmsXmlConfigUpdater getXmlConfigUpdater() {

        if (m_configUpdater == null) {
            m_configUpdater = new CmsXmlConfigUpdater(getXmlUpdateFolder(), getConfigFolder());
        }
        return m_configUpdater;
    }

    /**
     * Gets the folder for XML update files.
     *
     * @return the folder for XML update files
     */
    public File getXmlUpdateFolder() {

        return new File(new File(getWebAppRfsPath()), "WEB-INF/updatedata/xmlupdate");

    }

    /**
     * @see org.opencms.setup.CmsSetupBean#htmlModules()
     */
    @Override
    public String htmlModules() {

        StringBuffer html = new StringBuffer(1024);
        Set<String> uptodate = new HashSet<String>(getUptodateModules());
        Iterator<String> itModules = sortModules(getAvailableModules().values()).iterator();
        boolean hasModules = false;
        for (int i = 0; itModules.hasNext(); i++) {
            String moduleName = itModules.next();
            CmsModule module = getAvailableModules().get(moduleName);
            if (UPDATE_ALL_MODULES || !uptodate.contains(moduleName)) {
                html.append(htmlModule(module, i));
                hasModules = true;
            } else {
                html.append("<input type='hidden' name='availableModules' value='");
                html.append(moduleName);
                html.append("'>\n");
            }
        }
        if (!hasModules) {
            html.append("\t<tr>\n");
            html.append("\t\t<td style='vertical-align: middle;'>\n");
            html.append(Messages.get().getBundle().key(Messages.GUI_WARNING_ALL_MODULES_UPTODATE_0));
            html.append("\t\t</td>\n");
            html.append("\t</tr>\n");
        }
        return html.toString();
    }

    /**
     * Creates a new instance of the setup Bean.<p>
     *
     * @param webAppRfsPath path to the OpenCms web application
     * @param servletMapping the OpenCms servlet mapping
     * @param defaultWebApplication the name of the default web application
     */
    @Override
    public void init(String webAppRfsPath, String servletMapping, String defaultWebApplication) {

        try {
            super.init(webAppRfsPath, servletMapping, defaultWebApplication);
            CmsUpdateInfo.INSTANCE.setAdeModuleVersion(getInstalledModules().get("org.opencms.ade.containerpage"));

            if (m_workplaceUpdateThread != null) {
                if (m_workplaceUpdateThread.isAlive()) {
                    m_workplaceUpdateThread.kill();
                }
                m_workplaceUpdateThread = null;
            }
            if (m_dbUpdateThread != null) {
                if (m_dbUpdateThread.isAlive()) {
                    m_dbUpdateThread.kill();
                }
                m_dbUpdateThread = null;
                m_newLoggingOffset = 0;
                m_oldLoggingOffset = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the keep History parameter value.<p>
     *
     * @return the keep History parameter value
     */
    public boolean isKeepHistory() {

        return m_keepHistory;
    }

    /**
     * Returns <code>true</code> if a DB update is needed.<p>
     *
     * @return <code>true</code> if a DB update is needed
     */
    public boolean isNeedDbUpdate() {

        return m_detectedVersion != 8;
    }

    /**
     * Checks whether the selected user and password are valid and the user has the ROOT_ADMIN role.<p>
     *
     * @return <code>true</code> if the selected user and password are valid and the user has the ROOT_ADMIN role
     */
    public boolean isValidUser() {

        CmsShell shell = new CmsShell(
            getWebAppRfsPath() + "WEB-INF" + File.separator,
            getServletMapping(),
            getDefaultWebApplication(),
            "${user}@${project}>",
            Collections.emptyList());
        boolean validUser = shell.validateUser(getAdminUser(), getAdminPwd(), CmsRole.ROOT_ADMIN);
        shell.exit();
        return validUser;
    }

    /**
     * Prepares step 1 of the update wizard.<p>
     */
    public void prepareUpdateStep1() {

        // the MySQL driver class name has changed with OpenCms 11.0.0
        // it needs to be updated before any database access
        //updateDBDriverClassName();
    }

    /**
     * Prepares step 1 of the update wizard.<p>
     */
    public void prepareUpdateStep1b() {

        if (!isInitialized()) {
            return;
        }

        if ((m_dbUpdateThread != null) && (m_dbUpdateThread.isFinished())) {
            // update is already finished, just wait for client to collect final data
            return;
        }

        if (m_dbUpdateThread == null) {
            m_dbUpdateThread = new CmsUpdateDBThread(this);
        }

        if (!m_dbUpdateThread.isAlive()) {
            m_dbUpdateThread.start();
        }
    }

    /**
     * Generates the output for step 1 of the setup wizard.<p>
     *
     * @param out the JSP print stream
     * @throws IOException in case errors occur while writing to "out"
     */
    public void prepareUpdateStep1bOutput(JspWriter out) throws IOException {

        m_oldLoggingDBOffset = m_newLoggingDBOffset;
        m_newLoggingDBOffset = m_dbUpdateThread.getLoggingThread().getMessages().size();
        if (isInitialized()) {
            for (int i = m_oldLoggingDBOffset; i < m_newLoggingDBOffset; i++) {
                String str = m_dbUpdateThread.getLoggingThread().getMessages().get(i).toString();
                str = CmsEncoder.escapeWBlanks(str, CmsEncoder.ENCODING_UTF_8);
                out.println("output[" + (i - m_oldLoggingDBOffset) + "] = \"" + str + "\";");
            }
        } else {
            out.println("output[0] = 'ERROR';");
        }

        boolean threadFinished = m_dbUpdateThread.isFinished();
        boolean allWritten = m_oldLoggingDBOffset >= m_dbUpdateThread.getLoggingThread().getMessages().size();

        out.println("function initThread() {");
        if (isInitialized()) {
            out.print("send();");
            if (threadFinished && allWritten) {
                out.println("setTimeout('top.display.finish()', 1000);");
            } else {
                int timeout = 5000;
                if (getUpdateDBThread().getLoggingThread().getMessages().size() < 20) {
                    timeout = 2000;
                }
                out.println("setTimeout('location.reload()', " + timeout + ");");
            }
        }
        out.println("}");
    }

    /**
     * Prepares step 5 of the update wizard.<p>
     */
    public void prepareUpdateStep5() {

        if (isInitialized()) {
            try {
                String fileName = getWebAppRfsPath() + FOLDER_UPDATE + "cmsupdate";
                // read the file
                FileInputStream fis = new FileInputStream(fileName + CmsConfigurationManager.POSTFIX_ORI);
                String script = "";
                int readChar = fis.read();
                while (readChar > -1) {
                    script += (char)readChar;
                    readChar = fis.read();
                }
                fis.close();
                // substitute macros
                script = CmsStringUtil.substitute(script, C_ADMIN_USER, getAdminUser());
                script = CmsStringUtil.substitute(script, C_ADMIN_PWD, getAdminPwd());
                script = CmsStringUtil.substitute(script, C_UPDATE_PROJECT, getUpdateProject());
                script = CmsStringUtil.substitute(script, C_UPDATE_SITE, getUpdateSite());
                script = CmsStringUtil.substitute(script, C_ADMIN_GROUP, getAdminGroup());
                // write the new script
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
     * Prepares step 5 of the update wizard.<p>
     */
    public void prepareUpdateStep5b() {

        if (!isInitialized()) {
            return;
        }

        addSubscriptionDriver();

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
    public void prepareUpdateStep5bOutput(JspWriter out) throws IOException {

        if ((m_workplaceUpdateThread == null) || (m_workplaceUpdateThread.getLoggingThread() == null)) {
            return;
        }
        m_oldLoggingOffset = m_newLoggingOffset;
        m_newLoggingOffset = m_workplaceUpdateThread.getLoggingThread().getMessages().size();
        if (isInitialized()) {
            for (int i = m_oldLoggingOffset; i < m_newLoggingOffset; i++) {
                String str = m_workplaceUpdateThread.getLoggingThread().getMessages().get(i).toString();
                str = CmsEncoder.escapeWBlanks(str, CmsEncoder.ENCODING_UTF_8);
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

        Set<String> forced = new HashSet<String>();
        forced.add("driver.subscription");
        forced.add("db.subscription.driver");
        forced.add("db.subscription.pool");
        forced.add("db.subscription.sqlmanager");
        addSubscriptionDriver();
        if (isInitialized()) {
            // lock the wizard for further use
            lockWizard();
            // save Properties to file "opencms.properties"
            saveProperties(getProperties(), CmsSystemInfo.FILE_PROPERTIES, false, forced);
            deleteEmptyJars();
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
     * Sets the detected mayor version.<p>
     *
     * @param detectedVersion the value to set
     */
    public void setDetectedVersion(double detectedVersion) {

        m_detectedVersion = detectedVersion;
    }

    /**
     * Sets the keep History parameter value.<p>
     *
     * @param keepHistory the keep History parameter value to set
     */
    public void setKeepHistory(boolean keepHistory) {

        m_keepHistory = keepHistory;
    }

    /**
     * Sets the list of modules where the included libs should be preserved during update.<p>
     * Called from step_5_update_modules.jsp.<p>
     *
     * @param preserveLibModules the comma separated list of module names
     */
    public void setPreserveLibModules(String preserveLibModules) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(preserveLibModules)) {
            String[] modules = preserveLibModules.split(",");
            m_preserveLibModules = Arrays.asList(modules);
        } else {
            m_preserveLibModules = Collections.emptyList();
        }
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
    @Override
    public void shellExit() {

        System.out.println();
        System.out.println();
        System.out.println("The update is finished!\nThe OpenCms system used for the update will now shut down.");
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellStart()
     */
    @Override
    public void shellStart() {

        System.out.println();
        System.out.println("Starting Workplace update for OpenCms!");

        String[] copy = org.opencms.main.Messages.COPYRIGHT_BY_ALKACON;
        for (int i = copy.length - 1; i >= 0; i--) {
            System.out.println(copy[i]);
        }
        System.out.println(
            "This is OpenCms "
                + OpenCms.getSystemInfo().getVersionNumber()
                + " ["
                + OpenCms.getSystemInfo().getVersionId()
                + "]");
        System.out.println();
        System.out.println();
    }

    /**
     * Updates the JDBC driver class names.<p>
     * Needs to be executed before any database access.<p>
     */
    public void updateDBDriverProperties() {

        Map<String, String> modifiedElements = new HashMap<String, String>();
        // replace MySQL JDBC driver class name
        CmsParameterConfiguration properties = getProperties();
        for (Entry<String, String> propertyEntry : properties.entrySet()) {
            if (MYSQL_DRIVER_CLASS_OLD.equals(propertyEntry.getValue())
                || MYSQL_DRIVER_CLASS_NEW.equals(propertyEntry.getValue())) {
                modifiedElements.put(propertyEntry.getKey(), MYSQL_DRIVER_CLASS_MARIADB);
            }
        }

        if (properties.containsValue(MYSQL_DRIVER_CLASS_MARIADB)
            || properties.containsValue(MYSQL_DRIVER_CLASS_NEW)
            || properties.containsValue(MYSQL_DRIVER_CLASS_OLD)) {
            for (Entry<String, String> propertyEntry : properties.entrySet()) {
                if (MYSQL_DRIVER_CLASS_OLD.equals(propertyEntry.getValue())
                    || MYSQL_DRIVER_CLASS_NEW.equals(propertyEntry.getValue())
                    || MYSQL_DRIVER_CLASS_MARIADB.equals(propertyEntry.getValue())) {

                    String mysqlkey = propertyEntry.getKey().substring(0, propertyEntry.getKey().lastIndexOf("."));
                    String parameterKey = mysqlkey + ".jdbcUrl.params";
                    String currentParameter = properties.get(parameterKey);
                    String modifiedParameter = currentParameter;
                    if (modifiedParameter == null) {
                        modifiedParameter = "";
                    }
                    if (!modifiedParameter.contains("serverTimezone")) {
                        String parameterSeperator = "?";
                        if (modifiedParameter.contains("?")) {
                            parameterSeperator = "&";
                        }
                        modifiedParameter = currentParameter + parameterSeperator + "serverTimezone=UTC";
                    }
                    if (modifiedParameter.contains("useSSL=false")
                        && !modifiedParameter.contains("allowPublicKeyRetrieval")) {
                        modifiedParameter = currentParameter + "&" + "allowPublicKeyRetrieval=true";
                    }
                    if (!Objects.equal(modifiedParameter, currentParameter)) {
                        modifiedElements.put(parameterKey, modifiedParameter);
                    }
                    parameterKey = mysqlkey + ".jdbcUrl";
                    currentParameter = properties.get(parameterKey);
                    if ((currentParameter != null) && currentParameter.startsWith("jdbc:mysql:")) {
                        modifiedParameter = "jdbc:mariadb:" + currentParameter.substring(11);
                        modifiedElements.put(parameterKey, modifiedParameter);
                    }
                }
            }
        }

        for (String key : modifiedElements.keySet()) {
            properties.put(key, modifiedElements.get(key));
        }

        if (!modifiedElements.isEmpty()) {
            saveProperties(properties, CmsSystemInfo.FILE_PROPERTIES, false, modifiedElements.keySet());
        }
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

        if (m_cms != null) {

            I_CmsReport report = new CmsShellReport(m_cms.getRequestContext().getLocale());

            // remove obsolete modules in any case
            for (String moduleToRemove : getModulesToDelete()) {
                removeModule(moduleToRemove, report);
            }

            // check if there are any modules to install
            if (m_installModules != null) {
                Set<String> utdModules = new HashSet<String>(getUptodateModules());
                List<String> installList = Lists.newArrayList(m_installModules);
                for (String name : installList) {
                    if (!utdModules.contains(name)) {
                        String filename = m_moduleFilenames.get(name);
                        try {
                            updateModule(name, filename, report);
                        } catch (Exception e) {
                            // log a exception during module import, but make sure the next module is still imported
                            e.printStackTrace(System.err);
                        }
                    } else {
                        report.println(
                            Messages.get().container(Messages.RPT_MODULE_UPTODATE_1, name),
                            I_CmsReport.FORMAT_HEADLINE);
                    }
                }
            }
        }
    }

    /**
     * Fills the relations db tables during the update.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void updateRelations() throws Exception {

        if (m_detectedVersion > 6) {
            // skip if not updating from 6.x
            return;
        }
        I_CmsReport report = new CmsShellReport(m_cms.getRequestContext().getLocale());

        report.println(Messages.get().container(Messages.RPT_START_UPDATE_RELATIONS_0), I_CmsReport.FORMAT_HEADLINE);

        String storedSite = m_cms.getRequestContext().getSiteRoot();
        CmsProject project = null;
        try {
            String projectName = "Update relations project";
            try {
                // try to read a (leftover) unlock project
                project = m_cms.readProject(projectName);
            } catch (@SuppressWarnings("unused") CmsException e) {
                // create a Project to unlock the resources
                project = m_cms.createProject(
                    projectName,
                    projectName,
                    OpenCms.getDefaultUsers().getGroupAdministrators(),
                    OpenCms.getDefaultUsers().getGroupAdministrators(),
                    CmsProject.PROJECT_TYPE_TEMPORARY);
            }

            m_cms.getRequestContext().setSiteRoot(""); // change to the root site
            m_cms.getRequestContext().setCurrentProject(project);

            List<I_CmsResourceType> types = OpenCms.getResourceManager().getResourceTypes();
            int n = types.size();
            int m = 0;
            Iterator<I_CmsResourceType> itTypes = types.iterator();
            while (itTypes.hasNext()) {
                I_CmsResourceType type = itTypes.next();
                m++;
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(m),
                        String.valueOf(n)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        type.getTypeName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                if (type instanceof I_CmsLinkParseable) {
                    try {
                        CmsXmlContentRepairSettings settings = new CmsXmlContentRepairSettings(m_cms);
                        settings.setIncludeSubFolders(true);
                        settings.setVfsFolder("/");
                        settings.setForce(true);
                        settings.setResourceType(type.getTypeName());

                        CmsXmlContentRepairThread t = new CmsXmlContentRepairThread(m_cms, settings);
                        t.start();

                        synchronized (this) {
                            t.join();
                        }
                        report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                            I_CmsReport.FORMAT_OK);
                    } catch (Exception e) {
                        report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ERROR_0),
                            I_CmsReport.FORMAT_ERROR);
                        report.addError(e);
                        // log the error
                        e.printStackTrace(System.err);
                    }
                } else {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                        I_CmsReport.FORMAT_WARNING);
                }
            }
        } finally {
            try {
                if (project != null) {
                    try {
                        m_cms.unlockProject(project.getUuid()); // unlock everything
                        OpenCms.getPublishManager().publishProject(
                            m_cms,
                            report,
                            OpenCms.getPublishManager().getPublishList(m_cms));
                        OpenCms.getPublishManager().waitWhileRunning();
                    } finally {
                        m_cms.getRequestContext().setCurrentProject(m_cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    }
                }
            } finally {
                m_cms.getRequestContext().setSiteRoot(storedSite);
            }
            report.println(
                Messages.get().container(Messages.RPT_FINISH_UPDATE_RELATIONS_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
    }

    /**
     * Returns the admin Group.<p>
     *
     * @return the admin Group
     */
    protected String getAdminGroup() {

        return m_adminGroup;
    }

    /**
     * Computes a list of modules which need to be removed before updating the other modules, e.g. because of resource type
     * conflicts.<p>
     *
     * @return the list of names of modules which need to be removed
     */
    protected List<String> getModulesToDelete() {

        List<String> result = new ArrayList<String>();
        for (int i = 0; i < OBSOLETE_MODULES.length; i++) {
            if (OpenCms.getModuleManager().hasModule(OBSOLETE_MODULES[i])) {
                result.add(OBSOLETE_MODULES[i]);
            }
        }
        return result;
    }

    /**
     * Removes a module.<p>
     *
     * @param moduleName the name of the module to remove
     * @param report the report to write to
     *
     * @throws CmsException in case something goes wrong
     */
    protected void removeModule(String moduleName, I_CmsReport report) throws CmsException {

        if (OpenCms.getModuleManager().getModule(moduleName) != null) {
            OpenCms.getModuleManager().deleteModule(
                m_cms,
                moduleName,
                true,
                m_preserveLibModules.contains(moduleName),
                report);
        }
    }

    /**
     * Sets the admin Group.<p>
     *
     * @param adminGroup the admin Group to set
     */
    protected void setAdminGroup(String adminGroup) {

        m_adminGroup = adminGroup;
    }

    /**
     * Imports a module (zipfile) from the default module directory,
     * creating a temporary project for this.<p>
     *
     * @param moduleName the name of the module to replace
     * @param importFile the name of the import .zip file located in the update module directory
     * @param report the shell report to write the output
     *
     * @throws Exception if something goes wrong
     *
     * @see org.opencms.importexport.CmsImportExportManager#importData(org.opencms.file.CmsObject, I_CmsReport, org.opencms.importexport.CmsImportParameters)
     */
    protected void updateModule(String moduleName, String importFile, I_CmsReport report) throws Exception {

        String fileName = getModuleFolder() + importFile;

        report.println(
            Messages.get().container(Messages.RPT_BEGIN_UPDATE_MODULE_1, moduleName),
            I_CmsReport.FORMAT_HEADLINE);
        removeModule(moduleName, report);
        OpenCms.getPublishManager().stopPublishing();
        OpenCms.getPublishManager().startPublishing();
        OpenCms.getPublishManager().waitWhileRunning();
        OpenCms.getImportExportManager().importData(m_cms, report, new CmsImportParameters(fileName, "/", true));
        report.println(
            Messages.get().container(Messages.RPT_END_UPDATE_MODULE_1, moduleName),
            I_CmsReport.FORMAT_HEADLINE);
        OpenCms.getPublishManager().stopPublishing();
        OpenCms.getPublishManager().startPublishing();
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Marks all empty jars for deletion on VM exit.<p>
     */
    private void deleteEmptyJars() {

        File libFolder = new File(getLibFolder());
        if (libFolder.exists()) {
            File[] emptyJars = libFolder.listFiles(new FileFilter() {

                public boolean accept(File pathname) {

                    if (pathname.getName().endsWith(".jar")) {
                        FileInputStream fileInput = null;
                        JarInputStream jarStream = null;
                        try {
                            fileInput = new FileInputStream(pathname);
                            jarStream = new JarInputStream(fileInput);
                            // check the manifest for the empty jar marker attribute
                            Manifest mf = jarStream.getManifest();
                            Attributes att = mf.getMainAttributes();
                            if ((att != null) && "true".equals(att.getValue(EMPTY_JAR_ATTRIBUTE_KEY))) {
                                return true;
                            }
                        } catch (Exception e) {
                            LOG.warn(e.getMessage(), e);
                        } finally {
                            if (jarStream != null) {
                                try {
                                    jarStream.close();
                                } catch (IOException e) {
                                    LOG.warn(e.getMessage(), e);
                                }
                            }
                            if (fileInput != null) {
                                try {
                                    fileInput.close();
                                } catch (IOException e) {
                                    LOG.warn(e.getMessage(), e);
                                }
                            }
                        }
                    }
                    return false;
                }
            });
            for (int i = 0; i < emptyJars.length; i++) {
                emptyJars[i].deleteOnExit();
            }
        }
    }

    /**
     * Gets the database package name part.<p>
     *
     * @param dbName the db name from the opencms.properties file
     *
     * @return the db package name part
     */
    private String getDbPackage(String dbName) {

        if (dbName.contains("mysql")) {
            return "mysql";
        } else if (dbName.contains("oracle")) {
            return "oracle";
        } else {
            return dbName;
        }
    }
}
