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

import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Oracle implementation to drop the old indexes from the database.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBDropOldIndexes extends org.opencms.setup.db.update6to7.CmsUpdateDBDropOldIndexes {

    /** Constant for the field of the constraint name.<p> */
    private static final String FIELD_CONSTRAINT_ORACLE = "CONSTRAINT_NAME";

    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX_ORACLE = "INDEX_NAME";

    /** Constant for the sql query to drop a unique index key. */
    private static final String QUERY_DROP_CONSTRAINT = "Q_DROP_CONSTRAINT";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE_ORACLE = "cms_drop_all_indexes_queries.properties";

    /** Constant for the sql query to list contraints for a table. */
    private static final String QUERY_SHOW_CONSTRAINTS = "Q_SHOW_CONSTRAINTS";

    /** Constant for the replacement of the index name. */
    private static final String REPLACEMENT_INDEX_ORACLE = "${indexname}";

    /** Constant array of the temporary indexes. */
    private static final String[] TEMP_INDEXES = {
        "CMS_BACKUP_CONTENTS_INDEX_1",
        "CMS_BACKUP_CONTENTS_INDEX_2",
        "CMS_BACKUP_PROJECTRESOURCES_INDEX_1",
        "CMS_BACKUP_PROJECTS_INDEX_1",
        "CMS_BACKUP_PROJECTS_INDEX_2",
        "CMS_BACKUP_PROJECTS_INDEX_3",
        "CMS_BACKUP_PROJECTS_INDEX_4",
        "CMS_BACKUP_PROJECTS_INDEX_5",
        "CMS_BACKUP_PROJECTS_INDEX_6",
        "CMS_BACKUP_PROPERTIES_INDEX_1",
        "CMS_BACKUP_PROPERTIES_INDEX_2",
        "CMS_BACKUP_PROPERTIES_INDEX_3",
        "CMS_BACKUP_PROPERTYDEF_INDEX_1",
        "CMS_BACKUP_RESOURCES_INDEX_1",
        "CMS_BACKUP_RESOURCES_INDEX_2",
        "CMS_BACKUP_RESOURCES_INDEX_3",
        "CMS_BACKUP_RESOURCES_INDEX_4",
        "CMS_BACKUP_RESOURCES_INDEX_5",
        "CMS_BACKUP_STRUCTURE_INDEX_1",
        "CMS_BACKUP_STRUCTURE_INDEX_2",
        "CMS_BACKUP_STRUCTURE_INDEX_3",
        "CMS_BACKUP_STRUCTURE_INDEX_4",
        "CMS_GROUPS_INDEX_1",
        "CMS_OFFLINE_CONTENTS_INDEX_1",
        "CMS_OFFLINE_RESOURCES_INDEX_1",
        "CMS_OFFLINE_RESOURCES_INDEX_2",
        "CMS_OFFLINE_RESOURCES_INDEX_3",
        "CMS_OFFLINE_RESOURCES_INDEX_4",
        "CMS_OFFLINE_STRUCTURE_INDEX_1",
        "CMS_OFFLINE_STRUCTURE_INDEX_2",
        "CMS_OFFLINE_STRUCTURE_INDEX_3",
        "CMS_ONLINE_CONTENTS_INDEX_1",
        "CMS_ONLINE_RESOURCES_INDEX_1",
        "CMS_ONLINE_RESOURCES_INDEX_2",
        "CMS_ONLINE_RESOURCES_INDEX_3",
        "CMS_ONLINE_RESOURCES_INDEX_4",
        "CMS_ONLINE_STRUCTURE_INDEX_1",
        "CMS_ONLINE_STRUCTURE_INDEX_2",
        "CMS_ONLINE_STRUCTURE_INDEX_3",
        "CMS_PROJECTRESOURCES_INDEX_1",
        "CMS_PROJECTS_INDEX_1",
        "CMS_PROJECTS_INDEX_2",
        "CMS_PROJECTS_INDEX_3",
        "CMS_PROJECTS_INDEX_4",
        "CMS_PUBLISH_HISTORY_INDEX_1",
        "CMS_PUBLISH_HISTORY_INDEX_2"};

    /** Constant Array List of the temporary indexes. */
    private static final List<String> TEMP_INDEXES_LIST = Collections.unmodifiableList(Arrays.asList(TEMP_INDEXES));

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBDropOldIndexes()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE_ORACLE);
    }

    // No implementation yet

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) {

        List<String> tablenames = CMS_TABLES_LIST;

        // Iterate over all the tables.
        for (Iterator<String> tableIterator = tablenames.iterator(); tableIterator.hasNext();) {
            String tablename = tableIterator.next();
            System.out.println("dropping indexes for table " + tablename);
            // Check if the table is existing
            if (dbCon.hasTableOrColumn(tablename, null)) {
                try {

                    // First drop constraints
                    List<String> constraints = getConstraints(dbCon, tablename);
                    Iterator<String> iter = constraints.iterator();
                    while (iter.hasNext()) {
                        String constraint = iter.next();

                        String dropConstraint = readQuery(QUERY_DROP_CONSTRAINT);
                        Map<String, String> replacer = new HashMap<String, String>();
                        replacer.put(REPLACEMENT_TABLENAME, tablename);
                        replacer.put(REPLACEMENT_INDEX_ORACLE, constraint);
                        dbCon.updateSqlStatement(dropConstraint, replacer, null);
                    }

                    // Drop the indexes from the table.
                    List<String> indexes = getIndexes(dbCon, tablename);
                    iter = indexes.iterator();
                    while (iter.hasNext()) {
                        String index = iter.next();

                        // Drop the index
                        String dropIndex = readQuery(QUERY_DROP_INDEX);
                        Map<String, String> replacerIndex = new HashMap<String, String>();
                        replacerIndex.put(REPLACEMENT_INDEX_ORACLE, index);
                        dbCon.updateSqlStatement(dropIndex, replacerIndex, null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Create the temporary indexes
        // Each index must be created in its own query
        for (Iterator<String> tempIndexes = TEMP_INDEXES_LIST.iterator(); tempIndexes.hasNext();) {
            try {
                String createIndex = tempIndexes.next();
                String creationQuery = readQuery(createIndex);
                dbCon.updateSqlStatement(creationQuery, null, null);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the constraints for a table.<p>
     *
     * @param dbCon the db connection interface
     * @param tablename the table to get the indexes from
     *
     * @return a list of constraints
     *
     * @throws SQLException if something goes wrong
     */
    private List<String> getConstraints(CmsSetupDb dbCon, String tablename) throws SQLException {

        List<String> constraints = new ArrayList<String>();
        String tableConstraints = readQuery(QUERY_SHOW_CONSTRAINTS);
        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        CmsSetupDBWrapper db = null;
        try {
            db = dbCon.executeSqlStatement(tableConstraints, replacer);
            while (db.getResultSet().next()) {
                String constraint = db.getResultSet().getString(FIELD_CONSTRAINT_ORACLE);
                if (!constraints.contains(constraint)) {
                    constraints.add(constraint);
                }

            }
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return constraints;
    }

    /**
     * Gets the indexes for a table.<p>
     *
     * @param dbCon the db connection interface
     * @param tablename the table to get the indexes from
     *
     * @return a list of indexes
     *
     * @throws SQLException if something goes wrong
     */
    private List<String> getIndexes(CmsSetupDb dbCon, String tablename) throws SQLException {

        List<String> indexes = new ArrayList<String>();
        String tableIndex = readQuery(QUERY_SHOW_INDEX);
        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        CmsSetupDBWrapper db = null;
        try {
            db = dbCon.executeSqlStatement(tableIndex, replacer);
            while (db.getResultSet().next()) {
                String index = db.getResultSet().getString(FIELD_INDEX_ORACLE);
                if (!indexes.contains(index)) {
                    indexes.add(index);
                }
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return indexes;
    }
}
