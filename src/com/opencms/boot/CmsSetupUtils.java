/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsSetupUtils.java,v $
 * Date   : $Date: 2003/11/13 11:41:59 $
 * Version: $Revision: 1.39 $
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

import org.opencms.util.CmsStringSubstitution;

import com.opencms.util.Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Provides utilities methodsused by the OpenCms setup wizard.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.39 $
 */
public class CmsSetupUtils {

    private String m_configFolder;

    private Vector m_errors;

    /** 
     * Constructor.<p>
     * 
     * @param basePath the base path where OpenCms is installed
     */
    public CmsSetupUtils(String basePath) {
        m_errors = new Vector();
        m_configFolder = basePath + "WEB-INF/config/";
    }

    /**
     *  Checks if the used JDK is a higher version than the required JDK
     *  @param usedJDK The JDK version in use
     *  @param requiredJDK The required JDK version
     *  @return true if used JDK version is equal or higher than required JDK version,
     *  false otherwise
     */
    public static boolean compareJDKVersions(String usedJDK, String requiredJDK) {
        int compare = usedJDK.compareTo(requiredJDK);
        return (!(compare < 0));
    }

    /**
     * URLEncodes a given string similar to JavaScript.<p>
     * 
     * @param source string to be encoded
     * @param encoding the encoding to use
     * @return the encoding String
     */
    public static String escape(String source, String encoding) {
        return Encoder.escapeWBlanks(source, encoding);
    }

    /**
     * Loads the default OpenCms properties.<p>
     * 
     * @param path the path to read the properties from
     * @return the initialized OpenCms properties
     * @throws IOException in case of IO errors 
     */
    public static ExtendedProperties loadProperties(String path) throws IOException {

        FileInputStream input = null;
        ExtendedProperties properties = null;

        try {
            input = new FileInputStream(new File(path));
            properties = new ExtendedProperties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            try {
                input.close();
            } catch (Exception ex) {
                // nothing we can do
            }
            throw e;
        }

        for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            Object obj = properties.get(key);
            String value[] = {};

            if (obj instanceof Vector) {
                value = (String[]) ((Vector)obj).toArray(value);
            } else {
                String v[] = {(String)obj };
                value = v;
            }

            for (int j = 0; j < value.length; j++) {
                value[j] = CmsStringSubstitution.substitute(value[j], "\\,", ",");
                value[j] = CmsStringSubstitution.substitute(value[j], "\\=", "=");
            }

            if (value.length > 1) {
                properties.put(key, new Vector(Arrays.asList(value)));
            } else {
                properties.put(key, value[0]);
            }
        }

        return properties;
    }

    /** Checks if the used servlet engine is part of the servlet engines OpenCms supports
     *  @param thisEngine The servlet engine in use
     *  @param supportedEngines All known servlet engines OpenCms supports
     *  @return true if this engine is supported, false if it was not found in the list
     */
    public static boolean supportedServletEngine(String thisEngine, String[] supportedEngines) {
        boolean supported = false;
        engineCheck : for (int i = 0; i < supportedEngines.length; i++) {
            if (thisEngine.indexOf(supportedEngines[i]) >= 0) {
                supported = true;
                break engineCheck;
            }
        }
        return supported;
    }

    /** 
     * Checks if the used servlet engine is part of the servlet engines OpenCms
     * does NOT support<p>
     * 
     * @param thisEngine the servlet engine in use
     * @param unsupportedEngines all known servlet engines OpenCms does NOT support
     * @return the engine id or -1 if the engine is not supported
     */
    public static int unsupportedServletEngine(String thisEngine, String[] unsupportedEngines) {
        for (int i = 0; i < unsupportedEngines.length; i++) {
            if (thisEngine.indexOf(unsupportedEngines[i]) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Writes the version info of the used servlet engine and the used JDK
     * to the version.txt.<p>
     *
     * @param thisEngine The servlet engine in use
     * @param usedJDK The JDK version in use
     * @param basePath the OpenCms base path
     */
    public static void writeVersionInfo(String thisEngine, String usedJDK, String basePath) {
        FileWriter fOut = null;
        PrintWriter dOut = null;
        String filename = basePath + CmsSetupDb.C_SETUP_FOLDER + "versions.txt";
        try {
            File file = new File(filename);
            if (file.exists()) {
                // new FileOutputStream of the existing file with parameter append=true
                fOut = new FileWriter(filename, true);
            } else {
                fOut = new FileWriter(file);
            }
            // write the content to the file in server filesystem
            dOut = new PrintWriter(fOut);
            dOut.println();
            dOut.println("############### currently used configuration ################");
            dOut.println("Date:                " + DateFormat.getDateTimeInstance().format(new java.util.Date(System.currentTimeMillis())));
            dOut.println("Used JDK:            " + usedJDK);
            dOut.println("Used Servlet Engine: " + thisEngine);
            dOut.close();
        } catch (IOException e) {
            // nothing we can do
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                // nothing we can do
            }
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
            this.copyFile(originalRegistryFilename, registryFilename);
        } else {
            this.copyFile(registryFilename, originalRegistryFilename);
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
                if (line == null) {
                    break;
                }
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
     * Returns the config path.<p>
     * 
     * @return the config path
     */
    public String getConfigPath() {
        return m_configFolder;
    }

    /**
     * Returns a Vector containing all error that occured.<p>
     * 
     * @return a Vector containing all error that occured
     */
    public Vector getErrors() {
        return m_errors;
    }

    /**
     * Save properties to file.
     * @param extProp The properties to be saved
     * @param source source file to get the keys from
     * @param target target file to save the properties to
     */
    private void save(ExtendedProperties extProp, String source, String target) {

        //int ww = 0;
        //while (ww == 0) {
        //    ww = 0;
        //}

        try {
            HashSet alreadyWritten = new HashSet();

            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(m_configFolder + source)));

            FileWriter fw = new FileWriter(new File(m_configFolder + target));

            while (true) {
                String line = lnr.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();

                if ("".equals(line)) {
                    // output empty line
                    fw.write("\n");
                } else if (line.startsWith("#")) {
                    // output comment
                    fw.write(line);
                    fw.write("\n");
                } else {

                    int index = line.indexOf('=');
                    int index1 = line.indexOf("\\=");
                    if (line.indexOf('=') > -1 && index1 != index - 1) {

                        String key = line.substring(0, line.indexOf('=')).trim();
                        if (alreadyWritten.contains(key)) {
                            continue;
                        }
                        // write key
                        fw.write((key + "="));
                        try {
                            Object obj = extProp.get(key);
                            String value = "";

                            if (obj != null && obj instanceof Vector) {
                                String values[] = {};
                                values = (String[]) ((Vector)obj).toArray(values);
                                StringBuffer buf = new StringBuffer();

                                for (int i = 0; i < values.length; i++) {

                                    // escape commas and equals in value
                                    values[i] = CmsStringSubstitution.substitute(values[i], ",", "\\,");
                                    values[i] = CmsStringSubstitution.substitute(values[i], "=", "\\=");

                                    buf.append("\t" + values[i] + ((i < values.length - 1) ? ",\\\n" : ""));
                                }
                                value = buf.toString();

                                // write it
                                fw.write("\\\n" + value);

                            } else if (obj != null) {

                                value = ((String)obj).trim();

                                // escape commas and equals in value
                                value = CmsStringSubstitution.substitute(value, ",", "\\,");
                                value = CmsStringSubstitution.substitute(value, "=", "\\=");

                                // write it
                                fw.write(value);
                            }

                        } catch (NullPointerException e) {
                            // no value found - do nothing 
                        }
                        // add trailing line feed
                        fw.write("\n");

                        // remember that this properties is already written (multi values)
                        alreadyWritten.add(key);
                    }
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
     *  Saves properties to specified file.
     *  @param extProp Properties to be saved
     *  @param originalFile File to save props to
     *  @param backup if true, create backupfile
     */
    public void saveProperties(ExtendedProperties extProp, String originalFile, boolean backup) {
        if (new File(m_configFolder + originalFile).isFile()) {
            String backupFile = originalFile.substring(0, originalFile.lastIndexOf('.')) + ".ori";
            String tempFile = originalFile.substring(0, originalFile.lastIndexOf('.')) + ".tmp";

            m_errors.clear();

            // make a backup copy
            if (backup) {
                this.copyFile(originalFile, backupFile);
            }

            //save to temporary file
            this.copyFile(originalFile, tempFile);

            // save properties
            this.save(extProp, tempFile, originalFile);

            // delete temp file
            File temp = new File(m_configFolder + tempFile);
            temp.delete();
        } else {
            m_errors.addElement("No valid file: " + originalFile + "\n");
        }

    }

    /**
     * Sets the config path.<p>
     * 
     * @param configPath the config path to set
     */
    public void setConfigPath(String configPath) {
        m_configFolder = configPath;
    }
}