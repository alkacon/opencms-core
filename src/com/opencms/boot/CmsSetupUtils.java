/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsSetupUtils.java,v $
* Date   : $Date: 2003/01/08 13:54:55 $
* Version: $Revision: 1.21.4.2 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.boot;

import source.org.apache.java.util.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.text.*;
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
                line = line.trim();

                if(line.startsWith("#")) {
                    /* write comment). */
                    fw.write(line);
                }
                else if (line.indexOf('=') > -1) {
                    String key = line.substring(0,line.indexOf('=')).trim();

                    /* write key */
                    fw.write((key+"="));

                    try {
                        /* Get the value to the given key from the properties*/
                        String value = extProp.get(key).toString();

                        /* if this was a list, we need to delete leading and tailing '[]' characters */
                        if(value.startsWith("[") && value.endsWith("]") && value.indexOf(',')>-1) {
                            value = splitMultipleValues(value.substring(1,value.length()-1));
                        }
                        /* write it */
                        fw.write(value);
                    }
                    catch (NullPointerException e)  {
                        /* no value found. Do nothing */
                    }
                }
                else  {
                  //do nothing
                }

                /* add at the end of each line */
                fw.write("\n");
            }
            lnr.close();
            fw.close();
        }
        catch (Exception e) {
            m_errors.addElement("Could not save properties to " + target + " \n");
            m_errors.addElement(e.toString() + "\n");
        }
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

    /**
     *  Checks if the used JDK is a higher version than the required JDK
     *  @param usedJDK The JDK version in use
     *  @param requiredJDK The required JDK version
     *  @return true if used JDK version is equal or higher than required JDK version,
     *  false otherwise
     */
    public static boolean compareJDKVersions(String usedJDK, String requiredJDK)  {
        int compare = usedJDK.compareTo(requiredJDK);
        return (!(compare < 0));
    }

    /** Checks if the used servlet engine is part of the servlet engines OpenCms supports
     *  @param thisEngine The servlet engine in use
     *  @param supportedEngines All known servlet engines OpenCms supports
     *  @return true if this engine is supported, false if it was not found in the list
     */
    public static boolean supportedServletEngine(String thisEngine, String[] supportedEngines)  {
        boolean supported = false;
        engineCheck: for(int i = 0; i < supportedEngines.length; i++)  {
            if (thisEngine.indexOf(supportedEngines[i]) >= 0) {
                supported = true;
                break engineCheck;
            }
        }
        return supported;
    }

    /** Checks if the used servlet engine is part of the servlet engines OpenCms
     *  does NOT support
     *  @param thisEngine The servlet engine in use
     *  @param supportedEngines All known servlet engines OpenCms does NOT support
     *  @return supportedEngines index or -1 if not found
     */
    public static int unsupportedServletEngine(String thisEngine, String[] unsupportedEngines)  {
        engineCheck: for(int i = 0; i < unsupportedEngines.length; i++)  {
            if (thisEngine.indexOf(unsupportedEngines[i]) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Writes the version info of the used servlet engine and the used JDK
     * to the version.txt
     *
     * @param thisEngine The servlet engine in use
     * @param usedJDK The JDK version in use
     */
    public static void writeVersionInfo(String thisEngine, String usedJDK, String basePath){
        FileWriter fOut = null;
        PrintWriter dOut = null;
        String filename = basePath+"ocsetup/versions.txt";
        try {
            File file = new File(filename);
            if(file.exists()){
                // new FileOutputStream of the existing file with parameter append=true
                fOut = new FileWriter(filename, true);
            } else {
                fOut = new FileWriter(file);
            }
            // write the content to the file in server filesystem
            dOut = new PrintWriter((Writer)fOut);
            dOut.println();
            dOut.println("############### currently used configuration ################");
            dOut.println("Date:                "+DateFormat.getDateTimeInstance().format(new java.util.Date(System.currentTimeMillis())));
            dOut.println("Used JDK:            "+usedJDK);
            dOut.println("Used Servlet Engine: "+thisEngine);
            dOut.close();
        } catch (IOException e) {
        } finally {
            try {
                if (fOut != null)
                    fOut.close();
            } catch (IOException e) {
            }
        }
    }

    private String splitMultipleValues(String value)  {
      String tempValue = "";
      StringTokenizer st = new StringTokenizer(value,",");
      int counter = 1;
      int max = st.countTokens();
      while(st.hasMoreTokens()) {
        tempValue += st.nextToken().trim();
        if(counter < max)  {
          tempValue += ", \\ \n";
        }
        counter++;
      }
      return tempValue;
    }
}