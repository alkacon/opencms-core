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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class makes the remaining changes to some tables in order to update them.<p>
 *
 * The following tables will be altered
 *
 * CMS_ONLINE/OFFLINE_PROPERTYDEF   Add the TYPE column
 * CMS_ONLINE/OFFLINE_RESOURCES     Add the columns DATE_CONTENT and RESOURCE_VERSION
 * CMS_ONLINE/OFFLINE_STRUCTURE     Add the column STRUCTURE_VERSION
 * CMS_PROJECTS                     Drop the column TASK_ID and change the size for the project name
 *
 * @since 7.0.0
 */
public class CmsUpdateDBAlterTables extends A_CmsUpdateDBPart {

    /** Constant array with the queries for the CMS_ONLINE_CONTENTS table.<p> */
    protected static final String[] CMS_OFFLINE_CONTENTS_QUERIES = {"Q_OFFLINE_CONTENTS_DROP_COLUMN"};

    /** Constant ArrayList of the queries of the CMS_OFFLINE table.<p> */
    protected static final List<String> CMS_OFFLINE_CONTENTS_QUERIES_LIST = Collections.unmodifiableList(
        Arrays.asList(CMS_OFFLINE_CONTENTS_QUERIES));

    /** Constant array with the ONLINE and OFFLINE PROPERTYDEF tables.<p> */
    protected static final String[] CMS_PROPERTYDEF = {"CMS_OFFLINE_PROPERTYDEF", "CMS_ONLINE_PROPERTYDEF"};

    /** Constant ArrayList of the two PROPERTYDEF tables.<p> */
    protected static final List<String> CMS_PROPERTYDEF_LIST = Collections.unmodifiableList(
        Arrays.asList(CMS_PROPERTYDEF));

    /** Constant array with the ONLINE and OFFLINE RESOURCES tables.<p> */
    protected static final String[] CMS_RESOURCES = {"CMS_OFFLINE_RESOURCES", "CMS_ONLINE_RESOURCES"};

    /** Constant ArrayList of the two RESOURCES tables.<p> */
    protected static final List<String> CMS_RESOURCES_LIST = Collections.unmodifiableList(Arrays.asList(CMS_RESOURCES));

    /** Constant array with the ONLINE and OFFLINE STRUCTURE tables.<p> */
    protected static final String[] CMS_STRUCTURE = {"CMS_OFFLINE_STRUCTURE", "CMS_ONLINE_STRUCTURE"};

    /** Constant ArrayList of the two PROPERTYDEF tables.<p> */
    protected static final List<String> CMS_STRUCTURE_LIST = Collections.unmodifiableList(Arrays.asList(CMS_STRUCTURE));

    /** Constant for the column CONTENT_ID of the table CMS_OFFLINE_CONTENTS.<p> */
    protected static final String COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID = "CONTENT_ID";

    /** Constant for the column PROPERTYDEF_TYPE of the PROPERTYDEF tables.<p> */
    protected static final String COLUMN_CMS_PROPERTYDEF_TYPE = "PROPERTYDEF_TYPE";

    /** Constant for the column STRUCTURE_VERSION in the STRUCTURE tables.<p> */
    protected static final String COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION = "STRUCTURE_VERSION";

    /** Constant for the column PROJECT_NAME of the CMS_PROJECTS table.<p> */
    protected static final String COLUMN_PROJECTS_PROJECT_NAME = "PROJECT_NAME";

    /** Constant for the column TASK_ID of the CMS_PROJECTS table.<p> */
    protected static final String COLUMN_PROJECTS_TASK_ID = "TASK_ID";

    /** Constant for the new column DATE_CONTENT of the CMS_RESOURCES tables.<p> */
    protected static final String COLUMN_RESOURCES_DATE_CONTENT = "DATE_CONTENT";

    /** Constant for the new column RESOURCE_VERSION of the CMS_RESOURCES tables.<p> */
    protected static final String COLUMN_RESOURCES_RESOURCE_VERSION = "RESOURCE_VERSION";

    /** Constant for the sql replacement of the tablename.<p> */
    protected static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the table name CMS_OFFLINE_CONTENTS.<p> */
    protected static final String TABLE_CMS_OFFLINE_CONTENTS = "CMS_OFFLINE_CONTENTS";

    /** Constant for the table name CMS_PROJECTS.<p> */
    protected static final String TABLE_CMS_PROJECTS = "CMS_PROJECTS";

    /** Constant for the sql query to change the colum PROJECT_NAME.<p> */
    private static final String QUERY_CMS_PROJECTS_CHANGE_PROJECT_NAME = "Q_CMS_PROJECTS_CHANGE_PROJECT_NAME_SIZE";

    /** Constant for the sql query to drop the TASK_ID from the CMS_PROJECTS table.<p> */
    private static final String QUERY_CMS_PROJECTS_DROP_TASK_ID = "Q_CMS_PROJECTS_DROP_TASK_ID";

    /** Constant for the sql query to change the colum PROJECT_NAME.<p> */
    private static final String QUERY_CMS_PROJECTS_UPDATE_PROJECT_FLAGS = "Q_CMS_PROJECTS_UPDATE_PROJECT_FLAGS";

    /** Constant for the sql query to add the STRUCTURE_VERSION column to the STRUCTURE tables.<p> */
    private static final String QUERY_CMS_STRUCTURE_ADD_STRUCTURE_VERSION = "Q_CMS_STRUCTURE_ADD_STRUCTURE_VERSION";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_alter_remaining_queries.properties";

    /** Constant for the sql query to add the PROPERTYDEF_TYPE to the PROPERTYDEF tables.<p> */
    private static final String QUERY_PROPERTYDEF_TYPE = "Q_CMS_PROPERTYDEF";

    /** Constant for the sql query to select the correct resource versions.<p> */
    private static final String QUERY_SELECT_CMS_RESOURCE_VERSION = "Q_SELECT_CMS_RESOURCE_VERSION";

    /** Constant for the sql query to select the correct structure versions.<p> */
    private static final String QUERY_SELECT_CMS_STRUCTURE_VERSION = "Q_SELECT_CMS_STRUCTURE_VERSION";

    /** Constant for the sql query to update the resource version in each row.<p> */
    private static final String QUERY_UPDATE_RESOURCE_VERSION = "Q_UPDATE_RESOURCE_VERSION";

    /** Constant for the sql query to add the DATE_CONTENT column to the CMS_RESOURCES tables.<p> */
    private static final String QUERY_UPDATE_RESOURCES_DATE_CONTENT = "Q_UPDATE_RESOURCES_DATE_CONTENT";

    /** Constant for the sql query to add the RESOURCE_VERSIOn to the CMS_RESOURCES tables.<p> */
    private static final String QUERY_UPDATE_RESOURCES_RESOURCE_VERSION = "Q_UPDATE_RESOURCES_RESOURCE_VERSION";

    /** Constant for the sql query to update the structure version in each row.<p> */
    private static final String QUERY_UPDATE_STRUCTURE_VERSION = "Q_UPDATE_STRUCTURE_VERSION";

    /** Constant for the sql query to initialize the structure version in each row.<p> */
    private static final String QUERY_SET_STRUCTURE_VERSION = "Q_SET_STRUCTURE_VERSION";

    /** Constant for the sql query to initialize the resource version in each row.<p> */
    private static final String QUERY_SET_RESOURCES_VERSION = "Q_SET_RESOURCES_VERSION";

    /**
     * Default constructor.<p>
     *
     * @throws IOException if the default sql queries property file could not be read
     */
    public CmsUpdateDBAlterTables()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Update the CMS_OFFLINE_CONTENTS table
        // drop column content_id
        if (dbCon.hasTableOrColumn(TABLE_CMS_OFFLINE_CONTENTS, COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID)) {
            for (Iterator<String> it = CMS_OFFLINE_CONTENTS_QUERIES_LIST.iterator(); it.hasNext();) {
                String query = readQuery(it.next());
                dbCon.updateSqlStatement(query, null, null);
            }
        } else {
            System.out.println(
                "no column " + COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID + " in table " + TABLE_CMS_OFFLINE_CONTENTS);
        }

        // Update the CMS_ONLINE/OFFLINE_PROPERTYDEF tables
        // Add the column PROPERTYDEF_TYPE
        for (Iterator<String> it = CMS_PROPERTYDEF_LIST.iterator(); it.hasNext();) {
            String table = it.next();
            if (!dbCon.hasTableOrColumn(table, COLUMN_CMS_PROPERTYDEF_TYPE)) {
                String query = readQuery(QUERY_PROPERTYDEF_TYPE);
                HashMap<String, String> replacer = new HashMap<String, String>();
                replacer.put(REPLACEMENT_TABLENAME, table);
                dbCon.updateSqlStatement(query, replacer, null);
                replacer.clear();
            } else {
                System.out.println("column " + COLUMN_CMS_PROPERTYDEF_TYPE + " in table " + table + " already exists");
            }
        }

        // Update the ONLINE/OFFLINE_STRUCTURE
        // Add the STRUCTURE_VERSION
        for (Iterator<String> it = CMS_STRUCTURE_LIST.iterator(); it.hasNext();) {
            String table = it.next();
            // Add the column if needed
            if (!dbCon.hasTableOrColumn(table, COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION)) {
                String addColumn = readQuery(QUERY_CMS_STRUCTURE_ADD_STRUCTURE_VERSION);
                // Add the column
                HashMap<String, String> replacer = new HashMap<String, String>();
                replacer.put(REPLACEMENT_TABLENAME, table);
                dbCon.updateSqlStatement(addColumn, replacer, null);

                // initialize the STRUCTURE_VERSION column
                String initStructureVersion = readQuery(QUERY_SET_STRUCTURE_VERSION);
                dbCon.updateSqlStatement(initStructureVersion, replacer, null);

                // Update the entries of the newly created column
                String structureVersion = readQuery(QUERY_SELECT_CMS_STRUCTURE_VERSION);
                CmsSetupDBWrapper db = null;
                try {
                    db = dbCon.executeSqlStatement(structureVersion, replacer);
                    // update each row
                    while (db.getResultSet().next()) {
                        String updateQuery = readQuery(QUERY_UPDATE_STRUCTURE_VERSION);
                        String structureId = db.getResultSet().getString("STRUCTURE_ID");
                        int version = db.getResultSet().getInt("STRUCTURE_VERSION");
                        List<Object> params = new ArrayList<Object>();
                        params.add(Integer.valueOf(version)); // add the version
                        params.add(structureId);
                        dbCon.updateSqlStatement(updateQuery, replacer, params);
                    }
                } finally {
                    if (db != null) {
                        db.close();
                    }
                }
            } else {
                System.out.println(
                    "column " + COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION + " in table " + table + " already exists");
            }
        } // end update structure_version

        // Drop the TASK_ID column from CMS_PROJECTS
        if (dbCon.hasTableOrColumn(TABLE_CMS_PROJECTS, COLUMN_PROJECTS_TASK_ID)) {
            String dropTaskId = readQuery(QUERY_CMS_PROJECTS_DROP_TASK_ID);
            dbCon.updateSqlStatement(dropTaskId, null, null);
        } else {
            System.out.println("no column " + COLUMN_PROJECTS_TASK_ID + " in table " + TABLE_CMS_PROJECTS);
        }

        // Change the size of the project names
        if (dbCon.hasTableOrColumn(TABLE_CMS_PROJECTS, COLUMN_PROJECTS_PROJECT_NAME)) {
            String changeProjectName = readQuery(QUERY_CMS_PROJECTS_CHANGE_PROJECT_NAME);
            dbCon.updateSqlStatement(changeProjectName, null, null);
        } else {
            System.out.println("no column " + COLUMN_PROJECTS_PROJECT_NAME + " in table " + TABLE_CMS_PROJECTS);
        }

        // Update project flags for temporary projects
        if (dbCon.hasTableOrColumn(TABLE_CMS_PROJECTS, null)) {
            String updateProjectFlags = readQuery(QUERY_CMS_PROJECTS_UPDATE_PROJECT_FLAGS);
            dbCon.updateSqlStatement(updateProjectFlags, null, null);
        } else {
            System.out.println("table " + TABLE_CMS_PROJECTS + " does not exists");
        }

        // Update CMS_RESOURCES tables
        for (Iterator<String> it = CMS_RESOURCES_LIST.iterator(); it.hasNext();) {
            String table = it.next();
            HashMap<String, String> replacer = new HashMap<String, String>();
            replacer.put(REPLACEMENT_TABLENAME, table);
            if (!dbCon.hasTableOrColumn(table, COLUMN_RESOURCES_DATE_CONTENT)) {
                String addDateContent = readQuery(QUERY_UPDATE_RESOURCES_DATE_CONTENT);
                // add the DATE_CONTENT column
                dbCon.updateSqlStatement(addDateContent, replacer, null);
            } else {
                System.out.println(
                    "column " + COLUMN_RESOURCES_DATE_CONTENT + " in table " + table + " already exists");
            }

            if (!dbCon.hasTableOrColumn(table, COLUMN_RESOURCES_RESOURCE_VERSION)) {
                // add the RESOURCE_VERSION column
                String addResourceVersion = readQuery(QUERY_UPDATE_RESOURCES_RESOURCE_VERSION);
                dbCon.updateSqlStatement(addResourceVersion, replacer, null);

                // initialize the RESOURCE_VERSION column
                String initResourceVersion = readQuery(QUERY_SET_RESOURCES_VERSION);
                dbCon.updateSqlStatement(initResourceVersion, replacer, null);

                // Update the entries of the newly created column
                String resourceVersion = readQuery(QUERY_SELECT_CMS_RESOURCE_VERSION);
                CmsSetupDBWrapper db = null;
                try {
                    db = dbCon.executeSqlStatement(resourceVersion, replacer);
                    // update each row
                    while (db.getResultSet().next()) {
                        String updateQuery = readQuery(QUERY_UPDATE_RESOURCE_VERSION);
                        String resourceId = db.getResultSet().getString("RESOURCE_ID");
                        int version = db.getResultSet().getInt("RESOURCE_VERSION");
                        List<Object> params = new ArrayList<Object>();
                        params.add(Integer.valueOf(version)); // add the version
                        params.add(resourceId);
                        dbCon.updateSqlStatement(updateQuery, replacer, params);
                    }
                } finally {
                    if (db != null) {
                        db.close();
                    }
                }
            } else {
                System.out.println(
                    "column " + COLUMN_RESOURCES_RESOURCE_VERSION + " in table " + table + " already exists");
            }
        }
    }
}