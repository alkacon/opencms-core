/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/security/Attic/CmsAccessControlEntry.java,v $
 * Date   : $Date: 2003/06/09 17:07:08 $
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

import com.opencms.flex.util.CmsUUID;

/**
 * @version $Revision: 1.4 $ $Date: 2003/06/09 17:07:08 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
 */
/**
 * An access control entry defines the permissions of an user or group for a distinct resource.
 * 
 * The user or group is identified by its UUID, so any other entity may act as accessor also.
 * The access control entry contains two binary permission sets, the first grants permissions
 * and the second revokes permissions explicitly (second should have precedence)
 * 
 * @version $Revision: 1.4 $ $Date: 2003/06/09 17:07:08 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
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
	 * the permission set
	 */
	private CmsPermissionSet m_permissions;
	
	/**
	 * Constructor to create a new access control entry on a given resource and a given entity.
	 * 
	 * @param resource	the resource
	 * @param principal	the id of an entity
	 * @param allowed	the set of allowed permissions
	 * @param denied	set set of explicitly denied permissions
	 */
	public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) {
	
		m_resource = resource;
		m_principal = principal;
		m_permissions = new CmsPermissionSet(allowed, denied);
		m_permissions.setFlags(flags);
	}
	
	public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, CmsPermissionSet permissions, int flags) {
		
		m_resource = resource;
		m_principal = principal;
		m_permissions = permissions;
		m_permissions.setFlags(flags);
	}
	
	public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, String permissionString) {
		
		m_resource = resource;
		m_principal = principal;
		m_permissions = new CmsPermissionSet (permissionString);
	}
	
	/**
	 * Sets the allowed permissions in the access control entry
	 * 
	 * @param allowed	the set of allowed permissions
	 */
	public void grantPermissions(int allowed) {
		m_permissions.grantPermissions (allowed);
	}
	
	/**
	 * Returns the allowed permissions
	 * 
	 * @return	the set of permissions
	 */
	public CmsPermissionSet getPermissions() {
		
		return m_permissions;
	}
	
	public int getAllowedPermissions() {
		return m_permissions.getAllowedPermissions();
	}
	
	public int getDeniedPermissions() {
		return m_permissions.getDeniedPermissions();
	}
	
	/**
	 * Sets the explicitly denied permissions in the access control entry
	 * 
	 * @param denied
	 */
	public void denyPermissions(int denied) {
		
		m_permissions.denyPermissions(denied);
	}
		
	public void setPermissions(CmsPermissionSet permissions) {
		
		m_permissions.setPermissions(permissions);
	}
	
	/**
	 * Returns the resource of the access control entry.
	 * 
	 * @return the resource 
	 */
	public CmsUUID getResource() {
		
		return m_resource;
	}
	
	/**
	 * Returns the principal of the access control entry.
	 * 
	 * @return the principal
	 */
	public CmsUUID getPrincipal() {
		
		return m_principal;
	}
	
	public int getFlags() {
		
		return m_permissions.getFlags();
	}
	
	public String toString() {
		
		return "[Ace:] " + "ResourceId=" + m_resource + ", PrincipalId=" + m_principal + ", Permissions=" + m_permissions.toString();
	}
}
