/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/security/Attic/CmsAccessControlList.java,v $
 * Date   : $Date: 2003/06/05 14:15:48 $
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
package com.opencms.security;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import com.opencms.file.CmsUser;

/**
 * An access control list contains a permission set for a distinct resource
 * that is calculated on the permissions given by various access control entries
 * 
 * @version $Revision: 1.4 $ $Date: 2003/06/05 14:15:48 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsAccessControlList {

	private class PrincipalPermissions {
		int m_allowed;
		int m_denied;
		
		PrincipalPermissions(){
			m_allowed = 0;
			m_denied = 0;
		}
		
		public void addAllowedPermissions(int allowed){
			m_allowed |= allowed;
		}
		
		public void addDeniedPermissions(int denied){
			m_denied |= denied;
		}
		
		public int getPermissions() {
			return m_allowed & ~m_denied;
		}
	}
		
	/*
	 * Permissions of a principal on this resource 
	 */
	private Hashtable m_permissions;
	
	/**
	 * Constructor to create an empty access control list for a given resource
	 *
	 */
	public CmsAccessControlList(){
		m_permissions = new Hashtable();
	}
	
	/**
	 * Adds an access control entry to the access control list.
	 * 
	 * @param entry the access control entry to add
	 */
	public void add(CmsAccessControlEntry entry){

		PrincipalPermissions permissions = (PrincipalPermissions)m_permissions.get(entry.getPrincipal());
		if (permissions == null)
			permissions = new PrincipalPermissions();
		
		permissions.addAllowedPermissions(entry.getAllowedPermissions());
		permissions.addDeniedPermissions(entry.getDeniedPermissions());
		
		m_permissions.put(entry.getPrincipal(),permissions); 
	}
	
	/**
	 * Calculates the permission set of the access control list
	 * for a given vector of principals
	 * 
	 * @param principal	the user
	 * 
	 * @return the current permission set of the list
	 */
	public int getPermissions(CmsUser user, Vector groups){
		ListIterator pIterator = groups.listIterator();
		int allowed = 0, denied  = 0;
		
		I_CmsPrincipal principal = (I_CmsPrincipal)user;
		do {
			PrincipalPermissions permissions = (PrincipalPermissions)m_permissions.get(principal.getId());
			if (permissions != null) {
				allowed |= permissions.m_allowed;
				denied  |= permissions.m_denied;		
			}
			if (pIterator.hasNext()) {
				principal=(I_CmsPrincipal)pIterator.next();
			} else {
				principal = null;
			}
		} while (principal != null);
		
		return allowed & ~denied;
	}
	
	// TODO: change to Vector
	public String getPermissionString(I_CmsPrincipal principal){
		PrincipalPermissions permissions = (PrincipalPermissions)m_permissions.get(principal.getId());
		if (permissions == null)
			return "";
			
		return CmsAccessControlEntry.toPermissionString(permissions.m_allowed,permissions.m_denied,0);		
	}
	
	/**
	 * Checks if a given vector of principals has sufficient permissions on a resource
	 * 
	 * @param principals
	 * @param permissions
	 * @return true if permissions are sufficient
	 */
	public boolean hasPermissions(CmsUser user, Vector groups, int permissions){
		ListIterator pIterator = groups.listIterator();	
		int allowed = 0, denied  = 0;
		I_CmsPrincipal principal = (I_CmsPrincipal)user;
		do {
			PrincipalPermissions pp = (PrincipalPermissions)m_permissions.get(principal.getId());
			if (pp != null) {
				allowed |= pp.m_allowed;
				denied  |= pp.m_denied;		
			}
			if (pIterator.hasNext()) {
				principal=(I_CmsPrincipal)pIterator.next();
			} else {
				principal = null;
			}
		} while (principal != null);
		
		return (permissions & (allowed & ~denied)) == permissions;		
	}
	
	
	public Enumeration getPrincipals() {
		return m_permissions.keys();
	}
}
