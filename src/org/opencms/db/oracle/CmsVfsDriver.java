/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsVfsDriver.java,v $
 * Date   : $Date: 2004/08/12 11:01:30 $
 * Version: $Revision: 1.21 $
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

import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingResultSet;

/**
 * Oracle implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.21 $ $Date: 2004/08/12 11:01:30 $
 * @since 5.1
 */
public class CmsVfsDriver extends org.opencms.db.generic.CmsVfsDriver {     

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(CmsProject, org.opencms.util.CmsUUID, byte[], int, boolean)
     */
    public void createContent(CmsProject project, CmsUUID resourceId, byte[] content, int versionId, boolean writeBackup) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_CONTENTS_ADDBACKUP");
            } else {
                conn = m_sqlManager.getConnection(project);
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ORACLE_CONTENTS_ADD");
            }
            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, resourceId.toString());
            if (writeBackup) {
                stmt.setInt(3, versionId);
                stmt.setString(4, new CmsUUID().toString());
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "createFileContent resourceId=" + resourceId.toString(), CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        // now update the file content
        writeContent(project, resourceId, content, writeBackup);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(CmsProject, org.opencms.util.CmsUUID, byte[], boolean)
     */
    public void writeContent(CmsProject project, CmsUUID resourceId, byte[] content, boolean writeBackup) throws CmsException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        Connection conn = null;
        ResultSet res = null;
        
        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_CONTENTS_UPDATEBACKUP");
            } else {
                conn = m_sqlManager.getConnection(project);
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ORACLE_CONTENTS_UPDATECONTENT");
            }
            conn.setAutoCommit(false);
            
            // update the file content in the contents table
            stmt.setString(1, resourceId.toString());
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsException("writeFileContent resourceId=" + resourceId.toString() + " content not found", CmsException.C_NOT_FOUND);
            }
            
            // write file content 
            Blob blob = res.getBlob("FILE_CONTENT");
            ((oracle.sql.BLOB)blob).trim(0);
            OutputStream output = ((oracle.sql.BLOB)blob).getBinaryOutputStream();
            output.write(content);
            output.close();
                
            commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            commit.execute();
            
            m_sqlManager.closeAll(null, stmt, res);
            m_sqlManager.closeAll(null, commit, null);           

            commit = null;
            stmt = null;
            res = null;
                
            conn.setAutoCommit(true);
                
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "writeFileContent resourceId=" + resourceId.toString(), CmsException.C_SERIALIZATION, e, false);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "writeFileContent resourceId=" + resourceId.toString(), CmsException.C_SQL_ERROR, e, false);
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
