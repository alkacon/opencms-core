/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsBackupDriver.java,v $
 * Date   : $Date: 2003/11/03 09:05:52 $
 * Version: $Revision: 1.22 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.util.CmsUUID;

import com.opencms.core.CmsException;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsBackupResource;
import com.opencms.file.CmsResource;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.dbcp.DelegatingResultSet;

import source.org.apache.java.util.Configurations;

/**
 * Oracle implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.22 $ $Date: 2003/11/03 09:05:52 $
 * @since 5.1
 */
public class CmsBackupDriver extends org.opencms.db.generic.CmsBackupDriver {

    /*
     * Indicates that server side copying should be used
     */
    private boolean m_enableServerCopy = false;
    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(java.util.List, int)
     */
    public void deleteBackups(List existingBackups, int maxVersions) throws CmsException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;

        Connection conn = null;
        CmsBackupResource currentResource = null;
        int count = existingBackups.size() - maxVersions;

        try {
            conn = m_sqlManager.getConnectionForBackup();
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
                stmt4.setString(2, currentResource.getResourceId().toString());
                stmt4.setInt(3, currentResource.getTagId());
                stmt4.addBatch();
            }

            if (count > 0) {
                stmt1.executeBatch();
                stmt2.executeBatch();
                stmt3.executeBatch();
                stmt4.executeBatch();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt1, null);
            m_sqlManager.closeAll(conn, stmt2, null);
            m_sqlManager.closeAll(conn, stmt3, null);
            m_sqlManager.closeAll(conn, stmt4, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(source.org.apache.java.util.Configurations, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(Configurations config, List successiveDrivers, CmsDriverManager driverManager) {

        m_enableServerCopy = "true".equals(config.getString("db.oracle.servercopy"));
        super.init(config, successiveDrivers, driverManager);
    }
    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
    }

    /**
    * Internal method to write the backup content.<p>
    *  
    * @param backupId the backup id
    * @param resource the resource to backup
    * @param fileContent the content of the file
    * @param tagId the tag revision
    * @param versionId the version revision
    * @throws CmsException if something goes wrong
    */
    protected void internalWriteBackupFileContent(CmsUUID backupId, CmsResource resource, byte[] fileContent, int tagId, int versionId) throws CmsException {
      
        CmsUUID fileId = resource.getFileId();
                
        PreparedStatement stmt = null, stmt2 = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        Connection conn = null;
        ResultSet res = null;
                
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_ADDBACKUP");

            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            stmt.setString(1, fileId.toString());
            stmt.setInt(2, tagId);
            stmt.setInt(3, versionId);
            stmt.setString(4, backupId.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteBackupFileContent backupId=" + backupId.toString() + " fileId=" + fileId.toString(), CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        try {
            conn = m_sqlManager.getConnectionForBackup();
            conn.setAutoCommit(false);
            
            if (m_enableServerCopy) {
                // read the content blob
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_READCONTENT");
                stmt.setString(1, fileId.toString());
                res = stmt.executeQuery();
                
                if (res.next()) {
                    // backup the content
                    Blob content = res.getBlob(1);
                    stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_BACKUPCONTENT");
                    stmt2.setBlob(1, content);
                    stmt2.setString(2, fileId.toString());
                    stmt2.setString(3, backupId.toString());
                    stmt2.executeUpdate();
                    stmt2.close();
                    stmt2 = null;                
                } else {
                    // TODO: check if exception is useful
                }
                res.close();
                res = null;
            } else {
                // select the backup record for update            
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_UPDATEBACKUP");
                stmt.setString(1, fileId.toString());
                stmt.setString(2, backupId.toString());
                
                res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
                if (!res.next())
                    throw new CmsException("internalWriteBackupFileContent backupId=" + backupId.toString() + " fileId=" + fileId.toString() + " content not found", CmsException.C_NOT_FOUND);
            
                // write file content 
                Blob content = res.getBlob("FILE_CONTENT");
                ((oracle.sql.BLOB)content).trim(0);
                OutputStream output = ((oracle.sql.BLOB)content).getBinaryOutputStream();
                output.write(fileContent);
                output.close();
                res.close();
                res = null;
            }
            
            commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            commit.execute();
            commit.close();
            commit = null;
                        
            stmt.close();
            stmt = null;

            conn.setAutoCommit(true);                      
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteBackupFileContent backupId=" + backupId.toString() + " fileId=" + fileId.toString(), CmsException.C_SERIALIZATION, e, false);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteBackupFileContent backupId=" + backupId.toString() + " fileId=" + fileId.toString(), CmsException.C_SQL_ERROR, e, false);
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

    /**
     * @see org.opencms.db.I_CmsBackupDriver#getAllBackupProjects()
     */
    public Vector readBackupProjects() throws CmsException {
        Vector projects = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_PROJECTS_READLAST_BACKUP");
            stmt.setInt(1, 300);
            res = stmt.executeQuery();
            while (res.next()) {
                Vector resources = m_driverManager.getBackupDriver().readBackupProjectResources(res.getInt("TAG_ID"));
                projects.addElement(
                    new CmsBackupProject(
                        res.getInt("TAG_ID"),
                        res.getInt("PROJECT_ID"),
                        res.getString("PROJECT_NAME"),
                        res.getString("PROJECT_DESCRIPTION"),
                        res.getInt("TASK_ID"),
                        new CmsUUID(res.getString("USER_ID")),
                        new CmsUUID(res.getString("GROUP_ID")),
                        new CmsUUID(res.getString("MANAGERGROUP_ID")),
                        CmsDbUtil.getTimestamp(res, "PROJECT_CREATEDATE"),
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
            throw m_sqlManager.getCmsException(this, "readBackupProjects", CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return (projects);
    }
}
