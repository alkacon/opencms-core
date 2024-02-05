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
import java.util.ArrayList;
import java.util.List;

/**
 * This class inserts formerly deleted users/groups in the CMS_HISTORY_PRINCIPALS table.<p>
 *
 * These users/groups are read out of the following tables:
 * <ul>
 * <li>CMS_BACKUP_RESOURCES</li>
 * <li>CMS_BACKUP_PROJECTS</li>
 * </ul>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBHistoryPrincipals extends A_CmsUpdateDBPart {

    /** Constant for sql query to create the history principals table.<p> */
    protected static final String QUERY_HISTORY_PRINCIPALS_CREATE_TABLE = "Q_HISTORY_PRINCIPALS_CREATE_TABLE";

    /** Constant for the CMS_HISTORY_PRINICIPALS table.<p> */
    protected static final String TABLE_CMS_HISTORY_PRINCIPALS = "CMS_HISTORY_PRINCIPALS";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_GROUPS = "Q_HISTORY_PRINCIPALS_PROJECTS_GROUPS";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS = "Q_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED = "Q_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_USERS = "Q_HISTORY_PRINCIPALS_PROJECTS_USERS";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_RESOURCES = "Q_HISTORY_PRINCIPALS_RESOURCES";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_history_principals_queries.properties";

    /** Constant for the sql query to select the count of history principals.<p> */
    private static final String QUERY_SELECT_COUNT_HISTORY_PRINCIPALS = "Q_SELECT_COUNT_HISTORY_PRINICPALS";

    /** Constant for sql query.<p> */
    private static final String QUERY_UPDATE_DATEDELETED = "Q_UPDATE_DATEDELETED";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBHistoryPrincipals()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * Creates the CMS_HISTORY_PRINCIPALS table if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if soemthing goes wrong
     */
    protected void createHistPrincipalsTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(TABLE_CMS_HISTORY_PRINCIPALS, null)) {
            String createStatement = readQuery(QUERY_HISTORY_PRINCIPALS_CREATE_TABLE);
            dbCon.updateSqlStatement(createStatement, null, null);
        } else {
            System.out.println("table " + TABLE_CMS_HISTORY_PRINCIPALS + " already exists");
        }
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (insertHistoryPrincipals(dbCon)) {
            List<Object> params = new ArrayList<Object>();
            params.add(Long.valueOf(System.currentTimeMillis()));

            dbCon.updateSqlStatement(readQuery(QUERY_UPDATE_DATEDELETED), null, params);
        }
    }

    /**
     * Checks if the CMS_HISTORY_PRINCIPALS already has data in it.<p>
     *
     * @param dbCon the db connection interface
     * @return true if there is already data in the table, false if it is empty
     *
     * @throws SQLException if something goes wrong
     */
    private boolean hasData(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        boolean result = false;
        String query = readQuery(QUERY_SELECT_COUNT_HISTORY_PRINCIPALS);
        CmsSetupDBWrapper db = null;
        try {
            db = dbCon.executeSqlStatement(query, null);
            if (db.getResultSet().next()) {
                if (db.getResultSet().getInt("COUNT") > 0) {
                    result = true;
                }
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return result;
    }

    /**
     * Inserts deleted users/groups in the history principals table.<p>
     *
     * @param dbCon the db connection interface
     * @return true if the USER_DATEDELETED needs updating, false if not
     *
     * @throws SQLException if something goes wrong
     */
    private boolean insertHistoryPrincipals(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        createHistPrincipalsTable(dbCon);

        boolean updateUserDateDeleted = false;
        if (isKeepHistory() && !hasData(dbCon)) {
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_RESOURCES), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_GROUPS), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_USERS), null, null);
            updateUserDateDeleted = true; // update the colum USER_DATETELETED
        }

        return updateUserDateDeleted;
    }
}
