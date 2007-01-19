/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsOrganizationalUnit.java,v $
 * Date   : $Date: 2007/01/19 16:53:52 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.security;

import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * An organizational unit in OpenCms.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.6 
 */
public class CmsOrganizationalUnit {

    /** The description of this organizational unit. */
    private String m_description;

    /** The flags of this organizational unit. */
    private int m_flags;

    /** The unique id of this organizational unit. */
    private CmsUUID m_id;

    /** The fully qualified name of this organizational unit. */
    private String m_name;

    /**
     * Creates a new OpenCms organizational unit principal.
     * 
     * @param id the unique id of the organizational unit
     * @param fqn the fully qualified name of the this organizational unit (should end with slash)
     * @param description the description of the organizational unit
     * @param flags the flags of the organizational unit
     */
    public CmsOrganizationalUnit(CmsUUID id, String fqn, String description, int flags) {

        m_id = id;
        m_name = fqn;
        m_description = description;
        m_flags = flags;
    }

    /**
     * Returns the parent fully qualified name.<p>
     * 
     * @param fqn the fully qualified name to get the parent from
     * 
     * @return the parent fully qualified name
     */
    public static final String getParentFqn(String fqn) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(fqn) || fqn.equals("/")) {
            // in case of the root ou
            return null;
        }
        int pos;
        if (fqn.endsWith("/")) {
            pos = fqn.substring(0, fqn.length() - 1).lastIndexOf("/");
        } else {
            pos = fqn.lastIndexOf("/");
        }
        if (pos <= 0) {
            // in case of simple names assume root ou 
            return "/";
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
            return fqn.substring(parentFqn.length());
        }
        return fqn;
    }

    /**
     * Checks if the provided organizational unit name is valid and can be used as a name.<p> 
     * 
     * An organizational unit name must not be empty or whitespace only.<p>
     * 
     * @param name the organizational unit name to check
     * 
     * @see org.opencms.security.I_CmsValidationHandler#checkOrganizationalUnitName(String)
     */
    public void checkName(String name) {

        OpenCms.getValidationHandler().checkOrganizationalUnitName(name);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        return new CmsOrganizationalUnit(m_id, m_name, m_description, m_flags);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
     * @return the description of this organizational unit
     */
    public String getDescription() {

        return m_description;
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
     * @return the full qualified name of the parent organizational unit of this organizational unit
     */
    public String getParentFqn() {

        return getParentFqn(m_name);
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
     * @see java.lang.Object#hashCode()
     */
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

        m_description = description;
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
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        if (m_name.length() <= 1) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_ORGUNIT_ROOT_EDITION_0));
        }
        m_name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
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
