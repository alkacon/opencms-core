/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsServlet.java,v $
 * Date   : $Date: 2000/02/15 17:53:48 $
 * Version: $Revision: 1.16 $
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
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.file.*;


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
* @version $Revision: 1.16 $ $Date: 2000/02/15 17:53:48 $  
* 
*/

public class OpenCmsServlet extends HttpServlet implements I_CmsConstants 
{
    /**
     * The name of the property driver entry in the configuration file.
     */
     static final String C_PROPERTY_DRIVER="property.driver";
         
     /**
     * The name of the property connect string entry in the configuration file.
     */
     static final String C_PROPERTY_CONNECT="property.connectString";
     
      /**
     * The name of the initializer classname entry in the configuration file.
     */
     static final String C_INILITALIZER_CLASSNAME="initializer.classname";
     
     
     /**
      * The configuration for the OpenCms servlet.
      */
     private Configurations m_configurations;
     
     /**
      * The session storage for all active users.
      */
     private CmsSession m_sessionStorage;
 
     /**
      * The reference to the OpenCms system.
      */
     private A_OpenCms m_opencms;
     
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
           
        String propertyDriver=null;
        String propertyConnect=null;
        String initializerClassname=null;
     
        // Collect the configurations
    	try {	
            m_configurations = new Configurations (new ExtendedProperties(config.getInitParameter("properties")));
    	} catch (IOException e) {
    		throw new ServletException(e.getMessage() + ".  Properties file is: " + config.getInitParameter("properties"));
    	}
        
        // get the connect information for the property db from the configuration
        propertyDriver=(String)m_configurations.getString(C_PROPERTY_DRIVER);
        propertyConnect=(String)m_configurations.getString(C_PROPERTY_CONNECT);
        // get the classname of the initializer class
        initializerClassname=(String)m_configurations.getString(C_INILITALIZER_CLASSNAME);
        
        // invoke the OpenCms
        m_opencms=new OpenCms(propertyDriver,propertyConnect,initializerClassname);
        
        //initalize the session storage
        m_sessionStorage=new CmsSession();
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
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException {	
        
        CmsRequestHttpServlet cmsReq= new CmsRequestHttpServlet(req);
        CmsResponseHttpServlet cmsRes= new CmsResponseHttpServlet(req,res);
       
       try {
            CmsObject cms=initUser(cmsReq,cmsRes);
            CmsFile file=m_opencms.initResource(cms); 
            m_opencms.setResponse(cms,file);
            m_opencms.showResource(cms,file);
            updateUser(cms,cmsReq,cmsRes);
        } catch (CmsException e) {
            errorHandling(req,res,e);
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
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException {
	
		 //Check for content type "form/multipart" and decode it
		 String type = req.getHeader("content-type");

	     if ((type != null) && type.startsWith("multipart/form-data")){
		    req = new CmsMultipartRequest(req);
		 } 
         
         CmsRequestHttpServlet cmsReq= new CmsRequestHttpServlet(req);
         CmsResponseHttpServlet cmsRes= new CmsResponseHttpServlet(req,res);
         
       try {
            CmsObject cms=initUser(cmsReq,cmsRes);
            CmsFile file=m_opencms.initResource(cms); 
            m_opencms.setResponse(cms,file);
            m_opencms.showResource(cms,file);
            updateUser(cms,cmsReq,cmsRes);
        } catch (CmsException e) {
            errorHandling(req,res,e);
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
	 * @exception IOException Thrown if user autherization fails.
     */
    private CmsObject initUser(I_CmsRequest cmsReq, I_CmsResponse cmsRes)
      throws IOException{    
        
        HttpSession session;
        String user=null;
        String group=null;
        String project=null;
        String loginParameter;
        
        // get the original ServletRequest and response
        HttpServletRequest req=(HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res=(HttpServletResponse)cmsRes.getOriginalResponse();
        
        CmsObject cms=new CmsObject();
        
        //set up the default Cms object
        try {
            cms.init(cmsReq,cmsRes,C_USER_GUEST,C_GROUP_GUEST, C_PROJECT_ONLINE);
     
            // check if a parameter "opencms=login" was included in the request.
            // this is used to force the HTTP-Authentification to appear.
            loginParameter=req.getParameter("opencms");
            if (loginParameter != null) {
                // do only show the authentication box if user is not already 
                // authenticated.
                if (req.getHeader("Authorization") == null) {
                    if (loginParameter.equals("login")) {
                        requestAuthorization(req, res); 
                    }
                }
            }

            // get the actual session
            session=req.getSession(false);
            // there was a session returned, now check if this user is already authorized
            if (session !=null) {
             
                // get the username
                user=m_sessionStorage.getUserName(session.getId());
                System.err.println("Session authentifcation "+user.toString());
                             
                //check if a user was returned, i.e. the user is authenticated
                if (user != null) {
               
                    group=m_sessionStorage.getCurrentGroup(session.getId());
                    project=m_sessionStorage.getCurrentProject(session.getId());
                    cms.init(cmsReq,cmsRes,user,group,project);
                }
              } else {
                  
                 // there was either no session returned or this session was not 
                 // found in the CmsSession storage
       
                 String auth = req.getHeader("Authorization");
 		         // User is authenticated, check password	
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
                            user=cms.loginUser(username,password); 
                            System.err.println("HTTP authentifcation "+user.toString());
                            // authentification was successful create a session 
                            session=req.getSession(true);
                            OpenCmsServletNotify notify = new OpenCmsServletNotify(session.getId(),m_sessionStorage);
                            session.putValue("NOTIFY",notify);
                     
                            } catch (CmsException e) {
                                if (e.getType() == CmsException.C_NO_ACCESS){
                                    // authentification failed, so display a login screen
                                    requestAuthorization(req, res);
                                    System.err.println("HTTP authentifcation login required");
                                } else {
                                    throw e;               
                                }                                                                                                    
			    			 }
                        }
                 }
            } 
       } catch (CmsException e) {
            errorHandling(req,res,e);
       }
        return cms;
    }
    
    
     /**
     * Updated the the user data stored in the CmsSession after the requested document
     * is processed.<br>
     * 
     * This is nescessary if the user data (current group or project) was changed in 
     * the requested dockument. <br>
     * 
     * The user data is only updated if the user was authenticated to the system.
     * 
     * @param cms The actual CmsObject.
     * @param cmsReq The clints request.
	 * @param cmsRes The servlets response.
	 * @return The CmsObject
     */
     private void updateUser(CmsObject cms,I_CmsRequest cmsReq, I_CmsResponse cmsRes)
      throws IOException{    
        
        HttpSession session=null;
      
        // get the original ServletRequest and response
        HttpServletRequest req=(HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res=(HttpServletResponse)cmsRes.getOriginalResponse();
           
        //get the session if it is there
        session=req.getSession(false);
        // if the user was authenticated via sessions, update the information in the
        // sesssion stroage
        if (session!= null) {
             m_sessionStorage.putUser(session.getId(),
                                      cms.getRequestContext().currentUser().getName(),
                                      cms.getRequestContext().currentGroup().getName(),
                                      cms.getRequestContext().currentProject().getName());
        }                  
     }
    
    /**
     * This method sends a request to the client to display a login form.
     * It is needed for HTTP-Authentification.
     * 
     * @param req   The clints request.
	 * @param res   The servlets response.
     */
 	private void requestAuthorization(HttpServletRequest req, HttpServletResponse res) 
		throws IOException	{
		res.setHeader("WWW-Authenticate", "BASIC realm=\"OpenCmst\"");
		res.setStatus(401);
	}

    
    /**
     * This method performs the error handling for the OpenCms.
     * All CmsExetions throns in the OpenCms are forwared to this method and are
     * processed here.
     * 
	 * @param req   The clints request.
	 * @param res   The servlets response.
	 * @param e The CmsException to be processed. 
     */
    private void errorHandling(HttpServletRequest req, HttpServletResponse res, CmsException e){
        int errorType = e.getType();
        try{
            switch (errorType) {
            // access denied error - display login dialog
            case CmsException.C_ACCESS_DENIED: 
                requestAuthorization(req,res);  
                //System.err.println(e.toString());
                e.printStackTrace();
                break;
            // file not found - display 404 error.
            case CmsException.C_NOT_FOUND:
                //System.err.println(e.toString());
                res.setContentType("text/plain");
                e.printStackTrace();
                res.getWriter().print(e.toString());
                //res.sendError(res.SC_NOT_FOUND);
                break;
            case CmsException.C_SERVICE_UNAVAILABLE:
                //System.err.println(e.toString());
                res.sendError(res.SC_SERVICE_UNAVAILABLE, e.toString());
                break;
            default:
                //System.err.println(e.toString());
                res.setContentType("text/plain");
                e.printStackTrace();
                res.getWriter().print(e.toString());
                //res.sendError(res.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IOException ex) {
           
        }
    }
                 
}