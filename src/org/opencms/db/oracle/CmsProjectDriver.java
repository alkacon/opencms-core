/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsProjectDriver.java,v $
 * Date   : $Date: 2004/07/06 09:33:03 $
 * Version: $Revision: 1.27 $
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;

/** 
 * Oracle/OCI implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.27 $ $Date: 2004/07/06 09:33:03 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {    

    /*
     * Indicates that server side copying should be used
     */
    private boolean m_enableServerCopy;
    
    /**
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {

        m_enableServerCopy = "true".equals(configuration.getString("db.oracle.servercopy"));
        super.init(configuration, successiveDrivers, driverManager);
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set)
     */
    public CmsFile publishFileContent(CmsProject offlineProject, CmsProject onlineProject, CmsResource offlineFileHeader, Set publishedContentIds) throws Exception {
        CmsFile newFile = null;
        PreparedStatement stmt = null, stmt2 = null;
        Connection conn = null;
        ResultSet res = null;

        if (!m_enableServerCopy || onlineProject.getId() < 0) {
            return super.publishFileContent(offlineProject, onlineProject, offlineFileHeader, publishedContentIds);
        }
                            
        try {
            // binary content gets only published once while a project is published
            if (!offlineFileHeader.getContentId().isNullUUID() && !publishedContentIds.contains(offlineFileHeader.getContentId())) {

                // create the file online, but without content              
                // newFile = (CmsFile) offlineFile.clone();
                newFile = new CmsFile(
                    offlineFileHeader.getStructureId(),
                    offlineFileHeader.getResourceId(),
                    offlineFileHeader.getParentStructureId(),
                    offlineFileHeader.getContentId(),
                    offlineFileHeader.getName(),
                    offlineFileHeader.getTypeId(),
                    offlineFileHeader.getFlags(),
                    offlineFileHeader.getProjectLastModified(),
                    I_CmsConstants.C_STATE_UNCHANGED,
                    offlineFileHeader.getLoaderId(),
                    offlineFileHeader.getDateCreated(),
                    offlineFileHeader.getUserCreated(),
                    offlineFileHeader.getDateLastModified(),
                    offlineFileHeader.getUserLastModified(),
                    offlineFileHeader.getDateReleased(),            
                    offlineFileHeader.getDateExpired(),
                    offlineFileHeader.getSiblingCount(), 
                    offlineFileHeader.getLength(), 
                    new byte[0]
                );
                newFile.setRootPath(offlineFileHeader.getRootPath());                

                m_driverManager.getVfsDriver().createResource(
                    onlineProject, 
                    newFile, 
                    newFile.getContents());

                conn = m_sqlManager.getConnection();
            
                // read the content blob
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_READCONTENT");
                stmt.setString(1, offlineFileHeader.getContentId().toString());
                res = stmt.executeQuery();
                if (res.next()) {
                    Blob content = res.getBlob(1);
                    // publish the content
                    stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_PUBLISHCONTENT");
                    stmt2.setBlob(1, content);
                    stmt2.setString(2, offlineFileHeader.getContentId().toString());
                    stmt2.executeUpdate();
                    stmt2.close();
                    stmt2 = null;                    
                }
                
//                stmt.close();
//                stmt = null;
                                
                // read the file offline
                // offlineFile = m_driverManager.getVfsDriver().readFile(offlineProject.getId(), false, offlineFileHeader.getStructureId());
                // offlineFile.setFullResourceName(offlineFileHeader.getRootPath());


                // update the online/offline structure and resource records of the file
                // TODO: functionality to write content in writeResource is obsolete ?
                // m_driverManager.getVfsDriver().writeResource(onlineProject, newFile, offlineFile, false);

                // add the content ID to the content IDs that got already published
                publishedContentIds.add(offlineFileHeader.getContentId());
                
            } else {
                // create the sibling online
                m_driverManager.getVfsDriver().createSibling(
                    onlineProject, 
                    offlineFileHeader, 
                    offlineFileHeader.getName());

                newFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), false, offlineFileHeader.getStructureId());
                newFile.setRootPath(offlineFileHeader.getRootPath());                
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error creating file content " + offlineFileHeader.toString(), e);
            }

            throw e;
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
            m_sqlManager.closeAll(null, stmt2, null);
        }
        
        return newFile;
    }

    /**
     * Serialize object data to write it as byte array in the database.<p>
     * 
     * @param object the object
     * @return byte[] the byte array with object data
     * @throws IOException if something goes wrong
     */
    protected final byte[] internalSerializeObject (Serializable object) throws IOException {
        // this method is final to allow the java compiler to inline this code!

        // serialize the object
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(object);
        oout.close();

        return bout.toByteArray();
    }
}