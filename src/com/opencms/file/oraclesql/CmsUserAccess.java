/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oraclesql/Attic/CmsUserAccess.java,v $
 * Date   : $Date: 2003/05/15 12:39:34 $
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
package com.opencms.file.oraclesql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.file.genericSql.I_CmsUserAccess;
import com.opencms.flex.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

import oracle.jdbc.driver.OracleResultSet;
import source.org.apache.java.util.Configurations;

/**
 * Oracle/OCI implementation of the user access methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2003/05/15 12:39:34 $
 * 
 * @see com.opencms.file.genericSql.CmsUserAccess
 * @see com.opencms.file.genericSql.I_CmsUserAccess
 */
public class CmsUserAccess extends com.opencms.file.genericSql.CmsUserAccess implements I_CmsConstants, I_CmsLogChannels, I_CmsUserAccess {

    /**
     * Default constructor.
     * 
     * @param config the configurations objects (-> opencms.properties)
     * @param theResourceBroker the instance of the resource broker
     */
    public CmsUserAccess(Configurations config, I_CmsResourceBroker theResourceBroker) {
        super(config, theResourceBroker);
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
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser addImportUser(String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        //int id = m_SqlQueries.nextPkId("C_TABLE_USERS");
        CmsUUID id = new CmsUUID();
        byte[] value = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement nextStatement = null;
        Connection con = null;
        ResultSet res = null;

        try {
//            // serialize the hashtable
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            ObjectOutputStream oout = new ObjectOutputStream(bout);
//            oout.writeObject(additionalInfos);
//            oout.close();
//            value = bout.toByteArray();

            value = serializeAdditionalUserInfo( additionalInfos );

            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            con = m_SqlQueries.getConnection();
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_USERSFORINSERT"));

            statement.setString(1, id.toString());
            statement.setString(2, name);

            // crypt the password with MD5
            statement.setString(3, m_SqlQueries.validateNull(password));

            statement.setString(4, m_SqlQueries.validateNull(recoveryPassword));
            statement.setString(5, m_SqlQueries.validateNull(description));
            statement.setString(6, m_SqlQueries.validateNull(firstname));
            statement.setString(7, m_SqlQueries.validateNull(lastname));
            statement.setString(8, m_SqlQueries.validateNull(email));
            statement.setTimestamp(9, new Timestamp(lastlogin));
            statement.setTimestamp(10, new Timestamp(lastused));
            statement.setInt(11, flags);
            statement.setString(12, defaultGroup.getId().toString());
            statement.setString(13, m_SqlQueries.validateNull(address));
            statement.setString(14, m_SqlQueries.validateNull(section));
            statement.setInt(15, type);

            statement.executeUpdate();
            statement.close();

            // now update user_info of the new user
            statement2 = con.prepareStatement(m_SqlQueries.get("C_ORACLE_USERSFORUPDATE"));
            statement2.setString(1, id.toString());
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
            nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
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
                } catch (SQLException exc) {
                }
            }
            if (statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
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
        return readUser(id);
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
    public CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        //int id = m_SqlQueries.nextPkId("C_TABLE_USERS");
        CmsUUID id = new CmsUUID();
        byte[] value = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement nextStatement = null;
        Connection con = null;
        ResultSet res = null;

        try {
//            // serialize the hashtable
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            ObjectOutputStream oout = new ObjectOutputStream(bout);
//            oout.writeObject(additionalInfos);
//            oout.close();
//            value = bout.toByteArray();

            value = serializeAdditionalUserInfo( additionalInfos );

            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            con = m_SqlQueries.getConnection();
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_USERSFORINSERT"));

            statement.setString(1, id.toString());
            statement.setString(2, name);

            // crypt the password with MD5
            statement.setString(3, digest(password));

            statement.setString(4, digest(""));
            statement.setString(5, m_SqlQueries.validateNull(description));
            statement.setString(6, m_SqlQueries.validateNull(firstname));
            statement.setString(7, m_SqlQueries.validateNull(lastname));
            statement.setString(8, m_SqlQueries.validateNull(email));
            statement.setTimestamp(9, new Timestamp(lastlogin));
            statement.setTimestamp(10, new Timestamp(lastused));
            statement.setInt(11, flags);
            statement.setString(12, defaultGroup.getId().toString());
            statement.setString(13, m_SqlQueries.validateNull(address));
            statement.setString(14, m_SqlQueries.validateNull(section));
            statement.setInt(15, type);
            statement.executeUpdate();
            statement.close();

            // now update user_info of the new user
            statement2 = con.prepareStatement(m_SqlQueries.get("C_ORACLE_USERSFORUPDATE"));
            statement2.setString(1, id.toString());
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
            nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
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
                } catch (SQLException exc) {
                }
            }
            if (statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
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

        return readUser(id);
    }

    public com.opencms.file.genericSql.CmsQueries initQueries(Configurations config) {
        com.opencms.file.oraclesql.CmsQueries queries = new com.opencms.file.oraclesql.CmsQueries();
        queries.initJdbcPoolUrls(config);

        return queries;
    }

    /**
      * Writes a user to the database.
      *
      * @param user the user to write
      * @throws thorws CmsException if something goes wrong.
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
//            // serialize the hashtable
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            ObjectOutputStream oout = new ObjectOutputStream(bout);
//            oout.writeObject(user.getAdditionalInfo());
//            oout.close();
//            value = bout.toByteArray();

            value = serializeAdditionalUserInfo( user.getAdditionalInfo() );

            // write data to database
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_ORACLE_USERSWRITE"));
            statement.setString(1, m_SqlQueries.validateNull(user.getDescription()));
            statement.setString(2, m_SqlQueries.validateNull(user.getFirstname()));
            statement.setString(3, m_SqlQueries.validateNull(user.getLastname()));
            statement.setString(4, m_SqlQueries.validateNull(user.getEmail()));
            statement.setTimestamp(5, new Timestamp(user.getLastlogin()));
            statement.setTimestamp(6, new Timestamp(user.getLastUsed()));
            statement.setInt(7, user.getFlags());
            statement.setString(8, user.getDefaultGroupId().toString());
            statement.setString(9, m_SqlQueries.validateNull(user.getAddress()));
            statement.setString(10, m_SqlQueries.validateNull(user.getSection()));
            statement.setInt(11, user.getType());
            statement.setString(12, user.getId().toString());
            statement.executeUpdate();
            statement.close();
            // update user_info in this special way because of using blob
            statement2 = con.prepareStatement(m_SqlQueries.get("C_ORACLE_USERSFORUPDATE"));
            statement2.setString(1, user.getId().toString());
            con.setAutoCommit(false);
            res = statement2.executeQuery();
            try {
                while (res.next()) {
                    oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("USER_INFO");
                    // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                    // of the old content
                    //trimStatement = (OraclePreparedStatement) con.prepareStatement(cq.get("C_TRIMBLOB);
                    trimStatement = con.prepareStatement(m_SqlQueries.get("C_TRIMBLOB"));
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
                nextStatement = con.prepareStatement(m_SqlQueries.get("C_COMMIT"));
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
                    nextStatement = con.prepareStatement(m_SqlQueries.get("C_ROLLBACK"));
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

}
