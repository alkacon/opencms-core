/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/security/Attic/CmsAccessControlList.java,v $
 * Date   : $Date: 2003/06/02 15:33:07 $
 * Version: $Revision: 1.1 $
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

import java.util.Hashtable;

import com.opencms.file.CmsUser;

/**
 * An access control list contains a permission set for a distinct resource
 * that is calculated on the permissions given by various access control entries
 * 
 * @version $Revision: 1.1 $ $Date: 2003/06/02 15:33:07 $
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
	 * for a given user
	 * 
	 * @param principal	the user
	 * 
	 * @return the current permission set of the list
	 */
	public int getPermissions(CmsUser principal){
		PrincipalPermissions permissions = (PrincipalPermissions)m_permissions.get(principal.getId());
		if (permissions == null)
			return 0;
			
		return permissions.getPermissions();
	}
}
