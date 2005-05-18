/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsVfsDriver.java,v $
 * Date   : $Date: 2005/05/18 08:41:34 $
 * Version: $Revision: 1.32 $
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsProject;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
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
 * @version $Revision: 1.32 $ $Date: 2005/05/18 08:41:34 $
 * @since 5.1
 */
public class CmsVfsDriver extends org.opencms.db.generic.CmsVfsDriver {     

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, byte[], int)
     */
    public void createContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content, int versionId) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {            
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ORACLE_CONTENTS_ADD");
            
            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, resourceId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        // now update the file content
        writeContent(dbc, project, resourceId, content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, byte[])
     */
    public void writeContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        Connection conn = null;
        ResultSet res = null;
        
        boolean wasInTransaction = false;
        try {            
            conn = m_sqlManager.getConnection(dbc, project.getId());
            if (conn.getMetaData().getDriverMajorVersion()<9) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ORACLE8_CONTENTS_UPDATECONTENT");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ORACLE_CONTENTS_UPDATECONTENT");
            }
            
            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }
            
            // update the file content in the contents table
            stmt.setString(1, resourceId.toString());
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.LOG_READING_RESOURCE_1, resourceId));
            }
            
            // write file content 
            OutputStream output = CmsUserDriver.getOutputStreamFromBlob(res, "FILE_CONTENT");
            output.write(content);
            output.close();
                
            if (!wasInTransaction) {
                commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
                commit.execute();
                m_sqlManager.closeAll(dbc, null, commit, null); 
            }
            
            m_sqlManager.closeAll(dbc, null, stmt, res);          

            commit = null;
            stmt = null;
            res = null;
                
            if (!wasInTransaction) {
                conn.setAutoCommit(true);
            }
                
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_WRITING_TO_OUTPUT_STREAM_1, resourceId), e);
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)), e);
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
