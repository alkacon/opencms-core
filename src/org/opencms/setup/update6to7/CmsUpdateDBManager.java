/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBManager.java,v $
 * Date   : $Date: 2007/05/25 08:14:37 $
 * Version: $Revision: 1.1.2.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.update6to7;

import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This manager controls the update of the database from OpenCms 6 to OpenCms 7.<p>
 * 
 * @author metzler
 *
 */
public class CmsUpdateDBManager {

    /** The database name. */
    private String m_dbName;

    /** The pools connection data. */
    private Map m_dbPools = new HashMap();

    /** The rfs path to the web app. */
    private String m_webAppRfsPath;

    /**
     * Default constructor.<p>
     */
    public CmsUpdateDBManager() {

        // Stays empty
    }

    /**
     * Returns the configured jdbc driver for the given pool.<p>
     * 
     * @param pool the db pool to get the driver for
     * 
     * @return the driver class name
     */
    public String getDbDriver(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("driver");
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

        return (String)((Map)m_dbPools.get(pool)).get("params");
    }

    /**
     * Returns the configured jdbc connection url for the given pool.<p>
     * 
     * @param pool the db pool to get the url for
     * 
     * @return the jdbc connection url
     */
    public String getDbUrl(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("url");
    }

    /**
     * Returns the configured database user for the given pool.<p>
     * 
     * @param pool the db pool to get the user for
     * 
     * @return the database user
     */
    public String getDbUser(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("user");
    }

    /**
     * Returns all configured database pools.<p>
     * 
     * @return a list of {@link String} objects
     */
    public List getPools() {

        return new ArrayList(m_dbPools.keySet());
    }

    /**
     * Initializes the Update Manager object with the updateBean to get the database connection.<p>
     * 
     * @param updateBean the update bean with the database connection
     * 
     * @throws Exception if the setup bean is not initialized 
     */
    public void initialize(CmsSetupBean updateBean) throws Exception {

        if (updateBean.isInitialized()) {
            m_webAppRfsPath = updateBean.getWebAppRfsPath();

            //String db = updateBean.getDatabase(); // just to initialize some internal vars
            ExtendedProperties props = updateBean.getProperties();

            // Initialize the CmsUUID generator.
            CmsUUID.init(props.getString("server.ethernet.address"));

            m_dbName = props.getString("db.name");

            List pools = props.getList("db.pools");
            for (Iterator it = pools.iterator(); it.hasNext();) {
                String pool = (String)it.next();
                Map data = new HashMap();
                data.put("driver", props.getString("db.pool." + pool + ".jdbcDriver"));
                data.put("url", props.getString("db.pool." + pool + ".jdbcUrl"));
                data.put("params", props.getString("db.pool." + pool + ".jdbcUrl.params"));
                data.put("user", props.getString("db.pool." + pool + ".user"));
                data.put("pwd", props.getString("db.pool." + pool + ".password"));
                m_dbPools.put(pool, data);
            }
        } else {
            throw new Exception("setup bean not initialized");
        }
    }

    /**
     * Updates all database pools.<p>
     */
    public void run() {

        Iterator it = getPools().iterator();
        while (it.hasNext()) {
            String dbPool = (String)it.next();
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
     * @return true if everything worked out, false if not
     * 
     * @param pool the database pool to update
     * 
     * @throws IOException if the query properties are unreadable
     */
    public boolean updateDatabase(String pool) throws IOException {

        boolean result = false;

        System.out.println("JDBC Driver:                " + getDbDriver(pool));
        System.out.println("JDBC Connection Url:        " + getDbUrl(pool));
        System.out.println("JDBC Connection Url Params: " + getDbParams(pool));
        System.out.println("Database User:              " + getDbUser(pool));

        CmsSetupDb setupDb = new CmsSetupDb(m_webAppRfsPath);

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBDropOldIndexes dropIndexes = new CmsUpdateDBDropOldIndexes(setupDb, m_webAppRfsPath);
            dropIndexes.dropAllIndexes();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBUpdateOU updateOUs = new CmsUpdateDBUpdateOU(setupDb, m_webAppRfsPath);
            updateOUs.updateUOsForTables();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBCmsUsers updateCmsUsers = new CmsUpdateDBCmsUsers(setupDb, m_webAppRfsPath);
            updateCmsUsers.updateCmsUsers();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBProjectId updateProjectIds = new CmsUpdateDBProjectId(setupDb, m_webAppRfsPath);
            updateProjectIds.generateUUIDs();
            updateProjectIds.updateUUIDs();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            // Generate the new tables
            CmsUpdateDBNewTables newTables = new CmsUpdateDBNewTables(setupDb, m_webAppRfsPath);
            newTables.createNewTables();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));
            // transfer the data from the backup tables to the new history tables
            CmsUpdateDBHistoryTables historyTables = new CmsUpdateDBHistoryTables(setupDb, m_webAppRfsPath);
            historyTables.transferBackupToHistoryTables();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBHistoryPrincipals updateHistoryPrincipals = new CmsUpdateDBHistoryPrincipals(
                setupDb,
                m_webAppRfsPath);
            boolean update = updateHistoryPrincipals.insertHistoryPrincipals();
            if (update) {
                updateHistoryPrincipals.updateHistoryPrincipals();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        //        try {
        //            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));
        //
        //            CmsUpdateDBIndexUpdater indexUpdater = new CmsUpdateDBIndexUpdater(setupDb, m_webAppRfsPath);
        //            indexUpdater.updateIndexes();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        } finally {
        //            setupDb.closeConnection();
        //        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBDropUnusedTables dropUnusedTables = new CmsUpdateDBDropUnusedTables(setupDb, m_webAppRfsPath);
            dropUnusedTables.dropUnusedTables();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBContentTables updateContentTables = new CmsUpdateDBContentTables(setupDb, m_webAppRfsPath);
            updateContentTables.transferData();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBAlterTables alterTables = new CmsUpdateDBAlterTables(setupDb, m_webAppRfsPath);
            alterTables.updateRemaingTables();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBDropBackupTables dropBackupTables = new CmsUpdateDBDropBackupTables(setupDb, m_webAppRfsPath);
            dropBackupTables.dropBackupTables();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));
            // generate the new UUIDs
            CmsUpdateDBCreateIndexes7 createNewIndexes = new CmsUpdateDBCreateIndexes7(setupDb, m_webAppRfsPath);
            createNewIndexes.createNewIndexes();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        return result;
    }

    /**
     * Returns the configured database password for the given pool.<p>
     * 
     * @param pool the db pool to get the password for
     * 
     * @return the database password
     */
    private String getDbPwd(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("pwd");
    }
}
