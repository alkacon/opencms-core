/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsHttpServlet.java,v $
* Date   : $Date: 2002/08/21 11:32:45 $
* Version: $Revision: 1.28 $
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
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.boot.*;
import com.opencms.file.*;
import com.opencms.util.*;

/**
 * This class is the main servlet of the OpenCms system.
 * <p>
 * From here, all other operations are invoked.
 * It initializes the Servlet and processes all requests send to the OpenCms.
 * Any incoming request is handled in multiple steps:
 * <ul>
 * <li>The requesting user is authenticated and a CmsObject with the user information
 * is created. The CmsObject is needed to access all functions of the OpenCms, limited by
 * the actual user rights. If the user is not identified, it is set to the default (guest)
 * user. </li>
 * <li>The requested document is loaded into the OpenCms and depending on its type and the
 * users rights to display or modify it, it is send to one of the OpenCms launchers do
 * display it. </li>
 * <li>
 * The document is forwared to a template class which is selected by the launcher and the
 * output is generated.
 * </li>
 * </ul>
 * <p>
 * The class overloades the standard Servlet methods doGet and doPost to process
 * Http requests.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.28 $ $Date: 2002/08/21 11:32:45 $
 *
 * */
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
    private A_OpenCms m_opencms;

    /**
     * Storage for redirects.
     */
    private Vector m_redirect = new Vector();

    /**
     * Storage for redirect locations.
     */
    private Vector m_redirectlocation = new Vector();

    /**
     * Storage for the clusterurl
     */
    private String m_clusterurl = null;
    
    private boolean m_UseBasicAuthentication;
    private String m_AuthenticationFormURI;

    /**
     * Checks if the requested resource must be redirected to the server docroot and
     * excecutes the redirect if nescessary.
     * @param cms The CmsObject
     * @returns true, if the ressource was redirected
     * @exeption Throws CmsException if something goes wrong.
     */
    private boolean checkRelocation(CmsObject cms) throws CmsException {
        CmsRequestContext context = cms.getRequestContext();

        // check the if the current project is the online project. Only in this project,
        // a redirect is nescessary.
        if(context.currentProject().equals(cms.onlineProject())) {
            String filename = context.getUri();

            // check all redirect locations
            for(int i = 0;i < m_redirect.size();i++) {
                String redirect = (String)m_redirect.elementAt(i);

                // found a match, so redirect
                if(filename.startsWith(redirect)) {
                    String redirectlocation = (String)m_redirectlocation.elementAt(i);
                    String doRedirect = redirectlocation + filename.substring(redirect.length());

                    // try to redirect
                    try {
                        ((HttpServletResponse)context.getResponse().getOriginalResponse()).sendRedirect(doRedirect);
                    }
                    catch(Exception e) {
                        throw new CmsException("Redirect fails :" + doRedirect, CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    // the ressource was redirected, return true
                    return true;
                } else {
                    // the ressource was not redirected, return false
                    return false;
                }
            }
        }
        // not in online-project, or no redirect information found - so no redirect needed
        return false;
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
        output.append(Utils.getStackTrace(e));
        output.append(this.getErrormsg("C_ERRORPART_3"));
        return output.toString();
    }

    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Performing Shutdown....");
        }
        try {
            m_opencms.destroy();
        }catch(CmsException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet]" + e.toString());
            }
        }
        try{
            Utils.getModulShutdownMethods(OpenCms.getRegistry());
        }catch (CmsException e){
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet] Shutdown Completed");
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
     * @exception ServletException Thrown if request fails.
     * @exception IOException Thrown if user autherization fails.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {         
        // start time of this request
        if(req.getRequestURI().indexOf("system/workplace/action/login.html") > 0) {
            HttpSession session = req.getSession(false);
            if(session != null) {
                session.invalidate();
            }
        }
        CmsObject cms = null;
        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req);
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res, m_clusterurl);
        try {
            m_opencms.initStartupClasses();
            cms = initUser(cmsReq, cmsRes);
            
            if( !checkRelocation(cms) ) {
                // no redirect was done - deliver the ressource normally
                CmsFile file = m_opencms.initResource(cms);
                if(file != null) {

                    // If the CmsFile object is null, the resource could not be found.
                    // Stop processing in this case to avoid NullPointerExceptions
                    m_opencms.setResponse(cms, file);
                    m_opencms.showResource(cms, file);
                    updateUser(cms, cmsReq, cmsRes);
                }
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
     * @exception ServletException Thrown if request fails.
     * @exception IOException Thrown if user autherization fails.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {            
        //Check for content type "form/multipart" and decode it
        String type = req.getHeader("content-type");
        CmsObject cms = null;

        CmsRequestHttpServlet cmsReq = new CmsRequestHttpServlet(req);
        CmsResponseHttpServlet cmsRes = new CmsResponseHttpServlet(req, res, m_clusterurl);
        try {
            m_opencms.initStartupClasses();
            cms = initUser(cmsReq, cmsRes);
            
            if( !checkRelocation(cms) ) {
                // no redirect was done - deliver the ressource normally
                CmsFile file = m_opencms.initResource(cms);
                if(file != null) {

                    // If the CmsFile object is null, the resource could not be found.
                    // Stop processing in this case to avoid NullPointerExceptions
                    m_opencms.setResponse(cms, file);
                    m_opencms.showResource(cms, file);
                    updateUser(cms, cmsReq, cmsRes);
                }
            }
        }
        catch(CmsException e) {
            errorHandling(cms, cmsReq, cmsRes, e);
        }
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
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
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
                    res.sendError(res.SC_NOT_FOUND);
                  }
                  break;

              case CmsException.C_SERVICE_UNAVAILABLE:
                  if(canWrite) {
                    res.sendError(res.SC_SERVICE_UNAVAILABLE, e.toString());
                  }
                  break;

              // https page and http request - display 404 error.
              case CmsException.C_HTTPS_PAGE_ERROR:
                  if(canWrite) {
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Trying to get a http page with a https request. "+e.getMessage());
                    }
                    res.setContentType("text/HTML");
                    res.sendError(res.SC_NOT_FOUND);
                  }
                  break;

              // https request and http page - display 404 error.
              case CmsException.C_HTTPS_REQUEST_ERROR:
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Trying to get a https page with a http request. "+e.getMessage());
                    }
                  if(canWrite) {
                    res.setContentType("text/HTML");
                    res.sendError(res.SC_NOT_FOUND);
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
     * Initialization of the OpenCms servlet.
     * Used instead of a constructor (Overloaded Servlet API method)
     * <p>
     * The connection information for the property database is read from the configuration
     * file and all resource brokers are initialized via the initalizer.
     *
     * @param config Configuration of OpenCms.
     * @exception ServletException Thrown when sevlet initalization fails.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        String base = config.getInitParameter("opencms.home");
            System.err.println("BASE: " + config.getServletContext().getRealPath("/"));
            System.err.println("BASE2: " + System.getProperty("user.dir"));
        if(base == null || "".equals(base)) {
            System.err.println("No OpenCms home folder given. Trying to guess...");
            base = CmsMain.searchBaseFolder(config.getServletContext().getRealPath("/"));
            if(base == null || "".equals(base)) {
                throw new ServletException("OpenCms base folder could not be guessed. Please define init parameter \"opencms.home\" in servlet engine configuration.");
            }
        }
        base = CmsBase.setBasePath(base);        
        
        // Collect the configurations
        try {
            ExtendedProperties p = new ExtendedProperties(CmsBase.getPropertiesPath(true));

            // Change path to log file, if given path is not absolute
            String logFile = (String)p.get("log.file");
            if(logFile != null) {
                p.put("log.file", CmsBase.getAbsolutePath(logFile));
            }

            m_configurations = new Configurations(p);
        }
        catch(Exception e) {
            throw new ServletException(e.getMessage() + ".  Properties file is: " + CmsBase.getBasePath() + "opencms.properties");
        }

        // Initialize the logging
        A_OpenCms.initializeServletLogging(m_configurations);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] Server Info: " + config.getServletContext().getServerInfo());
        }

        // initialize the redirect information
        int count = 0;
        String redirect;
        String redirectlocation;
        while((redirect = (String)m_configurations.getString(C_PROPERTY_REDIRECT + "." + count)) != null) {
            redirectlocation = (String)m_configurations.getString(C_PROPERTY_REDIRECTLOCATION + "." + count);
            redirectlocation = Utils.replace(redirectlocation, C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            m_redirect.addElement(redirect);
            m_redirectlocation.addElement(redirectlocation);
            count++;
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] redirect-rule: " + redirect + " -> " + redirectlocation);
            }
        }
        m_clusterurl = (String)m_configurations.getString(C_CLUSTERURL, "");
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] Clusterurl: " + m_clusterurl);
        }
        
        try {
            // invoke the OpenCms
            m_opencms = new OpenCms(m_configurations);
        } catch(Exception exc) {
            throw new ServletException(Utils.getStackTrace(exc));
        }

        // initalize the session storage
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing session storage");
        }
        m_sessionStorage = new CmsCoreSession();
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing... DONE");
        }

        source.org.apache.java.util.Configurations openCmsConfig = m_opencms.getConfiguration();
        this.m_UseBasicAuthentication = openCmsConfig.getBoolean( "auth.basic", true );        
        this.m_AuthenticationFormURI = openCmsConfig.getString( "auth.form_uri" , "/system/workplace/action/authenticate.html" );		        

        
        /**
         * MERGE: This was removed in FLEX branch, let's see if it is stell needed...
         * 
        // create the cms-object for the class-loader to read classes from the vfs
        CmsObject cms = new CmsObject();
        try {
            // m_sessionStorage.getClass().
            m_opencms.initUser(cms, null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID, m_sessionStorage);
        } catch (CmsException exc) {
            throw new ServletException("Error while initializing the cms-object for the classloader", exc);
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing CmsClassLoader ");
        }
        // get the repositories for the classloader
        Vector repositories = new Vector();
        String[] repositoriesFromConfigFile = null;
        String[] repositoriesFromRegistry = null;

        // add repositories from the configuration file
        repositoriesFromConfigFile = cms.getConfigurations().getStringArray("repositories");
        for(int i = 0;i < repositoriesFromConfigFile.length;i++) {
            repositories.addElement(repositoriesFromConfigFile[i]);
        }

        // add the repositories from the registry, if it is available
        I_CmsRegistry reg = null;
        try{
            reg = cms.getRegistry();
        }catch(CmsException e){
             throw new ServletException(e.getMessage());
        }
        CmsClassLoader loader = (CmsClassLoader) (getClass().getClassLoader());
        if(reg != null) {
            repositoriesFromRegistry = reg.getRepositories();
            for(int i = 0;i < repositoriesFromRegistry.length;i++) {
                try {
                    cms.readFileHeader(repositoriesFromRegistry[i]);
                    //repositories.addElement(repositoriesFromRegistry[i]);
                    loader.addRepository(repositoriesFromRegistry[i], CmsClassLoader.C_REPOSITORY_VIRTUAL_FS);
                }
                catch(CmsException e) {
                // this repository was not found, do do not add it to the repository list
                // no exception handling is nescessary here.
                }
            }
        }
        loader.init(cms);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing CmsClassLoader... DONE");
        }
        try{
            Utils.getModulStartUpMethods(cms);
        } catch(CmsException e){}
         *
         */
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
     * @exception IOException Thrown if user autherization fails.
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
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] cannot restore session: " + com.opencms.util.Utils.getStackTrace(exc));
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
     * Updated the the user data stored in the CmsCoreSession after the requested document
     * is processed.<br>
     *
     * This is nescessary if the user data (current group or project) was changed in
     * the requested document. <br>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms The actual CmsObject.
     * @param cmsReq The clints request.
     * @param cmsRes The servlets response.
     * @return The CmsObject
     */
    private void updateUser(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes) throws IOException {
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
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[OpenCmsServlet] cannot store session: " + com.opencms.util.Utils.getStackTrace(exc));
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
            if(A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCmsHttpServlet] cannot get com/opencms/core/errormsg.properties");
            }
        } catch(java.io.IOException exc) {
            if(A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCmsHttpServlet] cannot get com/opencms/core/errormsg.properties");
            }
        }
        String value = props.getProperty(part);
        return value;
    }

}

