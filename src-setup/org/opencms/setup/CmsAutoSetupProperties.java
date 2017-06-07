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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.main.CmsLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Reads and manages the configuration file for the OpenCms auto-setup.<p>
 * Note that each property set in the file is overwritten if the identical property is set as Java system property (what is used, e.g., by the setup wizard).
 * Moreover, the property <code>setup.install.components</code> can be set via an accordingly named environment variable.
 *
 * @since 6.0.0
 */
public final class CmsAutoSetupProperties {

    /** The log object for this class. */
    public static final Log LOG = CmsLog.getLog(CmsAutoSetupProperties.class);

    /** The property key <code>db.connection.url</code> for providing the JDBC connection string,
     * e.g., <code>jdbc:mysql://localhost:3306/</code> for the default MySQL installation.
     */
    public static final String PROP_DB_CONNECTION_URL = "db.connection.url";

    /** The property key <code>db.create.db</code> for specifying if the database should be created during the setup.<P>
     *  Set to <code>true</code> to create the database automatically, or <code>false</code> if it should not.<P>
     *  NOTE: Automatic database creation is not supported for all DBMSs.
     */
    public static final String PROP_DB_CREATE_DB = "db.create.db";

    /** The property key <code>db.create.pwd</code> for specifying the password for the database user that is used during the setup connection. */
    public static final String PROP_DB_CREATE_PWD = "db.create.pwd";

    /** The property key <code>db.create.tables</code> for specifying if the database tables in the OpenCms database should be created.<P>
     *  Set to <code>true</code> to create the database automatically, or <code>false</code> if it should not.<P>
     */
    public static final String PROP_DB_CREATE_TABLES = "db.create.tables";

    /** The property key <code>db.create.user</code> for specifying the name of the database user that is used during the setup connection.
     *  NOTE: The user must have administration permissions. The user data is deleted, when the setup is finished.
     * */
    public static final String PROP_DB_CREATE_USER = "db.create.user";

    /** The property key <code>db.default.tablespace</code> necessary dependent on the chosen DBMS. */
    public static final String PROP_DB_DEFAULT_TABLESPACE = "db.default.tablespace";

    /** The property key <code>db.dropDb</code> for specifying if an existing database should be dropped when the setup is configured to create a database with the same name.
     *  Set to <code>true</code> if existing databases should be dropped when necessary, or to <code>false</code> if not.
     */
    public static final String PROP_DB_DROP_DB = "db.dropDb";

    /** The property key <code>db.index.tablespace</code> necessary dependent on the chosen DBMS. */
    public static final String PROP_DB_INDEX_TABLESPACE = "db.index.tablespace";

    /** The property key <code>db.jdbc.driver</code> for specifying the fully qualified name of the Java class implementing the JDBC driver to use for the connection.<P>
     * Hint: The names can be found in the <code>database.properties</code> file in the webapp' folders <code>setup/database/<db.product>/</code>.
     * */
    public static final String PROP_DB_JDBC_DRIVER = "db.jdbc.driver";

    /** The property key <code>db.name</code> for specifying the name of the database used by OpenCms, e.g. choose "opencms". */
    public static final String PROP_DB_NAME = "db.name";

    /** The property key <code>db.product</code> for specifying the used DBMS. Values should match the folders under the OpenCms webapp's folder <code>setup/database/</code>. */
    public static final String PROP_DB_PRODUCT = "db.product";

    /** The property key <code>db.provider</code> for specifying the database provider. This either <code>jpa</code> or the DBMS' product name. <P>
     * Hint: The available providers are defined as constants in {@link org.opencms.setup.CmsSetupBean}. */
    public static final String PROP_DB_PROVIDER = "db.provider";

    /** The property key <code>db.template.db</code> necessary dependent of the chosen DBMS. */
    public static final String PROP_DB_TEMPLATE_DB = "db.template.db";

    /** The property key <code>db.temporary.tablespace</code> necessary dependent of the chosen DBMS.. */
    public static final String PROP_DB_TEMPORARY_TABLESPACE = "db.temporary.tablespace";

    /** The property key <code>db.worker.pwd</code> for providing the password of the database user that is used when running OpenCms after the setup.
    *  CAUTION: For security reasons, the user should not have administration permissions.
    *  The user data is stored in the <code>opencms.properties</code> file after the setup.
    */
    public static final String PROP_DB_WORKER_PWD = "db.worker.pwd";

    /** The property key <code>db.worker.user</code> for providing the name of the database user that is used for the connection when running OpenCms after the setup.
     *  CAUTION: For security reasons, the user should not have administration permissions.
     *  The user data is stored in the <code>opencms.properties</code> file after the setup.
    . */
    public static final String PROP_DB_WORKER_USER = "db.worker.user";

    /** The property key <code>server.ethernet.address</code>. Specify a valid MAC-Address. It is used internally by OpenCms. (If not given, the address is generated automatically.) */
    public static final String PROP_SERVER_ETHERNET_ADDRESS = "server.ethernet.address";

    /** The property key <code>server.name</code> for specifying the server's name. Special server names are of particular interest in a cluster installation.*/
    public static final String PROP_SERVER_NAME = "server.name";

    /** The property key <code>server.servlet.mapping</code> for specifying the name of the OpenCms servlet (by default opencms). */
    public static final String PROP_SERVER_SERVLET_MAPPING = "server.servlet.mapping";

    /** The property key <code>server.url</code> for specifying the server's URL. It is used, e.g., for the site configuration. */
    public static final String PROP_SERVER_URL = "server.url";

    /** The property key <code>setup.default.webapp</code>. Provide the default webapp in your servlet container (Default: ROOT). */
    public static final String PROP_SETUP_DEFAULT_WEBAPP = "setup.default.webapp";

    /** The property key <code>setup.install.components</code> to choose the components that should be installed during the setup.<P>
     * The available components are configured in <code>setup/components.properties</code> in the webapp's folder.<P>
     * NOTE: You can specify the components to install also as Java system property (highest priority) or via an environment variable (second choice).
     * The value specified in the configuration file is only the third choice.
     */
    public static final String PROP_SETUP_INSTALL_COMPONENTS = "setup.install.components";

    /** The property key <code>setup.show.progress</code> to specify if dots '.' should be printed to the real STDOUT while a setup is in progress. */
    public static final String PROP_SETUP_SHOW_PROGRESS = "setup.show.progress";

    /** The property key <code>setup.webapp.path</code> to specify the path of the OpenCms webapp to install, e.g., <code>/var/lib/tomcat7/webapps/opencms</code>. */
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

    /** The db provider (sql or jpa). */
    private String m_dbProvider;

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
    private String m_servletMapping;

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

    /** Indicates if dots '.' should be printed to the real STDOUT while a setup is in progress. */
    private boolean m_showProgress;

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
        m_dbProvider = addProperty(PROP_DB_PROVIDER);
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
        m_servletMapping = addProperty(PROP_SERVER_SERVLET_MAPPING);
        m_showProgress = Boolean.valueOf(addProperty(PROP_SETUP_SHOW_PROGRESS)).booleanValue();

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
     * Returns the dbProvider.<p>
     *
     * @return the dbProvider
     */
    public String getDbProvider() {

        return m_dbProvider;
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

        return m_servletMapping;
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
     * Returns the setupWebappPath.<p>
     *
     * @return the setupWebappPath
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
     * Indicates if dots '.' should be printed to the real STDOUT while a setup is in progress.<p>
     *
     * @return true if dots '.' should be printed to the real STDOUT while a setup is in progress
     */
    public boolean isShowProgress() {

        return m_showProgress;
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
        result.put("dbProduct", new String[] {getDbProduct()});
        result.put("dbProvider", new String[] {getDbProvider()});
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