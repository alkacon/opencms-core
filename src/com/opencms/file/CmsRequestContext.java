package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This class gains access to the CmsRequestContext. 
 * <p>
 * In the request context are all methods bundeled, which can inform about the
 * current request, such like url or uri.
 * <p>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.7 $ $Date: 2000/01/11 19:07:50 $
 * 
 */
public class CmsRequestContext extends A_CmsRequestContext implements I_CmsConstants {

	/**
	 * The rb to get access to the OpenCms.
	 */
	private I_CmsResourceBroker m_rb;
	
	/**
	 * The current http-request.
	 */
	private HttpServletRequest m_req;
	
	/**
	 * The current http-response.
	 */
	private HttpServletResponse m_resp;
	
	/**
	 * The current user.
	 */
	private A_CmsUser m_user;
	
	/**
	 * The current group of the user.
	 */
	private A_CmsGroup m_currentGroup;
	
	/**
	 * The current project.
	 */
	private A_CmsProject m_currentProject;
		
	/**
	 * Initializes this RequestContext.
	 * 
	 * @param req the HttpServletRequest.
	 * @param resp the HttpServletResponse.
	 * @param user The current user for this request.
	 * @param currentGroup The current group for this request.
	 * @param currentProject The current project for this request.
	 */
	void init(I_CmsResourceBroker rb, HttpServletRequest req, HttpServletResponse resp, 
			  String user, String currentGroup, String currentProject) 
		throws CmsException {
		m_rb = rb;
		m_req = req;
		m_resp = resp;
		m_user = m_rb.readUser(null, null, user);
		
		// check, if the user is disabled
		if( m_user.getDisabled() == true ) {
			m_user = null;
		}
		
		// set current project and group for this request
		setCurrentProject(currentProject);
		m_currentGroup = m_rb.readGroup(m_user, m_currentProject, currentGroup);
	}
	
	/**
	 * Returns the uri for this CmsObject.
	 * 
	 * @return the uri for this CmsObject.
	 */
	public String getUri() {
		if( m_req != null ) {
			return( translatePath(m_req.getPathInfo()) );
		} else {
			return( C_ROOT );
		}
	}
	
	/**
	 * Returns the url for this CmsObject.
	 * 
	 * @return the url for this CmsObject.
	 */
	public String getUrl() {
		return("Unknown");	// TODO: implement this!
	}
	
	/**
	 * Returns the host for this CmsObject.
	 * 
	 * @return the host for this CmsObject.
	 */
	public String getHost() {
		return("Unknown");	// TODO: implement this!
	}

	/**
	 * Returns the current folder object.
	 * 
	 * @return the current folder object.
	 */
	public CmsFolder currentFolder() {
		return(null); // TODO: implement this!
	}

	/**
	 * Returns the current user object.
	 * 
	 * @return the current user object.
	 */
	public A_CmsUser currentUser() {
		return(m_user);
	}
	
	/**
	 * Returns the default group of the current user.
	 * 
	 * @return the default group of the current user.
	 */
	public A_CmsGroup userDefaultGroup() {
		return(m_user.getDefaultGroup());
	}
	
	/**
	 * Returns the current group of the current user.
	 * 
	 * @return the current group of the current user.
	 */
	public A_CmsGroup userCurrentGroup() {
		return(m_currentGroup);
	}

	/**
	 * Sets the current group of the current user.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void setUserCurrentGroup(String groupname) 
		throws CmsException {
		
		// is the user in that group?
		if(m_rb.userInGroup(m_user, m_currentProject, m_user.getName(), groupname)) {
			// Yes - set it to the current Group.
			m_currentGroup = m_rb.readGroup(m_user, m_currentProject, groupname);
		} else {
			// No - throw exception.
			throw new CmsException(CmsException.C_EXTXT[CmsException.C_NO_ACCESS],
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Determines, if the users is in the admin-group.
	 * 
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public boolean isAdmin() 
		throws CmsException {
		return( m_rb.isAdmin(m_user, m_currentProject) );
	}

	/**
	 * Determines, if the users current group is the projectleader-group.<BR>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public  boolean isProjectLeader() 
		throws CmsException	{
		return( m_rb.isProjectLeader(m_user, m_currentProject) );
	}

	/**
	 * Returns the current project for the user.
	 * 
	 * @return the current project for the user.
	 */
	public A_CmsProject getCurrentProject() {
		return m_currentProject;
	}

	/**
	 * Sets the current project for the user.
	 * 
	 * @param projectname The name of the project to be set as current.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject setCurrentProject(String projectname)
		throws CmsException  {
		// TODO: check if this is correct?!
		A_CmsProject newProject = m_rb.readProject(m_user, 
												   m_currentProject, 
												   projectname);
		if( newProject != null ) {
			m_currentProject = newProject;
		}
		return( m_currentProject );
	}

	/**
	 * Gets the current valid session associated with this request, if create 
	 * is false or, if necessary, creates a new session for the request, if 
	 * create is true.
	 * 
	 * @param create decides if a new session should be created, if needed.
	 * 
	 * @return the session associated with this request or null if create 
	 * was false and no valid session is associated with this request. 
	 */
	public HttpSession getSession(boolean create) {
		if( m_req != null ) {
			return( m_req.getSession(create) );
		} else {
			return null; // no request available!
		}
	}
	
	/**
	 * Gets the current request, if availaible.
	 * 
	 * @return the current request, if availaible.
	 */
	public HttpServletRequest getRequest() {
		return( m_req );
	}

	/**
	 * Gets the current response, if availaible.
	 * 
	 * @return the current response, if availaible.
	 */
	public HttpServletResponse getResponse() {
		return( m_resp );
	}

	/**
	 * Translates the url-path to the cms-path.
	 * 
	 * @param path The url-path.
	 * 
	 * @return the cms-path.
	 */
	private String translatePath(String path) {
		// TODO: find a mechanism to translate the path nicely.
		// TODO: in this moment there is NO translation!
		return( path );
	}
}
