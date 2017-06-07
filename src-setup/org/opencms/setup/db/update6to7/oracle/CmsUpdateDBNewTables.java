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

package org.opencms.setup.db.update6to7.oracle;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle implementation to create the new tables for version 7 of OpenCms.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBNewTables extends org.opencms.setup.db.update6to7.CmsUpdateDBNewTables {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_new_tables_queries.properties";

    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";

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
     * @see org.opencms.setup.db.update6to7.CmsUpdateDBNewTables#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        String indexTablespace = m_poolData.get("indexTablespace");

        Map<String, List<String>> elements = new HashMap<String, List<String>>();

        List<String> indexes = new ArrayList<String>();
        indexes.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_01");
        indexes.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_02");
        indexes.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_03");
        indexes.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_04");
        indexes.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_05");
        elements.put("CMS_OFFLINE_RESOURCE_RELATIONS", indexes);

        indexes = new ArrayList<String>();
        indexes.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_01");
        indexes.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_02");
        indexes.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_03");
        indexes.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_04");
        indexes.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_05");
        elements.put("CMS_ONLINE_RESOURCE_RELATIONS", indexes);

        elements.put("CMS_PUBLISH_JOBS", new ArrayList<String>());
        elements.put("CMS_RESOURCE_LOCKS", new ArrayList<String>());

        indexes = new ArrayList<String>();
        indexes.add("CREATE_INDEX_CMS_CONTENTS_01");
        indexes.add("CREATE_INDEX_CMS_CONTENTS_02");
        indexes.add("CREATE_INDEX_CMS_CONTENTS_03");
        indexes.add("CREATE_INDEX_CMS_CONTENTS_04");
        indexes.add("CREATE_INDEX_CMS_CONTENTS_05");
        elements.put("CMS_CONTENTS", indexes);

        elements.put("CMS_HISTORY_PROJECTRESOURCES", new ArrayList<String>());
        elements.put("CMS_HISTORY_PROPERTYDEF", new ArrayList<String>());

        indexes = new ArrayList<String>();
        indexes.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_01");
        indexes.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_02");
        indexes.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_03");
        indexes.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_04");
        indexes.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_05");
        elements.put("CMS_HISTORY_PROPERTIES", indexes);

        indexes = new ArrayList<String>();
        indexes.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_01");
        indexes.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_02");
        indexes.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_03");
        indexes.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_04");
        indexes.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_05");
        indexes.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_06");
        elements.put("CMS_HISTORY_RESOURCES", indexes);

        indexes = new ArrayList<String>();
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_01");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_02");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_03");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_04");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_05");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_06");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_07");
        indexes.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_08");
        elements.put("CMS_HISTORY_STRUCTURE", indexes);

        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);

        for (Map.Entry<String, List<String>> entry : elements.entrySet()) {
            String table = entry.getKey();
            if (!dbCon.hasTableOrColumn(table, null)) {
                String query = readQuery(table);
                dbCon.updateSqlStatement(query, replacer, null);

                for (String index : entry.getValue()) {
                    query = readQuery(index);
                    dbCon.updateSqlStatement(query, replacer, null);
                }
            } else {
                System.out.println("table " + table + " already exists");
            }
        }
    }
}
