/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/A_CmsUpdateDBPart.java,v $
 * Date   : $Date: 2007/07/26 09:03:26 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represent a part of the database update process.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.9.2 
 */
public abstract class A_CmsUpdateDBPart implements I_CmsUpdateDBPart {

    /** The filename/path of the SQL query properties. */
    protected static final String QUERY_PROPERTIES_PREFIX = "org/opencms/setup/update6to7/";

    /** The connection data to use. */
    protected Map m_poolData;

    /** A map holding all SQL queries. */
    protected Map m_queries;

    /**
     * Default constructor.<p>
     */
    public A_CmsUpdateDBPart() {

        m_queries = new HashMap();
    }

    /**
     * @see org.opencms.setup.update6to7.I_CmsUpdateDBPart#execute()
     */
    public void execute() {

        CmsSetupDb setupDb = new CmsSetupDb(null);

        try {
            setupDb.setConnection(
                (String)m_poolData.get("driver"),
                (String)m_poolData.get("url"),
                (String)m_poolData.get("params"),
                (String)m_poolData.get("user"),
                (String)m_poolData.get("pwd"));

            internalExecute(setupDb);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }
    }

    /**
     * @see org.opencms.setup.update6to7.I_CmsUpdateDBPart#getDbInstance(String, Map)
     */
    public I_CmsUpdateDBPart getDbInstance(String dbName, Map dbPoolData) {

        m_poolData = new HashMap(dbPoolData);
        if (dbName.indexOf("mysql") > -1) {
            String engine = "MYISAM";
            CmsSetupDb setupDb = new CmsSetupDb(null);
            CmsSetupDBWrapper db = null;
            try {
                setupDb.setConnection(
                    (String)m_poolData.get("driver"),
                    (String)m_poolData.get("url"),
                    (String)m_poolData.get("params"),
                    (String)m_poolData.get("user"),
                    (String)m_poolData.get("pwd"));

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
            m_poolData.put("engine", engine);
            System.out.println("Table engine:               " + engine);

            return getInstanceForDb("mysql");
        } else if (dbName.indexOf("oracle") > -1) {

            String dataTablespace = "users";
            String indexTablespace = "users";
            CmsSetupDb setupDb = new CmsSetupDb(null);

            try {
                setupDb.setConnection(
                    (String)m_poolData.get("driver"),
                    (String)m_poolData.get("url"),
                    (String)m_poolData.get("params"),
                    (String)m_poolData.get("user"),
                    (String)m_poolData.get("pwd"));

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

            m_poolData.put("indexTablespace", indexTablespace);
            System.out.println("Index Tablespace:           " + indexTablespace);

            m_poolData.put("dataTablespace", dataTablespace);
            System.out.println("Data Tablespace:            " + dataTablespace);

            return getInstanceForDb("oracle");
        } else {
            System.out.println("db " + dbName + " not supported");
            return null;
        }
    }

    /**
     * Returns the database pool Data.<p>
     *
     * @return the database pool Data
     */
    public Map getPoolData() {

        return Collections.unmodifiableMap(m_poolData);
    }

    /**
     * Searches for the SQL query with the specified key.<p>
     * 
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(String queryKey) {

        return (String)m_queries.get(queryKey);
    }

    /**
     * @see org.opencms.setup.update6to7.I_CmsUpdateDBPart#setPoolData(java.util.Map)
     */
    public void setPoolData(Map poolData) {

        m_poolData = new HashMap(poolData);
    }

    /**
     * Does the hard work.<p>
     * 
     * @param setupDb the db connection interface
     * 
     * @throws SQLException if somethign goes wrong
     */
    protected abstract void internalExecute(CmsSetupDb setupDb) throws SQLException;

    /**
     * Returns the keep History parameter value.<p>
     *
     * @return the keep History parameter value
     */
    protected boolean isKeepHistory() {

        Boolean keepHistory = (Boolean)m_poolData.get("keepHistory");
        if ((keepHistory != null) && keepHistory.booleanValue()) {
            return true;
        }
        return false;
    }

    /**
     * Loads a Java properties hash containing SQL queries.<p>
     * 
     * @param propertyFilename the package/filename of the properties hash
     * 
     * @throws IOException if the sql queries property file could not be read 
     */
    protected void loadQueryProperties(String propertyFilename) throws IOException {

        Properties properties = new Properties();

        properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
        m_queries.putAll(properties);
    }

    /**
     * Creates a new instance for the given database and setting the db pool data.<p>
     * 
     * @param dbName the database to get a new instance for
     * 
     * @return a new instance for the given database
     */
    private I_CmsUpdateDBPart getInstanceForDb(String dbName) {

        String clazz = getClass().getName();
        clazz = CmsStringUtil.substitute(clazz, ".generic.", "." + dbName + ".");
        I_CmsUpdateDBPart updatePart = null;
        try {
            updatePart = (I_CmsUpdateDBPart)Class.forName(clazz).newInstance();
            updatePart.setPoolData(getPoolData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updatePart;
    }
}
