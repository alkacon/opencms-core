/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/postgresql/CmsBackupDriver.java,v $
 * Date   : $Date: 2005/05/16 13:46:56 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.postgresql;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsProperty;
import org.opencms.util.CmsUUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * PostgreSql implementation of the backup driver methods.<p>
 * 
 * @author Antonio Core (antonio@starsolutions.it)
 * @version $Revision: 1.8 $ $Date: 2005/05/16 13:46:56 $
 * @since 6.0
 */
public class CmsBackupDriver extends org.opencms.db.generic.CmsBackupDriver {

    /**
     * @see org.opencms.db.I_CmsBackupDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {
        
        return CmsSqlManager.getInstance(classname);
    }

    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(org.opencms.db.CmsDbContext, java.util.List, int)
     */
    public void deleteBackups(CmsDbContext dbc, List existingBackups, int maxVersions) throws CmsDataAccessException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;

        Connection conn = null;
        CmsBackupResource currentResource = null;
        int count = existingBackups.size() - maxVersions;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt1 = m_sqlManager.getPreparedStatement(conn, "C_POSTGRESQL_BACKUP_DELETE_CONTENT");
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_POSTGRESQL_BACKUP_DELETE_RESOURCES");
            stmt3 = m_sqlManager.getPreparedStatement(conn, "C_POSTGRESQL_BACKUP_DELETE_STRUCTURE");
            stmt4 = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL_BACKUP");

            for (int i = 0; i < count; i++) {
                currentResource = (CmsBackupResource)existingBackups.get(i);
                // add the values to delete the file table
                stmt1.setString(1, currentResource.getBackupId().toString());
                stmt1.addBatch();
                // add the values to delete the resource table
                stmt2.setString(1, currentResource.getBackupId().toString());
                stmt2.addBatch();
                // add the values to delete the structure table
                stmt3.setString(1, currentResource.getBackupId().toString());
                stmt3.addBatch();
                // delete the properties
                stmt4.setString(1, currentResource.getBackupId().toString());
                stmt4.setInt(2, currentResource.getTagId());
                stmt4.setString(3, currentResource.getStructureId().toString());
                stmt4.setInt(4, CmsProperty.C_STRUCTURE_RECORD_MAPPING);
                stmt4.setString(5, currentResource.getResourceId().toString());
                stmt4.setInt(6, CmsProperty.C_RESOURCE_RECORD_MAPPING);
                stmt4.addBatch();
            }

            if (count > 0) {
                stmt1.executeBatch();
                stmt2.executeBatch();
                stmt3.executeBatch();
                stmt4.executeBatch();
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(this, null, e);
        } catch (Exception ex) {
            throw new CmsDataAccessException(ex);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt1, null);
            m_sqlManager.closeAll(dbc, conn, stmt2, null);
            m_sqlManager.closeAll(dbc, conn, stmt3, null);
            m_sqlManager.closeAll(dbc, conn, stmt4, null);
        }
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
                List resources = m_driverManager.getBackupDriver().readBackupProjectResources(dbc, res.getInt("PUBLISH_TAG"));
                projects.add(
                    new CmsBackupProject(
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
        } catch (SQLException exc) {
            throw new CmsDbSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }
    
    

    
}