/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oraclesql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2002/09/16 12:52:41 $
* Version: $Revision: 1.4 $
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

import oracle.jdbc.driver.*;

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.security.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.file.utils.*;
import com.opencms.util.*;
import com.opencms.report.*;


/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.4 $ $Date: 2002/09/16 12:52:41 $ *
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {

    /**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public CmsDbAccess(Configurations config)
        throws CmsException {

        super(config);
    }

    /**
     * Creates a serializable object in the systempropertys.
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @exception CmsException Throws CmsException if something goes wrong.
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
            statement = con.prepareStatement(m_cq.get("C_ORACLE_SYSTEMPROPERTIES_FORINSERT"));
            statement.setInt(1, id);
            statement.setString(2, name);
            //statement.setBytes(3,value);
            statement.executeUpdate();
            //statement.close();
            // now update the systemproperty_value
            statement2 = con.prepareStatement(m_cq.get("C_ORACLE_SYSTEMPROPERTIES_FORUPDATE"));
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
            nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
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
    /**
     * Adds a user to the database.
     *
     * @param name username
     * @param password user-password
     * @param description user-description
     * @param firstname user-firstname
     * @param lastname user-lastname
     * @param email user-email
     * @param lastlogin user-lastlogin
     * @param lastused user-lastused
     * @param flags user-flags
     * @param additionalInfos user-additional-infos
     * @param defaultGroup user-defaultGroup
     * @param address user-defauladdress
     * @param section user-section
     * @param type user-type
     *
     * @return the created user.
     * @exception thorws CmsException if something goes wrong.
     */
    public CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        int id = nextId(C_TABLE_USERS);
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
            oout.writeObject(additionalInfos);
            oout.close();
            value = bout.toByteArray();

            // write data to database
            // first insert the data without user_info
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_cq.get("C_ORACLE_USERSFORINSERT"));
            statement.setInt(1, id);
            statement.setString(2, name);
            // crypt the password with MD5
            statement.setString(3, digest(password));
            statement.setString(4, digest(""));
            statement.setString(5, checkNull(description));
            statement.setString(6, checkNull(firstname));
            statement.setString(7, checkNull(lastname));
            statement.setString(8, checkNull(email));
            statement.setTimestamp(9, new Timestamp(lastlogin));
            statement.setTimestamp(10, new Timestamp(lastused));
            statement.setInt(11, flags);
            //statement.setBytes(12,value);
            statement.setInt(12, defaultGroup.getId());
            statement.setString(13, checkNull(address));
            statement.setString(14, checkNull(section));
            statement.setInt(15, type);
            statement.executeUpdate();
            statement.close();
            // now update user_info of the new user
            statement2 = con.prepareStatement(m_cq.get("C_ORACLE_USERSFORUPDATE"));
            statement2.setInt(1, id);
            con.setAutoCommit(false);
            res = statement2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("USER_INFO");
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
            nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
            nextStatement.execute();
            nextStatement.close();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
                    nextStatement.execute();
                } catch (SQLException se) {
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
                } catch (SQLException se) {
                }
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
        return readUser(id);
    }

    /**
     * Adds a user to the database.
     *
     * @param name username
     * @param password user-password
     * @param recoveryPassword user-recoveryPassword
     * @param description user-description
     * @param firstname user-firstname
     * @param lastname user-lastname
     * @param email user-email
     * @param lastlogin user-lastlogin
     * @param lastused user-lastused
     * @param flags user-flags
     * @param additionalInfos user-additional-infos
     * @param defaultGroup user-defaultGroup
     * @param address user-defauladdress
     * @param section user-section
     * @param type user-type
     *
     * @return the created user.
     * @exception thorws CmsException if something goes wrong.
     */
    public CmsUser addImportUser(String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        int id = nextId(C_TABLE_USERS);
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
            oout.writeObject(additionalInfos);
            oout.close();
            value = bout.toByteArray();

            // write data to database
            // first insert the data without user_info
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_cq.get("C_ORACLE_USERSFORINSERT"));
            statement.setInt(1, id);
            statement.setString(2, name);
            // crypt the password with MD5
            statement.setString(3, checkNull(password));
            statement.setString(4, checkNull(recoveryPassword));
            statement.setString(5, checkNull(description));
            statement.setString(6, checkNull(firstname));
            statement.setString(7, checkNull(lastname));
            statement.setString(8, checkNull(email));
            statement.setTimestamp(9, new Timestamp(lastlogin));
            statement.setTimestamp(10, new Timestamp(lastused));
            statement.setInt(11, flags);
            //statement.setBytes(12,value);
            statement.setInt(12, defaultGroup.getId());
            statement.setString(13, checkNull(address));
            statement.setString(14, checkNull(section));
            statement.setInt(15, type);
            statement.executeUpdate();
            statement.close();
            // now update user_info of the new user
            statement2 = con.prepareStatement(m_cq.get("C_ORACLE_USERSFORUPDATE"));
            statement2.setInt(1, id);
            con.setAutoCommit(false);
            res = statement2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("USER_INFO");
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
            nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
            nextStatement.execute();
            nextStatement.close();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
                    nextStatement.execute();
                } catch (SQLException se) {
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
                } catch (SQLException se) {
                }
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
        return readUser(id);
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
            statement = con.prepareStatement(m_cq.get("C_ORACLE_SESSION_FORINSERT"));
            statement.setString(1, sessionId);
            statement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            statement.close();
            statement2 = con.prepareStatement(m_cq.get("C_ORACLE_SESSION_FORUPDATE"));
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
            nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
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
             statement = con.prepareStatement(m_cq.get("C_ORACLE_PROJECTS_READLAST_BACKUP"));
             statement.setInt(1, 300);
             res = statement.executeQuery();
             while(res.next()) {
                 Vector resources = readBackupProjectResources(res.getInt("VERSION_ID"));
                 projects.addElement( new CmsBackupProject(res.getInt("VERSION_ID"),
                                                    res.getInt("PROJECT_ID"),
                                                    res.getString("PROJECT_NAME"),
                                                    SqlHelper.getTimestamp(res,"PROJECT_PUBLISHDATE"),
                                                    res.getInt("PROJECT_PUBLISHED_BY"),
                                                    res.getString("PROJECT_PUBLISHED_BY_NAME"),
                                                    res.getString("PROJECT_DESCRIPTION"),
                                                    res.getInt("TASK_ID"),
                                                    res.getInt("USER_ID"),
                                                    res.getString("USER_NAME"),
                                                    res.getInt("GROUP_ID"),
                                                    res.getString("GROUP_NAME"),
                                                    res.getInt("MANAGERGROUP_ID"),
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
    protected com.opencms.file.genericSql.CmsQueries getQueries()
    {
        return new com.opencms.file.oraclesql.CmsQueries();
    }


    /**
     * Private helper method to read the fileContent for publishProject(export).
     *
     * @param fileId the fileId.
     *
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    protected byte[] readFileContent(int projectId, int fileId)
        throws CmsException {
        //System.out.println("PL/SQL: readFileContent");
        PreparedStatement statement = null;
        Connection con = null;
        ResultSet res = null;
        byte[] returnValue = null;
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        String usedPool;
        String usedStatement;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }

        try {
            // read fileContent from database
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_cq.get("C_FILE_READ"+usedStatement));
            statement.setInt(1,fileId);
            res = statement.executeQuery();
            if (res.next()) {
                returnValue = getBytesFromResultset(res, m_cq.get("C_RESOURCES_FILE_CONTENT"));
            } else {
                throw new CmsException("["+this.getClass().getName()+"]"+fileId,CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException se) {
                }
            }
            if( statement != null) {
                try {
                    statement.close();
                } catch (SQLException exc) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException se) {
                }
            }
        }
        return returnValue;
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
            statement = con.prepareStatement(m_cq.get("C_ORACLE_SESSION_UPDATE"));
            statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.setString(2, sessionId);
            retValue = statement.executeUpdate();
            statement.close();
            // now update the session_data
            statement2 = con.prepareStatement(m_cq.get("C_ORACLE_SESSION_FORUPDATE"));
            statement2.setString(1, sessionId);
            con.setAutoCommit(false);
            res = statement2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
                // first trim the blob to 0 bytes, otherwise there could be left some bytes
                // of the old content
                trimStatement = con.prepareStatement(m_cq.get("C_TRIMBLOB"));
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
            nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
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
     * @exception CmsException Throws CmsException if something goes wrong.
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
            statement = con.prepareStatement(m_cq.get("C_ORACLE_SYSTEMPROPERTIES_NAMEFORUPDATE"));
            statement.setString(1, name);
            con.setAutoCommit(false);
            res = statement.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
                // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                // of the old content

                trimStatement = con.prepareStatement(m_cq.get("C_TRIMBLOB"));
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
            nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
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
     * Writes a user to the database.
     *
     * @param user the user to write
     * @exception thorws CmsException if something goes wrong.
     */
    public void writeUser(CmsUser user) throws CmsException {

        byte[] value = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement nextStatement = null;
        PreparedStatement trimStatement = null;

        ResultSet res = null;
        Connection con = null;
        try {
            // serialize the hashtable
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(user.getAdditionalInfo());
            oout.close();
            value = bout.toByteArray();
            // write data to database
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_cq.get("C_ORACLE_USERSWRITE"));
            statement.setString(1, checkNull(user.getDescription()));
            statement.setString(2, checkNull(user.getFirstname()));
            statement.setString(3, checkNull(user.getLastname()));
            statement.setString(4, checkNull(user.getEmail()));
            statement.setTimestamp(5, new Timestamp(user.getLastlogin()));
            statement.setTimestamp(6, new Timestamp(user.getLastUsed()));
            statement.setInt(7, user.getFlags());
            statement.setInt(8, user.getDefaultGroupId());
            statement.setString(9, checkNull(user.getAddress()));
            statement.setString(10, checkNull(user.getSection()));
            statement.setInt(11, user.getType());
            statement.setInt(12, user.getId());
            statement.executeUpdate();
            statement.close();
            // update user_info in this special way because of using blob
            statement2 = con.prepareStatement(m_cq.get("C_ORACLE_USERSFORUPDATE"));
            statement2.setInt(1, user.getId());
            con.setAutoCommit(false);
            res = statement2.executeQuery();
            try {
                while (res.next()) {
                    oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("USER_INFO");
                    // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                    // of the old content
                    //trimStatement = (OraclePreparedStatement) con.prepareStatement(cq.get("C_TRIMBLOB);
                    trimStatement = con.prepareStatement(m_cq.get("C_TRIMBLOB"));
                    //trimStatement.setBLOB(1, blobnew);
                    trimStatement.setBlob(1, blobnew);
                    trimStatement.setInt(2, 0);
                    trimStatement.execute();
                    trimStatement.close();
                    ByteArrayInputStream instream = new ByteArrayInputStream(value);
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
                nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
                nextStatement.execute();
                nextStatement.close();
                con.setAutoCommit(true);
            } catch (IOException e) {
                throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
            }
            statement2.close();
            res.close();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
        } finally {
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
                    nextStatement = con.prepareStatement(m_cq.get("C_ROLLBACK"));
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
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException se) {
                }
            }
        }
    }

    /**
     * Destroys this access-module
     * @exception throws CmsException if something goes wrong.
     */
    public void destroy()
        throws CmsException {
        try {
            ((com.opencms.dbpool.CmsDriver) DriverManager.getDriver(m_poolName)).destroy();
        } catch(SQLException exc) {
            // destroy not possible - ignoring the exception
        }

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] shutdown complete.");
        }
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
    protected void createFileContent(int fileId, byte[] fileContent, int versionId,String usedPool, String usedStatement) throws CmsException{
        PreparedStatement statement = null;
        Connection con = null;
        try{
            con = DriverManager.getConnection(usedPool);
            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            statement = con.prepareStatement(m_cq.get("C_ORACLE_FILESFORINSERT"+usedStatement));
            statement.setInt(1, fileId);
            if("_BACKUP".equals(usedStatement)){
                statement.setInt(2, versionId);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
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
        // now update the file content
        writeFileContent(fileId, fileContent, usedPool, usedStatement);      
    }
     
    /**
     * Writes the file content of an existing file
     * 
     * @param fileId The ID of the file to update
     * @param fileContent The new content of the file
     * @param usedPool The name of the database pool to use
     * @param usedStatement Specifies which tables must be used: offline, online or backup
     */
    protected void writeFileContent(int fileId, byte[] fileContent, String usedPool, String usedStatement) throws CmsException{
        PreparedStatement statement = null;
        PreparedStatement nextStatement = null;
        PreparedStatement trimStatement = null;
        Connection con = null;
        ResultSet res = null;
        try{
            // update the file content in the FILES database.
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_cq.get("C_ORACLE_FILESFORUPDATE"+usedStatement));
            statement.setInt(1, fileId);
            con.setAutoCommit(false);
            res = statement.executeQuery();
            try {
                while (res.next()) {
                    oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("FILE_CONTENT");
                    // first trim the blob to 0 bytes, otherwise there could be left some bytes
                    // of the old content
                    trimStatement = con.prepareStatement(m_cq.get("C_TRIMBLOB"));
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
                nextStatement = con.prepareStatement(m_cq.get("C_COMMIT"));
                nextStatement.execute();
                nextStatement.close();
                con.setAutoCommit(true);
            } catch (IOException e) {
                throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
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
            if(nextStatement != null) {
                try {
                    nextStatement.close();
                } catch(SQLException exc) {
                     // nothing to do here
                }
            }
            if(trimStatement != null) {
                try {
                    trimStatement.close();
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
    }
    
    /**
     * Returns the bytes from a result set
     * 
     * @param res The ResultSet to read from
     * @param columnName The name of the column to read from
     * 
     * @return The byte value from the column
     */
    protected byte[] getBytesFromResultset(ResultSet res, String columnName) throws SQLException{
        // read the bytes from an oracle blob
        oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(columnName);
        byte[] content = new byte[(int) blob.length()];
        content = blob.getBytes(1, (int) blob.length());
        return content;
    }  
}
