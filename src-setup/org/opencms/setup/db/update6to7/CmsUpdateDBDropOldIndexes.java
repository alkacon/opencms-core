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
import org.opencms.util.CmsStringUtil;

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
 * This class drops all indexes of each table of the database.<p>
 * This is done so that the indexes can be updated to the version 6.2.3 and afterwards to version 7
 *
 * @since 7.0.0
 */
public class CmsUpdateDBDropOldIndexes extends A_CmsUpdateDBPart {

    /** Constant array of the base tables of the OpenCms 7.0.x installation.<p> */
    protected static final String[] CMS_TABLES = {
        "CMS_BACKUP_CONTENTS",
        "CMS_BACKUP_PROJECTRESOURCES",
        "CMS_BACKUP_PROJECTS",
        "CMS_BACKUP_PROPERTIES",
        "CMS_BACKUP_PROPERTYDEF",
        "CMS_BACKUP_RESOURCES",
        "CMS_BACKUP_STRUCTURE",
        "CMS_GROUPS",
        "CMS_GROUPUSERS",
        "CMS_OFFLINE_ACCESSCONTROL",
        "CMS_OFFLINE_CONTENTS",
        "CMS_OFFLINE_PROPERTIES",
        "CMS_OFFLINE_PROPERTYDEF",
        "CMS_OFFLINE_RESOURCES",
        "CMS_OFFLINE_STRUCTURE",
        "CMS_ONLINE_ACCESSCONTROL",
        "CMS_ONLINE_CONTENTS",
        "CMS_ONLINE_PROPERTIES",
        "CMS_ONLINE_PROPERTYDEF",
        "CMS_ONLINE_RESOURCES",
        "CMS_ONLINE_STRUCTURE",
        "CMS_PROJECTRESOURCES",
        "CMS_PROJECTS",
        "CMS_PUBLISH_HISTORY",
        "CMS_STATICEXPORT_LINKS",
        "CMS_SYSTEMID",
        "CMS_TASK",
        "CMS_TASKLOG",
        "CMS_TASKPAR",
        "CMS_TASKTYPE",
        "CMS_USERS",

    };

    /** Constant ArrayList of the tables of the base OpenCms 7.0.x installation.<p> */
    protected static final List<String> CMS_TABLES_LIST = Collections.unmodifiableList(Arrays.asList(CMS_TABLES));

    /** Constant for the sql query to drop an index from a table.<p> */
    protected static final String QUERY_DROP_INDEX = "Q_DROP_INDEX";

    /** Constant for the sql query to show the indexes of a table.<p> */
    protected static final String QUERY_SHOW_INDEX = "Q_SHOW_INDEXES";

    /** Constant for the sql query replacement of the tablename.<p> */
    protected static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX = "KEY_NAME";

    /** Constant for the primary key of a the index result set.<p> */
    private static final String PRIMARY_KEY = "PRIMARY";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_drop_all_indexes_queries.properties";

    /** Constant for the sql query replacement of the index.<p> */
    private static final String REPLACEMENT_INDEX = "${dropindexes}";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBDropOldIndexes()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

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
                    List<String> indexes = getIndexes(dbCon, tablename);

                    // Iterate over the indexes of one table
                    StringBuffer buffer = new StringBuffer();
                    for (Iterator<String> indexIt = indexes.iterator(); indexIt.hasNext();) {
                        String index = indexIt.next();
                        // Drop the primary key
                        if (index.equalsIgnoreCase(PRIMARY_KEY)) {
                            buffer.append("DROP PRIMARY KEY");
                            if (indexIt.hasNext()) {
                                buffer.append(",");
                            }
                        } else {
                            // Drop the index
                            buffer.append(" DROP INDEX ");
                            buffer.append(index);
                            if (indexIt.hasNext()) {
                                buffer.append(",");
                            }
                        }
                    }
                    String tempIndex = readQuery(tablename);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(tempIndex)) {
                        buffer.append(", ");
                        buffer.append(tempIndex);
                    }

                    // Only execute the query if there is something to change
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(buffer.toString())) {
                        String dropIndexQuery = readQuery(QUERY_DROP_INDEX);
                        Map<String, String> replacer = new HashMap<String, String>();
                        replacer.put(REPLACEMENT_TABLENAME, tablename);
                        replacer.put(REPLACEMENT_INDEX, buffer.toString());
                        // Drop the indexes
                        try {
                            dbCon.updateSqlStatement(dropIndexQuery, replacer, null);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the indexes for a table.<p>
     *
     * @param dbCon the db connection interface
     * @param tablename the table to get the indexes from
     *
     * @return a list of indexes
     *
     * @throws SQLException if somehting goes wrong
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
                String index = db.getResultSet().getString(FIELD_INDEX);

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