
package com.opencms.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.file.*;


/**
* This class is the main class of the OpenCms system. 
* <p>
* 
* @author Michael Emmerich
* @version $Revision: 1.5 $ $Date: 2000/01/12 16:38:14 $  
* 
*/

class OpenCms implements I_CmsConstants 
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
     }
     
     /**
     * This method gets the requested document from the OpenCms and forwards it to the
     * correct launcher.
     * 
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
     CmsFile initResource(CmsObject cms) 
        throws CmsException, IOException {
        // this method still contains debug information
        
        CmsFile file=null;
         
        //read the requested file
        file =cms.readFile(cms.getRequestContext().currentUser(),
                           cms.getRequestContext().getCurrentProject(),
                           cms.getRequestContext().getUri());
        if (file != null) {
        // test if this file is only available for internal access operations
        if ((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) >0) {
            throw new CmsException (CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]+cms.getRequestContext().getUri(),
                                    CmsException.C_INTERNAL_FILE);
        }}
        return file;
    }
    
}