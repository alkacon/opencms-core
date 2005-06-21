/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupBean.java,v $
 * Date   : $Date: 2005/06/21 15:50:00 $
 * Version: $Revision: 1.31 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.setup;

import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsShell;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsShellCommands;
import org.opencms.main.Messages;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.CmsShellReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.ExtendedProperties;

/**
 * A java bean as a controller for the OpenCms setup wizard.<p>
 * 
 * It is not allowed to customize this bean with methods for a specific database server setup!<p>
 * 
 * Database server specific settings should be set/read using get/setDbProperty, as for example like:
 * 
 * <pre>
 * setDbProperty("oracle.defaultTablespace", value);
 * </pre>
 *
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.31 $ 
 */
public class CmsSetupBean extends Object implements Serializable, Cloneable, I_CmsShellCommands {

    /** Name of the property file containing HTML fragments for setup wizard and error dialog. */
    public static final String HTML_MESSAGE_FILE = "org/opencms/setup/htmlmsg.properties";

    /** DB provider constant. */
    public static final String C_GENERIC_PROVIDER = "generic";

    /** DB provider constant. */
    public static final String C_MYSQL_PROVIDER = "mysql";

    /** DB provider constant. */
    public static final String C_ORACLE_PROVIDER = "oracle";

    /** DB provider constant. */
    public static final String C_POSTGRESQL_PROVIDER = "postgresql";

    /** Required files per database server setup. */
    public static final String[] REQUIRED_DB_SETUP_FILES = {
        "step_4_database_setup.jsp",
        "database.properties",
        "create_db.sql",
        "create_tables.sql",
        "drop_db.sql",
        "drop_tables.sql"};

    /** Contains HTML fragments for the output in the JSP pages of the setup wizard. */
    private static Properties m_htmlProps;

    /** A map with all available modules. */
    private Map m_availableModules;

    /** A CmsObject to execute shell commands. */
    private CmsObject m_cms;

    /** The absolute path to the config sub directory of the OpenCms web application. */
    private String m_configRfsPath;

    /** Key of the selected database server (e.g. "mysql", "generic" or "oracle") */
    private String m_databaseKey;

    /** List of keys of all available database server setups (e.g. "mysql", "generic" or "oracle") */
    private List m_databaseKeys;

    /** Map of database setup properties of all available database server setups keyed by their database keys. */
    private Map m_databaseProperties;

    /** Password used for the JDBC connection when the OpenCms database is created. */
    private String m_dbCreatePwd;

    /** The name of the default web application (in web.xml). */
    private String m_defaultWebApplication;

    /** Contains the error messages to be displayed in the setup wizard. */
    private Vector m_errors;

    /** Contains the properties of "opencms.properties". */
    private ExtendedProperties m_extProperties;

    /** A list with the package names of the modules to be installed .*/
    private List m_installModules;

    /** A map with lists of dependent module package names keyed by module package names. */
    private Map m_moduleDependencies;

    /** The new logging offset in the workplace import thread. */
    private int m_newLoggingOffset;

    /** The lod logging offset in the workplace import thread. */
    private int m_oldLoggingOffset;

    /** A map with tokens ${...} to be replaced in SQL scripts. */
    private Map m_replacer;

    /** The servlet mapping (in web.xml). */
    private String m_servletMapping;

    /** List of sorted keys by ranking of all available database server setups (e.g. "mysql", "generic" or "oracle") */
    private List m_sortedDatabaseKeys;

    /** The absolute path to the home directory of the OpenCms webapp. */
    private String m_webAppRfsPath;

    /** The workplace import thread. */
    private CmsSetupWorkplaceImportThread m_workplaceImportThread;

    /** 
     * Default constructor.<p>
     */
    public CmsSetupBean() {

        initHtmlParts();
    }

    /**
     * Restores the opencms.xml either to or from a backup file, depending
     * whether the setup wizard is executed the first time (the backup registry
     * doesnt exist) or not (the backup registry exists).
     * 
     * @param filename something like e.g. "opencms-xml.ori"
     * @param originalFilename the configurations real file name, e.g. "opencms.xml"
     */
    public void backupConfiguration(String filename, String originalFilename) {

        // ensure backup folder exists
        File backupFolder = new File(m_configRfsPath + File.separatorChar + "backup");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        // copy file to (or from) backup folder
        originalFilename = "backup" + File.separatorChar + originalFilename;
        File file = new File(m_configRfsPath + originalFilename);
        if (file.exists()) {
            copyFile(originalFilename, filename);
        } else {
            copyFile(filename, originalFilename);
        }
    }

    /**
     * Checks the ethernet address value and generates a dummy address, if necessary.<p>     *
     */
    public void checkEthernetAddress() {

        // check the ethernet address in order to generate a random address, if not available                   
        if (CmsStringUtil.isEmpty(getEthernetAddress())) {
            setEthernetAddress(CmsUUID.getDummyEthernetAddress());
        }
    }

    /** 
     * Copies a given file.<p>
     * 
     * @param source the source file
     * @param target the destination file
     */
    public void copyFile(String source, String target) {

        try {
            CmsFileUtil.copy(m_configRfsPath + source, m_configRfsPath + target);
        } catch (IOException e) {
            m_errors.addElement("Could not copy " + source + " to " + target + " \n");
            m_errors.addElement(e.toString() + "\n");
        }
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

            // open the folder "/WEB-INF/packages/modules/"
            File packagesFolder = new File(m_webAppRfsPath
                + "WEB-INF"
                + File.separator
                + "packages"
                + File.separator
                + "modules");

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
     * Returns the "config" path in the OpenCms web application.<p>
     * 
     * @return the config path
     */
    public String getConfigRfsPath() {

        return m_configRfsPath;
    }

    /** 
     * Returns the key of the selected database server (e.g. "mysql", "generic" or "oracle").<p>
     * 
     * @return the key of the selected database server (e.g. "mysql", "generic" or "oracle")
     */
    public String getDatabase() {

        if (m_databaseKey == null) {
            m_databaseKey = getExtProperty("db.name");
        }

        if (m_databaseKey == null || "".equals(m_databaseKey)) {
            m_databaseKey = (String)getSortedDatabases().get(0);
        }

        return m_databaseKey;
    }

    /**
     * Returns the URI of a database config page (in step 3) for a specified database key.<p>
     * 
     * 
     * @param key the database key (e.g. "mysql", "generic" or "oracle")
     * @return the URI of a database config page
     */
    public String getDatabaseConfigPage(String key) {

        return "database"
            + "/"
            + key
            + "/"
            + "step_4_database_setup.jsp";
    }

    /**
     * Returns the clear text name for a database server setup specified by a database key (e.g. "mysql", "generic" or "oracle").<p>
     * 
     * @param databaseKey a database key (e.g. "mysql", "generic" or "oracle")
     * @return the clear text name for a database server setup
     */
    public String getDatabaseName(String databaseKey) {

        return (String)((Map)getDatabaseProperties().get(databaseKey)).get(databaseKey + ".name");
    }

    /** 
     * Returns a map with the database properties of *all* available database configurations keyed
     * by their database keys (e.g. "mysql", "generic" or "oracle").<p>
     * 
     * @return a map with the database properties of *all* available database configurations
     */
    public Map getDatabaseProperties() {

        if (m_databaseProperties != null) {
            return m_databaseProperties;
        }

        readDatabaseConfig();
        return m_databaseProperties;
    }

    /**
     * Returns a list with they keys (e.g. "mysql", "generic" or "oracle") of all available
     * database server setups found in "/setup/database/".<p>
     * 
     * @return a list with they keys (e.g. "mysql", "generic" or "oracle") of all available database server setups
     */
    public List getDatabases() {

        File databaseSetupFolder = null;
        File[] childResources = null;
        File childResource = null;
        File setupFile = null;
        boolean hasMissingSetupFiles = false;

        if (m_databaseKeys != null) {
            return m_databaseKeys;
        }

        try {
            m_databaseKeys = new ArrayList();
            databaseSetupFolder = new File(m_webAppRfsPath + File.separator + "setup" + File.separator + "database");

            if (databaseSetupFolder.exists()) {
                childResources = databaseSetupFolder.listFiles();

                if (childResources != null) {
                    for (int i = 0; i < childResources.length; i++) {
                        childResource = childResources[i];
                        hasMissingSetupFiles = false;

                        if (childResource.exists() && childResource.isDirectory() && childResource.canRead()) {
                            for (int j = 0; j < REQUIRED_DB_SETUP_FILES.length; j++) {
                                setupFile = new File(childResource.getPath()
                                    + File.separator
                                    + REQUIRED_DB_SETUP_FILES[j]);

                                if (!setupFile.exists() || !setupFile.isFile() || !setupFile.canRead()) {
                                    hasMissingSetupFiles = true;
                                    System.err.println("["
                                        + getClass().getName()
                                        + "] missing or unreadable database setup file: "
                                        + setupFile.getPath());
                                    break;
                                }
                            }

                            if (!hasMissingSetupFiles) {
                                m_databaseKeys.add(childResource.getName().trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }

        return m_databaseKeys;
    }

    /** 
     * Returns the database name.<p>
     *
     * @return the database name
     */
    public String getDb() {

        return getDbProperty(m_databaseKey + ".dbname");
    }

    /**
     * Returns the JDBC connect URL parameters.<p>
     * 
     * @return the JDBC connect URL parameters
     */
    public String getDbConStrParams() {

        String str = null;
        str = getDbProperty(m_databaseKey + ".constr.params");
        return str;
    }

    /** 
     * Returns the database create statement.<p>
     * 
     * @return the database create statement
     */
    public String getDbCreateConStr() {

        String str = null;
        str = getDbProperty(m_databaseKey + ".constr");
        return str;
    }

    /** 
     * Returns the password used for database creation.<p>
     *
     * @return the password used for database creation
     */
    public String getDbCreatePwd() {

        return (m_dbCreatePwd != null) ? m_dbCreatePwd : "";
    }

    /** 
     * Returns the database user that is used to connect to the database.<p>
     * 
     * @return the database user
     */
    public String getDbCreateUser() {

        return getDbProperty(m_databaseKey + ".user");
    }

    /** 
     * Returns the database driver belonging to the database
     * from the default configuration.<p>
     *
     * @return name of the database driver 
     */
    public String getDbDriver() {

        return getDbProperty(m_databaseKey + ".driver");
    }

    /** 
     * Returns the value for a given key from the database properties.
     * 
     * @param key the property key
     * @return the string value for a given key
     */
    public String getDbProperty(String key) {

        Object value = null;

        // extract the database key out of the entire key
        String databaseKey = key.substring(0, key.indexOf('.'));
        Map databaseProperties = (Map)getDatabaseProperties().get(databaseKey);

        return ((value = databaseProperties.get(key)) != null) ? (String)value : "";
    }

    /** 
     * Returns the validation query belonging to the database
     * from the default configuration .<p>
     *
     * @return query used to validate connections 
     */
    public String getDbTestQuery() {

        return getDbProperty(m_databaseKey + ".testQuery");
    }

    /** 
     * Returns a connection string.<p>
     *
     * @return the connection string used by the OpenCms core  
     */
    public String getDbWorkConStr() {

        String str = getExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + ".jdbcUrl");
        return str;
    }

    /** 
     * Returns the password of the database from the properties .<p>
     *
     * @return the password for the OpenCms database user 
     */
    public String getDbWorkPwd() {

        return getExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + ".password");
    }

    /** 
     * Returns the user of the database from the properties.<p>
     * 
     * @return the database user used by the opencms core  
     */
    public String getDbWorkUser() {

        return getExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + ".user");
    }

    /** 
     * Returns the default content encoding.<p>
     * @return String
     */
    public String getDefaultContentEncoding() {

        return getExtProperty("defaultContentEncoding");
    }

    /** 
     * Returns the name of the default web application, configured in <code>web.xml</code>.<p>
     *
     * By default this is <code>"ROOT"</code>.<p>
     *
     * @return the name of the default web application, configured in <code>web.xml</code>
     */
    public String getDefaultWebApplication() {

        return m_defaultWebApplication;
    }

    /** 
     * Returns the error messages.<p>
     * 
     * @return a vector of error messages 
     */
    public Vector getErrors() {

        return m_errors;
    }

    /** 
     * Returns the mac ethernet address.<p>
     * 
     * @return the mac ethernet addess
     */
    public String getEthernetAddress() {

        return getExtProperty("server.ethernet.address");
    }

    /** 
     * Returns a help image icon tag to display a help text in the setup wizard.<p>
     * 
     * @param id the id of the desired help div
     * @param pathPrefix the path prefix to the image 
     * @return the HTML part for the help icon or an empty String, if the part was not found
     */
    public String getHtmlHelpIcon(String id, String pathPrefix) {

        String value = m_htmlProps.getProperty("C_HELP_IMG");
        if (value == null) {
            return "";
        } else {
            value = CmsStringUtil.substitute(value, "$replace$", id);
            return CmsStringUtil.substitute(value, "$path$", pathPrefix);
        }
    }

    /** 
     * Returns the specified HTML part of the HTML property file to create the output.<p>
     * 
     * @param part the name of the desired part
     * @return the HTML part or an empty String, if the part was not found
     */
    public String getHtmlPart(String part) {

        return getHtmlPart(part, "");
    }

    /** 
     * Returns the specified HTML part of the HTML property file to create the output.<p>
     * 
     * @param part the name of the desired part
     * @param replaceString String which is inserted in the found HTML part at the location of "$replace$"
     * @return the HTML part or an empty String, if the part was not found
     */
    public String getHtmlPart(String part, String replaceString) {

        String value = m_htmlProps.getProperty(part);
        if (value == null) {
            return "";
        } else {
            return CmsStringUtil.substitute(value, "$replace$", replaceString);
        }
    }

    /**
     * Returns a map with lists of dependent module package names keyed by module package names.<p>
     * 
     * @return a map with lists of dependent module package names keyed by module package names
     */
    public Map getModuleDependencies() {

        getAvailableModules();
        return m_moduleDependencies;
    }

    /**
     * Gets the default pool.<p>
     * 
     * @return name of the default pool 
     */
    public String getPool() {

        StringTokenizer tok = new StringTokenizer(getExtProperty("db.pools"), ",[]");
        String pool = tok.nextToken();
        return pool;
    }

    /**
     * Returns the extended properties.<p>
     * 
     * @return the extended properties  
     */
    public ExtendedProperties getProperties() {

        return m_extProperties;
    }

    /**
     * Returns the replacer.<p>
     * 
     * @return the replacer
     */
    public Map getReplacer() {

        return m_replacer;
    }

    /**
     * Return the OpenCms server name.<p>
     * 
     * @return the OpenCms server name
     */
    public String getServerName() {

        return getExtProperty("server.name");
    }

    /**
     * Returns the OpenCms servlet mapping, configured in <code>web.xml</code>.<p>
     * 
     * By default this is <code>"/opencms/*"</code>.<p>
     * 
     * @return the OpenCms servlet mapping, configured in <code>web.xml</code>
     */
    public String getServletMapping() {

        return m_servletMapping;
    }

    /**
     * Returns the name of the setup log file.<p>
     * 
     * @return the name of the setup log file
     */
    public String getSetupLogName() {

        StringBuffer result = new StringBuffer(m_webAppRfsPath).append("WEB-INF");
        result.append(File.separator).append("logs").append(File.separator).append("setup.log");
        return result.toString();
    }

    /** 
     * Returns a sorted list with they keys (e.g. "mysql", "generic" or "oracle") of all available
     * database server setups found in "/setup/database/" sorted by their ranking property.<p>
     *
     * @return a sorted list with they keys (e.g. "mysql", "generic" or "oracle") of all available database server setups
     */
    public List getSortedDatabases() {

        if (m_sortedDatabaseKeys == null) {
            List databases = m_databaseKeys;
            List sortedDatabases = new ArrayList(databases.size());
            SortedMap mappedDatabases = new TreeMap();
            for (int i = 0; i < databases.size(); i++) {
                String key = (String)databases.get(i);
                Integer ranking = new Integer(0);
                try {
                    ranking = Integer.valueOf(getDbProperty(key + ".ranking"));
                } catch (Exception e) {
                    // ignore
                }
                mappedDatabases.put(ranking, key);
            }

            while (mappedDatabases.size() > 0) {
                // get database with highest ranking 
                Integer key = (Integer)mappedDatabases.lastKey();
                String database = (String)mappedDatabases.get(key);
                sortedDatabases.add(database);
                mappedDatabases.remove(key);
            }
            m_sortedDatabaseKeys = new ArrayList(databases.size());
            m_sortedDatabaseKeys = sortedDatabases;
        }
        return m_sortedDatabaseKeys;
    }

    /** 
     * Returns the absolute path to the OpenCms home directory.<p>
     * 
     * @return the path to the OpenCms home directory 
     */
    public String getWebAppRfsPath() {

        return m_webAppRfsPath;
    }

    /**
     * Checks if the setup wizard is enabled.<p>
     * 
     * @return true if the setup wizard is enables, false otherwise
     */
    public boolean getWizardEnabled() {

        return "true".equals(getExtProperty("wizard.enabled"));
    }

    /**
     * Returns the workplace import thread.<p>
     * 
     * @return the workplace import thread
     */
    public CmsSetupWorkplaceImportThread getWorkplaceImportThread() {

        return m_workplaceImportThread;
    }

    /**
     * Return the OpenCms workplace site.<p>
     *
     * @return the OpenCms workplace site
     */
    public String getWorkplaceSite() {

        return getExtProperty("site.workplace");
    }

    /**
     * Installed all modules that have been set using {@link #setInstallModules(String)}.<p>
     * 
     * This method is invoked as a shell command.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void importModulesFromSetupBean() throws Exception {

        Map module = null;
        String filename = null;

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
                module = (Map)m_availableModules.get(m_installModules.get(i));
                filename = (String)module.get("filename");
                try {
                    importModuleFromDefault(filename);
                } catch (Exception e) {
                    // log a exception during module import, but make sure the next module is still imported
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /** 
     * Creates a new instance of the setup Bean from a JSP page.<p>
     * 
     * @param pageContext the JSP's page context
     */
    public void init(PageContext pageContext) {

        // check for OpenCms installation directory path
        String webAppRfsPath = pageContext.getServletConfig().getServletContext().getRealPath("/");

        // read the the OpenCms servlet mapping from the servlet context parameters
        String servletMapping = pageContext.getServletContext().getInitParameter(
            OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_SERVLET);

        // read the the default context name from the servlet context parameters
        String defaultWebApplication = pageContext.getServletContext().getInitParameter(
            OpenCmsServlet.SERVLET_PARAM_DEFAULT_WEB_APPLICATION);

        init(webAppRfsPath, servletMapping, defaultWebApplication);
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
            // explicit set to null to overwrite exiting values from session
            m_availableModules = null;
            m_databaseKey = null;
            m_databaseKeys = null;
            m_databaseProperties = null;
            m_extProperties = null;
            m_installModules = null;
            m_moduleDependencies = null;
            m_sortedDatabaseKeys = null;

            if (servletMapping == null) {
                servletMapping = "/opencms/*";
            }
            if (defaultWebApplication == null) {
                defaultWebApplication = "ROOT";
            }
            m_servletMapping = servletMapping;
            m_defaultWebApplication = defaultWebApplication;

            setWebAppRfsPath(webAppRfsPath);
            m_errors = new Vector();

            if (CmsStringUtil.isNotEmpty(webAppRfsPath)) {
                // workaround for JUnit test cases, this must not be executed in a test case
                m_extProperties = loadProperties(m_configRfsPath + "opencms.properties");
                readDatabaseConfig();
            }

            if (m_workplaceImportThread != null) {
                if (m_workplaceImportThread.isAlive()) {
                    m_workplaceImportThread.kill();
                }
                m_workplaceImportThread = null;
                m_newLoggingOffset = 0;
                m_oldLoggingOffset = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            m_errors.add(e.toString());
        }
    }

    /**
     * This method reads the properties from the htmlmsg.property file
     * and sets the HTML part properties with the matching values.<p>
     */
    public void initHtmlParts() {

        if (m_htmlProps != null) {
            // html already initialized
            return;
        }
        try {
            m_htmlProps = new Properties();
            m_htmlProps.load(getClass().getClassLoader().getResourceAsStream(HTML_MESSAGE_FILE));
        } catch (Exception e) {
            e.printStackTrace();
            m_errors.add(e.toString());
        }
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#initShellCmsObject(org.opencms.file.CmsObject, org.opencms.main.CmsShell)
     */
    public void initShellCmsObject(CmsObject cms, CmsShell shell) {

        m_cms = cms;
    }

    /**
     * Over simplistic helper to compare two strings to check radio buttons.
     * 
     * @param value1 the first value 
     * @param value2 the secound value
     * @return "checked" if both values are equal, the empty String "" otherwise
     */
    public String isChecked(String value1, String value2) {

        if (value1 == null || value2 == null) {
            return "";
        }

        if (value1.trim().equalsIgnoreCase(value2.trim())) {
            return "checked";
        }

        return "";
    }

    /**
     * Returns true if this setup bean is correctly initialized.<p>
     * 
     * @return true if this setup bean is correctly initialized
     */
    public boolean isInitialized() {

        return m_extProperties != null;
    }

    /**
     * Loads the default OpenCms properties.<p>
     * 
     * @param file the file tp read the properties from
     * @return the initialized OpenCms properties
     * @throws IOException in case of IO errors 
     */
    public ExtendedProperties loadProperties(String file) throws IOException {

        return CmsPropertyUtils.loadProperties(file);
    }

    /**
     * Locks (i.e. disables) the setup wizard.<p>
     *
     */
    public void lockWizard() {

        setExtProperty("wizard.enabled", "false");
    }

    /**
     * Prepares step 10 of the setup wizard.<p>
     */
    public void prepareStep10() {

        if (isInitialized()) {
            // lock the wizard for further use 
            lockWizard();
            // save Properties to file "opencms.properties" 
            saveProperties(getProperties(), "opencms.properties", false);
        }
    }

    /**
     * Prepares step 8 of the setup wizard.<p>
     * 
     * @return true if the workplace should be imported
     */
    public boolean prepareStep8() {

        if (isInitialized()) {
            checkEthernetAddress();
            // backup the XML configuration
            backupConfiguration("opencms-importexport.xml", "opencms-importexport.xml.ori");
            backupConfiguration("opencms-modules.xml", "opencms-modules.xml.ori");
            backupConfiguration("opencms-search.xml", "opencms-search.xml.ori");
            backupConfiguration("opencms-system.xml", "opencms-system.xml.ori");
            backupConfiguration("opencms-vfs.xml", "opencms-vfs.xml.ori");
            backupConfiguration("opencms-workplace.xml", "opencms-workplace.xml.ori");
            backupConfiguration("opencms.xml", "opencms.xml.ori");
            // save Properties to file "opencms.properties" 
            saveProperties(getProperties(), "opencms.properties", true);
        }
        return true;
    }

    /**
     * Prepares step 8b of the setup wizard.<p>
     */
    public void prepareStep8b() {

        if (!isInitialized()) {
            return;
        }

        if ((m_workplaceImportThread != null) && (m_workplaceImportThread.isFinished())) {
            // setup is already finished, just wait for client to collect final data
            return;
        }

        if (m_workplaceImportThread == null) {
            m_workplaceImportThread = new CmsSetupWorkplaceImportThread(this);
        }

        if (!m_workplaceImportThread.isAlive()) {
            m_workplaceImportThread.start();
        }
    }

    /**
     * Generates the output for step 8b of the setup wizard.<p>
     * 
     * @param out the JSP print stream
     * @throws IOException in case errors occur while writing to "out"
     */
    public void prepareStep8bOutput(JspWriter out) throws IOException {

        m_oldLoggingOffset = m_newLoggingOffset;
        m_newLoggingOffset = m_workplaceImportThread.getLoggingThread().getMessages().size();
        if (isInitialized()) {
            for (int i = m_oldLoggingOffset; i < m_newLoggingOffset; i++) {
                String str = m_workplaceImportThread.getLoggingThread().getMessages().elementAt(i).toString();
                str = CmsEncoder.escapeWBlanks(str, "UTF-8");
                out.println("output[" + (i - m_oldLoggingOffset) + "] = \"" + str + "\";");
            }
        } else {
            out.println("output[0] = 'ERROR';");
        }

        boolean threadFinished = m_workplaceImportThread.isFinished();
        boolean allWritten = m_oldLoggingOffset >= m_workplaceImportThread.getLoggingThread().getMessages().size();

        out.println("function initThread() {");
        if (isInitialized()) {
            out.print("send();");
            if (threadFinished && allWritten) {
                out.println("setTimeout('top.display.finish()', 500);");
            } else {
                int timeout = 5000;
                if (getWorkplaceImportThread().getLoggingThread().getMessages().size() < 20) {
                    timeout = 1000;
                }
                out.println("setTimeout('location.reload()', " + timeout + ");");
            }
        }
        out.println("}");
    }

    /**
     *  Saves properties to specified file.<p>
     * 
     *  @param properties the properties to be saved
     *  @param file the file to save the properties to
     *  @param backup if true, create a backupfile
     */
    public void saveProperties(ExtendedProperties properties, String file, boolean backup) {

        if (new File(m_configRfsPath + file).isFile()) {
            String backupFile = file + ".ori";
            String tempFile = file + ".tmp";

            m_errors.clear();

            if (backup) {
                // make a backup copy
                copyFile(file, "backup" + File.separatorChar + backupFile);
            }

            //save to temporary file
            copyFile(file, tempFile);

            // save properties
            save(properties, tempFile, file);

            // delete temp file
            File temp = new File(m_configRfsPath + tempFile);
            temp.delete();
        } else {
            m_errors.addElement("No valid file: " + file + "\n");
        }

    }

    /**
     * Sets the database drivers to the given value.<p>
     * 
     * @param databaseKey the key of the selected database server (e.g. "mysql", "generic" or "oracle")
     */
    public void setDatabase(String databaseKey) {

        m_databaseKey = databaseKey;

        String vfsDriver = getDbProperty(m_databaseKey + ".vfs.driver");
        String userDriver = getDbProperty(m_databaseKey + ".user.driver");
        String projectDriver = getDbProperty(m_databaseKey + ".project.driver");
        String workflowDriver = getDbProperty(m_databaseKey + ".workflow.driver");
        String backupDriver = getDbProperty(m_databaseKey + ".backup.driver");
        String sqlManager = getDbProperty(m_databaseKey + ".sqlmanager");

        setExtProperty("db.name", m_databaseKey);
        setExtProperty("db.vfs.driver", vfsDriver);
        setExtProperty("db.vfs.sqlmanager", sqlManager);
        setExtProperty("db.user.driver", userDriver);
        setExtProperty("db.user.sqlmanager", sqlManager);
        setExtProperty("db.project.driver", projectDriver);
        setExtProperty("db.project.sqlmanager", sqlManager);
        setExtProperty("db.workflow.driver", workflowDriver);
        setExtProperty("db.workflow.sqlmanager", sqlManager);
        setExtProperty("db.backup.driver", backupDriver);
        setExtProperty("db.backup.sqlmanager", sqlManager);
    }

    /**
     * Sets the database name.<p>
     * 
     * @param db the database name to set
     */
    public void setDb(String db) {

        setDbProperty(m_databaseKey + ".dbname", db);
    }

    /**
     * Sets the JDBC connect URL parameters.<p>
     * 
     * @param value the JDBC connect URL parameters
     */
    public void setDbConStrParams(String value) {

        setDbProperty(m_databaseKey + ".constr.params", value);
    }

    /**
     * Sets the database create statement.<p>
     * 
     * @param dbCreateConStr the database create statement
     */
    public void setDbCreateConStr(String dbCreateConStr) {

        setDbProperty(m_databaseKey + ".constr", dbCreateConStr);
    }

    /**
     * Sets the password used for the initial OpenCms database creation.<p>
     * 
     * This password will not be stored permanently, 
     * but used only in the setup wizard.<p>
     * 
     * @param dbCreatePwd the password used for the initial OpenCms database creation
     */
    public void setDbCreatePwd(String dbCreatePwd) {

        m_dbCreatePwd = dbCreatePwd;
    }

    /**
     * Set the database user that is used to connect to the database.<p>
     * 
     * @param dbCreateUser the user to set
     */
    public void setDbCreateUser(String dbCreateUser) {

        setDbProperty(m_databaseKey + ".user", dbCreateUser);
    }

    /** 
     * Sets the database driver belonging to the database.<p>
     * 
     * @param driver name of the opencms driver 
     */
    public void setDbDriver(String driver) {

        setDbProperty(m_databaseKey + ".driver", driver);
    }

    /**
     * Sets the needed database parameters.<p> 
     * 
     * @param request the http request
     * @param provider the db provider
     * 
     * @return true if already submitted
     */
    public boolean setDbParamaters(HttpServletRequest request, String provider) {

        String conStr = request.getParameter("dbCreateConStr");
        boolean isFormSubmitted = ((request.getParameter("submit") != null) && (conStr != null));
        String database = "";
        if (provider.equals(C_MYSQL_PROVIDER)) {
            database = request.getParameter("db");
        } else if (provider.equals(C_POSTGRESQL_PROVIDER)) {
            database = request.getParameter("dbName");
        }
        if (provider.equals(C_MYSQL_PROVIDER) || provider.equals(C_POSTGRESQL_PROVIDER)) {
            isFormSubmitted = (isFormSubmitted && (database != null));
        }

        if (isInitialized()) {
            String createDb = request.getParameter("createDb");
            if (createDb == null) {
                createDb = "";
            }

            String createTables = request.getParameter("createTables");
            if (createTables == null) {
                createTables = "";
            }

            if (isFormSubmitted) {
                if (provider.equals(C_MYSQL_PROVIDER) || provider.equals(C_POSTGRESQL_PROVIDER)) {
                    if (!conStr.endsWith("/")) {
                        conStr += "/";
                    }
                    conStr += database;
                }
                setDbWorkConStr(conStr);
                if (provider.equals(C_POSTGRESQL_PROVIDER)) {
                    setDb(database);
                }
                String dbCreateUser = request.getParameter("dbCreateUser");
                String dbCreatePwd = request.getParameter("dbCreatePwd");

                String dbWorkUser = request.getParameter("dbWorkUser");
                String dbWorkPwd = request.getParameter("dbWorkPwd");

                setDbCreateUser(dbCreateUser);
                setDbCreatePwd(dbCreatePwd);

                if (dbWorkUser.equals("")) {
                    dbWorkUser = request.getContextPath();
                }
                if (dbWorkUser.equals("")) {
                    dbWorkUser = "opencms";
                }
                if (dbWorkUser.startsWith("/")) {
                    dbWorkUser = dbWorkUser.substring(1, dbWorkUser.length());
                }
                setDbWorkUser(dbWorkUser);
                setDbWorkPwd(dbWorkPwd);

                if (provider.equals(C_ORACLE_PROVIDER)) {
                    String dbDefaultTablespace = request.getParameter("dbDefaultTablespace");
                    String dbTemporaryTablespace = request.getParameter("dbTemporaryTablespace");
                    String dbIndexTablespace = request.getParameter("dbIndexTablespace");

                    setDbProperty(getDatabase() + ".defaultTablespace", dbDefaultTablespace);
                    setDbProperty(getDatabase() + ".temporaryTablespace", dbTemporaryTablespace);
                    setDbProperty(getDatabase() + ".indexTablespace", dbIndexTablespace);
                }
                Map replacer = new HashMap();
                if (!provider.equals(C_MYSQL_PROVIDER)) {
                    replacer.put("${user}", dbWorkUser);
                    replacer.put("${password}", dbWorkPwd);
                }
                if (provider.equals(C_MYSQL_PROVIDER) || provider.equals(C_POSTGRESQL_PROVIDER)) {
                    replacer.put("${database}", database);
                }
                if (provider.equals(C_ORACLE_PROVIDER)) {
                    replacer.put("${defaultTablespace}", getDbProperty(getDatabase() + ".defaultTablespace"));
                    replacer.put("${indexTablespace}", getDbProperty(getDatabase() + ".indexTablespace"));
                    replacer.put("${temporaryTablespace}", getDbProperty(getDatabase() + ".temporaryTablespace"));
                }
                setReplacer(replacer);

                if (provider.equals(C_GENERIC_PROVIDER) || provider.equals(C_ORACLE_PROVIDER)) {
                    request.getSession().setAttribute("createTables", createTables);
                }
                request.getSession().setAttribute("createDb", createDb);
            } else {
                String dbName = "opencms";
                // initialize the database name with the app name
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(request.getContextPath())) {
                    dbName = request.getContextPath().substring(1);
                }
                if (provider.equals(C_ORACLE_PROVIDER) || provider.equals(C_POSTGRESQL_PROVIDER)) {
                    setDbWorkUser(dbName);
                } else {
                    setDb(dbName);
                }
            }
        }
        return isFormSubmitted;
    }

    /** 
     * This method sets the value for a given key in the database properties.<p>
     * 
     * @param key The key of the property
     * @param value The value of the property
     */
    public void setDbProperty(String key, String value) {

        // extract the database key out of the entire key
        String databaseKey = key.substring(0, key.indexOf('.'));
        Map databaseProperties = (Map)getDatabaseProperties().get(databaseKey);
        databaseProperties.put(key, value);
    }

    /** 
     * Sets the connection string to the database to the given value.<p>
     *
     * @param dbWorkConStr the connection string used by the OpenCms core 
     */
    public void setDbWorkConStr(String dbWorkConStr) {

        String driver = getDbProperty(m_databaseKey + ".driver");

        setExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPool.C_KEY_JDBC_DRIVER, driver);
        setExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPool.C_KEY_JDBC_URL, dbWorkConStr);
        setExtProperty(
            CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPool.C_KEY_TEST_QUERY,
            getDbTestQuery());
        setExtProperty(
            CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPool.C_KEY_JDBC_URL_PARAMS,
            getDbConStrParams());
    }

    /**
     * Sets the password of the database to the given value.<p>
     * 
     * @param dbWorkPwd the password for the OpenCms database user  
     */
    public void setDbWorkPwd(String dbWorkPwd) {

        setExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPool.C_KEY_PASSWORD, dbWorkPwd);
    }

    /** 
     * Sets the user of the database to the given value.<p> 
     *
     * @param dbWorkUser the database user used by the opencms core 
     */
    public void setDbWorkUser(String dbWorkUser) {

        setExtProperty(CmsDbPool.C_KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPool.C_KEY_POOL_USER, dbWorkUser);
    }

    /** 
     * Set the mac ethernet address, required for UUID generation.<p>
     * 
     * @param ethernetAddress the mac addess to set
     */
    public void setEthernetAddress(String ethernetAddress) {

        setExtProperty("server.ethernet.address", ethernetAddress);
    }

    /**
     * Sets the list with the package names of the modules to be installed.<p>
     * 
     * @param value a string with the package names of the modules to be installed delimited by the pipe symbol "|"
     */
    public void setInstallModules(String value) {

        StringTokenizer tokenizer = new StringTokenizer(value, "|");

        if (tokenizer.countTokens() > 0) {
            m_installModules = new ArrayList();

            while (tokenizer.hasMoreTokens()) {
                m_installModules.add(tokenizer.nextToken());
            }
        } else {
            m_installModules = Collections.EMPTY_LIST;
        }
    }

    /**
     * Sets the replacer.<p>
     * 
     * @param map the replacer to set
     */
    public void setReplacer(Map map) {

        m_replacer = map;
    }

    /**
     * Sets the OpenCms server name.<p>
     * 
     * @param name the OpenCms server name
     */
    public void setServerName(String name) {

        setExtProperty("server.name", name);
    }

    /**
     * Sets the OpenCms workplace site.<p>
     *
     * @param newSite the OpenCms workplace site
     */
    public void setWorkplaceSite(String newSite) {

        String oldSite = getWorkplaceSite();
        // get the site list
        String siteList = getExtProperty("site.root.list");
        // replace old site URL in site list with new site URL
        siteList = CmsStringUtil.substitute(siteList, oldSite, newSite);
        setExtProperty("site.root.list", siteList);
        setExtProperty("site.workplace", newSite);
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellExit()
     */
    public void shellExit() {

        System.out.println();
        System.out.println();
        System.out.println("The setup is finished!\nThe OpenCms system used for the setup will now shut down.");
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellStart()
     */
    public void shellStart() {

        System.out.println();
        System.out.println("Starting Workplace import and database setup for OpenCms!");

        String[] copy = Messages.COPYRIGHT_BY_ALKACON;
        for (int i = copy.length - 1; i >= 0; i--) {
            System.out.println(copy[i]);
        }
        System.out.println("This is OpenCms " + OpenCms.getSystemInfo().getVersionName());
        System.out.println();
        System.out.println();
    }

    /** 
     * Returns the value for a given key from the extended properties.
     * 
     * @param key the property key
     * @return the string value for a given key
     */
    protected String getExtProperty(String key) {

        Object value = null;

        return ((value = m_extProperties.get(key)) != null) ? value.toString() : "";
    }

    /**
     * Imports a module (zipfile) from the default module directory, 
     * creating a temporary project for this.<p>
     * 
     * @param importFile the name of the import module located in the default module directory
     * @throws Exception if something goes wrong
     * @see org.opencms.importexport.CmsImportExportManager#importData(CmsObject, String, String, org.opencms.report.I_CmsReport)
     */
    protected void importModuleFromDefault(String importFile) throws Exception {

        String exportPath = OpenCms.getSystemInfo().getPackagesRfsPath();
        String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            exportPath + I_CmsConstants.C_MODULE_PATH + importFile);
        OpenCms.getImportExportManager().importData(m_cms, fileName, null, new CmsShellReport());
    }

    /** 
     * Stores the properties of all available database configurations in a
     * map keyed by their database key names (e.g. "mysql", "generic" or "oracle").<p>
     */
    protected void readDatabaseConfig() {

        String databaseKey = null;
        FileInputStream input = null;
        String configPath = null;
        Properties databaseProperties = null;
        File databaseSetupFolder = null;
        File[] childResources = null;
        File childResource = null;
        File setupFile = null;
        boolean hasMissingSetupFiles = false;

        m_databaseKeys = new ArrayList();
        m_databaseProperties = new HashMap();

        try {
            databaseSetupFolder = new File(m_webAppRfsPath + File.separator + "setup" + File.separator + "database");

            if (databaseSetupFolder.exists()) {
                childResources = databaseSetupFolder.listFiles();

                if (childResources != null) {
                    for (int i = 0; i < childResources.length; i++) {
                        childResource = childResources[i];
                        hasMissingSetupFiles = false;

                        if (childResource.exists() && childResource.isDirectory() && childResource.canRead()) {
                            for (int j = 0; j < REQUIRED_DB_SETUP_FILES.length; j++) {
                                setupFile = new File(childResource.getPath()
                                    + File.separator
                                    + REQUIRED_DB_SETUP_FILES[j]);

                                if (!setupFile.exists() || !setupFile.isFile() || !setupFile.canRead()) {
                                    hasMissingSetupFiles = true;
                                    System.err.println("["
                                        + getClass().getName()
                                        + "] missing or unreadable database setup file: "
                                        + setupFile.getPath());
                                    break;
                                }

                                if (!hasMissingSetupFiles) {
                                    m_databaseKeys.add(childResource.getName().trim());
                                }
                            }
                        }
                    }
                }

                for (int i = 0; i < m_databaseKeys.size(); i++) {
                    databaseKey = (String)m_databaseKeys.get(i);
                    configPath = m_webAppRfsPath
                        + "setup"
                        + File.separator
                        + "database"
                        + File.separator
                        + databaseKey
                        + File.separator
                        + "database.properties";

                    try {
                        input = new FileInputStream(new File(configPath));
                        databaseProperties = new Properties();
                        databaseProperties.load(input);
                        m_databaseProperties.put(databaseKey, databaseProperties);
                    } catch (Exception e) {
                        System.err.println(e.toString());
                        e.printStackTrace(System.err);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                // noop
            }
        }
    }

    /** 
     * This method sets the value for a given key in the extended properties.
     * @param key The key of the property
     * @param value The value of the property
     */
    protected void setExtProperty(String key, String value) {

        m_extProperties.put(key, value);
    }

    /**
     * Saves the properties to a file.<p>
     * 
     * @param properties the properties to be saved
     * @param source the source file to get the keys from
     * @param target the target file to save the properties to
     */
    private void save(ExtendedProperties properties, String source, String target) {

        try {
            HashSet alreadyWritten = new HashSet();

            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(m_configRfsPath + source)));

            FileWriter fw = new FileWriter(new File(m_configRfsPath + target));

            while (true) {
                String line = lnr.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();

                if ("".equals(line)) {
                    // output empty line
                    fw.write("\n");
                } else if (line.startsWith("#")) {
                    // output comment
                    fw.write(line);
                    fw.write("\n");
                } else {

                    int index = line.indexOf('=');
                    int index1 = line.indexOf("\\=");
                    if (line.indexOf('=') > -1 && index1 != index - 1) {

                        String key = line.substring(0, line.indexOf('=')).trim();
                        if (alreadyWritten.contains(key)) {
                            continue;
                        }
                        // write key
                        fw.write((key + "="));
                        try {
                            Object obj = properties.get(key);
                            String value = "";

                            if (obj != null && obj instanceof Vector) {
                                String[] values = {};
                                values = (String[])((Vector)obj).toArray(values);
                                StringBuffer buf = new StringBuffer();

                                for (int i = 0; i < values.length; i++) {

                                    // escape commas and equals in value
                                    values[i] = CmsStringUtil.substitute(values[i], ",", "\\,");
                                    values[i] = CmsStringUtil.substitute(values[i], "=", "\\=");

                                    buf.append("\t" + values[i] + ((i < values.length - 1) ? ",\\\n" : ""));
                                }
                                value = buf.toString();

                                // write it
                                fw.write("\\\n" + value);

                            } else if (obj != null) {

                                value = ((String)obj).trim();

                                // escape commas and equals in value
                                value = CmsStringUtil.substitute(value, ",", "\\,");
                                value = CmsStringUtil.substitute(value, "=", "\\=");

                                // write it
                                fw.write(value);
                            }

                        } catch (NullPointerException e) {
                            // no value found - do nothing 
                        }
                        // add trailing line feed
                        fw.write("\n");

                        // remember that this properties is already written (multi values)
                        alreadyWritten.add(key);
                    }
                }
            }

            lnr.close();
            fw.close();
        } catch (Exception e) {
            m_errors.addElement("Could not save properties to " + target + " \n");
            m_errors.addElement(e.toString() + "\n");
        }
    }

    /**
     * Sets the path to the OpenCms home directory.<p>
     * 
     * @param webInfRfsPath path to OpenCms home directory
     */
    private void setWebAppRfsPath(String webInfRfsPath) {

        m_webAppRfsPath = webInfRfsPath;
        if ("".equals(webInfRfsPath)) {
            // required for test cases
            m_configRfsPath = "";
            return;
        }
        if (!m_webAppRfsPath.endsWith(File.separator)) {
            // make sure that Path always ends with a separator, not always the case in different 
            // environments since getServletContext().getRealPath("/") does not end with a "/" in 
            // all servlet runtimes
            m_webAppRfsPath += File.separator;
        }
        m_configRfsPath = m_webAppRfsPath + "WEB-INF" + File.separator + "config" + File.separator;
    }
}
