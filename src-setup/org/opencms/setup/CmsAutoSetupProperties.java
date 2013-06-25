/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Reads and manages the test.properties file.<p>
 * 
 * @since 6.0.0
 */
public final class CmsAutoSetupProperties {

    /** The log object for this class. */
    public static final Log LOG = CmsLog.getLog(CmsAutoSetupProperties.class);

    /** A property file key. */
    public static final String PROP_DB_CONNECTION_URL = "db.connection.url";

    /** A property file key. */
    public static final String PROP_DB_CREATE_DB = "db.create.db";

    /** A property file key. */
    public static final String PROP_DB_CREATE_PWD = "db.create.pwd";

    /** A property file key. */
    public static final String PROP_DB_CREATE_TABLES = "db.create.tables";

    /** A property file key. */
    public static final String PROP_DB_CREATE_USER = "db.create.user";

    /** A property file key. */
    public static final String PROP_DB_DEFAULT_TABLESPACE = "db.default.tablespace";

    /** A property file key. */
    public static final String PROP_DB_DROP_DB = "db.dropDb";

    /** A property file key. */
    public static final String PROP_DB_INDEX_TABLESPACE = "db.index.tablespace";

    /** A property file key. */
    public static final String PROP_DB_JDBC_DRIVER = "db.jdbc.driver";

    /** A property file key. */
    public static final String PROP_DB_NAME = "db.name";

    /** A property file key. */
    public static final String PROP_DB_PRODUCT = "db.product";

    /** A property file key. */
    public static final String PROP_DB_TEMPLATE_DB = "db.template.db";

    /** A property file key. */
    public static final String PROP_DB_TEMPORARY_TABLESPACE = "db.temporary.tablespace";

    /** A property file key. */
    public static final String PROP_DB_WORKER_PWD = "db.worker.pwd";

    /** A property file key. */
    public static final String PROP_DB_WORKER_USER = "db.worker.user";

    /** A property file key. */
    public static final String PROP_SETUP_DEFAULT_WEBAPP = "setup.default.webapp";

    /** A property file key. */
    public static final String PROP_SETUP_INSTALL_COMPONENTS = "install.components";

    /** A property file key. */
    public static final String PROP_SETUP_SERVER_NAME = "server.name";

    /** A property file key. */
    public static final String PROP_SETUP_SERVER_URL = "server.url";

    /** A property file key. */
    public static final String PROP_SETUP_WEBAPP_PATH = "setup.webapp.path";

    /** The configuration from <code>opencms.properties</code>. */
    private static CmsParameterConfiguration m_configuration;

    /** The singleton instance. */
    private static CmsAutoSetupProperties m_setupSingleton;

    /** The connection String/URL. */
    private String m_connectionUrl;

    /** The create db flag. */
    private boolean m_createDb;

    /** The db user pwd for the setup. */
    private String m_createPwd;

    /** The create table flag. */
    private boolean m_createTables;

    /** The db user name for the setup. */
    private String m_createUser;

    /** The database name for OpenCms. */
    private String m_dbName;

    /** The database to use. */
    private String m_dbProduct;

    /** The default table space for oracle DBs. */
    private String m_defaultTablespace;

    /** The drop db flag. */
    private boolean m_dropDb;

    /** The index table space for oracle DBs. */
    private String m_indexTablespace;

    /** The list of component IDs to install. */
    private List<String> m_installComponents = new ArrayList<String>();

    /** The name of the jdbc driver to use. */
    private String m_jdbcDriver;

    /** The server name. */
    private String m_serverName;

    /** The server URL. */
    private String m_serverUrl;

    /** The name of the OpenCms webapp. */
    private String m_setupDefaultWebappName;

    /** The path to the webapp setup folder. */
    private String m_setupWebappPath;

    /** The template DB for PostgreSQL. */
    private String m_templateDb;

    /** The temporary table space for oracle DBs. */
    private String m_temporaryTablespace;

    /** The servlet containers webapps folder. */
    private String m_webappPath;

    /** The db user name for production. */
    private String m_workerPwd;

    /** The db user pwd for production. */
    private String m_workerUser;

    /**
     * Private default constructor.
     */
    private CmsAutoSetupProperties() {

        // noop
    }

    /**
     * @return the singleton instance
     */
    public static CmsAutoSetupProperties getInstance() {

        if (m_setupSingleton == null) {
            throw new RuntimeException("You have to initialize the setup properties.");
        }
        return m_setupSingleton;
    }

    /**
     * Returns the absolute path name for the given relative 
     * path name if it was found by the context Classloader of the 
     * current Thread.<p>
     * 
     * The argument has to denote a resource within the Classloaders 
     * scope. A <code>{@link java.net.URLClassLoader}</code> implementation for example would 
     * try to match a given path name to some resource under it's URL 
     * entries.<p>
     * 
     * As the result is internally obtained as an URL it is reduced to 
     * a file path by the call to <code>{@link java.net.URL#getFile()}</code>. Therefore 
     * the returned String will start with a '/' (no problem for java.io).<p>
     * 
     * @param fileName the filename to return the path from the Classloader for
     * 
     * @return the absolute path name for the given relative 
     *   path name if it was found by the context Classloader of the 
     *   current Thread or an empty String if it was not found
     * 
     * @see Thread#getContextClassLoader()
     */
    public static String getResourcePathFromClassloader(String fileName) {

        boolean isFolder = CmsResource.isFolder(fileName);
        String result = "";
        URL inputUrl = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (inputUrl != null) {
            // decode name here to avoid url encodings in path name
            result = CmsFileUtil.normalizePath(inputUrl);
            if (isFolder && !CmsResource.isFolder(result)) {
                result = result + '/';
            }
        } else {
            try {
                URLClassLoader cl = (URLClassLoader)Thread.currentThread().getContextClassLoader();
                URL[] paths = cl.getURLs();
                LOG.error("Missing classloader resource: " + fileName + "\n" + Arrays.asList(paths));
            } catch (Throwable t) {
                LOG.error("Missing classloader resource: " + fileName);
            }
        }
        return result;
    }

    /**
     * Reads property file setup.properties and fills singleton members.<p>
     * 
     * @param basePath the path where to find the setup.properties file
     */
    public static void initialize(String basePath) {

        if (m_setupSingleton != null) {
            return;
        }

        m_setupSingleton = new CmsAutoSetupProperties();

        try {
            String setupPropPath = null;
            String propertiesFileName = "setup.properties";

            if (basePath != null) {
                setupPropPath = CmsFileUtil.addTrailingSeparator(basePath) + propertiesFileName;
                File propFile = new File(setupPropPath);
                if (!propFile.exists()) {
                    setupPropPath = CmsAutoSetupProperties.getResourcePathFromClassloader(propertiesFileName);
                }
            }

            if (setupPropPath == null) {
                throw new RuntimeException(
                    "Setup property file ('setup.properties') could not be found by context Classloader.");
            }
            File f = new File(setupPropPath);
            if (!f.exists()) {
                throw new RuntimeException(
                    "Setup property file ('setup.properties') could not be found. Context Classloader suggested location: "
                        + setupPropPath);
            }
            m_configuration = new CmsParameterConfiguration(setupPropPath);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }

        try {
            // for java 1.4, getenv is deprecated and raises an error,
            // so prefer properties set with "-D"
            // read environment and update configuration if required
            boolean allowGetEnv = true;

            // get the setup webapp path
            if (System.getProperty(PROP_SETUP_WEBAPP_PATH) != null) {
                m_configuration.put(PROP_SETUP_WEBAPP_PATH, System.getProperty(PROP_SETUP_WEBAPP_PATH));
            } else if (allowGetEnv && (System.getenv(PROP_SETUP_WEBAPP_PATH) != null)) {
                m_configuration.put(PROP_SETUP_WEBAPP_PATH, System.getenv(PROP_SETUP_WEBAPP_PATH));
            }
            m_setupSingleton.m_setupWebappPath = m_configuration.get(PROP_SETUP_WEBAPP_PATH);

            // get the default webapp name
            if (System.getProperty(PROP_SETUP_DEFAULT_WEBAPP) != null) {
                m_configuration.put(PROP_SETUP_DEFAULT_WEBAPP, System.getProperty(PROP_SETUP_DEFAULT_WEBAPP));
            } else if (allowGetEnv && (System.getenv(PROP_SETUP_DEFAULT_WEBAPP) != null)) {
                m_configuration.put(PROP_SETUP_DEFAULT_WEBAPP, System.getenv(PROP_SETUP_DEFAULT_WEBAPP));
            }
            m_setupSingleton.m_setupDefaultWebappName = m_configuration.get(PROP_SETUP_DEFAULT_WEBAPP);

            // get the db product name
            if (System.getProperty(PROP_DB_PRODUCT) != null) {
                m_configuration.put(PROP_DB_PRODUCT, System.getProperty(PROP_DB_PRODUCT));
            } else if (allowGetEnv && (System.getenv(PROP_DB_PRODUCT) != null)) {
                m_configuration.put(PROP_DB_PRODUCT, System.getenv(PROP_DB_PRODUCT));
            }
            m_setupSingleton.m_dbProduct = m_configuration.get(PROP_DB_PRODUCT);

            // get the db name
            if (System.getProperty(PROP_DB_NAME) != null) {
                m_configuration.put(PROP_DB_NAME, System.getProperty(PROP_DB_NAME));
            } else if (allowGetEnv && (System.getenv(PROP_DB_NAME) != null)) {
                m_configuration.put(PROP_DB_NAME, System.getenv(PROP_DB_NAME));
            }
            m_setupSingleton.m_dbName = m_configuration.get(PROP_DB_NAME);

            // get the user name used for the setup process
            if (System.getProperty(PROP_DB_CREATE_USER) != null) {
                m_configuration.put(PROP_DB_CREATE_USER, System.getProperty(PROP_DB_CREATE_USER));
            } else if (allowGetEnv && (System.getenv(PROP_DB_CREATE_USER) != null)) {
                m_configuration.put(PROP_DB_CREATE_USER, System.getenv(PROP_DB_CREATE_USER));
            }
            m_setupSingleton.m_createUser = m_configuration.get(PROP_DB_CREATE_USER);

            // get the db user password used for the setup process
            if (System.getProperty(PROP_DB_CREATE_PWD) != null) {
                m_configuration.put(PROP_DB_CREATE_PWD, System.getProperty(PROP_DB_CREATE_PWD));
            } else if (allowGetEnv && (System.getenv(PROP_DB_CREATE_PWD) != null)) {
                m_configuration.put(PROP_DB_CREATE_PWD, System.getenv(PROP_DB_CREATE_PWD));
            }
            m_setupSingleton.m_createPwd = m_configuration.get(PROP_DB_CREATE_PWD) != null
            ? m_configuration.get(PROP_DB_CREATE_PWD)
            : "";

            // get the db user used for production
            if (System.getProperty(PROP_DB_WORKER_USER) != null) {
                m_configuration.put(PROP_DB_WORKER_USER, System.getProperty(PROP_DB_WORKER_USER));
            } else if (allowGetEnv && (System.getenv(PROP_DB_WORKER_USER) != null)) {
                m_configuration.put(PROP_DB_WORKER_USER, System.getenv(PROP_DB_WORKER_USER));
            }
            m_setupSingleton.m_workerUser = m_configuration.get(PROP_DB_WORKER_USER);

            // get the db user password used for production
            if (System.getProperty(PROP_DB_WORKER_PWD) != null) {
                m_configuration.put(PROP_DB_WORKER_PWD, System.getProperty(PROP_DB_WORKER_PWD));
            } else if (allowGetEnv && (System.getenv(PROP_DB_WORKER_PWD) != null)) {
                m_configuration.put(PROP_DB_WORKER_PWD, System.getenv(PROP_DB_WORKER_PWD));
            }
            m_setupSingleton.m_workerPwd = m_configuration.get(PROP_DB_WORKER_PWD) != null
            ? m_configuration.get(PROP_DB_WORKER_PWD)
            : "";

            // get the connection URL
            if (System.getProperty(PROP_DB_CONNECTION_URL) != null) {
                m_configuration.put(PROP_DB_CONNECTION_URL, System.getProperty(PROP_DB_CONNECTION_URL));
            } else if (allowGetEnv && (System.getenv(PROP_DB_CONNECTION_URL) != null)) {
                m_configuration.put(PROP_DB_CONNECTION_URL, System.getenv(PROP_DB_CONNECTION_URL));
            }
            m_setupSingleton.m_connectionUrl = m_configuration.get(PROP_DB_CONNECTION_URL);

            // get the create db flag
            if (System.getProperty(PROP_DB_CREATE_DB) != null) {
                m_configuration.put(PROP_DB_CREATE_DB, System.getProperty(PROP_DB_CREATE_DB));
            } else if (allowGetEnv && (System.getenv(PROP_DB_CREATE_DB) != null)) {
                m_configuration.put(PROP_DB_CREATE_DB, System.getenv(PROP_DB_CREATE_DB));
            }
            m_setupSingleton.m_createDb = Boolean.valueOf(m_configuration.get(PROP_DB_CREATE_DB)).booleanValue();

            // get the create db flag
            if (System.getProperty(PROP_DB_CREATE_TABLES) != null) {
                m_configuration.put(PROP_DB_CREATE_TABLES, System.getProperty(PROP_DB_CREATE_TABLES));
            } else if (allowGetEnv && (System.getenv(PROP_DB_CREATE_TABLES) != null)) {
                m_configuration.put(PROP_DB_CREATE_TABLES, System.getenv(PROP_DB_CREATE_TABLES));
            }
            m_setupSingleton.m_createTables = Boolean.valueOf(m_configuration.get(PROP_DB_CREATE_TABLES)).booleanValue();

            // get the create db flag
            if (System.getProperty(PROP_DB_DROP_DB) != null) {
                m_configuration.put(PROP_DB_DROP_DB, System.getProperty(PROP_DB_DROP_DB));
            } else if (allowGetEnv && (System.getenv(PROP_DB_DROP_DB) != null)) {
                m_configuration.put(PROP_DB_DROP_DB, System.getenv(PROP_DB_DROP_DB));
            }
            m_setupSingleton.m_dropDb = Boolean.valueOf(m_configuration.get(PROP_DB_DROP_DB)).booleanValue();

            // get the create db flag
            if (System.getProperty(PROP_DB_DEFAULT_TABLESPACE) != null) {
                m_configuration.put(PROP_DB_DEFAULT_TABLESPACE, System.getProperty(PROP_DB_DEFAULT_TABLESPACE));
            } else if (allowGetEnv && (System.getenv(PROP_DB_DEFAULT_TABLESPACE) != null)) {
                m_configuration.put(PROP_DB_DEFAULT_TABLESPACE, System.getenv(PROP_DB_DEFAULT_TABLESPACE));
            }
            m_setupSingleton.m_defaultTablespace = m_configuration.get(PROP_DB_DEFAULT_TABLESPACE);

            // get the create db flag
            if (System.getProperty(PROP_DB_INDEX_TABLESPACE) != null) {
                m_configuration.put(PROP_DB_INDEX_TABLESPACE, System.getProperty(PROP_DB_INDEX_TABLESPACE));
            } else if (allowGetEnv && (System.getenv(PROP_DB_INDEX_TABLESPACE) != null)) {
                m_configuration.put(PROP_DB_INDEX_TABLESPACE, System.getenv(PROP_DB_INDEX_TABLESPACE));
            }
            m_setupSingleton.m_indexTablespace = m_configuration.get(PROP_DB_INDEX_TABLESPACE);

            // get the create db flag
            if (System.getProperty(PROP_DB_JDBC_DRIVER) != null) {
                m_configuration.put(PROP_DB_JDBC_DRIVER, System.getProperty(PROP_DB_JDBC_DRIVER));
            } else if (allowGetEnv && (System.getenv(PROP_DB_JDBC_DRIVER) != null)) {
                m_configuration.put(PROP_DB_JDBC_DRIVER, System.getenv(PROP_DB_JDBC_DRIVER));
            }
            m_setupSingleton.m_jdbcDriver = m_configuration.get(PROP_DB_JDBC_DRIVER);

            // get the create db flag
            if (System.getProperty(PROP_DB_TEMPLATE_DB) != null) {
                m_configuration.put(PROP_DB_TEMPLATE_DB, System.getProperty(PROP_DB_TEMPLATE_DB));
            } else if (allowGetEnv && (System.getenv(PROP_DB_TEMPLATE_DB) != null)) {
                m_configuration.put(PROP_DB_TEMPLATE_DB, System.getenv(PROP_DB_TEMPLATE_DB));
            }
            m_setupSingleton.m_templateDb = m_configuration.get(PROP_DB_TEMPLATE_DB);

            // get the create db flag
            if (System.getProperty(PROP_DB_TEMPORARY_TABLESPACE) != null) {
                m_configuration.put(PROP_DB_TEMPORARY_TABLESPACE, System.getProperty(PROP_DB_TEMPORARY_TABLESPACE));
            } else if (allowGetEnv && (System.getenv(PROP_DB_TEMPORARY_TABLESPACE) != null)) {
                m_configuration.put(PROP_DB_TEMPORARY_TABLESPACE, System.getenv(PROP_DB_TEMPORARY_TABLESPACE));
            }
            m_setupSingleton.m_temporaryTablespace = m_configuration.get(PROP_DB_TEMPORARY_TABLESPACE);

            // get the create db flag
            if (System.getProperty(PROP_SETUP_INSTALL_COMPONENTS) != null) {
                m_configuration.put(PROP_SETUP_INSTALL_COMPONENTS, System.getProperty(PROP_SETUP_INSTALL_COMPONENTS));
            } else if (allowGetEnv && (System.getenv(PROP_SETUP_INSTALL_COMPONENTS) != null)) {
                m_configuration.put(PROP_SETUP_INSTALL_COMPONENTS, System.getenv(PROP_SETUP_INSTALL_COMPONENTS));
            }
            m_setupSingleton.m_installComponents = m_configuration.getList(PROP_SETUP_INSTALL_COMPONENTS);

            if (System.getProperty(PROP_SETUP_SERVER_URL) != null) {
                m_configuration.put(PROP_SETUP_SERVER_URL, System.getProperty(PROP_SETUP_SERVER_URL));
            } else if (allowGetEnv && (System.getenv(PROP_SETUP_SERVER_URL) != null)) {
                m_configuration.put(PROP_SETUP_SERVER_URL, System.getenv(PROP_SETUP_SERVER_URL));
            }
            m_setupSingleton.m_serverUrl = m_configuration.get(PROP_SETUP_SERVER_URL);

            if (System.getProperty(PROP_SETUP_SERVER_NAME) != null) {
                m_configuration.put(PROP_SETUP_SERVER_NAME, System.getProperty(PROP_SETUP_SERVER_NAME));
            } else if (allowGetEnv && (System.getenv(PROP_SETUP_SERVER_NAME) != null)) {
                m_configuration.put(PROP_SETUP_SERVER_NAME, System.getenv(PROP_SETUP_SERVER_NAME));
            }
            m_setupSingleton.m_serverName = m_configuration.get(PROP_SETUP_SERVER_NAME);

        } catch (SecurityException e) {
            // unable to read environment, use only properties from file
            e.printStackTrace(System.out);
        }
    }

    /**
     * Returns the connectionUrl.<p>
     *
     * @return the connectionUrl
     */
    public String getConnectionUrl() {

        return m_connectionUrl;
    }

    /**
     * Returns the createPwd.<p>
     *
     * @return the createPwd
     */
    public String getCreatePwd() {

        return m_createPwd;
    }

    /**
     * Returns the createUser.<p>
     *
     * @return the createUser
     */
    public String getCreateUser() {

        return m_createUser;
    }

    /**
     * @return the name of the db name used
     */
    public String getDbName() {

        return m_dbName;
    }

    /**
     * @return the name of the db product used
     */
    public String getDbProduct() {

        return m_dbProduct;
    }

    /**
     * Returns the defaultTablespace.<p>
     *
     * @return the defaultTablespace
     */
    public String getDefaultTablespace() {

        return m_defaultTablespace;
    }

    /**
     * Returns the indexTablespace.<p>
     *
     * @return the indexTablespace
     */
    public String getIndexTablespace() {

        return m_indexTablespace;
    }

    /**
     * Returns the installComponents.<p>
     *
     * @return the installComponents
     */
    public List<String> getInstallComponents() {

        return m_installComponents;
    }

    /**
     * Returns the jdbcDriver.<p>
     *
     * @return the jdbcDriver
     */
    public String getJdbcDriver() {

        return m_jdbcDriver;
    }

    /**
     * Returns the serverName.<p>
     *
     * @return the serverName
     */
    public String getServerName() {

        return m_serverName;
    }

    /**
     * Returns the serverUrl.<p>
     *
     * @return the serverUrl
     */
    public String getServerUrl() {

        return m_serverUrl;
    }

    /**
     * Returns the setup configuration object.<p>
     * 
     * @return the setup configuration object
     */
    public CmsParameterConfiguration getSetupConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the setupWebappName.<p>
     *
     * @return the setupWebappName
     */
    public String getSetupDefaultWebappName() {

        return m_setupDefaultWebappName;
    }

    /**
     * @return the path to the webapp test directory
     */
    public String getSetupWebappPath() {

        return m_setupWebappPath;
    }

    /**
     * Returns the templateDb.<p>
     *
     * @return the templateDb
     */
    public String getTemplateDb() {

        return m_templateDb;
    }

    /**
     * Returns the temporaryTablespace.<p>
     *
     * @return the temporaryTablespace
     */
    public String getTemporaryTablespace() {

        return m_temporaryTablespace;
    }

    /**
     * Returns the webappPath.<p>
     *
     * @return the webappPath
     */
    public String getWebappPath() {

        return m_webappPath;
    }

    /**
     * Returns the workerPwd.<p>
     *
     * @return the workerPwd
     */
    public String getWorkerPwd() {

        return m_workerPwd;
    }

    /**
     * Returns the workerUser.<p>
     *
     * @return the workerUser
     */
    public String getWorkerUser() {

        return m_workerUser;
    }

    /**
     * Returns the createDb.<p>
     *
     * @return the createDb
     */
    public boolean isCreateDb() {

        return m_createDb;
    }

    /**
     * Returns the createTables.<p>
     *
     * @return the createTables
     */
    public boolean isCreateTables() {

        return m_createTables;
    }

    /**
     * Returns the dropDb.<p>
     *
     * @return the dropDb
     */
    public boolean isDropDb() {

        return m_dropDb;
    }

    /**
     * Converts and returns this object as map.<p>
     * 
     * @return this object as map
     */
    public Map<String, String[]> toParameterMap() {

        Map<String, String[]> result = new HashMap<String, String[]>();

        result.put("dbCreateConStr", new String[] {getConnectionUrl()});
        result.put("dbName", new String[] {getDbName()});
        result.put("db", new String[] {getDbName()});
        result.put("createDb", new String[] {Boolean.toString(isCreateDb())});
        result.put("createTables", new String[] {Boolean.toString(isCreateTables())});
        result.put("jdbcDriver", new String[] {getJdbcDriver()});
        result.put("templateDb", new String[] {getTemplateDb()});
        result.put("dbCreateUser", new String[] {getCreateUser()});
        result.put("dbCreatePwd", new String[] {getCreatePwd()});
        result.put("dbWorkUser", new String[] {getWorkerUser()});
        result.put("dbWorkPwd", new String[] {getWorkerPwd()});
        result.put("dbDefaultTablespace", new String[] {getDefaultTablespace()});
        result.put("dbTemporaryTablespace", new String[] {getTemporaryTablespace()});
        result.put("dbIndexTablespace", new String[] {getIndexTablespace()});
        result.put("dropDb", new String[] {Boolean.toString(isDropDb())});

        return result;
    }
}