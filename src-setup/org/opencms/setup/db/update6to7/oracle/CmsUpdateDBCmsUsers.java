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

package org.opencms.setup.db.update6to7.oracle;

import org.opencms.db.oracle.CmsUserDriver;
import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsDataTypeUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Oracle implementation of the generic update class for the Users.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBCmsUsers extends org.opencms.setup.db.update6to7.CmsUpdateDBCmsUsers {

    /** Constant for the query to insert the new user data into the new table CMS_USERDATA.<p> */
    private static final String QUERY_ORACLE_USERDATA_UPDATE = "Q_ORACLE_USERDATA_UPDATE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_users_queries.properties";

    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";

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
     * @see org.opencms.setup.db.update6to7.CmsUpdateDBCmsUsers#writeUserInfo(org.opencms.setup.CmsSetupDb, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    protected void writeUserInfo(CmsSetupDb dbCon, String id, String key, Object value) {

        String query = readQuery(QUERY_INSERT_CMS_USERDATA);

        try {
            // Generate the list of parameters to add into the user info table
            List<Object> params = new ArrayList<Object>();
            params.add(id);
            params.add(key);
            params.add(value.getClass().getName());

            dbCon.updateSqlStatement(query, null, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // update user_info in this special way because of using blob

        boolean wasInTransaction = false;

        CmsSetupDBWrapper db = null;

        try {

            wasInTransaction = !dbCon.getConnection().getAutoCommit();
            if (!wasInTransaction) {
                dbCon.getConnection().setAutoCommit(false);
            }

            String stmt = readQuery(QUERY_ORACLE_USERDATA_UPDATE);

            // Generate the list of parameters to add into the user info table
            List<Object> params = new ArrayList<Object>();
            params.add(id);
            params.add(key);

            db = dbCon.executeSqlStatement(stmt, null, params);
            if (db.getResultSet().next()) {

                // write serialized user info
                OutputStream output = CmsUserDriver.getOutputStreamFromBlob(db.getResultSet(), "DATA_VALUE");
                output.write(CmsDataTypeUtil.dataSerialize(value));
                output.close();

            } else {
                System.out.println("Could not insert blob");
            }

            if (!wasInTransaction) {
                CmsSetupDBWrapper db2 = null;
                try {
                    String commit = readQuery("Q_COMMIT");
                    db2 = dbCon.executeSqlStatement(commit, null);
                } finally {
                    if (db2 != null) {
                        db2.close();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // close result set
            if (db != null) {
                db.close();
            }

            // rollback
            CmsSetupDBWrapper db2 = null;
            try {
                if (!wasInTransaction) {
                    String rollback = readQuery("Q_ROLLBACK");
                    db2 = dbCon.executeSqlStatement(rollback, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db2 != null) {
                    db2.close();
                }
            }

            // set auto commit back to original value
            try {
                if (!wasInTransaction) {
                    dbCon.getConnection().setAutoCommit(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
