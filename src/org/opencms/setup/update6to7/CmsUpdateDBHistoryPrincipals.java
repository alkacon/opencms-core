/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBHistoryPrincipals.java,v $
 * Date   : $Date: 2007/05/24 13:07:19 $
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

package org.opencms.setup.update6to7;

import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This class inserts formerly deleted users/groups in the CMS_HISTORY_PRINCIPALS table.<p>
 * 
 * These users/groups are read out of the following tables:
 * <ul>
 * <li>CMS_BACKUP_RESOURCES</li>
 * <li>CMS_BACKUP_PROJECTS</li>
 * </ul>
 *
 * @author Raphael Schnuck
 */
public class CmsUpdateDBHistoryPrincipals {

    /** Constant for sql query to create the history principals table.<p> */
    private static final String QUERY_HISTORY_PRINCIPALS_CREATE_TABLE = "Q_HISTORY_PRINCIPALS_CREATE_TABLE";

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
    private static final String QUERY_PROPERTY_FILE = "/update/sql/history_principals_queries.properties";

    /** Constant for the sql query to select the count of history principals.<p> */
    private static final String QUERY_SELECT_COUNT_HISTORY_PRINCIPALS = "Q_SELECT_COUNT_HISTORY_PRINICPALS";

    /** Constant for sql query.<p> */
    private static final String QUERY_UPDATE_DATEDELETED = "Q_UPDATE_DATEDELETED";

    /** Constant for the CMS_HISTORY_PRINICIPALS table.<p> */
    private static final String TABLE_CMS_HISTORY_PRINCIPALS = "CMS_HISTORY_PRINCIPALS";

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
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBHistoryPrincipals(CmsSetupDb dbcon, String rfsPath)
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
     * Gets the sql statements in the extended properties.<p> 
     * 
     * @return the queryProperties
     */
    public ExtendedProperties getQueryProperties() {

        return m_queryProperties;
    }

    /**
     * Inserts deleted users/groups in the history principals table.<p>
     * 
     * @return true if the USER_DATEDELETED needs updating, false if not
     * 
     * @throws SQLException if something goes wrong
     */
    public boolean insertHistoryPrincipals() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        boolean updateUserDateDeleted = false;
        // Check if the table exists. If not, create it
        if (!m_dbcon.hasTableOrColumn(TABLE_CMS_HISTORY_PRINCIPALS, null)) {
            String query = (String)m_queryProperties.get(QUERY_HISTORY_PRINCIPALS_CREATE_TABLE);
            m_dbcon.updateSqlStatement(query, null, null);
        } else {
            System.out.println(" table " + TABLE_CMS_HISTORY_PRINCIPALS + " already exists");
        }

        if (!hasData()) {
            m_dbcon.updateSqlStatement((String)m_queryProperties.get(QUERY_HISTORY_PRINCIPALS_RESOURCES), null, null);
            m_dbcon.updateSqlStatement(
                (String)m_queryProperties.get(QUERY_HISTORY_PRINCIPALS_PROJECTS_GROUPS),
                null,
                null);
            m_dbcon.updateSqlStatement(
                (String)m_queryProperties.get(QUERY_HISTORY_PRINCIPALS_PROJECTS_MANAGERGROUPS),
                null,
                null);
            m_dbcon.updateSqlStatement(
                (String)m_queryProperties.get(QUERY_HISTORY_PRINCIPALS_PROJECTS_PUBLISHED),
                null,
                null);
            m_dbcon.updateSqlStatement(
                (String)m_queryProperties.get(QUERY_HISTORY_PRINCIPALS_PROJECTS_USERS),
                null,
                null);
            updateUserDateDeleted = true; // update the colum USER_DATETELETED
        }

        return updateUserDateDeleted;

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
     * Sets the sql statements for the extended properties.<p>
     * 
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(ExtendedProperties queryProperties) {

        m_queryProperties = queryProperties;
    }

    /**
     * Updates the history principals table with the current time in the DATEDELETED column.<p>
     * 
     * @throws SQLException if something goes wrong
     */
    public void updateHistoryPrincipals() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        List params = new ArrayList();
        params.add(new Long(System.currentTimeMillis()));

        m_dbcon.updateSqlStatement((String)m_queryProperties.get(QUERY_UPDATE_DATEDELETED), null, params);
    }

    /**
     * Checks if the CMS_HISTORY_PRINCIPALS already has data in it.<p>
     * 
     * @return true if there is already data in the table, false if it is empty
     * 
     * @throws SQLException if something goes wrong 
     */
    private boolean hasData() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        boolean result = false;
        String query = (String)m_queryProperties.get(QUERY_SELECT_COUNT_HISTORY_PRINCIPALS);
        ResultSet set = m_dbcon.executeSqlStatement(query, null);
        if (set.next()) {
            if (set.getInt("COUNT") > 0) {
                result = true;
            }
        }

        return result;
    }
}
