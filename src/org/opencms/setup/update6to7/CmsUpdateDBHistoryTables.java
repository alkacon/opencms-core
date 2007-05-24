/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBHistoryTables.java,v $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsPropertyUtils;

/**
 * This class converts the backup tables to history tables.<p>
 * 
 * The following tables are converted
 * CMS_BACKUP_PROJECTRESOURCES
 * CMS_BACKUP_PROPERTIES
 * CMS_BACKUP_PROPERTYDEF
 * CMS_BACKUP_RESOURCES
 * CMS_BACKUP_STRUCTURE
 * 
 * The tables CMS_HISTORY_PRINCIPALS and CMS_HISTORY_PROJECTS are created in other classes.
 * 
 * CMS_HISTORY_PRINCIPALS is a completely new table and is therefor handled by its own class.
 * 
 * CMS_HISTORY_PROJECTS needs extra conversion beyond the execution of SQL statements and is
 * also handled by a special class.
 * 
 * @author metzler
 *
 */
public class CmsUpdateDBHistoryTables {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "/update/sql/cms_history_queries.properties";

    /** Constant for the sql query to count the contents of a table.<p> */
    private static final String QUERY_SELECT_COUNT_HISTORY_TABLE = "Q_SELECT_COUNT_HISTORY_TABLE";

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
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBHistoryTables(CmsSetupDb dbcon, String rfsPath)
    throws IOException {

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
     * Gets the sql statements in the extended properties.<p> 
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
     * Sets the sql statements for the extended properties.<p>
     * 
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(ExtendedProperties queryProperties) {

        m_queryProperties = queryProperties;
    }

    /**
     * Transfers the data from the CMS_BACKUP* tables to the CMS_HISTORY* tables.<p> 
     * 
     * @throws SQLException if something goes wrong
     *
     */
    public void transferBackupToHistoryTables() throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query;

        Set elements = m_queryProperties.entrySet();

        for (Iterator it = elements.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            if (m_dbcon.hasTableOrColumn((String)entry.getKey(), null)) {
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, entry.getKey());
                ResultSet set = m_dbcon.executeSqlStatement(
                    (String)m_queryProperties.get(QUERY_SELECT_COUNT_HISTORY_TABLE),
                    replacer);
                boolean update = false;
                if (set.next()) {
                    if (set.getInt("COUNT") <= 0) {
                        update = true;
                    }
                }
                set.close();
                if (update) {
                    query = (String)entry.getValue();
                    m_dbcon.updateSqlStatement(query, null, null);
                }
            } else {
                System.out.println("table " + entry.getKey() + " does not exists");
            }
        }
    }
}
