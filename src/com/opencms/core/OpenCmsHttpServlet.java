/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsHttpServlet.java,v $
* Date   : $Date: 2003/03/19 08:43:10 $
* Version: $Revision: 1.45 $
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

import com.opencms.boot.CmsBase;
import com.opencms.boot.CmsMain;
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.util.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import source.org.apache.java.util.Configurations;
import source.org.apache.java.util.ExtendedProperties;

/**
 * This the main servlet of the OpenCms system.<p>
 * 
 * From here, all other operations are invoked.
 * Any incoming request is handled in multiple steps:
 * 
 * <ol><li>The requesting user is authenticated and a CmsObject with the user information
 * is created. The CmsObject is used to access all functions of OpenCms, limited by
 * the authenticated users permissions. If the user is not identified, it is set to the default (guest)
 * user.</li>
 * 
 * <li>The requested document is loaded into OpenCms and depending on its type 
 * (and the users persmissions to display or modify it), 
 * it is send to one of the OpenCms launchers do be processed.</li>
 * 
 * <li>
 * The loaded launcher will then decide what to do with the contents of the 
 * requested document. In case of an XMLTemplate the template mechanism will 
 * be started, in case of a JSP the JSP handling mechanism is invoked, 
 * in case of an image (or other static file) this will simply be returned etc.
 * </li></ol>
 *
 * @author Michael Emmerich
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.45 $ $Date: 2003/03/19 08:43:10 $
 */
public class OpenCmsHttpServlet extends HttpServlet implements I_CmsConstants,I_CmsLogChannels {

    /**
     * The name of the redirect entry in the configuration file.
     */
    static final String C_PROPERTY_REDIRECT = "redirect";

    /**
     * The name of the redirect location entry in the configuration file.
     */
    static final String C_PROPERTY_REDIRECTLOCATION = "redirectlocation";

    /**
     * The configuration for the OpenCms servlet.
     */
    private Configurations m_configurations;

    /**
     * The session storage for all active users.
     */
    private CmsCoreSession m_sessionStorage;

    /**
     * The reference to the OpenCms system.
     */
    private OpenCms m_opencms;

    /**
     * Storage for the clusterurl.
     */
    private String m_clusterurl = null;
    
    /**
     * Flag to indicate if basic or form based authentication is used.
     */
    private boolean m_UseBasicAuthentication;
    
    /**
     * URI of the authentication form (read from properties).
     */
    private String m_AuthenticationFormURI;
    
    /**
     * Flag for debugging.
     */
    private static final boolean DEBUG = false;
    
    /**
     * Prefix for error messages for initialization errors.       */
    private static final String C_ERRORMSG = "OpenCms initialization error!\n\n";

    /**
     * Prints the OpenCms copyright information to all log-files.<p>
     */
    private void printCopyrightInformation() {
        String copy[] = C_COPYRIGHT;

        // log to error-stream
        System.err.println("\n\nStarting OpenCms, version " + A_OpenCms.getVersionName());
        for(int i = 0;i < copy.length;i++) {
            System.err.println(copy[i]);
        }

        // log with opencms-logger
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT)) {
            A_OpenCms.log(C_OPENCMS_INIT, ". OpenCms version " + A_OpenCms.getVersionName());
            for(int i = 0;i < copy.length;i++) {
                A_OpenCms.log(C_OPENCMS_INIT, ". " + copy[i]);
            }
        }
    }   
    
    /**
     * Throws a servlet exception that is also logged and written to the error output console.<p>
     * 
     * @param cause the original Exception
     * @throws ServletException the <code>cause</code> parameter
     */
    private void throwInitException(ServletException cause) throws ServletException {
        String message = cause.getMessage();
        if (message == null) message = cause.toString();
        System.err.println("\n--------------------\nCritical error during OpenCms servlet init phase:\n" + message);
        System.err.println("Giving up, unable to start OpenCms.\n--------------------");        
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL)) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, message);
        }         
        throw cause;
    }
        
    /**
     * Initialization of the OpenCms servlet (overloaded Servlet API method).<p>
     *
     * The connection information for the database is read 
     * from the <code>opencms.properties</code> configuration file and all 
     * resource brokers are initialized via the initalizer, 
     * which usually will be an instance of a <code>OpenCms</code> class.
     *
     * @param config configuration of OpenCms from <code>web.xml</code>
     * @throws ServletException when sevlet initalization fails
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        A_OpenCms.initVersion(this);
        
        // Check for OpenCms home (base) directory path
        String base = config.getInitParameter("opencms.home");
        if (DEBUG) System.err.println("BASE: " + config.getServletContext().getRealPath("/"));
        if (DEBUG) System.err.println("BASE2: " + System.getProperty("user.dir"));
        if(base == null || "".equals(base)) {
            if (DEBUG) System.err.println("No OpenCms home folder given. Trying to guess...");
            base = CmsMain.searchBaseFolder(config.getServletContext().getRealPath("/"));
            if(base == null || "".equals(base)) {
                throwInitException(new ServletException(C_ERRORMSG + "OpenCms base folder could not be guessed. Please define init parameter \"opencms.home\" in servlet engine configuration.\n\n"));
            }
        }
        base = CmsBase.setBasePath(base);        
        
        String logFile;
        ExtendedProperties extendedProperties = null;
        
        // Collect the configurations        
        try {
            extendedProperties = new ExtendedProperties(CmsBase.getPropertiesPath(true));
        }
        catch(Exception e) {
            throwInitException(new ServletException(C_ERRORMSG + "Trouble reading property file " + CmsBase.getPropertiesPath(true) + ".\n\n", e));
        }
        
        // Change path to log file, if given path is not absolute
        logFile = (String)extendedProperties.get("log.file");
        if(logFile != null) {
            extendedProperties.put("log.file", CmsBase.getAbsolutePath(logFile));
        }

        // Create the configurations object
        m_configurations = new Configurations(extendedProperties);        

        // Initialize the logging
        A_OpenCms.initializeServletLogging(m_configurations);
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT)) {
            A_OpenCms.log(C_OPENCMS_INIT, ".");        
            A_OpenCms.log(C_OPENCMS_INIT, ".");        
            A_OpenCms.log(C_OPENCMS_INIT, ".");        
            A_OpenCms.log(C_OPENCMS_INIT, ".");
            printCopyrightInformation();       
            A_OpenCms.log(C_OPENCMS_INIT, ".                      ...............................................................");        
            A_OpenCms.log(C_OPENCMS_INIT, ". Startup time         : " + (new Date(System.currentTimeMillis())));        
            A_OpenCms.log(C_OPENCMS_INIT, ". Servlet container    : " + config.getServletContext().getServerInfo());        
            A_OpenCms.log(C_OPENCMS_INIT, ". OpenCms version      : " + A_OpenCms.getVersionName()); 
            A_OpenCms.log(C_OPENCMS_INIT, ". OpenCms base path    : " + CmsBase.getBasePath());        
            A_OpenCms.log(C_OPENCMS_INIT, ". OpenCms property file: " + CmsBase.getPropertiesPath(true));        
            A_OpenCms.log(C_OPENCMS_INIT, ". OpenCms logfile      : " + CmsBase.getAbsolutePath(logFile));        
        }
        
        // check cluster configuration
        m_clusterurl = (String)m_configurations.getString(C_CLUSTERURL, "");
        if((! "".equals(m_clusterurl)) && C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT)) A_OpenCms.log(C_OPENCMS_INIT, ". Clusterurl           : " + m_clusterurl);                

        try {
            // create the OpenCms object
            m_opencms = new OpenCms(m_configurations);
        } catch (CmsException cmsex) {
            if (cmsex.getType() == CmsException.C_RB_INIT_ERROR) {
                throwInitException(new ServletException(C_ERRORMSG + "Could not connect to the database. Is the database up and running?\n\n", cmsex));                
            }
        } catch(Exception exc) {
            throwInitException(new ServletException(C_ERRORMSG + "Trouble creating the com.opencms.core.CmsObject. Please check the root cause for more information.\n\n", exc));
        }

        // initalize the session storage
        m_sessionStorage = new CmsCoreSession();
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT)) A_OpenCms.log(C_OPENCMS_INIT, ". Session storage      : initialized");
             
        // check if basic or form based authentication should be used      
        this.m_UseBasicAuthentication = m_configurations.getBoolean( "auth.basic", true );        
        this.m_AuthenticationFormURI = m_configurations.getString( "auth.form_uri" , "/system/workplace/action/authenticate.html" );
    }

    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Performing shutdown ...");
        try {
            m_opencms.destroy();
        }catch(CmsException e) {
            if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL)) A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet]" + e.toString());
        }
        try{
            Utils.getModulShutdownMethods(OpenCms.getRegistry());
        }catch (CmsException e){
            // log exception since we are about to shutdown anyway
            if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL)) A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet] Module shutdown exception: " + e);
        }
        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] ... shutdown completed.");
    }

    /**
     * Method invoked on each HTML GET request.
     * <p>
     * (Overloaded Servlet API method, requesting a document).
     * Reads the URI received from the client and invokes the appropiate action.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     * @throws ServletException if request fails
     * @throws IOException if the user authentication fails
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {         
        CmsObject cms = null;
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req, m_opencms.getFileTranslator());
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res, m_clusterurl);
        try {
            m_opencms.initStartupClasses(req, res);
            cms = initUser(cmsReq, cmsRes);
            // no redirect was done - deliver the ressource normally
            CmsFile file = m_opencms.initResource(cms);
            if(file != null) {
                // a file was read, go on process it
                m_opencms.setResponse(cms, file);
                m_opencms.showResource(cms, file);
                updateUser(cms, cmsReq);
            }
        }
        catch(CmsException e) {
            errorHandling(cms, cmsReq, cmsRes, e);
        }
    }

    /**
     * Method invoked on each HTML POST request.
     * <p>
     * (Overloaded Servlet API method, posting a document)
     * The OpenCmsMultipartRequest is invoked to upload a new document into OpenCms.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     * @throws ServletException Thrown if request fails.
     * @throws IOException Thrown if user autherization fails.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {            
        doGet(req, res);
    }

    /**
     * Generates a formated exception output. <br>
     * Because the exception could be thrown while accessing the system files,
     * the complete HTML code must be added here!
     * @param e The caught CmsException.
     * @return String containing the HTML code of the error message.
     */
    private String createErrorBox(CmsException e, CmsObject cms) {
        StringBuffer output = new StringBuffer();
        output.append(this.getErrormsg("C_ERRORPART_1"));
        output.append(cms.getRequestContext().getRequest().getWebAppUrl());
        output.append(this.getErrormsg("C_ERRORPART_2"));
        output.append("\n\n");
        output.append(Utils.getStackTrace(e));
        output.append("\n\n");
        output.append(this.getErrormsg("C_ERRORPART_3"));
        return output.toString();
    }
    
    /**
     * This method performs the error handling for the OpenCms.
     * All CmsExetions throns in the OpenCms are forwared to this method and are
     * processed here.
     *
     * @param cms The CmsObject
     * @param cmsReq   The clints request.
     * @param cmsRes   The servlets response.
     * @param e The CmsException to be processed.
     */
    private void errorHandling(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes, CmsException e) {
        int errorType = e.getType();
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res = (HttpServletResponse)cmsRes.getOriginalResponse();
        boolean canWrite = ((!cmsRes.isRedirected()) && (!cmsRes.isOutputWritten()));
        try {
            switch(errorType) {

              // access denied error - display login dialog
              case CmsException.C_ACCESS_DENIED:
                  if(canWrite) {
                    if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Access denied. "+e.getStackTraceAsString());
                    }
                    requestAuthorization(req, res);
                  }

                  break;

              // file not found - display 404 error.
              case CmsException.C_NOT_FOUND:
                  if(canWrite) {
                    res.setContentType("text/HTML");

                    //res.getWriter().print(createErrorBox(e));
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                  }
                  break;

              case CmsException.C_SERVICE_UNAVAILABLE:
                  if(canWrite) {
                    res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.toString());
                  }
                  break;

              // https page and http request - display 404 error.
              case CmsException.C_HTTPS_PAGE_ERROR:
                  if(canWrite) {
                    if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Trying to get a http page with a https request. "+e.getMessage());
                    }
                    res.setContentType("text/HTML");
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                  }
                  break;

              // https request and http page - display 404 error.
              case CmsException.C_HTTPS_REQUEST_ERROR:
                    if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Trying to get a https page with a http request. "+e.getMessage());
                    }
                  if(canWrite) {
                    res.setContentType("text/HTML");
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                  }
                  break;

              default:
                  if(canWrite) {
                    // send errorbox, but only if the request was not redirected
                    res.setContentType("text/HTML");

                    // set some HTTP headers preventing proxy servers from caching the error box
                    res.setHeader("Cache-Control", "no-cache");
                    res.setHeader("Pragma", "no-cache");
                    res.getWriter().print(createErrorBox(e, cms));
                  }
            //res.sendError(res.SC_INTERNAL_SERVER_ERROR);
            }
        }
        catch(IOException ex) {

        }
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
     * @param req   The clints request.
     * @param res   The servlets response.
     * @return The CmsObject
     * @throws IOException Thrown if user autherization fails.
     */
    private CmsObject initUser(I_CmsRequest cmsReq, I_CmsResponse cmsRes) throws IOException {
        HttpSession session;
        String user = null;
        String group = null;
        Integer project = null;
        String loginParameter;

        // get the original ServletRequest and response
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res = (HttpServletResponse)cmsRes.getOriginalResponse();
        CmsObject cms = new CmsObject();

        //set up the default Cms object
        try {
            m_opencms.initUser(cms, cmsReq, cmsRes, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID, m_sessionStorage);

            // check if a parameter "opencms=login" was included in the request.
            // this is used to force the HTTP-Authentification to appear.
            loginParameter = cmsReq.getParameter("opencms");
            if(loginParameter != null) {
                // do only show the authentication box if user is not already
                // authenticated.
                if(req.getHeader("Authorization") == null) {
                    if(loginParameter.equals("login")) {
                        requestAuthorization(req, res);
                    }
                }
            }

            // check for the clearcache parameter
            loginParameter = cmsReq.getParameter("_clearcache");
            if(loginParameter != null) {
                cms.clearcache();
            }

            // get the actual session
            session = req.getSession(false);

            // there is no session
            if((session == null)) {
                // was there an old session-id?
                String oldSessionId = req.getRequestedSessionId();
                if(oldSessionId != null) {

                    // yes - try to load that session
                    Hashtable sessionData = null;
                    try {
                        sessionData = m_opencms.restoreSession(oldSessionId);
                    }
                    catch(CmsException exc) {
                        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) {
                            A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] cannot restore session: " + com.opencms.util.Utils.getStackTrace(exc));
                        }
                    }

                    // can the session be restored?
                    if(sessionData != null) {

                        // create a new session first
                        session = req.getSession(true);
                        m_sessionStorage.putUser(session.getId(), sessionData);

                        // restore the session-data
                        session.setAttribute(C_SESSION_DATA, sessionData.get(C_SESSION_DATA));
                    }
                }
            }

            // there was a session returned, now check if this user is already authorized
            if(session != null) {
                // get the username
                user = m_sessionStorage.getUserName(session.getId());
                //check if a user was returned, i.e. the user is authenticated
                if(user != null) {
                    group = m_sessionStorage.getCurrentGroup(session.getId());
                    project = m_sessionStorage.getCurrentProject(session.getId());
                    m_opencms.initUser(cms, cmsReq, cmsRes, user, group, project.intValue(), m_sessionStorage);
                }
            }
            else {
                // there was either no session returned or this session was not
                // found in the CmsCoreSession storage
                String auth = req.getHeader("Authorization");

                // User is authenticated, check password
                if(auth != null) {

                    // only do basic authentification
                    if(auth.toUpperCase().startsWith("BASIC ")) {

                        // Get encoded user and password, following after "BASIC "
                        String userpassEncoded = auth.substring(6);

                        // Decode it, using any base 64 decoder
                        sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
                        String userstr = new String(dec.decodeBuffer(userpassEncoded));
                        String username = null;
                        String password = null;
                        StringTokenizer st = new StringTokenizer(userstr, ":");
                        if(st.hasMoreTokens()) {
                            username = st.nextToken();
                        }
                        if(st.hasMoreTokens()) {
                            password = st.nextToken();
                        }
                        // autheification in the DB
                        try {
                            try {
                                // try to login as a user first ...
                                user = cms.loginUser(username, password);
                            } catch(CmsException exc) {
                                // login as user failed, try as webuser ...
                                user = cms.loginWebUser(username, password);
                            }
                            // authentification was successful create a session
                            session = req.getSession(true);
                            OpenCmsServletNotify notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                            session.setAttribute("NOTIFY", notify);
                        }
                        catch(CmsException e) {
                            if(e.getType() == CmsException.C_NO_ACCESS) {

                                // authentification failed, so display a login screen
                                requestAuthorization(req, res);

                            }
                            else {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
        catch(CmsException e) {
            errorHandling(cms, cmsReq, cmsRes, e);
        }
        
        return cms;
    }

    /**
     * This method sends a request to the client to display a login form.
     * It is needed for HTTP-Authentification.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
     */
    private void requestAuthorization(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String servletPath = null;
        String redirectURL = null;
        
        if (this.m_UseBasicAuthentication) {
            // HTTP basic authentication is used
            res.setHeader("WWW-Authenticate", "BASIC realm=\"OpenCms\"");
            res.setStatus(401);
        }
        else {
            // form based authentication is used, redirect the user to
            // a page with a form to enter his username and password
            servletPath = req.getContextPath() + req.getServletPath();
            redirectURL = servletPath + this.m_AuthenticationFormURI + "?requestedResource=" + req.getPathInfo();
            res.sendRedirect( redirectURL );
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
    private void updateUser(CmsObject cms, I_CmsRequest cmsReq) throws IOException {
        if (! cms.getRequestContext().isUpdateSessionEnabled()) {
            return;
        }
        
        HttpSession session = null;

        // get the original ServletRequest and response
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();

        //get the session if it is there
        session = req.getSession(false);

        // if the user was authenticated via sessions, update the information in the
        // sesssion stroage
        if((session != null)) {
            if(!cms.getRequestContext().currentUser().getName().equals(C_USER_GUEST)) {

                Hashtable sessionData = new Hashtable(4);
                sessionData.put(C_SESSION_USERNAME, cms.getRequestContext().currentUser().getName());
                sessionData.put(C_SESSION_CURRENTGROUP, cms.getRequestContext().currentGroup().getName());
                sessionData.put(C_SESSION_PROJECT, new Integer(cms.getRequestContext().currentProject().getId()));
                Hashtable oldData = (Hashtable)session.getAttribute(C_SESSION_DATA);
                if(oldData == null) {
                    oldData = new Hashtable();
                }
                sessionData.put(C_SESSION_DATA, oldData);

                // was there any change on current-user, current-group or current-project?
                boolean dirty = false;
                dirty = dirty || (!sessionData.get(C_SESSION_USERNAME).equals(m_sessionStorage.getUserName(session.getId())));
                dirty = dirty || (!sessionData.get(C_SESSION_CURRENTGROUP).equals(m_sessionStorage.getCurrentGroup(session.getId())));
                dirty = dirty || (!sessionData.get(C_SESSION_PROJECT).equals(m_sessionStorage.getCurrentProject(session.getId())));

                // update the user-data
                m_sessionStorage.putUser(session.getId(), sessionData);

                // was the session changed?
                if((session.getAttribute(C_SESSION_IS_DIRTY) != null) || dirty) {

                    // yes- store it to the database
                    session.removeAttribute(C_SESSION_IS_DIRTY);
                    try {
                        m_opencms.storeSession(session.getId(), sessionData);
                    }
                    catch(CmsException exc) {
                        if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO)) {
                            A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] cannot store session: " + com.opencms.util.Utils.getStackTrace(exc));
                        }
                    }
                }

                // check if the session notify is set, it is nescessary to remove the
                // session from the internal storage on its destruction.
                OpenCmsServletNotify notify = null;
                Object sessionValue = session.getAttribute("NOTIFY");
                if(sessionValue instanceof OpenCmsServletNotify) {
                    notify = (OpenCmsServletNotify)sessionValue;
                    if(notify == null) {
                        notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                        session.setAttribute("NOTIFY", notify);
                    }
                }
                else {
                    notify = new OpenCmsServletNotify(session.getId(), m_sessionStorage);
                    session.setAttribute("NOTIFY", notify);
                }
            }
        }
    }


    /**
     * Get the value for the property entry
     *
     * @param part the name of the property
     * @return The value of the property
     */
    public String getErrormsg(String part){
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("com/opencms/core/errormsg.properties"));
        } catch(NullPointerException exc) {
            if(A_OpenCms.isLogging(C_OPENCMS_CRITICAL) && C_LOGGING) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsHttpServlet] cannot get com/opencms/core/errormsg.properties");
            }
        } catch(java.io.IOException exc) {
            if(A_OpenCms.isLogging(C_OPENCMS_CRITICAL) && C_LOGGING) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsHttpServlet] cannot get com/opencms/core/errormsg.properties");
            }
        }
        String value = props.getProperty(part);
        return value;
    }

}

