/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/Attic/CmsAccessGuard.java,v $
 * Date   : $Date: 2003/07/02 11:03:12 $
 * Version: $Revision: 1.4 $
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
package org.opencms.security;

import com.opencms.core.CmsException;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;

/**
 * An access guard checks the permissions of an user on a given resource against required permissions,
 * additionally depending on the policy that is implemented in a subclass.<p>
 * 
 * @version $Revision: 1.4 $ $Date: 2003/07/02 11:03:12 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public abstract class CmsAccessGuard {

	/*
	 * The user whose permissions are checked
	 */
	private CmsUser m_user;
	
	/*
	 * The project, in which the permissions of the user are checked
	 */
	private CmsProject m_project;
	
	/*
	 * Defines the checks to perform when evaluating the permissions
	 */
	private int m_checks;
		
	/**
	 * Constructor to create a new access guard for a given user and project.
	 * 
	 * @param user			the user whose permissions are checked
	 * @param project		the project, in which the permissions of the user are checked 
	 * @param performChecks flags to define the checks performed when evaluation the permissions
	 */
	public CmsAccessGuard (CmsUser user, CmsProject project, int performChecks) {
		m_user = user;
		m_project = project;
		m_checks = performChecks;
	}
	
	/**
	 * Abstract method to evaluate the permissions of the given user on a resource within a given project by 
	 * calculating a permission set following a certain policy.
	 * 
	 * @param resource		the resource on which permissions are required
	 * @param performChecks flags to define the checks performed when evaluation the permissions
	 * 
	 * @return a set of allowed and denied permissions for the given user on the resource  
	 * @throws CmsException if something goes wrong
	 */
	public abstract CmsPermissionSet evaluatePermissions (CmsResource resource, int performChecks) throws CmsException;
	
	
	/**
	 * Blocking permission check on a resource (strong check, all required permissions are neccessary).
	 * If the required permissions are not satisfied by the permissions the user has on a resource,
	 * an access denied exception is thrown.
	 * 
	 * @param resource			  	the resource on which permissions are required
	 * @param requiredPermissions 	the set of permissions required to access the reosurce
	 * 
	 * @throws CmsException			if something goes wrong
	 */
	public void check (CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException {
		
		check (resource, requiredPermissions, true, true);
	}
	
	/**
	 * General permission check on a resource.
	 * Depending on the value of blockAccess, an access denied exception is thrown if the required 
	 * permissions are not satisfied by the permissions the user has on a resource.
	 * 
	 * If blockAccess is set to false, the result of the check is returned.
	 * 
	 * @param resource				the resource on which permissions are required
	 * @param requiredPermissions	the set of permissions required to access the resource
	 * @param strongCheck			if set to true, all required permission have to be granted, otherwise only one
	 * @param blockAccess			if true, an access denied exception is thrown if the required permissions are not satisfied
	 * @return						if the required permissions are satisfied
	 * @throws CmsException			if something goes wrong
	 */
	public boolean check (CmsResource resource, CmsPermissionSet requiredPermissions, boolean strongCheck, boolean blockAccess) throws CmsException {
	
		CmsPermissionSet currentPermissions = evaluatePermissions(resource, m_checks);
		boolean hasPermissions = false;
		
		if (strongCheck)
			hasPermissions = (requiredPermissions.getPermissions() & (currentPermissions.getPermissions())) == requiredPermissions.getPermissions();
		else
			hasPermissions = (requiredPermissions.getPermissions() & (currentPermissions.getPermissions())) > 0;
		
		if (blockAccess && ! hasPermissions) {
			throw new CmsException("[" + this.getClass().getName() + "] denied access to resource " + resource.getResourceName() + ", required permissions are " + requiredPermissions.getPermissionString() + ((strongCheck) ? " (required each)": " (required one)"), CmsException.C_NO_ACCESS);
		}
		
		return hasPermissions;
	}

	/**
	 * General permission check on a resource.
	 * Depending on the value of blockAccess, an access denied exception is thrown if the required 
	 * permissions are not satisfied by the permissions the user has on a resource.
	 *
	 * @param resource				the resource on which permissions are required
	 * @param requiredPermissions	the set of permissions required to access the resource
	 * @param strongCheck			if set to true, all required permission have to be granted, otherwise only one
	 * @param performChecks			flags defining the checks to perform when evaluating the permissions
	 * @param blockAccess			if true, an access denied exception is thrown if the required permissions are not satisfied
	 * @return						true, if the required permissions are satisfied
	 * @throws CmsException			C_NO_ACCESS if the required permissions are not satisfied and blockAccess is true
	 */
	public boolean check (CmsResource resource, CmsPermissionSet requiredPermissions, boolean strongCheck, int performChecks, boolean blockAccess) throws CmsException {
	
		CmsPermissionSet currentPermissions = evaluatePermissions(resource, performChecks);
		boolean hasPermissions = (requiredPermissions.getPermissions() & (currentPermissions.getPermissions())) == requiredPermissions.getPermissions();
		
		if (blockAccess && ! hasPermissions) {
			throw new CmsException("[" + this.getClass().getName() + "] denied access to resource " + resource.getResourceName() + ", required permissions are " + requiredPermissions.getPermissionString(), CmsException.C_NO_ACCESS);
		}
		
		return hasPermissions;
	}
		
	/**
	 * Returns the user that is checked by the access guard
	 * 
	 * @return the user
	 */
	public CmsUser getUser() {
		
		return m_user;
	}
	
	/**
	 * Returns the project in which the permission evaluation should be done.
	 * 
	 * @return the project
	 */
	public CmsProject getProject() {
		
		return m_project;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[CmsAccessGuard:] " + m_user.toString() + ", " + m_project.toString();
	}
}