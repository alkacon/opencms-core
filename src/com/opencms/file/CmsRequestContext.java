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
 * @author Michael Emmerich
 * @version $Revision: 1.11 $ $Date: 2000/01/24 10:34:44 $
 * 
 */
public class CmsRequestContext extends A_CmsRequestContext implements I_CmsConstants {

	/**
	 * The rb to get access to the OpenCms.
	 */
	private I_CmsResourceBroker m_rb;
	
	/**
	 * The current CmsRequest.
	 */
	private I_CmsRequest m_req;
	
	/**
	 * The current CmsResponse.
	 */
	private I_CmsResponse m_resp;
	
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
	 * @param req the CmsRequest.
	 * @param resp the CmsResponse.
	 * @param user The current user for this request.
	 * @param currentGroup The current group for this request.
	 * @param currentProject The current project for this request.
	 */
	void init(I_CmsResourceBroker rb, I_CmsRequest req, I_CmsResponse resp, 
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
			return( m_req.getRequestedResource() );
		} else {
			return( C_ROOT );
		}
	}
	
	/**
	 * Returns the current folder object.
	 * 
	 * @return the current folder object.
	 */
	public CmsFolder currentFolder() 
		throws CmsException	{
		return( m_rb.readFolder(currentUser(), currentProject(), getUri(), "") );
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
	 * Returns the current group of the current user.
	 * 
	 * @return the current group of the current user.
	 */
	public A_CmsGroup currentGroup() {
		return(m_currentGroup);
	}

	/**
	 * Sets the current group of the current user.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void setCurrentGroup(String groupname) 
		throws CmsException {
		
		// is the user in that group?
		if(m_rb.userInGroup(m_user, m_currentProject, m_user.getName(), groupname)) {
			// Yes - set it to the current Group.
			m_currentGroup = m_rb.readGroup(m_user, m_currentProject, groupname);
		} else {
			// No - throw exception.
			throw new CmsException(this.getClass().getName() + ": " + groupname,
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
	public A_CmsProject currentProject() {
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
		A_CmsProject newProject = m_rb.readProject(m_user, 
												   m_currentProject, 
												   projectname);
		if( newProject != null ) {
			m_currentProject = newProject;
		}
		return( m_currentProject );
	}
	
	/**
	 * Gets the current request, if availaible.
	 * 
	 * @return the current request, if availaible.
	 */
	public I_CmsRequest getRequest() {
		return( m_req );
	}

	/**
	 * Gets the current response, if availaible.
	 * 
	 * @return the current response, if availaible.
	 */
	public I_CmsResponse getResponse() {
		return( m_resp );
	}
}
