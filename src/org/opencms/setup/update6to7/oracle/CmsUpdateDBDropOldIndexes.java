/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/oracle/Attic/CmsUpdateDBDropOldIndexes.java,v $
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencms.setup.CmsSetupDb;

/**
 * Oracle implementation to drop the old indexes from the database.<p> 
 * 
 * @author Roland Metzler
 *
 */
public class CmsUpdateDBDropOldIndexes extends org.opencms.setup.update6to7.generic.CmsUpdateDBDropOldIndexes {

    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX_ORACLE = "INDEX_NAME";

    /** Constant for the field for the uniqueness of an index. */
    private static final String FIELD_INDEX_UNIQUENESS_ORACLE = "UNIQUENESS";

    /** Constant for the sql query to drop an index. */
    private static final String QUERY_DROP_INDEX_ORACLE = "Q_DROP_INDEX_ORACLE";

    /** Constant for the sql query to drop a unique index key. */
    private static final String QUERY_DROP_UNIQUE_INDEX_ORACLE = "Q_DROP_UNIQUE_INDEX_ORACLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE_ORACLE = "oracle/cms_drop_all_indexes_queries.properties";

    /** Constant for the sql query to show the indexes of a table.<p> */
    private static final String QUERY_SHOW_INDEX_ORACLE = "Q_SHOW_INDEXES_ORACLE";

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
    private static final List TEMP_INDEXES_LIST = Collections.unmodifiableList(Arrays.asList(TEMP_INDEXES));

    /** Constant for a unique key of the index result set. */
    private static final String UNIQUE_KEY_ORACLE = "UNIQUE";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBDropOldIndexes()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE_ORACLE);
    }

    // No implementation yet

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) {

        List tablenames = CMS_TABLES_LIST;

        // Iterate over all the tables.
        for (Iterator tableIterator = tablenames.iterator(); tableIterator.hasNext();) {
            String tablename = (String)tableIterator.next();
            System.out.println("dropping indexes for table " + tablename);
            // Check if the table is existing
            if (dbCon.hasTableOrColumn(tablename, null)) {
                try {
                    HashMap indexes = getIndexes(dbCon, tablename);
                    // Drop the indexes from the table.
                    Set indexEntries = indexes.entrySet();

                    for (Iterator indexIt = indexEntries.iterator(); indexIt.hasNext();) {
                        Map.Entry entry = (Map.Entry)indexIt.next();
                        if (entry.getKey() != null && entry.getValue() != null) {
                            String index = (String)entry.getKey();
                            String uniqueness = (String)entry.getValue();

                            // Drop a unique index key (Including primary keys
                            if (UNIQUE_KEY_ORACLE.equals(uniqueness)) {
                                String dropUniqueKey = readQuery(QUERY_DROP_UNIQUE_INDEX_ORACLE);
                                HashMap replacerUnique = new HashMap();
                                replacerUnique.put(REPLACEMENT_TABLENAME, tablename);
                                replacerUnique.put(REPLACEMENT_INDEX_ORACLE, index);
                                dbCon.updateSqlStatement(dropUniqueKey, replacerUnique, null);
                            }

                            else {
                                // Otherwise drop the index
                                String dropIndex = readQuery(QUERY_DROP_INDEX_ORACLE);
                                HashMap replacerIndex = new HashMap();
                                replacerIndex.put(REPLACEMENT_INDEX_ORACLE, index);
                                dbCon.updateSqlStatement(dropIndex, replacerIndex, null);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Create the temporary indexes
        // Each index must be created in its own query
        for (Iterator tempIndexes = TEMP_INDEXES_LIST.iterator(); tempIndexes.hasNext();) {
            try {
                String createIndex = (String)tempIndexes.next();
                String creationQuery = readQuery(createIndex);
                dbCon.updateSqlStatement(creationQuery, null, null);
            } catch (SQLException e) {
                e.printStackTrace();
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
    private HashMap getIndexes(CmsSetupDb dbCon, String tablename) throws SQLException {

        HashMap indexes = new HashMap();
        String tableIndex = readQuery(QUERY_SHOW_INDEX_ORACLE);
        HashMap replacer = new HashMap();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        ResultSet set = dbCon.executeSqlStatement(tableIndex, replacer);
        while (set.next()) {
            String index = set.getString(FIELD_INDEX_ORACLE);
            String unique = set.getString(FIELD_INDEX_UNIQUENESS_ORACLE);
            if (!indexes.containsKey(index)) {
                indexes.put(index, unique);
            }

        }
        set.close();
        return indexes;
    }
}
