
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsLauncherManager.java,v $
* Date   : $Date: 2001/01/24 09:42:27 $
* Version: $Revision: 1.13 $
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

package com.opencms.launcher;

import com.opencms.core.*;
import java.util.*;

/**
 * Collects all available lauchners at startup and provides
 * a method for looking up the appropriate launcher class for a
 * given launcher id.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.13 $ $Date: 2001/01/24 09:42:27 $
 */
public class CmsLauncherManager implements I_CmsLogChannels {
    
    /**
     * Static array of all known launcher.
     * Not all of these launchers must be integrated into the OpenCms system.
     * Only really relevant launchers are required.
     */
    private static final String[] C_KNOWN_LAUNCHERS =  {
        "CmsXmlLauncher", "CmsDumpLauncher", "CmsLinkLauncher", 
        "CmsPdfLauncher"
    };
    
    /**
     * Hashtable to store instances of all launchers.
     * So they can be re-used and only have to be instantiated
     * once at startup
     */
    private Hashtable launchers;
    
    /**
     * Constructor for building a new launcher manager.
     * Uses the C_KNOWN_LAUNCHERS array to scan for all available
     * launchers.
     * <P>
     * When updating to Java 1.2 this should be done by using
     * the <code>Package</code> class. Then, C_KNOWN_LAUNCHERS is
     * not needed any more.
     * 
     * @exception CmsException
     */
    public CmsLauncherManager() throws CmsException {
        String launcherPackage = getLauncherPackage();
        Class launcherClass = null;
        I_CmsLauncher launcherInstance = null;
        Integer launcherId = null;
        
        // Initialize Hashtable
        launchers = new Hashtable();
        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsLauncherManager] launcher package is:" + launcherPackage);
        
        // try to load launcher classes.
        for(int i = 0;i < C_KNOWN_LAUNCHERS.length;i++) {
            try {
                launcherClass = Class.forName(launcherPackage + "." + C_KNOWN_LAUNCHERS[i]);
                launcherInstance = (I_CmsLauncher)launcherClass.newInstance();
            }
            catch(Throwable e) {
                if(e instanceof ClassNotFoundException) {
                    
                    // The launcher class could not be loaded.                    
                    // This is no critical error.                    
                    // We assume the launcher should not be integrated into the OpenCms system.
                    if(A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] OpenCms launcher \"" + C_KNOWN_LAUNCHERS[i] + "\" not found. Ignoring.");
                    }
                    continue;
                }
                else {
                    if(e instanceof ClassCastException) {
                        
                        // The launcher could be loaded but doesn't implement the interface                        
                        // I_CmsLauncher.                        
                        // So this class is anything, but NOT a OpenCms launcher.                        
                        // We have to stop the system.
                        String errorMessage = "Loaded launcher class \"" + C_KNOWN_LAUNCHERS[i] + "\" is no OpenCms launcher (does not implement I_CmsLauncher). Ignoring";
                        if(A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] " + errorMessage);
                        }
                        continue;
                    }
                    else {
                        String errorMessage = "Unknown error while initializing launcher \"" + C_KNOWN_LAUNCHERS[i] + "\". Ignoring.";
                        if(A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] " + errorMessage);
                            A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] " + e);
                        }
                        continue;
                    }
                }
            }
            
            // Now the launcher class was loaded successfully.            
            // Let's check the launcher ID
            launcherId = new Integer(launcherInstance.getLauncherId());
            if(launchers.containsKey(launcherId)) {
                String errorMessage = "Duplicate launcher ID " + launcherId + " in launcher \"" + C_KNOWN_LAUNCHERS[i] + "\".";
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] " + errorMessage);
                }
                throw new CmsException(errorMessage, CmsException.C_LAUNCH_ERROR);
            }
            
            // Now everything is fine.            
            // We can store the launcher in our Hashtable.
            launchers.put(launcherId, launcherInstance);
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] OpenCms launcher \"" + C_KNOWN_LAUNCHERS[i] + "\" with launcher ID " + launcherId + " loaded successfully.");
            }
        }
    }
    
    /**
     * Clears all launchercaches.
     * @author Finn Nielsen
     * Creation date: (10/23/00 13:28:38)
     */
    public void clearCaches() {
        for(Enumeration e = launchers.elements();e.hasMoreElements();) {
            I_CmsLauncher l = (I_CmsLauncher)e.nextElement();
            l.clearCache();
        }
    }
    
    /**
     * Looks up the appropriate launcher class instance for the given 
     * launcher id in the internal hashtable.
     * @return I_CmsLauncher object for the requested launcher id.
     */
    public I_CmsLauncher getLauncher(int launcherId) {
        return (I_CmsLauncher)launchers.get(new Integer(launcherId));
    }
    
    /**
     * Gets the name of the own package.
     * Needed to look up all available launchers.
     * @return Name of the package this class belongs to. 
     */
    private String getLauncherPackage() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(0, fullClassName.lastIndexOf("."));
    }
}
