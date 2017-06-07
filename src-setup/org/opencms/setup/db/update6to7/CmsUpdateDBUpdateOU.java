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

import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.db.A_CmsUpdateDBPart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class upgrades the database tables containing new OU columns.<p>
 *
 * These tables are
 * cms_groups
 * cms_history_principals
 * cms_history_projects
 * cms_projects
 * cms_users
 *
 * @since 7.0.0
 */
public class CmsUpdateDBUpdateOU extends A_CmsUpdateDBPart {

    /** Constant for the GROUP_OU column.<p> */
    protected static final String GROUP_OU_COLUMN = "GROUP_OU";

    /** Constant for the PROJECT_OU column.<p> */
    protected static final String PROJECT_OU_COLUMN = "PROJECT_OU";

    /** Constant for the query that adds the ous to the table.<p> */
    protected static final String QUERY_ADD_OUS_TO_TABLE = "Q_ADD_OUS_TO_TABLE";

    /** Constant for the alteration of the table.<p> */
    protected static final String QUERY_KEY_ALTER_TABLE = "Q_ALTER_TABLE_ADD_OU_COLUMN";

    /** Constant for the replacement in the SQL query for the columnname.<p> */
    protected static final String REPLACEMENT_COLUMNNAME = "${columnname}";

    /** Constant for the replacement in the SQL query for the tablename.<p> */
    protected static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the CMS_BACKUP_PROJECTS table.<p> */
    protected static final String TABLE_BACKUP_PROJECTS = "CMS_BACKUP_PROJECTS";

    /** Constant for the CMS_GROUPS table.<p> */
    protected static final String TABLE_CMS_GROUPS = "CMS_GROUPS";

    /** Constant for the CMS_USERS table.<p> */
    protected static final String TABLE_CMS_USERS = "CMS_USERS";

    /** Constant for the CMS_PROJECTS table.<p> */
    protected static final String TABLE_PROJECTS = "CMS_PROJECTS";

    /** Constant for the USER_OU column.<p> */
    protected static final String USER_OU_COLUMN = "USER_OU";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_ou_query.properties";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBUpdateOU()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * Checks if the column USER_OU is found in the resultset.<p>
     *
     * @param dbCon the db connection interface
     * @param table the table to check
     * @param ouColumn the type of OU to find (e.g. USER_OU or GROUP_OU)
     *
     * @return true if the column is in the result set, false if not
     */
    protected boolean findOUColumn(CmsSetupDb dbCon, String table, String ouColumn) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        return dbCon.hasTableOrColumn(table, ouColumn);
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());

        updateOUs(dbCon, TABLE_CMS_USERS, USER_OU_COLUMN);
        updateOUs(dbCon, TABLE_CMS_GROUPS, GROUP_OU_COLUMN);
        updateOUs(dbCon, TABLE_PROJECTS, PROJECT_OU_COLUMN);
        updateOUs(dbCon, TABLE_BACKUP_PROJECTS, PROJECT_OU_COLUMN);
    }

    /**
     * Updates the database tables with the new OUs if necessary for the given table.<p>
     *
     * @param dbCon the db connection interface
     * @param table the table to update
     * @param ouColumn the column to insert
     *
     * @return true if everything worked fine, false if not
     */
    protected int updateOUs(CmsSetupDb dbCon, String table, String ouColumn) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        int result = 1;
        try {

            if (!findOUColumn(dbCon, table, ouColumn)) {
                // Alter the table and add the OUs
                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put(REPLACEMENT_TABLENAME, table);
                replacements.put(REPLACEMENT_COLUMNNAME, ouColumn);
                String alterQuery = readQuery(QUERY_KEY_ALTER_TABLE);

                // Update the database and alter the table to add the OUs
                dbCon.updateSqlStatement(alterQuery, replacements, null);

                // Insert the value '/' into the OUs
                String insertQuery = readQuery(QUERY_ADD_OUS_TO_TABLE);
                dbCon.updateSqlStatement(insertQuery, replacements, null);
                result = 0;
            } else {
                System.out.println("column " + ouColumn + " in table " + table + " already exists");
            }
            // Nothing needs to be done
            result = 0;
        } catch (SQLException e) {
            e.printStackTrace();
            result = 1;
        }

        return result;
    }

}
