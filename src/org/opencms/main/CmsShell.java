/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShell.java,v $
 * Date   : $Date: 2004/02/13 17:13:40 $
 * Version: $Revision: 1.14 $
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

package org.opencms.main;


import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsObject;
import org.opencms.setup.CmsSetupUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * A commad line interface to OpenCms which 
 * is used for the initial setup and also can be used to directly access the OpenCms
 * repository without the Workplace.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.14 $ $Date: 2004/02/13 17:13:40 $
 */
public class CmsShell {

    /** The OpenCms context object */
    private CmsObject m_cms;
    
    /** Internal driver manager */
    private CmsDriverManager m_driverManager;

    /** If set to true, all commands are echoed */
    private boolean m_echo;

    /** Indicates if the 'exit' command has been called */
    private boolean m_exitCalled;

    /** The OpenCms system object */
    private OpenCmsCore m_openCms;
    
    /** Internal shell command object */
    private CmsShellCommands m_shellCommands;

    /**
     * Creates a new CmsShell.<p>
     * 
     * @param fileInput a (file) input stream from which commands are read
     * @param basePath the OpenCms base application path
     */
    public CmsShell(String basePath, FileInputStream fileInput) {
        try {
            if (basePath == null || "".equals(basePath)) {
                System.err.println("No OpenCms home folder given. Trying to guess...");
                basePath = searchBaseFolder(System.getProperty("user.dir"));
                if (basePath == null || "".equals(basePath)) {
                    System.err.println("-----------------------------------------------------------------------");
                    System.err.println("OpenCms base folder could not be guessed.");
                    System.err.println("");
                    System.err.println("Please start the OpenCms command line interface from the directory");
                    System.err.println("containing the \"opencms.properties\" and the \"oclib\" folder or pass the");
                    System.err.println("OpenCms home folder as argument.");
                    System.err.println("-----------------------------------------------------------------------");
                    return;
                }
            }
            
            // first initialize runlevel 1 and set all path information
            m_openCms = OpenCmsCore.getInstance();
            m_openCms.getSystemInfo().setWebInfPath(basePath);
            
            String propsPath = m_openCms.getSystemInfo().getConfigurationFilePath();
            System.out.println("[OpenCms] Property path: " + propsPath);
            ExtendedProperties conf = CmsSetupUtils.loadProperties(propsPath);
            
            // now upgrade to runlevel 2
            m_openCms = m_openCms.upgradeRunlevel(conf);

            m_echo = false;
            m_exitCalled = false;
                    
            m_driverManager = m_openCms.getDriverManager();
            m_cms = m_openCms.initCmsObject(null, null, m_openCms.getDefaultUsers().getUserGuest(), m_openCms.getSiteManager().getDefaultSite().getSiteRoot(), I_CmsConstants.C_PROJECT_ONLINE_ID, null);    
            
            commands(fileInput);            
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    /**
     * Internal helper method for {@link #searchBaseFolder(String)}.<p>
     * 
     * @param currentDir current directory
     * @return String directory name
     */
    private static String downSearchBaseFolder(File currentDir) {
        if (isBaseFolder(currentDir)) {
            return currentDir.getAbsolutePath();
        } else {
            if (currentDir.exists() && currentDir.isDirectory()) {
                File webinfDir = new File(currentDir, "WEB-INF");
                if (isBaseFolder(webinfDir)) {
                    return webinfDir.getAbsolutePath();
                }
            }
        }
        return null;
    }

    /**
     * Internal helper method for {@link #searchBaseFolder(String)}.<p>
     * 
     * @param currentDir current directory
     * @return boolean <code>true</code> if currentDir is the base folder
     */
    private static boolean isBaseFolder(File currentDir) {
        if (currentDir.exists() && currentDir.isDirectory()) {
            File f1 = new File(currentDir.getAbsolutePath() + File.separator + I_CmsConstants.C_CONFIGURATION_PROPERTIES_FILE.replace('/', File.separatorChar));
            return f1.exists() && f1.isFile();
        }
        return false;
    }   

    /**
     * Main program entry point when started via the command line.<p>
     *
     * @param args parameters passed to the application via the command line
     */
    public static void main(String[] args) {
        boolean wrongUsage = false;    
        String base = null;
        String script = null;
    
        if (args.length > 2) {
            wrongUsage = true;
        } else {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-base=")) {
                    base = arg.substring(6);
                } else if (arg.startsWith("-script=")) {
                    script = arg.substring(8);
                } else {
                    System.out.println("wrong usage!");
                    wrongUsage = true;
                }
            }
        }
        if (wrongUsage) {
            System.out.println("Usage: java " + CmsShell.class.getName() + " [-base=<basepath>] [-script=<scriptfile>]");
        } else {
            FileInputStream stream = null;
            if (script != null) {
                try {
                    stream = new FileInputStream(script);
                } catch (IOException exc) {
                    System.out.println("wrong script-file " + script + " using stdin instead");
                }
            }
    
            if (stream == null) {
                // no script-file use input-stream
                stream = new FileInputStream(FileDescriptor.in);
            }
    
            new CmsShell(base, stream);
        }
    }

    /**
     * Searches for the OpenCms web application base folder during startup.<p>
     * 
     * @param startFolder the folder where to start searching
     * @return String the name of the folder on the local file system
     */
    public static String searchBaseFolder(String startFolder) {
        File currentDir = null;
        String base = null;
        File father = null;
        File grandFather = null;
    
        // Get a file obkect of the current folder
        if (startFolder != null && !"".equals(startFolder)) {
            currentDir = new File(startFolder);
        }
    
        // Get father and grand father
        if (currentDir != null && currentDir.exists()) {
            father = currentDir.getParentFile();
        }
        if (father != null && father.exists()) {
            grandFather = father.getParentFile();
        }
    
        if (currentDir != null) {
            base = downSearchBaseFolder(currentDir);
        }
        if (base == null && grandFather != null) {
            base = downSearchBaseFolder(grandFather);
        }
        if (base == null && father != null) {
            base = downSearchBaseFolder(father);
        }
        return base;
    }

    /**
     * Entry point when started from the OpenCms setup wizard.<p>
     *
     * @param file filename of a file containing the setup commands (e.g. cmssetup.txt)
     * @param base base folder for the OpenCms web application
     */
    public static void startSetup(String file, String base) {
        try {
            new CmsShell(base, new FileInputStream(new File(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls a list of commands.<p>
     *
     * @param commands a Vector of commands to be called
     */
    private void call(Vector commands) {
        if (m_echo) {
            // all commands should be echoed to the shell
            for (int i = 0; i < commands.size(); i++) {
                System.out.print(commands.elementAt(i) + " ");
            }
            System.out.println();
        }
        if ((commands == null) || (commands.size() == 0)) {
            return;
        }
        String splittet[] = new String[commands.size()];
        String toCall;
        commands.copyInto(splittet);
        toCall = splittet[0];
        if (toCall == null) {
            return;
        }
        Class paramClasses[] = new Class[splittet.length - 1];
        String params[] = new String[splittet.length - 1];
        for (int z = 0; z < splittet.length - 1; z++) {
            params[z] = splittet[z + 1];
            paramClasses[z] = String.class;
        }
        try {
            m_shellCommands.getClass().getMethod(toCall, paramClasses).invoke(m_shellCommands, params);
        } catch (InvocationTargetException ite) {
            System.err.println("Got Exception while using reflection:");
            ite.getTargetException().printStackTrace();
        } catch (NoSuchMethodException nsm) {
            System.out.println("The requested command was not found.\n-----------------------------------------------");
            m_shellCommands.printHelpText();
        } catch (Throwable t) {
            System.err.println("Got Exception while using reflection:");
            t.printStackTrace(System.err);            
        }
    }

    /**
     * The OpenCms command line interface.<p>
     * 
     * @param input a file input stream from which the commands are read
     */
    private void commands(FileInputStream input) {
        try {
            m_shellCommands = new CmsShellCommands(this, m_openCms, m_cms, m_driverManager);
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(input));
            while (!m_exitCalled) {
                printPrompt();
                String line = lnr.readLine();
                if ((line != null) && line.trim().startsWith("#")) {
                    System.out.println(line);
                    continue;                    
                }
                StringReader reader = new StringReader(line);
                StreamTokenizer st = new StreamTokenizer(reader);
                st.eolIsSignificant(true);                
                
                // put all tokens into a vector
                Vector args = new Vector();
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        args.addElement(Integer.toString(new Double(st.nval).intValue()));
                    } else {
                        args.addElement(st.sval);
                    }
                }
                reader.close();

                // exec the command
                call(args);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);            
        }
    }

    /**
     * Exits this shell.<p>
     */
    protected void exit() {
        m_exitCalled = true;
    }

    /**
     * Prints the current shell prompt.<p>
     */
    private void printPrompt() {
        System.out.print("{" + m_cms.getRequestContext().currentUser().getName() + "@" + m_cms.getRequestContext().currentProject().getName() + "}");
        System.out.print("> ");
    }
    
    /**
     * Sets the echo status.<p>
     * 
     * @param echo the echo status to set
     */
    protected void setEcho(boolean echo) {
        m_echo = echo;
    }

}
