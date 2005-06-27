/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsBackupDriver.java,v $
 * Date   : $Date: 2005/06/27 23:22:25 $
 * Version: $Revision: 1.56 $
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp.DelegatingResultSet;

/**
 * Oracle implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert  
 * @author Michael Emmerich   
 * @author Carsten Weinholz  
 * 
 * @version $Revision: 1.56 $
 * 
 * @since 6.0.0 
 */
public class CmsBackupDriver extends org.opencms.db.generic.CmsBackupDriver {

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
            stmt1 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_BACKUP_DELETE_CONTENT");
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_BACKUP_DELETE_RESOURCES");
            stmt3 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_BACKUP_DELETE_STRUCTURE");
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
                stmt4.setInt(4, CmsProperty.STRUCTURE_RECORD_MAPPING);
                stmt4.setString(5, currentResource.getResourceId().toString());
                stmt4.setInt(6, CmsProperty.RESOURCE_RECORD_MAPPING);
                stmt4.addBatch();
            }

            if (count > 0) {
                stmt1.executeBatch();
                stmt2.executeBatch();
                stmt3.executeBatch();
                stmt4.executeBatch();
            }

        } catch (Exception e) {
            throw new CmsDbException(Messages.get().container(
                Messages.ERR_DELETE_BACKUP_VERSIONS_1,
                currentResource.getRootPath()), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt1, null);
            m_sqlManager.closeAll(dbc, conn, stmt2, null);
            m_sqlManager.closeAll(dbc, conn, stmt3, null);
            m_sqlManager.closeAll(dbc, conn, stmt4, null);
        }
    }

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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_PROJECTS_READLAST_BACKUP");
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

    /**
     * @see org.opencms.db.generic.CmsBackupDriver#internalWriteBackupFileContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.file.CmsResource, int, int)
     */
    protected void internalWriteBackupFileContent(
        CmsDbContext dbc,
        CmsUUID backupId,
        CmsResource resource,
        int tagId,
        int versionId) throws CmsDataAccessException {

        PreparedStatement stmt = null, stmt2 = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        Connection conn = null;
        ResultSet res = null;

        CmsUUID contentId;
        byte[] fileContent;
        if (resource instanceof CmsFile) {
            contentId = ((CmsFile)resource).getContentId();
            fileContent = ((CmsFile)resource).getContents();
        } else {
            contentId = CmsUUID.getNullUUID();
            fileContent = new byte[0];
        }

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_CONTENTS_ADDBACKUP");

            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            stmt.setString(1, contentId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setInt(3, tagId);
            stmt.setInt(4, versionId);
            stmt.setString(5, backupId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        boolean wasInTransaction = false;
        try {
            conn = m_sqlManager.getConnection(dbc);

            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }

            // select the backup record for update            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_CONTENTS_UPDATEBACKUP");
            stmt.setString(1, contentId.toString());
            stmt.setString(2, backupId.toString());

            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_BACKUP_CONTENT_ID_2,
                    contentId,
                    backupId));
            }

            // write file content
            OutputStream output = CmsUserDriver.getOutputStreamFromBlob(res, "FILE_CONTENT");
            output.write(fileContent);
            output.close();
            res.close();
            res = null;
            fileContent = null;

            if (!wasInTransaction) {
                commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
                commit.execute();
                commit.close();
                commit = null;
            }

            stmt.close();
            stmt = null;

            if (!wasInTransaction) {
                conn.setAutoCommit(true);
            }
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_WRITING_TO_OUTPUT_STREAM_1, resource), e);
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, stmt), e);
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException exc) {
                    // ignore
                }
            }
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                    // ignore
                }
            }
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                    // ignore
                }
            }

            if (!wasInTransaction) {
                if (stmt != null) {
                    try {
                        rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                        rollback.execute();
                        rollback.close();
                    } catch (SQLException se) {
                        // ignore
                    }
                    try {
                        stmt.close();
                    } catch (SQLException exc) {
                        // ignore
                    }
                }
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException se) {
                        // ignore
                    }
                }
            }
        }
    }
}
