/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupDb.java,v $
 * Date   : $Date: 2007/05/29 12:58:48 $
 * Version: $Revision: 1.25.4.6 $
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

package org.opencms.setup;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.opencms.main.CmsException;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsStringUtil;

/**
 * Helper class to call database setup scripts.<p>
 * 
 * @author Thomas Weckert  
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.25.4.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSetupDb extends Object {

    /** The folder where to read the setup data from. */
    public static final String SETUP_DATA_FOLDER = "WEB-INF/setupdata/";

    /** The folder where the setup wizard is located. */
    public static final String SETUP_FOLDER = "setup/";

    private String m_basePath;
    private Connection m_con;
    private boolean m_errorLogging;
    private Vector m_errors;

    /**
     * Creates a new CmsSetupDb object.<p>
     * 
     * @param basePath the location of the setup scripts
     */
    public CmsSetupDb(String basePath) {

        m_errors = new Vector();
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
        SQLException exception = null;
        if (db.equals("mysql")) { // just for 4.0, > is not needed, < is not supported.
            String statement = "SELECT @@max_allowed_packet;";
            Statement stmt = null;
            ResultSet rs = null;
            long map = 0;
            try {
                stmt = m_con.createStatement();
                rs = stmt.executeQuery(statement);
                if (rs.next()) {
                    map = rs.getLong(1);
                }
            } catch (SQLException e) {
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
                if (map > 0) {
                    html.append("MySQL system variable <code>'max_allowed_packet'</code> is set to ");
                    html.append(map);
                    html.append(" Bytes.<p>\n");
                }
                html.append("Please, note that it will not be possible for OpenCms to handle files bigger than this value.<p>\n");
                if (map < 15 * 1024 * 1024) {
                    m_errors.addElement("<b>Your <code>'max_allowed_packet'</code> variable is set to less than 16Mb ("
                        + map
                        + ").</b>\n"
                        + "The recommended value for running OpenCms is 16Mb."
                        + "Please change your MySQL configuration (in your <code>mi.ini</code> or <code>my.cnf</code> file).\n");
                }
            }
        }
        if ((exception != null) || db.equals("mysql_3")) {
            html.append("<i>OpenCms was not able to detect the value of your <code>'max_allowed_packet'</code> variable.</i><p>\n");
            html.append("Please, note that it will not be possible for OpenCms to handle files bigger than this value.<p>\n");
            html.append("<b>The recommended value for running OpenCms is 16Mb, please set it in your MySQL configuration (in your <code>mi.ini</code> or <code>my.cnf</code> file).</b>\n");
            if (exception != null) {
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
    public void createDatabase(String database, Map replacer) {

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
    public void createDatabase(String database, Map replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "create_db.sql", replacer, abortOnError);
    }

    /**
     * Calls the create tables script for the given database.<p>
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createTables(String database, Map replacer) {

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
    public void createTables(String database, Map replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "create_tables.sql", replacer, abortOnError);
    }

    /**
     * Calls the drop script for the given database.
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void dropDatabase(String database, Map replacer) {

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
    public void dropDatabase(String database, Map replacer, boolean abortOnError) {

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
    public void dropTables(String database, Map replacer) {

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
    public void dropTables(String database, Map replacer, boolean abortOnError) {

        m_errorLogging = true;
        executeSql(database, "drop_tables.sql", replacer, abortOnError);
    }

    /**
     * Creates and executes a database statment from a String returning the result set.<p>
     * 
     * @param query the query to execute
     * @param replacer the replacements to perform in the script
     * 
     * @return the result set of the query 
     * 
     * @throws SQLException if something goes wrong
     */
    public ResultSet executeSqlStatement(String query, Map replacer) throws SQLException {

        Statement stmt = null;
        ResultSet resultSet = null;

        stmt = m_con.createStatement();
        String queryToExecute = query;
        // Check if a map of replacements is given
        if (replacer != null) {
            queryToExecute = replaceTokens(query, replacer);
        }

        resultSet = stmt.executeQuery(queryToExecute);

        return resultSet;
    }

    /**
     * Returns a Vector of Error messages.<p>
     * 
     * @return all error messages collected internally
     */
    public Vector getErrors() {

        return m_errors;
    }

    /**
     * Checks if the given table, column or combination of both is available in the database.<P>
     * 
     * @param table the sought table
     * @param column the sought column
     * 
     * @return true if the requested table/column is available, false if not
     */
    public boolean hasTableOrColumn(String table, String column) {

        boolean result = false;
        ResultSet set = null;

        try {

            // Check the table 
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(table)) {
                set = m_con.getMetaData().getTables(null, null, table, null);

                while (set.next()) {
                    String tablename = set.getString("TABLE_NAME");
                    if (tablename.equalsIgnoreCase(table)) {
                        result = true;
                    } else {
                        result = false;
                    }
                }

                set.close();
                set = null;
            }

            // Check if the column is given
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(column)) {
                result = false; // reset the boolean value to false
                set = m_con.getMetaData().getColumns(null, null, table, column);

                while (set.next()) {
                    String colname = set.getString("COLUMN_NAME");
                    if (colname.equalsIgnoreCase(column)) {
                        result = true; // The column is available
                    } else {
                        result = false;
                    }
                }
                set.close();
                set = null;
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = false;
        } finally {
            try {
                if (set != null) {
                    set.close();
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Checks if internal errors occured.<p>
     * 
     * @return true if internal errors occured
     */
    public boolean noErrors() {

        return m_errors.isEmpty();
    }

    /**
     * Sets a new internal connection to teh database.<p>
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

        String jdbcUrl = DbConStr;
        try {
            if (DbConStrParams != null) {
                jdbcUrl += DbConStrParams;
            }
            Class.forName(DbDriver).newInstance();
            m_con = DriverManager.getConnection(jdbcUrl, DbUser, DbPwd);
        } catch (ClassNotFoundException e) {
            m_errors.addElement(Messages.get().getBundle().key(Messages.ERR_LOAD_JDBC_DRIVER_1, DbDriver));
            m_errors.addElement(CmsException.getStackTraceAsString(e));
        } catch (Exception e) {
            m_errors.addElement(Messages.get().getBundle().key(Messages.ERR_DB_CONNECT_1, DbConStr));
            m_errors.addElement(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Calls an update script.<p>
     * 
     * @param updateScript the update script code
     * @param replacers the replacers to use in the script code
     */
    public void updateDatabase(String updateScript, Map replacers) {

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
    public void updateDatabase(String updateScript, Map replacers, boolean abortOnError) {

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
    public int updateSqlStatement(String query, Map replacer, List params) throws SQLException {

        PreparedStatement stmt = null;
        int result;

        String queryToExecute = query;
        // Check if a map of replacements is given
        if (replacer != null) {
            queryToExecute = replaceTokens(query, replacer);
        }

        stmt = m_con.prepareStatement(queryToExecute);
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

        return result;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            closeConnection();
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }

    /**
     * Internal method to parse and execute a setup script.<p>
     * 
     * @param inputReader an input stream reader on the setup script
     * @param replacers the replacements to perform in the script
     * @param abortOnError if a error occurs this flag indicates if to continue or to abort
     */
    private void executeSql(Reader inputReader, Map replacers, boolean abortOnError) {

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
                                    m_errors.addElement("Error executing SQL statement: " + statement);
                                    m_errors.addElement(CmsException.getStackTraceAsString(e));
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
                m_errors.addElement("Error executing SQL statement: " + statement);
                m_errors.addElement(CmsException.getStackTraceAsString(e));
            }
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.addElement("Error parsing database setup SQL script in line: " + line);
                m_errors.addElement(CmsException.getStackTraceAsString(e));
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
    private void executeSql(String databaseKey, String sqlScript, Map replacers, boolean abortOnError) {

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
                m_errors.addElement("Database setup SQL script not found: " + filename);
                m_errors.addElement(CmsException.getStackTraceAsString(e));
            }
        }
    }

    /**
     * Creates and executes a database statment from a String.<p>
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
    private String replaceTokens(String sql, Map replacers) {

        Iterator keys = replacers.entrySet().iterator();
        while (keys.hasNext()) {
            Map.Entry entry = (Map.Entry)keys.next();

            String key = (String)entry.getKey();
            String value = (String)entry.getValue();

            sql = CmsStringUtil.substitute(sql, key, value);
        }

        return sql;
    }

}