/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/09/22 09:27:12 $
 * Version: $Revision: 1.10 $
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

import com.opencms.core.CmsException;

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

import org.apache.commons.dbcp.DelegatingResultSet;

/** 
 * Oracle/OCI implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.10 $ $Date: 2003/09/22 09:27:12 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {    

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
            if (!res.next()) 
                throw new CmsException("createSystemProperty name=" + name + " system property not found", CmsException.C_NOT_FOUND);

            // write serialized system property 
            Blob propertyValue = res.getBlob("SYSTEMPROPERTY_VALUE");
            ((oracle.sql.BLOB)propertyValue).trim(0);
            OutputStream output = ((oracle.sql.BLOB)propertyValue).getBinaryOutputStream();
            output.write(value);
            output.close();
                         
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
                }                
            } 
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                }
            } 
            if (stmt != null) {
                try {
                    rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    rollback.execute();
                    rollback.close();
                } catch (SQLException se) {
                }
                try {
                    stmt.close();
                } catch (SQLException exc) {
                }                
            }                
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException se) {
                }                   
            }
        }

        return readSystemProperty(name);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
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
            if (!res.next()) 
                throw new CmsException("writeSystemProperty name=" + name + " system property not found", CmsException.C_NOT_FOUND);

            // write serialized system property 
            Blob propertyValue = res.getBlob("SYSTEMPROPERTY_VALUE");
            ((oracle.sql.BLOB)propertyValue).trim(0);
            OutputStream output = ((oracle.sql.BLOB)propertyValue).getBinaryOutputStream();
            output.write(value);
            output.close();
                         
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
                }                
            } 
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                }
            } 
            if (stmt != null) {
                try {
                    rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    rollback.execute();
                    rollback.close();
                } catch (SQLException se) {
                }
                try {
                    stmt.close();
                } catch (SQLException exc) {
                }                
            }                
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException se) {
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