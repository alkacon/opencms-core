/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/generic/Attic/CmsUpdateDBContentTables.java,v $
 * Date   : $Date: 2007/05/25 11:54:08 $
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

package org.opencms.setup.update6to7.generic;

import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.update6to7.A_CmsUpdateDBPart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * This class creates the table CMS_CONTENTS and fills it with data from the tables CMS_BACKUP_CONTENTS and CMS_ONLINE_CONTENTS.<p>
 *
 * @author metzler
 */
public class CmsUpdateDBContentTables extends A_CmsUpdateDBPart {

    /** Constant for the sql query to create the CMS_CONTENTS table.<p> */
    private static final String QUERY_CREATE_CMS_CONTENTS_TABLE = "Q_CREATE_CMS_CONTENTS_TABLE";

    /** Constant for the sql query to drop a table.<p> */
    private static final String QUERY_DROP_TABLE = "Q_DROP_TABLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_content_table_queries.properties";

    /** Constant for the sql query to transfer the backup contents.<p> */
    private static final String QUERY_TRANSFER_BACKUP_CONTENTS = "Q_TRANSFER_BACKUP_CONTENTS";

    /** Constant for the sql query to transfer the online contents.<p> */
    private static final String QUERY_TRANSFER_ONLINE_CONTENTS = "Q_TRANSFER_ONLINE_CONTENTS";

    /** Constant for the replacement in the SQL query for the tablename.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the table CMS_BACKUP_CONTENTS.<p> */
    private static final String TABLE_CMS_BACKUP_CONTENTS = "CMS_BACKUP_CONTENTS";

    /** Constant for the tbale CMS_ONLINE_CONTENTS.<p> */
    private static final String TABLE_CMS_ONLINE_CONTENTS = "CMS_ONLINE_CONTENTS";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBContentTables()
    throws IOException {

        super();
    }

    /**
     * @see org.opencms.setup.update6to7.I_CmsUpdateDBPart#getSqlQueriesFile()
     */
    public String getSqlQueriesFile() {

        return QUERY_PROPERTY_FILE;
    }

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        // Create the CMS_CONTENTS table if it does not exist yet. 
        // The database checks if the table exists before creating it.
        String query = readQuery(QUERY_CREATE_CMS_CONTENTS_TABLE);
        dbCon.updateSqlStatement(query, null, null);

        // Transfer the online contents if the table exists
        if (dbCon.hasTableOrColumn(TABLE_CMS_ONLINE_CONTENTS, null)) {
            query = readQuery(QUERY_TRANSFER_ONLINE_CONTENTS);
            dbCon.updateSqlStatement(query, null, null);
        } else {
            System.out.println("no table " + TABLE_CMS_ONLINE_CONTENTS + " found");
        }

        // Transfer the backup contents if the table exists
        if (dbCon.hasTableOrColumn(TABLE_CMS_BACKUP_CONTENTS, null)) {
            query = readQuery(QUERY_TRANSFER_BACKUP_CONTENTS);
            dbCon.updateSqlStatement(query, null, null);
        } else {
            System.out.println("no table " + TABLE_CMS_BACKUP_CONTENTS + " found");
        }

        // Drop the tables CMS_BACKUP_CONTENTS and CMS_ONLINE_CONTENTS
        cleanUpContentsTables(dbCon);
    }

    /**
     * After the transfer the tables CMS_ONLINE_CONTENTS and CMS_BACKUP contents are dropped as they are no longer needed.<p>
     *  
     * @param dbCon the db connection interface
     * @throws SQLException if something goes wrong 
     */
    private void cleanUpContentsTables(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String query = readQuery(QUERY_DROP_TABLE);
        HashMap replacers = new HashMap();
        // Drop the CMS_ONLINE_CONTENTS table
        replacers.put(REPLACEMENT_TABLENAME, TABLE_CMS_ONLINE_CONTENTS);
        dbCon.updateSqlStatement(query, replacers, null);

        replacers.clear();
        // Drop the CMS_BACKUP_CONTENTS table
        replacers.put(REPLACEMENT_TABLENAME, TABLE_CMS_BACKUP_CONTENTS);
        dbCon.updateSqlStatement(query, replacers, null);
    }
}
