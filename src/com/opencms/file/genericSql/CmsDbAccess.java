/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2003/05/07 11:43:26 $
* Version: $Revision: 1.279 $
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

package com.opencms.file.genericSql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.*;
import com.opencms.file.utils.CmsAccessFilesystem;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.report.I_CmsReport;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;


/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @author Finn Nielsen
 * @author Mark Foley
 * @version $Revision: 1.279 $ $Date: 2003/05/07 11:43:26 $ *
 */
public class CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {
    
    public static int C_RESTYPE_LINK_ID = 2;
    public static boolean C_USE_TARGET_DATE = true;    

    /**
     * The name of the pool to use
     */
    protected String m_poolName;

    /**
     * The name of the online pool to use
     */
    protected String m_poolNameOnline;

    /**
     * The name of the backup pool to use
     */
    protected String m_poolNameBackup;

    /**
     * The session-timeout value:
     * currently six hours. After that time the session can't be restored.
     */
    public static long C_SESSION_TIMEOUT = 6 * 60 * 60 * 1000;

    /**
     * The maximum amount of tables.
     */
    protected static int C_MAX_TABLES = 18;

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_SYSTEMPROPERTIES = "CMS_SYSTEMPROPERTIES";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_GROUPS = "CMS_GROUPS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_GROUPUSERS = "CMS_GROUPUSERS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_USERS = "CMS_USERS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROJECTS = "CMS_PROJECTS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_RESOURCES = "CMS_RESOURCES";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_FILES = "CMS_FILES";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROPERTYDEF = "CMS_PROPERTYDEF";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROPERTIES = "CMS_PROPERTIES";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASK = "CMS_TASKS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASKTYPE = "CMS_TASKTYPE";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASKPAR = "CMS_TASKPAR";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASKLOG = "CMS_TASKLOG";

    /**
     * Constant to get property from configurations.
     */
    protected static String C_CONFIGURATIONS_DIGEST = "digest";

    /**
     * Constant to get property from configurations.
     */
    protected static String C_CONFIGURATIONS_DIGEST_FILE_ENCODING = "digest.fileencoding";

    /**
     * Constant to get property from configurations.
     */
    protected static String C_CONFIGURATIONS_POOL = "pool";

    /**
     * A array containing all max-ids for the tables.
     */
    protected int[] m_maxIds;



    /**
     * Storage for all exportpoints.
     */
    //protected Hashtable m_exportpointStorage=null;

   /**
     * 'Constants' file.
     */
   protected com.opencms.file.genericSql.CmsQueries m_SqlQueries;
   
   protected I_CmsResourceBroker m_ResourceBroker;

   /**
    * TESTFIX (mfoley@iee.org) New Code:
    * Performs an Oracle-safe setBytes() action.
    * @param statement The PreparedStatement.
    * @param posn The parameter placeholder in the prepared statement.
    * @param content The byte array to be inserted into the prepared statement.
    * @throws SQLException Throws SQLException if something goes wrong.
    */
   protected void m_doSetBytes(PreparedStatement statement, int posn, byte[] content)
        throws SQLException {
        if(content.length < 2000) {
            statement.setBytes(posn,content);
        } else {
            statement.setBinaryStream(posn, new ByteArrayInputStream(content), content.length);
        }
    }

    /**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsDbAccess(Configurations config, I_CmsResourceBroker theResourceBroker ) throws CmsException {
        
        m_ResourceBroker = theResourceBroker;

        // set configurations for the dbpool driver
        com.opencms.dbpool.CmsDriver.setConfigurations(config);

        m_SqlQueries = initQueries(config);

        String rbName = null;
        //String digest = null;
        boolean fillDefaults = true;

        if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database access init : ok" );
        }

        // read the name of the rb from the properties
        rbName = (String)config.getString(C_CONFIGURATION_RESOURCEBROKER);

        // read all needed parameters from the configuration
        // all needed pools are read in the following method
        getConnectionPools(config, rbName);

        /*
        digest = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_DIGEST, "MD5");
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Digest configured    : " + digest);
        }

        m_digestFileEncoding = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_DIGEST_FILE_ENCODING, "ISO-8859-1");
        if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Digest file encoding : " + m_digestFileEncoding);
        }

        // create the digest
        try {
            m_digest = MessageDigest.getInstance(digest);
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Using digest encoding: " + 
                    m_digest.getAlgorithm() + 
                    " from " + m_digest.getProvider().getName() +
                    " version " + m_digest.getProvider().getVersion());
            }
        } catch (NoSuchAlgorithmException e){
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Error setting digest : using clear passwords - " + e.getMessage());
            }
        }
        */

        // have we to fill the default resource like root and guest?
        try {
            if (readProject(C_PROJECT_ONLINE_ID) != null) {
                // online-project exists - no need of filling defaults
                fillDefaults = false;
            }
        } catch(CmsException exc) {
            // ignore the exception - the project was not readable so fill in the defaults
        }
        if(fillDefaults) {
            // YES!
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database fill default: yes");
            }
            fillDefaults();
        }
    }

    /**
     * Gets all necessary connection pools
     * This method can be adjusted for each resourcebroker
     * @param config The configuration
     */
    protected void getConnectionPools(Configurations config, String rbName){
        // get the standard pool
        m_poolName = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_POOL);
        // set the default pool for the id generator
        com.opencms.dbpool.CmsIdGenerator.setDefaultPool(m_poolName);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database offline pool: " + m_poolName);
        }
        //get the pool for the online resources
        m_poolNameOnline = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + ".online." + C_CONFIGURATIONS_POOL);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database online pool : " +  m_poolNameOnline);
        }
        //get the pool for the backup resources
        m_poolNameBackup = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + ".backup." + C_CONFIGURATIONS_POOL);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database backup pool : " + m_poolNameBackup);
        }
    }

    /**
     * Creates a serializable object in the systempropertys.
     *
     * @param name The name of the property.
     * @param object The property-object.
     *
     * @return object The property-object.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
     public Serializable addSystemProperty(String name, Serializable object)
         throws CmsException {

        byte[] value;
        Connection con = null;
        PreparedStatement statement = null;
         try    {
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();

            // create the object
            con = DriverManager.getConnection(m_poolName);
            statement=  con.prepareStatement(m_SqlQueries.get("C_SYSTEMPROPERTIES_WRITE"));
            statement.setInt(1,nextId(C_TABLE_SYSTEMPROPERTIES));
            statement.setString(2,name);
            // TESTFIX (mfoley@iee.org) Old Code: statement.setBytes(3,value);
            m_doSetBytes(statement,3,value);
            statement.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);
        }finally {
             // close all db-resources
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
        return readSystemProperty(name);
     }



    /**
     * Private helper method for publihing into the filesystem.
     * test if resource must be written to the filesystem
     *
     * @param filename Name of a resource in the OpenCms system.
     * @return key in exportpoint Hashtable or null.
     */
    protected String checkExport(String filename, Hashtable exportpoints){

        String key = null;
        String exportpoint = null;
        Enumeration e = exportpoints.keys();

        while (e.hasMoreElements()) {
          exportpoint = (String)e.nextElement();
          if (filename.startsWith(exportpoint)){
            return exportpoint;
          }
        }
        return key;
    }

    /**
     * Checks, if the String was null or is empty. If this is so it returns " ".
     * This is for oracle-issues, because in oracle an empty string is the same as null.
     * TODO: this method should be removed!
     * 
     * @param value the String to check.
     * @return the value, or " " if needed.
     * @deprecated
     */
    protected String checkNull(String value) {
        String ret = " ";
        if( (value != null) && (value.length() != 0) ) {
            ret = value;
        }
        return ret;
    }

    /**
     * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
     *
     *
     */
    protected void clearFilesTable() throws CmsException {
        Connection con = null;
        PreparedStatement statementDelete = null;

        try {
            con = DriverManager.getConnection(m_poolName);

            statementDelete = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_DELETE_LOST_ID"));
            statementDelete.executeUpdate();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
            if(statementDelete != null) {
                 try {
                     statementDelete.close();
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
     * Counts the locked resources in this project.
     *
     * @param project The project to be unlocked.
     * @return the amount of locked resources in this project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int countLockedResources(CmsProject project)
        throws CmsException {

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet res = null;
        int retValue;
        String usedPool;
        String usedStatement;
        //int onlineProject = getOnlineProject(project.getId()).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (project.getId() == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }

        try {
            // create the statement
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_COUNTLOCKED"+usedStatement));
            statement.setInt(1,project.getId());
            res = statement.executeQuery();
            if(res.next()) {
                retValue = res.getInt(1);
            } else {
                retValue=0;
            }
        } catch( Exception exc ) {
            throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
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
        return retValue;
    }

    /**
     * Returns the amount of properties for a propertydefinition.
     *
     * @param metadef The propertydefinition to test.
     *
     * @return the amount of properties for a propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    protected int countProperties(CmsPropertydefinition metadef)
        throws CmsException {
        ResultSet result = null;
        PreparedStatement statement = null;
        Connection con = null;

        int returnValue;
        try {
            // create statement
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_READALL_COUNT"));
            statement.setInt(1, metadef.getId());
            result = statement.executeQuery();

            if( result.next() ) {
                returnValue = result.getInt(1) ;
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(),
                    CmsException.C_UNKNOWN_EXCEPTION);
            }
        } catch(SQLException exc) {
             throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
        }finally {
            // close all db-resources
            if(result != null) {
                 try {
                     result.close();
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
        return returnValue;
    }


    /**
     * Creates a new projectResource from an given CmsResource object.
     *
     * @param project The project in which the resource will be used.
     * @param resource The resource to be written to the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void createProjectResource(int projectId, String resourceName) throws CmsException {
        // do not create entries for online-project
        PreparedStatement statement = null;
        Connection con = null;
        try {
            m_ResourceBroker.getVfsAccess().readProjectResource(projectId, resourceName);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTRESOURCES_CREATE"));
            // write new resource to the database
            statement.setInt(1, projectId);
            statement.setString(2, resourceName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
            if (statement != null) {
                try{
                    statement.close();
                } catch (SQLException e){
                }
            }
            if (con != null){
                try{
                    con.close();
                } catch (SQLException e){
                }
            }
        }
    }


     /**
     * Creates a project.
     *
     * @param owner The owner of this project.
     * @param group The group for this project.
     * @param managergroup The managergroup for this project.
     * @param task The task.
     * @param name The name of the project to create.
     * @param description The description for the new project.
     * @param flags The flags for the project (e.g. archive).
     * @param type the type for the project (e.g. normal).
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup,
                                    CmsTask task, String name, String description,
                                    int flags, int type)
        throws CmsException {


        if ((description==null) || (description.length()<1)) {
            description=" ";
        }

        Timestamp createTime = new Timestamp(new java.util.Date().getTime());
        Connection con = null;
        PreparedStatement statement = null;

        int id = nextId(C_TABLE_PROJECTS);

        try {
            con = DriverManager.getConnection(m_poolName);

            // write data to database
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_CREATE"));

            statement.setInt(1,id);
            statement.setInt(2,owner.getId());
            statement.setInt(3,group.getId());
            statement.setInt(4,managergroup.getId());
            statement.setInt(5,task.getId());
            statement.setString(6,name);
            statement.setString(7,description);
            statement.setInt(8,flags);
            statement.setTimestamp(9,createTime);
            // no publish data
            //statement.setNull(10,Types.TIMESTAMP);
            //statement.setInt(11,C_UNKNOWN_ID);
            statement.setInt(10,type);
            statement.executeUpdate();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
        return readProject(id);
    }


   /**
    * Helper method to serialize the hashtable.
    * This method is used by updateSession() and createSession()
    */
    private byte[] serializeSession(Hashtable data) throws IOException {
        // serialize the hashtable
        byte[] value;
        Hashtable sessionData = (Hashtable) data.remove(C_SESSION_DATA);
        StringBuffer notSerializable = new StringBuffer();
        ByteArrayOutputStream bout= new ByteArrayOutputStream();
        ObjectOutputStream oout=new ObjectOutputStream(bout);

        // first write the user data
        oout.writeObject(data);
        if(sessionData != null) {
            Enumeration keys = sessionData.keys();
            while(keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object sessionValue = sessionData.get(key);
                if( sessionValue instanceof Serializable ) {
                    // this value is serializeable -> write it to the outputstream
                    oout.writeObject(key);
                    oout.writeObject(sessionValue);
                } else {
                    // this object is not serializeable -> remark for warning
                    notSerializable.append(key);
                    notSerializable.append("; ");
                }
            }
        }
        oout.close();
        value=bout.toByteArray();
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && (notSerializable.length()>0)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] warning, following entrys are not serializeable in the session: " + notSerializable.toString() + ".");
        }
        return value;
    }

    /**
     * This method creates a new session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public void createSession(String sessionId, Hashtable data)
        throws CmsException {
        byte[] value=null;

        Connection con = null;
        PreparedStatement statement = null;

        try {
                  value = serializeSession(data);

            con = DriverManager.getConnection(m_poolName);

            // write data to database
            statement = con.prepareStatement(m_SqlQueries.get("C_SESSION_CREATE"));

            statement.setString(1,sessionId);
            statement.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
            // TESTFIX (mfoley@iee.org) Old Code: statement.setBytes(3,value);
            m_doSetBytes(statement,3,value);
            statement.executeUpdate();
        }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]:"+CmsException.C_SERIALIZATION, e);
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
    }

    /**
     * Creates a new task.<p>
     * 
     * @param rootId id of the root task project
     * @param parentId id of the parent task
     * @param tasktype type of the task
     * @param ownerId id of the owner
     * @param agentId id of the agent
     * @param roleId id of the role
     * @param taskname name of the task
     * @param wakeuptime time when the task will be wake up
     * @param timeout time when the task times out
     * @param priority priority of the task
     *
     * @return the Task object of the generated task
     *
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(int rootId, int parentId, int tasktype,
                               int ownerId, int agentId,int  roleId, String taskname,
                               java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout,
                               int priority)
        throws CmsException {
        int newId = C_UNKNOWN_ID;
        CmsTask task = null;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            newId = nextId(C_TABLE_TASK);
            con = DriverManager.getConnection(m_poolName);
            // this statement causes trouble with MySQL 4.0.10
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_TYPE_COPY"));
            // create task by copying from tasktype table
            statement.setInt(1,newId);
            statement.setInt(2,tasktype);
            statement.executeUpdate();

            task = this.readTask(newId);
            task.setRoot(rootId);
            task.setParent(parentId);

            task.setName(taskname);
            task.setTaskType(tasktype);
            task.setRole(roleId);

            if(agentId==C_UNKNOWN_ID){
                agentId = findAgent(roleId);
            }
            if(agentId == C_UNKNOWN_ID) {
                throw new CmsException(CmsException.C_NO_USER);
            }
            task.setAgentUser(agentId);
            task.setOriginalUser(agentId);
            task.setWakeupTime(wakeuptime);
            task.setTimeOut(timeout);
            task.setPriority(priority);
            task.setPercentage(0);
            task.setState(C_TASK_STATE_STARTED);
            task.setInitiatorUser(ownerId);
            task.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
            task.setMilestone(0);
            task = this.writeTask(task);
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
        return task;
    }

    /**
     * Deletes all properties for a file or folder.
     *
     * @param resourceId The id of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(int projectId, CmsResource resource)
        throws CmsException {

        Connection con = null;
        PreparedStatement statement = null;
        String usedPool = null;
        String usedStatement = null;
        //int onlineProject = getOnlineProject(projectId).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            // create statement
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_DELETEALL"+usedStatement));
            statement.setInt(1, resource.getResourceId());
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
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
    }

    /**
     * Deletes all properties for a file or folder.
     *
     * @param resourceId The id of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(int projectId, int resourceId)
        throws CmsException {

        Connection con = null;
        PreparedStatement statement = null;
        String usedPool = null;
        String usedStatement = null;
        //int onlineProject = getOnlineProject(projectId).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            // create statement
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_DELETEALL"+usedStatement));
            statement.setInt(1, resourceId);
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
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
    }



    /**
     * Deletes a project from the cms.
     * Therefore it deletes all files, resources and properties.
     *
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deleteProject(CmsProject project)
        throws CmsException {

        // delete the resources from project_resources
        deleteAllProjectResources(project.getId());

        // delete all lost files
        // Removed because it takes too much time, ednfal 2002/07/19
        //clearFilesTable();

        // finally delete the project
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // create the statement
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_DELETE"));
            statement.setInt(1,project.getId());
            statement.executeUpdate();
        } catch( Exception exc ) {
            throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
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
    }

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project)
        throws CmsException {
/*
        // get all resources of the project
        Vector resources = readResources(project);

        for( int i = 0; i < resources.size(); i++) {
            // delete the properties for each resource in project
            deleteAllProperties( ((CmsResource) resources.elementAt(i)).getResourceId());
        }
*/
        // delete properies with one statement
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // create statement
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_DELETEALLPROP"));
            statement.setInt(1, project.getId());
            statement.executeQuery();
        } catch( SQLException exc ) {
            throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
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

        }

    /**
     * Deletes a specified project
     *
     * @param project The project to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteProjectResources(CmsProject project)
        throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // delete all project-resources.
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_DELETE_PROJECT"));
            statement.setInt(1,project.getId());
            statement.executeQuery();
            // delete all project-files.
            //clearFilesTable();
         } catch (SQLException e){
           throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
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
    }

    /**
     * delete all projectResource from an given CmsProject object.
     *
     * @param project The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProjectResources(int projectId) throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTRESOURCES_DELETEALL"));
            // delete all projectResources from the database
            statement.setInt(1, projectId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
            if (statement != null) {
                try{
                    statement.close();
                } catch (SQLException e){
                }
            }
            if (con != null){
                try{
                    con.close();
                } catch (SQLException e){
                }
            }
        }
    }

    /**
     * delete a projectResource from an given CmsResource object.
     *
     * @param project The project in which the resource is used.
     * @param resource The resource to be deleted from the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectResource(int projectId, String resourceName)
            throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTRESOURCES_DELETE"));
            // delete resource from the database
            statement.setInt(1, projectId);
            statement.setString(2, resourceName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
            if (statement != null) {
                try{
                    statement.close();
                } catch (SQLException e){
                }
            }
            if (con != null){
                try{
                    con.close();
                } catch (SQLException e){
                }
            }
        }
    }

    /**
     * Deletes a property for a file or folder.
     *
     * @param meta The property-name of which the property has to be read.
     * @param resourceId The id of the resource.
     * @param resourceType The Type of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType)
        throws CmsException {
        String usedPool = null;
        String usedStatement = null;
        //int onlineProject = getOnlineProject(projectId).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        CmsPropertydefinition propdef = m_ResourceBroker.getVfsAccess().readPropertydefinition(meta, resourceType);
        if( propdef == null) {
            // there is no propdefinition with the overgiven name for the resource
            throw new CmsException("[" + this.getClass().getName() + "] " + meta,
                CmsException.C_NOT_FOUND);
        } else {
            // delete the metainfo in the db
            Connection con = null;
            PreparedStatement statement = null;
            try {
                // create statement
                con = DriverManager.getConnection(usedPool);
                statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_DELETE"+usedStatement));
                statement.setInt(1, propdef.getId());
                statement.setInt(2, resource.getResourceId());
                statement.executeUpdate();
            } catch(SQLException exc) {
                throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                    CmsException.C_SQL_ERROR, exc);
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
        }
    }

    /**
     * Delete the propertydefinitions for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param metadef The propertydefinitions to be deleted.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deletePropertydefinition(CmsPropertydefinition metadef)
        throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            if(countProperties(metadef) != 0) {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(),
                    CmsException.C_UNKNOWN_EXCEPTION);
            }
            // delete the propertydef from offline db
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTYDEF_DELETE"));
            statement.setInt(1, metadef.getId() );
            statement.executeUpdate();
            statement.close();
            con.close();
            // delete the propertydef from online db
            con = DriverManager.getConnection(m_poolNameOnline);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTYDEF_DELETE_ONLINE"));
            statement.setInt(1, metadef.getId() );
            statement.executeUpdate();
            statement.close();
            con.close();
            // delete the propertydef from backup db
            con = DriverManager.getConnection(m_poolNameBackup);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTYDEF_DELETE_BACKUP"));
            statement.setInt(1, metadef.getId() );
            statement.executeUpdate();
            statement.close();
            con.close();
         } catch( SQLException exc ) {
             throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
         }finally {
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
    }


    /**
     * Deletes old sessions.
     */
    public void deleteSessions() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_SESSION_DELETE"));
            statement.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT ));

            statement.execute();
         }
        catch (Exception e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error while deleting old sessions: " + com.opencms.util.Utils.getStackTrace(e));
            }
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
    }

    /**
     * Deletes a serializable object from the systempropertys.
     *
     * @param name The name of the property.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deleteSystemProperty(String name)
        throws CmsException {

        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
           statement = con.prepareStatement(m_SqlQueries.get("C_SYSTEMPROPERTIES_DELETE"));
           statement.setString(1,name);
           statement.executeUpdate();
        }catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
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
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[genericSql.CmsDbAccess] Destroyed");
        }
    }

    /**
     * Ends a task from the Cms.
     *
     * @param taskid Id of the task to end.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void endTask(int taskId)
        throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        try{
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_END"));
            statement.setInt(1, 100);
            statement.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
            statement.setInt(3,taskId);
            statement.executeUpdate();

        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
    }

    /**
     * Private method to init all default-resources
     */
    protected void fillDefaults() throws CmsException
    {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] fillDefaults() starting NOW!");
        }

        // set the groups
        CmsGroup guests = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
        CmsGroup administrators = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER, null);
        CmsGroup users = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED | C_FLAG_GROUP_ROLE | C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
        CmsGroup projectleader = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER | C_FLAG_GROUP_PROJECTCOWORKER | C_FLAG_GROUP_ROLE, users.getName());

        // add the users
        CmsUser guest = m_ResourceBroker.getUserAccess().addUser(C_USER_GUEST, "", "the guest-user", " ", " ", " ", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, " ", " ", C_USER_TYPE_SYSTEMUSER);
        CmsUser admin = m_ResourceBroker.getUserAccess().addUser(C_USER_ADMIN, "admin", "the admin-user", " ", " ", " ", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, " ", " ", C_USER_TYPE_SYSTEMUSER);
        m_ResourceBroker.getUserAccess().addUserToGroup(guest.getId(), guests.getId());
        m_ResourceBroker.getUserAccess().addUserToGroup(admin.getId(), administrators.getId());
        writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);
        // create the online project
        CmsTask task = createTask(0, 0, 1, // standart project type,
        admin.getId(), admin.getId(), administrators.getId(), C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
        CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        int siteRootId = 0;
        CmsFolder rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        // create the folder for the default site
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, rootFolder.getResourceId(), C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        // create the folder for the virtual file system
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        // create the folder for the context objects system
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        // create the task for the setup project
        task = createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(),
                                    "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()),
                                    new java.sql.Timestamp(new java.util.Date().getTime()),
                                    C_TASK_PRIORITY_NORMAL);

        CmsProject setup = createProject(admin, administrators, administrators, task, "_setupProject",
                                           "Initial site import", C_FLAG_ENABLED, C_PROJECT_TYPE_TEMPORARY);

        // create the root-folder for the offline project
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
        // create the folder for the default site
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, rootFolder.getResourceId(), C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        // create the folder for the virtual file system
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
        // create the folder for the context objects system
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
    }

    /**
     * Finds an agent for a given role (group).
     * @param roleId The Id for the role (group).
     *
     * @return A vector with the tasks
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    protected int findAgent(int roleid)
        throws CmsException {
        int result = C_UNKNOWN_ID;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet res = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_FIND_AGENT"));
            statement.setInt(1,roleid);
            res = statement.executeQuery();

            if(res.next()) {
                result = res.getInt(1);
            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } catch( Exception exc ) {
              throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
        return result;
    }

    /**
     * Forwards a task to another user.
     *
     * @param taskId The id of the task that will be fowarded.
     * @param newRoleId The new Group the task belongs to
     * @param newUserId User who gets the task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void forwardTask(int taskId, int newRoleId, int newUserId)
        throws CmsException {

        Connection con = null;
        PreparedStatement statement = null;
        try{
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_FORWARD"));
            statement.setInt(1,newRoleId);
            statement.setInt(2,newUserId);
            statement.setInt(3,taskId);
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
    }

    /**
     * Returns all projects, with the overgiven state.
     *
     * @param state The state of the projects to read.
     *
     * @return a Vector of projects.
     */
     public Vector getAllProjects(int state)
         throws CmsException {
         Vector projects = new Vector();
         ResultSet res = null;
         PreparedStatement statement = null;
         Connection con = null;

         try {
             // create the statement
             con = DriverManager.getConnection(m_poolName);

             statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_READ_BYFLAG"));

             statement.setInt(1,state);
             res = statement.executeQuery();

             while(res.next()) {
                 projects.addElement( new CmsProject(res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_ID")),
                                                    res.getString(m_SqlQueries.get("C_PROJECTS_PROJECT_NAME")),
                                                    res.getString(m_SqlQueries.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                                                    res.getInt(m_SqlQueries.get("C_PROJECTS_TASK_ID")),
                                                    res.getInt(m_SqlQueries.get("C_PROJECTS_USER_ID")),
                                                    res.getInt(m_SqlQueries.get("C_PROJECTS_GROUP_ID")),
                                                    res.getInt(m_SqlQueries.get("C_PROJECTS_MANAGERGROUP_ID")),
                                                    res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_FLAGS")),
                                                    SqlHelper.getTimestamp(res,m_SqlQueries.get("C_PROJECTS_PROJECT_CREATEDATE")),
                                                    res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_TYPE"))));
             }
         } catch( SQLException exc ) {
             throw new CmsException("[" + this.getClass().getName() + ".getAllProjects(int)] " + exc.getMessage(),
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
             statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_READLAST_BACKUP"));
             res = statement.executeQuery();
             int i = 0;
             int max = 300;
             while(res.next() && (i < max)) {
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
                 i++;
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
     * Returns all child groups of a groups<P/>
     *
     *
     * @param groupname The name of the group.
     * @return users A Vector of all child groups or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
     public Vector getChild(String groupname)
      throws CmsException {

         Vector childs = new Vector();
         CmsGroup group;
         CmsGroup parent;
         ResultSet res = null;
         PreparedStatement statement = null;
         Connection con = null;
         try {
             // get parent group
             parent=m_ResourceBroker.getUserAccess().readGroup(groupname);
            // parent group exists, so get all childs
            if (parent != null) {
                // create statement
                con = DriverManager.getConnection(m_poolName);
                statement=con.prepareStatement(m_SqlQueries.get("C_GROUPS_GETCHILD"));

                statement.setInt(1,parent.getId());
                res = statement.executeQuery();
                // create new Cms group objects
                while ( res.next() ) {
                     group=new CmsGroup(res.getInt(m_SqlQueries.get("C_GROUPS_GROUP_ID")),
                                  res.getInt(m_SqlQueries.get("C_GROUPS_PARENT_GROUP_ID")),
                                  res.getString(m_SqlQueries.get("C_GROUPS_GROUP_NAME")),
                                  res.getString(m_SqlQueries.get("C_GROUPS_GROUP_DESCRIPTION")),
                                  res.getInt(m_SqlQueries.get("C_GROUPS_GROUP_FLAGS")));
                    childs.addElement(group);
                }
             }

         } catch (SQLException e){

            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
         //check if the child vector has no elements, set it to null.
         if (childs.size() == 0) {
             childs=null;
         }
         return childs;
     }


    /**
     * Returns a Vector with all resource-names that have set the given property to the given value.
     *
     * @param projectid, the id of the project to test.
     * @param propertydef, the name of the propertydefinition to check.
     * @param property, the value of the property for the resource.
     *
     * @return Vector with all names of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getFilesWithProperty(int projectId, String propertyDefinition, String propertyValue) throws CmsException {
        Vector names = new Vector();
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;
        String usedPool = null;
        String usedStatement = null;
        //int onlineProject = getOnlineProject(projectId).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_GET_FILES_WITH_PROPERTY"+usedStatement));
            statement.setInt(1, projectId);
            statement.setString(2, propertyValue);
            statement.setString(3, propertyDefinition);
            res = statement.executeQuery();

            // store the result into the vector
            while (res.next()) {
                String result = res.getString(1);
                names.addElement(result);
            }
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } catch (Exception exc) {
            throw new CmsException("getFilesWithProperty" + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
        return names;
    }



    /**
     * Retrieves the online project from the database.
     *
     * @return com.opencms.file.CmsProject the  onlineproject for the given project.
     * @throws CmsException Throws CmsException if the resource is not found, or the database communication went wrong.
     */
    public CmsProject getOnlineProject() throws CmsException {
        return readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
    }

    /**
     * Checks which Group can read the resource and all the parent folders.
     *
     * @param projectid the project to check the permission.
     * @param res The resource name to be checked.
     * @return The Group Id of the Group which can read the resource.
     *          null for all Groups and
     *          Admingroup for no Group.
     */
    public String getReadingpermittedGroup(int projectId, String resource) throws CmsException {
        CmsResource res = m_ResourceBroker.getVfsAccess().readFileHeader(projectId, resource, false);
        int groupId = -1;
        boolean noGroupCanReadThis = false;
        do{
            int flags = res.getAccessFlags();
            if(!((flags & C_ACCESS_PUBLIC_READ ) == C_ACCESS_PUBLIC_READ)){
                if((flags & C_ACCESS_GROUP_READ) == C_ACCESS_GROUP_READ){
                    if((groupId == -1) || (groupId == res.getGroupId())){
                        groupId = res.getGroupId();
                    }else{
                        int result = m_ResourceBroker.getUserAccess().checkGroupDependence(groupId, res.getGroupId());
                        if(result == -1){
                            noGroupCanReadThis = true;
                        }else{
                            groupId = result;
                        }
                    }
                }else{
                    noGroupCanReadThis = true;
                }
            }
            res = m_ResourceBroker.getVfsAccess().readFileHeader(projectId, res.getParentId());
        }while(!(noGroupCanReadThis || C_ROOT.equals(res.getAbsolutePath())));
        if (noGroupCanReadThis){
            return C_GROUP_ADMIN;
        }
        if(groupId == -1){
            return null;
        }else{
            return m_ResourceBroker.getUserAccess().readGroup(groupId).getName();
        }
    }


    /**
     * Get a parameter value for a task.
     *
     * @param task The task.
     * @param parname Name of the parameter.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public String getTaskPar(int taskId, String parname)
        throws CmsException {

        String result = null;
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKPAR_GET"));
            statement.setInt(1, taskId);
            statement.setString(2, parname);
            res = statement.executeQuery();
            if(res.next()) {
                result = res.getString(m_SqlQueries.get("C_PAR_VALUE"));
            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
        return result;
    }

    /**
     * Get the template task id fo a given taskname.
     *
     * @param taskName Name of the TAsk
     *
     * @return id from the task template
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int getTaskType(String taskName)
        throws CmsException {
        int result = 1;

        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_GET_TASKTYPE"));
            statement.setString(1, taskName);
            res = statement.executeQuery();
            if (res.next()) {
                result = res.getInt("id");
            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
        return result;
    }

        protected String getTaskTypeConditon(boolean first, int tasktype) {

        String result = "";
        // handle the tasktype for the SQL String
        if(!first){
            result = result+" AND ";
        }

        switch(tasktype)
        {
        case C_TASKS_ALL: {
                result = result + m_SqlQueries.get("C_TASK_ROOT") + "<>0";
                break;
            }
        case C_TASKS_OPEN: {
                result = result + m_SqlQueries.get("C_TASK_STATE") + "=" + C_TASK_STATE_STARTED;
                break;
            }
        case C_TASKS_ACTIVE: {
                result = result + m_SqlQueries.get("C_TASK_STATE") + "=" + C_TASK_STATE_STARTED;
                break;
            }
        case C_TASKS_DONE: {
                result = result + m_SqlQueries.get("C_TASK_STATE") + "=" + C_TASK_STATE_ENDED;
                break;
            }
        case C_TASKS_NEW: {
                result = result + m_SqlQueries.get("C_TASK_PERCENTAGE") + "='0' AND " +
                        m_SqlQueries.get("C_TASK_STATE") + "=" + C_TASK_STATE_STARTED;
                break;
            }
        default:{}
        }

        return result;
    }

    /**
     * Gets all resources that are marked as undeleted.
     * @param resources Vector of resources
     * @return Returns all resources that are markes as deleted
     */
    protected Vector getUndeletedResources(Vector resources) {
        Vector undeletedResources=new Vector();

        for (int i=0;i<resources.size();i++) {
            CmsResource res=(CmsResource)resources.elementAt(i);
            if (res.getState() != C_STATE_DELETED) {
                undeletedResources.addElement(res);
            }
        }

        return undeletedResources;
    }


    protected int insertTaskPar(int taskId, String parname, String parvalue)
        throws CmsException {
        PreparedStatement statement = null;
        Connection con = null;

        int newId = C_UNKNOWN_ID;

        try {
            con = DriverManager.getConnection(m_poolName);
            newId = nextId(C_TABLE_TASKPAR);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKPAR_INSERT"));
            statement.setInt(1, newId);
            statement.setInt(2, taskId);
            statement.setString(3, checkNull(parname));
            statement.setString(4, checkNull(parvalue));
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
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
        return newId;
    }

    protected int insertTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref)
        throws CmsException {
        PreparedStatement statement = null;
        Connection con = null;

        int newId = C_UNKNOWN_ID;

        try {
            con = DriverManager.getConnection(m_poolName);
            newId = nextId(C_TABLE_TASKPAR);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKTYPE_INSERT"));
            statement.setInt(1, autofinish);
            statement.setInt(2, escalationtyperef);
            statement.setString(3, htmllink);
            statement.setInt(4, newId);
            statement.setString(5, name);
            statement.setString(6, permission);
            statement.setInt(7, priorityref);
            statement.setInt(8, roleref);
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
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
        return newId;
    }

    /**
     * Private method to get the next id for a table.
     * This method is synchronized, to generate unique id's.
     * TODO: this method should be removed!
     *
     * @param key A key for the table to get the max-id from.
     * @return next-id The next possible id for this table.
     * @deprecated
     */
    protected synchronized int nextId(String key)
        throws CmsException {
        // return the next id for this table
        return com.opencms.dbpool.CmsIdGenerator.nextId(m_poolName, key);
    }

    /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
     * @param onlineProject The online project of the OpenCms.
     * @param report A report object to provide the loggin messages.
     * @return a vector of changed or deleted resources.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(CmsUser user, int projectId, CmsProject onlineProject,
                         boolean enableHistory, I_CmsReport report, Hashtable exportpoints) throws CmsException{
        CmsAccessFilesystem discAccess = new CmsAccessFilesystem(exportpoints);
        CmsFolder currentFolder = null;
        CmsFile currentFile = null;
        CmsFolder newFolder = null;
        CmsFile newFile = null;
        Vector offlineFolders;
        Vector offlineFiles;
        Vector deletedFolders = new Vector();
        // folderIdIndex:    offlinefolderId   |   onlinefolderId
        Hashtable folderIdIndex = new Hashtable();
        Vector changedResources = new Vector();

        CmsProject currentProject = readProject(projectId);
        int versionId = 1;
        long publishDate = System.currentTimeMillis();
        if (enableHistory){
            // get the version id for the backup
            versionId = getBackupVersionId();
            // store the projectdata to the backuptables for history
            backupProject(currentProject, versionId, publishDate, user);
        }
        // read all folders in offlineProject
        offlineFolders = m_ResourceBroker.getVfsAccess().readFolders(projectId, false, true);
        for (int i = 0; i < offlineFolders.size(); i++){
            currentFolder = ((CmsFolder) offlineFolders.elementAt(i));
            report.print(report.key("report.publishing"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFolder.getAbsolutePath());
            // do not publish the folder if it is locked in another project
            if (currentFolder.isLocked()){
              // in this case do nothing
            // C_STATE_DELETE
            } else if (currentFolder.getState() == C_STATE_DELETED){
                deletedFolders.addElement(currentFolder);
                changedResources.addElement(currentFolder.getResourceName());
                // C_STATE_NEW
            } else if (currentFolder.getState() == C_STATE_NEW){
                changedResources.addElement(currentFolder.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
                if (exportKey != null){
                    discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
                }
                // get parentId for onlineFolder either from folderIdIndex or from the database
                Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFolder.getParentId()));
                if (parentId == null){
                    CmsFolder currentOnlineParent = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFolder.getRootName()+currentFolder.getParent());
                    parentId = new Integer(currentOnlineParent.getResourceId());
                    folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
                }
                // create the new folder and insert its id in the folderindex
                try {
                    newFolder = m_ResourceBroker.getVfsAccess().createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(), currentFolder.getResourceName());
                    newFolder.setState(C_STATE_UNCHANGED);
                    updateResourcestate(newFolder);
                } catch (CmsException e) {
                    // if the folder already exists in the onlineProject then update the onlineFolder
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        CmsFolder onlineFolder = null;
                        try {
                            onlineFolder = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                        } catch (CmsException exc) {
                            throw exc;
                        } // end of catch
                        PreparedStatement statement = null;
                        Connection con = null;
                        try {
                            con = DriverManager.getConnection(m_poolNameOnline);
                            // update the onlineFolder with data from offlineFolder
                            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_ONLINE"));
                            statement.setInt(1, currentFolder.getType());
                            statement.setInt(2, currentFolder.getFlags());
                            statement.setInt(3, currentFolder.getOwnerId());
                            statement.setInt(4, currentFolder.getGroupId());
                            statement.setInt(5, onlineFolder.getProjectId());
                            statement.setInt(6, currentFolder.getAccessFlags());
                            statement.setInt(7, C_STATE_UNCHANGED);
                            statement.setInt(8, currentFolder.isLockedBy());
                            statement.setInt(9, currentFolder.getLauncherType());
                            statement.setString(10, currentFolder.getLauncherClassname());
                            statement.setTimestamp(11, new Timestamp(currentFolder.getDateLastModified()));
                            statement.setInt(12, currentFolder.getResourceLastModifiedBy());
                            statement.setInt(13, 0);
                            statement.setInt(14, onlineFolder.getFileId());
                            statement.setInt(15, onlineFolder.getResourceId());
                            statement.executeUpdate();
                            newFolder = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                        } catch (SQLException sqle) {
                            throw new CmsException("[" + this.getClass().getName() + "] " + sqle.getMessage(), CmsException.C_SQL_ERROR, sqle);
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
                    } else {
                        throw e;
                    }
                }
                folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(newFolder.getResourceId()));
                // copy properties
                Map props = new HashMap();
                try {
                    props = m_ResourceBroker.getVfsAccess().readProperties(projectId, currentFolder, currentFolder.getType());
                    m_ResourceBroker.getVfsAccess().writeProperties(props, onlineProject.getId(), newFolder, newFolder.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFolder.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory){
                    // backup the offline resource
                    backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
                }
                // set the state of current folder in the offline project to unchanged
                currentFolder.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFolder);
                // C_STATE_CHANGED
            } else if (currentFolder.getState() == C_STATE_CHANGED){
                changedResources.addElement(currentFolder.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
                if (exportKey != null){
                    discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
                }
                CmsFolder onlineFolder = null;
                try {
                    onlineFolder = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                } catch (CmsException exc){
                    // if folder does not exist create it
                    if (exc.getType() == CmsException.C_NOT_FOUND){
                        // get parentId for onlineFolder either from folderIdIndex or from the database
                        Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFolder.getParentId()));
                        if (parentId == null){
                            CmsFolder currentOnlineParent = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFolder.getRootName()+currentFolder.getParent());
                            parentId = new Integer(currentOnlineParent.getResourceId());
                            folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
                        }
                        // create the new folder
                        onlineFolder = m_ResourceBroker.getVfsAccess().createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(), currentFolder.getResourceName());
                        onlineFolder.setState(C_STATE_UNCHANGED);
                        updateResourcestate(onlineFolder);
                    } else {
                        throw exc;
                    }
                } // end of catch
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DriverManager.getConnection(m_poolNameOnline);
                    // update the onlineFolder with data from offlineFolder
                    statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_ONLINE"));
                    statement.setInt(1, currentFolder.getType());
                    statement.setInt(2, currentFolder.getFlags());
                    statement.setInt(3, currentFolder.getOwnerId());
                    statement.setInt(4, currentFolder.getGroupId());
                    statement.setInt(5, onlineFolder.getProjectId());
                    statement.setInt(6, currentFolder.getAccessFlags());
                    statement.setInt(7, C_STATE_UNCHANGED);
                    statement.setInt(8, currentFolder.isLockedBy());
                    statement.setInt(9, currentFolder.getLauncherType());
                    statement.setString(10, currentFolder.getLauncherClassname());
                    statement.setTimestamp(11, new Timestamp(currentFolder.getDateLastModified()));
                    statement.setInt(12, currentFolder.getResourceLastModifiedBy());
                    statement.setInt(13, 0);
                    statement.setInt(14, onlineFolder.getFileId());
                    statement.setInt(15, onlineFolder.getResourceId());
                    statement.executeUpdate();
                } catch (SQLException e){
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
                folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));
                // copy properties
                Map props = new HashMap();
                try {
                    deleteAllProperties(onlineProject.getId(), onlineFolder);
                    props = m_ResourceBroker.getVfsAccess().readProperties(projectId, currentFolder, currentFolder.getType());
                    m_ResourceBroker.getVfsAccess().writeProperties(props, onlineProject.getId(), onlineFolder, currentFolder.getType());
                } catch (CmsException exc){
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()){
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory){
                    // backup the offline resource
                    backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
                }
                // set the state of current folder in the offline project to unchanged
                currentFolder.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFolder);
            } // end of else if
        } // end of for(...

        // now read all FILES in offlineProject
        offlineFiles = m_ResourceBroker.getVfsAccess().readFiles(projectId, false, true);
        for (int i = 0; i < offlineFiles.size(); i++){
            currentFile = ((CmsFile) offlineFiles.elementAt(i));
            report.print(report.key("report.publishing"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFile.getAbsolutePath());     
            if(!currentFile.isLocked()){
                // remove the temporary files for this resource
                removeTemporaryFile(currentFile);
            }
            // do not publish files that are locked in another project
            if (currentFile.isLocked()){
                //in this case do nothing
            } else if (currentFile.getName().startsWith(C_TEMP_PREFIX)){
                deleteAllProperties(projectId, currentFile);
                removeFile(projectId, currentFile.getResourceName());
                // C_STATE_DELETE
            } else if (currentFile.getState() == C_STATE_DELETED){
                changedResources.addElement(currentFile.getResourceName());
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null){
                    try{
                        discAccess.removeResource(currentFile.getAbsolutePath(), exportKey);
                    }catch (Exception ex){
                    }
                }
                CmsFile currentOnlineFile = m_ResourceBroker.getVfsAccess().readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getResourceName());
                if (enableHistory){
                    // read the properties for backup
                    Map props = m_ResourceBroker.getVfsAccess().readProperties(projectId, currentFile, currentFile.getType());
                    // backup the offline resource
                    backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                try{
                    deleteAllProperties(onlineProject.getId(), currentOnlineFile);
                    deleteAllProperties(projectId, currentFile);
                }catch (CmsException exc){
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()){
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }try{
                    m_ResourceBroker.getVfsAccess().deleteResource(currentOnlineFile);
                    m_ResourceBroker.getVfsAccess().deleteResource(currentFile);
                }catch (CmsException exc){
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()){
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting resource for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
            // C_STATE_CHANGED
            }else if (currentFile.getState() == C_STATE_CHANGED){
                changedResources.addElement(currentFile.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null){
                    // Encoding project: Make sure files are written in the right encoding 
                    byte[] contents = currentFile.getContents();
                    String encoding = m_ResourceBroker.getVfsAccess().readProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, projectId, currentFile, currentFile.getType());
                    if (encoding != null) {
                        // Only files that have the encodig property set will be encoded,
                        // the other files will be ignored. So images etc. are not touched.                        
                        try {
                            contents = (new String(contents, encoding)).getBytes();
                        } catch (UnsupportedEncodingException uex) {
                            // contents will keep original value
                        }
                    }
                    discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, contents);
                }
                CmsFile onlineFile = null;
                try{
                    onlineFile = m_ResourceBroker.getVfsAccess().readFileHeader(onlineProject.getId(), currentFile.getResourceName(), false);
                }catch (CmsException exc){
                    if (exc.getType() == CmsException.C_NOT_FOUND){
                        // get parentId for onlineFolder either from folderIdIndex or from the database
                        Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFile.getParentId()));
                        if (parentId == null){
                            CmsFolder currentOnlineParent = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFile.getRootName()+currentFile.getParent());
                            parentId = new Integer(currentOnlineParent.getResourceId());
                            folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
                        }
                        // create a new File
                        currentFile.setState(C_STATE_UNCHANGED);
                        onlineFile = m_ResourceBroker.getVfsAccess().createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId.intValue(), currentFile.getResourceName());
                    }
                } // end of catch
                Connection con = null;
                PreparedStatement statement = null;
                try{
                    con = DriverManager.getConnection(m_poolNameOnline);
                    // update the onlineFile with data from offlineFile
                    statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_ONLINE"));
                    statement.setInt(1, currentFile.getType());
                    statement.setInt(2, currentFile.getFlags());
                    statement.setInt(3, currentFile.getOwnerId());
                    statement.setInt(4, currentFile.getGroupId());
                    statement.setInt(5, onlineFile.getProjectId());
                    statement.setInt(6, currentFile.getAccessFlags());
                    statement.setInt(7, C_STATE_UNCHANGED);
                    statement.setInt(8, currentFile.isLockedBy());
                    statement.setInt(9, currentFile.getLauncherType());
                    statement.setString(10, currentFile.getLauncherClassname());
                    statement.setTimestamp(11, new Timestamp(currentFile.getDateLastModified()));
                    statement.setInt(12, currentFile.getResourceLastModifiedBy());
                    statement.setInt(13, currentFile.getLength());
                    statement.setInt(14, onlineFile.getFileId());
                    statement.setInt(15, onlineFile.getResourceId());
                    statement.executeUpdate();
                    statement.close();
                    m_ResourceBroker.getVfsAccess().writeFileContent(onlineFile.getFileId(), currentFile.getContents(), m_poolNameOnline, "_ONLINE");
                    /*
                    statement = con.prepareStatement(m_SqlQueries.get("C_FILES_UPDATE_ONLINE"));
                    // TESTFIX (mfoley@iee.org) Old Code: statement.setBytes(1, currentFile.getContents());
                    m_doSetBytes(statement,1,currentFile.getContents());
                    statement.setInt(2, onlineFile.getFileId());
                    statement.executeUpdate();
                    statement.close();*/
                }catch (SQLException e){
                    throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
                }finally{
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
                // copy properties
                Map props = new HashMap();
                try {
                    deleteAllProperties(onlineProject.getId(), onlineFile);
                    props = m_ResourceBroker.getVfsAccess().readProperties(projectId, currentFile, currentFile.getType());
                    m_ResourceBroker.getVfsAccess().writeProperties(props, onlineProject.getId(), onlineFile, currentFile.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory){
                    // backup the offline resource
                    backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                // set the file state to unchanged
                currentFile.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFile);
            // C_STATE_NEW
            } else if (currentFile.getState() == C_STATE_NEW) {
                changedResources.addElement(currentFile.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null){
                    // Encoding project: Make sure files are written in the right encoding 
                    byte[] contents = currentFile.getContents();
                    String encoding = m_ResourceBroker.getVfsAccess().readProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, projectId, currentFile, currentFile.getType());
                    if (encoding != null) {
                        // Only files that have the encodig property set will be encoded,
                        // the other files will be ignored. So images etc. are not touched.
                        try {
                            contents = (new String(contents, encoding)).getBytes();
                        } catch (UnsupportedEncodingException uex) {
                            // contents will keep original value
                        }
                    }
                    discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, contents);                    
                }
                // get parentId for onlineFile either from folderIdIndex or from the database
                Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFile.getParentId()));
                if (parentId == null){
                    CmsFolder currentOnlineParent = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(), currentFile.getRootName()+currentFile.getParent());
                    parentId = new Integer(currentOnlineParent.getResourceId());
                    folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
                }
                // create the new file
                try {
                    newFile = m_ResourceBroker.getVfsAccess().createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId.intValue(), currentFile.getResourceName());
                    newFile.setState(C_STATE_UNCHANGED);
                    updateResourcestate(newFile);
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        CmsFile onlineFile = null;
                        try {
                            onlineFile = m_ResourceBroker.getVfsAccess().readFileHeader(onlineProject.getId(), currentFile.getResourceName(), false);
                        } catch (CmsException exc) {
                            throw exc;
                        } // end of catch
                        Connection con = null;
                        PreparedStatement statement = null;
                        try {
                            con = DriverManager.getConnection(m_poolNameOnline);
                            // update the onlineFile with data from offlineFile
                            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_ONLINE"));
                            statement.setInt(1, currentFile.getType());
                            statement.setInt(2, currentFile.getFlags());
                            statement.setInt(3, currentFile.getOwnerId());
                            statement.setInt(4, currentFile.getGroupId());
                            statement.setInt(5, onlineFile.getProjectId());
                            statement.setInt(6, currentFile.getAccessFlags());
                            statement.setInt(7, C_STATE_UNCHANGED);
                            statement.setInt(8, currentFile.isLockedBy());
                            statement.setInt(9, currentFile.getLauncherType());
                            statement.setString(10, currentFile.getLauncherClassname());
                            statement.setTimestamp(11, new Timestamp(currentFile.getDateLastModified()));
                            statement.setInt(12, currentFile.getResourceLastModifiedBy());
                            statement.setInt(13, currentFile.getLength());
                            statement.setInt(14, onlineFile.getFileId());
                            statement.setInt(15, onlineFile.getResourceId());
                            statement.executeUpdate();
                            m_ResourceBroker.getVfsAccess().writeFileContent(onlineFile.getFileId(), currentFile.getContents(), m_poolNameOnline, "_ONLINE");
                            /*
                            statement = con.prepareStatement(m_SqlQueries.get("C_FILES_UPDATE_ONLINE"));
                            // TESTFIX (mfoley@iee.org) Old Code: statement.setBytes(1, currentFile.getContents());
                            m_doSetBytes(statement,1,currentFile.getContents());
                            statement.setInt(2, onlineFile.getFileId());
                            statement.executeUpdate();
                            statement.close();*/
                            newFile = m_ResourceBroker.getVfsAccess().readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getResourceName());
                        } catch (SQLException sqle) {
                            throw new CmsException("[" + this.getClass().getName() + "] " + sqle.getMessage(), CmsException.C_SQL_ERROR, sqle);
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
                    } else {
                        throw e;
                    }
                }
                // copy properties
                Map props = new HashMap();
                try{
                    props = m_ResourceBroker.getVfsAccess().readProperties(projectId, currentFile, currentFile.getType());
                    m_ResourceBroker.getVfsAccess().writeProperties(props, onlineProject.getId(), newFile, newFile.getType());
                }catch (CmsException exc){
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()){
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory){
                    // backup the offline resource
                    backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                // set the file state to unchanged
                currentFile.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFile);
            }
        } // end of for(...
        // now delete the "deleted" folders
        for (int i = deletedFolders.size() - 1; i > -1; i--){
            currentFolder = ((CmsFolder) deletedFolders.elementAt(i));
            report.print(report.key("report.deleting"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFolder.getAbsolutePath());
            String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
            if (exportKey != null){
                discAccess.removeResource(currentFolder.getAbsolutePath(), exportKey);
            }
            if (enableHistory){
                Map props = m_ResourceBroker.getVfsAccess().readProperties(projectId, currentFolder,currentFolder.getType());
                // backup the offline resource
                backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
            }
            CmsResource delOnlineFolder = m_ResourceBroker.getVfsAccess().readFolder(onlineProject.getId(),currentFolder.getResourceName());
            try{
                deleteAllProperties(onlineProject.getId(), delOnlineFolder);
                deleteAllProperties(projectId, currentFolder);
            }catch (CmsException exc){
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ){
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentFolder.toString() + " Message= " + exc.getMessage());
                }
            }
            removeFolderForPublish(onlineProject.getId(), currentFolder.getResourceName());
            removeFolderForPublish(projectId, currentFolder.getResourceName());
        } // end of for
        //clearFilesTable();
        return changedResources;
    }
    /**
     * Get the next version id for the published backup resources
     *
     * @return int The new version id
     */
    public int getBackupVersionId(){
        PreparedStatement statement = null;
        Connection con = null;
        ResultSet res = null;
        int versionId = 1;
        int resVersionId = 1;
        try{
            // get the max version id
            con = DriverManager.getConnection(m_poolNameBackup);
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_BACKUP_MAXVER"));
            res = statement.executeQuery();
            if (res.next()){
                versionId = res.getInt(1)+1;
            }
            res.close();
            statement.close();
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_BACKUP_MAXVER_RESOURCE"));
            res = statement.executeQuery();
            if (res.next()){
                resVersionId = res.getInt(1)+1;
            }
            if (resVersionId > versionId){
                versionId = resVersionId;
            }
            return versionId;
        } catch (SQLException exc){
            return 1;
        } finally {
            if (res != null){
                try{
                    res.close();
                } catch (SQLException ex){
                }
            }
            if (statement != null){
                try{
                    statement.close();
                } catch (SQLException ex){
                }
            }
            if (con != null){
                try{
                    con.close();
                } catch (SQLException ex){
                }
            }
        }
    }

    /**
     * Creates a backup of the published project
     *
     * @param project The project in which the resource was published.
     * @param projectresources The resources of the project
     * @param versionId The version of the backup
     * @param publishDate The date of publishing
     * @param userId The id of the user who had published the project
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */

    public void backupProject(CmsProject project, int versionId,
                              long publishDate, CmsUser currentUser) throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        String ownerName = new String();
        String group = new String();
        String managerGroup = new String();
        try{
            CmsUser owner = m_ResourceBroker.getUserAccess().readUser(project.getOwnerId());
            ownerName = owner.getName()+" "+owner.getFirstname()+" "+owner.getLastname();
        } catch (CmsException e){
            // the owner could not be read
            ownerName = "";
        }
        try{
            group = m_ResourceBroker.getUserAccess().readGroup(project.getGroupId()).getName();
        } catch (CmsException e){
            // the group could not be read
            group = "";
        }
        try{
            managerGroup = m_ResourceBroker.getUserAccess().readGroup(project.getManagerGroupId()).getName();
        } catch (CmsException e){
            // the group could not be read
            managerGroup = "";
        }
        Vector projectresources = readAllProjectResources(project.getId());
        // write backup project to the database
        try {
            con = DriverManager.getConnection(m_poolNameBackup);
            // first write the project
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_CREATE_BACKUP"));
            statement.setInt(1, versionId);
            statement.setInt(2, project.getId());
            statement.setString(3, project.getName());
            statement.setTimestamp(4, new Timestamp(publishDate));
            statement.setInt(5, currentUser.getId());
            statement.setString(6, currentUser.getName()+" "+currentUser.getFirstname()+" "+currentUser.getLastname());
            statement.setInt(7, project.getOwnerId());
            statement.setString(8, ownerName);
            statement.setInt(9, project.getGroupId());
            statement.setString(10, group);
            statement.setInt(11, project.getManagerGroupId());
            statement.setString(12, managerGroup);
            statement.setString(13, project.getDescription());
            statement.setTimestamp(14, new Timestamp(project.getCreateDate()));
            statement.setInt(15, project.getType());
            statement.setInt(16, project.getTaskId());
            statement.executeUpdate();
            statement.close();
            // now write the projectresources
            for(int i = 0; i < projectresources.size(); i++){
                statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTRESOURCES_CREATE_BACKUP"));
                statement.setInt(1, versionId);
                statement.setInt(2, project.getId());
                statement.setString(3, (String)projectresources.get(i));
                statement.executeUpdate();
                statement.close();
            }
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
    }

    /**
     * Creates a backup of the published resource
     *
     * @param projectId The project in which the resource was published.
     * @param resource The published resource
     * @param content The file content if the resource is a file.
     * @param properties The properties of the resource.
     * @param versionId The version of the backup
     * @param publishDate The date of publishing
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */

    public void backupResource(int projectId, CmsResource resource, byte[] content,
                                   Map properties, int versionId, long publishDate) throws CmsException {
            Connection con = null;
            PreparedStatement statement = null;
            String ownerName = null;
            String groupName = new String();
            String lastModifiedName = null;
            try{
                CmsUser owner = m_ResourceBroker.getUserAccess().readUser(resource.getOwnerId());
                ownerName = owner.getName()+" "+owner.getFirstname()+" "+owner.getLastname();
            } catch (CmsException e){
                // the user could not be read
                ownerName = "";
            }
            try{
                groupName = m_ResourceBroker.getUserAccess().readGroup(resource.getGroupId()).getName();
            } catch (CmsException e){
                // the group could not be read
                groupName = "";
            }
            try{
                CmsUser lastModified = m_ResourceBroker.getUserAccess().readUser(resource.getResourceLastModifiedBy());
                lastModifiedName = lastModified.getName()+" "+lastModified.getFirstname()+" "+lastModified.getLastname();
            } catch (CmsException e){
                // the user could not be read
                lastModifiedName = "";
            }
            int resourceId = nextId(m_SqlQueries.get("C_TABLE_RESOURCES_BACKUP"));
            int fileId = C_UNKNOWN_ID;
            // write backup resource to the database
            try {
                con = DriverManager.getConnection(m_poolNameBackup);
                // if the resource is not a folder then backup the filecontent
                if (resource.getType() != C_TYPE_FOLDER){
                    fileId = nextId(m_SqlQueries.get("C_TABLE_FILES_BACKUP"));
                    // write new resource to the database
                    m_ResourceBroker.getVfsAccess().createFileContent(fileId, content, versionId, m_poolNameBackup, "_BACKUP");
                }
                statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_WRITE_BACKUP"));
                statement.setInt(1, resourceId);
                statement.setInt(2, C_UNKNOWN_ID);
                statement.setString(3, resource.getResourceName());
                statement.setInt(4, resource.getType());
                statement.setInt(5, resource.getFlags());
                statement.setInt(6, resource.getOwnerId());
                statement.setString(7, ownerName);
                statement.setInt(8, resource.getGroupId());
                statement.setString(9, groupName);
                statement.setInt(10, projectId);
                statement.setInt(11, fileId);
                statement.setInt(12, resource.getAccessFlags());
                statement.setInt(13, resource.getState());
                statement.setInt(14, resource.getLauncherType());
                statement.setString(15, resource.getLauncherClassname());
                // set date created = publish date
                statement.setTimestamp(16, new Timestamp(publishDate));
                statement.setTimestamp(17, new Timestamp(resource.getDateLastModified()));
                statement.setInt(18, content.length);
                statement.setInt(19, resource.getResourceLastModifiedBy());
                statement.setString(20, lastModifiedName);
                statement.setInt(21, versionId);
                statement.executeUpdate();
                statement.close();
                // now write the properties
                // get all metadefs
                Iterator keys = properties.keySet().iterator();
                // one metainfo-name:
                String key;
                while(keys.hasNext()) {
                    key = (String) keys.next();
                    CmsPropertydefinition propdef = m_ResourceBroker.getVfsAccess().readPropertydefinition(key, resource.getType());
                    String value = (String) properties.get(key);
                    if( propdef == null) {
                        // there is no propertydefinition for with the overgiven name for the resource
                        throw new CmsException("[" + this.getClass().getName() + "] " + key,
                        CmsException.C_NOT_FOUND);
                    } else {
                        // write the property into the db
                        statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_CREATE_BACKUP"));
                        statement.setInt(1, nextId(m_SqlQueries.get("C_TABLE_PROPERTIES_BACKUP")));
                        statement.setInt(2, propdef.getId());
                        statement.setInt(3, resourceId);
                        statement.setString(4, checkNull(value));
                        statement.setInt(5, versionId);
                        statement.executeUpdate();
                        statement.close();
                    }
                }
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
        }

    /**
     * select all projectResources from an given project
     *
     * @param project The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(int projectId) throws CmsException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTRESOURCES_READALL"));
            // select all resources from the database
            statement.setInt(1, projectId);
            res = statement.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
            if (statement != null) {
                try{
                    statement.close();
                } catch (SQLException e){
                }
            }
            if (con != null) {
                try{
                    con.close();
                } catch (SQLException e){
                }
            }
        }
        return projectResources;
    }


    /**
     * Reads all file headers of a file in the OpenCms.<BR>
     * The reading excludes the filecontent.
     *
     * @param filename The name of the file to be read.
     *
     * @return Vector of file headers read from the Cms.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllFileHeadersForHist(String resourceName)
        throws CmsException {

        CmsBackupResource file=null;
        ResultSet res =null;
        Vector allHeaders = new Vector();
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolNameBackup);
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_READ_ALL_BACKUP"));
            // read file header data from database
            statement.setString(1, resourceName);
            res = statement.executeQuery();
            // create new file headers
            while(res.next()) {
                int versionId=res.getInt(m_SqlQueries.get("C_RESOURCES_VERSION_ID"));
                int resId=res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_ID"));
                int parentId=res.getInt(m_SqlQueries.get("C_RESOURCES_PARENT_ID"));
                String resName=res.getString(m_SqlQueries.get("C_RESOURCES_RESOURCE_NAME"));
                int resType= res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_TYPE"));
                int resFlags=res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_FLAGS"));
                int userId=res.getInt(m_SqlQueries.get("C_RESOURCES_USER_ID"));
                String userName=res.getString(m_SqlQueries.get("C_RESOURCES_USER_NAME"));
                int groupId= res.getInt(m_SqlQueries.get("C_RESOURCES_GROUP_ID"));
                String groupName= res.getString(m_SqlQueries.get("C_RESOURCES_GROUP_NAME"));
                int projectID=res.getInt(m_SqlQueries.get("C_RESOURCES_PROJECT_ID"));
                int fileId=res.getInt(m_SqlQueries.get("C_RESOURCES_FILE_ID"));
                int accessFlags=res.getInt(m_SqlQueries.get("C_RESOURCES_ACCESS_FLAGS"));
                int state= res.getInt(m_SqlQueries.get("C_RESOURCES_STATE"));
                int launcherType= res.getInt(m_SqlQueries.get("C_RESOURCES_LAUNCHER_TYPE"));
                String launcherClass=  res.getString(m_SqlQueries.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
                long created=SqlHelper.getTimestamp(res,m_SqlQueries.get("C_RESOURCES_DATE_CREATED")).getTime();
                long modified=SqlHelper.getTimestamp(res,m_SqlQueries.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
                int resSize= res.getInt(m_SqlQueries.get("C_RESOURCES_SIZE"));
                int modifiedBy=res.getInt(m_SqlQueries.get("C_RESOURCES_LASTMODIFIED_BY"));
                String modifiedByName=res.getString(m_SqlQueries.get("C_RESOURCES_LASTMODIFIED_BY_NAME"));
                int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
                file=new CmsBackupResource(versionId,resId,parentId,fileId,resName,resType,resFlags,
                                           userId,userName,groupId,groupName,projectID,accessFlags,
                                           state,launcherType,launcherClass,created,modified,
                                           modifiedBy,modifiedByName,new byte[0],resSize,
                                           lockedInProject);

                allHeaders.addElement(file);
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch( Exception exc ) {
            throw new CmsException("readAllFileHeaders "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
        return allHeaders;
    }



/****************     methods for link management            ****************************/

    /**
     * deletes all entrys in the link table that belong to the pageId
     *
     * @param pageId The resourceId (offline) of the page whose links should be deleted
     */
    public void deleteLinkEntrys(int pageId)throws CmsException{
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // delete all project-resources.
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_DELETE_ENTRYS"));
            statement.setInt(1, pageId);
            statement.executeUpdate();
        } catch (SQLException e){
           throw new CmsException("["+this.getClass().getName()+"] deleteLinkEntrys "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
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
    }

    /**
     * creates a link entry for each of the link targets in the linktable.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createLinkEntrys(int pageId, Vector linkTargets)throws CmsException{
        //first delete old entrys in the database
        deleteLinkEntrys(pageId);
        if(linkTargets == null || linkTargets.size()==0){
            return;
        }
        // now write it
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_WRITE_ENTRY"));
            statement.setInt(1, pageId);
            for(int i=0; i < linkTargets.size(); i++){
                try{
                    statement.setString(2, (String)linkTargets.elementAt(i));
                    statement.executeUpdate();
                }catch(SQLException e){
                }
            }
        } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] createLinkEntrys "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be read.
     */
    public Vector readLinkEntrys(int pageId)throws CmsException{
        Vector result = new Vector();
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_READ_ENTRYS"));
            statement.setInt(1, pageId);
            res = statement.executeQuery();
            while(res.next()){
                result.add(res.getString(m_SqlQueries.get("C_LM_LINK_DEST")));
            }
            return result;
        }catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] readLinkEntrys "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] readLinkEntrys ", e);
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
    }

    /**
     * deletes all entrys in the online link table that belong to the pageId
     *
     * @param pageId The resourceId (online) of the page whose links should be deleted
     */
    public void deleteOnlineLinkEntrys(int pageId)throws CmsException{
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // delete all project-resources.
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_DELETE_ENTRYS_ONLINE"));
            statement.setInt(1, pageId);
            statement.executeUpdate();
        } catch (SQLException e){
           throw new CmsException("["+this.getClass().getName()+"] deleteOnlineLinkEntrys "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
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
    }

    /**
     * creates a link entry for each of the link targets in the online linktable.
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createOnlineLinkEntrys(int pageId, Vector linkTargets)throws CmsException{
        //first delete old entrys in the database
        deleteLinkEntrys(pageId);
        if(linkTargets == null || linkTargets.size()==0){
            return;
        }
        // now write it
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_WRITE_ENTRY_ONLINE"));
            statement.setInt(1, pageId);
            for(int i=0; i < linkTargets.size(); i++){
                try{
                    statement.setString(2, (String)linkTargets.elementAt(i));
                    statement.executeUpdate();
                }catch(SQLException e){
                }
            }
        } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] createOnlineLinkEntrys "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (online) of the page whose liks should be read.
     */
    public Vector readOnlineLinkEntrys(int pageId)throws CmsException{
        Vector result = new Vector();
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_READ_ENTRYS_ONLINE"));
            statement.setInt(1, pageId);
            res = statement.executeQuery();
            while(res.next()){
                result.add(res.getString(m_SqlQueries.get("C_LM_LINK_DEST")));
            }
            return result;
        }catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] readOnlineLinkEntrys "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] readOnlineLinkEntrys ", e);
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
    }

    /**
     * serches for broken links in the online project.
     *
     * @return A Vector with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target.
     */
    public Vector getOnlineBrokenLinks() throws CmsException{
        Vector result = new Vector();
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_GET_ONLINE_BROKEN_LINKS"));
            res = statement.executeQuery();
            int current = -1;
            CmsPageLinks links = null;
            while(res.next()){
                int next = res.getInt(m_SqlQueries.get("C_LM_PAGE_ID"));
                if(next != current){
                    if(links != null){
                        result.add(links);
                    }
                    links = new CmsPageLinks(next);
                    links.addLinkTarget(res.getString(m_SqlQueries.get("C_LM_LINK_DEST")));
                    try{
                        links.setResourceName(((CmsFile)m_ResourceBroker.getVfsAccess().readFileHeader(I_CmsConstants.C_PROJECT_ONLINE_ID, next)).getResourceName());
                    }catch(CmsException e){
                        links.setResourceName("id="+next+". Sorry, can't read resource. "+e.getMessage());
                    }
                    links.setOnline(true);
                }else{
                    links.addLinkTarget(res.getString(m_SqlQueries.get("C_LM_LINK_DEST")));
                }
                current = next;
            }
            if(links != null){
                result.add(links);
            }
            return result;
        }catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] getOnlineBrokenLinks "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] getOnlineBrokenLinks ", e);
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
    }

    /**
     * checks a project for broken links that would appear if the project is published.
     *
     * @param report A cmsReport object for logging while the method is still running.
     * @param changed A vecor (of CmsResources) with the changed resources in the project.
     * @param deleted A vecor (of CmsResources) with the deleted resources in the project.
     * @param newRes A vecor (of CmsResources) with the new resources in the project.
     */
     public void getBrokenLinks(I_CmsReport report, Vector changed, Vector deleted, Vector newRes)throws CmsException{

        // first create some Vectors for performance increase
        Vector deletedByName = new Vector(deleted.size());
        for(int i=0; i<deleted.size(); i++){
            deletedByName.add(((CmsResource)deleted.elementAt(i)).getResourceName());
        }
        Vector newByName = new Vector(newRes.size());
        for(int i=0; i<newRes.size(); i++){
            newByName.add(((CmsResource)newRes.elementAt(i)).getResourceName());
        }
        Vector changedByName = new Vector(changed.size());
        for(int i=0; i<changed.size(); i++){
            changedByName.add(((CmsResource)changed.elementAt(i)).getResourceName());
        }
        Vector onlineResNames = getOnlineResourceNames();

        // now check the new and the changed resources
        for(int i=0; i<changed.size(); i++){
            int resId = ((CmsResource)changed.elementAt(i)).getResourceId();
            Vector currentLinks = readLinkEntrys(resId);
            CmsPageLinks aktualBrokenList = new CmsPageLinks(resId);
            for(int index=0; index<currentLinks.size(); index++){
                String curElement = (String)currentLinks.elementAt(index);
                if(!( (onlineResNames.contains(curElement) && !deletedByName.contains(curElement))
                        ||(newByName.contains(curElement)) )){
                    // this is a broken link
                    aktualBrokenList.addLinkTarget(curElement);
                }
            }
            if(aktualBrokenList.getLinkTargets().size() != 0){
                aktualBrokenList.setResourceName(((CmsResource)changed.elementAt(i)).getResourceName());
                report.println(aktualBrokenList);
            }
        }
        for(int i=0; i<newRes.size(); i++){
            int resId = ((CmsResource)newRes.elementAt(i)).getResourceId();
            Vector currentLinks = readLinkEntrys(resId);
            CmsPageLinks aktualBrokenList = new CmsPageLinks(resId);
            for(int index=0; index<currentLinks.size(); index++){
                String curElement = (String)currentLinks.elementAt(index);
                if(!( (onlineResNames.contains(curElement) && !deletedByName.contains(curElement))
                        ||(newByName.contains(curElement)) )){
                    // this is a broken link
                    aktualBrokenList.addLinkTarget(curElement);
                }
            }
            if(aktualBrokenList.getLinkTargets().size() != 0){
                aktualBrokenList.setResourceName(((CmsResource)newRes.elementAt(i)).getResourceName());
                report.println(aktualBrokenList);
            }
        }

        // now we have to check if the deleted resources make any problems
        Hashtable onlineResults = new Hashtable();
        changedByName.addAll(deletedByName);
        for(int i=0; i<deleted.size(); i++){
            Vector refs = getAllOnlineReferencesForLink(((CmsResource)deleted.elementAt(i)).getResourceName(), changedByName);
            for(int index=0; index<refs.size(); index++){
                CmsPageLinks pl = (CmsPageLinks)refs.elementAt(index);
                Integer key = new Integer(pl.getResourceId());
                CmsPageLinks old = (CmsPageLinks)onlineResults.get(key);
                if(old == null){
                    onlineResults.put(key, pl);
                }else{
                    old.addLinkTarget((String)(pl.getLinkTargets().firstElement()));
                }
            }
        }
        // now lets put the results in the report (behind a seperator)
        Enumeration enu = onlineResults.elements();
        while(enu.hasMoreElements()){
            report.println((CmsPageLinks)enu.nextElement());
        }
     }

     /**
      * helper method for getBrokenLinks.
      */
     private Vector getAllOnlineReferencesForLink(String link, Vector exeptions)throws CmsException{
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_GET_ONLINE_REFERENCES"));
            statement.setString(1, link);
            res = statement.executeQuery();
            while(res.next()) {
                String resName=res.getString(m_SqlQueries.get("C_RESOURCES_RESOURCE_NAME"));
                if(!exeptions.contains(resName)){
                    CmsPageLinks pl = new CmsPageLinks(res.getInt(m_SqlQueries.get("C_LM_PAGE_ID")));
                    pl.setOnline(true);
                    pl.addLinkTarget(link);
                    pl.setResourceName(resName);
                    resources.add(pl);
                }
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] getAllOnlineReferencesForLink "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"] getAllOnlineReferencesForLink ", ex);
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
        return resources;
     }

     /**
      * This method reads all resource names from the table CmsOnlineResources
      *
      * @return A Vector (of Strings) with the resource names (like from getAbsolutePath())
      */
     public Vector getOnlineResourceNames()throws CmsException{

        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_LM_GET_ALL_ONLINE_RES_NAMES"));
            res = statement.executeQuery();
            // create new resource
            while(res.next()) {
                String resName=res.getString(m_SqlQueries.get("C_RESOURCES_RESOURCE_NAME"));
                resources.add(resName);
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] getOnlineResourceNames "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"] getOnlineResourceNames ", ex);
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
        return resources;
     }

    /**
     * When a project is published this method aktualises the online link table.
     *
     * @param deleted A Vector (of CmsResources) with the deleted resources of the project.
     * @param changed A Vector (of CmsResources) with the changed resources of the project.
     * @param newRes A Vector (of CmsResources) with the newRes resources of the project.
     */
    public void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException{
        if(deleted != null){
            for(int i=0; i<deleted.size(); i++){
                // delete the old values in the online table
                if(((CmsResource)deleted.elementAt(i)).getType() == pageType){
                    int id = readOnlineId(((CmsResource)deleted.elementAt(i)).getResourceName());
                    if(id != -1){
                        deleteOnlineLinkEntrys(id);
                    }
                }
            }
        }
        if(changed != null){
            for(int i=0; i<changed.size(); i++){
                // delete the old values and copy the new values from the project link table
                if(((CmsResource)changed.elementAt(i)).getType() == pageType){
                    int id = readOnlineId(((CmsResource)changed.elementAt(i)).getResourceName());
                    if(id != -1){
                        deleteOnlineLinkEntrys(id);
                        createOnlineLinkEntrys(id, readLinkEntrys(((CmsResource)changed.elementAt(i)).getResourceId()));
                    }
                }
            }
        }
        if(newRes != null){
            for(int i=0; i<newRes.size(); i++){
                // copy the values from the project link table
                if(((CmsResource)newRes.elementAt(i)).getType() == pageType){
                    int id = readOnlineId(((CmsResource)newRes.elementAt(i)).getResourceName());
                    if(id != -1){
                        createOnlineLinkEntrys(id, readLinkEntrys(((CmsResource)newRes.elementAt(i)).getResourceId()));
                    }
                }
            }
        }
    }

    /**
     * reads the online id of a offline file.
     * @param filename
     * @return the id or -1 if not found (should not happen).
     */
    private int readOnlineId(String filename)throws CmsException {

        ResultSet res =null;
        PreparedStatement statement = null;
        Connection con = null;
        int resourceId = -1;
        try {
            con = DriverManager.getConnection(m_poolNameOnline);
            statement=con.prepareStatement(m_SqlQueries.get("C_LM_READ_ONLINE_ID"));
            // read file data from database
            statement.setString(1, filename);
            res = statement.executeQuery();
            // read the id
            if(res.next()) {
                resourceId = res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_ID"));
                while(res.next()){
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] readOnlineId "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch( Exception exc ) {
            throw new CmsException("readOnlineId "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
        return resourceId;
    }
/****************  end  methods for link management          ****************************/

    /**
     * Reads a exportrequest from the Cms.
     *
     *
     * @param request The request to be read.
     *
     * @return The exportrequest read from the Cms or null if it is not found.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsExportLink readExportLink(String request) throws CmsException{
        CmsExportLink link = null;
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_EXPORT_LINK_READ"));
            statement.setString(1, request);
            res = statement.executeQuery();

            // create new Cms exportlink object
            if(res.next()) {
                link = new CmsExportLink(res.getInt(m_SqlQueries.get("C_EXPORT_ID")),
                                   res.getString(m_SqlQueries.get("C_EXPORT_LINK")),
                                   SqlHelper.getTimestamp(res,m_SqlQueries.get("C_EXPORT_DATE")).getTime(),
                                   null);

                // now the dependencies
                try{
                    res.close();
                    statement.close();
                }catch(SQLException ex){
                }
                statement = con.prepareStatement(m_SqlQueries.get("C_EXPORT_DEPENDENCIES_READ"));
                statement.setInt(1,link.getId());
                res = statement.executeQuery();
                while(res.next()){
                    link.addDependency(res.getString(m_SqlQueries.get("C_EXPORT_DEPENDENCIES_RESOURCE")));
                }
            }
            return link;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] readExportLink "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] readExportLink ", e);
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
     }

    /**
     * Reads a exportrequest without the dependencies from the Cms.<BR/>
     *
     *
     * @param request The request to be read.
     *
     * @return The exportrequest read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsExportLink readExportLinkHeader(String request) throws CmsException{
        CmsExportLink link = null;
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_EXPORT_LINK_READ"));
            statement.setString(1, request);
            res = statement.executeQuery();

            // create new Cms exportlink object
            if(res.next()) {
                link = new CmsExportLink(res.getInt(m_SqlQueries.get("C_EXPORT_ID")),
                                   res.getString(m_SqlQueries.get("C_EXPORT_LINK")),
                                   SqlHelper.getTimestamp(res,m_SqlQueries.get("C_EXPORT_DATE")).getTime(),
                                   null);

            }
            return link;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] readExportLinkHeader "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] readExportLinkHeader ", e);
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
     }

    /**
     * Sets one exportLink to procecced.
     *
     * @param link the cmsexportlink.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLinkProcessedState(CmsExportLink link) throws CmsException {
        int linkId = link.getId();
        if(linkId == 0){
            CmsExportLink dbLink = readExportLink(link.getLink());
            if(dbLink == null){
                return;
            }else{
                linkId = dbLink.getId();
            }
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // delete the link table entry
            statement=con.prepareStatement(m_SqlQueries.get("C_EXPORT_LINK_SET_PROCESSED"));
            statement.setBoolean(1, link.getProcessedState());
            statement.setInt(2, linkId);
            statement.executeUpdate();
        } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] writeExportLinkProcessedState "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
    }

    /**
     * Deletes an exportlink from the Cms.
     *
     * @param link the cmsexportlink to delete.
     *
     * @throws CmsException if something goes wrong.
     */
    public void deleteExportLink(String link) throws CmsException {
        CmsExportLink dbLink = readExportLink(link);
        if(dbLink != null){
            deleteExportLink(dbLink);
        }
    }

    /**
     * Deletes an exportlink from the Cms.
     *
     * @param link the cmsexportlink object to delete.
     *
     * @throws CmsException if something goes wrong.
     */
    public void deleteExportLink(CmsExportLink link) throws CmsException {
        int deleteId = link.getId();
        if(deleteId == 0){
            CmsExportLink dbLink = readExportLink(link.getLink());
            if(dbLink == null){
                return;
            }else{
                deleteId = dbLink.getId();
                link.setLinkId(deleteId);
            }
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // delete the link table entry
            statement=con.prepareStatement(m_SqlQueries.get("C_EXPORT_LINK_DELETE"));
            statement.setInt(1, deleteId);
            statement.executeUpdate();
            // now the dependencies
            try{
                statement.close();
            }catch(SQLException ex){
            }
            statement=con.prepareStatement(m_SqlQueries.get("C_EXPORT_DEPENDENCIES_DELETE"));
            statement.setInt(1, deleteId);
            statement.executeUpdate();
        } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] deleteExportLink "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
    }
    /**
     * Writes an exportlink to the Cms.
     *
     * @param link the cmsexportlink object to write.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLink(CmsExportLink link) throws CmsException {
        //first delete old entrys in the database
        deleteExportLink(link);
        int id = link.getId();
        if(id == 0){
            id = nextId(m_SqlQueries.get("C_TABLE_EXPORT_LINKS"));
            link.setLinkId(id);
        }
        // now write it
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // write the link table entry
            statement=con.prepareStatement(m_SqlQueries.get("C_EXPORT_LINK_WRITE"));
            statement.setInt(1, id);
            statement.setString(2, link.getLink());
            statement.setTimestamp(3, new Timestamp(link.getLastExportDate()));
            statement.setBoolean(4, link.getProcessedState());
            statement.executeUpdate();
            // now the dependencies
            try{
                statement.close();
            }catch(SQLException ex){
            }
            statement = con.prepareStatement(m_SqlQueries.get("C_EXPORT_DEPENDENCIES_WRITE"));
            statement.setInt(1, id);
            Vector deps = link.getDependencies();
            for(int i=0; i < deps.size(); i++){
                try{
                    statement.setString(2, (String)deps.elementAt(i));
                    statement.executeUpdate();
                }catch(SQLException e){
                    // this should be an Duplicate entry error and can be ignored
                    // todo: if it is something else we should coutionary delete the whole exportlink
                }
            }
        } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] writeExportLink "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
    }


     /**
     * Reads all export links that depend on the resource.
     * @param res. The resourceName() of the resources that has changed (or the String
     *              that describes a contentdefinition).
     * @return a Vector(of Strings) with the linkrequest names.
     */
     public Vector getDependingExportLinks(Vector resources) throws CmsException{
        Vector retValue = new Vector();
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            Vector firstResult = new Vector();
            Vector secondResult = new Vector();
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_EXPORT_GET_ALL_DEPENDENCIES"));
            res = statement.executeQuery();
            while(res.next()) {
                firstResult.add(res.getString(m_SqlQueries.get("C_EXPORT_DEPENDENCIES_RESOURCE")));
                secondResult.add(res.getString(m_SqlQueries.get("C_EXPORT_LINK")));
            }
            // now we have all dependencies that are there. We can search now for
            // the ones we need
            for(int i=0; i<resources.size(); i++){
                for(int j=0; j<firstResult.size(); j++){
                    if(((String)firstResult.elementAt(j)).startsWith((String)resources.elementAt(i))){
                        if(!retValue.contains(secondResult.elementAt(j))){
                            retValue.add(secondResult.elementAt(j));
                        }
                    }else if(((String)resources.elementAt(i)).startsWith((String)firstResult.elementAt(j))){
                        if(!retValue.contains(secondResult.elementAt(j))){
                            // only direct subfolders count
                            int index = ((String)firstResult.elementAt(j)).length();
                            String test = ((String)resources.elementAt(i)).substring(index);
                            index=test.indexOf("/");
                            if(index == -1 || index+1 == test.length()){
                                retValue.add(secondResult.elementAt(j));
                            }
                        }
                    }
                }
            }
            return retValue;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] getDependingExportLinks "+e.getMessage(), CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] getDependingExportLinks ", e);
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
     }

    /**
     * Reads all export links.
     *
     * @return a Vector(of Strings) with the links.
     */
     public Vector getAllExportLinks() throws CmsException{
        Vector retValue = new Vector();
        PreparedStatement statement = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_EXPORT_GET_ALL_LINKS"));
            res = statement.executeQuery();
            while(res.next()) {
                retValue.add(res.getString(m_SqlQueries.get("C_EXPORT_LINK")));
            }
            return retValue;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] getAllExportLinks "+e.getMessage(), CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"] getAllExportLinks ", e);
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
     }





    /**
     * Reads a project.
     *
     * @param id The id of the project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(int id)
        throws CmsException {

        PreparedStatement statement = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_READ"));

            statement.setInt(1,id);
            res = statement.executeQuery();

            if(res.next()) {
                project = new CmsProject(res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_ID")),
                                         res.getString(m_SqlQueries.get("C_PROJECTS_PROJECT_NAME")),
                                         res.getString(m_SqlQueries.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_TASK_ID")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_USER_ID")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_GROUP_ID")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_MANAGERGROUP_ID")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_FLAGS")),
                                         SqlHelper.getTimestamp(res,m_SqlQueries.get("C_PROJECTS_PROJECT_CREATEDATE")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] " + id,
                    CmsException.C_NOT_FOUND);
            }
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);
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
        return project;
    }

    /**
     * Reads a project by task-id.
     *
     * @param task The task to read the project for.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsTask task)
        throws CmsException {

        PreparedStatement statement = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);

            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_READ_BYTASK"));

            statement.setInt(1,task.getId());
            res = statement.executeQuery();

            if(res.next())
                 project = new CmsProject(res,m_SqlQueries);
          else
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] " + task,
                    CmsException.C_NOT_FOUND);
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
        return project;
    }

    /**
     * Reads all resource from the Cms, that are in one project.<BR/>
     * A resource is either a file header or a folder.
     *
     * @param project The id of the project in which the resource will be used.
     * @param filter The filter for the resources to be read
     *
     * @return A Vecor of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readProjectView(int currentProject, int project, String filter)
        throws CmsException {

        Vector resources = new Vector();
        CmsResource file;
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;
        String addStatement = filter+" ORDER BY RESOURCE_NAME";
        try {
            con = DriverManager.getConnection(m_poolName);
            // read resource data from database
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_PROJECTVIEW")+addStatement);
            statement.setInt(1,project);
            res = statement.executeQuery();
            // create new resource
            while(res.next()) {
                int resId=res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_ID"));
                int parentId=res.getInt(m_SqlQueries.get("C_RESOURCES_PARENT_ID"));
                String resName=res.getString(m_SqlQueries.get("C_RESOURCES_RESOURCE_NAME"));
                int resType= res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_TYPE"));
                int resFlags=res.getInt(m_SqlQueries.get("C_RESOURCES_RESOURCE_FLAGS"));
                int userId=res.getInt(m_SqlQueries.get("C_RESOURCES_USER_ID"));
                int groupId= res.getInt(m_SqlQueries.get("C_RESOURCES_GROUP_ID"));
                //int projectId=res.getInt(m_SqlQueries.get("C_RESOURCES_PROJECT_ID"));
                int projectId=currentProject;
                int fileId=res.getInt(m_SqlQueries.get("C_RESOURCES_FILE_ID"));
                int accessFlags=res.getInt(m_SqlQueries.get("C_RESOURCES_ACCESS_FLAGS"));
                int state= res.getInt(m_SqlQueries.get("C_RESOURCES_STATE"));
                int lockedBy= res.getInt(m_SqlQueries.get("C_RESOURCES_LOCKED_BY"));
                int launcherType= res.getInt(m_SqlQueries.get("C_RESOURCES_LAUNCHER_TYPE"));
                String launcherClass=  res.getString(m_SqlQueries.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
                long created=SqlHelper.getTimestamp(res,m_SqlQueries.get("C_RESOURCES_DATE_CREATED")).getTime();
                long modified=SqlHelper.getTimestamp(res,m_SqlQueries.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
                int modifiedBy=res.getInt(m_SqlQueries.get("C_RESOURCES_LASTMODIFIED_BY"));
                int resSize= res.getInt(m_SqlQueries.get("C_RESOURCES_SIZE"));
                int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
                
                if (com.opencms.file.genericSql.CmsDbAccess.C_USE_TARGET_DATE && resType == com.opencms.file.genericSql.CmsDbAccess.C_RESTYPE_LINK_ID && resFlags > 0) {
                    modified = m_ResourceBroker.getVfsAccess().fetchDateFromResource(projectId, resFlags, modified);
                }                

                file=new CmsResource(resId,parentId,fileId,resName,resType,resFlags,
                                     userId,groupId,projectId,accessFlags,state,lockedBy,
                                     launcherType,launcherClass,created,modified,modifiedBy,
                                     resSize,lockedInProject);
                resources.addElement(file);
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"]", ex);
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
        return resources;
    }

    /**
     * Reads a project from the backup tables.
     *
     * @param versionId The versionId of the backup project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsBackupProject readBackupProject(int versionId)
        throws CmsException {

        PreparedStatement statement = null;
        CmsBackupProject project = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolNameBackup);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_READBYVERSION_BACKUP"));

            statement.setInt(1,versionId);
            res = statement.executeQuery();

            if(res.next()) {
                Vector projectresources = readBackupProjectResources(versionId);
                project = new CmsBackupProject(res.getInt("VERSION_ID"),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_ID")),
                                         res.getString(m_SqlQueries.get("C_PROJECTS_PROJECT_NAME")),
                                         SqlHelper.getTimestamp(res,"PROJECT_PUBLISHDATE"),
                                         res.getInt("PROJECT_PUBLISHED_BY"),
                                         res.getString("PROJECT_PUBLISHED_BY_NAME"),
                                         res.getString(m_SqlQueries.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_TASK_ID")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_USER_ID")),
                                         res.getString("USER_NAME"),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_GROUP_ID")),
                                         res.getString("GROUP_NAME"),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_MANAGERGROUP_ID")),
                                         res.getString("MANAGERGROUP_NAME"),
                                         SqlHelper.getTimestamp(res,m_SqlQueries.get("C_PROJECTS_PROJECT_CREATEDATE")),
                                         res.getInt(m_SqlQueries.get("C_PROJECTS_PROJECT_TYPE")),
                                         projectresources);
            } else {
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] version " + versionId,
                    CmsException.C_NOT_FOUND);
            }
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);
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
        return project;
    }

    /**
     * Reads log entries for a project.
     *
     * @param project The projec for tasklog to read.
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(int projectid)
        throws CmsException {
        ResultSet res = null;
        Connection con = null;

        CmsTaskLog tasklog = null;
        Vector logs = new Vector();
        PreparedStatement statement = null;
        String comment = null;
        java.sql.Timestamp starttime = null;
        int id = C_UNKNOWN_ID;
        int task = C_UNKNOWN_ID;
        int user = C_UNKNOWN_ID;
        int type = C_UNKNOWN_ID;

        try {
            con = DriverManager.getConnection(m_poolName);

            statement = con.prepareStatement(m_SqlQueries.get("C_TASKLOG_READ_PPROJECTLOGS"));
            statement.setInt(1, projectid);
            res = statement.executeQuery();
            while(res.next()) {
                comment = res.getString(m_SqlQueries.get("C_LOG_COMMENT"));
                id = res.getInt(m_SqlQueries.get("C_LOG_ID"));
                starttime = SqlHelper.getTimestamp(res,m_SqlQueries.get("C_LOG_STARTTIME"));
                task = res.getInt(m_SqlQueries.get("C_LOG_TASK"));
                user = res.getInt(m_SqlQueries.get("C_LOG_USER"));
                type = res.getInt(m_SqlQueries.get("C_LOG_TYPE"));

                tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
                logs.addElement(tasklog);
            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } catch( Exception exc ) {
              throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
        return logs;
    }




    /**
     * select a projectResource from an given project and resourcename
     *
     * @param project The project in which the resource is used.
     * @param resource The resource to be read from the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    protected Vector readBackupProjectResources(int versionId) throws CmsException {
        PreparedStatement statement = null;
        Connection con = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        try {
            con = DriverManager.getConnection(m_poolNameBackup);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTRESOURCES_READ_BACKUP"));
            // select resource from the database
            statement.setInt(1, versionId);
            res = statement.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
        } finally {
            if (statement != null) {
                try{
                    statement.close();
                } catch (SQLException e){
                }
            }
            if (con != null){
                try{
                    con.close();
                } catch (SQLException e){
                }
            }
        }
        return projectResources;
    }

    /**
     * Reads a session from the database.
     *
     * @param sessionId, the id og the session to read.
     * @return the read session as Hashtable.
     * @throws thorws CmsException if something goes wrong.
     */
    public Hashtable readSession(String sessionId)
        throws CmsException {
        PreparedStatement statement = null;
        ResultSet res = null;
        Hashtable sessionData = new Hashtable();
        Hashtable data = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_SESSION_READ"));
            statement.setString(1,sessionId);
            statement.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT ));

            res = statement.executeQuery();

            // create new Cms user object
            if(res.next()) {
                // read the additional infos.
                byte[] value = m_SqlQueries.getBytes(res,"SESSION_DATA");
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                data = (Hashtable) oin.readObject();
                                try {
                      for(;;) {
                    Object key = oin.readObject();
                    Object sessionValue = oin.readObject();
                    sessionData.put(key, sessionValue);
                                  }
                                } catch(EOFException exc) {
                                  // reached eof - stop reading all is done now.
                }
                data.put(C_SESSION_DATA, sessionData);
            } else {
                deleteSessions();
            }
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);
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
        return data;
    }

     /**
     * Reads a serializable object from the systempropertys.
     *
     * @param name The name of the property.
     *
     * @return object The property-object.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Serializable readSystemProperty(String name)
        throws CmsException {

        Serializable property=null;
        byte[] value;
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;

        // create get the property data from the database
        try {
            con = DriverManager.getConnection(m_poolName);
          statement=con.prepareStatement(m_SqlQueries.get("C_SYSTEMPROPERTIES_READ"));
          statement.setString(1,name);
          res = statement.executeQuery();
          if(res.next()) {
                value = m_SqlQueries.getBytes(res,m_SqlQueries.get("C_SYSTEMPROPERTY_VALUE"));
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property=(Serializable)oin.readObject();
            }
        }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);
        }
        catch (ClassNotFoundException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);
        }finally {
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
        return property;
    }

/**
 * Reads a task from the Cms.
 *
 * @param id The id of the task to read.
 *
 * @return a task object or null if the task is not found.
 *
 * @throws CmsException Throws CmsException if something goes wrong.
 */
public CmsTask readTask(int id) throws CmsException {
    ResultSet res = null;
    CmsTask task = null;
    PreparedStatement statement = null;
    Connection con = null;


    try {
        con = DriverManager.getConnection(m_poolName);
        
        statement = con.prepareStatement(m_SqlQueries.get("C_TASK_READ"));
        statement.setInt(1, id);
        res = statement.executeQuery();
                
        if (res.next()) {
            int autofinish = res.getInt(m_SqlQueries.get("C_TASK_AUTOFINISH"));
            java.sql.Timestamp endtime = SqlHelper.getTimestamp(res, m_SqlQueries.get("C_TASK_ENDTIME"));
            int escalationtype = res.getInt(m_SqlQueries.get("C_TASK_ESCALATIONTYPE"));
            id = res.getInt(m_SqlQueries.get("C_TASK_ID"));
            int initiatoruser = res.getInt(m_SqlQueries.get("C_TASK_INITIATORUSER"));
            int milestone = res.getInt(m_SqlQueries.get("C_TASK_MILESTONE"));
            String name = res.getString(m_SqlQueries.get("C_TASK_NAME"));
            int originaluser = res.getInt(m_SqlQueries.get("C_TASK_ORIGINALUSER"));
            int agentuser = res.getInt(m_SqlQueries.get("C_TASK_AGENTUSER"));
            int parent = res.getInt(m_SqlQueries.get("C_TASK_PARENT"));
            int percentage = res.getInt(m_SqlQueries.get("C_TASK_PERCENTAGE"));
            String permission = res.getString(m_SqlQueries.get("C_TASK_PERMISSION"));
            int priority = res.getInt(m_SqlQueries.get("C_TASK_PRIORITY"));
            int role = res.getInt(m_SqlQueries.get("C_TASK_ROLE"));
            int root = res.getInt(m_SqlQueries.get("C_TASK_ROOT"));
            java.sql.Timestamp starttime = SqlHelper.getTimestamp(res, m_SqlQueries.get("C_TASK_STARTTIME"));
            int state = res.getInt(m_SqlQueries.get("C_TASK_STATE"));
            int tasktype = res.getInt(m_SqlQueries.get("C_TASK_TASKTYPE"));
            java.sql.Timestamp timeout = SqlHelper.getTimestamp(res, m_SqlQueries.get("C_TASK_TIMEOUT"));
            java.sql.Timestamp wakeuptime = SqlHelper.getTimestamp(res, m_SqlQueries.get("C_TASK_WAKEUPTIME"));
            String htmllink = res.getString(m_SqlQueries.get("C_TASK_HTMLLINK"));
            task = new CmsTask(id, name, state, tasktype, root, parent, initiatoruser, role, agentuser, originaluser, starttime, wakeuptime, timeout, endtime, percentage, permission, priority, escalationtype, htmllink, milestone, autofinish);
        }
    } catch (SQLException exc) {
        throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
    } catch (Exception exc) {
        throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
    return task;
}

    /**
     * Reads a log for a task.
     *
     * @param id The id for the tasklog .
     * @return A new TaskLog object
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTaskLog readTaskLog(int id)
        throws CmsException {
        ResultSet res = null;
        CmsTaskLog tasklog = null;
        PreparedStatement statement = null;
        Connection con = null;


        try {
            con = DriverManager.getConnection(m_poolName);

            statement = con.prepareStatement(m_SqlQueries.get("C_TASKLOG_READ"));
            statement.setInt(1, id);
            res = statement.executeQuery();
            if(res.next()) {
                String comment = res.getString(m_SqlQueries.get("C_LOG_COMMENT"));
                id = res.getInt(m_SqlQueries.get("C_LOG_ID"));
                java.sql.Timestamp starttime = SqlHelper.getTimestamp(res,m_SqlQueries.get("C_LOG_STARTTIME"));
                int task = res.getInt(m_SqlQueries.get("C_LOG_TASK"));
                int user = res.getInt(m_SqlQueries.get("C_LOG_USER"));
                int type = res.getInt(m_SqlQueries.get("C_LOG_TYPE"));

                tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } catch( Exception exc ) {
              throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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

        return tasklog;
    }

    /**
     * Reads log entries for a task.
     *
     * @param taskid The id of the task for the tasklog to read .
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTaskLogs(int taskId)
        throws CmsException {
        Connection con = null;
        ResultSet res = null;
        CmsTaskLog tasklog = null;
        Vector logs = new Vector();
        PreparedStatement statement = null;
        String comment = null;
        java.sql.Timestamp starttime = null;
        int id = C_UNKNOWN_ID;
        int task = C_UNKNOWN_ID;
        int user = C_UNKNOWN_ID;
        int type = C_UNKNOWN_ID;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKLOG_READ_LOGS"));
            statement.setInt(1, taskId);
            res = statement.executeQuery();
            while(res.next()) {
                comment = res.getString(m_SqlQueries.get("C_TASKLOG_COMMENT"));
                id = res.getInt(m_SqlQueries.get("C_TASKLOG_ID"));
                starttime = SqlHelper.getTimestamp(res,m_SqlQueries.get("C_TASKLOG_STARTTIME"));
                task = res.getInt(m_SqlQueries.get("C_TASKLOG_TASK"));
                user = res.getInt(m_SqlQueries.get("C_TASKLOG_USER"));
                type = res.getInt(m_SqlQueries.get("C_TASKLOG_TYPE"));
                tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
                logs.addElement(tasklog);
            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } catch( Exception exc ) {
              throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
        return logs;
    }

    /**
     * Reads all tasks of a user in a project.
     * @param project The Project in which the tasks are defined.
     * @param agent The task agent
     * @param owner The task owner .
     * @param group The group who has to process the task.
     * @param tasktype C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy Chooses, how to order the tasks.
     * @param sort Sort Ascending or Descending (ASC or DESC)
     *
     * @return A vector with the tasks
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner,
                            CmsGroup role, int tasktype,
                            String orderBy, String sort)
        throws CmsException {
        boolean first = true;
        Vector tasks = new Vector(); // vector for the return result
        CmsTask task = null;         // tmp task for adding to vector
        ResultSet recset = null;
        Connection con = null;

        // create the sql string depending on parameters
        // handle the project for the SQL String
        String sqlstr = "SELECT * FROM " + m_SqlQueries.get("C_TABLENAME_TASK")+" WHERE ";
        if(project!=null){
            sqlstr = sqlstr + m_SqlQueries.get("C_TASK_ROOT") + "=" + project.getTaskId();
            first = false;
        }
        else
        {
            sqlstr = sqlstr + m_SqlQueries.get("C_TASK_ROOT") + "<>0 AND " + m_SqlQueries.get("C_TASK_PARENT") + "<>0";
            first = false;
        }

        // handle the agent for the SQL String
        if(agent!=null){
            if(!first){
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_SqlQueries.get("C_TASK_AGENTUSER") + "=" + agent.getId();
            first = false;
        }

        // handle the owner for the SQL String
        if(owner!=null){
            if(!first){
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_SqlQueries.get("C_TASK_INITIATORUSER") + "=" + owner.getId();
            first = false;
        }

        // handle the role for the SQL String
        if(role!=null){
            if(!first){
                sqlstr = sqlstr+" AND ";
            }
            sqlstr = sqlstr + m_SqlQueries.get("C_TASK_ROLE") + "=" + role.getId();
            first = false;
        }

        sqlstr = sqlstr + getTaskTypeConditon(first, tasktype);

        // handel the order and sort parameter for the SQL String
        if(orderBy!=null) {
            if(!orderBy.equals("")) {
                sqlstr = sqlstr + " ORDER BY " + orderBy;
                if(orderBy!=null) {
                    if(!orderBy.equals("")) {
                        sqlstr = sqlstr + " " + sort;
                    }
                }
            }
        }

        Statement statement = null;

        try {
            con = DriverManager.getConnection(m_poolName);

            statement = con.createStatement();
            recset = statement.executeQuery(sqlstr);

            // if resultset exists - return vector of tasks
            while(recset.next()) {
                task =  new CmsTask(recset.getInt(m_SqlQueries.get("C_TASK_ID")),
                                    recset.getString(m_SqlQueries.get("C_TASK_NAME")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_STATE")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_TASKTYPE")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_ROOT")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_PARENT")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_INITIATORUSER")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_ROLE")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_AGENTUSER")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_ORIGINALUSER")),
                                    SqlHelper.getTimestamp(recset,m_SqlQueries.get("C_TASK_STARTTIME")),
                                    SqlHelper.getTimestamp(recset,m_SqlQueries.get("C_TASK_WAKEUPTIME")),
                                    SqlHelper.getTimestamp(recset,m_SqlQueries.get("C_TASK_TIMEOUT")),
                                    SqlHelper.getTimestamp(recset,m_SqlQueries.get("C_TASK_ENDTIME")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_PERCENTAGE")),
                                    recset.getString(m_SqlQueries.get("C_TASK_PERMISSION")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_PRIORITY")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_ESCALATIONTYPE")),
                                    recset.getString(m_SqlQueries.get("C_TASK_HTMLLINK")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_MILESTONE")),
                                    recset.getInt(m_SqlQueries.get("C_TASK_AUTOFINISH")));


                tasks.addElement(task);
            }

        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } catch( Exception exc ) {
              throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            // close all db-resources
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

        return tasks;
    }


     /**
      * Deletes a file in the database.
      * This method is used to physically remove a file form the database.
      *
      * @param project The project in which the resource will be used.
      * @param filename The complete path of the file.
      * @throws CmsException Throws CmsException if operation was not succesful
      */
     public void removeFile(int projectId, String filename)
        throws CmsException{

        PreparedStatement statement = null;
        Connection con = null;
        String usedPool;
        String usedStatement;
        CmsResource resource = m_ResourceBroker.getVfsAccess().readFileHeader(projectId, filename, true);
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            con = DriverManager.getConnection(usedPool);
            // delete the file header
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_DELETE"+usedStatement));
            statement.setString(1, filename);
            statement.executeUpdate();
            statement.close();
            // delete the file content
            statement = con.prepareStatement(m_SqlQueries.get("C_FILE_DELETE"+usedStatement));
            statement.setInt(1, resource.getFileId());
            statement.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
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
    }

    /**
     * Deletes a folder in the database.
     * This method is used to physically remove a folder form the database.
     *
     * @param folder The folder.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void removeFolder(int projectId, CmsFolder folder)
        throws CmsException{

        // the current implementation only deletes empty folders
        // check if the folder has any files in it
        String usedPool;
        String usedStatement;
        //int onlineProject = getOnlineProject(projectId).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject) {
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        Vector files= m_ResourceBroker.getVfsAccess().getFilesInFolder(projectId, folder);
        files=getUndeletedResources(files);
        if (files.size()==0) {
            // check if the folder has any folders in it
            Vector folders= m_ResourceBroker.getVfsAccess().getSubFolders(projectId, folder);
            folders=getUndeletedResources(folders);
            if (folders.size()==0) {
                //this folder is empty, delete it
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DriverManager.getConnection(usedPool);
                    // delete the folder
                    statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_ID_DELETE"+usedStatement));
                    statement.setInt(1,folder.getResourceId());
                    statement.executeUpdate();
                } catch (SQLException e){
                    throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
            } else {
                throw new CmsException("["+this.getClass().getName()+"] "+folder.getAbsolutePath(),CmsException.C_NOT_EMPTY);
            }
        } else {
            throw new CmsException("["+this.getClass().getName()+"] "+folder.getAbsolutePath(),CmsException.C_NOT_EMPTY);
        }
    }

    /**
     * Deletes a folder in the database.
     * This method is used to physically remove a folder form the database.
     * It is internally used by the publish project method.
     *
     * @param project The project in which the resource will be used.
     * @param foldername The complete path of the folder.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    protected void removeFolderForPublish(int projectId, String foldername)
        throws CmsException{

        PreparedStatement statement = null;
        Connection con = null;
        String usedPool;
        String usedStatement;
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if(projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            con = DriverManager.getConnection(usedPool);
            // delete the folder
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_DELETE"+usedStatement));
            statement.setString(1, foldername);
            statement.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
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
    }

    /**
     * Removes the temporary files of the given resource
     *
     * @param file The file of which the remporary files should be deleted
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    protected void removeTemporaryFile(CmsFile file) throws CmsException{
        PreparedStatement statement = null;
        PreparedStatement statementCont = null;
        PreparedStatement statementProp = null;
        Connection con = null;
        ResultSet res = null;
              
        String tempFilename = file.getRootName() + file.getPath() + C_TEMP_PREFIX + file.getName()+"%";
        try{
            con = DriverManager.getConnection(m_poolName);
            // get all temporary files of the resource
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_GETTEMPFILES"));
            statement.setString(1, tempFilename);
            res = statement.executeQuery();
            while(res.next()){
                int fileId = res.getInt("FILE_ID");
                int resourceId = res.getInt("RESOURCE_ID");
                // delete the properties
                statementProp = con.prepareStatement(m_SqlQueries.get("C_PROPERTIES_DELETEALL"));
                statementProp.setInt(1, resourceId);
                statementProp.executeQuery();
                statementProp.close();
                // delete the file content
                statementCont = con.prepareStatement(m_SqlQueries.get("C_FILE_DELETE"));
                statementCont.setInt(1, fileId);
                statementCont.executeUpdate();
                statementCont.close();
            }
            res.close();
            statement.close();
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_DELETETEMPFILES"));
            statement.setString(1, tempFilename);
            statement.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
            if(res != null) {
                 try {
                     res.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(statementProp != null) {
                 try {
                     statementProp.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(statementCont != null) {
                 try {
                     statementCont.close();
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
    }


    /**
     * Set a Parameter for a task.
     *
     * @param task The task.
     * @param parname Name of the parameter.
     * @param parvalue Value if the parameter.
     *
     * @return The id of the inserted parameter or 0 if the parameter exists for this task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int setTaskPar(int taskId, String parname, String parvalue)
        throws CmsException {

        ResultSet res = null;
        int result = 0;
        Connection con = null;
        PreparedStatement statement = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            // test if the parameter already exists for this task
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKPAR_TEST"));
            statement.setInt(1, taskId);
            statement.setString(2, parname);
            res = statement.executeQuery();

            if(res.next()) {
                //Parameter exisits, so make an update
                updateTaskPar(res.getInt(m_SqlQueries.get("C_PAR_ID")), parvalue);
            }
            else {
                //Parameter is not exisiting, so make an insert
                result = insertTaskPar(taskId, parname, parvalue);

            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
        return result;
    }

    /**
     * Sorts a vector of files or folders alphabetically.
     * This method uses an insertion sort algorithm.
     * NOT IN USE AT THIS TIME
     *
     * @param unsortedList Array of strings containing the list of files or folders.
     * @return Array of sorted strings.
     */
    protected Vector SortEntrys(Vector list)
    {
        int in, out;
        int nElem = list.size();
        CmsResource[] unsortedList = new CmsResource[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            unsortedList[i] = (CmsResource) list.elementAt(i);
        }
        for (out = 1; out < nElem; out++)
        {
            CmsResource temp = unsortedList[out];
            in = out;
            while (in > 0 && unsortedList[in - 1].getResourceName().compareTo(temp.getResourceName()) >= 0)
            {
                unsortedList[in] = unsortedList[in - 1];
                --in;
            }
            unsortedList[in] = temp;
        }
        Vector sortedList = new Vector();
        for (int i = 0; i < list.size(); i++)
        {
            sortedList.addElement(unsortedList[i]);
        }
        return sortedList;
    }

    /**
     * Unlocks all resources in this project.
     *
     * @param project The project to be unlocked.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void unlockProject(CmsProject project)
        throws CmsException {
        PreparedStatement statement = null;
        Connection con = null;
        String usedPool;
        String usedStatement;
        //int onlineProject = getOnlineProject(project.getId()).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (project.getId() == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            con = DriverManager.getConnection(usedPool);
            // create the statement
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UNLOCK"+usedStatement));
            statement.setInt(1,project.getId());
            statement.executeUpdate();
        } catch( Exception exc ) {
            throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
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
    }

    /**
     * Updates the LOCKED_BY state of a Resource.
     * Creation date: (29.08.00 15:01:55)
     * @param res com.opencms.file.CmsResource
     * @throws com.opencms.core.CmsException The exception description.
     */
    public void updateLockstate(CmsResource res, int projectId) throws CmsException {

        PreparedStatement statement = null;
        Connection con = null;
        String usedPool;
        String usedStatement;
        //int onlineProject = getOnlineProject(res.getProjectId()).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (projectId == onlineProject) {
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_LOCK"+usedStatement));
            statement.setInt(1, res.isLockedBy());
            statement.setInt(2, projectId);
            statement.setInt(3, res.getResourceId());
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
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
    }

    /**
     * Updates the state of a Resource.
     *
     * @param res com.opencms.file.CmsResource
     * @throws com.opencms.core.CmsException The exception description.
     */
    public void updateResourcestate(CmsResource res) throws CmsException {

        PreparedStatement statement = null;
        Connection con = null;
        String usedPool;
        String usedStatement;
        //int onlineProject = getOnlineProject(res.getProjectId()).getId();
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        if (res.getProjectId() == onlineProject) {
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        try {
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_STATE"+usedStatement));
            statement.setInt(1, res.getState());
            statement.setInt(2, res.getResourceId());
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
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
    }

    /**
     * This method updates a session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public int updateSession(String sessionId, Hashtable data)
        throws CmsException {
        byte[] value=null;
        PreparedStatement statement = null;
        Connection con = null;
        int retValue;

        try {
                  value = serializeSession(data);
            con = DriverManager.getConnection(m_poolName);

            // write data to database
            statement = con.prepareStatement(m_SqlQueries.get("C_SESSION_UPDATE"));

            statement.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
            // TESTFIX (mfoley@iee.org) Old Code: statement.setBytes(2,value);
            m_doSetBytes(statement,2,value);
            statement.setString(3,sessionId);
            retValue = statement.executeUpdate();
        }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]:"+CmsException.C_SERIALIZATION, e);
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
        return retValue;
    }
    protected void updateTaskPar(int parid, String parvalue)
        throws CmsException {

        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKPAR_UPDATE"));
            statement.setString(1, parvalue);
            statement.setInt(2, parid);
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
    }
    protected void updateTaskType(int taskId, int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref)
        throws CmsException {

        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKTYPE_UPDATE"));
            statement.setInt(1, autofinish);
            statement.setInt(2, escalationtyperef);
            statement.setString(3, htmllink);
            statement.setString(4, name);
            statement.setString(5, permission);
            statement.setInt(6, priorityref);
            statement.setInt(7, roleref);
            statement.setInt(8, taskId);
            statement.executeUpdate();
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
    }


     /**
      * Deletes a project from the cms.
      * Therefore it deletes all files, resources and properties.
      *
      * @param project the project to delete.
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void writeProject(CmsProject project)
         throws CmsException {

         PreparedStatement statement = null;
         Connection con = null;

         try {
             con = DriverManager.getConnection(m_poolName);
             // create the statement
             statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_WRITE"));

             statement.setInt(1,project.getOwnerId());
             statement.setInt(2,project.getGroupId());
             statement.setInt(3,project.getManagerGroupId());
             statement.setInt(4,project.getFlags());
             // no publishing data
             //statement.setTimestamp(5,new Timestamp(project.getPublishingDate()));
             //statement.setInt(6,project.getPublishedBy());
             statement.setInt(7,project.getId());
             statement.executeUpdate();
         } catch( Exception exc ) {
             throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                 CmsException.C_SQL_ERROR, exc);
         } finally {
            // close all db-resources
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
     }


    /**
     * Updates the name of the propertydefinition for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param metadef The propertydef to be written.
     *
     * @return The propertydefinition, that was written.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef)
        throws CmsException {
        PreparedStatement statement = null;
        CmsPropertydefinition returnValue = null;
        Connection con = null;
        try {
            // write the propertydef in the offline db
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTYDEF_UPDATE"));
            statement.setString(1, metadef.getName() );
            statement.setInt(2, metadef.getId() );
            statement.executeUpdate();
            statement.close();
            con.close();
            // write the propertydef in the online db
            con = DriverManager.getConnection(m_poolNameOnline);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTYDEF_UPDATE_ONLINE"));
            statement.setString(1, metadef.getName() );
            statement.setInt(2, metadef.getId() );
            statement.executeUpdate();
            statement.close();
            con.close();
            // write the propertydef in the backup db
            con = DriverManager.getConnection(m_poolNameBackup);
            statement = con.prepareStatement(m_SqlQueries.get("C_PROPERTYDEF_UPDATE_BACKUP"));
            statement.setString(1, metadef.getName() );
            statement.setInt(2, metadef.getId() );
            statement.executeUpdate();
            statement.close();
            con.close();
            // read the propertydefinition
            returnValue = m_ResourceBroker.getVfsAccess().readPropertydefinition(metadef.getName(), metadef.getType());
         } catch( SQLException exc ) {
             throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(),
                CmsException.C_SQL_ERROR, exc);
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
          return returnValue;
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
    public Serializable writeSystemProperty(String name, Serializable object)
        throws CmsException {

        byte[] value=null;
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();

            statement=con.prepareStatement(m_SqlQueries.get("C_SYSTEMPROPERTIES_UPDATE"));
            // TESTFIX (mfoley@iee.org) Old Code: statement.setBytes(1,value);
            m_doSetBytes(statement,1,value);
            statement.setString(2,name);
            statement.executeUpdate();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);
        }finally {
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

          return readSystemProperty(name);
    }


    public void writeSystemTaskLog(int taskid, String comment)
        throws CmsException {
        this.writeTaskLog(taskid, C_UNKNOWN_ID,
                          new java.sql.Timestamp(System.currentTimeMillis()),
                          comment, C_TASKLOG_USER);
    }

    /**
     * Updates a task.
     *
     * @param task The task that will be written.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask writeTask(CmsTask task)
        throws CmsException {

        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_UPDATE"));
            statement.setString(1,task.getName());
            statement.setInt(2,task.getState());
            statement.setInt(3,task.getTaskType());
            statement.setInt(4,task.getRoot());
            statement.setInt(5,task.getParent());
            statement.setInt(6,task.getInitiatorUser());
            statement.setInt(7,task.getRole());
            statement.setInt(8,task.getAgentUser());
            statement.setInt(9,task.getOriginalUser());
            statement.setTimestamp(10,task.getStartTime());
            statement.setTimestamp(11,task.getWakeupTime());
            statement.setTimestamp(12,task.getTimeOut());
            statement.setTimestamp(13,task.getEndTime());
            statement.setInt(14,task.getPercentage());
            statement.setString(15,task.getPermission());
            statement.setInt(16,task.getPriority());
            statement.setInt(17,task.getEscalationType());
            statement.setString(18,task.getHtmlLink());
            statement.setInt(19,task.getMilestone());
            statement.setInt(20,task.getAutoFinish());
            statement.setInt(21,task.getId());
            statement.executeUpdate();

        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
        return(readTask(task.getId()));
    }

    /**
     * Writes new log for a task.
     *
     * @param taskid The id of the task.
     * @param user User who added the Log.
     * @param starttime Time when the log is created.
     * @param comment Description for the log.
     * @param type Type of the log. 0 = Sytem log, 1 = User Log
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void writeTaskLog(int taskId, int userid,
                             java.sql.Timestamp starttime, String comment, int type)
        throws CmsException {

        int newId = C_UNKNOWN_ID;
        PreparedStatement statement = null;
        Connection con = null;
        try{
            con = DriverManager.getConnection(m_poolName);
            newId = nextId(C_TABLE_TASKLOG);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASKLOG_WRITE"));
            statement.setInt(1, newId);
            statement.setInt(2, taskId);
            if(userid!=C_UNKNOWN_ID){
                statement.setInt(3, userid);
            }
            else {
                // no user is specified so set to system user
                // is only valid for system task log
                statement.setInt(3, 1);
            }
            statement.setTimestamp(4, starttime);
            statement.setString(5, checkNull(comment));
            statement.setInt(6, type);

            statement.executeUpdate();

        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
    }

    /**
     * Creates a new tasktype set in the database.
     * @return The id of the inserted parameter or 0 if the parameter exists for this task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int writeTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref)
        throws CmsException {
        ResultSet res = null;
        int result = 0;
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            // test if the parameter already exists for this task
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_GET_TASKTYPE"));
            statement.setString(1, name);
            res = statement.executeQuery();

            if(res.next()) {
                //Parameter exisits, so make an update
                updateTaskType(res.getInt(m_SqlQueries.get("C_PAR_ID")), autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            }
            else {
                //Parameter is not exisiting, so make an insert
                result = insertTaskType(autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            }
        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
        return result;
    }


    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The new project-id
     * @param resourcename The name of the resource to change
     */
    public void changeLockedInProject(int newProjectId, String resourcename) throws CmsException{
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            // write data to database
            statement = con.prepareStatement(m_SqlQueries.get("C_RESOURCES_UPDATE_PROJECTID"));
            statement.setInt(1, newProjectId);
            statement.setString(2, resourcename);
            statement.executeUpdate();
        }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
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

    }



    /**
     * Deletes the versions from the backup tables that are older then the given date
     *
     * @param maxdate The date of the last version that should be remained after deleting
     * @return int The oldest remaining version
     */
    public int deleteBackups(long maxdate) throws CmsException{
        ResultSet res = null;
        PreparedStatement statement = null;
        Connection con = null;
        int maxVersion = 0;
        try{
            con = DriverManager.getConnection(m_poolNameBackup);
            // read the max. version_id from database by the publish_date
            statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_READ_MAXVERSION"));
            statement.setTimestamp(1, new Timestamp(maxdate));
            res = statement.executeQuery();
            if(res.next()){
                maxVersion = res.getInt(1);
            }
            res.close();
            statement.close();
            if(maxVersion > 0){
                // delete the elder backup projects by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_PROJECT_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
                // delete the elder backup projectresources by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_PROJECTRESOURCES_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
                // delete the elder backup resources by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_RESOURCES_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
                // delete the elder backup files by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_FILES_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
                // delete the elder backup properties by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_PROPERTIES_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
                // delete the elder backup moduledata by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_MODULEMASTER_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
                // delete the elder backup module media by versionId
                statement = con.prepareStatement(m_SqlQueries.get("C_BACKUP_DELETE_MODULEMEDIA_BYVERSION"));
                statement.setInt(1, maxVersion);
                statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"]", ex);
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
        return maxVersion;
    }

    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.file.genericSql.CmsQueries initQueries(Configurations config) {
        m_SqlQueries = new com.opencms.file.genericSql.CmsQueries();
        m_SqlQueries.initJdbcPoolUrls(config);        
        
        com.opencms.file.genericSql.CmsQueries queries = new com.opencms.file.genericSql.CmsQueries();
        queries.initJdbcPoolUrls(config);
        
        return queries;
    }

    
}
