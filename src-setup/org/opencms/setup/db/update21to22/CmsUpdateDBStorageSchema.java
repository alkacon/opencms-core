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

package org.opencms.setup.db.update21to22;

import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.db.A_CmsUpdateDBPart;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds the storage schema introduced with OpenCms 22.<p>
 */
public class CmsUpdateDBStorageSchema extends A_CmsUpdateDBPart {

    /** Query key for adding CMS_CONTENTS.HASH. */
    protected static final String ADD_CMS_CONTENTS_HASH = "ADD_CMS_CONTENTS_HASH";

    /** Query key for adding CMS_CONTENTS.STORAGE. */
    protected static final String ADD_CMS_CONTENTS_STORAGE = "ADD_CMS_CONTENTS_STORAGE";

    /** Query key for adding CMS_OFFLINE_CONTENTS.HASH. */
    protected static final String ADD_CMS_OFFLINE_CONTENTS_HASH = "ADD_CMS_OFFLINE_CONTENTS_HASH";

    /** Query key for adding CMS_OFFLINE_CONTENTS.STORAGE. */
    protected static final String ADD_CMS_OFFLINE_CONTENTS_STORAGE = "ADD_CMS_OFFLINE_CONTENTS_STORAGE";

    /** Query key for creating the CMS_CONTENTS storage index. */
    protected static final String CREATE_CMS_CONTENTS_HASH_INDEX = "CREATE_CMS_CONTENTS_HASH_INDEX";

    /** Query key for creating the CMS_CONTENTS storage-leading index. */
    protected static final String CREATE_CMS_CONTENTS_STORAGE_INDEX = "CREATE_CMS_CONTENTS_STORAGE_INDEX";

    /** Query key for creating CMS_STORAGE. */
    protected static final String CREATE_CMS_STORAGE = "CREATE_CMS_STORAGE";

    /** Query key for creating the CMS_OFFLINE_CONTENTS storage index. */
    protected static final String CREATE_CMS_OFFLINE_CONTENTS_HASH_INDEX = "CREATE_CMS_OFFLINE_CONTENTS_HASH_INDEX";

    /** Query key for creating the CMS_OFFLINE_CONTENTS storage-leading index. */
    protected static final String CREATE_CMS_OFFLINE_CONTENTS_STORAGE_INDEX = "CREATE_CMS_OFFLINE_CONTENTS_STORAGE_INDEX";

    /** Query key for the CMS_CONTENTS storage index name. */
    protected static final String INDEX_CMS_CONTENTS_HASH = "INDEX_CMS_CONTENTS_HASH";

    /** Query key for the CMS_CONTENTS storage-leading index name. */
    protected static final String INDEX_CMS_CONTENTS_STORAGE = "INDEX_CMS_CONTENTS_STORAGE";

    /** Query key for the CMS_OFFLINE_CONTENTS storage index name. */
    protected static final String INDEX_CMS_OFFLINE_CONTENTS_HASH = "INDEX_CMS_OFFLINE_CONTENTS_HASH";

    /** Query key for the CMS_OFFLINE_CONTENTS storage-leading index name. */
    protected static final String INDEX_CMS_OFFLINE_CONTENTS_STORAGE = "INDEX_CMS_OFFLINE_CONTENTS_STORAGE";

    /**
     * Executes this update on an existing setup database connection.<p>
     *
     * @param dbCon the database connection
     * @param dbPoolData the database pool data
     *
     * @throws SQLException if something goes wrong
     */
    public void execute(CmsSetupDb dbCon, Map<String, String> dbPoolData) throws SQLException {

        m_poolData = dbPoolData;
        internalExecute(dbCon);
    }

    /**
     * Adds a column if it is missing.<p>
     *
     * @param dbCon the database connection
     * @param table the table name
     * @param column the column name
     * @param queryKey the query key
     *
     * @throws SQLException if something goes wrong
     */
    protected void addColumn(CmsSetupDb dbCon, String table, String column, String queryKey) throws SQLException {

        if (dbCon.hasTableOrColumn(table, column)) {
            System.out.println("column " + table + "." + column + " already exists");
            return;
        }
        dbCon.updateSqlStatement(readQuery(queryKey), getReplacer(), null);
    }

    /**
     * Adds an index if it is missing.<p>
     *
     * @param dbCon the database connection
     * @param table the table name
     * @param indexKey the index name query key
     * @param queryKey the query key
     *
     * @throws SQLException if something goes wrong
     */
    protected void addIndex(CmsSetupDb dbCon, String table, String indexKey, String queryKey) throws SQLException {

        String index = readQuery(indexKey);
        if (dbCon.hasIndex(table, index)) {
            System.out.println("index " + index + " already exists");
            return;
        }
        dbCon.updateSqlStatement(readQuery(queryKey), getReplacer(), null);
    }

    /**
     * Creates a table if it is missing.<p>
     *
     * @param dbCon the database connection
     * @param table the table name
     * @param queryKey the query key
     *
     * @throws SQLException if something goes wrong
     */
    protected void createTable(CmsSetupDb dbCon, String table, String queryKey) throws SQLException {

        if (dbCon.hasTableOrColumn(table, null)) {
            System.out.println("table " + table + " already exists");
            return;
        }
        dbCon.updateSqlStatement(readQuery(queryKey), getReplacer(), null);
    }

    /**
     * Returns the token replacements for the update queries.<p>
     *
     * @return the token replacements
     */
    protected Map<String, String> getReplacer() {

        Map<String, String> result = new HashMap<String, String>();
        addReplacer(result, "${dataTablespace}", m_poolData.get("dataTablespace"));
        addReplacer(result, "${indexTablespace}", m_poolData.get("indexTablespace"));
        addReplacer(result, "${tableEngine}", m_poolData.get("engine"));
        return result;
    }

    /**
     * @see org.opencms.setup.db.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void internalExecute(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());

        createTable(dbCon, "CMS_STORAGE", CREATE_CMS_STORAGE);
        if (dbCon.hasTableOrColumn("CMS_CONTENTS", null)) {
            addColumn(dbCon, "CMS_CONTENTS", "STORAGE", ADD_CMS_CONTENTS_STORAGE);
            addColumn(dbCon, "CMS_CONTENTS", "HASH", ADD_CMS_CONTENTS_HASH);
            addIndex(dbCon, "CMS_CONTENTS", INDEX_CMS_CONTENTS_HASH, CREATE_CMS_CONTENTS_HASH_INDEX);
            addIndex(dbCon, "CMS_CONTENTS", INDEX_CMS_CONTENTS_STORAGE, CREATE_CMS_CONTENTS_STORAGE_INDEX);
        }
        if (dbCon.hasTableOrColumn("CMS_OFFLINE_CONTENTS", null)) {
            addColumn(dbCon, "CMS_OFFLINE_CONTENTS", "STORAGE", ADD_CMS_OFFLINE_CONTENTS_STORAGE);
            addColumn(dbCon, "CMS_OFFLINE_CONTENTS", "HASH", ADD_CMS_OFFLINE_CONTENTS_HASH);
            addIndex(
                dbCon,
                "CMS_OFFLINE_CONTENTS",
                INDEX_CMS_OFFLINE_CONTENTS_HASH,
                CREATE_CMS_OFFLINE_CONTENTS_HASH_INDEX);
            addIndex(
                dbCon,
                "CMS_OFFLINE_CONTENTS",
                INDEX_CMS_OFFLINE_CONTENTS_STORAGE,
                CREATE_CMS_OFFLINE_CONTENTS_STORAGE_INDEX);
        }
    }

    /**
     * Adds a token replacement if the value is available.<p>
     *
     * @param replacer the replacer map
     * @param key the token
     * @param value the replacement
     */
    private void addReplacer(Map<String, String> replacer, String key, String value) {

        if (value != null) {
            replacer.put(key, value);
        }
    }
}
