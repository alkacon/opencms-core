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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Oracle implementation of the generic update of the contents tables.<p>
 *
 * @since 7.0.0
 */
public class CmsUpdateDBContentTables extends org.opencms.setup.db.update6to7.CmsUpdateDBContentTables {

    /** Constant for the sql query to transfer the online contents.<p> */
    protected static final String QUERY_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_FROM = "Q_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_FROM";

    /** Constant for the sql query to transfer the online contents.<p> */
    protected static final String QUERY_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_FROM2 = "Q_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_FROM2";

    /** Constant for the sql query to transfer the online contents.<p> */
    protected static final String QUERY_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_TO = "Q_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_TO";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_content_table_queries.properties";

    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";

    /**
     * Constructor.<p>
     *
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBContentTables()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.db.update6to7.CmsUpdateDBContentTables#createContentsTable(org.opencms.setup.CmsSetupDb)
     */
    @Override
    protected void createContentsTable(CmsSetupDb dbCon) throws SQLException {

        String indexTablespace = m_poolData.get("indexTablespace");

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(TABLE_CMS_CONTENTS, null)) {
            String query = readQuery(QUERY_CREATE_CMS_CONTENTS_TABLE);

            Map<String, String> replacer = new HashMap<String, String>();
            replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);

            dbCon.updateSqlStatement(query, null, null);
        } else {
            System.out.println("table " + TABLE_CMS_CONTENTS + " already exists");
        }
    }

    /**
     * @see org.opencms.setup.db.update6to7.CmsUpdateDBContentTables#transferOnlineContents(org.opencms.setup.CmsSetupDb, int)
     */
    @Override
    protected void transferOnlineContents(CmsSetupDb dbCon, int pubTag) throws SQLException {

        String query = readQuery(QUERY_TRANSFER_ONLINE_CONTENTS);
        Map<String, String> replacer = Collections.singletonMap("${pubTag}", "" + pubTag);
        dbCon.updateSqlStatement(query, replacer, null);

        query = readQuery(QUERY_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_FROM);
        dbCon.updateSqlStatement(query, null, null);

        query = readQuery(QUERY_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_FROM2);
        dbCon.updateSqlStatement(query, null, Collections.<Object> singletonList(Integer.valueOf(pubTag)));

        query = readQuery(QUERY_UPDATE_ONLINE_CONTENTS_PUBLISH_TAG_TO);
        dbCon.updateSqlStatement(query, null, null);
    }
}