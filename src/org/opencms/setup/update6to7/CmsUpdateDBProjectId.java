/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBProjectId.java,v $
 * Date   : $Date: 2007/05/24 15:10:51 $
 * Version: $Revision: 1.1.2.3 $
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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.file.CmsProject;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * This class updates the project ids from integer values to CmsUUIDs in all existing database tables.<p>
 * 
 * It creates new UUIDs for each existing project and stores it into a temporary table.<p>
 * 
 * For each table using a project id a new column for the UUID is added and the according data is transferred.<p>
 * After that the original indexes and the column for the project id index is dropped and the new column with the
 * project uuid becomes the primary key.<p>
 * 
 * @author metzler
 */
public class CmsUpdateDBProjectId {

    /** Constant for the sql column PROJECT_ID.<p> */
    private static final String COLUMN_PROJECT_ID = "PROJECT_ID";

    /** Constant for the sql primary key of the CMS_PROJECTRESOURCES table.<p> */
    private static final String COLUMN_PROJECT_ID_RESOURCE_PATH = "PROJECT_ID,RESOURCE_PATH(255)";

    /** Constant for the sql query to use the column PROJECT_LASTMODIFIED.<p> */
    private static final String COLUMN_PROJECT_LASTMODIFIED = "PROJECT_LASTMODIFIED";

    /** Constant for the sql column PROJECT_UUID.<p> */
    private static final String COLUMN_PROJECT_UUID = "PROJECT_UUID";

    /** Constant for the sql column TEMP_PROJECT_UUID.<p> */
    private static final String COLUMN_TEMP_PROJECT_UUID = "TEMP_PROJECT_UUID";

    /** Constant for the table name of the CMS_HISTORY_PROJECTS table.<p> */
    private static final String HISTORY_PROJECTS_TABLE = "CMS_HISTORY_PROJECTS";

    /** Constant for the sql query to add a new primary key.<p> */
    private static final String QUERY_ADD_PRIMARY_KEY = "Q_ADD_PRIMARY_KEY";

    /** Constant for the sql query to add a new column to the table.<p> */
    private static final String QUERY_ADD_TEMP_UUID_COLUMN = "Q_ADD_COLUMN";

    /** Constant for the sql query to create the new CMS_HISTORY_PROJECTS table.<p> */
    private static final String QUERY_CREATE_HISTORY_PROJECTS_TABLE = "Q_CREATE_HISTORY_PROJECTS_TABLE";

    /** Constant for the sql query to create the temporary table.<p> */
    private static final String QUERY_CREATE_TEMP_TABLE_UUIDS = "Q_CREATE_TEMPORARY_TABLE_UUIDS";

    /** Constant for the sql query to describe the given table.<p> */
    private static final String QUERY_DESCRIBE_TABLE = "Q_DESCRIBE_TABLE";

    /** Constant for the sql query to drop a given column.<p> */
    private static final String QUERY_DROP_COLUMN = "Q_DROP_COLUMN";

    /** Constant for the sql query to drop the given index.<p> */
    private static final String QUERY_DROP_INDEX = "Q_DROP_INDEX";

    /** Constant for the sql query to drop the primary key of a table.<p> */
    private static final String QUERY_DROP_PRIMARY_KEY = "Q_DROP_PRIMARY_KEY";

    /** Constant for the sql query to get the project ids.<p> */
    private static final String QUERY_GET_PROJECT_IDS = "Q_SELECT_PROJECT_IDS";

    /** Constant for the sql query to get the uuids and project ids.<p> */
    private static final String QUERY_GET_UUIDS = "Q_SELECT_UUIDS";

    /** Constant for the sql query to insert the data into the CMS_HISTORY_PROJECTS table.<p> */
    private static final String QUERY_INSERT_CMS_HISTORY_TABLE = "Q_INSERT_CMS_HISTORY_TABLE";

    /** Constant for the sql query to insert a pair of values to the temp table.<p> */
    private static final String QUERY_INSERT_UUIDS = "Q_INSERT_UUIDS_TEMP_TABLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_projectid_queries.properties";

    /** Constant for the sql query to add a rename a column in the table.<p> */
    private static final String QUERY_RENAME_COLUMN = "Q_RENAME_COLUMN";

    /** Constant for the sql query to select the data from the CMS_BACKUP_PROJECTS table.<p> */
    private static final String QUERY_SELECT_DATA_FROM_BACKUP_PROJECTS = "Q_SELECT_DATA_FROM_BACKUP_PROJECTS";

    /** Constant for the sql query to transfer the new uuids to the temporary column.<p> */
    private static final String QUERY_TRANSFER_UUID = "Q_TRANSFER_UUID";

    /** Constant for the replacement in the SQL query for the columnname.<p> */
    private static final String REPLACEMENT_COLUMN = "${column}";

    /** Constant for the replacement in the SQL query for the index name.<p> */
    private static final String REPLACEMENT_INDEX = "${index}";

    /** Constant for the replacement in the SQL query for the new columnname.<p> */
    private static final String REPLACEMENT_NEW_COLUMN = "${newcolumn}";

    /** Constant for the replacement in the SQL query for old id to update.<p> */
    private static final String REPLACEMENT_OLDID = "${oldid}";

    /** Constant for the replacement in the SQL query for the primary key.<p> */
    private static final String REPLACEMENT_PRIMARY_KEY = "${primarykeycolumn}";

    /** Constant for the replacement in the SQL query for the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Array of the online and offline resources tables.<p> */
    private static final String[] RESOURCE_TABLES = {"CMS_OFFLINE_RESOURCES", "CMS_ONLINE_RESOURCES"};

    /** Arraylist for the online and offline resources tables that shall be updated.<p> */
    private static final List RESOURCES_TABLES_LIST = Collections.unmodifiableList(Arrays.asList(RESOURCE_TABLES));

    /** Array of the tables that are to be updated.<p> */
    private static final String[] TABLES = {
        "CMS_OFFLINE_RESOURCES",
        "CMS_ONLINE_RESOURCES",
        "CMS_PROJECTRESOURCES",
        "CMS_PROJECTS"};

    /** Arraylist for the tables that shall be updated.<p> */
    private static final List TABLES_LIST = Collections.unmodifiableList(Arrays.asList(TABLES));

    /** Constant for the temporary UUID column in the tables.<p> */
    private static final String TEMP_UUID_COLUMN = "TEMP_PROJECT_UUID";

    /** Constant for the name of temporary table containing the project ids and uuids.<p> */
    private static final String TEMPORARY_TABLE_NAME = "TEMP_PROJECT_UUIDS";

    /** The database connection.<p> */
    private CmsSetupDb m_dbcon;

    /** The sql queries.<p> */
    private ExtendedProperties m_queryProperties;

    /**
     * Constructor with paramaters for the database connection and query properties file.<p>
     * 
     * @param dbcon the database connection
     * @param rfsPath the path to the opencms installation
     * 
     * @throws IOException if the query properties cannot be read
     * 
     */
    public CmsUpdateDBProjectId(CmsSetupDb dbcon, String rfsPath)
    throws IOException {

        System.out.println(getClass().getName());
        m_dbcon = dbcon;
        m_queryProperties = CmsPropertyUtils.loadProperties(rfsPath + QUERY_PROPERTY_FILE);
    }

    /**
     * Creates the temporary table to store the project ids and uuids.<p>
     * 
     * @param toCreate the constant of the table name to create
     * 
     * @throws SQLException if something goes wrong
     * 
     */
    public void createNewTable(String toCreate) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = (String)m_queryProperties.get(toCreate);
        m_dbcon.updateSqlStatement(query, null, null);
    }

    /**
     * Generates the new UUIDs for the project ids.<p>
     * The new uuids are stored in the temporary table.<p>
     * 
     * @throws SQLException if something goes wrong 
     */
    public void generateUUIDs() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = (String)m_queryProperties.get(QUERY_GET_PROJECT_IDS);

        ResultSet set = m_dbcon.executeSqlStatement(query, null);
        ResultSetMetaData metaData = set.getMetaData();
        // Check the type of the column if it is integer, then create the new uuids
        int columnType = metaData.getColumnType(1);
        if (columnType == java.sql.Types.INTEGER) {
            if (!m_dbcon.hasTableOrColumn(TEMPORARY_TABLE_NAME, null)) {
                createNewTable(QUERY_CREATE_TEMP_TABLE_UUIDS);

                String updateQuery = (String)m_queryProperties.get(QUERY_INSERT_UUIDS);
                List params = new ArrayList();
                // Get the project id and insert it with a new uuid into the temp table
                boolean hasNullId = false;
                while (set.next()) {
                    int id = set.getInt("PROJECT_ID");
                    params.add(new Integer(id)); // Add the number
                    CmsUUID uuid = new CmsUUID();

                    // Check for 0 project id
                    if (id == 0) {
                        hasNullId = true;
                        uuid = CmsUUID.getNullUUID();
                    }
                    // Check for the online project
                    if (id == 1) {
                        uuid = CmsProject.ONLINE_PROJECT_ID;
                    }
                    params.add(uuid); // Add the uuid

                    // Insert the values to the temp table
                    m_dbcon.updateSqlStatement(updateQuery, null, params);

                    params.clear();
                }

                // If no project id with value 0 was found 
                if (!hasNullId) {
                    params.add(new Integer(0));
                    params.add(CmsUUID.getNullUUID());
                    m_dbcon.updateSqlStatement(updateQuery, null, params);
                }
            } else {
                System.out.println("table " + TEMPORARY_TABLE_NAME + " already exists");
            }
        }
    }

    /**
     * Gets the database connection.<p>
     * 
     * @return the dbcon
     */
    public CmsSetupDb getDbcon() {

        return m_dbcon;
    }

    /**
     * Gets the query properties.<p>
     * 
     * @return the queryProperties
     */
    public ExtendedProperties getQueryProperties() {

        return m_queryProperties;
    }

    /**
     * Sets the database connection.<p>
     * 
     * @param dbcon the dbcon to set
     */
    public void setDbcon(CmsSetupDb dbcon) {

        m_dbcon = dbcon;
    }

    /**
     * Sets the query properties.<p>
     *  
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(ExtendedProperties queryProperties) {

        m_queryProperties = queryProperties;
    }

    /**
     * Transfers the data from the CMS_BACKUP_PROJECTS to the CMS_HISTORY_PROJECTS table.<p>
     * 
     * The datetime type for the column PROJECT_PUBLISHDATE is converted to the new long value
     * 
     * @throws SQLException if something goes wrong
     *
     */
    public void transferDataToHistoryTable() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Get the data from the CMS_BACKUP table
        String query = (String)m_queryProperties.get(QUERY_SELECT_DATA_FROM_BACKUP_PROJECTS);
        ResultSet set = m_dbcon.executeSqlStatement(query, null);

        List params = new ArrayList();
        String insertQuery = (String)m_queryProperties.get(QUERY_INSERT_CMS_HISTORY_TABLE);
        while (set.next()) {
            // Add the values to be inserted into the CMS_HISTORY_PROJECTS table
            params.add(set.getString("PROJECT_UUID"));
            params.add(set.getString("PROJECT_NAME"));
            params.add(set.getString("PROJECT_DESCRIPTION"));
            params.add(new Integer(set.getInt("PROJECT_TYPE")));
            params.add(set.getString("USER_ID"));
            params.add(set.getString("GROUP_ID"));
            params.add(set.getString("MANAGERGROUP_ID"));
            params.add(new Long(set.getLong("DATE_CREATED")));
            params.add(new Integer(set.getInt("PUBLISH_TAG")));
            Date date = set.getDate("PROJECT_PUBLISHDATE");
            params.add(new Long(date.getTime()));
            params.add(set.getString("PROJECT_PUBLISHED_BY"));
            params.add(set.getString("PROJECT_OU"));

            m_dbcon.updateSqlStatement(insertQuery, null, params);
            // Clear the parameter list for the next loop
            params.clear();
        }
    }

    /**
     * Updates the tables with the according new UUIDs.<p>
     * 
     * @throws SQLException if something goes wrong 
     */
    public void updateUUIDs() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        // Check for the CMS_HISTORY_PROJECTS table and transfer the data to it
        if (!m_dbcon.hasTableOrColumn(HISTORY_PROJECTS_TABLE, null)) {
            createNewTable(QUERY_CREATE_HISTORY_PROJECTS_TABLE);
            transferDataToHistoryTable();
        } else {
            System.out.println("table " + HISTORY_PROJECTS_TABLE + " already exists");
        }

        Map uuids = getUUIDs(); // Get the UUIDS
        
        /*
         * Add the temporary column for the new UUIDs and fill it with data
         */
        for (Iterator it = TABLES_LIST.iterator(); it.hasNext();) {
            String tablename = (String)it.next();

            if (needsUpdating(tablename)) {
                addUUIDColumnToTable(tablename, TEMP_UUID_COLUMN);
                boolean isInResourcesList = RESOURCES_TABLES_LIST.contains(tablename);
                // Add the new uuids
                Iterator entries = uuids.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry)entries.next();
                    if (entry.getKey() != null && entry.getValue() != null) {
                        if (isInResourcesList) {
                            fillUUIDSColumn(
                                tablename,
                                TEMP_UUID_COLUMN,
                                (String)entry.getValue(),
                                COLUMN_PROJECT_LASTMODIFIED,
                                (String)entry.getKey());
                        } else {
                            fillUUIDSColumn(
                                tablename,
                                TEMP_UUID_COLUMN,
                                (String)entry.getValue(),
                                COLUMN_PROJECT_ID,
                                (String)entry.getKey());
                        }
                    }
                }

                /*
                 * In this phase the primary keys or indexes are dropped and the old columns containing the 
                 * old project ids are dropped. After that the temporary columns are renamed and the new
                 * indexes and primary keys are added.
                 */
                if (isInResourcesList) {

                    // Drop the column PROJECT_LASTMODIFIED
                    dropColumn(tablename, COLUMN_PROJECT_LASTMODIFIED);
                    // rename the column TEMP_PROJECT_UUID to PROJECT_LASTMODIFIED
                    renameColumn(tablename, COLUMN_TEMP_PROJECT_UUID, COLUMN_PROJECT_LASTMODIFIED);


                } else {
                    // Drop only the primary key
                    dropPrimaryKeyAndIndex(tablename, true, null);
                    // drop the columns
                    dropColumn(tablename, COLUMN_PROJECT_ID);

                    // rename the column TEMP_PROJECT_UUID to PROJECT_ID
                    renameColumn(tablename, COLUMN_TEMP_PROJECT_UUID, COLUMN_PROJECT_ID);

                    // add the new primary key
                    if (tablename.equals("CMS_PROJECTRESOURCES")) {
                        addPrimaryKey(tablename, COLUMN_PROJECT_ID_RESOURCE_PATH);
                    }
                    if (tablename.equals("CMS_PROJECTS")) {
                        addPrimaryKey(tablename, COLUMN_PROJECT_ID);
                    }
                }
            } else {
                System.out.println("table " + tablename + " does not need to be updated");
            }
        }
    }


    /**
     * Adds a new primary key to the given table.<p>
     * 
     * @param tablename the table to add the primary key to
     * @param primaryKey the new primary key
     * 
     * @throws SQLException if something goes wrong
     */
    private void addPrimaryKey(String tablename, String primaryKey) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (m_dbcon.hasTableOrColumn(tablename, null)) {
            String query = (String)m_queryProperties.get(QUERY_ADD_PRIMARY_KEY);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_PRIMARY_KEY, primaryKey);
            m_dbcon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("table " + tablename + " does not exists");
        }
    }

    /**
     * Adds the new column for the uuids to a table.<p>
     * 
     * @param tablename the table to add the column to
     * @param column the new colum to add
     * 
     * @throws SQLException if something goes wrong
     * 
     */
    private void addUUIDColumnToTable(String tablename, String column) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!m_dbcon.hasTableOrColumn(tablename, column)) {
            String query = (String)m_queryProperties.get(QUERY_ADD_TEMP_UUID_COLUMN); // Get the query
            // if the table is not one of the ONLINE or OFFLINE resources add the new column in the first position
            if (!RESOURCES_TABLES_LIST.contains(tablename)) {
                query += " FIRST";
            }
            Map replacer = new HashMap(); // Build the replacements
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            m_dbcon.updateSqlStatement(query, replacer, null); // execute the query
        } else {
            System.out.println("column " + column + " in table " + tablename + " already exists");
        }
    }

    /**
     * Drops the column of the given table.<p>
     * 
     * @param tablename the table in which the columns shall be dropped
     * @param column the column to drop
     * 
     * @throws SQLException if something goes wrong 
     */
    private void dropColumn(String tablename, String column) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (m_dbcon.hasTableOrColumn(tablename, column)) {
            String query = (String)m_queryProperties.get(QUERY_DROP_COLUMN);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            m_dbcon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("column " + column + " in table " + tablename + " does not exist");
        }

    }

    /**
     * Drops the primary key and the given index of the given table.<p> 
     *
     * @param tablename the table to alter
     * @param primaryKey boolean value to determine if the primary key shall also be dropped
     * @param index the index to drop
     * 
     * @throws SQLException if something goes wrong
     *
     */
    private void dropPrimaryKeyAndIndex(String tablename, boolean primaryKey, String index) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (m_dbcon.hasTableOrColumn(tablename, null)) {
            // Drop the primary key
            Map replacer = new HashMap();
            if (primaryKey) {
                String dropPrimaryKey = (String)m_queryProperties.get(QUERY_DROP_PRIMARY_KEY);
                replacer.put(REPLACEMENT_TABLENAME, tablename);
                m_dbcon.updateSqlStatement(dropPrimaryKey, replacer, null);
                replacer.clear();
            }

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(index)) {
                String dropIndex = (String)m_queryProperties.get(QUERY_DROP_INDEX);
                replacer.put(REPLACEMENT_TABLENAME, tablename);
                replacer.put(REPLACEMENT_INDEX, index);
                m_dbcon.updateSqlStatement(dropIndex, replacer, null);
            }
        } else {
            System.out.println("table " + tablename + " does not exist");
        }
    }

    /**
     * Updates the given table with the new UUID value.<p>
     * 
     * @param tablename the table to update
     * @param column the column to update
     * @param newvalue the new value to insert
     * @param oldid the old id to compare the old value to
     * @param tempValue the old value in the temporary table
     * 
     * @throws SQLException if something goes wrong
     */
    private void fillUUIDSColumn(String tablename, String column, String newvalue, String oldid, String tempValue)
    throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (m_dbcon.hasTableOrColumn(tablename, column)) {
            String query = (String)m_queryProperties.get(QUERY_TRANSFER_UUID);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            replacer.put(REPLACEMENT_OLDID, oldid);
            List params = new ArrayList();
            params.add(newvalue);
            params.add(new Integer(tempValue)); // Change type to integer

            m_dbcon.updateSqlStatement(query, replacer, params);
        } else {
            System.out.println("column " + column + " in table " + tablename + " does not exists");
        }
    }

    /**
     * Gets the UUIDs from the temporary table TEMP_CMS_UUIDS.<p> 
     * 
     * @return a map with the old project ids and the new uuids
     * 
     * @throws SQLException if something goes wrong 
     */
    private Map getUUIDs() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        Map result = new HashMap();

        String query = (String)m_queryProperties.get(QUERY_GET_UUIDS);
        ResultSet set = m_dbcon.executeSqlStatement(query, null);
        while (set.next()) {
            String key = Integer.toString(set.getInt(COLUMN_PROJECT_ID));
            String value = set.getString(COLUMN_PROJECT_UUID);

            result.put(key, value);
        }
        return result;
    }

    /**
     * Checks if the given table needs an update of the uuids.<p>
     * 
     * @param tablename the table to check
     * 
     * @return true if the project ids are not yet updated, false if nothing needs to be done
     * 
     * @throws SQLException if something goes wrong 
     */
    private boolean needsUpdating(String tablename) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        boolean result = true;

        String query = (String)m_queryProperties.get(QUERY_DESCRIBE_TABLE);
        Map replacer = new HashMap();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        ResultSet set = m_dbcon.executeSqlStatement(query, replacer);

        while (set.next()) {
            String fieldname = set.getString("Field");
            if (fieldname.equals(COLUMN_PROJECT_ID) || fieldname.equals(COLUMN_PROJECT_LASTMODIFIED)) {
                try {
                    String fieldtype = set.getString("Type");
                    // If the type is varchar then no update needs to be done.
                    if (fieldtype.indexOf("varchar") > 0) {
                        return false;
                    }
                } catch (SQLException e) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Renames the column of the given table the new name.<p>
     * 
     * @param tablename the table in which the column shall be renamed
     * @param oldname the old name of the column
     * @param newname the new name of the column
     * 
     * @throws SQLException if something goes wrong
     */
    private void renameColumn(String tablename, String oldname, String newname) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (m_dbcon.hasTableOrColumn(tablename, oldname)) {
            String query = (String)m_queryProperties.get(QUERY_RENAME_COLUMN);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, oldname);
            replacer.put(REPLACEMENT_NEW_COLUMN, newname);

            m_dbcon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("column " + oldname + " in table " + tablename + " not found exists");
        }
    }
}