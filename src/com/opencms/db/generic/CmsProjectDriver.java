/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/generic/Attic/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/05/23 09:16:02 $
 * Version: $Revision: 1.3 $
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
 
package com.opencms.db.generic;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.db.CmsDriverManager;
import com.opencms.file.*;
import com.opencms.flex.util.CmsUUID;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * @version $Revision: 1.3 $ $Date: 2003/05/23 09:16:02 $ *
 */
public class CmsProjectDriver implements I_CmsConstants, I_CmsLogChannels {
	/**
	 * Returns all projects, which are owned by a user.
	 *
	 * @param user The requesting user.
	 *
	 * @return a Vector of projects.
	 */
	public Vector getAllAccessibleProjectsByUser(CmsUser user) throws CmsException {
	    Vector projects = new Vector();
	    ResultSet res = null;
	    PreparedStatement stmt = null;
	    Connection conn = null;
	
	    try {
	        // create the statement
	        conn = m_sqlManager.getConnection();
	        stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYUSER");
	
	        stmt.setString(1, user.getId().toString());
	        res = stmt.executeQuery();
	
	        while (res.next()) {
	            projects.addElement(new CmsProject(res, m_sqlManager));
	        }
	    } catch (Exception exc) {
	        throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
	    } finally {
	        m_sqlManager.closeAll(conn, stmt, res);
	    }
	    return (projects);
	}
	/**
	 * Returns all projects, which are manageable by a group.
	 *
	 * @param group The requesting group.
	 *
	 * @return a Vector of projects.
	 */
	public Vector getAllAccessibleProjectsByManagerGroup(CmsGroup group) throws CmsException {
	    Vector projects = new Vector();
	    ResultSet res = null;
	    PreparedStatement stmt = null;
	    Connection conn = null;
	
	    try {
	        // create the statement
	        conn = m_sqlManager.getConnection();
	        stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYMANAGER");
	        
	        stmt.setString(1, group.getId().toString());
	        res = stmt.executeQuery();
	
	        while (res.next()) {
	            projects.addElement(new CmsProject(res, m_sqlManager));
	        }
	    } catch (Exception exc) {
	        throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
	    } finally {
	        m_sqlManager.closeAll(conn, stmt, res);
	    }
	    return (projects);
	}
	/**
	 * Returns all projects, which are accessible by a group.
	 *
	 * @param group The requesting group.
	 *
	 * @return a Vector of projects.
	 */
	public Vector getAllAccessibleProjectsByGroup(CmsGroup group) throws CmsException {
	    Vector projects = new Vector();
	    ResultSet res = null;
	    Connection conn = null;
	    PreparedStatement stmt = null;
	
	    try {
	        // create the statement
	        conn = m_sqlManager.getConnection();
	        stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYGROUP");
	
	        stmt.setString(1, group.getId().toString());
	        stmt.setString(2, group.getId().toString());
	        res = stmt.executeQuery();
	
	        while (res.next()) {
	            projects.addElement(new CmsProject(res, m_sqlManager));
	        }
	    } catch (Exception exc) {
	        throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
	    } finally {
	        m_sqlManager.closeAll(conn, stmt, res);
	    }
	    return (projects);
	}
    
    public static int C_RESTYPE_LINK_ID = 2;
    public static boolean C_USE_TARGET_DATE = true;    

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
    protected static String C_TABLE_PROJECTS = "CMS_PROJECTS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROPERTYDEF = "CMS_PROPERTYDEF";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROPERTIES = "CMS_PROPERTIES";

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
   protected com.opencms.db.generic.CmsSqlManager m_sqlManager;
   
   protected CmsDriverManager m_driverManager;
   protected String m_dbPoolUrl;

    public void init(Configurations config, String dbPoolUrl) throws CmsException {
        m_sqlManager = initQueries(dbPoolUrl);
		m_dbPoolUrl = dbPoolUrl;

        if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database access init : ok" );
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
        Connection conn = null;
        PreparedStatement stmt = null;
         try    {
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();

            // create the object
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_WRITE");
            stmt.setInt(1,m_sqlManager.nextId(C_TABLE_SYSTEMPROPERTIES));
            stmt.setString(2,name);
            m_sqlManager.setBytes(stmt,3,value);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        }finally {
             // close all db-resources
             m_sqlManager.closeAll(conn, stmt, null);
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
     * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
     *
     *
     */
    protected void clearFilesTable() throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_LOST_ID");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int retValue;
        try {
            // create the statement
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_COUNTLOCKED");
            stmt.setString(1,CmsUUID.getNullUUID().toString());
            stmt.setInt(2,project.getId());
            res = stmt.executeQuery();
            if(res.next()) {
                retValue = res.getInt(1);
            } else {
                retValue=0;
            }
        } catch( Exception exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        int returnValue;
        try {
            // create statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_READALL_COUNT");
            stmt.setInt(1, metadef.getId());
            res = stmt.executeQuery();

            if( res.next() ) {
                returnValue = res.getInt(1) ;
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(),
                    CmsException.C_UNKNOWN_EXCEPTION);
            }
        } catch(SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        }finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            m_driverManager.getVfsDriver().readProjectResource(projectId, resourceName);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE");
            // write new resource to the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourceName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
    public CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type) throws CmsException {

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        Timestamp createTime = new Timestamp(new java.util.Date().getTime());
        Connection conn = null;
        PreparedStatement stmt = null;

        int id = m_sqlManager.nextId(C_TABLE_PROJECTS);

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE");
            // write data to database
            stmt.setInt(1, id);
            stmt.setString(2, owner.getId().toString());
            stmt.setString(3, group.getId().toString());
            stmt.setString(4, managergroup.getId().toString());
            stmt.setInt(5, task.getId());
            stmt.setString(6, name);
            stmt.setString(7, description);
            stmt.setInt(8, flags);
            stmt.setTimestamp(9, createTime);
            // no publish data
            stmt.setInt(10, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] warning, following entrys are not serializeable in the session: " + notSerializable.toString() + ".");
        }
        return value;
    }

    /**
     * Deletes all properties for a file or folder.
     *
     * @param resourceId The id of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(int projectId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETEALL");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException exc) { 
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);         
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes all properties for a file or folder.
     *
     * @param resourceId The id of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(int projectId, CmsUUID resourceId)
        throws CmsException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETEALL");
            stmt.setString(1, resourceId.toString());
            stmt.executeUpdate();
        } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_DELETE");
            // create the statement
            stmt.setInt(1,project.getId());
            stmt.executeUpdate();
        } catch( Exception exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project) throws CmsException {

        // delete properties with one statement
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALLPROP");
            // create statement
            stmt.setInt(1, project.getId());
            stmt.executeQuery();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

    }

    /**
     * Deletes a specified project
     *
     * @param project The project to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteProjectResources(CmsProject project) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_PROJECT");
            // delete all project-resources.
            stmt.setInt(1, project.getId());
            stmt.executeQuery();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL");
            // delete all projectResources from the database
            stmt.setInt(1, projectId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETE");
            // delete resource from the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourceName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        CmsPropertydefinition propdef = m_driverManager.getVfsDriver().readPropertydefinition(meta, resourceType);
        if( propdef == null) {
            // there is no propdefinition with the overgiven name for the resource
            throw new CmsException("[" + this.getClass().getName() + "] " + meta,
                CmsException.C_NOT_FOUND);
        } else {
            // delete the metainfo in the db
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                // create statement
                conn = m_sqlManager.getConnection(projectId);
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETE");
                stmt.setInt(1, propdef.getId());
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();
            } catch(SQLException exc) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            if(countProperties(metadef) != 0) {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(),
                    CmsException.C_UNKNOWN_EXCEPTION);
            }
            for (int i = 0; i < 3; i++){
                // delete the propertydef from offline db
                if (i == 0) {
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE");
                }
                // delete the propertydef from online db
                else if (i == 1){
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_DELETE");
                }
                // delete the propertydef from backup db
                else {
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE_BACKUP");
                }
                stmt.setInt(1, metadef.getId() );
                stmt.executeUpdate();
                stmt.close();
                conn.close();
            }
         } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
         }finally {
            m_sqlManager.closeAll(conn, stmt, null);
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

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_DELETE");
            stmt.setString(1,name);
            stmt.executeUpdate();
        }catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        }finally {
            m_sqlManager.closeAll(conn, stmt, null);
          }
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

    /**
     * Ends a task from the Cms.
     *
     * @param taskid Id of the task to end.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void endTask(int taskId)
        throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_END");
            stmt.setInt(1, 100);
            stmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setInt(3,taskId);
            stmt.executeUpdate();

        } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Private method to init all default-resources
     */
    // protected 
    public void fillDefaults(CmsDriverManager driverManager) throws CmsException
    {
    	// TODO: there is still the driver manager neccessary, remove it!!
    	m_driverManager = driverManager;
    	
		try {
			if (readProject(C_PROJECT_ONLINE_ID) != null) {
				// online-project exists - no need of filling defaults
				return;
			}
		} catch(CmsException exc) {
			// ignore the exception - the project was not readable so fill in the defaults
		}
		
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsProjectDriver] Initial setup of the OpenCms database starts NOW!");
        }

        // set the groups
        CmsGroup guests = m_driverManager.getUserDriver().createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
        CmsGroup administrators = m_driverManager.getUserDriver().createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER, null);
        CmsGroup users = m_driverManager.getUserDriver().createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED | C_FLAG_GROUP_ROLE | C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
        CmsGroup projectleader = m_driverManager.getUserDriver().createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER | C_FLAG_GROUP_PROJECTCOWORKER | C_FLAG_GROUP_ROLE, users.getName());

        // add the users
        CmsUser guest = m_driverManager.getUserDriver().addUser(C_USER_GUEST, "", "the guest-user", " ", " ", " ", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, " ", " ", C_USER_TYPE_SYSTEMUSER);
        CmsUser admin = m_driverManager.getUserDriver().addUser(C_USER_ADMIN, "admin", "the admin-user", " ", " ", " ", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, " ", " ", C_USER_TYPE_SYSTEMUSER);
		m_driverManager.getUserDriver().addUserToGroup(guest.getId(), guests.getId());
		m_driverManager.getUserDriver().addUserToGroup(admin.getId(), administrators.getId());
        m_driverManager.getWorkflowDriver().writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);
        
        // create the online project
        CmsTask task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
        CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        CmsUUID siteRootId = CmsUUID.getNullUUID();
        CmsFolder rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);
        
        // create the folder for the default site
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, rootFolder.getResourceId(), CmsUUID.getNullUUID(), C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        
        // create the folder for the virtual file system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, siteRootId, CmsUUID.getNullUUID(), C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);
        
        // create the folder for the context objects system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, siteRootId, CmsUUID.getNullUUID(), C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);
        
        // create the task for the setup project
        task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
        CmsProject setup = createProject(admin, administrators, administrators, task, "_setupProject", "Initial site import", C_FLAG_ENABLED, C_PROJECT_TYPE_TEMPORARY);

        // create the root-folder for the offline project
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);
        
        // create the folder for the default site
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, rootFolder.getResourceId(), CmsUUID.getNullUUID(), C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        
        // create the folder for the virtual file system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, siteRootId, CmsUUID.getNullUUID(), C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);
        
        // create the folder for the context objects system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, siteRootId, CmsUUID.getNullUUID(), C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
		m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);
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
    public void forwardTask(int taskId, CmsUUID newRoleId, CmsUUID newUserId)
        throws CmsException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_FORWARD");
            stmt.setString(1,newRoleId.toString());
            stmt.setString(2,newUserId.toString());
            stmt.setInt(3,taskId);
            stmt.executeUpdate();
        } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Returns all projects, with the overgiven state.
     *
     * @param state The state of the projects to read.
     *
     * @return a Vector of projects.
     */
    public Vector getAllProjects(int state) throws CmsException {
        Vector projects = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYFLAG");

            stmt.setInt(1, state);
            res = stmt.executeQuery();

            while (res.next()) {
                projects.addElement(
                    new CmsProject(
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_FLAGS")),
                        SqlHelper.getTimestamp(res, m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, "getAllProjects(int)", CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
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
             stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READLAST_BACKUP");
             res = stmt.executeQuery();
             int i = 0;
             int max = 300;
             while(res.next() && (i < max)) {
                 Vector resources = readBackupProjectResources(res.getInt("VERSION_ID"));
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
                 i++;
             }
         } catch( SQLException exc ) {
             throw m_sqlManager.getCmsException(this, "getAllBackupProjects()", CmsException.C_SQL_ERROR, exc);
         } finally {
             m_sqlManager.closeAll(conn, stmt, res);
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
         PreparedStatement stmt = null;
         Connection conn = null;
         try {
             // get parent group
             parent=m_driverManager.getUserDriver().readGroup(groupname);
            // parent group exists, so get all childs
            if (parent != null) {
                // create statement
                conn = m_sqlManager.getConnection();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETCHILD");
                stmt.setString(1,parent.getId().toString());
                res = stmt.executeQuery();
                // create new Cms group objects
                while ( res.next() ) {
                     group=new CmsGroup(new CmsUUID(res.getString(m_sqlManager.get("C_GROUPS_GROUP_ID"))),
                                  new CmsUUID(res.getString(m_sqlManager.get("C_GROUPS_PARENT_GROUP_ID"))),
                                  res.getString(m_sqlManager.get("C_GROUPS_GROUP_NAME")),
                                  res.getString(m_sqlManager.get("C_GROUPS_GROUP_DESCRIPTION")),
                                  res.getInt(m_sqlManager.get("C_GROUPS_GROUP_FLAGS")));
                    childs.addElement(group);
                }
             }

         } catch (SQLException e){
             throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
         } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_FILES_WITH_PROPERTY");
            stmt.setInt(1, projectId);
            stmt.setString(2, propertyValue);
            stmt.setString(3, propertyDefinition);
            res = stmt.executeQuery();

            // store the result into the vector
            while (res.next()) {
                String result = res.getString(1);
                names.addElement(result);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, "getFilesWithProperty(int, String, String)", CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        CmsResource res = m_driverManager.getVfsDriver().readFileHeader(projectId, resource, false);
        CmsUUID groupId = CmsUUID.getNullUUID();
        boolean noGroupCanReadThis = false;
        do{
            int flags = res.getAccessFlags();
            if(!((flags & C_ACCESS_PUBLIC_READ ) == C_ACCESS_PUBLIC_READ)){
                if((flags & C_ACCESS_GROUP_READ) == C_ACCESS_GROUP_READ){
                    if((groupId.isNullUUID()) || (groupId.equals(res.getGroupId()))){
                        groupId = res.getGroupId();
                    }else{
                        CmsUUID result = m_driverManager.getUserDriver().checkGroupDependence(groupId, res.getGroupId());
                        if(result.isNullUUID()){
                            noGroupCanReadThis = true;
                        }else{
                            groupId = result;
                        }
                    }
                }else{
                    noGroupCanReadThis = true;
                }
            }
            res = m_driverManager.getVfsDriver().readFileHeader(projectId, res.getParentId());
        }while(!(noGroupCanReadThis || C_ROOT.equals(res.getAbsolutePath())));
        if (noGroupCanReadThis){
            return C_GROUP_ADMIN;
        }
        if(groupId.isNullUUID()){
            return null;
        }else{
            return m_driverManager.getUserDriver().readGroup(groupId).getName();
        }
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


    /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
     * @param onlineProject The online project of the OpenCms.
     * @param report A report object to provide the loggin messages.
     * @return a vector of changed or deleted resources.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(CmsUser user, int projectId, CmsProject onlineProject, boolean enableHistory, I_CmsReport report, Hashtable exportpoints) throws CmsException {
        CmsExportPointDriver discAccess = new CmsExportPointDriver(exportpoints);
        CmsFolder currentFolder = null;
        CmsFile currentFile = null;
        CmsFolder newFolder = null;
        CmsFile newFile = null;
        Vector offlineFolders;
        Vector offlineFiles;
        Vector deletedFolders = new Vector();
        Vector changedResources = new Vector();

        Map folderIdIndex = (Map) new HashMap();

        CmsProject currentProject = readProject(projectId);
        int versionId = 1;
        long publishDate = System.currentTimeMillis();
        
        if (enableHistory) {
            // get the version id for the backup
            versionId = getBackupVersionId();
            // store the projectdata to the backuptables for history
            backupProject(currentProject, versionId, publishDate, user);
        }
        
        // read all folders in offlineProject
        offlineFolders = m_driverManager.getVfsDriver().readFolders(projectId, false, true);
        for (int i = 0; i < offlineFolders.size(); i++) {
            currentFolder = ((CmsFolder) offlineFolders.elementAt(i));
            report.print(report.key("report.publishing"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFolder.getAbsolutePath());
            // do not publish the folder if it is locked in another project
            
            if (currentFolder.isLocked()) {
                // in this case do nothing
                // C_STATE_DELETE
            } else if (currentFolder.getState() == C_STATE_DELETED) {
                deletedFolders.addElement(currentFolder);
                changedResources.addElement(currentFolder.getResourceName());
                // C_STATE_NEW
            } else if (currentFolder.getState() == C_STATE_NEW) {
                changedResources.addElement(currentFolder.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
                }
                
                // get parentId for onlineFolder either from folderIdIndex or from the database
                CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFolder.getParentId());
                if (parentId == null) {
                    CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getRootName() + currentFolder.getParent());
                    parentId = currentOnlineParent.getResourceId();
                    folderIdIndex.put(currentFolder.getParentId(), parentId);
                }
                
                // create the new folder and insert its id in the folderindex
                try {
                    newFolder = m_driverManager.getVfsDriver().createFolder(user, onlineProject, onlineProject, currentFolder, parentId, currentFolder.getResourceName());
                    newFolder.setState(C_STATE_UNCHANGED);
                    updateResourcestate(newFolder);
                } catch (CmsException e) {
                    // if the folder already exists in the onlineProject then update the onlineFolder
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        CmsFolder onlineFolder = null;
                        try {
                            onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                        } catch (CmsException exc) {
                            throw exc;
                        } // end of catch
                        PreparedStatement stmt = null;
                        Connection conn = null;
                        try {
                            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                            // update the onlineFolder with data from offlineFolder
                            stmt.setInt(1, currentFolder.getType());
                            stmt.setInt(2, currentFolder.getFlags());
                            stmt.setString(3, currentFolder.getOwnerId().toString());
                            stmt.setString(4, currentFolder.getGroupId().toString());
                            stmt.setInt(5, onlineFolder.getProjectId());
                            stmt.setInt(6, currentFolder.getAccessFlags());
                            stmt.setInt(7, C_STATE_UNCHANGED);
                            stmt.setString(8, currentFolder.isLockedBy().toString());
                            stmt.setInt(9, currentFolder.getLauncherType());
                            stmt.setString(10, currentFolder.getLauncherClassname());
                            stmt.setTimestamp(11, new Timestamp(currentFolder.getDateLastModified()));
                            stmt.setString(12, currentFolder.getResourceLastModifiedBy().toString());
                            stmt.setInt(13, 0);
                            stmt.setString(14, onlineFolder.getFileId().toString());
                            stmt.setString(15, onlineFolder.getResourceId().toString());
                            stmt.executeUpdate();
                            newFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                        } catch (SQLException exc) {
                            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
                        } finally {
                            m_sqlManager.closeAll(conn, stmt, null);
                        }
                    } else {
                        throw e;
                    }
                }
                
                folderIdIndex.put(currentFolder.getResourceId(), newFolder.getResourceId());
                // copy properties
                Map props = new HashMap();
                
                try {
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFolder, currentFolder.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), newFolder, newFolder.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, copy properties for " + newFolder.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
                }
                
                // set the state of current folder in the offline project to unchanged
                currentFolder.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFolder);
                // C_STATE_CHANGED
            } else if (currentFolder.getState() == C_STATE_CHANGED) {
                changedResources.addElement(currentFolder.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
                }
                CmsFolder onlineFolder = null;
                try {
                    onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                } catch (CmsException exc) {
                    // if folder does not exist create it
                    if (exc.getType() == CmsException.C_NOT_FOUND) {
                        // get parentId for onlineFolder either from folderIdIndex or from the database
                        CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFolder.getParentId());
                        if (parentId == null) {
                            CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getRootName() + currentFolder.getParent());
                            parentId = currentOnlineParent.getResourceId();
                            folderIdIndex.put(currentFolder.getParentId(), parentId);
                        }
                        // create the new folder
                        onlineFolder = m_driverManager.getVfsDriver().createFolder(user, onlineProject, onlineProject, currentFolder, parentId, currentFolder.getResourceName());
                        onlineFolder.setState(C_STATE_UNCHANGED);
                        updateResourcestate(onlineFolder);
                    } else {
                        throw exc;
                    }
                } // end of catch
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                    // update the onlineFolder with data from offlineFolder
                    stmt.setInt(1, currentFolder.getType());
                    stmt.setInt(2, currentFolder.getFlags());
                    stmt.setString(3, currentFolder.getOwnerId().toString());
                    stmt.setString(4, currentFolder.getGroupId().toString());
                    stmt.setInt(5, onlineFolder.getProjectId());
                    stmt.setInt(6, currentFolder.getAccessFlags());
                    stmt.setInt(7, C_STATE_UNCHANGED);
                    stmt.setString(8, currentFolder.isLockedBy().toString());
                    stmt.setInt(9, currentFolder.getLauncherType());
                    stmt.setString(10, currentFolder.getLauncherClassname());
                    stmt.setTimestamp(11, new Timestamp(currentFolder.getDateLastModified()));
                    stmt.setString(12, currentFolder.getResourceLastModifiedBy().toString());
                    stmt.setInt(13, 0);
                    stmt.setString(14, onlineFolder.getFileId().toString());
                    stmt.setString(15, onlineFolder.getResourceId().toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
                folderIdIndex.put(currentFolder.getResourceId(), onlineFolder.getResourceId());
                // copy properties
                Map props = new HashMap();
                try {
                    deleteAllProperties(onlineProject.getId(), onlineFolder);
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFolder, currentFolder.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), onlineFolder, currentFolder.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
                }
                // set the state of current folder in the offline project to unchanged
                currentFolder.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFolder);
            } // end of else if
        } // end of for(...

        // now read all FILES in offlineProject
        
        offlineFiles = m_driverManager.getVfsDriver().readFiles(projectId, false, true);
        
        for (int i = 0; i < offlineFiles.size(); i++) {
            currentFile = ((CmsFile) offlineFiles.elementAt(i));
            report.print(report.key("report.publishing"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFile.getAbsolutePath());
            if (!currentFile.isLocked()) {
                // remove the temporary files for this resource
                removeTemporaryFile(currentFile);
            }
            // do not publish files that are locked in another project
            if (currentFile.isLocked()) {
                //in this case do nothing
            } else if (currentFile.getName().startsWith(C_TEMP_PREFIX)) {
                deleteAllProperties(projectId, currentFile);
                removeFile(projectId, currentFile.getResourceName());
                // C_STATE_DELETE
            } else if (currentFile.getState() == C_STATE_DELETED) {
                changedResources.addElement(currentFile.getResourceName());
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    try {
                        discAccess.removeResource(currentFile.getAbsolutePath(), exportKey);
                    } catch (Exception ex) {
                    }
                }
                CmsFile currentOnlineFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getResourceName());
                if (enableHistory) {
                    // read the properties for backup
                    Map props = m_driverManager.getVfsDriver().readProperties(projectId, currentFile, currentFile.getType());
                    // backup the offline resource
                    backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                try {
                    deleteAllProperties(onlineProject.getId(), currentOnlineFile);
                    deleteAllProperties(projectId, currentFile);
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                try {
                    m_driverManager.getVfsDriver().deleteResource(currentOnlineFile);
                    m_driverManager.getVfsDriver().deleteResource(currentFile);
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting resource for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                // C_STATE_CHANGED
            } else if (currentFile.getState() == C_STATE_CHANGED) {
                changedResources.addElement(currentFile.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    // Encoding project: Make sure files are written in the right encoding 
                    byte[] contents = currentFile.getContents();
                    String encoding = m_driverManager.getVfsDriver().readProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, projectId, currentFile, currentFile.getType());
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
                try {
                    onlineFile = m_driverManager.getVfsDriver().readFileHeader(onlineProject.getId(), currentFile.getResourceName(), false);
                } catch (CmsException exc) {
                    if (exc.getType() == CmsException.C_NOT_FOUND) {
                        // get parentId for onlineFolder either from folderIdIndex or from the database
                        CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFile.getParentId());
                        if (parentId == null) {
                            CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFile.getRootName() + currentFile.getParent());
                            parentId = currentOnlineParent.getResourceId();
                            folderIdIndex.put(currentFile.getParentId(), parentId);
                        }
                        // create a new File
                        currentFile.setState(C_STATE_UNCHANGED);
                        onlineFile = m_driverManager.getVfsDriver().createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId, currentFile.getResourceName());
                    }
                } // end of catch
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                    // update the onlineFile with data from offlineFile
                    stmt.setInt(1, currentFile.getType());
                    stmt.setInt(2, currentFile.getFlags());
                    stmt.setString(3, currentFile.getOwnerId().toString());
                    stmt.setString(4, currentFile.getGroupId().toString());
                    stmt.setInt(5, onlineFile.getProjectId());
                    stmt.setInt(6, currentFile.getAccessFlags());
                    stmt.setInt(7, C_STATE_UNCHANGED);
                    stmt.setString(8, currentFile.isLockedBy().toString());
                    stmt.setInt(9, currentFile.getLauncherType());
                    stmt.setString(10, currentFile.getLauncherClassname());
                    stmt.setTimestamp(11, new Timestamp(currentFile.getDateLastModified()));
                    stmt.setString(12, currentFile.getResourceLastModifiedBy().toString());
                    stmt.setInt(13, currentFile.getLength());
                    stmt.setString(14, onlineFile.getFileId().toString());
                    stmt.setString(15, onlineFile.getResourceId().toString());
                    stmt.executeUpdate();
                    stmt.close();
                    m_driverManager.getVfsDriver().writeFileContent(onlineFile.getFileId(), currentFile.getContents(), I_CmsConstants.C_PROJECT_ONLINE_ID, false);
                } catch (SQLException e) {
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
                // copy properties
                Map props = new HashMap();
                try {
                    deleteAllProperties(onlineProject.getId(), onlineFile);
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFile, currentFile.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), onlineFile, currentFile.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + onlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
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
                if (exportKey != null) {
                    // Encoding project: Make sure files are written in the right encoding 
                    byte[] contents = currentFile.getContents();
                    String encoding = m_driverManager.getVfsDriver().readProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, projectId, currentFile, currentFile.getType());
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
                CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFile.getParentId());
                if (parentId == null) {
                    CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFile.getRootName() + currentFile.getParent());
                    parentId = currentOnlineParent.getResourceId();
                    folderIdIndex.put(currentFile.getParentId(), parentId);
                }
                // create the new file
                try {
                    newFile = m_driverManager.getVfsDriver().createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId, currentFile.getResourceName());
                    newFile.setState(C_STATE_UNCHANGED);
                    updateResourcestate(newFile);
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        CmsFile onlineFile = null;
                        try {
                            onlineFile = m_driverManager.getVfsDriver().readFileHeader(onlineProject.getId(), currentFile.getResourceName(), false);
                        } catch (CmsException exc) {
                            throw exc;
                        } // end of catch
                        Connection conn = null;
                        PreparedStatement stmt = null;
                        try {
                            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                            // update the onlineFile with data from offlineFile
                            stmt.setInt(1, currentFile.getType());
                            stmt.setInt(2, currentFile.getFlags());
                            stmt.setString(3, currentFile.getOwnerId().toString());
                            stmt.setString(4, currentFile.getGroupId().toString());
                            stmt.setInt(5, onlineFile.getProjectId());
                            stmt.setInt(6, currentFile.getAccessFlags());
                            stmt.setInt(7, C_STATE_UNCHANGED);
                            stmt.setString(8, currentFile.isLockedBy().toString());
                            stmt.setInt(9, currentFile.getLauncherType());
                            stmt.setString(10, currentFile.getLauncherClassname());
                            stmt.setTimestamp(11, new Timestamp(currentFile.getDateLastModified()));
                            stmt.setString(12, currentFile.getResourceLastModifiedBy().toString());
                            stmt.setInt(13, currentFile.getLength());
                            stmt.setString(14, onlineFile.getFileId().toString());
                            stmt.setString(15, onlineFile.getResourceId().toString());
                            stmt.executeUpdate();
                            m_driverManager.getVfsDriver().writeFileContent(onlineFile.getFileId(), currentFile.getContents(), I_CmsConstants.C_PROJECT_ONLINE_ID, false);
                            newFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getResourceName());
                        } catch (SQLException exc) {
                            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
                        } finally {
                            m_sqlManager.closeAll(conn, stmt, null);
                        }
                    } else {
                        throw e;
                    }
                }
                // copy properties
                Map props = new HashMap();
                try {
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFile, currentFile.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), newFile, newFile.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, copy properties for " + newFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                // set the file state to unchanged
                currentFile.setState(C_STATE_UNCHANGED);
                updateResourcestate(currentFile);
            }
        } // end of for(...
        // now delete the "deleted" folders
        for (int i = deletedFolders.size() - 1; i > -1; i--) {
            currentFolder = ((CmsFolder) deletedFolders.elementAt(i));
            report.print(report.key("report.deleting"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFolder.getAbsolutePath());
            String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
            if (exportKey != null) {
                discAccess.removeResource(currentFolder.getAbsolutePath(), exportKey);
            }
            if (enableHistory) {
                Map props = m_driverManager.getVfsDriver().readProperties(projectId, currentFolder, currentFolder.getType());
                // backup the offline resource
                backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
            }
            CmsResource delOnlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
            try {
                deleteAllProperties(onlineProject.getId(), delOnlineFolder);
                deleteAllProperties(projectId, currentFolder);
            } catch (CmsException exc) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + currentFolder.toString() + " Message= " + exc.getMessage());
                }
            }
            removeFolderForPublish(onlineProject.getId(), currentFolder.getResourceName());
            removeFolderForPublish(projectId, currentFolder.getResourceName());
        } // end of for
        return changedResources;
    }
    
    /**
     * Get the next version id for the published backup resources
     *
     * @return int The new version id
     */
    public int getBackupVersionId(){
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int versionId = 1;
        int resVersionId = 1;
        try{
            // get the max version id
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER");
            res = stmt.executeQuery();
            if (res.next()){
                versionId = res.getInt(1)+1;
            }
            res.close();
            stmt.close();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER_RESOURCE");
            res = stmt.executeQuery();
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
            m_sqlManager.closeAll(conn, stmt, res);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        String ownerName = new String();
        String group = new String();
        String managerGroup = new String();
        try{
            CmsUser owner = m_driverManager.getUserDriver().readUser(project.getOwnerId());
            ownerName = owner.getName()+" "+owner.getFirstname()+" "+owner.getLastname();
        } catch (CmsException e){
            // the owner could not be read
            ownerName = "";
        }
        try{
            group = m_driverManager.getUserDriver().readGroup(project.getGroupId()).getName();
        } catch (CmsException e){
            // the group could not be read
            group = "";
        }
        try{
            managerGroup = m_driverManager.getUserDriver().readGroup(project.getManagerGroupId()).getName();
        } catch (CmsException e){
            // the group could not be read
            managerGroup = "";
        }
        Vector projectresources = readAllProjectResources(project.getId());
        // write backup project to the database
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE_BACKUP");
            // first write the project
            stmt.setInt(1, versionId);
            stmt.setInt(2, project.getId());
            stmt.setString(3, project.getName());
            stmt.setTimestamp(4, new Timestamp(publishDate));
            stmt.setString(5, currentUser.getId().toString());
            stmt.setString(6, currentUser.getName()+" "+currentUser.getFirstname()+" "+currentUser.getLastname());
            stmt.setString(7, project.getOwnerId().toString());
            stmt.setString(8, ownerName);
            stmt.setString(9, project.getGroupId().toString());
            stmt.setString(10, group);
            stmt.setString(11, project.getManagerGroupId().toString());
            stmt.setString(12, managerGroup);
            stmt.setString(13, project.getDescription());
            stmt.setTimestamp(14, new Timestamp(project.getCreateDate()));
            stmt.setInt(15, project.getType());
            stmt.setInt(16, project.getTaskId());
            stmt.executeUpdate();
            stmt.close();
            // now write the projectresources
            for(int i = 0; i < projectresources.size(); i++){
                stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE_BACKUP");
                stmt.setInt(1, versionId);
                stmt.setInt(2, project.getId());
                stmt.setString(3, (String)projectresources.get(i));
                stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
            Connection conn = null;
            PreparedStatement stmt = null;
            String ownerName = null;
            String groupName = new String();
            String lastModifiedName = null;
            try{
                CmsUser owner = m_driverManager.getUserDriver().readUser(resource.getOwnerId());
                ownerName = owner.getName()+" "+owner.getFirstname()+" "+owner.getLastname();
            } catch (CmsException e){
                // the user could not be read
                ownerName = "";
            }
            try{
                groupName = m_driverManager.getUserDriver().readGroup(resource.getGroupId()).getName();
            } catch (CmsException e){
                // the group could not be read
                groupName = "";
            }
            try{
                CmsUser lastModified = m_driverManager.getUserDriver().readUser(resource.getResourceLastModifiedBy());
                lastModifiedName = lastModified.getName()+" "+lastModified.getFirstname()+" "+lastModified.getLastname();
            } catch (CmsException e){
                // the user could not be read
                lastModifiedName = "";
            }
            
            CmsUUID resourceId = new CmsUUID();        
            CmsUUID fileId = CmsUUID.getNullUUID();
            
            // write backup resource to the database
            
            try {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_BACKUP");
                // if the resource is not a folder then backup the filecontent
                if (resource.getType() != C_TYPE_FOLDER){
                    // write new resource to the database
                    fileId = new CmsUUID();
                    m_driverManager.getVfsDriver().createFileContent(fileId, content, versionId, projectId, true);
                }
                stmt.setString(1, resourceId.toString());
                stmt.setString(2, CmsUUID.getNullUUID().toString());
                stmt.setString(3, resource.getResourceName());
                stmt.setInt(4, resource.getType());
                stmt.setInt(5, resource.getFlags());
                stmt.setString(6, resource.getOwnerId().toString());
                stmt.setString(7, ownerName);
                stmt.setString(8, resource.getGroupId().toString());
                stmt.setString(9, groupName);
                stmt.setInt(10, projectId);
                stmt.setString(11, fileId.toString());
                stmt.setInt(12, resource.getAccessFlags());
                stmt.setInt(13, resource.getState());
                stmt.setInt(14, resource.getLauncherType());
                stmt.setString(15, resource.getLauncherClassname());
                // set date created = publish date
                stmt.setTimestamp(16, new Timestamp(publishDate));
                stmt.setTimestamp(17, new Timestamp(resource.getDateLastModified()));
                stmt.setInt(18, content.length);
                stmt.setString(19, resource.getResourceLastModifiedBy().toString());
                stmt.setString(20, lastModifiedName);
                stmt.setInt(21, versionId);
                stmt.executeUpdate();
                stmt.close();
                // now write the properties
                // get all metadefs
                Iterator keys = properties.keySet().iterator();
                // one metainfo-name:
                String key;
                while(keys.hasNext()) {
                    key = (String) keys.next();
                    CmsPropertydefinition propdef = m_driverManager.getVfsDriver().readPropertydefinition(key, resource.getType());
                    String value = (String) properties.get(key);
                    if( propdef == null) {
                        // there is no propertydefinition for with the overgiven name for the resource
                        throw new CmsException("[" + this.getClass().getName() + "] " + key,
                        CmsException.C_NOT_FOUND);
                    } else {
                        // write the property into the db
                        stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_CREATE_BACKUP");
                        stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTIES_BACKUP")));
                        stmt.setInt(2, propdef.getId());
                        stmt.setString(3, resourceId.toString());
                        stmt.setString(4, m_sqlManager.validateNull(value));
                        stmt.setInt(5, versionId);
                        stmt.executeUpdate();
                        stmt.close();
                    }
                }
            } catch (SQLException e) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READALL");
            // select all resources from the database
            stmt.setInt(1, projectId);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
    public Vector readAllFileHeadersForHist(String resourceName) throws CmsException {

        CmsBackupResource file = null;
        ResultSet res = null;
        Vector allHeaders = new Vector();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_BACKUP");
            // read file header data from database
            stmt.setString(1, resourceName);
            res = stmt.executeQuery();
            
            // create new file headers
            while (res.next()) {
                file = m_driverManager.getVfsDriver().createCmsBackupResourceFromResultSet(res);
                allHeaders.addElement(file);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, "readAllFileHeadersForHist(String)", CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return allHeaders;
    }



/****************     methods for link management            ****************************/

    /**
     * deletes all entrys in the link table that belong to the pageId
     *
     * @param pageId The resourceId (offline) of the page whose links should be deleted
     */
    public void deleteLinkEntrys(CmsUUID pageId)throws CmsException{
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_DELETE_ENTRYS");
            // delete all project-resources.
            stmt.setString(1, pageId.toString());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "deleteLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        }finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * creates a link entry for each of the link targets in the linktable.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createLinkEntrys(CmsUUID pageId, Vector linkTargets)throws CmsException{
        //first delete old entrys in the database
        deleteLinkEntrys(pageId);
        if(linkTargets == null || linkTargets.size()==0){
            return;
        }
        // now write it
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_WRITE_ENTRY");
            stmt.setString(1, pageId.toString());
            for(int i=0; i < linkTargets.size(); i++){
                try{
                    stmt.setString(2, (String)linkTargets.elementAt(i));
                    stmt.executeUpdate();
                }catch(SQLException e){
                }
            }
        } catch (SQLException e){
             throw m_sqlManager.getCmsException(this, "createLinkEntrys(CmsUUID, Vector)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be read.
     */
    public Vector readLinkEntrys(CmsUUID pageId)throws CmsException{
        Vector result = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_READ_ENTRYS");
            stmt.setString(1, pageId.toString());
            res = stmt.executeQuery();
            while(res.next()){
                result.add(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
            }
            return result;
        }catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        }catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readLinkEntrys(CmsUUID)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * deletes all entrys in the online link table that belong to the pageId
     *
     * @param pageId The resourceId (online) of the page whose links should be deleted
     */
    public void deleteOnlineLinkEntrys(CmsUUID pageId)throws CmsException{
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // CHECK: use online or offline pool here? (was offline before (AZ, 19.05.2003)...)
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_DELETE_ENTRYS_ONLINE");
            // delete all project-resources.
            stmt.setString(1, pageId.toString());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "deleteOnlineLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        }finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * creates a link entry for each of the link targets in the online linktable.
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets)throws CmsException{
        //first delete old entrys in the database
        deleteLinkEntrys(pageId);
        if(linkTargets == null || linkTargets.size()==0){
            return;
        }
        // now write it
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_WRITE_ENTRY_ONLINE");
            stmt.setString(1, pageId.toString());
            for(int i=0; i < linkTargets.size(); i++){
                try{
                    stmt.setString(2, (String)linkTargets.elementAt(i));
                    stmt.executeUpdate();
                }catch(SQLException e){
                }
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "createOnlineLinkEntrys(CmsUUID, Vector)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (online) of the page whose liks should be read.
     */
    public Vector readOnlineLinkEntrys(CmsUUID pageId)throws CmsException{
        Vector result = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_READ_ENTRYS_ONLINE");
            stmt.setString(1, pageId.toString());
            res = stmt.executeQuery();
            while(res.next()){
                result.add(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
            }
            return result;
        }catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readOnlineLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        }catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readOnlineLinkEntrys(CmsUUID)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * searches for broken links in the online project.
     *
     * @return A Vector with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target.
     */
    public Vector getOnlineBrokenLinks() throws CmsException{
        Vector result = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_GET_ONLINE_BROKEN_LINKS");
            res = stmt.executeQuery();
            CmsUUID current = CmsUUID.getNullUUID();
            CmsPageLinks links = null;
            while(res.next()){
                CmsUUID next = new CmsUUID( res.getString(m_sqlManager.get("C_LM_PAGE_ID") ));
                if(!next.equals(current)){
                    if(links != null){
                        result.add(links);
                    }
                    links = new CmsPageLinks(next);
                    links.addLinkTarget(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
                    try{
                        links.setResourceName(((CmsFile)m_driverManager.getVfsDriver().readFileHeader(I_CmsConstants.C_PROJECT_ONLINE_ID, next)).getResourceName());
                    }catch(CmsException e){
                        links.setResourceName("id="+next+". Sorry, can't read resource. "+e.getMessage());
                    }
                    links.setOnline(true);
                }else{
                    links.addLinkTarget(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
                }
                current = next;
            }
            if(links != null){
                result.add(links);
            }
            return result;
        }catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "getOnlineBrokenLinks()", CmsException.C_SQL_ERROR, e);
        }catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "getOnlineBrokenLinks()", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
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
            CmsUUID resId = ((CmsResource)changed.elementAt(i)).getResourceId();
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
            CmsUUID resId = ((CmsResource)newRes.elementAt(i)).getResourceId();
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
                CmsUUID key = pl.getResourceId();
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
     private Vector getAllOnlineReferencesForLink(String link, Vector exceptions)throws CmsException{
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_GET_ONLINE_REFERENCES");
            stmt.setString(1, link);
            res = stmt.executeQuery();
            while(res.next()) {
                String resName=res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
                if(!exceptions.contains(resName)){
                    CmsPageLinks pl = new CmsPageLinks( new CmsUUID(res.getString(m_sqlManager.get("C_LM_PAGE_ID"))));
                    pl.setOnline(true);
                    pl.addLinkTarget(link);
                    pl.setResourceName(resName);
                    resources.add(pl);
                }
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "getAllOnlineReferencesForLink(String, Vector)", CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, "getAllOnlineReferencesForLink(String, Vector)", CmsException.C_UNKNOWN_EXCEPTION, ex);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_GET_ALL_ONLINE_RES_NAMES");
            res = stmt.executeQuery();
            // create new resource
            while(res.next()) {
                String resName=res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
                resources.add(resName);
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "getOnlineResourceNames()", CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, "getOnlineResourceNames()", CmsException.C_UNKNOWN_EXCEPTION, ex);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
                    CmsUUID id = readOnlineId(((CmsResource)deleted.elementAt(i)).getResourceName());
                    if(!id.isNullUUID()){
                        deleteOnlineLinkEntrys(id);
                    }
                }
            }
        }
        if(changed != null){
            for(int i=0; i<changed.size(); i++){
                // delete the old values and copy the new values from the project link table
                if(((CmsResource)changed.elementAt(i)).getType() == pageType){
                    CmsUUID id = readOnlineId(((CmsResource)changed.elementAt(i)).getResourceName());
                    if(!id.isNullUUID()){
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
                    CmsUUID id = readOnlineId(((CmsResource)newRes.elementAt(i)).getResourceName());
                    if(!id.isNullUUID()){
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
    private CmsUUID readOnlineId(String filename)throws CmsException {
        ResultSet res =null;
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID resourceId = CmsUUID.getNullUUID();
        
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_READ_ONLINE_ID");
            // read file data from database
            stmt.setString(1, filename);
            res = stmt.executeQuery();
            // read the id
            if(res.next()) {
                resourceId = new CmsUUID( res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")) );
                while(res.next()){
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readOnlineId(String)", CmsException.C_SQL_ERROR, e);
        } catch( Exception exc ) {
            throw m_sqlManager.getCmsException(this, "readOnlineId(String)", CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_READ");
            stmt.setString(1, request);
            res = stmt.executeQuery();

            // create new Cms exportlink object
            if(res.next()) {
                link = new CmsExportLink(res.getInt(m_sqlManager.get("C_EXPORT_ID")),
                                   res.getString(m_sqlManager.get("C_EXPORT_LINK")),
                                   SqlHelper.getTimestamp(res,m_sqlManager.get("C_EXPORT_DATE")).getTime(),
                                   null);

                // now the dependencies
                try{
                    res.close();
                    stmt.close();
                }catch(SQLException ex){
                }
                stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_DEPENDENCIES_READ");
                stmt.setInt(1,link.getId());
                res = stmt.executeQuery();
                while(res.next()){
                    link.addDependency(res.getString(m_sqlManager.get("C_EXPORT_DEPENDENCIES_RESOURCE")));
                }
            }
            return link;
         }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readExportLink(String)", CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readExportLink(String)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_READ");
            stmt.setString(1, request);
            res = stmt.executeQuery();

            // create new Cms exportlink object
            if(res.next()) {
                link = new CmsExportLink(res.getInt(m_sqlManager.get("C_EXPORT_ID")),
                                   res.getString(m_sqlManager.get("C_EXPORT_LINK")),
                                   SqlHelper.getTimestamp(res,m_sqlManager.get("C_EXPORT_DATE")).getTime(),
                                   null);

            }
            return link;
         }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readExportLinkHeader(String)", CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readExportLinkHeader(String)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_SET_PROCESSED");
            // delete the link table entry
            stmt.setBoolean(1, link.getProcessedState());
            stmt.setInt(2, linkId);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "writeExportLinkProcessedState(CmsExportLink)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_DELETE");
            // delete the link table entry
            stmt.setInt(1, deleteId);
            stmt.executeUpdate();
            // now the dependencies
            try{
                stmt.close();
            }catch(SQLException ex){
            }
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_DEPENDENCIES_DELETE");
            stmt.setInt(1, deleteId);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "deleteExportLink(CmsExportLink)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
            id = m_sqlManager.nextId(m_sqlManager.get("C_TABLE_EXPORT_LINKS"));
            link.setLinkId(id);
        }
        // now write it
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_WRITE");
            // write the link table entry
            stmt.setInt(1, id);
            stmt.setString(2, link.getLink());
            stmt.setTimestamp(3, new Timestamp(link.getLastExportDate()));
            stmt.setBoolean(4, link.getProcessedState());
            stmt.executeUpdate();
            // now the dependencies
            try{
                stmt.close();
            }catch(SQLException ex){
            }
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_DEPENDENCIES_WRITE");
            stmt.setInt(1, id);
            Vector deps = link.getDependencies();
            for(int i=0; i < deps.size(); i++){
                try{
                    stmt.setString(2, (String)deps.elementAt(i));
                    stmt.executeUpdate();
                }catch(SQLException e){
                    // this should be an Duplicate entry error and can be ignored
                    // todo: if it is something else we should coutionary delete the whole exportlink
                }
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "writeExportLink(CmsExportLink)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            Vector firstResult = new Vector();
            Vector secondResult = new Vector();
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_GET_ALL_DEPENDENCIES");
            res = stmt.executeQuery();
            while(res.next()) {
                firstResult.add(res.getString(m_sqlManager.get("C_EXPORT_DEPENDENCIES_RESOURCE")));
                secondResult.add(res.getString(m_sqlManager.get("C_EXPORT_LINK")));
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
            throw m_sqlManager.getCmsException(this, "getDependingExportlinks(Vector)", CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "getDependingExportLinks(Vector)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
     }

    /**
     * Reads all export links.
     *
     * @return a Vector(of Strings) with the links.
     */
     public Vector getAllExportLinks() throws CmsException{
        Vector retValue = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_GET_ALL_LINKS");
            res = stmt.executeQuery();
            while(res.next()) {
                retValue.add(res.getString(m_sqlManager.get("C_EXPORT_LINK")));
            }
            return retValue;
         }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "getAllExportLinks()", CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "getAllExportLinks()", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
     }





    /**
     * Reads a project.
     *
     * @param id The id of the project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(int id) throws CmsException {
        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ");

            stmt.setInt(1, id);
            res = stmt.executeQuery();

            if (res.next()) {                
                project =
                    new CmsProject(
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_FLAGS")),
                        SqlHelper.getTimestamp(res, m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw m_sqlManager.getCmsException(this,"project with ID " + id + " not found", CmsException.C_NOT_FOUND, null);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(int)/1 ", CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYTASK");

            stmt.setInt(1,task.getId());
            res = stmt.executeQuery();

            if(res.next())
                 project = new CmsProject(res,m_sqlManager);
          else
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] " + task,
                    CmsException.C_NOT_FOUND);
         }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readProject(CmsTask)", CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        String addStatement = filter + " ORDER BY RESOURCE_NAME";
        
        try {
            conn = m_sqlManager.getConnection();
            //stmt = conn.prepareStatement(m_sqlManager.get("C_RESOURCES_PROJECTVIEW") + addStatement);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get("C_RESOURCES_PROJECTVIEW") + addStatement);
            
            stmt.setInt(1,project);
            res = stmt.executeQuery();
            
            // create new resource
            while(res.next()) {
                file = m_driverManager.getVfsDriver().createCmsResourceFromResultSet(res,project);
                resources.addElement(file);
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readProjectView(int, int, String)", CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, "readProjectView(int,int, String)", CmsException.C_UNKNOWN_EXCEPTION, ex);
        } finally {
            m_sqlManager.closeAll(conn,stmt,res);
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

        PreparedStatement stmt = null;
        CmsBackupProject project = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READBYVERSION_BACKUP");

            stmt.setInt(1,versionId);
            res = stmt.executeQuery();

            if(res.next()) {
                Vector projectresources = readBackupProjectResources(versionId);
                project = new CmsBackupProject(res.getInt("VERSION_ID"),
                                         res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                                         res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                                         SqlHelper.getTimestamp(res,"PROJECT_PUBLISHDATE"),
                                         new CmsUUID( res.getString("PROJECT_PUBLISHED_BY") ),
                                         res.getString("PROJECT_PUBLISHED_BY_NAME"),
                                         res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                                         res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                                         new CmsUUID( res.getString(m_sqlManager.get("C_PROJECTS_USER_ID")) ),
                                         res.getString("USER_NAME"),
                                         new CmsUUID( res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID")) ),
                                         res.getString("GROUP_NAME"),
                                         new CmsUUID( res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID")) ),
                                         res.getString("MANAGERGROUP_NAME"),
                                         SqlHelper.getTimestamp(res,m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                                         res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE")),
                                         projectresources);
            } else {
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] version " + versionId,
                    CmsException.C_NOT_FOUND);
            }
         }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, "readBackupProjectId(int)", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readBackupProjectId(int)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        Connection conn = null;

        CmsTaskLog tasklog = null;
        Vector logs = new Vector();
        PreparedStatement stmt = null;
        String comment = null;
        java.sql.Timestamp starttime = null;
        int id = C_UNKNOWN_ID;
        int task = C_UNKNOWN_ID;
        CmsUUID user = CmsUUID.getNullUUID();
        int type = C_UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ_PPROJECTLOGS");
            stmt.setInt(1, projectid);
            res = stmt.executeQuery();
            while(res.next()) {
                comment = res.getString(m_sqlManager.get("C_LOG_COMMENT"));
                id = res.getInt(m_sqlManager.get("C_LOG_ID"));
                starttime = SqlHelper.getTimestamp(res,m_sqlManager.get("C_LOG_STARTTIME"));
                task = res.getInt(m_sqlManager.get("C_LOG_TASK"));
                user = new CmsUUID( res.getString(m_sqlManager.get("C_LOG_USER")) );
                type = res.getInt(m_sqlManager.get("C_LOG_TYPE"));

                tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
                logs.addElement(tasklog);
            }
        } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } catch( Exception exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BACKUP");
            // select resource from the database
            stmt.setInt(1, versionId);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        ResultSet res = null;
        Hashtable sessionData = new Hashtable();
        Hashtable data = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_READ");
            stmt.setString(1,sessionId);
            stmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT ));

            res = stmt.executeQuery();

            // create new Cms user object
            if(res.next()) {
                // read the additional infos.
                byte[] value = m_sqlManager.getBytes(res,"SESSION_DATA");
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        }
        catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        Connection conn = null;

        // create get the property data from the database
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_READ");
          stmt.setString(1,name);
          res = stmt.executeQuery();
          if(res.next()) {
                value = m_sqlManager.getBytes(res,m_sqlManager.get("C_SYSTEMPROPERTY_VALUE"));
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property=(Serializable)oin.readObject();
            }
        }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        }
        catch (ClassNotFoundException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_CLASSLOADER_ERROR, e);
        }finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
          }
        return property;
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
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsResource resource = m_driverManager.getVfsDriver().readFileHeader(projectId, filename, true);
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_DELETE");
            // delete the file header
            stmt.setString(1, filename);
            stmt.executeUpdate();
            stmt.close();
            // delete the file content
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILE_DELETE");
            stmt.setString(1, resource.getFileId().toString());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
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
        Vector files= m_driverManager.getVfsDriver().getFilesInFolder(projectId, folder);
        files=getUndeletedResources(files);
        if (files.size()==0) {
            // check if the folder has any folders in it
            Vector folders= m_driverManager.getVfsDriver().getSubFolders(projectId, folder);
            folders=getUndeletedResources(folders);
            if (folders.size()==0) {
                //this folder is empty, delete it
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(projectId);
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_ID_DELETE");
                    // delete the folder
                    stmt.setString(1,folder.getResourceId().toString());
                    stmt.executeUpdate();
                } catch (SQLException e){
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] "+folder.getAbsolutePath(),CmsException.C_NOT_EMPTY);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] "+folder.getAbsolutePath(),CmsException.C_NOT_EMPTY);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_DELETE");
            // delete the folder
            stmt.setString(1, foldername);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Removes the temporary files of the given resource
     *
     * @param file The file of which the remporary files should be deleted
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    protected void removeTemporaryFile(CmsFile file) throws CmsException{
        PreparedStatement stmt = null;
        PreparedStatement statementCont = null;
        PreparedStatement statementProp = null;
        Connection conn = null;
        ResultSet res = null;
              
        String tempFilename = file.getRootName() + file.getPath() + C_TEMP_PREFIX + file.getName()+"%";
        try{
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_GETTEMPFILES");
            // get all temporary files of the resource
            stmt.setString(1, tempFilename);
            res = stmt.executeQuery();
            while(res.next()){
                int fileId = res.getInt("FILE_ID");
                int resourceId = res.getInt("RESOURCE_ID");
                // delete the properties
                statementProp = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL");
                statementProp.setInt(1, resourceId);
                statementProp.executeQuery();
                statementProp.close();
                // delete the file content
                statementCont = m_sqlManager.getPreparedStatement(conn, "C_FILE_DELETE");
                statementCont.setInt(1, fileId);
                statementCont.executeUpdate();
                statementCont.close();
            }
            res.close();
            stmt.close();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETETEMPFILES");
            stmt.setString(1, tempFilename);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources          
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
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }


    /**
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
        PreparedStatement stmt = null;
        Connection conn = null;
        int retValue;

        try {
            value = serializeSession(data);
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_UPDATE");
            // write data to database
            stmt.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
            m_sqlManager.setBytes(stmt,2,value);
            stmt.setString(3,sessionId);
            retValue = stmt.executeUpdate();
        }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return retValue;
    }     
    
    /* Sorts a vector of files or folders alphabetically.
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
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UNLOCK");
            // create the statement
            stmt.setString(1,CmsUUID.getNullUUID().toString());
            stmt.setInt(2,project.getId());
            stmt.executeUpdate();
        } catch( Exception exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Updates the LOCKED_BY state of a Resource.
     * Creation date: (29.08.00 15:01:55)
     * @param res com.opencms.file.CmsResource
     * @throws com.opencms.core.CmsException The exception description.
     */
    public void updateLockstate(CmsResource res, int projectId) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_LOCK");
            stmt.setString(1, res.isLockedBy().toString());
            stmt.setInt(2, projectId);
            stmt.setString(3, res.getResourceId().toString());
            stmt.executeUpdate();
        } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Updates the state of a Resource.
     *
     * @param res com.opencms.file.CmsResource
     * @throws com.opencms.core.CmsException The exception description.
     */
    public void updateResourcestate(CmsResource res) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(res.getProjectId());
            stmt = m_sqlManager.getPreparedStatement(conn, res.getProjectId(), "C_RESOURCES_UPDATE_STATE");
            stmt.setInt(1, res.getState());
            stmt.setString(2, res.getResourceId().toString());
            stmt.executeUpdate();
        } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
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

         PreparedStatement stmt = null;
         Connection conn = null;

         try {
             conn = m_sqlManager.getConnection();
             stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_WRITE");

             stmt.setString(1,project.getOwnerId().toString());
             stmt.setString(2,project.getGroupId().toString());
             stmt.setString(3,project.getManagerGroupId().toString());
             stmt.setInt(4,project.getFlags());
             // no publishing data
             stmt.setInt(7,project.getId());
             stmt.executeUpdate();
         } catch( Exception exc ) {
             throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
         } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
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
        PreparedStatement stmt = null;
        CmsPropertydefinition returnValue = null;
        Connection conn = null;
        try {
            for (int i=0; i<3; i++){
                // write the propertydef in the offline db
                if (i == 0) {
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_UPDATE");
                }
                // write the propertydef in the online db
                else if (i == 1) {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_UPDATE_ONLINE");
                }
                // write the propertydef in the backup db
                else {
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_UPDATE_BACKUP");
                }
                stmt.setString(1, metadef.getName() );
                stmt.setInt(2, metadef.getId() );
                stmt.executeUpdate();
                stmt.close();
                conn.close();
            }
            // read the propertydefinition
            returnValue = m_driverManager.getVfsDriver().readPropertydefinition(metadef.getName(), metadef.getType());
         } catch( SQLException exc ) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
         } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();
            
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_UPDATE");
            m_sqlManager.setBytes(stmt,1,value);
            stmt.setString(2,name);
            stmt.executeUpdate();
         }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        }finally {
            m_sqlManager.closeAll(conn, stmt, null);
          }

          return readSystemProperty(name);
    }


    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The new project-id
     * @param resourcename The name of the resource to change
     */
    public void changeLockedInProject(int newProjectId, String resourcename) throws CmsException{
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_PROJECTID");
            // write data to database
            stmt.setInt(1, newProjectId);
            stmt.setString(2, resourcename);
            stmt.executeUpdate();
        }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        PreparedStatement stmt = null;
        Connection conn = null;
        int maxVersion = 0;
        try{
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_READ_MAXVERSION");
            // read the max. version_id from database by the publish_date
            stmt.setTimestamp(1, new Timestamp(maxdate));
            res = stmt.executeQuery();
            if(res.next()){
                maxVersion = res.getInt(1);
            }
            res.close();
            stmt.close();
            if(maxVersion > 0){
                String[] statements = { "C_BACKUP_DELETE_PROJECT_BYVERSION",
                        "C_BACKUP_DELETE_PROJECTRESOURCES_BYVERSION",
                        "C_BACKUP_DELETE_RESOURCES_BYVERSION",
                        "C_BACKUP_DELETE_FILES_BYVERSION",
                        "C_BACKUP_DELETE_PROPERTIES_BYVERSION",
                        "C_BACKUP_DELETE_MODULEMASTER_BYVERSION",
                        "C_BACKUP_DELETE_MODULEMEDIA_BYVERSION" };
                for (int i = 0; i < statements.length; i++) {
                    stmt = m_sqlManager.getPreparedStatement(conn, statements[i]);
                    stmt.setInt(1, maxVersion);
                    stmt.executeUpdate();
                    stmt.close();
                } 
            }
        } catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return maxVersion;
    }

    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {           
        return new com.opencms.db.generic.CmsSqlManager(dbPoolUrl);
    }

    /**
     * Deletes old sessions.
     */
    public void deleteSessions() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_DELETE");
            stmt.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT ));
            stmt.execute();
         }
        catch (Exception e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error while deleting old sessions: " + com.opencms.util.Utils.getStackTrace(e));
            }
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }    /**
     * This method creates a new session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public void createSession(String sessionId, Hashtable data)
        throws CmsException {
        byte[] value=null;

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            value = serializeSession(data);
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_CREATE");
            // write data to database
            stmt.setString(1,sessionId);
            stmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
            m_sqlManager.setBytes(stmt,3,value);
            stmt.executeUpdate();
        }
        catch (SQLException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        }
        catch (IOException e){
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }    }