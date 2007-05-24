/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBAlterTables.java,v $
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;

/**
 * This class makes the remaining changes to some tables in order to update them.<p>
 * 
 * The following tables will be altered
 * 
 * CMS_OFFLINE_CONTENTS             Change the primary key and drop the original one
 * CMS_ONLINE/OFFLINE_PROPERTYDEF   Add the TYPE column
 * CMS_ONLINE/OFFLINE_RESOURCES     Add the columns DATE_CONTENT and RESOURCE_VERSION
 * CMS_ONLINE/OFFLINE_STRUCTURE     Add the column STRUCTURE_VERSION
 * CMS_PROJECTS                     Drop the column TASK_ID
 * CMS_PUBLISH_HISTORY              Change the primary key and drop the original one
 * 
 * @author metzler
 */
public class CmsUpdateDBAlterTables {

    /** Constant array with the queries for the CMS_ONLINE_CONTENTS table.<p> */
    private static final String[] CMS_OFFLINE_CONTENTS_QUERIES = {
        "Q_OFFLINE_CONTENTS_DROP_PRIMARY_KEY",
        "Q_OFFLINE_CONTENTS_DROP_COLUMN",
        "Q_OFFLINE_CONTENTS_ADD_PRIMARY_KEY"};

    /** Constant ArrayList of the queries of the CMS_OFFLINE table.<p> */
    private static final List CMS_OFFLINE_CONTENTS_QUERIES_LIST = Collections.unmodifiableList(Arrays.asList(CMS_OFFLINE_CONTENTS_QUERIES));

    /** Constant array with the ONLINE and OFFLINE PROPERTYDEF tables.<p> */
    private static final String[] CMS_PROPERTYDEF = {"CMS_OFFLINE_PROPERTYDEF", "CMS_ONLINE_PROPERTYDEF"};

    /** Constant ArrayList of the two PROPERTYDEF tables.<p> */
    private static final List CMS_PROPERTYDEF_LIST = Collections.unmodifiableList(Arrays.asList(CMS_PROPERTYDEF));

    /** Constant array with the queries for the CMS_PUBLISH_HISTORY table.<p> */
    private static final String[] CMS_PUBLISH_HISTORY_QUERIES = {
        "Q_PUBLISH_HISTORY_DROP_PRIMARY_KEY",
        "Q_PUBLISH_HISTORY_ADD_PRIMARY_KEY"};

    /** Constant ArrayList of the queries for the CMS_PUBLISH_HISTORY table.<p> */
    private static final List CMS_PUBLISH_HISTORY_QUERIES_LIST = Collections.unmodifiableList(Arrays.asList(CMS_PUBLISH_HISTORY_QUERIES));

    /** Constant array with the ONLINE and OFFLINE RESOURCES tables.<p> */
    private static final String[] CMS_RESOURCES = {"CMS_OFFLINE_RESOURCES", "CMS_ONLINE_RESOURCES"};

    /** Constant ArrayList of the two RESOURCES tables.<p> */
    private static final List CMS_RESOURCES_LIST = Collections.unmodifiableList(Arrays.asList(CMS_RESOURCES));

    /** Constant array with the ONLINE and OFFLINE STRUCTURE tables.<p> */
    private static final String[] CMS_STRUCTURE = {"CMS_OFFLINE_STRUCTURE", "CMS_ONLINE_STRUCTURE"};

    /** Constant ArrayList of the two PROPERTYDEF tables.<p> */
    private static final List CMS_STRUCTURE_LIST = Collections.unmodifiableList(Arrays.asList(CMS_STRUCTURE));

    /** Constant for the column CONTENT_ID of the table CMS_OFFLINE_CONTENTS.<p> */
    private static final String COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID = "CONTENT_ID";

    /** Constant for the column PROPERTYDEF_TYPE of the PROPERTYDEF tables.<p> */
    private static final String COLUMN_CMS_PROPERTYDEF_TYPE = "PROPERTYDEF_TYPE";

    /** Constant for the column STRUCTURE_VERSION in the STRUCTURE tables.<p> */
    private static final String COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION = "STRUCTURE_VERSION";

    /** Constant for the column TASK_ID of the CMS_PROJECTS table.<p> */
    private static final String COLUMN_PROJECTS_TASK_ID = "TASK_ID";

    /** Constant for the new column DATE_CONTENT of the CMS_RESOURCES tables.<p> */
    private static final String COLUMN_RESOURCES_DATE_CONTENT = "DATE_CONTENT";

    /** Constant for the new column RESOURCE_VERSION of the CMS_RESOURCES tables.<p> */
    private static final String COLUMN_RESOURCES_RESOURCE_VERSION = "RESOURCE_VERSION";

    /** Constant for the sql query to drop the TASK_ID from the CMS_PROJECTS table.<p> */
    private static final String QUERY_CMS_PROJECTS_DROP_TASK_ID = "Q_CMS_PROJECTS_DROP_TASK_ID";

    /** Constant for the sql query to add the STRUCTURE_VERSION column to the STRUCTURE tables.<p> */
    private static final String QUERY_CMS_STRUCTURE_ADD_STRUCTURE_VERSION = "Q_CMS_STRUCTURE_ADD_STRUCTURE_VERSION";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_alter_remaining_queries.properties";

    /** Constant for the sql query to add the PROPERTYDEF_TYPE to the PROPERTYDEF tables.<p> */
    private static final String QUERY_PROPERTYDEF_TYPE = "Q_CMS_PROPERTYDEF";

    /** Constant for the sql query to select the correct structure versions.<p> */
    private static final String QUERY_SELECT_CMS_STRUCTURE_VERSION = "Q_SELECT_CMS_STRUCTURE_VERSION";

    /** Constant for the sql query to add the DATE_CONTENT column to the CMS_RESOURCES tables.<p> */
    private static final String QUERY_UPDATE_RESOURCES_DATE_CONTENT = "Q_UPDATE_RESOURCES_DATE_CONTENT";

    /** Constant for the sql query to add the RESOURCE_VERSIOn to the CMS_RESOURCES tables.<p> */
    private static final String QUERY_UPDATE_RESOURCES_RESOURCE_VERSION = "Q_UPDATE_RESOURCES_RESOURCE_VERSION";

    /** Constant for the sql query to update the structure version in each row.<p> */
    private static final String QUERY_UPDATE_STRUCTURE_VERSION = "Q_UPDATE_STRUCTURE_VERSION";

    /** Constant for the sql replacement of the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the table name CMS_OFFLINE_CONTENTS.<p> */
    private static final String TABLE_CMS_OFFLINE_CONTENTS = "CMS_OFFLINE_CONTENTS";

    /** Constant for the table name CMS_PROJECTS.<p> */
    private static final String TABLE_CMS_PROJECTS = "CMS_PROJECTS";

    /** Constant for the table name CMS_PUBLISH_HISTORY.<p> */
    private static final String TABLE_CMS_PUBLISH_HISTORY = "CMS_PUBLISH_HISTORY";

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
     * @throws IOException if the sql query properties file could not be read
     */
    public CmsUpdateDBAlterTables(CmsSetupDb dbcon, String rfsPath)
    throws IOException {

        System.err.println(getClass().getName());
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
     * Gets the sql query properties.<p>
     * 
     * @return the queryProperties
     */
    public ExtendedProperties getQueryProperties() {

        return m_queryProperties;
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
     * Sets the sql query properties.<p>
     * 
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(ExtendedProperties queryProperties) {

        m_queryProperties = queryProperties;
    }

    /**
     * Executes the remaing updates for some of the tables to bring them to the new structure.<p>
     *
     * @throws SQLException if something goes wrong
     */
    public void updateRemaingTables() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Update the CMS_OFFLINE_CONTENTS table
        // Drop primary key, drop column content_id, add primary key to resource_id
        if (m_dbcon.hasTableOrColumn(TABLE_CMS_OFFLINE_CONTENTS, COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID)) {
            for (Iterator it = CMS_OFFLINE_CONTENTS_QUERIES_LIST.iterator(); it.hasNext();) {
                String query = (String)m_queryProperties.get(it.next());
                m_dbcon.updateSqlStatement(query, null, null);
            }
        } else {
            System.out.println("no column " + COLUMN_CMS_OFFLINE_CONTENTS_CONTENT_ID + " in table " + TABLE_CMS_OFFLINE_CONTENTS);
        }

        // Update the CMS_ONLINE/OFFLINE_PROPERTYDEF tables
        // Add the column PROPERTYDEF_TYPE
        for (Iterator it = CMS_PROPERTYDEF_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            if (!m_dbcon.hasTableOrColumn(table, COLUMN_CMS_PROPERTYDEF_TYPE)) {
                String query = (String)m_queryProperties.get(QUERY_PROPERTYDEF_TYPE);
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, table);
                m_dbcon.updateSqlStatement(query, replacer, null);
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
            if (!m_dbcon.hasTableOrColumn(table, COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION)) {
                String addColumn = (String)m_queryProperties.get(QUERY_CMS_STRUCTURE_ADD_STRUCTURE_VERSION);
                // Add the column
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, table);
                m_dbcon.updateSqlStatement(addColumn, replacer, null);

                // Update the entries of the newly created column
                String structureVersion = (String)m_queryProperties.get(QUERY_SELECT_CMS_STRUCTURE_VERSION);
                ResultSet set = m_dbcon.executeSqlStatement(structureVersion, replacer);
                // update each row
                while (set.next()) {
                    String updateQuery = (String)m_queryProperties.get(QUERY_UPDATE_STRUCTURE_VERSION);
                    String structureId = set.getString("STRUCTURE_ID");
                    int version = set.getInt("STRUCTURE_VERSION");
                    List params = new ArrayList();
                    params.add(new Integer(version)); // add the version
                    params.add(structureId);
                    m_dbcon.updateSqlStatement(updateQuery, replacer, params);
                }
            } else {
                System.out.println("column " + COLUMN_CMS_STRUCTURE_STRUCTURE_VERSION + " in table " + table + " already exists");
            }
        } // end update structure_version

        // Drop the TASK_ID column from CMS_PROJECTS
        if (m_dbcon.hasTableOrColumn(TABLE_CMS_PROJECTS, COLUMN_PROJECTS_TASK_ID)) {
            String dropTaskId = (String)m_queryProperties.get(QUERY_CMS_PROJECTS_DROP_TASK_ID);
            m_dbcon.updateSqlStatement(dropTaskId, null, null);
        } else {
            System.out.println("no column " + COLUMN_PROJECTS_TASK_ID + " in table " + TABLE_CMS_PROJECTS);
        }

        // Update CMS_GROUPS and add the system roles

        // Update CMS_RESOURCES tables
        for (Iterator it = CMS_RESOURCES_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            HashMap replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, table);
            if (!m_dbcon.hasTableOrColumn(table, COLUMN_RESOURCES_DATE_CONTENT)) {
                String addDateContent = (String)m_queryProperties.get(QUERY_UPDATE_RESOURCES_DATE_CONTENT);
                // add the DATE_CONTENT column
                m_dbcon.updateSqlStatement(addDateContent, replacer, null);
            } else {
                System.out.println("column " + COLUMN_RESOURCES_DATE_CONTENT + " in table " + table + " already exists");
            }

            if (!m_dbcon.hasTableOrColumn(table, COLUMN_RESOURCES_RESOURCE_VERSION)) {
                // add the RESOURCE_VERISION column
                String addResourceVersion = (String)m_queryProperties.get(QUERY_UPDATE_RESOURCES_RESOURCE_VERSION);
                m_dbcon.updateSqlStatement(addResourceVersion, replacer, null);
            } else {
                System.out.println("column " + COLUMN_RESOURCES_RESOURCE_VERSION + " in table " + table + " already exists");
            }

        }

        // Update the CMS_PUBLISH_HISTORY table
        if (m_dbcon.hasTableOrColumn(TABLE_CMS_PUBLISH_HISTORY, null)) {
            for (Iterator it = CMS_PUBLISH_HISTORY_QUERIES_LIST.iterator(); it.hasNext();) {
                String query = (String)m_queryProperties.get(it.next());
                m_dbcon.updateSqlStatement(query, null, null);
            }
        } else {
            System.out.println("no table " + TABLE_CMS_PUBLISH_HISTORY + " found");
        }
    }
}
