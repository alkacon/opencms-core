
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
* It is used to read a requested resource from the OpenCms System and forward it to 
* a launcher, which is performs the output of the requested resource. <br>
* 
* The OpenCms class is independent of access module to the OpenCms (e.g. Servlet,
* Command Shell), therefore this class is <b>not</b> responsible for user authentification.
* This is done by the access module to the OpenCms.
*  
* @author Michael Emmerich
* @author Alexander Lucas
* @version $Revision: 1.15 $ $Date: 2000/01/24 19:13:04 $  
* 
*/

class OpenCms extends A_OpenCms implements I_CmsConstants, I_CmsLogChannels 
{

    /**
     * Definition of the index page
     */
    private static String C_INDEX ="index.html";
    
    /**
     * The default mimetype
     */
     //private static String C_DEFAULT_MIMETYPE="application/octet-stream";
    private static String C_DEFAULT_MIMETYPE="text/html";
     
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
      * Hashtable with all available Mimetypes.
      */
     private Hashtable m_mt=new Hashtable();
     
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
            // initalize the Hashtable with all available mimetypes
            m_mt=cms.readMimeTypes();
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
        try {
            //read the requested file
            file =cms.readFile(cms.getRequestContext().getUri());
        } catch (CmsException e ) {
            if (e.getType() == CmsException.C_NOT_FOUND) {
                
                // there was no file found with this name. 
                // it is possible that the requested resource was a folder, so try to access an
                // index.html there
                String resourceName=cms.getRequestContext().getUri();
                // test if the requested file is already the index.html
                if (!resourceName.endsWith(C_INDEX)) {
                    // check if the requested file ends with an "/"
                    if (!resourceName.endsWith("/")) {
                       resourceName+="/";
                     }
                     //redirect the request to the index.html
                    resourceName+=C_INDEX;
                    cms.getRequestContext().getResponse().sendCmsRedirect(resourceName);
                } else {
                    // throw the CmsException.
                    throw e;
                }
           } else {
               // throw the CmsException.
              throw e;
          }
        }
        if (file != null) {
            // test if this file is only available for internal access operations
            if ((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) >0) {
            throw new CmsException (CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]+cms.getRequestContext().getUri(),
                                    CmsException.C_INTERNAL_FILE);
            }
        }
            
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
        String startTemplateClass = file.getLauncherClassname();
        I_CmsLauncher launcher = m_launcherManager.getLauncher(launcherId);
        if(launcher == null) {
            String errorMessage = "Could not launch file " + file.getName() 
                + ". Launcher for requested launcher ID " + launcherId + " could not be found.";
            if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[OpenCms] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
        launcher.initlaunch(cms, file, startTemplateClass);
    }
        
    /**
     * Sets the mimetype of the response.<br>
     * The mimetype is selected by the file extension of the requested document.
     * If no available mimetype is found, it is set to the default 
     * "application/octet-stream".
     * 
     * @param cms The actual OpenCms object.
     * @param file The requested document.
     * 
     */
    void setResponse(A_CmsObject cms, CmsFile file){
        String ext=null;
        String mimetype=null;
        int lastDot=file.getName().lastIndexOf(".");
        // check if there was a file extension
        if ((lastDot>0) && (!file.getName().endsWith("."))){
         ext=file.getName().substring(lastDot+1,file.getName().length());   
         mimetype=(String)m_mt.get(ext);
         // was there a mimetype fo this extension?
         if (mimetype != null) {
             cms.getRequestContext().getResponse().setContentType(mimetype);
         } else {
             cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
         }
        } else {
             cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
        }
    }
    
}