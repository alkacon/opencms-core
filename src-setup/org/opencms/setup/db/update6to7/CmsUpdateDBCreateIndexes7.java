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

import org.opencms.setup.CmsSetupDBWrapper;
import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.db.A_CmsUpdateDBPart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class creates all the indexes that are used in the database version 7.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBCreateIndexes7 extends A_CmsUpdateDBPart {

    /** Constant for the sql query to read the indexes.<p> */
    protected static final String QUERY_SHOW_INDEX = "QUERY_SHOW_INDEX";

    /** Constant for the replacement of the tablename in the sql query.<p> */
    protected static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the field of the index name.<p> */
    private static final String FIELD_INDEX = "KEY_NAME";

    /** Constant for the primary key.<p> */
    private static final String PRIMARY_KEY = "PRIMARY";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_add_new_indexes_queries.properties";

    /** Constant for the replacement of the indexes to drop.<p> */
    private static final String REPLACEMENT_INDEXES = "${dropindexes}";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBCreateIndexes7()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        List<String> elements = new ArrayList<String>();
        elements.add("CMS_CONTENTS");
        elements.add("CMS_GROUPS");
        elements.add("CMS_GROUPUSERS");
        elements.add("CMS_OFFLINE_ACCESSCONTROL");
        elements.add("CMS_OFFLINE_CONTENTS");
        elements.add("CMS_OFFLINE_PROPERTIES");
        elements.add("CMS_OFFLINE_PROPERTYDEF");
        elements.add("CMS_OFFLINE_RESOURCES");
        elements.add("CMS_OFFLINE_STRUCTURE");
        elements.add("CMS_ONLINE_ACCESSCONTROL");
        elements.add("CMS_ONLINE_PROPERTIES");
        elements.add("CMS_ONLINE_PROPERTYDEF");
        elements.add("CMS_ONLINE_RESOURCES");
        elements.add("CMS_ONLINE_STRUCTURE");
        elements.add("CMS_PROJECTRESOURCES");
        elements.add("CMS_PROJECTS");
        elements.add("CMS_PUBLISH_HISTORY");
        elements.add("CMS_STATICEXPORT_LINKS");
        elements.add("CMS_USERS");

        // iterate the queries
        for (Iterator<String> it = elements.iterator(); it.hasNext();) {
            String query = it.next();
            // Check if the table exists
            if (dbCon.hasTableOrColumn(query, null)) {
                HashMap<String, String> replacer = new HashMap<String, String>();
                replacer.put(REPLACEMENT_INDEXES, getIndexesToDrop(dbCon, query));
                try {
                    dbCon.updateSqlStatement(readQuery(query), replacer, null);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Table " + query + "does not exist.");
            }
        }
    }

    /**
     * Returns the string of the indexes that shall be dropped before adding the final new indexes.<p>
     *
     * @param dbCon the connection to the database
     * @param tablename the table to drop the indexes from
     *
     * @return the string to drop the temporary indexes
     */
    private String getIndexesToDrop(CmsSetupDb dbCon, String tablename) {

        List<String> indexes = new ArrayList<String>();
        String tableIndex = readQuery(QUERY_SHOW_INDEX);
        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        CmsSetupDBWrapper db = null;
        try {
            db = dbCon.executeSqlStatement(tableIndex, replacer);
            while (db.getResultSet().next()) {
                String index = db.getResultSet().getString(FIELD_INDEX);
                if (!indexes.contains(index)) {
                    indexes.add(index);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        StringBuffer dropIndex = new StringBuffer();
        for (Iterator<String> it = indexes.iterator(); it.hasNext();) {
            String index = it.next();
            if (index.equals(PRIMARY_KEY)) {
                dropIndex.append("DROP PRIMARY KEY, ");
            } else {
                dropIndex.append("DROP INDEX ");
                dropIndex.append(index);
                dropIndex.append(", ");
            }
        }
        return dropIndex.toString();
    }

}
