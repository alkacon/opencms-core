/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/oracle/Attic/CmsUpdateDBUpdateOU.java,v $
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.opencms.setup.CmsSetupDb;

/**
 * Oracle implementation to update the OUs of of the database.<p>
 * 
 * @author Roland Metzler
 *
 */
public class CmsUpdateDBUpdateOU extends org.opencms.setup.update6to7.generic.CmsUpdateDBUpdateOU {

    /** Constant for the query that adds the ous to the table.<p> */
    private static final String QUERY_ADD_OUS_TO_TABLE_ORACLE = "Q_ADD_OUS_TO_TABLE_ORACLE";

    /** Constant for the alteration of the table.<p> */
    private static final String QUERY_KEY_ALTER_TABLE_ORACLE = "Q_ALTER_TABLE_ADD_OU_COLUMN_ORACLE";
    
    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "oracle/cms_ou_query.properties";
    

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBUpdateOU()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBUpdateOU#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());

        updateOUs(dbCon, TABLE_CMS_USERS, USER_OU_COLUMN);
        updateOUs(dbCon, TABLE_CMS_GROUPS, GROUP_OU_COLUMN);
        updateOUs(dbCon, TABLE_PROJECTS, PROJECT_OU_COLUMN);
        updateOUs(dbCon, TABLE_BACKUP_PROJECTS, PROJECT_OU_COLUMN);
    }

    
    /**
     * Checks if the column USER_OU is found in the resultset.<p>
     * 
     * @param dbCon the db connection interface
     * @param table the table to check
     * @param ouColumn the type of OU to find (e.g. USER_OU or GROUP_OU)
     * 
     * @return true if the column is in the result set, false if not
     */
    private boolean findOUColumn(CmsSetupDb dbCon, String table, String ouColumn) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        return dbCon.hasTableOrColumn(table, ouColumn);
    }
    
    /**
     * Updates the database tables with the new OUs if necessary for the given table.<p>
     * 
     * @param dbCon the db connection interface
     * @param table the table to update
     * @param ouColumn the column to insert
     * 
     * @return true if everything worked fine, false if not
     */
    private int updateOUs(CmsSetupDb dbCon, String table, String ouColumn) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        int result = 1;
        try {

            if (!findOUColumn(dbCon, table, ouColumn)) {
                // Alter the table and add the OUs
                Map replacements = new HashMap();
                replacements.put(REPLACEMENT_TABLENAME, table);
                replacements.put(REPLACEMENT_COLUMNNAME, ouColumn);
                String alterQuery = readQuery(QUERY_KEY_ALTER_TABLE_ORACLE);

                // Update the database and alter the table to add the OUs
                dbCon.updateSqlStatement(alterQuery, replacements, null);

                // Insert the value '/' into the OUs
                String insertQuery = readQuery(QUERY_ADD_OUS_TO_TABLE_ORACLE);
                dbCon.updateSqlStatement(insertQuery, replacements, null);
                result = 0;
            } else {
                System.out.println("column " + ouColumn + " in table " + table + " already exists");
            }
            // Nothing needs to be done
            result = 0;
        } catch (SQLException e) {
            e.printStackTrace();
            result = 1;
        }

        return result;
    }
}
