/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/generic/Attic/CmsUpdateDBProjectId.java,v $
 * Date   : $Date: 2007/05/29 14:52:12 $
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

package org.opencms.setup.update6to7.generic;

import org.opencms.file.CmsProject;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.update6to7.A_CmsUpdateDBPart;
import org.opencms.util.CmsUUID;

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
public class CmsUpdateDBProjectId extends A_CmsUpdateDBPart {

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

    /** Constant for the sql query to get the project ids.<p> */
    private static final String QUERY_GET_PROJECT_IDS = "Q_SELECT_PROJECT_IDS";

    /** Constant for the sql query to get the uuids and project ids.<p> */
    private static final String QUERY_GET_UUIDS = "Q_SELECT_UUIDS";

    /** Constant for the sql query to insert the data into the CMS_HISTORY_PROJECTS table.<p> */
    private static final String QUERY_INSERT_CMS_HISTORY_TABLE = "Q_INSERT_CMS_HISTORY_TABLE";

    /** Constant for the sql query to insert a pair of values to the temp table.<p> */
    private static final String QUERY_INSERT_UUIDS = "Q_INSERT_UUIDS_TEMP_TABLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_projectid_queries.properties";

    private static final String QUERY_READ_ADMIN_GROUP = "Q_READ_ADMIN_GROUP";

    private static final String QUERY_READ_ADMIN_USER = "Q_READ_ADMIN_USER";

    /** Constant for the sql query to add a rename a column in the table.<p> */
    private static final String QUERY_RENAME_COLUMN = "Q_RENAME_COLUMN";

    /** Constant for the sql query to count the hsitorical projects.<p> */
    private static final String QUERY_SELECT_COUNT_HISTORY_TABLE = "Q_SELECT_COUNT_HISTORY_TABLE";

    /** Constant for the sql query to select the data from the CMS_BACKUP_PROJECTS table.<p> */
    private static final String QUERY_SELECT_DATA_FROM_BACKUP_PROJECTS = "Q_SELECT_DATA_FROM_BACKUP_PROJECTS";

    /** Constant for the sql query to transfer the new uuids to the temporary column.<p> */
    private static final String QUERY_TRANSFER_UUID = "Q_TRANSFER_UUID";

    /** Constant for the sql query to repair lost project ids.<p> */
    private static final String QUERY_UPDATE_NULL_PROJECTID = "Q_UPDATE_NULL_PROJECTID";

    /** Constant for the replacement in the SQL query for the columnname.<p> */
    private static final String REPLACEMENT_COLUMN = "${column}";

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

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBProjectId()
    throws IOException {

        super();
    }

    /**
     * @see org.opencms.setup.update6to7.I_CmsUpdateDBPart#getSqlQueriesFile()
     */
    public String getSqlQueriesFile() {

        return QUERY_PROPERTY_FILE;
    }

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        generateUUIDs(dbCon);

        // Check for the CMS_HISTORY_PROJECTS table and transfer the data to it
        if (!dbCon.hasTableOrColumn(HISTORY_PROJECTS_TABLE, null)) {
            createNewTable(dbCon, QUERY_CREATE_HISTORY_PROJECTS_TABLE);
            transferDataToHistoryTable(dbCon);
        } else {
            System.out.println("table " + HISTORY_PROJECTS_TABLE + " already exists");
        }

        Map uuids = getUUIDs(dbCon); // Get the UUIDS

        /*
         * Add the temporary column for the new UUIDs and fill it with data
         */
        for (Iterator it = TABLES_LIST.iterator(); it.hasNext();) {
            String tablename = (String)it.next();

            if (needsUpdating(dbCon, tablename)) {
                addUUIDColumnToTable(dbCon, tablename, TEMP_UUID_COLUMN);
                boolean isInResourcesList = RESOURCES_TABLES_LIST.contains(tablename);
                // Add the new uuids
                Iterator entries = uuids.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry)entries.next();
                    if (entry.getKey() != null && entry.getValue() != null) {
                        if (isInResourcesList) {
                            fillUUIDSColumn(
                                dbCon,
                                tablename,
                                TEMP_UUID_COLUMN,
                                (String)entry.getValue(),
                                COLUMN_PROJECT_LASTMODIFIED,
                                (String)entry.getKey());
                        } else {
                            fillUUIDSColumn(
                                dbCon,
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
                    // fix lost project ids
                    Map replacer = Collections.singletonMap("${tablename}", tablename);
                    List params = Collections.singletonList(CmsUUID.getNullUUID().toString());
                    String query = readQuery(QUERY_UPDATE_NULL_PROJECTID);
                    dbCon.updateSqlStatement(query, replacer, params);

                    // Drop the column PROJECT_LASTMODIFIED
                    dropColumn(dbCon, tablename, COLUMN_PROJECT_LASTMODIFIED);
                    // rename the column TEMP_PROJECT_UUID to PROJECT_LASTMODIFIED
                    renameColumn(dbCon, tablename, COLUMN_TEMP_PROJECT_UUID, COLUMN_PROJECT_LASTMODIFIED);
                } else {
                    // drop the columns
                    dropColumn(dbCon, tablename, COLUMN_PROJECT_ID);

                    // rename the column TEMP_PROJECT_UUID to PROJECT_ID
                    renameColumn(dbCon, tablename, COLUMN_TEMP_PROJECT_UUID, COLUMN_PROJECT_ID);

                    // add the new primary key
                    if (tablename.equals("CMS_PROJECTRESOURCES")) {
                        addPrimaryKey(dbCon, tablename, COLUMN_PROJECT_ID_RESOURCE_PATH);
                    }
                    if (tablename.equals("CMS_PROJECTS")) {
                        addPrimaryKey(dbCon, tablename, COLUMN_PROJECT_ID);
                    }
                }
            } else {
                System.out.println("table " + tablename + " does not need to be updated");
            }
        }

        ResultSet set = dbCon.executeSqlStatement(readQuery(QUERY_SELECT_COUNT_HISTORY_TABLE), null);
        boolean update = false;
        if (set.next()) {
            if (set.getInt("COUNT") <= 0) {
                update = true;
            }
        }
        set.close();
        if (update) {
            System.out.println("table " + HISTORY_PROJECTS_TABLE + " has no content, create a dummy entry");

            CmsUUID userId = CmsUUID.getNullUUID();
            set = dbCon.executeSqlStatement(readQuery(QUERY_READ_ADMIN_USER), null);
            if (set.next()) {
                userId = new CmsUUID(set.getString(1));
            }
            CmsUUID groupId = CmsUUID.getNullUUID();
            set = dbCon.executeSqlStatement(readQuery(QUERY_READ_ADMIN_GROUP), null);
            if (set.next()) {
                groupId = new CmsUUID(set.getString(1));
            }

            List params = new ArrayList();
            params.add(new CmsUUID().toString());
            params.add("updateWizardDummyProject");
            params.add("dummy project just for having an entry");
            params.add(new Integer(1));
            params.add(userId);
            params.add(groupId);
            params.add(groupId);
            params.add(new Long(System.currentTimeMillis()));
            params.add(new Integer(1));
            params.add(new Long(System.currentTimeMillis()));
            params.add(userId);
            params.add(CmsOrganizationalUnit.SEPARATOR);

            String query = readQuery(QUERY_INSERT_CMS_HISTORY_TABLE);
            dbCon.updateSqlStatement(query, null, params);
        } else {
            System.out.println("table " + HISTORY_PROJECTS_TABLE + " has content");
        }
    }

    /**
     * Adds a new primary key to the given table.<p>
     * 
     * @param dbCon the db connection interface
     * @param tablename the table to add the primary key to
     * @param primaryKey the new primary key
     * 
     * @throws SQLException if something goes wrong
     */
    private void addPrimaryKey(CmsSetupDb dbCon, String tablename, String primaryKey) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (dbCon.hasTableOrColumn(tablename, null)) {
            String query = readQuery(QUERY_ADD_PRIMARY_KEY);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_PRIMARY_KEY, primaryKey);
            dbCon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("table " + tablename + " does not exists");
        }
    }

    /**
     * Adds the new column for the uuids to a table.<p>
     * 
     * @param dbCon the db connection interface
     * @param tablename the table to add the column to
     * @param column the new colum to add
     * 
     * @throws SQLException if something goes wrong
     */
    private void addUUIDColumnToTable(CmsSetupDb dbCon, String tablename, String column) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(tablename, column)) {
            String query = readQuery(QUERY_ADD_TEMP_UUID_COLUMN); // Get the query
            // if the table is not one of the ONLINE or OFFLINE resources add the new column in the first position
            if (!RESOURCES_TABLES_LIST.contains(tablename)) {
                query += " FIRST";
            }
            Map replacer = new HashMap(); // Build the replacements
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            dbCon.updateSqlStatement(query, replacer, null); // execute the query
        } else {
            System.out.println("column " + column + " in table " + tablename + " already exists");
        }
    }

    /**
     * Creates the temporary table to store the project ids and uuids.<p>
     * 
     * @param dbCon the db connection interface
     * @param toCreate the constant of the table name to create
     * 
     * @throws SQLException if something goes wrong
     */
    private void createNewTable(CmsSetupDb dbCon, String toCreate) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = readQuery(toCreate);
        dbCon.updateSqlStatement(query, null, null);
    }

    /**
     * Drops the column of the given table.<p>
     * 
     * @param dbCon the db connection interface
     * @param tablename the table in which the columns shall be dropped
     * @param column the column to drop
     * 
     * @throws SQLException if something goes wrong 
     */
    private void dropColumn(CmsSetupDb dbCon, String tablename, String column) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (dbCon.hasTableOrColumn(tablename, column)) {
            String query = readQuery(QUERY_DROP_COLUMN);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            dbCon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("column " + column + " in table " + tablename + " does not exist");
        }

    }

    /**
     * Updates the given table with the new UUID value.<p>
     * 
     * @param dbCon the db connection interface
     * @param tablename the table to update
     * @param column the column to update
     * @param newvalue the new value to insert
     * @param oldid the old id to compare the old value to
     * @param tempValue the old value in the temporary table
     * 
     * @throws SQLException if something goes wrong
     */
    private void fillUUIDSColumn(
        CmsSetupDb dbCon,
        String tablename,
        String column,
        String newvalue,
        String oldid,
        String tempValue) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (dbCon.hasTableOrColumn(tablename, column)) {
            String query = readQuery(QUERY_TRANSFER_UUID);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            replacer.put(REPLACEMENT_OLDID, oldid);
            List params = new ArrayList();
            params.add(newvalue);
            params.add(new Integer(tempValue)); // Change type to integer

            dbCon.updateSqlStatement(query, replacer, params);
        } else {
            System.out.println("column " + column + " in table " + tablename + " does not exists");
        }
    }

    /**
     * Generates the new UUIDs for the project ids.<p>
     * The new uuids are stored in the temporary table.<p>
     * 
     * @param dbCon the db connection interface
     * 
     * @throws SQLException if something goes wrong 
     */
    private void generateUUIDs(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = readQuery(QUERY_GET_PROJECT_IDS);

        ResultSet set = dbCon.executeSqlStatement(query, null);
        ResultSetMetaData metaData = set.getMetaData();
        // Check the type of the column if it is integer, then create the new uuids
        int columnType = metaData.getColumnType(1);
        if (columnType == java.sql.Types.INTEGER) {
            if (!dbCon.hasTableOrColumn(TEMPORARY_TABLE_NAME, null)) {
                createNewTable(dbCon, QUERY_CREATE_TEMP_TABLE_UUIDS);

                String updateQuery = readQuery(QUERY_INSERT_UUIDS);
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
                    dbCon.updateSqlStatement(updateQuery, null, params);

                    params.clear();
                }

                // If no project id with value 0 was found 
                if (!hasNullId) {
                    params.add(new Integer(0));
                    params.add(CmsUUID.getNullUUID());
                    dbCon.updateSqlStatement(updateQuery, null, params);
                }
            } else {
                System.out.println("table " + TEMPORARY_TABLE_NAME + " already exists");
            }
        }
    }

    /**
     * Gets the UUIDs from the temporary table TEMP_CMS_UUIDS.<p>
     *  
     * @param dbCon the db connection interface
     * 
     * @return a map with the old project ids and the new uuids
     * 
     * @throws SQLException if something goes wrong 
     */
    private Map getUUIDs(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        Map result = new HashMap();

        String query = readQuery(QUERY_GET_UUIDS);
        ResultSet set = dbCon.executeSqlStatement(query, null);
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
     * @param dbCon the db connection interface
     * @param tablename the table to check
     * 
     * @return true if the project ids are not yet updated, false if nothing needs to be done
     * 
     * @throws SQLException if something goes wrong 
     */
    private boolean needsUpdating(CmsSetupDb dbCon, String tablename) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        boolean result = true;

        String query = readQuery(QUERY_DESCRIBE_TABLE);
        Map replacer = new HashMap();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        ResultSet set = dbCon.executeSqlStatement(query, replacer);

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
     * @param dbCon the db connection interface
     * @param tablename the table in which the column shall be renamed
     * @param oldname the old name of the column
     * @param newname the new name of the column
     * 
     * @throws SQLException if something goes wrong
     */
    private void renameColumn(CmsSetupDb dbCon, String tablename, String oldname, String newname) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (dbCon.hasTableOrColumn(tablename, oldname)) {
            String query = readQuery(QUERY_RENAME_COLUMN);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, oldname);
            replacer.put(REPLACEMENT_NEW_COLUMN, newname);

            dbCon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("column " + oldname + " in table " + tablename + " not found exists");
        }
    }

    /**
     * Transfers the data from the CMS_BACKUP_PROJECTS to the CMS_HISTORY_PROJECTS table.<p>
     * 
     * The datetime type for the column PROJECT_PUBLISHDATE is converted to the new long value.<p>
     * 
     * @param dbCon the db connection interface
     * 
     * @throws SQLException if something goes wrong
     */
    private void transferDataToHistoryTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Get the data from the CMS_BACKUP table
        String query = readQuery(QUERY_SELECT_DATA_FROM_BACKUP_PROJECTS);
        ResultSet set = dbCon.executeSqlStatement(query, null);

        String insertQuery = readQuery(QUERY_INSERT_CMS_HISTORY_TABLE);
        while (set.next()) {
            // Add the values to be inserted into the CMS_HISTORY_PROJECTS table
            List params = new ArrayList();
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

            dbCon.updateSqlStatement(insertQuery, null, params);
        }
    }
}