/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCmsCore.java,v $
 * Date   : $Date: 2003/11/13 16:32:30 $
 * Version: $Revision: 1.49 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.cron.CmsCronEntry;
import org.opencms.cron.CmsCronScheduleJob;
import org.opencms.cron.CmsCronScheduler;
import org.opencms.cron.CmsCronTable;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsDriverManager;
import org.opencms.flex.CmsFlexCache;
import org.opencms.loader.CmsJspLoader;
import org.opencms.loader.CmsLoaderManager;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.I_CmsDialogHandler;

import com.opencms.boot.CmsBase;
import com.opencms.boot.CmsMain;
import com.opencms.boot.CmsSetupUtils;
import com.opencms.core.CmsCoreSession;
import com.opencms.core.CmsException;
import com.opencms.core.CmsRequestHttpServlet;
import com.opencms.core.CmsResponseHttpServlet;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsRequest;
import com.opencms.core.I_CmsResourceInit;
import com.opencms.core.I_CmsResponse;
import com.opencms.core.OpenCmsServletNotify;
import com.opencms.core.exceptions.CmsResourceInitException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRegistry;
import com.opencms.file.CmsResource;
import com.opencms.util.Utils;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * This class is the main class of the OpenCms system,
 * think of it as the "operating system" of OpenCms.<p>
 *  
 * Any request to an OpenCms resource will be processed by this class first.
 * The class will try to map the request to a VFS (Virtual File System) resource,
 * i.e. an URI. If the resource is found, it will be read and forwarded to
 * to a resource loader, which then genertates the output of the requested resource.<p>
 *
 * There will be only one instance of this object created for
 * any accessing class. This means that in the default configuration, where 
 * OpenCms is accessed through a servlet context, there will be only one instance of 
 * this class in that servlet context.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.49 $
 * @since 5.1
 */
public final class OpenCmsCore {
    
    /** Default encoding */
    private static final String C_DEFAULT_ENCODING = "ISO-8859-1";
  
    /** The default mimetype */
    private static final String C_DEFAULT_MIMETYPE = "text/html";

    /** Static version name to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NAME = "Salusa Secundus";

    /** Static version number to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NUMBER = "5.1.x";
    
    /** Prefix for error messages for initialization errors */
    private static final String C_ERRORMSG = "OpenCms initialization error!\n\n";     
    
    public static final String C_MSG_CRITICAL_ERROR = "Critical init error/";
    
    /** One instance to rule them all, one instance to find them... */        
    private static OpenCmsCore m_instance;
    
    /** URI of the authentication form (read from properties) in case of form based authentication */
    private String m_authenticationFormURI;    

    /** The OpenCms application base path */
    private String m_basePath;

    /** Member variable to store instances to modify resources */
    private List m_checkFile;

    /** The OpenCms configuration read from <code>opencms.properties</code> */
    private ExtendedProperties m_configuration;
    
    /** Default encoding, can be overwritten in "opencms.properties" */
    private String m_defaultEncoding;
    
    /** Array of configured default file names (for faster access) */
    private String[] m_defaultFilenames;

    /** The default user and group names */
    private CmsDefaultUsers m_defaultUsers;

    /** Directory translator, used to translate all access to resources */
    private CmsResourceTranslator m_directoryTranslator;

    /** The driver manager to access the database */
    private CmsDriverManager m_driverManager;

    /** The static export manager */
    private CmsStaticExportManager m_exportProperties;
    
    /** The cron manager */
    // TODO enable the cron manager
    //private CmsCronManager m_cronManager;

    /** Filename translator, used only for the creation of new files */
    private CmsResourceTranslator m_fileTranslator;
    
    /** The link manager to resolve links in &lt;link&gt; tags */
    private CmsLinkManager m_linkManager;    

    /** List to save the event listeners in */
    private Map m_listeners;

    /** The loader manager used for loading individual resources */
    private CmsLoaderManager m_loaderManager;

    /** The OpenCms log to write all log messages to */
    private CmsLog m_log;

    /** The filename of the log file */
    private String m_logfile;

    /** The memory monitor for collection memory statistics */
    private CmsMemoryMonitor m_memoryMonitor;
    
    /** The OpenCms map of configured mime types */
    private Map m_mimeTypes;

    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code> */
    private String m_openCmsContext;

    /** The name of the class used to validate a new password */
    private String m_passwordValidatingClass;
    
    /** Map of request handlers */
    private Map m_requestHandlers;

    /** The runlevel of this OpenCmsCore object instance */
    private int m_runLevel;

    /** A Map for the storage of various runtime properties */
    private Map m_runtimeProperties;

    /**  The cron scheduler to schedule the cronjobs */
    private CmsCronScheduler m_scheduler;
    
    /** The name of the OpenCms server */
    private String m_serverName;

    /** The session manager */
    private OpenCmsSessionManager m_sessionManager;
    
    /** The session storage for all active users */
    private CmsCoreSession m_sessionStorage;
    
    /** The site manager contains information about all configured sites */
    private CmsSiteManager m_siteManager;
    
    /** Flag to indicate if the startup classes have already been initialized */
    private boolean m_startupClassesInitialized;

    /** The cron table to use with the scheduler */
    private CmsCronTable m_table;
    
    /** The Thread store */
    private CmsThreadStore m_threadStore;
    
    /** Flag to indicate if basic or form based authentication is used */
    private boolean m_useBasicAuthentication;

    /** The default setting for the user access flags */
    private int m_userDefaultaccessFlags;

    /** The default setting for the user language */
    private String m_userDefaultLanguage;

    /** The version name (including version number) of this OpenCms installation */
    private String m_versionName;

    /** The version number of this OpenCms installation */
    private String m_versionNumber;

    /**
     * Protected constructor that will initialize the singleton OpenCms instance with runlevel 1.<p>
     * @throws CmsInitException in case of errors during the initialization
     */
    private OpenCmsCore() throws CmsInitException {
        synchronized (this) {
            if (m_instance != null && (m_instance.getRunLevel() > 0)) {
                throw new CmsInitException("OpenCms already initialized!");
            } 
            m_instance = this;
            initMembers();
            setRunLevel(1);
        }
    }
        
    /**
     * Returns the initialized OpenCms instance.<p>
     * 
     * @return the initialized OpenCms instance
     * @throws RuntimeException in case the OpenCms instance was not properly initialized
     */
    public static synchronized OpenCmsCore getInstance() {
        if (m_instance == null) {
            try {
                // create a new core object with runlevel 1
                new OpenCmsCore();
            } catch (CmsInitException e) {
                // already initialized, this all we need
            }
        }
        return m_instance;
    }
    
    /**
     * Add a cms event listener that listens to all events.<p>
     *
     * @param listener the listener to add
     */
    protected void addCmsEventListener(I_CmsEventListener listener) {
        addCmsEventListener(listener, null);
    }

    /**
     * Add a cms event listener.<p>
     *
     * @param listener the listener to add
     * @param eventTypes the events to listen for
     */
    protected void addCmsEventListener(I_CmsEventListener listener, int[] eventTypes) {
        synchronized (m_listeners) {
            if (eventTypes == null) {
                eventTypes = new int[] {I_CmsEventListener.LISTENERS_FOR_ALL_EVENTS.intValue()};                
            }
            for (int i = 0; i < eventTypes.length; i++) {
                Integer eventType = new Integer(eventTypes[i]);
                List listeners = (List)m_listeners.get(eventType);
                if (listeners == null) {
                    listeners = new ArrayList();
                    m_listeners.put(eventType, listeners);
                }
                listeners.add(listener);
            }
        }
    }
    
    /**
     * Adds the specified request handler to the Map of OpenCms request handlers.<p>
     * 
     * @param handler the handler to add
     */
    protected void addRequestHandler(I_CmsRequestHandler handler) {
        if (handler == null) {
            return;
        }
        if (m_requestHandlers.get(handler.getHandlerName()) != null) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("Duplicate OpenCms request handler, ignoring '" + handler.getHandlerName() + "'");
            }
            return;
        }
        m_requestHandlers.put(handler.getHandlerName(), handler);
    }
    
    /**
     * Checks if the current request contains http basic authentication information in 
     * the headers, if so tries to log in the user identified.<p>
     *  
     * @param cms the current cms context
     * @param req the current http request
     * @param res the current http response
     * @throws IOException in case of errors reading from the streams
     * @throws CmsException if user information was not correct
     */
    private void checkBasicAuthorization(CmsObject cms, HttpServletRequest req, HttpServletResponse res) throws IOException, CmsException {
        // no user identified from the session and basic authentication is enabled
        String auth = req.getHeader("Authorization");

        // user is authenticated, check password
        if (auth != null) {

            // only do basic authentification
            if (auth.toUpperCase().startsWith("BASIC ")) {

                // Get encoded user and password, following after "BASIC "
                String userpassEncoded = auth.substring(6);

                // Decode it, using any base 64 decoder
                sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
                String userstr = new String(dec.decodeBuffer(userpassEncoded));
                String username = null;
                String password = null;
                StringTokenizer st = new StringTokenizer(userstr, ":");
                if (st.hasMoreTokens()) {
                    username = st.nextToken();
                }
                if (st.hasMoreTokens()) {
                    password = st.nextToken();
                }
                // authentication in the DB
                try {
                    try {
                        // try to login as a user first ...
                        cms.loginUser(username, password);
                    } catch (CmsException exc) {
                        // login as user failed, try as webuser ...
                        cms.loginWebUser(username, password);
                    }
                    // authentification was successful create a session
                    req.getSession(true);
                } catch (CmsSecurityException e) {
                    // authentification failed, so display a login screen
                    requestAuthorization(req, res);
                }
            }
        }        
    }
        
    /**
     * Generates a formated exception output.<p>
     * 
     * Because the exception could be thrown while accessing the system files,
     * the complete HTML code must be added here!<p>
     * 
     * @param t the caught Exception
     * @param title the title to display
     * @return String containing the HTML code of the error message.
     */
    private String createErrorBox(Throwable t, String title) {
        StringBuffer output = new StringBuffer();
        output.append(this.getErrormsg("C_ERRORPART_1"));
        output.append(title);
        output.append(this.getErrormsg("C_ERRORPART_2"));
        output.append("\n\n");
        output.append(Utils.getStackTrace(t));
        output.append("\n\n");
        output.append(this.getErrormsg("C_ERRORPART_3"));
        return output.toString();
    }

    /**
     * Destroys this OpenCms instance.<p> 
     */    
    protected synchronized void destroy() {        
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info("[OpenCms] Performing shutdown ...");
        }
        try {
            m_scheduler.shutDown();
            m_driverManager.destroy();
        } catch (Throwable e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("[OpenCms]" + e.toString());
            }
        }
        try {
            Utils.getModulShutdownMethods(getRegistry());
        } catch (Throwable e) {
            // log exception since we are about to shutdown anyway
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("[OpenCms] Module shutdown exception: " + e);
            }
        }
        if (getLog(this).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info("[OpenCms] ... shutdown completed.");
        }        
        m_instance = null;
    }
     
    /**
     * This method performs the error handling for OpenCms.<p>
     *
     * @param cms the current cms context, might be null !
     * @param req the client request
     * @param res the client response
     * @param t the exception that occured
     */
    private void errorHandling(CmsObject cms, HttpServletRequest req, HttpServletResponse res, Throwable t) {
        
        boolean canWrite = !res.isCommitted() && !res.containsHeader("Location");
        int status = -1;        
        
        boolean isNotGuest = false;
                
        if (t instanceof ServletException) {
            ServletException s = (ServletException)t;
            if (s.getRootCause() != null) {
                t = s.getRootCause();
            }
        }        

        if (t instanceof CmsSecurityException) {
            CmsSecurityException e = (CmsSecurityException)t;

            // access error - display login dialog
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("[OpenCms] Access denied: " + e.getMessage());
            }
            if (canWrite) {
                try {
                    requestAuthorization(req, res);
                } catch (IOException ioe) {
                    // there is nothing we can do about this
                }
                return;
            }
        } else if (t instanceof CmsException) {
            CmsException e = (CmsException)t;

            int exceptionType = e.getType();
            switch (exceptionType) {

                case CmsException.C_NOT_FOUND :
                    // file not found - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    break;

                case CmsException.C_SERVICE_UNAVAILABLE :
                    status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                    break;
                    
                case CmsException.C_NO_USER:
                case CmsException.C_NO_GROUP:
                    status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                    isNotGuest = true;
                    break;

                case CmsException.C_HTTPS_PAGE_ERROR :
                    // http page and https request - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info("Trying to get a http page with a https request", e);
                    }
                    break;                                       

                case CmsException.C_HTTPS_REQUEST_ERROR :
                    // https request and http page - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info("Trying to get a https page with a http request", e);
                    }
                    break;

                default :
                    // other CmsException
                    break;
            }
            
            if (e.getRootCause() != null) {
                t = e.getRootCause();
            }
        }

        if (status > 0) {
            res.setStatus(status);
        }   

        try {
            isNotGuest = isNotGuest 
                || (cms != null 
                    && cms.getRequestContext().currentUser() != null 
                    && (! OpenCms.getDefaultUsers().getUserGuest().equals(cms.getRequestContext().currentUser().getName())) 
                    && ((cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupUsers())) 
                        || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupProjectmanagers())) 
                        || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupAdministrators()))));
        } catch (CmsException e) {
            // result is false
        }
        
        if (canWrite) {
            res.setContentType("text/HTML");
            res.setHeader("Cache-Control", "no-cache");
            res.setHeader("Pragma", "no-cache");                
            if (isNotGuest && cms != null) {
                try {
                    res.getWriter().print(createErrorBox(t, cms.getRequestContext().getRequest().getWebAppUrl()));
                } catch (IOException e) {
                    // can be ignored
                }
            } else {
                if (status < 1) {                    
                    status = HttpServletResponse.SC_NOT_FOUND;
                }
                try {
                    res.sendError(status, t.toString());
                } catch (IOException e) {
                    // can be ignored
                }
            }
        }
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.<p>  
     * 
     * The default implementation performs
     * this notification synchronously using the calling thread.<p>
     *
     * @param event a CmsEvent
     */
    protected void fireCmsEvent(CmsEvent event) {
        fireCmsEventHandler((List)m_listeners.get(event.getTypeInteger()), event);
        fireCmsEventHandler((List)m_listeners.get(I_CmsEventListener.LISTENERS_FOR_ALL_EVENTS), event);    
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.<p>  
     * 
     * The default implementation performs
     * this notification synchronously using the calling thread.<p>
     *
     * @param cms an initialized CmsObject
     * @param type event type
     * @param data event data
     */
    protected void fireCmsEvent(CmsObject cms, int type, java.util.Map data) {
        fireCmsEvent(new CmsEvent(cms, type, data));
    }
    
    /**
     * Fires the specified event to a list of event listeners.<p>
     * 
     * @param listeners the listeners to fire
     * @param event the event to fire
     */
    private void fireCmsEventHandler(List listeners, CmsEvent event) {
        if ((listeners != null) && (listeners.size() > 0)) {
            // handle all event listeners that listen only to this event type
            I_CmsEventListener list[] = new I_CmsEventListener[0];
            synchronized (listeners) {
                list = (I_CmsEventListener[])listeners.toArray(list);
            }
            for (int i = 0; i < list.length; i++) {
                list[i].cmsEvent(event);
            }
        }      
    }

    /** 
     * Returns the OpenCms application base path.<p>
     *
     * @return the OpenCms application base path
     */
    protected String getBasePath() {
        return m_basePath;
    }

    /**
     * This method returns the runtime configuration.
     * 
     * @return The runtime configuration.
     */
    protected ExtendedProperties getConfiguration() {
        return m_configuration;
    }
    
    /**
     * Return the OpenCms default character encoding.<p>
     * 
     * The default is set in the "opencms.properties" file.
     * If this is not set in "opencms.properties" the default 
     * is "ISO-8859-1".<p>
     * 
     * @return the default encoding, e.g. "UTF-8" or "ISO-8859-1"
     */
    protected String getDefaultEncoding() {
        return m_defaultEncoding;
    }
    
    /**
     * Returns the configured list of default directory file names.<p>
     *  
     * Caution: This list can not be modified.<p>
     * 
     * @return the configured list of default directory file names
     */
    protected List getDefaultFilenames() {
        return Collections.unmodifiableList(Arrays.asList(m_defaultFilenames));
    }
    
    /**
     * Returns the default user and group name configuration.<p>
     * 
     * @return the default user and group name configuration
     */
    protected CmsDefaultUsers getDefaultUsers() {
        return m_defaultUsers;
    }
    
    /**
     * Returns the driver manager.<p>
     * 
     * This is required for starting the CmsShell only
     * and should not be used otherwise.<p>
     *
     * @return the driver manager
     */
    protected CmsDriverManager getDriverManager() {
        return m_driverManager;
    }
    
    /**
     * Returns a part of the html error message dialog.<p>
     *
     * @param part the name of the piece to return
     * @return a part of the html error message dialog
     */
    private String getErrormsg(String part) {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("com/opencms/core/errormsg.properties"));
        } catch (Throwable t) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("Could not load com/opencms/core/errormsg.properties", t);
            }
        }
        String value = props.getProperty(part);
        return value;
    }

    /**
     * Returns the file name translator this OpenCms has read from the opencms.properties.<p>
     * 
     * @return The file name translator this OpenCms has read from the opencms.properties
     */
    protected CmsResourceTranslator getFileTranslator() {
        return m_fileTranslator;
    }    
    
    /**
     * Returns the link manager to resolve links in &lt;link&gt; tags.<p>
     * 
     * @return  the link manager to resolve links in &lt;link&gt; tags
     */
    protected CmsLinkManager getLinkManager() {
        return m_linkManager;        
    }

    /**
     * Returns the loader manager used for loading individual resources.<p>
     * 
     * @return the loader manager used for loading individual resources
     */
    protected CmsLoaderManager getLoaderManager() {
        return m_loaderManager;
    }
    
    /**
     * Returns the initialized logger (for changing the runlevel).<p>
     * 
     * @return the initialized logger
     */
    protected CmsLog getLog() {
        return m_log;
    }
        
    /**
     * Returns the log for the selected object.<p>
     * 
     * If the provided object is a String, this String will
     * be used as channel name. Otherwise the objects 
     * class name will be used as channel name.<p>
     *  
     * @param obj the object channel to use
     * @return the log for the selected object channel
     */      
    protected Log getLog(Object obj) {
        if ((obj == null) || (!m_log.isInitialized())) {
            return m_log;
        }
        return m_log.getLogger(obj);
    }

    /**
     * Returns the filename of the logfile.<p>
     * 
     * @return The filename of the logfile.
     */
    protected String getLogFileName() {
        return m_logfile;
    }

    /**
     * Returns the memory monitor.<p>
     * 
     * @return the memory monitor
     */
    protected CmsMemoryMonitor getMemoryMonitor() {
        return m_memoryMonitor;    
    }   
     
    /**
     * Returns the mime type for a specified file.<p>
     * 
     * @param filename the file name to check the mime type for
     * @param encoding default encoding in case of mime types is of type "text"
     * @return the mime type for a specified file
     */
    protected String getMimeType(String filename, String encoding) {        
        String mimetype = null;
        int lastDot = filename.lastIndexOf(".");
        // check if there was a file extension
        if ((lastDot > 0) && (lastDot < (filename.length() - 1))) {
            String ext = filename.substring(lastDot + 1);
            mimetype = (String)m_mimeTypes.get(ext);
            // was there a mimetype fo this extension?
            if (mimetype == null) {
                mimetype = C_DEFAULT_MIMETYPE;
            }
        } else {
            mimetype = C_DEFAULT_MIMETYPE;
        }
        mimetype = mimetype.toLowerCase();
        if ((encoding != null)         
        && mimetype.startsWith("text")
        && (mimetype.indexOf("charset") == -1)) {
            mimetype += "; charset=" + encoding;
        }
        
        return mimetype;                
    }

    /**
     * Returns the OpenCms request context, e.g. /opencms/opencms.<p>
     * 
     * The context will always start with a "/" and never have a trailing "/".<p>
     * 
     * @return String the OpenCms request context, e.g. /opencms/opencms
     */
    protected String getOpenCmsContext() {
        return m_openCmsContext;
    }

    /**
     * Returns the Class that is used for the password validation.<p>
     * 
     * @return the Class that is used for the password validation
     */
    protected String getPasswordValidatingClass() {
        return m_passwordValidatingClass;
    }

    /**
     * Returns the registry to read values from it.<p>
     * 
     * You don't have the permissions to write values. 
     * This is useful for modules to read module-parameters.<p>
     *
     * @return the registry
     */
    protected CmsRegistry getRegistry() {
        if (m_driverManager == null) {
            return null;
        }
        return m_driverManager.getRegistry(null);
    }
    
    /**
     * Returns the handler instance for the specified name, 
     * or null if the name does not match any handler name.<p>
     * 
     * @param name the name of the handler instance to return
     * @return the handler instance for the specified name
     */
    protected I_CmsRequestHandler getRequestHandler(String name) {
        return (I_CmsRequestHandler)m_requestHandlers.get(name);
    }

    /** 
     * Returns the runlevel of this OpenCmsCore object instance.<p>
     * 
     * @return the runlevel of this OpenCmsCore object instance
     */
    protected int getRunLevel() {
        return m_runLevel;
    }

    /** 
     * Looks up a value in the runtime property Map.<p>
     *
     * @param key the key to look up in the runtime properties
     * @return the value for the key, or null if the key was not found
     */
    protected Object getRuntimeProperty(Object key) {
        if (m_runtimeProperties == null) {
            return null;
        }
        return m_runtimeProperties.get(key);
    }

    /** 
     * Returns the complete runtime property Map.<p>
     *
     * @return the Map of runtime properties
     */
    protected Map getRuntimePropertyMap() {
        return m_runtimeProperties;
    }

    /**
     * Returns the OpenCms server name.<p>
     * 
     * @return the OpenCms server name
     */
    protected String getServerName() {
        return m_serverName;
    }
    
    /**
     * Returns the session manager.<p>
     * 
     * @return the session manager
     */
    public OpenCmsSessionManager getSessionManager() {
        return m_sessionManager;
    }

    /**
     * Returns the OpenCms session storage.<p>
     * 
     * @return the OpenCms session storage
     */
    public CmsCoreSession getSessionStorage() {
        return m_sessionStorage;
    }

    /**
     * Returns the initialized site manager, 
     * which contains information about all configured sites.<p> 
     * 
     * @return the initialized site manager
     */
    protected CmsSiteManager getSiteManager() {
        return m_siteManager;
    }

    /**
     * Returns the properties for the static export.<p>
     * 
     * @return the properties for the static export
     */
    protected CmsStaticExportManager getStaticExportManager() {
        return m_exportProperties;
    }
    
    /**
     * Returns the OpenCms Thread store.<p>
     * 
     * @return the OpenCms Thread store
     */
    protected CmsThreadStore getThreadStore() {
        return m_threadStore;
    }

    /**
     * Returns the value for the default user access flags.<p>
     * 
     * @return the value for the default user access flags
     */
    protected int getUserDefaultAccessFlags() {
        return m_userDefaultaccessFlags;
    }

    /**
     * Returns the value of the user default language.<p>
     * 
     * @return the value of the user default language
     */
    protected String getUserDefaultLanguage() {
        return m_userDefaultLanguage;
    }

    /**
     * Returns a String containing the version information (version name and version number) 
     * of this OpenCms system.<p>
     *
     * @return version a String containing the version information
     */
    protected String getVersionName() {
        return m_versionName;
    }

    /**
     * Returns a String containing the version number 
     * of this OpenCms system.<p>
     *
     * @return version a String containing the version number
     */
    protected String getVersionNumber() {
        return m_versionNumber;
    }
    
    /**
     * This method handled the user authentification for each request sent to the
     * OpenCms. <p>
     *
     * User authentification is done in three steps:
     * <ul>
     * <li> Session Authentification: OpenCms stores all active sessions of authentificated
     * users in an internal storage. During the session authetification phase, it is checked
     * if the session of the active user is stored there. </li>
     * <li> HTTP Autheification: If session authentification fails, it is checked if the current
     * user has loged in using HTTP authentification. If this check is positive, the user account is
     * checked. </li>
     * <li> Default user: When both authentification methods fail, the current user is
     * set to the default (guest) user. </li>
     * </ul>
     *
     * @param req the current http request
     * @param res the current http response
     * @param cmsReq the wrapped http request
     * @param cmsRes the wrapped http response
     * @return the initialized cms context
     * @throws IOException if user authentication fails
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(
        HttpServletRequest req, 
        HttpServletResponse res, 
        I_CmsRequest cmsReq, 
        I_CmsResponse cmsRes
    ) throws IOException, CmsException {
        CmsObject cms;

        // try to get the current session
        HttpSession session = req.getSession(false);
        String sessionId;
        // check if there is user data already stored in the session
        String user = null;
        if (session != null) {
            // session exists, try to reuse the user from the session
            user = m_sessionStorage.getUserName(session.getId());
            sessionId = session.getId();
        } else {
            sessionId = req.getParameter("JSESSIONID");
            if (sessionId != null) {
                user = m_sessionStorage.getUserName(sessionId);
            }
        }
                   
        // initialize the requested site root
        CmsSite site = getSiteManager().matchRequest(req);
                   
        if (user != null) {
            // a user name is found in the session, reuse this user
            Integer project = m_sessionStorage.getCurrentProject(sessionId);

            // initialize site root from request
            String siteroot = null;
            // a dedicated workplace site is configured
            if ((getSiteManager().getWorkplaceSiteMatcher().equals(site.getSiteMatcher()))) {
                // if no dedicated workplace site is configured, 
                // or for the dedicated workplace site, use the site root from the session attribute
                siteroot = m_sessionStorage.getCurrentSite(sessionId);
            }
            if (siteroot == null) {
                siteroot = site.getSiteRoot();
            }        
            cms = initCmsObject(cmsReq, cmsRes, user, siteroot, project.intValue(), m_sessionStorage);
        } else {
            // no user name found in session or no session, login the user as guest user
            cms = initCmsObject(cmsReq, cmsRes, OpenCms.getDefaultUsers().getUserGuest(), site.getSiteRoot(), I_CmsConstants.C_PROJECT_ONLINE_ID, null);            
            if (m_useBasicAuthentication) {
                // check if basic authorization data was provided
                checkBasicAuthorization(cms, req, res);
            }
        }
        
        // return the initialized cms user context object
        return cms;
    }    
    
    /**
     * Returns an initialized CmsObject with "Guest" user permissions.<p>
     * 
     * In case the password is null, or the user is the Guest user,
     * no password check is done and the Guest user is initialized.<p>
     * 
     * @param req the current request
     * @param res the current response
     * @param user the user to initialize the CmsObject with
     * @param password the password of the user 
     * @return a cms context that has been initialized with "Guest" permissions
     * @throws CmsException in case the CmsObject could not be initialized
     */
    protected CmsObject initCmsObject(
        HttpServletRequest req, 
        HttpServletResponse res,
        String user,
        String password
    ) throws CmsException {
        CmsRequestHttpServlet cmsReq = null;
        CmsResponseHttpServlet cmsRes = null;
        String siteroot = null;
        // gather information from request / response if provided
        if ((req != null) && (res != null)) {
            try {
                initStartupClasses(req, res);
                cmsReq = new CmsRequestHttpServlet(req, getFileTranslator());
                cmsRes = new CmsResponseHttpServlet(req, res);
                siteroot = OpenCms.getSiteManager().matchRequest(req).getSiteRoot();
            } catch (IOException e) {
                throw new CmsException("Exception initializing CmsObject for user " + user, CmsException.C_UNKNOWN_EXCEPTION, e);
            }
        }
        // initialize the user        
        if (user == null) {
            user = getDefaultUsers().getUserGuest();
        }
        if (siteroot == null) {
            siteroot = "/";
        }
        CmsObject cms = initCmsObject(cmsReq, cmsRes, user, siteroot, I_CmsConstants.C_PROJECT_ONLINE_ID, null);
        // login the user if different from Guest
        if ((password != null) && !getDefaultUsers().getUserGuest().equals(user)) {
            cms.loginUser(user, password, I_CmsConstants.C_IP_LOCALHOST);
        }
        return cms;
    }

    /**
     * Inits a CmsObject with the given users information.<p>
     * 
     * @param cmsReq the current I_CmsRequest (usually initialized form the HttpServletRequest)
     * @param cmsRes the current I_CmsResponse (usually initialized form the HttpServletResponse)
     * @param user the name of the user to init
     * @param currentSite the users current site 
     * @param project the id of the current project
     * @param sessionStorage the session storage for this OpenCms instance
     * @return the initialized CmsObject
     * @throws CmsException in case something goes wrong
     */
    protected CmsObject initCmsObject(
        I_CmsRequest cmsReq, 
        I_CmsResponse cmsRes,
        String user, 
        String currentSite, 
        int project, 
        CmsCoreSession sessionStorage
    ) throws CmsException {
        CmsObject cms = new CmsObject();
        cms.init(m_driverManager, cmsReq, cmsRes, user, project, currentSite, sessionStorage, m_directoryTranslator, m_fileTranslator);
        return cms;
    }
    
    /**
     * Returns an initialized CmsObject with the user initialized as provided,
     * with the "online" project selected and "/" set as the current site root.<p>
     * 
     * Note: Only the default users 'Guest' and 'Export' can initialized with 
     * this method, all other user names will throw a RuntimeException.<p>
     * 
     * @param user the user name to initialize, can only be 
     *        {@link org.opencms.db.CmsDefaultUsers#getUserGuest()} or
     *        {@link org.opencms.db.CmsDefaultUsers#getUserExport()}
     * @return an initialized CmsObject with "Guest" user permissions
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @throws RuntimeException in case an invalid user name is provided
     */
    protected CmsObject initCmsObject(String user) {
        if (user == null) {
            user = getDefaultUsers().getUserGuest();
        } else if (!user.equalsIgnoreCase(getDefaultUsers().getUserGuest()) 
                && !user.equalsIgnoreCase(getDefaultUsers().getUserExport())) {
            throw new RuntimeException("Invalid user for default CmsObject initialization: " + user);
        }
        try {
            return initCmsObject(null, null, user, null);
        } catch (CmsException e) {
            // should not happen since the guest user can always be created like this
            if (getLog(this).isWarnEnabled()) {
                getLog(this).warn("Unable to initialize CmsObject for user " + user, e);
            }
            return new CmsObject();            
        } 
    }
    
    /**
     * Constructor to create a new OpenCms object.<p>
     * 
     * It reads the configurations from the <code>opencms.properties</code>
     * file in the <code>config/</code> subdirectory. With the information 
     * from this file is inits a ResourceBroker (Database access module),
     * various caching systems and other options.<p>
     * 
     * This will only be done once per accessing class.
     *
     * @param configuration the configurations from the <code>opencms.properties</code> file
     * @throws Exception in case of problems initializing OpenCms, this is usually fatal 
     */
    protected void initConfiguration(ExtendedProperties configuration) throws Exception {
        // this will initialize the encoding with some default from the A_OpenCms
        m_defaultEncoding = getDefaultEncoding();
        // check the opencms.properties for a different setting
        m_defaultEncoding = configuration.getString("defaultContentEncoding", m_defaultEncoding);
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms encoding     : " + m_defaultEncoding);
        }
        String systemEncoding = null;
        try {
            systemEncoding = System.getProperty("file.encoding");
        } catch (SecurityException se) {
            // security manager is active, but we will try other options before giving up
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". System file.encoding : " + systemEncoding);
        }
        if (!m_defaultEncoding.equals(systemEncoding)) {
            String msg = "OpenCms startup failure: System file.encoding '" + systemEncoding + "' not equal to OpenCms encoding '" + m_defaultEncoding + "'";
            if (getLog(this).isFatalEnabled()) {
                getLog(this).fatal(OpenCmsCore.C_MSG_CRITICAL_ERROR + "1: " + msg);
            }
            throw new Exception(msg);
        }
        try {
            // check if the found encoding is supported 
            // this will work with Java 1.4+ only
            if (!java.nio.charset.Charset.isSupported(m_defaultEncoding)) {
                m_defaultEncoding = OpenCms.getDefaultEncoding();
            }
        } catch (Throwable t) {
            // will be thrown in Java < 1.4 (NoSuchMethodException etc.)
            // in Java < 1.4 there is no easy way to check if encoding is supported,
            // so you must make sure your setting in "opencms.properties" is correct.             
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Encoding set to      : " + m_defaultEncoding);
        }
        
        // read server ethernet address (MAC) and init UUID generator
        String ethernetAddress = configuration.getString("server.ethernet.address", CmsUUID.getDummyEthernetAddress());
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
            getLog(CmsLog.CHANNEL_INIT).info(". Ethernet address used: " + ethernetAddress);
        }
        CmsUUID.init(ethernetAddress);
        
        m_serverName = configuration.getString("server.name", "OpenCmsServer");
        
        // initialize the memory monitor
        m_memoryMonitor = CmsMemoryMonitor.initialize(configuration);
        
        // check the installed Java SDK
        try {
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                getLog(CmsLog.CHANNEL_INIT).info(". Java VM in use       : " + jdkinfo);
                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                getLog(CmsLog.CHANNEL_INIT).info(". Operating sytem      : " + osinfo);
            }
        } catch (Exception e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "2", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }
                
        // read the default user configuration
        m_defaultUsers = CmsDefaultUsers.initialize(configuration);      

        try {
            // init the rb via the manager with the configuration
            // and init the cms-object with the rb.
            m_driverManager = CmsDriverManager.newInstance(configuration);            
        } catch (Exception e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "3", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw new CmsException("Database init failed", CmsException.C_RB_INIT_ERROR, e);
        }

        try {
            // initalize the Hashtable with all available mimetypes
            Hashtable mimeTypes = m_driverManager.readMimeTypes();
            setMimeTypes(mimeTypes);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Found mime types     : " + mimeTypes.size() + " entrys");
            }

            // if the System property opencms.disableScheduler is set to true, don't start scheduling
            if (!new Boolean(System.getProperty("opencms.disableScheduler")).booleanValue()) {
                // now initialise the OpenCms scheduler to launch cronjobs
                m_table = new CmsCronTable(m_driverManager.readCronTable());
                m_scheduler = new CmsCronScheduler(this, m_table);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". OpenCms scheduler    : enabled");
                }
            } else {
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". OpenCms scheduler    : disabled");
                }
            }
        } catch (Exception e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "5", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }
        
        // initialize "dialoghandler" registry classes
        try {
            List dialogHandlerClasses = OpenCms.getRegistry().getDialogHandler();
            Iterator i = dialogHandlerClasses.iterator();
            while (i.hasNext()) {
                String currentClass = (String)i.next();                
                I_CmsDialogHandler handler = (I_CmsDialogHandler)Class.forName(currentClass).newInstance();            
                setRuntimeProperty(handler.getDialogHandler(), handler);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". Dialog handler class : " + currentClass + " instanciated");
                }
            }
        } catch (Exception e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "7", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }
        
        // initialize the Thread store
        m_threadStore = new CmsThreadStore();
        
        // initialize the link manager
        m_linkManager = new CmsLinkManager();

        // read flex jsp export url property and save in runtime configuration
        String flexExportUrl = configuration.getString(CmsJspLoader.C_LOADER_JSPEXPORTURL, null);
        if (null != flexExportUrl) {
            // if JSP export URL is null it will be set in initStartupClasses()
            if (flexExportUrl.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                flexExportUrl = flexExportUrl.substring(0, flexExportUrl.length() - 1);
            }
            setRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL, flexExportUrl);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". JSP export URL       : using value from opencms.properties - " + flexExportUrl);
            }
        }

        // read flex jsp error page commit property and save in runtime configuration
        Boolean flexErrorPageCommit = configuration.getBoolean(CmsJspLoader.C_LOADER_ERRORPAGECOMMIT, new Boolean(true));
        setRuntimeProperty(CmsJspLoader.C_LOADER_ERRORPAGECOMMIT, flexErrorPageCommit);
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". JSP errorPage commit : " + (flexErrorPageCommit.booleanValue() ? "enabled" : "disabled"));
        }

        // try to initialize the flex cache
        try {
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Flex cache init      : starting");
            }
            // pass configuration to flex cache for initialization
            new CmsFlexCache(configuration);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Flex cache init      : finished");
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Flex cache init      : non-critical error " + e.toString());
            }
        }
        
        // initialize the loaders
        try {
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader init  : starting");
            }
            m_loaderManager = new CmsLoaderManager(configuration);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader init  : finished");
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". ResourceLoader init  : non-critical error " + e.toString());
            }
        }

        // try to initialize directory translations
        try {
            boolean translationEnabled = configuration.getBoolean("directory.translation.enabled", false);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) { 
                getLog(CmsLog.CHANNEL_INIT).info(". Directory translation: " + (translationEnabled ? "enabled" : "disabled"));
            }
            if (translationEnabled) {
                String[] translations = configuration.getStringArray("directory.translation.rules");
                // Directory translation stops after fist match, hence the "false" parameter
                m_directoryTranslator = new CmsResourceTranslator(translations, false);
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Directory translation: non-critical error " + e.toString());
            }
        }
        // make sure we always have at least an empty array      
        if (m_directoryTranslator == null) {
            m_directoryTranslator = new CmsResourceTranslator(new String[0], false);
        }

        // try to initialize filename translations
        try {
            boolean translationEnabled = configuration.getBoolean("filename.translation.enabled", false);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) { 
                getLog(CmsLog.CHANNEL_INIT).info(". Filename translation : " + (translationEnabled ? "enabled" : "disabled"));
            }
            if (translationEnabled) {
                String[] translations = configuration.getStringArray("filename.translation.rules");
                // Filename translations applies all rules, hence the true patameters
                m_fileTranslator = new CmsResourceTranslator(translations, true);
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Filename translation : non-critical error " + e.toString());
            }
        }
        // make sure we always have at last an emtpy array      
        if (m_fileTranslator == null) {
            m_fileTranslator = new CmsResourceTranslator(new String[0], false);
        }

        m_defaultFilenames = null;
        // try to initialize default directory file names (e.g. index.html)
        try {
            m_defaultFilenames = configuration.getStringArray("directory.default.files");
            for (int i = 0; i < m_defaultFilenames.length; i++) {
                // remove possible white space
                m_defaultFilenames[i] = m_defaultFilenames[i].trim();
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". Default file         : " + (i + 1) + " - " + m_defaultFilenames[i]);
                }
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Default file         : non-critical error " + e.toString());
            }
        }
        // make sure we always have at last an emtpy array      
        if (m_defaultFilenames == null) {
            m_defaultFilenames = new String[0];
        }
            
        // read the immutable import resources
        String[] immuResources = configuration.getStringArray("import.immutable.resources");
        if (immuResources == null) {
            immuResources = new String[0];
        }
        List immutableResourcesOri = java.util.Arrays.asList(immuResources);
        ArrayList immutableResources = new ArrayList();
        for (int i = 0; i < immutableResourcesOri.size(); i++) {
            // remove possible white space
            String path = ((String)immutableResourcesOri.get(i)).trim();
            if (path != null && !"".equals(path)) {
                immutableResources.add(path);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". Immutable resource   : " + (i + 1) + " - " + path);
                }
            }
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Immutable resources  : " + ((immutableResources.size() > 0) ? "enabled" : "disabled"));
        }
        setRuntimeProperty("import.immutable.resources", immutableResources);
        
        // read the default user settings
        try {
            m_userDefaultLanguage = configuration.getString("workplace.user.default.language", I_CmsWpConstants.C_DEFAULT_LANGUAGE);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". User data init       : Default language is '" + m_userDefaultLanguage + "'");
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". User data init       : non-critical error " + e.toString());
            }
        }
                
        // read the password validating class
        m_passwordValidatingClass = configuration.getString("passwordvalidatingclass", "com.opencms.util.PasswordValidtation");
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Password validation  : " + m_passwordValidatingClass);
        }
        
        // read the maximum file upload size limit
        Integer fileMaxUploadSize = new Integer(configuration.getInteger("workplace.file.maxuploadsize", -1));
        setRuntimeProperty("workplace.file.maxuploadsize", fileMaxUploadSize);
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". File max. upload size: " + (fileMaxUploadSize.intValue() > 0 ? (fileMaxUploadSize + " KB") : "unlimited"));
        }
        
        // read old (proprietary XML-style) locale backward compatibily support flag
        Boolean showUserGroupIcon = configuration.getBoolean("workplace.administration.showusergroupicon", new Boolean(true));
        setRuntimeProperty("workplace.administration.showusergroupicon", showUserGroupIcon);
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Show user/group icon : " + (showUserGroupIcon.booleanValue() ? "yes" : "no"));
        }
        
        // initialize "resourceinit" registry classes
        try {
            List resourceInitClasses = OpenCms.getRegistry().getResourceInit();
            Iterator i = resourceInitClasses.iterator();
            while (i.hasNext()) {
                String currentClass = (String)i.next();
                try {
                    m_checkFile.add(Class.forName(currentClass).newInstance());
                    if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                        getLog(CmsLog.CHANNEL_INIT).info(". Resource init class  : " + currentClass + " instanciated");
                    }
                } catch (Exception e1) {
                    if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                        getLog(CmsLog.CHANNEL_INIT).warn(". Resource init class  : non-critical error " + e1.toString());
                    }
                }
            }
        } catch (Exception e2) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Resource init class  : non-critical error " + e2.toString());
            }
        } 
        
        // read old (proprietary XML-style) locale backward compatibily support flag
        Boolean supportOldLocales = configuration.getBoolean("compatibility.support.oldlocales", new Boolean(false));
        setRuntimeProperty("compatibility.support.oldlocales", supportOldLocales);
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Old locale support   : " + (supportOldLocales.booleanValue() ? "enabled" : "disabled"));
        }

        // convert import files from 4.x versions old webapp URL
        String webappUrl = configuration.getString("compatibility.support.import.old.webappurl", null);
        if (webappUrl != null) {
            setRuntimeProperty("compatibility.support.import.old.webappurl", webappUrl);
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Old webapp URL       : " + ((webappUrl == null) ? "not set!" : webappUrl));
        }

        // unwanted resource properties which are deleted during import
        String[] propNames = configuration.getStringArray("compatibility.support.import.remove.propertytags");
        if (propNames == null) {
            propNames = new String[0];
        }
        List propertyNamesOri = java.util.Arrays.asList(propNames);
        ArrayList propertyNames = new ArrayList();
        for (int i = 0; i < propertyNamesOri.size(); i++) {
            // remove possible white space
            String name = ((String)propertyNamesOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                propertyNames.add(name);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". Clear import property: " + (i + 1) + " - " + name);
                }
            }
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Remove properties    : " + ((propertyNames.size() > 0) ? "enabled" : "disabled"));
        }
        setRuntimeProperty("compatibility.support.import.remove.propertytags", propertyNames);

        // old web application names (for editor macro replacement) 
        String[] appNames = configuration.getStringArray("compatibility.support.webAppNames");
        if (appNames == null) {
            appNames = new String[0];
        }
        List webAppNamesOri = java.util.Arrays.asList(appNames);
        ArrayList webAppNames = new ArrayList();
        for (int i = 0; i < webAppNamesOri.size(); i++) {
            // remove possible white space
            String name = ((String)webAppNamesOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                webAppNames.add(name);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". Old context path     : " + (i + 1) + " - " + name);
                }
            }
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Old context support  : " + ((webAppNames.size() > 0) ? "enabled" : "disabled"));
        }
        setRuntimeProperty("compatibility.support.webAppNames", webAppNames);

        // get a Admin cms context object
        CmsObject adminCms = initCmsObject(null, null, getDefaultUsers().getUserAdmin(), null);
        // initialize the site manager
        m_siteManager = CmsSiteManager.initialize(configuration, adminCms);  

        // site folders for which links should be labeled specially in the explorer
        String[] labelSiteFolderString = configuration.getStringArray("site.labeled.folders");
        if (labelSiteFolderString == null) {
            labelSiteFolderString = new String[0];
        }
        List labelSiteFoldersOri = java.util.Arrays.asList(labelSiteFolderString);
        ArrayList labelSiteFolders = new ArrayList();
        for (int i = 0; i < labelSiteFoldersOri.size(); i++) {
            // remove possible white space
            String name = ((String)labelSiteFoldersOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                labelSiteFolders.add(name);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". Label links in folder: " + (i + 1) + " - " + name);
                }
            }
        }
        setRuntimeProperty("site.labeled.folders", labelSiteFolders);
        
        // initializes the cron manager
        // TODO enable the cron manager
        //m_cronManager = new CmsCronManager();
        
        // set the property whether siblings should get published if a file gets published directly
        String directPublishSiblings = configuration.getString("workplace.directpublish.siblings", "false");
        setRuntimeProperty("workplace.directpublish.siblings", directPublishSiblings);
        
        // save the configuration
        m_configuration = configuration;        
    }

    /**
     * Initialization of the OpenCms runtime environment.<p>
     *
     * The connection information for the database is read 
     * from the <code>opencms.properties</code> configuration file and all 
     * driver manager are initialized via the initalizer, 
     * which usually will be an instance of a <code>OpenCms</code> class.
     *
     * @param context configuration of OpenCms from <code>web.xml</code>
     * @throws CmsInitException if initalization fails
     */
    protected synchronized void initContext(ServletContext context) throws CmsInitException {        
        initVersion(this);
                
        // Check for OpenCms home (base) directory path
        String base = context.getInitParameter("opencms.home");
        if (base == null || "".equals(base)) {
            base = CmsMain.searchBaseFolder(context.getRealPath("/"));
            if (base == null || "".equals(base)) {
                throwInitException(new CmsInitException(C_ERRORMSG + "OpenCms base folder could not be guessed. Please define init parameter \"opencms.home\" in servlet engine configuration.\n\n"));
            }
        }
        base = setBasePath(base);  
        
        // Collect the configurations 
        ExtendedProperties configuration = null;       
        try {
            configuration = CmsSetupUtils.loadProperties(CmsBase.getPropertiesPath(true));
        } catch (Exception e) {
            throwInitException(new CmsInitException(C_ERRORMSG + "Trouble reading property file " + CmsBase.getPropertiesPath(true) + ".\n\n", e));
        }
        
        // set path to log file
        m_logfile = (String)configuration.get("log.file");
        if (m_logfile != null) {
            m_logfile = CmsBase.getAbsolutePath(m_logfile);
            configuration.put("log.file", m_logfile);
        }
            
        // read the the OpenCms servlet mapping from the servlet context
        String servletMapping = context.getInitParameter("OpenCmsServlet");
        if (servletMapping == null) {
            m_instance = null;
            throw new CmsInitException("OpenCms servlet mapping not configured in 'web.xml'");
        }        
        if (servletMapping.endsWith("/*")) {
            servletMapping = servletMapping.substring(0, servletMapping.length()-2);
        }        
        configuration.put("servlet.mapping", servletMapping);
        
        // check if the wizard is enabled, if so stop initialization     
        if (configuration.getBoolean("wizard.enabled", true)) {
            m_instance = null;
            throw new CmsInitException("OpenCms setup wizard is enabled, unable to start OpenCms context", CmsInitException.C_INIT_WIZARD_ENABLED);
        }       

        // initialize the logging
        initLogging(configuration);
        
        // output startup message
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(".");        
            getLog(CmsLog.CHANNEL_INIT).info(".");        
            getLog(CmsLog.CHANNEL_INIT).info(".");        
            getLog(CmsLog.CHANNEL_INIT).info(".");
            printCopyrightInformation();       
            getLog(CmsLog.CHANNEL_INIT).info(".                      ...............................................................");        
            getLog(CmsLog.CHANNEL_INIT).info(". Startup time         : " + (new Date(System.currentTimeMillis())));        
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms version      : " + OpenCms.getVersionName()); 
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms context path : /" + CmsBase.getWebAppName());                    
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms servlet path : " + servletMapping);                    
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms base path    : " + getBasePath());        
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms property file: " + CmsBase.getPropertiesPath(true));      
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms logfile      : " + m_logfile);   
            getLog(CmsLog.CHANNEL_INIT).info(". Servlet container    : " + context.getServerInfo());        
        }

        try {
            // initialize the configuration
            initConfiguration(configuration);
        } catch (CmsException cmsex) {
            if (cmsex.getType() == CmsException.C_RB_INIT_ERROR) {
                throwInitException(new CmsInitException(C_ERRORMSG + "Could not connect to the database. Is the database up and running?\n\n", cmsex));                
            }
        } catch (Exception exc) {
            throwInitException(new CmsInitException(C_ERRORMSG + "Trouble creating the com.opencms.core.CmsObject. Please check the root cause for more information.\n\n", exc));
        }
        
        // initialize static export variables
        m_exportProperties = new CmsStaticExportManager();
        
        // set if the static export is enabled or not
        m_exportProperties.setStaticExportEnabled("true".equalsIgnoreCase(configuration.getString("staticexport.enabled", "false")));

        // set the default value for the "export" property
        m_exportProperties.setExportPropertyDefault("true".equalsIgnoreCase(configuration.getString("staticexport.export_default", "false")));
                
        // set the export suffixes
        String[] exportSuffixes = configuration.getStringArray("staticexport.export_suffixes");
        if (exportSuffixes == null) {
            exportSuffixes = new String[0];
        }
        m_exportProperties.setExportSuffixes(exportSuffixes);
                
        // set the path for the export
        m_exportProperties.setExportPath(com.opencms.boot.CmsBase.getAbsoluteWebPath(CmsBase.getAbsoluteWebPath(configuration.getString("staticexport.export_path"))));

        // replace the "magic" names                 
        String servletName = configuration.getString("servlet.mapping"); 
        String contextName = "/" + CmsBase.getWebAppName(); 
        if ("/ROOT".equals(contextName)) {
            contextName = "";
        }
        
        // set the "magic" names in the extended properties
        configuration.setProperty("CONTEXT_NAME", contextName);
        configuration.setProperty("SERVLET_NAME", servletName);
                
        // get the export prefix variables for rfs and vfs
        String rfsPrefix = configuration.getString("staticexport.prefix_rfs", contextName + "/export");
        String vfsPrefix = configuration.getString("staticexport.prefix_vfs", contextName + servletName);
                                        
        // set the export prefix variables for rfs and vfs
        m_exportProperties.setRfsPrefix(rfsPrefix);
        m_exportProperties.setVfsPrefix(vfsPrefix);    
                        
        // set if links in the export should be relative or not
        m_exportProperties.setExportRelativeLinks(configuration.getBoolean("staticexport.relative_links", false)); 

        // initialize "exportname" folders
        m_exportProperties.setExportnames();
        
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Static export        : " + (m_exportProperties.isStaticExportEnabled()?"enabled":"disabled"));
            if (m_exportProperties.isStaticExportEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Export default       : " + m_exportProperties.getExportPropertyDefault());
                getLog(CmsLog.CHANNEL_INIT).info(". Export path          : " + m_exportProperties.getExportPath());
                getLog(CmsLog.CHANNEL_INIT).info(". Export rfs prefix    : " + m_exportProperties.getRfsPrefix());
                getLog(CmsLog.CHANNEL_INIT).info(". Export vfs prefix    : " + m_exportProperties.getVfsPrefix());
                getLog(CmsLog.CHANNEL_INIT).info(". Export link style    : " + (m_exportProperties.relativLinksInExport()?"relative":"absolute"));                
            }
        }                       

        // initalize the session storage
        m_sessionStorage = new CmsCoreSession();
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Session storage      : initialized");
        }
                         
        // initialize "requesthandler" registry classes
        try {
            List resourceHandlers = OpenCms.getRegistry().getSystemSubNodes("requesthandler");
            Iterator i = resourceHandlers.iterator();
            while (i.hasNext()) {
                String currentClass = (String)i.next();
                try {
                    I_CmsRequestHandler handler = (I_CmsRequestHandler)Class.forName(currentClass).newInstance();
                    m_requestHandlers.put(handler.getHandlerName(), handler);
                    if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                        getLog(CmsLog.CHANNEL_INIT).info(". Request handler class: " + currentClass + " instanciated");
                    }
                } catch (Exception e1) {
                    if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                        getLog(CmsLog.CHANNEL_INIT).warn(". Request handler class: non-critical error " + e1.toString());
                    }
                }
            }
        } catch (Exception e2) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Request handler class: non-critical error " + e2.toString());
            }
        }                           
                                     
        // check if basic or form based authentication should be used      
        m_useBasicAuthentication = configuration.getBoolean("auth.basic", true);        
        m_authenticationFormURI = configuration.getString("auth.form_uri" , I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/authenticate.html");                
    }
    
    /**
     * Initializes the logging mechanism of OpenCms.<p>
     * 
     * @param configuration The configurations read from <code>opencms.properties</code>
     */
    private void initLogging(ExtendedProperties configuration) {
        m_log.init(configuration, CmsBase.getPropertiesPath(true));
    }
    
    /**
     * Initialize member variables.<p>
     */
    protected void initMembers() {
        m_log = new CmsLog();
        m_passwordValidatingClass = "";
        m_checkFile = new ArrayList();
        m_defaultEncoding = C_DEFAULT_ENCODING;
        m_listeners = new HashMap();
        m_requestHandlers = new HashMap();
        m_startupClassesInitialized = false;
        m_userDefaultaccessFlags = I_CmsConstants.C_ACCESS_DEFAULT_FLAGS;
        m_versionName = C_DEFAULT_VERSION_NUMBER + " " + C_DEFAULT_VERSION_NAME;
        m_versionNumber = C_DEFAULT_VERSION_NUMBER;        
    }

    /**
     * Reads the requested resource from the OpenCms VFS,
     * in case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.<p>
     *
     * @param cms the current CmsObject
     * @param resourceName the requested resource
     * @return CmsFile the requested file read from the VFS
     * 
     * @throws CmsException in case the file does not exist or the user has insufficient access permissions 
     */
    public CmsFile initResource(CmsObject cms, String resourceName) throws CmsException {

        CmsFile file = null;
        CmsException tmpException = null;

        try {
            // Try to read the requested file
            file = cms.readFile(resourceName);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_NOT_FOUND) {
                // The requested file was not found
                // Check if a folder name was requested, and if so, try
                // to read the default pages in that folder

                try {
                    // Try to read the requested resource name as a folder
                    CmsFolder folder = cms.readFolder(resourceName);
                    // If above call did not throw an exception the folder
                    // was sucessfully read, so lets go on check for default 
                    // pages in the folder now

                    // Check if C_PROPERTY_DEFAULT_FILE is set on folder
                    String defaultFileName = cms.readProperty(CmsResource.getFolderPath(cms.readAbsolutePath(folder)), I_CmsConstants.C_PROPERTY_DEFAULT_FILE);
                    if (defaultFileName != null) {
                        // Property was set, so look up this file first
                        String tmpResourceName = CmsResource.getFolderPath(cms.readAbsolutePath(folder)) + defaultFileName;

                        try {
                            file = cms.readFile(tmpResourceName);
                            // No exception? So we have found the default file                         
                            cms.getRequestContext().getRequest().setRequestedResource(tmpResourceName);
                        } catch (CmsSecurityException se) {
                            // Maybe no access to default file?
                            throw se;
                        } catch (CmsException exc) {
                            // Ignore all other exceptions
                        }
                    }
                    if (file == null) {
                        // No luck with the property, so check default files specified in opencms.properties (if required)         
                        for (int i = 0; i < m_defaultFilenames.length; i++) {
                            String tmpResourceName = CmsResource.getFolderPath(cms.readAbsolutePath(folder)) + m_defaultFilenames[i];
                            try {
                                file = cms.readFile(tmpResourceName);
                                // No exception? So we have found the default file                         
                                cms.getRequestContext().getRequest().setRequestedResource(tmpResourceName);
                                // Stop looking for default files   
                                break;
                            } catch (CmsSecurityException se) {
                                // Maybe no access to default file?
                                throw se;
                            } catch (CmsException exc) {
                                // Ignore all other exceptions
                            }
                        }
                    }
                    if (file == null) {
                        // No default file was found, throw original exception
                        throw e;
                    }
                } catch (CmsException ex) {
                    // Exception trying to read the folder (or it's properties)
                    if (ex.getType() == CmsException.C_NOT_FOUND) {
                        // Folder with the name does not exist, store original exception
                        tmpException = e;
                        // throw e;
                    } else {
                        // If the folder was found there might have been a permission problem
                        throw ex;
                    }
                }

            } else {
                // Throw the CmsException (possible cause e.g. no access permissions)
                throw e;
            }
        }

        if (file != null) {
            // test if this file is only available for internal access operations
            if ((file.getFlags() & I_CmsConstants.C_ACCESS_INTERNAL_READ) > 0) {
                throw new CmsException(CmsException.C_ERROR_DESCRIPTION[CmsException.C_INTERNAL_FILE] + cms.getRequestContext().getUri(), CmsException.C_INTERNAL_FILE);
            }
        }

        // test if this file has to be checked or modified
        Iterator i = m_checkFile.iterator();
        while (i.hasNext()) {
            try {
                file = ((I_CmsResourceInit)i.next()).initResource(file, cms);
                // the loop has to be interrupted when the exception is thrown!
            } catch (CmsResourceInitException e) {
                break;
            }
        }

        // file is still null and not found exception was thrown, so throw original exception
        if (file == null && tmpException != null) {
            throw tmpException;
        }

        // Return the file read from the VFS
        return file;
    }

    /**
     * Initialize the startup classes of this OpenCms object.<p>
     * 
     * A startup class has to be configured in the <code>registry.xml</code> 
     * file of OpenCms. Startup classes are a way to create plug-in 
     * functions that required to be initialized once at OpenCms load time 
     * without the need to add initializing code to the constructor of this 
     * class.<p>
     * 
     * This must be done only once per running OpenCms object instance.
     * Usually this will be done by the OpenCms servlet.
     * 
     * @param req the current request
     * @param res the current response 
     */
    protected void initStartupClasses(HttpServletRequest req, HttpServletResponse res) {
        if (m_startupClassesInitialized) {
            return;
        }

        synchronized (this) {
            // Set the initialized flag to true
            m_startupClassesInitialized = true;

            if (res == null) {
                // currently no init action depends on res, this might change in the future
            }

            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Startup class init   : starting");
            }

            // set context once and for all
            String context = req.getContextPath() + req.getServletPath();
            if (context.endsWith("/")) {
                context = context.substring(0, context.lastIndexOf('/'));
            }
            setOpenCmsContext(context);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". OpenCms context      : " + context);
            }

            // check for old webapp names and extend with context
            ArrayList webAppNames = (ArrayList)OpenCms.getRuntimeProperty("compatibility.support.webAppNames");
            if (webAppNames == null) {
                webAppNames = new ArrayList();
            }
            if (!webAppNames.contains(context)) {
                webAppNames.add(context);
                setRuntimeProperty("compatibility.support.webAppNames", webAppNames);
            }

            // check for the JSP export URL runtime property
            String jspExportUrl = (String)getRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL);
            if (jspExportUrl == null) {
                // not initialized yet, so we use the value from the first request
                StringBuffer url = new StringBuffer(256);
                url.append(req.getScheme());
                url.append("://");
                url.append(req.getServerName());
                url.append(":");
                url.append(req.getServerPort());
                url.append(context);
                String flexExportUrl = new String(url);
                // check if the URL ends with a "/", this is not allowed
                if (flexExportUrl.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                    flexExportUrl = flexExportUrl.substring(0, flexExportUrl.length() - 1);
                }
                setRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL, flexExportUrl);
                CmsJspLoader.setJspExportUrl(flexExportUrl);
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". JSP export URL       : using value from first request - " + flexExportUrl);
                }
            }

            // initialize 1 instance per class listed in the startup node          
            try {
                Hashtable startupNode = OpenCms.getRegistry().getSystemValues("startup");
                if (startupNode != null) {
                    for (int i = 1; i <= startupNode.size(); i++) {
                        String currentClass = (String)startupNode.get("class" + i);
                        try {
                            Class.forName(currentClass).newInstance();
                            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                                getLog(CmsLog.CHANNEL_INIT).info(". Startup class init   : " + currentClass + " instanciated");
                            }
                        } catch (Exception e1) {
                            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                                getLog(CmsLog.CHANNEL_INIT).warn(". Startup class init   : non-critical error " + e1.toString());
                            }
                        }
                    }
                }
            } catch (Exception e2) {
                if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).warn(". Startup class init   : non-critical error " + e2.toString());
                }
            }

            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Startup class init   : finished");
                getLog(CmsLog.CHANNEL_INIT).info(".                      ...............................................................");
                getLog(CmsLog.CHANNEL_INIT).info(".");
            }
        }
    }

    /**
     * Initializes the version for this OpenCms, will be called by 
     * CmsHttpServlet or CmsShell upon system startup.<p>
     * 
     * @param o instance of calling object
     */
    protected void initVersion(Object o) {
        // read the version-informations from properties, if not done
        Properties props = new Properties();
        try {
            props.load(o.getClass().getClassLoader().getResourceAsStream("com/opencms/core/version.properties"));
        } catch (Throwable t) {
            // ignore this exception - no properties found
            return;
        }
        m_versionNumber = props.getProperty("version.number", C_DEFAULT_VERSION_NUMBER);
        m_versionName = m_versionNumber + " " + props.getProperty("version.name", C_DEFAULT_VERSION_NAME);
    }
    
    /**
     * Prints the OpenCms copyright information to all log-files.<p>
     */
    private void printCopyrightInformation() {
        String copy[] = I_CmsConstants.C_COPYRIGHT;

        // log to error-stream
        System.err.println("\n\nStarting OpenCms, version " + OpenCms.getVersionName() + " in context " + CmsBase.getWebAppName());
        for (int i = 0; i<copy.length; i++) {
            System.err.println(copy[i]);
        }

        // log with opencms-logger
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms version " + OpenCms.getVersionName());
            for (int i = 0; i<copy.length; i++) {
                getLog(CmsLog.CHANNEL_INIT).info(". " + copy[i]);
            }
        }
    }     

    /**
     * Removes a cms event listener.<p>
     *
     * @param listener the listener to remove
     */
    protected void removeCmsEventListener(I_CmsEventListener listener) {
        synchronized (m_listeners) {
            m_listeners.remove(listener);
        }
    }

    /**
     * This method sends a request to the client to display a login form,
     * it is needed for HTTP-Authentification.<p>
     *
     * @param req the client request
     * @param res the response
     * @throws IOException if something goes wrong
     */
    private void requestAuthorization(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String servletPath = null;
        String redirectURL = null;
        
        if (m_useBasicAuthentication) {
            // HTTP basic authentication is used
            res.setHeader("WWW-Authenticate", "BASIC realm=\"OpenCms\"");
            res.setStatus(401);
        } else {
            // form based authentication is used, redirect the user to
            // a page with a form to enter his username and password
            servletPath = req.getContextPath() + req.getServletPath();
            redirectURL = servletPath + m_authenticationFormURI + "?requestedResource=" + req.getPathInfo();
            res.sendRedirect(redirectURL);
        }
    }    
    
    /**
     * Sets the OpenCms base application path.<p>
     * 
     * @param path the base application path to set
     * @return the base path with resolved relative path information 
     */
    protected String setBasePath(String path) {
        path = path.replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        m_basePath = CmsLinkManager.normalizeRfsPath(path);
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info("OpenCms: Base application path is " + m_basePath);
        }
        return m_basePath;
    }
    
    /**
     * Initilizes the map of available mime types.<p>
     * 
     * @param types the map of available mime types
     */
    private void setMimeTypes(Hashtable types) {
        m_mimeTypes = new HashMap(types.size());
        m_mimeTypes.putAll(types);
    }

    /**
     * Sets the OpenCms request context.<p>
     * 
     * @param value the OpenCms request context
     */
    private void setOpenCmsContext(String value) {
        if ((value != null) && (value.startsWith("/ROOT"))) {
            value = value.substring("/ROOT".length());
        }
        if (value == null) {
            value = "";
        }
        m_openCmsContext = value;
    }

    /**
     * Sets the mimetype of the response.<p>
     * 
     * The mimetype is selected by the file extension of the requested document.
     * If no available mimetype is found, it is set to the default
     * "application/octet-stream".
     *
     * @param cms The current initialized CmsObject
     * @param file The requested document
     */
    protected void setResponse(CmsObject cms, CmsFile file) {
        String mimetype = getMimeType(file.getName(), cms.getRequestContext().getEncoding());
        cms.getRequestContext().getResponse().setContentType(mimetype);
    }

    /**       
     * Sets the init level of this OpenCmsCore object instance.<p>
     * 
     * @param level the level to set
     */
    private void setRunLevel(int level) {
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info("OpenCms: Changing runlevel from " + m_runLevel + " to " + level);
        }          
        m_runLevel = level;
    }

    /**       
     * This method adds an Object to the OpenCms runtime properties.
     * The runtime properties can be used to store Objects that are shared
     * in the whole system.<p>
     *
     * @param key the key to add the Object with
     * @param value the value of the Object to add
     */
    protected void setRuntimeProperty(Object key, Object value) {
        if (m_runtimeProperties == null) {
            m_runtimeProperties = Collections.synchronizedMap(new HashMap());
        }
        m_runtimeProperties.put(key, value);
    }

    /**
     * Sets the session manager.<p>
     * 
     * @param sessionManager the session manager to set
     */
    protected void setSessionManager(OpenCmsSessionManager sessionManager) {
        m_sessionManager = sessionManager;
    }
    
    /**
     * Displays a resource from the OpenCms by writing the result to the provided 
     * Servlet response output stream.<p>
     * 
     * @param req the current servlet request
     * @param res the current servlet response
     * 
     * @throws IOException in case of errors writing to the stream
     */
    protected void showResource(HttpServletRequest req, HttpServletResponse res) throws IOException {         
        CmsObject cms = null;
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req, getFileTranslator());
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res);
        
        try {
            initStartupClasses(req, res);
            cms = initCmsObject(req, res, cmsReq, cmsRes);
            // user is initialized, now deliver the requested resource
            CmsFile file = initResource(cms, cms.getRequestContext().getUri());
            if (file != null) {
                // a file was read, go on process it
                setResponse(cms, file);
                showResource(req, res, cms, file);
                updateUser(cms, cmsReq);
            }
                            
        } catch (Throwable t) {
            errorHandling(cms, req, res, t);
        }
    }    

    /**    
     * Delivers (i.e. shows) the requested resource to the user.<p>
     * 
     * @param req the current http request
     * @param res the current http response
     * @param cms the curren cms context
     * @param file the requested file
     * @throws ServletException if some other things goes wrong
     * @throws IOException if io things go wrong
     */
    protected void showResource(
        HttpServletRequest req, 
        HttpServletResponse res, 
        CmsObject cms, 
        CmsFile file
    ) throws ServletException, IOException {
        I_CmsResourceLoader loader = getLoaderManager().getLoader(file.getLoaderId());
        loader.load(cms, file, req, res);
    }

    /**
     * Starts a scheduled job with a correct instantiated CmsObject.<p>
     * 
     * @param entry the CmsCronEntry to start.
     */
    public void startScheduleJob(CmsCronEntry entry) {
        try {
            // TODO: Maybe implement site root as a parameter in cron job table 
            CmsObject cms = initCmsObject(null, null, entry.getUserName(), "/", I_CmsConstants.C_PROJECT_ONLINE_ID, null);
            // create a new ScheduleJob and start it
            CmsCronScheduleJob job = new CmsCronScheduleJob(cms, entry);
            job.start();
        } catch (Exception exc) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("Error initialising cron job for " + entry, exc);
            }
        }
    }

    /**
     * Throws an exception that is also logged and written to the error output console.<p>
     * 
     * @param cause the original Exception
     * @throws CmsInitException the <code>cause</code> parameter
     */
    private void throwInitException(CmsInitException cause) throws CmsInitException {
        String message = cause.getMessage();
        if (message == null) {
            message = cause.toString();
        }
        System.err.println("\n--------------------\nCritical error during OpenCms context init phase:\n" + message);
        System.err.println("Giving up, unable to start OpenCms.\n--------------------");        
        if (getLog(this).isFatalEnabled()) {
            getLog(this).fatal("Unable to start OpenCms", cause);
        }         
        throw cause;
    }

    /**
     * Reads the current cron entries from the database and updates the Crontable.<p>
     */
    public void updateCronTable() {
        try {
            m_table.update(m_driverManager.readCronTable());
        } catch (Exception exc) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("Crontable is corrupt, Cron scheduler has been disabled", exc);
            }
        }
    }
    
    /**
     * Updates the the user data stored in the CmsCoreSession after the requested document
     * is processed.<p>
     *
     * This is required if the user data (current group or project) was changed in
     * the requested document.<p>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms the current CmsObject initialized with the user data
     * @param cmsReq the current request
     */
    private void updateUser(CmsObject cms, I_CmsRequest cmsReq) {
        if (! cms.getRequestContext().isUpdateSessionEnabled()) {
            return;
        }
                
        // get the original ServletRequest and response
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();

        // get the session if it is there
        HttpSession session = req.getSession(false);

        // if the user was authenticated via sessions, update the information in the
        // sesssion stroage
        if ((session != null)) {
            if (!cms.getRequestContext().currentUser().getName().equals(OpenCms.getDefaultUsers().getUserGuest())) {

                Hashtable sessionData = new Hashtable(4);
                sessionData.put(I_CmsConstants.C_SESSION_USERNAME, cms.getRequestContext().currentUser().getName());
                sessionData.put(I_CmsConstants.C_SESSION_PROJECT, new Integer(cms.getRequestContext().currentProject().getId()));
                sessionData.put(I_CmsConstants.C_SESSION_CURRENTSITE, cms.getRequestContext().getSiteRoot());
                
                // get current session data
                Hashtable oldData = (Hashtable)session.getAttribute(I_CmsConstants.C_SESSION_DATA);
                if (oldData == null) {
                    oldData = new Hashtable();
                }
                sessionData.put(I_CmsConstants.C_SESSION_DATA, oldData);

                // update the user-data
                m_sessionStorage.putUser(session.getId(), sessionData);

                // ensure that the session notify is set
                // this is required to remove the session from the internal storage on its destruction
                OpenCmsServletNotify notify = null;
                Object sessionValue = session.getAttribute("NOTIFY");
                if (sessionValue instanceof OpenCmsServletNotify) {
                    notify = (OpenCmsServletNotify)sessionValue;
                }
                if (notify == null) {
                    notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                    session.setAttribute("NOTIFY", notify);
                }
            }
        }
    }    
    
    /**
     * Upgrades to runlevel 2.<p>
     * 
     * @param configuration the configuration
     * @return the initialized OpenCmsCore
     */
    protected synchronized OpenCmsCore upgradeRunlevel(ExtendedProperties configuration) {
        if ((m_instance != null) && (getRunLevel() == 2)) {
            // instance already in runlevel 2
            return m_instance;
        }        
        setRunLevel(2);
        try {
            m_instance.initConfiguration(configuration);                        
        } catch (Throwable t) {
            if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).error("Critical error during OpenCms initialization", t);
            }
            m_instance = null;
        }                  
        return m_instance;
    }

    /**
     * Upgrades to runlevel 3.<p>
     * 
     * @param context the context
     * @return the initialized OpenCmsCore
     */
    protected synchronized OpenCmsCore upgradeRunlevel(ServletContext context) {
        if ((m_instance != null) && (getRunLevel() == 3)) {
            // instance already in runlevel 3
            return m_instance;
        }
        try {
            setRunLevel(3);
            m_instance.initContext(context);
        } catch (CmsInitException e) {
            if (e.getType() != CmsInitException.C_INIT_WIZARD_ENABLED) {
                // do not output the "wizard enabled" message on the log 
                if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).error("Critical error during OpenCms initialization", e);
                }
            }
            m_instance = null;
        } catch (Throwable t) {
            if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).error("Critical error during OpenCms initialization", t);
            }            
            m_instance = null;            
        }
        return m_instance;
    }

}
