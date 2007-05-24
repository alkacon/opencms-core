/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBIndexUpdater.java,v $
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;

/**
 * This class updates the indexes of some of the tables in the database.<p> 
 * 
 * These tables are
 * CMS_GROUPS
 * CMS_PROJECTS
 * CMS_PUBISH_HISTORY
 * 
 * @author metzler
 */
public class CmsUpdateDBIndexUpdater {

    /** Constant HashMap with the columns for each new index for the CMS_USERS table.<p> */
    private static final HashMap INDEX_CMS_GROUPS_MAP;

    /** Constant array of the indexes to drop in the CMS_PROJECTS table.<p> */
    private static final String[] INDEX_CMS_PROJECTS_DROP = {
        "PROJECT_NAME_DATE_CREATED_IDX",
        "MANAGERGROUP_ID_IDX",
        "GROUP_ID_IDX",
        "USER_ID_IDX",
        "TASK_ID_IDX"};

    /** Arraylist for the tables that shall be updated.<p> */
    private static final List INDEX_CMS_PROJECTS_DROP_LIST = Collections.unmodifiableList(Arrays.asList(INDEX_CMS_PROJECTS_DROP));

    /** Constant HashMap with the columns for each new index for the CMS_PROJECTS table.<p> */
    private static final HashMap INDEX_CMS_PROJECTS_MAP;

    /** Constant HashMap with the columns for each new index for the CMS_PUBLISH_HISTORY table.<p> */
    private static final HashMap INDEX_CMS_PUBLISH_HISTORY_MAP;

    /** Constant for the sql query to add an index.<p> */
    private static final String QUERY_ADD_INDEX = "Q_ADD_INDEX";

    /** Constant for the sql query to drop an index.<p> */
    private static final String QUERY_DROP_INDEX = "Q_DROP_INDEX";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_update_index_queries.properties";

    /** Constant for the sql query to update the length of the project name in the CMS_PROJECTS table.<p> */
    private static final String QUERY_UPDATE_PROJECT_NAME_SIZE = "Q_UPDATE_PROJECT_NAME_SIZE";

    /** Constant for the replacement of the indexcolumns.<p> */
    private static final String REPLACEMENT_INDEXCOLUMNS = "${indexcolumns}";

    /** Constant for the replacement of the indexname.<p> */
    private static final String REPLACEMENT_INDEXNAME = "${indexname}";

    /** Constant for the replacement of the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the table CMS_GROUPS.<p> */
    private static final String TABLE_CMS_GROUPS = "CMS_GROUPS";

    /** Constant for the table CMS_PROEJCTS.<p> */
    private static final String TABLE_CMS_PROJECTS = "CMS_PROJECTS";

    /** Constant for the table CMS_PUBLISH_HISTORY.<p> */
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
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBIndexUpdater(CmsSetupDb dbcon, String rfsPath)
    throws IOException {

        System.out.println(getClass().getName());
        m_dbcon = dbcon;
        m_queryProperties = CmsPropertyUtils.loadProperties(rfsPath + QUERY_PROPERTY_FILE);
    }

    // Initialize the Hash Maps
    static {
        // The map for the CMS_PROJECTS table
        INDEX_CMS_PROJECTS_MAP = new HashMap();
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_NAME_DATE_CREATED_IDX", "PROJECT_OU,PROJECT_NAME,DATE_CREATED");
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_MANAGERGROUP_IDX", "MANAGERGROUP_ID");
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_OU_NAME_IDX", "PROJECT_OU,PROJECT_NAME");
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_NAME_IDX", "PROJECT_NAME");
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_OU_IDX", "PROJECT_OU");
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_USER_ID_IDX", "USER_ID");
        INDEX_CMS_PROJECTS_MAP.put("PROJECT_GROUP_ID_IDX", "GROUP_ID");

        // The map for the CMS_PUBLISH_HISTORY table
        INDEX_CMS_PUBLISH_HISTORY_MAP = new HashMap();
        INDEX_CMS_PUBLISH_HISTORY_MAP.put("HISTORY_ID_IDX", "HISTORY_ID");

        // The map for the CMS_USERS table
        INDEX_CMS_GROUPS_MAP = new HashMap();
        INDEX_CMS_GROUPS_MAP.put("GROUP_FQN_IDX", "GROUP_OU,GROUP_NAME");
        INDEX_CMS_GROUPS_MAP.put("GROUP_OU_IDX", "GROUP_OU");
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
     * Gets the query properties.<p> 
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
     * Sets the query properties.<p>
     * 
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(ExtendedProperties queryProperties) {

        m_queryProperties = queryProperties;
    }

    /**
     * Updates the indexes of the tables.<p>
     * 
     * @throws SQLException if something goes wrong 
     *
     */
    public void updateIndexes() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        /*
         * Update of the CMS_PROJECTS table
         */
        dropIndexes(TABLE_CMS_PROJECTS, INDEX_CMS_PROJECTS_DROP_LIST);
        updateProjectNameSize(); // Update the project name size from 255 to 200
        createIndexes(TABLE_CMS_PROJECTS, INDEX_CMS_PROJECTS_MAP);

        /*
         * Update of the CMS_GROUPS table
         */
        createIndexes(TABLE_CMS_GROUPS, INDEX_CMS_GROUPS_MAP);

        /*
         * Update of the CMS_PUBLISH_HISTORY table
         */
        createIndexes(TABLE_CMS_PUBLISH_HISTORY, INDEX_CMS_PUBLISH_HISTORY_MAP);
    }

    /**y
     * Creates new indexes in the given table.<p>
     * 
     * @param tablename the table where the indexes are created
     * @param indexes the indexes to create
     * 
     * @throws SQLException if something goes wrong
     */
    private void createIndexes(String tablename, HashMap indexes) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Create the new indexes
        String createIndex = (String)m_queryProperties.get(QUERY_ADD_INDEX);
        Set set = indexes.entrySet();
        for (Iterator it = set.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            if (entry.getKey() != null && entry.getValue() != null) {
                Map replacers = new HashMap();
                replacers.put(REPLACEMENT_TABLENAME, tablename);
                replacers.put(REPLACEMENT_INDEXNAME, entry.getKey());
                replacers.put(REPLACEMENT_INDEXCOLUMNS, entry.getValue());
                m_dbcon.updateSqlStatement(createIndex, replacers, null);
            }
        }
    }

    /**
     * Drops the list of given indexes in the given table.<p>
     *  
     * @param tablename the table where the indexes shall be dropped
     * @param indexes the list of indexes to drop
     * 
     * @throws SQLException if something goes wrong 
     */
    private void dropIndexes(String tablename, List indexes) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Drop the old indexes
        String dropIndex = (String)m_queryProperties.get(QUERY_DROP_INDEX);
        for (Iterator it = indexes.iterator(); it.hasNext();) {
            String index = (String)it.next();
            Map replacers = new HashMap();
            replacers.put(REPLACEMENT_TABLENAME, tablename);
            replacers.put(REPLACEMENT_INDEXNAME, index);
            m_dbcon.updateSqlStatement(dropIndex, replacers, null);
        }
    }

    /**
     * Changes the length of the project name from 255 chars to 200 chars in the CMS_PROJECTS table.<p>
     * 
     * @throws SQLException if something goes wrong 
     *
     */
    private void updateProjectNameSize() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = (String)m_queryProperties.get(QUERY_UPDATE_PROJECT_NAME_SIZE);
        m_dbcon.updateSqlStatement(query, null, null);
    }

}
