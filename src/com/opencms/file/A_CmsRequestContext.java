package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * The class which extends this class gains access to the CmsRequestContext. 
 * <p>
 * In the request context are all methods bundeled, which can inform about the
 * current request, such like url or uri.
 * <p>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/22 14:02:30 $ 
 * 
 */
public abstract class A_CmsRequestContext
{

	/**
	 * Initializes this RequestContext.
	 * 
	 * @param req the HttpServletRequest.
	 * @param resp the HttpServletResponse.
	 * @param user The current user for this request.
	 */
	abstract void init(HttpServletRequest req, HttpServletResponse resp, A_CmsUser user);
	
	/**
	 * Returns the uri for this CmsObject.
	 * 
	 * @return the uri for this CmsObject.
	 */
	public abstract String getUri();
	
	/**
	 * Returns the url for this CmsObject.
	 * 
	 * @return the url for this CmsObject.
	 */
	public abstract String getUrl();
	
	/**
	 * Returns the host for this CmsObject.
	 * 
	 * @return the host for this CmsObject.
	 */
	public abstract String getHost();

	/**
	 * Returns the current folder object.
	 * 
	 * @return the current folder object.
	 */
	abstract public CmsFolder currentFolder();	

	/**
	 * Returns the current user object.
	 * 
	 * @return the current user object.
	 */
	abstract public A_CmsUser currentUser();
	
	/**
	 * Returns the default group of the current user.
	 * 
	 * @return the default group of the current user.
	 */
	abstract public A_CmsGroup userDefaultGroup();
	
	/**
	 * Returns the current group of the current user.
	 * 
	 * @return the current group of the current user.
	 */
	abstract public A_CmsGroup userCurrentGroup();

	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 */	
	abstract public boolean isAdmin();

	/**
	 * Determines, if the users current group is the projectleader-group.<BR>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 */	
	abstract public  boolean isProjectLeader();

	/**
	 * Returns the current project for the user.
	 * 
	 * @return the current project for the user.
	 */
	abstract public A_CmsProject getCurrentProject();

	/**
	 * Sets the current project for the user.
	 * 
	 * @param projectname The name of the project to be set as current.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsProject setCurrentProject(String projectname)
		throws CmsException;

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
	public abstract HttpSession getSession(boolean create);
}
