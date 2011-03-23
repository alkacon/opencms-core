/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsGroup.java,v $
 * Date   : $Date: 2011/03/23 14:51:11 $
 * Version: $Revision: 1.29 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;

import java.util.Locale;

/**
 * A group principal in the OpenCms permission system.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.29 $
 * 
 * @since 6.0.0 
 * 
 * @see CmsUser
 */
public class CmsGroup extends CmsPrincipal {

    /** The parent id of the group. */
    private CmsUUID m_parentId;

    /**
     * Creates a new, empty OpenCms group principal.
     */
    public CmsGroup() {

        // noop
    }

    /**
     * Creates a new OpenCms group principal.
     * 
     * @param id the unique id of the group
     * @param parentId the is of the parent group
     * @param name the fully qualified name of the name of the group
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
     * Checks if the provided group name is valid and can be used as an argument value 
     * for {@link #setName(String)}.<p> 
     * 
     * A group name must not be empty or whitespace only.<p>
     * 
     * @param name the group name to check
     * 
     * @see org.opencms.security.I_CmsValidationHandler#checkGroupName(String)
     */
    public void checkName(String name) {

        OpenCms.getValidationHandler().checkGroupName(name);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        return new CmsGroup(m_id, m_parentId, m_name, m_description, m_flags);
    }

    /**
     * Returns the description of this organizational unit.<p>
     *
     * @param locale the locale
     *
     * @return the description of this organizational unit
     */
    public String getDescription(Locale locale) {

        CmsMacroResolver macroResolver = new CmsMacroResolver();
        macroResolver.setMessages(org.opencms.db.generic.Messages.get().getBundle(locale));
        return macroResolver.resolveMacros(m_description);
    }

    /**
     * Returns true if this group is disabled.<p>
     * 
     * @return true if this group is disabled
     * 
     * @deprecated use {@link CmsPrincipal#isEnabled()} instead
     */
    public boolean getDisabled() {

        return !isEnabled();
    }

    /**
     * Returns the parent group id of this group.<p>
     * 
     * @return the parent group id of this group
     */
    public CmsUUID getParentId() {

        return m_parentId;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#isGroup()
     */
    @Override
    public boolean isGroup() {

        return true;
    }

    /**
     * Returns <code>true</code> if this group is enabled as a project user group.<p> 
     * 
     * @return <code>true</code> if this group is enabled as a project user group 
     */
    public boolean isProjectCoWorker() {

        return (getFlags() & I_CmsPrincipal.FLAG_GROUP_PROJECT_USER) == I_CmsPrincipal.FLAG_GROUP_PROJECT_USER;
    }

    /**
     * Returns <code>true</code> if this group is enabled as a project manager group.<p> 
     * 
     * @return <code>true</code> if this group is enabled as a project manager group
     */
    public boolean isProjectManager() {

        return (getFlags() & I_CmsPrincipal.FLAG_GROUP_PROJECT_MANAGER) == I_CmsPrincipal.FLAG_GROUP_PROJECT_MANAGER;
    }

    /**
     * Checks if this group is a role group.<p>
     * 
     * @return <code>true</code> if this group is a role group
     */
    public boolean isRole() {

        return (getFlags() & I_CmsPrincipal.FLAG_GROUP_ROLE) == I_CmsPrincipal.FLAG_GROUP_ROLE;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#isUser()
     */
    @Override
    public boolean isUser() {

        return false;
    }

    /**
     * Checks if this group is a virtual group, emulating a role.<p>
     * 
     * @return if this group is a virtual group
     */
    public boolean isVirtual() {

        return (getFlags() & I_CmsPrincipal.FLAG_GROUP_VIRTUAL) == I_CmsPrincipal.FLAG_GROUP_VIRTUAL;
    }

    /**
     * Disables this group.<p>
     * 
     * @deprecated use {@link CmsPrincipal#setEnabled(boolean)} instead
     */
    public void setDisabled() {

        setEnabled(false);
    }

    /**
     * Enables this group.<p>
     * 
     * @deprecated use {@link CmsPrincipal#setEnabled(boolean)} instead
     */
    public void setEnabled() {

        setEnabled(true);
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

        if (isProjectCoWorker() != value) {
            setFlags(getFlags() ^ I_CmsPrincipal.FLAG_GROUP_PROJECT_USER);
        }
    }

    /**
     * Sets the project manager flag for this group to the given value.<p>
     * 
     * @param value the value to set
     */
    public void setProjectManager(boolean value) {

        if (isProjectManager() != value) {
            setFlags(getFlags() ^ I_CmsPrincipal.FLAG_GROUP_PROJECT_MANAGER);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Group]");
        result.append(" name:");
        result.append(getName());
        result.append(" id:");
        result.append(m_id);
        result.append(" description:");
        result.append(m_description);
        return result.toString();
    }
}