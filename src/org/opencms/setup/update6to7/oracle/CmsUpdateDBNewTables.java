/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/oracle/Attic/CmsUpdateDBNewTables.java,v $
 * Date   : $Date: 2007/06/05 12:25:03 $
 * Version: $Revision: 1.1.2.2 $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opencms.setup.CmsSetupDb;

/**
 * Oracle implementation to create the new tables for version 7 of OpenCms.<p>
 * 
 * @author Roland Metzler
 *
 */
public class CmsUpdateDBNewTables extends org.opencms.setup.update6to7.generic.CmsUpdateDBNewTables {

    
    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";
    
    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "oracle/cms_new_tables_queries.properties";

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
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        
        String indexTablespace = (String) m_poolData.get("indexTablespace");
        
        List elements = new ArrayList();
        elements.add("CMS_OFFLINE_RESOURCE_RELATIONS_ORACLE");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_01_ORACLE");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_02_ORACLE");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_03_ORACLE");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_04_ORACLE");
        elements.add("CREATE_INDEX_CMS_OFFLINE_RELATIONS_05_ORACLE");
        
        elements.add("CMS_ONLINE_RESOURCE_RELATIONS_ORACLE");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_01_ORACLE");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_02_ORACLE");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_03_ORACLE");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_04_ORACLE");
        elements.add("CREATE_INDEX_CMS_ONLINE_RELATIONS_05_ORACLE");
        
        elements.add("CMS_PUBLISH_JOBS_ORACLE");       
        elements.add("CMS_RESOURCE_LOCKS_ORACLE");
        
        elements.add("CMS_CONTENTS_ORACLE");
        elements.add("CREATE_INDEX_CMS_CONTENTS_01_ORACLE");
        elements.add("CREATE_INDEX_CMS_CONTENTS_02_ORACLE");
        elements.add("CREATE_INDEX_CMS_CONTENTS_03_ORACLE");
        elements.add("CREATE_INDEX_CMS_CONTENTS_04_ORACLE");
        elements.add("CREATE_INDEX_CMS_CONTENTS_05_ORACLE");
        
        elements.add("CMS_HISTORY_PROJECTRESOURCES_ORACLE");
        elements.add("CMS_HISTORY_PROPERTYDEF_ORACLE");
        
        elements.add("CMS_HISTORY_PROPERTIES_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_01_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_02_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_03_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_04_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_PROPERTIES_05_ORACLE");
        
        elements.add("CMS_HISTORY_RESOURCES_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_01_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_02_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_03_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_04_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_05_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_RESOURCES_06_ORACLE");
        
        elements.add("CMS_HISTORY_STRUCTURE_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_01_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_02_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_03_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_04_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_05_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_06_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_07_ORACLE");
        elements.add("CREATE_INDEX_CMS_HISTORY_STRUCTURE_08_ORACLE");

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
