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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.db.update7to8.postgresql;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle implementation to create the new tables for version 7 of OpenCms.<p>
 *
 * @since 7.0.0
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
        Map<String, List<String>> elements = new LinkedHashMap<String, List<String>>();
        List<String> indexes = new ArrayList<String>();
        elements.put("CMS_LOG", indexes);
        indexes.add("CREATE_INDEX_CMS_LOG_01_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_02_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_03_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_04_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_05_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_06_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_07_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_08_IDX");
        indexes.add("CREATE_INDEX_CMS_LOG_09_IDX");

        indexes = new ArrayList<String>();
        elements.put("CMS_SUBSCRIPTION_VISIT", indexes);
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_VISIT_01_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_VISIT_02_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_VISIT_03_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_VISIT_04_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_VISIT_05_IDX");

        indexes = new ArrayList<String>();
        elements.put("CMS_SUBSCRIPTION", indexes);
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_01_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_02_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_03_IDX");
        indexes.add("CREATE_INDEX_CMS_SUBSCRIPTION_04_IDX");

        indexes = new ArrayList<String>();
        elements.put("CMS_COUNTERS", indexes);

        indexes = new ArrayList<String>();
        elements.put("CMS_OFFLINE_URLNAME_MAPPINGS", indexes);
        indexes.add("CREATE_INDEX_CMS_OFFLINE_URLNAME_MAPPINGS_01_IDX");
        indexes.add("CREATE_INDEX_CMS_OFFLINE_URLNAME_MAPPINGS_02_IDX");

        indexes = new ArrayList<String>();
        elements.put("CMS_ONLINE_URLNAME_MAPPINGS", indexes);
        indexes.add("CREATE_INDEX_CMS_ONLINE_URLNAME_MAPPINGS_01_IDX");
        indexes.add("CREATE_INDEX_CMS_ONLINE_URLNAME_MAPPINGS_02_IDX");

        indexes = new ArrayList<String>();
        elements.put("CMS_ALIASES", indexes);
        indexes.add("CMS_ALIASES_IDX_1");

        indexes = new ArrayList<String>();
        elements.put("CMS_USER_PUBLISH_LIST", indexes);
        indexes.add("CMS_USERPUBLIST_IDX_01");
        indexes.add("CMS_USERPUBLIST_IDX_02");

        indexes = new ArrayList<String>();
        elements.put("CMS_REWRITES", indexes);
        indexes.add("CMS_REWRITES_IDX_01");

        Map<String, String> replacer = Collections.emptyMap();
        for (Map.Entry<String, List<String>> entry : elements.entrySet()) {
            String table = entry.getKey();
            if (!dbCon.hasTableOrColumn(table, null)) {
                String query = readQuery(table);
                if (query == null) {
                    System.out.println("Query not found: " + table);
                }
                dbCon.updateSqlStatement(query, replacer, null);
                for (String index : entry.getValue()) {
                    query = readQuery(index);
                    if (query == null) {
                        System.out.println("Query not found: " + index);
                    }
                    dbCon.updateSqlStatement(query, replacer, null);
                }
            } else {
                System.out.println("table " + table + " already exists");
            }
        }

    }
}
