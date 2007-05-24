/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBManager.java,v $
 * Date   : $Date: 2007/05/24 13:07:19 $
 * Version: $Revision: 1.1.2.1 $
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

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.CmsSetupLoggingThread;

/**
 * This manager controls the update of the database from OpenCms 6 to OpenCms 7.<p>
 * 
 * @author metzler
 *
 */
public class CmsUpdateDBManager {

    private Map m_dbPools = new HashMap();

    /** The ethernet address of the system.<p> */
    private String m_ethernetAddress;

    /** Logging thread. */
    private CmsSetupLoggingThread m_loggingThread;

    /** System.out and System.err are redirected to this stream. */
    private PipedOutputStream m_pipedOut;

    /** Saves the System.err stream so it can be restored. */
    private PrintStream m_tempErr;

    /** Saves the System.out stream so it can be restored. */
    private PrintStream m_tempOut;

    /** The rfs path to the web app. */
    private String m_webAppRfsPath;

    /**
     * Default constructor.<p>
     */
    public CmsUpdateDBManager() {

        // Stays empty
    }

    /**
     * Gets the ethernet address of the system.<p>
     * 
     * @return the ethernetAddress
     */
    public String getEthernetAddress() {

        return m_ethernetAddress;
    }

    /**
     * Returns the logging thread.<p>
     * 
     * @return the logging thread
     */
    public CmsSetupLoggingThread getLoggingThread() {

        return m_loggingThread;
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

            m_ethernetAddress = props.getString("server.ethernet.address");

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
     * Returns the status of the logging thread.<p>
     * 
     * @return the status of the logging thread 
     */
    public boolean isFinished() {

        return m_loggingThread.isFinished();
    }

    /**
     * Kills this Thread as well as the included logging Thread.<p> 
     */
    public void kill() {

        if (m_loggingThread != null) {
            m_loggingThread.stopThread();
        }
    }

    /**
     * Write somthing to System.out during setup.<p>
     * 
     * @param str the string to write
     */
    public void printToStdOut(String str) {

        m_tempOut.println(str);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        // save the original out and err stream 
        m_tempOut = System.out;
        m_tempErr = System.err;
        try {
            // init stream and logging thread
            m_pipedOut = new PipedOutputStream();
            m_loggingThread = new CmsSetupLoggingThread(m_pipedOut, new StringBuffer(m_webAppRfsPath).append(
                CmsSystemInfo.FOLDER_WEBINF).append(CmsLog.FOLDER_LOGS).append(
                "db-" + (String)getPools().get(0) + "-update.log").toString());

            // redirect the streams 
            System.setOut(new PrintStream(m_pipedOut));
            System.setErr(new PrintStream(m_pipedOut));

            // start the logging thread 
            m_loggingThread.start();

            updateDatabase((String)getPools().get(0));
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // stop the logging thread
            kill();
            try {
                if (m_pipedOut != null) {
                    m_pipedOut.close();
                }
            } catch (IOException e) {
                //ignore
                e.printStackTrace();
            }
            // restore to the old streams
            System.setOut(m_tempOut);
            System.setErr(m_tempErr);
        }
    }

    /**
     * Sets the ethernet address of the system.<p>
     * 
     * @param ethernetAddress the ethernetAddress to set
     */
    public void setEthernetAddress(String ethernetAddress) {

        m_ethernetAddress = ethernetAddress;
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

        CmsSetupDb setupDb = new CmsSetupDb(m_webAppRfsPath);
        try {
            setupDb.setConnection(getDbDriver(pool), getDbUrl(pool), getDbParams(pool), getDbUser(pool), getDbPwd(pool));

            CmsUpdateDBUpdateOU updateOU = new CmsUpdateDBUpdateOU(setupDb, m_webAppRfsPath);
            result = updateOU.updateUOsForTables();
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
            // generate the new UUIDs
            CmsUpdateDBProjectId updateProjectIDs = new CmsUpdateDBProjectId(setupDb, m_webAppRfsPath);
            updateProjectIDs.updateUUIDs(m_ethernetAddress);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }

        //        CmsUpdateDBNewTables newTables = new CmsUpdateDBNewTables(m_setupDb, m_setupBean.getWebAppRfsPath());
        //        try {
        //            // Generate the new tables
        //            newTables.createNewTables();
        //
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBHistoryTables historyTables = new CmsUpdateDBHistoryTables(m_setupDb, m_setupBean.getWebAppRfsPath());
        //        try {
        //            // transfer the data from the backup tables to the new history tables
        //            historyTables.transferBackupToHistoryTables();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBHistoryPrincipals updateHistoryPrincipals = new CmsUpdateDBHistoryPrincipals(
        //            m_setupDb,
        //            m_setupBean.getWebAppRfsPath());
        //        try {
        //            boolean update = updateHistoryPrincipals.insertHistoryPrincipals();
        //            if (update) {
        //                updateHistoryPrincipals.updateHistoryPrincipals();
        //            }
        //
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBIndexUpdater indexUpdater = new CmsUpdateDBIndexUpdater(m_setupDb, m_setupBean.getWebAppRfsPath());
        //        try {
        //            indexUpdater.updateIndexes();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBDropUnusedTables dropUnusedTables = new CmsUpdateDBDropUnusedTables(
        //            m_setupDb,
        //            m_setupBean.getWebAppRfsPath());
        //
        //        try {
        //            dropUnusedTables.dropUnusedTables();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBContentTables updateContentTables = new CmsUpdateDBContentTables(
        //            m_setupDb,
        //            m_setupBean.getWebAppRfsPath());
        //
        //        try {
        //            updateContentTables.transferData();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBAlterTables alterTables = new CmsUpdateDBAlterTables(m_setupDb, m_setupBean.getWebAppRfsPath());
        //
        //        try {
        //            alterTables.updateRemaingTables();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        CmsUpdateDBDropBackupTables dropBackupTables = new CmsUpdateDBDropBackupTables(
        //            m_setupDb,
        //            m_setupBean.getWebAppRfsPath());
        //
        //        try {
        //            dropBackupTables.dropBackupTables();
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        setupDb.closeConnection();

        return result;
    }

    private String getDbDriver(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("driver");
    }

    private String getDbParams(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("params");
    }

    private String getDbPwd(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("pwd");
    }

    private String getDbUrl(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("url");
    }

    private String getDbUser(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("user");
    }
}
