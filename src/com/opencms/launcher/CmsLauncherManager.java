package com.opencms.launcher;

import com.opencms.core.*;
import java.util.*;

public class CmsLauncherManager implements I_CmsLogChannels {
       
    private static final String[] C_KNOWN_LAUNCHERS = {
        "CmsXmlLauncher",
        "CmsDummyLauncher",
        "CmsDumpLauncher"};
    
    private Hashtable launchers;
    
    public CmsLauncherManager() throws CmsException {
        String launcherPackage = getLauncherPackage();
        Class launcherClass = null;
        I_CmsLauncher launcherInstance = null;
        Integer launcherId = null;
        
        // Initialize Hashtable
        launchers = new Hashtable();
        
        // try to load launcher classes.
        for(int i=0; i<C_KNOWN_LAUNCHERS.length; i++) {
            try {
                launcherClass = Class.forName(launcherPackage + "." + C_KNOWN_LAUNCHERS[i]);                          
                launcherInstance = (I_CmsLauncher)launcherClass.newInstance();
            } catch(Exception e) {
                if(e instanceof ClassNotFoundException) {
                    // The launcher class could not be loaded.
                    // This is no critical error.
                    // We assume the launcher should not be integrated into the OpenCms system.
                    if(A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[CmsLauncherManager] OpenCms launcher " + C_KNOWN_LAUNCHERS[i] + " not found. Ignoring.");
                    }
                    continue;
                } else if(e instanceof ClassCastException) {
                    // The launcher could be loaded but doesn't implement the interface
                    // I_CmsLauncher.
                    // So this class is anything, but NOT a OpenCms launcher.
                    // We have to stop the system.
                    String errorMessage = "Loaded launcher class \"" + C_KNOWN_LAUNCHERS[i] + "\" is no OpenCms launcher (does not implement I_CmsLauncher).";
                    if(A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[CmsLauncherManager] " + errorMessage);
                    }
                    throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
                } else {
                    String errorMessage = "Unknown error while initializing launcher \"" + C_KNOWN_LAUNCHERS[i] + "\". " + e.toString();
                    if(A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[CmsLauncherManager] " + errorMessage);
                    }
                    throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
                }                
            }
            
            // Now the launcher class was loaded successfully.
            // Let's check the launcher ID
            
            launcherId = new Integer(launcherInstance.getLauncherId());
            if(launchers.containsKey(launcherId)) {
                String errorMessage = "Duplicate launcher ID " + launcherId + " in launcher \"" + C_KNOWN_LAUNCHERS[i] + "\".";
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsLauncherManager] " + errorMessage);
                }
                throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
            }
            
            // Now everything is fine.
            // We can store the launcher in our Hashtable.
            launchers.put(launcherId, launcherInstance);
            if(A_OpenCms.isLogging()) {                
                A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsLauncherManager] OpenCms launcher \"" + C_KNOWN_LAUNCHERS[i] 
                        + "\" with launcher ID " + launcherId + " loaded successfully."); 
            }
        }
    }

    public I_CmsLauncher getLauncher(int launcherId) {
        return (I_CmsLauncher)launchers.get(new Integer(launcherId));
    }
    
    private String getLauncherPackage() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(0, fullClassName.lastIndexOf("."));
    }
}
