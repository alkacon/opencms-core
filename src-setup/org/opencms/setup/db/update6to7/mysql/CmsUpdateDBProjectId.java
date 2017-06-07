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

package org.opencms.setup.db.update6to7.mysql;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * This class updates the project ids from integer values to CmsUUIDs in all existing database tables.<p>
 *
 * It creates new UUIDs for each existing project and stores it into a temporary table.<p>
 *
 * For each table using a project id a new column for the UUID is added and the according data is transferred.<p>
 * After that the original indexes and the column for the project id index is dropped and the new column with the
 * project uuid becomes the primary key.<p>
 */
public class CmsUpdateDBProjectId extends org.opencms.setup.db.update6to7.CmsUpdateDBProjectId {

    /** Constant for the sql query to create the new CMS_HISTORY_PROJECTS table.<p> */
    private static final String QUERY_CREATE_HISTORY_PROJECTS_TABLE_MYSQL = "Q_CREATE_HISTORY_PROJECTS_TABLE_MYSQL";

    /** Constant for the sql query to create the temporary table.<p> */
    private static final String QUERY_CREATE_TEMP_TABLE_UUIDS_MYSQL = "Q_CREATE_TEMPORARY_TABLE_UUIDS_MYSQL";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_projectid_queries.properties";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBProjectId()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * Creates the CMS_HISTORY_PROJECTS table if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    @Override
    protected void createHistProjectsTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(HISTORY_PROJECTS_TABLE, null)) {
            String createStatement = readQuery(QUERY_CREATE_HISTORY_PROJECTS_TABLE_MYSQL);
            Map<String, String> replacer = Collections.singletonMap("${tableEngine}", m_poolData.get("engine"));
            dbCon.updateSqlStatement(createStatement, replacer, null);
            transferDataToHistoryTable(dbCon);
        } else {
            System.out.println("table " + HISTORY_PROJECTS_TABLE + " already exists");
        }
    }

    /**
     * Creates the temp table for project ids if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    @Override
    protected void createTempTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(TEMPORARY_TABLE_NAME, null)) {
            String createStatement = readQuery(QUERY_CREATE_TEMP_TABLE_UUIDS_MYSQL);
            Map<String, String> replacer = Collections.singletonMap("${tableEngine}", m_poolData.get("engine"));
            dbCon.updateSqlStatement(createStatement, replacer, null);
        } else {
            System.out.println("table " + TEMPORARY_TABLE_NAME + " already exists");
        }
    }

}