/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/A_OpenCms.java,v $
* Date   : $Date: 2002/09/11 13:31:59 $
* Version: $Revision: 1.25 $
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

package com.opencms.core;

import java.io.*;
import java.util.*;
import com.opencms.boot.*;
import com.opencms.file.*;
import com.opencms.template.cache.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.flex.*;

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
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.25 $ $Date: 2002/09/11 13:31:59 $
 *
 */
public abstract class A_OpenCms implements I_CmsLogChannels {

    private static String m_logfile;

    /** List to save the event listeners in */
    private static java.util.ArrayList m_listeners = new ArrayList();
            
    /** A Map for the storage of various runtime properties */
    private Map m_runtimeProperties = null;

    /** The runtime configuration */
    private Configurations m_conf = null;
    
    /** Default encoding, can be overwritten in "opencms.properties" */
    private static String m_defaultEncoding = "UTF-8";    
    
    /**
     * Destructor, called when the the servlet is shut down.
     */
    abstract void destroy() throws CmsException;

    /**
     * Initializes the logging mechanism of the Jserv
     * @param configurations the configurations needed at initialization.
     */
    public static void initializeServletLogging(Configurations config) {
        m_logfile = config.getString("log.file");
        CmsBase.initializeServletLogging(config);
    }

    /**
     * Returns the name of the logfile.
     */
    public static String getLogFileName() {
        return m_logfile;
    }

    /**
     * This method gets the requested document from the OpenCms and returns it to the
     * calling module.
     *
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
    abstract CmsFile initResource(CmsObject cms) throws CmsException,IOException;

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
    abstract public void initUser(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes,
        String user, String group, int project, CmsCoreSession sessionStorage) throws CmsException;

    /**
     * Checks if the system logging is active.
     * @return <code>true</code> if the logging is active, <code>false</code> otherwise.
     */
    public static boolean isLogging() {
        return CmsBase.isLogging();    
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
        CmsBase.log(channel, message);
    }

    /**
     * This method loads old sessiondata from the database. It is used
     * for sessionfailover.
     *
     * @param oldSessionId the id of the old session.
     * @return the old sessiondata.
     */
    abstract Hashtable restoreSession(String oldSessionId) throws CmsException;

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
     * This method stores sessiondata into the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @param isNew determines, if the session is new or not.
     * @return data the sessionData.
     */
    abstract void storeSession(String sessionId, Hashtable sessionData) throws CmsException;

    /**
     * Reads the actual entries from the database and updates the Crontable
     */
    abstract void updateCronTable();

    /**
     * Starts a schedule job with a correct instantiated CmsObject.
     * @param entry the CmsCronEntry to start.
     */
    abstract void startScheduleJob(CmsCronEntry entry);
    
    /**       
     * This method adds an Object to the OpenCms runtime properties.
     * The runtime properties can be used to store Objects that are shared
     * in the whole system.<p>
     *
     * @since FLEX alpha 1
     * @param key The key to add the Object with.
     * @param value The value of the Object to add.
     */
    public void setRuntimeProperty(Object key, Object value) {
        if (m_runtimeProperties == null) {
            m_runtimeProperties = Collections.synchronizedMap(new HashMap());
        }
        m_runtimeProperties.put(key, value);
    }
    
    /** This method looks up a value in the runtime property Map.
     *
     * @since FLEX alpha 1
     * @param key The key to look up in the runtime properties.
     * @return The value for the key, or null if the key was not found.
     */
    public Object getRuntimeProperty(Object key) {
        if (m_runtimeProperties == null) return null;
        return m_runtimeProperties.get(key);
    }
    
    /** This method returns the complete runtime property Map.
     *
     * @since FLEX alpha 1
     * @return The Map of runtime properties.
     */    
    public Map getRuntimePropertyMap() {
        return m_runtimeProperties;
    }

    /** This method sets the runtime configuration.
     *
     * @since FLEX alpha 1
     */       
    public void setConfiguration(Configurations conf) {
        m_conf = conf;
    }    
    
    /** This method returns the runtime configuration.
     *
     * @since FLEX alpha 1
     * @return The runtime configuration.
     */       
    public Configurations getConfiguration() {
        return m_conf;
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @since FLEX alpha 1
     * @param cms An initialized CmsObject
     * @param type Event type
     * @param data Event data
     */
    public static void fireCmsEvent(CmsObject cms, int type, java.util.Map data) {
       OpenCms.fireCmsEvent( new CmsEvent(cms, type, data) );
    }    
    
    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @since FLEX beta 1
     * @param cms An initialized CmsObject
     * @param event A CmsEvent
     */
    public static void fireCmsEvent(CmsEvent event) {
        if (m_listeners.size() < 1)
            return;
        I_CmsEventListener list[] = new I_CmsEventListener[0];
        synchronized (m_listeners) {
            list = (I_CmsEventListener[]) m_listeners.toArray(list);
        }
        for (int i = 0; i < list.length; i++)
            ((I_CmsEventListener) list[i]).cmsEvent(event);
    }    
    
    /**
     * Add a cms event listener.
     *
     * @since FLEX alpha 1
     * @param listener The listener to add
     */
    public static void addCmsEventListener(I_CmsEventListener listener) {
        synchronized (m_listeners) {
            m_listeners.add(listener);
        }        
    }    
    
    /**
     * Remove a cms event listener.
     *
     * @since FLEX alpha 1
     * @param listener The listener to add
     */
    public static void removeCmsEventListener(I_CmsEventListener listener) {
        synchronized (m_listeners) {
            m_listeners.remove(listener);
        }
    }
    
    /**
     * Return the OpenCms default character encoding.
     * The default is set in the "opencms.properties" file.
     * If this is not set in "opencms.properties" the default 
     * is "UTF-8". 
     * 
     * @return The default encoding (e.g. "UTF-8")
     */
    public static String getDefaultEncoding() {
        return m_defaultEncoding;
    }    
    
    /**
     * Sets the default encoding to the value specified.
     * 
     * @param encoding The value to set, e.g. "UTF-8" or "ISO-8859-1".
     */
    protected void setDefaultEncoding(String encoding) {
        m_defaultEncoding = encoding;
    }


    abstract public void initStartupClasses() throws CmsException;    
}
