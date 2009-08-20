/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/update6to7/postgresql/CmsUpdateDBNewTables.java,v $
 * Date   : $Date: 2009/08/20 11:31:31 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.update6to7.postgresql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opencms.setup.CmsSetupDb;

/**
 * PostgreSQL implementation to create the new tables for version 7 of OpenCms.<p>
 * 
 * @author Ivan Codarin
 * @author Georgi Naplatanov
 *
 * @version $Revision: 1.4 $
 * 
 * @since 7.0.2
 */
public class CmsUpdateDBNewTables extends org.opencms.setup.update6to7.generic.CmsUpdateDBNewTables {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "postgresql/cms_new_tables_queries.properties";

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
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }

    /** 
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBNewTables#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        String indexTablespace = (String)m_poolData.get("indexTablespace");

        List elements = new ArrayList();
        elements.add("CMS_OFFLINE_RESOURCE_RELATIONS");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_01");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_02");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_03");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_04");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_05");

        elements.add("CMS_ONLINE_RESOURCE_RELATIONS");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_01");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_02");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_03");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_04");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_05");

        elements.add("CMS_PUBLISH_JOBS");
        elements.add("CMS_RESOURCE_LOCKS");

        elements.add("CMS_CONTENTS");
        elements.add("CREATE_INDEX_CMS_CONTENTS_01");
        elements.add("CREATE_INDEX_CMS_CONTENTS_02");
        elements.add("CREATE_INDEX_CMS_CONTENTS_03");
        elements.add("CREATE_INDEX_CMS_CONTENTS_04");
        elements.add("CREATE_INDEX_CMS_CONTENTS_05");

        elements.add("CMS_HISTORY_PROJECTRESOURCES");
        elements.add("CMS_HISTORY_PROPERTYDEF");

        elements.add("CMS_HISTORY_PROPERTIES");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_01");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_02");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_03");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_04");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_05");

        elements.add("CMS_HISTORY_RESOURCES");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_01");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_02");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_03");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_04");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_05");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_06");

        elements.add("CMS_HISTORY_STRUCTURE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_01");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_02");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_03");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_04");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_05");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_06");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_07");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_08");

        for (Iterator it = elements.iterator(); it.hasNext();) {
            String table = (String)it.next();
            if (!dbCon.hasTableOrColumn(table, null)) {
                String query = readQuery(table);
                HashMap replacer = new HashMap();
                replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);
                dbCon.updateSqlStatement(query, replacer, null);
            } else {
                System.out.println("table " + table + " already exists");
            }
        }
    }
}
