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
 * @version $Revision: 1.1 $ $Date: 1999/12/23 16:49:21 $
 * 
 */
public class CmsRequestContext extends A_CmsRequestContext {
	
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
	 * The current user.
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
	void init(HttpServletRequest req, HttpServletResponse resp, 
			  String user, String currentGroup, String currentProject) {
		m_req = req;
		m_resp = resp;
		// TODO: implement this!
		// m_user = user;
		// m_currentGroup = currentGroup;
		// m_currentProject = currentProject;
	}
	
	/**
	 * Returns the uri for this CmsObject.
	 * 
	 * @return the uri for this CmsObject.
	 */
	public String getUri() {
		return("Unknown");	// TODO: implement this!
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
	void setUserCurrentGroup(String groupname) 
		throws CmsException {
		return; // TODO: implement this!
	}

	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 */	
	public boolean isAdmin() {
		return false; // TODO: implement this!
	}

	/**
	 * Determines, if the users current group is the projectleader-group.<BR>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 */	
	public  boolean isProjectLeader() {
		return false; // TODO: implement this!
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
		return null; // TODO: implement this!
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
		return null; // TODO: implement this!
	}
}
