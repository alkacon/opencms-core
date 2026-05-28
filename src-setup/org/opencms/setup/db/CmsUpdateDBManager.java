/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.db;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.CmsUpdateBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This manager controls the update of the database from OpenCms 6 to OpenCms 7.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBManager {

    /** Property for controlling the storage schema update. */
    public static final String PARAM_STORAGE_SCHEMA_UPDATE = "setup.storage.schema.update";

    /** Automatic storage schema update mode. */
    public static final String STORAGE_SCHEMA_UPDATE_AUTO = "auto";

    /** Disabled storage schema update mode. */
    public static final String STORAGE_SCHEMA_UPDATE_FALSE = "false";

    /** Enabled storage schema update mode. */
    public static final String STORAGE_SCHEMA_UPDATE_TRUE = "true";

    /** The database name. */
    private String m_dbName;

    /** The pools connection data. */
    private Map<String, Map<String, String>> m_dbPools = new HashMap<String, Map<String, String>>();

    /** The detected mayor version, based on DB structure. */
    private double m_detectedVersion;

    /** List of xml update plugins. */
    private List<I_CmsUpdateDBPart> m_plugins;

    /** List of schema based update plugins. */
    private List<I_CmsUpdateDBPart> m_schemaPlugins;

    /** If true, the storage schema should be updated if needed. */
    private boolean m_updateStorageSchema;

    /**
     * Default constructor.<p>
     */
    public CmsUpdateDBManager() {

        // no-op
    }

    /**
     * Returns the configured jdbc driver for the given pool.<p>
     *
     * @param pool the db pool to get the driver for
     *
     * @return the driver class name
     */
    public String getDbDriver(String pool) {

        return m_dbPools.get(pool).get("driver");
    }

    /**
     * Returns the database name.<p>
     *
     * @return the database name
     */
    public String getDbName() {

        return m_dbName;
    }

    /**
     * Returns the configured jdbc url parameters for the given pool.<p>
     *
     * @param pool the db pool to get the params for
     *
     * @return the jdbc url parameters
     */
    public String getDbParams(String pool) {

        return m_dbPools.get(pool).get("params");
    }

    /**
     * Returns the configured jdbc connection url for the given pool.<p>
     *
     * @param pool the db pool to get the url for
     *
     * @return the jdbc connection url
     */
    public String getDbUrl(String pool) {

        return m_dbPools.get(pool).get("url");
    }

    /**
     * Returns the configured database user for the given pool.<p>
     *
     * @param pool the db pool to get the user for
     *
     * @return the database user
     */
    public String getDbUser(String pool) {

        return m_dbPools.get(pool).get("user");
    }

    /**
     * Returns the detected mayor version, based on DB structure.<p>
     *
     * @return the detected mayor version
     */
    public double getDetectedVersion() {

        if (m_detectedVersion == 0) {
            needUpdate();
        }
        return m_detectedVersion;
    }

    /**
     * Returns all configured database pools.<p>
     *
     * @return a list of {@link String} objects
     */
    public List<String> getPools() {

        return new ArrayList<String>(m_dbPools.keySet());
    }

    /**
     * Generates html code for the given db pool.<p>
     *
     * @param pool the db pool to generate html for
     *
     * @return html code
     *
     * @throws Exception if something goes wrong
     */
    public String htmlPool(String pool) throws Exception {

        StringBuffer html = new StringBuffer(256);

        html.append("<a href=\"javascript:switchview('").append(pool).append("');\">");
        html.append(pool).append("</a><br>\n");
        html.append("\t<div id='").append(pool).append("' style='display: none;'>\n");
        html.append("\t\t<table border='0'>\n");
        html.append("\t\t\t<tr><td>JDBC Driver:</td><td>" + getDbDriver(pool) + "</td></tr>\n");
        html.append("\t\t\t<tr><td>JDBC Connection Url:</td><td>" + getDbUrl(pool) + "</td></tr>\n");
        html.append("\t\t\t<tr><td>JDBC Connection Url Params:</td><td>" + getDbParams(pool) + "</td></tr>\n");
        html.append("\t\t\t<tr><td>Database User:</td><td>" + getDbUser(pool) + "</td></tr>\n");
        html.append("\t\t</table>\n");
        html.append("\t</div>\n");

        return html.toString();
    }

    public String htmlPool(String pool, boolean hiddenInfo) {

        if (hiddenInfo) {
            try {
                return htmlPool(pool);
            } catch (Exception e) {
                //
            }
        }
        StringBuffer html = new StringBuffer(256);

        html.append("<p>");
        html.append(pool).append("</p><br>\n");
        html.append("\t<div id='").append(pool);
        html.append("\t\t<table border='0'>\n");
        html.append("\t\t\t<tr><td>JDBC Driver:</td><td>" + getDbDriver(pool) + "</td></tr>\n");
        html.append("\t\t\t<tr><td>JDBC Connection Url:</td><td>" + getDbUrl(pool) + "</td></tr>\n");
        html.append("\t\t\t<tr><td>JDBC Connection Url Params:</td><td>" + getDbParams(pool) + "</td></tr>\n");
        html.append("\t\t\t<tr><td>Database User:</td><td>" + getDbUser(pool) + "</td></tr>\n");
        html.append("\t\t</table>\n");
        html.append("\t</div>\n");

        return html.toString();
    }

    /**
     * Initializes the Update Manager object with the updateBean to get the database connection.<p>
     *
     * @param updateBean the update bean with the database connection
     *
     * @throws Exception if the setup bean is not initialized
     */
    public void initialize(CmsUpdateBean updateBean) throws Exception {

        if (updateBean.isInitialized()) {
            CmsParameterConfiguration props = updateBean.getProperties();

            // Initialize the CmsUUID generator.
            CmsUUID.init(props.get("server.ethernet.address"));

            m_dbName = props.get("db.name");
            m_updateStorageSchema = isStorageSchemaUpdateEnabled(props.get(PARAM_STORAGE_SCHEMA_UPDATE));

            List<String> pools = CmsStringUtil.splitAsList(props.get("db.pools"), ',');
            for (String pool : pools) {
                Map<String, String> data = new HashMap<String, String>();
                data.put("driver", props.get("db.pool." + pool + ".jdbcDriver"));
                data.put("url", props.get("db.pool." + pool + ".jdbcUrl"));
                data.put("params", props.get("db.pool." + pool + ".jdbcUrl.params"));
                data.put("user", props.get("db.pool." + pool + ".user"));
                data.put("pwd", props.get("db.pool." + pool + ".password"));
                data.put("keepHistory", String.valueOf(updateBean.isKeepHistory()));
                m_dbPools.put(pool, data);
            }
        } else {
            throw new Exception("setup bean not initialized");
        }
    }

    /**
     * Checks if the storage schema update is enabled by configuration.<p>
     *
     * @param value the configured value
     *
     * @return true if the storage schema should be updated if needed
     */
    public boolean isStorageSchemaUpdateEnabled(String value) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            return false;
        }
        String normalizedValue = value.trim().toLowerCase();
        return STORAGE_SCHEMA_UPDATE_AUTO.equals(normalizedValue) || STORAGE_SCHEMA_UPDATE_TRUE.equals(normalizedValue);
    }

    /**
     * Checks if the storage schema is missing or incomplete.<p>
     *
     * @param setupDb the database connection
     *
     * @return true if the storage schema needs to be updated
     */
    public boolean needsStorageSchemaUpdate(CmsSetupDb setupDb) {

        return !setupDb.hasTableOrColumn("CMS_STORAGE", null)
            || !setupDb.hasTableOrColumn("CMS_CONTENTS", "STORAGE")
            || !setupDb.hasTableOrColumn("CMS_CONTENTS", "HASH")
            || !setupDb.hasTableOrColumn("CMS_OFFLINE_CONTENTS", "STORAGE")
            || !setupDb.hasTableOrColumn("CMS_OFFLINE_CONTENTS", "HASH")
            || !hasStorageContentIndex(setupDb)
            || !hasStorageContentStorageIndex(setupDb)
            || !hasStorageOfflineContentIndex(setupDb)
            || !hasStorageOfflineContentStorageIndex(setupDb);
    }

    /**
     * Checks if an update is needed.<p>
     *
     * @return if an update is needed
     */
    public boolean needUpdate() {

        String pool = "default";

        double currentVersion = 8.5;
        m_detectedVersion = 8.5;

        CmsSetupDb setupDb = new CmsSetupDb(null);

        try {
            setupDb.setConnection(
                getDbDriver(pool),
                getDbUrl(pool),
                getDbParams(pool),
                getDbUser(pool),
                m_dbPools.get(pool).get("pwd"));

            if (!setupDb.hasTableOrColumn("CMS_USERS", "USER_OU")) {
                m_detectedVersion = 6;
            } else if (!setupDb.hasTableOrColumn("CMS_ONLINE_URLNAME_MAPPINGS", null)) {
                m_detectedVersion = 7;
            } else if (!setupDb.hasTableOrColumn("CMS_USER_PUBLISH_LIST", null)) {
                m_detectedVersion = 8;
            }
        } finally {
            setupDb.closeConnection();
        }

        return (currentVersion != m_detectedVersion) || (m_updateStorageSchema && needsStorageSchemaUpdate());
    }

    /**
     * Updates all database pools.<p>
     */
    public void run() {

        try {
            // add a list of plugins to execute
            // be sure to use the right order
            m_plugins = new ArrayList<I_CmsUpdateDBPart>();
            m_schemaPlugins = new ArrayList<I_CmsUpdateDBPart>();

            if (getDetectedVersion() < 7) {
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBDropOldIndexes());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBUpdateOU());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBCmsUsers());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBProjectId());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBNewTables());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBHistoryTables());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBHistoryPrincipals());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBDropUnusedTables());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBContentTables());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBAlterTables());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBDropBackupTables());
                m_plugins.add(new org.opencms.setup.db.update6to7.CmsUpdateDBCreateIndexes7());
            } else if (getDetectedVersion() < 8.5) {
                m_plugins.add(new org.opencms.setup.db.update7to8.CmsUpdateDBNewTables());
                m_plugins.add(new org.opencms.setup.db.update7to8.CmsUpdatePasswordColumn());
            }

            if (m_updateStorageSchema) {
                m_schemaPlugins.add(new org.opencms.setup.db.update21to22.CmsUpdateDBStorageSchema());
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }

        Iterator<String> it = getPools().iterator();
        while (it.hasNext()) {
            String dbPool = it.next();
            System.out.println("Starting DB Update for pool " + dbPool + "... ");

            try {
                updateDatabase(dbPool);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            System.out.println("... DB Update finished for " + dbPool + ".");
        }
    }

    /**
     * Updates the database.<p>
     *
     * @param pool the database pool to update
     */
    public void updateDatabase(String pool) {

        Map<String, String> dbPoolData = new HashMap<String, String>(m_dbPools.get(pool));

        // display info
        System.out.println("JDBC Driver:                " + getDbDriver(pool));
        System.out.println("JDBC Connection Url:        " + getDbUrl(pool));
        System.out.println("JDBC Connection Url Params: " + getDbParams(pool));
        System.out.println("Database User:              " + getDbUser(pool));

        String legacyDbName = getLegacyUpdateDbName(dbPoolData);
        if (legacyDbName != null) {
            executeUpdatePlugins(dbPoolData, m_plugins, legacyDbName);
        } else if (!m_plugins.isEmpty()) {
            System.out.println("db " + getDbName() + " not supported for legacy DB updates");
        }

        String schemaDbName = getSchemaUpdateDbName(dbPoolData);
        if (schemaDbName != null) {
            executeUpdatePlugins(dbPoolData, m_schemaPlugins, schemaDbName);
        } else if (!m_schemaPlugins.isEmpty()) {
            System.out.println("db " + getDbName() + " not supported for schema DB updates");
        }
    }

    /**
     * Executes the given update plugins.<p>
     *
     * @param dbPoolData the database pool data
     * @param plugins the update plugins
     * @param dbName the database implementation name
     */
    protected void executeUpdatePlugins(
        Map<String, String> dbPoolData,
        List<I_CmsUpdateDBPart> plugins,
        String dbName) {

        Iterator<I_CmsUpdateDBPart> it = plugins.iterator();
        while (it.hasNext()) {
            I_CmsUpdateDBPart updatePart = it.next();
            I_CmsUpdateDBPart dbUpdater = getInstanceForDb(updatePart, dbName);
            if (dbUpdater != null) {
                dbUpdater.execute(dbPoolData);
            }
        }
    }

    /**
     * Creates a new instance for the given database and setting the db pool data.<p>
     *
     * @param dbUpdater the generic updater part
     * @param dbName the database to get a new instance for
     *
     * @return right instance instance for the given database
     */
    protected I_CmsUpdateDBPart getInstanceForDb(I_CmsUpdateDBPart dbUpdater, String dbName) {

        String clazz = dbUpdater.getClass().getName();
        int pos = clazz.lastIndexOf('.');
        clazz = clazz.substring(0, pos) + "." + dbName + clazz.substring(pos);
        try {
            return (I_CmsUpdateDBPart)Class.forName(clazz).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the database implementation name for legacy database updates.<p>
     *
     * @param dbPoolData the database pool data
     *
     * @return the database implementation name, or null if the database is not supported by the legacy updater
     */
    protected String getLegacyUpdateDbName(Map<String, String> dbPoolData) {

        String dbName = getDbName();
        if (dbName.indexOf("mysql") > -1) {
            getMySqlEngine(dbPoolData);
            return "mysql";
        } else if (dbName.indexOf("oracle") > -1) {
            getOracleTablespaces(dbPoolData);
            return "oracle";
        } else if (dbName.indexOf("postgresql") > -1) {
            getPostgreSqlTablespaces(dbPoolData);
            return "postgresql";
        }
        return null;
    }

    /**
     * Retrieves the mysql engine name.<p>
     *
     * @param dbPoolData the database pool data
     */
    protected void getMySqlEngine(Map<String, String> dbPoolData) {

        String engine = "MYISAM";
        CmsSetupDb setupDb = new CmsSetupDb(null);
        CmsSetupDBWrapper db = null;
        try {
            setupDb.setConnection(
                dbPoolData.get("driver"),
                dbPoolData.get("url"),
                dbPoolData.get("params"),
                dbPoolData.get("user"),
                dbPoolData.get("pwd"));

            db = setupDb.executeSqlStatement("SHOW TABLE STATUS LIKE 'CMS_GROUPS';", null);
            if (db.getResultSet().next()) {
                engine = db.getResultSet().getString("Engine").toUpperCase();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            setupDb.closeConnection();
        }
        dbPoolData.put("engine", engine);
        System.out.println("Table engine:               " + engine);
    }

    /**
     * Retrieves the oracle tablespace names.<p>
     *
     * @param dbPoolData the database pool data
     */
    protected void getOracleTablespaces(Map<String, String> dbPoolData) {

        String dataTablespace = "users";
        String indexTablespace = "users";
        CmsSetupDb setupDb = new CmsSetupDb(null);

        try {
            setupDb.setConnection(
                dbPoolData.get("driver"),
                dbPoolData.get("url"),
                dbPoolData.get("params"),
                dbPoolData.get("user"),
                dbPoolData.get("pwd"));

            // read tablespace for data
            CmsSetupDBWrapper db = null;
            try {
                db = setupDb.executeSqlStatement("SELECT DISTINCT tablespace_name FROM user_tables", null);
                if (db.getResultSet().next()) {
                    dataTablespace = db.getResultSet().getString(1).toLowerCase();
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
            // read tablespace for indexes
            try {
                db = setupDb.executeSqlStatement("SELECT DISTINCT tablespace_name FROM user_indexes", null);
                if (db.getResultSet().next()) {
                    indexTablespace = db.getResultSet().getString(1).toLowerCase();
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        dbPoolData.put("indexTablespace", indexTablespace);
        System.out.println("Index Tablespace:           " + indexTablespace);

        dbPoolData.put("dataTablespace", dataTablespace);
        System.out.println("Data Tablespace:            " + dataTablespace);
    }

    /**
     * Retrieves the postgresql tablespace names.<p>
     *
     * @param dbPoolData the database pool data
     */
    protected void getPostgreSqlTablespaces(Map<String, String> dbPoolData) {

        String dataTablespace = "pg_default";
        String indexTablespace = "pg_default";
        CmsSetupDb setupDb = new CmsSetupDb(null);

        try {
            setupDb.setConnection(
                dbPoolData.get("driver"),
                dbPoolData.get("url"),
                dbPoolData.get("params"),
                dbPoolData.get("user"),
                dbPoolData.get("pwd"));

            // read tablespace for data
            CmsSetupDBWrapper db = null;
            try {
                db = setupDb.executeSqlStatement(
                    "SELECT DISTINCT pg_tablespace.spcname FROM pg_class, pg_tablespace WHERE pg_class.relname='cms_user' AND pg_class.reltablespace = pg_tablespace.oid",
                    null);
                if (db.getResultSet().next()) {
                    dataTablespace = db.getResultSet().getString(1).toLowerCase();
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
            // read tablespace for indexes
            try {
                db = setupDb.executeSqlStatement(
                    "SELECT DISTINCT pg_tablespace.spcname FROM pg_class, pg_tablespace WHERE pg_class.relname='cms_users_pkey' AND pg_class.reltablespace = pg_tablespace.oid",
                    null);
                if (db.getResultSet().next()) {
                    indexTablespace = db.getResultSet().getString(1).toLowerCase();
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        dbPoolData.put("indexTablespace", indexTablespace);
        System.out.println("Index Tablespace:           " + indexTablespace);

        dbPoolData.put("dataTablespace", dataTablespace);
        System.out.println("Data Tablespace:            " + dataTablespace);
    }

    /**
     * Returns the database implementation name for schema based database updates.<p>
     *
     * @param dbPoolData the database pool data
     *
     * @return the database implementation name, or null if the database is not supported
     */
    protected String getSchemaUpdateDbName(Map<String, String> dbPoolData) {

        String dbName = getDbName();
        if (dbName.indexOf("mysql") > -1) {
            getMySqlEngine(dbPoolData);
            return "mysql";
        } else if (dbName.indexOf("oracle") > -1) {
            getOracleTablespaces(dbPoolData);
            return "oracle";
        } else if (dbName.indexOf("postgresql") > -1) {
            getPostgreSqlTablespaces(dbPoolData);
            return "postgresql";
        } else if (dbName.indexOf("mssql") > -1) {
            return "mssql";
        } else if (dbName.indexOf("hsqldb") > -1) {
            return "hsqldb";
        } else if (dbName.indexOf("db2") > -1) {
            return "db2";
        } else if (dbName.indexOf("as400") > -1) {
            return "as400";
        }
        return null;
    }

    /**
     * Checks if the content table storage index exists.<p>
     *
     * @param setupDb the database connection
     *
     * @return true if the index exists
     */
    protected boolean hasStorageContentIndex(CmsSetupDb setupDb) {

        return setupDb.hasIndex("CMS_CONTENTS", "CMS_CONTENTS_05_IDX")
            || setupDb.hasIndex("CMS_CONTENTS", "CMS_CONTENTS_06_IDX")
            || setupDb.hasIndex("CMS_CONTENTS", "CMS_CONTENTS_06")
            || setupDb.hasIndex("CMS_CONTENTS", "HASH_IDX");
    }

    /**
     * Checks if the content table storage-leading index exists.<p>
     *
     * @param setupDb the database connection
     *
     * @return true if the index exists
     */
    protected boolean hasStorageContentStorageIndex(CmsSetupDb setupDb) {

        return setupDb.hasIndex("CMS_CONTENTS", "CMS_CONTENTS_07_IDX")
            || setupDb.hasIndex("CMS_CONTENTS", "CMS_CONTENTS_07")
            || setupDb.hasIndex("CMS_CONTENTS", "STORAGE_IDX");
    }

    /**
     * Checks if the offline content table storage index exists.<p>
     *
     * @param setupDb the database connection
     *
     * @return true if the index exists
     */
    protected boolean hasStorageOfflineContentIndex(CmsSetupDb setupDb) {

        return setupDb.hasIndex("CMS_OFFLINE_CONTENTS", "CMS_OFFLINE_CONTENTS_01_IDX")
            || setupDb.hasIndex("CMS_OFFLINE_CONTENTS", "CMS_OFFLINE_CONTENTS_01")
            || setupDb.hasIndex("CMS_OFFLINE_CONTENTS", "HASH_IDX");
    }

    /**
     * Checks if the offline content table storage-leading index exists.<p>
     *
     * @param setupDb the database connection
     *
     * @return true if the index exists
     */
    protected boolean hasStorageOfflineContentStorageIndex(CmsSetupDb setupDb) {

        return setupDb.hasIndex("CMS_OFFLINE_CONTENTS", "CMS_OFFLINE_CONTENTS_02_IDX")
            || setupDb.hasIndex("CMS_OFFLINE_CONTENTS", "CMS_OFFLINE_CONTENTS_02")
            || setupDb.hasIndex("CMS_OFFLINE_CONTENTS", "STORAGE_IDX");
    }

    /**
     * Checks if the storage schema is missing or incomplete for the default database pool.<p>
     *
     * @return true if the storage schema needs to be updated
     */
    protected boolean needsStorageSchemaUpdate() {

        String pool = "default";
        CmsSetupDb setupDb = new CmsSetupDb(null);

        try {
            setupDb.setConnection(
                getDbDriver(pool),
                getDbUrl(pool),
                getDbParams(pool),
                getDbUser(pool),
                m_dbPools.get(pool).get("pwd"));

            return needsStorageSchemaUpdate(setupDb);
        } finally {
            setupDb.closeConnection();
        }
    }
}
