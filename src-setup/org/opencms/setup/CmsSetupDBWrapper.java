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

package org.opencms.setup;

import org.opencms.util.CmsDataTypeUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Wrapper to encapsulate, connection, statement and result set for the setup
 * and update wizard.<p>
 */
public class CmsSetupDBWrapper {

    /** the statement to use in the db wrapper. */
    private Statement m_statement;

    /** the connection to use in the db wrapper. */
    private Connection m_connection;

    /** the result set returned by the db wrapper. */
    private ResultSet m_resultset;

    /** the prepared statement. */
    private PreparedStatement m_preparedStatement;

    /**
     * Constructor, creates a new CmsSetupDBWrapper.<p>
     * @param con the connection to use in this db wrapper.
     */
    public CmsSetupDBWrapper(Connection con) {

        m_connection = con;
    }

    /**
     * Closes result set, and statement. <p>
     */
    public void close() {

        // result set
        if (m_resultset != null) {
            try {
                m_resultset.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        // statement
        if (m_statement != null) {
            try {
                m_statement.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        // prepared statement
        if (m_preparedStatement != null) {
            try {
                m_preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

    /**
     * Creates a new SQL Statement on the connection of this DB wrapper.<p>
     * @throws SQLException if statement cannot be created
     */
    public void createStatement() throws SQLException {

        m_statement = m_connection.createStatement();
    }

    /**
     * Creates a new SQL Statement on the connection of this DB wrapper.<p>
     *
     * @param query the DB query to use
     * @param params List of additional parameters
     *
     * @throws SQLException if statement cannot be created
     */
    public void createPreparedStatement(String query, List<Object> params) throws SQLException {

        m_preparedStatement = m_connection.prepareStatement(query);

        // Check the params
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                Object item = params.get(i);

                // Check if the parameter is a string
                if (item instanceof String) {
                    m_preparedStatement.setString(i + 1, (String)item);
                }
                if (item instanceof Integer) {
                    Integer number = (Integer)item;
                    m_preparedStatement.setInt(i + 1, number.intValue());
                }
                if (item instanceof Long) {
                    Long longNumber = (Long)item;
                    m_preparedStatement.setLong(i + 1, longNumber.longValue());
                }

                // If item is none of types above set the statement to use the bytes
                if (!(item instanceof Integer) && !(item instanceof String) && !(item instanceof Long)) {
                    try {
                        m_preparedStatement.setBytes(i + 1, CmsDataTypeUtil.dataSerialize(item));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Executes a query on the connection and statement of this db wrapper.<p>
     *
     * @param query the query to execute
     *
     * @throws SQLException if statement cannot be created
     */
    public void excecuteQuery(String query) throws SQLException {

        m_resultset = m_statement.executeQuery(query);
    }

    /**
     * Executes a query on the connection and prepared statement of this db wrapper.<p>
     * @throws SQLException if statement cannot be created
     */
    public void excecutePreparedQuery() throws SQLException {

        m_resultset = m_preparedStatement.executeQuery();
    }

    /**
     * Returns the res.<p>
     *
     * @return the res
     */
    public ResultSet getResultSet() {

        return m_resultset;
    }
}