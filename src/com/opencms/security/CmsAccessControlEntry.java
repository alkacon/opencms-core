/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/security/Attic/CmsAccessControlEntry.java,v $
 * Date   : $Date: 2003/06/02 10:59:17 $
 * Version: $Revision: 1.2 $
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
 * @version $Revision: 1.2 $ $Date: 2003/06/02 10:59:17 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
 */
/**
 * An access control entry defines the permissions of an user or group for a distinct resource.
 * 
 * The user or group is identified by its UUID, so any other entity may act as accessor also.
 * The access control entry contains two binary permission sets, the first grants permissions
 * and the second revokes permissions explicitly (second should have precedence)
 * 
 * @version $Revision: 1.2 $ $Date: 2003/06/02 10:59:17 $
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
	 * Allowed permission set
	 */
	private int m_allowed;
	
	/*
	 * Denied permission set
	 */
	private int m_denied;
	
	/*
	 * Further control flags
	 */
	private int m_flags;
	
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
		m_allowed = allowed;
		m_denied = denied;	
		m_flags = flags;
	}
	
	/**
	 * Sets the allowed permissions in the access control entry
	 * 
	 * @param allowed	the set of allowed permissions
	 */
	public void setAllowedPermissions(int allowed) {
		
		m_allowed = allowed;
	}
	
	/**
	 * Returns the allowed permissions
	 * 
	 * @return	the set of allowed permissions
	 */
	public int getAllowedPermissions() {
		
		return m_allowed;
	}
	
	/**
	 * Sets the explicitly denied permissions in the access control entry
	 * 
	 * @param denied
	 */
	public void setDeniedPermissions(int denied) {
		
		m_denied = denied;
	}
	
	/**
	 * Returns the denied permissions
	 * 
	 * @return	the set of explicitly denied permissions
	 */
	public int getDeniedPermissions() {

		return m_denied;		
	}
	
	/**
	 * Sets the given flags in the access control entry
	 * @param flags bitset with flag values to set
	 */
	public void setFlags(int flags) {
		
		m_flags |= flags;
	}
	
	/**
	 * Resets the given flags in the access control entry
	 * @param flags bitset with flag values to reset
	 */
	public void resetFlags(int flags) {
		
		m_flags &= ~flags;
	}
	
	/**
	 * Returns the current flags
	 * @return bitset with flag values
	 */
	public int getFlags() {
		
		return m_flags;
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
}
