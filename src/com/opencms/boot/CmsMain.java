
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsMain.java,v $
* Date   : $Date: 2001/07/11 11:48:38 $
* Version: $Revision: 1.2 $
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
 * @version $Revision: 1.2 $ $Date: 2001/07/11 11:48:38 $
 */
public class CmsMain {

/**
     * Main entry point when started via the command line.
     *
     * @param args Array of parameters passed to the application
     * via the command line.
     */
    public static void main(String[] args) {
        if(args.length > 1) {

            // print out usage-information.
            usage();
        }
        else {
            String base = null;
            if(args.length == 1) {
                base = args[0];
            }
        begin(new FileInputStream(FileDescriptor.in),base);
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
            begin(new FileInputStream(new File(file)),base);
        }
        catch (FileNotFoundException  e)  {
          e.printStackTrace();
        }
    }

    /**
     * Used to launch the OpenCms command line interface (CmsShell)
     */
    private static void begin(FileInputStream fis, String base)  {
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
            CmsClassLoader loader = new CmsClassLoader();
            // Search for jar files in the oclib folder.
            collectRepositories(base, loader);
            Class c = loader.loadClass(classname);
            // Now we have to look for the constructor
            Object o = c.newInstance();

            Class classArgs[] = {fis.getClass()};
            Method m = c.getMethod("commands", classArgs);

            Object objArgs[] = {fis};
            m.invoke(o, objArgs);
        } catch(InvocationTargetException e) {
            Throwable t = e.getTargetException();
            t.printStackTrace();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }


    protected static String searchBaseFolder(String startFolder) {

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
            File f2 = new File(currentDir, "oclib");
            return (f1.exists() && f1.isFile() && f2.exists() && f2.isDirectory());
        }
        return false;
    }

    /**
     * Gives the usage-information to the user.
     */
    private static void usage() {
        System.out.println("Usage: java com.opencms.core.CmsMain properties-file");
    }

    protected static void collectRepositories(String base, CmsClassLoader cl) {
        System.out.println("Collecting Repositories");
        // Add simple, unpacked classes
        File classesFolder = new File(base + "occlasses");
        if(classesFolder.exists() && classesFolder.isDirectory()) {
            cl.addRepository(classesFolder.getAbsolutePath(), CmsClassLoader.C_REPOSITORY_CLASSIC_FS);
        }

        // Add jar and zip files in "lib" folder
        File libFolder = new File(base + "oclib");
        System.out.println("oclib folder: " + base + "oclib");
        if(libFolder.exists() && libFolder.isDirectory()) {
            System.out.println("jarlist");
            String[] jarlist = libFolder.list(new FilenameFilter() {
                public boolean accept(File dir, String fileName) {
                        return(fileName.endsWith(".jar") || fileName.endsWith(".zip"));
                }});
            for(int i=0; (jarlist != null) && (i < jarlist.length); ++i) {
                cl.addRepository(new File(libFolder, jarlist[i]).getAbsolutePath(), CmsClassLoader.C_REPOSITORY_CLASSIC_FS);
            }
        }
    }

}
