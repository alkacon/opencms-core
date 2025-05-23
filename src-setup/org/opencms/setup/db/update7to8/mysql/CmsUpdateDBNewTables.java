/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.db.update7to8.mysql;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class creates the new tables for the database version of OpenCms 8.<p>
 *
 * The new tables in OpenCms 8 are:
 * <ul>
 * <li><code>CMS_LOG</code></li>
 * </ul>
 *
 * @since 8.0.0
 */
public class CmsUpdateDBNewTables extends org.opencms.setup.db.update7to8.CmsUpdateDBNewTables {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_new_tables_queries.properties";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBNewTables()
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

        List<String> elements = Arrays.asList(
            new String[] {
                "CMS_LOG",
                "CMS_COUNTERS",
                "CMS_OFFLINE_URLNAME_MAPPINGS",
                "CMS_ONLINE_URLNAME_MAPPINGS",
                "CMS_SUBSCRIPTION",
                "CMS_SUBSCRIPTION_VISIT",
                "CMS_ALIASES",
                "CMS_REWRITES",
                "CMS_USER_PUBLISH_LIST"});

        Map<String, String> replacer = Collections.singletonMap("${tableEngine}", m_poolData.get("engine"));
        for (String table : elements) {
            if (!dbCon.hasTableOrColumn(table, null)) {
                String query = readQuery(table + "_MYSQL");
                dbCon.updateSqlStatement(query, replacer, null);
            } else {
                System.out.println("table " + table + " already exists");
            }
        }
    }
}
