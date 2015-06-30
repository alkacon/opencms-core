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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The class used to transmit the original alias list when the alias editor dialog is first loaded.<p>
 */
public class CmsAliasInitialFetchResult implements IsSerializable {

    /** The alias lock owner name (null if the current user is lock owner). */
    private String m_aliasLockOwner;

    /** The list of aliases. */
    private List<CmsAliasTableRow> m_aliasRows;

    /** The alias download URL. */
    private String m_downloadUrl;

    /** The initial list of rewrite aliases. */
    private List<CmsRewriteAliasTableRow> m_rewriteAliases = new ArrayList<CmsRewriteAliasTableRow>();

    /**
     * Gets the alias lock owner.<p>
     *
     * This will return null if the current user is the lock owner.<p>
     *
     * @return the alias lock owner
     */
    public String getAliasTableLockOwner() {

        return m_aliasLockOwner;
    }

    /**
     * Gets the alias download URL.<p>
     *
     * @return the alias download URL
     */
    public String getDownloadUrl() {

        return m_downloadUrl;
    }

    /**
     * Gets the list of rewrite aliases.<p>
     *
     * @return the list of rewrite aliases
     */
    public List<CmsRewriteAliasTableRow> getRewriteAliases() {

        return m_rewriteAliases;
    }

    /**
     * Gets the alias table rows.<p>
     *
     * @return the alias table rows
     */
    public List<CmsAliasTableRow> getRows() {

        return m_aliasRows;
    }

    /**
     * Sets the alias lock owner name.<p>
     *
     * @param name the alias lock owner name
     */
    public void setAliasLockOwner(String name) {

        m_aliasLockOwner = name;
    }

    /**
     * Sets the download URL for aliases.<p>
     *
     * @param downloadUrl the download URL for aliases
     */
    public void setDownloadUrl(String downloadUrl) {

        m_downloadUrl = downloadUrl;

    }

    /**
     * Sets the initial list of rewrite aliases.<p>
     *
     * @param rows the list of rewrite aliases
     */
    public void setRewriteRows(List<CmsRewriteAliasTableRow> rows) {

        m_rewriteAliases = rows;
    }

    /**
     * Sets the alias table rows.<p>
     *
     * @param rows the alias table rows
     */
    public void setRows(List<CmsAliasTableRow> rows) {

        m_aliasRows = rows;
    }

}
