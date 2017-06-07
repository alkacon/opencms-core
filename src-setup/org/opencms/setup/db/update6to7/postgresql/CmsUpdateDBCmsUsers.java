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

package org.opencms.setup.db.update6to7.postgresql;

import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL implementation of the generic update class for the Users.<p>
 *
 * @since 7.0.2
 */
public class CmsUpdateDBCmsUsers extends org.opencms.setup.db.update6to7.CmsUpdateDBCmsUsers {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_users_queries.properties";

    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";

    /** Constant for the table CMS_USERDATA.<p> */
    private static final String CHECK_CMS_USERDATA = "CMS_USERDATA";

    /** Constant for the table name of CMS_USERS.<p> */
    private static final String CMS_USERS_TABLE = "CMS_USERS";

    /** Constant for the columnname USER_ID of the resultset.<p> */
    private static final String RESULTSET_USER_ID = "USER_ID";

    /** Constant for the columnname USER_INFO of the resultset.<p> */
    private static final String RESULTSET_USER_INFO = "USER_INFO";

    /** Constant for the columnname USER_ADDRESS of the resultset.<p> */
    private static final String USER_ADDRESS = "USER_ADDRESS";

    /** Constant for the columnname USER_DESCRIPTION of the resultset.<p> */
    private static final String USER_DESCRIPTION = "USER_DESCRIPTION";

    /** Constant for the columnname USER_TYPE.<p> */
    private static final String USER_TYPE = "USER_TYPE";

    /** Constant for the query to the select the user infos for a user.<p> */
    private static final String QUERY_SELECT_USER_DATA = "Q_SELECT_USER_DATA";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
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

                        try {
                            byte[] blob = db.getResultSet().getBytes(RESULTSET_USER_INFO);

                            ByteArrayInputStream bin = new ByteArrayInputStream(blob);
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
     * @see org.opencms.setup.db.update6to7.CmsUpdateDBCmsUsers#createUserDataTable(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void createUserDataTable(CmsSetupDb dbCon) throws SQLException {

        String indexTablespace = m_poolData.get("indexTablespace");

        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);

        String createStatement = readQuery(QUERY_CREATE_TABLE_USERDATA);
        dbCon.updateSqlStatement(createStatement, replacer, null);

        // create indices
        List<String> indexElements = new ArrayList<String>();
        indexElements.add("CMS_USERDATA_01_IDX_INDEX");
        indexElements.add("CMS_USERDATA_02_IDX_INDEX");

        Iterator<String> iter = indexElements.iterator();
        while (iter.hasNext()) {
            String stmt = readQuery(iter.next());
            try {
                // Create the index
                dbCon.updateSqlStatement(stmt, replacer, null);
            } catch (SQLException e) {
                e.printStackTrace();
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
    @Override
    protected void writeUserInfo(CmsSetupDb dbCon, String id, String key, Object value) {

        Connection conn = dbCon.getConnection();

        try {
            PreparedStatement p = conn.prepareStatement(readQuery(QUERY_INSERT_CMS_USERDATA));
            p.setString(1, id);
            p.setString(2, key);
            p.setBytes(3, CmsDataTypeUtil.dataSerialize(value));
            p.setString(4, value.getClass().getName());
            p.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
