/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsMain.java,v $
 * Date   : $Date: 2003/04/01 15:20:18 $
 * Version: $Revision: 1.12 $
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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Command line interface to access resources in OpenCms.<p>
 * 
 * This can be used to test the OpenCms VFS, 
 * it is also used for the initial setup.<p>
 *
 * @author Andreas Schouten
 * @author Anders Fugmann
 * @version $Revision: 1.12 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsMain {

  private static final int C_MODE_ECMASCRIPT = 1;
  private static final int C_MODE_CLASSIC = 0;

    /**
     * Main program entry point when started via the command line.<p>
     *
     * @param args parameters passed to the application via the command line
     */
    public static void main(String[] args) {
        boolean wrongUsage = false;

        String base = null;
        String script = null;
        String cmdLineMode = null;

        int mode = C_MODE_CLASSIC;

        if(args.length > 2) {

            wrongUsage = true;
        } else {
            for(int i=0; i < args.length; i++) {
                String arg = args[i];
                if(arg.startsWith("-base=") ) {
                    base = arg.substring(6);
                } else if(arg.startsWith("-script=") ) {
                    script = arg.substring(8);
                } else if(arg.startsWith("-mode=") ) {
                    cmdLineMode = arg.substring(6);
                } else {
                  System.out.println("wrong usage!");
                  wrongUsage = true;
                }
            }
        }
        if(wrongUsage) {
            usage();
        } else {
            FileInputStream stream = null;
            if(script != null) {
                try {
                    stream = new FileInputStream(script);
                } catch (IOException exc) {
                    System.out.println("wrong script-file " + script + " using stdin instead");
                }
            }
            if(cmdLineMode!=null){
              if(cmdLineMode.equals("ecmascript"))mode=C_MODE_ECMASCRIPT;
              if(cmdLineMode.equals("es"))mode=C_MODE_ECMASCRIPT;
              if(cmdLineMode.equals("classic"))mode=C_MODE_CLASSIC;
            }

            if(stream == null) {
                // no script-file use input-stream
                stream = new FileInputStream(FileDescriptor.in);
            }

            begin(stream, base, mode);
        }
    }

    /**
     * Main program entry point when started via the OpenCms setup wizard.<p>
     *
     * @param file filename of a file containing the setup commands (e.g. cmssetup.txt)
     * @param base base folder for the OpenCms web application
     */
    public static void startSetup(String file, String base)  {
        try {
            begin(new FileInputStream(new File(file)), base, C_MODE_CLASSIC);
        }
        catch (FileNotFoundException  e)  {
          e.printStackTrace();
        }
    }

    /**
     * Used to launch the OpenCms command line interface (CmsShell).<p>
     * 
     * @param fis file input stream of a scrfipt containing commands
     * @param base base folder for the OpenCms web application
     * @param mode can be C_MODE_CLASSIC or C_MODE_ECMASCRIPT
     */
    private static void begin(FileInputStream fis, String base, int mode)  {
        String classname = "com.opencms.core.CmsShell";
        if(base == null || "".equals(base)) {
            System.out.println("No OpenCms home folder given. Trying to guess...");
            base = searchBaseFolder(System.getProperty("user.dir"));
            if(base == null || "".equals(base)) {
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
        base = CmsBase.setBasePath(base);
        try {
            
            Class c = Class.forName(classname);
            // Now we have to look for the constructor
            Object o = c.newInstance();

            Class classArgs[] = {fis.getClass()};

            // the "classic" mode
            if(mode==C_MODE_CLASSIC){
                    Method m = c.getMethod("commands", classArgs);

                    Object objArgs[] = {fis};
                    m.invoke(o, objArgs);
            }

            // the "ecmascript" mode
            if(mode==C_MODE_ECMASCRIPT){
                Method m = c.getMethod("ecmacommands", classArgs);

                Object objArgs[] = {fis};
                m.invoke(o, objArgs);
            }
        } catch(InvocationTargetException e) {
            Throwable t = e.getTargetException();
            t.printStackTrace();
        } catch(Throwable t) {
            t.printStackTrace();
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
        if(startFolder != null && !"".equals(startFolder)) {
            currentDir = new File(startFolder);
        }

        // Get father and grand father
        if(currentDir != null && currentDir.exists()) {
            father = currentDir.getParentFile();
        }
        if(father != null && father.exists()) {
            grandFather = father.getParentFile();
        }

        if(currentDir != null) {
            base = downSearchBaseFolder(currentDir);
        }
        if(base == null && grandFather != null) {
            base = downSearchBaseFolder(grandFather);
        }
        if(base == null && father != null) {
            base = downSearchBaseFolder(father);
        }
        return base;
    }

    /**
     * Internal helper method for {@link #searchBaseFolder(String)}.<p>
     * 
     * @param currentDir current directory
     * @return String directory name
     */
    private static String downSearchBaseFolder(File currentDir) {
        if(isBaseFolder(currentDir)) {
            return currentDir.getAbsolutePath();
        } else {
            if(currentDir.exists() && currentDir.isDirectory()) {
                File webinfDir = new File(currentDir, "WEB-INF");
                if(isBaseFolder(webinfDir)) {
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
        if(currentDir.exists() && currentDir.isDirectory()) {
            File f1 = new File(currentDir.getAbsolutePath() + File.separator + CmsBase.getPropertiesPath(false));
            File f2 = new File(currentDir, "ocsetup");
            return (f1.exists() && f1.isFile() && f2.exists() && f2.isDirectory());
        }
        return false;
    }

    /**
     * Prints out a usage help message to <code>System.out</code>.<p>
     */
    private static void usage() {
        System.out.println("Usage: java com.opencms.core.CmsMain [-base=<basepath>] [-script=<scriptfile>] [-mode=[<ecmascript><es>/<classic>]]");
    }
}
