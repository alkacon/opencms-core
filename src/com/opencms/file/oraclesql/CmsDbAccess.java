/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oraclesql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2003/05/15 12:39:34 $
* Version: $Revision: 1.10 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file.oraclesql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import oracle.jdbc.driver.OracleResultSet;
import source.org.apache.java.util.Configurations;


/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.10 $ $Date: 2003/05/15 12:39:34 $ *
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {

    /**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsDbAccess(Configurations config, I_CmsResourceBroker theResourceBroker)
        throws CmsException {

        super(config,theResourceBroker);
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
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement nextStatement = null;
        Connection con = null;
        ResultSet res = null;
        try {
            int id = nextId(C_TABLE_SYSTEMPROPERTIES);
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();

            // create the object
            // first insert the new systemproperty with empty systemproperty_value, then update
            // the systemproperty_value. These two steps are necessary because of using Oracle BLOB
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SYSTEMPROPERTIES_FORINSERT"));
            statement.setInt(1, id);
            statement.setString(2, name);
            //statement.setBytes(3,value);
            statement.executeUpdate();
            //statement.close();
            // now update the systemproperty_value
            statement2 = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SYSTEMPROPERTIES_FORUPDATE"));
            statement2.setInt(1, id);
            con.setAutoCommit(false);
            res = statement2.executeQuery();
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
            nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
            nextStatement.execute();
            nextStatement.close();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + CmsException.C_SERIALIZATION, e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException se) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException exc){
                }
            }
            if (statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException exc){
                }
                try {
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
                    nextStatement.execute();
                } catch (SQLException exc){
                    // nothing to do here
                }
            }
            if (nextStatement != null) {
                try {
                    nextStatement.close();
                } catch (SQLException exc){
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException exc){
                    // nothing to do here
                }
                try {
                    con.close();
                } catch (SQLException e){
                }
            }
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
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement nextStatement = null;
        Connection con = null;
        ResultSet res = null;
        try {
            // serialize the hashtable
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(data);
            oout.close();
            value = bout.toByteArray();

            // write data to database
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SESSION_FORINSERT"));
            statement.setString(1, sessionId);
            statement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            statement.close();
            statement2 = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SESSION_FORUPDATE"));
            statement2.setString(1, sessionId);
            con.setAutoCommit(false);
            res = statement2.executeQuery();
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
            statement2.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false
            nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
            nextStatement.execute();
            nextStatement.close();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[" + this.getClass().getName() + "]:" + CmsException.C_SERIALIZATION, e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException se) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException exc) {
                }
            }
            if (statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
                    nextStatement.execute();
                } catch (SQLException se) {
                }
            }
            if (nextStatement != null) {
                try {
                    nextStatement.close();
                } catch (SQLException exc) {
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException se) {
                }
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
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
         PreparedStatement statement = null;
         Connection con = null;

         try {
             // create the statement
             con = DriverManager.getConnection(m_poolNameBackup);
             statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_PROJECTS_READLAST_BACKUP"));
             statement.setInt(1, 300);
             res = statement.executeQuery();
             while(res.next()) {
                 Vector resources = m_ResourceBroker.getVfsAccess().readBackupProjectResources(res.getInt("VERSION_ID"));
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
             throw new CmsException("[" + this.getClass().getName() + ".getAllBackupProjects()] " + exc.getMessage(),
                 CmsException.C_SQL_ERROR, exc);
         } finally {
            // close all db-resources
            if(res != null) {
                 try {
                     res.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(statement != null) {
                 try {
                     statement.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(con != null) {
                 try {
                     con.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
         }
         return(projects);
     }
     
    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.file.genericSql.CmsQueries initQueries(Configurations config)
    {
        m_SqlQueries = new com.opencms.file.oraclesql.CmsQueries();
        m_SqlQueries.initJdbcPoolUrls(config);         
                        
        return new com.opencms.file.oraclesql.CmsQueries();
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
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement nextStatement = null;
        PreparedStatement trimStatement = null;
        Connection con = null;
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
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SESSION_UPDATE"));
            statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.setString(2, sessionId);
            retValue = statement.executeUpdate();
            statement.close();
            // now update the session_data
            statement2 = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SESSION_FORUPDATE"));
            statement2.setString(1, sessionId);
            con.setAutoCommit(false);
            res = statement2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
                // first trim the blob to 0 bytes, otherwise there could be left some bytes
                // of the old content
                trimStatement = con.prepareStatement(m_SqlQueries.get("C_TRIMBLOB"));
                trimStatement.setBlob(1, blob);
                trimStatement.setInt(2, 0);
                trimStatement.execute();
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
            statement2.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
            nextStatement.execute();
            nextStatement.close();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[" + this.getClass().getName() + "]:" + CmsException.C_SERIALIZATION, e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException se) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException exc) {
                }
            }
            if (statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
                    nextStatement.execute();
                } catch (SQLException se) {
                }
            }
            if (nextStatement != null) {
                try {
                    nextStatement.close();
                } catch (SQLException exc) {
                }
            }
            if (trimStatement != null) {
                try {
                    trimStatement.close();
                } catch (SQLException exc) {
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException se) {
                }
                try {
                    con.close();
                } catch (SQLException se) {
                }
            }
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

        PreparedStatement statement = null;
        PreparedStatement nextStatement = null;
        PreparedStatement trimStatement = null;
        ResultSet res = null;
        Connection con = null;
        byte[] value = null;
        try {
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_SYSTEMPROPERTIES_NAMEFORUPDATE"));
            statement.setString(1, name);
            con.setAutoCommit(false);
            res = statement.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
                // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                // of the old content

                trimStatement = con.prepareStatement(m_SqlQueries.get("C_TRIMBLOB"));
                trimStatement.setBlob(1, blob);
                trimStatement.setInt(2, 0);
                trimStatement.execute();
                trimStatement.close();
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
            statement.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
            nextStatement.execute();
            nextStatement.close();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + CmsException.C_SERIALIZATION, e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException se) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException se) {
                }
                try {
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
                    nextStatement.execute();
                } catch (SQLException exc){
                }
            }
            if (nextStatement != null) {
                try {
                    nextStatement.close();
                } catch (SQLException exc) {
                }
            }
            if (trimStatement != null) {
                try {
                    trimStatement.close();
                } catch (SQLException exc) {
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException se) {
                }
                try {
                    con.close();
                } catch (SQLException se) {
                }
            }
        }
        return readSystemProperty(name);
    }
    

    /**
     * Destroys this access-module
     * @throws throws CmsException if something goes wrong.
     */
    public void destroy()
        throws CmsException {
        try {
            ((com.opencms.dbpool.CmsDriver) DriverManager.getDriver(m_poolName)).destroy();
        } catch(SQLException exc) {
            // destroy not possible - ignoring the exception
        }

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[oraclesql.CmsDbAccess] Destroyed");
        }
    }
  
}
