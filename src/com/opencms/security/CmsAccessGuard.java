/*
 * Created on Jun 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.opencms.security;

import com.opencms.core.CmsException;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;

/**
 * @author cweinholz
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class CmsAccessGuard {

	private CmsUser m_user;
	
	private CmsProject m_project;
	
	public CmsAccessGuard (CmsUser user, CmsProject project) {
		m_user = user;
		m_project = project;
	}
	
	public abstract CmsPermissionSet evaluatePermissions (CmsResource resource)  throws CmsException;
	
	public void check (CmsResource resource, CmsPermissionSet permissions)  throws CmsException{
		check (resource, permissions, true);
	}
	
	public boolean check (CmsResource resource, CmsPermissionSet requiredPermissions, boolean blockAccess)  throws CmsException {
	
			CmsPermissionSet currentPermissions = evaluatePermissions(resource);
			boolean hasPermissions = (requiredPermissions.getPermissions() & (currentPermissions.getPermissions())) == requiredPermissions.getPermissions();
			
			if (blockAccess && ! hasPermissions) {
				throw new CmsException("[" + this.getClass().getName() + "] denied access to resource " + resource.getAbsolutePath() + ", required permissions are " + requiredPermissions.getPermissionString(), CmsException.C_NO_ACCESS);
			}
			
			return hasPermissions;
	}
	
	public CmsUser getUser() {
		return m_user;
	}
	
	public CmsProject getProject() {
		return m_project;
	}
}
