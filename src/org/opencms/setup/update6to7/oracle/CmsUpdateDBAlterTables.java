/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/oracle/Attic/CmsUpdateDBAlterTables.java,v $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opencms.setup.CmsSetupDb;

/**
 * Oracle implementation of the generic Alter Table class.<p>
 * 
 * @author Roland Metzler
 *
 */
public class CmsUpdateDBAlterTables extends org.opencms.setup.update6to7.generic.CmsUpdateDBAlterTables {

    /** Constant for the sql query to change the colum PROJECT_NAME.<p> */
    private static final String QUERY_CMS_PROJECTS_CHANGE_PROJECT_NAME_ORACLE = "Q_CMS_PROJECTS_CHANGE_PROJECT_NAME_SIZE_ORACLE";

    /** Constant for the sql query to drop the TASK_ID from the CMS_PROJECTS table.<p> */
    private static final String QUERY_CMS_PROJECTS_DROP_TASK_ID_ORACLE = "Q_CMS_PROJECTS_DROP_TASK_ID_ORACLE";

    /** Constant for the sql query to add the STRUCTURE_VERSION column to the STRUCTURE tables.<p> */
    private static final String QUERY_CMS_STRUCTURE_ADD_STRUCTURE_VERSION_ORACLE = "Q_CMS_STRUCTURE_ADD_STRUCTURE_VERSION_ORACLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE_ORACLE = "oracle/cms_alter_remaining_queries.properties";

    /** Constant for the sql query to add the PROPERTYDEF_TYPE to the PROPERTYDEF tables.<p> */
    private static final String QUERY_PROPERTYDEF_TYPE_ORACLE = "Q_CMS_PROPERTYDEF_ORACLE";

    /** Constant for the sql query to select the correct structure versions.<p> */
    private static final String QUERY_SELECT_CMS_STRUCTURE_VERSION_ORACLE = "Q_SELECT_CMS_STRUCTURE_VERSION_ORACLE";

    /** Constant for the sql query to add the DATE_CONTENT column to the CMS_RESOURCES tables.<p> */
    private static final String QUERY_UPDATE_RESOURCES_DATE_CONTENT_ORACLE = "Q_UPDATE_RESOURCES_DATE_CONTENT_ORACLE";

    /** Constant for the sql query to add the RESOURCE_VERSIOn to the CMS_RESOURCES tables.<p> */
    private static final String QUERY_UPDATE_RESOURCES_RESOURCE_VERSION_ORACLE = "Q_UPDATE_RESOURCES_RESOURCE_VERSION_ORACLE";

    /** Constant for the sql query to update the structure version in each row.<p> */
    private static final String QUERY_UPDATE_STRUCTURE_VERSION_ORACLE = "Q_UPDATE_STRUCTURE_VERSION_ORACLE";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBAlterTables()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE_ORACLE);

    }

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    public void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Update the CMS_OFFLINE_CONTENTS table
        // drop column content_id
        if (dbCon.hasTableOrColumn(TABLE_CMS_OFFLINE_CONTENTS, COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID)) {
            for (Iterator it = CMS_OFFLINE_CONTENTS_QUERIES_LIST.iterator(); it.hasNext();) {
                String query = readQuery((String)it.next());
                dbCon.updateSqlStatement(query, null, null);
            }
        } else {
            System.out.println("no column "
                + COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID
                + " in table "
                + TABLE_CMS_OFFLINE_CONTENTS);
        }

        // Update the CMS_ONLINE/OFFLINE_PROPERTYDEF tables
        // Add the column PROPERTYDEF_TYPE
        for (Iterator it = CMS_PROPERTYDEF_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            if (!dbCon.hasTableOrColumn(table, COLUMN_CMS_PROPERTYDEF_TYPE)) {
                String query = readQuery(QUERY_PROPERTYDEF_TYPE_ORACLE);
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, table);
                dbCon.updateSqlStatement(query, replacer, null);
                replacer.clear();
            } else {
                System.out.println("column " + COLUMN_CMS_PROPERTYDEF_TYPE + " in table " + table + " already exists");
            }
        }

        // Update the ONLINE/OFFLINE_STRUCTURE 
        // Add the STRUCTURE_VERSION
        for (Iterator it = CMS_STRUCTURE_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            // Add the column if needed
            if (!dbCon.hasTableOrColumn(table, COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION)) {
                String addColumn = readQuery(QUERY_CMS_STRUCTURE_ADD_STRUCTURE_VERSION_ORACLE);
                // Add the column
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, table);
                dbCon.updateSqlStatement(addColumn, replacer, null);

                // Update the entries of the newly created column
                String structureVersion = readQuery(QUERY_SELECT_CMS_STRUCTURE_VERSION_ORACLE);
                ResultSet set = dbCon.executeSqlStatement(structureVersion, replacer);
                // update each row
                while (set.next()) {
                    String updateQuery = readQuery(QUERY_UPDATE_STRUCTURE_VERSION_ORACLE);
                    String structureId = set.getString("STRUCTURE_ID");
                    int version = set.getInt("STRUCTURE_VERSION");
                    List params = new ArrayList();
                    params.add(new Integer(version)); // add the version
                    params.add(structureId);
                    dbCon.updateSqlStatement(updateQuery, replacer, params);
                }
            } else {
                System.out.println("column "
                    + COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION
                    + " in table "
                    + table
                    + " already exists");
            }
        } // end update structure_version

        // Drop the TASK_ID column from CMS_PROJECTS
        if (dbCon.hasTableOrColumn(TABLE_CMS_PROJECTS, COLUMN_PROJECTS_TASK_ID)) {
            String dropTaskId = readQuery(QUERY_CMS_PROJECTS_DROP_TASK_ID_ORACLE);
            dbCon.updateSqlStatement(dropTaskId, null, null);
        } else {
            System.out.println("no column " + COLUMN_PROJECTS_TASK_ID + " in table " + TABLE_CMS_PROJECTS);
        }

        // Change the size of the project names
        if (dbCon.hasTableOrColumn(TABLE_CMS_PROJECTS, COLUMN_PROJECTS_PROJECT_NAME)) {
            String changeProjectName = readQuery(QUERY_CMS_PROJECTS_CHANGE_PROJECT_NAME_ORACLE);
            dbCon.updateSqlStatement(changeProjectName, null, null);
        } else {
            System.out.println("no column " + COLUMN_PROJECTS_PROJECT_NAME + " in table " + TABLE_CMS_PROJECTS);
        }

        // Update CMS_GROUPS and add the system roles

        // Update CMS_RESOURCES tables
        for (Iterator it = CMS_RESOURCES_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            HashMap replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, table);
            if (!dbCon.hasTableOrColumn(table, COLUMN_RESOURCES_DATE_CONTENT)) {
                String addDateContent = readQuery(QUERY_UPDATE_RESOURCES_DATE_CONTENT_ORACLE);
                // add the DATE_CONTENT column
                dbCon.updateSqlStatement(addDateContent, replacer, null);
            } else {
                System.out.println("column " + COLUMN_RESOURCES_DATE_CONTENT + " in table " + table + " already exists");
            }

            if (!dbCon.hasTableOrColumn(table, COLUMN_RESOURCES_RESOURCE_VERSION)) {
                // add the RESOURCE_VERISION column
                String addResourceVersion = readQuery(QUERY_UPDATE_RESOURCES_RESOURCE_VERSION_ORACLE);
                dbCon.updateSqlStatement(addResourceVersion, replacer, null);
            } else {
                System.out.println("column "
                    + COLUMN_RESOURCES_RESOURCE_VERSION
                    + " in table "
                    + table
                    + " already exists");
            }
        }
    }
}
