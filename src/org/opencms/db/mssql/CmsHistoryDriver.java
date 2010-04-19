/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mssql/CmsHistoryDriver.java,v $
 * Date   : $Date: 2010/04/19 15:19:35 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2010 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db.mssql;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsDataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MS SQL implementation of the history driver methods.<p>
 *
 * @author Andras Balogh 
 *
 * @version $Revision: 1.9 $
 *
 * @since 6.9.1
 */
public class CmsHistoryDriver extends org.opencms.db.generic.CmsHistoryDriver {

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#initSqlManager(String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjects(org.opencms.db.CmsDbContext)
     */
    @Override
    public List readProjects(CmsDbContext dbc) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            I_CmsHistoryDriver historyDriver = m_driverManager.getHistoryDriver(dbc);
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_MSSQL_PROJECTS_READLAST_HISTORY");
            res = stmt.executeQuery();
            while (res.next()) {
                List resources = historyDriver.readProjectResources(dbc, res.getInt("PUBLISH_TAG"));
                projects.add(internalCreateProject(res, resources));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }
}