/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsVfsAccess.java,v $
 * Date   : $Date: 2003/05/20 10:45:32 $
 * Version: $Revision: 1.5 $
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
package com.opencms.file.mySql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.Encoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;

import source.org.apache.java.util.Configurations;

/**
 * MySQL implementation of the VFS access methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.5 $ $Date: 2003/05/20 10:45:32 $
 */
public class CmsVfsAccess extends com.opencms.file.genericSql.CmsVfsAccess implements I_CmsConstants, I_CmsLogChannels {

    private static Boolean m_escapeStrings = null;

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
     * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
     */
    protected void clearFilesTable() throws CmsException {
        PreparedStatement stmtSearch = null;
        PreparedStatement stmtDestroy = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_SqlQueries.getConnection();
            stmtSearch = m_SqlQueries.getPreparedStatement(conn, "C_RESOURCES_GET_LOST_ID");
            res = stmtSearch.executeQuery();
            // delete the lost fileId's
            stmtDestroy = m_SqlQueries.getPreparedStatement(conn, "C_FILE_DELETE");
            while (res.next()) {
                stmtDestroy.setInt(1, res.getInt(m_SqlQueries.get("C_FILE_ID")));
                stmtDestroy.executeUpdate();
                stmtDestroy.clearParameters();
            }
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_SqlQueries.closeAll(null, stmtDestroy, null);
            m_SqlQueries.closeAll(conn, stmtSearch, res);           
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
    public CmsFile createFile(CmsUser user, CmsProject project, CmsProject onlineProject, String filename, int flags, CmsUUID parentId, byte[] contents, I_CmsResourceType resourceType) throws CmsException {
        if (filename.length() > C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Resourcename too long(>" + C_MAX_LENGTH_RESOURCE_NAME + ") ", CmsException.C_BAD_NAME);
        }

        int state = C_STATE_NEW;
        // Test if the file is already there and marked as deleted.
        // If so, delete it
        try {
            readFileHeader(project.getId(), filename, false);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            // if the file is maked as deleted remove it!
            if (e.getType() == CmsException.C_RESOURCE_DELETED) {
                removeFile(project.getId(), filename);
                state = C_STATE_CHANGED;
            }
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }
      
        CmsUUID resourceId = new CmsUUID();
        CmsUUID fileId = new CmsUUID();

        PreparedStatement stmt = null;
        PreparedStatement stmtFileWrite = null;
        Connection conn = null;

        try {
            conn = m_SqlQueries.getConnection(project);
            stmt = m_SqlQueries.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
            // write new resource to the database
            stmt.setString(1, resourceId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, filename);
            stmt.setInt(4, resourceType.getResourceType());
            stmt.setInt(5, flags);
            stmt.setString(6, user.getId().toString());
            stmt.setString(7, user.getDefaultGroupId().toString());
            stmt.setInt(8, project.getId());
            stmt.setString(9, fileId.toString());
            stmt.setInt(10, C_ACCESS_DEFAULT_FLAGS);
            stmt.setInt(11, state);
            stmt.setString(12, CmsUUID.getNullUUID().toString());
            stmt.setInt(13, resourceType.getLauncherType());
            stmt.setString(14, resourceType.getLauncherClass());
            stmt.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(16, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(17, contents.length);
            stmt.setString(18, user.getId().toString());
            stmt.executeUpdate();
            
            stmtFileWrite = m_SqlQueries.getPreparedStatement(conn, project, "C_FILES_WRITE");
            stmtFileWrite.setString(1, fileId.toString());
            stmtFileWrite.setBytes(2, contents);
            stmtFileWrite.executeUpdate();
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, null);
            m_SqlQueries.closeAll(null, stmtFileWrite, null);
        }
        return readFile(project.getId(), onlineProject.getId(), filename);
    }

    /**
     * Escapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * TODO: this method is both in the DbcAccess and VfsAccess!
     * 
     * @param value String to be escaped
     * @return the escaped String
     */
    private String escape(String value) {
        // no need to encode if OpenCms is not running in Unicode mode
        if (singleByteEncoding())
            return value;
        return Encoder.encode(value);
    }

    public com.opencms.file.genericSql.CmsQueries initQueries(Configurations config) {
        com.opencms.file.mySql.CmsQueries queries = new com.opencms.file.mySql.CmsQueries();
        queries.initJdbcPoolUrls(config);

        return queries;
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
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_SqlQueries.getConnection(projectId);
            stmt = m_SqlQueries.getPreparedStatement(conn, projectId, "C_FILES_READ");
            
            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            
            if (res.next()) {                
                file = createCmsFileFromResultSet(res,projectId,filename);
                
                // check if this resource is marked as deleted
                if (file.getState() == C_STATE_DELETED) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, res);
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
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_SqlQueries.getConnection(projectId);
            stmt = m_SqlQueries.getPreparedStatement(conn, projectId, "C_FILES_READ");
            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            if (res.next()) {                
                file = createCmsFileFromResultSet(res,projectId,filename);
                
                // check if this resource is marked as deleted
                if (file.getState() == C_STATE_DELETED && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, res);
        }
        return file;
    }

    /**
     * Added unescaping of property values as MySQL doesn't support Unicode strings
     * 
     * @see com.opencms.file.genericSql.CmsDbAccess#readProperties(int, CmsResource, int)
     */
    public HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException {
        HashMap original = super.readProperties(projectId, resource, resourceType);
        if (singleByteEncoding())
            return original;
        HashMap result = new HashMap(original.size());
        Iterator keys = original.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            result.put(key, unescape((String) original.get(key)));
        }
        original.clear();
        return result;
    }

    /**
     * Added unescaping of property values as MySQL doesn't support Unicode strings
     * 
     * @see com.opencms.file.genericSql.CmsDbAccess#readProperty(String, int, CmsResource, int)
     */
    public String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {
        return unescape(super.readProperty(meta, projectId, resource, resourceType));
    }

    /**
     * Returns <code>true</code> if Strings must be escaped before they are stored in the DB, 
     * this is required because MySQL does not support multi byte unicode strings.<p>
     * TODO: this method is both in the DbcAccess and VfsAccess!
     * 
     * @return boolean <code>true</code> if Strings must be escaped before they are stored in the DB
     */
    private boolean singleByteEncoding() {
        if (m_escapeStrings == null) {
            String encoding = A_OpenCms.getDefaultEncoding();
            m_escapeStrings = new Boolean("ISO-8859-1".equalsIgnoreCase(encoding) || "ISO-8859-15".equalsIgnoreCase(encoding) || "US-ASCII".equalsIgnoreCase(encoding) || "Cp1252".equalsIgnoreCase(encoding));
        }
        return m_escapeStrings.booleanValue();
    }

    /**
     * Unescapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * TODO: this method is both in the DbcAccess and VfsAccess!
     * 
     * @param value String to be unescaped
     * @return the unescaped String
     */
    private String unescape(String value) {
        // no need to encode if OpenCms is not running in Unicode mode
        if (singleByteEncoding())
            return value;
        return Encoder.decode(value);
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
    public void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {
        super.writeProperty(meta, projectId, escape(value), resource, resourceType, addDefinition);
    }

}
