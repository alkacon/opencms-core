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

package org.opencms.security;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Locale;

/**
 * An organizational unit in OpenCms.<p>
 *
 * Be sure the flags does not conflict with the flags defined in {@link org.opencms.file.CmsResource}.<p>
 *
 * @since 6.5.6
 */
public class CmsOrganizationalUnit {

    /** The flag constant to hide the organizational units from the login form. */
    public static final int FLAG_HIDE_LOGIN = 1;

    /** The flag constant to mark the organizational units as containing only webusers. */
    public static final int FLAG_WEBUSERS = 8;

    /** The character used to separate each level in a fully qualified name. */
    public static final String SEPARATOR = "/";

    /** The description of this organizational unit. */
    private String m_description;

    /** The flags of this organizational unit. */
    private int m_flags;

    /** The unique id of this organizational unit. */
    private final CmsUUID m_id;

    /** The fully qualified name of this organizational unit. */
    private final String m_name;

    /** The id of the related default project. */
    private final CmsUUID m_projectId;

    /**
     * Creates a new OpenCms organizational unit principal.
     *
     * @param id the unique id of the organizational unit
     * @param fqn the fully qualified name of the this organizational unit (should end with slash)
     * @param description the description of the organizational unit
     * @param flags the flags of the organizational unit
     * @param projectId the id of the related default project
     */
    public CmsOrganizationalUnit(CmsUUID id, String fqn, String description, int flags, CmsUUID projectId) {

        m_id = id;
        m_name = fqn;
        m_description = description;
        m_flags = flags;
        m_projectId = projectId;
    }

    /**
     * Returns the parent fully qualified name.<p>
     *
     * This is <code>null</code> for the root ou, and
     * the empty string for first level ous.<p>
     *
     * @param fqn the fully qualified name to get the parent from
     *
     * @return the parent fully qualified name
     */
    public static final String getParentFqn(String fqn) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(fqn)) {
            // in case of the root ou
            return null;
        }
        int pos;
        if (fqn.endsWith(CmsOrganizationalUnit.SEPARATOR)) {
            pos = fqn.substring(0, fqn.length() - 1).lastIndexOf(CmsOrganizationalUnit.SEPARATOR);
        } else {
            pos = fqn.lastIndexOf(CmsOrganizationalUnit.SEPARATOR);
        }
        if (pos <= 0) {
            // in case of simple names assume root ou
            return "";
        }
        return fqn.substring(0, pos + 1);
    }

    /**
     * Returns the last name of the given fully qualified name.<p>
     *
     * @param fqn the fully qualified name to get the last name from
     *
     * @return the last name of the given fully qualified name
     */
    public static final String getSimpleName(String fqn) {

        String parentFqn = getParentFqn(fqn);
        if (parentFqn != null) {
            fqn = fqn.substring(parentFqn.length());
        }
        if ((fqn != null) && fqn.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
            fqn = fqn.substring(CmsOrganizationalUnit.SEPARATOR.length());
        }
        return fqn;
    }

    /**
     * Prefixes a simple name with an OU.<p>
     *
     * @param ou the OU to use as a prefix
     * @param principal the simple name to which the OU should be prepended
     *
     * @return the FQN
     */
    public static String prefixWithOu(String ou, String principal) {

        String result = CmsStringUtil.joinPaths(ou, principal);
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    /**
     * Returns the given fully qualified name without leading separator.<p>
     *
     * @param fqn the fully qualified name to fix
     *
     * @return the given fully qualified name without leading separator
     */
    public static String removeLeadingSeparator(String fqn) {

        if ((fqn != null) && fqn.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
            return fqn.substring(1);
        }
        return fqn;
    }

    /**
     * Adds the given flag to the flags for this organizational unit.<p>
     *
     * @param flag the flag to add
     */
    public void addFlag(int flag) {

        m_flags = (m_flags ^ flag);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        return new CmsOrganizationalUnit(m_id, m_name, m_description, m_flags, m_projectId);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsOrganizationalUnit) {
            if (m_id != null) {
                return m_id.equals(((CmsOrganizationalUnit)obj).getId());
            }
        }
        return false;
    }

    /**
     * Returns the description of this organizational unit.<p>
     *
     * This could return also just a macro, so please use the
     * {@link #getDescription(Locale)} method.<p>
     *
     * @return the description of this organizational unit
     */
    public String getDescription() {

        return m_description;
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
     * Returns the display name for this organizational unit.<p>
     *
     * @param locale the locale
     *
     * @return the display name for this organizational unit
     */
    public String getDisplayName(Locale locale) {

        if (getParentFqn() == null) {
            // for the root ou
            return getDescription(locale);
        }
        return Messages.get().getBundle(locale).key(
            Messages.GUI_ORGUNIT_DISPLAY_NAME_2,
            getDescription(locale),
            CmsOrganizationalUnit.SEPARATOR + getName());
    }

    /**
     * Returns the flags of this organizational unit.<p>
     *
     * The organizational unit flags are used to store special information about the
     * organizational unit state encoded bitwise. Usually the flags int value should not
     * be directly accessed. <p>
     *
     * @return the flags of this organizational unit
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the id of this organizational unit.
     *
     * @return the id of this organizational unit.
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the fully qualified name of this organizational unit.<p>
     *
     * @return the fully qualified name of this organizational unit
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the full qualified name of the parent organizational unit of this organizational unit.<p>
     *
     * This is <code>null</code> for the root ou, and the empty string for first level ous.<p>
     *
     * @return the full qualified name of the parent organizational unit of this organizational unit
     */
    public String getParentFqn() {

        return getParentFqn(m_name);
    }

    /**
     * Returns the id of the related default project.<p>
     *
     * @return the id of the related default project
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the simple name of this organizational unit.
     *
     * @return the simple name of this organizational unit.
     */
    public String getSimpleName() {

        return getSimpleName(m_name);
    }

    /**
     * Checks if this organizational unit has the given flag set.<p>
     *
     * @param flag the flag to check
     *
     * @return <code>true</code> if this organizational unit has the given flag set
     */
    public boolean hasFlag(int flag) {

        return (m_flags & flag) == flag;
    }

    /**
     * Checks if this organizational unit has the "hide from login form" flag set.<p>
     *
     * @return <code>true</code> if this organizational unit has the "hide from login form" flag set
     */
    public boolean hasFlagHideLogin() {

        return hasFlag(FLAG_HIDE_LOGIN);
    }

    /**
     * Checks if this organizational unit has the "webusers" flag set.<p>
     *
     * @return <code>true</code> if this organizational unit has the "webusers" flag set
     */
    public boolean hasFlagWebuser() {

        return hasFlag(FLAG_WEBUSERS);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_id != null) {
            return m_id.hashCode();
        }
        return CmsUUID.getNullUUID().hashCode();
    }

    /**
     * Sets the description of this organizational unit.<p>
     *
     * @param description the principal organizational unit to set
     */
    public void setDescription(String description) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(description)) {
            throw new CmsIllegalArgumentException(
                org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_BAD_OU_DESCRIPTION_EMPTY_0));
        }

        m_description = description;
    }

    /**
     * Sets the "hide from login form" flag.<p>
     */
    public void setFlagHideLogin() {

        addFlag(FLAG_HIDE_LOGIN);
    }

    /**
     * Sets this organizational unit flags to the specified value.<p>
     *
     * The organizational unit flags are used to store special information about the
     * organizational units state encoded bitwise. Usually the flags int value should not
     * be directly accessed. <p>
     *
     * @param value the value to set this organizational units flags to
     */
    public void setFlags(int value) {

        m_flags = value;
    }

    /**
     * Sets the "webusers" flag.<p>
     */
    public void setFlagWebusers() {

        addFlag(FLAG_WEBUSERS);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Organizational Unit]");
        result.append(" fqn:");
        result.append(getName());
        result.append(" id:");
        result.append(m_id);
        result.append(" description:");
        result.append(m_description);
        return result.toString();
    }
}
