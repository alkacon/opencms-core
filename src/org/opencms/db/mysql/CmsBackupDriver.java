/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsBackupDriver.java,v $
 * Date   : $Date: 2005/06/23 11:11:38 $
 * Version: $Revision: 1.28 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db.mysql;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsDataAccessException;
import org.opencms.util.CmsUUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert  
 * @author Michael Emmerich  
 * 
 * @version $Revision: 1.28 $
 * 
 * @since 6.0.0 
 */
public class CmsBackupDriver extends org.opencms.db.generic.CmsBackupDriver {

    /**
     * @see org.opencms.db.I_CmsBackupDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjects(org.opencms.db.CmsDbContext)
     */
    public List readBackupProjects(CmsDbContext dbc) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READLAST_BACKUP");
            stmt.setInt(1, 300);
            res = stmt.executeQuery();
            while (res.next()) {
                List resources = m_driverManager.getBackupDriver().readBackupProjectResources(
                    dbc,
                    res.getInt("PUBLISH_TAG"));
                projects.add(new CmsBackupProject(
                    res.getInt("PUBLISH_TAG"),
                    res.getInt("PROJECT_ID"),
                    res.getString("PROJECT_NAME"),
                    res.getString("PROJECT_DESCRIPTION"),
                    res.getInt("TASK_ID"),
                    new CmsUUID(res.getString("USER_ID")),
                    new CmsUUID(res.getString("GROUP_ID")),
                    new CmsUUID(res.getString("MANAGERGROUP_ID")),
                    res.getLong("DATE_CREATED"),
                    res.getInt("PROJECT_TYPE"),
                    CmsDbUtil.getTimestamp(res, "PROJECT_PUBLISHDATE"),
                    new CmsUUID(res.getString("PROJECT_PUBLISHED_BY")),
                    res.getString("PROJECT_PUBLISHED_BY_NAME"),
                    res.getString("USER_NAME"),
                    res.getString("GROUP_NAME"),
                    res.getString("MANAGERGROUP_NAME"),
                    resources));
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
