/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/Attic/CmsIdGenerator.java,v $
 * Date   : $Date: 2003/05/22 16:07:45 $
 * Version: $Revision: 1.2 $
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
 
package com.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * This class is used to create OpenCms system-IDs.
 */
public class CmsIdGenerator {

    /**
     * Hashtable with next available id's
     */
    private static Hashtable c_currentId = new Hashtable();

    /**
     * Hashtable with border id's
     */
    private static Hashtable c_borderId = new Hashtable();

    private static final int C_GROW_VALUE = 10;

    /**
     * The name of the default pool
     */
    private static String c_defaultPool = "";

    /**
     *
     */
    public static void setDefaultPool(String poolName){
        c_defaultPool = poolName;
    }
    /**
     * Creates a new id for the given table.
     * @param pooName - the name of the pool.
     * @param tableName - the name of the table to create the id.
     * @return the next id for this resource.
     */
    public static synchronized int nextId(String tableName) throws CmsException{
        return nextId(c_defaultPool, tableName);
    }
    /**
     * Creates a new id for the given table.
     * @param pooName - the name of the pool.
     * @param tableName - the name of the table to create the id.
     * @return the next id for this resource.
     */
    public static synchronized int nextId(String poolName, String tableName)
        throws CmsException {
        String key = poolName + "." + tableName;
        if( c_currentId.containsKey(key) ) {
            int id = ((Integer)c_currentId.get(key)).intValue();
            int borderId = ((Integer)c_borderId.get(key)).intValue();
            if(id < borderId) {
                            //EF: c_currentId contains the next available key
                            //like the systemid table
                            //id++;
                            int nextId = id + 1;
                            c_currentId.put(key, new Integer(nextId));
                            return id;
            }
        }

        // there is no id in the cache - generate them
        generateNextId(poolName, tableName, key);
        return nextId(poolName, tableName);
    }

    /**
     * Creates a new id for the given table.
     * @param pooName - the name of the pool.
     * @param tableName - the name of the table to create the id.
     * @param key - the key to store the generated values.
     * @return the next id for this resource.
     */
    private static void generateNextId(String poolName, String tableName, String key)
        throws CmsException {
        Connection con = null;
        int id;
        int borderId;
        try {
            if (!poolName.startsWith(CmsDbPool.C_DBCP_JDBC_URL_PREFIX)) {
                poolName = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolName;
            }
            
            con = DriverManager.getConnection(poolName);
            // repeat this operation, until the nextId is valid and can be saved
            // (this is for clustering of several OpenCms)
            do {
                id = readId(con, tableName);
                if( id == I_CmsConstants.C_UNKNOWN_ID ) {
                    // there was no entry - set it to 0
                                        // EF: set id to 1 because the table contains
                                        // the next available id
                    id = 1;
                    createId(con, tableName, id);
                }
                borderId = id + C_GROW_VALUE;
                // save the next id for future requests
            } while(!writeId(con, tableName, id, borderId));
            // store the generated values in the cache
            c_currentId.put(key, new Integer(id));
            c_borderId.put(key, new Integer(borderId));
        } catch (SQLException e){
            throw new CmsException("["+CmsIdGenerator.class.getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
             // close all db-resources
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
     * Static method to read the id for the given table.
     * @param con - The connection to read from.
     * @param table - The name of the table to read the id for.
     * @return the id, or C_UNKNOWN_ID if there is no entry for the table-name
     * @throws CmsException - if an sql-error occures.
     */
    private static int readId(Connection con, String table)
        throws CmsException {
        PreparedStatement statement = null;
        ResultSet res = null;
         try    {
            statement = con.prepareStatement("select ID from CMS_SYSTEMID where TABLE_KEY = ?");
            statement.setString(1,table);
            res = statement.executeQuery();
            if(res.next()) {
                return res.getInt(1);
            } else {
                return I_CmsConstants.C_UNKNOWN_ID;
            }
        } catch (SQLException e){
            throw new CmsException("["+CmsIdGenerator.class.getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
         }
    }

    /**
     * Static method to read the id for the given table.
     * @param con - The connection to read from.
     * @param table - The name of the table to read the id for.
     * @param oldId - The oldId for this table.
     * @param newId - The newId for this table
     * @return if the row was updatet, or not.
     * @throws CmsException - if an sql-error occures.
     */
    private static boolean writeId(Connection con, String table, int oldId, int newId)
        throws CmsException {
        PreparedStatement statement = null;
         try {
            statement = con.prepareStatement("update CMS_SYSTEMID set ID = ? where TABLE_KEY = ? and ID = ?");
            statement.setInt(1, newId);
            statement.setString(2, table);
            statement.setInt(3, oldId);
            int amount = statement.executeUpdate();
            // return, if the update had succeeded
            return (amount == 1);
        } catch (SQLException e){
            throw new CmsException("["+CmsIdGenerator.class.getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
             if(statement != null) {
                 try {
                     statement.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
             }
         }
    }

    /**
     * Static method to read the id for the given table.
     * @param con - The connection to read from.
     * @param table - The name of the table to read the id for.
     * @param newId - The newId for this table
     * @throws CmsException - if an sql-error occures.
     */
    private static void createId(Connection con, String table, int newId)
        throws CmsException {
        PreparedStatement statement = null;
         try {
            statement = con.prepareStatement("insert into CMS_SYSTEMID values(?, ?)");
            statement.setString(1, table);
            statement.setInt(2, newId);
            statement.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+CmsIdGenerator.class.getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        }finally {
             if(statement != null) {
                 try {
                     statement.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
             }
         }
    }
}
