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

import org.opencms.file.CmsObject;
import org.opencms.workplace.I_CmsGroupNameTranslation;

import java.util.Locale;

/**
 * Wrapper around CmsRole objects so they can be used as principals.
 */
public class CmsRoleAsPrincipal extends CmsPrincipal {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The wrapped role. */
    private CmsRole m_role;

    /**
     * Creates a new instane.
     *
     * @param role the wrapped role
     */
    public CmsRoleAsPrincipal(CmsRole role) {

        m_role = role;
        m_name = role.getRoleName();
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#checkName(java.lang.String)
     */
    public void checkName(String name) {

        // do nothing
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {

        return m_role.getDescription(locale);
    }

    /**
     * @see org.opencms.security.CmsPrincipal#getDisplayName(org.opencms.file.CmsObject, java.util.Locale)
     */
    @Override
    public String getDisplayName(CmsObject cms, Locale locale) {

        return m_role.getName(locale);
    }

    /**
     * @see org.opencms.security.CmsPrincipal#getDisplayName(org.opencms.file.CmsObject, java.util.Locale, org.opencms.workplace.I_CmsGroupNameTranslation)
     */
    @Override
    public String getDisplayName(CmsObject cms, Locale locale, I_CmsGroupNameTranslation translation) {

        return getDisplayName(cms, locale);
    }

    /**
     * Gets the wrapped role.
     *
     * @return the wrapped role
     */
    public CmsRole getRole() {

        return m_role;
    }

}
