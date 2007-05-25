/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBDropOldIndexes.java,v $
 * Date   : $Date: 2007/05/25 08:02:43 $
 * Version: $Revision: 1.1.2.4 $
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
 * This class drops all indexes of each table of the database.<p> 
 * This is done so that the indexes can be updated to the version 6.2.3 and afterwards to version 7
 * 
 * @author metzler
 *
 */
public class CmsUpdateDBDropOldIndexes {

    /** Constant array of the base tables of the OpenCms 6.2.3 installation.<p> */
    private static final String[] CMS_TABLES = {
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

    /** Constant ArrayList of the tables of the base OpenCms 6.2.3 installation.<p> */
    private static final List CMS_TABLES_LIST = Collections.unmodifiableList(Arrays.asList(CMS_TABLES));

    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX = "KEY_NAME";

    /** Constant for the primary key of a the index result set.<p> */
    private static final String PRIMARY_KEY = "PRIMARY";

    /** Constant for the sql query to drop an index from a table.<p> */
    private static final String QUERY_DROP_INDEX = "Q_DROP_INDEX";

    /** Constant for the sql query to drop the primary key.<p> */
    private static final String QUERY_DROP_PRIMARY_KEY = "Q_DROP_PRIMARY_KEY";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_drop_all_indexes_queries.properties";

    /** Constant for the sql query to show the indexes of a table.<p> */
    private static final String QUERY_SHOW_INDEX = "Q_SHOW_INDEXES";

    /** Constant for the sql query replacement of the index.<p> */
    private static final String REPLACEMENT_INDEX = "${index}";

    /** Constant for the sql query replacement of the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

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
     * @throws IOException if the query properties cannot be read
     * 
     */
    public CmsUpdateDBDropOldIndexes(CmsSetupDb dbcon, String rfsPath)
    throws IOException {

        m_dbcon = dbcon;
        m_queryProperties = CmsPropertyUtils.loadProperties(rfsPath + QUERY_PROPERTY_FILE);

    }

    /** 
     * Drops all the indexes from the database tables.<p>
     * 
     * @throws SQLException if something goes wrong 
     *
     */
    public void dropAllIndexes() throws SQLException {

        List tablenames = CMS_TABLES_LIST;

        // Iterate over all the tables.
        for (Iterator tableIterator = tablenames.iterator(); tableIterator.hasNext();) {
            String tablename = (String)tableIterator.next();
            // Check if the table is existing
            if (m_dbcon.hasTableOrColumn(tablename, null)) {
                List indexes = getIndexes(tablename);
                // Iterate over the indexes of one table
                for (Iterator indexIterator = indexes.iterator(); indexIterator.hasNext();) {
                    String indexname = (String)indexIterator.next();
                    String dropIndexQuery = (String)m_queryProperties.get(QUERY_DROP_INDEX);
                    HashMap replacer = new HashMap();
                    replacer.put(REPLACEMENT_TABLENAME, tablename);
                    replacer.put(REPLACEMENT_INDEX, indexname);
                    // Drop the index
                    try {
                        m_dbcon.updateSqlStatement(dropIndexQuery, replacer, null);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                }
                indexes.clear(); // Clear the indexes for the next loop

                // Drop the primary key
                String dropPrimaryKey = (String)m_queryProperties.get(QUERY_DROP_PRIMARY_KEY);
                HashMap primaryKeyReplacer = new HashMap();
                primaryKeyReplacer.put(REPLACEMENT_TABLENAME, tablename);
                try {
                    m_dbcon.updateSqlStatement(dropPrimaryKey, primaryKeyReplacer, null);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
            }

        }
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
     * Sets the database properties.<p>
     * 
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(ExtendedProperties queryProperties) {

        m_queryProperties = queryProperties;
    }

    /**
     * Gets the indexes for a table.<p>
     * 
     * @param tablename the table to get the indexes from
     * 
     * @return a list of indexes
     * 
     * @throws SQLException if somehting goes wrong 
     */
    private List getIndexes(String tablename) throws SQLException {

        List indexes = new ArrayList();
        String tableIndex = (String)m_queryProperties.get(QUERY_SHOW_INDEX);
        HashMap replacer = new HashMap();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        ResultSet set = m_dbcon.executeSqlStatement(tableIndex, replacer);
        while (set.next()) {
            String index = set.getString(FIELD_INDEX);
            // Drop all indexes
            if (!index.equals(PRIMARY_KEY)) {
                if (!indexes.contains(index)) {
                    indexes.add(index);
                }
            }

        }
        set.close();
        return indexes;
    }

}
