/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/11/14 10:09:15 $
 * Version: $Revision: 1.17 $
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
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.dbcp.DelegatingResultSet;

/** 
 * Oracle/OCI implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.17 $ $Date: 2003/11/14 10:09:15 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {    

    /*
     * Indicates that server side copying should be used
     */
    private boolean m_enableServerCopy = false;
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#addSystemProperty(java.lang.String, java.io.Serializable)
     */
    public Serializable createSystemProperty(String name, Serializable object) throws CmsException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            
            // serialize the object
            byte[] value = internalSerializeObject(object);

            int id = m_sqlManager.nextId(C_TABLE_SYSTEMPROPERTIES);
                        
            conn = m_sqlManager.getConnection();
            
            // create the object
            // first insert the new systemproperty with empty systemproperty_value, then update
            // the systemproperty_value. These two steps are necessary because of using Oracle BLOB
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_ADD");
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
            
            conn.setAutoCommit(false);
            
            // now update the systemproperty_value
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_UPDATE");
            stmt.setInt(1, id);
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsException("createSystemProperty name=" + name + " system property not found", CmsException.C_NOT_FOUND);
            }

            // write serialized system property 
            Blob propertyValue = res.getBlob("SYSTEMPROPERTY_VALUE");
            ((oracle.sql.BLOB)propertyValue).trim(0);
            OutputStream output = ((oracle.sql.BLOB)propertyValue).getBinaryOutputStream();
            output.write(value);
            output.close();
            value = null;
                         
            commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            commit.execute();
            commit.close();
            commit = null;
               
            stmt.close();
            stmt = null;
            res = null;
                          
            conn.setAutoCommit(true);            

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "createSystemProperty name=" + name, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "createSystemProperty name=" + name, CmsException.C_SERIALIZATION, e, false);
        } finally {
            
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException exc) {
                    // ignore
                }                
            } 
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                    // ignore
                }
            } 
            if (stmt != null) {
                try {
                    rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    rollback.execute();
                    rollback.close();
                } catch (SQLException se) {
                    // ignore
                }
                try {
                    stmt.close();
                } catch (SQLException exc) {
                    // ignore
                }                
            }                
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException se) {
                    // ignore
                }                   
            }
        }

        return readSystemProperty(name);
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(source.org.apache.java.util.Configurations, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {

        m_enableServerCopy = "true".equals(configuration.getString("db.oracle.servercopy"));
        super.init(configuration, successiveDrivers, driverManager);
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(com.opencms.file.CmsRequestContext, com.opencms.file.CmsProject, com.opencms.file.CmsResource, java.util.Set)
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
            if (!offlineFileHeader.getFileId().isNullUUID() && !publishedContentIds.contains(offlineFileHeader.getFileId())) {

                // create the file online, but without content              
                // newFile = (CmsFile) offlineFile.clone();
                newFile = new CmsFile(
                    offlineFileHeader.getStructureId(),
                    offlineFileHeader.getResourceId(),
                    offlineFileHeader.getParentStructureId(),
                    offlineFileHeader.getFileId(),
                    offlineFileHeader.getName(),
                    offlineFileHeader.getType(),
                    offlineFileHeader.getFlags(),
                    offlineFileHeader.getProjectLastModified(),
                    I_CmsConstants.C_STATE_UNCHANGED,
                    offlineFileHeader.getLoaderId(),
                    offlineFileHeader.getDateCreated(),
                    offlineFileHeader.getUserCreated(),
                    offlineFileHeader.getDateLastModified(),
                    offlineFileHeader.getUserLastModified(),
                    offlineFileHeader.getLength(),            
                    offlineFileHeader.getLinkCount(),
                    new byte[0]
                );
                newFile.setFullResourceName(offlineFileHeader.getRootPath());                
                m_driverManager.getVfsDriver().createFile(onlineProject, newFile, offlineFileHeader.getUserCreated(), newFile.getParentStructureId(), newFile.getName());

                conn = m_sqlManager.getConnection();
            
                // read the content blob
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_READCONTENT");
                stmt.setString(1, offlineFileHeader.getFileId().toString());
                res = stmt.executeQuery();
                if (res.next()) {
                    Blob content = res.getBlob(1);
                    // publish the content
                    stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_FILES_PUBLISHCONTENT");
                    stmt2.setBlob(1, content);
                    stmt2.setString(2, offlineFileHeader.getFileId().toString());
                    stmt2.executeUpdate();
                    stmt2.close();
                    stmt2 = null;                    
                }
                
                stmt.close();
                stmt = null;
                                
                // read the file offline
                // offlineFile = m_driverManager.getVfsDriver().readFile(offlineProject.getId(), false, offlineFileHeader.getStructureId());
                // offlineFile.setFullResourceName(offlineFileHeader.getRootPath());


                // update the online/offline structure and resource records of the file
                // TODO: functionality to write content in writeResource is obsolete ?
                // m_driverManager.getVfsDriver().writeResource(onlineProject, newFile, offlineFile, false);

                // add the content ID to the content IDs that got already published
                publishedContentIds.add(offlineFileHeader.getFileId());
                
            } else {
                // create the sibling online
                m_driverManager.getVfsDriver().createSibling(onlineProject, offlineFileHeader, offlineFileHeader.getUserCreated(), offlineFileHeader.getParentStructureId(), offlineFileHeader.getName());

                newFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), false, offlineFileHeader.getStructureId());
                newFile.setFullResourceName(offlineFileHeader.getRootPath());                
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error creating file content " + offlineFileHeader.toString(), e);
            }

            throw e;
        }
        
        return newFile;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeSystemProperty(java.lang.String, java.io.Serializable)
     */
    public Serializable writeSystemProperty(String name, Serializable object) throws CmsException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            
            // serialize the object
            byte[] value = internalSerializeObject(object);
            
            conn = m_sqlManager.getConnection();
            conn.setAutoCommit(false);
            
            // update system property
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_UPDATE_BYNAME");
            stmt.setString(1, name);
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsException("writeSystemProperty name=" + name + " system property not found", CmsException.C_NOT_FOUND);
            }

            // write serialized system property 
            Blob propertyValue = res.getBlob("SYSTEMPROPERTY_VALUE");
            ((oracle.sql.BLOB)propertyValue).trim(0);
            OutputStream output = ((oracle.sql.BLOB)propertyValue).getBinaryOutputStream();
            output.write(value);
            output.close();
            value = null;
                         
            commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            commit.execute();
            commit.close();
            commit = null;
               
            stmt.close();
            stmt = null;
            res = null;
                          
            conn.setAutoCommit(true);            

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "writeSystemProperty name=" + name, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "writeSystemProperty name=" + name, CmsException.C_SERIALIZATION, e, false);
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException exc) {
                    // ignore
                }                
            } 
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                    // ignore
                }
            } 
            if (stmt != null) {
                try {
                    rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    rollback.execute();
                    rollback.close();
                } catch (SQLException se) {
                    // ignore
                }
                try {
                    stmt.close();
                } catch (SQLException exc) {
                    // ingnore
                }                
            }                
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException se) {
                    // ignore
                }                   
            }
        }

        return readSystemProperty(name);
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