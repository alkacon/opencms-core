/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsSetupDb.java,v $
 * Date   : $Date: 2003/11/13 10:29:27 $
 * Version: $Revision: 1.11 $
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
 
package com.opencms.boot;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.11 $ $Date: 2003/11/13 10:29:27 $
 */
public class CmsSetupDb extends Object {

    public static String C_SETUP_DATA_FOLDER = "WEB-INF/setupdata/";
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
            Class.forName(DbDriver);
            m_con = DriverManager.getConnection(DbConStr, DbUser, DbPwd);
        } catch (SQLException e) {
            m_errors.addElement("Could no connect to database via: " + DbConStr + "\n" + e.toString() + "\n");
        } catch (ClassNotFoundException e) {
            m_errors.addElement("Error while loading driver: " + DbDriver + "\n" + e.toString() + "\n");
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
    public void dropDatabase(String database, Hashtable replacer) {
        String file = getScript(database + ".dropdb");
        if (file != null) {
            m_errorLogging = true;
            parseScript(file, replacer);
        } else {
            m_errors.addElement("Could not open database drop script: " + database + ".dropdb");
        }
    }

    /**
     * Calls the create database script for the given database.<p>
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createDatabase(String database, Hashtable replacer) {
        String file = getScript(database + ".createdb");
        if (file != null) {
            m_errorLogging = true;
            parseScript(file, replacer);
        } else {
            m_errors.addElement("No create database script found: " + database + ".createdb \n");
        }
    }

    /**
     * Calls the create tables script for the given database.<p>
     * 
     * @param database the name of the database
     * @param replacer the replacements to perform in the drop script
     */
    public void createTables(String database, Hashtable replacer) {
        String file = getScript(database + ".createtables");
        if (file != null) {
            m_errorLogging = true;
            parseScript(file, replacer);
        } else {
            m_errors.addElement("No create tables script found: " + database + ".createtables \n");
        }
    }

    /**
     * Calls the drop tables script for the given database.<p>
     * 
     * @param database the name of the database
     */
    public void dropTables(String database) {
        String file = getScript(database + ".droptables");
        if (file != null) {
            m_errorLogging = true;
            parseScript(file, null);
        } else {
            m_errors.addElement("No drop tables script found: " + database + ".droptables \n");
        }
    }

    /**
     * Internal method to parse and execute a setup script.<p>
     * 
     * @param file the filename of the setup script
     * @param replacers the replacements to perform in the script
     */
    private void parseScript(String file, Hashtable replacers) {

        /* indicates if the setup script contains included files (oracle) */
        boolean includedFile = false;
        
        String statement = "";

        /* get and parse the setup script */
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(m_basePath + C_SETUP_DATA_FOLDER + file));
            String line = null;
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

                    /* there is an included file */
                    if (currentToken.startsWith("@")) {
                        /* cut of '@' */
                        currentToken = currentToken.substring(1);
                        includedFile = true;
                    }

                    /* add token to query */
                    statement += " " + currentToken;

                    /* query complete (terminated by ';') */
                    if (currentToken.endsWith(";")) {
                        /* cut of ';' at the end */
                        statement = statement.substring(0, (statement.length() - 1));

                        /* there is an included File. Get it and execute it */
                        if (includedFile) {
                            if (replacers != null) {
                                ExecuteStatement(replaceValues(getIncludedFile(statement), replacers));
                            } else {
                                ExecuteStatement(getIncludedFile(statement));
                            }
                            //reset
                            includedFile = false;
                        } else {
                            /* normal statement. Execute it */
                            if (replacers != null) {
                                ExecuteStatement(replaceValues(statement, replacers));
                            } else {
                                ExecuteStatement(statement);
                            }
                        }
                        //reset
                        statement = "";
                    }
                }
                statement += " \n";
            }
            reader.close();
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.addElement("error: " + e.toString() + "\nin statement:\n" + statement);
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
    private String replaceValues(String source, Hashtable replacers) {
        StringTokenizer tokenizer = new StringTokenizer(source);
        String temp = "";

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            // replace identifier found
            if (token.startsWith("$$") && token.endsWith("$$")) {

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
     * Returns the filename of a script from dbsetup.properties.<p> 
     * 
     * @param key the key to identify the script
     * @return the filename of the script
     */
    private String getScript(String key) {
        /* open properties, get the value */
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("com/opencms/boot/dbsetup.properties"));
            String value = properties.getProperty(key);
            return value;
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.addElement(e.toString() + " \n");
            }
            return null;
        }
    }

    /**
     * Returns an included file as a String.<p>
     * 
     * @param statement the filename from the include statement  
     * @return the contents of the file as String
     */
    private String getIncludedFile(String statement) {
        String file;
        statement = statement.trim();

        if (statement.startsWith("./")) {
            /* cut off */
            file = statement.substring(2);
        } else if (statement.startsWith(".")) {
            /* cut off */
            file = statement.substring(1);
        } else {
            file = statement;
        }

        /* read and return everything */
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(m_basePath + C_SETUP_DATA_FOLDER + file));
            String stat = "";
            String line = null;

            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if  (!(line.startsWith("--") || line.startsWith("/"))) {
                    stat += line + " \n";
                }
            }
            reader.close();
            return stat.trim();
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.addElement(e.toString() + "\n");
            }
            return null;
        }
    }

    /**
     * Creates and executes a database statment from a String.<p>
     * 
     * @param statement the database statement
     */
    private void ExecuteStatement(String statement) {
        Statement stat;
        try {
            stat = m_con.createStatement();
            stat.execute(statement);
            stat.close();
        } catch (Exception e) {
            if (m_errorLogging) {
                m_errors.addElement("error: " + e.toString() + "\nin statement:\n" + statement);
            }
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
        try {
            m_con.close();
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }
}