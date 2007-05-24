/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBDropOldIndexes.java,v $
 * Date   : $Date: 2007/05/24 15:10:51 $
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

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_drop_all_indexes_queries.properties";
    
    /** Constant for the sql query to show the indexes of a table.<p> */
    private static final String QUERY_SHOW_INDEX = "Q_SHOW_INDEXES";
    
    /** Constant for the sql query to drop an index from a table.<p> */
    private static final String QUERY_DROP_INDEX = "Q_DROP_INDEX";
    
    /** Constant for the sql query to show all the tables of the database.<p> */
    private static final String QUERY_SHOW_TABLES = "Q_SHOW_TABLES";
    
    /** Constant for the Primary index.<p> */
    private static final String PRIMARY_KEY_NAME = "PRIMARY";
    
    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX = "KEY_NAME";
    
    /** Constant for the sql query replacement of the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";
    
    /** Constant for the sql query replacement of the index.<p> */
    private static final String REPLACEMENT_INDEX = "${index}";
    
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
     * Gets the database connection.<p> 
     * 
     * @return the dbcon
     */
    public CmsSetupDb getDbcon() {
    
        return m_dbcon;
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
     * Gets the query properties.<p>
     * 
     * @return the queryProperties
     */
    public ExtendedProperties getQueryProperties() {
    
        return m_queryProperties;
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
     * Drops all the indexes from the database tables.<p>
     * 
     * @throws SQLException if something goes wrong 
     *
     */
    public void dropAllIndexes() throws SQLException {

        List tablenames = getTableNames();
        
        // Iterate over all the tables.
        for (Iterator tableIterator=tablenames.iterator(); tableIterator.hasNext();) {
            String tablename = (String)tableIterator.next();
            List indexes = getIndexes(tablename);
            // Iterate over the indexes of one table
            for (Iterator indexIterator = indexes.iterator(); indexIterator.hasNext();) {
                String indexname = (String)indexIterator.next();
                String dropIndexQuery = (String)m_queryProperties.get(QUERY_DROP_INDEX);
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, tablename);
                replacer.put(REPLACEMENT_INDEX, indexname);
                // Drop the index
                m_dbcon.updateSqlStatement(dropIndexQuery, replacer, null);
            }
            indexes.clear();    // Clear the indexes for the next loop
        }
        
    }
    
    /**
     * Gets the tablenames of the database.<p> 
     * 
     * @return a list of tablenames
     * 
     * @throws SQLException if somehting goes wrong 
     */
    private List getTableNames() throws SQLException {
        List tablenames = new ArrayList();
        // Get the tables of the database
        String showTables = (String)m_queryProperties.get(QUERY_SHOW_TABLES);
        ResultSet tables = m_dbcon.executeSqlStatement(showTables, null);
        // Get the tablenames
        while (tables.next()) {
            tablenames.add(tables.getString(1));
        }
        tables.close();
        
        return tablenames;
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
            // Check to not to add the primary key
            if (!index.equalsIgnoreCase(PRIMARY_KEY_NAME)) {
                if (!indexes.contains(index)) {
                    indexes.add(index);
                }

            }
        }
        set.close();
        return indexes;
    }
    
}
