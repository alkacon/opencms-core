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

    /** The database name. */
    private String m_dbName;

    /** The pools connection data. */
    private Map<String, Map<String, String>> m_dbPools = new HashMap<String, Map<String, String>>();

    /** The detected mayor version, based on DB structure. */
    private double m_detectedVersion;

    /** List of xml update plugins. */
    private List<I_CmsUpdateDBPart> m_plugins;

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

        return true;
    }

    /**
     * Updates all database pools.<p>
     */
    public void run() {

        try {
            // add a list of plugins to execute
            // be sure to use the right order
            m_plugins = new ArrayList<I_CmsUpdateDBPart>();

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
            } else {
                m_plugins.add(new org.opencms.setup.db.update7to8.CmsUpdateDBNewTables());
                m_plugins.add(new org.opencms.setup.db.update7to8.CmsUpdatePasswordColumn());
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

        // get the db implementation name
        String dbName = getDbName();
        String name = null;
        if (dbName.indexOf("mysql") > -1) {
            getMySqlEngine(dbPoolData);
            name = "mysql";
        } else if (dbName.indexOf("oracle") > -1) {
            getOracleTablespaces(dbPoolData);
            name = "oracle";
        } else if (dbName.indexOf("postgresql") > -1) {
            getPostgreSqlTablespaces(dbPoolData);
            name = "postgresql";
        } else {
            System.out.println("db " + dbName + " not supported");
            return;
        }

        // execute update
        Iterator<I_CmsUpdateDBPart> it = m_plugins.iterator();
        while (it.hasNext()) {
            I_CmsUpdateDBPart updatePart = it.next();
            I_CmsUpdateDBPart dbUpdater = getInstanceForDb(updatePart, name);
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
}
