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

/**
 * This class represents an alias which does not just map a fixed path to a fixed resource, but instead uses
 * a regular expression substitution to determine the target path.<p>
 */
public class CmsRewriteAlias {

    /** The id of the alias. */
    private CmsUUID m_id;

    /** The alias mode. */
    private CmsAliasMode m_mode;

    /** The regular expression string used for matching. */
    private String m_patternString;

    /** The replacement string used when the regular expression matches. */
    private String m_replacementString;

    /** The site root inside which this alias should be valid. */
    private String m_siteRoot;

    /**
     * Creates a new instance.<p>
     *
     * @param id the id of the alias
     * @param siteRoot the site root inside which the alias is valid
     * @param patternString the regular expression used for matching the URI
     * @param replacementString the replacement string used when the URI is matched
     * @param mode the alias mode
     */
    public CmsRewriteAlias(
        CmsUUID id,
        String siteRoot,
        String patternString,
        String replacementString,
        CmsAliasMode mode) {

        m_id = id;
        m_patternString = patternString;
        m_replacementString = replacementString;
        m_siteRoot = siteRoot;
        m_mode = mode;
    }

    /**
     * Gets the id of the alias.<p>
     *
     * @return the id of the alias
     */
    public CmsUUID getId() {

        return m_id;
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
     * Gets the regular expression string.<p>
     *
     * @return the regular expression string
     */
    public String getPatternString() {

        return m_patternString;
    }

    /**
     * Gets the string used to replace the string matching the regex.<p>
     *
     * @return the replacement string
     */
    public String getReplacementString() {

        return m_replacementString;
    }

    /**
     * Gets the root of the site in which this alias is valid.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

}
