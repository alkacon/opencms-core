package com.opencms.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.file.*;

public class OpenCms extends HttpServlet implements I_CmsConstants 
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
      * The reference to the resource broker
      */
     private I_CmsResourceBroker m_rb;
     
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
        
        // invoke the ResourceBroker via the initalizer
        try {
  		    m_rb = ((A_CmsInit) Class.forName(initializerClassname).newInstance() ).init(propertyDriver, propertyConnect);
            CmsObject cms=new CmsObject();
            cms.init(m_rb);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());    
        }
        
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
             
        res.setContentType("text/html");
        PrintWriter out=res.getWriter();
        
        out.println("<html>");
        out.println("<head><title>Der Erdrotationshamster</title></head>");
        out.println("<body><h1>Hallo Mindfact</h1>");
        out.println("<br>"+req.getRequestURI());
              
  
        initUser(req,res);
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
	
		 }
         
        initUser(req,res);
	}
    
    
    private void initUser(HttpServletRequest req, HttpServletResponse res)
      throws IOException{    
        
        HttpSession session;
        String user=null;
        CmsObject cms=new CmsObject();
        
        //set up the default Cms object
        try {
            cms.init(null,null,C_USER_GUEST,C_GROUP_GUEST, C_PROJECT_ONLINE);
             
            PrintWriter out=res.getWriter();
            if (req.getParameter("LOGIN") != null) {
                // hack
                session=req.getSession(true);
                String u=req.getParameter("LOGIN");      
                m_sessionStorage.putUser(session,u);           
                out.println("Adding user <br><br><br>");
                out.println(m_sessionStorage.toString()+"<br><br>");
            }

            // get the actual session
            session=req.getSession(false);
            // there was a session returned, now check if this user is already authorized
            if (session !=null) {
                out.println("Got session: "+session.getId()+"<br>");
                // get the username
                user=m_sessionStorage.getUserName(session);
                out.println("Got user: "+user+"<br>");
            }
            // there was either no session returned or this session was not found in the
            // CmsSession storage
            if (user==null) {
                out.println("No Session found, trying HTTP Authent<br>");
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
                        out.println(user);                
                        if (user == null) {
                            out.println("Unknown user");
						    requestAuthorization(req, res);
						 }
                    }
                }
            }
           // user was not loged in, so set it to the default user
           if (user==null) {
               out.println("User not logged in");
            }
       } catch (CmsException e) {
            errorHandling(req,res,e);
       }
    }

 	public void requestAuthorization(HttpServletRequest req, HttpServletResponse res) 
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
     // to do
    }
            
          
}