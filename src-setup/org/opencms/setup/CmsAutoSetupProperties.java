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
import org.opencms.main.CmsLog;

import java.io.IOException;
import java.util.ArrayList;
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
    public static final String PROP_SERVER_ETHERNET_ADDRESS = "server.ethernet.address";

    /** A property file key. */
    public static final String PROP_SERVER_NAME = "server.name";

    /** A property file key. */
    public static final String PROP_SERVER_SERVLET_MAPPING = "server.servlet.mapping";

    /** A property file key. */
    public static final String PROP_SERVER_URL = "server.url";

    /** A property file key. */
    public static final String PROP_SETUP_DEFAULT_WEBAPP = "setup.default.webapp";

    /** A property file key. */
    public static final String PROP_SETUP_INSTALL_COMPONENTS = "setup.install.components";

    /** A property file key. */
    public static final String PROP_SETUP_WEBAPP_PATH = "setup.webapp.path";

    /** The configuration from <code>opencms.properties</code>. */
    private CmsParameterConfiguration m_configuration;

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

    /** The ethernet address. */
    private String m_ethernetAddress;

    /** The index table space for oracle DBs. */
    private String m_indexTablespace;

    /** The list of component IDs to install. */
    private List<String> m_installComponents = new ArrayList<String>();

    /** The name of the jdbc driver to use. */
    private String m_jdbcDriver;

    /** The servlet mapping. */
    private String m_serveltMapping;

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
     * Public constructor.<p>
     * 
     * @param propertiesFile the path to the setup properties file
     *  
     * @throws IOException if the property file could not be read
     * @throws SecurityException if the environment variables could not be read
     */
    public CmsAutoSetupProperties(String propertiesFile)
    throws IOException, SecurityException {

        m_configuration = new CmsParameterConfiguration(propertiesFile);

        m_setupWebappPath = addProperty(PROP_SETUP_WEBAPP_PATH);
        m_setupDefaultWebappName = addProperty(PROP_SETUP_DEFAULT_WEBAPP);
        m_dbProduct = addProperty(PROP_DB_PRODUCT);
        m_dbName = addProperty(PROP_DB_NAME);
        m_createUser = addProperty(PROP_DB_CREATE_USER);
        m_createPwd = addProperty(PROP_DB_CREATE_PWD);
        m_workerUser = addProperty(PROP_DB_WORKER_USER);
        m_workerPwd = addProperty(PROP_DB_WORKER_PWD);
        m_connectionUrl = addProperty(PROP_DB_CONNECTION_URL);
        m_createDb = Boolean.valueOf(addProperty(PROP_DB_CREATE_DB)).booleanValue();
        m_createTables = Boolean.valueOf(addProperty(PROP_DB_CREATE_TABLES)).booleanValue();
        m_dropDb = Boolean.valueOf(addProperty(PROP_DB_DROP_DB)).booleanValue();
        m_defaultTablespace = addProperty(PROP_DB_DEFAULT_TABLESPACE);
        m_indexTablespace = addProperty(PROP_DB_INDEX_TABLESPACE);
        m_jdbcDriver = addProperty(PROP_DB_JDBC_DRIVER);
        m_templateDb = addProperty(PROP_DB_TEMPLATE_DB);
        m_temporaryTablespace = addProperty(PROP_DB_TEMPORARY_TABLESPACE);
        m_serverUrl = addProperty(PROP_SERVER_URL);
        m_serverName = addProperty(PROP_SERVER_NAME);
        m_ethernetAddress = addProperty(PROP_SERVER_ETHERNET_ADDRESS);
        m_serveltMapping = addProperty(PROP_SERVER_SERVLET_MAPPING);

        if (System.getProperty(PROP_SETUP_INSTALL_COMPONENTS) != null) {
            m_configuration.put(PROP_SETUP_INSTALL_COMPONENTS, System.getProperty(PROP_SETUP_INSTALL_COMPONENTS));
        } else if (System.getenv(PROP_SETUP_INSTALL_COMPONENTS) != null) {
            m_configuration.put(PROP_SETUP_INSTALL_COMPONENTS, System.getenv(PROP_SETUP_INSTALL_COMPONENTS));
        }
        m_installComponents = m_configuration.getList(PROP_SETUP_INSTALL_COMPONENTS);
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
     * Returns the ethernetAddress.<p>
     *
     * @return the ethernetAddress
     */
    public String getEthernetAddress() {

        return m_ethernetAddress;
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
     * Returns the serveltMapping.<p>
     *
     * @return the serveltMapping
     */
    public String getServeltMapping() {

        return m_serveltMapping;
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
        result.put("dbCreatePwd", new String[] {getCreatePwd() == null ? "" : getCreatePwd()});
        result.put("dbWorkUser", new String[] {getWorkerUser()});
        result.put("dbWorkPwd", new String[] {getWorkerPwd() == null ? "" : getWorkerPwd()});
        result.put("dbDefaultTablespace", new String[] {getDefaultTablespace()});
        result.put("dbTemporaryTablespace", new String[] {getTemporaryTablespace()});
        result.put("dbIndexTablespace", new String[] {getIndexTablespace()});
        result.put("dropDb", new String[] {Boolean.toString(isDropDb())});
        result.put("servletMapping", new String[] {getServeltMapping()});
        result.put("submit", new String[] {Boolean.TRUE.toString()});

        return result;
    }

    /**
     * Adds and returns the property for the given key.<p>
     * 
     * @param key the key to add the property
     * 
     * @return the value of that property
     */
    private String addProperty(String key) {

        if (System.getProperty(key) != null) {
            m_configuration.put(key, System.getProperty(key));
        }
        return m_configuration.get(key);
    }
}