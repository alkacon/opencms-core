/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupDb.java,v $
 * Date   : $Date: 2004/02/18 11:49:35 $
 * Version: $Revision: 1.4 $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
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
 * @version $Revision: 1.4 $ $Date: 2004/02/18 11:49:35 $
 */
public class CmsSetupDb extends Object {
    
    /** The folder where to read the setup data from */
    public static String C_SETUP_DATA_FOLDER = "WEB-INF/setupdata/";
    
    /** The folder where the setup wizard is located */
    public static String C_SETUP_FOLDER = "setup/";

    private Connection m_con = null;
    private Vector m_errors = null;
    private String m_basePath = null;
    private boolean m_errorLogging = true;

    /**
     * Creates a new CmsSetupDb object.<p>
     * 
     * @param basePath the location of the setup scripts
     */
    public CmsSetupDb(String basePath) {
        m_errors = new Vector();
        m_basePath = basePath;
    }

    /**
     * Creates a new internal connection to the database.<p>
     * 
     * @param DbDriver the name of the driver class
     * @param DbConStr the databse url
     * @param DbUser the database user
     * @param DbPwd the password of the database user
     */
    public void setConnection(String DbDriver, String DbConStr, String DbUser, String DbPwd) {
        try {
            Class.forName(DbDriver).newInstance();
            m_con = DriverManager.getConnection(DbConStr, DbUser, DbPwd);
        } catch (ClassNotFoundException e) {
            m_errors.addElement("Error loading JDBC driver: " + DbDriver + "\n" + e.toString() + "\n");
        } catch (Exception e) {
            m_errors.addElement("Error connecting to database using: " + DbConStr + "\n" + e.toString() + "\n");
        }
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
     * Internal method to parse and execute a setup script.<p>
     * 
     * @param file the filename of the setup script
     * @param replacers the replacements to perform in the script
     */
    private void executeSql(String databaseKey, String sqlScript, Map replacers) {
        String statement = "";
        LineNumberReader reader = null;
        String filename = null;
        String line = null;

        /* get and parse the setup script */
        try {
            filename = m_basePath + "setup" + File.separator + "database" + File.separator + databaseKey + File.separator + sqlScript;
            reader = new LineNumberReader(new FileReader(filename));
            line = null;

            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                StringTokenizer st = new StringTokenizer(line);

                while (st.hasMoreTokens()) {
                    String currentToken = st.nextToken();

                    /* comment! Skip rest of the line */
                    if (currentToken.startsWith("#")) {
                        break;
                    }

                    /* not to be executed */
                    if (currentToken.startsWith("prompt")) {
                        break;
                    }

                    /* add token to query */
                    statement += " " + currentToken;

                    /* query complete (terminated by ';') */
                    if (currentToken.endsWith(";")) {
                        /* cut of ';' at the end */
                        statement = statement.substring(0, (statement.length() - 1));

                        /* normal statement. Execute it */
                        if (replacers != null) {
                            ExecuteStatement(replaceValues(statement, replacers));
                        } else {
                            ExecuteStatement(statement);
                        }
                        //reset
                        statement = "";
                    }
                }

                statement += " \n";
            }
        } catch (FileNotFoundException e) {
            if (m_errorLogging) {
                m_errors.addElement("Database setup SQL script not found: " + filename);
                m_errors.addElement(e.toString());
            }            
        } catch (SQLException e) {
            if (m_errorLogging) {
                m_errors.addElement("Error executing SQL statement: " + statement);
                m_errors.addElement(e.toString());
            }            
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.addElement("Error parsing database setup SQL script in line: " + line);
                m_errors.addElement(e.toString());
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
    private void ExecuteStatement(String statement) throws SQLException {
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