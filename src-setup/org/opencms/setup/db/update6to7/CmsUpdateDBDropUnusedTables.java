/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.db.update6to7;

import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.db.A_CmsUpdateDBPart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * @since 7.0.0
 */
public class CmsUpdateDBDropUnusedTables extends A_CmsUpdateDBPart {

    /** Constant for the SQL query to drop a table.<p> */
    private static final String QUERY_DROP_TABLE = "Q_DROP_TABLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_drop_unused_tables_queries.properties";

    /** Constant for the replacement of the tablename in the sql query.<p> */
    private static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant array with the unused tables.<p> */
    private static final String[] UNUSED_TABLES = {
        "CMS_SYSTEMID",
        "CMS_TASK",
        "CMS_TASKLOG",
        "CMS_TASKPAR",
        "CMS_TASKTYPE",
        "TEMP_PROJECT_UUIDS"};

    /** Constant ArrayList of the unused tables that are to be dropped.<p> */
    private static final List<String> UNUSED_TABLES_LIST = Collections.unmodifiableList(Arrays.asList(UNUSED_TABLES));

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBDropUnusedTables()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        for (Iterator<String> it = UNUSED_TABLES_LIST.iterator(); it.hasNext();) {
            String table = it.next();
            // Check if the table to drop exists
            if (dbCon.hasTableOrColumn(table, null)) {
                Map<String, String> replacer = new HashMap<String, String>();
                replacer.put(REPLACEMENT_TABLENAME, table);
                dbCon.updateSqlStatement(readQuery(QUERY_DROP_TABLE), replacer, null);
            } else {
                System.out.println("table " + table + " not found");
            }
        }
    }
}
