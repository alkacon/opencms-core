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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.history.CmsHistoryProject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle implementation of the history driver methods.<p>
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
     * @see org.opencms.db.generic.CmsHistoryDriver#readProjects(org.opencms.db.CmsDbContext)
     */
    @Override
    public List<CmsHistoryProject> readProjects(CmsDbContext dbc) throws CmsDataAccessException {

        List<CmsHistoryProject> projects = new ArrayList<CmsHistoryProject>();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        Map<Integer, CmsHistoryProject> tmpProjects = new HashMap<Integer, CmsHistoryProject>();

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_PROJECTS_READLAST_HISTORY");
            stmt.setInt(1, 300);
            res = stmt.executeQuery();
            while (res.next()) {
                tmpProjects.put(Integer.valueOf(res.getInt("PUBLISH_TAG")), internalCreateProject(res, null));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        I_CmsHistoryDriver historyDriver = m_driverManager.getHistoryDriver(dbc);
        for (Map.Entry<Integer, CmsHistoryProject> entry : tmpProjects.entrySet()) {
            List<String> resources = historyDriver.readProjectResources(dbc, entry.getKey().intValue());
            entry.getValue().setProjectResources(resources);
            projects.add(entry.getValue());
        }
        return projects;
    }
}