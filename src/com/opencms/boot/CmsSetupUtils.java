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


    public CmsSetupUtils(String basePath) {
      m_basePath = basePath;
      m_ocsetupFolder = basePath + "WEB-INF/ocsetup/";
      m_configFolder = basePath + "WEB-INF/config/";

    }

    public void saveProperties(ExtendedProperties extProp, String originalFile)  {
        if (new File(m_configFolder + originalFile).isFile()) {
            String backupFile = originalFile.substring(0,originalFile.lastIndexOf('.')) + ".ori";
            backup(originalFile, backupFile);
            save(extProp, backupFile, originalFile);
        }
        else  {
            CmsSetup.setErrors("No valid file: " + originalFile);
        }

    }


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
          CmsSetup.setErrors("Could not save " + originalFile + " to " + backupFile + " \n");
          CmsSetup.setErrors(e.toString());
        }
    }


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
            CmsSetup.setErrors("Could not save properties to " + target + " \n");
            CmsSetup.setErrors(e.toString() + "\n");
        }
    }


    /** This function opens a connection to a database and gives sql statements from
     *  the setup scripts (indicated by the resourceBroker) to the database.
     *  @param DbDriver The database driver
     *  @param DbUser The user of the database
     *  @param DbPwd Users password
     *  @param resourceBroker The resourceBroker for the database
     */
    public void createDatabase(String DbDriver, String DbConStr,
            String DbUser, String DbPwd, String resourceBroker) {

        Connection con;

        try   {
            Class.forName(DbDriver);
            con = DriverManager.getConnection(DbConStr,DbUser,DbPwd);

            String file = getScript(resourceBroker+".createdb");
            if (file != null) {
                m_errorLogging = false;
                parseScript(con,file);
                m_errorLogging = true;
            }
        }
        catch (SQLException e)  {
            CmsSetup.setErrors(e.toString() + "\n");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)  {
            CmsSetup.setErrors(e.toString() + "\n");
            e.printStackTrace();
        }
    }



    public void createTables(String DbDriver, String DbConStr,
            String DbUser, String DbPwd, String resourceBroker) {

        Connection con;

        try   {
            Class.forName(DbDriver);
            con = DriverManager.getConnection(DbConStr,DbUser,DbPwd);

            String file = getScript(resourceBroker+".createtables");
            parseScript(con,file);
        }
        catch (SQLException e)  {
            CmsSetup.setErrors(e.toString() + "\n");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)  {
            CmsSetup.setErrors(e.toString() + "\n");
            e.printStackTrace();
        }
    }

    private void parseScript(Connection con, String file)   {

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
                              ExecuteStatement(con, getIncludedFile(statement));
                              //reset
                              includedFile = false;
                          }
                          /* normal statement. Execute it */
                          else  {
                              ExecuteStatement(con, statement);
                          }
                          //reset
                          statement = "";
                      }
                  }
              }
              reader.close();
          }
          catch (Exception e) {
              CmsSetup.setErrors(e.toString() + "\n");
              e.printStackTrace();
          }
      }

    /** This function is called every time a file is included in the setup script.
    *  The sql statement from the setup script in fact contains the relative
    *  path to the included file, so that this file can be opened.
    *  The complete content of the included file (one large statement) is returned.
    *  @param statement Statement from the main setup script. Should contain relative path to the included file.
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
     *  @param con Connection to the database on which the statement shall be executed
     *  @param statement The statement to be executed
     */
    private void ExecuteStatement(Connection con, String statement)  {
        Statement stat;
        if(statement != null)  {
            try  {
                stat = con.createStatement();
                stat.executeUpdate(statement);
            }
            catch (Exception e)  {
                if(m_errorLogging)  {
                  CmsSetup.setErrors(e.toString() + "\n");
                }
            }
        }
    }

    /** This function returns the matching script filename
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
}