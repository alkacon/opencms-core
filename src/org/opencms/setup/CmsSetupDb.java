/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupDb.java,v $
 * Date   : $Date: 2004/08/05 11:19:22 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.setup;

import org.opencms.main.CmsException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringBufferInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Helper class to call database setup scripts.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.8 $ $Date: 2004/08/05 11:19:22 $
 */
public class CmsSetupDb extends Object {
    
    /** The folder where to read the setup data from. */
    public static String C_SETUP_DATA_FOLDER = "WEB-INF/setupdata/";
    
    /** The folder where the setup wizard is located. */
    public static String C_SETUP_FOLDER = "setup/";

    private Connection m_con;
    private Vector m_errors;
    private String m_basePath;
    private boolean m_errorLogging;

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
            m_errors.addElement("Error loading JDBC driver: " + DbDriver);
            m_errors.addElement(CmsException.getStackTraceAsString(e));
        } catch (Exception e) {
            m_errors.addElement("Error connecting to database using: " + DbConStr);
            m_errors.addElement(CmsException.getStackTraceAsString(e));
        }
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
     * Calls the drop script for the given database.
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void dropDatabase(String database, Map replacer) {
        m_errorLogging = true;
        executeSql(database, "drop_db.sql", replacer);
    }

    /**
     * Calls the create database script for the given database.<p>
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createDatabase(String database, Map replacer) {
        m_errorLogging = true;
        executeSql(database, "create_db.sql", replacer);
    }

    /**
     * Calls the create tables script for the given database.<p>
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createTables(String database, Map replacer) {
        m_errorLogging = true;
        executeSql(database, "create_tables.sql", replacer);
    }

    /**
     * Calls the drop tables script for the given database.<p>
     * 
     * @param database the name of the database
     */
    public void dropTables(String database) {
        m_errorLogging = true;
        executeSql(database, "drop_tables.sql", null);
    }

    /**
     * Calls an update script.<p>
     * 
     * @param updateScript the update script (script code, NOT filename!)
     */
    public void updateDatabase(String updateScript, Map replacers) {
        InputStreamReader reader = new InputStreamReader(new StringBufferInputStream(updateScript));
        executeSql(reader, replacers);
    }
    
    /**
     * Internal method to parse and execute a setup script.<p>
     * 
     * @param databaseKey the database variant of the script
     * @param sqlScript the name of the script
     * @param replacers the replacements to perform in the script
     */
    private void executeSql(String databaseKey, String sqlScript, Map replacers) {
        String filename = null;
        InputStreamReader reader = null;
        try {
            filename = m_basePath + "setup" + File.separator + "database" + File.separator + databaseKey + File.separator + sqlScript;
            // 
            executeSql(new FileReader(filename), replacers);
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
     * Internal method to parse and execute a setup script.<p>
     * 
     * @param inputStreamReader an input stream reader on the setup script
     * @param replacers the replacements to perform in the script
     */
    private void executeSql(InputStreamReader inputStreamReader, Map replacers) {
        String statement = "";
        LineNumberReader reader = null;
        String line = null;

        // parse the setup script 
        try {
            reader = new LineNumberReader(inputStreamReader);
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
                        if (replacers != null) {
                            executeStatement(replaceValues(statement, replacers));
                        } else {
                            executeStatement(statement);
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
     * Internal method to perform replacements within a single line of script code.<p>
     * 
     * @param source the line of script code
     * @param replacers the replacements to perform in the line
     * @return the script line with replaced values
     */
    private String replaceValues(String source, Map replacers) {
        StringTokenizer tokenizer = new StringTokenizer(source);
        String temp = "";

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            // replace identifier found
            if (token.startsWith("${") && token.endsWith("}")) {

                // look in the hashtable
                Object value = replacers.get(token);

                //found value
                if (value != null) {
                    token = value.toString();
                }
            }
            temp += token + " ";
        }
        return temp;
    }

    /**
     * Creates and executes a database statment from a String.<p>
     * 
     * @param statement the database statement
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
     * Returns a Vector of Error messages.<p>
     * 
     * @return all error messages collected internally
     */
    public Vector getErrors() {
        return m_errors;
    }
    
    /**
     * Clears the error messages stored internally.<p>
     */
    public void clearErrors() {
        m_errors.clear();
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
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        closeConnection();
        super.finalize();
    }
}