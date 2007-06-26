/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/generic/Attic/CmsUpdateDBNewTables.java,v $
 * Date   : $Date: 2007/06/26 12:25:48 $
 * Version: $Revision: 1.1.2.4 $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class creates the new tables for the database version of OpenCms 7.<p>
 * 
 * The new tables are
 * CMS_OFFLINE_RESOURCE_RELATIONS
 * CMS_ONLINE_RESOURCE_RELATOINS
 * CMS_PUBLISH_JOBS
 * CMS_RESOURCE_LOCKS
 * CMS_CONTENTS 
 * 
 * @author Roland Metzler
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 7.0.0
 */
public class CmsUpdateDBNewTables extends A_CmsUpdateDBPart {

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "generic/cms_new_tables_queries.properties";

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
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        List elements = new ArrayList();
        elements.add("CMS_OFFLINE_RESOURCE_RELATIONS");
        elements.add("CMS_ONLINE_RESOURCE_RELATIONS");
        elements.add("CMS_PUBLISH_JOBS");
        elements.add("CMS_RESOURCE_LOCKS");
        elements.add("CMS_CONTENTS");
        elements.add("CMS_HISTORY_PROJECTRESOURCES");
        elements.add("CMS_HISTORY_PROPERTYDEF");
        elements.add("CMS_HISTORY_PROPERTIES");
        elements.add("CMS_HISTORY_RESOURCES");
        elements.add("CMS_HISTORY_STRUCTURE");

        for (Iterator it = elements.iterator(); it.hasNext();) {
            String table = (String)it.next();
            if (!dbCon.hasTableOrColumn(table, null)) {
                String query = readQuery(table);
                dbCon.updateSqlStatement(query, null, null);
            } else {
                System.out.println("table " + table + " already exists");
            }
        }
    }
}
