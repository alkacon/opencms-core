/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.util.CmsUUID;

import java.util.regex.Pattern;

import com.google.common.base.Objects;

/**
 * This class represents an alias from a virtual path to a resource in the VFS.<p>
 */
public class CmsAlias {

    /** The regular expression which describes valid alias paths:
     * one or more segments, each consisting of a slash at the front followed
     * by one or more 'unreserved characters' for URIs (see RFC 2396).
     */
    public static final Pattern ALIAS_PATTERN = Pattern.compile("(?:/[a-zA-Z0-9-_\\.!~\\*\\'\\(\\)]+)+"); //$NON-NLS-1$

    /** The alias path. */
    protected String m_aliasPath;

    /** The alias mode. */
    protected CmsAliasMode m_mode;

    /** The site root for the alias. */
    protected String m_siteRoot;

    /** The structure id of the aliased page. */
    protected CmsUUID m_structureId;

    /**
     * Creates a new alias.<p>
     *
     * @param structureId the structure id of the aliased page
     * @param siteRoot the site root of the alias
     * @param aliasPath the alias path
     * @param mode the alias mode
     */
    public CmsAlias(CmsUUID structureId, String siteRoot, String aliasPath, CmsAliasMode mode) {

        m_aliasPath = aliasPath;
        m_structureId = structureId;
        m_siteRoot = siteRoot;
        m_mode = mode;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        if (!(other instanceof CmsAlias)) {
            return false;
        }
        CmsAlias otherAlias = (CmsAlias)other;
        return Objects.equal(m_aliasPath, otherAlias.m_aliasPath)
            && Objects.equal(m_siteRoot, otherAlias.m_siteRoot)
            && Objects.equal(m_structureId, otherAlias.m_structureId)
            && Objects.equal(m_mode, otherAlias.m_mode);
    }

    /**
     * Gets the alias path.<p>
     *
     * @return the alias path
     */
    public String getAliasPath() {

        return m_aliasPath;
    }

    /**
     * Gets the alias mode.<p>
     *
     * @return the alias mode
     */
    public CmsAliasMode getMode() {

        return m_mode;
    }

    /**
     * Gets the alias site root.<p>
     *
     * @return the alias site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the structure id of the aliased resource.<p>
     *
     * @return the structure id of the aliased resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(m_aliasPath, m_siteRoot, m_mode, m_structureId);
    }

    /**
     * Checks whether the mode of the alias is 'permanent redirect'.<p>
     *
     * @return true if the mode of the alias is 'permanent redirect'
     */
    public boolean isPermanentRedirect() {

        return m_mode.equals(CmsAliasMode.permanentRedirect);
    }

    /**
     * Checks whether the mode of the alias is a redirect type (permanent or temporary).<p>
     *
     * @return true if the mode of the alias is a redirect type
     */
    public boolean isRedirect() {

        return m_mode.equals(CmsAliasMode.permanentRedirect) || m_mode.equals(CmsAliasMode.redirect);
    }

}
