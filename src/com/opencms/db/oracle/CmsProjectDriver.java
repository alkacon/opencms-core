/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/oracle/Attic/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/05/22 16:07:26 $
 * Version: $Revision: 1.2 $
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
 
package com.opencms.db.oracle;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsBackupProject;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import oracle.jdbc.driver.OracleResultSet;


/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @version $Revision: 1.2 $ $Date: 2003/05/22 16:07:26 $ *
 */
public class CmsProjectDriver extends com.opencms.db.generic.CmsProjectDriver implements I_CmsConstants, I_CmsLogChannels {

    /**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProjectDriver() {
    	
    }

    /**
     * Creates a serializable object in the systempropertys.
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Serializable addSystemProperty(String name, Serializable object) throws CmsException {
        byte[] value;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        Connection conn = null;
        ResultSet res = null;
        try {
            int id = m_sqlManager.nextId(C_TABLE_SYSTEMPROPERTIES);
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();

            // create the object
            // first insert the new systemproperty with empty systemproperty_value, then update
            // the systemproperty_value. These two steps are necessary because of using Oracle BLOB
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_FORINSERT");
            stmt.setInt(1, id);
            stmt.setString(2, name);
            //statement.setBytes(3,value);
            stmt.executeUpdate();
            //statement.close();
            // now update the systemproperty_value
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_FORUPDATE");
            stmt2.setInt(1, id);
            conn.setAutoCommit(false);
            res = stmt2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
                ByteArrayInputStream instream = new ByteArrayInputStream(value);
                OutputStream outstream = blob.getBinaryOutputStream();
                byte[] chunk = new byte[blob.getChunkSize()];
                int i = -1;
                while ((i = instream.read(chunk)) != -1) {
                    outstream.write(chunk, 0, i);
                }
                instream.close();
                outstream.close();
            }
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();
            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {           
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc){
                }
                try {
                    //nextStmt = conn.prepareStatement(m_sqlManager.get("C_ROLLBACK"));
                    nextStmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get("C_ROLLBACK"));
                    nextStmt.execute();
                } catch (SQLException exc){
                    // nothing to do here
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return readSystemProperty(name);
    }


// methods working with session-storage

    /**
     * This method creates a new session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public void createSession(String sessionId, Hashtable data) throws CmsException {
        byte[] value = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        Connection conn = null;
        ResultSet res = null;
        try {
            // serialize the hashtable
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(data);
            oout.close();
            value = bout.toByteArray();

            // write data to database
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SESSION_FORINSERT");
            stmt.setString(1, sessionId);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            stmt.close();
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SESSION_FORUPDATE");
            stmt2.setString(1, sessionId);
            conn.setAutoCommit(false);
            res = stmt2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
                ByteArrayInputStream instream = new ByteArrayInputStream(value);
                OutputStream outstream = blob.getBinaryOutputStream();
                byte[] chunk = new byte[blob.getChunkSize()];
                int i = -1;
                while ((i = instream.read(chunk)) != -1) {
                    outstream.write(chunk, 0, i);
                }
                instream.close();
                outstream.close();
            }
            stmt2.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();
            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                }
                try {
                    //nextStmt = conn.prepareStatement(m_sqlManager.get("C_ROLLBACK"));
                    nextStmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get("C_ROLLBACK"));
                    nextStmt.execute();
                } catch (SQLException se) {
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * Returns all projects from the history.
     *
     *
     * @return a Vector of projects.
     */
     public Vector getAllBackupProjects() throws CmsException {
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
             while(res.next()) {
                 Vector resources = m_driverManager.getVfsDriver().readBackupProjectResources(res.getInt("VERSION_ID"));
                 projects.addElement( new CmsBackupProject(res.getInt("VERSION_ID"),
                                                    res.getInt("PROJECT_ID"),
                                                    res.getString("PROJECT_NAME"),
                                                    SqlHelper.getTimestamp(res,"PROJECT_PUBLISHDATE"),
                                                    new CmsUUID(res.getString("PROJECT_PUBLISHED_BY")),
                                                    res.getString("PROJECT_PUBLISHED_BY_NAME"),
                                                    res.getString("PROJECT_DESCRIPTION"),
                                                    res.getInt("TASK_ID"),
                                                    new CmsUUID(res.getString("USER_ID")),
                                                    res.getString("USER_NAME"),
                                                    new CmsUUID(res.getString("GROUP_ID")),
                                                    res.getString("GROUP_NAME"),
                                                    new CmsUUID(res.getString("MANAGERGROUP_ID")),
                                                    res.getString("MANAGERGROUP_NAME"),
                                                    SqlHelper.getTimestamp(res,"PROJECT_CREATEDATE"),
                                                    res.getInt("PROJECT_TYPE"),
                                                    resources));
             }
         } catch( SQLException exc ) {
             throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
         } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
         }
         return(projects);
     }
     
    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new com.opencms.db.oracle.CmsSqlManager(dbPoolUrl);
    }



    /**
     * This method updates a session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public int updateSession(String sessionId, Hashtable data) throws CmsException {

        byte[] value = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        PreparedStatement trimStmt = null;
        Connection conn = null;
        int retValue;
        ResultSet res = null;
        try {
            // serialize the hashtable
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(data);
            oout.close();
            value = bout.toByteArray();

            // write data to database in two steps because of using Oracle BLOB
            // first update the session_time
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SESSION_UPDATE");
            stmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setString(2, sessionId);
            retValue = stmt.executeUpdate();
            stmt.close();
            // now update the session_data
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SESSION_FORUPDATE");
            stmt2.setString(1, sessionId);
            conn.setAutoCommit(false);
            res = stmt2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
                // first trim the blob to 0 bytes, otherwise there could be left some bytes
                // of the old content
                trimStmt = m_sqlManager.getPreparedStatement(conn, "C_TRIMBLOB");
                trimStmt.setBlob(1, blob);
                trimStmt.setInt(2, 0);
                trimStmt.execute();
                ByteArrayInputStream instream = new ByteArrayInputStream(value);
                OutputStream outstream = blob.getBinaryOutputStream();
                byte[] chunk = new byte[blob.getChunkSize()];
                int i = -1;
                while ((i = instream.read(chunk)) != -1) {
                    outstream.write(chunk, 0, i);
                }
                instream.close();
                outstream.close();
            }
            stmt2.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();
            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStmt = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    nextStmt.execute();
                } catch (SQLException se) {
                }
            }
            m_sqlManager.closeAll(null, trimStmt, null);
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return retValue;
    }

    /**
     * Writes a serializable object to the systemproperties.
     *
     * @param name The name of the property.
     * @param object The property-object.
     *
     * @return object The property-object.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Serializable writeSystemProperty(String name, Serializable object) throws CmsException {

        PreparedStatement stmt = null;
        PreparedStatement nextStmt = null;
        PreparedStatement trimStmt = null;
        ResultSet res = null;
        Connection conn = null;
        byte[] value = null;
        try {
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_NAMEFORUPDATE");
            stmt.setString(1, name);
            conn.setAutoCommit(false);
            res = stmt.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
                // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                // of the old content

                trimStmt = m_sqlManager.getPreparedStatement(conn, "C_TRIMBLOB");
                trimStmt.setBlob(1, blob);
                trimStmt.setInt(2, 0);
                trimStmt.execute();
                trimStmt.close();
                ByteArrayInputStream instream = new ByteArrayInputStream(value);
                OutputStream outstream = blob.getBinaryOutputStream();
                byte[] chunk = new byte[blob.getChunkSize()];
                int i = -1;
                while ((i = instream.read(chunk)) != -1) {
                    outstream.write(chunk, 0, i);
                }
                instream.close();
                outstream.close();
            }
            stmt.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();
            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {           
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException se) {
                }
                try {
                    nextStmt = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    nextStmt.execute();
                } catch (SQLException exc){
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, trimStmt, res);
        }
        return readSystemProperty(name);
    }
    

    /**
     * Destroys this access-module
     * @throws throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] Destroyed");
        }
    }
  
}