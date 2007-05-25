/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/A_CmsUpdateDBPart.java,v $
 * Date   : $Date: 2007/05/25 11:54:08 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.update6to7;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represent a part of the database update process.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.9.2 
 */
public abstract class A_CmsUpdateDBPart implements I_CmsUpdateDBPart {

    /** The filename/path of the SQL query properties. */
    private static final String QUERY_PROPERTIES_PREFIX = "org/opencms/setup/update6to7/generic/";

    /** A map holding all SQL queries. */
    protected Map m_queries;

    /**
     * Default constructor.<p>
     * 
     * @throws IOException if the default sql queries property file could not be read 
     */
    public A_CmsUpdateDBPart()
    throws IOException {

        m_queries = new HashMap();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + getSqlQueriesFile());
    }

    /**
     * Executes the update part.<p>
     * 
     * @param dbPoolData the connection data to use
     */
    public void execute(Map dbPoolData) {

        CmsSetupDb setupDb = new CmsSetupDb(null);

        try {
            setupDb.setConnection(
                (String)dbPoolData.get("driver"),
                (String)dbPoolData.get("url"),
                (String)dbPoolData.get("params"),
                (String)dbPoolData.get("user"),
                (String)dbPoolData.get("pwd"));

            internalExecute(setupDb);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }
    }

    /**
     * Searches for the SQL query with the specified key.<p>
     * 
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(String queryKey) {

        return (String)m_queries.get(queryKey);
    }

    /**
     * Does the hard work.<p>
     * 
     * @param setupDb the db connection interface
     * 
     * @throws SQLException if somethign goes wrong
     */
    protected abstract void internalExecute(CmsSetupDb setupDb) throws SQLException;

    /**
     * Loads a Java properties hash containing SQL queries.<p>
     * 
     * @param propertyFilename the package/filename of the properties hash
     * 
     * @throws IOException if the sql queries property file could not be read 
     */
    protected void loadQueryProperties(String propertyFilename) throws IOException {

        Properties properties = new Properties();

        properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
        m_queries.putAll(properties);
    }
}
