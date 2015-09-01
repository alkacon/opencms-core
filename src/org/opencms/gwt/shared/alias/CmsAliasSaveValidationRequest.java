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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The class used for transmitting alias data to the server for serving.<p>
 */
public class CmsAliasSaveValidationRequest extends CmsAliasEditValidationRequest {

    /** The set of structure ids of deleted aliases. */
    private Set<CmsUUID> m_deletedIds = new HashSet<CmsUUID>();

    /** The list of rewrite aliases to save. */
    private List<CmsRewriteAliasTableRow> m_rewriteData;

    /** The site root. */
    private String m_siteRoot;

    /**
     * Default constructor.<p>
     */
    public CmsAliasSaveValidationRequest() {

        super();
    }

    /**
     * Gets the set of structure ids of deleted aliases.<p>
     *
     * @return the set of structure ids of deleted aliases
     */
    public Set<CmsUUID> getDeletedIds() {

        return m_deletedIds;
    }

    /**
     * Gets the list of rewrite aliases to save.<p>
     *
     * @return the rewrite aliases to save
     */
    public List<CmsRewriteAliasTableRow> getRewriteData() {

        return m_rewriteData;
    }

    /**
     * Gets the site root.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Sets the list of rewrite aliases to save.<p>
     *
     * @param rewriteData the list of rewrite aliases to save
     */
    public void setRewriteData(List<CmsRewriteAliasTableRow> rewriteData) {

        m_rewriteData = new ArrayList<CmsRewriteAliasTableRow>(rewriteData);
    }

    /**
     * Sets the site root.<p>
     *
     * @param siteRoot the site root
     */
    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

}
