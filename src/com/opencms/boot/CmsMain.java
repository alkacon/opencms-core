/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsMain.java,v $
* Date   : $Date: 2002/08/21 11:32:45 $
* Version: $Revision: 1.7 $
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

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import source.org.apache.java.util.*;

/**
 * This class is a commadnlineinterface for the opencms. It can be used to test
 * the opencms, and for the initial setup. It uses the OpenCms-Object.
 *
 * @author Andreas Schouten
 * @author Anders Fugmann
 * @version $Revision: 1.7 $ $Date: 2002/08/21 11:32:45 $
 */
public class CmsMain {


  private static final int C_MODE_ECMASCRIPT = 1;
  private static final int C_MODE_CLASSIC = 0;

    /**
     * Main entry point when started via the command line.
     *
     * @param args Array of parameters passed to the application
     * via the command line.
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
     * Main entry point when started via the OpenCms setup wizard.
     *
     * @param file file containing the setup commands (cmssetup.txt)
     * @param base OpenCms base folder
     */
    public static void startSetup(String file, String base)  {
        try {
            begin(new FileInputStream(new File(file)),base,C_MODE_CLASSIC);
        }
        catch (FileNotFoundException  e)  {
          e.printStackTrace();
        }
    }

    /**
     * Used to launch the OpenCms command line interface (CmsShell)
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
            /* FLEX: The classes are loaded from the system class loader now
            CmsClassLoader loader = new CmsClassLoader();
            // Search for jar files in the oclib folder.
            collectRepositories(base, loader);
            Class c = loader.loadClass(classname);
             */ 
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

    private static boolean isBaseFolder(File currentDir) {
        if(currentDir.exists() && currentDir.isDirectory()) {
            File f1 = new File(currentDir.getAbsolutePath() + File.separator + CmsBase.getPropertiesPath(false));
            File f2 = new File(currentDir, "ocsetup");
            return (f1.exists() && f1.isFile() && f2.exists() && f2.isDirectory());
        }
        return false;
    }

    /**
     * Gives the usage-information to the user.
     */
    private static void usage() {
        System.out.println("Usage: java com.opencms.core.CmsMain [-base=<basepath>] [-script=<scriptfile>] [-mode=[<ecmascript><es>/<classic>]]");
    }

    /* FLEX: Reopsitorys are collected in com.opencms.flex.CmsFlexClassLoader
    public static void collectRepositories(String base, CmsClassLoader cl) {
        System.out.println("Collecting Repositories");
        File classesFolder;
        // Add simple, unpacked classes
        classesFolder = new File(base + "occlasses");
        if(classesFolder.exists() && classesFolder.isDirectory()) {
            cl.addRepository(classesFolder.getAbsolutePath(), CmsClassLoader.C_REPOSITORY_CLASSIC_FS);
        }

        // Add standard class directory in case of development envronment
        classesFolder = new File(base + "classes");
        if(classesFolder.exists() && classesFolder.isDirectory()) {
            cl.addRepository(classesFolder.getAbsolutePath(), CmsClassLoader.C_REPOSITORY_CLASSIC_FS);
        }
        
        // Add jar and zip files in "lib" folder
        File libFolder = new File(base + "oclib");
        System.out.println("oclib folder: " + base + "oclib");
        if(libFolder.exists() && libFolder.isDirectory()) {
            System.out.println("jarlist");
            String[] jarlist = libFolder.list(
                new FilenameFilter() {
                    public boolean accept(File dir, String fileName) {
                        return(fileName.endsWith(".jar") || fileName.endsWith(".zip"));
                    }
                }
            );
            for(int i=0; (jarlist != null) && (i < jarlist.length); ++i) {
                cl.addRepository(new File(libFolder, jarlist[i]).getAbsolutePath(), CmsClassLoader.C_REPOSITORY_CLASSIC_FS);
            }
        }
    }
     */

}
