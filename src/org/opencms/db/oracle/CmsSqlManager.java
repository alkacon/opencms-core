/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsSqlManager.java,v $
 * Date   : $Date: 2003/08/21 16:17:56 $
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

package org.opencms.db.oracle;

import oracle.jdbc.driver.OracleResultSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.DelegatingPreparedStatement;

/**
 * Handles SQL queries from query.properties of the Oracle/OCI package.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.5 $ $Date: 2003/08/21 16:17:56 $ 
 * @since 5.1
 */
public class CmsSqlManager extends org.opencms.db.generic.CmsSqlManager {

    private static Properties c_queries = null;
    private static final String C_PROPERTY_FILENAME = "org/opencms/db/oracle/query.properties";

    /**
     * Initializes the SQL manager.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.
     * 
     * @see setOfflinePoolUrl(String)
     * @see setOnlinePoolUrl(String)
     * @see setBackupPoolUrl(String)
     */  
    public CmsSqlManager() {
        super();

        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        if (c_queries != null) {
            c_queries.clear();
        }
        c_queries = null;
        
        super.finalize();
    }    

    /**
     * @see org.opencms.db.generic.CmsSqlManager#get(java.lang.String)
     */
    public String get(String queryName) {
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }

        String value = c_queries.getProperty(queryName);
        if (value == null || "".equals(value)) {
            value = super.get(queryName);
        }

        return value;
    }

    /**
     * @see org.opencms.db.generic.CmsSqlManager#getBytes(java.sql.ResultSet, java.lang.String)
     */
    public byte[] getBytes(ResultSet res, String attributeName) throws SQLException {
        oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(attributeName);
        byte[] content = new byte[(int) blob.length()];
        content = blob.getBytes(1, (int) blob.length());

        return content;
    }

    /**
     * @see org.opencms.db.generic.CmsSqlManager#getPreparedStatementForSql(java.sql.Connection, java.lang.String)
     */
    public PreparedStatement getPreparedStatementForSql(Connection con, String query) throws SQLException {
        // unfortunately, this wrapper is essential. some JDBC driver implementations 
        // don't accept the delegated objects of DBCP's connection pool.        
        return ((DelegatingPreparedStatement) con.prepareStatement(query)).getDelegate();
    }   

}
