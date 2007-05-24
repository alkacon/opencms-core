/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBDropUnusedTables.java,v $
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

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;

/**
 * This class drops the outdated tables from the OpenCms database.<p>
 * 
 * These tables are
 * CMS_SYSTEMID
 * CMS_TASK
 * CMS_TASKLOG
 * CMS_TASKPAR
 * CMS_TASKTYPE
 * TEMP_PROJECT_UUIDS
 * 
 * @author metzler
 */
public class CmsUpdateDBDropUnusedTables {
    
    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_drop_unused_tables_queries.properties";
    
    /** Constant for the SQL query to drop a table.<p> */
    private static final String QUERY_DROP_TABLE = "Q_DROP_TABLE";
    
    /** Constant for the replacement of the tablename in the sql query.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";
    
    /** Constant array with the unused tables.<p> */ 
    private static final String[] UNUSED_TABLES = {
        "CMS_SYSTEMID",
        "CMS_TASK",
        "CMS_TASKLOG",
        "CMS_TASKPAR",
        "CMS_TASKTYPE",
        "TEMP_PROJECT_UUIDS"
    };
    
    /** Constant ArrayList of the unused tables that are to be dropped.<p> */
    private static final List UNUSED_TABLES_LIST = Collections.unmodifiableList(Arrays.asList(UNUSED_TABLES));
    
    
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
    public CmsUpdateDBDropUnusedTables(CmsSetupDb dbcon, String rfsPath) throws IOException {

        System.out.println(getClass().getName());
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
     * Gets the sql query properties.<p>
     * 
     * @return the queryProperties
     */
    public ExtendedProperties getQueryProperties() {
    
        return m_queryProperties;
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
     * Drops the unused tables.<p>
     * 
     * @throws SQLException if something goes wrong 
     *
     */
    public void dropUnusedTables() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        for (Iterator it=UNUSED_TABLES_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            // Check if the table to drop exists
            if (m_dbcon.hasTableOrColumn(table, null)) {
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, table);
                m_dbcon.updateSqlStatement((String)m_queryProperties.get(QUERY_DROP_TABLE), replacer, null);
            } else {
                System.out.println("table " + table + " not found");
            }
        }
    }
}
