/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/generic/Attic/CmsUpdateDBHistoryTables.java,v $
 * Date   : $Date: 2007/06/04 12:00:33 $
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

package org.opencms.setup.update6to7.generic;

import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.update6to7.A_CmsUpdateDBPart;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
public class CmsUpdateDBHistoryTables extends A_CmsUpdateDBPart {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "generic/cms_history_queries.properties";

    /** Constant for the sql query to count the contents of a table.<p> */
    private static final String QUERY_SELECT_COUNT_HISTORY_TABLE = "Q_SELECT_COUNT_HISTORY_TABLE";

    /** Constant for the replacement of the tablename in the sql query. */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBHistoryTables()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        List elements = new ArrayList();
        elements.add("CMS_HISTORY_PROJECTRESOURCES");
        elements.add("CMS_HISTORY_PROPERTIES");
        elements.add("CMS_HISTORY_PROPERTYDEF");
        elements.add("CMS_HISTORY_RESOURCES");
        elements.add("CMS_HISTORY_STRUCTURE");

        for (Iterator it = elements.iterator(); it.hasNext();) {
            String table = (String)it.next();
            System.out.println("Updating table " + table);
            if (dbCon.hasTableOrColumn(table, null)) {
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLENAME, table);
                ResultSet set = dbCon.executeSqlStatement(readQuery(QUERY_SELECT_COUNT_HISTORY_TABLE), replacer);
                boolean update = false;
                if (set.next()) {
                    if (set.getInt("COUNT") <= 0) {
                        update = true;
                    }
                }
                set.close();
                if (update) {
                    String query = readQuery(table);
                    dbCon.updateSqlStatement(query, null, null);
                } else {
                    System.out.println("table " + table + " already has data");
                }
            } else {
                System.out.println("table " + table + " does not exists");
            }
        }
    }
}
