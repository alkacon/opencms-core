/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oraclesql/Attic/CmsVfsAccess.java,v $
 * Date   : $Date: 2003/05/20 10:45:32 $
 * Version: $Revision: 1.4 $
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
package com.opencms.file.oraclesql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.flex.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.driver.OracleResultSet;
import source.org.apache.java.util.Configurations;

/**
 * Oracle/OCI implementation of the VFS access methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $ $Date: 2003/05/20 10:45:32 $
 */
public class CmsVfsAccess extends com.opencms.file.genericSql.CmsVfsAccess implements I_CmsConstants, I_CmsLogChannels {

    /**
     * Default constructor.
     * 
     * @param config the configurations objects (-> opencms.properties)
     * @param theResourceBroker the instance of the resource broker
     */
    public CmsVfsAccess(Configurations config, I_CmsResourceBroker theResourceBroker) {
        super(config, theResourceBroker);
    }

    /**
     * Creates the content entry for a file
     * 
     * @param fileId The ID of the new file
     * @param fileContent The content of the new file
     * @param versionId For the content of a backup file you need to insert the versionId of the backup
     * @param usedPool The name of the databasepool to use
     * @param usedStatement Specifies which tables must be used: offline, online or backup
     * 
     */
    public void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            if (writeBackup) {
                conn = m_SqlQueries.getConnectionForBackup();
                stmt = m_SqlQueries.getPreparedStatement(conn, "C_ORACLE_FILESFORINSERT_BACKUP");
            }
            else {
                conn = m_SqlQueries.getConnection(projectId);
                stmt = m_SqlQueries.getPreparedStatement(conn, projectId, "C_ORACLE_FILESFORINSERT");
            }
            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            stmt.setString(1, fileId.toString());
            if (writeBackup) {
                stmt.setInt(2, versionId);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, null);
        }

        // now update the file content
        writeFileContent(fileId, fileContent, projectId, writeBackup);
    }

    public com.opencms.file.genericSql.CmsQueries initQueries(Configurations config) {
        com.opencms.file.oraclesql.CmsQueries queries = new com.opencms.file.oraclesql.CmsQueries();
        queries.initJdbcPoolUrls(config);

        return queries;
    }

    /**
     * Private helper method to read the fileContent for publishProject(export).
     *
     * @param fileId the fileId.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public byte[] readFileContent(int projectId, int fileId) throws CmsException {
        //System.out.println("PL/SQL: readFileContent");
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        byte[] returnValue = null;
        try {
            // read fileContent from database
            conn = m_SqlQueries.getConnection(projectId);
            stmt = m_SqlQueries.getPreparedStatement(conn, projectId, "C_FILE_READ");
            stmt.setInt(1, fileId);
            res = stmt.executeQuery();
            if (res.next()) {
                returnValue = m_SqlQueries.getBytes(res, m_SqlQueries.get("C_RESOURCES_FILE_CONTENT"));
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFileContent/1]" + fileId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, res);
        }

        return returnValue;
    }

    /**
     * Writes the file content of an existing file
     * 
     * @param fileId The ID of the file to update
     * @param fileContent The new content of the file
     * @param usedPool The name of the database pool to use
     * @param usedStatement Specifies which tables must be used: offline, online or backup
     */
    public void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException {
        PreparedStatement stmt = null;
        PreparedStatement nextStatement = null;
        PreparedStatement trimStatement = null;
        Connection conn = null;
        ResultSet res = null;
        try {
            if (writeBackup) {
                conn = m_SqlQueries.getConnectionForBackup();
                stmt = m_SqlQueries.getPreparedStatement(conn, "C_ORACLE_FILESFORUPDATE_BACKUP");
            }
            else {
                conn = m_SqlQueries.getConnection(projectId);
                stmt = m_SqlQueries.getPreparedStatement(conn, projectId, "C_ORACLE_FILESFORUPDATE");
            }
            
            
            // update the file content in the FILES database.
            stmt.setString(1, fileId.toString());
            conn.setAutoCommit(false);
            res = stmt.executeQuery();
            try {
                while (res.next()) {
                    oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("FILE_CONTENT");
                    // first trim the blob to 0 bytes, otherwise there could be left some bytes
                    // of the old content
                    trimStatement = conn.prepareStatement(m_SqlQueries.get("C_TRIMBLOB"));
                    trimStatement.setBlob(1, blobnew);
                    trimStatement.setInt(2, 0);
                    trimStatement.execute();
                    ByteArrayInputStream instream = new ByteArrayInputStream(fileContent);
                    OutputStream outstream = blobnew.getBinaryOutputStream();
                    byte[] chunk = new byte[blobnew.getChunkSize()];
                    int i = -1;
                    while ((i = instream.read(chunk)) != -1) {
                        outstream.write(chunk, 0, i);
                    }
                    instream.close();
                    outstream.close();
                }
                // for the oracle-driver commit or rollback must be executed manually
                // because setAutoCommit = false in CmsDbPool.CmsDbPool
                nextStatement = conn.prepareStatement(m_SqlQueries.get("C_COMMIT"));
                nextStatement.execute();
                nextStatement.close();
                conn.setAutoCommit(true);
            } catch (IOException e) {
                throw m_SqlQueries.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
            }
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, res);
            m_SqlQueries.closeAll(null, nextStatement, null);
            m_SqlQueries.closeAll(null, trimStatement, null);
        }
    }

}
