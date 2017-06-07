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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class creates the table CMS_CONTENTS and fills it with data from the tables CMS_BACKUP_CONTENTS and CMS_ONLINE_CONTENTS.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBContentTables extends A_CmsUpdateDBPart {

    /** Constant for the sql query to create the CMS_CONTENTS table.<p> */
    protected static final String QUERY_CREATE_CMS_CONTENTS_TABLE = "Q_CREATE_CMS_CONTENTS_TABLE";

    /** Constant for the sql query to transfer the online contents.<p> */
    protected static final String QUERY_TRANSFER_ONLINE_CONTENTS = "Q_TRANSFER_ONLINE_CONTENTS";

    /** Constant for the table CMS_CONTENTS.<p> */
    protected static final String TABLE_CMS_CONTENTS = "CMS_CONTENTS";

    /** Constant for the sql query to drop a table.<p> */
    private static final String QUERY_DROP_TABLE = "Q_DROP_TABLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_content_table_queries.properties";

    /** Constant for the sql query to read the max publish tag.<p> */
    private static final String QUERY_READ_MAX_PUBTAG = "Q_READ_MAX_PUBTAG";

    /** Constant for the sql query to transfer the backup contents.<p> */
    private static final String QUERY_TRANSFER_BACKUP_CONTENTS = "Q_TRANSFER_BACKUP_CONTENTS";

    /** Constant for the replacement in the SQL query for the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the table CMS_BACKUP_CONTENTS.<p> */
    private static final String TABLE_CMS_BACKUP_CONTENTS = "CMS_BACKUP_CONTENTS";

    /** Constant for the table CMS_ONLINE_CONTENTS.<p> */
    private static final String TABLE_CMS_ONLINE_CONTENTS = "CMS_ONLINE_CONTENTS";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBContentTables()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * Creates the CMS_CONTENTS table if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    protected void createContentsTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(TABLE_CMS_CONTENTS, null)) {
            String query = readQuery(QUERY_CREATE_CMS_CONTENTS_TABLE);
            dbCon.updateSqlStatement(query, null, null);
        } else {
            System.out.println("table " + TABLE_CMS_CONTENTS + " already exists");
        }
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        createContentsTable(dbCon);

        // Transfer the online contents if the table exists
        if (dbCon.hasTableOrColumn(TABLE_CMS_ONLINE_CONTENTS, null)) {
            int pubTag = 1;
            String query = readQuery(QUERY_READ_MAX_PUBTAG);
            CmsSetupDBWrapper db = null;
            try {
                db = dbCon.executeSqlStatement(query, null);
                if (db.getResultSet().next()) {
                    pubTag = db.getResultSet().getInt(1);
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
            transferOnlineContents(dbCon, pubTag);
        } else {
            System.out.println("no table " + TABLE_CMS_ONLINE_CONTENTS + " found");
        }

        if (isKeepHistory()) {
            // Transfer the backup contents if the table exists
            if (dbCon.hasTableOrColumn(TABLE_CMS_BACKUP_CONTENTS, null)) {
                String query = readQuery(QUERY_TRANSFER_BACKUP_CONTENTS);
                dbCon.updateSqlStatement(query, null, null);
            } else {
                System.out.println("no table " + TABLE_CMS_BACKUP_CONTENTS + " found");
            }
        }

        // Drop the tables CMS_BACKUP_CONTENTS and CMS_ONLINE_CONTENTS
        cleanUpContentsTables(dbCon);
    }

    /**
     * Transfers the online content.<p>
     *
     * @param dbCon the db connection interface
     * @param pubTag the publish tag to use
     *
     * @throws SQLException if something goes wrong
     */
    protected void transferOnlineContents(CmsSetupDb dbCon, int pubTag) throws SQLException {

        String query = readQuery(QUERY_TRANSFER_ONLINE_CONTENTS);
        Map<String, String> replacer = Collections.singletonMap("${pubTag}", "" + pubTag);
        dbCon.updateSqlStatement(query, replacer, null);
    }

    /**
     * After the transfer the tables CMS_ONLINE_CONTENTS and CMS_BACKUP contents are dropped as they are no longer needed.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    private void cleanUpContentsTables(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = readQuery(QUERY_DROP_TABLE);
        HashMap<String, String> replacers = new HashMap<String, String>();
        // Drop the CMS_ONLINE_CONTENTS table
        replacers.put(REPLACEMENT_TABLENAME, TABLE_CMS_ONLINE_CONTENTS);
        dbCon.updateSqlStatement(query, replacers, null);

        replacers.clear();
        // Drop the CMS_BACKUP_CONTENTS table
        replacers.put(REPLACEMENT_TABLENAME, TABLE_CMS_BACKUP_CONTENTS);
        dbCon.updateSqlStatement(query, replacers, null);
    }
}
