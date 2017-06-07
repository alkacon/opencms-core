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
 * This class makes an update of the CMS_USERS table splitting it up into CMS_USERS and CMS_USERDATA.<p>
 * Unnecessary colums from CMS_USERS will be deleted and the new column USER_DATECREATED is added.
 */
public class CmsUpdateDBCmsUsers extends org.opencms.setup.db.update6to7.CmsUpdateDBCmsUsers {

    /** Constant for the query to create the user data table.<p> */
    private static final String QUERY_CREATE_TABLE_USERDATA_MYSQL = "Q_CREATE_TABLE_USERDATA_MYSQL";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "cms_users_queries.properties";

    /**
     * Default constructor.<p>
     *
     * @throws IOException if the default sql queries property file could not be read
     */
    public CmsUpdateDBCmsUsers()
    throws IOException {

        super();
        loadQueryProperties(getPropertyFileLocation() + QUERY_PROPERTY_FILE);
    }

    /**
     * Creates the CMS_USERDATA table if it does not exist yet.<p>
     *
     * @param dbCon the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    @Override
    protected void createUserDataTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String createStatement = readQuery(QUERY_CREATE_TABLE_USERDATA_MYSQL);
        Map<String, String> replacer = Collections.singletonMap("${tableEngine}", m_poolData.get("engine"));
        dbCon.updateSqlStatement(createStatement, replacer, null);
    }
}
