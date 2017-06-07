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

package org.opencms.setup.db.update6to7.mysql;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * This class creates the table CMS_CONTENTS and fills it with data from the tables CMS_BACKUP_CONTENTS and CMS_ONLINE_CONTENTS.<p>
 */
public class CmsUpdateDBContentTables extends org.opencms.setup.db.update6to7.CmsUpdateDBContentTables {

    /** Constant for the sql query to create the CMS_CONTENTS table.<p> */
    private static final String QUERY_CREATE_CMS_CONTENTS_TABLE_MYSQL = "Q_CREATE_CMS_CONTENTS_TABLE_MYSQL";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_content_table_queries.properties";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBContentTables()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * Creates the CMS_CONTENTS table if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    @Override
    protected void createContentsTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(TABLE_CMS_CONTENTS, null)) {
            String query = readQuery(QUERY_CREATE_CMS_CONTENTS_TABLE_MYSQL);
            Map<String, String> replacer = Collections.singletonMap("${tableEngine}", m_poolData.get("engine"));
            dbCon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("table " + TABLE_CMS_CONTENTS + " already exists");
        }
    }
}
