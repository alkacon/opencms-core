/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsServlet.java,v $
 * Date   : $Date: 2000/04/04 15:57:59 $
 * Version: $Revision: 1.32 $
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
* @version $Revision: 1.32 $ $Date: 2000/04/04 15:57:59 $  
* 
*/

public class OpenCmsServlet extends HttpServlet implements I_CmsConstants, I_CmsLogChannels {
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
      * Database scheduler for keeping the connection alive.
      */
     private CmsSchedulerDbConnector m_schedulerDbConnector;
          
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
    	} catch (Exception e) {
    		throw new ServletException(e.getMessage() + ".  Properties file is: " + config.getInitParameter("properties"));
    	}
        
        // Initialize the logging
        A_OpenCms.initializeServletLogging(m_configurations);

		// get the connect information for the property db from the configuration
        propertyDriver=(String)m_configurations.getString(C_PROPERTY_DRIVER);
        propertyConnect=(String)m_configurations.getString(C_PROPERTY_CONNECT);

		// get the classname of the initializer class
        initializerClassname=(String)m_configurations.getString(C_INILITALIZER_CLASSNAME);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing opencms with initializer: " + initializerClassname);
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] connecting to propertyDB via " + propertyDriver);
		}
        
        // invoke the OpenCms
        m_opencms=new OpenCms(propertyDriver,propertyConnect,initializerClassname);

        
        // build the database scheduler for keeping connections alive
        CmsObject cms=new CmsObject();
        try {
            cms.init(null, null, C_USER_ADMIN, C_GROUP_ADMIN, C_PROJECT_ONLINE_ID);
    	} catch (CmsException e) {
    		throw new ServletException("Could not initialize cms object for DB scheduler. " + e);
    	}
        m_schedulerDbConnector = new CmsSchedulerDbConnector(cms, 120);
        m_schedulerDbConnector.start();
        
        //initalize the session storage
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing session storage");
		}
        m_sessionStorage=new CmsSession();
        
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing... DONE");
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
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException {	
        
        CmsObject cms=null;
        
        CmsRequestHttpServlet cmsReq= new CmsRequestHttpServlet(req);
        CmsResponseHttpServlet cmsRes= new CmsResponseHttpServlet(req,res);

        try {
           cms=initUser(cmsReq,cmsRes);
           CmsFile file=m_opencms.initResource(cms); 
           m_opencms.setResponse(cms,file);
           m_opencms.showResource(cms,file);
           updateUser(cms,cmsReq,cmsRes);
        } catch (CmsException e) {
            errorHandling(cms,cmsReq,cmsRes,e);
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

         CmsObject cms=null;
	     if ((type != null) && type.startsWith("multipart/form-data")){
		  //  req = new CmsMultipartRequest(req);
		 }
         
         CmsRequestHttpServlet cmsReq= new CmsRequestHttpServlet(req);
         CmsResponseHttpServlet cmsRes= new CmsResponseHttpServlet(req,res);
         
       try {
            cms=initUser(cmsReq,cmsRes);
            CmsFile file=m_opencms.initResource(cms); 
            m_opencms.setResponse(cms,file);
            m_opencms.showResource(cms,file);
            updateUser(cms,cmsReq,cmsRes);
        } catch (CmsException e) {
            errorHandling(cms,cmsReq,cmsRes,e);
        } 
	}

    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_INFO, "[OpenCmsServlet] Performing Shutdown....");
        }
        m_schedulerDbConnector.destroy();
			
        if(A_OpenCms.isLogging()) {
	        A_OpenCms.log(C_OPENCMS_CRITICAL, "[OpenCmsServlet] Shutdown Completed");
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
        Integer project=null;
        String loginParameter;
        
        // get the original ServletRequest and response
        HttpServletRequest req=(HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res=(HttpServletResponse)cmsRes.getOriginalResponse();
        
        CmsObject cms=new CmsObject();
        
        //set up the default Cms object
        try {
            cms.init(cmsReq,cmsRes,C_USER_GUEST,C_GROUP_GUEST, C_PROJECT_ONLINE_ID);
     
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
                //System.err.println("Session authentifcation "+user.toString());
                             
                //check if a user was returned, i.e. the user is authenticated
                if (user != null) {
               
                    group=m_sessionStorage.getCurrentGroup(session.getId());
                    project=m_sessionStorage.getCurrentProject(session.getId());
                    cms.init(cmsReq,cmsRes,user,group,project.intValue());
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
                            // System.err.println("HTTP authentifcation "+user.toString());
                            // authentification was successful create a session 
                            session=req.getSession(true);
                            OpenCmsServletNotify notify = new OpenCmsServletNotify(session.getId(),m_sessionStorage);
                            session.putValue("NOTIFY",notify);
                     
                            } catch (CmsException e) {
                                if (e.getType() == CmsException.C_NO_ACCESS){
                                    // authentification failed, so display a login screen
                                    requestAuthorization(req, res);
                                    // System.err.println("HTTP authentifcation login required");
                                } else {
                                    throw e;               
                                }                                                                                                    
			    			 }
                        }
                 }
            } 
       } catch (CmsException e) {
            errorHandling(cms,cmsReq,cmsRes,e);
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
           
        //get the session if it is there
        session=req.getSession(false);
        // if the user was authenticated via sessions, update the information in the
        // sesssion stroage
        if (session!= null) {
             m_sessionStorage.putUser(session.getId(),
                                      cms.getRequestContext().currentUser().getName(),
                                      cms.getRequestContext().currentGroup().getName(),
									  new Integer(cms.getRequestContext().currentProject().getId()));
             
             // check if the session notify is set, it is nescessary to remove the
             // session from the internal storage on its destruction.             
             OpenCmsServletNotify notify = (OpenCmsServletNotify)session.getValue("NOTIFY");
             if (notify == null) {
                notify = new OpenCmsServletNotify(session.getId(),m_sessionStorage);
                session.putValue("NOTIFY",notify);                  
             }
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
		res.setHeader("WWW-Authenticate", "BASIC realm=\"OpenCms\"");
		res.setStatus(401);
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
     private void errorHandling(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes,
                                CmsException e){
        int errorType = e.getType();
        
        HttpServletRequest req=(HttpServletRequest)cmsReq.getOriginalRequest();
        HttpServletResponse res=(HttpServletResponse)cmsRes.getOriginalResponse();
        
        try{
            switch (errorType) {
            // access denied error - display login dialog
            case CmsException.C_ACCESS_DENIED: 
                requestAuthorization(req,res); 
                e.printStackTrace();
                break;
            // file not found - display 404 error.
            case CmsException.C_NOT_FOUND:
                res.setContentType("text/HTML");
                res.getWriter().print(createErrorBox(e));
                //res.sendError(res.SC_NOT_FOUND);
                break;
            case CmsException.C_SERVICE_UNAVAILABLE:
                res.sendError(res.SC_SERVICE_UNAVAILABLE, e.toString());
                break;
            default:
                res.setContentType("text/HTML");
                e.printStackTrace();
                res.getWriter().print(createErrorBox(e));
                //res.sendError(res.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IOException ex) {
           
        }
    }
     
    /**
      * Generates a formated exception output. <br>
      * Because the exception could be thrown while accessing the system files,
      * the complete HTML code must be added here!
      * @param e The caught CmsException.
      * @return String containing the HTML code of the error message.
      */
     private String createErrorBox(CmsException e) {
		StringBuffer output=new StringBuffer();
		output.append("<HTML>\n");
		output.append("<script language=JavaScript>\n");
		output.append("<!--\n");
		output.append("function show_help()\n");
		output.append("{\n");
		output.append("return '2_2_2_2_5_5_1.html';\n");
		output.append("}\n");
		output.append("var shown=false;\n");
		output.append("function checkBrowser(){\n");
		output.append("this.ver=navigator.appVersion\n");
		output.append("this.dom=document.getElementById?1:0\n");
		output.append("this.ie5=(this.ver.indexOf(\"MSIE 5\")>-1 && this.dom)?1:0;\n");
		output.append("this.ie4=(document.all && !this.dom)?1:0;\n");
		output.append("this.ns5=(this.dom && parseInt(this.ver) >= 5) ?1:0;\n");
		output.append("this.ns4=(document.layers && !this.dom)?1:0;\n");
		output.append("this.bw=(this.ie5 || this.ie4 || this.ns4 || this.ns5)\n");
		output.append("return this\n");
		output.append("}\n");
		output.append("bw=new checkBrowser();\n");
		output.append("function makeObj(obj,nest){\n");
		output.append("nest=(!nest) ? '':'document.'+nest+'.';\n");
		output.append("this.el=bw.dom?document.getElementById(obj):bw.ie4?document.all[obj]:bw.ns4?eval(nest+'document.'+obj):0;\n");
		output.append("this.css=bw.dom?document.getElementById(obj).style:bw.ie4?document.all[obj].style:bw.ns4?eval(nest+'document.'+obj):0;\n");
		output.append("this.scrollHeight=bw.ns4?this.css.document.height:this.el.offsetHeight; \n");
		output.append("this.clipHeight=bw.ns4?this.css.clip.height:this.el.offsetHeight;\n");
		output.append("this.obj = obj + \"Object\";\n");
		output.append("eval(this.obj + \"=this\");\n");
		output.append("return this;\n");
		output.append("}\n");
		output.append("ns = (document.layers)? true:false;\n");
		output.append("ie = (document.all)? true:false;\n");
		output.append("if(ns)\n");
		output.append("{\n");
		output.append("var layerzeigen_01= 'document.layers.';\n");
		output.append("var layerzeigen_02= '.visibility=\"show\"';\n");
		output.append("var layerverstecken_01= 'document.layers.';\n");
		output.append("var layerverstecken_02= '.visibility=\"hide\"';\n");
		output.append("}\n");
		output.append("else\n");
		output.append("{\n");
		output.append("var layerzeigen_01= 'document.all.';\n");
		output.append("var layerzeigen_02= '.style.visibility=\"visible\"';\n");
		output.append("var layerverstecken_01= 'document.all.';\n");
		output.append("var layerverstecken_02= '.style.visibility=\"hidden\"';\n");
		output.append("}\n");
		output.append("function getWindowWidth() {\n");
		output.append("if( ns==true ) {\n");
		output.append("windowWidth = innerWidth;\n");
		output.append("}\n");
		output.append("else if( ie==true ) {\n");
		output.append("windowWidth = document.body.clientWidth;\n");
		output.append("}\n");
		output.append("return windowWidth;\n");
		output.append("}\n");
		output.append("function centerLayer(layerName){\n");
		output.append("totalWidth=getWindowWidth();\n");
		output.append("if (ie) {\n");
		output.append("eval('lyrWidth = document.all[\"'+ layerName +'\"].offsetWidth');\n");
		output.append("eval('document.all[\"'+ layerName +'\"].style.left=Math.round((totalWidth-lyrWidth)/2)');\n");
		output.append("}\n");
		output.append("else if (ns) {\n");
		output.append("eval('lyrWidth = document[\"'+ layerName +'\"].clip.width');\n");
		output.append("eval('document[\"'+ layerName +'\"].left=Math.round((totalWidth-lyrWidth)/2)');\n");
		output.append("}\n");
		output.append("}\n");
		output.append("function justifyLayers(lyr1,lyr2) {\n");
		output.append("if (ie) {\n");
		output.append("eval('y1 = document.all[\"'+ lyr1 +'\"].offsetTop');\n");
		output.append("eval('lyrHeight = document.all[\"'+ lyr1 +'\"].offsetHeight');\n");
		output.append("y2 = y1 + lyrHeight;\n");
		output.append("eval('document.all[\"'+ lyr2 +'\"].style.top = y2-22');\n");
		output.append("}\n");
		output.append("else if (ns) {\n");
		output.append("eval('y1 = document[\"'+ lyr1 +'\"].top');\n");
		output.append("eval('lyrHeight = document[\"'+ lyr1 +'\"].clip.height');\n");
		output.append("y2 = y1 + lyrHeight; \n");
		output.append("eval('document[\"'+ lyr2 +'\"].top = y2-22');\n");
		output.append("}\n");
		output.append("}\n");
		output.append("function chInputTxt(formName,field,txt,layerName)\n");
		output.append("{\n");
		output.append("if (ie || layerName==null)\n");
		output.append("eval('document.' + formName +'.'+ field + '.value=\"'+ txt +'\";');\n");
		output.append("else if (ns && layerName!=null) \n");
		output.append("eval('document[\"'+layerName+'\"].document.forms[\"'+formName+'\"].'+ field + '.value=\"'+ txt +'\";');\n");
		output.append("}\n");
		output.append("function chButtonTxt(formName,field,txt1,txt2,layerName)\n");
		output.append("{\n");
		output.append("if (shown)\n");
		output.append("chInputTxt(formName,field,txt2,layerName);\n");
		output.append("else\n");
		output.append("chInputTxt(formName,field,txt1,layerName);\n");
		output.append("}\n");
		output.append("function newObj(layerName,visibility){\n");
		output.append("eval('oCont=new makeObj(\"'+ layerName +'\")');\n");
		output.append("eval('oCont.css.visibility=\"'+ visibility +'\"');\n");
		output.append("eval('centerLayer(\"'+ layerName +'\")');\n");
		output.append("}\n");
		output.append("function showlyr(welche)\n");
		output.append("{\n");
		output.append("eval(layerzeigen_01+welche+layerzeigen_02);\n");
		output.append("}\n");
		output.append("function hidelyr(welche)\n");
		output.append("{\n");
		output.append("eval(layerverstecken_01+welche+layerverstecken_02);\n");
		output.append("}\n");
		output.append("function toggleLayer(layerName)\n");
		output.append("{\n");
		output.append("if (shown) {\n");
		output.append("eval('hidelyr(\"'+ layerName +'\")');\n");
		output.append("shown=false;\n");
		output.append("}\n");
		output.append("else {\n");
		output.append("eval('showlyr(\"'+ layerName +'\")');\n");
		output.append("shown=true;\n");
		output.append("}\n");
		output.append("}\n");
		output.append("function resized(){\n");
		output.append("pageWidth2=bw.ns4?innerWidth:document.body.offsetWidth;\n");
		output.append("pageHeight2=bw.ns4?innerHeight:document.body.offsetHeight;\n");
		output.append("if(pageWidth!=pageWidth2 || pageHeight!=pageHeight2) location.reload();\n");
		output.append("}\n");
		output.append("function errorinit() {\n");
		output.append("newObj('errormain','hidden');\n");
		output.append("newObj('errordetails','hidden');\n");
		output.append("justifyLayers('errormain','errordetails');\n");
		output.append("showlyr('errormain');\n");
		output.append("pageWidth=bw.ns4?innerWidth:document.body.offsetWidth;\n");
		output.append("pageHeight=bw.ns4?innerHeight:document.body.offsetHeight;\n");
		output.append("window.onresize=resized;\n");
		output.append("}\n");
		output.append("//-->\n");
		output.append("</script>\n");
		output.append("<head>\n");
		output.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\">\n");
		output.append("<title>OpenCms - Systemfehler</title>\n");
		output.append("<style type=\"text/css\">\n");
		output.append("TD.dialogtxt\n");
		output.append("{\n");
		output.append("BACKGROUND-COLOR: #c0c0c0;\n");
		output.append("COLOR: #000000;\n");
		output.append("FONT-FAMILY: MS sans serif,arial,helvetica,sans-serif;\n");
		output.append("FONT-SIZE: 8px;\n");
		output.append("FONT-WEIGHT: normal\n");
		output.append("}\n");
		output.append("TD.head\n");
		output.append("{\n");
		output.append("BACKGROUND-COLOR: #000066;\n");
		output.append("COLOR: white;\n");
		output.append("FONT-FAMILY: MS Sans Serif, Arial, helevitca, sans-serif;\n");
		output.append("FONT-SIZE: 8px;\n");
		output.append("FONT-WEIGHT: bold\n");
		output.append("}\n");
		output.append("TD.leerzeile\n");
		output.append("{\n");
		output.append("BACKGROUND-COLOR: #c0c0c0;\n");
		output.append("HEIGHT: 3px;\n");
		output.append("PADDING-BOTTOM: 0px;\n");
		output.append("PADDING-LEFT: 10px;\n");
		output.append("PADDING-RIGHT: 10px\n");
		output.append("}\n");
		output.append("TD.formular\n");
		output.append("{\n");
		output.append("BACKGROUND-COLOR: #c0c0c0;\n");
		output.append("COLOR: black;\n");
		output.append("FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif;\n");
		output.append("FONT-SIZE: 8px;\n");
		output.append("FONT-WEIGHT: bold\n");
		output.append("}\n");
		output.append("INPUT.button\n");
		output.append("{\n");
		output.append("COLOR: black;\n");
		output.append("FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif;\n");
		output.append("FONT-SIZE: 8px;\n");
		output.append("FONT-WEIGHT: normal;\n");
		output.append("WIDTH: 100px;\n");
		output.append("}\n");
		output.append("TEXTAREA.textarea\n");
		output.append("{\n");
		output.append("COLOR: #000000;\n");
		output.append("FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif;\n");
		output.append("FONT-SIZE: 8px;\n");
		output.append("WIDTH: 300px\n");
		output.append("}\n");
		output.append("#errormain{position:absolute; top:78; left:0; width:350; visibility:hidden; z-index:10; }\n");
		output.append("#errordetails{position:absolute; top:78; left:0; width:350; visibility:hidden; z-index:100}\n");
		output.append("</style>\n");
		output.append("</HEAD>\n");
		output.append("<BODY onLoad=\"errorinit();\" bgcolor=\"#ffffff\" marginwidth = 0 marginheight = 0 topmargin=0 leftmargin=0>\n");
		output.append("<div id=\"errormain\">\n");
		output.append("<!-- Tabellenaufbau für Dialog mit OK und Abbrechen-->\n");
		output.append("<form id=ERROR name=ERROR>\n");
		output.append("<table cellspacing=0 cellpadding=0 border=2 width=350>\n");
		output.append("<tr><td class=dialogtxt>\n");
		output.append("<table cellspacing=0 cellpadding=5 border=0 width=100% height=100%>\n");
		output.append("<!-- Beginn Tabellenkopf blau-->\n");
		output.append("<tr> \n");
		output.append("<td colspan=2 class=\"head\">Systemfehler</td>\n");
		output.append("</tr>\n");
		output.append("<!-- Ende Tabellenkopf blau-->\n");
		output.append("<!-- Beginn Leerzeile-->\n");
		output.append("<tr> <td colspan=2 class=\"leerzeile\">&nbsp;</td></tr>\n");
		output.append("<!-- Ende Leerzeile-->\n");
		output.append("<tr> \n");
		output.append("<td class=\"dialogtxt\" rowspan=3 valign=top><IMG border=0 src=\"/pics/system/ic_caution.gif\" width=32 height=32></td>\n");
		output.append("<td class=dialogtxt>Ein Systemfehler ist aufgetreten.</td>\n");
		output.append("</tr>	\n");
		output.append("<tr><td class=dialogtxt>F&uuml;r weitere Informationen klicken Sie auf \"Details anzeigen\" oder kontaktieren Sie Ihren Systemadministrator.</td></tr>\n");
		output.append("<!-- Beginn Leerzeile-->\n");
		output.append("<tr> <td class=\"leerzeile\">&nbsp;</td></tr>\n");
		output.append("<!-- Ende Leerzeile-->\n");
		output.append("<!-- Beginn Buttonleisete-->\n");
		output.append("<tr><td class=dialogtxt colspan=2>\n");
		output.append("<table cellspacing=0 cellpadding=5 width=100%>\n");
		output.append("<tr>\n");
		output.append("<td class=formular align=center><INPUT class=button width = 100 type=\"button\" value=\"OK\" id=OK name=OK onClick=\"location.href='explorer_files_projekt.html'\"></td>\n");
		output.append("<td class=formular align=center><INPUT class=button width = 100 type=\"button\" value=\"Details anzeigen\" id=details name=details onClick=\"toggleLayer('errordetails');chButtonTxt('ERROR','details','Details anzeigen','Details verbergen','errormain')\"></td>\n");
		output.append("</tr>\n");
		output.append("</table>\n");
		output.append("</td>\n");
		output.append("</tr>\n");
		output.append("</table>\n");
		output.append("<!-- Ende Buttonleisete-->	\n");
		output.append("</td></tr>\n");
		output.append("</table>\n");
		output.append("</form>\n");
		output.append("</div>\n");
		output.append("<div id=\"errordetails\">\n");
		output.append("<form id=DETAILS name=DETAILS>\n");
		output.append("<table cellspacing=0 cellpadding=0 border=2 width=350>\n");
		output.append("<tr><td class=dialogtxt>\n");
		output.append("<table cellspacing=1 cellpadding=5 border=0 width=100% height=100%>\n");
		output.append("<tr><td class=formular>Fehlermeldung:</td>\n");
		output.append("<tr><td align=center class=dialogtxt>\n");
		output.append("<textarea wrap=off cols=33 rows=6 style=\"width:330\" class=\"textarea\" onfocus=\"this.blur();\" id=EXCEPTION name=EXCEPTION>");

		// print the stack-trace into a writer, to get its content
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);
		
		if (e.getException() != null){
			e.getException().printStackTrace(writer);
		}
		
		writer.close();
		stringWriter.close();
		output.append(stringWriter.toString());
		
		output.append("</textarea>\n");
		output.append("</td></tr>\n");
		output.append("</table>\n");
		output.append("</td></tr>\n");
		output.append("</table>\n");
		output.append("</form>\n");
		output.append("</div> \n");
		output.append("</BODY>\n");
		output.append("</html>\n");
		return output.toString();
     }
 }