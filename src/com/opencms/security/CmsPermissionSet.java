/*
 * Created on Jun 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.opencms.security;

import java.util.StringTokenizer;

import com.opencms.core.I_CmsConstants;

/**
 * @author cweinholz
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CmsPermissionSet {

	int m_allowed;
	int m_denied;
	int m_flags;
	
	public CmsPermissionSet() {
		m_allowed = 0;
		m_denied = 0;
		m_flags = 0;
	}
	
	public CmsPermissionSet (int allowedPermissions) {
		m_allowed = allowedPermissions;
		m_denied = 0;
		m_flags = 0;
	}
	
	public CmsPermissionSet(int allowedPermissions, int deniedPermissions) {
		m_allowed = allowedPermissions;
		m_denied = deniedPermissions;
		m_flags = 0;
	}
	
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
				case 'I': case 'i':
					if (prefix.charAt(0) == '+') m_flags |= I_CmsConstants.C_ACCESSFLAGS_INHERITED;
					if (prefix.charAt(0) == '-') m_flags &= ~I_CmsConstants.C_ACCESSFLAGS_INHERITED;
					break;
			}
		}
	}
	
	public void grantPermissions (int permissions) {
		m_allowed |= permissions;
	}
	
	public void denyPermissions (int permissions) {
		m_denied |= permissions;
	}
	
	public void addPermissions (int allowedPermissions, int deniedPermissions) {
		m_allowed |= allowedPermissions;
		m_denied |= deniedPermissions;
	}
	
	public void setPermissions(int allowedPermissions, int deniedPermissions)  {
		m_allowed = allowedPermissions;
		m_denied = deniedPermissions;
	}
	
	public void addPermissions (CmsPermissionSet permissionSet) {
		m_allowed |= permissionSet.m_allowed;
		m_denied |= permissionSet.m_denied;
	}
	
	public void setPermissions (CmsPermissionSet permissionSet) {
		m_allowed = permissionSet.m_allowed;
		m_denied = permissionSet.m_denied;	
	}
	
	public int getPermissions ()  {
		return m_allowed & ~m_denied;
	}
	
	public int getAllowedPermissions() {
		return m_allowed;
	}
	
	public int getDeniedPermissions(){
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
		
	public String getPermissionString() {
		StringBuffer p = new StringBuffer("");
		p.append(((m_allowed & I_CmsConstants.C_PERMISSION_READ)>0)    ? "+r":""); 
		p.append(((m_allowed & I_CmsConstants.C_PERMISSION_WRITE)>0)   ? "+w":"");
		p.append(((m_allowed & I_CmsConstants.C_PERMISSION_VIEW)>0) ? "+v":"");
		p.append(((m_denied  & I_CmsConstants.C_PERMISSION_READ)>0)    ? "-r":""); 
		p.append(((m_denied  & I_CmsConstants.C_PERMISSION_WRITE)>0)   ? "-w":"");
		p.append(((m_denied  & I_CmsConstants.C_PERMISSION_VIEW)>0) ? "-v":"");
		return p.toString();			
	}
	
	public String toString () {
		return "[PermissionSet:] " + getPermissionString() + ", Flags=" + m_flags;
	}
}
