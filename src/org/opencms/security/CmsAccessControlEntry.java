/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsAccessControlEntry.java,v $
 * Date   : $Date: 2003/06/23 16:34:59 $
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
package org.opencms.security;

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;

import java.util.StringTokenizer;

/**
 * An access control entry defines the permissions of an user or group for a distinct resource.<p>
 * 
 * The user or group is identified by its UUID, so any other entity may act as accessor also.
 * The access control entry contains two binary permission sets, the first grants permissions
 * and the second revokes permissions explicitly (second should have precedence)
 * 
 * @version $Revision: 1.3 $ $Date: 2003/06/23 16:34:59 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsAccessControlEntry {

	/*
	 * Id of the resource
	 */
	private CmsUUID m_resource;
	
	/*
	 * Id of the principal
	 */
	private CmsUUID m_principal;
	
	/*
	 * Flags of this access control entry
	 */
	private int m_flags;
	
	/*
	 * the permission set
	 */
	private CmsPermissionSet m_permissions;
	
	/**
	 * Constructor to create a new access control entry on a given resource and a given principal.
	 * Permissions and flags are specified as bitsets.
	 * 
	 * @see CmsPermissionSet
	 * 
	 * @param resource	the resource
	 * @param principal	the id of a principal (user or group)
	 * @param allowed	the set of allowed permissions
	 * @param denied	set set of explicitly denied permissions
	 * @param flags		additional flags of the access control entry
	 */
	public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) {
	
		m_resource = resource;
		m_principal = principal;
		m_permissions = new CmsPermissionSet(allowed, denied);
		m_flags = flags;
	}
	
	/**
	 * Constructor to create a new access control entry on a given resource and a given principal.
	 * Permissions are specified as permission set, flags as bitset.
	 * 
	 * @param resource		the resource
	 * @param principal		the id of a principal (user or group)
	 * @param permissions	the set of allowed and denied permissions as permission set
	 * @param flags			additional flags of the access control entry
	 */
	public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, CmsPermissionSet permissions, int flags) {
		
		m_resource = resource;
		m_principal = principal;
		m_permissions = permissions;
		m_flags = flags;
	}
	
	/**
	 * Constructor to create a new access control entry on a given resource and a given principal.
	 * Permission and flags are specified as string of the format {{+|-}{r|w|v|c|i}}*
	 * 
	 * @param resource				the resource
	 * @param principal				the id of a principal (user or group)
	 * @param acPermissionString	allowed and denied permissions and also flags
	 */
	public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, String acPermissionString) {
		
		m_resource = resource;
		m_principal = principal;
		m_flags = 0;
		
		StringTokenizer tok = new StringTokenizer(acPermissionString, "+-", true);
		StringBuffer permissionString = new StringBuffer();
		
		while(tok.hasMoreElements()) {
			String prefix = tok.nextToken();
			String suffix = tok.nextToken();
			switch (suffix.charAt(0)) {
				case 'I': case 'i':
					if (prefix.charAt(0) == '+') m_flags |= I_CmsConstants.C_ACCESSFLAGS_INHERIT;
					if (prefix.charAt(0) == '-') m_flags &= ~I_CmsConstants.C_ACCESSFLAGS_INHERIT;
					break;
				case 'O': case 'o':
					if (prefix.charAt(0) == '+') m_flags |= I_CmsConstants.C_ACCESSFLAGS_OVERWRITE;
					if (prefix.charAt(0) == '-') m_flags &= ~I_CmsConstants.C_ACCESSFLAGS_OVERWRITE;
					break;					
				default:
					permissionString.append(prefix);
					permissionString.append(suffix);
					break;
			}
		}
				
		m_permissions = new CmsPermissionSet(permissionString.toString());
	}

	/**
	 * Sets the allowed and denied permissions of the access control entry.
	 * 
	 * @param permissions the set of permissions
	 */
	public void setPermissions(CmsPermissionSet permissions) {
		
		m_permissions.setPermissions(permissions);
	}
		
	/**
	 * Sets the allowed permissions in the access control entry.
	 * 
	 * @param allowed	the allowed permissions as bitset
	 */
	public void grantPermissions(int allowed) {
		
		m_permissions.grantPermissions (allowed);
	}
		
	/**
	 * Sets the explicitly denied permissions in the access control entry.
	 * 
	 * @param denied the denied permissions as bitset
	 */
	public void denyPermissions(int denied) {
		
		m_permissions.denyPermissions(denied);
	}	
	/**
	 * Returns the current permission set (both allowed and denied permissions).
	 * 
	 * @return	the set of permissions
	 */
	public CmsPermissionSet getPermissions() {
		
		return m_permissions;
	}
	
	/**
	 * Returns the currently allowed permissions as bitset.
	 * 
	 * @return the allowed permissions
	 */
	public int getAllowedPermissions() {
		
		return m_permissions.getAllowedPermissions();
	}
	
	/**
	 * Return the currently denied permissions as bitset.
	 * 
	 * @return the denied permissions
	 */
	public int getDeniedPermissions() {
		
		return m_permissions.getDeniedPermissions();
	}

	/**
	 * Returns the resource assigned with this access control entry.
	 * 
	 * @return the resource 
	 */
	public CmsUUID getResource() {
		
		return m_resource;
	}
	
	/**
	 * Returns the principal assigned with this access control entry.
	 * 
	 * @return the principal
	 */
	public CmsUUID getPrincipal() {
		
		return m_principal;
	}

	/**
	 * Sets the given flags in the access control entry.
	 * 
	 * @param flags bitset with flag values to set
	 */
	public void setFlags(int flags) {
		
		m_flags |= flags;
	}
	
	/**
	 * Resets the given flags in the access control entry.
	 * 
	 * @param flags bitset with flag values to reset
	 */
	public void resetFlags(int flags) {
		
		m_flags &= ~flags;
	}
	
	/**
	 * Returns the current flags of the access control entry.
	 * 
	 * @return bitset with flag values
	 */
	public int getFlags() {
		
		return m_flags;
	}	

	/**
	 * Returns if this access control entry has the inherited flag set.
	 * Note: to check if an access control entry is inherited, also the
	 * resource id and the id of the current resource must be different.
	 * 
	 * @return true, if the inherited flag is set
	 */
	public boolean isInherited() {
		return ((m_flags & I_CmsConstants.C_ACCESSFLAGS_INHERITED) > 0);		
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return "[Ace:] " + "ResourceId=" + m_resource + ", PrincipalId=" + m_principal + ", Permissions=" + m_permissions.toString() + ", Flags=" + m_flags;
	}
}