/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsAccessControlList.java,v $
 * Date   : $Date: 2003/06/26 15:36:41 $
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

import com.opencms.file.CmsUser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

/**
 * An access control list contains the permission sets of all principals for a distinct resource
 * that are calculated on the permissions given by various access control entries.<p>
 * 
 * @version $Revision: 1.4 $ $Date: 2003/06/26 15:36:41 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsAccessControlList {
		
	/*
	 * Permissions of a principal on this resource 
	 */
	private HashMap m_permissions;
	
	/**
	 * Constructor to create an empty access control list for a given resource
	 *
	 */
	public CmsAccessControlList() {
		
		m_permissions = new HashMap();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		
		CmsAccessControlList acl = new CmsAccessControlList();
		Iterator i = m_permissions.keySet().iterator();
 
		while (i.hasNext()) {
			Object key = i.next();
			acl.m_permissions.put(key, ((CmsPermissionSet)m_permissions.get(key)).clone());
		}

		return acl;	
	}
	
	/**
	 * Adds an access control entry to the access control list.
	 * 
	 * @param entry the access control entry to add
	 */
	public void add(CmsAccessControlEntry entry) {

		CmsPermissionSet permissions = (CmsPermissionSet)m_permissions.get(entry.getPrincipal());
		if (permissions == null)
			permissions = new CmsPermissionSet();
		
		permissions.addPermissions(entry.getPermissions());
		m_permissions.put(entry.getPrincipal(),permissions); 
	}

	/**
	 * Sets the allowed permissions of a given access control entry as allowed permissions in the access control list.
	 * 
	 * @param entry the access control entry
	 */
	public void setAllowedPermissions(CmsAccessControlEntry entry) {
	
		CmsPermissionSet permissions = (CmsPermissionSet)m_permissions.get(entry.getPrincipal());
		if (permissions == null)
			permissions = new CmsPermissionSet();	

		permissions.setPermissions(entry.getAllowedPermissions(),0);
		m_permissions.put(entry.getPrincipal(),permissions);
	}
	
	/**
	 * Sets the denied permissions of a given access control entry as denied permissions in the access control list.
	 * 
	 * @param entry the access control entry
	 */
	public void setDeniedPermissions(CmsAccessControlEntry entry) {
		
		CmsPermissionSet permissions = (CmsPermissionSet)m_permissions.get(entry.getPrincipal());
		if (permissions == null)
			permissions = new CmsPermissionSet();
			
		permissions.setPermissions(0, entry.getDeniedPermissions());		
	}
	
	/**
	 * Returns the permission set of a principal as stored in the access control list.
	 * 
	 * @param principal the principal (group or user)
	 * 
	 * @return the current permissions of this single principal
	 */
	public CmsPermissionSet getPermissions(I_CmsPrincipal principal) {
		
		return (CmsPermissionSet)m_permissions.get(principal.getId());
	}
		
	/**
	 * Calculates the permission set of the given user from the access control list.
	 *  
	 * @param user		the user
	 * @param groups	the groups of this user
	 * 
	 * @return the summarized permission set of the user
	 */
	public CmsPermissionSet getPermissions(CmsUser user, Vector groups) {
		
		CmsPermissionSet sum = new CmsPermissionSet();
		ListIterator pIterator = null;
		if (groups != null)
			pIterator = groups.listIterator();
		
		I_CmsPrincipal principal = (I_CmsPrincipal)user;
		do {
			CmsPermissionSet permissions = (CmsPermissionSet)m_permissions.get(principal.getId());
			if (permissions != null) 
				sum.addPermissions(permissions);	

			if (pIterator != null && pIterator.hasNext()) {
				principal=(I_CmsPrincipal)pIterator.next();
			} else {
				principal = null;
			}
		} while (principal != null);
		
		return sum;
	}
		
	/**
	 * Calculates the permission set of the given user from the access control list
	 * and returns it as permission string in the format {{+|-}{r|w|v|c|i}}*
	 * 
	 * @param user		the user
	 * @param groups	the groups oft this user
	 * 
	 * @return			a string that displays the permissions
	 */
	public String getPermissionString(CmsUser user, Vector groups) {
		
		CmsPermissionSet permissions = getPermissions(user, groups);
		return permissions.getPermissionString();
	}
	
	/**
	 * Returns the principals with specific permissions stored in this access control list.
	 * 
	 * @return enumeration of principals (each group or user)
	 */
	public Set getPrincipals() {
		
		return m_permissions.keySet();
	}
}