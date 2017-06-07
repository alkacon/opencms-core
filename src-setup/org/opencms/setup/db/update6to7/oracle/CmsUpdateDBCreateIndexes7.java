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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Oracle implementation for the creation of the indexes of the tables in version 7.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBCreateIndexes7 extends org.opencms.setup.db.update6to7.CmsUpdateDBCreateIndexes7 {

    /** Constant for the field of the constraint name.<p> */
    private static final String FIELD_CONSTRAINT = "CONSTRAINT_NAME";

    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX = "INDEX_NAME";

    /** Constant for the sql query to drop the primary key of a table. */
    private static final String QUERY_DROP_CONSTRAINT = "Q_DROP_CONSTRAINT";

    /** Constant for the sql query to drop an index. */
    private static final String QUERY_DROP_INDEX = "Q_DROP_INDEX";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_add_new_indexes_queries.properties";

    /** Constant for the sql query to list contraints for a table. */
    private static final String QUERY_SHOW_CONSTRAINTS = "Q_SHOW_CONSTRAINTS";

    /** Constant for the replacement of the indexname. */
    private static final String REPLACEMENT_INDEXNAME = "${indexname}";

    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBCreateIndexes7()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        List<String> elements = new ArrayList<String>();
        elements.add("CMS_CONTENTS");
        elements.add("CMS_GROUPS");
        elements.add("CMS_GROUPUSERS");
        elements.add("CMS_OFFLINE_ACCESSCONTROL");
        elements.add("CMS_OFFLINE_CONTENTS");
        elements.add("CMS_OFFLINE_PROPERTIES");
        elements.add("CMS_OFFLINE_PROPERTYDEF");
        elements.add("CMS_OFFLINE_RESOURCES");
        elements.add("CMS_OFFLINE_STRUCTURE");
        elements.add("CMS_ONLINE_ACCESSCONTROL");
        elements.add("CMS_ONLINE_PROPERTIES");
        elements.add("CMS_ONLINE_PROPERTYDEF");
        elements.add("CMS_ONLINE_RESOURCES");
        elements.add("CMS_ONLINE_STRUCTURE");
        elements.add("CMS_PROJECTRESOURCES");
        elements.add("CMS_PROJECTS");
        elements.add("CMS_PUBLISH_HISTORY");
        elements.add("CMS_STATICEXPORT_LINKS");
        elements.add("CMS_USERS");

        // iterate the queries
        for (Iterator<String> it = elements.iterator(); it.hasNext();) {
            String tablename = it.next();

            // Check if the table exists
            if (dbCon.hasTableOrColumn(tablename, null)) {

                // Drop the constraints
                try {
                    List<String> constraints = getConstraintsTopDrop(dbCon, tablename);
                    Iterator<String> iter = constraints.iterator();
                    while (iter.hasNext()) {
                        String constraint = iter.next();

                        String dropConstraint = readQuery(QUERY_DROP_CONSTRAINT);
                        Map<String, String> replacer = new HashMap<String, String>();
                        replacer.put(REPLACEMENT_TABLENAME, tablename);
                        replacer.put(REPLACEMENT_INDEXNAME, constraint);
                        dbCon.updateSqlStatement(dropConstraint, replacer, null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // Drop the indexes
                try {
                    List<String> indexes = getIndexesToDrop(dbCon, tablename);
                    Iterator<String> iter = indexes.iterator();
                    while (iter.hasNext()) {
                        String index = iter.next();

                        // Drop the index
                        String dropIndex = readQuery(QUERY_DROP_INDEX);
                        Map<String, String> replacerIndex = new HashMap<String, String>();
                        replacerIndex.put(REPLACEMENT_INDEXNAME, index);
                        dbCon.updateSqlStatement(dropIndex, replacerIndex, null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Table " + tablename + "does not exist.");
            }
        }
        // Create the new indexes for the table
        createNewIndexes(dbCon);
    }

    /**
     * Creates the new indexes for the given table.<p>
     *
     * @param dbCon the connection to the database
     */
    private void createNewIndexes(CmsSetupDb dbCon) {

        List<String> indexElements = new ArrayList<String>();
        indexElements.add("CMS_CONTENTS_PRIMARY_KEY");
        indexElements.add("CMS_CONTENTS_01_IDX_INDEX");
        indexElements.add("CMS_CONTENTS_02_IDX_INDEX");
        indexElements.add("CMS_CONTENTS_03_IDX_INDEX");
        indexElements.add("CMS_CONTENTS_04_IDX_INDEX");
        indexElements.add("CMS_CONTENTS_05_IDX_INDEX");
        indexElements.add("CMS_GROUPS_PRIMARY_KEY");
        indexElements.add("CMS_GROUPS_UNIQUE_KEY_GROUPS");
        indexElements.add("CMS_GROUPS_01_IDX_INDEX");
        indexElements.add("CMS_GROUPS_02_IDX_INDEX");
        indexElements.add("CMS_GROUPS_03_IDX_INDEX");
        indexElements.add("CMS_GROUPUSERS_PRIMARY_KEY");
        indexElements.add("CMS_GROUPUSERS_01_IDX_INDEX");
        indexElements.add("CMS_GROUPUSERS_02_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_ACCESSCONTROL_PRIMARY_KEY");
        indexElements.add("CMS_OFFLINE_ACCESSCONTROL_01_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_ACCESSCONTROL_02_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_CONTENTS_PRIMARY_KEY");
        indexElements.add("CMS_OFFLINE_PROPERTIES_PRIMARY_KEY");
        indexElements.add("CMS_OFFLINE_PROPERTIES_UNIQUE_KEY_PROPERTIES");
        indexElements.add("CMS_OFFLINE_PROPERTIES_01_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_PROPERTIES_02_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_PROPERTYDEF_PRIMARY_KEY");
        indexElements.add("CMS_OFFLINE_PROPERTYDEF_UNIQUE_KEY_PROPERTYDEF");
        indexElements.add("CMS_OFFLINE_RESOURCES_PRIMARY_KEY");
        indexElements.add("CMS_OFFLINE_RESOURCES_01_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_RESOURCES_02_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_RESOURCES_03_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_RESOURCES_04_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_RESOURCES_05_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_PRIMARY_KEY");
        indexElements.add("CMS_OFFLINE_STRUCTURE_01_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_02_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_03_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_04_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_05_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_06_IDX_INDEX");
        indexElements.add("CMS_OFFLINE_STRUCTURE_07_IDX_INDEX");
        indexElements.add("CMS_ONLINE_ACCESSCONTROL_PRIMARY_KEY");
        indexElements.add("CMS_ONLINE_ACCESSCONTROL_01_IDX_INDEX");
        indexElements.add("CMS_ONLINE_ACCESSCONTROL_02_IDX_INDEX");
        indexElements.add("CMS_ONLINE_PROPERTIES_PRIMARY_KEY");
        indexElements.add("CMS_ONLINE_PROPERTIES_UNIQUE_KEY_PROPERTIES");
        indexElements.add("CMS_ONLINE_PROPERTIES_01_IDX_INDEX");
        indexElements.add("CMS_ONLINE_PROPERTIES_02_IDX_INDEX");
        indexElements.add("CMS_ONLINE_PROPERTYDEF_PRIMARY_KEY");
        indexElements.add("CMS_ONLINE_PROPERTYDEF_UNIQUE_KEY_PROPERTYDEF");
        indexElements.add("CMS_ONLINE_RESOURCES_PRIMARY_KEY");
        indexElements.add("CMS_ONLINE_RESOURCES_01_IDX_INDEX");
        indexElements.add("CMS_ONLINE_RESOURCES_02_IDX_INDEX");
        indexElements.add("CMS_ONLINE_RESOURCES_03_IDX_INDEX");
        indexElements.add("CMS_ONLINE_RESOURCES_04_IDX_INDEX");
        indexElements.add("CMS_ONLINE_RESOURCES_05_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_PRIMARY_KEY");
        indexElements.add("CMS_ONLINE_STRUCTURE_01_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_02_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_03_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_04_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_05_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_06_IDX_INDEX");
        indexElements.add("CMS_ONLINE_STRUCTURE_07_IDX_INDEX");
        indexElements.add("CMS_PROJECTRESOURCES_PRIMARY_KEY");
        indexElements.add("CMS_PROJECTRESOURCES_01_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_PRIMARY_KEY");
        indexElements.add("CMS_PROJECTS_UNIQUE_KEY_PROJECTS");
        indexElements.add("CMS_PROJECTS_01_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_02_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_03_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_04_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_05_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_06_IDX_INDEX");
        indexElements.add("CMS_PROJECTS_07_IDX_INDEX");
        indexElements.add("CMS_PUBLISH_HISTORY_PRIMARY_KEY");
        indexElements.add("CMS_PUBLISH_HISTORY_01_IDX_INDEX");
        indexElements.add("CMS_STATICEXPORT_LINKS_PRIMARY_KEY");
        indexElements.add("CMS_STATICEXPORT_LINKS_01_IDX_INDEX");
        indexElements.add("CMS_USERS_PRIMARY_KEY");
        indexElements.add("CMS_USERS_UNIQUE_KEY_USERS");
        indexElements.add("CMS_USERS_01_IDX_INDEX");
        indexElements.add("CMS_USERS_02_IDX_INDEX");

        String indexTablespace = m_poolData.get("indexTablespace");

        // Create the indexes
        for (Iterator<String> createIndexes = indexElements.iterator(); createIndexes.hasNext();) {
            String queryToRead = createIndexes.next();
            String query = readQuery(queryToRead);
            try {
                Map<String, String> replacer = new HashMap<String, String>();
                replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);
                // Create the index
                dbCon.updateSqlStatement(query, replacer, null);
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
    private List<String> getConstraintsTopDrop(CmsSetupDb dbCon, String tablename) throws SQLException {

        List<String> constraints = new ArrayList<String>();
        String tableConstraints = readQuery(QUERY_SHOW_CONSTRAINTS);
        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        CmsSetupDBWrapper db = null;
        try {
            db = dbCon.executeSqlStatement(tableConstraints, replacer);
            while (db.getResultSet().next()) {
                String constraint = db.getResultSet().getString(FIELD_CONSTRAINT);
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
     * Returns the list of the indexes that shall be dropped before adding the final new indexes.<p>
     *
     * @param dbCon the connection to the database
     * @param tablename the table to drop the indexes from
     *
     * @return the list of indexes to drop
     */
    private List<String> getIndexesToDrop(CmsSetupDb dbCon, String tablename) {

        List<String> indexes = new ArrayList<String>();
        String tableIndex = readQuery(QUERY_SHOW_INDEX);
        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        CmsSetupDBWrapper db = null;
        try {
            db = dbCon.executeSqlStatement(tableIndex, replacer);
            while (db.getResultSet().next()) {
                String index = db.getResultSet().getString(FIELD_INDEX);
                if (!indexes.contains(index)) {
                    indexes.add(index);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return indexes;
    }

}
