/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsVfsDriver.java,v $
 * Date   : $Date: 2003/07/03 13:29:45 $
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

package org.opencms.db.mysql;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.util.CmsUUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;

/**
 * MySQL implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $ $Date: 2003/07/03 13:29:45 $
 * @since 5.1
 */
public class CmsVfsDriver extends org.opencms.db.generic.CmsVfsDriver {        

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(com.opencms.file.CmsUser, com.opencms.file.CmsProject, java.lang.String, int, com.opencms.flex.util.CmsUUID, byte[], com.opencms.file.I_CmsResourceType)
     */
    public CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsUUID parentId, byte[] contents, I_CmsResourceType resourceType) throws CmsException {
        if (filename.length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource name '" + filename + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }

        int state = I_CmsConstants.C_STATE_NEW;
        // Test if the file is already there and marked as deleted.
        // If so, delete it
        try {
            readFileHeader(project.getId(), parentId, filename, false);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            // if the file is maked as deleted remove it!
            if (e.getType() == CmsException.C_RESOURCE_DELETED) {
                removeFile(project, parentId, filename);
                state = I_CmsConstants.C_STATE_CHANGED;
            }
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        CmsUUID resourceId = new CmsUUID();
        CmsUUID fileId = new CmsUUID();
        CmsUUID structureId = new CmsUUID();

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(project);

            // write the content
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_FILES_WRITE");
            stmt.setString(1, fileId.toString());
            stmt.setBytes(2, contents);
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            // write the resource
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, resourceType.getResourceType());
            stmt.setInt(3, flags);
            stmt.setString(4, user.getId().toString());
            stmt.setString(5, user.getDefaultGroupId().toString());
            stmt.setString(6, fileId.toString());
            stmt.setInt(7, I_CmsConstants.C_ACCESS_DEFAULT_FLAGS);
            stmt.setInt(8, resourceType.getLauncherType());
            stmt.setString(9, resourceType.getLauncherClass());
            stmt.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(12, contents.length);
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, structureId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, resourceId.toString());
            stmt.setInt(4, project.getId());
            stmt.setString(5, filename);
            stmt.setInt(6, 0);
            stmt.setInt(7, state);
            stmt.setString(8, CmsUUID.getNullUUID().toString());
            stmt.setString(9, user.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readFile(project.getId(), structureId, false);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new org.opencms.db.mysql.CmsSqlManager(dbPoolUrl);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFile(int, java.lang.String, boolean)
     */
    public CmsFile readFile(int projectId, CmsUUID structureId, boolean includeDeleted) throws CmsException {
        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_READ");
            stmt.setString(1, structureId.toString());
            //stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            
            if (res.next()) {
                file = createCmsFileFromResultSet(res, projectId);

                // check if this resource is marked as deleted
                if (file.getState() == I_CmsConstants.C_STATE_DELETED && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFile] " + file.getResourceName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFile] " + structureId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readProperties(int, com.opencms.file.CmsResource, int)
     */
    public HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException {
        HashMap original = super.readProperties(projectId, resource, resourceType);
        if (CmsSqlManager.singleByteEncoding())
            return original;
        HashMap result = new HashMap(original.size());
        Iterator keys = original.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            result.put(key, CmsSqlManager.unescape((String) original.get(key)));
        }
        original.clear();
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readProperty(java.lang.String, int, com.opencms.file.CmsResource, int)
     */
    public String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {
        return CmsSqlManager.unescape(super.readProperty(meta, projectId, resource, resourceType));
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeProperty(java.lang.String, int, java.lang.String, com.opencms.file.CmsResource, int, boolean)
     */
    public void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {
        super.writeProperty(meta, projectId, CmsSqlManager.escape(value), resource, resourceType, addDefinition);
    }

}
