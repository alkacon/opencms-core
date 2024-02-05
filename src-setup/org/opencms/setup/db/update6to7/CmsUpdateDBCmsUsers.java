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

package org.opencms.setup.db.update6to7;

import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.db.A_CmsUpdateDBPart;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class makes an update of the CMS_USERS table splitting it up into CMS_USERS and CMS_USERDATA.<p>
 * Unnecessary colums from CMS_USERS will be deleted and the new column USER_DATECREATED is added.
 *
 * @since 7.0.0
 */
public class CmsUpdateDBCmsUsers extends A_CmsUpdateDBPart {

    /** Constant for the query to create the user data table.<p> */
    protected static final String QUERY_CREATE_TABLE_USERDATA = "Q_CREATE_TABLE_USERDATA";

    /** Constant for the query to insert the new user data into the new table CMS_USERDATA.<p> */
    protected static final String QUERY_INSERT_CMS_USERDATA = "Q_INSERT_CMS_USERDATA";

    /** Constant for the table CMS_USERDATA.<p> */
    private static final String CHECK_CMS_USERDATA = "CMS_USERDATA";

    /** Constant for the table name of CMS_USERS.<p> */
    private static final String CMS_USERS_TABLE = "CMS_USERS";

    /** Constant for the sql query to add the USER_DATECREATED column to CMS_USERS.<p> */
    private static final String QUERY_ADD_USER_DATECREATED_COLUMN = "Q_ADD_USER_DATECREATED";

    /** Constant for the sql query to add all webusers to the group with the given id.<p> */
    private static final String QUERY_ADD_WEBUSERS_TO_GROUP = "Q_ADD_WEBUSERS_TO_GROUP";

    /** Constant for the sql query to create a new group in the CMS_GROUPS table for the webusers.<p> */
    private static final String QUERY_CREATE_WEBUSERS_GROUP = "Q_CREATE_WEBUSERS_GROUP";

    /** Constant for the sql query to drop the USER_ADDRESS column from CMS_USERS.<p> */
    private static final String QUERY_DROP_USER_ADDRESS_COLUMN = "Q_DROP_USER_ADDRESS_COLUMN";

    /** Constant for the sql query to drop the USER_DESCRIPTION column from CMS_USERS.<p> */
    private static final String QUERY_DROP_USER_DESCRIPTION_COLUMN = "Q_DROP_USER_DESCRIPTION_COLUMN";

    /** Constant for the sql query to drop the USER_INFO column from CMS_USERS.<p> */
    private static final String QUERY_DROP_USER_INFO_COLUMN = "Q_DROP_USER_INFO_COLUMN";

    /** Constant for the sql query to drop the USER_TYPE column from CMS_USERS.<p> */
    private static final String QUERY_DROP_USER_TYPE_COLUMN = "Q_DROP_USER_TYPE_COLUMN";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_users_queries.properties";

    /** Constant for the query to the select the user infos for a user.<p> */
    private static final String QUERY_SELECT_USER_DATA = "Q_SELECT_USER_DATA";

    /** Constant for the sql query to set the USER_DATECREATED value.<p> */
    private static final String QUERY_SET_USER_DATECREATED = "Q_SET_USER_DATECREATED";

    /** Constant for the columnname USER_ID of the resultset.<p> */
    private static final String RESULTSET_USER_ID = "USER_ID";

    /** Constant for the columnname USER_INFO of the resultset.<p> */
    private static final String RESULTSET_USER_INFO = "USER_INFO";

    /** Constant for the columnname USER_ADDRESS of the resultset.<p> */
    private static final String USER_ADDRESS = "USER_ADDRESS";

    /** Constant for the columnname USER_DATECREATED.<p> */
    private static final String USER_DATECREATED = "USER_DATECREATED";

    /** Constant for the columnname USER_DESCRIPTION of the resultset.<p> */
    private static final String USER_DESCRIPTION = "USER_DESCRIPTION";

    /** Constant for the columnname USER_INFO.<p> */
    private static final String USER_INFO = "USER_INFO";

    /** Constant for the columnname USER_TYPE.<p> */
    private static final String USER_TYPE = "USER_TYPE";

    /**
     * Default constructor.<p>
     *
     * @throws IOException if the default sql queries property file could not be read
     */
    public CmsUpdateDBCmsUsers()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        try {
            if (dbCon.hasTableOrColumn(CMS_USERS_TABLE, USER_TYPE)) {
                CmsUUID id = createWebusersGroup(dbCon);
                addWebusersToGroup(dbCon, id);
            } else {
                System.out.println("table " + CHECK_CMS_USERDATA + " already exists");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            // Check if the CMS_USERDATA table exists
            if (!checkUserDataTable(dbCon)) {
                createUserDataTable(dbCon); // Could throw Exception during table creation

                String query = readQuery(QUERY_SELECT_USER_DATA);
                CmsSetupDBWrapper db = null;
                try {
                    db = dbCon.executeSqlStatement(query, null);
                    while (db.getResultSet().next()) {
                        String userID = (String)db.getResultSet().getObject(RESULTSET_USER_ID);
                        System.out.println("UserId: " + userID);

                        try {
                            Blob blob = db.getResultSet().getBlob(RESULTSET_USER_INFO);

                            ByteArrayInputStream bin = new ByteArrayInputStream(blob.getBytes(1, (int)blob.length()));
                            ObjectInputStream oin = new ObjectInputStream(bin);

                            Map<String, Object> infos = CmsCollectionsGenericWrapper.map(oin.readObject());

                            if (infos == null) {
                                infos = new HashMap<String, Object>();
                            }

                            // Add user address and user description of the current user
                            String userAddress = (String)db.getResultSet().getObject(USER_ADDRESS);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userAddress)) {
                                infos.put(USER_ADDRESS, userAddress);
                            }
                            String userDescription = (String)db.getResultSet().getObject(USER_DESCRIPTION);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userDescription)) {
                                infos.put(USER_DESCRIPTION, userDescription);
                            }

                            // Write the user data to the table
                            writeAdditionalUserInfo(dbCon, userID, infos);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    if (db != null) {
                        db.close();
                    }
                }

                // add the column USER_DATECREATED
                addUserDateCreated(dbCon);

                // remove the unnecessary columns from CMS_USERS
                removeUnnecessaryColumns(dbCon);

            } else {
                System.out.println("table " + CHECK_CMS_USERDATA + " already exists");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the new column USER_DATECREATED to the CMS_USERS table.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    protected void addUserDateCreated(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Add the column to the table if necessary
        if (!dbCon.hasTableOrColumn(CMS_USERS_TABLE, USER_DATECREATED)) {
            String addUserDateCreated = readQuery(QUERY_ADD_USER_DATECREATED_COLUMN);
            dbCon.updateSqlStatement(addUserDateCreated, null, null);

            String setUserDateCreated = readQuery(QUERY_SET_USER_DATECREATED);
            List<Object> param = new ArrayList<Object>();
            // Set the creation date to the current time
            param.add(Long.valueOf(System.currentTimeMillis()));

            dbCon.updateSqlStatement(setUserDateCreated, null, param);
        } else {
            System.out.println("column " + USER_DATECREATED + " in table " + CMS_USERS_TABLE + " already exists");
        }
    }

    /**
     * Adds all webusers to the new previously created webusers group.<p>
     *
     * @param dbCon the db connection interface
     * @param id the id of the new webusers group
     *
     * @throws SQLException if something goes wrong
     */
    protected void addWebusersToGroup(CmsSetupDb dbCon, CmsUUID id) throws SQLException {

        String sql = readQuery(QUERY_ADD_WEBUSERS_TO_GROUP);
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("${GROUP_ID}", id.toString());
        dbCon.updateSqlStatement(sql, replacements, null);
    }

    /**
     * Checks if the CMS_USERDATA table exists.<p>
     *
     * @param dbCon the db connection interface
     *
     * @return true if it exists, false if not.
     */
    protected boolean checkUserDataTable(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        return dbCon.hasTableOrColumn(CHECK_CMS_USERDATA, null);
    }

    /**
     * Creates the CMS_USERDATA table if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if soemthing goes wrong
     */
    protected void createUserDataTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String createStatement = readQuery(QUERY_CREATE_TABLE_USERDATA);
        dbCon.updateSqlStatement(createStatement, null, null);
    }

    /**
     * creates a new group for the webusers.<p>
     *
     * @param dbCon the db connection interface
     *
     * @return the id of the new generated group
     *
     * @throws SQLException if something goes wrong
     */
    protected CmsUUID createWebusersGroup(CmsSetupDb dbCon) throws SQLException {

        String sql = readQuery(QUERY_CREATE_WEBUSERS_GROUP);
        List<Object> params = new ArrayList<Object>();
        CmsUUID id = new CmsUUID();
        params.add(id.toString());
        params.add(CmsUUID.getNullUUID().toString());
        params.add("allWebusersFromUpgrade6to7");
        params.add(
            "This group was created by the OpenCms Upgrade Wizard to facilitate the handling of former called WebUsers, can be deleted if needed.");
        params.add(Integer.valueOf(0));
        params.add("/");
        dbCon.updateSqlStatement(sql, null, params);
        return id;
    }

    /**
     * Removes the columns USER_INFO, USER_ADDRESS, USER_DESCRIPTION and USER_TYPE from the CMS_USERS table.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    protected void removeUnnecessaryColumns(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Get the sql queries to drop the columns
        String dropUserInfo = readQuery(QUERY_DROP_USER_INFO_COLUMN);
        String dropUserAddress = readQuery(QUERY_DROP_USER_ADDRESS_COLUMN);
        String dropUserDescription = readQuery(QUERY_DROP_USER_DESCRIPTION_COLUMN);
        String dropUserType = readQuery(QUERY_DROP_USER_TYPE_COLUMN);

        // execute the queries to drop the columns, if they exist
        if (dbCon.hasTableOrColumn(CMS_USERS_TABLE, USER_INFO)) {
            dbCon.updateSqlStatement(dropUserInfo, null, null);
        } else {
            System.out.println("no column " + USER_INFO + " in table " + CMS_USERS_TABLE + " found");
        }
        if (dbCon.hasTableOrColumn(CMS_USERS_TABLE, USER_ADDRESS)) {
            dbCon.updateSqlStatement(dropUserAddress, null, null);
        } else {
            System.out.println("no column " + USER_ADDRESS + " in table " + CMS_USERS_TABLE + " found");
        }
        if (dbCon.hasTableOrColumn(CMS_USERS_TABLE, USER_DESCRIPTION)) {
            dbCon.updateSqlStatement(dropUserDescription, null, null);
        } else {
            System.out.println("no column " + USER_DESCRIPTION + " in table " + CMS_USERS_TABLE + " found");
        }
        if (dbCon.hasTableOrColumn(CMS_USERS_TABLE, USER_TYPE)) {
            dbCon.updateSqlStatement(dropUserType, null, null);
        } else {
            System.out.println("no column " + USER_TYPE + " in table " + CMS_USERS_TABLE + " found");
        }
    }

    /**
     * Writes the additional user infos to the database.<p>
     *
     * @param dbCon the db connection interface
     * @param id the user id
     * @param additionalInfo the additional info of the user
     */
    protected void writeAdditionalUserInfo(CmsSetupDb dbCon, String id, Map<String, Object> additionalInfo) {

        Iterator<Map.Entry<String, Object>> entries = additionalInfo.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            if ((entry.getKey() != null) && (entry.getValue() != null)) {
                // Write the additional user information to the database
                writeUserInfo(dbCon, id, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Writes one set of additional user info (key and its value) to the CMS_USERDATA table.<p>
     *
     * @param dbCon the db connection interface
     * @param id the user id
     * @param key the data key
     * @param value the data value
     */
    protected void writeUserInfo(CmsSetupDb dbCon, String id, String key, Object value) {

        String query = readQuery(QUERY_INSERT_CMS_USERDATA);

        try {
            // Generate the list of parameters to add into the user info table
            List<Object> params = new ArrayList<Object>();
            params.add(id);
            params.add(key);
            params.add(value);
            params.add(value.getClass().getName());

            dbCon.updateSqlStatement(query, null, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}