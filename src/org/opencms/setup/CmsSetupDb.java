/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupDb.java,v $
 * Date   : $Date: 2005/07/06 11:40:30 $
 * Version: $Revision: 1.23 $
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

import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Helper class to call database setup scripts.<p>
 * 
 * @author Thomas Weckert  
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.23 $ 
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
            m_con.close();
        } catch (Exception e) {
            // ignore
        }
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
     * Returns a Vector of Error messages.<p>
     * 
     * @return all error messages collected internally
     */
    public Vector getErrors() {

        return m_errors;
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
            m_errors.addElement(Messages.get().key(Messages.ERR_LOAD_JDBC_DRIVER_1, DbDriver));
            m_errors.addElement(CmsException.getStackTraceAsString(e));
        } catch (Exception e) {
            m_errors.addElement(Messages.get().key(Messages.ERR_DB_CONNECT_1, DbConStr));
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
            line = null;

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
        InputStreamReader reader = null;
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
            stmt.close();
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

        Iterator keys = replacers.keySet().iterator();
        while (keys.hasNext()) {

            String key = (String)keys.next();
            String value = (String)replacers.get(key);

            sql = CmsStringUtil.substitute(sql, key, value);
        }

        return sql;
    }
}