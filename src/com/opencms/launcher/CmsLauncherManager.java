/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsLauncherManager.java,v $
* Date   : $Date: 2003/07/12 11:29:22 $
* Version: $Revision: 1.26 $
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

package com.opencms.launcher;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.*;
import com.opencms.file.*;
import java.util.*;

/**
 * Collects all available lauchners at startup and provides
 * a method for looking up the appropriate launcher class for a
 * given launcher id.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.26 $ $Date: 2003/07/12 11:29:22 $
 */
public class CmsLauncherManager implements I_CmsLogChannels {

    /**
     * Static array of all known launcher.
     * Not all of these launchers must be integrated into the OpenCms system.
     * Only really relevant launchers are required.
     */
    //private static final String[] C_KNOWN_LAUNCHERS =  {
    //    "CmsXmlLauncher", "CmsDumpLauncher", "CmsLinkLauncher",
    //    "CmsPdfLauncher"
    //};

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
     * @throws CmsException
     */
    public CmsLauncherManager(OpenCms cms) throws CmsException {
        String launcherPackage = getLauncherPackage();
        String launcherName = new String();
        Class launcherClass = null;
        I_CmsLauncher launcherInstance = null;
        Integer launcherId = null;

        Hashtable knownLaunchers = getKnownLaunchers();
        // Initialize Hashtable
        launchers = new Hashtable();
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT) ) {
            A_OpenCms.log(C_OPENCMS_INIT, ". Launcher package     : " + launcherPackage);
        }

        // try to load launcher classes.
        for(int i = 1;i <= knownLaunchers.size();i++) {
            try {
                launcherName = (String)knownLaunchers.get(I_CmsConstants.C_REGISTRY_LAUNCHER+i);
                launcherClass = Class.forName(launcherName);
                launcherInstance = (I_CmsLauncher)launcherClass.newInstance();
                launcherInstance.setOpenCms(cms);
            }
            catch(Throwable e) {
                if(e instanceof ClassNotFoundException) {

                    // The launcher class could not be loaded.
                    // This is no critical error.
                    // We assume the launcher should not be integrated into the OpenCms system.
                    if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL) ) {
                        A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsLauncherManager] OpenCms launcher \"" + launcherName + "\" not found. Ignoring.");
                    }
                    continue;
                }
                else {
                    if(e instanceof ClassCastException) {

                        // The launcher could be loaded but doesn't implement the interface
                        // I_CmsLauncher.
                        // So this class is anything, but NOT a OpenCms launcher.
                        // We have to stop the system.
                        String errorMessage = "Loaded launcher class \"" + launcherName + "\" is no OpenCms launcher (does not implement I_CmsLauncher). Ignoring";
                        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL) ) {
                            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsLauncherManager] " + errorMessage);
                        }
                        continue;
                    }
                    else {
                        String errorMessage = "Unknown error while initializing launcher \"" + launcherName + "\". Ignoring.";
                        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL) ) {
                            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsLauncherManager] " + errorMessage);
                            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsLauncherManager] " + e);
                        }
                        continue;
                    }
                }
            }

            // Now the launcher class was loaded successfully.
            // Let's check the launcher ID
            launcherId = new Integer(launcherInstance.getLauncherId());
            if(launchers.containsKey(launcherId)) {
                String errorMessage = "Duplicate launcher ID " + launcherId + " in launcher \"" + launcherName + "\".";
                if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL) ) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsLauncherManager] " + errorMessage);
                }
                throw new CmsException(errorMessage, CmsException.C_LAUNCH_ERROR);
            }

            // Now everything is fine.
            // We can store the launcher in our Hashtable.
            launchers.put(launcherId, launcherInstance);
            if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT) ) {
                A_OpenCms.log(C_OPENCMS_INIT, ". Launcher loaded      : " + launcherName + " with id " + launcherId);
            }
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
     * Clears all launchercaches.
     */
    public void clearCaches() {
        for(Enumeration e = launchers.elements();e.hasMoreElements();) {
            I_CmsLauncher l = (I_CmsLauncher)e.nextElement();
            l.clearCache();
        }
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

    /**
     * Returns the known launchers configured in the registry,
     * used to look up all available launchers.<p>
     * 
     * @return Hashtable with the known launchers
     */
    private Hashtable getKnownLaunchers() {
        Hashtable knownLaunchers = new Hashtable();
        try{
            I_CmsRegistry reg = OpenCms.getRegistry();
            knownLaunchers = reg.getSystemValues(I_CmsConstants.C_REGISTRY_KNOWNLAUNCHERS);
        } catch (CmsException exc){
            A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] error getKnownLaunchers: "+exc.getMessage());
        } catch (Exception e){
            A_OpenCms.log(C_OPENCMS_INIT, "[CmsLauncherManager] error getKnownLaunchers: "+e.getMessage());
        }
        return knownLaunchers;
    }
}
