/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/security/Attic/CmsAccessControlEntry.java,v $
 * Date   : $Date: 2003/06/04 12:08:56 $
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
import com.opencms.flex.util.CmsUUID;

/**
 * @version $Revision: 1.3 $ $Date: 2003/06/04 12:08:56 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
 */
/**
 * An access control entry defines the permissions of an user or group for a distinct resource.
 * 
 * The user or group is identified by its UUID, so any other entity may act as accessor also.
 * The access control entry contains two binary permission sets, the first grants permissions
 * and the second revokes permissions explicitly (second should have precedence)
 * 
 * @version $Revision: 1.3 $ $Date: 2003/06/04 12:08:56 $
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
	
	public String getPermissionString() {
	
		return CmsAccessControlEntry.toPermissionString(m_allowed, m_denied, m_flags);
	}
	
	public String toString() {
		
		return "[Ace:] " + "ResourceId=" + m_resource + ", PrincipalId=" + m_principal + ", Allowed=" + m_allowed + ", Denied=" + m_denied + ", Flags=" + m_flags;
	}
	
	public static String toPermissionString (int allowed, int denied, int flags){
		StringBuffer p = new StringBuffer("");
		p.append(((allowed & I_CmsConstants.C_ACCESS_READ)>0)    ? "+r":""); 
		p.append(((allowed & I_CmsConstants.C_ACCESS_WRITE)>0)   ? "+w":"");
		p.append(((allowed & I_CmsConstants.C_ACCESS_VISIBLE)>0) ? "+v":"");
		p.append(((denied  & I_CmsConstants.C_ACCESS_READ)>0)    ? "-r":""); 
		p.append(((denied  & I_CmsConstants.C_ACCESS_WRITE)>0)   ? "-w":"");
		p.append(((denied  & I_CmsConstants.C_ACCESS_VISIBLE)>0) ? "-v":"");
		p.append(((flags   & I_CmsConstants.C_ACCESS_VISIBLE)>0) ? "+i":"");
		return p.toString();		
	}
	
	public static int readPermissionString(String permissionString, int perm[]) {
		StringTokenizer tok = new StringTokenizer(permissionString, "+-", true);
		int flags = 0;
		
		while(tok.hasMoreElements()) {
			String prefix = tok.nextToken();
			String suffix = tok.nextToken();
			switch (suffix.charAt(0)) {
				case 'R': case 'r':
					if (prefix.charAt(0) == '+') perm[0] |= I_CmsConstants.C_ACCESS_READ;
					if (prefix.charAt(0) == '-') perm[1] |= I_CmsConstants.C_ACCESS_READ;
					break;
				case 'W': case 'w':
					if (prefix.charAt(0) == '+') perm[0] |= I_CmsConstants.C_ACCESS_WRITE;
					if (prefix.charAt(0) == '-') perm[1] |= I_CmsConstants.C_ACCESS_WRITE;				
					break;
				case 'V': case 'v':
					if (prefix.charAt(0) == '+') perm[0] |= I_CmsConstants.C_ACCESS_VISIBLE;
					if (prefix.charAt(0) == '-') perm[1] |= I_CmsConstants.C_ACCESS_VISIBLE;
					break;
				case 'I': case 'i':
					if (prefix.charAt(0) == '+') flags |= I_CmsConstants.C_ACCESSFLAGS_INHERITED;
					if (prefix.charAt(0) == '-') flags &= ~I_CmsConstants.C_ACCESSFLAGS_INHERITED;
					break;
			}
		}
		
		return flags;
	}
}
