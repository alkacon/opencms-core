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

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;

/**
 * Helper class to call database setup scripts.<p>
 *
 * @since 6.0.0
 */
public class CmsSetupDb extends Object {

    /** The folder where to read the setup data from. */
    public static final String SETUP_DATA_FOLDER = "WEB-INF/setupdata/";

    /** The folder where the setup wizard is located. */
    public static final String SETUP_FOLDER = "setup/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSetupDb.class);

    /** The setup base path. */
    private String m_basePath;

    /** A SQL connection. */
    private Connection m_con;

    /** A flag signaling if error logging is enabled. */
    private boolean m_errorLogging;

    /** A list to store error messages. */
    private List<String> m_errors;

    /**
     * Creates a new CmsSetupDb object.<p>
     *
     * @param basePath the location of the setup scripts
     */
    public CmsSetupDb(String basePath) {

        m_errors = new ArrayList<String>();
        m_basePath = basePath;
        m_errorLogging = true;
    }

    /**
     * Returns an optional warning message if needed, <code>null</code> if not.<p>
     *
     * @param db the selected database key
     *
     * @return html warning, or <code>null</code> if no warning
     */
    public String checkVariables(String db) {

        StringBuffer html = new StringBuffer(512);
        if (m_con == null) {
            return null; // prior error, trying to get a connection
        }
        Exception exception = null;
        if (db.equals("mysql")) {
            String statement = "SELECT @@max_allowed_packet;";
            Statement stmt = null;
            ResultSet rs = null;
            long maxAllowedPacket = 0;
            try {
                stmt = m_con.createStatement();
                rs = stmt.executeQuery(statement);
                if (rs.next()) {
                    maxAllowedPacket = rs.getLong(1);
                }
            } catch (Exception e) {
                exception = e;
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        // ignore
                    }
                }
            }
            if (exception == null) {
                int megabyte = 1024 * 1024;
                if (maxAllowedPacket > 0) {
                    html.append("<p>MySQL system variable <code>'max_allowed_packet'</code> is set to ");
                    html.append(maxAllowedPacket);
                    html.append(" Byte (");
                    html.append((maxAllowedPacket / megabyte) + "MB).</p>\n");
                }
                html.append(
                    "<p>Please note that it will not be possible for OpenCms to handle files bigger than this value in the VFS.</p>\n");
                int requiredMaxAllowdPacket = 16;
                if (maxAllowedPacket < (requiredMaxAllowdPacket * megabyte)) {
                    m_errors.add(
                        "<p><b>Your <code>'max_allowed_packet'</code> variable is set to less than "
                            + (requiredMaxAllowdPacket * megabyte)
                            + " Byte ("
                            + requiredMaxAllowdPacket
                            + "MB).</b></p>\n"
                            + "<p>The required value for running OpenCms is at least "
                            + requiredMaxAllowdPacket
                            + "MB."
                            + "Please change your MySQL configuration (in the <code>my.ini</code> or <code>my.cnf</code> file).</p>\n");
                }
            } else {
                html.append(
                    "<p><i>OpenCms was not able to detect the value of your <code>'max_allowed_packet'</code> variable.</i></p>\n");
                html.append(
                    "<p>Please note that it will not be possible for OpenCms to handle files bigger than this value.</p>\n");
                html.append(
                    "<p><b>The recommended value for running OpenCms is 16MB, please set it in your MySQL configuration (in your <code>my.ini</code> or <code>my.cnf</code> file).</b></p>\n");
                html.append(CmsException.getStackTraceAsString(exception));
            }
        }
        if (html.length() == 0) {
            return null;
        }
        return html.toString();
    }

    /**
     * Clears the error messages stored internally.<p>
     */
    public void clearErrors() {

        m_errors.clear();
    }

    /**
     * Closes the internal connection to the database.<p>
     */
    public void closeConnection() {

        try {
            if (m_con != null) {
                m_con.close();
            }
        } catch (Exception e) {
            // ignore
        }
        m_con = null;
    }

    /**
     * Calls the create database script for the given database.<p>
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createDatabase(String database, Map<String, String> replacer) {

        m_errorLogging = true;
        executeSql(database, "create_db.sql", replacer, true);
    }

    /**
     * Calls the create database script for the given database.<p>
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     * @param abortOnError indicates if the script is aborted if an error occurs
     */
    public void createDatabase(String database, Map<String, String> replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "create_db.sql", replacer, abortOnError);
    }

    /**
     * Calls the create tables script for the given database.<p>
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createTables(String database, Map<String, String> replacer) {

        m_errorLogging = true;
        executeSql(database, "create_tables.sql", replacer, true);
    }

    /**
     * Calls the create tables script for the given database.<p>
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     * @param abortOnError indicates if the script is aborted if an error occurs
     */
    public void createTables(String database, Map<String, String> replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "create_tables.sql", replacer, abortOnError);
    }

    /**
     * Calls the drop script for the given database.
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void dropDatabase(String database, Map<String, String> replacer) {

        m_errorLogging = true;
        executeSql(database, "drop_db.sql", replacer, false);
    }

    /**
     * Calls the drop script for the given database.
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     * @param abortOnError indicates if the script is aborted if an error occurs
     */
    public void dropDatabase(String database, Map<String, String> replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "drop_db.sql", replacer, abortOnError);
    }

    /**
     * Calls the drop tables script for the given database.<p>
     *
     * @param database the name of the database
     */
    public void dropTables(String database) {

        m_errorLogging = true;
        executeSql(database, "drop_tables.sql", null, false);
    }

    /**
     * Calls the drop tables script for the given database.<p>
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void dropTables(String database, Map<String, String> replacer) {

        m_errorLogging = true;
        executeSql(database, "drop_tables.sql", replacer, false);
    }

    /**
     * Calls the drop tables script for the given database.<p>
     *
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     * @param abortOnError indicates if the script is aborted if an error occurs
     */
    public void dropTables(String database, Map<String, String> replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "drop_tables.sql", replacer, abortOnError);
    }

    /**
     * Creates and executes a database statement from a String returning the result set.<p>
     *
     * @param query the query to execute
     * @param replacer the replacements to perform in the script
     *
     * @return the result set of the query
     *
     * @throws SQLException if something goes wrong
     */
    public CmsSetupDBWrapper executeSqlStatement(String query, Map<String, String> replacer) throws SQLException {

        CmsSetupDBWrapper dbwrapper = new CmsSetupDBWrapper(m_con);
        dbwrapper.createStatement();

        String queryToExecute = query;

        // Check if a map of replacements is given
        if (replacer != null) {
            queryToExecute = replaceTokens(query, replacer);
        }
        // do the query
        dbwrapper.excecuteQuery(queryToExecute);

        // return the result
        return dbwrapper;

    }

    /** Creates and executes a database statement from a String returning the result set.<p>
     *
     * @param query the query to execute
     * @param replacer the replacements to perform in the script
     * @param params the list of parameters for the statement
     *
     * @return the result set of the query
     *
     * @throws SQLException if something goes wrong
     */
    public CmsSetupDBWrapper executeSqlStatement(String query, Map<String, String> replacer, List<Object> params)
    throws SQLException {

        CmsSetupDBWrapper dbwrapper = new CmsSetupDBWrapper(m_con);

        String queryToExecute = query;

        // Check if a map of replacements is given
        if (replacer != null) {
            queryToExecute = replaceTokens(query, replacer);
        }

        dbwrapper.createPreparedStatement(queryToExecute, params);

        dbwrapper.excecutePreparedQuery();

        return dbwrapper;
    }

    /**
     * Returns the connection.<p>
     *
     * @return the connection
     */
    public Connection getConnection() {

        return m_con;
    }

    /**
     * Returns a Vector of Error messages.<p>
     *
     * @return all error messages collected internally
     */
    public List<String> getErrors() {

        return m_errors;
    }

    /**
     * Checks if the given table, column or combination of both is available in the database in case insensitive way.<P>
     *
     * @param table the sought table
     * @param column the sought column
     *
     * @return true if the requested table/column is available, false if not
     */
    public boolean hasTableOrColumn(String table, String column) {

        String tableName, columnName;
        boolean result;

        tableName = table == null ? null : table.toUpperCase();
        columnName = column == null ? null : column.toUpperCase();
        result = hasTableOrColumnCaseSensitive(tableName, columnName);

        if (!result) {
            tableName = table == null ? null : table.toLowerCase();
            columnName = column == null ? null : column.toLowerCase();
            result = result || hasTableOrColumnCaseSensitive(tableName, columnName);
        }

        return result;
    }

    /**
     * Checks if the given table, column or combination of both is available in the database in a case sensitive way.<P>
     *
     * @param table the sought table
     * @param column the sought column
     *
     * @return true if the requested table/column is available, false if not
     */
    public boolean hasTableOrColumnCaseSensitive(String table, String column) {

        boolean result = false;
        ResultSet set = null;

        try {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(column)) {
                // Check if the column is given
                set = m_con.getMetaData().getColumns(null, null, table, column);
                if (set.next()) {
                    String colname = set.getString("COLUMN_NAME");
                    if (colname.equalsIgnoreCase(column)) {
                        result = true; // The column is available
                    }
                }
            } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(table)) {
                // Check the table
                set = m_con.getMetaData().getTables(null, null, table, null);
                if (set.next()) {
                    String tablename = set.getString("TABLE_NAME");
                    if (tablename.equalsIgnoreCase(table)) {
                        result = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            try {
                if (set != null) {
                    set.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Checks if internal errors occurred.<p>
     *
     * @return true if internal errors occurred
     */
    public boolean noErrors() {

        return m_errors.isEmpty();
    }

    /**
     * Sets a new internal connection to the database.<p>
     *
     * @param conn the connection to use
     */
    public void setConnection(Connection conn) {

        m_con = conn;
    }

    /**
     * Creates a new internal connection to the database.<p>
     *
     * @param DbDriver JDBC driver class name
     * @param DbConStr JDBC connect URL
     * @param DbConStrParams JDBC connect URL params, or null
     * @param DbUser JDBC database user
     * @param DbPwd JDBC database password
     */
    public void setConnection(String DbDriver, String DbConStr, String DbConStrParams, String DbUser, String DbPwd) {

        setConnection(DbDriver, DbConStr, DbConStrParams, DbUser, DbPwd, true);
    }

    /**
     * Creates a new internal connection to the database.<p>
     *
     * @param DbDriver JDBC driver class name
     * @param DbConStr JDBC connect URL
     * @param DbConStrParams JDBC connect URL params, or null
     * @param DbUser JDBC database user
     * @param DbPwd JDBC database password
     * @param logErrors if set to 'true', errors are written to the log file
     */
    public void setConnection(
        String DbDriver,
        String DbConStr,
        String DbConStrParams,
        String DbUser,
        String DbPwd,
        boolean logErrors) {

        String jdbcUrl = DbConStr;
        try {
            if (DbConStrParams != null) {
                jdbcUrl += DbConStrParams;
            }
            Class.forName(DbDriver).newInstance();
            m_con = DriverManager.getConnection(jdbcUrl, DbUser, DbPwd);
            LOG.info("OpenCms setup connection established: " + m_con);
            LOG.info(" [autocommit: " + m_con.getAutoCommit() + "]");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found exception: " + e);
            m_errors.add(Messages.get().getBundle().key(Messages.ERR_LOAD_JDBC_DRIVER_1, DbDriver));
            m_errors.add(CmsException.getStackTraceAsString(e));
        } catch (Exception e) {
            if (logErrors) {
                System.out.println("Exception: " + CmsException.getStackTraceAsString(e));
            }
            m_errors.add(Messages.get().getBundle().key(Messages.ERR_DB_CONNECT_1, DbConStr));
            m_errors.add(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Calls an update script.<p>
     *
     * @param updateScript the update script code
     * @param replacers the replacers to use in the script code
     */
    public void updateDatabase(String updateScript, Map<String, String> replacers) {

        StringReader reader = new StringReader(updateScript);
        executeSql(reader, replacers, true);
    }

    /**
     * Calls an update script.<p>
     *
     * @param updateScript the update script code
     * @param replacers the replacers to use in the script code
     * @param abortOnError indicates if the script is aborted if an error occurs
     */
    public void updateDatabase(String updateScript, Map<String, String> replacers, boolean abortOnError) {

        StringReader reader = new StringReader(updateScript);
        executeSql(reader, replacers, abortOnError);
    }

    /**
     * Creates and executes a database statment from a String.<p>
     *
     * @param query the query to execute
     * @param replacer the replacements to perform in the script
     * @param params the list of parameters for the statement
     *
     * @return the result set of the query
     *
     * @throws SQLException if something goes wrong
     */
    public int updateSqlStatement(String query, Map<String, String> replacer, List<Object> params) throws SQLException {

        String queryToExecute = query;
        // Check if a map of replacements is given
        if (replacer != null) {
            queryToExecute = replaceTokens(query, replacer);
        }

        int result;
        PreparedStatement stmt = null;
        stmt = m_con.prepareStatement(queryToExecute);
        try {
            // Check the params
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    Object item = params.get(i);

                    // Check if the parameter is a string
                    if (item instanceof String) {
                        stmt.setString(i + 1, (String)item);
                    }
                    if (item instanceof Integer) {
                        Integer number = (Integer)item;
                        stmt.setInt(i + 1, number.intValue());
                    }
                    if (item instanceof Long) {
                        Long longNumber = (Long)item;
                        stmt.setLong(i + 1, longNumber.longValue());
                    }

                    // If item is none of types above set the statement to use the bytes
                    if (!(item instanceof Integer) && !(item instanceof String) && !(item instanceof Long)) {
                        try {
                            stmt.setBytes(i + 1, CmsDataTypeUtil.dataSerialize(item));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (!queryToExecute.startsWith("UPDATE CMS_ONLINE_STRUCTURE SET STRUCTURE_VERSION")
                && !queryToExecute.startsWith("UPDATE CMS_OFFLINE_STRUCTURE SET STRUCTURE_VERSION")) {
                System.out.println("executing query: " + queryToExecute);
                if ((params != null) && !params.isEmpty()) {
                    System.out.println("params: " + params);
                }
            }
            result = stmt.executeUpdate();
        } finally {
            stmt.close();
        }

        return result;
    }

    /**
     * Internal method to parse and execute a setup script.<p>
     *
     * @param inputReader an input stream reader on the setup script
     * @param replacers the replacements to perform in the script
     * @param abortOnError if a error occurs this flag indicates if to continue or to abort
     */
    private void executeSql(Reader inputReader, Map<String, String> replacers, boolean abortOnError) {

        String statement = "";
        LineNumberReader reader = null;
        String line = null;

        // parse the setup script
        try {
            reader = new LineNumberReader(inputReader);

            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                StringTokenizer st = new StringTokenizer(line);

                while (st.hasMoreTokens()) {
                    String currentToken = st.nextToken();

                    // comment! Skip rest of the line
                    if (currentToken.startsWith("#")) {
                        break;
                    }

                    // not to be executed
                    if (currentToken.startsWith("prompt")) {
                        break;
                    }

                    // add token to query
                    statement += " " + currentToken;

                    // query complete (terminated by ';')
                    if (currentToken.endsWith(";")) {
                        // cut of ';' at the end
                        statement = statement.substring(0, (statement.length() - 1));

                        // normal statement, execute it
                        try {
                            if (replacers != null) {
                                statement = replaceTokens(statement, replacers);
                                executeStatement(statement);
                            } else {
                                executeStatement(statement);
                            }
                        } catch (SQLException e) {
                            if (!abortOnError) {
                                if (m_errorLogging) {
                                    m_errors.add("Error executing SQL statement: " + statement);
                                    m_errors.add(CmsException.getStackTraceAsString(e));
                                }
                            } else {
                                throw e;
                            }
                        }

                        // reset
                        statement = "";
                    }
                }

                statement += " \n";
            }
        } catch (SQLException e) {
            if (m_errorLogging) {
                m_errors.add("Error executing SQL statement: " + statement);
                m_errors.add(CmsException.getStackTraceAsString(e));
            }
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.add("Error parsing database setup SQL script in line: " + line);
                m_errors.add(CmsException.getStackTraceAsString(e));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                // noop
            }
        }
    }

    /**
     * Internal method to parse and execute a setup script.<p>
     *
     * @param databaseKey the database variant of the script
     * @param sqlScript the name of the script
     * @param replacers the replacements to perform in the script
     * @param abortOnError if a error occurs this flag indicates if to continue or to abort
     */
    private void executeSql(String databaseKey, String sqlScript, Map<String, String> replacers, boolean abortOnError) {

        String filename = null;
        try {
            filename = m_basePath
                + "setup"
                + File.separator
                + "database"
                + File.separator
                + databaseKey
                + File.separator
                + sqlScript;
            executeSql(new FileReader(filename), replacers, abortOnError);
        } catch (FileNotFoundException e) {
            if (m_errorLogging) {
                m_errors.add("Database setup SQL script not found: " + filename);
                m_errors.add(CmsException.getStackTraceAsString(e));
            }
        }
    }

    /**
     * Creates and executes a database statement from a String.<p>
     *
     * @param statement the database statement
     *
     * @throws SQLException if something goes wrong
     */
    private void executeStatement(String statement) throws SQLException {

        Statement stmt = null;

        try {
            stmt = m_con.createStatement();
            stmt.execute(statement);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Replaces tokens "${xxx}" in a specified SQL query.<p>
     *
     * @param sql a SQL query
     * @param replacers a Map with values keyed by "${xxx}" tokens
     * @return the SQl query with all "${xxx}" tokens replaced
     */
    private String replaceTokens(String sql, Map<String, String> replacers) {

        Iterator<Map.Entry<String, String>> keys = replacers.entrySet().iterator();
        while (keys.hasNext()) {
            Map.Entry<String, String> entry = keys.next();

            String key = entry.getKey();
            String value = entry.getValue();

            sql = CmsStringUtil.substitute(sql, key, value);
        }

        return sql;
    }
}