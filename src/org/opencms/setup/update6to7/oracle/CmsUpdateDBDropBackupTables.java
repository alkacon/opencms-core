/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/oracle/Attic/CmsUpdateDBDropBackupTables.java,v $
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
import java.util.Iterator;

import org.opencms.setup.CmsSetupDb;

/**
 * Oracle implementation of the generic class to drop the backup tables from the database.<p>
 * 
 * @author Roland Metzler
 *
 */
public class CmsUpdateDBDropBackupTables extends org.opencms.setup.update6to7.generic.CmsUpdateDBDropBackupTables {

    /** Constant for the sql query to drop a table.<p> */
    private static final String QUERY_DROP_TABLE_ORACLE = "Q_DROP_TABLE_ORACLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "oracle/cms_drop_backup_tables_queries.properties";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBDropBackupTables()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String dropQuery = readQuery(QUERY_DROP_TABLE_ORACLE);
        for (Iterator it = BACKUP_TABLES_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            HashMap replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, table);
            try {
                if (dbCon.hasTableOrColumn(table, null)) {
                    dbCon.updateSqlStatement(dropQuery, replacer, null);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
