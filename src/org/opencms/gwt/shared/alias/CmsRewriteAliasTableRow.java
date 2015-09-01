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

package org.opencms.gwt.shared.alias;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A class containing the data for a row of the rewrite alias table.<p>
 */
public class CmsRewriteAliasTableRow implements IsSerializable {

    /** The error message for this rewrite alias. */
    private String m_error;

    /** The id of the alias. */
    private CmsUUID m_id;

    /** The alias mode. */
    private CmsAliasMode m_mode;

    /** The regular expression string used for matching. */
    private String m_patternString;

    /** The replacement string used when the regular expression matches. */
    private String m_replacementString;

    /**
     * Default constructor, used for serialization.<p>
     */
    public CmsRewriteAliasTableRow() {

        // nothing
    }

    /**
     * Creates a new instance.<p>
     *
     * @param id the id of the alias
     * @param patternString the regular expression used for matching the URI
     * @param replacementString the replacement string used when the URI is matched
     * @param mode the alias mode for this row
     */
    public CmsRewriteAliasTableRow(CmsUUID id, String patternString, String replacementString, CmsAliasMode mode) {

        m_id = id;
        m_patternString = patternString;
        m_replacementString = replacementString;
        m_mode = mode;
    }

    /**
     * Gets the error message for this row.<p>
     *
     * @return the error message for this row
     */
    public String getError() {

        return m_error;
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
     * Gets the alias mode for this row.<p>
     *
     * @return the alias mode for this row
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
     * Sets the error message for this row.<p>
     *
     * @param error the new error message
     */
    public void setError(String error) {

        m_error = error;
    }

    /**
     * Sets the id of this row.<p>
     *
     * @param id the new id
     */
    public void setId(CmsUUID id) {

        m_id = id;
    }

    /**
     * Sets the mode of this row.<p>
     *
     * @param mode the new mode
     */
    public void setMode(CmsAliasMode mode) {

        m_mode = mode;
    }

    /**
     * Sets the pattern of this row.<p>
     *
     * @param patternString the new pattern
     */
    public void setPatternString(String patternString) {

        m_patternString = patternString;
    }

    /**
     * Sets the replacement string for this row.<p>
     *
     * @param replacementString the new replacement string
     */
    public void setReplacementString(String replacementString) {

        m_replacementString = replacementString;
    }

}
