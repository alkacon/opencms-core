/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsHttpServlet.java,v $
 * Date   : $Date: 2003/08/03 09:42:42 $
 * Version: $Revision: 1.63 $
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

package com.opencms.core;

import org.opencms.site.CmsSite;

import com.opencms.boot.CmsBase;
import com.opencms.boot.CmsMain;
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.util.Utils;
import com.opencms.workplace.I_CmsWpConstants;

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
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.63 $
 */
public class OpenCmsHttpServlet extends HttpServlet {
    
    /** Prefix for error messages for initialization errors */
    private static final String C_ERRORMSG = "OpenCms initialization error!\n\n";
    
    /** Flag for debugging */
    private static final boolean DEBUG = false;
    
    /** URI of the authentication form (read from properties) in case of form based authentication */
    private String m_authenticationFormURI;

    /** The configuration for the OpenCms servlet */
    private Configurations m_configurations;

    /** The reference to the OpenCms system */
    private OpenCms m_opencms;

    /** The session storage for all active users */
    private CmsCoreSession m_sessionStorage;
    
    /** Flag to indicate if basic or form based authentication is used */
    private boolean m_useBasicAuthentication;
    
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
    private void checkBasicAuthorization (CmsObject cms, HttpServletRequest req, HttpServletResponse res) throws IOException, CmsException {
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
                // autheification in the DB
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
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_NO_ACCESS) {

                        // authentification failed, so display a login screen
                        requestAuthorization(req, res);

                    } else {
                        throw e;
                    }
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
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] Performing shutdown ...");
        }
        try {
            m_opencms.destroy();
        } catch (Throwable e) {
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCmsServlet]" + e.toString());
            }
        }
        try {
            Utils.getModulShutdownMethods(A_OpenCms.getRegistry());
        } catch (CmsException e) {
            // log exception since we are about to shutdown anyway
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCmsServlet] Module shutdown exception: " + e);
            }
        }
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] ... shutdown completed.");
        }
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
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {         
        CmsObject cms = null;
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req, m_opencms.getFileTranslator());
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res);
        
        try {
            m_opencms.initStartupClasses(req, res);
            cms = initUser(req, res, cmsReq, cmsRes);
            // user is initialized, now deliver the requested resource
            CmsFile file = m_opencms.initResource(cms);
            if (file != null) {
                // a file was read, go on process it
                m_opencms.setResponse(cms, file);
                m_opencms.showResource(req, res, cms, file);
                updateUser(cms, cmsReq);
            }
        } catch (Throwable t) {
            errorHandling(cms, req, res, t);
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
    public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {            
        doGet(req, res);
    }
    
    /**
     * This method performs the error handling for OpenCms.<p>
     *
     * @param cms the current cms context
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

        if (t instanceof CmsException) {
            CmsException e = (CmsException)t;

            int exceptionType = e.getType();
            switch (exceptionType) {
                // access denied error - display login dialog
                case CmsException.C_ACCESS_DENIED :
                case CmsException.C_NO_ACCESS :
                    if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] Access denied: " + e.getMessage());
                    }
                    if (canWrite) {
                        try {
                            requestAuthorization(req, res);
                        } catch (IOException ioe) {
                            // there is nothing we can do about this
                        }
                        return;
                    }
                    break;

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
                    if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] Trying to get a http page with a https request. " + e.getMessage());
                    }
                    break;                                       

                case CmsException.C_HTTPS_REQUEST_ERROR :
                    // https request and http page - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] Trying to get a https page with a http request. " + e.getMessage());
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
            isNotGuest = isNotGuest || (((cms.getRequestContext().currentUser()) != null) 
                && (! I_CmsConstants.C_USER_GUEST.equals(cms.getRequestContext().currentUser().getName())) 
                && ((cms.userInGroup(cms.getRequestContext().currentUser().getName(), I_CmsConstants.C_GROUP_USERS)) 
                    || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), I_CmsConstants.C_GROUP_PROJECTLEADER)) 
                    || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), I_CmsConstants.C_GROUP_ADMIN))));
        } catch (CmsException e) {
            // result is false
        }
        
        if (canWrite) {
            res.setContentType("text/HTML");
            res.setHeader("Cache-Control", "no-cache");
            res.setHeader("Pragma", "no-cache");                
            if (isNotGuest) {
                try {
                    res.getWriter().print(createErrorBox(t, cms.getRequestContext().getRequest().getWebAppUrl()));
                } catch (IOException e) {
                    // can be ignored
                }
            } else {
                if (status < 1) status = HttpServletResponse.SC_NOT_FOUND;
                try {
                    res.sendError(status);
                } catch (IOException e) {
                    // can be ignored
                }
            }
        }
    }


    /**
     * Returns a part of the html error message dialog.<p>
     *
     * @param part the name of the piece to return
     * @return a part of the html error message dialog
     */
    public String getErrormsg(String part) {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("com/opencms/core/errormsg.properties"));
        } catch (NullPointerException exc) {
            if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) && I_CmsLogChannels.C_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCmsHttpServlet] cannot get com/opencms/core/errormsg.properties");
            }
        } catch (java.io.IOException exc) {
            if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) && I_CmsLogChannels.C_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCmsHttpServlet] cannot get com/opencms/core/errormsg.properties");
            }
        }
        String value = props.getProperty(part);
        return value;
    }
        
    /**
     * Initialization of the OpenCms servlet (overloaded Servlet API method).<p>
     *
     * The connection information for the database is read 
     * from the <code>opencms.properties</code> configuration file and all 
     * driver manager are initialized via the initalizer, 
     * which usually will be an instance of a <code>OpenCms</code> class.
     *
     * @param config configuration of OpenCms from <code>web.xml</code>
     * @throws ServletException when sevlet initalization fails
     */
    public synchronized void init(ServletConfig config) throws ServletException {
        super.init(config);
        A_OpenCms.initVersion(this);
        
        // Check for OpenCms home (base) directory path
        String base = config.getInitParameter("opencms.home");
        if (DEBUG) System.err.println("BASE: " + config.getServletContext().getRealPath("/"));
        if (DEBUG) System.err.println("BASE2: " + System.getProperty("user.dir"));
        if (base == null || "".equals(base)) {
            if (DEBUG) System.err.println("No OpenCms home folder given. Trying to guess...");
            base = CmsMain.searchBaseFolder(config.getServletContext().getRealPath("/"));
            if (base == null || "".equals(base)) {
                throwInitException(new ServletException(C_ERRORMSG + "OpenCms base folder could not be guessed. Please define init parameter \"opencms.home\" in servlet engine configuration.\n\n"));
            }
        }
        base = CmsBase.setBasePath(base);        
        
        String logFile;
        ExtendedProperties extendedProperties = null;
        
        // Collect the configurations        
        try {
            extendedProperties = new ExtendedProperties(CmsBase.getPropertiesPath(true));
        } catch (Exception e) {
            throwInitException(new ServletException(C_ERRORMSG + "Trouble reading property file " + CmsBase.getPropertiesPath(true) + ".\n\n", e));
        }
        
        // Change path to log file, if given path is not absolute
        logFile = (String)extendedProperties.get("log.file");
        if (logFile != null) {
            extendedProperties.put("log.file", CmsBase.getAbsolutePath(logFile));
        }

        // Create the configurations object
        m_configurations = new Configurations(extendedProperties);        

        // Initialize the logging
        A_OpenCms.initializeServletLogging(m_configurations);
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ".");        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ".");        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ".");        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ".");
            printCopyrightInformation();       
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ".                      ...............................................................");        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Startup time         : " + (new Date(System.currentTimeMillis())));        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Servlet container    : " + config.getServletContext().getServerInfo());        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". OpenCms version      : " + A_OpenCms.getVersionName()); 
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". OpenCms base path    : " + CmsBase.getBasePath());        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". OpenCms property file: " + CmsBase.getPropertiesPath(true));        
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". OpenCms logfile      : " + CmsBase.getAbsolutePath(logFile));        
        }
        
        try {
            // create the OpenCms object
            m_opencms = new OpenCms(m_configurations);
        } catch (CmsException cmsex) {
            if (cmsex.getType() == CmsException.C_RB_INIT_ERROR) {
                throwInitException(new ServletException(C_ERRORMSG + "Could not connect to the database. Is the database up and running?\n\n", cmsex));                
            }
        } catch (Exception exc) {
            throwInitException(new ServletException(C_ERRORMSG + "Trouble creating the com.opencms.core.CmsObject. Please check the root cause for more information.\n\n", exc));
        }

        // initalize the session storage
        m_sessionStorage = new CmsCoreSession();
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Session storage      : initialized");
             
        // check if basic or form based authentication should be used      
        m_useBasicAuthentication = m_configurations.getBoolean("auth.basic", true);        
        m_authenticationFormURI = m_configurations.getString("auth.form_uri" , I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/authenticate.html");
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
    private CmsObject initUser(HttpServletRequest req, HttpServletResponse res, I_CmsRequest cmsReq, I_CmsResponse cmsRes) throws IOException, CmsException {
        CmsObject cms = new CmsObject();

        // try to get the current session
        HttpSession session = req.getSession(false);

        // check if there is user data already stored in the session
        String user = null;
        if (session != null) {
            // session exists, try to reuse the user from the session
            user = m_sessionStorage.getUserName(session.getId());
        }
                   
        if (user != null) {
            // a user name is found in the session, reuse this user
            Integer project = m_sessionStorage.getCurrentProject(session.getId());
            // initialize the requested site root from session if available
            String siteroot = m_sessionStorage.getCurrentSite(session.getId());
            if (siteroot == null) {
                // initialize site root from request
                CmsSite site = A_OpenCms.getSiteManager().matchRequest(req);
                siteroot = site.getSiteRoot();
            }        
            m_opencms.initUser(cms, cmsReq, cmsRes, user, siteroot, project.intValue(), m_sessionStorage);
        } else {
            // initialize the requested site root
            CmsSite site = A_OpenCms.getSiteManager().matchRequest(req);
            // no user name found in session or no session, login the user as "Guest"
            m_opencms.initUser(cms, cmsReq, cmsRes, I_CmsConstants.C_USER_GUEST, site.getSiteRoot(), I_CmsConstants.C_PROJECT_ONLINE_ID, m_sessionStorage);            
            if (m_useBasicAuthentication) {
                // check if basic authorization data was provided
                checkBasicAuthorization(cms, req, res);
            }
        }
        
        // return the initialized cms user context object
        return cms;
    }

    /**
     * Prints the OpenCms copyright information to all log-files.<p>
     */
    private void printCopyrightInformation() {
        String copy[] = I_CmsConstants.C_COPYRIGHT;

        // log to error-stream
        System.err.println("\n\nStarting OpenCms, version " + A_OpenCms.getVersionName());
        for (int i = 0; i<copy.length; i++) {
            System.err.println(copy[i]);
        }

        // log with opencms-logger
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". OpenCms version " + A_OpenCms.getVersionName());
            for (int i = 0; i<copy.length; i++) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". " + copy[i]);
            }
        }
    }   

    /**
     * This method sends a request to the client to display a login form.
     * It is needed for HTTP-Authentification.
     *
     * @param req   The clints request.
     * @param res   The servlets response.
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
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, message);
        }         
        throw cause;
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
     * @throws IOException if something goes wrong
     */
    private void updateUser(CmsObject cms, I_CmsRequest cmsReq) throws IOException {
        if (! cms.getRequestContext().isUpdateSessionEnabled()) {
            return;
        }
                
        // get the original ServletRequest and response
        HttpServletRequest req = (HttpServletRequest)cmsReq.getOriginalRequest();

        //get the session if it is there
        HttpSession session = req.getSession(false);

        // if the user was authenticated via sessions, update the information in the
        // sesssion stroage
        if ((session != null)) {
            if (!cms.getRequestContext().currentUser().getName().equals(I_CmsConstants.C_USER_GUEST)) {

                Hashtable sessionData = new Hashtable(4);
                sessionData.put(I_CmsConstants.C_SESSION_USERNAME, cms.getRequestContext().currentUser().getName());
//                sessionData.put(I_CmsConstants.C_SESSION_CURRENTGROUP, cms.getRequestContext().currentGroup().getName());
                sessionData.put(I_CmsConstants.C_SESSION_PROJECT, new Integer(cms.getRequestContext().currentProject().getId()));
                sessionData.put(I_CmsConstants.C_SESSION_CURRENTSITE, cms.getRequestContext().getSiteRoot());
                
                // get current session data
                Hashtable oldData = (Hashtable)session.getAttribute(I_CmsConstants.C_SESSION_DATA);
                if (oldData == null) oldData = new Hashtable();
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

}

