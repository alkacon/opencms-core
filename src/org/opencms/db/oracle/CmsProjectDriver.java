/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/09/18 16:24:55 $
 * Version: $Revision: 1.9 $
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

import oracle.jdbc.driver.OracleResultSet;

import com.opencms.core.CmsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingResultSet;

/** 
 * Oracle/OCI implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.9 $ $Date: 2003/09/18 16:24:55 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {    

    /**
     * @see org.opencms.db.I_CmsProjectDriver#addSystemProperty(java.lang.String, java.io.Serializable)
     */
    public Serializable createSystemProperty(String name, Serializable object) throws CmsException {
        byte[] value;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        Connection conn = null;
        ResultSet res = null;
        try {
            int id = m_sqlManager.nextId(C_TABLE_SYSTEMPROPERTIES);
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();

            // create the object
            // first insert the new systemproperty with empty systemproperty_value, then update
            // the systemproperty_value. These two steps are necessary because of using Oracle BLOB
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_FORINSERT");
            stmt.setInt(1, id);
            stmt.setString(2, name);
            //statement.setBytes(3,value);
            stmt.executeUpdate();
            //statement.close();
            // now update the systemproperty_value
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_FORUPDATE");
            stmt2.setInt(1, id);
            conn.setAutoCommit(false);
            // res = stmt2.executeQuery();
            res = ((DelegatingResultSet)stmt2.executeQuery()).getInnermostDelegate();
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
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();
            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                }
                try {
                    //nextStmt = conn.prepareStatement(m_sqlManager.get("C_ROLLBACK"));
                    nextStmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.readQuery("C_ROLLBACK"));
                    nextStmt.execute();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
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
        PreparedStatement nextStmt = null;
        PreparedStatement trimStmt = null;
        ResultSet res = null;
        Connection conn = null;
        byte[] value = null;

        try {
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_SYSTEMPROPERTIES_NAMEFORUPDATE");
            stmt.setString(1, name);
            conn.setAutoCommit(false);
            // res = stmt.executeQuery();
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
                // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                // of the old content

                trimStmt = m_sqlManager.getPreparedStatement(conn, "C_TRIMBLOB");
                trimStmt.setBlob(1, blob);
                trimStmt.setInt(2, 0);
                trimStmt.execute();
                trimStmt.close();
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
            stmt.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();
            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException se) {
                    // noop
                }

                try {
                    nextStmt = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    nextStmt.execute();
                } catch (SQLException exc) {
                    // noop
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, trimStmt, res);
        }

        return readSystemProperty(name);
    }

}