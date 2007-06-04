/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/oracle/Attic/CmsUpdateDBHistoryPrincipals.java,v $
 * Date   : $Date: 2007/06/04 12:00:33 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.setup.update6to7.oracle;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opencms.setup.CmsSetupDb;

/**
 * Oracle implementation to create the history principals table and contents.<p>
 * 
 * @author Roland Metzler
 *
 */
public class CmsUpdateDBHistoryPrincipals extends org.opencms.setup.update6to7.generic.CmsUpdateDBHistoryPrincipals {

    /** Constant for sql query to create the history principals table.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_CREATE_TABLE_ORACLE = "Q_HISTORY_PRINCIPALS_CREATE_TABLE_ORACLE";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_GROUPS_ORACLE = "Q_HISTORY_PRINCIPALS_PROJECTS_GROUPS_ORACLE";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS_ORACLE = "Q_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS_ORACLE";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED_ORACLE = "Q_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED_ORACLE";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_PROJECTS_USERS_ORACLE = "Q_HISTORY_PRINCIPALS_PROJECTS_USERS_ORACLE";

    /** Constant for sql query.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_RESOURCES_ORACLE = "Q_HISTORY_PRINCIPALS_RESOURCES_ORACLE";

    /** Constant for the sql query to select the count of history principals.<p> */
    private static final String QUERY_SELECT_COUNT_HISTORY_PRINCIPALS_ORACLE = "Q_SELECT_COUNT_HISTORY_PRINICPALS_ORACLE";

    /** Constant for sql query.<p> */
    private static final String QUERY_UPDATE_DATEDELETED_ORACLE = "Q_UPDATE_DATEDELETED_ORACLE";
    
    
    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "oracle/cms_history_principals_queries.properties";
    
    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBHistoryPrincipals()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }
    // Implement me
    
    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (insertHistoryPrincipals(dbCon)) {
            List params = new ArrayList();
            params.add(new Long(System.currentTimeMillis()));

            dbCon.updateSqlStatement(readQuery(QUERY_UPDATE_DATEDELETED_ORACLE), null, params);
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
        String query = readQuery(QUERY_SELECT_COUNT_HISTORY_PRINCIPALS_ORACLE);
        ResultSet set = dbCon.executeSqlStatement(query, null);
        if (set.next()) {
            if (set.getInt("COUNT") > 0) {
                result = true;
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
        boolean updateUserDateDeleted = false;
        // Check if the table exists. If not, create it
        if (!dbCon.hasTableOrColumn(TABLE_CMS_HISTORY_PRINCIPALS, null)) {
            String query = readQuery(QUERY_HISTORY_PRINCIPALS_CREATE_TABLE_ORACLE);
            dbCon.updateSqlStatement(query, null, null);
        } else {
            System.out.println(" table " + TABLE_CMS_HISTORY_PRINCIPALS + " already exists");
        }

        if (!hasData(dbCon)) {
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_RESOURCES_ORACLE), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_GROUPS_ORACLE), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS_ORACLE), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED_ORACLE), null, null);
            dbCon.updateSqlStatement(readQuery(QUERY_HISTORY_PRINCIPALS_PROJECTS_USERS_ORACLE), null, null);
            updateUserDateDeleted = true; // update the colum USER_DATETELETED
        }

        return updateUserDateDeleted;

    }
}
