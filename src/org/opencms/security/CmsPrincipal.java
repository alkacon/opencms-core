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

package org.opencms.security;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.I_CmsGroupNameTranslation;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Common methods shared among user and group principals,
 * also contains several utility functions to deal with principal instances.<p>
 *
 * @since 6.2.0
 */
public abstract class CmsPrincipal implements I_CmsPrincipal, Comparable<I_CmsPrincipal> {

    /** The description of this principal. */
    protected String m_description;

    /** The flags of this principal. */
    protected int m_flags;

    /** The unique id of this principal. */
    protected CmsUUID m_id;

    /** The fully qualified name of this principal. */
    protected String m_name;

    /**
     * Empty constructor for subclassing.<p>
     */
    protected CmsPrincipal() {

        // empty constructor for subclassing
    }

    /**
     * Filters out all principals that do not have the given flag set,
     * but leaving principals with flags less than <code>{@link I_CmsPrincipal#FLAG_CORE_LIMIT}</code> untouched.<p>
     *
     * The given parameter list is directly modified, so the returned list is the same object as the input list.<p>
     *
     * @param principals a list of <code>{@link CmsPrincipal}</code> objects
     * @param flag the flag for filtering
     *
     * @return the filtered principal list
     */
    public static List<? extends CmsPrincipal> filterCoreFlag(List<? extends CmsPrincipal> principals, int flag) {

        Iterator<? extends CmsPrincipal> it = principals.iterator();
        while (it.hasNext()) {
            CmsPrincipal p = it.next();
            if ((p.getFlags() > I_CmsPrincipal.FLAG_CORE_LIMIT) && ((p.getFlags() & flag) != flag)) {
                it.remove();
            }
        }
        return principals;
    }

    /**
     * Filters out all groups with flags greater than <code>{@link I_CmsPrincipal#FLAG_CORE_LIMIT}</code>.<p>
     *
     * The given parameter list is directly modified, so the returned list is the same object as the input list.<p>
     *
     * @param groups a list of <code>{@link CmsGroup}</code> objects
     *
     * @return the filtered principal list
     */
    public static List<CmsGroup> filterCoreGroups(List<CmsGroup> groups) {

        Iterator<CmsGroup> it = groups.iterator();
        while (it.hasNext()) {
            CmsGroup p = it.next();
            if (p.getFlags() > I_CmsPrincipal.FLAG_CORE_LIMIT) {
                it.remove();
            }
        }
        return groups;
    }

    /**
     * Filters out all users with flags greater than <code>{@link I_CmsPrincipal#FLAG_CORE_LIMIT}</code>.<p>
     *
     * The given parameter list is directly modified, so the returned list is the same object as the input list.<p>
     *
     * @param users a list of <code>{@link CmsUser}</code> objects
     *
     * @return the filtered principal list
     */
    public static List<CmsUser> filterCoreUsers(List<CmsUser> users) {

        Iterator<CmsUser> it = users.iterator();
        while (it.hasNext()) {
            I_CmsPrincipal p = it.next();
            if (p.getFlags() > I_CmsPrincipal.FLAG_CORE_LIMIT) {
                it.remove();
            }
        }
        return users;
    }

    /**
     * Filters out all principals that do not have the given flag set.<p>
     *
     * The given parameter list is directly modified, so the returned list is the same object as the input list.<p>
     *
     * @param principals the list of <code>{@link CmsPrincipal}</code> objects
     * @param flag the flag for filtering
     *
     * @return the filtered principal list
     */
    public static List<? extends CmsPrincipal> filterFlag(List<? extends CmsPrincipal> principals, int flag) {

        Iterator<? extends CmsPrincipal> it = principals.iterator();
        while (it.hasNext()) {
            CmsPrincipal p = it.next();
            if ((p.getFlags() & flag) != flag) {
                it.remove();
            }
        }
        return principals;
    }

    /**
     * Returns the provided group name prefixed with <code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}.</code>.<p>
     *
     * @param name the name to add the prefix to
     * @return the provided group name prefixed with <code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}.</code>
     */
    public static String getPrefixedGroup(String name) {

        StringBuffer result = new StringBuffer(name.length() + 10);
        result.append(I_CmsPrincipal.PRINCIPAL_GROUP);
        result.append('.');
        result.append(name);
        return result.toString();
    }

    /**
     * Returns the provided user name prefixed with <code>{@link I_CmsPrincipal#PRINCIPAL_USER}.</code>.<p>
     *
     * @param name the name to add the prefix to
     * @return the provided user name prefixed with <code>{@link I_CmsPrincipal#PRINCIPAL_USER}.</code>
     */
    public static String getPrefixedUser(String name) {

        StringBuffer result = new StringBuffer(name.length() + 10);
        result.append(I_CmsPrincipal.PRINCIPAL_USER);
        result.append('.');
        result.append(name);
        return result.toString();
    }

    /**
     * Utility function to read a prefixed principal from the OpenCms database using the
     * provided OpenCms user context.<p>
     *
     * The principal must be either prefixed with <code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}.</code> or
     * <code>{@link I_CmsPrincipal#PRINCIPAL_USER}.</code>.<p>
     *
     * @param cms the OpenCms user context to use when reading the principal
     * @param name the prefixed principal name
     *
     * @return the principal read from the OpenCms database
     *
     * @throws CmsException in case the principal could not be read
     */
    public static I_CmsPrincipal readPrefixedPrincipal(CmsObject cms, String name) throws CmsException {

        if (CmsGroup.hasPrefix(name)) {
            // this principal is a group
            return cms.readGroup(CmsGroup.removePrefix(name));
        } else if (CmsUser.hasPrefix(name)) {
            // this principal is a user
            return cms.readUser(CmsUser.removePrefix(name));
        }
        // invalid principal name was given
        throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_INVALID_PRINCIPAL_1, name));
    }

    /**
     * Utility function to read a principal by its id from the OpenCms database using the
     * provided OpenCms user context.<p>
     *
     * @param cms the OpenCms user context to use when reading the principal
     * @param id the id of the principal to read
     *
     * @return the principal read from the OpenCms database
     *
     * @throws CmsException in case the principal could not be read
     */
    public static I_CmsPrincipal readPrincipal(CmsObject cms, CmsUUID id) throws CmsException {

        try {
            // first try to read the principal as a user
            return cms.readUser(id);
        } catch (CmsException exc) {
            // assume user does not exist
        }
        try {
            // now try to read the principal as a group
            return cms.readGroup(id);
        } catch (CmsException exc) {
            //  assume group does not exist
        }
        // invalid principal name was given
        throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_INVALID_PRINCIPAL_1, id));
    }

    /**
     * Utility function to read a principal of the given type from the OpenCms database using the
     * provided OpenCms user context.<p>
     *
     * The type must either be <code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}</code> or
     * <code>{@link I_CmsPrincipal#PRINCIPAL_USER}</code>.<p>
     *
     * @param cms the OpenCms user context to use when reading the principal
     * @param type the principal type
     * @param name the principal name
     *
     * @return the principal read from the OpenCms database
     *
     * @throws CmsException in case the principal could not be read
     */
    public static I_CmsPrincipal readPrincipal(CmsObject cms, String type, String name) throws CmsException {

        if (CmsStringUtil.isNotEmpty(type)) {
            String upperCaseType = type.toUpperCase();
            if (PRINCIPAL_GROUP.equals(upperCaseType)) {
                // this principal is a group
                return cms.readGroup(name);
            } else if (PRINCIPAL_USER.equals(upperCaseType)) {
                // this principal is a user
                return cms.readUser(name);
            }
        }
        // invalid principal type was given
        throw new CmsDbEntryNotFoundException(
            Messages.get().container(Messages.ERR_INVALID_PRINCIPAL_TYPE_2, type, name));
    }

    /**
     * Utility function to read a principal by its id from the OpenCms database using the
     * provided OpenCms user context.<p>
     *
     * @param cms the OpenCms user context to use when reading the principal
     * @param id the id of the principal to read
     *
     * @return the principal read from the OpenCms database
     *
     * @throws CmsException in case the principal could not be read
     */
    public static I_CmsPrincipal readPrincipalIncludingHistory(CmsObject cms, CmsUUID id) throws CmsException {

        try {
            // first try to read the principal as a user
            return cms.readUser(id);
        } catch (CmsException exc) {
            // assume user does not exist
        }
        try {
            // now try to read the principal as a group
            return cms.readGroup(id);
        } catch (CmsException exc) {
            //  assume group does not exist
        }
        try {
            // at the end try to read the principal from the history
            return cms.readHistoryPrincipal(id);
        } catch (CmsException exc) {
            //  assume the principal does not exist at all
        }
        // invalid principal name was given
        throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_INVALID_PRINCIPAL_1, id));
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_CmsPrincipal obj) {

        if ((this == obj) || equals(obj)) {
            return 0;
        }
        return getName().compareTo(obj.getName());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof I_CmsPrincipal) {
            if (m_id != null) {
                return m_id.equals(((I_CmsPrincipal)obj).getId());
            }
        }
        return false;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the display name of this principal including the organizational unit.<p>
     *
     * @param cms the cms context
     * @param locale the locale
     *
     * @return the display name of this principal including the organizational unit
     *
     * @throws CmsException if the organizational unit could not be read
     */
    public String getDisplayName(CmsObject cms, Locale locale) throws CmsException {

        return Messages.get().getBundle(locale).key(
            Messages.GUI_PRINCIPAL_DISPLAY_NAME_2,
            getSimpleName(),
            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, getOuFqn()).getDisplayName(locale));
    }

    /**
     * Returns the translated display name of this principal if it is a group and the display name otherwise.<p>
     *
     * @param cms the current CMS context
     * @param locale the locale
     * @param translation the group name translation to use
     *
     * @return the translated display name
     *
     * @throws CmsException if something goes wrong
     */
    public String getDisplayName(CmsObject cms, Locale locale, I_CmsGroupNameTranslation translation)
    throws CmsException {

        if (!isGroup() || (translation == null)) {
            return getDisplayName(cms, locale);
        }
        return Messages.get().getBundle(locale).key(
            Messages.GUI_PRINCIPAL_DISPLAY_NAME_2,
            translation.translateGroupName(getName(), false),
            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, getOuFqn()).getDisplayName(locale));
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#getFlags()
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#getId()
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the fully qualified name of this principal.<p>
     *
     * @return the fully qualified name of this principal
     *
     * @see java.security.Principal#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the fully qualified name of the associated organizational unit.<p>
     *
     * @return the fully qualified name of the associated organizational unit
     */
    public String getOuFqn() {

        return CmsOrganizationalUnit.getParentFqn(m_name);
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#getPrefixedName()
     */
    public String getPrefixedName() {

        if (isUser()) {
            return getPrefixedUser(getName());
        } else if (isGroup()) {
            return getPrefixedGroup(getName());
        }
        return getName();
    }

    /**
     * Returns the simple name of this organizational unit.
     *
     * @return the simple name of this organizational unit.
     */
    public String getSimpleName() {

        return CmsOrganizationalUnit.getSimpleName(m_name);
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
     * @see org.opencms.security.I_CmsPrincipal#isEnabled()
     */
    public boolean isEnabled() {

        return (getFlags() & I_CmsPrincipal.FLAG_DISABLED) == 0;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#isGroup()
     */
    public boolean isGroup() {

        return (this instanceof CmsGroup);
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#isUser()
     */
    public boolean isUser() {

        return (this instanceof CmsUser);
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#setDescription(java.lang.String)
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        if (enabled != isEnabled()) {
            // toggle disabled flag if required
            setFlags(getFlags() ^ I_CmsPrincipal.FLAG_DISABLED);
        }
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#setFlags(int)
     */
    public void setFlags(int value) {

        m_flags = value;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#setName(java.lang.String)
     */
    public void setName(String name) {

        checkName(CmsOrganizationalUnit.getSimpleName(name));
        m_name = name;
    }
}