/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsSetupUtils.java,v $
* Date   : $Date: 2003/07/28 15:03:24 $
* Version: $Revision: 1.32 $
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

import com.opencms.util.Encoder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.StringTokenizer;
import java.util.Vector;

import source.org.apache.java.util.ExtendedProperties;
/**
 * This class provides several utilities and methods
 * used by the OpenCms setup wizard
 *
 * @author Magnus Meurer
 */
public class CmsSetupUtils {

    private String m_configFolder;

    private Vector m_errors;


    /** Constructor */
    public CmsSetupUtils(String basePath) {
      m_errors = new Vector();
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
               this.copyFile(originalFile, backupFile);
            }

            //save to temporary file
            this.copyFile(originalFile, tempFile);

            // save properties
            this.save(extProp, tempFile, originalFile);

            // delete temp file
            File temp = new File(m_configFolder + tempFile);
            temp.delete();
        } else  {
            m_errors.addElement("No valid file: " + originalFile+ "\n");
        }

    }

    /** 
     * Copies a given file.
     * 
     * @param sourceFilename source file
     * @param destFilename destination file
     */
    public void copyFile(String sourceFilename, String destFilename) {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(m_configFolder + sourceFilename)));
            FileWriter fw = new FileWriter(new File(m_configFolder + destFilename));
            
            while (true) {
                String line = lnr.readLine();
                if (line == null)
                    break;
                fw.write(line + '\n');
            }
            
            lnr.close();
            fw.close();
        } catch (IOException e) {
            m_errors.addElement("Could not copy " + sourceFilename + " to " + destFilename + " \n");
            m_errors.addElement(e.toString() + "\n");
        }
    }
    
    /**
     * Restores the registry.xml either to or from a backup file, depending
     * whether the setup wizard is executed the first time (the backup registry
     * doesnt exist) or not (the backup registry exists).
     * 
     * @param originalRegistryFilename something like "registry.ori"
     * @param registryFilename the registry's real file name "registry.xml"
     */
    public void backupRegistry(String registryFilename, String originalRegistryFilename) {
        File originalRegistry = new File(m_configFolder + originalRegistryFilename);
        
        if (originalRegistry.exists()) {
            this.copyFile(originalRegistryFilename,registryFilename);
        } else {
            this.copyFile(registryFilename,originalRegistryFilename);
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
                    // output comment
                    fw.write(line);
                    fw.write("\n");                    
                }
                else if (line.indexOf('=') > -1) {
                    String key = line.substring(0,line.indexOf('=')).trim();
                    // write key
                    fw.write((key+"="));
                    try {
                        // Get the value to the given key from the properties 
                        String value = extProp.get(key).toString();

                        // if this was a list (array), we need to delete leading and tailing '[]' characters
                        if(value.startsWith("[") && value.endsWith("]") && value.indexOf(',')>-1) {
                            value = splitMultipleValues(value.substring(1,value.length()-1));
                        }
                        // write it
                        fw.write(value);
                    } catch (NullPointerException e)  {
                        // no value found - do nothing 
                    }                    
                    // add trailing line feed
                    fw.write("\n");                    
                } else if ("".equals(line)) {
                    // output empty line
                    fw.write("\n");
                }
            }
            
            lnr.close();
            fw.close();
        } catch (Exception e) {
            m_errors.addElement("Could not save properties to " + target + " \n");
            m_errors.addElement(e.toString() + "\n");
        }
    }

    /**
     * URLEncodes a given string similar to JavaScript.
     * @param source string to be encoded
     */
        public static String escape(String source, String encoding) {
        return Encoder.escapeWBlanks(source,encoding);
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
        for(int i = 0; i < unsupportedEngines.length; i++)  {
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
        String filename = basePath + CmsSetupDb.C_SETUP_FOLDER + "versions.txt";
        try {
            File file = new File(filename);
            if(file.exists()){
                // new FileOutputStream of the existing file with parameter append=true
                fOut = new FileWriter(filename, true);
            } else {
                fOut = new FileWriter(file);
            }
            // write the content to the file in server filesystem
            dOut = new PrintWriter(fOut);
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