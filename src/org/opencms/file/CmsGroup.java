/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsGroup.java,v $
 * Date   : $Date: 2005/06/22 09:39:32 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.I_CmsConstants;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * A group in the OpenCms system.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 */
public class CmsGroup implements I_CmsPrincipal {

    /** The description of the group. */
    private String m_description;

    /** The flags of the group. */
    private int m_flags;

    /** The id of the group. */
    private CmsUUID m_id;

    /** The name of the group. */
    private String m_name;

    /** The parent id of the group. */
    private CmsUUID m_parentId;

    /**
     * Default Constructor.<p> 
     */
    public CmsGroup() {

        // noop
    }

    /**
     * Constructor, creates a new Cms group object.
     * 
     * @param id the id of the group
     * @param parentId the parent group of the group (or C_UNKNOWN_ID)
     * @param name the name of the group
     * @param description the description of the group
     * @param flags the flags of the group    
     */
    public CmsGroup(CmsUUID id, CmsUUID parentId, String name, String description, int flags) {

        m_id = id;
        m_name = name;
        m_description = description;
        m_flags = flags;
        m_parentId = parentId;
    }

    /**
     * Validates a group name.<p>
     * 
     * That means, the parameter should only be composed by digits and standard english letters, points, minus and underscores.<p>
     * 
     * @param name the login to validate
     */
    public static void checkName(String name) {

        String regex = "[\\w\\.~_-]*";
        if (!CmsStringUtil.validateRegex(name, regex, false)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_GROUPNAME_VALIDATION_1, name));
        }
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsGroup(m_id, m_parentId, m_name, m_description, m_flags);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsGroup) {
            return ((CmsGroup)obj).m_id.equals(m_id);
        }
        return false;
    }

    /**
     * Returns the description of this group.<p>
     * 
     * @return the description of this group
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns true if this group is disabled.<p>
     * 
     * @return true if this group is disabled
     */
    public boolean getDisabled() {

        return (m_flags & I_CmsConstants.C_FLAG_DISABLED) != 0;
    }

    /**
     * Returns this groups flags.<p>
     * 
     * @return this groups flags
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the id of this group.<p> 
     * 
     * @return id the id of this group
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the name of this group.<p>
     * 
     * @return name the name of this group
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the parent id of this group, or C_UNKNOWN_ID.<p>
     * 
     * @return the parent id of this group
     */
    public CmsUUID getParentId() {

        return m_parentId;
    }

    /**
     * Returns true if this group is enabled as a project user group.<p> 
     * 
     * @return true if this group is enabled as a project user group 
     */
    public boolean getProjectCoWorker() {

        return (m_flags & I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER) != 0;
    }

    /**
     * Returns true if this group is enabled as a project manager group.<p> 
     * 
     * @return true if this group is enabled as a project manager group
     */
    public boolean getProjectManager() {

        return (m_flags & I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER) != 0;
    }

    /**
     * Returns true if this group is enabled as a role for tasks group.<p>
     * 
     * @return true if this group is enabled as a role for tasks group 
     */
    public boolean getRole() {

        return (m_flags & I_CmsConstants.C_FLAG_GROUP_ROLE) != 0;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_id != null) {
            return m_id.hashCode();
        }
        return CmsUUID.getNullUUID().hashCode();
    }

    /**
     * Returns the enabled flag.<p>
     * 
     * It should replace the <code>{@link #getDisabled()}</code> method.<p> 
     * 
     * @return the enabled flag
     */
    public boolean isEnabled() {

        return (getFlags() & I_CmsConstants.C_FLAG_DISABLED) == 0;
    }

    /**
     * Sets the description of this group.<p>
     * 
     * @param description the description of this group
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Disables this group by setting the C_FLAG_DISABLED flag.<p>
     */
    public void setDisabled() {

        if (!getDisabled()) {
            setFlags(getFlags() ^ I_CmsConstants.C_FLAG_DISABLED);
        }
    }

    /**
     * Enables this group by setting the C_FLAG_ENABLED flag.<p>
     */
    public void setEnabled() {

        if (getDisabled()) {
            setFlags(getFlags() ^ I_CmsConstants.C_FLAG_DISABLED);
        }
    }

    /**
     * Sets the enabled flag.<p>
     * 
     * It should replace the <code>{@link #setDisabled()}</code> and 
     * the <code>{@link #setEnabled()}</code> methods.<p> 
     * 
     * @param enabled the enabled flag
     */
    public void setEnabled(boolean enabled) {

        if (enabled) {
            if (getDisabled()) {
                setFlags(getFlags() ^ I_CmsConstants.C_FLAG_DISABLED);
            }
        } else {
            if (!getDisabled()) {
                setFlags(getFlags() ^ I_CmsConstants.C_FLAG_DISABLED);
            }
        }
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        checkName(name);
        m_name = name;
    }

    /**
     * Sets the parent group id of this group.<p>
     * 
     * @param parentId the parent group id to set
     */
    public void setParentId(CmsUUID parentId) {

        m_parentId = parentId;
    }

    /**
     * Sets the project user flag for this group to the given value.<p>
     * 
     * @param value the value to set
     */
    public void setProjectCoWorker(boolean value) {

        if (getProjectCoWorker() != value) {
            setFlags(getFlags() ^ I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER);
        }
    }

    /**
     * Sets the project manager flag for this group to the given value.<p>
     * 
     * @param value the value to set
     */
    public void setProjectManager(boolean value) {

        if (getProjectManager() != value) {
            setFlags(getFlags() ^ I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER);
        }
    }

    /**
     * Sets the "role for tasks" flag for this group to the given value.<p>
     * 
     * @param value the value to set
     */
    public void setRole(boolean value) {

        if (getRole() != value) {
            setFlags(getFlags() ^ I_CmsConstants.C_FLAG_GROUP_ROLE);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Group]");
        result.append(" name:");
        result.append(m_name);
        result.append(" id:");
        result.append(m_id);
        result.append(" description:");
        result.append(m_description);
        return result.toString();
    }

    /**
     * Sets this groups flags.<p>
     * 
     * @param flags the flags to set
     */
    void setFlags(int flags) {

        m_flags = flags;
    }
}