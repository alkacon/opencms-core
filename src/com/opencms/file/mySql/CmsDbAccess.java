/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2003/03/18 01:51:54 $
* Version: $Revision: 1.85 $
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

package com.opencms.file.mySql;


import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.*;
import com.opencms.util.Encoder;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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
 * @version $Revision: 1.85 $ $Date: 2003/03/18 01:51:54 $ *
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {

    private static Boolean m_escapeStrings = null;

    /**
     * Returns <code>true</code> if Strings must be escaped before they are stored in the DB, 
     * this is required because MySQL does not support multi byte unicode strings.<p>
     * 
     * @return boolean <code>true</code> if Strings must be escaped before they are stored in the DB
     */
    private boolean singleByteEncoding() {
        if (m_escapeStrings == null) {    
            String encoding = A_OpenCms.getDefaultEncoding();
            m_escapeStrings = new Boolean (
                "ISO-8859-1".equalsIgnoreCase(encoding) || 
                "ISO-8859-15".equalsIgnoreCase(encoding) || 
                "US-ASCII".equalsIgnoreCase(encoding) || 
                "Cp1252".equalsIgnoreCase(encoding) 
            ); 
        }
        return m_escapeStrings.booleanValue();          
    }

    /**
     * Escapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * 
     * @param value String to be escaped
     * @return the escaped String
     */
    private String escape(String value) {          
        // no need to encode if OpenCms is not running in Unicode mode
        if (singleByteEncoding()) return value;
        return Encoder.encode(value);
    }    

    /**
     * Unescapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * 
     * @param value String to be unescaped
     * @return the unescaped String
     */
    private String unescape(String value) {        
        // no need to encode if OpenCms is not running in Unicode mode
        if (singleByteEncoding()) return value;
        return Encoder.decode(value);
    }
    
    /**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsDbAccess(Configurations config)
        throws CmsException {

        super(config);
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
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser addUser(String name, String password, String description,
                          String firstname, String lastname, String email,
                          long lastlogin, long lastused, int flags, Hashtable additionalInfos,
                          CmsGroup defaultGroup, String address, String section, int type)
        throws CmsException {
        int id = nextId(C_TABLE_USERS);
        byte[] value=null;
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(additionalInfos);
            oout.close();
            value=bout.toByteArray();

            // write data to database
            statement = con.prepareStatement(m_cq.get("C_USERS_ADD"));

            statement.setInt(1,id);
            statement.setString(2,name);
            // crypt the password with MD5
            statement.setString(3, digest(password));
            statement.setString(4, digest(""));
            statement.setString(5,description);
            statement.setString(6,firstname);
            statement.setString(7,lastname);
            statement.setString(8,email);
            statement.setTimestamp(9, new Timestamp(lastlogin));
            statement.setTimestamp(10, new Timestamp(lastused));
            statement.setInt(11,flags);
            statement.setBytes(12,value);
            statement.setInt(13,defaultGroup.getId());
            statement.setString(14,address);
            statement.setString(15,section);
            statement.setInt(16,type);
            statement.executeUpdate();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);
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
        return readUser(id);
    }
    /**
     * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
     *
     *
     */
    protected void clearFilesTable()
      throws CmsException{
        PreparedStatement statementSearch = null;
        PreparedStatement statementDestroy = null;
        ResultSet res = null;
        Connection con = null;

        try{
            con = DriverManager.getConnection(m_poolName);
            statementSearch = con.prepareStatement(m_cq.get("C_RESOURCES_GET_LOST_ID"));
            res = statementSearch.executeQuery();
            // delete the lost fileId's
            statementDestroy = con.prepareStatement(m_cq.get("C_FILE_DELETE"));
            while (res.next() ){
                statementDestroy.setInt(1,res.getInt(m_cq.get("C_FILE_ID")));
                statementDestroy.executeUpdate();
                statementDestroy.clearParameters();
            }
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
            // close all db-resources
            if(res != null) {
                 try {
                     res.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(statementDestroy != null) {
                 try {
                     statementDestroy.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(statementSearch != null) {
                 try {
                     statementSearch.close();
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
     * Creates a new file with the given content and resourcetype.
     *
     * @param user The user who wants to create the file.
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param filename The complete name of the new file (including pathinformation).
     * @param flags The flags of this resource.
     * @param parentId The parentId of the resource.
     * @param contents The contents of the new file.
     * @param resourceType The resourceType of the new file.
     *
     * @return file The created file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile createFile(CmsUser user,
                               CmsProject project,
                               CmsProject onlineProject,
                               String filename, int flags,int parentId,
                               byte[] contents, I_CmsResourceType resourceType)

        throws CmsException {
        if (filename.length() > C_MAX_LENGTH_RESOURCE_NAME){
            throw new CmsException("["+this.getClass().getName()+"] "+"Resourcename too long(>"+C_MAX_LENGTH_RESOURCE_NAME+") ",CmsException.C_BAD_NAME);
        }

        int state= C_STATE_NEW;
        // Test if the file is already there and marked as deleted.
        // If so, delete it
        try {
            readFileHeader(project.getId(), filename, false);
            throw new CmsException("["+this.getClass().getName()+"] ",CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            // if the file is maked as deleted remove it!
            if (e.getType()==CmsException.C_RESOURCE_DELETED) {
                removeFile(project.getId(),filename);
                state=C_STATE_CHANGED;
                //throw new CmsException("["+this.getClass().getName()+"] ",CmsException.C_FILE_EXISTS);
            }
            if (e.getType()==CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        String usedPool;
        String usedStatement;
        if (project.getId() == onlineProject.getId()) {
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }
        int resourceId = nextId(m_cq.get("C_TABLE_RESOURCES"+usedStatement));
        int fileId = nextId(m_cq.get("C_TABLE_FILES"+usedStatement));

        PreparedStatement statement = null;
        PreparedStatement statementFileWrite = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(usedPool);
            statement = con.prepareStatement(m_cq.get("C_RESOURCES_WRITE"+usedStatement));
            // write new resource to the database
            statement.setInt(1,resourceId);
            statement.setInt(2,parentId);
            statement.setString(3, filename);
            statement.setInt(4,resourceType.getResourceType());
            statement.setInt(5,flags);
            statement.setInt(6,user.getId());
            statement.setInt(7,user.getDefaultGroupId());
            statement.setInt(8,project.getId());
            statement.setInt(9,fileId);
            statement.setInt(10,C_ACCESS_DEFAULT_FLAGS);
            statement.setInt(11,state);
            statement.setInt(12,C_UNKNOWN_ID);
            statement.setInt(13,resourceType.getLauncherType());
            statement.setString(14,resourceType.getLauncherClass());
            statement.setTimestamp(15,new Timestamp(System.currentTimeMillis()));
            statement.setTimestamp(16,new Timestamp(System.currentTimeMillis()));
            statement.setInt(17,contents.length);
            statement.setInt(18,user.getId());
            statement.executeUpdate();

            statementFileWrite = con.prepareStatement(m_cq.get("C_FILES_WRITE"+usedStatement));
            statementFileWrite.setInt(1,fileId);
            statementFileWrite.setBytes(2,contents);
            statementFileWrite.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
            // close all db-resources
            if(statement != null) {
                try {
                    statement.close();
                } catch(SQLException exc) {
                    // nothing to do here
                }
            }
            if(statementFileWrite != null) {
                try {
                    statementFileWrite.close();
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
        return readFile(project.getId(),onlineProject.getId(),filename);
    }

    /**
     * Add a new group to the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param name The name of the new group.
     * @param description The description for the new group.
     * @param flags The flags for the new group.
     * @param name The name of the parent group (or null).
     *
     * @return Group
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
  public CmsGroup createGroup (String name, String description, int flags,
                   String parent) throws CmsException
  {
        int parentId = C_UNKNOWN_ID;
        CmsGroup group = null;
        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            // get the id of the parent group if nescessary
            if ((parent != null) && (!"".equals (parent))){
                parentId = readGroup (parent).getId ();
            }
            // create statement
            statement = con.prepareStatement(m_cq.get("C_GROUPS_CREATEGROUP"));
            // write new group to the database
            statement.setInt (1, nextId (C_TABLE_GROUPS));
            statement.setInt (2, parentId);
            statement.setString (3, name);
            statement.setString (4, description);
            statement.setInt (5, flags);
            statement.executeUpdate ();

            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            group = readGroup (name);
        } catch (SQLException e){
            throw new CmsException ("[" + this.getClass ().getName () + "] " +
                e.getMessage (), CmsException.C_SQL_ERROR, e);
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
        return group;
    }

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project) throws CmsException {
        // get all resources of the project
        Vector resources = readResources(project);
        for (int i = 0; i < resources.size(); i++) {
            // delete the properties for each resource in project
            deleteAllProperties(project.getId(),(CmsResource) resources.elementAt(i));
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
             statement = con.prepareStatement(m_cq.get("C_PROJECTS_READLAST_BACKUP"));
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
     * Destroys this access-module
     * @throws throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        try {
            ((com.opencms.dbpool.CmsDriver) DriverManager.getDriver(m_poolName)).destroy();
        } catch(SQLException exc) {
            // destroy not possible - ignoring the exception
        }

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[mySql.CmsDbAccess] Destroyed");
        }
    }

    /**
     * Private method to init all default-resources
     */
    protected void fillDefaults() throws CmsException {
        // set the groups
        CmsGroup guests = createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
        CmsGroup administrators = createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER, null);
        CmsGroup users = createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED | C_FLAG_GROUP_ROLE | C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
        CmsGroup projectleader = createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER | C_FLAG_GROUP_PROJECTCOWORKER | C_FLAG_GROUP_ROLE, users.getName());

        // add the users
        CmsUser guest = addUser(C_USER_GUEST, "", "the guest-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, "", "", C_USER_TYPE_SYSTEMUSER);
        CmsUser admin = addUser(C_USER_ADMIN, "admin", "the admin-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, "", "", C_USER_TYPE_SYSTEMUSER);
        addUserToGroup(guest.getId(), guests.getId());
        addUserToGroup(admin.getId(), administrators.getId());
        writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);
        // create the online project
        CmsTask task = createTask(0, 0, 1, // standart project type,
                    admin.getId(), admin.getId(), administrators.getId(), C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
        CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        int siteRootId = 0;
        CmsFolder rootFolder = createFolder(admin, online, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(online, rootFolder, false);
        rootFolder = createFolder(admin, online, rootFolder.getResourceId(), C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(online, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        rootFolder = createFolder(admin, online, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(online, rootFolder, false);
        rootFolder = createFolder(admin, online, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(online, rootFolder, false);
        // create the setup project
        task = createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(),
                                    "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()),
                                    new java.sql.Timestamp(new java.util.Date().getTime()),
                                    C_TASK_PRIORITY_NORMAL);

        CmsProject setup = createProject(admin, administrators, administrators, task, "_setupProject",
                                           "the project for setup", C_FLAG_ENABLED, C_PROJECT_TYPE_TEMPORARY);

        // create the root-folder for the offline project
        rootFolder = createFolder(admin, setup, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(setup, rootFolder, false);
        rootFolder = createFolder(admin, setup, rootFolder.getResourceId(), C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(setup, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        rootFolder = createFolder(admin, setup, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(setup, rootFolder, false);
        rootFolder = createFolder(admin, setup, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        writeFolder(setup, rootFolder, false);
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
        return super.findAgent(roleid);
    }

/**
 * retrieve the correct instance of the queries holder.
 * This method should be overloaded if other query strings should be used.
 */
protected com.opencms.file.genericSql.CmsQueries getQueries()
{
    return new com.opencms.file.mySql.CmsQueries();
}

/**
 * Reads a file from the Cms.<BR/>
 *
 * @param projectId The Id of the project in which the resource will be used.
 * @param onlineProjectId The online projectId of the OpenCms.
 * @param filename The complete name of the new file (including pathinformation).
 *
 * @return file The read file.
 *
 * @throws CmsException Throws CmsException if operation was not succesful
 */
public CmsFile readFile(int projectId, int onlineProjectId, String filename) throws CmsException {
    CmsFile file = null;
    PreparedStatement statement = null;
    ResultSet res = null;
    Connection con = null;
    String usedPool;
    String usedStatement;
    if (projectId == onlineProjectId){
        usedPool = m_poolNameOnline;
        usedStatement = "_ONLINE";
    } else {
        usedPool = m_poolName;
        usedStatement = "";
    }
    try {
        con = DriverManager.getConnection(usedPool);
        statement = con.prepareStatement(m_cq.get("C_FILES_READ"+usedStatement));
        statement.setString(1, filename);
        statement.setInt(2, projectId);
        res = statement.executeQuery();
        if (res.next()) {
            int resId = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_ID"));
            int parentId = res.getInt(m_cq.get("C_RESOURCES_PARENT_ID"));
            int resType = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_TYPE"));
            int resFlags = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_FLAGS"));
            int userId = res.getInt(m_cq.get("C_RESOURCES_USER_ID"));
            int groupId = res.getInt(m_cq.get("C_RESOURCES_GROUP_ID"));
            int fileId = res.getInt(m_cq.get("C_RESOURCES_FILE_ID"));
            int accessFlags = res.getInt(m_cq.get("C_RESOURCES_ACCESS_FLAGS"));
            int state = res.getInt(m_cq.get("C_RESOURCES_STATE"));
            int lockedBy = res.getInt(m_cq.get("C_RESOURCES_LOCKED_BY"));
            int launcherType = res.getInt(m_cq.get("C_RESOURCES_LAUNCHER_TYPE"));
            String launcherClass = res.getString(m_cq.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
            long created = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_CREATED")).getTime();
            long modified = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
            int modifiedBy = res.getInt(m_cq.get("C_RESOURCES_LASTMODIFIED_BY"));
            int resSize = res.getInt(m_cq.get("C_RESOURCES_SIZE"));
            byte[] content = res.getBytes(m_cq.get("C_RESOURCES_FILE_CONTENT"));
            int resProjectId = res.getInt(m_cq.get("C_RESOURCES_PROJECT_ID"));
            int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
            file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId,
                               groupId, resProjectId, accessFlags, state, lockedBy, launcherType,
                               launcherClass, created, modified, modifiedBy, content, resSize, lockedInProject);
            // check if this resource is marked as deleted
            if (file.getState() == C_STATE_DELETED) {
                throw new CmsException("["+this.getClass().getName()+"] "+file.getAbsolutePath(),CmsException.C_RESOURCE_DELETED);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
        }
    } catch (SQLException e) {
        throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
    } catch (CmsException ex) {
        throw ex;
    } catch (Exception exc) {
        throw new CmsException("readFile " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
    return file;
}

/**
 * Reads a file from the Cms.<BR/>
 *
 * @param projectId The Id of the project in which the resource will be used.
 * @param onlineProjectId The online projectId of the OpenCms.
 * @param filename The complete name of the new file (including pathinformation).
 *
 * @return file The read file.
 *
 * @throws CmsException Throws CmsException if operation was not succesful
 */
public CmsFile readFile(int projectId, int onlineProjectId, String filename, boolean includeDeleted) throws CmsException {
    CmsFile file = null;
    PreparedStatement statement = null;
    ResultSet res = null;
    Connection con = null;
    String usedPool;
    String usedStatement;
    if (projectId == onlineProjectId){
        usedPool = m_poolNameOnline;
        usedStatement = "_ONLINE";
    } else {
        usedPool = m_poolName;
        usedStatement = "";
    }
    try {
        con = DriverManager.getConnection(usedPool);
        statement = con.prepareStatement(m_cq.get("C_FILES_READ"+usedStatement));
        statement.setString(1, filename);
        statement.setInt(2, projectId);
        res = statement.executeQuery();
        if (res.next()) {
            int resId = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_ID"));
            int parentId = res.getInt(m_cq.get("C_RESOURCES_PARENT_ID"));
            int resType = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_TYPE"));
            int resFlags = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_FLAGS"));
            int userId = res.getInt(m_cq.get("C_RESOURCES_USER_ID"));
            int groupId = res.getInt(m_cq.get("C_RESOURCES_GROUP_ID"));
            int fileId = res.getInt(m_cq.get("C_RESOURCES_FILE_ID"));
            int accessFlags = res.getInt(m_cq.get("C_RESOURCES_ACCESS_FLAGS"));
            int state = res.getInt(m_cq.get("C_RESOURCES_STATE"));
            int lockedBy = res.getInt(m_cq.get("C_RESOURCES_LOCKED_BY"));
            int launcherType = res.getInt(m_cq.get("C_RESOURCES_LAUNCHER_TYPE"));
            String launcherClass = res.getString(m_cq.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
            long created = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_CREATED")).getTime();
            long modified = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
            int modifiedBy = res.getInt(m_cq.get("C_RESOURCES_LASTMODIFIED_BY"));
            int resSize = res.getInt(m_cq.get("C_RESOURCES_SIZE"));
            byte[] content = res.getBytes(m_cq.get("C_RESOURCES_FILE_CONTENT"));
            int resProjectId = res.getInt(m_cq.get("C_RESOURCES_PROJECT_ID"));
            int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
            file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId,
                               groupId, resProjectId, accessFlags, state, lockedBy, launcherType,
                               launcherClass, created, modified, modifiedBy, content, resSize, lockedInProject);
            // check if this resource is marked as deleted
            if (file.getState() == C_STATE_DELETED &&!includeDeleted) {
                throw new CmsException("["+this.getClass().getName()+"] "+file.getAbsolutePath(),CmsException.C_RESOURCE_DELETED);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
        }
    } catch (SQLException e) {
        throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
    } catch (CmsException ex) {
        throw ex;
    } catch (Exception exc) {
        throw new CmsException("readFile " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
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
    return file;
}

    /**
     * Creates a new task.<p>
     * 
     * MySQL 4 does not support the SQL from the generic driver, so that's
     * why we have that special implementation here. 
     * This was tested with MySQL 4.0.10. 
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
        // fetch new task id
        int newId = nextId(C_TABLE_TASK);        
        // create the task id entry in the DB                 
        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_cq.get("C_TASK_CREATE"));
            statement.setInt(1, newId);
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
        // create the task object, note that this does not user the "task type" table
        // because the generic SQL does not work with MySQL 4 
        CmsTask task = new CmsTask(newId, taskname, C_TASK_STATE_STARTED, tasktype, rootId, parentId, ownerId, roleId, agentId, 
                        agentId, new java.sql.Timestamp(System.currentTimeMillis()), wakeuptime, timeout, null, 0, 
                        "30308", priority, 0, "../taskforms/adhoc.asp", 0, 1);       
        // write tast
        task = writeTask(task);
        return task;
    }
    
    /**
     * Reads a task from the Cms with
     * added escaping of Strings since MySQL dosen't support Unicode strings
     *
     * @param id the id of the task to read
     * @return a task object or null if the task is not found
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(int id) throws CmsException {
        CmsTask task = super.readTask(id);
        if (task != null) task.setName(unescape(task.getName()));
        return task;
    }

    /**
     * Reads all tasks of a user in a project with
     * added escaping of Strings since MySQL dosen't support Unicode strings.
     * 
     * @param project the Project in which the tasks are defined
     * @param agent the task agent
     * @param owner the task owner .
     * @param group the group who has to process the task
     * @param tasktype one of C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy selects filter how to order the tasks
     * @param sort select to sort ascending or descending ("ASC" or "DESC")
     * @return a vector with the tasks read
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) 
    throws CmsException {
        Vector v = super.readTasks(project, agent, owner, role, tasktype, orderBy, sort);
        for (int i=0; i<v.size(); i++) {
            CmsTask task = (CmsTask)v.elementAt(i);
            task.setName(unescape(task.getName()));
            v.set(i, task);
        }    
        return v;          
    }

    /**
     * Writes a task from the Cms with
     * added escaping of Strings since MySQL dosen't support Unicode strings
     *
     * @param id the id of the task to write
     * @return written task object
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask writeTask(CmsTask task) throws CmsException {
        task.setName(escape(task.getName()));
        task = super.writeTask(task);
        task.setName(unescape(task.getName()));        
        return task;
    }

    /**
     * Writes a property for a file or folder with
     * added escaping of property values as MySQL doesn't support Unicode strings
     *
     * @param meta The property-name of which the property has to be read.
     * @param value The value for the property to be set.
     * @param resourceId The id of the resource.
     * @param resourceType The Type of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperty(String meta, int projectId, String value, CmsResource resource,
                                      int resourceType, boolean addDefinition)
        throws CmsException {            
        super.writeProperty(meta, projectId, escape(value), resource, resourceType, addDefinition);
    }

    /**
     * Added unescaping of property values as MySQL doesn't support Unicode strings
     * 
     * @see com.opencms.file.genericSql.CmsDbAccess#readProperty(String, int, CmsResource, int)
     */
    public String readProperty(String meta, int projectId,
        CmsResource resource, int resourceType) throws CmsException {
        return unescape(super.readProperty(meta, projectId, resource, resourceType));
    }

    /**
     * Added unescaping of property values as MySQL doesn't support Unicode strings
     * 
     * @see com.opencms.file.genericSql.CmsDbAccess#readAllProperties(int, CmsResource, int)
     */
    public HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException {
        HashMap original = super.readProperties(projectId, resource, resourceType);
        if (singleByteEncoding()) return original; 
        HashMap result = new HashMap(original.size());
        Iterator keys = original.keySet().iterator();        
        while (keys.hasNext()) {
            Object key = keys.next();       
            result.put(key, unescape((String)original.get(key)));
        }
        original.clear();
        return result;
    }

    /**
     * Writes new log for a task with
     * added escaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param taskid The id of the task.
     * @param user User who added the Log.
     * @param starttime Time when the log is created.
     * @param comment Description for the log.
     * @param type Type of the log. 0 = Sytem log, 1 = User Log
     *
     * @throws CmsException if something goes wrong
     */
    public void writeTaskLog(int taskId, int userid,
                             java.sql.Timestamp starttime, String comment, int type)
        throws CmsException {
        super.writeTaskLog(taskId, userid, starttime, escape(comment), type);
    }
        
    /**
     * Reads a log for a task with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param id The id for the tasklog .
     * @return A new TaskLog object
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTaskLog readTaskLog(int id)
        throws CmsException {
        CmsTaskLog log = super.readTaskLog(id);
        log.setComment(unescape(log.getComment()));
        return log;
    }    

    /**
     * Reads log entries for a task with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param taskid The id of the task for the tasklog to read .
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTaskLogs(int taskId) throws CmsException {
        Vector v = super.readTaskLogs(taskId);
        for (int i=0; i<v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog)v.elementAt(i);
            log.setComment(unescape(log.getComment()));
            v.set(i, log);
        }    
        return v;    
    }
    
    /**
     * Reads log entries for a project with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param project The projec for tasklog to read.
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(int projectid) throws CmsException {
        Vector v = super.readProjectLogs(projectid);
        for (int i=0; i<v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog)v.elementAt(i);
            log.setComment(unescape(log.getComment()));
            v.set(i, log);
        }
        return v;    
    }    
                                       
    /**
     * Writes a user to the database.
     *
     * @param user the user to write
     * @throws thorws CmsException if something goes wrong.
     */
    public void writeUser(CmsUser user)
        throws CmsException {
        byte[] value=null;
        PreparedStatement statement = null;
        Connection con = null;

        try {
            con = DriverManager.getConnection(m_poolName);
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(user.getAdditionalInfo());
            oout.close();
            value=bout.toByteArray();

            // write data to database
            statement = con.prepareStatement(m_cq.get("C_USERS_WRITE"));

            statement.setString(1,user.getDescription());
            statement.setString(2,user.getFirstname());
            statement.setString(3,user.getLastname());
            statement.setString(4,user.getEmail());
            statement.setTimestamp(5, new Timestamp(user.getLastlogin()));
            statement.setTimestamp(6, new Timestamp(user.getLastUsed()));
            statement.setInt(7,user.getFlags());
            statement.setBytes(8,value);
            statement.setInt(9, user.getDefaultGroupId());
            statement.setString(10,user.getAddress());
            statement.setString(11,user.getSection());
            statement.setInt(12,user.getType());
            statement.setInt(13,user.getId());
            statement.executeUpdate();
        }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);
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
}
