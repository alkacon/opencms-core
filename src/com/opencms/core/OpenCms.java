
package com.opencms.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.file.*;
import com.opencms.launcher.*;


/**
* This class is the main class of the OpenCms system. 
* <p>
* 
* @author Michael Emmerich
* @author Alexander Lucas
* @version $Revision: 1.9 $ $Date: 2000/01/13 18:02:16 $  
* 
*/

class OpenCms implements I_CmsConstants, I_CmsLogChannels 
{
     /**
      * The session storage for all active users.
      */
     private CmsSession m_sessionStorage;
 
     /**
      * The reference to the resource broker
      */
     private I_CmsResourceBroker m_rb;

     /**
      * Reference to the OpenCms launcer manager
      */
     private CmsLauncherManager m_launcherManager;
     
     /**
      * Constructor, creates a new OpenCms object.
      * It connects to the poerty database to read all requred data to set up the
      * OpenCms system and creates an initalizer object which initiates all requires
      * access modules and resource brokers.
      * 
      * @param driver The database driver for the property database.
      * @param connect The connect string to the property database.
      * @param classname The name of the initalizer class. 
      */
     OpenCms(String driver, String connect, String classname) {
        // invoke the ResourceBroker via the initalizer
        try {
  		    m_rb = ((A_CmsInit) Class.forName(classname).newInstance() ).init(driver, connect);
            CmsObject cms=new CmsObject();
            cms.init(m_rb);
        } catch (Exception e) {
            System.err.println(e.getMessage());    
        }
        
        // try to initialize the launchers.
        try {
            m_launcherManager = new CmsLauncherManager();
        } catch (Exception e) {
            System.err.println(e.getMessage());    
        }            
     }
     
     /**
     * This method gets the requested document from the OpenCms and returns it to the 
     * calling module.
     * 
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
     CmsFile initResource(CmsObject cms) 
        throws CmsException, IOException {
          
        CmsFile file=null;
 
        //read the requested file
        file =cms.readFile(cms.getRequestContext().currentUser(),
                           cms.getRequestContext().currentProject(),
                           cms.getRequestContext().getUri());
        if (file != null) {
        // test if this file is only available for internal access operations
        if ((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) >0) {
            throw new CmsException (CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]+cms.getRequestContext().getUri(),
                                    CmsException.C_INTERNAL_FILE);
        }}
        return file;
    }

     
    /**
     * Selects the appropriate launcher for a given file by analyzing the 
     * file's launcher id and calls the initlaunch() method to initiate the 
     * generating of the output.
     * 
     * @param cms A_CmsObject containing all document and user information
     * @param file CmsFile object representing the selected file.
     * @exception CmsException
     */
    void showResource(A_CmsObject cms, CmsFile file) throws CmsException { 
        int launcherId = file.getLauncherType();
        I_CmsLauncher launcher = m_launcherManager.getLauncher(launcherId);
        if(launcher == null) {
            String errorMessage = "Could not launch file " + file.getName() 
                + ". Launcher for requested launcher ID " + launcherId + " could not be found.";
            if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[OpenCms] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
        launcher.initlaunch(cms, file);
    }
}