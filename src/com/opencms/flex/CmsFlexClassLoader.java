/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsFlexClassLoader.java,v $
 * Date   : $Date: 2002/08/21 11:29:32 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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
 *
 * First created on 18. April 2002, 08:39
 */


package com.opencms.flex;

/**
 * This class loader loads classes from the opencms system using
 * a CmsObject object to get access to all system resources. 
 * The CmsObject is initialized with Guest permissions, so it is vital
 * that all class files in the VFS can be read by user "Guest".<p>
 *
 * The CmsFlexClassLoader will look in all directories specified in 
 * the file /WEB-INF/config/registry.xml in the &lt;repositories&gt; tag.
 * Usually this is set only to /system/classes which is fine since 
 * all OpenCms modules are saving their classes there. 
 * In case your application requires classes to be read from 
 * a different location in the OpenCms VFS, just add another repository 
 * entry.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsFlexClassLoader extends ClassLoader {

    /** Boolean for additional debug output control */
    private static final boolean DEBUG = false;
    
    /** Each element of the vector should be a String that desribes a cms folder */
    private java.util.Vector m_repository; 

    /** A CmsObject to read classes from the virtual file system */
    private com.opencms.file.CmsObject m_cms;
    
    /**
     * Constructor for the CmsFlexClassLoader.
     * 
     * @param parent The parent classloader for delegation
     * @param openCms An initialized OpenCms object 
     */
    public CmsFlexClassLoader(ClassLoader parent, com.opencms.core.A_OpenCms openCms) {
        super(parent);
  
        if (DEBUG) System.err.println("FlexClassLoader: Initializing...");
        
        // First initialize an CmsObject for access to the VFS
        try {
            // Create an OpenCms object
            m_cms = new com.opencms.file.CmsObject();
            // Log in default user.
            openCms.initUser(m_cms, null, null, com.opencms.core.I_CmsConstants.C_USER_GUEST, com.opencms.core.I_CmsConstants.C_GROUP_GUEST, com.opencms.core.I_CmsConstants.C_PROJECT_ONLINE_ID, null);
        } catch (Exception e) {
            // User could not be initialized
            if (DEBUG) System.err.println("FlexClassLoader: Aborting initialization, phase 1 caught exception " + e);
            log("Aborting initialization, phase 1 (creating CmsObject) caught exception " + e);
            m_cms = null;
            return;
        }      

        // Initialize new vector for the repositories
        m_repository = new java.util.Vector();
        
        // Get the repositories for the classloader from the registry.xml file
        try {
            // Add the repositories from the registry, if it is available
            com.opencms.file.I_CmsRegistry reg = m_cms.getRegistry();

            if(reg != null) {
                String[] repositoriesFromRegistry = reg.getRepositories();
                for(int i = 0;i < repositoriesFromRegistry.length;i++) {
                    if (DEBUG) System.err.println("FlexClassLoader: Adding repository from registry: " + repositoriesFromRegistry[i]);
                    try {
                        // This will throw an exception if the repository does not exist
                        m_cms.readFileHeader(repositoriesFromRegistry[i]);
                        // If no exception is throws the repository exists ans is added to the internal list
                        if(! m_repository.contains(repositoriesFromRegistry[i])) {
                            // Make sure this is really a new entry
                            m_repository.addElement(repositoriesFromRegistry[i]);
                        }                        
                    }
                    catch(com.opencms.core.CmsException e) {
                        // In case of error the repository is just not used
                    }
                }
            }
        } catch (Exception e) {
            // Something happed while initializing the repositories
            if (DEBUG) System.err.println("FlexClassLoader: Aborting initialization, phase 2 caught exception " + e);
            log("Aborting initialization, phase 2 (setting repositories) caught exception " + e);
            m_cms = null;
            return;
        }        
        if (DEBUG) System.err.println("FlexClassLoader: Initializing finished successfully!");
        log("Initializing finished successfully!");
    }
    
    /**
     * This is the method where the task of class loading
     * is delegated to our custom loader.
     *
     * @param  name The name of the class
     * @return The resulting <code>Class</code> object
     * @exception ClassNotFoundException If the class could not be found
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        if (m_cms == null) throw new ClassNotFoundException(name);
        if (DEBUG) System.err.println("FlexClassLoader: Looking for class " + name);
        
        Class c = null;
        
       // Search for class in CMS repositories
        java.util.Enumeration allRepositories = m_repository.elements();
        String filename = null;
        byte[] myClassData = null;
        
        while((allRepositories.hasMoreElements()) && (myClassData == null)) {
            filename = (String)allRepositories.nextElement();

            if(filename.endsWith("/")) {
                String classname = name.replace('.', '/');
                filename = filename + classname + ".class";
            }
                    
            // Try to load from the virtual OpenCms filesystem
            try {
                com.opencms.file.CmsFile file = m_cms.readFile(filename);
                myClassData = file.getContents();
                if (DEBUG) System.err.println("FlexClassLoader: Found class " + filename);
            } catch(Exception e) {
                myClassData = null;
            }
        }

        if(myClassData == null) {
            // Class not found
            throw new ClassNotFoundException(name);
        } else {        
            // Class data successfully read. Now define a new class using this data
            try {
                c = defineClass(null, myClassData, 0, myClassData.length);
            }
            catch(ClassFormatError e) {
                throw new ClassNotFoundException(filename + " seems to be no class file. Sorry.");
            }
            catch(Exception e) {
                throw new ClassNotFoundException(e.toString());
            }
            catch(Error e) {
                throw new ClassNotFoundException("Something really bad happened while loading class " + name + ": " + e);
            }
        }
        log("Successfully loaded class " + name);
        return c;
    }    
    
    /**     
     * Logs a message to the OpenCms log in the channel "flex_classloader".
     *
     * @param message The string to write in the log file
     */        
    private void log(String message) {
        if (com.opencms.boot.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
            com.opencms.boot.CmsBase.log(com.opencms.boot.CmsBase.C_FLEX_CLASSLOADER, "[CmsFlexClassLoader] " + message);
        }
    }        
}
