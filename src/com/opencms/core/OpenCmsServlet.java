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
* @version $Revision: 1.1 $ $Date: 2000/01/06 11:52:08 $  
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
     private OpenCms m_opencms;
     
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
        
        // this method still contains debug information
        res.setContentType("text/html");
        PrintWriter out=res.getWriter();
        
        out.println("<html>");
        out.println("<head><title>Der Erdrotationshamster</title></head>");
        out.println("<body><h1>Hallo Mindfact</h1>");
        out.println("<br>"+req.getRequestURI());
        out.println("<br>"+req.getServletPath());
        out.println("<br>"+req.getPathInfo());

        try {
            CmsObject cms=initUser(req,res);
            out.println("<br>"+cms.getRequestContext().currentUser());
            CmsFile file=m_opencms.initResource(cms);
            out.println("<br><br><h2>"+new String(file.getContents())+"</h2>");
        } catch (CmsException e) {
            out.println("<br>"+e.toString());
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
         CmsObject cms=initUser(req,res);
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
    private CmsObject initUser(HttpServletRequest req, HttpServletResponse res)
      throws IOException{    
        
        HttpSession session;
        String user=null;
        String group=null;
        String project=null;
        
        CmsObject cms=new CmsObject();
        
        //set up the default Cms object
        try {
            cms.init(req,res,C_USER_GUEST,C_GROUP_GUEST, C_PROJECT_ONLINE);
     
            if (req.getParameter("LOGIN") != null) {
                // hack
                session=req.getSession(true);
                String u=req.getParameter("LOGIN");      
                m_sessionStorage.putUser(session.getId(),u);           
            }

            // get the actual session
            session=req.getSession(false);
            // there was a session returned, now check if this user is already authorized
            if (session !=null) {
                // get the username
                user=m_sessionStorage.getUserName(session.getId());
                //check if a user was returned, i.e. the user is authenticated
                if (user != null) {
                    group=m_sessionStorage.getCurrentGroup(session.getId());
                    project=m_sessionStorage.getCurrentProject(session.getId());
                    cms.init(req,res,user,group,project);
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
                            user=cms.loginUser(username,password); 
                            // check if the user is authenticated
                            if (user != null) {
                                cms.init(req,res,user,C_GROUP_GUEST, C_PROJECT_ONLINE);
                            } else {
                               // authentification failed, so display a login screen
                               requestAuthorization(req, res);
			    			 }
                        }
                    }
                }
            }
           // user was not loged in, so set it to the default user
           if (user==null) {
               // do nothing, the Cms Object is already initalized with the default
               // user and group
            }
     
       } catch (CmsException e) {
            errorHandling(req,res,e);
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
            case CmsException.C_NO_ACCESS: 
                requestAuthorization(req,res);  
                break;
            // file not found - display 404 error.
            case CmsException.C_NOT_FOUND:
                res.sendError(res.SC_NOT_FOUND);
                break;
            default:
                res.getWriter().print(e.toString());
                //res.sendError(res.SC_INTERNAL_SERVER_ERROR);
            }
          
        } catch (IOException ex) {
           
        }
    }
           

    
          
}