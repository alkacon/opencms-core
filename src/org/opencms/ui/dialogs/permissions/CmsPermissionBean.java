/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.dialogs.permissions;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

/**
 * Bean for permissions which have changed.<p>
 */
public class CmsPermissionBean {
    /** Logger for this class. */ 
    private static final Log LOG = CmsLog.getLog(CmsPermissionBean.class);

    /**Principal Type. */
    private String m_principalType;

    /**Principal Name. */
    private String m_principalName;

    /**Allowed state. */
    private int m_allowed;

    /**Denied state. */
    private int m_denied;

    /**Flags. */
    private int m_flags;

    /**Permission string. */
    private String m_permissionString;

    /**Should the permission be deleted? */
    private boolean m_delete;

    /**
     * Constructor for delete permission.<p>
     *
     * @param principalType principal type
     * @param principalName principal name
     */
    public CmsPermissionBean(String principalType, String principalName) {

        m_principalName = principalName;
        m_principalType = principalType;
        m_delete = true;
    }

    /**
     * Constructor for new or edited permission.<p>
     *
     * @param principalType principal type
     * @param principalName principal name
     * @param allowed int
     * @param denied int
     * @param flags int
     */
    public CmsPermissionBean(String principalType, String principalName, int allowed, int denied, int flags) {

        m_principalName = principalName;
        m_principalType = principalType;
        m_allowed = allowed;
        m_denied = denied;
        m_flags = flags;
        m_delete = false;
    }

    /**
     * Constructor with permission string.<p>
     *
     * @param principalType type
     * @param principalName name
     * @param permissionString permission string
     */
    public CmsPermissionBean(String principalType, String principalName, String permissionString) {

        m_principalName = principalName;
        m_principalType = principalType;
        m_permissionString = permissionString;
    }

    /**
     * Gets the bean for principal from list of beans.<p>
     *
     * @param beans to look principal up
     * @param principalName name of principal to get bean of
     * @return CmsPermissionBean
     */
    public static CmsPermissionBean getBeanForPrincipal(Set<CmsPermissionBean> beans, String principalName) {

        for (CmsPermissionBean bean : beans) {
            if (bean.getPrincipalName().equals(principalName)) {
                return bean;
            }
        }
        return null;
    }

    /**
     * Get name of principal from ACE.<p>
     *
     * @param cms CmsObject
     * @param entry ACE
     * @return principal name
     */
    public static String getPrincipalNameFromACE(CmsObject cms, CmsAccessControlEntry entry) {

        if (entry.isAllOthers()) {
            return CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME;
        }
        if (entry.isOverwriteAll()) {
            return CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME;
        }
        CmsRole role = CmsRole.valueOfId(entry.getPrincipal());
        if (role != null) {
            return role.getRoleName();
        } else {
            try {
                return CmsPrincipal.readPrincipal(cms, entry.getPrincipal()).getName();
            } catch (CmsException e) {
                //
            }
        }
        return "";
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if (!((o instanceof CmsPermissionBean) || (o instanceof String))) {
            return false;
        }
        if (o instanceof String) {
            return m_principalName.equals(o);
        }
        CmsPermissionBean bean = (CmsPermissionBean)o;
        return bean.getPrincipalName().equals(m_principalName) && bean.getPrincipalType().equals(m_principalType);
    }

    /**
     * Gets the allowed flag.<p>
     *
     * @return int
     */
    public int getAllowed() {

        return m_allowed;
    }

    /**
     * Gets the denied flag.<p>
     *
     * @return int
     */
    public int getDenied() {

        return m_denied;
    }

    /**
     * Gets the flag.<p>
     *
     * @return int
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the permission string.<p>
     *
     * @return the permission string or null if not set
     */
    public String getPermissionString() {

        return m_permissionString;
    }

    /**
     * Gets the principal name.<p>
     *
     * @return the name of the principal
     */
    public String getPrincipalName() {

        return m_principalName;
    }

    /**
     * Gets the type of the principal.<p>
     *
     * @return String
     */
    public String getPrincipalType() {

        return m_principalType;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 31).append(m_principalName).toHashCode();
    }

    /**
     * Returns whether the permission should be removed.<p>
     *
     * @return true-> permission will be removed
     */
    public boolean isDeleted() {

        return m_delete;
    }

    /**
     * Checks if principal is real.<p>
     *
     * @return true if principal is real
     */
    public boolean isRealPrinciple() {

        return !(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME.equals(m_principalName)
            | CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME.equals(m_principalName));
    }

    /**
     * Sets the flag of the ACE.<p>
     *
     * @param flags to be set
     */
    public void setFlags(int flags) {

        m_flags |= flags;

    }

    /**
     * Creates ACE from bean.<p>
     *
     * @param cms CmsObject
     * @param resID id of resource
     * @return CmsAccessControlEntry
     */
    public CmsAccessControlEntry toAccessControlEntry(CmsObject cms, CmsUUID resID) {

        CmsUUID id = null;
        if (isRealPrinciple()) {
            if (CmsRole.PRINCIPAL_ROLE.equals(m_principalType)) {
                CmsRole role = CmsRole.valueOfRoleName(m_principalName);
                if (role != null) {
                    id = role.getId();
                }
            } else {
                try {
                    if (I_CmsPrincipal.PRINCIPAL_GROUP.equals(m_principalType)) {
                        id = cms.readGroup(m_principalName).getId();
                    } else if (I_CmsPrincipal.PRINCIPAL_USER.equals(m_principalType)) {
                        id = cms.readUser(m_principalName).getId();
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        } else {
            if (m_principalName.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME)) {
                id = CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID;
            }
            if (m_principalName.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME)) {
                id = CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID;
            }
        }

        if (id == null) {
            return null;
        }

        if (m_permissionString == null) {
            return new CmsAccessControlEntry(resID, id, m_allowed, m_denied, m_flags);
        }
        CmsAccessControlEntry entry = new CmsAccessControlEntry(resID, id, m_permissionString);
        return entry;
    }

}