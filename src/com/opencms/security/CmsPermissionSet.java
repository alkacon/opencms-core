/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/security/Attic/CmsPermissionSet.java,v $
 * Date   : $Date: 2003/06/12 15:16:32 $
 * Version: $Revision: 1.3 $
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

import java.util.StringTokenizer;

import com.opencms.core.I_CmsConstants;

/**
 * A permission set contains both allowed and denied permissions as bitsets.
 * 
 * @version $Revision: 1.3 $ $Date: 2003/06/12 15:16:32 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsPermissionSet {

	/*
	 * the set of allowed permissions
	 */
	int m_allowed;
	
	/*
	 * the set of denied permissions
	 */
	int m_denied;
	
	int m_flags;
	
	
	/**
	 * Constructor to create an empty permission set.
	 */
	public CmsPermissionSet() {
		
		m_allowed = 0;
		m_denied = 0;
		m_flags = 0;
	}
	
	/**
	 * Constructor to create a permission set with some preset allowed permissions.
	 * 
	 * @param allowedPermissions	bitset of allowed permissions
	 */
	public CmsPermissionSet (int allowedPermissions) {
		
		m_allowed = allowedPermissions;
		m_denied = 0;
		m_flags = 0;
	}
	
	/**
	 * Constructor to create a permission set with some preset allowed and denied permissions.
	 * 
	 * @param allowedPermissions
	 * @param deniedPermissions
	 */
	public CmsPermissionSet(int allowedPermissions, int deniedPermissions) {
		
		m_allowed = allowedPermissions;
		m_denied = deniedPermissions;
		m_flags = 0;
	}
	
	/**
	 * Constructor to create a permission set with preset allowed and denied permissions
	 * from a string representation of permissions of the format {{+|-}{r|w|v|c}}*.
	 * 
	 * @param permissionString	the string representation of allowed and denied permissions
	 */
	public CmsPermissionSet(String permissionString) {
		
		StringTokenizer tok = new StringTokenizer(permissionString, "+-", true);
		m_allowed = 0;
		m_denied = 0;
		m_flags = 0;
		
		while(tok.hasMoreElements()) {
			String prefix = tok.nextToken();
			String suffix = tok.nextToken();
			switch (suffix.charAt(0)) {
				case 'R': case 'r':
					if (prefix.charAt(0) == '+') m_allowed |= I_CmsConstants.C_PERMISSION_READ;
					if (prefix.charAt(0) == '-') m_denied |= I_CmsConstants.C_PERMISSION_READ;
					break;
				case 'W': case 'w':
					if (prefix.charAt(0) == '+') m_allowed |= I_CmsConstants.C_PERMISSION_WRITE;
					if (prefix.charAt(0) == '-') m_denied |= I_CmsConstants.C_PERMISSION_WRITE;				
					break;
				case 'V': case 'v':
					if (prefix.charAt(0) == '+') m_allowed |= I_CmsConstants.C_PERMISSION_VIEW;
					if (prefix.charAt(0) == '-') m_denied |= I_CmsConstants.C_PERMISSION_VIEW;
					break;
				default:
					// ignore
					break;
			}
		}
	}
	
	/**
	 * Sets permissions additionally as allowed permissions.
	 * 
	 * @param permissions	bitset of permissions to allow
	 */
	public void grantPermissions (int permissions) {

		m_allowed |= permissions;
	}
	
	/**
	 * Sets permsissions additionally as denied permissions.
	 * 
	 * @param permissions bitset of permissions to deny
	 */
	public void denyPermissions (int permissions) {

		m_denied |= permissions;
	}
	
	/**
	 * Sets permissions additionally both as allowed and denied permissions.
	 * 
	 * @param allowedPermissions bitset of permissions to allow
	 * @param deniedPermissions  bitset of permissions to deny
	 */
	public void addPermissions (int allowedPermissions, int deniedPermissions) {
		
		m_allowed |= allowedPermissions;
		m_denied |= deniedPermissions;
	}
	
	/**
	 * Sets permissions from another permission set additionally both as allowed and denied permissions.
	 * 
	 * @param permissionSet the set of permissions to set additionally.
	 */
	public void addPermissions (CmsPermissionSet permissionSet) {
		
		m_allowed |= permissionSet.m_allowed;
		m_denied |= permissionSet.m_denied;
	}

	/**
	 * Sets permissions as allowed and denied permissions in the permission set.
	 * Permissions formerly set are overwritten.
	 * 
	 * @param allowedPermissions bitset of permissions to allow
	 * @param deniedPermissions  bitset of permissions to deny
	 */
	public void setPermissions(int allowedPermissions, int deniedPermissions)  {
		
		m_allowed = allowedPermissions;
		m_denied = deniedPermissions;
	}
		
	/**
	 * Set permissions from another permission set both as allowed and denied permissions.
	 * Permissions formerly set are overwritten.
	 * 
	 * @param permissionSet the set of permissions
	 */
	public void setPermissions (CmsPermissionSet permissionSet) {
		
		m_allowed = permissionSet.m_allowed;
		m_denied = permissionSet.m_denied;	
	}
	
	/**
	 * Returns the permissions calculated from this permission set.
	 * These are all permissions allowed but not denied.
	 *  
	 * @return the resulting permission set
	 */
	public int getPermissions() {
		
		return m_allowed & ~m_denied;
	}
	
	/**
	 * Returns the currently allowed permissions of ths permission set.
	 * 
	 * @return the allowed permissions as bitset
	 */
	public int getAllowedPermissions() {
		
		return m_allowed;
	}
	
	/**
	 * Returns the currently denied permissions of this permission set.
	 * 
	 * @return the denied permissions as bitset.
	 */
	public int getDeniedPermissions() {
		
		return m_denied;
	}
		
	/**
	 * Returns the string representation of the current permissions in this permission set.
	 * 
	 * @return string of the format {{+|-}{r|w|v}}*
	 */
	public String getPermissionString() {
		
		StringBuffer p = new StringBuffer("");
		
		if ((m_allowed & I_CmsConstants.C_PERMISSION_READ)>0)		p.append("+r");
		else if ((m_denied  & I_CmsConstants.C_PERMISSION_READ)>0)	p.append("-r");
			
		if ((m_allowed & I_CmsConstants.C_PERMISSION_WRITE)>0) 		p.append("+w");
		else if ((m_denied  & I_CmsConstants.C_PERMISSION_WRITE)>0)	p.append("-w");
			
		if ((m_allowed & I_CmsConstants.C_PERMISSION_VIEW)>0) 		p.append("+v");
		else if ((m_denied  & I_CmsConstants.C_PERMISSION_VIEW)>0)	p.append("-v");
			
		return p.toString();			
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString () {
		
		return "[PermissionSet:] " + getPermissionString();
	}
}