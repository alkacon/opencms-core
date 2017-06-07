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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Locale;

/**
 * A group principal in the OpenCms permission system.<p>
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
     * Checks if the given String starts with {@link I_CmsPrincipal#PRINCIPAL_GROUP} followed by a dot.<p>
     *
     * <ul>
     * <li>Works if the given String is <code>null</code>.
     * <li>Removes white spaces around the String before the check.
     * <li>Also works with prefixes not being in upper case.
     * <li>Does not check if the group after the prefix actually exists.
     * </ul>
     *
     * @param principalName the group name to check
     *
     * @return <code>true</code> in case the String starts with {@link I_CmsPrincipal#PRINCIPAL_GROUP}
     */
    public static boolean hasPrefix(String principalName) {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(principalName)
            && (principalName.trim().toUpperCase().startsWith(I_CmsPrincipal.PRINCIPAL_GROUP + "."));
    }

    /**
     * Removes the prefix if the given String starts with {@link I_CmsPrincipal#PRINCIPAL_GROUP} followed by a dot.<p>
     *
     * <ul>
     * <li>Works if the given String is <code>null</code>.
     * <li>If the given String does not start with {@link I_CmsPrincipal#PRINCIPAL_GROUP} followed by a dot it is returned unchanged.
     * <li>Removes white spaces around the group name.
     * <li>Also works with prefixes not being in upper case.
     * <li>Does not check if the group after the prefix actually exists.
     * </ul>
     *
     * @param principalName the group name to remove the prefix from
     *
     * @return the given String with the prefix {@link I_CmsPrincipal#PRINCIPAL_GROUP} with the following dot removed
     */
    public static String removePrefix(String principalName) {

        String result = principalName;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(principalName)) {
            if (hasPrefix(principalName)) {
                result = principalName.trim().substring(I_CmsPrincipal.PRINCIPAL_GROUP.length() + 1);
            }
        }
        return result;
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
     * Sets the parent group id of this group.<p>
     *
     * @param parentId the parent group id to set
     */
    public void setParentId(CmsUUID parentId) {

        m_parentId = parentId;
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