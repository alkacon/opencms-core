/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBCmsUsers.java,v $
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * This class makes an update of the CMS_USERS table splitting it up into CMS_USERS and CMS_USERDATA.<p>
 * Unnecessary colums from CMS_USERS will be deleted and the new column USER_DATECREATED is added.
 * 
 * @author metzler
 */
public class CmsUpdateDBCmsUsers {

    /** Constant for the table CMS_USERDATA.<p> */
    private static final String CHECK_CMS_USERDATA = "CMS_USERDATA";

    /** Constant for the table name of CMS_USERS.<p> */
    private static final String CMS_USERS_TABLE = "CMS_USERS";

    /** Constant for the sql query to add the USER_DATECREATED column to CMS_USERS.<p> */
    private static final String QUERY_ADD_USER_DATECREATED_COLUMN = "Q_ADD_USER_DATECREATED";

    /** Constant for the sql query to add all webusers to the group with the given id.<p> */
    private static final String QUERY_ADD_WEBUSERS_TO_GROUP = "Q_ADD_WEBUSERS_TO_GROUP";

    /** Constant for the query to create the user data table.<p> */
    private static final String QUERY_CREATE_TABLE_USERDATA = "Q_CREATE_TABLE_USERDATA";

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

    /** Constant for the query to insert the new user data into the new table CMS_USERDATA.<p> */
    private static final String QUERY_INSERT_CMS_USERDATA = "Q_INSERT_CMS_USERDATA";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_users_queries.properties";

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
     * @throws IOException if the sql query props could not be read
     */
    public CmsUpdateDBCmsUsers(CmsSetupDb dbcon, String rfsPath)
    throws IOException {

        System.out.println(getClass().getName());
        m_dbcon = dbcon;
        m_queryProperties = CmsPropertyUtils.loadProperties(rfsPath + QUERY_PROPERTY_FILE);
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
     * Executes the necessary steps to update the CMS_USERS table to the new version.<p> 
     * 
     * The CMS_USERS table is split up into the CMS_USERS and the CMS_USERDATA table.
     * The unnecessary columns from CMS_USERS are removed after the transfer of the
     * data to the CMS_USERDATA table.
     */
    public void updateCmsUsers() {

        System.out.println(new Exception().getStackTrace()[0].toString());
        try {
            if (m_dbcon.hasTableOrColumn(CMS_USERS_TABLE, USER_TYPE)) {
                CmsUUID id = createWebusersGroup();
                addWebusersToGroup(id);
            } else {
                System.out.println("table " + CHECK_CMS_USERDATA + " already exists");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            // Check if the CMS_USERDATA table exists            
            if (!checkUserDataTable()) {
                createUserDataTable(); // Could throw Exception during table creation

                String query = (String)m_queryProperties.get(QUERY_SELECT_USER_DATA);
                ResultSet set = m_dbcon.executeSqlStatement(query, null);
                while (set.next()) {
                    String userID = (String)set.getObject(RESULTSET_USER_ID);
                    System.out.println("UserId: " + userID);

                    try {
                        Blob blob = set.getBlob(RESULTSET_USER_INFO);

                        ByteArrayInputStream bin = new ByteArrayInputStream(blob.getBytes(1, (int)blob.length()));
                        ObjectInputStream oin = new ObjectInputStream(bin);

                        Map infos = (Map)oin.readObject();

                        if (infos == null) {
                            infos = new HashMap();
                        }

                        // Add user address and user description of the current user
                        String userAddress = (String)set.getObject(USER_ADDRESS);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userAddress)) {
                            infos.put(USER_ADDRESS, userAddress);
                        }
                        String userDescription = (String)set.getObject(USER_DESCRIPTION);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userDescription)) {
                            infos.put(USER_DESCRIPTION, userDescription);
                        }

                        // Write the user data to the table
                        writeAdditionalUserInfo(userID, infos);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                // add the column USER_DATECREATED
                addUserDateCreated();

                // remove the unnecessary columns from CMS_USERS
                removeUnnecessaryColumns();

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
     * @throws SQLException if something goes wrong 
     */
    private void addUserDateCreated() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Add the column to the table if necessary
        if (!m_dbcon.hasTableOrColumn(CMS_USERS_TABLE, USER_DATECREATED)) {
            String addUserDateCreated = (String)m_queryProperties.get(QUERY_ADD_USER_DATECREATED_COLUMN);
            m_dbcon.updateSqlStatement(addUserDateCreated, null, null);

            String setUserDateCreated = (String)m_queryProperties.get(QUERY_SET_USER_DATECREATED);
            List param = new ArrayList();
            // Set the creation date to the current time
            param.add(new Long(System.currentTimeMillis()));

            m_dbcon.updateSqlStatement(setUserDateCreated, null, param);
        } else {
            System.out.println("column " + USER_DATECREATED + " in table " + CMS_USERS_TABLE + " already exists");
        }
    }

    /**
     * Adds all webusers to the new previously created webusers group.<p>
     * 
     * @param id the id of the new webusers group
     * 
     * @throws SQLException if something goes wrong 
     */
    private void addWebusersToGroup(CmsUUID id) throws SQLException {

        String sql = (String)m_queryProperties.get(QUERY_ADD_WEBUSERS_TO_GROUP);
        Map replacements = new HashMap();
        replacements.put("${GROUP_ID}", id.toString());
        m_dbcon.updateSqlStatement(sql, replacements, null);
    }

    /**
     * Checks if the CMS_USERDATA table exists.<p> 
     * 
     * @return true if it exists, false if not.
     */
    private boolean checkUserDataTable() {

        System.out.println(new Exception().getStackTrace()[0].toString());
        return m_dbcon.hasTableOrColumn(CHECK_CMS_USERDATA, null);
    }

    /**
     * Creates the CMS_USERDATA table if it does not exist yet.<p> 
     * 
     * @throws SQLException 
     *
     */
    private void createUserDataTable() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String createStatement = m_queryProperties.getString(QUERY_CREATE_TABLE_USERDATA);
        m_dbcon.updateSqlStatement(createStatement, null, null);
    }

    /**
     * creates a new group for the webusers.<p>
     * 
     * @return the id of the new generated group
     * 
     * @throws SQLException if something goes wrong 
     */
    private CmsUUID createWebusersGroup() throws SQLException {

        String sql = (String)m_queryProperties.get(QUERY_CREATE_WEBUSERS_GROUP);
        List params = new ArrayList();
        CmsUUID id = new CmsUUID();
        params.add(id.toString());
        params.add(CmsUUID.getNullUUID().toString());
        params.add("allWebusersFromUpgrade6to7");
        params.add("This group was created by the OpenCms Upgrade Wizard to facilitate the handling of former called WebUsers, can be deleted if needed.");
        params.add(new Integer(0));
        params.add("/");
        m_dbcon.updateSqlStatement(sql, null, params);
        return id;
    }

    /**
     * Removes the columns USER_INFO, USER_ADDRESS, USER_DESCRIPTION and USER_TYPE from the CMS_USERS table.<p>
     * 
     * @throws SQLException if something goes wrong 
     *
     */
    private void removeUnnecessaryColumns() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Get the sql queries to drop the columns
        String dropUserInfo = (String)m_queryProperties.get(QUERY_DROP_USER_INFO_COLUMN);
        String dropUserAddress = (String)m_queryProperties.get(QUERY_DROP_USER_ADDRESS_COLUMN);
        String dropUserDescription = (String)m_queryProperties.get(QUERY_DROP_USER_DESCRIPTION_COLUMN);
        String dropUserType = (String)m_queryProperties.get(QUERY_DROP_USER_TYPE_COLUMN);

        // execute the queries to drop the columns, if they exist
        if (m_dbcon.hasTableOrColumn(CMS_USERS_TABLE, USER_INFO)) {
            m_dbcon.updateSqlStatement(dropUserInfo, null, null);
        } else {
            System.out.println("no column " + USER_INFO + " in table " + CMS_USERS_TABLE + " found");
        }
        if (m_dbcon.hasTableOrColumn(CMS_USERS_TABLE, USER_ADDRESS)) {
            m_dbcon.updateSqlStatement(dropUserAddress, null, null);
        } else {
            System.out.println("no column " + USER_ADDRESS + " in table " + CMS_USERS_TABLE + " found");
        }
        if (m_dbcon.hasTableOrColumn(CMS_USERS_TABLE, USER_DESCRIPTION)) {
            m_dbcon.updateSqlStatement(dropUserDescription, null, null);
        } else {
            System.out.println("no column " + USER_DESCRIPTION + " in table " + CMS_USERS_TABLE + " found");
        }
        if (m_dbcon.hasTableOrColumn(CMS_USERS_TABLE, USER_TYPE)) {
            m_dbcon.updateSqlStatement(dropUserType, null, null);
        } else {
            System.out.println("no column " + USER_TYPE + " in table " + CMS_USERS_TABLE + " found");
        }
    }

    /**
     * Writes the additional user infos to the database.<p>
     * 
     * @param id
     * @param additionalInfo
     */
    private void writeAdditionalUserInfo(String id, Map additionalInfo) {

        Iterator entries = additionalInfo.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry)entries.next();
            if (entry.getKey() != null && entry.getValue() != null) {
                // Write the additional user information to the database
                writeUserInfo(id, (String)entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Writes one set of additional user info (key and its value) to the CMS_USERDATA table.<p>
     * 
     * @param id the user id 
     * @param key the data key
     * @param value the data value
     */
    private void writeUserInfo(String id, String key, Object value) {

        String query = m_queryProperties.getString(QUERY_INSERT_CMS_USERDATA);

        try {
            // Generate the list of parameters to add into the user info table
            List params = new ArrayList();
            params.add(id);
            params.add(key);
            params.add(value);
            params.add(value.getClass().getName());

            m_dbcon.updateSqlStatement(query, null, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}