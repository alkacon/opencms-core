/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/A_OpenCms.java,v $
 * Date   : $Date: 2000/08/02 13:34:53 $
 * Version: $Revision: 1.9 $
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

package com.opencms.core;

import java.io.*;
import java.util.*;

import com.opencms.file.*;
import com.opencms.launcher.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

/**
* Abstract class for the main class of the OpenCms system. 
* <p>
* It is used to read a requested resource from the OpenCms System and forward it to 
* a launcher, which is performs the output of the requested resource. <br>
* 
* The OpenCms class is independent of access module to the OpenCms (e.g. Servlet,
* Command Shell), therefore this class is <b>not</b> responsible for user authentification.
* This is done by the access module to the OpenCms.
*   
* @author Alexander Lucas
* @author Michael Emmerich
* @version $Revision: 1.9 $ $Date: 2000/08/02 13:34:53 $  
* 
*/
public abstract class A_OpenCms implements I_CmsLogChannels {

    /** Reference to the system log */
    private static CmsLog c_cmsLog = null;

    /** Indicates if the system log is initialized */
    protected static boolean c_servletLogging = false;
    
    /**
     * This method gets the requested document from the OpenCms and returns it to the 
     * calling module.
     * 
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
     abstract CmsFile initResource(CmsObject cms) 
        throws CmsException, IOException;
     
     /**
     * Selects the appropriate launcher for a given file by analyzing the 
     * file's launcher id and calls the initlaunch() method to initiate the 
     * generating of the output.
     * 
     * @param cms CmsObject containing all document and user information
     * @param file CmsFile object representing the selected file.
     * @exception CmsException
     */
    abstract public void showResource(CmsObject cms, CmsFile file) throws CmsException;
    
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
    abstract void setResponse(CmsObject cms, CmsFile file);
    
    
     /**
     * Destructor, called when the the servlet is shut down.
     */
    abstract void destroy()
          throws CmsException;
    
        
    /**
     * Initializes the logging mechanism of the Jserv
     * @param configurations the configurations needed at initialization.
     */
    public static void initializeServletLogging(Configurations config) {
        c_cmsLog = new CmsLog("log", config);
        c_servletLogging = true;
    }
    
    /**
     * Checks if the system logging is active.
     * @return <code>true</code> if the logging is active, <code>false</code> otherwise.
     */
    public static boolean isLogging() {
        if(c_servletLogging) {
            return c_cmsLog.isActive();
        } else {
            return true;
        }
    }
    
    /**
     * Logs a message into the OpenCms logfile.
     * If the logfile was not initialized (e.g. due tue a missing
     * ServletConfig while working with the console)
     * any log output will be written to the apache error log.
     * @param channel The channel the message is logged into
     * @message The message to be logged,
     */
    public static void log(String channel, String message) {
        if(c_servletLogging) {
            c_cmsLog.log(channel, message);
        } else {            
            System.err.println(message);
        }
    }
	
	/**
	 * Inits a new user and sets it into the overgiven cms-object.
	 * 
	 * @param cms the cms-object to use.
	 * @param cmsReq the cms-request for this http-request.
	 * @param cmsRes the cms-response for this http-request.
	 * @param user The name of the user to init.
	 * @param group The name of the current group.
	 * @param project The id of the current project.
	 */
	abstract public void initUser(CmsObject cms,I_CmsRequest cmsReq,I_CmsResponse cmsRes,String user,String group,int project)
		throws CmsException;
	
	/**
	 * This method loads old sessiondata from the database. It is used 
	 * for sessionfailover.
	 * 
	 * @param oldSessionId the id of the old session.
	 * @return the old sessiondata.
	 */
	abstract Hashtable restoreSession(String oldSessionId) 
		throws CmsException;

	/**
	 * This method stores sessiondata into the database. It is used 
	 * for sessionfailover.
	 * 
	 * @param sessionId the id of the session.
	 * @param isNew determines, if the session is new or not.
	 * @return data the sessionData.
	 */
	abstract void storeSession(String sessionId, Hashtable sessionData) 
		throws CmsException;
}
