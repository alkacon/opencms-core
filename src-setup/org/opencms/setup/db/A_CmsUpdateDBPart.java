/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.db;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represent a part of the database update process.<p>
 *
 * @since 6.9.2
 */
public abstract class A_CmsUpdateDBPart implements I_CmsUpdateDBPart {

    /** The connection data to use. */
    protected Map<String, String> m_poolData;

    /** A map holding all SQL queries. */
    protected Map<String, String> m_queries;

    /**
     * Default constructor.<p>
     */
    public A_CmsUpdateDBPart() {

        m_queries = new HashMap<String, String>();
    }

    /**
     * @see org.opencms.setup.db.I_CmsUpdateDBPart#execute(Map)
     */
    public void execute(Map<String, String> dbPoolData) {

        m_poolData = new HashMap<String, String>(dbPoolData);

        CmsSetupDb setupDb = new CmsSetupDb(null);
        try {
            setupDb.setConnection(
                m_poolData.get("driver"),
                m_poolData.get("url"),
                m_poolData.get("params"),
                m_poolData.get("user"),
                m_poolData.get("pwd"));

            internalExecute(setupDb);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            setupDb.closeConnection();
        }
    }

    /**
     * Returns the database pool Data.<p>
     *
     * @return the database pool Data
     */
    public Map<String, String> getPoolData() {

        return Collections.unmodifiableMap(m_poolData);
    }

    /**
     * Searches for the SQL query with the specified key.<p>
     *
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(String queryKey) {

        String result = m_queries.get(queryKey);
        if (result != null) {
            result = result.replace('\t', ' ');
            result = result.replace('\n', ' ');
            result = result.replace('\r', ' ');
        }
        return result;
    }

    /**
     * Returns the default property file location.<p>
     *
     * @return the default property file location
     */
    protected String getPropertyFileLocation() {

        return getClass().getPackage().getName().replace('.', '/') + '/';
    }

    /**
     * Does the hard work.<p>
     *
     * @param setupDb the db connection interface
     *
     * @throws SQLException if something goes wrong
     */
    protected abstract void internalExecute(CmsSetupDb setupDb) throws SQLException;

    /**
     * Returns the keep History parameter value.<p>
     *
     * @return the keep History parameter value
     */
    protected boolean isKeepHistory() {

        return Boolean.parseBoolean(m_poolData.get("keepHistory"));
    }

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
        @SuppressWarnings("unchecked")
        Enumeration<String> propertyNames = (Enumeration<String>)properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            m_queries.put(propertyName, properties.getProperty(propertyName));
        }
    }
}
