/*
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.boot;

import source.org.apache.java.util.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;

/**
 * This class provides several utilities and methods
 * used by the OpenCms setup wizard
 *
 * @author: Magnus Meurer
 */
public class CmsSetupUtils {

    private String m_configFolder;

    private String m_ocsetupFolder;

    private String m_basePath;

    private boolean m_errorLogging = true;

    private Vector m_errors;


    /** Constructor */
    public CmsSetupUtils(String basePath) {
      m_errors = new Vector();
      m_basePath = basePath;
      m_ocsetupFolder = basePath + "WEB-INF/ocsetup/";
      m_configFolder = basePath + "WEB-INF/config/";

    }

    /**
     *  Saves properties to specified file.
     *  @param extProp Properties to be saved
     *  @param originalFile File to save props to
     *  @param backup if true, create backupfile
     */
    public void saveProperties(ExtendedProperties extProp, String originalFile, boolean backup)  {
        if (new File(m_configFolder + originalFile).isFile()) {
            String backupFile = originalFile.substring(0,originalFile.lastIndexOf('.')) + ".ori";
            String tempFile = originalFile.substring(0,originalFile.lastIndexOf('.')) + ".tmp";

            m_errors.clear();

            // make a backup copy
            if(backup)  {
               backup(originalFile, backupFile);
            }

            //save to temporary file
            backup(originalFile, tempFile);

            // save properties
            save(extProp, tempFile, originalFile);

            // delete temp file
            File temp = new File(m_configFolder + tempFile);
            temp.delete();
        }
        else  {
            m_errors.addElement("No valid file: " + originalFile+ "\n");
        }

    }

    /** Copies a given file. (backup)
     *  @param originalFile source file
     *  @param backupFile target file
     */
    private void backup(String originalFile, String backupFile) {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(m_configFolder
                    + originalFile)));
            FileWriter fw = new FileWriter(new File(m_configFolder + backupFile));
            while (true)  {
                String line = lnr.readLine();
                if(line == null) break;
                fw.write(line+'\n');
            }
            lnr.close();
            fw.close();
        }
        catch (IOException e) {
          e.printStackTrace();
          m_errors.addElement("Could not save " + originalFile + " to " + backupFile + " \n");
          m_errors.addElement(e.toString()+"\n");
        }
    }

    /**
     * Save properties to file.
     * @param extProp The properties to be saved
     * @param source source file to get the keys from
     * @param target target file to save the properties to
     */
    private void save(ExtendedProperties extProp, String source, String target) {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(m_configFolder
                    + source)));

            FileWriter fw = new FileWriter(new File(m_configFolder + target));

            while(true) {
                String line = lnr.readLine();
                if(line == null)  break;

                int equalSign = line.indexOf('=');

                if (equalSign > 0 && (line.indexOf('#') == -1)) {
                    String key = line.substring(0,equalSign).trim();

                    /* write key */
                    fw.write((key+"="));

                    try {
                        /* Get the value to the given key from the properties*/
                        String value = extProp.get(key).toString();
                        /* write it */
                        fw.write(value);
                    }
                    catch (NullPointerException e)  {
                        /* no value found. Do nothing */
                    }
                }
                else  {
                    /* no key found ! write the line anyway (might be a comment). */
                    fw.write(line);
                }
                /* add at the end of each line */
                fw.write("\n");
            }
            lnr.close();
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            m_errors.addElement("Could not save properties to " + target + " \n");
            m_errors.addElement(e.toString() + "\n");
        }
    }

    /**
     * Connects to a database and tries to drop it, by executing the statements
     * from a file specified in dbsetup.properties.
     * @param DbDriver database driver
     * @param DbConStr database connection string
     * @param DbUser database user
     * @param DbPwd database password
     * @param resourceBroker identifier for the matching drop db script
     * @param replacer keys found in drop script will be replaced by value
     */
    public boolean dropDatabase(String DbDriver, String DbConStr, String DbUser,
            String DbPwd, String resourceBroker, Hashtable replacer) {
        Connection con = null;
        try {
            Class.forName(DbDriver);
            con = DriverManager.getConnection(DbConStr,DbUser,DbPwd);

            String file = getScript(resourceBroker+".dropdb");
            m_errors.clear();
            if (file != null) {
                m_errorLogging = true;
                parseScript(con,file,replacer);
                return m_errors.isEmpty();
            }
            else  {
                return false;
            }
        }
        catch (SQLException e)  {
            m_errors.addElement(e.toString());
            e.printStackTrace();
            return false;
        }
        catch(ClassNotFoundException e) {
            m_errors.addElement(e.toString());
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                con.close();
            } catch(Exception exc) {
                // ignore the exception. The connection is closed already
            }
        }
    }


    /**
     *  Connects to a database server and tries to create an new database
     *  by executing the statements from a file specified in dbsetup.properties.
     * @param DbDriver database driver
     * @param DbConStr database connection string
     * @param DbUser database user
     * @param DbPwd database password
     * @param resourceBroker identifier for the matching create db script
     * @param replacer keys found in create script will be replaced by value
     */
    public boolean createDatabase(String DbDriver, String DbConStr,
            String DbUser, String DbPwd, String resourceBroker, Hashtable replacer) {

        Connection con = null;
        try   {
            Class.forName(DbDriver);
            con = DriverManager.getConnection(DbConStr,DbUser,DbPwd);

            String file = getScript(resourceBroker+".createdb");
            m_errors.clear();
            if (file != null) {
                m_errorLogging = true;
                parseScript(con,file,replacer);
                return m_errors.isEmpty();
            }
            else  {
                m_errors.addElement("No create database file found. \n");
                return false;
            }
        }
        catch (SQLException e)  {
            m_errors.addElement(e.toString() + "\n");
            e.printStackTrace();
            return false;
        }
        catch (ClassNotFoundException e)  {
            m_errors.addElement(e.toString() + "\n");
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                con.close();
            } catch(Exception exc) {
                // ignore the exception. The connection is closed already
            }
        }
    }


    /**
     *  Connects to a database and tries to execute the statements
     *  from a file specified in dbsetup.properties.
     * @param DbDriver database driver
     * @param DbConStr database connection string
     * @param DbUser database user
     * @param DbPwd database password
     * @param resourceBroker identifier for the matching create db script
     */
    public boolean createTables(String DbDriver, String DbConStr,
            String DbUser, String DbPwd, String resourceBroker) {

        Connection con = null;

        try   {
            Class.forName(DbDriver);
            con = DriverManager.getConnection(DbConStr,DbUser,DbPwd);

            String file = getScript(resourceBroker+".createtables");
            m_errors.clear();
            if (file != null) {
                m_errorLogging = true;
                parseScript(con,file,null);
                return m_errors.isEmpty();
            }
            else  {
                m_errors.addElement("No create tables file found. \n");
                return false;
            }
        }
        catch (SQLException e)  {
            m_errors.addElement(e.toString() + "\n");
            e.printStackTrace();
            return false;
        }
        catch (ClassNotFoundException e)  {
            m_errors.addElement(e.toString() + "\n");
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                con.close();
            } catch(Exception exc) {
                // ignore the exception. The connection is closed already
            }
        }
    }

    /**
     * Gets sql queries from a file and executes them.
     * @param con database connection
     * @param file File to be parsed
     * @param replacers replaces keys found in file with values
     */
    private void parseScript(Connection con, String file, Hashtable replacers)   {

        /* indicates if the setup script contains included files (oracle) */
        boolean includedFile = false;

        /* get and parse the setup script */
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(m_ocsetupFolder + file));

              String line = null;
              String statement = "";
              while(true)  {
                  line = reader.readLine();
                  if(line==null)break;

                  StringTokenizer st = new StringTokenizer(line);

                  while (st.hasMoreTokens())  {
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
                          statement = statement.substring(0,(statement.length()-1));

                          /* there is an included File. Get it and execute it */
                          if (includedFile) {
                              if (replacers != null) {
                                  ExecuteStatement(con, replaceValues(getIncludedFile(statement),replacers));
                              }
                              else  {
                                  ExecuteStatement(con, getIncludedFile(statement));
                              }
                              //reset
                              includedFile = false;
                          }
                          /* normal statement. Execute it */
                          else  {
                              if (replacers != null) {
                                  ExecuteStatement(con, replaceValues(statement,replacers));
                              }
                              else  {
                                  ExecuteStatement(con, statement);
                              }
                          }
                          //reset
                          statement = "";
                      }
                  }
              }
              con.close();
              reader.close();
          }
          catch (Exception e) {
              m_errors.addElement(e.toString() + "\n");
              e.printStackTrace();
          }
      }

    /**
    * This function is called every time a file is included in the setup script.
    *  The sql statement from the setup script in fact contains the relative
    *  path to the included file, so that this file can be opened.
    *  The complete content of the included file (one large statement) is returned.
    *  @param statement Statement from the main setup script. Should contain relative path to the included file.
    *  @return sql query
    */
    private String getIncludedFile(String statement)  {
        String file;
        statement = statement.trim();

        if(statement.startsWith("./"))  {
            /* cut off */
            file = statement.substring(2);
        }
        else if(statement.startsWith("."))  {
            /* cut off */
            file = statement.substring(1);
        }
        else  {
            file = statement;
        }

        /* read and return everything */
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(m_ocsetupFolder + file));
            String stat = "";
            String line = null;

            while (true) {
                line = reader.readLine();
                if(line==null)break;
                stat += " " + line;
            }
            reader.close();
            stat = stat.trim();
            return stat;
        }
        catch (Exception e) {
            return null;
        }
    }

    /** Executes a given statement on the database
     *  @param con Connection to the database
     *  @param statement The statement to be executed
     */
    private void ExecuteStatement(Connection con, String statement)  {
        Statement stat;
        if(statement != null)  {
            try  {
                System.err.println("-----------------------------------------");
                System.err.println("-----------------------------------------");
                System.err.println("JETZT HIER DENN DA: "+statement);
                stat = con.createStatement();
                stat.executeUpdate(statement);
            }
            catch (Exception e)  {
                if(m_errorLogging)  {
                  m_errors.addElement(e.toString() + "\n");
                }
                e.printStackTrace();
            }
        }
    }

    /**
     *  This function returns the matching script filename
     *  from "dbsetup.properties"
     *  @param key Key for the properties
     *  @return script filename for the given key
     */
    private String getScript(String key) {
        /* open properties, get the value */
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("com/opencms/boot/dbsetup.properties"));
            String value = properties.getProperty(key);
            return value;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Replaces a token from a string.
     * Each token which starts and ends with "$$" gets replaced
     * @param source String with the tokens to be replaced
     * @param replacers replacing values
     */
    private String replaceValues(String source, Hashtable replacers)  {
        StringTokenizer tokenizer = new StringTokenizer(source);
        String temp = "";

        while(tokenizer.hasMoreTokens())  {
            String token = tokenizer.nextToken();

            // replace identifier found
            if(token.startsWith("$$") && token.endsWith("$$"))  {

                // look in the hashtable
                Object value = replacers.get(token);

                //found value
                if(value != null) {
                    token = value.toString();
                }
            }
            temp += token + " ";
        }
        return temp;
    }

    /**
     * URLEncodes a given string.
     * @param source string to be encoded
     */
    public static String escape(String source) {
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript

        // encoding, except the blank which is not encoded into a %20.
        String enc = URLEncoder.encode(source);
        StringTokenizer t = new StringTokenizer(enc, "+");
        while(t.hasMoreTokens()) {
            ret.append(t.nextToken());
            if(t.hasMoreTokens()) {
                ret.append("%20");
            }
        }
        return ret.toString();
    }

    public Vector getErrors() {
        return m_errors;
    }



}