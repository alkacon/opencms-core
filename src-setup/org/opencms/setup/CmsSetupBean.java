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
import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.CmsSitesConfiguration;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.db.CmsDbPoolV11;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.I_CmsShellCommands;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.CmsShellReport;
import org.opencms.setup.comptest.CmsSetupTestResult;
import org.opencms.setup.comptest.CmsSetupTestSimapi;
import org.opencms.setup.comptest.I_CmsSetupTest;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

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
 * <p>
 *
 * @since 6.0.0
 */
public class CmsSetupBean implements I_CmsShellCommands {

    /** Prefix for 'marker' properties in opencms.properties where additional properties should be inserted. */
    public static final String ADDITIONAL_PREFIX = "additional.";

    /** DB provider constant for as400. */
    public static final String AS400_PROVIDER = "as400";

    /** The name of the components properties file. */
    public static final String COMPONENTS_PROPERTIES = "components.properties";

    /** DB provider constant for db2. */
    public static final String DB2_PROVIDER = "db2";

    /** Folder constant name.<p> */
    public static final String FOLDER_BACKUP = "backup" + File.separatorChar;

    /** Folder constant name.<p> */
    public static final String FOLDER_DATABASE = "database" + File.separatorChar;

    /** Folder constant name.<p> */
    public static final String FOLDER_LIB = "lib" + File.separatorChar;

    /** Folder constant name.<p> */
    public static final String FOLDER_SETUP = "WEB-INF/setupdata" + File.separatorChar;

    /** DB provider constant. */
    public static final String GENERIC_PROVIDER = "generic";

    /** DB provider constant for hsqldb. */
    public static final String HSQLDB_PROVIDER = "hsqldb";

    /** Name of the property file containing HTML fragments for setup wizard and error dialog. */
    public static final String HTML_MESSAGE_FILE = "org/opencms/setup/htmlmsg.properties";

    /** DB provider constant for maxdb. */
    public static final String MAXDB_PROVIDER = "maxdb";

    /** DB provider constant for mssql. */
    public static final String MSSQL_PROVIDER = "mssql";

    /** DB provider constant for mysql. */
    public static final String MYSQL_PROVIDER = "mysql";

    /** DB provider constant for oracle. */
    public static final String ORACLE_PROVIDER = "oracle";

    /** DB provider constant for postgresql. */
    public static final String POSTGRESQL_PROVIDER = "postgresql";

    /** The default component position, is missing. */
    protected static final int DEFAULT_POSITION = 9999;

    /** Properties file key constant post fix. */
    protected static final String PROPKEY_CHECKED = ".checked";

    /** Properties file key constant prefix. */
    protected static final String PROPKEY_COMPONENT = "component.";

    /** Properties file key constant. */
    protected static final String PROPKEY_COMPONENTS = "components";

    /** Properties file key constant post fix. */
    protected static final String PROPKEY_DEPENDENCIES = ".dependencies";

    /** Properties file key constant post fix. */
    protected static final String PROPKEY_DESCRIPTION = ".description";

    /** Properties file key constant post fix. */
    protected static final String PROPKEY_MODULES = ".modules";

    /** Properties file key constant post fix. */
    protected static final String PROPKEY_NAME = ".name";

    /** Properties file key constant post fix. */
    protected static final String PROPKEY_POSITION = ".position";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSetupBean.class);

    /** Contains HTML fragments for the output in the JSP pages of the setup wizard. */
    private static Properties m_htmlProps;

    /** Required files per database setup (for sql supported dbs). */
    private static final String[] REQUIRED_SQL_DB_SETUP_FILES = {"database.properties"};

    /** A map with all available modules. */
    protected Map<String, CmsModule> m_availableModules;

    /** A CmsObject to execute shell commands. */
    protected CmsObject m_cms;

    /** Contains all defined components. */
    protected CmsIdentifiableObjectContainer<CmsSetupComponent> m_components;

    /** A list with the package names of the modules to be installed .*/
    protected List<String> m_installModules;

    /** Location for log file.  */
    protected String m_logFile = OpenCms.getSystemInfo().getLogFileRfsFolder() + "setup.log";

    /** A map with lists of dependent module package names keyed by module package names. */
    protected Map<String, List<String>> m_moduleDependencies;

    /** A map with all available modules filenames. */
    protected Map<String, String> m_moduleFilenames;

    /** Location for module archives relative to the webapp folder.  */
    protected String m_modulesFolder = CmsSystemInfo.FOLDER_WEBINF
        + CmsSystemInfo.FOLDER_PACKAGES
        + CmsSystemInfo.FOLDER_MODULES;

    /** The new logging offset in the workplace import thread. */
    protected int m_newLoggingOffset;

    /** The lod logging offset in the workplace import thread. */
    protected int m_oldLoggingOffset;

    /** The absolute path to the home directory of the OpenCms webapp. */
    protected String m_webAppRfsPath;

    /** Additional property sets to be inserted into opencms.properties during setup. */
    private Map<String, String> m_additionalProperties = Maps.newHashMap();

    /** Signals whether the setup is executed in the auto mode or in the wizard mode. */
    private boolean m_autoMode;

    /** The absolute path to the config sub directory of the OpenCms web application. */
    private String m_configRfsPath;

    /** Contains the properties of "opencms.properties". */
    private CmsParameterConfiguration m_configuration;

    private String m_contextPath;

    /** Key of the selected database server (e.g. "mysql", "generic" or "oracle") */
    private String m_databaseKey;

    /** List of keys of all available database server setups (e.g. "mysql", "generic" or "oracle") */
    private List<String> m_databaseKeys;

    /** Map of database setup properties of all available database server setups keyed by their database keys. */
    private Map<String, Properties> m_databaseProperties;

    /** Password used for the JDBC connection when the OpenCms database is created. */
    private String m_dbCreatePwd;

    /** The name of the default web application (in web.xml). */
    private String m_defaultWebApplication;

    /** Contains the error messages to be displayed in the setup wizard. */
    private List<String> m_errors;

    /** The ethernet address. */
    private String m_ethernetAddress;

    /** The full key of the selected database including the "_jpa" or "_sql" information. */
    private String m_fullDatabaseKey;

    /** Flag which is set to true after module import if there is an index.html file in the default site. */
    private boolean m_hasIndexHtml;

    /** The Database Provider used in setup. */
    private String m_provider;

    /** A map with tokens ${...} to be replaced in SQL scripts. */
    private Map<String, String> m_replacer;

    /** The initial servlet configuration. */
    private ServletConfig m_servletConfig;

    /** The servlet mapping (in web.xml). */
    private String m_servletMapping;

    private CmsShell m_shell;

    /** List of sorted keys by ranking of all available database server setups (e.g. "mysql", "generic" or "oracle") */
    private List<String> m_sortedDatabaseKeys;

    /** The workplace import thread. */
    private CmsSetupWorkplaceImportThread m_workplaceImportThread;

    /** Xml read/write helper object. */
    private CmsSetupXmlHelper m_xmlHelper;

    /**
     * Default constructor.<p>
     */
    public CmsSetupBean() {

        initHtmlParts();
    }

    /**
     * Restores the opencms.xml either to or from a backup file, depending
     * whether the setup wizard is executed the first time (the backup
     * does not exist) or not (the backup exists).
     *
     * @param filename something like e.g. "opencms.xml"
     * @param originalFilename the configurations real file name, e.g. "opencms.xml.ori"
     */
    public void backupConfiguration(String filename, String originalFilename) {

        // ensure backup folder exists
        File backupFolder = new File(m_configRfsPath + FOLDER_BACKUP);
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        // copy file to (or from) backup folder
        originalFilename = FOLDER_BACKUP + originalFilename;
        File file = new File(m_configRfsPath + originalFilename);
        if (file.exists()) {
            copyFile(originalFilename, filename);
        } else {
            copyFile(filename, originalFilename);
        }
    }

    /**
     * Returns a map of dependencies.<p>
     *
     * The component dependencies are get from the setup and module components.properties files found.<p>
     *
     * @return a Map of component ids as keys and a list of dependency names as values
     */
    public Map<String, List<String>> buildDepsForAllComponents() {

        Map<String, List<String>> ret = new HashMap<String, List<String>>();

        Iterator<CmsSetupComponent> itComponents = CmsCollectionsGenericWrapper.<CmsSetupComponent> list(
            m_components.elementList()).iterator();
        while (itComponents.hasNext()) {
            CmsSetupComponent component = itComponents.next();

            // if component a depends on component b, and component c depends also on component b:
            // build a map with a list containing "a" and "c" keyed by "b" to get a
            // list of components depending on component "b"...
            Iterator<String> itDeps = component.getDependencies().iterator();
            while (itDeps.hasNext()) {
                String dependency = itDeps.next();

                // get the list of dependent modules
                List<String> componentDependencies = ret.get(dependency);
                if (componentDependencies == null) {
                    // build a new list if "b" has no dependent modules yet
                    componentDependencies = new ArrayList<String>();
                    ret.put(dependency, componentDependencies);
                }
                // add "a" as a module depending on "b"
                componentDependencies.add(component.getId());
            }
        }
        itComponents = CmsCollectionsGenericWrapper.<CmsSetupComponent> list(m_components.elementList()).iterator();
        while (itComponents.hasNext()) {
            CmsSetupComponent component = itComponents.next();
            if (ret.get(component.getId()) == null) {
                ret.put(component.getId(), new ArrayList<String>());
            }
        }
        return ret;
    }

    /**
     * Checks the ethernet address value and generates a dummy address, if necessary.<p>     *
     */
    public void checkEthernetAddress() {

        // check the ethernet address in order to generate a random address, if not available
        if (CmsStringUtil.isEmpty(getEthernetAddress())) {
            setEthernetAddress(CmsStringUtil.getEthernetAddress());
        }
    }

    /**
     * Clears the cache.<p>
     */
    public void clearCache() {

        OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
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
            m_errors.add("Could not copy " + source + " to " + target + " \n");
            m_errors.add(e.toString() + "\n");
        }
    }

    /**
     * Returns html code to display an error.<p>
     *
     * @param pathPrefix to adjust the path
     *
     * @return html code
     */
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
        html.append("\t\t\t\t\t\tThe Alkacon OpenCms setup wizard has not been started correctly!<br>");
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
     * Returns html code to display the errors occurred.<p>
     *
     * @param pathPrefix to adjust the path
     *
     * @return html code
     */
    public String displayErrors(String pathPrefix) {

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

        Iterator<String> iter = getErrors().iterator();
        while (iter.hasNext()) {
            String msg = iter.next();
            html.append("\t\t\t\t\t\t");
            html.append(msg);
            html.append("<br/>");
        }

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
     * Returns a map with all available modules.<p>
     *
     * The map contains maps keyed by module package names. Each of these maps contains various
     * information about the module such as the module name, version, description, and a list of
     * it's dependencies. You should refer to the source code of this method to understand the data
     * structure of the map returned by this method!<p>
     *
     * @return a map with all available modules
     */
    public Map<String, CmsModule> getAvailableModules() {

        if ((m_availableModules == null) || m_availableModules.isEmpty()) {
            m_availableModules = new HashMap<String, CmsModule>();
            m_moduleDependencies = new HashMap<String, List<String>>();
            m_moduleFilenames = new HashMap<String, String>();
            m_components = new CmsIdentifiableObjectContainer<CmsSetupComponent>(true, true);

            try {
                addComponentsFromPath(m_webAppRfsPath + FOLDER_SETUP);
                Map<CmsModule, String> modules = CmsModuleManager.getAllModulesFromPath(getModuleFolder());
                Iterator<Map.Entry<CmsModule, String>> itMods = modules.entrySet().iterator();
                while (itMods.hasNext()) {
                    Map.Entry<CmsModule, String> entry = itMods.next();
                    CmsModule module = entry.getKey();
                    // put the module information into a map keyed by the module packages names
                    m_availableModules.put(module.getName(), module);
                    m_moduleFilenames.put(module.getName(), entry.getValue());
                    addComponentsFromPath(getModuleFolder() + entry.getValue());
                }
            } catch (CmsConfigurationException e) {
                throw new CmsRuntimeException(e.getMessageContainer());
            }
            initializeComponents(new HashSet<String>(m_availableModules.keySet()));
        }
        return m_availableModules;
    }

    /**
     * Returns the components.<p>
     *
     * @return the components
     */
    public CmsIdentifiableObjectContainer<CmsSetupComponent> getComponents() {

        return m_components;
    }

    /**
     * Returns the "config" path in the OpenCms web application.<p>
     *
     * @return the config path
     */
    public String getConfigRfsPath() {

        return m_configRfsPath;
    }

    public String getContextPath() {

        return m_contextPath;
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
        if (CmsStringUtil.isEmpty(m_databaseKey)) {
            m_databaseKey = getSortedDatabases().get(0);
        }
        return m_databaseKey;
    }

    /**
     * Returns a list of needed jar filenames for a database server setup specified by a database key (e.g. "mysql", "generic" or "oracle").<p>
     *
     * @param databaseKey a database key (e.g. "mysql", "generic" or "oracle")
     *
     * @return a list of needed jar filenames
     */
    public List<String> getDatabaseLibs(String databaseKey) {

        List<String> lst;
        lst = CmsStringUtil.splitAsList(
            getDatabaseProperties().get(databaseKey).getProperty(databaseKey + ".libs"),
            ',',
            true);

        return lst;
    }

    /**
     * Returns the clear text name for a database server setup specified by a database key (e.g. "mysql", "generic" or "oracle").<p>
     *
     * @param databaseKey a database key (e.g. "mysql", "generic" or "oracle")
     * @return the clear text name for a database server setup
     */
    public String getDatabaseName(String databaseKey) {

        return getDatabaseProperties().get(databaseKey).getProperty(databaseKey + PROPKEY_NAME);
    }

    /**
     * Returns a map with the database properties of *all* available database configurations keyed
     * by their database keys (e.g. "mysql", "generic" or "oracle").<p>
     *
     * @return a map with the database properties of *all* available database configurations
     */
    public Map<String, Properties> getDatabaseProperties() {

        if (m_databaseProperties == null) {
            readDatabaseConfig();
        }
        return m_databaseProperties;
    }

    /**
     * Returns a list with they keys (e.g. "mysql", "generic" or "oracle") of all available
     * database server setups found in "/setup/database/".<p>
     *
     * @return a list with they keys (e.g. "mysql", "generic" or "oracle") of all available database server setups
     */
    public List<String> getDatabases() {

        if (m_databaseKeys == null) {
            readDatabaseConfig();
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

        return getDbProperty(m_databaseKey + ".constr.params");
    }

    /**
     * Returns the database create statement.<p>
     *
     * @return the database create statement
     */
    public String getDbCreateConStr() {

        return getDbProperty(m_databaseKey + ".constr");
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
     *
     * @return the string value for a given key
     */
    public String getDbProperty(String key) {

        // extract the database key out of the entire key
        String databaseKey = key.substring(0, key.indexOf('.'));
        Properties databaseProperties = getDatabaseProperties().get(databaseKey);

        return databaseProperties.getProperty(key, "");
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

        if (m_provider.equals(POSTGRESQL_PROVIDER)) {
            return getDbProperty(m_databaseKey + ".constr.newDb");
        } else {
            return getExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + '.' + getPool() + ".jdbcUrl");
        }
    }

    /**
     * Returns the password of the database from the properties .<p>
     *
     * @return the password for the OpenCms database user
     */
    public String getDbWorkPwd() {

        return getExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + '.' + getPool() + ".password");
    }

    /**
     * Returns the user of the database from the properties.<p>
     *
     * @return the database user used by the opencms core
     */
    public String getDbWorkUser() {

        String user = getExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + '.' + getPool() + ".user");
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(user)) {
            return getDbCreateUser();
        }
        return user;
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
     * Returns the display string for a given module.<p>
     *
     * @param module a module
     *
     * @return the display string for the given module
     */
    public String getDisplayForModule(CmsModule module) {

        String name = module.getNiceName();
        String group = module.getGroup();
        String version = module.getVersion().getVersion();
        String display = name;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(group)) {
            display = group + ": " + display;
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(version)) {
            display += " (" + version + ")";
        }
        return display;
    }

    /**
     * Returns the error messages.<p>
     *
     * @return a vector of error messages
     */
    public List<String> getErrors() {

        return m_errors;
    }

    /**
     * Returns the mac ethernet address.<p>
     *
     * @return the mac ethernet addess
     */
    public String getEthernetAddress() {

        if (m_ethernetAddress == null) {
            String address = getExtProperty("server.ethernet.address");
            m_ethernetAddress = CmsStringUtil.isNotEmpty(address) ? address : CmsStringUtil.getEthernetAddress();
        }
        return m_ethernetAddress;
    }

    /**
     * Returns the fullDatabaseKey.<p>
     *
     * @return the fullDatabaseKey
     */
    public String getFullDatabaseKey() {

        if (m_fullDatabaseKey == null) {
            m_fullDatabaseKey = getExtProperty("db.name") + "_sql";
        }
        if (CmsStringUtil.isEmpty(m_fullDatabaseKey) || m_fullDatabaseKey.equals("_sql")) {
            m_fullDatabaseKey = getSortedDatabases().get(0) + "_sql";
        }
        return m_fullDatabaseKey;
    }

    /**
     * Generates the HTML code for the drop down for db selection.<p>
     *
     * @return the generated HTML
     */
    public String getHtmlForDbSelection() {

        StringBuffer buf = new StringBuffer(2048);

        buf.append(
            "<select name=\"fullDatabaseKey\" style=\"width: 250px;\" size=\"1\" onchange=\"location.href='../../step_3_database_selection.jsp?fullDatabaseKey='+this.options[this.selectedIndex].value;\">");
        buf.append("<!-- --------------------- JSP CODE --------------------------- -->");

        // get all available databases
        List<String> databases = getSortedDatabases();

        //  List all databases found in the dbsetup.properties
        if ((databases != null) && (databases.size() > 0)) {

            List<String> sqlDbs = new ArrayList<String>();

            for (String dbKey : databases) {
                sqlDbs.add(dbKey);
            }

            // show the sql dbs first
            for (String dbKey : sqlDbs) {
                String dn = getDatabaseName(dbKey);
                String selected = "";
                if (getFullDatabaseKey().equals(dbKey + "_sql")) {
                    selected = "selected";
                }
                buf.append("<option value='" + dbKey + "_sql' " + selected + ">" + dn);
            }
        } else {
            buf.append("<option value='null'>no database found");
        }

        buf.append("<!-- --------------------------------------------------------- -->");
        buf.append("</select>");

        return buf.toString();
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
     * Returns the path to the /WEB-INF/lib folder.<p>
     *
     * @return the path to the /WEB-INF/lib folder
     */
    public String getLibFolder() {

        return getWebAppRfsPath() + CmsSystemInfo.FOLDER_WEBINF + FOLDER_LIB;
    }

    /**
     * Returns the name of the log file.<p>
     *
     * @return the name of the log file
     */
    public String getLogName() {

        return m_logFile;
    }

    /**
     * Returns a map with lists of dependent module package names keyed by module package names.<p>
     *
     * @return a map with lists of dependent module package names keyed by module package names
     */
    public Map<String, List<String>> getModuleDependencies() {

        if ((m_moduleDependencies == null) || m_moduleDependencies.isEmpty()) {
            try {
                // open the folder "/WEB-INF/packages/modules/"
                m_moduleDependencies = CmsModuleManager.buildDepsForAllModules(getModuleFolder(), true);
            } catch (CmsConfigurationException e) {
                throw new CmsRuntimeException(e.getMessageContainer());
            }
        }
        return m_moduleDependencies;
    }

    /**
     * Returns the absolute path to the module root folder.<p>
     *
     * @return the absolute path to the module root folder
     */
    public String getModuleFolder() {

        return new StringBuffer(m_webAppRfsPath).append(m_modulesFolder).toString();
    }

    /**
     * Returns A list with the package names of the modules to be installed.<p>
     *
     * @return A list with the package names of the modules to be installed
     */
    public List<String> getModulesToInstall() {

        if ((m_installModules == null) || m_installModules.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(m_installModules);
    }

    /**
     * Gets the default pool.<p>
     *
     * @return name of the default pool
     */
    public String getPool() {

        return CmsStringUtil.splitAsArray(getExtProperty("db.pools"), ",")[0];
    }

    /**
     * Returns the parameter configuration.<p>
     *
     * @return the parameter configuration
     */
    public CmsParameterConfiguration getProperties() {

        return m_configuration;
    }

    /**
     * Returns the replacer.<p>
     *
     * @return the replacer
     */
    public Map<String, String> getReplacer() {

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
     * Returns the initial servlet configuration.<p>
     *
     * @return the initial servlet configuration
     */
    public ServletConfig getServletConfig() {

        return m_servletConfig;
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
     * Returns a sorted list with they keys (e.g. "mysql", "generic" or "oracle") of all available
     * database server setups found in "/setup/database/" sorted by their ranking property.<p>
     *
     * @return a sorted list with they keys (e.g. "mysql", "generic" or "oracle") of all available database server setups
     */
    public List<String> getSortedDatabases() {

        if (m_sortedDatabaseKeys == null) {
            List<String> databases = m_databaseKeys;
            List<String> sortedDatabases = new ArrayList<String>(databases.size());
            SortedMap<Integer, String> mappedDatabases = new TreeMap<Integer, String>();
            for (int i = 0; i < databases.size(); i++) {
                String key = databases.get(i);
                Integer ranking = Integer.valueOf(0);
                try {
                    ranking = Integer.valueOf(getDbProperty(key + ".ranking"));
                } catch (Exception e) {
                    // ignore
                }
                mappedDatabases.put(ranking, key);
            }

            while (mappedDatabases.size() > 0) {
                // get database with highest ranking
                Integer key = mappedDatabases.lastKey();
                String database = mappedDatabases.get(key);
                sortedDatabases.add(database);
                mappedDatabases.remove(key);
            }
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

        return Boolean.valueOf(getExtProperty("wizard.enabled")).booleanValue();
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
     * Returns the xml Helper object.<p>
     *
     * @return the xml Helper object
     */
    public CmsSetupXmlHelper getXmlHelper() {

        if (m_xmlHelper == null) {
            // lazzy initialization
            m_xmlHelper = new CmsSetupXmlHelper(getConfigRfsPath());
        }
        return m_xmlHelper;
    }

    /**
     * Returns true if there is an index.html file in the default site after the module import.
     *
     * @return true if there is an index.html file
     */
    public boolean hasIndexHtml() {

        return m_hasIndexHtml;
    }

    /**
     * Returns the html code for component selection.<p>
     *
     * @return html code
     */
    public String htmlComponents() {

        StringBuffer html = new StringBuffer(1024);
        Iterator<CmsSetupComponent> itComponents = CmsCollectionsGenericWrapper.<CmsSetupComponent> list(
            m_components.elementList()).iterator();
        while (itComponents.hasNext()) {
            CmsSetupComponent component = itComponents.next();
            html.append(htmlComponent(component));
        }
        return html.toString();
    }

    /**
     * Returns html code for the module descriptions in help ballons.<p>
     *
     * @return html code
     */
    public String htmlModuleHelpDescriptions() {

        StringBuffer html = new StringBuffer(1024);
        Iterator<String> itModules = sortModules(getAvailableModules().values()).iterator();
        for (int i = 0; itModules.hasNext(); i++) {
            String moduleName = itModules.next();
            CmsModule module = getAvailableModules().get(moduleName);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(module.getDescription())) {
                html.append(getHtmlPart("C_HELP_START", "" + i));
                html.append(module.getDescription());
                html.append("\n");
                html.append(getHtmlPart("C_HELP_END"));
                html.append("\n");
            }
        }
        return html.toString();
    }

    /**
     * Returns html for displaying a module selection box.<p>
     *
     * @return html code
     */
    public String htmlModules() {

        StringBuffer html = new StringBuffer(1024);
        Iterator<String> itModules = sortModules(getAvailableModules().values()).iterator();
        for (int i = 0; itModules.hasNext(); i++) {
            String moduleName = itModules.next();
            CmsModule module = getAvailableModules().get(moduleName);
            html.append(htmlModule(module, i));
        }
        return html.toString();
    }

    /**
     * Installed all modules that have been set using {@link #setInstallModules(String)}.<p>
     *
     * This method is invoked as a shell command.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void importModulesFromSetupBean() throws Exception {

        // read here how the list of modules to be installed is passed from the setup bean to the
        // setup thread, and finally to the shell process that executes the setup script:
        // 1) the list with the package names of the modules to be installed is saved by setInstallModules
        // 2) the setup thread gets initialized in a JSP of the setup wizard
        // 3) the instance of the setup bean is passed to the setup thread by setAdditionalShellCommand
        // 4) the setup bean is passed to the shell by startSetup
        // 5) because the setup bean implements I_CmsShellCommands, the shell constructor can pass the shell's CmsObject back to the setup bean
        // 6) thus, the setup bean can do things with the Cms

        if ((m_cms != null) && (m_installModules != null)) {
            for (int i = 0; i < m_installModules.size(); i++) {
                String filename = m_moduleFilenames.get(m_installModules.get(i));
                try {
                    importModuleFromDefault(filename);
                } catch (Exception e) {
                    // log a exception during module import, but make sure the next module is still imported
                    e.printStackTrace(System.err);
                }
            }
            m_hasIndexHtml = false;
            try {
                m_cms.readResource("/index.html");
                m_hasIndexHtml = true;
            } catch (Exception e) {

            }

        }

    }

    /**
     * Creates a new instance of the setup Bean from a JSP page.<p>
     *
     * @param pageContext the JSP's page context
     */
    public void init(PageContext pageContext) {

        ServletContext servCtx = pageContext.getServletContext();
        ServletConfig servConfig = pageContext.getServletConfig();

        init(servCtx, servConfig);
    }

    public void init(ServletContext servCtx, ServletConfig servConfig) {

        // check for OpenCms installation directory path
        String webAppRfsPath = servConfig.getServletContext().getRealPath("/");

        // read the the OpenCms servlet mapping from the servlet context parameters
        String servletMapping = servCtx.getInitParameter(OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_SERVLET);

        // read the the default context name from the servlet context parameters
        String defaultWebApplication = servCtx.getInitParameter(OpenCmsServlet.SERVLET_PARAM_DEFAULT_WEB_APPLICATION);

        m_servletConfig = servConfig;
        m_contextPath = servCtx.getContextPath();

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
            m_fullDatabaseKey = null;
            m_databaseKey = null;
            m_databaseKeys = null;
            m_databaseProperties = null;
            m_configuration = null;
            m_installModules = null;
            m_moduleDependencies = null;
            m_sortedDatabaseKeys = null;
            m_moduleFilenames = null;

            if (servletMapping == null) {
                servletMapping = "/opencms/*";
            }
            if (defaultWebApplication == null) {
                defaultWebApplication = "ROOT";
            }
            m_servletMapping = servletMapping;
            m_defaultWebApplication = defaultWebApplication;

            setWebAppRfsPath(webAppRfsPath);
            m_errors = new ArrayList<String>();

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(webAppRfsPath)) {
                m_configuration = new CmsParameterConfiguration(m_configRfsPath + CmsSystemInfo.FILE_PROPERTIES);
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
        m_shell = shell;
    }

    /**
     * Returns the autoMode.<p>
     *
     * @return the autoMode
     */
    public boolean isAutoMode() {

        return m_autoMode;
    }

    /**
     * Over simplistic helper to compare two strings to check radio buttons.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return "checked" if both values are equal, the empty String "" otherwise
     */
    public String isChecked(String value1, String value2) {

        if ((value1 == null) || (value2 == null)) {
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

        return m_configuration != null;
    }

    /**
     * Returns js code with array definition for the available component dependencies.<p>
     *
     * @return js code
     */
    public String jsComponentDependencies() {

        List<CmsSetupComponent> components = CmsCollectionsGenericWrapper.list(m_components.elementList());
        Map<String, List<String>> componentDependencies = buildDepsForAllComponents();

        StringBuffer jsCode = new StringBuffer(1024);
        jsCode.append("\t// an array holding the dependent components for the n-th component\n");
        jsCode.append("\tvar componentDependencies = new Array(");
        jsCode.append(components.size());
        jsCode.append(");\n");
        for (int i = 0; i < components.size(); i++) {
            CmsSetupComponent component = components.get(i);
            List<String> dependencies = componentDependencies.get(component.getId());
            jsCode.append("\tcomponentDependencies[" + i + "] = new Array(");
            if (dependencies != null) {
                for (int j = 0; j < dependencies.size(); j++) {
                    jsCode.append("\"" + dependencies.get(j) + "\"");
                    if (j < (dependencies.size() - 1)) {
                        jsCode.append(", ");
                    }
                }
            }
            jsCode.append(");\n");
        }
        jsCode.append("\n\n");
        return jsCode.toString();
    }

    /**
     * Returns js code with array definition for the component modules.<p>
     *
     * @return js code
     */
    public String jsComponentModules() {

        List<CmsSetupComponent> components = CmsCollectionsGenericWrapper.list(m_components.elementList());

        StringBuffer jsCode = new StringBuffer(1024);
        jsCode.append("\t// an array holding the components modules\n");
        jsCode.append("\tvar componentModules = new Array(");
        jsCode.append(components.size());
        jsCode.append(");\n");
        for (int i = 0; i < components.size(); i++) {
            CmsSetupComponent component = components.get(i);
            jsCode.append("\tcomponentModules[" + i + "] = \"");
            List<String> modules = getComponentModules(component);
            for (int j = 0; j < modules.size(); j++) {
                jsCode.append(modules.get(j));
                if (j < (modules.size() - 1)) {
                    jsCode.append("|");
                }
            }
            jsCode.append("\";\n");
        }
        jsCode.append("\n\n");
        return jsCode.toString();
    }

    /**
     * Returns js code with array definition for the available components names.<p>
     *
     * @return js code
     */
    public String jsComponentNames() {

        StringBuffer jsCode = new StringBuffer(1024);
        jsCode.append("\t// an array from 1...n holding the component names\n");
        jsCode.append("\tvar componentNames = new Array(");
        jsCode.append(m_components.elementList().size());
        jsCode.append(");\n");
        for (int i = 0; i < m_components.elementList().size(); i++) {
            CmsSetupComponent component = m_components.elementList().get(i);
            jsCode.append("\tcomponentNames[" + i + "] = \"" + component.getId() + "\";\n");
        }
        jsCode.append("\n\n");
        return jsCode.toString();
    }

    /**
     * Returns js code with array definition for the available module dependencies.<p>
     *
     * @return js code
     */
    public String jsModuleDependencies() {

        List<String> moduleNames = sortModules(getAvailableModules().values());

        StringBuffer jsCode = new StringBuffer(1024);
        jsCode.append("\t// an array holding the dependent modules for the n-th module\n");
        jsCode.append("\tvar moduleDependencies = new Array(");
        jsCode.append(moduleNames.size());
        jsCode.append(");\n");
        for (int i = 0; i < moduleNames.size(); i++) {
            String moduleName = moduleNames.get(i);
            List<String> dependencies = getModuleDependencies().get(moduleName);
            jsCode.append("\tmoduleDependencies[" + i + "] = new Array(");
            if (dependencies != null) {
                for (int j = 0; j < dependencies.size(); j++) {
                    jsCode.append("\"" + dependencies.get(j) + "\"");
                    if (j < (dependencies.size() - 1)) {
                        jsCode.append(", ");
                    }
                }
            }
            jsCode.append(");\n");
        }
        jsCode.append("\n\n");
        return jsCode.toString();
    }

    /**
     * Returns js code with array definition for the available module names.<p>
     *
     * @return js code
     */
    public String jsModuleNames() {

        List<String> moduleNames = sortModules(getAvailableModules().values());
        StringBuffer jsCode = new StringBuffer(1024);
        jsCode.append("\t// an array from 1...n holding the module package names\n");
        jsCode.append("\tvar modulePackageNames = new Array(");
        jsCode.append(moduleNames.size());
        jsCode.append(");\n");
        for (int i = 0; i < moduleNames.size(); i++) {
            String moduleName = moduleNames.get(i);
            jsCode.append("\tmodulePackageNames[" + i + "] = \"" + moduleName + "\";\n");
        }
        jsCode.append("\n\n");
        return jsCode.toString();
    }

    /**
     * Locks (i.e. disables) the setup wizard.<p>
     *
     */
    public void lockWizard() {

        setExtProperty("wizard.enabled", CmsStringUtil.FALSE);
    }

    /**
     * Prepares step 10 of the setup wizard.<p>
     */
    public void prepareStep10() {

        if (isInitialized()) {
            // lock the wizard for further use
            lockWizard();
            // save Properties to file "opencms.properties"
            saveProperties(getProperties(), CmsSystemInfo.FILE_PROPERTIES, false);
        }
    }

    /**
     * Prepares step 8 of the setup wizard.<p>
     *
     * @return true if the workplace should be imported
     */
    public boolean prepareStep8() {

        if (isInitialized()) {
            try {
                checkEthernetAddress();
                // backup the XML configuration
                backupConfiguration(
                    CmsImportExportConfiguration.DEFAULT_XML_FILE_NAME,
                    CmsImportExportConfiguration.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);
                backupConfiguration(
                    CmsModuleConfiguration.DEFAULT_XML_FILE_NAME,
                    CmsModuleConfiguration.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);
                backupConfiguration(
                    CmsSearchConfiguration.DEFAULT_XML_FILE_NAME,
                    CmsSearchConfiguration.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);
                backupConfiguration(
                    CmsSystemConfiguration.DEFAULT_XML_FILE_NAME,
                    CmsSystemConfiguration.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);
                backupConfiguration(
                    CmsVfsConfiguration.DEFAULT_XML_FILE_NAME,
                    CmsVfsConfiguration.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);
                backupConfiguration(
                    CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME,
                    CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);
                backupConfiguration(
                    CmsConfigurationManager.DEFAULT_XML_FILE_NAME,
                    CmsConfigurationManager.DEFAULT_XML_FILE_NAME + CmsConfigurationManager.POSTFIX_ORI);

                // save Properties to file "opencms.properties"
                setDatabase(m_databaseKey);
                saveProperties(getProperties(), CmsSystemInfo.FILE_PROPERTIES, true);

                // if has sql driver the sql scripts will be eventualy executed
                CmsSetupTestResult testResult = new CmsSetupTestSimapi().execute(this);
                if (testResult.getResult().equals(I_CmsSetupTest.RESULT_FAILED)) {
                    // "/opencms/vfs/resources/resourceloaders/loader[@class='org.opencms.loader.CmsImageLoader']/param[@name='image.scaling.enabled']";
                    StringBuffer xp = new StringBuffer(256);
                    xp.append("/").append(CmsConfigurationManager.N_ROOT);
                    xp.append("/").append(CmsVfsConfiguration.N_VFS);
                    xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
                    xp.append("/").append(CmsVfsConfiguration.N_RESOURCELOADERS);
                    xp.append("/").append(CmsVfsConfiguration.N_LOADER);
                    xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
                    xp.append("='").append(CmsImageLoader.class.getName());
                    xp.append("']/").append(I_CmsXmlConfiguration.N_PARAM);
                    xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
                    xp.append("='").append(CmsImageLoader.CONFIGURATION_SCALING_ENABLED).append("']");

                    getXmlHelper().setValue(
                        CmsVfsConfiguration.DEFAULT_XML_FILE_NAME,
                        xp.toString(),
                        Boolean.FALSE.toString());
                }
                // /opencms/system/sites/workplace-server
                StringBuffer xp = new StringBuffer(256);
                xp.append("/").append(CmsConfigurationManager.N_ROOT);
                xp.append("/").append(CmsSitesConfiguration.N_SITES);
                xp.append("/").append(CmsSitesConfiguration.N_WORKPLACE_SERVER);

                getXmlHelper().setValue(CmsSitesConfiguration.DEFAULT_XML_FILE_NAME, xp.toString(), getWorkplaceSite());

                // /opencms/system/sites/site[@uri='/sites/default/']/@server
                xp = new StringBuffer(256);
                xp.append("/").append(CmsConfigurationManager.N_ROOT);
                xp.append("/").append(CmsSitesConfiguration.N_SITES);
                xp.append("/").append(I_CmsXmlConfiguration.N_SITE);
                xp.append("[@").append(I_CmsXmlConfiguration.A_URI);
                xp.append("='").append(CmsResource.VFS_FOLDER_SITES);
                xp.append("/default/']/@").append(CmsSitesConfiguration.A_SERVER);

                getXmlHelper().setValue(CmsSitesConfiguration.DEFAULT_XML_FILE_NAME, xp.toString(), getWorkplaceSite());
                getXmlHelper().writeAll();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
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

        if (!m_workplaceImportThread.isAlive() && !m_workplaceImportThread.getState().equals(Thread.State.TERMINATED)) {
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

        if ((m_workplaceImportThread == null) || (m_workplaceImportThread.getLoggingThread() == null)) {
            return;
        }
        m_oldLoggingOffset = m_newLoggingOffset;
        m_newLoggingOffset = m_workplaceImportThread.getLoggingThread().getMessages().size();
        if (isInitialized()) {
            for (int i = m_oldLoggingOffset; i < m_newLoggingOffset; i++) {
                String str = m_workplaceImportThread.getLoggingThread().getMessages().get(i).toString();
                str = CmsEncoder.escapeWBlanks(str, CmsEncoder.ENCODING_UTF_8);
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
                out.println("setTimeout('top.display.finish()', 1000);");
            } else {
                int timeout = 5000;
                if (getWorkplaceImportThread().getLoggingThread().getMessages().size() < 20) {
                    timeout = 2000;
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
    public void saveProperties(CmsParameterConfiguration properties, String file, boolean backup) {

        if (new File(m_configRfsPath + file).isFile()) {
            String backupFile = file + CmsConfigurationManager.POSTFIX_ORI;
            String tempFile = file + ".tmp";

            m_errors.clear();

            if (backup) {
                // make a backup copy
                copyFile(file, FOLDER_BACKUP + backupFile);
            }

            //save to temporary file
            copyFile(file, tempFile);

            // save properties
            save(properties, tempFile, file, null);

            // delete temp file
            File temp = new File(m_configRfsPath + tempFile);
            temp.delete();
        } else {
            m_errors.add("No valid file: " + file + "\n");
        }

    }

    /**
     *  Saves properties to specified file.<p>
     *
     *  @param properties the properties to be saved
     *  @param file the file to save the properties to
     *  @param backup if true, create a backupfile
     *  @param forceWrite the keys for the properties which should always be written, even if they don't exist in the configuration file
     */
    public void saveProperties(
        CmsParameterConfiguration properties,
        String file,
        boolean backup,
        Set<String> forceWrite) {

        if (new File(m_configRfsPath + file).isFile()) {
            String backupFile = file + CmsConfigurationManager.POSTFIX_ORI;
            String tempFile = file + ".tmp";

            m_errors.clear();

            if (backup) {
                // make a backup copy
                copyFile(file, FOLDER_BACKUP + backupFile);
            }

            //save to temporary file
            copyFile(file, tempFile);

            // save properties
            save(properties, tempFile, file, forceWrite);

            // delete temp file
            File temp = new File(m_configRfsPath + tempFile);
            temp.delete();
        } else {
            m_errors.add("No valid file: " + file + "\n");
        }

    }

    /**
     * Sets the autoMode.<p>
     *
     * @param autoMode the autoMode to set
     */
    public void setAutoMode(boolean autoMode) {

        m_autoMode = autoMode;
    }

    /**
     * Sets the database drivers to the given value.<p>
     *
     * @param databaseKey the key of the selected database server (e.g. "mysql", "generic" or "oracle")
     */
    public void setDatabase(String databaseKey) {

        m_databaseKey = databaseKey;
        String vfsDriver;
        String userDriver;
        String projectDriver;
        String historyDriver;
        String subscriptionDriver;
        String sqlManager;
        vfsDriver = getDbProperty(m_databaseKey + ".vfs.driver");
        userDriver = getDbProperty(m_databaseKey + ".user.driver");
        projectDriver = getDbProperty(m_databaseKey + ".project.driver");
        historyDriver = getDbProperty(m_databaseKey + ".history.driver");
        subscriptionDriver = getDbProperty(m_databaseKey + ".subscription.driver");
        sqlManager = getDbProperty(m_databaseKey + ".sqlmanager");
        // set the db properties
        setExtProperty("db.name", m_databaseKey);
        setExtProperty("db.vfs.driver", vfsDriver);
        setExtProperty("db.vfs.sqlmanager", sqlManager);
        setExtProperty("db.user.driver", userDriver);
        setExtProperty("db.user.sqlmanager", sqlManager);
        setExtProperty("db.project.driver", projectDriver);
        setExtProperty("db.project.sqlmanager", sqlManager);
        setExtProperty("db.history.driver", historyDriver);
        setExtProperty("db.history.sqlmanager", sqlManager);
        setExtProperty("db.subscription.driver", subscriptionDriver);
        setExtProperty("db.subscription.sqlmanager", sqlManager);
        Properties dbProps = getDatabaseProperties().get(databaseKey);
        String prefix = "additional.";
        String dbPropBlock = "";
        for (Map.Entry<Object, Object> entry : dbProps.entrySet()) {
            if (entry.getKey() instanceof String) {
                String key = (String)entry.getKey();
                if (key.startsWith(prefix)) {
                    key = key.substring(prefix.length());
                    String val = (String)(entry.getValue());
                    setExtProperty(key, val);
                    dbPropBlock += key + "=" + val + "\n";
                }
            }

        }
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(dbPropBlock)) {
            m_additionalProperties.put("dbprops", dbPropBlock);
        }
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

        return setDbParamaters(request.getParameterMap(), provider, request.getContextPath(), request.getSession());
    }

    /**
     * Sets the needed database parameters.<p>
     *
     * @param request the http request
     * @param provider the db provider
     * @param contextPath the context path to use
     * @param session  the session to use or <code>null</code> if running outside a servlet container
     *
     * @return true if already submitted
     */
    public boolean setDbParamaters(
        Map<String, String[]> request,
        String provider,
        String contextPath,
        HttpSession session) {

        String conStr = getReqValue(request, "dbCreateConStr");
        // store the DB provider
        m_provider = provider;

        boolean isFormSubmitted = ((getReqValue(request, "submit") != null) && (conStr != null));
        if (conStr == null) {
            conStr = "";
        }
        String database = "";
        if (provider.equals(MYSQL_PROVIDER)
            || provider.equals(MSSQL_PROVIDER)
            || provider.equals(DB2_PROVIDER)
            || provider.equals(AS400_PROVIDER)) {
            database = getReqValue(request, "db");
        } else if (provider.equals(POSTGRESQL_PROVIDER)) {
            database = getReqValue(request, "dbName");
        }
        if (provider.equals(MYSQL_PROVIDER)
            || provider.equals(MSSQL_PROVIDER)
            || provider.equals(POSTGRESQL_PROVIDER)
            || provider.equals(AS400_PROVIDER)
            || provider.equals(DB2_PROVIDER)) {
            isFormSubmitted = (isFormSubmitted && (database != null));
        }
        if (!(MAXDB_PROVIDER.equals(provider)
            || MSSQL_PROVIDER.equals(provider)
            || MYSQL_PROVIDER.equals(provider)
            || ORACLE_PROVIDER.equals(provider)
            || DB2_PROVIDER.equals(provider)
            || AS400_PROVIDER.equals(provider)
            || HSQLDB_PROVIDER.equals(provider)
            || POSTGRESQL_PROVIDER.equals(provider))) {
            throw new RuntimeException("Database provider '" + provider + "' not supported!");
        }

        if (isInitialized()) {
            String createDb = getReqValue(request, "createDb");
            if ((createDb == null) || provider.equals(DB2_PROVIDER) || provider.equals(AS400_PROVIDER)) {
                createDb = "";
            }

            String createTables = getReqValue(request, "createTables");
            if (createTables == null) {
                createTables = "";
            }

            if (isFormSubmitted) {

                if (provider.equals(POSTGRESQL_PROVIDER)) {
                    setDb(database);

                    String templateDb = getReqValue(request, "templateDb");
                    setDbProperty(getDatabase() + ".templateDb", templateDb);
                    setDbProperty(getDatabase() + ".newDb", database);

                    if (!conStr.endsWith("/")) {
                        conStr += "/";
                    }
                    setDbProperty(getDatabase() + ".constr", conStr + getDbProperty(getDatabase() + ".templateDb"));
                    setDbProperty(getDatabase() + ".constr.newDb", conStr + getDbProperty(getDatabase() + ".newDb"));
                    conStr += database;
                } else if (provider.equals(MYSQL_PROVIDER)
                    || provider.equals(DB2_PROVIDER)
                    || provider.equals(MSSQL_PROVIDER)
                    || provider.equals(POSTGRESQL_PROVIDER)) {
                        if (!conStr.endsWith("/")) {
                            conStr += "/";
                        }
                        conStr += database;
                    } else if (provider.equals(AS400_PROVIDER)) {
                        if (conStr.endsWith("/")) {
                            conStr = conStr.substring(0, conStr.length() - 1);
                        }
                        if (!conStr.endsWith(";")) {
                            conStr += ";";
                        }
                        conStr += "libraries='" + database + "'";
                    }
                setDbWorkConStr(conStr);
                if (provider.equals(POSTGRESQL_PROVIDER)) {
                    setDb(database);
                }
                String dbCreateUser = getReqValue(request, "dbCreateUser");
                String dbCreatePwd = getReqValue(request, "dbCreatePwd");

                String dbWorkUser = getReqValue(request, "dbWorkUser");
                String dbWorkPwd = getReqValue(request, "dbWorkPwd");

                if ((dbCreateUser != null) && !provider.equals(DB2_PROVIDER) && !provider.equals(AS400_PROVIDER)) {
                    setDbCreateUser(dbCreateUser);
                }
                setDbCreatePwd(dbCreatePwd);

                if (dbWorkUser.equals("")) {
                    dbWorkUser = contextPath;
                }
                if (dbWorkUser.equals("")) {
                    dbWorkUser = "opencms";
                }
                if (dbWorkUser.startsWith("/")) {
                    dbWorkUser = dbWorkUser.substring(1, dbWorkUser.length());
                }
                setDbWorkUser(dbWorkUser);
                setDbWorkPwd(dbWorkPwd);

                if (provider.equals(ORACLE_PROVIDER)) {
                    String dbDefaultTablespace = getReqValue(request, "dbDefaultTablespace");
                    String dbTemporaryTablespace = getReqValue(request, "dbTemporaryTablespace");
                    String dbIndexTablespace = getReqValue(request, "dbIndexTablespace");

                    setDbProperty(getDatabase() + ".defaultTablespace", dbDefaultTablespace);
                    setDbProperty(getDatabase() + ".temporaryTablespace", dbTemporaryTablespace);
                    setDbProperty(getDatabase() + ".indexTablespace", dbIndexTablespace);
                }
                Map<String, String> replacer = new HashMap<String, String>();
                if (!provider.equals(MYSQL_PROVIDER) || provider.equals(MSSQL_PROVIDER)) {
                    replacer.put("${user}", dbWorkUser);
                    replacer.put("${password}", dbWorkPwd);
                }
                if (provider.equals(MYSQL_PROVIDER)
                    || provider.equals(MSSQL_PROVIDER)
                    || provider.equals(POSTGRESQL_PROVIDER)) {
                    replacer.put("${database}", database);
                }
                if (provider.equals(ORACLE_PROVIDER)) {
                    replacer.put("${defaultTablespace}", getDbProperty(getDatabase() + ".defaultTablespace"));
                    replacer.put("${indexTablespace}", getDbProperty(getDatabase() + ".indexTablespace"));
                    replacer.put("${temporaryTablespace}", getDbProperty(getDatabase() + ".temporaryTablespace"));
                }
                setReplacer(replacer);

                if (session != null) {
                    if (provider.equals(GENERIC_PROVIDER)
                        || provider.equals(ORACLE_PROVIDER)
                        || provider.equals(DB2_PROVIDER)
                        || provider.equals(AS400_PROVIDER)
                        || provider.equals(MAXDB_PROVIDER)) {
                        session.setAttribute("createTables", createTables);
                    }
                    session.setAttribute("createDb", createDb);
                }
            } else {
                String dbName = "opencms";
                // initialize the database name with the app name
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(contextPath)) {
                    dbName = contextPath.substring(1);
                }
                if (provider.equals(ORACLE_PROVIDER)
                    || provider.equals(POSTGRESQL_PROVIDER)
                    || provider.equals(MAXDB_PROVIDER)) {
                    setDbWorkUser(dbName);
                } else if (dbName != null) {
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
        Properties databaseProperties = getDatabaseProperties().get(databaseKey);
        databaseProperties.put(key, value);
    }

    /**
     * Sets the connection string to the database to the given value.<p>
     *
     * @param dbWorkConStr the connection string used by the OpenCms core
     */
    public void setDbWorkConStr(String dbWorkConStr) {

        String driver;
        String pool = '.' + getPool() + '.';

        driver = getDbProperty(m_databaseKey + ".driver");

        setExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + pool + CmsDbPoolV11.KEY_JDBC_DRIVER, driver);
        setExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + pool + CmsDbPoolV11.KEY_JDBC_URL, dbWorkConStr);
        String testQuery = getDbTestQuery();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(testQuery)) {
            setExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + pool + "v11.connectionTestQuery", testQuery);
        }
        setExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + pool + CmsDbPoolV11.KEY_JDBC_URL_PARAMS, getDbConStrParams());
    }

    /**
     * Sets the password of the database to the given value.<p>
     *
     * @param dbWorkPwd the password for the OpenCms database user
     */
    public void setDbWorkPwd(String dbWorkPwd) {

        setExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPoolV11.KEY_PASSWORD, dbWorkPwd);
    }

    /**
     * Sets the user of the database to the given value.<p>
     *
     * @param dbWorkUser the database user used by the opencms core
     */
    public void setDbWorkUser(String dbWorkUser) {

        setExtProperty(CmsDbPoolV11.KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPoolV11.KEY_POOL_USER, dbWorkUser);
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
     * Sets the fullDatabaseKey.<p>
     *
     * @param fullDatabaseKey the fullDatabaseKey to set
     */
    public void setFullDatabaseKey(String fullDatabaseKey) {

        m_fullDatabaseKey = fullDatabaseKey;

        String driverPart = fullDatabaseKey.substring(fullDatabaseKey.lastIndexOf("_"), fullDatabaseKey.length());
        String keyPart = fullDatabaseKey.substring(0, fullDatabaseKey.lastIndexOf("_"));
        setDatabase(keyPart);
    }

    /**
     * Sets the list with the package names of the modules to be installed.<p>
     *
     * @param value a string with the package names of the modules to be installed delimited by the pipe symbol "|"
     */
    public void setInstallModules(String value) {

        m_installModules = CmsStringUtil.splitAsList(value, "|", true);
        try {
            m_installModules = CmsModuleManager.topologicalSort(m_installModules, getModuleFolder());
        } catch (CmsConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the replacer.<p>
     *
     * @param map the replacer to set
     */
    public void setReplacer(Map<String, String> map) {

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
     * Sets the start view for the given user.<p>
     *
     * @param userName the name of the user
     * @param view the start view path
     *
     * @throws CmsException if something goes wrong
     */
    public void setStartView(String userName, String view) throws CmsException {

        try {

            CmsUser user = m_cms.readUser(userName);
            user.getAdditionalInfo().put(
                CmsUserSettings.PREFERENCES
                    + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                    + CmsWorkplaceConfiguration.N_WORKPLACEVIEW,
                view);
            m_cms.writeUser(user);
        } catch (CmsException e) {
            e.printStackTrace(System.err);
            throw e;
        }
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

        m_shell.getOut().println();
        m_shell.getOut().println();
        m_shell.getOut().println("The setup is finished!\nThe OpenCms system used for the setup will now shut down.");
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellStart()
     */
    public void shellStart() {

        m_shell.getOut().println();
        m_shell.getOut().println("Starting Workplace import and database setup for OpenCms!");

        String[] copy = org.opencms.main.Messages.COPYRIGHT_BY_ALKACON;
        for (int i = copy.length - 1; i >= 0; i--) {
            m_shell.getOut().println(copy[i]);
        }
        m_shell.getOut().println(
            "This is OpenCms "
                + OpenCms.getSystemInfo().getVersionNumber()
                + " ["
                + OpenCms.getSystemInfo().getVersionId()
                + "]");
        m_shell.getOut().println();
        m_shell.getOut().println();
    }

    /**
     * Sorts the modules for display.<p>
     *
     * @param modules the list of {@link CmsModule} objects
     *
     * @return a sorted list of module names
     */
    public List<String> sortModules(Collection<CmsModule> modules) {

        List<CmsModule> aux = new ArrayList<CmsModule>(modules);
        Collections.sort(aux, new Comparator<CmsModule>() {

            public int compare(CmsModule module1, CmsModule module2) {

                return getDisplayForModule(module1).compareTo(getDisplayForModule(module2));
            }
        });

        List<String> ret = new ArrayList<String>(aux.size());
        for (Iterator<CmsModule> it = aux.iterator(); it.hasNext();) {
            CmsModule module = it.next();
            ret.add(module.getName());
        }
        return ret;
    }

    /**
     * Writes configuration changes back to the XML configuration.
     */
    public void updateConfiguration() {

        // currently we only need the system configuration
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);
    }

    /**
     * Checks the jdbc driver.<p>
     *
     * @return <code>true</code> if at least one of the recommended drivers is found
     */
    public boolean validateJdbc() {

        boolean result = false;
        String libFolder = getLibFolder();
        Iterator<String> it = getDatabaseLibs(getDatabase()).iterator();
        while (it.hasNext()) {
            String libName = it.next();
            File libFile = new File(libFolder, libName);
            if (libFile.exists()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Reads all components from the given location, a folder or a zip file.<p>
     *
     * @param fileName the location to read the components from
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    protected void addComponentsFromPath(String fileName) throws CmsConfigurationException {

        CmsParameterConfiguration configuration;
        try {
            configuration = getComponentsProperties(fileName);
        } catch (FileNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            return;
        }

        for (String componentId : configuration.getList(PROPKEY_COMPONENTS)) {
            CmsSetupComponent componentBean = new CmsSetupComponent();
            componentBean.setId(componentId);
            componentBean.setName(configuration.get(PROPKEY_COMPONENT + componentId + PROPKEY_NAME));
            componentBean.setDescription(configuration.get(PROPKEY_COMPONENT + componentId + PROPKEY_DESCRIPTION));
            componentBean.setModulesRegex(configuration.get(PROPKEY_COMPONENT + componentId + PROPKEY_MODULES));
            componentBean.setDependencies(
                configuration.getList(PROPKEY_COMPONENT + componentId + PROPKEY_DEPENDENCIES));
            componentBean.setPosition(
                configuration.getInteger(PROPKEY_COMPONENT + componentId + PROPKEY_POSITION, DEFAULT_POSITION));
            componentBean.setChecked(
                configuration.getBoolean(PROPKEY_COMPONENT + componentId + PROPKEY_CHECKED, false));
            m_components.addIdentifiableObject(componentBean.getId(), componentBean, componentBean.getPosition());
        }
    }

    /**
     * Returns a pipe separated list of module names for the given list of components.<p>
     *
     * @param componentIds the list of component IDs to get the modules for
     *
     * @return a pipe separated list of module names for the given list of components
     */
    protected String getComponentModules(List<String> componentIds) {

        Set<String> comps = new HashSet<String>();
        List<CmsSetupComponent> components = CmsCollectionsGenericWrapper.list(m_components.elementList());
        for (CmsSetupComponent comp : components) {
            if (componentIds.contains(comp.getId())) {
                comps.addAll(getComponentModules(comp));
            }

        }

        StringBuffer buf = new StringBuffer();
        List<String> moduleNames = sortModules(getAvailableModules().values());
        boolean first = true;
        for (String moduleName : moduleNames) {
            if (!first) {
                buf.append("|");
            }
            if (comps.contains(moduleName)) {
                buf.append(moduleName);
            }
            first = false;
        }

        return buf.toString();
    }

    /**
     * Reads all properties from the components.properties file at the given location, a folder or a zip file.<p>
     *
     * @param location the location to read the properties from
     *
     * @return the read properties
     *
     * @throws FileNotFoundException if the properties file could not be found
     * @throws CmsConfigurationException if the something else goes wrong
     */
    protected CmsParameterConfiguration getComponentsProperties(String location)
    throws FileNotFoundException, CmsConfigurationException {

        InputStream stream = null;
        ZipFile zipFile = null;
        try {
            // try to interpret the fileName as a folder
            File folder = new File(location);

            // if it is a file it must be a zip-file
            if (folder.isFile()) {
                zipFile = new ZipFile(location);
                ZipEntry entry = zipFile.getEntry(COMPONENTS_PROPERTIES);
                // path to file might be relative, too
                if ((entry == null) && location.startsWith("/")) {
                    entry = zipFile.getEntry(location.substring(1));
                }
                if (entry == null) {
                    zipFile.close();
                    throw new FileNotFoundException(
                        org.opencms.importexport.Messages.get().getBundle().key(
                            org.opencms.importexport.Messages.LOG_IMPORTEXPORT_FILE_NOT_FOUND_IN_ZIP_1,
                            location + "/" + COMPONENTS_PROPERTIES));
                }

                stream = zipFile.getInputStream(entry);
            } else {
                // it is a folder
                File file = new File(folder, COMPONENTS_PROPERTIES);
                stream = new FileInputStream(file);
            }
            return new CmsParameterConfiguration(stream);
        } catch (Throwable ioe) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
            if (ioe instanceof FileNotFoundException) {
                throw (FileNotFoundException)ioe;
            }

            CmsMessageContainer msg = org.opencms.importexport.Messages.get().container(
                org.opencms.importexport.Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                location + "/" + COMPONENTS_PROPERTIES);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), ioe);
            }
            throw new CmsConfigurationException(msg, ioe);
        }
    }

    /**
     * Returns the value for a given key from the extended properties.
     *
     * @param key the property key
     * @return the string value for a given key
     */
    protected String getExtProperty(String key) {

        return m_configuration.getString(key, "");
    }

    /**
     * Returns html for the given component to fill the selection list.<p>
     *
     * @param component the component to generate the code for
     *
     * @return html code
     */
    protected String htmlComponent(CmsSetupComponent component) {

        StringBuffer html = new StringBuffer(256);
        html.append("\t<tr>\n");
        html.append("\t\t<td>\n");
        html.append("\t\t\t<input type='checkbox' name='availableComponents' value='");
        html.append(component.getId());
        html.append("'");
        if (component.isChecked()) {
            html.append(" checked='checked'");
        }
        html.append(" onClick=\"checkComponentDependencies('");
        html.append(component.getId());
        html.append("');\">\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='width: 100%; '>\n\t\t\t");
        html.append(component.getName());
        html.append("\n\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td>&nbsp;</td>\n");
        html.append(
            "\t\t<td style='vertical-align: top; width: 100%; padding-bottom: 8px; font-style: italic;'>\n\t\t\t");
        html.append(component.getDescription());
        html.append("\n\t\t</td>\n");
        html.append("\t</tr>\n");

        return html.toString();
    }

    /**
     * Returns html for the given module to fill the selection list.<p>
     *
     * @param module the module to generate the code for
     * @param pos the position in the list
     *
     * @return html code
     */
    protected String htmlModule(CmsModule module, int pos) {

        StringBuffer html = new StringBuffer(256);
        html.append("\t<tr>\n");
        html.append("\t\t<td style='vertical-align: top;'>\n");
        html.append("\t\t\t<input type='checkbox' name='availableModules' value='");
        html.append(module.getName());
        html.append("' checked='checked' onClick=\"checkModuleDependencies('");
        html.append(module.getName());
        html.append("');\">\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='vertical-align: top; width: 100%; padding-top: 4px;'>\n\t\t\t");
        html.append(getDisplayForModule(module));
        html.append("\n\t\t</td>\n");
        html.append("\t\t<td style='vertical-align: top; text-align: right;'>\n");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(module.getDescription())) {
            html.append("\t\t\t");
            html.append(getHtmlHelpIcon("" + pos, ""));
        }
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        return html.toString();
    }

    /**
     * Imports a module (zipfile) from the default module directory,
     * creating a temporary project for this.<p>
     *
     * @param importFile the name of the import module located in the default module directory
     *
     * @throws Exception if something goes wrong
     *
     * @see org.opencms.importexport.CmsImportExportManager#importData(CmsObject, org.opencms.report.I_CmsReport, CmsImportParameters)
     */
    protected void importModuleFromDefault(String importFile) throws Exception {

        String fileName = getModuleFolder() + importFile;
        OpenCms.getImportExportManager().importData(
            m_cms,
            new CmsShellReport(m_cms.getRequestContext().getLocale()),
            new CmsImportParameters(fileName, "/", true));
    }

    /**
     * Initializes and validates the read components.<p>
     *
     * @param modules a modifiable list of the modules to be imported
     */
    protected void initializeComponents(Collection<String> modules) {

        Iterator<CmsSetupComponent> itGroups = new ArrayList<CmsSetupComponent>(
            CmsCollectionsGenericWrapper.<CmsSetupComponent> list(m_components.elementList())).iterator();
        while (itGroups.hasNext()) {
            CmsSetupComponent component = itGroups.next();
            String errMsg = "";
            String warnMsg = "";
            // check name
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(component.getName())) {
                errMsg += Messages.get().container(Messages.ERR_COMPONENT_NAME_EMPTY_1, component.getId()).key();
                errMsg += "\n";
            }
            // check description
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(component.getName())) {
                warnMsg += Messages.get().container(Messages.LOG_WARN_COMPONENT_DESC_EMPTY_1, component.getId()).key();
                warnMsg += "\n";
            }
            // check position
            if (component.getPosition() == DEFAULT_POSITION) {
                warnMsg += Messages.get().container(Messages.LOG_WARN_COMPONENT_POS_EMPTY_1, component.getId()).key();
                warnMsg += "\n";
            }
            // check dependencies
            Iterator<String> itDeps = component.getDependencies().iterator();
            while (itDeps.hasNext()) {
                String dependency = itDeps.next();
                if (m_components.getObject(dependency) == null) {
                    errMsg += Messages.get().container(
                        Messages.LOG_WARN_COMPONENT_DEPENDENCY_BROKEN_2,
                        component.getId(),
                        dependency).key();
                    errMsg += "\n";
                }
            }
            // check modules match
            boolean match = false;
            Iterator<String> itModules = modules.iterator();
            while (itModules.hasNext()) {
                String module = itModules.next();
                if (component.match(module)) {
                    match = true;
                    itModules.remove();
                }
            }
            if (!match) {
                errMsg += Messages.get().container(Messages.ERR_COMPONENT_MODULES_EMPTY_1, component.getId()).key();
                errMsg += "\n";
            }

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(errMsg)) {
                m_components.removeObject(component.getId());
                if (LOG.isErrorEnabled()) {
                    LOG.error(errMsg);
                }
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(warnMsg)) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(warnMsg);
                }
            }
        }
        if (!modules.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().container(Messages.LOG_WARN_MODULES_LEFT_1, modules.toString()));
            }
        }
    }

    /**
     * Returns <code>true</code> if the import thread is currently running.<p>
     *
     * @return <code>true</code> if the import thread is currently running
     */
    protected boolean isImportRunning() {

        return (m_workplaceImportThread != null) && m_workplaceImportThread.isAlive();
    }

    /**
     * Stores the properties of all available database configurations in a
     * map keyed by their database key names (e.g. "mysql", "generic" or "oracle").<p>
     */
    protected void readDatabaseConfig() {

        m_databaseKeys = new ArrayList<String>();
        m_databaseProperties = new HashMap<String, Properties>();

        FileInputStream input = null;
        File childResource = null;

        List<String> databaseKeys = new ArrayList<String>();
        Map<String, Properties> databaseProperties = new HashMap<String, Properties>();

        try {
            File databaseSetupFolder = new File(m_webAppRfsPath + FOLDER_SETUP + FOLDER_DATABASE);

            File[] childResources = databaseSetupFolder.listFiles();

            // collect all possible db configurations
            if (childResources != null) {
                for (int i = 0; i < childResources.length; i++) {
                    childResource = childResources[i];
                    if (childResource.exists() && childResource.isDirectory() && childResource.canRead()) {
                        databaseKeys.add(childResource.getName().trim());
                    }
                }
            }

            // create the properties with all possible configurations
            if (databaseSetupFolder.exists()) {
                for (String key : databaseKeys) {
                    String dbDir = m_webAppRfsPath + CmsSetupBean.FOLDER_SETUP + "database" + File.separatorChar + key;
                    String configPath = dbDir + File.separatorChar + "database.properties";
                    try {
                        input = new FileInputStream(new File(configPath));
                        Properties databaseProps = new Properties();
                        databaseProps.load(input);
                        databaseProperties.put(key, databaseProps);
                    } catch (Exception e) {
                        System.err.println(e.toString());
                        e.printStackTrace(System.err);
                        continue;
                    }
                }
            }

            // add the properties and the keys to the memeber variables if all needed files exist
            if (childResources != null) {
                for (int i = 0; i < childResources.length; i++) {
                    childResource = childResources[i];
                    if (childResource.exists() && childResource.isDirectory() && childResource.canRead()) {
                        String dataBasekey = childResource.getName().trim();
                        Properties props = databaseProperties.get(dataBasekey);
                        if (checkFilesExists(REQUIRED_SQL_DB_SETUP_FILES, childResource)) {
                            m_databaseKeys.add(childResource.getName().trim());
                            m_databaseProperties.put(dataBasekey, props);
                        }
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
     *
     * @param key The key of the property
     * @param value The value of the property
     */
    protected void setExtProperty(String key, String value) {

        m_configuration.put(key, value);
    }

    /**
     * Checks if the necessary files for the configuration are existent or not.<p>
     *
     * @param requiredFiles the required files
     * @param childResource the folder to check
     *
     * @return true if the files are existent
     */
    private boolean checkFilesExists(String[] requiredFiles, File childResource) {

        File setupFile = null;
        boolean hasMissingSetupFiles = false;

        for (int j = 0; j < requiredFiles.length; j++) {
            setupFile = new File(childResource.getPath() + File.separatorChar + requiredFiles[j]);

            if (!setupFile.exists() || !setupFile.isFile() || !setupFile.canRead()) {
                hasMissingSetupFiles = true;
                System.err.println(
                    "[" + getClass().getName() + "] missing or unreadable database setup file: " + setupFile.getPath());
                break;
            }

            if (!hasMissingSetupFiles) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates an string out of the given array to store back in the property file.<p>
     *
     * @param values the array with the values to create a string from
     *
     * @return a string with the values of the array which is ready to store in the property file
     */
    private String createValueString(String[] values) {

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < values.length; i++) {

            // escape commas and equals in value
            values[i] = CmsStringUtil.substitute(values[i], ",", "\\,");
            values[i] = CmsStringUtil.substitute(values[i], "=", "\\=");

            buf.append("\t" + values[i] + ((i < (values.length - 1)) ? ",\\\n" : ""));
        }
        return buf.toString();
    }

    /**
     * Returns a list of matching modules for the given component.<p>
     *
     * @param component the component to get the modules for
     *
     * @return a list of matching module names
     */
    private List<String> getComponentModules(CmsSetupComponent component) {

        List<String> modules = new ArrayList<String>();
        Iterator<String> itModules = m_availableModules.keySet().iterator();
        while (itModules.hasNext()) {
            String moduleName = itModules.next();
            if (component.match(moduleName)) {
                modules.add(moduleName);
            }
        }
        return modules;
    }

    /**
     * Returns the text which should be written to the configuration file for a given property value.<p>
     *
     * @param obj the property value
     *
     * @return the text to write for that property value
     */
    private String getPropertyValueToWrite(Object obj) {

        String value;
        String valueToWrite = null;
        if (obj instanceof List<?>) {
            String[] values = {};
            values = CmsCollectionsGenericWrapper.list(obj).toArray(values);
            // write it
            valueToWrite = "\\\n" + createValueString(values);
        } else {
            value = String.valueOf(obj).trim();
            // escape commas and equals in value
            value = CmsStringUtil.substitute(value, ",", "\\,");
            // value = CmsStringUtil.substitute(value, "=", "\\=");
            // write it
            valueToWrite = value;
        }
        return valueToWrite;
    }

    /**
     * Returns the first value of the array for the given key.<p>
     *
     * @param map the map to search the key in
     * @param key the key to get the first value from
     *
     * @return the first value of the array for the given key
     */
    private String getReqValue(Map<String, String[]> map, String key) {

        return map.get(key) != null ? map.get(key)[0] : null;
    }

    private String paramsToString(Map<String, String[]> request) {

        String result = "";
        for (Map.Entry<String, String[]> entry : request.entrySet()) {
            String strEntry = entry.getKey() + "=(";
            for (String val : entry.getValue()) {
                strEntry += val + " ";
            }
            strEntry += ")";
            result += strEntry + " ";
        }
        return "{" + result + "}";

    }

    /**
     * Saves the properties to a file.<p>
     *
     * @param properties the properties to be saved
     * @param source the source file to get the keys from
     * @param target the target file to save the properties to
     * @param forceWrite the keys of the properties which should always be written, even if they don't exist in the configuration file
     */
    private void save(CmsParameterConfiguration properties, String source, String target, Set<String> forceWrite) {

        try {
            Set<String> alreadyWritten = new HashSet<String>();

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
                    if ((line.indexOf('=') > -1) && (index1 != (index - 1))) {

                        String key = line.substring(0, line.indexOf('=')).trim();
                        if (alreadyWritten.contains(key)) {
                            continue;
                        }
                        String additionalPrefix = ADDITIONAL_PREFIX;
                        if (key.startsWith(additionalPrefix)) {
                            String additionalPropKey = key.substring(additionalPrefix.length());
                            String additionalContent = m_additionalProperties.get(additionalPropKey);
                            if (additionalContent == null) {
                                fw.write(key + "=\n");
                            } else {
                                fw.write(additionalContent + "\n");
                            }
                            continue;
                        }
                        // write key
                        fw.write((key + "="));
                        try {
                            Object obj = properties.getObject(key);
                            if (obj != null) {
                                String valueToWrite = getPropertyValueToWrite(obj);
                                fw.write(valueToWrite);
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
            if (forceWrite != null) {
                for (String forced : forceWrite) {
                    if (!alreadyWritten.contains(forced) && properties.containsKey(forced)) {
                        fw.write("\n\n");
                        fw.write(forced + "=");
                        try {
                            Object obj = properties.getObject(forced);

                            if (obj != null) {
                                String valueToWrite = getPropertyValueToWrite(obj);
                                fw.write(valueToWrite);
                            }
                        } catch (NullPointerException e) {
                            // no value found - do nothing
                        }
                        fw.write("\n");

                    }
                }
            }

            lnr.close();
            fw.close();
        } catch (Exception e) {
            m_errors.add("Could not save properties to " + target + " \n");
            m_errors.add(e.toString() + "\n");
        }
    }

    /**
     * Sets the pool size.<p>
     *
     * @param poolSize the pool size
     */
    private void setEntityManagerPoolSize(String poolSize) {

        setExtProperty(
            CmsDbPoolV11.KEY_DATABASE_POOL + '.' + getPool() + '.' + CmsDbPoolV11.KEY_ENTITY_MANAGER_POOL_SIZE,
            poolSize);
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

        if (CmsStringUtil.isNotEmpty(System.getProperty(CmsSystemInfo.CONFIG_FOLDER_PROPERTY))) {
            m_configRfsPath = System.getProperty(CmsSystemInfo.CONFIG_FOLDER_PROPERTY);
        } else {
            m_configRfsPath = m_webAppRfsPath + CmsSystemInfo.FOLDER_WEBINF + CmsSystemInfo.FOLDER_CONFIG_DEFAULT;
        }
    }
}
